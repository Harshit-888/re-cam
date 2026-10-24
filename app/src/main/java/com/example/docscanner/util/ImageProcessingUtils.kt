package com.example.docscanner.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import androidx.compose.ui.graphics.ColorFilter
import com.example.docscanner.model.FilterType
import com.example.docscanner.model.QuadCorners

object ImageProcessingUtils {

    /**
     * Compose ColorFilter for live previews. Prefer rendering the baked bitmap
     * over applying a preview filter, so what the user sees is what the PDF gets.
     */
    fun getComposeColorFilter(filterType: FilterType): ColorFilter? {
        val matrix = getColorMatrix(filterType) ?: return null
        return ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix(matrix.array))
    }

    /** The 5 scanner enhancement modes, as hardware color matrices. */
    fun getColorMatrix(filterType: FilterType): ColorMatrix? {
        return when (filterType) {
            FilterType.ORIGINAL -> null
            FilterType.MAGIC_COLOR -> {
                // Flatten shadows and lift paper white without killing colour ink.
                val contrast = 1.35f
                val brightness = 15f
                ColorMatrix(
                    floatArrayOf(
                        contrast, 0f, 0f, 0f, brightness,
                        0f, contrast, 0f, 0f, brightness,
                        0f, 0f, contrast, 0f, brightness,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            FilterType.CLEAR_BW -> {
                // Desaturate, then push luminance through a steep ramp so text
                // goes near-black and paper clips to white.
                val cm = ColorMatrix()
                cm.setSaturation(0f)
                val contrast = 2.4f
                cm.postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            contrast, 0f, 0f, 0f, -140f,
                            0f, contrast, 0f, 0f, -140f,
                            0f, 0f, contrast, 0f, -140f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                )
                cm
            }
            FilterType.GRAYSCALE -> {
                val cm = ColorMatrix()
                cm.setSaturation(0f)
                val contrast = 1.2f
                cm.postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            contrast, 0f, 0f, 0f, 5f,
                            0f, contrast, 0f, 0f, 5f,
                            0f, 0f, contrast, 0f, 5f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                )
                cm
            }
            FilterType.LIGHTEN -> ColorMatrix(
                floatArrayOf(
                    1.1f, 0f, 0f, 0f, 40f,
                    0f, 1.1f, 0f, 0f, 40f,
                    0f, 0f, 1.1f, 0f, 40f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
    }

    /**
     * Normalises any angle into `[0, 360)`.
     *
     * The previous revision stored `(rotation - 90f) % 360f`, which yields
     * **negative** degrees; `Matrix.postRotate` accepts them, so the bug was
     * invisible until something compared angles.
     */
    fun normalizeRotation(degrees: Float): Float {
        val wrapped = degrees % 360f
        return if (wrapped < 0f) wrapped + 360f else wrapped
    }

    /**
     * Full enhancement pipeline, in the order the geometry demands:
     * **warp → rotate → colour**.
     *
     * The source bitmap is never recycled here; the caller owns it (it is
     * usually a cached decode).
     *
     * @param corners perspective quad to correct first; `null` or full-frame skips it.
     */
    fun applyEnhancements(
        src: Bitmap,
        filterType: FilterType,
        rotationDegrees: Float,
        corners: QuadCorners? = null
    ): Bitmap {
        var current = if (corners != null) PerspectiveWarp.warp(src, corners) else src

        val normalizedRotation = normalizeRotation(rotationDegrees)
        if (normalizedRotation != 0f) {
            val rotated = try {
                Bitmap.createBitmap(
                    current,
                    0,
                    0,
                    current.width,
                    current.height,
                    Matrix().apply { postRotate(normalizedRotation) },
                    true
                )
            } catch (_: OutOfMemoryError) {
                current
            }
            if (rotated !== current && current !== src) current.recycle()
            if (rotated !== current) current = rotated
        }

        val colorMatrix = getColorMatrix(filterType)
        if (colorMatrix == null) return current

        val output = try {
            Bitmap.createBitmap(current.width, current.height, Bitmap.Config.ARGB_8888)
        } catch (_: OutOfMemoryError) {
            return current
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        Canvas(output).drawBitmap(current, 0f, 0f, paint)
        if (current !== src) current.recycle()
        return output
    }

    /**
     * Composites a signature PNG onto the bottom-right of [base] at ~32 % of the
     * page width, inset by 6 %.
     *
     * Applied *after* warping and filtering so the ink is never distorted or
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
