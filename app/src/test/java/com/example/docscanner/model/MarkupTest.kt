package com.example.docscanner.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Undo/redo and the geometry contract of a stroke.
 *
 * The failure these guard against is quiet: a stroke list that keeps a redo branch it
 * cannot honour, or a width that scales with the long side of the page, produces a
 * drawing that looks right in the preview and wrong in the PDF.
 */
class MarkupTest {

    private fun stroke(
        tool: MarkupTool = MarkupTool.PEN,
        points: Int = 3,
        widthFraction: Float = 0.008f
    ) = MarkupStroke(
        tool = tool,
        widthFraction = widthFraction,
        points = List(points) { MarkupPoint(it / 10f, it / 10f) }
    )

    // ------------------------------------------------------------------ history

    @Test
    fun `nothing to undo or redo on an empty history`() {
        val history = MarkupHistory()
        assertTrue(history.isEmpty)
        assertFalse(history.canUndo)
        assertFalse(history.canRedo)
        assertNull(history.undo())
        assertNull(history.redo())
    }

    @Test
    fun `undo and redo walk the same strokes back and forth`() {
        val history = MarkupHistory()
        val first = stroke()
        val second = stroke(tool = MarkupTool.HIGHLIGHTER)
        history.add(first)
        history.add(second)

        assertEquals(2, history.size)
        assertEquals(second, history.undo())
        assertEquals(listOf(first), history.strokes)
        assertTrue(history.canRedo)

        assertEquals(second, history.redo())
        assertEquals(listOf(first, second), history.strokes)
        assertFalse(history.canRedo)
    }

    @Test
    fun `drawing after undo drops the redo branch`() {
        val history = MarkupHistory()
        history.add(stroke())
        history.add(stroke())
        history.undo()
        assertTrue(history.canRedo)

        history.add(stroke(tool = MarkupTool.WHITEOUT))
        // Offering a redo that would append a stroke behind the new one would produce
        // an order the user never drew.
        assertFalse(history.canRedo)
        assertEquals(2, history.size)
    }

    @Test
    fun `a tap with no drag is not an annotation`() {
        val history = MarkupHistory()
        history.add(stroke(points = 1))
        assertTrue(history.isEmpty)
        // Otherwise Undo appears to do nothing at all.
        assertFalse(history.canUndo)
    }

    @Test
    fun `clear restores everything on one undo, in order`() {
        val a = stroke()
        val b = stroke(tool = MarkupTool.HIGHLIGHTER)
        val history = MarkupHistory(listOf(a, b))

        history.clear()
        assertTrue(history.isEmpty)
        assertTrue(history.canUndo)
        assertFalse(history.canRedo)

        history.undo()
        assertEquals(listOf(a, b), history.strokes)
        // The restore was a single step, so Undo now offers the strokes themselves
        // rather than another crack at the clear — and there is no redo of it.
        assertFalse(history.canRedo)
        assertTrue(history.canUndo)
    }

    @Test
    fun `drawing after clear forgets the cleared strokes`() {
        val history = MarkupHistory(listOf(stroke(), stroke()))
        history.clear()
        history.add(stroke(tool = MarkupTool.WHITEOUT))

        assertEquals(1, history.size)
        // The cleared strokes were not restored by Undo, so they must not come back
        // through it: the only thing undoable is the stroke just drawn.
        history.undo()
        assertTrue(history.isEmpty)
        assertNull(history.undo())
    }

    @Test
    fun `clearing an empty history is not an undoable step`() {
        val history = MarkupHistory()
        history.clear()
        assertFalse(history.canUndo)
    }

    @Test
    fun `an existing page's markup is the starting point, not a fresh sheet`() {
        val existing = listOf(stroke(), stroke(tool = MarkupTool.WHITEOUT))
        val history = MarkupHistory(existing)
        assertEquals(existing, history.strokes)
        history.add(stroke())
        assertEquals(3, history.size)
    }

    // ----------------------------------------------------------------- geometry

    @Test
    fun `stroke width follows the short side, so orientation does not change it`() {
        val ink = stroke(widthFraction = 0.01f)
        val portrait = ink.widthPx(pageWidth = 1000, pageHeight = 2000)
        val landscape = ink.widthPx(pageWidth = 2000, pageHeight = 1000)
        assertEquals(portrait, landscape, 0.001f)
        assertEquals(10f, portrait, 0.001f)
    }

    @Test
    fun `a stroke is never invisible`() {
        val hairline = stroke(widthFraction = 0.00001f)
        assertTrue(hairline.widthPx(100, 100) >= 1f)
    }

    @Test
    fun `a width must be positive`() {
        try {
            stroke(widthFraction = 0f)
            throw AssertionError("a zero-width stroke was accepted")
        } catch (expected: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `a single point is not drawable, two are`() {
        assertFalse(stroke(points = 1).isDrawable)
        assertTrue(stroke(points = 2).isDrawable)
    }
}
