package com.example.docscanner.util

import com.example.docscanner.model.QuadCorners
import kotlin.math.abs
import kotlin.math.max

/**
 * Everything the live viewfinder border needs that is *not* Android: how a stream
 * of per-frame detections becomes one steady outline, and how that outline is
 * mapped onto the preview view.
 *
 * No `Bitmap`, no `ImageProxy`, no camera — so `LiveBorderTrackerTest` can drive it
 * with synthetic frames. That matters more here than in the editor: this code runs
 * six times a second, on a device held in a hand that is never quite still, and a
 * detector that is right 90 % of the time looks *broken* at 6 Hz. The failure mode
 * users report ("it flickers, so I stopped trusting it") is decided by the rules in
 * this file, not by the detector.
 *
 * ## Why a tracker instead of drawing each frame
 *
 * Three separate problems, and each one has its own answer:
 *
 * 1. **A single bad frame must not move the outline.** Confidence is a counter:
 *    +1 for a frame that found a page, −2 for one that did not, 0–[CONFIDENCE_MAX].
 *    The outline appears at [SHOW_AT] and disappears below [HIDE_BELOW], so it takes
 *    three good frames to appear and two bad ones to go. One missed frame — the
 *    exact thing that happens when a hand crosses the page or the torch changes the
 *    exposure — is invisible.
 * 2. **A good frame must not make the outline twitch.** Detection is deterministic
 *    but the *input* is not: the page moves a pixel, the exposure settles, and a
 *    corner that was found at 0.34 is now found at 0.345. When every corner of the
 *    new quad is within [SNAP] of the drawn one, the drawn one is kept. Real
 *    movement is still followed, because a corner that moves further than [SNAP]
 *    falls through to the glide.
 * 3. **Real movement must not teleport.** The remaining error is interpolated
 *    ([GLIDE] per frame), so the outline slides to the new place over two or three
 *    frames instead of jumping. Between them: no jitter, no lag worth noticing.
 *
 * This mirrors what Adobe's live boundary shim does with
 * `AreCurrentCornersWithinTolerance` and its accumulating display-confidence
 * counter (see `adobe-scan/decompiled_src/`, read-only) — the thresholds here were
 * chosen for a 150 ms cadence rather than copied, because the shim runs at its own
 * timer's rate.
 *
 * ## What it deliberately does *not* do
 *
 * Adobe's shim also decides when the torch should come on (`LiveBoundaryTorchHint`,
 * `imageHasDarkShadow`, `imageHasGlareAfterTorch`) and when the shutter may be
 * pressed by itself. Neither belongs in this app: flash is an explicit control the
 * user can see the state of, and the shutter is the user's finger.
 */
class LiveBorderTracker(
    private val showAt: Int = SHOW_AT,
    private val hideBelow: Int = HIDE_BELOW
) {

    /**
     * The outline to draw.
     *
     * [corners] is always the *held* quad — the thing last worth showing — even on
     * frames where the detector found nothing. [visible] is what decides whether it
     * is drawn at all, and it is the only field the animation reads.
     */
    data class State(
        val corners: QuadCorners,
        val sidesFound: Int,
        val visible: Boolean
    )

    private var confidence = 0
    private var visible = false
    private var held: QuadCorners? = null
    private var heldSides = 0

    /** True while an outline is on screen. */
    val showing: Boolean get() = visible

    fun reset() {
        confidence = 0
        visible = false
        held = null
        heldSides = 0
    }

    /**
     * Feeds one detection result and returns what the overlay should draw.
     *
     * @param corners the detected quad, or `null` when this frame found nothing.
     * @param sidesFound how many of its four sides came from real edges (0–4).
     */
    fun onFrame(corners: QuadCorners?, sidesFound: Int): State {
        val candidate = corners
            ?.takeIf { sidesFound >= ACCEPT_SIDES }
            ?.takeIf { it.isShootablePage() }

        if (candidate == null) {
            confidence = max(0, confidence - PENALTY)
        } else {
            confidence = (confidence + 1).coerceAtMost(CONFIDENCE_MAX)
            val previous = held
            val next = when {
                // First sighting, or the page genuinely moved: nothing to hold on to.
                previous == null -> candidate
                previous.isWithinTolerance(candidate) -> previous
                else -> previous.glideTo(candidate)
            }
            held = next
            heldSides = sidesFound
        }

        if (visible) {
            if (confidence < hideBelow) visible = false
        } else if (confidence >= showAt) {
            visible = true
        }

        return State(
            corners = held ?: QuadCorners.FULL_FRAME,
            sidesFound = if (visible) heldSides else 0,
            visible = visible && held != null
        )
    }

    /**
     * A page worth outlining: a real area, and not degenerate.
     *
     * `DocumentQuad` already rejects crossed and sliver quads, so this is a second,
     * cheaper net for the *viewfinder* — where the cost of being wrong is a line
     * drawn across the user's frame rather than a bad crop. A quad covering a
     * tenth of the frame is not something anyone is about to photograph.
     */
    private fun QuadCorners.isShootablePage(): Boolean {
        val area = abs(shoelace()) / 2f
        if (area < MIN_AREA || area > MAX_AREA) return false
        // Longest side against shortest: a quad folded onto a line has ~0 short side.
        val sides = floatArrayOf(
            hypot(topRightX - topLeftX, topRightY - topLeftY),
            hypot(bottomRightX - topRightX, bottomRightY - topRightY),
            hypot(bottomLeftX - bottomRightX, bottomLeftY - bottomRightY),
            hypot(topLeftX - bottomLeftX, topLeftY - bottomLeftY)
        )
        val longest = sides.max()
        val shortest = sides.min()
        return shortest > 0f && longest / shortest <= MAX_SIDE_RATIO
    }

    private fun QuadCorners.shoelace(): Float {
        // Signed area ×2 in normalised space; the sign only says which way the
        // corners are wound, which is the detector's business, not ours.
        val x = floatArrayOf(topLeftX, topRightX, bottomRightX, bottomLeftX)
        val y = floatArrayOf(topLeftY, topRightY, bottomRightY, bottomLeftY)
        var sum = 0f
        for (i in 0 until 4) {
            val j = (i + 1) % 4
            sum += x[i] * y[j] - x[j] * y[i]
        }
        return sum
    }

    private fun QuadCorners.isWithinTolerance(other: QuadCorners): Boolean =
        abs(topLeftX - other.topLeftX) <= SNAP && abs(topLeftY - other.topLeftY) <= SNAP &&
            abs(topRightX - other.topRightX) <= SNAP && abs(topRightY - other.topRightY) <= SNAP &&
            abs(bottomRightX - other.bottomRightX) <= SNAP &&
            abs(bottomRightY - other.bottomRightY) <= SNAP &&
            abs(bottomLeftX - other.bottomLeftX) <= SNAP &&
            abs(bottomLeftY - other.bottomLeftY) <= SNAP

    private fun QuadCorners.glideTo(target: QuadCorners): QuadCorners {
        fun mix(from: Float, to: Float) = from + (to - from) * GLIDE
        return QuadCorners(
            topLeftX = mix(topLeftX, target.topLeftX),
            topLeftY = mix(topLeftY, target.topLeftY),
            topRightX = mix(topRightX, target.topRightX),
            topRightY = mix(topRightY, target.topRightY),
            bottomRightX = mix(bottomRightX, target.bottomRightX),
            bottomRightY = mix(bottomRightY, target.bottomRightY),
            bottomLeftX = mix(bottomLeftX, target.bottomLeftX),
            bottomLeftY = mix(bottomLeftY, target.bottomLeftY)
        )
    }

    private fun hypot(dx: Float, dy: Float): Float =
        kotlin.math.sqrt(dx * dx + dy * dy)

    companion object {
        /** A frame counts as "found a page" at this many real sides. */
        const val ACCEPT_SIDES = 3

        /** Confidence ceiling. Six good frames is a firmly locked outline. */
        const val CONFIDENCE_MAX = 6

        /** Confidence lost per frame that did not find a page. */
        const val PENALTY = 2

        /** Confidence needed before the outline is drawn (three good frames). */
        const val SHOW_AT = 3

        /** Confidence below which a drawn outline is removed (two bad frames). */
        const val HIDE_BELOW = 2

        /** Normalised distance under which a corner is treated as "not moving". */
        const val SNAP = 0.02f

        /** Fraction of the remaining distance covered per frame when it is moving. */
        const val GLIDE = 0.5f

        /** Smallest page area, as a fraction of the frame, worth outlining. */
        const val MIN_AREA = 0.12f

        /** Area ceiling — a "quad" larger than the frame is a detection artefact. */
        const val MAX_AREA = 1.02f

        /** Longest side ÷ shortest side. 8 is a page seen at an extreme angle. */
        const val MAX_SIDE_RATIO = 8f
    }
}

/**
 * The rectangle a camera stream actually occupies inside a view, for
 * `PreviewView.ScaleType.FIT_CENTER`.
 *
 * The viewfinder is a 3:4 box and the stream is asked for 4:3, which is the same
 * shape rotated — but "asked for" is not "given". A device that hands back 16:9
 * letterboxes the stream inside the box, and an outline mapped as if it filled the
 * box would sit off the page. This is the same arithmetic `FIT_CENTER` uses, in one
 * place, with a test.
 */
data class SceneRect(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
) {
    fun x(fraction: Float): Float = left + fraction * width
    fun y(fraction: Float): Float = top + fraction * height

    companion object {
        /**
         * @param containerW width of the view, in px
         * @param containerH height of the view, in px
         * @param contentAspect width ÷ height of the stream
         */
        fun fitCenter(containerW: Float, containerH: Float, contentAspect: Float): SceneRect {
            if (containerW <= 0f || containerH <= 0f || contentAspect <= 0f) {
                return SceneRect(0f, 0f, max(0f, containerW), max(0f, containerH))
            }
            val containerAspect = containerW / containerH
            val width: Float
            val height: Float
            if (contentAspect >= containerAspect) {
                // Wider than the box: full width, letterboxed top and bottom.
                width = containerW
                height = containerW / contentAspect
            } else {
                width = containerH * contentAspect
                height = containerH
            }
            return SceneRect((containerW - width) / 2f, (containerH - height) / 2f, width, height)
        }
    }
}

/** A greyscale frame: brightness 0–255, row-major, no Android type in it. */
class GreyFrame(val width: Int, val height: Int, val pixels: FloatArray)

/**
 * Rotates a greyscale frame so it is upright, the way the camera sensor's
 * `rotationDegrees` says it should be.
 *
 * Detection has to run on the *upright* frame, not on the buffer: `DocumentQuad`
 * scans columns for the left and right sides and rows for the top and bottom, and
 * a page lying across a landscape buffer would have its sides found along the
 * wrong axis. Rotating the luma here — once, at 360 px wide — costs a fraction of
 * what rotating the detection would.
 */
object GreyFrames {

    /** @param degrees one of 0, 90, 180, 270 (clockwise). */
    fun rotate(src: GreyFrame, degrees: Int): GreyFrame {
        val normalised = ((degrees % 360) + 360) % 360
        if (normalised == 0) return src
        val w = src.width
        val h = src.height
        val px = src.pixels
        return when (normalised) {
            90 -> {
                // Buffer (x, y) → upright (h - 1 - y, x).
                val out = FloatArray(w * h)
                val outW = h
                for (y in 0 until h) {
                    for (x in 0 until w) {
                        out[x * outW + (h - 1 - y)] = px[y * w + x]
                    }
                }
                GreyFrame(outW, w, out)
            }
            180 -> {
                val out = FloatArray(w * h)
                val last = w * h - 1
                for (i in px.indices) out[last - i] = px[i]
                GreyFrame(w, h, out)
            }
            270 -> {
                // Buffer (x, y) → upright (y, w - 1 - x).
                val out = FloatArray(w * h)
                val outW = h
                for (y in 0 until h) {
                    for (x in 0 until w) {
                        out[(w - 1 - x) * outW + y] = px[y * w + x]
                    }
                }
                GreyFrame(outW, w, out)
            }
            else -> src
        }
    }
}
