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
     * Cheap "auto detect" used by the editor's *Auto Detect* button.
     *
     * Samples the page on a coarse grid, estimates the paper brightness from the
     * upper percentile of luminance, and returns the bounding box of pixels
     * bright enough to be paper. This is a **brightness bounding box**, not
     * contour/edge detection — it finds a straight document on a dark background
     * reliably and politely gives up (returning a small inset) otherwise.
     *
     * Upgrade path: replace with ML Kit Document Scanner or a contour pass over
     * a Canny mask if a true quad is needed.
     */
    fun detectQuad(bitmap: Bitmap): QuadCorners {
        // When detection is inconclusive, report the FULL frame: silently
        // trimming 6% off every edge could cut real content off the user's page.
        val fallback = QuadCorners.FULL_FRAME
        val gridWidth = 64
        val gridHeight = (64.0 * bitmap.height / bitmap.width).roundToInt().coerceIn(48, 96)

        val luminance = FloatArray(gridWidth * gridHeight)
        for (gy in 0 until gridHeight) {
            val y = (gy + 0.5f) / gridHeight * (bitmap.height - 1)
            for (gx in 0 until gridWidth) {
                val x = (gx + 0.5f) / gridWidth * (bitmap.width - 1)
                val pixel = bitmap.getPixel(x.toInt().coerceIn(0, bitmap.width - 1), y.toInt().coerceIn(0, bitmap.height - 1))
                // Rec. 601 luminance, matching the grayscale filter's weights.
                luminance[gy * gridWidth + gx] =
                    0.299f * android.graphics.Color.red(pixel) +
                    0.587f * android.graphics.Color.green(pixel) +
                    0.114f * android.graphics.Color.blue(pixel)
            }
        }

        val sorted = luminance.copyOf().also { it.sort() }
        val paperLevel = sorted[(sorted.size * 0.85f).toInt().coerceIn(0, sorted.lastIndex)]
        if (paperLevel < 60f) return fallback // nothing bright enough: no paper visible

        val threshold = paperLevel * 0.62f
        var minX = gridWidth
        var minY = gridHeight
        var maxX = -1
        var maxY = -1
        var brightCount = 0

        for (gy in 0 until gridHeight) {
            for (gx in 0 until gridWidth) {
                if (luminance[gy * gridWidth + gx] >= threshold) {
                    brightCount++
                    if (gx < minX) minX = gx
                    if (gx > maxX) maxX = gx
                    if (gy < minY) minY = gy
                    if (gy > maxY) maxY = gy
                }
            }
        }

        if (maxX < 0 || maxY < 0) return fallback

        val coverage = brightCount.toFloat() / (gridWidth * gridHeight)
        val boxWidth = (maxX - minX + 1).toFloat() / gridWidth
        val boxHeight = (maxY - minY + 1).toFloat() / gridHeight

        // Reject implausible results: too little paper anywhere, or a box that is
        // essentially the whole frame (in which case cropping gains nothing).
        if (coverage < 0.08f || (boxWidth > 0.985f && boxHeight > 0.985f)) return fallback

        return QuadCorners(
            topLeftX = minX.toFloat() / gridWidth,
            topLeftY = minY.toFloat() / gridHeight,
            topRightX = (maxX + 1).toFloat() / gridWidth,
            topRightY = minY.toFloat() / gridHeight,
            bottomRightX = (maxX + 1).toFloat() / gridWidth,
            bottomRightY = (maxY + 1).toFloat() / gridHeight,
            bottomLeftX = minX.toFloat() / gridWidth,
            bottomLeftY = (maxY + 1).toFloat() / gridHeight
        )
    }

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
