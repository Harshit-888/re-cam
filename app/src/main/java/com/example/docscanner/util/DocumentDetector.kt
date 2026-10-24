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
 * ## History
 * 1. A 64×48 **brightness bounding box** — could not express perspective at all.
 * 2. **Extreme-point quad over Sobel edges** — still failed on the common case,
 *    because Sobel skipped the 1-px frame border and interior text edges
 *    outnumbered the page edge, so a page filling the frame produced an invalid
 *    quad and silently fell back to "no crop".
 * 3. **This version:** a boundary scan. For every row it finds the first strong
 *    edge from the left and the last from the right; for every column the first
 *    from the top and the last from the bottom. Four sides are then fitted with
 *    RANSAC and intersected.
 *
 * The boundary scan is what makes this robust for how people actually shoot a
 * document: the page roughly fills the frame, so scanning **inward from the
 * frame edge** hits the paper boundary before any text. A side that has no
 * visible boundary (the page runs past the frame) falls back to the frame edge,
 * which is the geometrically correct answer rather than a failure.
 *
 * Everything is validated before it is returned: slopes, convexity, bounds, side
 * lengths and area. Detection never throws, and [Detection.confident] tells the
 * UI whether the quad came from real edges or from frame-edge defaults.
 */
object DocumentDetector {

    /** Working resolution. Geometry needs nothing more, and it keeps this fast. */
    private const val WORK_WIDTH = 360

    /** Minimum inlier count before a side's line fit is trusted. */
    private const val MIN_SIDE_INLIERS = 14

    /** RANSAC iterations per side. */
    private const val RANSAC_ITERATIONS = 220

    /** Inlier tolerance as a fraction of the relevant dimension. */
    private const val RANSAC_TOLERANCE = 0.012f

    /** A document must cover at least this fraction of the frame. */
    private const val MIN_AREA_RATIO = 0.10f

    /** Above this the page already fills the frame; cropping gains nothing. */
    private const val MAX_AREA_RATIO = 0.995f

    /** Each side must span at least this fraction of its axis. */
    private const val MIN_SIDE_RATIO = 0.18f

    /** Steepest allowed side slope (dependent/independent). ~45°. */
    private const val MAX_SLOPE = 1.1f

    /** Corners are pulled this far toward the centre to avoid a dark rim. */
    private const val EDGE_INSET = 0.004f

    /** Below this gradient magnitude a photo has no usable page boundary. */
    private const val EDGE_FLOOR = 18f

    /** Result of a detection attempt. */
    data class Detection(
        val corners: QuadCorners,
        /**
         * True when at least three of the four sides were fitted from real edge
         * pixels. False means one or more sides fell back to the frame edge, or
         * nothing plausible was found — the UI should invite manual adjustment.
         */
        val confident: Boolean
    )

    /**
     * Detects the document quad.
     *
     * @return the quad plus a confidence flag. Never throws; on any failure it
     *   returns [QuadCorners.FULL_FRAME] with `confident = false`.
     */
    fun detect(src: Bitmap): Detection = try {
        detectInternal(src) ?: Detection(QuadCorners.FULL_FRAME, confident = false)
    } catch (_: Throwable) {
        Detection(QuadCorners.FULL_FRAME, confident = false)
    }

    /** Convenience for call sites that only need the corners. */
    fun detectQuad(src: Bitmap): QuadCorners = detect(src).corners

    // ------------------------------------------------------------------ core

    private fun detectInternal(src: Bitmap): Detection? {
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

        var gray = FloatArray(w * h)
        for (i in pixels.indices) {
            val p = pixels[i]
            gray[i] = 0.299f * ((p ushr 16) and 0xFF) +
                0.587f * ((p ushr 8) and 0xFF) +
                0.114f * (p and 0xFF)
        }
        gray = blur3(gray, w, h)

        // Sobel with clamped indices: the old version started at 1 and stopped at
        // w-1, so a page edge lying on the frame border produced no gradient at
        // all — the most common failure mode.
        val magnitude = sobel(gray, w, h)
        val threshold = edgeThreshold(magnitude)

        // ---- 1. boundary scan ---------------------------------------------
        val scan = scanBoundaries(magnitude, w, h, threshold)
        if (scan != null) {
            val quad = quadFromBoundaries(scan, w, h)
            if (quad != null) {
                return Detection(quad.corners, quad.confident)
            }
        }

        // ---- 2. fallback: extreme-point quad ------------------------------
        // Handles small/rotated documents where the "scan inward from the frame
        // edge" assumption does not hold.
        val extreme = extremePointQuad(magnitude, w, h, threshold)
        if (extreme != null) {
            return Detection(extreme, confident = false)
        }

        return null
    }

    /** Four fitted sides. A `null` fit means "use the frame edge". */
    private class BoundaryScan(
        val left: Line?,
        val right: Line?,
        val top: Line?,
        val bottom: Line?,
        val fittedSides: Int
    )

    private fun scanBoundaries(
        magnitude: FloatArray,
        w: Int,
        h: Int,
        threshold: Float
    ): BoundaryScan? {
        // Left/right: per row. Top/bottom: per column.
        val leftY = IntArray(h)
        val leftX = IntArray(h)
        var leftCount = 0
        val rightY = IntArray(h)
        val rightX = IntArray(h)
        var rightCount = 0
        val topX = IntArray(w)
        val topY = IntArray(w)
        var topCount = 0
        val bottomX = IntArray(w)
        val bottomY = IntArray(w)
        var bottomCount = 0

        val halfW = (w * 0.48f).toInt()
        val halfH = (h * 0.48f).toInt()

        for (y in 0 until h) {
            val row = y * w
            // First strong edge from the left.
            var x = 0
            while (x < w) {
                if (magnitude[row + x] >= threshold) break
                x++
            }
            if (x < w && x <= halfW) {
                leftY[leftCount] = y
                leftX[leftCount] = x
                leftCount++
            }
            // Last strong edge from the right.
            var rx = w - 1
            while (rx >= 0) {
                if (magnitude[row + rx] >= threshold) break
                rx--
            }
            if (rx >= 0 && rx >= w - halfW && rx > x) {
                rightY[rightCount] = y
                rightX[rightCount] = rx
                rightCount++
            }
        }

        for (x in 0 until w) {
            var y = 0
            while (y < h) {
                if (magnitude[y * w + x] >= threshold) break
                y++
            }
            if (y < h && y <= halfH) {
                topX[topCount] = x
                topY[topCount] = y
                topCount++
            }
            var by = h - 1
            while (by >= 0) {
                if (magnitude[by * w + x] >= threshold) break
                by--
            }
            if (by >= 0 && by >= h - halfH && by > y) {
                bottomX[bottomCount] = x
                bottomY[bottomCount] = by
                bottomCount++
            }
        }

        if (leftCount + rightCount + topCount + bottomCount < MIN_SIDE_INLIERS) return null

        val left = ransacLine(leftY, leftX, leftCount, h.toFloat(), fitXbyY = true)
        val right = ransacLine(rightY, rightX, rightCount, h.toFloat(), fitXbyY = true)
        val top = ransacLine(topX, topY, topCount, w.toFloat(), fitXbyY = false)
        val bottom = ransacLine(bottomX, bottomY, bottomCount, w.toFloat(), fitXbyY = false)

        val fitted = listOf(left, right, top, bottom).count { it != null }
        if (fitted == 0) return null
        return BoundaryScan(left, right, top, bottom, fitted)
    }

    /**
     * Assembles the quad from the four lines, substituting the frame edge for any
     * side that could not be fitted.
     */
    private fun quadFromBoundaries(scan: BoundaryScan, w: Int, h: Int): Pair<QuadCorners, Boolean>? {
        val wf = w.toFloat()
        val hf = h.toFloat()

        // x = a·y + b  (left/right),  y = a·x + b  (top/bottom)
        val left = scan.left ?: Line(a = 0f, b = 0f, fitXbyY = true)
        val right = scan.right ?: Line(a = 0f, b = wf - 1f, fitXbyY = true)
        val top = scan.top ?: Line(a = 0f, b = 0f, fitXbyY = false)
        val bottom = scan.bottom ?: Line(a = 0f, b = hf - 1f, fitXbyY = false)

        if (abs(left.a) > MAX_SLOPE || abs(right.a) > MAX_SLOPE) return null
        if (abs(top.a) > MAX_SLOPE || abs(bottom.a) > MAX_SLOPE) return null
        // Left edge must sit left of the right edge at both reference rows.
        if (left.b >= right.b - 4f) return null
        if (top.b >= bottom.b - 4f) return null

        val tl = intersectXY(left, top) ?: return null
        val tr = intersectXY(right, top) ?: return null
        val br = intersectXY(right, bottom) ?: return null
        val bl = intersectXY(left, bottom) ?: return null

        return validate(floatArrayOf(tl[0], tr[0], br[0], bl[0]), floatArrayOf(tl[1], tr[1], br[1], bl[1]), w, h)
            ?.let { it to (scan.fittedSides >= 3) }
    }

    /**
     * Extreme-point quad, kept as the fallback for documents that do not span the
     * frame. TL minimises `x+y`, BR maximises it, TR maximises `x−y`, BL minimises
     * it. Fails for documents rotated near 45°, where one corner wins two extremes;
     * that is acceptable because this is only the fallback path.
     */
    private fun extremePointQuad(
        magnitude: FloatArray,
        w: Int,
        h: Int,
        threshold: Float
    ): QuadCorners? {
        var topLeft = -1
        var topRight = -1
        var bottomRight = -1
        var bottomLeft = -1
        var minSum = Int.MAX_VALUE
        var maxSum = Int.MIN_VALUE
        var minDiff = Int.MAX_VALUE
        var maxDiff = Int.MIN_VALUE

        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                if (magnitude[row + x] < threshold) continue
                val sum = x + y
                val diff = x - y
                if (sum < minSum) { minSum = sum; topLeft = row + x }
                if (sum > maxSum) { maxSum = sum; bottomRight = row + x }
                if (diff > maxDiff) { maxDiff = diff; topRight = row + x }
                if (diff < minDiff) { minDiff = diff; bottomLeft = row + x }
            }
        }
        if (topLeft < 0 || topRight < 0 || bottomRight < 0 || bottomLeft < 0) return null
        val indices = intArrayOf(topLeft, topRight, bottomRight, bottomLeft)
        if (indices.toSet().size < 3) return null

        val xs = FloatArray(4) { (indices[it] % w).toFloat() }
        val ys = FloatArray(4) { (indices[it] / w).toFloat() }
        // Rough quad: expand slightly toward the frame so a thin rim is removed.
        return validate(xs, ys, w, h)
    }

    // --------------------------------------------------------------- geometry

    /** Shared validation + normalisation for both detection paths. */
    private fun validate(xs: FloatArray, ys: FloatArray, w: Int, h: Int): QuadCorners? {
        val wf = w.toFloat()
        val hf = h.toFloat()
        val tolerance = 0.02f

        for (i in 0..3) {
            if (xs[i] < -tolerance * wf || xs[i] > wf * (1f + tolerance)) return null
            if (ys[i] < -tolerance * hf || ys[i] > hf * (1f + tolerance)) return null
        }
        if (!isConvex(xs, ys)) return null

        val ratio = quadArea(xs, ys) / (wf * hf)
        if (ratio < MIN_AREA_RATIO || ratio > MAX_AREA_RATIO) return null

        val topLength = distance(xs[0], ys[0], xs[1], ys[1])
        val bottomLength = distance(xs[3], ys[3], xs[2], ys[2])
        val leftLength = distance(xs[0], ys[0], xs[3], ys[3])
        val rightLength = distance(xs[1], ys[1], xs[2], ys[2])
        if (topLength < MIN_SIDE_RATIO * wf || bottomLength < MIN_SIDE_RATIO * wf) return null
        if (leftLength < MIN_SIDE_RATIO * hf || rightLength < MIN_SIDE_RATIO * hf) return null

        // TL must be up and left of BR or the quad is crossed.
        if (ys[0] >= ys[2] || xs[0] >= xs[2]) return null

        val cx = (xs[0] + xs[1] + xs[2] + xs[3]) / 4f
        val cy = (ys[0] + ys[1] + ys[2] + ys[3]) / 4f
        val shrink = 1f - EDGE_INSET

        fun normX(v: Float) = (((v - cx) * shrink + cx) / wf).coerceIn(0f, 1f)
        fun normY(v: Float) = (((v - cy) * shrink + cy) / hf).coerceIn(0f, 1f)

        return QuadCorners(
            topLeftX = normX(xs[0]), topLeftY = normY(ys[0]),
            topRightX = normX(xs[1]), topRightY = normY(ys[1]),
            bottomRightX = normX(xs[2]), bottomRightY = normY(ys[2]),
            bottomLeftX = normX(xs[3]), bottomLeftY = normY(ys[3])
        )
    }

    /**
     * A line fitted from scan points.
     *
     * @param fitXbyY true when the points are `(y, x)` and we model `x = a·y + b`
     *   (left/right edges); false for `(x, y)` → `y = a·x + b` (top/bottom).
     */
    private class Line(val a: Float, val b: Float, val fitXbyY: Boolean)

    /**
     * RANSAC fit of dependent = a·independent + b, then a least-squares refit on
     * the inliers. RANSAC is what makes this survive a few rows where the scan hit
     * a shadow, a curled corner, or a text line before the paper edge.
     */
    private fun ransacLine(
        indep: IntArray,
        dep: IntArray,
        count: Int,
        scale: Float,
        fitXbyY: Boolean
    ): Line? {
        if (count < MIN_SIDE_INLIERS) return null
        val tolerance = RANSAC_TOLERANCE * scale

        var bestA = 0f
        var bestB = 0f
        var bestInliers = -1
        var bestError = Float.MAX_VALUE

        // Deterministic pseudo-random pairs: reproducible output for the same photo,
        // which matters when a user retries detection and expects the same quad.
        var seed = 0x9E3779B9.toInt() xor count xor (scale.toInt() shl 3)
        fun nextInt(bound: Int): Int {
            seed = seed * 1103515245 + 12345
            val v = (seed ushr 16) and 0x7FFF
            return v % bound
        }

        for (iteration in 0 until RANSAC_ITERATIONS) {
            val i = nextInt(count)
            val j = nextInt(count)
            if (i == j) continue
            val x1 = indep[i].toFloat()
            val y1 = dep[i].toFloat()
            val x2 = indep[j].toFloat()
            val y2 = dep[j].toFloat()
            if (abs(x2 - x1) < 1e-3f) continue

            val a = (y2 - y1) / (x2 - x1)
            if (abs(a) > MAX_SLOPE) continue
            val b = y1 - a * x1

            var inliers = 0
            var error = 0f
            for (k in 0 until count) {
                val predicted = a * indep[k] + b
                val residual = abs(dep[k] - predicted)
                if (residual <= tolerance) {
                    inliers++
                    error += residual
                }
            }
            if (inliers > bestInliers || (inliers == bestInliers && error < bestError)) {
                bestInliers = inliers
                bestError = error
                bestA = a
                bestB = b
            }
        }

        val minimum = max(MIN_SIDE_INLIERS, (count * 0.25f).toInt())
        if (bestInliers < minimum) return null

        // Least-squares refit on the inlier set for a sub-pixel line.
        var sumX = 0.0
        var sumY = 0.0
        var sumXX = 0.0
        var sumXY = 0.0
        var n = 0
        for (k in 0 until count) {
            val residual = abs(dep[k] - (bestA * indep[k] + bestB))
            if (residual > tolerance) continue
            val x = indep[k].toDouble()
            val y = dep[k].toDouble()
            sumX += x
            sumY += y
            sumXX += x * x
            sumXY += x * y
            n++
        }
        if (n < MIN_SIDE_INLIERS) return Line(bestA, bestB, fitXbyY)

        val denominator = n * sumXX - sumX * sumX
        if (abs(denominator) < 1e-6) return Line(bestA, bestB, fitXbyY)
        val a = ((n * sumXY - sumX * sumY) / denominator).toFloat()
        val b = ((sumY - a * sumX) / n).toFloat()
        if (!a.isFinite() || !b.isFinite() || abs(a) > MAX_SLOPE) return Line(bestA, bestB, fitXbyY)
        return Line(a, b, fitXbyY)
    }

    /**
     * Intersection of a left/right line (`x = a₁y + b₁`) and a top/bottom line
     * (`y = a₂x + b₂`), returned as `[x, y]`.
     */
    private fun intersectXY(vertical: Line, horizontal: Line): FloatArray? {
        val denominator = 1f - horizontal.a * vertical.a
        if (abs(denominator) < 1e-3f) return null
        val y = (horizontal.a * vertical.b + horizontal.b) / denominator
        val x = vertical.a * y + vertical.b
        if (!x.isFinite() || !y.isFinite()) return null
        return floatArrayOf(x, y)
    }

    // ------------------------------------------------------------ image maths

    private fun sobel(gray: FloatArray, w: Int, h: Int): FloatArray {
        val magnitude = FloatArray(w * h)
        for (y in 0 until h) {
            val ym1 = if (y > 0) y - 1 else 0
            val yp1 = if (y < h - 1) y + 1 else h - 1
            val rowM = y * w
            val rowUp = ym1 * w
            val rowDown = yp1 * w
            for (x in 0 until w) {
                val xm1 = if (x > 0) x - 1 else 0
                val xp1 = if (x < w - 1) x + 1 else w - 1

                val tl = gray[rowUp + xm1]
                val tc = gray[rowUp + x]
                val tr = gray[rowUp + xp1]
                val ml = gray[rowM + xm1]
                val mr = gray[rowM + xp1]
                val bl = gray[rowDown + xm1]
                val bc = gray[rowDown + x]
                val br = gray[rowDown + xp1]

                val dx = (tr + 2f * mr + br) - (tl + 2f * ml + bl)
                val dy = (bl + 2f * bc + br) - (tl + 2f * tc + tr)
                magnitude[rowM + x] = sqrt(dx * dx + dy * dy)
            }
        }
        return magnitude
    }

    /**
     * Gradient threshold. Lower than the previous `p995 × 0.28` (which could exceed
     * the magnitude of a soft paper edge), and floored so a blurry, low-contrast
     * photo does not turn noise into edges.
     */
    private fun edgeThreshold(magnitude: FloatArray): Float {
        val sorted = magnitude.copyOf()
        sorted.sort()
        val p75 = sorted[(sorted.size * 0.75f).toInt().coerceIn(0, sorted.lastIndex)]
        val p97 = sorted[(sorted.size * 0.97f).toInt().coerceIn(0, sorted.lastIndex)]
        return max(EDGE_FLOOR, max(p75 * 0.85f, p97 * 0.18f))
    }

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
}
