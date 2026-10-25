package com.example.docscanner.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * One place for every duration and curve in the app.
 *
 * Expressive motion is *springy, not linear*: things arrive with a little
 * overshoot and settle, instead of sliding at a constant speed. Two families are
 * enough — spatial springs for anything that moves or resizes, and a short tween
 * for pure fades, where a spring would read as a wobble.
 */
object Motion {
    /** For anything that answers a touch. Fast enough to feel instant. */
    val press: AnimationSpec<Float> = spring(
        dampingRatio = 0.55f,
        stiffness = Spring.StiffnessHigh
    )

    /** Selection, expansion, shape and colour changes. */
    val spatial: AnimationSpec<Float> = spring(
        dampingRatio = 0.78f,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Big arrivals: sheets, stage changes, page transitions. */
    val emphasized: AnimationSpec<Float> = spring(
        dampingRatio = 0.85f,
        stiffness = 260f
    )

    /** A deliberately visible overshoot, for one-off delight moments. */
    val bouncy: AnimationSpec<Float> = spring(
        dampingRatio = 0.45f,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Opacity only. Springs on alpha look like flicker. */
    val fade: AnimationSpec<Float> = tween(durationMillis = 180, easing = LinearEasing)

    /** Stagger step between sibling items entering a screen. */
    const val staggerMs = 45

    /** Longest stagger, so a long list never feels slow to arrive. */
    const val maxStaggerSteps = 6
}

/**
 * Scales [this] element down while it is held.
 *
 * Every tappable surface in the app uses this, which is what gives the whole thing
 * one tactile personality. The interaction source must be the same one handed to
 * the clickable, or the press never registers.
 */
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.96f
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = Motion.press,
        label = "pressScale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Fades and lifts an element into place, delayed by its position in a list.
 *
 * Staggering is what makes a screen feel *composed* rather than dumped: the eye
 * follows the items in reading order instead of being hit by everything at once.
 * Only the first few items are staggered — past that the list would arrive slower
 * than the user can scroll.
 */
@Composable
fun entrance(index: Int, distance: Dp = 14.dp): Modifier {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(
            durationMillis = 280,
            delayMillis = index.coerceAtMost(Motion.maxStaggerSteps) * Motion.staggerMs
        ),
        label = "entrance"
    )
    return Modifier
        .alpha(progress)
        .graphicsLayer { translationY = (1f - progress) * distance.toPx() }
}
