package com.example.docscanner.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.docscanner.theme.pressScale

/**
 * A pressable area that is at least [MIN_TAP_DP] even when the thing you can see is
 * smaller.
 *
 * ## Why this exists
 *
 * The minimum comfortable touch target is **48 dp** (Android's own guidance, and the
 * lowest value that survives being used one-handed on a moving bus). A colour swatch,
 * a small icon button or a dot is often drawn at 24–32 dp, and if the *clickable* is
 * the same size as the drawing then that is the size of the target — the visual
 * dictates the hit area, and the user misses.
 *
 * Material's `IconButton` and `Chip` already reserve 48 dp around their visuals, so
 * they need nothing. What needs this are the hand-built cases: a swatch you tap to
 * pick an ink colour, a small glyph with its own `clickable`.
 *
 * ```kotlin
 * TapTarget(onClick = { colorArgb = ink }, label = "Ink colour") {
 *     Box(Modifier.size(30.dp).clip(CircleShape).background(Color(ink)))
 * }
 * ```
 *
 * The wrapper takes the touch area; [content] draws at whatever size it likes and is
 * centred inside it. Press feedback comes from the same [pressScale] every other
 * tappable in the app uses, so a swatch feels like the rest of the interface.
 */
@Composable
fun TapTarget(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    selected: Boolean? = null,
    size: Dp = MIN_TAP_DP,
    shape: Shape = CircleShape,
    content: @Composable BoxScope.() -> Unit
) {
    val interactions = remember { MutableInteractionSource() }
    val action = Modifier
        .size(size)
        .pressScale(interactions, pressedScale = 0.9f)
        .clip(shape)
        .then(
            if (label != null) {
                Modifier.semantics { contentDescription = label }
            } else {
                Modifier
            }
        )

    Box(
        modifier = modifier.then(
            if (selected == null) {
                // A plain action: published as a button so TalkBack says "double tap to
                // activate" rather than reading an unlabelled box.
                action.clickable(
                    interactionSource = interactions,
                    indication = ripple(),
                    role = Role.Button,
                    onClick = onClick
                )
            } else {
                // A choice among several: `selectable` publishes *which* one is chosen,
                // which colour alone never does.
                action.selectable(
                    selected = selected,
                    interactionSource = interactions,
                    indication = ripple(),
                    role = Role.RadioButton,
                    onClick = onClick
                )
            }
        ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * 48 dp. The floor for anything a finger has to find, not a design preference — below
 * it, misses are measurable even for people with small hands.
 */
val MIN_TAP_DP: Dp = 48.dp
