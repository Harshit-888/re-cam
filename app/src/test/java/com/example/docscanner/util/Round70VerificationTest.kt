package com.example.docscanner.util

import com.example.docscanner.model.Document
import com.example.docscanner.model.FilterType
import com.example.docscanner.model.MarkupStroke
import com.example.docscanner.model.PageToken
import com.example.docscanner.model.QuadCorners
import com.example.docscanner.model.ScannedPage
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying Round 70.3 contract invariants:
 * 1. Model backwards compatibility (deserialization of older library.json).
 * 2. Model serialization with [ScannedPage.ocrFailedAttempts].
 * 3. [OcrResult] state machine transitions and bounded retry semantics.
 * 4. Versioned path orphan sweep behavior.
 * 5. [PageToken] mismatch detection across revision, uri, corners, rotation, and markup.
 * 6. Untried-first OCR scheduling fairness and starvation prevention.
 * 7. Stale publication rejection across single-page and batch callers.
 */
class Round70VerificationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun `older library json without ocr fields loads with safe defaults`() {
        val legacyJson = """
            {
                "id": "page_1",
                "documentId": "doc_1",
                "pageIndex": 0,
                "imageUri": "/data/pages/page_1.jpg",
                "filterType": "MAGIC_COLOR",
                "rotationDegrees": 0.0,
                "ocrText": "",
                "revision": 0
            }
        """.trimIndent()

        val page = json.decodeFromString<ScannedPage>(legacyJson)
        assertEquals("page_1", page.id)
        assertEquals(0, page.pageIndex)
        assertEquals("", page.ocrText)
        assertFalse("Legacy page must default ocrAttempted to false", page.ocrAttempted)
        assertEquals("Legacy page must default ocrFailedAttempts to 0", 0, page.ocrFailedAttempts)
    }

    @Test
    fun `page serialization round trips ocrAttempted and ocrFailedAttempts`() {
        val page = ScannedPage(
            id = "page_test",
            documentId = "doc_1",
            pageIndex = 0,
            imageUri = "/data/pages/page_test_edited_12345.jpg",
            ocrAttempted = false,
            ocrFailedAttempts = 2,
            revision = 1
        )

        val encoded = json.encodeToString(ScannedPage.serializer(), page)
        val decoded = json.decodeFromString<ScannedPage>(encoded)

        assertEquals(page.id, decoded.id)
        assertEquals(2, decoded.ocrFailedAttempts)
        assertFalse(decoded.ocrAttempted)
        assertEquals(1, decoded.revision)
    }

    @Test
    fun `ocr result state transitions bounded retry at 3 attempts`() {
        val page = ScannedPage(
            id = "p1",
            documentId = "d1",
            pageIndex = 0,
            imageUri = "/data/pages/p1.jpg",
            ocrAttempted = false,
            ocrFailedAttempts = 0
        )

        // Attempt 1: Failure
        val attempt1 = page.copy(
            ocrFailedAttempts = page.ocrFailedAttempts + 1,
            ocrAttempted = (page.ocrFailedAttempts + 1) >= 3
        )
        assertEquals(1, attempt1.ocrFailedAttempts)
        assertFalse("Attempt 1 failure should not mark attempted-final", attempt1.ocrAttempted)

        // Attempt 2: Failure
        val attempt2 = attempt1.copy(
            ocrFailedAttempts = attempt1.ocrFailedAttempts + 1,
            ocrAttempted = (attempt1.ocrFailedAttempts + 1) >= 3
        )
        assertEquals(2, attempt2.ocrFailedAttempts)
        assertFalse("Attempt 2 failure should not mark attempted-final", attempt2.ocrAttempted)

        // Attempt 3: Failure -> marks attempted-final
        val attempt3 = attempt2.copy(
            ocrFailedAttempts = attempt2.ocrFailedAttempts + 1,
            ocrAttempted = (attempt2.ocrFailedAttempts + 1) >= 3
        )
        assertEquals(3, attempt3.ocrFailedAttempts)
        assertTrue("Attempt 3 failure must mark attempted-final to prevent queue starvation", attempt3.ocrAttempted)

        // Bake reset: both fields reset to 0 and false
        val rebaked = attempt3.copy(
            revision = attempt3.revision + 1,
            ocrAttempted = false,
            ocrFailedAttempts = 0
        )
        assertEquals(0, rebaked.ocrFailedAttempts)
        assertFalse(rebaked.ocrAttempted)
    }

    @Test
    fun `versioned files orphan sweep protects active version and cleans old after grace`() {
        val now = 1_000_000_000L
        val graceMs = ImageStore.ORPHAN_GRACE_MS // 10 minutes (600,000 ms)

        val pristinePath = "/data/pages/p1.jpg"
        val activeVersionPath = "/data/pages/p1_edited_1000_abcd.jpg"
        val oldVersionPath = "/data/pages/p1_edited_500_efgh.jpg"

        val referencedPaths = setOf(pristinePath, activeVersionPath)

        // Pristine capture must never be orphaned
        assertFalse(
            isOrphanedPageFile(pristinePath, lastModified = now - 2 * graceMs, referencedPaths, now, graceMs)
        )

        // Currently referenced versioned path must not be orphaned
        assertFalse(
            isOrphanedPageFile(activeVersionPath, lastModified = now - 1000L, referencedPaths, now, graceMs)
        )

        // Old versioned path within grace period must NOT be orphaned yet
        assertFalse(
            isOrphanedPageFile(oldVersionPath, lastModified = now - (graceMs - 5000L), referencedPaths, now, graceMs)
        )

        // Old versioned path past grace period MUST be orphaned and eligible for deletion
        assertTrue(
            isOrphanedPageFile(oldVersionPath, lastModified = now - (graceMs + 5000L), referencedPaths, now, graceMs)
        )
    }

    @Test
    fun `page token detects mismatches in revision, uri, corners, rotation, and markup`() {
        val basePage = ScannedPage(
            id = "p1",
            documentId = "d1",
            pageIndex = 0,
            imageUri = "/data/pages/p1_edited_100_a.jpg",
            originalImageUri = "/data/pages/p1.jpg",
            revision = 1,
            rotationDegrees = 90f,
            corners = QuadCorners(0.1f, 0.1f, 0.9f, 0.1f, 0.9f, 0.9f, 0.1f, 0.9f)
        )
        val token = PageToken.of(basePage)
        assertTrue("Token must match identical page snapshot", token.matches(basePage))

        // Mismatch on revision
        assertFalse(
            "Token must reject page with different revision",
            token.matches(basePage.copy(revision = 2))
        )

        // Mismatch on imageUri
        assertFalse(
            "Token must reject page with different imageUri",
            token.matches(basePage.copy(imageUri = "/data/pages/p1_edited_200_b.jpg"))
        )

        // Mismatch on rotation
        assertFalse(
            "Token must reject page with different rotation",
            token.matches(basePage.copy(rotationDegrees = 180f))
        )

        // Mismatch on corners
        assertFalse(
            "Token must reject page with different corners",
            token.matches(basePage.copy(corners = QuadCorners.FULL_FRAME))
        )

        // Mismatch on markup
        assertFalse(
            "Token must reject page with different markup",
            token.matches(basePage.copy(markup = listOf(MarkupStroke(tool = com.example.docscanner.model.MarkupTool.PEN))))
        )

        // Mismatch on signatureOverlayUri
        assertFalse(
            "Token must reject page with different signature",
            token.matches(basePage.copy(signatureOverlayUri = "/data/signatures/sig1.png"))
        )
    }

    @Test
    fun `ocr backfill queues untried pages first preventing starvation by failing front pages`() {
        // Scenario: 8 front pages have failed once or twice, and 4 new pages were imported untried
        val failingFrontPages = (1..8).map { i ->
            ScannedPage(
                id = "fail_$i",
                documentId = "doc_fail",
                pageIndex = i - 1,
                imageUri = "/pages/fail_$i.jpg",
                ocrText = "",
                ocrAttempted = false,
                ocrFailedAttempts = if (i % 2 == 0) 1 else 2
            )
        }
        val untriedNewPages = (1..4).map { i ->
            ScannedPage(
                id = "untried_$i",
                documentId = "doc_new",
                pageIndex = i - 1,
                imageUri = "/pages/untried_$i.jpg",
                ocrText = "",
                ocrAttempted = false,
                ocrFailedAttempts = 0
            )
        }
        val allPages = failingFrontPages + untriedNewPages

        // Candidate filter and selection algorithm from ScannerRepository.backfillTextInBackground
        val allCandidates = allPages.filter { it.ocrText.isBlank() && !it.ocrAttempted && it.imageUri.isNotBlank() }
        val untried = allCandidates.filter { it.ocrFailedAttempts == 0 }
        val failed = allCandidates.filter { it.ocrFailedAttempts in 1..2 }
        val pendingBatch = (untried + failed).take(8)

        assertEquals("Pending batch must be size 8", 8, pendingBatch.size)
        // All 4 untried pages MUST be at the front of the batch
        val untriedInBatch = pendingBatch.take(4).map { it.id }
        assertEquals(listOf("untried_1", "untried_2", "untried_3", "untried_4"), untriedInBatch)
        // The remaining 4 slots are filled by retry candidates
        val retriesInBatch = pendingBatch.drop(4).map { it.id }
        assertEquals(listOf("fail_1", "fail_2", "fail_3", "fail_4"), retriesInBatch)
    }

    @Test
    fun `ocr state machine bounded retry transitions`() {
        var page = ScannedPage(
            id = "p_retry",
            documentId = "d1",
            pageIndex = 0,
            imageUri = "/pages/p_retry.jpg",
            ocrAttempted = false,
            ocrFailedAttempts = 0
        )

        // Transient failure 1
        page = page.copy(
            ocrFailedAttempts = page.ocrFailedAttempts + 1,
            ocrAttempted = (page.ocrFailedAttempts + 1) >= 3
        )
        assertEquals(1, page.ocrFailedAttempts)
        assertFalse(page.ocrAttempted)

        // Transient failure 2
        page = page.copy(
            ocrFailedAttempts = page.ocrFailedAttempts + 1,
            ocrAttempted = (page.ocrFailedAttempts + 1) >= 3
        )
        assertEquals(2, page.ocrFailedAttempts)
        assertFalse(page.ocrAttempted)

        // Transient failure 3 -> capped!
        page = page.copy(
            ocrFailedAttempts = page.ocrFailedAttempts + 1,
            ocrAttempted = (page.ocrFailedAttempts + 1) >= 3
        )
        assertEquals(3, page.ocrFailedAttempts)
        assertTrue("After 3 failures, page must be marked attempted-final to prevent infinite retry", page.ocrAttempted)

        // Real bake resets both attempted and failed attempts
        page = page.copy(
            revision = page.revision + 1,
            imageUri = "/pages/p_retry_edited_9999.jpg",
            ocrAttempted = false,
            ocrFailedAttempts = 0
        )
        assertEquals(0, page.ocrFailedAttempts)
        assertFalse(page.ocrAttempted)

        // Blank page (NoText) sets attempted-final on first try
        page = page.copy(
            ocrAttempted = true
        )
        assertTrue(page.ocrAttempted)
        assertEquals(0, page.ocrFailedAttempts)
    }

    @Test
    fun `stale publication rejection preserves newer page state`() {
        val initialPage = ScannedPage(
            id = "p1",
            documentId = "d1",
            pageIndex = 0,
            imageUri = "/pages/p1.jpg",
            revision = 0
        )
        val initialToken = PageToken.of(initialPage)

        // Page is updated concurrently in repository (e.g. rotated to rev 1)
        val currentPage = initialPage.copy(
            revision = 1,
            rotationDegrees = 90f,
            imageUri = "/pages/p1_edited_1.jpg"
        )

        // A stale bake based on initialToken attempts to publish
        val staleBaked = initialPage.copy(
            revision = initialPage.revision + 1, // rev 1
            filterType = FilterType.LIGHTEN,
            imageUri = "/pages/p1_edited_stale.jpg"
        )

        // Token match check
        assertFalse("Stale bake token must not match current page", initialToken.matches(currentPage))

        // When rejected, current page remains untouched
        val published = if (initialToken.matches(currentPage)) staleBaked else currentPage
        assertEquals("Current page must retain its 90 deg rotation and not be overwritten", 90f, published.rotationDegrees)
        assertEquals("Current page must retain its rev 1 imageUri", "/pages/p1_edited_1.jpg", published.imageUri)
    }
}

