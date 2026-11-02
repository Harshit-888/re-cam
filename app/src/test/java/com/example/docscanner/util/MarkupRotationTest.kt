package com.example.docscanner.util

import com.example.docscanner.model.MarkupPoint
import com.example.docscanner.model.MarkupStroke
import com.example.docscanner.model.MarkupTool
import com.example.docscanner.model.ScannedPage
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkupRotationTest {
    @Test fun `ink follows quarter turns and returns to start`() {
        val page = ScannedPage(id = "p", documentId = "d", pageIndex = 0, imageUri = "p.jpg",
            markup = listOf(MarkupStroke(tool = MarkupTool.PEN, points = listOf(MarkupPoint(0f, 0f)))))
        val turned = page.withRotationAndMarkup(90f)
        assertEquals(MarkupPoint(1f, 0f), turned.markup.single().points.single())
        assertEquals(MarkupPoint(1f, 1f), turned.withRotationAndMarkup(180f).markup.single().points.single())
        assertEquals(page.markup, turned.withRotationAndMarkup(0f).markup)
    }
}
