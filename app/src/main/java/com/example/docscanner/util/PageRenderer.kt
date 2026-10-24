package com.example.docscanner.util

import android.content.Context
import android.graphics.Bitmap
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
        jpegQuality: Int = ImageStore.JPEG_QUALITY
    ): ScannedPage? {
        val pristinePath = page.originalImageUri ?: page.imageUri
        val source = ImageStore.loadBitmap(context, pristinePath, ImageStore.MAX_CAPTURE_DIMENSION)
            ?: return null

        var result: Bitmap = source
        try {
            result = ImageProcessingUtils.applyEnhancements(
                src = source,
                filterType = page.filterType,
                rotationDegrees = page.rotationDegrees,
                corners = page.corners
            )

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

            val savedPath = ImageStore.saveEditedPage(context, page.id, result, jpegQuality)
                ?: return null
            return page.copy(imageUri = savedPath, originalImageUri = pristinePath)
        } finally {
            if (result !== source) result.recycle()
            if (!source.isRecycled) source.recycle()
        }
    }

    suspend fun bake(
        context: Context,
        page: ScannedPage,
        jpegQuality: Int = ImageStore.JPEG_QUALITY
    ): ScannedPage? = withContext(Dispatchers.IO) { bakeBlocking(context, page, jpegQuality) }

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
