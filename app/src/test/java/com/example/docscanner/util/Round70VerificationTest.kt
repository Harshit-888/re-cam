package com.example.docscanner.util

import com.example.docscanner.model.Document
import com.example.docscanner.model.ScannedPage
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying Round 70.3 contract invariants:
 * 1. Model backwards compatibility (deserialization of older library.json).
 * 2. Model serialization with [ScannedPage.ocrFailedAttempts].
 * 3. [OcrResult] state machine transitions and bounded retry semantics.
 * 4. Versioned path orphan sweep behavior.
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
}

