package com.example.docscanner.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Expressive shape scale.
 *
 * Material 3 Expressive pushes corner radii apart from the 4/8/12/16/28 default and
 * uses the shape itself as a hierarchy signal: the bigger the surface, the rounder
 * it is. That is why a page-sized card gets 26 dp while a chip gets a full pill —
 * reading the silhouette alone tells you what a thing is before you read the label.
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

/**
 * Named shapes for surfaces that need to keep their identity in and out of the
 * theme. Screens must use these instead of inventing `RoundedCornerShape(12.dp)`
 * inline — that drift is what makes an app feel assembled rather than designed.
 */
object DocShapes {
    /** Chips, filters, counters. A full pill reads as "tappable", not "content". */
    val pill = RoundedCornerShape(percent = 50)

    /** A scanned page: nearly square corners, like paper itself. */
    val page = RoundedCornerShape(6.dp)

    /** Standard content card. */
    val card = RoundedCornerShape(26.dp)

    /** Nested card — one step smaller so the nesting is legible. */
    val cardSmall = RoundedCornerShape(20.dp)

    /** Bottom sheet / control surface. */
    val sheet = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)

    /** A dialog card: round on every corner, unlike a sheet that meets an edge. */
    val dialog = RoundedCornerShape(30.dp)

    /** Floating action button: a squircle, not a circle, in Expressive. */
    val fab = RoundedCornerShape(20.dp)

    /** Text fields and search bars. */
    val field = RoundedCornerShape(18.dp)

    /** Small square buttons (icon buttons on a tonal background). */
    val iconButton = RoundedCornerShape(14.dp)

    /** Thumbnail of a page inside a strip. */
    val thumbnail = RoundedCornerShape(12.dp)

    /**
     * The "pressed" step of a card: corners tighten under the finger. Subtle, but
     * it is the difference between a picture of a button and a button.
     */
    fun pressed(pressed: Boolean) = RoundedCornerShape(if (pressed) 20.dp else 26.dp)
}
