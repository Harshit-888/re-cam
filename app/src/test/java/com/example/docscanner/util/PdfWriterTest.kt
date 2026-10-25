package com.example.docscanner.util

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Byte-level tests for the app's own PDF writer.
 *
 * A PDF is a file format with offsets in it: if the cross-reference table points
 * one byte off, every reader refuses the document, and no compiler can catch
 * that. These tests write a real file with [PdfWriter] and then read it back as
 * bytes — header, object offsets, page count, trailer, encryption — which is the
 * only kind of test that can fail for the reasons a PDF actually fails.
 *
 * They also need no device: no `Bitmap` is involved, because the writer takes
 * already-encoded image bytes.
 */
class PdfWriterTest {

    private val marker = byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())

    private fun plan(index: Int, total: Int, withImage: Boolean = true) = PdfPagePlan(
        widthPt = 595f,
        heightPt = 842f,
        image = if (withImage) PdfPageImage(marker, width = 4, height = 1, jpeg = true) else null,
        left = 25f,
        top = 25f,
        drawWidth = 545f,
        drawHeight = 400f,
        clip = null,
        footerBaseline = 836f,
        footer = "$index / $total",
        notice = if (withImage) null else "Page $index image unavailable"
    )

    private fun write(password: String? = null): ByteArray {
        val file = File.createTempFile("pdf-writer-test", ".pdf")
        try {
            val writer = PdfWriter("Test (draft)", password)
            writer.writeTo(file) { addPage ->
                addPage(plan(1, 2))
                addPage(plan(2, 2, withImage = false))
            }
            return file.readBytes()
        } finally {
            file.delete()
        }
    }

    private fun asText(bytes: ByteArray): String = String(bytes, Charsets.ISO_8859_1)

    @Test
    fun `starts with a pdf header and ends with the end marker`() {
        val text = asText(write())
        assertTrue(text.startsWith("%PDF-1.4"))
        assertTrue(text.trimEnd().endsWith("%%EOF"))
    }

    @Test
    fun `declares the catalog, the pages and a count of two`() {
        val text = asText(write())
        assertTrue(text.contains("/Type /Catalog /Pages 2 0 R"))
        assertTrue(text.contains("/Type /Pages"))
        assertTrue(text.contains("/Count 2"))
        assertTrue(text.contains("/MediaBox [0 0 595.000 842.000]"))
    }

    @Test
    fun `every cross reference entry points at its own object`() {
        val text = asText(write())
        val startxref = text.lastIndexOf("startxref")
        assertTrue("no startxref", startxref > 0)
        val tableOffset = text.substring(startxref + "startxref".length).trim()
            .substringBefore('\n').trim().toInt()
        assertEquals("xref", text.substring(tableOffset, tableOffset + 4))

        var cursor = tableOffset + "xref\n".length
        val header = text.substring(cursor, text.indexOf('\n', cursor))
        val count = header.substringAfter(' ').toInt()
        assertTrue(count >= 6)
        cursor = text.indexOf('\n', cursor) + 1

        for (number in 0 until count) {
            val entry = text.substring(cursor, cursor + 20)
            cursor += 20
            if (number == 0) {
                assertTrue("entry 0 must be free", entry[17] == 'f')
                continue
            }
            assertTrue("entry $number must be in use", entry[17] == 'n')
            val offset = entry.substring(0, 10).toInt()
            assertTrue(
                "object $number should start at $offset, found '${text.substring(offset, offset + 10)}'",
                text.startsWith("$number 0 obj", offset)
            )
        }
    }

    @Test
    fun `an unencrypted document has no encrypt dictionary and keeps its streams verbatim`() {
        val bytes = write()
        val text = asText(bytes)
        assertFalse(text.contains("/Encrypt"))
        assertTrue(text.contains("/Filter /DCTDecode"))
        assertTrue(
            "the image bytes should be present unencrypted",
            String(bytes, Charsets.ISO_8859_1).contains(String(marker, Charsets.ISO_8859_1))
        )
    }

    @Test
    fun `a protected document names the security handler and hides its streams`() {
        val text = asText(write(password = "s3cret"))
        assertTrue(text.contains("/Encrypt 5 0 R"))
        assertTrue(text.contains("/Filter /Standard /V 2 /R 3 /Length 128"))
        assertTrue(text.contains("/P -1"))
        assertTrue(text.contains("/ID [<"))
        assertFalse(
            "the image bytes must not appear in a protected document",
            text.contains(String(marker, Charsets.ISO_8859_1))
        )
    }

    @Test
    fun `an unprotected document is left byte-clean of the security vocabulary`() {
        val text = asText(write())
        assertFalse(text.contains("/O <"))
        assertFalse(text.contains("/U <"))
    }

    @Test
    fun `literal strings are escaped so the file stays parseable`() {
        assertEquals("a\\(b\\)c", PdfWriter.escapeText("a(b)c"))
        assertEquals("back\\\\slash", PdfWriter.escapeText("back\\slash"))
        assertEquals("? for non-ascii", PdfWriter.sanitiseText("\u2014 for non-ascii"))
    }

    @Test
    fun `page number centring uses the real helvetica advance widths`() {
        // 556 (digit) + 278 (space) + 278 (slash) + 278 (space) + 556 (digit)
        // = 1946/1000 em, at 9 pt.
        assertEquals(17.514f, PdfWriter.helveticaWidth("1 / 3", 9f), 0.001f)
    }
}
