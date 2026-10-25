package com.example.docscanner.util

import android.graphics.Bitmap
import com.example.docscanner.model.QuadCorners
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Document-border detection: finds the four corners of the page in a photo.
 *
 * ## History
 * 1. **Brightness bounding box** — could not express perspective at all.
 * 2. **Extreme-point quad over Sobel edges** — the page edge competed with
 *    interior text, so a page that filled the frame produced an invalid quad and
 *    silently fell back to "no crop".
 * 3. **First-edge-from-the-frame scan** — better on tilted pages, but it took the
 *    *first* strong gradient along a row, so dense text won over the paper edge.
 *    With a page filling the frame it happily cut a crop at the bottom of the
 *    text block and called itself confident, which is worse than not detecting.
 * 4. **This version: background-step detection.** The photo is reduced to a
 *    "background" image — grey, closed morphologically (which erases ink), box
 *    blurred — and each side of the page is located as the *50 % level crossing*
 *    between the paper and whatever it lies on. Ink cannot satisfy that test, so
 *    a text block can no longer masquerade as a page.
 *
 * ## Why the level crossing, and not the strongest gradient
 * A page edge is a step blurred by the optics. The 50 % crossing of a blurred
 * step is exactly the step position, so interpolation between two pixels gives a
 * sub-pixel, unbiased answer. The maximum of a finite difference is *not*
 * unbiased: across the ramp of a blurred step the difference is flat for several
 * pixels, so its peak wanders by up to half a centimetre on the working image.
 * Earlier revisions used thresholds on a first crossing and drifted outward or
 * inward by ~4 % of the frame — a visible dark rim, or lost content.
 *
 * ## Run-off sides
 * When the page continues past the frame edge there is no crossing to find, and
 * the side stays unfitted. [quadFrom] then substitutes the frame edge, which is
 * the geometrically correct answer. That is why [Detection.confident] separates
 * "found three or four real sides" from "some sides are assumptions".
 *
 * Detection never throws. Everything is validated for convexity, area, side
 * length and slope before it is returned.
 */
object DocumentDetector {

    /** Resolution the geometry runs at. Going wider buys nothing measurable. */
    private const val WORK_WIDTH = 360

    /** The text-erasing closing runs on a half-width copy of the working image. */
    private const val CLOSE_DIVISOR = 2

    /** Below this the source is too small to say anything useful. */
    private const val MIN_SOURCE_SIDE = 48

    /** RANSAC iterations per side. */
    private const val RANSAC_ITERATIONS = 200

    /** Inlier tolerance as a fraction of the axis the side spans. */
    private const val RANSAC_TOLERANCE = 0.008f

    /** A side's line must be supported by this fraction of its candidate points. */
    private const val MIN_INLIER_FRACTION = 0.30f

    /** …and by at least this many points, whatever the fraction says. */
    private const val MIN_INLIER_POINTS = 10

    /** Fewer candidate points than this cannot define a line. */
    private const val MIN_CANDIDATE_POINTS = 10

    /** A document must cover at least this fraction of the frame. */
    private const val MIN_AREA_RATIO = 0.06f

    /** Anything above the frame is clamped, so this is only a sanity ceiling. */
    private const val MAX_AREA_RATIO = 1.05f

    /** Each side must span at least this fraction of its axis. */
    private const val MIN_SIDE_RATIO = 0.18f

    /** Steepest allowed side slope (dependent/independent). ~58°. */
    private const val MAX_SLOPE = 1.6f

    /** Corners are pulled this far toward the centre to avoid a dark rim. */
    private const val EDGE_INSET = 0.004f

    /** A fitted side this close to the frame is snapped onto it. */
    private const val SNAP = 0.010f

    /** All four corners this close to the frame means "nothing to crop". */
    private const val FULL_FRAME_TOLERANCE = 0.015f

    /** Smaller than this and the photo has no usable contrast at all. */
    private const val MIN_CONTRAST = 8f

    /** A page is assumed brighter than the surface it lies on unless it is dark. */
    private const val DARK_SCENE_LEVEL = 128f

    /** Result of a detection attempt. */
    data class Detection(
        val corners: QuadCorners,
        /**
         * True when at least three of the four sides were fitted from real
         * background steps. False means one or more sides fell back to the frame
         * edge, or nothing plausible was found — the UI should invite manual
         * adjustment.
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
        detectInternal(src)
    } catch (_: Throwable) {
        failed()
    }

    /** Convenience for call sites that only need the corners. */
    fun detectQuad(src: Bitmap): QuadCorners = detect(src).corners

    // ------------------------------------------------------------------ core

    /** A single-channel image at an arbitrary resolution. */
    private class Grey(val w: Int, val h: Int, val px: FloatArray)

    /** Candidate points of one side: `dep = a·indep + b`. */
    private class Points(val indep: FloatArray, val dep: FloatArray, val count: Int)

    /** A fitted side, `dep = a·indep + b`. */
    private class Line(val a: Float, val b: Float)

    private enum class Side { LEFT, RIGHT, TOP, BOTTOM }

    private fun failed() = Detection(QuadCorners.FULL_FRAME, confident = false)

    private fun detectInternal(src: Bitmap): Detection {
        if (src.isRecycled) return failed()
        val working = greyOf(src) ?: return failed()

        // The closing runs on a half-width copy: its only job is to erase ink,
        // which is a coarse job, and doing it there makes it four times cheaper.
        val smallRadius = closeRadius(working.w / CLOSE_DIVISOR)
        val blurRadius = max(2, smallRadius)
        val bg = background(working, smallRadius, blurRadius)

        // Contrast of the background image, by histogram — no sort of the whole
        // frame, which used to cost more than everything else put together.
        val lo = percentile(bg, 0.03f)
        val hi = percentile(bg, 0.97f)
        val span = hi - lo
        if (span < MIN_CONTRAST) {
            // Nothing but one tone in view. Either the page fills the frame or
            // there is no page; in both cases the full frame is the right answer
            // and there is nothing for the user to adjust.
            return Detection(QuadCorners.FULL_FRAME, confident = true)
        }

        val threshold = max(4f, 0.18f * span)
        val mid = 0.5f * (lo + hi)
        // Wide enough to step over the ramp the blur created, so the step test
        // compares paper against surroundings rather than two points on a slope.
        val band = 2 * blurRadius

        // A bright page on a darker surface is the overwhelmingly common case, so
        // that polarity is tried first and on its own. The inverted polarity is a
        // last resort for a genuinely dark document, and is only attempted when
        // the bright or mid tones are actually dark — otherwise a darker patch
        // *inside* a bright page could be mistaken for the page itself.
        val polarities = if (hi < DARK_SCENE_LEVEL) floatArrayOf(1f, -1f) else floatArrayOf(1f)
        for (polarity in polarities) {
            val quad = quadFrom(bg, threshold, mid, band, polarity)
            if (quad != null) return quad
        }
        return failed()
    }

    /** Builds the background image: ink erased, then a gentle blur for the ramps. */
    private fun background(working: Grey, smallRadius: Int, blurRadius: Int): Grey {
        val small = halve(working)
        val closed = close(small, smallRadius)
        val grown = upscale(closed, working.w, working.h)
        return Grey(grown.w, grown.h, boxBlur(grown.px, grown.w, grown.h, blurRadius))
    }

    /**
     * Closing radius for an image of the given width.
     *
     * Body text at this scale has strokes 1–2 px wide with a 5 px pitch, so a
     * radius of 3 erases ink with room to spare. Larger radii erased thin desk
     * margins too — a page inset by less than the window then looked like it ran
     * off the frame, and the crop stopped short of the paper edge.
     */
    private fun closeRadius(width: Int): Int = max(2, (width / 60f).roundToInt())

    // ------------------------------------------------------- side extraction

    /**
     * Finds candidate points for one side.
     *
     * Walking inward from the frame edge, the first place where the background
     * tone crosses [mid] is the paper's boundary — provided the step across it is
     * real ([threshold]), which rejects noise and slow shadows. The crossing is
     * interpolated between the two samples that straddle it.
     *
     * @param polarity +1 when the page is brighter than its surroundings, −1 when
     *   it is darker. Every side shares the polarity; only the walk direction
     *   differs.
     */
    private fun scanSide(
        bg: Grey,
        side: Side,
        threshold: Float,
        mid: Float,
        band: Int,
        polarity: Float
    ): Points {
        val horizontal = side == Side.LEFT || side == Side.RIGHT
        val inward = if (side == Side.LEFT || side == Side.TOP) 1 else -1
        val span = if (horizontal) bg.w else bg.h
        val rows = if (horizontal) bg.h else bg.w
        val stride = if (horizontal) 1 else bg.w
        val half = span / 2
        val start = if (inward > 0) 0 else span - 1
        // A side is only searched in the half of the frame nearest to it: a
        // crossing found beyond the middle belongs to some other edge, and
        // accepting it was how text blocks used to win.
        val limit = if (inward > 0) half else span - half - 1

        val indep = FloatArray(rows)
        val dep = FloatArray(rows)
        var count = 0

        for (row in 0 until rows) {
            val base = if (horizontal) row * bg.w else row
            val px = bg.px
            // Already on the page side of the crossing at the frame edge: the page
            // runs off the frame, so this row contributes nothing.
            if (polarity * (px[base + start * stride] - mid) >= 0f) continue

            var previous = start
            var p = start
            while (true) {
                if (polarity * (px[base + p * stride] - mid) >= 0f) {
                    val before = px[base + previous * stride]
                    val after = px[base + p * stride]
                    val t = if (after == before) 0f else (mid - before) / (after - before)
                    val edge = previous + t * (p - previous)
                    val nearest = edge.roundToInt()
                    val outer = nearest - inward * band
                    val inner = nearest + inward * band
                    if (outer < 0 || inner < 0 || outer >= span || inner >= span) break
                    if (polarity * (px[base + inner * stride] - px[base + outer * stride]) >= threshold) {
                        indep[count] = row.toFloat()
                        dep[count] = edge
                        count++
                    }
                    break
                }
                previous = p
                p += inward
                if (inward > 0) {
                    if (p > limit) break
                } else {
                    if (p < limit) break
                }
            }
        }
        return Points(indep, dep, count)
    }

    /** Fits the four sides, then assembles and validates the quad. */
    private fun quadFrom(
        bg: Grey,
        threshold: Float,
        mid: Float,
        band: Int,
        polarity: Float
    ): Detection? {
        val w = bg.w
        val h = bg.h
        val wf = w.toFloat()
        val hf = h.toFloat()

        val left = fitSide(scanSide(bg, Side.LEFT, threshold, mid, band, polarity), hf)
        val right = fitSide(scanSide(bg, Side.RIGHT, threshold, mid, band, polarity), hf)
        val top = fitSide(scanSide(bg, Side.TOP, threshold, mid, band, polarity), wf)
        val bottom = fitSide(scanSide(bg, Side.BOTTOM, threshold, mid, band, polarity), wf)
        val fitted = listOf(left, right, top, bottom).count { it != null }
        if (fitted == 0) return null

        // Unfitted sides keep the frame edge: the page runs past it.
        val leftN = left?.snapNear(0f, wf) ?: Line(0f, 0f)
        val rightN = right?.snapNear(wf - 1f, wf) ?: Line(0f, wf - 1f)
        val topN = top?.snapNear(0f, hf) ?: Line(0f, 0f)
        val bottomN = bottom?.snapNear(hf - 1f, hf) ?: Line(0f, hf - 1f)
        for (line in listOf(leftN, rightN, topN, bottomN)) {
            if (abs(line.a) > MAX_SLOPE) return null
        }

        val topLeft = intersect(leftN, topN) ?: return null
        val topRight = intersect(rightN, topN) ?: return null
        val bottomRight = intersect(rightN, bottomN) ?: return null
        val bottomLeft = intersect(leftN, bottomN) ?: return null

        val corners = validate(
            floatArrayOf(topLeft[0], topRight[0], bottomRight[0], bottomLeft[0]),
            floatArrayOf(topLeft[1], topRight[1], bottomRight[1], bottomLeft[1]),
            w,
            h
        ) ?: return null

        return Detection(corners, confident = fitted >= 3)
    }

    /** Pulls a side onto the frame edge when it is already within a hair of it. */
    private fun Line.snapNear(target: Float, span: Float): Line =
        if (abs(b - target) < SNAP * span) Line(a, target) else this

    /**
     * RANSAC fit of `dep = a·indep + b`, followed by a least-squares refit on the
     * inliers. Survives rows where the scan hit a shadow, a finger, or a curl.
     */
    private fun fitSide(points: Points, axisLength: Float): Line? {
        val n = points.count
        if (n < MIN_CANDIDATE_POINTS) return null
        val tolerance = RANSAC_TOLERANCE * axisLength

        var bestA = 0f
        var bestB = 0f
        var bestInliers = -1
        var bestError = Float.MAX_VALUE

        // Deterministic pseudo-random pairs: re-detecting the same photo must give
        // the same quad, or "detect borders" would jitter when pressed twice.
        var seed = 0x9E3779B9.toInt() xor n xor (axisLength.toInt() shl 3)
        fun nextIndex(bound: Int): Int {
            seed = seed * 1103515245 + 12345
            return ((seed ushr 16) and 0x7FFF) % bound
        }

        for (iteration in 0 until RANSAC_ITERATIONS) {
            val i = nextIndex(n)
            val j = nextIndex(n)
            if (i == j) continue
            val x1 = points.indep[i]
            val y1 = points.dep[i]
            val x2 = points.indep[j]
            val y2 = points.dep[j]
            if (abs(x2 - x1) < 1e-3f) continue
            val a = (y2 - y1) / (x2 - x1)
            if (abs(a) > MAX_SLOPE) continue
            val b = y1 - a * x1

            var inliers = 0
            var error = 0f
            for (k in 0 until n) {
                val residual = abs(points.dep[k] - (a * points.indep[k] + b))
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

        if (bestInliers < max(MIN_INLIER_POINTS, (n * MIN_INLIER_FRACTION).toInt())) return null
        return refit(points, bestA, bestB, tolerance)
    }

    /** Least-squares refit on the inlier set for a sub-pixel line. */
    private fun refit(points: Points, a: Float, b: Float, tolerance: Float): Line {
        var sumX = 0.0
        var sumY = 0.0
        var sumXX = 0.0
        var sumXY = 0.0
        var n = 0
        for (k in 0 until points.count) {
            if (abs(points.dep[k] - (a * points.indep[k] + b)) > tolerance) continue
            val x = points.indep[k].toDouble()
            val y = points.dep[k].toDouble()
            sumX += x
            sumY += y
            sumXX += x * x
            sumXY += x * y
            n++
        }
        if (n < MIN_INLIER_POINTS) return Line(a, b)
        val denominator = n * sumXX - sumX * sumX
        if (abs(denominator) < 1e-6) return Line(a, b)
        val slope = ((n * sumXY - sumX * sumY) / denominator).toFloat()
        val intercept = ((sumY - slope * sumX) / n).toFloat()
        if (!slope.isFinite() || !intercept.isFinite() || abs(slope) > MAX_SLOPE) return Line(a, b)
        return Line(slope, intercept)
    }

    /** Intersection of a left/right line and a top/bottom line, as `[x, y]`. */
    private fun intersect(vertical: Line, horizontal: Line): FloatArray? {
        val denominator = 1f - horizontal.a * vertical.a
        if (abs(denominator) < 1e-3f) return null
        val y = (horizontal.a * vertical.b + horizontal.b) / denominator
        val x = vertical.a * y + vertical.b
        if (!x.isFinite() || !y.isFinite()) return null
        return floatArrayOf(x, y)
    }

    // --------------------------------------------------------------- geometry

    /** Shared validation + normalisation. Returns `null` when implausible. */
    private fun validate(xs: FloatArray, ys: FloatArray, w: Int, h: Int): QuadCorners? {
        val wf = w.toFloat()
        val hf = h.toFloat()
        val tolerance = 0.02f
        for (i in 0..3) {
            if (xs[i] < -tolerance * wf || xs[i] > wf * (1f + tolerance)) return null
            if (ys[i] < -tolerance * hf || ys[i] > hf * (1f + tolerance)) return null
        }

        val ordered = orderCorners(xs, ys)
        val ox = ordered[0]
        val oy = ordered[1]

        val area = quadArea(ox, oy)
        if (area / (wf * hf) < MIN_AREA_RATIO) return null
        if (area / (wf * hf) > MAX_AREA_RATIO) return null

        val top = hypot(ox[1] - ox[0], oy[1] - oy[0])
        val bottom = hypot(ox[2] - ox[3], oy[2] - oy[3])
        val left = hypot(ox[3] - ox[0], oy[3] - oy[0])
        val right = hypot(ox[2] - ox[1], oy[2] - oy[1])
        if (top < MIN_SIDE_RATIO * wf || bottom < MIN_SIDE_RATIO * wf) return null
        if (left < MIN_SIDE_RATIO * hf || right < MIN_SIDE_RATIO * hf) return null

        var cx = 0f
        var cy = 0f
        for (i in 0..3) {
            cx += ox[i]
            cy += oy[i]
        }
        cx /= 4f
        cy /= 4f
        val shrink = 1f - EDGE_INSET
        fun normX(v: Float) = ((((v - cx) * shrink) + cx) / wf).coerceIn(0f, 1f)
        fun normY(v: Float) = ((((v - cy) * shrink) + cy) / hf).coerceIn(0f, 1f)

        val quad = QuadCorners(
            topLeftX = normX(ox[0]), topLeftY = normY(oy[0]),
            topRightX = normX(ox[1]), topRightY = normY(oy[1]),
            bottomRightX = normX(ox[2]), bottomRightY = normY(oy[2]),
            bottomLeftX = normX(ox[3]), bottomLeftY = normY(oy[3])
        )
        val nearFrame = quad.topLeftX <= FULL_FRAME_TOLERANCE &&
            quad.topLeftY <= FULL_FRAME_TOLERANCE &&
            quad.topRightX >= 1f - FULL_FRAME_TOLERANCE &&
            quad.topRightY <= FULL_FRAME_TOLERANCE &&
            quad.bottomRightX >= 1f - FULL_FRAME_TOLERANCE &&
            quad.bottomRightY >= 1f - FULL_FRAME_TOLERANCE &&
            quad.bottomLeftX <= FULL_FRAME_TOLERANCE &&
            quad.bottomLeftY >= 1f - FULL_FRAME_TOLERANCE
        return if (nearFrame) QuadCorners.FULL_FRAME else quad
    }

    /**
     * Returns the corners as `[xs, ys]` in top-left, top-right, bottom-right,
     * bottom-left order.
     *
     * Angular ordering around the centroid is exact for a convex quad, and it
     * replaces the old "top-left must be above and left of bottom-right" check,
     * which rejected genuinely full-frame pages and fired on near-square crops.
     */
    private fun orderCorners(xs: FloatArray, ys: FloatArray): Array<FloatArray> {
        var cx = 0f
        var cy = 0f
        for (i in 0..3) {
            cx += xs[i]
            cy += ys[i]
        }
        cx /= 4f
        cy /= 4f
        val order = (0..3).sortedBy { atan2(ys[it] - cy, xs[it] - cx) }.toIntArray()
        // Rotate so the first corner is the top-left one.
        var startIndex = 0
        var bestSum = Float.MAX_VALUE
        for (k in 0..3) {
            val sum = xs[order[k]] + ys[order[k]]
            if (sum < bestSum) {
                bestSum = sum
                startIndex = k
            }
        }
        val ox = FloatArray(4)
        val oy = FloatArray(4)
        for (k in 0..3) {
            val index = order[(startIndex + k) % 4]
            ox[k] = xs[index]
            oy[k] = ys[index]
        }
        // Angular order is counter-clockwise on screen; flip it to TL→TR→BR→BL.
        val cross = (ox[1] - ox[0]) * (oy[3] - oy[0]) - (oy[1] - oy[0]) * (ox[3] - ox[0])
        if (cross < 0f) {
            val flippedX = floatArrayOf(ox[0], ox[3], ox[2], ox[1])
            val flippedY = floatArrayOf(oy[0], oy[3], oy[2], oy[1])
            return arrayOf(flippedX, flippedY)
        }
        return arrayOf(ox, oy)
    }

    private fun quadArea(xs: FloatArray, ys: FloatArray): Float {
        var sum = 0f
        for (i in 0..3) {
            val j = (i + 1) % 4
            sum += xs[i] * ys[j] - xs[j] * ys[i]
        }
        return abs(sum) / 2f
    }

    // ----------------------------------------------------------- image maths

    /** Reads the bitmap at working resolution and converts it to luma. */
    private fun greyOf(src: Bitmap): Grey? {
        val workingWidth = min(src.width, WORK_WIDTH)
        if (workingWidth < MIN_SOURCE_SIDE) return null
        val workingHeight = max(
            MIN_SOURCE_SIDE,
            (src.height * (workingWidth.toFloat() / src.width)).roundToInt()
        )
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
        val luma = FloatArray(w * h)
        for (i in pixels.indices) {
            val p = pixels[i]
            luma[i] = 0.299f * ((p ushr 16) and 0xFF) +
                0.587f * ((p ushr 8) and 0xFF) +
                0.114f * (p and 0xFF)
        }
        return Grey(w, h, luma)
    }

    /** 2×2 mean downsample. Cheaper and less aliased than a second bitmap scale. */
    private fun halve(src: Grey): Grey {
        val w = max(1, src.w / CLOSE_DIVISOR)
        val h = max(1, src.h / CLOSE_DIVISOR)
        val out = FloatArray(w * h)
        for (y in 0 until h) {
            val rowA = (y * 2) * src.w
            val rowB = min(y * 2 + 1, src.h - 1) * src.w
            for (x in 0 until w) {
                val xa = x * 2
                val xb = min(x * 2 + 1, src.w - 1)
                out[y * w + x] =
                    (src.px[rowA + xa] + src.px[rowA + xb] + src.px[rowB + xa] + src.px[rowB + xb]) * 0.25f
            }
        }
        return Grey(w, h, out)
    }

    /** Grey-scale closing: erases dark structures thinner than `2r + 1`, i.e. ink. */
    private fun close(src: Grey, radius: Int): Grey {
        if (radius <= 0) return src
        return erode(dilate(src, radius), radius)
    }

    private fun dilate(src: Grey, radius: Int): Grey {
        val horizontal = FloatArray(src.px.size)
        val w = src.w
        val h = src.h
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                val from = max(0, x - radius)
                val to = min(w - 1, x + radius)
                var value = src.px[row + x]
                for (k in from..to) {
                    if (src.px[row + k] > value) value = src.px[row + k]
                }
                horizontal[row + x] = value
            }
        }
        val out = FloatArray(src.px.size)
        for (y in 0 until h) {
            val from = max(0, y - radius)
            val to = min(h - 1, y + radius)
            for (x in 0 until w) {
                var value = horizontal[y * w + x]
                for (k in from..to) {
                    if (horizontal[k * w + x] > value) value = horizontal[k * w + x]
                }
                out[y * w + x] = value
            }
        }
        return Grey(w, h, out)
    }

    private fun erode(src: Grey, radius: Int): Grey {
        val horizontal = FloatArray(src.px.size)
        val w = src.w
        val h = src.h
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                val from = max(0, x - radius)
                val to = min(w - 1, x + radius)
                var value = src.px[row + x]
                for (k in from..to) {
                    if (src.px[row + k] < value) value = src.px[row + k]
                }
                horizontal[row + x] = value
            }
        }
        val out = FloatArray(src.px.size)
        for (y in 0 until h) {
            val from = max(0, y - radius)
            val to = min(h - 1, y + radius)
            for (x in 0 until w) {
                var value = horizontal[y * w + x]
                for (k in from..to) {
                    if (horizontal[k * w + x] < value) value = horizontal[k * w + x]
                }
                out[y * w + x] = value
            }
        }
        return Grey(w, h, out)
    }

    /** Bilinear upscale of the closed background to the working resolution. */
    private fun upscale(src: Grey, w: Int, h: Int): Grey {
        if (src.w == w && src.h == h) return src
        val out = FloatArray(w * h)
        val scaleX = src.w.toFloat() / w
        val scaleY = src.h.toFloat() / h
        for (y in 0 until h) {
            val sy = ((y + 0.5f) * scaleY) - 0.5f
            val y0 = sy.toInt().coerceIn(0, src.h - 1)
            val y1 = (y0 + 1).coerceAtMost(src.h - 1)
            val fy = (sy - y0).coerceIn(0f, 1f)
            for (x in 0 until w) {
                val sx = ((x + 0.5f) * scaleX) - 0.5f
                val x0 = sx.toInt().coerceIn(0, src.w - 1)
                val x1 = (x0 + 1).coerceAtMost(src.w - 1)
                val fx = (sx - x0).coerceIn(0f, 1f)
                val top = src.px[y0 * src.w + x0] * (1f - fx) + src.px[y0 * src.w + x1] * fx
                val bottom = src.px[y1 * src.w + x0] * (1f - fx) + src.px[y1 * src.w + x1] * fx
                out[y * w + x] = top * (1f - fy) + bottom * fy
            }
        }
        return Grey(w, h, out)
    }

    /** Separable box blur with clamped edges. */
    private fun boxBlur(px: FloatArray, w: Int, h: Int, radius: Int): FloatArray {
        if (radius <= 0) return px
        val horizontal = FloatArray(px.size)
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                val from = max(0, x - radius)
                val to = min(w - 1, x + radius)
                var sum = 0f
                for (k in from..to) sum += px[row + k]
                horizontal[row + x] = sum / (to - from + 1)
            }
        }
        val out = FloatArray(px.size)
        for (y in 0 until h) {
            val from = max(0, y - radius)
            val to = min(h - 1, y + radius)
            for (x in 0 until w) {
                var sum = 0f
                for (k in from..to) sum += horizontal[k * w + x]
                out[y * w + x] = sum / (to - from + 1)
            }
        }
        return out
    }

    /** Value at the given quantile, from a 256-bin histogram. */
    private fun percentile(bg: Grey, quantile: Float): Float {
        val histogram = IntArray(256)
        for (value in bg.px) {
            histogram[value.toInt().coerceIn(0, 255)]++
        }
        val target = (bg.px.size * quantile).toInt().coerceIn(0, bg.px.size - 1)
        var seen = 0
        for (bin in 0 until 256) {
            seen += histogram[bin]
            if (seen > target) return bin.toFloat()
        }
        return 255f
    }
}
