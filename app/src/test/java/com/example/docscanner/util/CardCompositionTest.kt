package com.example.docscanner.util

import com.example.docscanner.util.CardComposition.Axis
import com.example.docscanner.util.CardComposition.Size
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * The ID-card page layout. These are the rules that are invisible when right and
 * embarrassing when wrong: a squeezed card, a card under the margin, or two cards
 * overlapping by a pixel all print fine and look wrong.
 */
class CardCompositionTest {

    /** A4 at 200 dpi, the size `createIdCardDocument` composes at. */
    private val a4Portrait = Size(1654, 2339)

    /** 85.6 × 54 mm card, photographed at 1000 px wide. */
    private val card = Size(1000, 631)

    /**
     * `assertNotNull` is a `void` method in JUnit 4, so `val x = assertNotNull(…)` is a
     * `Unit` in Kotlin and every `.axis` after it is a compile error. Assert, then `!!`.
     */
    private fun layout(
        page: Size,
        front: Size,
        back: Size,
        axis: Axis? = null,
        gapPx: Int? = null,
        marginPx: Int? = null
    ): CardComposition.Placement {
        val placement = CardComposition.placement(
            page = page,
            front = front,
            back = back,
            axis = axis ?: CardComposition.defaultAxis(page),
            marginPx = marginPx ?: CardComposition.defaultMargin(page),
            gapPx = gapPx ?: CardComposition.defaultGap(page)
        )
        assertNotNull("no layout for ${page.width}x${page.height}", placement)
        return placement!!
    }

    private fun Box.aspect(): Double = width.toDouble() / height.toDouble()

    private fun assertAspectKept(expected: Size, box: CardComposition.Box, tolerance: Double = 0.01) {
        val want = expected.width.toDouble() / expected.height.toDouble()
        val got = box.aspect()
        assertTrue(
            "aspect drifted: wanted $want, got $got for $box",
            abs(want - got) / want <= tolerance
        )
    }

    @Test
    fun `a portrait page puts the cards side by side`() {
        val placement = layout(a4Portrait, card, card)
        assertEquals(Axis.SIDE_BY_SIDE, placement.axis)
        // Front on the left, back on the right.
        assertTrue(placement.front.right <= placement.back.left)
        assertTrue(placement.front.left < placement.back.left)
    }

    @Test
    fun `a landscape page stacks the cards`() {
        val landscape = Size(2339, 1654)
        val placement = layout(landscape, card, card)
        assertEquals(Axis.STACKED, placement.axis)
        assertTrue(placement.front.bottom <= placement.back.top)
    }

    @Test
    fun `nothing leaves the printable area, and the cards never overlap`() {
        val placement = layout(a4Portrait, card, card)
        val margin = CardComposition.defaultMargin(a4Portrait)
        val gap = CardComposition.defaultGap(a4Portrait)

        listOf(placement.front, placement.back).forEach { box ->
            assertTrue("left margin on $box", box.left >= margin)
            assertTrue("top margin on $box", box.top >= margin)
            assertTrue("right margin on $box", box.right <= a4Portrait.width - margin)
            assertTrue("bottom margin on $box", box.bottom <= a4Portrait.height - margin)
        }
        assertFalse(placement.front.overlaps(placement.back))
        assertTrue(
            "the gap is at least $gap, was ${placement.back.left - placement.front.right}",
            placement.back.left - placement.front.right >= gap
        )
    }

    @Test
    fun `the same card scanned at two resolutions comes out identical`() {
        // Same aspect, different pixel size: the cells are equal, so the fitted
        // rectangles are equal to the pixel.
        val back = Size(500, 316) // 1.582, within rounding of 1.585
        val placement = layout(a4Portrait, Size(1000, 632), back)
        assertEquals(placement.front.width, placement.back.width)
        assertEquals(placement.front.height, placement.back.height)
    }

    @Test
    fun `a differently cropped back keeps the same width`() {
        // Both cards are width-constrained in their half-page cell, so they match
        // across; the heights follow each scan's own aspect rather than being
        // stretched to match, which is the whole point of preserving it.
        val placement = layout(a4Portrait, card, Size(980, 640))
        assertEquals(placement.front.width, placement.back.width)
        assertAspectKept(card, placement.front)
        assertAspectKept(Size(980, 640), placement.back)
    }

    @Test
    fun `the aspect ratio survives`() {
        val placement = layout(a4Portrait, card, Size(700, 900))
        assertAspectKept(card, placement.front)
        assertAspectKept(Size(700, 900), placement.back)
    }

    @Test
    fun `a card sits centred in its half, not jammed against one side`() {
        val placement = layout(a4Portrait, card, card)
        val margin = CardComposition.defaultMargin(a4Portrait)
        val usableWidth = a4Portrait.width - 2 * margin
        val gap = CardComposition.defaultGap(a4Portrait)
        val cell = (usableWidth - gap) / 2

        // Each card is centred horizontally inside its own cell, so the space to its
        // left and right inside that cell matches to within rounding.
        val frontLeftInset = placement.front.left - margin
        val frontRightInset = (margin + cell) - placement.front.right
        assertTrue(
            "front not centred: $frontLeftInset vs $frontRightInset",
            abs(frontLeftInset - frontRightInset) <= 1
        )
        // And vertically inside the full height.
        val topInset = placement.front.top - margin
        val bottomInset = (a4Portrait.height - margin) - placement.front.bottom
        assertTrue("front not centred vertically: $topInset vs $bottomInset", abs(topInset - bottomInset) <= 1)
    }

    @Test
    fun `a page too small for the requested margins has no layout`() {
        // The default margins are fractions, so even a 10x10 page has room. What must
        // be refused is a page that cannot hold the margin it was actually asked for.
        assertNull(CardComposition.placement(Size(10, 10), card, card, marginPx = 6))
        assertNull(CardComposition.placement(Size(100, 100), card, card, gapPx = 500))
    }

    @Test
    fun `the axis can be forced`() {
        val forced = layout(a4Portrait, card, card, axis = Axis.STACKED)
        assertEquals(Axis.STACKED, forced.axis)
        assertTrue(forced.front.bottom <= forced.back.top)
        assertAspectKept(card, forced.front)
    }

    @Test
    fun `a zero gap still does not overlap`() {
        val placement = layout(a4Portrait, card, card, gapPx = 0)
        assertFalse(placement.front.overlaps(placement.back))
    }

    @Test
    fun `an extreme scan is contained rather than stretched`() {
        // A back side photographed in portrait: tall and narrow.
        val tall = Size(400, 1200)
        val placement = layout(a4Portrait, card, tall)
        assertAspectKept(tall, placement.back)
        assertTrue(placement.back.right <= a4Portrait.width)
        assertTrue(placement.back.bottom <= a4Portrait.height)
        assertFalse(placement.front.overlaps(placement.back))
    }
}
