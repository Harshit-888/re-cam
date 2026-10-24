package com.example.docscanner.util

import android.graphics.Bitmap
import com.example.docscanner.model.QuadCorners
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Document-border detection: finds the four corners of the page in a photo.
 *
 * ## Why this exists
 * The previous implementation sampled a 64×48 luminance grid and returned the
 * **axis-aligned bounding box** of "bright enough" cells. That has three problems
 * a user notices immediately:
 * - it cannot represent perspective, so a page shot at an angle is cropped with
 *   a rectangle that cuts off two corners and keeps desk at the other two;
 * - it keys on absolute brightness, so a white desk (or a bright object nearby)
 *   swallows the page;
 * - its threshold is compared against the 85th percentile of the *whole frame*,
 *   so a page filling the frame fails the "box too large" test and silently
 *   returns the full frame.
 *
 * ## How it works
 * 1. Downscale to [WORK_WIDTH] px and blur — detection only needs geometry.
 * 2. **Sobel gradients**; keep the strongest edges (percentile threshold).
 * 3. **Extreme-point quad**: for a document occupying most of the frame, the
 *   top-left corner minimises `x + y`, the bottom-right maximises it, the
 *   top-right maximises `x − y` and the bottom-left minimises it. This adapts to
 *   perspective naturally and ignores interior edges (text) because those are
 *   never extreme.
 * 4. **Robust line fit per side** (least squares + MAD outlier rejection), using
 *   only edge pixels near that side.
 * 5. **Intersect adjacent lines** to get well-conditioned corners, then validate:
 *   convex, in-bounds, plausible side lengths and area.
 *
 * Detection is deliberately conservative: any doubt returns
 * [QuadCorners.FULL_FRAME], which never crops content. The user can always drag
 * the handles.
 */
object DocumentDetector {

    /** Working resolution for detection. Geometry does not need more. */
    private const val WORK_WIDTH = 360

    /** Minimum edge pixels on a side before we trust a line fit. */
    private const val MIN_SIDE_POINTS = 10

    /** A document must cover at least this fraction of the frame. */
    private const val MIN_AREA_RATIO = 0.12f

    /** Above this the page already fills the frame; cropping gains nothing. */
    private const val MAX_AREA_RATIO = 0.995f

    /** Each side must span at least this fraction of its axis. */
    private const val MIN_SIDE_RATIO = 0.22f

    /** How far (as a fraction of the long edge) a point may sit from a side. */
    private const val SIDE_BAND = 0.055f

    /** Corners are pulled this far toward the centre to avoid a dark rim. */
    private const val EDGE_INSET = 0.004f

    /**
     * Detects the document quad, or returns [QuadCorners.FULL_FRAME] when the
     * result is not trustworthy. Never throws.
     */
    fun detectQuad(src: Bitmap): QuadCorners = try {
        detect(src) ?: QuadCorners.FULL_FRAME
    } catch (_: Throwable) {
        QuadCorners.FULL_FRAME
    }

    private fun detect(src: Bitmap): QuadCorners? {
        if (src.isRecycled) return null
        val workingWidth = min(src.width, WORK_WIDTH)
        if (workingWidth < 48) return null
        val workingHeight = max(48, (src.height * (workingWidth.toFloat() / src.width)).roundToInt())

        val scaled = if (workingWidth == src.width && workingHeight == src.height) {
            src
        } else {
            try {
                Bitmap.createScaledBitmap(src, workingWidth, workingHeight, true)
            } catch (_: Throwable) {
                return null
            }
        }
        val w = scaled.width
        val h = scaled.height
        val pixels = IntArray(w * h)
        try {
            scaled.getPixels(pixels, 0, w, 0, 0, w, h)
        } catch (_: Throwable) {
            return null
        } finally {
            if (scaled !== src && !scaled.isRecycled) scaled.recycle()
        }

        // Luminance, then a light blur so JPEG noise and halftone dots do not
        // create spurious strong gradients.
        var gray = FloatArray(w * h)
        for (i in pixels.indices) {
            val p = pixels[i]
            gray[i] = 0.299f * ((p ushr 16) and 0xFF) +
                0.587f * ((p ushr 8) and 0xFF) +
                0.114f * (p and 0xFF)
        }
        gray = blur3(gray, w, h)

        // Sobel magnitude.
        val magnitude = FloatArray(w * h)
        for (y in 1 until h - 1) {
            val row = y * w
            for (x in 1 until w - 1) {
                val i = row + x
                val tl = gray[i - w - 1]
                val tc = gray[i - w]
                val tr = gray[i - w + 1]
                val ml = gray[i - 1]
                val mr = gray[i + 1]
                val bl = gray[i + w - 1]
                val bc = gray[i + w]
                val br = gray[i + w + 1]

                val dx = (tr + 2f * mr + br) - (tl + 2f * ml + bl)
                val dy = (bl + 2f * bc + br) - (tl + 2f * tc + tr)
                magnitude[i] = sqrt(dx * dx + dy * dy)
            }
        }

        val threshold = edgeThreshold(magnitude)
        var edgeCount = 0
        for (value in magnitude) if (value >= threshold) edgeCount++
        if (edgeCount < 4 * MIN_SIDE_POINTS) return null

        val ex = IntArray(edgeCount)
        val ey = IntArray(edgeCount)
        var written = 0
        for (y in 1 until h - 1) {
            val row = y * w
            for (x in 1 until w - 1) {
                if (magnitude[row + x] >= threshold) {
                    ex[written] = x
                    ey[written] = y
                    written++
                }
            }
        }

        // ---- step 1: extreme-point quad ------------------------------------
        var topLeft = -1
        var topRight = -1
        var bottomRight = -1
        var bottomLeft = -1
        var minSum = Int.MAX_VALUE
        var maxSum = Int.MIN_VALUE
        var minDiff = Int.MAX_VALUE
        var maxDiff = Int.MIN_VALUE

        for (i in 0 until written) {
            val sum = ex[i] + ey[i]
            val diff = ex[i] - ey[i]
            if (sum < minSum) { minSum = sum; topLeft = i }
            if (sum > maxSum) { maxSum = sum; bottomRight = i }
            if (diff > maxDiff) { maxDiff = diff; topRight = i }
            if (diff < minDiff) { minDiff = diff; bottomLeft = i }
        }
        if (topLeft < 0 || topRight < 0 || bottomRight < 0 || bottomLeft < 0) return null

        val rough = floatArrayOf(
            ex[topLeft].toFloat(), ey[topLeft].toFloat(),
            ex[topRight].toFloat(), ey[topRight].toFloat(),
            ex[bottomRight].toFloat(), ey[bottomRight].toFloat(),
            ex[bottomLeft].toFloat(), ey[bottomLeft].toFloat()
        )

        // ---- step 2: robust line per side ----------------------------------
        val band = SIDE_BAND * max(w, h)
        val topLine = fitSide(ex, ey, written, rough, 0, horizontal = true, band = band) ?: return null
        val rightLine = fitSide(ex, ey, written, rough, 1, horizontal = false, band = band) ?: return null
        val bottomLine = fitSide(ex, ey, written, rough, 2, horizontal = true, band = band) ?: return null
        val leftLine = fitSide(ex, ey, written, rough, 3, horizontal = false, band = band) ?: return null

        // ---- step 3: corners from line intersections -----------------------
        val tlx = intersect(topLine, leftLine)
        val trx = intersect(topLine, rightLine)
        val brx = intersect(bottomLine, rightLine)
        val blx = intersect(bottomLine, leftLine)
        if (tlx == null || trx == null || brx == null || blx == null) return null

        // ---- step 4: validate ----------------------------------------------
        val tolerance = 0.03f
        val xs = floatArrayOf(tlx[0], trx[0], brx[0], blx[0])
        val ys = floatArrayOf(tlx[1], trx[1], brx[1], blx[1])
        for (i in 0..3) {
            if (xs[i] < -tolerance * w || xs[i] > w * (1f + tolerance)) return null
            if (ys[i] < -tolerance * h || ys[i] > h * (1f + tolerance)) return null
        }
        if (!isConvex(xs, ys)) return null

        val area = quadArea(xs, ys)
        val ratio = area / (w.toFloat() * h.toFloat())
        if (ratio < MIN_AREA_RATIO || ratio > MAX_AREA_RATIO) return null

        val topLength = distance(xs[0], ys[0], xs[1], ys[1])
        val bottomLength = distance(xs[3], ys[3], xs[2], ys[2])
        val leftLength = distance(xs[0], ys[0], xs[3], ys[3])
        val rightLength = distance(xs[1], ys[1], xs[2], ys[2])
        if (topLength < MIN_SIDE_RATIO * w || bottomLength < MIN_SIDE_RATIO * w) return null
        if (leftLength < MIN_SIDE_RATIO * h || rightLength < MIN_SIDE_RATIO * h) return null

        // TL must actually be up and left of BR, otherwise the quad is crossed.
        if (ys[0] >= ys[2] || xs[0] >= xs[2]) return null

        // ---- step 5: normalise, inset slightly, clamp ----------------------
        val cx = (xs[0] + xs[1] + xs[2] + xs[3]) / 4f
        val cy = (ys[0] + ys[1] + ys[2] + ys[3]) / 4f
        val shrink = 1f - EDGE_INSET

        val nx = FloatArray(4) { ((xs[it] - cx) * shrink + cx) / w }.also {
            for (i in 0..3) it[i] = it[i].coerceIn(0f, 1f)
        }
        val ny = FloatArray(4) { ((ys[it] - cy) * shrink + cy) / h }.also {
            for (i in 0..3) it[i] = it[i].coerceIn(0f, 1f)
        }

        return QuadCorners(
            topLeftX = nx[0], topLeftY = ny[0],
            topRightX = nx[1], topRightY = ny[1],
            bottomRightX = nx[2], bottomRightY = ny[2],
            bottomLeftX = nx[3], bottomLeftY = ny[3]
        )
    }

    /**
     * Gradient threshold: the stronger of a high percentile of all magnitudes and
     * a fraction of the maximum, with an absolute floor so a blurry low-contrast
     * photo does not turn noise into edges.
     */
    private fun edgeThreshold(magnitude: FloatArray): Float {
        val sorted = magnitude.copyOf()
        sorted.sort()
        val p90 = sorted[(sorted.size * 0.90f).toInt().coerceIn(0, sorted.lastIndex)]
        val p995 = sorted[(sorted.size * 0.995f).toInt().coerceIn(0, sorted.lastIndex)]
        return max(EDGE_FLOOR, max(p90 * 0.55f, p995 * 0.28f))
    }

    /**
     * Collects edge pixels lying in a band around one side of [rough] and fits a
     * line through them.
     *
     * @param side 0 = top, 1 = right, 2 = bottom, 3 = left.
     * @param horizontal when true the fit is `y = a·x + b`, else `x = a·y + b`.
     */
    private fun fitSide(
        ex: IntArray,
        ey: IntArray,
        count: Int,
        rough: FloatArray,
        side: Int,
        horizontal: Boolean,
        band: Float
    ): Line? {
        val a0 = (side * 2) % 8
        val b0 = ((side + 1) * 2) % 8
        val ax = rough[a0]
        val ay = rough[a0 + 1]
        val bx = rough[b0]
        val by = rough[b0 + 1]

        val dx = bx - ax
        val dy = by - ay
        val lengthSquared = dx * dx + dy * dy
        if (lengthSquared < 1f) return null

        val selectedX = IntArray(count)
        val selectedY = IntArray(count)
        var n = 0

        for (i in 0 until count) {
            val px = ex[i].toFloat()
            val py = ey[i].toFloat()
            val t = ((px - ax) * dx + (py - ay) * dy) / lengthSquared
            // Skip the corners themselves: they belong to two sides and would
            // bias both fits.
            if (t < 0.10f || t > 0.90f) continue
            val projectedX = ax + t * dx
            val projectedY = ay + t * dy
            val dist = sqrt((px - projectedX) * (px - projectedX) + (py - projectedY) * (py - projectedY))
            if (dist > band) continue
            selectedX[n] = ex[i]
            selectedY[n] = ey[i]
            n++
        }

        if (n < MIN_SIDE_POINTS) return null

        val first = leastSquares(selectedX, selectedY, n, horizontal) ?: return null
        val refined = refine(selectedX, selectedY, n, horizontal, first)
        return refined ?: first
    }

    /** One robust refit: drop outliers beyond 3× the median absolute residual. */
    private fun refine(
        xs: IntArray,
        ys: IntArray,
        count: Int,
        horizontal: Boolean,
        initial: Line
    ): Line? {
        val residuals = FloatArray(count)
        for (i in 0 until count) {
            val independent = if (horizontal) xs[i].toFloat() else ys[i].toFloat()
            val dependent = if (horizontal) ys[i].toFloat() else xs[i].toFloat()
            residuals[i] = abs(dependent - (initial.a * independent + initial.b))
        }
        val sorted = residuals.copyOf()
        sorted.sort()
        val median = sorted[count / 2]
        val limit = max(1.2f, 3f * median + 0.75f)

        val keptX = IntArray(count)
        val keptY = IntArray(count)
        var kept = 0
        for (i in 0 until count) {
            if (residuals[i] <= limit) {
                keptX[kept] = xs[i]
                keptY[kept] = ys[i]
                kept++
            }
        }
        if (kept < MIN_SIDE_POINTS) return null
        return leastSquares(keptX, keptY, kept, horizontal)
    }

    private fun leastSquares(
        xs: IntArray,
        ys: IntArray,
        count: Int,
        horizontal: Boolean
    ): Line? {
        var sumI = 0.0
        var sumD = 0.0
        var sumII = 0.0
        var sumID = 0.0
        for (i in 0 until count) {
            val independent = (if (horizontal) xs[i] else ys[i]).toDouble()
            val dependent = (if (horizontal) ys[i] else xs[i]).toDouble()
            sumI += independent
            sumD += dependent
            sumII += independent * independent
            sumID += independent * dependent
        }
        val denominator = count * sumII - sumI * sumI
        if (abs(denominator) < 1e-6) return null
        val a = ((count * sumID - sumI * sumD) / denominator).toFloat()
        val b = ((sumD - a * sumI) / count).toFloat()
        if (!a.isFinite() || !b.isFinite()) return null
        return Line(a, b, horizontal)
    }

    /** `y = a·x + b` when [horizontal], otherwise `x = a·y + b`. */
    private class Line(val a: Float, val b: Float, val horizontal: Boolean)

    /**
     * Intersection of a horizontal line (`y = a₁x + b₁`) and a vertical one
     * (`x = a₂y + b₂`), returned as `[x, y]`.
     */
    private fun intersect(horizontal: Line, vertical: Line): FloatArray? {
        val denominator = 1f - vertical.a * horizontal.a
        if (abs(denominator) < 1e-4f) return null
        val y = (horizontal.a * vertical.b + horizontal.b) / denominator
        val x = vertical.a * y + vertical.b
        if (!x.isFinite() || !y.isFinite()) return null
        return floatArrayOf(x, y)
    }

    /** True when all four turns go the same way. */
    private fun isConvex(xs: FloatArray, ys: FloatArray): Boolean {
        var sign = 0
        for (i in 0..3) {
            val j = (i + 1) % 4
            val k = (i + 2) % 4
            val cross = (xs[j] - xs[i]) * (ys[k] - ys[j]) - (ys[j] - ys[i]) * (xs[k] - xs[j])
            if (abs(cross) < 0.001f) return false
            val current = if (cross > 0f) 1 else -1
            if (sign == 0) sign = current else if (sign != current) return false
        }
        return true
    }

    private fun quadArea(xs: FloatArray, ys: FloatArray): Float {
        var sum = 0f
        for (i in 0..3) {
            val j = (i + 1) % 4
            sum += xs[i] * ys[j] - xs[j] * ys[i]
        }
        return abs(sum) / 2f
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }

    private fun blur3(src: FloatArray, w: Int, h: Int): FloatArray {
        val horizontal = FloatArray(src.size)
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                val c = src[row + x]
                val l = if (x > 0) src[row + x - 1] else c
                val r = if (x < w - 1) src[row + x + 1] else c
                horizontal[row + x] = (l + c + r) / 3f
            }
        }
        val out = FloatArray(src.size)
        for (y in 0 until h) {
            val centreRow = y * w
            val upRow = (if (y > 0) y - 1 else y) * w
            val downRow = (if (y < h - 1) y + 1 else y) * w
            for (x in 0 until w) {
                out[centreRow + x] =
                    (horizontal[upRow + x] + horizontal[centreRow + x] + horizontal[downRow + x]) / 3f
            }
        }
        return out
    }

    /** Absolute gradient floor; below this a photo has no usable page boundary. */
    private const val EDGE_FLOOR = 26f
}
