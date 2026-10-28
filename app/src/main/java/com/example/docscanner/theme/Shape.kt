package com.example.docscanner.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
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

    /**
     * A sheet whose **top corners flare outwards**, for content that scrolls under a
     * fixed header.
     *
     * The usual `topStart`/`topEnd` radius rounds the sheet's corners *inwards* — the
     * sheet gets smaller than its own edge, which is right for a card floating on a
     * page and wrong for a surface that continues the screen. This shape curves the
     * other way: the top edge runs straight across and each end turns *down and out*
     * into the sides, so the sheet looks tucked under the bar above it instead of lying
     * on top of it. That is the seam detail the library's header was missing: with a
     * plain straight divider the shelf and the grid read as two unrelated screens.
     */
    val outwardTop: Shape = OutwardTopShape(radius = 26.dp)
}

/**
 * The shape above: straight top edge, concave (outward) fillets at both ends.
 *
 * Built from two quadratic curves rather than arcs because a curve gives the corner a
 * continuous tangent with the vertical side, which is what makes the flare read as a
 * surface bending rather than as a notch being cut out of it.
 */
private class OutwardTopShape(private val radius: Dp) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { radius.toPx() }.coerceAtMost(size.width / 2f)
        val path = Path().apply {
            // Start on the left edge, one radius down: the fillet has already happened.
            moveTo(0f, r)
            // Left shoulder: control point in the *corner* pulls the curve outward.
            quadraticTo(0f, 0f, r, 0f)
            lineTo(size.width - r, 0f)
            // Right shoulder, mirrored.
            quadraticTo(size.width, 0f, size.width, r)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean =
        other is OutwardTopShape && other.radius == radius

    override fun hashCode(): Int = radius.hashCode()
}
