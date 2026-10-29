package com.example.docscanner.util

import com.example.docscanner.model.QuadCorners
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the corner-handle arithmetic that the border canvas runs on every drag frame.
 *
 * The reports were "the dots misbehave when dragged", and the causes were all here or
 * next to here: a tap teleported a corner, the grab region was a fifth of the page,
 * and a corner could be dragged through its neighbour. None of that is visible to a
 * unit test of the *canvas*, but all of it is visible to a unit test of the maths —
 * which is why the maths is a plain object in `util`.
 */
class CropHandlesTest {

    private val inset = QuadCorners.inset(0.1f)

    @Test
    fun `a zero delta moves nothing`() {
        // The tap case. This is what "the dots jump when I touch them" was: the old
        // code wrote the finger's position into the nearest corner on pointer-down.
        for (corner in 0..3) {
            assertEquals(inset, CropHandles.move(inset, corner, 0f, 0f))
        }
    }

    @Test
    fun `only the grabbed corner moves`() {
        val moved = CropHandles.move(inset, CropHandles.TOP_LEFT, 0.05f, 0.05f)
        assertEquals(inset.topRightX, moved.topRightX, 0f)
        assertEquals(inset.topRightY, moved.topRightY, 0f)
        assertEquals(inset.bottomRightX, moved.bottomRightX, 0f)
        assertEquals(inset.bottomRightY, moved.bottomRightY, 0f)
        assertEquals(inset.bottomLeftX, moved.bottomLeftX, 0f)
        assertEquals(inset.bottomLeftY, moved.bottomLeftY, 0f)
        assertEquals(inset.topLeftX + 0.05f, moved.topLeftX, 1e-5f)
        assertEquals(inset.topLeftY + 0.05f, moved.topLeftY, 1e-5f)
    }

    @Test
    fun `horizontal and vertical drags are independent`() {
        val horizontal = CropHandles.move(inset, CropHandles.TOP_RIGHT, 0.05f, 0f)
        val vertical = CropHandles.move(inset, CropHandles.TOP_RIGHT, 0f, 0.05f)
        assertEquals(inset.topRightY, horizontal.topRightY, 0f)
        assertEquals(inset.topRightX, vertical.topRightX, 0f)
    }

    @Test
    fun `a handle dragged to the edge lands on it`() {
        // Within SNAP of the frame edge, the corner snaps onto it: a crop that should
        // reach the edge does, instead of stopping 3 px short.
        val nearLeft = QuadCorners.inset(0.02f)
        val dragged = CropHandles.move(nearLeft, CropHandles.TOP_LEFT, -0.015f, 0f)
        assertEquals(0f, dragged.topLeftX, 0f)
    }

    @Test
    fun `a handle cannot cross its neighbour`() {
        // Dragging the top-left corner right across the page used to fold the quad
        // through itself — and, before the bounds were ordered, threw from
        // `coerceIn`. It must stop one gap before the corner opposite it.
        val dragged = CropHandles.move(inset, CropHandles.TOP_LEFT, 5f, 5f)
        assertEquals(inset.topRightX - CropHandles.MIN_GAP, dragged.topLeftX, 1e-5f)
        assertEquals(inset.bottomLeftY - CropHandles.MIN_GAP, dragged.topLeftY, 1e-5f)
        assertTrue(CropHandles.isSound(dragged))
    }

    @Test
    fun `a handle stays inside the frame`() {
        val dragged = CropHandles.move(inset, CropHandles.BOTTOM_RIGHT, 5f, 5f)
        assertEquals(1f, dragged.bottomRightX, 0f)
        assertEquals(1f, dragged.bottomRightY, 0f)
        val other = CropHandles.move(inset, CropHandles.TOP_LEFT, -5f, -5f)
        assertEquals(0f, other.topLeftX, 0f)
        assertEquals(0f, other.topLeftY, 0f)
    }

    @Test
    fun `no sequence of drags can fold the quad`() {
        // 2 000 random moves in a row, the way a real finger wanders. After every one
        // the quad has to stay ordered and inside the frame; that is the property the
        // warp and the PDF geometry rely on.
        val random = Random(20261027)
        var quad = QuadCorners.inset(0.08f)
        repeat(2_000) {
            val corner = random.nextInt(4)
            val dx = (random.nextFloat() - 0.5f) * 0.8f
            val dy = (random.nextFloat() - 0.5f) * 0.8f
            quad = CropHandles.move(quad, corner, dx, dy)
            assertTrue("folded at step: $quad", CropHandles.isSound(quad))
            for (value in quad.asList()) {
                assertTrue("out of frame: $quad", value in 0f..1f)
            }
        }
        // It really did move: the fuzz above is not silently testing a no-op.
        assertNotEquals(QuadCorners.inset(0.08f), quad)
    }

    @Test
    fun `an unknown corner index is ignored`() {
        assertEquals(inset, CropHandles.move(inset, 7, 0.3f, 0.3f))
    }

    @Test
    fun `dragging edge moves both adjacent corners`() {
        val movedTop = CropHandles.move(inset, CropHandles.EDGE_TOP, 0f, 0.05f)
        assertEquals(inset.topLeftY + 0.05f, movedTop.topLeftY, 1e-5f)
        assertEquals(inset.topRightY + 0.05f, movedTop.topRightY, 1e-5f)
        assertEquals(inset.bottomLeftY, movedTop.bottomLeftY, 0f)
        assertEquals(inset.bottomRightY, movedTop.bottomRightY, 0f)

        val movedLeft = CropHandles.move(inset, CropHandles.EDGE_LEFT, 0.04f, 0f)
        assertEquals(inset.topLeftX + 0.04f, movedLeft.topLeftX, 1e-5f)
        assertEquals(inset.bottomLeftX + 0.04f, movedLeft.bottomLeftX, 1e-5f)
        assertEquals(inset.topRightX, movedLeft.topRightX, 0f)
        assertEquals(inset.bottomRightX, movedLeft.bottomRightX, 0f)
    }

    @Test
    fun `edge dragging respects min gap and frame bounds`() {
        val extremeTop = CropHandles.move(inset, CropHandles.EDGE_TOP, 0f, 5f)
        assertTrue(CropHandles.isSound(extremeTop))
        assertEquals(inset.bottomLeftY - CropHandles.MIN_GAP, extremeTop.topLeftY, 1e-5f)

        val extremeLeft = CropHandles.move(inset, CropHandles.EDGE_LEFT, -5f, 0f)
        assertTrue(CropHandles.isSound(extremeLeft))
        assertEquals(0f, extremeLeft.topLeftX, 0f)
        assertEquals(0f, extremeLeft.bottomLeftX, 0f)
    }

    /**
     * A quad whose edges have collapsed must not throw.
     *
     * The edge drags clamp a delta between bounds derived from the quad, and an
     * *unsound* quad — three corners on one line, a stored crop from an older
     * revision, a page so skewed the detector's quad folded — makes those bounds
     * cross. `coerceIn(low, high)` throws `IllegalArgumentException` when
     * `low > high`, i.e. the app died **inside a drag**. The corner drags always went
     * through the ordered `clamp`; the four edge drags did not.
     */
    @Test
    fun `a collapsed quad cannot throw out of an edge drag`() {
        // Every horizontal edge at the same y, every vertical edge at the same x:
        // height and width are both zero, so every "max" bound is negative while
        // every "min" bound is zero.
        val collapsed = QuadCorners(
            topLeftX = 0.5f, topLeftY = 0.5f,
            topRightX = 0.5f, topRightY = 0.5f,
            bottomRightX = 0.5f, bottomRightY = 0.5f,
            bottomLeftX = 0.5f, bottomLeftY = 0.5f
        )
        for (edge in listOf(
            CropHandles.EDGE_TOP,
            CropHandles.EDGE_BOTTOM,
            CropHandles.EDGE_LEFT,
            CropHandles.EDGE_RIGHT
        )) {
            // Any of these used to `coerceIn(low, high)` with low > high.
            CropHandles.move(collapsed, edge, 0.4f, -0.4f)
            CropHandles.move(collapsed, edge, -4f, 4f)
        }
    }

    /** The same, for a quad whose corners are past each other. */
    @Test
    fun `an inverted quad cannot throw out of an edge drag`() {
        val inverted = QuadCorners(
            topLeftX = 0.8f, topLeftY = 0.9f,
            topRightX = 0.2f, topRightY = 0.8f,
            bottomRightX = 0.1f, bottomRightY = 0.1f,
            bottomLeftX = 0.9f, bottomLeftY = 0.2f
        )
        for (edge in listOf(
            CropHandles.EDGE_TOP,
            CropHandles.EDGE_BOTTOM,
            CropHandles.EDGE_LEFT,
            CropHandles.EDGE_RIGHT
        )) {
            CropHandles.move(inverted, edge, 1.5f, -1.5f)
        }
    }

    private fun QuadCorners.asList() = listOf(
        topLeftX, topLeftY, topRightX, topRightY,
        bottomRightX, bottomRightY, bottomLeftX, bottomLeftY
    )
}
