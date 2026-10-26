package com.example.docscanner.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
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
    /**
     * For anything that answers a touch.
     *
     * Fast, and only *just* under-damped. At 0.55 the scale visibly wobbles on
     * release, which is a delight on a one-off button and a tic on something
     * pressed all day; `bouncy` below exists for the one-off cases.
     */
    val press: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.8f,
        stiffness = Spring.StiffnessHigh
    )

    /** Selection, expansion, shape and colour changes. */
    val spatial: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.78f,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Big arrivals: sheets, stage changes, page transitions. */
    val emphasized: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.85f,
        stiffness = 260f
    )

    /** A deliberately visible overshoot, for one-off delight moments. */
    val bouncy: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.45f,
        stiffness = Spring.StiffnessMediumLow
    )

    /**
     * The app's one "it arrived" curve: `cubic-bezier(0.23, 1, 0.32, 1)`.
     *
     * Compose's `FastOutSlowInEasing` is a Material default, deliberately gentle —
     * it is weak in the same way CSS's `ease-out` is. A strong curve gets the
     * element most of the way in the first third of the duration and spends the
     * rest settling, which is what reads as "responsive" rather than "animated".
     */
    val EaseOut: Easing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

    /**
     * The mirror of [EaseOut]: slow to start, quick to leave.
     *
     * Only for something that is *going away* while something else arrives. It is
     * the one place `ease-in` is right — an element that accelerates off screen
     * reads as "done with", where a decelerating one reads as still competing for
     * attention with the screen arriving behind it.
     */
    val EaseIn: Easing = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)

    /**
     * Opacity only. Springs on alpha look like flicker.
     *
     * `ease-out`, not linear: a linear fade spends the same time on the first
     * millisecond as the last, so it reads as *slower* than it is. Ease-out front-loads
     * the change, which is what makes 180 ms feel instant next to a 180 ms linear.
     * (Linear is correct for constant motion — a progress bar — and wrong for a fade.)
     */
    val fade: FiniteAnimationSpec<Float> = tween(durationMillis = 180, easing = EaseOut)

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
fun Modifier.entrance(index: Int, distance: Dp = 14.dp): Modifier = composed {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(
            durationMillis = 280,
            delayMillis = index.coerceAtMost(Motion.maxStaggerSteps) * Motion.staggerMs,
            easing = Motion.EaseOut
        ),
        label = "entrance"
    )
    // One `graphicsLayer` for both properties, not `.alpha()` + a layer.
    //
    // `Modifier.alpha` *is* a graphics layer, so this used to allocate two of them
    // per entering element: two offscreen buffers, two composites, for one fade and
    // one translation that the same node can carry. Alpha and translationY are the
    // two properties that stay on the render thread, which is the point.
    this.graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * distance.toPx()
    }
}
