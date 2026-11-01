package com.example.docscanner.util

import android.content.Context
import android.graphics.Bitmap
import com.example.docscanner.model.FilterType
import com.example.docscanner.model.ImageQuality
import com.example.docscanner.model.ScannedPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Renders a [ScannedPage] into real pixels and stores the result.
 *
 * **Non-destructive editing:** every bake starts from `originalImageUri` (the
 * pristine capture) and applies the page's current transform parameters. The
 * output replaces `imageUri` with a `_edited.jpg` bake while the original is kept
 * for the next edit. Re-editing therefore never compounds JPEG loss, and because
 * the parameters are re-applied from scratch each time, a page can be re-warped
 * or re-filtered indefinitely.
 *
 * Pipeline order matters: **warp → rotate → colour filter → signature overlay**.
 * The signature is composited last so it is never perspective-distorted or
 * desaturated by the document enhancement.
 */
object PageRenderer {

    /**
     * Bakes [page] and returns an updated copy with `imageUri` pointing at the
     * new file and `originalImageUri` pointing at the pristine source.
     * Blocking — call from `Dispatchers.IO`.
     *
     * @return the updated page, or `null` if the source bitmap could not be read.
     */
    fun bakeBlocking(
        context: Context,
        page: ScannedPage,
        jpegQuality: Int = ImageStore.JPEG_QUALITY,
        maxDimension: Int = DocumentFilters.MAX_PROCESS_DIMENSION
    ): ScannedPage? {
        val pristinePath = page.originalImageUri ?: page.imageUri
        val source = try {
            ImageStore.loadBitmap(context, pristinePath, ImageStore.MAX_CAPTURE_DIMENSION)
                ?: return null
        } catch (error: OutOfMemoryError) {
            android.util.Log.w("DocScannerPerf", "[bake] OutOfMemoryError in decode stage for page ${page.id}: ${error.message}")
            throw error
        }

        var result: Bitmap = source
        try {
            result = renderEnhanced(source, page, maxDimension)

            val signaturePath = page.signatureOverlayUri
            if (!signaturePath.isNullOrBlank()) {
                val signature = try {
                    ImageStore.loadBitmap(context, signaturePath, SIGNATURE_MAX_DIMENSION)
                } catch (error: OutOfMemoryError) {
                    android.util.Log.w("DocScannerPerf", "[bake] OutOfMemoryError in signature-decode stage for page ${page.id}: ${error.message}")
                    throw error
                }
                if (signature == null) {
                    throw IllegalStateException("Signature image is unavailable")
                }
                try {
                    val composited = try {
                        ImageProcessingUtils.drawSignatureOverlay(result, signature)
                    } catch (error: OutOfMemoryError) {
                        android.util.Log.w("DocScannerPerf", "[bake] OutOfMemoryError in signature-composite stage for page ${page.id}: ${error.message}")
                        throw error
                    }
                    if (result !== source) result.recycle()
                    result = composited
                } finally {
                    signature.recycle()
                }
            }

            // Annotations go on last: they are the user's ink over a finished page,
            // so nothing — not the filter, not the perspective warp, not a signature
            // stamp — sits on top of them or distorts them. They are stored on the
            // page rather than baked in, so this replay happens on every re-bake and
            // changing the filter afterwards does not erase them.
            if (page.markup.isNotEmpty()) {
                result = if (result.isMutable) {
                    result
                } else {
                    // `applyEnhancements` may hand back a bitmap it considers
                    // immutable (or one shared with `source`); the caller's `finally`
                    // still recycles whatever `result` ends up being.
                    val mutable = try {
                        result.copy(Bitmap.Config.ARGB_8888, true)
                    } catch (error: OutOfMemoryError) {
                        android.util.Log.w("DocScannerPerf", "[bake] OutOfMemoryError in markup-copy stage for page ${page.id}: ${error.message}")
                        throw error
                    }
                    if (mutable != null) {
                        if (result !== source) result.recycle()
                        mutable
                    } else {
                        result
                    }
                }
                MarkupRenderer.draw(result, page.markup)
            }

            val savedPath = try {
                ImageStore.saveEditedPage(
                    context = context,
                    pageId = page.id,
                    bitmap = result,
                    quality = jpegQuality,
                    lossless = savesLosslessly(page.filterType)
                ) ?: return null
            } catch (error: OutOfMemoryError) {
                android.util.Log.w("DocScannerPerf", "[bake] OutOfMemoryError in output/save stage for page ${page.id}: ${error.message}")
                throw error
            }
            // `revision` is what tells every viewer, tile and cache that the file
            // behind this same path has changed. Without it a re-bake rewrites
            // in place and the screen keeps showing the old bitmap.
            return page.copy(
                imageUri = savedPath,
                originalImageUri = pristinePath,
                revision = page.revision + 1,
                // Round 70.1 / 70.3: the pixels just changed, so previous verdicts
                // belong to the old image. Reset both attempted and failed attempts.
                ocrAttempted = false,
                ocrFailedAttempts = 0
            )
        } finally {
            if (result !== source) result.recycle()
            if (!source.isRecycled) source.recycle()
        }
    }

    /**
     * Applies the page's transform at [maxDimension].
     *
     * Round 70.3 (Arena H004 follow-up): fail fast on inner OOM. The silent 3200->2400
     * step-down loop is removed because it produced downgraded saves published as ordinary
     * success, and did not cover decode, warp, rotation, signature, markup, or output.
     * On OOM, logs to DocScannerPerf and throws so the caller keeps previous good output.
     */
    private fun renderEnhanced(source: Bitmap, page: ScannedPage, maxDimension: Int): Bitmap {
        try {
            return ImageProcessingUtils.applyEnhancements(
                src = source,
                filterType = page.filterType,
                rotationDegrees = page.rotationDegrees,
                corners = page.corners,
                maxDimension = maxDimension,
                failOnFilterError = true
            )
        } catch (error: OutOfMemoryError) {
            android.util.Log.w(
                "DocScannerPerf",
                "[bake] OutOfMemoryError in filter/enhancement stage (filter: ${page.filterType}, maxDim: $maxDimension) for page ${page.id}: ${error.message}"
            )
            throw error
        }
    }

    /**
     * Bi-level and single-channel pages are worth storing losslessly.
     *
     * *Clear B&W* is binary: JPEG turns every glyph edge into ringing and, because
     * the PDF embeds the decoded bitmap losslessly, that ringing becomes the
     * ceiling of the exported document. PNG of binary text is smaller *and* exact.
     * *Grayscale* is the same argument with more levels.
     */
    private fun savesLosslessly(filterType: FilterType): Boolean =
        filterType == FilterType.CLEAR_BW || filterType == FilterType.GRAYSCALE

    suspend fun bake(
        context: Context,
        page: ScannedPage,
        jpegQuality: Int = ImageStore.JPEG_QUALITY,
        maxDimension: Int = DocumentFilters.MAX_PROCESS_DIMENSION
    ): ScannedPage? =
        withContext(Dispatchers.IO) { bakeBlocking(context, page, jpegQuality, maxDimension) }

    /**
     * The longest edge a baked page will have for [quality] — the one number the
     * bake and the editor preview must agree on, or the preview stops predicting
     * the save.
     *
     * Capped at [DocumentFilters.MAX_PROCESS_DIMENSION]: that is the working
     * resolution the filters are tuned and budgeted for. High (3200) and the cap
     * are the same number on purpose — that is what makes the PDF a 300 DPI page
     * rather than a 187 DPI one. Medium (2000) is honoured as-is.
     */
    fun bakeDimension(quality: ImageQuality): Int =
        minOf(quality.maxDimension, DocumentFilters.MAX_PROCESS_DIMENSION)

    /**
     * Bitmap to embed in a PDF. Uses the already-baked `imageUri`; if that file
     * has gone missing it re-bakes from the pristine original.
     */
    fun bitmapForExport(context: Context, page: ScannedPage, maxDimension: Int): Bitmap? {
        ImageStore.loadBitmap(context, page.imageUri, maxDimension)?.let { return it }
        val rebaked = bakeBlocking(context, page) ?: return null
        return ImageStore.loadBitmap(context, rebaked.imageUri, maxDimension)
    }

    private const val SIGNATURE_MAX_DIMENSION = 1200

}
