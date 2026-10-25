package com.example.docscanner.util

import com.example.docscanner.model.Document
import com.example.docscanner.model.ScannedPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Ranking tests for [DocumentSearch].
 *
 * The point of the index is not "does it match" — the old `contains` loop already
 * answered that — but that the best match comes first and that the result carries
 * the passage it matched.
 */
class DocumentSearchTest {

    private fun document(
        id: String,
        title: String,
        vararg pages: String,
        tags: List<String> = emptyList(),
        modified: Long = 0L
    ) = Document(
        id = id,
        title = title,
        createdDate = modified,
        modifiedDate = modified,
        tags = tags,
        pages = pages.mapIndexed { index, text ->
            ScannedPage(
                id = "$id-$index",
                documentId = id,
                pageIndex = index,
                imageUri = "/pages/$id-$index.jpg",
                ocrText = text
            )
        }
    )

    @Test
    fun titleMatchOutranksBodyMatch() {
        val index = DocumentSearch.Index(
            listOf(
                document("body", "Notes", "work done in the garage"),
                document("title", "Garage", "nothing here")
            )
        )
        val hits = index.search("garage")
        assertEquals(2, hits.size)
        assertEquals("title", hits.first().document.id)
    }

    @Test
    fun moreOccurrencesRankHigherOnComparableLength() {
        val index = DocumentSearch.Index(
            listOf(
                document("once", "A", "invoice"),
                document("many", "B", "invoice invoice invoice")
            )
        )
        assertEquals("many", index.search("invoice").first().document.id)
    }

    @Test
    fun phraseInTitleBeatsScatteredTerms() {
        val index = DocumentSearch.Index(
            listOf(
                document("scattered", "Tax notes", "return the form for the tax office"),
                document("phrase", "Tax return", "unrelated body")
            )
        )
        val hits = index.search("tax return")
        assertEquals("phrase", hits.first().document.id)
    }

    @Test
    fun snippetShowsTheMatchedPassage() {
        val long = "Chapter one. ".repeat(20) + "The landlord must return the deposit. " + "Closing notes. ".repeat(20)
        val index = DocumentSearch.Index(listOf(document("lease", "Lease", long)))
        val hit = index.search("deposit").single()
        val snippet = hit.snippet
        assertNotNull(snippet)
        requireNotNull(snippet)
        assertTrue("snippet should contain the match: $snippet", snippet.contains("deposit", ignoreCase = true))
        assertTrue("snippet should be trimmed to a window, not the whole page", snippet.length < long.length)
        assertTrue("a mid-page window is marked as clipped", snippet.startsWith("…"))
    }

    @Test
    fun titleOnlyMatchHasNoSnippet() {
        val index = DocumentSearch.Index(listOf(document("empty", "Annual report")))
        val hit = index.search("annual").single()
        assertNull(hit.snippet)
    }

    @Test
    fun unmatchedQueryReturnsNothing() {
        val index = DocumentSearch.Index(listOf(document("a", "Invoice", "total due")))
        assertTrue(index.search("passport").isEmpty())
        assertTrue(index.search("   ").isEmpty())
    }

    @Test
    fun tagMatchesAreSearchable() {
        val index = DocumentSearch.Index(
            listOf(
                document("tagged", "Scan 1", "illegible", tags = listOf("passport")),
                document("other", "Scan 2", "illegible")
            )
        )
        val hits = index.search("passport")
        assertEquals(1, hits.size)
        assertEquals("tagged", hits.first().document.id)
    }

    @Test
    fun tokenizerKeepsShortWordsAndNonLatinScript() {
        assertEquals(listOf("tax", "return", "2024"), DocumentSearch.tokenize("Tax return — 2024"))
        // One-letter words are noise in a document search.
        assertEquals(listOf("a4", "paper"), DocumentSearch.tokenize("a A4 paper"))
        // Devanagari words are letters, not separators.
        assertTrue(DocumentSearch.tokenize("कर विवरण").isNotEmpty())
    }

    @Test
    fun secondPageMatchIsFound() {
        val index = DocumentSearch.Index(
            listOf(document("multi", "Two pages", "first page only", "the warranty number is 4471"))
        )
        val hit = index.search("warranty").single()
        assertNotNull(hit.snippet)
        assertTrue(hit.snippet!!.contains("warranty", ignoreCase = true))
    }
}
