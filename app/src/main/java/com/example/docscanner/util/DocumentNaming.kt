package com.example.docscanner.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Titles for newly scanned documents.
 *
 * ⚠️ `SimpleDateFormat` patterns treat **bare letters as pattern characters**.
 * Anything that should appear literally — "Scan", "Invoice", "Jan" — must be
 * wrapped in single quotes, and only legal pattern letters may appear outside
 * them (`G y M L w W D d F E u a H k K h m s S z Z X`).
 *
 * A previous revision passed `"Scan yyyy-MM-dd HH:mm"` straight to
 * `SimpleDateFormat`, which throws `IllegalArgumentException: Illegal pattern
 * character 'c'` — crashing the app the instant the user pressed *Done* on their
 * first scan. [DocumentNamingTest] now guards against a regression.
 */
object DocumentNaming {

    /** Default pattern for a scan title. Literal words are quoted. */
    const val DEFAULT_SCAN_PATTERN = "'Scan' yyyy-MM-dd HH-mm-ss"

    /** Fallback used when no pages were captured and a title is still needed. */
    const val FALLBACK_TITLE = "Scanned document"

    /**
     * Builds the default document title for [now].
     *
     * Uses `HH-mm-ss` rather than `HH:mm:ss` so the title is safe to reuse as a
     * filename in the PDF exporter.
     */
    fun defaultScanTitle(
        now: Long = System.currentTimeMillis(),
        locale: Locale = Locale.getDefault(),
        timeZone: java.util.TimeZone = java.util.TimeZone.getDefault()
    ): String {
        val formatted = try {
            SimpleDateFormat(DEFAULT_SCAN_PATTERN, locale).apply {
                this.timeZone = timeZone
            }.format(Date(now))
        } catch (_: IllegalArgumentException) {
            // Defensive: never let title generation take the app down again.
            ""
        }
        return formatted.ifBlank { FALLBACK_TITLE }
    }
}
