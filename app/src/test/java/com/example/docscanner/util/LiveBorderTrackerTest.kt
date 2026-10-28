package com.example.docscanner.util

import com.example.docscanner.model.QuadCorners
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The rules that turn a per-frame detection into a steady viewfinder outline.
 *
 * These are the decisions a user judges the *live* border by, and none of them are
 * about detection accuracy — they are about what happens between two frames, which
 * is where "it flickers" and "it jumps" come from. Each test below is one of those
 * behaviours, stated as a scene:
 *
 * 1. One missed frame must not take the outline away.
 * 2. A sub-threshold wobble must not move it.
 * 3. A real move must be followed — and followed smoothly, not teleported.
 * 4. Nothing plausible must never be outlined at all, however many frames pass.
 * 5. A stream of nonsense must take the outline away in a couple of frames.
 * 6. The outline must land on the page in the view, letterboxing included.
 */
class LiveBorderTrackerTest {

    /** A portrait page inset a fifth into the frame. */
    private val page = QuadCorners(
        topLeftX = 0.20f, topLeftY = 0.10f,
        topRightX = 0.80f, topRightY = 0.12f,
        bottomRightX = 0.78f, bottomRightY = 0.92f,
        bottomLeftX = 0.18f, bottomLeftY = 0.90f
    )

    /** The same page nudged by [dx] horizontally, for wobble tests. */
    private fun pageAt(dx: Float) = page.copy(
        topLeftX = page.topLeftX + dx,
        topRightX = page.topRightX + dx,
        bottomRightX = page.bottomRightX + dx,
        bottomLeftX = page.bottomLeftX + dx
    )

    /**
     * Runs the tracker up to a full lock: [LiveBorderTracker.CONFIDENCE_MAX] good
     * frames, which is where it sits while the user is actually framing a page.
     */
    private fun settle(tracker: LiveBorderTracker): LiveBorderTracker.State {
        var state = tracker.onFrame(null, 0)
        repeat(LiveBorderTracker.CONFIDENCE_MAX) { state = tracker.onFrame(page, 4) }
        return state
    }

    // ---------------------------------------------------------------- 1. decays

    @Test
    fun `one missed frame keeps the outline`() {
        val tracker = LiveBorderTracker()
        val shown = settle(tracker)
        assertTrue("three good frames should show the outline", shown.visible)

        val missed = tracker.onFrame(null, 0)
        assertTrue("a single dropped frame must not hide the outline", missed.visible)
        assertEquals("the held quad is still the one to draw", 0.20f, missed.corners.topLeftX, 1e-4f)
    }

    @Test
    fun `the outline appears only after a few good frames`() {
        val tracker = LiveBorderTracker()
        val first = tracker.onFrame(page, 4)
        val second = tracker.onFrame(page, 4)
        assertFalse("one frame is not enough to trust", first.visible)
        assertFalse("nor two", second.visible)
        assertTrue("three is", tracker.onFrame(page, 4).visible)
    }

    @Test
    fun `it disappears after repeated failures`() {
        val tracker = LiveBorderTracker()
        settle(tracker)
        // A locked outline (6) survives two misses (4, then 2) and retires on the
        // third: three frames at 6 Hz is half a second, which is the difference
        // between "it noticed" and "it is broken".
        assertTrue(tracker.onFrame(null, 0).visible)
        assertTrue(tracker.onFrame(null, 0).visible)
        assertFalse("three bad frames retire the outline", tracker.onFrame(null, 0).visible)
    }

    // ---------------------------------------------------------------- 2. wobble

    @Test
    fun `a wobble under the snap distance does not move the outline`() {
        val tracker = LiveBorderTracker()
        settle(tracker)
        // A third of the snap distance: exposure settling, not movement.
        val wobbled = tracker.onFrame(pageAt(LiveBorderTracker.SNAP / 3f), 4)
        assertEquals(0.20f, wobbled.corners.topLeftX, 1e-4f)
    }

    // ----------------------------------------------------------------- 3. motion

    @Test
    fun `a real move is followed over a few frames, not in one`() {
        val tracker = LiveBorderTracker()
        settle(tracker)
        val moved = pageAt(0.10f)

        val firstStep = tracker.onFrame(moved, 4)
        // Half the distance on the first frame: moved, but not teleported.
        assertEquals(0.25f, firstStep.corners.topLeftX, 1e-3f)
        assertTrue(firstStep.visible)

        var state = firstStep
        repeat(6) { state = tracker.onFrame(moved, 4) }
        // It settles inside the snap window rather than exactly on the target —
        // deliberately: holding the last drawn position is what stops the outline
        // breathing while a detector jitters by a pixel.
        assertEquals(
            "it arrives to within the snap window",
            0.30f,
            state.corners.topLeftX,
            LiveBorderTracker.SNAP + 1e-3f
        )
    }

    // ------------------------------------------------------------ 4. rejections

    @Test
    fun `a frame with fewer than three sides is never outlined`() {
        val tracker = LiveBorderTracker()
        var state = tracker.onFrame(null, 0)
        repeat(10) { state = tracker.onFrame(page, 2) }
        assertFalse("two real sides is a guess, not a border", state.visible)
    }

    @Test
    fun `a sliver quad is never outlined`() {
        val tracker = LiveBorderTracker()
        val sliver = QuadCorners(
            topLeftX = 0.10f, topLeftY = 0.50f,
            topRightX = 0.90f, topRightY = 0.50f,
            bottomRightX = 0.90f, bottomRightY = 0.52f,
            bottomLeftX = 0.10f, bottomLeftY = 0.52f
        )
        var state = tracker.onFrame(null, 0)
        repeat(10) { state = tracker.onFrame(sliver, 4) }
        assertFalse("a page 2 % tall is a detector artefact", state.visible)
    }

    @Test
    fun `a tiny quad is never outlined`() {
        val tracker = LiveBorderTracker()
        val speck = QuadCorners(
            topLeftX = 0.40f, topLeftY = 0.40f,
            topRightX = 0.50f, topRightY = 0.40f,
            bottomRightX = 0.50f, bottomRightY = 0.50f,
            bottomLeftX = 0.40f, bottomLeftY = 0.50f
        )
        var state = tracker.onFrame(null, 0)
        repeat(10) { state = tracker.onFrame(speck, 4) }
        assertFalse("1 % of the frame is not a page to photograph", state.visible)
    }

    @Test
    fun `reset forgets everything`() {
        val tracker = LiveBorderTracker()
        settle(tracker)
        tracker.reset()
        assertFalse(tracker.showing)
        assertFalse(tracker.onFrame(page, 4).visible)
    }

    // -------------------------------------------------------------- 5. mapping

    @Test
    fun `a matching aspect fills the view`() {
        val rect = SceneRect.fitCenter(1080f, 1440f, 3f / 4f)
        assertEquals(0f, rect.left, 1e-3f)
        assertEquals(0f, rect.top, 1e-3f)
        assertEquals(1080f, rect.width, 1e-3f)
        assertEquals(1440f, rect.height, 1e-3f)
    }

    @Test
    fun `a wider stream is letterboxed top and bottom`() {
        // A 16:9 stream in a 3:4 view: full width, bars above and below.
        val rect = SceneRect.fitCenter(1080f, 1440f, 16f / 9f)
        assertEquals(1080f, rect.width, 1e-3f)
        assertEquals(607.5f, rect.height, 1e-3f)
        assertEquals(416.25f, rect.top, 1e-3f)
        assertEquals(0f, rect.left, 1e-3f)
    }

    @Test
    fun `a taller stream is pillarboxed left and right`() {
        val rect = SceneRect.fitCenter(1080f, 1440f, 9f / 16f)
        assertEquals(810f, rect.width, 1e-3f)
        assertEquals(1440f, rect.height, 1e-3f)
        assertEquals(135f, rect.left, 1e-3f)
    }

    @Test
    fun `corners map inside the stream rectangle`() {
        val rect = SceneRect.fitCenter(1080f, 1440f, 16f / 9f)
        assertEquals(0f, rect.x(0f), 1e-3f)
        assertEquals(1080f, rect.x(1f), 1e-3f)
        assertEquals(416.25f, rect.y(0f), 1e-3f)
        assertEquals(1023.75f, rect.y(1f), 1e-3f)
    }

    @Test
    fun `a degenerate view does not divide by zero`() {
        val rect = SceneRect.fitCenter(0f, 0f, 3f / 4f)
        assertEquals(0f, rect.width, 0f)
        assertEquals(0f, rect.height, 0f)
    }

    // -------------------------------------------------------------- 6. rotation

    @Test
    fun `rotating a frame ninety degrees makes it upright`() {
        // A 4×2 landscape buffer, row-major: 0 1 2 3 / 4 5 6 7.
        val src = GreyFrame(4, 2, floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f))
        val out = GreyFrames.rotate(src, 90)
        assertEquals("width and height swap", 2, out.width)
        assertEquals(4, out.height)
        //   buffer           upright (2 wide, 4 tall)
        //   0 1 2 3          4 0
        //   4 5 6 7          5 1
        //                    6 2
        //                    7 3
        val expected = floatArrayOf(4f, 0f, 5f, 1f, 6f, 2f, 7f, 3f)
        for (i in expected.indices) assertEquals(expected[i], out.pixels[i], 0f)
    }

    @Test
    fun `rotating by a full turn is the identity`() {
        val src = GreyFrame(3, 3, FloatArray(9) { it.toFloat() })
        val out = GreyFrames.rotate(src, 360)
        assertEquals(3, out.width)
        for (i in 0 until 9) assertEquals(src.pixels[i], out.pixels[i], 0f)
    }

    @Test
    fun `rotating by a hundred and eighty reverses the buffer`() {
        val src = GreyFrame(3, 2, floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f))
        val out = GreyFrames.rotate(src, 180)
        assertEquals(3, out.width)
        assertEquals(2, out.height)
        for (i in 0 until 6) assertEquals(src.pixels[5 - i], out.pixels[i], 0f)
    }

    @Test
    fun `two ninety degree turns match one eighty`() {
        val src = GreyFrame(3, 2, floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f))
        val twice = GreyFrames.rotate(GreyFrames.rotate(src, 90), 90)
        val once = GreyFrames.rotate(src, 180)
        assertEquals(once.width, twice.width)
        assertEquals(once.height, twice.height)
        for (i in once.pixels.indices) assertEquals(once.pixels[i], twice.pixels[i], 0f)
    }
}
