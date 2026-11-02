package com.example.docscanner.util

import com.example.docscanner.model.MarkupPoint
import com.example.docscanner.model.ScannedPage
import kotlin.math.roundToInt

/** Rotate existing page-space ink by the same quarter-turn as the page content. */
internal fun ScannedPage.withRotationAndMarkup(newRotation: Float): ScannedPage {
    val turns = (((newRotation - rotationDegrees) / 90f).roundToInt() % 4 + 4) % 4
    if (turns == 0) return copy(rotationDegrees = newRotation)
    return copy(rotationDegrees = newRotation, markup = markup.map { stroke ->
        stroke.copy(points = stroke.points.map { point ->
            when (turns) {
                1 -> MarkupPoint(1f - point.y, point.x)
                2 -> MarkupPoint(1f - point.x, 1f - point.y)
                else -> MarkupPoint(point.y, 1f - point.x)
            }
        })
    })
}
