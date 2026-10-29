package com.example.docscanner.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import com.example.docscanner.model.FilterType
import com.example.docscanner.model.QuadCorners

object ImageProcessingUtils {

    /**
     * Full enhancement pipeline, in the order the geometry demands:
     * **warp → rotate → colour**.
     *
     * Colour work is delegated to [DocumentFilters], whose maths is ported from
     * the original APK (`docs/ORIGINAL_APP_FILTER_SPEC.md`): `fa1.H` estimation,
     * a linear stretch for *Lighten*, paper-max illumination normalisation for the
     * two document filters and Otsu for *Clear B&W*. Earlier revisions used a
     * global [android.graphics.ColorMatrix] here and the results looked washed out
     * or clipped.
     *
     * The source bitmap is never recycled: the caller owns it (it is usually a
     * cached decode).
     *
     * @param corners perspective quad to correct first; `null` or full-frame skips it.
     */
    fun applyEnhancements(
        src: Bitmap,
        filterType: FilterType,
        rotationDegrees: Float,
        corners: QuadCorners? = null,
        maxDimension: Int = DocumentFilters.MAX_PROCESS_DIMENSION,
        failOnFilterError: Boolean = false
    ): Bitmap {
        var current = if (corners != null) PerspectiveWarp.warp(src, corners) else src

        val normalizedRotation = normalizeRotation(rotationDegrees)
        if (normalizedRotation != 0f) {
            val rotated = Bitmap.createBitmap(
                current,
                0,
                0,
                current.width,
                current.height,
                Matrix().apply { postRotate(normalizedRotation) },
                true
            )
            if (rotated !== current) {
                if (current !== src) current.recycle()
                current = rotated
            }
        }

        if (filterType == FilterType.ORIGINAL) return current

        val filtered = try {
            DocumentFilters.apply(current, filterType, maxDimension, failOnFilterError)
        } catch (error: Throwable) {
            // An OutOfMemoryError must reach the caller: swallowing it here means
            // saving the *unfiltered* page, which looks like "the filter did
            // nothing" rather than "this page was too big". PageRenderer retries
            // at a lower resolution when it sees one.
            if (error is OutOfMemoryError || failOnFilterError) throw error
            null
        }
        // DocumentFilters returns the input for ORIGINAL and on any failure, so a
        // non-null-but-identical result means "nothing was applied".
        if (filtered == null || filtered === current) return current

        if (current !== src) current.recycle()
        return filtered
    }

    /** Alias kept for readability at call sites that only need the colour pass. */
    fun applyFilter(src: Bitmap, filterType: FilterType, maxDimension: Int = DocumentFilters.MAX_PROCESS_DIMENSION): Bitmap =
        DocumentFilters.apply(src, filterType, maxDimension)

    /**
     * Normalises any angle into `[0, 360)`.
     *
     * An earlier revision stored `(rotation - 90f) % 360f`, which yields
     * **negative** degrees; `Matrix.postRotate` accepts them, so the bug was
     * invisible until something compared angles.
     */
    fun normalizeRotation(degrees: Float): Float {
        val wrapped = degrees % 360f
        return if (wrapped < 0f) wrapped + 360f else wrapped
    }

    /**
     * Composites a signature PNG onto the bottom-right of [base] at ~32 % of the
     * page width, inset by 6 %.
     *
     * Applied *after* warping and filtering so the ink is neither distorted nor
     * desaturated by the document enhancement.
     */
    fun drawSignatureOverlay(base: Bitmap, signature: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(base.width, base.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawBitmap(base, 0f, 0f, null)

        val targetWidth = base.width * 0.32f
        val scale = targetWidth / signature.width
        val targetHeight = signature.height * scale
        val marginX = base.width * 0.06f
        val marginY = base.height * 0.06f
        val dest = android.graphics.RectF(
            base.width - marginX - targetWidth,
            base.height - marginY - targetHeight,
            base.width - marginX,
            base.height - marginY
        )

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(signature, null, dest, paint)
        return output
    }
}
