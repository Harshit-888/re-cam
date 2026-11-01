package com.example.docscanner.data

import com.example.docscanner.model.FilterType
import com.example.docscanner.model.PageToken
import com.example.docscanner.model.ScannedPage
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AtomicStateUpdateTest {
    @Test
    fun `result comes from successful retry not discarded attempt`() {
        val state = MutableStateFlow(0)
        var attempts = 0
        val result = atomicStateUpdate(state) { current ->
            attempts++
            if (attempts == 1) state.value = 1 // force the first compareAndSet to fail
            if (current == 0) 2 to "published" else current to "rejected"
        }
        assertEquals(2, attempts)
        assertEquals("rejected", result)
        assertEquals(1, state.value)
    }

    @Test
    fun `token rejects changes to all omitted metadata`() {
        val page = ScannedPage(id = "p", documentId = "d", pageIndex = 0, imageUri = "/p.jpg")
        val token = PageToken.of(page)
        assertTrue(token.matches(page))
        listOf(
            page.copy(documentId = "other"),
            page.copy(pageIndex = 1),
            page.copy(originalImageUri = "/original.jpg"),
            page.copy(filterType = FilterType.LIGHTEN),
            page.copy(ocrText = "recognized"),
            page.copy(ocrAttempted = true),
            page.copy(ocrFailedAttempts = 1),
            page.copy(watermarkText = "stamp")
        ).forEach { assertFalse("changed page must not match: $it", token.matches(it)) }
    }
}
