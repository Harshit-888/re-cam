package com.example.docscanner.util

import com.example.docscanner.model.ScannedPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PageCopySourcesTest {
    @get:Rule val temp = TemporaryFolder()

    private fun resolve(path: String): File? = File(path).takeIf { it.isFile && it.length() > 0 }

    @Test fun `referenced versioned pixels win over legacy file`() {
        val original = temp.newFile("p.jpg").apply { writeText("original") }
        val current = temp.newFile("p_edited_123.png").apply { writeText("current") }
        temp.newFile("p_edited.jpg").writeText("stale")
        val page = ScannedPage(id = "p", documentId = "d", pageIndex = 0,
            imageUri = current.path, originalImageUri = original.path)
        val sources = resolvePageCopySources(page, ::resolve)!!
        assertEquals("current", sources.current.readText())
        assertEquals("original", sources.pristine.readText())
        assertTrue(sources.isEdited)
        current.delete()
        assertNull(resolvePageCopySources(page, ::resolve))
    }

    @Test fun `missing pristine or unknown pristine for legacy edit fails`() {
        val current = temp.newFile("p_edited.jpg").apply { writeText("edited") }
        val page = ScannedPage(id = "p", documentId = "d", pageIndex = 0,
            imageUri = current.path, originalImageUri = File(temp.root, "missing.jpg").path)
        assertNull(resolvePageCopySources(page, ::resolve))
        assertNull(resolvePageCopySources(page.copy(originalImageUri = null), ::resolve))
    }

    @Test fun `unmodified page has a single authoritative file`() {
        val original = temp.newFile("original.jpg").apply { writeText("original") }
        val page = ScannedPage(id = "p", documentId = "d", pageIndex = 0, imageUri = original.path)
        val sources = resolvePageCopySources(page, ::resolve)!!
        assertFalse(sources.isEdited)
        assertEquals(sources.current, sources.pristine)
    }
}
