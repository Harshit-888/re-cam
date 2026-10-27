package com.example.docscanner.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the paper table to the original app's database.
 *
 * The expected values come from `app.db`, which ships inside
 * `docs/original_apk_pipeline_artifacts.zip`:
 *
 * ```sql
 * select c_paper_size_name, i_width_in_point, i_height_in_point
 * from paper_size where b_is_show_in_select_list = 1 order by i_paper_size_id;
 * ```
 *
 * Fifteen rows, all reproduced here by name and point value. The test exists
 * because these numbers are the kind that get "tidied" — A3 has already been
 * through 1191 and 1190 in this repository — and because renaming an entry
 * silently resets the saved setting for anyone who chose it.
 */
class PaperSizeTest {

    /** The original's visible sizes, in its stored order. */
    private val databaseSizes = listOf(
        "A3" to (842 to 1190),
        "A4" to (595 to 842),
        "A5" to (420 to 595),
        "B4" to (709 to 1001),
        "B5" to (499 to 709),
        "LETTER" to (612 to 792),
        "LEGAL" to (612 to 1008),
        "LEDGER" to (792 to 1224),
        "TABLOID" to (1224 to 792),
        "EXECUTIVE" to (522 to 756),
        "ANSI_C" to (1584 to 1224),
        "ANSI_D" to (2448 to 1584),
        "ANSI_E" to (3168 to 2448),
        "FOLIO" to (612 to 936)
    )

    @Test
    fun `every size the original shows exists here`() {
        databaseSizes.forEach { (name, _) ->
            assertNotNull("$name is missing from PaperSize", runCatching {
                PaperSize.valueOf(name)
            }.getOrNull())
        }
    }

    @Test
    fun `dimensions match the database`() {
        databaseSizes.forEach { (name, dimensions) ->
            val size = PaperSize.valueOf(name)
            assertEquals("$name width", dimensions.first, size.widthPt)
            assertEquals("$name height", dimensions.second, size.heightPt)
        }
    }

    /**
     * The one deliberate departure: the original stores 353 x 206 pt for its
     * business card, which is 124.5 x 72.7 mm. That is not the size its own name
     * promises, so this app keeps ISO/IEC 7810 ID-1.
     */
    @Test
    fun `business card stays the ISO ID-1 size, not the original's stored value`() {
        val card = PaperSize.BUSINESS_CARD
        assertEquals(243, card.widthPt)
        assertEquals(153, card.heightPt)
        assertTrue(card.label.contains("85.6 × 54 mm"))
    }

    /**
     * Saved settings store the enum *name*. A rename is a silent reset, so the
     * names that have ever shipped are pinned here.
     */
    @Test
    fun `names that have shipped never disappear`() {
        val shipped = listOf(
            "A4", "LETTER", "LEGAL", "A3", "A5", "EXECUTIVE", "BUSINESS_CARD", "RECEIPT"
        )
        shipped.forEach { name ->
            assertNotNull("$name was renamed or removed", runCatching {
                PaperSize.valueOf(name)
            }.getOrNull())
        }
    }

    @Test
    fun `sixteen presets, all with a readable label`() {
        assertEquals(16, PaperSize.entries.size)
        PaperSize.entries.forEach { size ->
            assertTrue("${size.name} has no width", size.widthPt > 0)
            assertTrue("${size.name} has no height", size.heightPt > 0)
            // `shortLabel` / `dimensionsLabel` in the export sheet split on the
            // parenthesis, and a label without one renders as a blank second line.
            assertTrue("${size.name} label lacks dimensions: ${size.label}", size.label.contains('('))
            assertTrue("${size.name} label is unbalanced", size.label.endsWith(")"))
        }
    }

    @Test
    fun `grouping covers every preset once, in declaration order`() {
        val grouped = PaperSize.byGroup
        assertEquals(PaperSize.entries.toList(), grouped.flatMap { it.second })
        assertEquals(listOf(3, 2, 10, 1), grouped.map { it.second.size })
        assertEquals(
            listOf(PaperGroup.ISO_A, PaperGroup.ISO_B, PaperGroup.AMERICAN, PaperGroup.OTHER),
            grouped.map { it.first }
        )
    }
}
