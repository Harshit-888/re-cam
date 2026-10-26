package com.example.docscanner.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot
import kotlin.math.max

/** The working resolution the detector runs at, and the size of every scene here. */
private const val FRAME_WIDTH = 360
private const val FRAME_HEIGHT = 480

/**
 * Border detection, checked against synthetic photographs.
 *
 * Detection is the thing a document scanner is judged by, and until now the only
 * way to test it was to photograph a piece of paper with a real phone. These tests
 * build a frame instead: a desk with a gradient, a page with perspective, ink
 * lines on it, sensor noise — all deterministic — and assert on the corners the
 * detector returns.
 *
 * The two regressions pinned here were found by re-implementing the *previous*
 * algorithm and running it over the same scenes:
 *
 * * an off-centre page used to come back as a quad snapped to the left frame edge,
 *   nearly half the frame away from the truth — a confidently wrong crop;
 * * a dark page on a light surface used to be missed entirely, because only the
 *   "brighter than its surroundings" hypothesis was ever tried.
 *
 * Tolerances are deliberately loose (2.5 % of the long side, about 9 px on a
 * 360 px frame). The measured error on these scenes is ~0.5 %, so the tests pass
 * with a wide margin and fail when something is genuinely wrong rather than
 * drifting out of a sub-pixel window.
 */
class DocumentQuadTest {

    /** A page with real perspective: no two edges parallel. */
    private val page = arrayOf(
        55f to 75f,
        305f to 95f,
        320f to 415f,
        42f to 400f
    )

    // ------------------------------------------------------------------ tests

    @Test
    fun `finds a page on a darker desk`() {
        val result = detectAt(page)

        assertNotNull("a plain page on a plain desk must be found", result)
        result!!
        assertEquals("all four sides should be real edges", 4, result.sidesFound)
        assertCornersNear(page, result.corners, tolerance = 0.025f)
    }

    @Test
    fun `finds a page that sits off centre`() {
        // The page's left edge is right of the middle of the frame. The previous
        // revision only scanned the half of the frame nearest each border, so it
        // could not see this edge at all and returned a quad on the frame edge.
        val offCentre = arrayOf(
            190f to 60f,
            350f to 70f,
            350f to 430f,
            180f to 420f
        )
        val result = detectAt(offCentre)

        assertNotNull(result)
        result!!
        assertCornersNear(offCentre, result.corners, tolerance = 0.025f)
        assertTrue(
            "the left edge is at 49 % of the frame; a detection at the frame edge is the bug",
            result.corners.topLeftX > 0.45f
        )
    }

    @Test
    fun `finds a darker page on a light surface`() {
        // A menu board, a phone screen, a laminated form. The previous revision
        // only tried the inverted polarity when the whole frame was dark, so this
        // never detected anything.
        val result = detectAt(page, desk = 235f, paper = 70f, deskGradient = -15f)

        assertNotNull("a dark page on a light desk must be found", result)
        result!!
        assertCornersNear(page, result.corners, tolerance = 0.025f)
    }

    @Test
    fun `finds a page on a desk that is nearly as bright`() {
        // White page, light wooden desk: the step is real but small in absolute
        // terms, which is what the secondary thresholds exist for.
        val result = detectAt(page, desk = 200f, paper = 228f, deskGradient = 10f)

        assertNotNull(result)
        result!!
        assertCornersNear(page, result.corners, tolerance = 0.03f)
    }

    @Test
    fun `finds a rotated page`() {
        // A diamond: every side is strongly sloped, which is what MAX_SLOPE and the
        // line intersection have to cope with.
        val rotated = arrayOf(
            135f to 40f,
            325f to 210f,
            180f to 440f,
            20f to 260f
        )
        val result = detectAt(rotated)

        assertNotNull(result)
        result!!
        assertCornersNear(rotated, result.corners, tolerance = 0.025f)
    }

    @Test
    fun `copes with sensor noise`() {
        val result = detectAt(page, noise = 8f)

        assertNotNull(result)
        result!!
        assertCornersNear(page, result.corners, tolerance = 0.03f)
    }

    @Test
    fun `reports a featureless frame as the whole frame, not as a failure`() {
        // A page that fills the frame, or a photo of a blank wall. There is nothing
        // to crop, and `sidesFound = 0` says exactly that. Reporting it as "failed"
        // would put a "drag the corners" hint over a page that needs no dragging.
        val flat = FloatArray(FRAME_WIDTH * FRAME_HEIGHT) { 190f }
        val result = DocumentQuad.detect(flat, FRAME_WIDTH, FRAME_HEIGHT)

        assertNotNull(result)
        result!!
        assertEquals(0, result.sidesFound)
        assertTrue(result.corners.isFullFrame)
    }

    @Test
    fun `a page that fills the frame is not cropped`() {
        val nearlyFullFrame = arrayOf(
            2f to 2f,
            358f to 2f,
            358f to 478f,
            2f to 478f
        )
        val result = detectAt(nearlyFullFrame)

        assertNotNull(result)
        result!!
        assertTrue("the quad covers the frame, so there is nothing to correct", result.corners.isFullFrame)
    }

    @Test
    fun `refuses to guess on an image too small to judge`() {
        assertNull(DocumentQuad.detect(FloatArray(20 * 20) { 128f }, 20, 20))
        assertNull(DocumentQuad.detect(FloatArray(4) { 128f }, 2, 2))
        // A buffer shorter than the declared size is a caller bug, not a crash.
        assertNull(DocumentQuad.detect(FloatArray(100) { 128f }, 64, 64))
    }

    @Test
    fun `is deterministic`() {
        // "Detect borders" can be pressed twice; the handles must not jump.
        val luma = scene(page)
        val first = DocumentQuad.detect(luma, FRAME_WIDTH, FRAME_HEIGHT)
        val second = DocumentQuad.detect(luma, FRAME_WIDTH, FRAME_HEIGHT)

        assertEquals(first?.corners, second?.corners)
        assertEquals(first?.sidesFound, second?.sidesFound)
    }

    @Test
    fun `handles a landscape frame`() {
        val width = 480
        val height = 360
        val quad = arrayOf(
            90f to 40f,
            400f to 55f,
            410f to 325f,
            75f to 310f
        )
        val luma = scene(quad, width = width, height = height)
        val result = DocumentQuad.detect(luma, width, height)

        assertNotNull(result)
        result!!
        assertCornersNear(quad, result.corners, width = width, height = height, tolerance = 0.025f)
    }

    // ---------------------------------------------------------------- helpers

    private fun detectAt(
        quad: Array<Pair<Float, Float>>,
        desk: Float = 55f,
        paper: Float = 225f,
        deskGradient: Float = 25f,
        text: Boolean = true,
        noise: Float = 3f
    ): DocumentQuad.Result? = DocumentQuad.detect(
        scene(quad, desk = desk, paper = paper, deskGradient = deskGradient, text = text, noise = noise),
        FRAME_WIDTH,
        FRAME_HEIGHT
    )

    /**
     * A synthetic photograph: a desk with a brightness gradient, an opaque page
     * with real ink lines on it, and deterministic sensor noise.
     *
     * Ink matters: it is the thing the morphological closing has to erase before
     * the edge can be found, and a blank page would let a broken closing pass.
     */
    private fun scene(
        quad: Array<Pair<Float, Float>>,
        width: Int = FRAME_WIDTH,
        height: Int = FRAME_HEIGHT,
        desk: Float = 55f,
        paper: Float = 225f,
        deskGradient: Float = 25f,
        text: Boolean = true,
        noise: Float = 3f
    ): FloatArray {
        val luma = FloatArray(width * height)
        var seed = 0x5DEECE66DL
        for (y in 0 until height) {
            for (x in 0 until width) {
                var value = desk + deskGradient * (x.toFloat() / width)
                if (inside(quad, x + 0.5f, y + 0.5f)) {
                    value = paper
                    val lineIndex = y - quad[0].second
                    val inTextBlock = lineIndex > 0.08f * height && lineIndex < 0.85f * height
                    if (text && inTextBlock && y % 6 < 2 && x % 5 != 0) {
                        value = paper - 150f
                    }
                }
                if (noise > 0f) {
                    seed = (seed * 1103515245L + 12345L) and 0x7FFFFFFFL
                    value += ((seed % 20_000L) / 20_000f - 0.5f) * 2f * noise
                }
                luma[y * width + x] = value.coerceIn(0f, 255f)
            }
        }
        return luma
    }

    /** Standard point-in-polygon: same side of every edge. */
    private fun inside(quad: Array<Pair<Float, Float>>, x: Float, y: Float): Boolean {
        var sign = 0
        for (i in 0..3) {
            val ax = quad[i].first
            val ay = quad[i].second
            val bx = quad[(i + 1) % 4].first
            val by = quad[(i + 1) % 4].second
            val cross = (bx - ax) * (y - ay) - (by - ay) * (x - ax)
            val current = if (cross >= 0f) 1 else -1
            if (sign == 0) sign = current
            if (current != sign) return false
        }
        return true
    }

    /**
     * Each true corner must have a detected corner within [tolerance] of the long
     * side. Matching is by nearest rather than by index so that a test fails on
     * geometry, not on corner ordering.
     */
    private fun assertCornersNear(
        truth: Array<Pair<Float, Float>>,
        corners: com.example.docscanner.model.QuadCorners,
        width: Int = FRAME_WIDTH,
        height: Int = FRAME_HEIGHT,
        tolerance: Float = 0.025f
    ) {
        val detected = listOf(
            corners.topLeftX to corners.topLeftY,
            corners.topRightX to corners.topRightY,
            corners.bottomRightX to corners.bottomRightY,
            corners.bottomLeftX to corners.bottomLeftY
        ).map { (nx, ny) -> nx * width to ny * height }
        val allowed = tolerance * max(width, height)
        for ((tx, ty) in truth) {
            val nearest = detected.minOf { (dx, dy) -> hypot(dx - tx, dy - ty) }
            assertTrue(
                "no detected corner within ${allowed}px of ($tx, $ty); nearest is ${nearest}px",
                nearest <= allowed
            )
        }
    }
}
