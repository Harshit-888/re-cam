package com.example.docscanner.util

import com.example.docscanner.model.QuadCorners
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * The geometry of document-border detection, with no Android type in it.
 *
 * This is the part of [DocumentDetector] that decides *where the page is*: the
 * background image, the edge crossings, the RANSAC line fits and the quad
 * validation. It is separated out for one reason — so it can be **tested**.
 * Detection is the feature users judge the app by, and until now the only way to
 * find out whether it worked was to take a photograph of a piece of paper on a
 * real phone. `DocumentQuadTest` now feeds it synthetic pages and asserts on the
 * corners it returns.
 *
 * [DocumentDetector] keeps the `Bitmap` handling: decode, downscale to
 * [DocumentDetector.WORK_WIDTH], read the pixels, call this, wrap the result.
 *
 * ## What changed from the previous revision, and why
 *
 * Both were reproduced with an independent implementation of the old algorithm on
 * synthetic scenes, so they are not guesses:
 *
 * 1. **Each side was only searched in the half of the frame nearest to it.** A
 *    page that sat off-centre therefore had no left edge to find, the left side
 *    fell back to the frame edge, and the result was a quad displaced by nearly
 *    half the frame — a confidently wrong crop, which is worse than no crop.
 *    Sides are now scanned across the whole span. "The first real step walking in
 *    from the frame edge" was always the outermost edge anyway.
 * 2. **The inverted polarity was only tried when the whole frame was dark.** A
 *    dark document on a light table — a form, a menu, a phone screen — was
 *    therefore never detected at all. Both polarities are now tried, always, and
 *    the one that fits more sides wins.
 *
 * A third change is not a bug fix but a robustness one: three step thresholds are
 * tried rather than one, because a white page on a light desk produces a genuine
 * step that is small in absolute terms, and one threshold cannot serve both that
 * and a page on a black table. The hypotheses are evaluated in order of
 * preference and the first one that fits all four sides wins, so the common case
 * still costs exactly one pass.
 */
internal object DocumentQuad {

    /** The corners, plus how many sides were fitted from real edges (0–4). */
    data class Result(val corners: QuadCorners, val sidesFound: Int)

    /** A single-channel image at an arbitrary resolution. */
    private class Grey(val w: Int, val h: Int, val px: FloatArray)

    /** Candidate points of one side: `dep = a·indep + b`. */
    private class Points(val indep: FloatArray, val dep: FloatArray, val count: Int)

    /** A fitted side, `dep = a·indep + b`. */
    private class Line(val a: Float, val b: Float)

    private enum class Side { LEFT, RIGHT, TOP, BOTTOM }

    /**
     * Step thresholds, as a fraction of the background's dynamic range.
     *
     * 0.18 is the historical value and handles the ordinary page-on-a-desk
     * photograph. 0.30 recovers a page that lies on something nearly as bright as
     * itself, where 18 % of the range would accept a shadow as an edge. 0.10 is the
     * last resort for a page in dim light, where the whole range is compressed.
     */
    private val THRESHOLD_FRACTIONS = floatArrayOf(0.18f, 0.30f, 0.10f)

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

    /**
     * Finds the page in [luma], a greyscale copy of the frame.
     *
     * @param luma row-major brightness, 0–255, at least `width * height` long.
     * @return the quad and how many sides were real, `null` when nothing plausible
     *   was found, or a full-frame result with `sidesFound = 0` when the frame is
     *   featureless — which is what a page filling the frame looks like, and is the
     *   correct answer for it.
     */
    fun detect(luma: FloatArray, width: Int, height: Int): Result? {
        if (width < MIN_SOURCE_SIDE || height < MIN_SOURCE_SIDE) return null
        if (luma.size < width * height) return null

        val working = Grey(width, height, luma)
        val smallRadius = closeRadius(width / CLOSE_DIVISOR)
        val blurRadius = max(2, smallRadius)
        val bg = background(working, smallRadius, blurRadius)

        // Contrast of the background image, by histogram — no sort of the whole
        // frame, which used to cost more than everything else put together.
        val lo = percentile(bg, 0.03f)
        val hi = percentile(bg, 0.97f)
        val span = hi - lo
        if (span < MIN_CONTRAST) {
            // Nothing but one tone in view. Either the page fills the frame or
            // there is no page; in full both are the same picture, and a crop is
            // not something the user could plausibly want.
            return Result(QuadCorners.FULL_FRAME, sidesFound = 0)
        }

        val mid = 0.5f * (lo + hi)
        // Wide enough to step over the ramp the blur created, so the step test
        // compares paper against surroundings rather than two points on a slope.
        val band = 2 * blurRadius

        var best: Result? = null
        // Bright-on-dark first: it is the overwhelmingly common case, and keeping
        // it first means the ordinary photograph is decided by the first pass.
        for (polarity in floatArrayOf(1f, -1f)) {
            for (fraction in THRESHOLD_FRACTIONS) {
                val threshold = max(4f, fraction * span)
                val candidate = quadFrom(bg, threshold, mid, band, polarity) ?: continue
                val previous = best
                if (previous == null || candidate.sidesFound > previous.sidesFound) {
                    best = candidate
                    // Four real sides cannot be improved on; stop paying for the rest.
                    if (candidate.sidesFound >= 4) return candidate
                }
            }
        }
        return best
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
     * interpolated between the two samples that straddle it, which is unbiased:
     * the 50 % point of a blurred step *is* the step.
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
        val start = if (inward > 0) 0 else span - 1
        // The whole span, not half of it: a page can sit anywhere in the frame,
        // and "walking in from the edge, the first real step" already picks the
        // outermost edge. Limiting the walk to the nearer half made an off-centre
        // page undetectable on the side it is furthest from.
        val limit = if (inward > 0) span - 1 else 0

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
    ): Result? {
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

        return Result(corners, sidesFound = fitted)
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
        val w = src.w
        val h = src.h
        val horizontal = FloatArray(src.px.size)
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
        val w = src.w
        val h = src.h
        val horizontal = FloatArray(src.px.size)
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
