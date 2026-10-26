package com.example.docscanner.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.docscanner.theme.DocShapes
import com.example.docscanner.theme.pressScale

/**
 * The app's tactile button vocabulary.
 *
 * Material 3 Expressive is mostly three ideas — generous shapes, springy motion,
 * and tonal colour that states importance — so these primitives implement those
 * three and every screen composes from them rather than reaching for defaults.
 */

/** Filled tonal pill. The primary action on a surface. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    container: Color = MaterialTheme.colorScheme.primary,
    content: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true
) {
    val interactions = remember { MutableInteractionSource() }
    val enabledAlpha by animateFloatAsState(if (enabled) 1f else 0.45f, label = "pillEnabled")
    Surface(
        color = container,
        contentColor = content,
        shape = DocShapes.pill,
        modifier = modifier
            .graphicsLayer { alpha = enabledAlpha }
            .pressScale(interactions)
            .clip(DocShapes.pill)
            .clickable(
                interactionSource = interactions,
                indication = ripple(),
                enabled = enabled,
                onClick = onClick
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Quieter pill for the secondary action next to [PillButton]. */
@Composable
fun TonalPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) = PillButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    icon = icon,
    container = MaterialTheme.colorScheme.secondaryContainer,
    content = MaterialTheme.colorScheme.onSecondaryContainer,
    enabled = enabled
)

/**
 * Icon button on a soft tonal square. Used for the camera's flash / gallery / grid
 * controls, where a bare icon would disappear against a photograph.
 */
@Composable
fun TonalIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.72f),
    content: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp
) {
    val interactions = remember { MutableInteractionSource() }
    Surface(
        color = container,
        contentColor = content,
        shape = DocShapes.iconButton,
        modifier = modifier
            .size(size)
            .pressScale(interactions)
            .clip(DocShapes.iconButton)
            .clickable(interactionSource = interactions, indication = ripple(), onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(iconSize))
        }
    }
}

/**
 * A small label that states a fact: "12 pages", "A4", "3 selected".
 *
 * Used in place of stacked text because a row of pills is scanned instantly and
 * reads as data, while a paragraph of `bodySmall` reads as prose.
 */
@Composable
fun InfoPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    content: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    // A `Box`, not a `Surface`: this is drawn once per library card ("12 pages"),
    // and `Surface` composes semantics, a clip, a background and an elevation layer
    // for every one of them. On a scroll that is per-tile overhead for a label.
    Box(
        modifier = modifier
            .clip(DocShapes.pill)
            .background(container)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(5.dp))
            }
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = content,
                maxLines = 1
            )
        }
    }
}

/**
 * A grouped card: one idea per card, with an optional label and trailing action.
 *
 * Settings, document metadata and export options all use this so the app has a
 * single notion of "a group of related rows" instead of each screen inventing its
 * own dividers.
 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    container: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Surface(
        color = container,
        shape = DocShapes.card,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(vertical = 6.dp)) {
            if (title != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (subtitle != null) {
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    trailing?.invoke()
                }
            }
            content()
        }
    }
}

/**
 * A segmented control with a sliding thumb.
 *
 * Two states only in this app (Borders / Filters), which is the case where a
 * segmented control beats tabs: both options stay visible and the thumb makes the
 * current one unmistakable, even at a glance.
 */
@Composable
fun SegmentedPill(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = DocShapes.pill,
        modifier = modifier
    ) {
        BoxWithConstraints(modifier = Modifier.padding(4.dp)) {
            val segmentWidth = maxWidth / options.size
            // Pixels, translated on the layer — not a `Dp` handed to `offset`.
            //
            // `Modifier.offset(x = animatedDp)` is a *layout* modifier: every frame
            // of the slide re-places the thumb and its subtree. `translationX` on a
            // graphics layer is a draw-time transform, so the slide costs the same
            // as an idle frame and cannot re-run measure on the pill's contents.
            // Animating a layout property is the one thing motion must never do.
            val density = LocalDensity.current
            val segmentWidthPx = with(density) { segmentWidth.toPx() }
            val thumbOffsetPx by animateFloatAsState(
                targetValue = segmentWidthPx * selectedIndex,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
                label = "segmentedThumb"
            )
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = DocShapes.pill,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .width(segmentWidth)
                    .height(38.dp)
                    .graphicsLayer { translationX = thumbOffsetPx }
            ) {}
            Row(Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier
                            .width(segmentWidth)
                            .height(38.dp)
                            .clip(DocShapes.pill)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onSelect(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        val selected = index == selectedIndex
                        Text(
                            label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Three dots that breathe while work happens.
 *
 * A spinner says "wait"; staggered dots say "working" and, because they have a
 * rhythm, they read as alive rather than stalled on a long filter bake. Each dot
 * reads the animation *inside* the layer block, so the loop redraws without
 * recomposing the row sixty times a second.
 */
@Composable
fun LoadingDots(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    dotSize: Dp = 8.dp
) {
    val phase = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        phase.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1100, easing = LinearEasing)
            )
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier) {
        repeat(3) { index ->
            Box(
                Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        val t = (phase.value + index * 0.16f) % 1f
                        val wave = kotlin.math.sin(t * Math.PI).toFloat().coerceAtLeast(0f)
                        scaleX = 0.7f + 0.3f * wave
                        scaleY = 0.7f + 0.3f * wave
                        alpha = 0.35f + 0.65f * wave
                    }
                    .clip(RoundedCornerShape(percent = 50))
                    .background(color)
            )
        }
    }
}

/**
 * A thin progress track with a rounded head, for long operations that have a
 * known page count. The determinate form is what lets a five-page bake show
 * "2 of 5" instead of an indeterminate spinner that hides the cost.
 */
@Composable
fun PageProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 260f),
        label = "pageProgress"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(DocShapes.pill)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(6.dp)
                .clip(DocShapes.pill)
                .background(
                    Brush.horizontalGradient(
                        listOf(color, MaterialTheme.colorScheme.tertiary)
                    )
                )
        )
    }
}

/**
 * Section eyebrow: the small caps label above a group.
 *
 * Deliberately not a `titleMedium`: an eyebrow is a navigation aid, not content,
 * so it stays quiet and lets the cards below carry the hierarchy.
 */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 8.dp, top = 4.dp, bottom = 8.dp)
    )
}

/** Horizontal hairline that is quiet enough to disappear when not needed. */
@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}

/** Expands a trailing chip only when there is room, so rows never truncate badly. */
@Composable
fun RevealWhen(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally()
    ) { content() }
}
