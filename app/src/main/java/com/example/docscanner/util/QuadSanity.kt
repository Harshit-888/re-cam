package com.example.docscanner.util

import com.example.docscanner.model.QuadCorners
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max

/**
 * Is this quad a plausible page?
 *
 * The Kotlin geometry detector has its own validation (`DocumentQuad.validate`) built
 * from the lines it fitted. A model does not: it emits four points and a confidence
 * per point, and a model that is confidently wrong produces a confidently wrong crop —
 * a page warped from a quad that crosses itself, or a quad whose "page" is 3 % of the
 * frame. The warp will happily do either, and the user sees a blank or a sliver.
 *
 * So the ML result is checked here before anything trusts it. The rules are the same
 * ones the geometric path already applies, deliberately: the two paths must agree on
 * what a page is, or the same photograph would be cropped differently depending on
 * which detector answered.
 *
 * Pure float maths on [QuadCorners] — no Android, no bitmaps — so it is unit-testable,
 * which is the only way this can be verified in a sandbox without a device.
 */
object QuadSanity {

    /** Smallest believable page area, as a fraction of the frame. */
    const val MIN_AREA = 0.06f

    /** Largest believable page area; slightly over 1 for rounding on the edges. */
    const val MAX_AREA = 1.05f

    /** Shortest side ÷ longest side. A sliver is not a page. */
    const val MIN_SIDE_RATIO = 0.18f

    /** How far a corner may sit outside the frame before the quad is rejected. */
    const val EDGE_TOLERANCE = 0.02f

    /** How far a corner may be from "above/below" its neighbour before rejecting. */
    private const val ORDER_TOLERANCE = 0.01f

    /**
     * The first rule [quad] breaks, or `null` when it is plausible.
     *
     * Returning the *reason* rather than a boolean is what makes a bad detection
     * debuggable from a log line instead of guesswork.
     */
    fun rejection(quad: QuadCorners): String? {
        val xs = floatArrayOf(
            quad.topLeftX, quad.topRightX, quad.bottomRightX, quad.bottomLeftX
        )
        val ys = floatArrayOf(
            quad.topLeftY, quad.topRightY, quad.bottomRightY, quad.bottomLeftY
        )

        for (index in 0..3) {
            if (xs[index] < -EDGE_TOLERANCE || xs[index] > 1f + EDGE_TOLERANCE ||
                ys[index] < -EDGE_TOLERANCE || ys[index] > 1f + EDGE_TOLERANCE
            ) {
                return "corner $index outside the frame"
            }
            if (!xs[index].isFinite() || !ys[index].isFinite()) {
                return "corner $index is not a number"
            }
        }

        // The corners have to read as top-left, top-right, bottom-right, bottom-left.
        // A crossed quad warps into a self-overlapping image, which looks like a bug
        // in the export rather than in the detection.
        if (quad.topLeftX > quad.topRightX + ORDER_TOLERANCE) return "top edge runs backwards"
        if (quad.bottomLeftX > quad.bottomRightX + ORDER_TOLERANCE) {
            return "bottom edge runs backwards"
        }
        if (quad.topLeftY > quad.bottomLeftY + ORDER_TOLERANCE) return "left edge runs backwards"
        if (quad.topRightY > quad.bottomRightY + ORDER_TOLERANCE) return "right edge runs backwards"

        val area = areaRatio(quad)
        if (area < MIN_AREA) return "page area is ${percent(area)} of the frame"
        if (area > MAX_AREA) return "page area is ${percent(area)} of the frame"

        val sides = listOf(
            hypot(quad.topRightX - quad.topLeftX, quad.topRightY - quad.topLeftY),
            hypot(quad.bottomRightX - quad.bottomLeftX, quad.bottomRightY - quad.bottomLeftY),
            hypot(quad.bottomLeftX - quad.topLeftX, quad.bottomLeftY - quad.topLeftY),
            hypot(quad.bottomRightX - quad.topRightX, quad.bottomRightY - quad.topRightY)
        )
        val longest = sides.max()
        val shortest = sides.min()
        if (longest <= 0f) return "page has no size"
        val ratio = shortest / longest
        if (ratio < MIN_SIDE_RATIO) return "page is a sliver (short ÷ long = ${percent(ratio)})"

        return null
    }

    /** `true` when [quad] passes every rule above. */
    fun isPlausible(quad: QuadCorners): Boolean = rejection(quad) == null

    /**
     * Quad area as a fraction of the frame, by the shoelace formula.
     *
     * Absolute value because the corner order is what the caller is being checked
     * for; a negative here means the winding is reversed, not that the area is
     * negative.
     */
    fun areaRatio(quad: QuadCorners): Float {
        val xs = floatArrayOf(
            quad.topLeftX, quad.topRightX, quad.bottomRightX, quad.bottomLeftX
        )
        val ys = floatArrayOf(
            quad.topLeftY, quad.topRightY, quad.bottomRightY, quad.bottomLeftY
        )
        var sum = 0f
        for (index in 0..3) {
            val next = (index + 1) % 4
            sum += xs[index] * ys[next] - xs[next] * ys[index]
        }
        return abs(sum) / 2f
    }

    private fun percent(fraction: Float): String =
        String.format(java.util.Locale.US, "%.0f%%", max(0f, fraction) * 100f)
}
