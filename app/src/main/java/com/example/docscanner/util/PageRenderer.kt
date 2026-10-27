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
        val source = ImageStore.loadBitmap(context, pristinePath, ImageStore.MAX_CAPTURE_DIMENSION)
            ?: return null

        var result: Bitmap = source
        try {
            result = renderEnhanced(source, page, maxDimension)

            val signaturePath = page.signatureOverlayUri
            if (!signaturePath.isNullOrBlank()) {
                val signature = ImageStore.loadBitmap(context, signaturePath, SIGNATURE_MAX_DIMENSION)
                if (signature != null) {
                    val composited = ImageProcessingUtils.drawSignatureOverlay(result, signature)
                    if (result !== source) result.recycle()
                    result = composited
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
                    val mutable = result.copy(Bitmap.Config.ARGB_8888, true)
                    if (mutable != null) {
                        if (result !== source) result.recycle()
                        mutable
                    } else {
                        result
                    }
                }
                MarkupRenderer.draw(result, page.markup)
            }

            val savedPath = ImageStore.saveEditedPage(
                context = context,
                pageId = page.id,
                bitmap = result,
                quality = jpegQuality,
                lossless = savesLosslessly(page.filterType)
            ) ?: return null
            return page.copy(imageUri = savedPath, originalImageUri = pristinePath)
        } finally {
            if (result !== source) result.recycle()
            if (!source.isRecycled) source.recycle()
        }
    }

    /**
     * Applies the page's transform, falling back to a smaller working resolution
     * only if the full one runs out of memory.
     *
     * 3200 px needs about 95 MB of planes at peak. A device that cannot give it
     * should still save the page — at 2400 px (~225 DPI on A4) rather than at
     * 300 DPI — instead of failing the save, which is what an uncaught
     * `OutOfMemoryError` inside the filter would otherwise do.
     */
    private fun renderEnhanced(source: Bitmap, page: ScannedPage, maxDimension: Int): Bitmap {
        var dimension = maxDimension
        while (true) {
            try {
                return ImageProcessingUtils.applyEnhancements(
                    src = source,
                    filterType = page.filterType,
                    rotationDegrees = page.rotationDegrees,
                    corners = page.corners,
                    maxDimension = dimension
                )
            } catch (error: OutOfMemoryError) {
                if (dimension <= FALLBACK_MIN_DIMENSION) throw error
                dimension = maxOf(FALLBACK_MIN_DIMENSION, (dimension * 3) / 4)
            }
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

    /** Lowest working resolution the memory fallback will step down to (~225 DPI on A4). */
    private const val FALLBACK_MIN_DIMENSION = 2400
}
