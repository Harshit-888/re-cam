package com.example.docscanner.util

import com.example.docscanner.model.QuadCorners
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the gate between the ML border model and the warp.
 *
 * A model boundary is not like a fitted line: it answers with a number, and a wrong
 * number is just as confident as a right one. These cases are the shapes a badly
 * scaled input, a wrong corner order or a mis-read tensor actually produce — a crossed
 * quad, a sliver, a page 2 % of the frame — and every one of them has to be rejected
 * before `CropAndFilterScreen` applies it, because the screen applies
 * `detection.corners` whether or not the detection was *confident*.
 */
class QuadSanityTest {

    @Test
    fun `a normal page inside the frame is accepted`() {
        assertNull(QuadSanity.rejection(QuadCorners.inset(0.05f)))
        assertTrue(QuadSanity.isPlausible(QuadCorners.inset(0.1f)))
    }

    @Test
    fun `a slightly trapezoidal page is accepted`() {
        // Photographs of pages are rarely rectangles; the model is allowed to find a
        // perspective quad.
        val quad = QuadCorners(
            topLeftX = 0.08f, topLeftY = 0.06f,
            topRightX = 0.94f, topRightY = 0.10f,
            bottomRightX = 0.90f, bottomRightY = 0.95f,
            bottomLeftX = 0.12f, bottomLeftY = 0.90f
        )
        assertNull(QuadSanity.rejection(quad))
    }

    @Test
    fun `a crossed quad is rejected`() {
        // Top-left to the right of top-right: the warped image self-overlaps, which
        // reads as a broken export rather than a bad crop.
        val crossed = QuadCorners(
            topLeftX = 0.9f, topLeftY = 0.1f,
            topRightX = 0.1f, topRightY = 0.1f,
            bottomRightX = 0.9f, bottomRightY = 0.9f,
            bottomLeftX = 0.1f, bottomLeftY = 0.9f
        )
        val reason = QuadSanity.rejection(crossed)
        assertNotNull(reason)
        assertTrue("reason should name the edge: $reason", reason!!.contains("backwards"))
    }

    @Test
    fun `a sliver is rejected even when it is large`() {
        // A narrow strip down the page: 9 % of the frame, so the area rule passes and
        // only the shape rule can catch it. This is what a model does when it locks
        // onto the fold of a page rather than its edge.
        val sliver = QuadCorners(
            topLeftX = 0.10f, topLeftY = 0.05f,
            topRightX = 0.20f, topRightY = 0.05f,
            bottomRightX = 0.20f, bottomRightY = 0.95f,
            bottomLeftX = 0.10f, bottomLeftY = 0.95f
        )
        assertEquals(0.09f, QuadSanity.areaRatio(sliver), 1e-4f)
        val reason = QuadSanity.rejection(sliver)
        assertNotNull(reason)
        assertTrue("reason should be about the shape: $reason", reason!!.contains("sliver"))
    }

    @Test
    fun `a tiny quad in the corner of the frame is rejected`() {
        // 2 % of the frame: what a model that has locked onto a logo produces.
        val speck = QuadCorners(
            topLeftX = 0.10f, topLeftY = 0.10f,
            topRightX = 0.24f, topRightY = 0.10f,
            bottomRightX = 0.24f, bottomRightY = 0.24f,
            bottomLeftX = 0.10f, bottomLeftY = 0.24f
        )
        assertFalse(QuadSanity.isPlausible(speck))
        assertTrue(QuadSanity.rejection(speck)!!.contains("area"))
    }

    @Test
    fun `a corner far outside the frame is rejected`() {
        val outside = QuadCorners.inset(0.05f).copy(topLeftX = -0.4f)
        assertTrue(QuadSanity.rejection(outside)!!.contains("outside"))
    }

    @Test
    fun `a NaN corner is rejected rather than propagated`() {
        // A model fed the wrong dtype scale can emit NaN. Left alone it reaches
        // `Matrix.setPolyToPoly`, and the page disappears with no error anywhere.
        val nan = QuadCorners.inset(0.05f).copy(topLeftY = Float.NaN)
        assertTrue(QuadSanity.rejection(nan)!!.contains("not a number"))
    }

    @Test
    fun `area is measured by the shoelace formula`() {
        assertEquals(1f, QuadSanity.areaRatio(QuadCorners.FULL_FRAME), 1e-4f)
        assertEquals(0.64f, QuadSanity.areaRatio(QuadCorners.inset(0.1f)), 1e-4f)
        // Winding does not change the magnitude.
        val reversed = QuadCorners(
            topLeftX = 0f, topLeftY = 1f,
            topRightX = 1f, topRightY = 1f,
            bottomRightX = 1f, bottomRightY = 0f,
            bottomLeftX = 0f, bottomLeftY = 0f
        )
        assertEquals(1f, QuadSanity.areaRatio(reversed), 1e-4f)
    }
}
