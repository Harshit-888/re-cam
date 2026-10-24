package com.example.docscanner.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import androidx.compose.ui.graphics.ColorFilter
import com.example.docscanner.model.FilterType

object ImageProcessingUtils {

    /**
     * Compose ColorFilter for live preview in Compose Image composables
     */
    fun getComposeColorFilter(filterType: FilterType): ColorFilter? {
        val matrix = getColorMatrix(filterType) ?: return null
        return ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix(matrix.array))
    }

    /**
     * Android Graphics ColorMatrix for the 5 scanner enhancement modes
     */
    fun getColorMatrix(filterType: FilterType): ColorMatrix? {
        return when (filterType) {
            FilterType.ORIGINAL -> null
            FilterType.MAGIC_COLOR -> {
                // Boost contrast, sharpen text, balance whites while preserving color signatures
                val cm = ColorMatrix()
                // Increase contrast and saturation
                val contrast = 1.35f
                val brightness = 15f
                cm.set(floatArrayOf(
                    contrast, 0f, 0f, 0f, brightness,
                    0f, contrast, 0f, 0f, brightness,
                    0f, 0f, contrast, 0f, brightness,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm
            }
            FilterType.CLEAR_BW -> {
                // Extreme contrast thresholding (deep black text on pure white)
                val cm = ColorMatrix()
                cm.setSaturation(0f)
                val contrast = 2.4f
                val scale = ColorMatrix(floatArrayOf(
                    contrast, 0f, 0f, 0f, -140f,
                    0f, contrast, 0f, 0f, -140f,
                    0f, 0f, contrast, 0f, -140f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(scale)
                cm
            }
            FilterType.GRAYSCALE -> {
                val cm = ColorMatrix()
                cm.setSaturation(0f)
                // Slight contrast enhance for readable documents
                val contrast = 1.2f
                val scale = ColorMatrix(floatArrayOf(
                    contrast, 0f, 0f, 0f, 5f,
                    0f, contrast, 0f, 0f, 5f,
                    0f, 0f, contrast, 0f, 5f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(scale)
                cm
            }
            FilterType.LIGHTEN -> {
                val cm = ColorMatrix()
                val brightness = 40f
                cm.set(floatArrayOf(
                    1.1f, 0f, 0f, 0f, brightness,
                    0f, 1.1f, 0f, 0f, brightness,
                    0f, 0f, 1.1f, 0f, brightness,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm
            }
        }
    }

    /**
     * Applies rotation and color filters onto a Bitmap
     */
    fun applyEnhancements(src: Bitmap, filterType: FilterType, rotationDegrees: Float): Bitmap {
        val matrix = Matrix()
        if (rotationDegrees != 0f) {
            matrix.postRotate(rotationDegrees)
        }

        val rotated = if (rotationDegrees != 0f) {
            Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
        } else {
            src
        }

        val colorMatrix = getColorMatrix(filterType) ?: return rotated

        val output = Bitmap.createBitmap(rotated.width, rotated.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        canvas.drawBitmap(rotated, 0f, 0f, paint)
        return output
    }
}
