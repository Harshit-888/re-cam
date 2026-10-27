package com.example.docscanner.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pins the units the export sheet shows.
 *
 * The rule is the user's own wording — "in KB if less than 1 MB" — and it is the
 * kind of thing that breaks silently: a formatter that switches units at
 * 1 024 000 bytes, or one that prints "1024 KB", looks fine in isolation and wrong
 * on a phone whose file manager says "1.0 MB". These are the boundary cases only;
 * [PdfSizeEstimate.estimate] itself needs bitmaps and is exercised on device.
 */
class PdfSizeEstimateTest {

    @Test
    fun `stays in kilobytes below one megabyte`() {
        assertEquals("390 KB", PdfSizeEstimate.format(390_000))
        assertEquals("742 KB", PdfSizeEstimate.format(742_000))
        assertEquals("999 KB", PdfSizeEstimate.format(999_400))
    }

    @Test
    fun `switches to megabytes at one million bytes`() {
        assertEquals("1.0 MB", PdfSizeEstimate.format(1_000_000))
        assertEquals("1.4 MB", PdfSizeEstimate.format(1_440_000))
        assertEquals("24.6 MB", PdfSizeEstimate.format(24_600_000))
    }

    @Test
    fun `never prints a zero kilobyte file`() {
        // A small page still has a PDF object and an image stream; "0 KB" would be
        // a promise the writer cannot keep.
        assertEquals("1 KB", PdfSizeEstimate.format(0))
        assertEquals("1 KB", PdfSizeEstimate.format(400))
    }

    @Test
    fun `rounds to the nearest kilobyte`() {
        assertEquals("2 KB", PdfSizeEstimate.format(1_600))
        assertEquals("1 KB", PdfSizeEstimate.format(1_400))
    }

    @Test
    fun `drops the decimal past a hundred megabytes`() {
        // Three-digit megabytes with a decimal is a number nobody reads.
        assertEquals("120 MB", PdfSizeEstimate.format(120_400_000))
    }
}
