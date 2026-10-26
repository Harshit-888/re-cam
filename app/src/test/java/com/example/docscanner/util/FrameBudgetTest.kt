package com.example.docscanner.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The frame-budget arithmetic, checked without a device.
 *
 * These are the numbers the app judges itself by. The previous revision hardcoded
 * 60 Hz, so on a 120 Hz phone a frame that took two slots was reported as smooth —
 * the instrumentation could not see the problem it existed to find. Everything is
 * now a function of the rate the display reports, and that function is testable
 * here.
 */
class FrameBudgetTest {

    @Test
    fun `a slot is one over the refresh rate`() {
        assertEquals(16_666_666L, FrameBudget.budgetNs(60f))
        assertEquals(8_333_333L, FrameBudget.budgetNs(120f))
        assertEquals(11_111_111L, FrameBudget.budgetNs(90f))
        assertEquals(6_944_444L, FrameBudget.budgetNs(144f))
    }

    @Test
    fun `an implausible rate falls back to the supported band`() {
        // A broken display driver reporting 0 or 1000 must not produce a zero
        // budget, which would mark every frame as missed.
        assertEquals(FrameBudget.budgetNs(30f), FrameBudget.budgetNs(0f))
        assertEquals(FrameBudget.budgetNs(240f), FrameBudget.budgetNs(1000f))
        assertTrue(FrameBudget.budgetNs(0f) > 0L)
    }

    @Test
    fun `the same frame is late at 120 and on time at 60`() {
        val frame = 16_000_000L
        // The whole point of this round: 16 ms is a perfect 60 Hz frame and a
        // two-slot stutter at 120 Hz.
        assertFalse(FrameBudget.isMissed(60f, frame))
        assertTrue(FrameBudget.isMissed(120f, frame))
    }

    @Test
    fun `a frame exactly on budget is not missed`() {
        val budget = FrameBudget.budgetNs(120f)
        assertFalse(FrameBudget.isMissed(120f, budget))
        // Half a slot over is still inside the 1.5x threshold.
        assertFalse(FrameBudget.isMissed(120f, (budget * 3) / 2))
        // Anything beyond it did not fit.
        assertTrue(FrameBudget.isMissed(120f, (budget * 3) / 2 + 1))
    }

    @Test
    fun `a window is janky on a p90 that hitched`() {
        val budget = FrameBudget.budgetNs(120f)
        // A perfect window.
        assertFalse(FrameBudget.isJankyWindow(120f, budget, budget))
        // p90 within 1.25 slots: fine even if one frame was nearly two slots.
        assertFalse(FrameBudget.isJankyWindow(120f, budget, budget * 2 - 1))
        // A single frame that took two whole slots is a dropped frame.
        assertTrue(FrameBudget.isJankyWindow(120f, budget, budget * 2))
        // p90 over 1.25 slots and no single frame over two: still janky.
        assertTrue(FrameBudget.isJankyWindow(120f, budget * 2, budget * 2))
    }

    @Test
    fun `intervals land in honest histogram buckets`() {
        assertEquals(8, FrameBudget.bucketOf(8_333_333L, 100))
        assertEquals(16, FrameBudget.bucketOf(16_000_000L, 100))
        assertEquals(0, FrameBudget.bucketOf(0L, 100))
        // A pathological frame is clamped, not out of bounds.
        assertEquals(99, FrameBudget.bucketOf(5_000_000_000L, 100))
    }

    @Test
    fun `p90 of an even histogram is the top of the mass`() {
        // 100 frames all on 8 ms: the 90th percentile is 8.
        val histogram = IntArray(100).apply { this[8] = 100 }
        assertEquals(8, FrameBudget.percentile90Ms(histogram))
    }

    @Test
    fun `p90 ignores a small tail of slow frames`() {
        // 95 frames at 8 ms and 5 at 17 ms: p90 stays at 8, which is why p90 is
        // reported next to `missed` rather than instead of it.
        val histogram = IntArray(100).apply {
            this[8] = 95
            this[17] = 5
        }
        assertEquals(8, FrameBudget.percentile90Ms(histogram))
    }

    @Test
    fun `p90 moves when the tail is the story`() {
        // 80 frames at 8 ms and 20 at 17 ms: the 90th percentile is in the slow
        // bucket, which is what a genuinely stuttering scroll looks like.
        val histogram = IntArray(100).apply {
            this[8] = 80
            this[17] = 20
        }
        assertEquals(17, FrameBudget.percentile90Ms(histogram))
    }

    @Test
    fun `p90 of nothing is reported as unknown, not as zero`() {
        assertEquals(-1, FrameBudget.percentile90Ms(IntArray(100)))
    }

    @Test
    fun `fps is the reciprocal of the mean interval`() {
        assertEquals(120.0, FrameBudget.fpsOf(8_333_333L), 0.01)
        assertEquals(60.0, FrameBudget.fpsOf(16_666_666L), 0.01)
        assertEquals(0.0, FrameBudget.fpsOf(0L), 0.0)
    }

    @Test
    fun `log formatting is one decimal place with a dot`() {
        assertEquals("118.4", FrameBudget.decimal(118.44))
        assertEquals("8.3", FrameBudget.decimalMs(8_333_333L))
        assertEquals("0.4", FrameBudget.decimalMs(400_000L))
    }
}
