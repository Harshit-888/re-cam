package com.example.docscanner.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import com.example.docscanner.model.QuadCorners
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Genuine 4-point perspective correction.
 *
 * The previous revision stored corner coordinates but only ever *drew* them over
 * a mock paper shape — no pixels moved. This maps the user's quadrilateral onto
 * an axis-aligned rectangle using a projective transform
 * ([Matrix.setPolyToPoly] with 4 points), which is the same homography the
 * legacy app delegated to `PSOpenCV`.
 */
object PerspectiveWarp {

    /** Smallest output edge we accept before giving up on a degenerate quad. */
    private const val MIN_OUTPUT_EDGE = 16

    /**
     * Warps [src] so the quadrilateral described by [corners] fills the frame.
     *
     * @return a new bitmap, or [src] itself when the quad is already the full
     *   frame or is degenerate. The caller owns the result.
     */
    fun warp(src: Bitmap, corners: QuadCorners): Bitmap {
        if (corners.isFullFrame) return src

        val width = src.width.toFloat()
        val height = src.height.toFloat()

        val tlX = corners.topLeftX * width
        val tlY = corners.topLeftY * height
        val trX = corners.topRightX * width
        val trY = corners.topRightY * height
        val brX = corners.bottomRightX * width
        val brY = corners.bottomRightY * height
        val blX = corners.bottomLeftX * width
        val blY = corners.bottomLeftY * height

        // Output keeps the document's real-world aspect ratio: take the longer
        // horizontal edge and the longer vertical edge of the quad.
        val outWidth = max(distance(tlX, tlY, trX, trY), distance(blX, blY, brX, brY))
        val outHeight = max(distance(tlX, tlY, blX, blY), distance(trX, trY, brX, brY))

        if (outWidth < MIN_OUTPUT_EDGE || outHeight < MIN_OUTPUT_EDGE) return src
        if (quadArea(tlX, tlY, trX, trY, brX, brY, blX, blY) < MIN_OUTPUT_EDGE * MIN_OUTPUT_EDGE) {
            return src
        }

        val source = floatArrayOf(tlX, tlY, trX, trY, brX, brY, blX, blY)
        val destination = floatArrayOf(
            0f, 0f,
            outWidth, 0f,
            outWidth, outHeight,
            0f, outHeight
        )

        val matrix = Matrix()
        if (!matrix.setPolyToPoly(source, 0, destination, 0, 4)) return src

        val output = try {
            Bitmap.createBitmap(
                outWidth.roundToInt().coerceAtLeast(MIN_OUTPUT_EDGE),
                outHeight.roundToInt().coerceAtLeast(MIN_OUTPUT_EDGE),
                Bitmap.Config.ARGB_8888
            )
        } catch (_: OutOfMemoryError) {
            return src
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        Canvas(output).apply {
            drawColor(android.graphics.Color.WHITE)
            drawBitmap(src, matrix, paint)
        }
        return output
    }

    /**
     * Auto-detects the document quad.
     *
     * Delegates to [DocumentDetector], which does Sobel edge extraction plus a
     * robust four-line fit. This used to be a brightness bounding box here, which
     * could not represent perspective and failed on white backgrounds.
     *
     * Kept on this object because several call sites and the agent integration
     * contract refer to `PerspectiveWarp.detectQuad`.
     */
    fun detectQuad(bitmap: Bitmap): QuadCorners = DocumentDetector.detectQuad(bitmap)

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }

    /** Shoelace area of a quad, absolute value. */
    private fun quadArea(
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        x3: Float, y3: Float,
        x4: Float, y4: Float
    ): Float {
        val sum = x1 * y2 - x2 * y1 +
            x2 * y3 - x3 * y2 +
            x3 * y4 - x4 * y3 +
            x4 * y1 - x1 * y4
        return abs(sum) / 2f
    }
}
