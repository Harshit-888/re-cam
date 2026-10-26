package com.example.docscanner.util

import com.example.docscanner.model.Document
import com.example.docscanner.model.ScannedPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Magic Search: the behaviours that make it more than a `contains`.
 *
 * Every ranking expectation below was checked against an independent
 * implementation of the same formula before it was written here, and the expansion
 * vectors are shared with the Rust kernel's own unit tests — so a change that
 * silently reorders results in one implementation fails a test in the other.
 */
class MagicSearchTest {

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

    // ---------------------------------------------------------------- typo

    @Test
    fun aMisspellingStillFindsTheDocument() {
        val index = DocumentSearch.Index(
            listOf(
                document("inv", "Invoice March", "total due 5000"),
                document("other", "Notes", "lunch with sam")
            )
        )
        assertEquals("inv", index.search("invoce").first().document.id)
    }

    @Test
    fun aTypoOnAShortWordIsNotGuessedAt() {
        // "toy" is one edit from "tax", and one edit on three letters is a different
        // word far more often than it is a typo. Matching it would be worse than
        // returning nothing.
        val index = DocumentSearch.Index(listOf(document("tax", "Tax", "the tax is due")))
        assertTrue(index.search("toy").isEmpty())
    }

    @Test
    fun aPrefixFindsTheWordWhileItIsStillBeingTyped() {
        val index = DocumentSearch.Index(
            listOf(
                document("inv", "Invoice March", "total due 5000"),
                document("other", "Notes", "lunch with sam")
            )
        )
        assertEquals("inv", index.search("invoi").first().document.id)
    }

    // ------------------------------------------------------------- stemming

    @Test
    fun pluralsAndTheirSingularsMeet() {
        val index = DocumentSearch.Index(listOf(document("a", "March", "invoices sent")))
        assertEquals(1, index.search("invoice").size)
        assertEquals(1, index.search("invoices").size)
        assertEquals(1, index.search("INVOICES").size)
    }

    @Test
    fun theStemmerLeavesOtherScriptsAlone() {
        assertEquals("कर", DocumentSearch.stem("कर"))
        assertEquals("invoice", DocumentSearch.stem("invoices"))
        assertEquals("book", DocumentSearch.stem("booked"))
        assertEquals("invoice", DocumentSearch.stem("invoice"))
    }

    // -------------------------------------------------------------- phrases

    @Test
    fun aQuotedPhraseOutranksTheSameWordsScattered() {
        val index = DocumentSearch.Index(
            listOf(
                document("scattered", "Tax notes", "return the form for the tax office"),
                document("phrase", "Tax return", "unrelated body")
            )
        )
        assertEquals("phrase", index.search("\"tax return\"").first().document.id)
        // …and the same is true without the quotes: adjacency is evidence either way.
        assertEquals("phrase", index.search("tax return").first().document.id)
    }

    @Test
    fun aQueryThatIsOnlyAPhraseStillSearches() {
        val index = DocumentSearch.Index(listOf(document("a", "Statement", "the tax return is filed")))
        val parsed = DocumentSearch.parseQuery("\"tax return\"")
        assertEquals(listOf("tax", "return"), parsed.terms)
        assertEquals(1, index.search("\"tax return\"").size)
    }

    // ----------------------------------------------------------- exclusions

    @Test
    fun aMinusExcludesTheDocumentThatContainsTheWord() {
        val index = DocumentSearch.Index(
            listOf(
                document("draft", "Invoice", "draft copy"),
                document("final", "Invoice", "final copy")
            )
        )
        val hits = index.search("invoice -draft")
        assertEquals(1, hits.size)
        assertEquals("final", hits.first().document.id)
    }

    // -------------------------------------------------------------- synonyms

    @Test
    fun aCuratedSpellingGroupMatches() {
        val index = DocumentSearch.Index(
            listOf(
                document("aad", "Identity", "aadhar number 1234"),
                document("x", "Other", "nothing here")
            )
        )
        assertEquals("aad", index.search("aadhaar").first().document.id)
    }

    // ----------------------------------------------------------------- years

    @Test
    fun aYearInTheQueryPicksTheYearInTheDocument() {
        val index = DocumentSearch.Index(
            listOf(
                document("y23", "Statement 2023", "invoice for 2023"),
                document("y24", "Statement 2024", "invoice for 2024")
            )
        )
        assertEquals("y23", index.search("invoice 2023").first().document.id)
        assertEquals("y24", index.search("invoice 2024").first().document.id)
    }

    // ------------------------------------------------------------- recency

    @Test
    fun recencyBreaksATieWithoutOverrulingRelevance() {
        val day = 86_400_000L
        val index = DocumentSearch.Index(
            listOf(
                document("old", "Invoice", "total due", modified = 0L),
                document("new", "Invoice", "total due", modified = 365 * day)
            )
        )
        // Identical text: the newer one wins.
        assertEquals("new", index.search("invoice").first().document.id)

        val relevant = DocumentSearch.Index(
            listOf(
                document("old", "Passport", "passport number k1234567", modified = 0L),
                document("new", "Notes", "nothing relevant", modified = 365 * day)
            )
        )
        // The relevant document wins however old it is.
        assertEquals("old", relevant.search("passport").first().document.id)
    }

    // ------------------------------------------------------------- snippets

    @Test
    fun theSnippetMarksTheWordsThatMatched() {
        val text = "Chapter one. ".repeat(20) + "The landlord must return the deposit. " +
            "Closing notes. ".repeat(20)
        val index = DocumentSearch.Index(listOf(document("lease", "Lease", text)))
        val hit = index.search("deposit").single()
        val snippet = hit.snippet
        assertNotNull(snippet)
        requireNotNull(snippet)
        assertTrue(hit.highlights.isNotEmpty())
        for (range in hit.highlights) {
            assertTrue("range must be inside the snippet", range.first >= 0 && range.last < snippet.length)
            assertEquals(
                "the marked range must be the matched word",
                "deposit",
                snippet.substring(range.first, range.last + 1).lowercase()
            )
        }
    }

    @Test
    fun aStemmedMatchIsHighlightedEvenThoughTheWordsDiffer() {
        val index = DocumentSearch.Index(listOf(document("a", "March", "the invoices were sent")))
        val hit = index.search("invoice").single()
        val snippet = requireNotNull(hit.snippet)
        // `invoice` is not in the sentence; `invoices` is, and that is what a reader
        // needs to see marked.
        assertFalse(snippet.contains(" invoice "))
        assertTrue(hit.highlights.isNotEmpty())
    }

    @Test
    fun titleOnlyMatchesHaveNoHighlights() {
        val index = DocumentSearch.Index(listOf(document("empty", "Annual report")))
        val hit = index.search("annual").single()
        assertNull(hit.snippet)
        assertTrue(hit.highlights.isEmpty())
    }

    // ------------------------------------------------------- query parsing

    @Test
    fun theQueryParserSeparatesWordsPhrasesExclusionsAndYears() {
        val parsed = DocumentSearch.parseQuery("tax \"pan card\" -draft 2024")
        assertEquals(listOf("tax", "2024"), parsed.terms)
        assertEquals(listOf("pan card"), parsed.phrases)
        assertEquals(listOf("draft"), parsed.excluded)
        assertEquals(listOf(2024), parsed.years)
    }

    @Test
    fun stopwordsAreDroppedOnlyWhenSomethingElseRemains() {
        assertEquals(listOf("invoice"), DocumentSearch.parseQuery("the invoice").terms)
        // A query that is only a stopword is still a query.
        assertEquals(listOf("the"), DocumentSearch.parseQuery("the").terms)
    }

    // ------------------------------------- the walk the Rust kernel mirrors

    /** The same vocabulary and queries as `search.rs`'s `matches_the_golden_expansion`. */
    @Test
    fun theVocabularyWalkProducesTheGoldenTriples() {
        val vocabulary = listOf(
            "aadhaar", "aadhar", "bill", "billing", "birth", "card", "date", "dob",
            "invoice", "invoices", "ledger", "pan", "pancard", "receipt", "receipts",
            "return", "tax", "total"
        )
        val queries = listOf("invoce", "invoi", "aadhar", "dob", "totl")
        val triples = DocumentSearch.expandReference(vocabulary, queries, 2)
            .flatMap { listOf(it.queryIndex, it.vocabularyIndex, it.code) }
        assertEquals(
            listOf(
                // invoce → invoice (1 edit), invoices (2 edits)
                2, 0, 1,
                2, 1, 0,
                // dob → dob (exact)
                3, 7, 0,
                // invoce → invoice, then invoi is a prefix of both
                0, 8, 1,
                1, 8, -1,
                0, 9, 2,
                1, 9, -1,
                // totl → total (1 edit)
                4, 17, 1
            ),
            triples
        )
    }

    @Test
    fun theWalkReturnsNothingForAnUnrelatedWord() {
        val triples = DocumentSearch.expandReference(listOf("invoice", "tax"), listOf("passport"), 2)
        assertTrue(triples.isEmpty())
    }

    @Test
    fun boundedEditDistanceIsCappedAndCharacterAware() {
        assertEquals(1, DocumentSearch.boundedEditDistance("aadhar", "aadhaar", 2))
        // Further than the cap: reported as cap + 1, never as a large number.
        assertEquals(3, DocumentSearch.boundedEditDistance("dob", "date", 2))
        assertEquals(0, DocumentSearch.boundedEditDistance("कर", "कर", 2))
        // Two characters, not six bytes.
        assertEquals(2, "कर".length)
    }

    // ---------------------------------------------------------- the packing

    @Test
    fun thePackingFormatIsLengthPrefixedUtf8() {
        val packed = DocumentSearch.packStrings(listOf("ab", "कर"))
        assertEquals(
            // "ab": 2 bytes; "कर": 2 characters, 6 UTF-8 bytes.
            listOf(2, 0, 0, 0, 97, 98, 6, 0, 0, 0, 224, 164, 149, 224, 164, 176),
            packed.map { it.toInt() and 0xFF }
        )
    }

    @Test
    fun theNativeKernelIsAbsentOnTheJvmAndTheReferenceStillAnswers() {
        // No `.so` in a JVM unit test: this is the fallback path, and it must be the
        // one the app ships with when the toolchain was not available.
        assertFalse(NativeSearch.isLoaded)
        assertNull(NativeSearch.expand(DocumentSearch.packStrings(listOf("invoice")), listOf("invoce"), 2))
    }
}
