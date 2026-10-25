package com.example.docscanner.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the exact bytes of the security handler.
 *
 * The values below were produced by an independent implementation of the same
 * algorithms (PDF 32000-1 §7.6.3, algorithms 2, 3 and 5) and then checked by
 * opening the resulting file with **three** readers: PDFium (the engine inside
 * Android's own `PdfRenderer`), MuPDF and pypdf. The right password opened it, a
 * wrong one was rejected, and the decrypted streams matched the source bytes.
 *
 * That is the whole argument for these constants: they are not what this code
 * computes, they are what two readers accepted. If a refactor changes them, the
 * export stops opening, and this test says so before anyone ships it.
 */
class PdfSecurityTest {

    /** `md5("docscanner-id-0")` — the file identifier the vectors were derived with. */
    private val documentId = "0a1b51cb94e6f966a10d8de12f52059c".hexToBytes()

    @Test
    fun `rc4 matches the reference vector`() {
        val cipher = Rc4.apply("Key".toByteArray(Charsets.ISO_8859_1), "Plaintext".toByteArray(Charsets.ISO_8859_1))
        assertEquals("BBF316E8D940AF0AD3", cipher.toHex().uppercase())
    }

    @Test
    fun `owner entry matches the dictionary two readers accepted`() {
        val security = PdfSecurity("s3cret", documentId)
        assertEquals(
            "ccfafd28178159f9caeebf09351010004be32ec4b94fd14f3fb0723ecb85360d",
            security.ownerEntry.toHex()
        )
    }

    @Test
    fun `user entry is the algorithm 5 value plus its sixteen padding bytes`() {
        val security = PdfSecurity("s3cret", documentId)
        assertEquals(
            "af07ef3a01681e6e1a50a3bc7b2955a3" + "0".repeat(32),
            security.userEntry.toHex()
        )
        assertEquals(32, security.userEntry.size)
    }

    @Test
    fun `file key matches the derivation`() {
        // Not exposed; the per-object key is its only observable, and this is that
        // derivation for object 7 with generation 0.
        val security = PdfSecurity("s3cret", documentId)
        assertEquals("69ad6fae0a8f87e06e225561e3ad0111", security.objectKey(7).toHex())
    }

    @Test
    fun `every object gets its own key`() {
        val security = PdfSecurity("s3cret", documentId)
        val keys = (1..6).map { security.objectKey(it).toHex() }
        assertEquals(keys.size, keys.toSet().size)
        // 128-bit keys, as `/Length 128` promises.
        assertTrue(keys.all { it.length == 32 })
        assertNotEquals(security.objectKey(7, 0).toHex(), security.objectKey(7, 1).toHex())
    }

    @Test
    fun `encryption round trips under the object key`() {
        val security = PdfSecurity("s3cret", documentId)
        val plain = "a page's stream (with punctuation) and \u00e9".toByteArray(Charsets.ISO_8859_1)
        val encrypted = security.encrypt(9, plain)
        assertEquals(plain.size, encrypted.size)
        assertNotEquals(plain.toHex(), encrypted.toHex())
        assertEquals(plain.toHex(), Rc4.apply(security.objectKey(9), encrypted).toHex())
    }

    @Test
    fun `an empty passphrase is still padded, never zero length`() {
        assertEquals(32, PdfSecurity.padPassword("").size)
        assertEquals(PdfSecurity.PADDING.toHex(), PdfSecurity.padPassword("").toHex())
    }

    @Test
    fun `a long passphrase is truncated to thirty two bytes`() {
        val long = "x".repeat(64)
        val padded = PdfSecurity.padPassword(long)
        assertEquals(32, padded.size)
        assertEquals("x".repeat(32), String(padded, Charsets.ISO_8859_1))
    }

    @Test
    fun `different passphrases produce different entries`() {
        val a = PdfSecurity("alpha", documentId)
        val b = PdfSecurity("bravo", documentId)
        assertNotEquals(a.ownerEntry.toHex(), b.ownerEntry.toHex())
        assertNotEquals(a.userEntry.toHex(), b.userEntry.toHex())
    }
}
