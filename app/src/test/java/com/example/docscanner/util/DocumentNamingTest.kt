package com.example.docscanner.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

/**
 * Regression tests for the crash that took the app down on the first *Done*
 * press: `SimpleDateFormat("Scan yyyy-MM-dd HH:mm")` throws
 * `IllegalArgumentException: Illegal pattern character 'c'` because "Scan" was
 * not quoted.
 */
class DocumentNamingTest {

    @Test
    fun defaultScanTitle_doesNotThrow() {
        // Must not throw. If the pattern ever loses its quotes this fails.
        DocumentNaming.defaultScanTitle()
    }

    @Test
    fun defaultScanTitle_quotesLiteralText() {
        val calendar = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.OCTOBER, 25, 14, 5, 9)
            set(Calendar.MILLISECOND, 0)
        }
        val title = DocumentNaming.defaultScanTitle(
            now = calendar.timeInMillis,
            locale = Locale.US,
            timeZone = TimeZone.getTimeZone("UTC")
        )
        assertEquals("Scan 2026-10-25 14-05-09", title)
    }

    @Test
    fun defaultScanTitle_isFilenameSafe() {
        val title = DocumentNaming.defaultScanTitle()
        assertFalse("title must not contain ':'", title.contains(':'))
    }

    @Test
    fun bareLetterInPattern_isTheBugWeAreGuardingAgainst() {
        // Demonstrates the original defect so the intent of this test file is
        // obvious to the next agent. 'S' and 'a' are legal letters, 'c' is not.
        var threw = false
        try {
            SimpleDateFormat("Scan yyyy-MM-dd HH:mm", Locale.US)
        } catch (_: IllegalArgumentException) {
            threw = true
        }
        assertTrue("unquoted 'Scan' must be rejected by SimpleDateFormat", threw)
    }
}
