package com.example.docscanner.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.docscanner.theme.DocShapes
import com.example.docscanner.theme.Motion
import com.example.docscanner.theme.pressScale

/**
 * The app's one bottom action bar, in the Material 3 Expressive idiom.
 *
 * ## What makes it "expressive" rather than four icons in a row
 *
 * Expressive Material is not a palette, it is a set of behaviours:
 *
 * - **Springs, not curves.** Press and release run on [Motion.press] / [Motion.bouncy];
 *   a linear tween on a 40 dp object reads as a web page, a spring reads as an object.
 * - **The cells are a connected group.** [ConnectedButtonGroup] draws them to Google's
 *   own tokens — pill ends, 8 dp inner corners, 4 dp while held — rather than four
 *   identical rounded rectangles side by side.
 * - **Containers, not dividers.** The bar is one tonal container with the page counter
 *   inside it, rather than a row of icons floating on the page.
 *
 * Every animation here is transform/alpha — no layout is animated, so none of it can
 * cost a measure pass.
 */
@Composable
fun ExpressiveActionBar(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    shape: Shape = DocShapes.sheet,
    shadowElevation: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    // Arrives with a short lift. A bar that is simply *there* on screen open reads
    // as part of the page; 12 dp and a fade reads as chrome that has slid up to be
    // useful, which is what it is.
    val arrive = remember { Animatable(0f) }
    LaunchedEffect(Unit) { arrive.animateTo(1f, Motion.emphasized) }

    Surface(
        color = containerColor,
        shape = shape,
        shadowElevation = shadowElevation,
        modifier = modifier.graphicsLayer {
            alpha = arrive.value
            translationY = (1f - arrive.value) * 12.dp.toPx()
        }
    ) {
        Column(content = content)
    }
}

/**
 * Where you are in the document: `‹ 3 / 8 ›` with a progress rail under it.
 *
 * The counter swaps with a directional slide — the number that leaves moves the way the
 * page went, so a swipe and a tap produce the same visual answer to "which way did I
 * just move". The rail is a `scaleX` on a fixed-width box (draw-phase, no re-measure)
 * and fills as you approach the last page, which quietly answers "how long is this?"
 * without spending a row of text on it.
 */
@Composable
fun ExpressivePager(
    index: Int,
    count: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    railColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
    accentColor: Color = MaterialTheme.colorScheme.primary,
    trailing: (@Composable () -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            PagerStep(
                icon = Icons.Default.ChevronLeft,
                description = "Previous page",
                enabled = index > 0,
                tint = contentColor,
                onClick = onPrev
            )
            // Fixed width so the arrows do not shuffle sideways when the counter goes
            // from 9 / 10 to 10 / 10. A pager that moves under your thumb is a pager
            // you miss.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.width(PAGER_NUMBER_WIDTH)
            ) {
                AnimatedContent(
                    targetState = index,
                    transitionSpec = {
                        val forward = targetState > initialState
                        val enter = slideInHorizontally(
                            animationSpec = tween(durationMillis = 220, easing = Motion.EaseOut)
                        ) { width -> if (forward) width / 2 else -width / 2 } +
                            fadeIn(animationSpec = Motion.fade)
                        val exit = slideOutHorizontally(
                            animationSpec = tween(durationMillis = 160, easing = Motion.EaseOut)
                        ) { width -> if (forward) -width / 2 else width / 2 } +
                            fadeOut(animationSpec = tween(durationMillis = 120))
                        enter togetherWith exit
                    },
                    label = "pagerCounter"
                ) { current ->
                    Text(
                        text = "${current + 1} / $count",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                        maxLines = 1
                    )
                }
            }
            PagerStep(
                icon = Icons.Default.ChevronRight,
                description = "Next page",
                enabled = index < count - 1,
                tint = contentColor,
                onClick = onNext
            )
            if (trailing != null) {
                Spacer(Modifier.width(6.dp))
                trailing()
            }
        }

        Spacer(Modifier.height(6.dp))

        // The rail. `transformOrigin` at the left edge with `scaleX` = fraction, not a
        // `fillMaxWidth(fraction)`: the latter is a layout change every frame.
        val progress by animateFloatAsState(
            targetValue = if (count <= 0) 0f else (index + 1f) / count,
            animationSpec = Motion.spatial,
            label = "pagerRail"
        )
        Box(
            modifier = Modifier
                .width(RAIL_WIDTH)
                .height(3.dp)
                .clip(DocShapes.pill)
                .background(railColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0f, 0.5f)
                        scaleX = progress
                    }
                    .background(accentColor)
            )
        }
    }
}

/** One chevron in [ExpressivePager]. */
@Composable
private fun PagerStep(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    tint: Color,
    onClick: () -> Unit
) {
    val interactions = remember { MutableInteractionSource() }
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.25f,
        animationSpec = Motion.fade,
        label = "pagerStepAlpha"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer { this.alpha = alpha }
            .clip(DocShapes.pill)
            .pressScale(interactions, pressedScale = 0.88f)
            .clickable(
                interactionSource = interactions,
                indication = ripple(bounded = true),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(20.dp))
    }
}

/**
 * A horizontal divider that is a hairline of the *container* colour rather than a
 * border: in a translucent bar a real divider is a hard line, and a bar that floats
 * over a page should not have one.
 */
@Composable
fun BarHairline(modifier: Modifier = Modifier, color: Color) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

/** Plate corner radius at rest; pressed it relaxes toward [PLATE_CORNER_PRESSED_DP]. */
private const val PLATE_CORNER_DP = 14f

/** The pressed radius. Kept next to the rest radius so the pair can be tuned together. */
private const val PLATE_CORNER_PRESSED_DP = 22f

private val PAGER_NUMBER_WIDTH = 78.dp
private val RAIL_WIDTH = 96.dp
