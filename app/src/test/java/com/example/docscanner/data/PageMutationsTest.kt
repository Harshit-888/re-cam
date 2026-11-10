package com.example.docscanner.data

import com.example.docscanner.model.Document
import com.example.docscanner.model.ScannedPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PageMutationsTest {

    private fun page(id: String, docId: String = "d1", index: Int = 0) =
        ScannedPage(id = id, documentId = docId, pageIndex = index, imageUri = "/files/pages/$id.jpg")

    private fun doc(vararg pages: ScannedPage) = Document(
        id = "d1",
        title = "Doc",
        createdDate = 1L,
        modifiedDate = 1L,
        pages = pages.toList()
    )

    // ---- append -----------------------------------------------------------

    @Test
    fun `append renumbers the whole list and stamps the document id`() {
        val start = doc(page("a", index = 0), page("b", index = 0)) // duplicate indices on purpose
        val updated = PageMutations.appendTo(start, "d1", listOf(page("c", docId = "WRONG"), page("d")))!!

        assertEquals(listOf("a", "b", "c", "d"), updated.pages.map { it.id })
        assertEquals(listOf(0, 1, 2, 3), updated.pages.map { it.pageIndex })
        // A page handed in with the wrong documentId is corrected, not trusted.
        assertTrue(updated.pages.all { it.documentId == "d1" })
    }

    @Test
    fun `append to a different document is a miss, not a mutation`() {
        val start = doc(page("a"))
        assertNull(PageMutations.appendTo(start, "somebody-else", listOf(page("b"))))
    }

    @Test
    fun `appending nothing returns the same document instance`() {
        val start = doc(page("a"))
        assertSame(start, PageMutations.appendTo(start, "d1", emptyList()))
    }

    // ---- remove -----------------------------------------------------------

    @Test
    fun `removing the last page removes the document`() {
        val start = doc(page("only"))
        val removal = PageMutations.removeFrom(start, "d1", setOf("only"))!!

        assertNull("an emptied document must not survive as a husk", removal.document)
        assertTrue(removal.removedAll)
        assertEquals(listOf("only"), removal.removed.map { it.id })
    }

    @Test
    fun `removing some pages keeps the document and renumbers survivors`() {
        val start = doc(page("a"), page("b"), page("c"))
        val removal = PageMutations.removeFrom(start, "d1", setOf("a", "c"))!!

        val remaining = removal.document
        assertNotNull("the document survives while it still has a page", remaining)
        assertEquals(listOf("b"), remaining!!.pages.map { it.id })
        assertEquals(listOf(0), remaining.pages.map { it.pageIndex })
        assertEquals(listOf("a", "c"), removal.removed.map { it.id })
        assertFalse(removal.removedAll)
    }

    @Test
    fun `removing a set that matches nothing returns the same document and no removals`() {
        val start = doc(page("a"), page("b"))
        val removal = PageMutations.removeFrom(start, "d1", setOf("ghost"))!!

        assertSame(start, removal.document)
        assertTrue(removal.removed.isEmpty())
        assertFalse(removal.removedAll)
    }

    @Test
    fun `removing everything at once removes the document`() {
        val start = doc(page("a"), page("b"), page("c"))
        val removal = PageMutations.removeFrom(start, "d1", setOf("a", "b", "c"))!!

        assertNull(removal.document)
        assertEquals(listOf("a", "b", "c"), removal.removed.map { it.id })
    }

    @Test
    fun `remove from a different document is a miss`() {
        val start = doc(page("a"))
        assertNull(PageMutations.removeFrom(start, "somebody-else", setOf("a")))
    }

    @Test
    fun `the transform never mutates its input`() {
        val original = doc(page("a"), page("b"))
        val snapshot = original.pages.map { it.id to it.pageIndex }

        PageMutations.removeFrom(original, "d1", setOf("a"))
        PageMutations.appendTo(original, "d1", listOf(page("z")))

        assertEquals(snapshot, original.pages.map { it.id to it.pageIndex })
    }
}
