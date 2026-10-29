package com.example.docscanner.util

import com.example.docscanner.model.QuadCorners

/**
 * The arithmetic behind the four corner handles in the border editor.
 *
 * It lives here, away from the canvas, for one reason: it is the part that was
 * wrong. The handles are dragged with **relative** movement now (the corner follows
 * the finger from wherever it was, instead of teleporting under it), every move is
 * clamped so two corners can never cross, and a value that lands within
 * [SNAP] of the frame edge is pulled onto it. All of that is plain float maths on a
 * [QuadCorners] — no Compose, no bitmaps — so it can be tested in
 * `testDebugUnitTest`, which the gesture itself cannot be.
 *
 * Coordinates are the stored ones: fractions of the *unrotated* capture, 0 at the
 * left/top edge and 1 at the right/bottom. The canvas converts screen touches into
 * this space (undoing pan, zoom and rotation) before calling [move], so nothing here
 * has to know about the view.
 */
object CropHandles {

    /** Corner indices, in the order they are drawn and stored. */
    const val TOP_LEFT = 0
    const val TOP_RIGHT = 1
    const val BOTTOM_RIGHT = 2
    const val BOTTOM_LEFT = 3

    /** Edge midpoint indices, matching original app's 8-handle CropView. */
    const val EDGE_TOP = 10
    const val EDGE_RIGHT = 11
    const val EDGE_BOTTOM = 12
    const val EDGE_LEFT = 13

    /**
     * Closest two corners of the same edge may get, as a fraction of the frame.
     *
     * Without it a corner dragged past its neighbour produces a self-intersecting
     * quad — and, before the bounds were ordered, a `coerceIn` crash. 2 % of a
     * 3200 px page is 64 px, which still looks like "the crop goes right to the
     * edge" while leaving the warp a real quadrilateral to work with.
     */
    const val MIN_GAP = 0.02f

    /**
     * A handle this close to a frame edge is pulled onto it.
     *
     * Landing exactly on the edge with a fingertip is a pixel-hunting exercise, and
     * "the crop stops four pixels short of the page" is the result. 1.2 % of the
     * frame is about 12 px on a preview — far enough to catch a deliberate drag to
     * the edge, close enough that it never fights a corner being placed just inside.
     */
    const val SNAP = 0.012f

    /**
     * A copy of [quad] with corner or edge [handle] moved by ([dx], [dy]).
     *
     * For corner handles, only that corner moves; the other three are returned untouched.
     * For edge handles, both adjacent corners translate together in parallel.
     * Every moved corner is kept inside the frame and at least [MIN_GAP] away from the
     * corners it opposes, so the quad cannot fold through itself.
     *
     * A zero delta returns an equal quad — the reason a tap is harmless.
     */
    fun move(quad: QuadCorners, handle: Int, dx: Float, dy: Float): QuadCorners =
        when (handle) {
            TOP_LEFT -> quad.copy(
                topLeftX = clamp(snap(quad.topLeftX + dx), 0f, quad.topRightX - MIN_GAP),
                topLeftY = clamp(snap(quad.topLeftY + dy), 0f, quad.bottomLeftY - MIN_GAP)
            )

            TOP_RIGHT -> quad.copy(
                topRightX = clamp(snap(quad.topRightX + dx), quad.topLeftX + MIN_GAP, 1f),
                topRightY = clamp(snap(quad.topRightY + dy), 0f, quad.bottomRightY - MIN_GAP)
            )

            BOTTOM_RIGHT -> quad.copy(
                bottomRightX = clamp(snap(quad.bottomRightX + dx), quad.bottomLeftX + MIN_GAP, 1f),
                bottomRightY = clamp(snap(quad.bottomRightY + dy), quad.topRightY + MIN_GAP, 1f)
            )

            BOTTOM_LEFT -> quad.copy(
                bottomLeftX = clamp(snap(quad.bottomLeftX + dx), 0f, quad.bottomRightX - MIN_GAP),
                bottomLeftY = clamp(snap(quad.bottomLeftY + dy), quad.topLeftY + MIN_GAP, 1f)
            )

            EDGE_TOP -> {
                if (dx == 0f && dy == 0f) return quad
                val maxDy = minOf(
                    quad.bottomLeftY - MIN_GAP - quad.topLeftY,
                    quad.bottomRightY - MIN_GAP - quad.topRightY
                )
                val minDy = -minOf(quad.topLeftY, quad.topRightY)
                val effectiveDy = clamp(dy, minDy, maxDy)

                val minDx = -minOf(quad.topLeftX, quad.topRightX)
                val maxDx = 1f - maxOf(quad.topLeftX, quad.topRightX)
                val effectiveDx = clamp(dx, minDx, maxDx)

                val newTLX = clamp(snap(quad.topLeftX + effectiveDx), 0f, quad.topRightX + effectiveDx - MIN_GAP)
                val newTLY = clamp(snap(quad.topLeftY + effectiveDy), 0f, quad.bottomLeftY - MIN_GAP)
                val newTRX = clamp(snap(quad.topRightX + effectiveDx), newTLX + MIN_GAP, 1f)
                val newTRY = clamp(snap(quad.topRightY + effectiveDy), 0f, quad.bottomRightY - MIN_GAP)

                quad.copy(
                    topLeftX = newTLX,
                    topLeftY = newTLY,
                    topRightX = newTRX,
                    topRightY = newTRY
                )
            }

            EDGE_BOTTOM -> {
                if (dx == 0f && dy == 0f) return quad
                val minDy = maxOf(
                    quad.topLeftY + MIN_GAP - quad.bottomLeftY,
                    quad.topRightY + MIN_GAP - quad.bottomRightY
                )
                val maxDy = 1f - maxOf(quad.bottomLeftY, quad.bottomRightY)
                val effectiveDy = clamp(dy, minDy, maxDy)

                val minDx = -minOf(quad.bottomLeftX, quad.bottomRightX)
                val maxDx = 1f - maxOf(quad.bottomLeftX, quad.bottomRightX)
                val effectiveDx = clamp(dx, minDx, maxDx)

                val newBLX = clamp(snap(quad.bottomLeftX + effectiveDx), 0f, quad.bottomRightX + effectiveDx - MIN_GAP)
                val newBLY = clamp(snap(quad.bottomLeftY + effectiveDy), quad.topLeftY + MIN_GAP, 1f)
                val newBRX = clamp(snap(quad.bottomRightX + effectiveDx), newBLX + MIN_GAP, 1f)
                val newBRY = clamp(snap(quad.bottomRightY + effectiveDy), quad.topRightY + MIN_GAP, 1f)

                quad.copy(
                    bottomLeftX = newBLX,
                    bottomLeftY = newBLY,
                    bottomRightX = newBRX,
                    bottomRightY = newBRY
                )
            }

            EDGE_LEFT -> {
                if (dx == 0f && dy == 0f) return quad
                val maxDx = minOf(
                    quad.topRightX - MIN_GAP - quad.topLeftX,
                    quad.bottomRightX - MIN_GAP - quad.bottomLeftX
                )
                val minDx = -minOf(quad.topLeftX, quad.bottomLeftX)
                val effectiveDx = clamp(dx, minDx, maxDx)

                val minDy = -minOf(quad.topLeftY, quad.bottomLeftY)
                val maxDy = 1f - maxOf(quad.topLeftY, quad.bottomLeftY)
                val effectiveDy = clamp(dy, minDy, maxDy)

                val newTLX = clamp(snap(quad.topLeftX + effectiveDx), 0f, quad.topRightX - MIN_GAP)
                val newTLY = clamp(snap(quad.topLeftY + effectiveDy), 0f, quad.bottomLeftY + effectiveDy - MIN_GAP)
                val newBLX = clamp(snap(quad.bottomLeftX + effectiveDx), 0f, quad.bottomRightX - MIN_GAP)
                val newBLY = clamp(snap(quad.bottomLeftY + effectiveDy), newTLY + MIN_GAP, 1f)

                quad.copy(
                    topLeftX = newTLX,
                    topLeftY = newTLY,
                    bottomLeftX = newBLX,
                    bottomLeftY = newBLY
                )
            }

            EDGE_RIGHT -> {
                if (dx == 0f && dy == 0f) return quad
                val minDx = maxOf(
                    quad.topLeftX + MIN_GAP - quad.topRightX,
                    quad.bottomLeftX + MIN_GAP - quad.bottomRightX
                )
                val maxDx = 1f - maxOf(quad.topRightX, quad.bottomRightX)
                val effectiveDx = clamp(dx, minDx, maxDx)

                val minDy = -minOf(quad.topRightY, quad.bottomRightY)
                val maxDy = 1f - maxOf(quad.topRightY, quad.bottomRightY)
                val effectiveDy = clamp(dy, minDy, maxDy)

                val newTRX = clamp(snap(quad.topRightX + effectiveDx), quad.topLeftX + MIN_GAP, 1f)
                val newTRY = clamp(snap(quad.topRightY + effectiveDy), 0f, quad.bottomRightY + effectiveDy - MIN_GAP)
                val newBRX = clamp(snap(quad.bottomRightX + effectiveDx), quad.bottomLeftX + MIN_GAP, 1f)
                val newBRY = clamp(snap(quad.bottomRightY + effectiveDy), newTRY + MIN_GAP, 1f)

                quad.copy(
                    topRightX = newTRX,
                    topRightY = newTRY,
                    bottomRightX = newBRX,
                    bottomRightY = newBRY
                )
            }

            else -> quad
        }

    /**
     * True when two corners of the same edge have swapped or collided — the
     * condition [move] exists to prevent. Cheap enough to assert in a test.
     */
    fun isSound(quad: QuadCorners): Boolean =
        quad.topRightX - quad.topLeftX >= MIN_GAP - 1e-4f &&
            quad.bottomRightX - quad.bottomLeftX >= MIN_GAP - 1e-4f &&
            quad.bottomLeftY - quad.topLeftY >= MIN_GAP - 1e-4f &&
            quad.bottomRightY - quad.topRightY >= MIN_GAP - 1e-4f

    /** Pulls a value onto the near edge when it is within [SNAP] of it. */
    private fun snap(value: Float): Float = when {
        value <= SNAP -> 0f
        value >= 1f - SNAP -> 1f
        else -> value
    }

    /**
     * `coerceIn` throws when `low > high`; order the bounds first.
     *
     * Every clamp inside [move] goes through here, including the four edge drags.
     * Those used to call `coerceIn` directly on a pair of bounds derived from the
     * quad — and a quad that is **unsound** (loaded from a library written by an
     * older revision, or detected on a page so skewed that two corners ended up
     * within [MIN_GAP] of each other) makes `maxDy` smaller than `minDy`, which is an
     * `IllegalArgumentException` thrown *inside a drag gesture*: the app dies while
     * the finger is on the crop handle. A degenerate quad should make the drag do
     * nothing, not crash.
     */
    private fun clamp(value: Float, first: Float, second: Float): Float {
        val low = minOf(first, second)
        val high = maxOf(first, second)
        return value.coerceIn(low, high)
    }
}
