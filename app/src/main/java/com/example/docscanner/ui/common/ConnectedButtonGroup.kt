package com.example.docscanner.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.docscanner.theme.Motion
import com.example.docscanner.theme.pressScale

/**
 * A **connected button group**, built to Google's own numbers.
 *
 * Material 3 Expressive ships this as `ButtonGroup` / `MaterialButtonGroup` in
 * `material-components-android`. Compose does not have it yet — `ButtonGroup` first
 * appears in `material3` **1.5.0-beta01** and this app is on the 1.4 line — so the
 * anatomy is implemented here from the library's tokens rather than approximated by
 * eye. Every number below is quoted from
 * `lib/java/com/google/android/material/button/res/values/button_group_tokens.xml`
 * and `res/xml/m3expressive_connected_buttons_inner_corner_size_state_list.xml`:
 *
 * | Token | Value |
 * |:---|:---|
 * | outer corners (`ShapeAppearance.M3.Comp.ButtonGroup.Connected.Small.Container.Shape`) | **Full** — a pill |
 * | inner corners at rest | **8 dp** (`shapeCornerSizeSmall`) |
 * | inner corners while **pressed** | **4 dp** (`shapeCornerSizeExtraSmall`) |
 * | inner corners while **selected** | **50 %** |
 * | spacing between cells | **2 dp** |
 *
 * The middle of that table is the part nobody guesses right: a pressed cell's inner
 * corners get *tighter*, not rounder, and a selected one relaxes all the way to a
 * pill. That is what makes a connected group read as one object that changes shape
 * rather than as a row of buttons that all have the same corner radius.
 *
 * ## Deviation from the Views anatomy, and why
 *
 * `MaterialButton` puts the icon beside the label, because a button group in Google's
 * own apps holds two or three actions and pushes the rest into an overflow menu. This
 * app's bars hold four labelled actions on a 360 dp phone, which does not fit
 * side-by-side at a readable size. So the cells stack icon over label while keeping
 * every shape token above. When the bars grow an overflow menu, the stacking should go
 * too — that is the honest trade, written down rather than hidden.
 */
@Immutable
data class ConnectedAction(
    val label: String,
    val icon: ImageVector,
    /** The cell's own action. `null` means the cell is only a menu opener. */
    val onClick: (() -> Unit)? = null,
    /** Spoken label when the drawn one is not the whole story (e.g. "More"). */
    val contentDescription: String? = null,
    val enabled: Boolean = true,
    /** The one primary action: filled with the accent and relaxed to a pill. */
    val emphasized: Boolean = false,
    /** A checkable cell — a selection, not an action. */
    val selected: Boolean = false,
    /**
     * Overrides the icon/label tint for one cell — the destructive colour on Delete,
     * say. Kept per-cell rather than per-group because a group is otherwise uniform.
     */
    val tintColor: Color? = null,
    /**
     * Closely-related variants of this cell's action.
     *
     * With an [onClick] as well, the cell becomes a **split button**
     * (`MaterialSplitButton`): the body still runs the action on one tap and a chevron
     * at its trailing edge opens the variants. Without an [onClick], the whole cell is
     * the menu opener and there is no chevron, because two ways to do the same thing is
     * one control too many.
     *
     * `dismiss` closes the menu — call it first in an item's `onClick`, or the menu
     * stays open over the sheet the item just opened.
     */
    val menuContent: (@Composable ColumnScope.(dismiss: () -> Unit) -> Unit)? = null
)

@Composable
fun ConnectedButtonGroup(
    actions: List<ConnectedAction>,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onAccentColor: Color = MaterialTheme.colorScheme.onPrimary,
    cellHeight: Dp = ConnectedCellHeight
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        // 2 dp, the `m3_comp_button_group_connected_small_between_space` token. The
        // gap is what lets each cell keep its own shape; at 0 the group would need a
        // single background and the pressed cell could not change shape at all.
        horizontalArrangement = Arrangement.spacedBy(ConnectedSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEachIndexed { index, action ->
            ConnectedCell(
                action = action,
                first = index == 0,
                last = index == actions.lastIndex,
                containerColor = containerColor,
                contentColor = contentColor,
                accentColor = accentColor,
                onAccentColor = onAccentColor,
                cellHeight = cellHeight,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ConnectedCell(
    action: ConnectedAction,
    first: Boolean,
    last: Boolean,
    containerColor: Color,
    contentColor: Color,
    accentColor: Color,
    onAccentColor: Color,
    cellHeight: Dp,
    modifier: Modifier = Modifier
) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    var menuOpen by remember { mutableStateOf(false) }

    // The whole expressive trick, in one value: 8 dp at rest, 4 dp under the finger,
    // the full cell radius when it is the chosen one. `animateDpAsState` because the
    // corner is a `CornerSize` in dp — the 50 % token is realised as half the cell
    // height, which is exactly what 50 % of a corner resolves to on a square-ish cell.
    val innerCorner by animateDpAsState(
        targetValue = when {
            action.selected || action.emphasized -> cellHeight / 2
            pressed -> ConnectedPressedInnerCorner
            else -> ConnectedInnerCorner
        },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 520f),
        label = "connectedInnerCorner"
    )

    val shape = remember(first, last, innerCorner, cellHeight) {
        // The 50 % token, expressed in dp: half the cell height *is* 50 % of the
        // corner on a square-ish cell, and mixing a percent `CornerSize` with a dp one
        // would mean two corner systems in one shape.
        val outer = cellHeight / 2
        // `RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart)` — note the
        // bottom pair is reversed; getting this wrong swaps two corners on every
        // left-hand cell and is invisible until someone looks closely.
        RoundedCornerShape(
            topStart = if (first) outer else innerCorner,
            topEnd = if (last) outer else innerCorner,
            bottomEnd = if (last) outer else innerCorner,
            bottomStart = if (first) outer else innerCorner
        )
    }

    val filled = action.emphasized
    val plate by animateColorAsState(
        targetValue = when {
            filled -> accentColor
            pressed -> accentColor.copy(alpha = 0.16f)
            else -> containerColor
        },
        animationSpec = tween(durationMillis = if (pressed) 90 else 160),
        label = "connectedPlate"
    )
    val tint by animateColorAsState(
        targetValue = if (filled) onAccentColor else action.tintColor ?: contentColor,
        animationSpec = tween(durationMillis = 120),
        label = "connectedTint"
    )
    val alpha by animateFloatAsState(
        targetValue = if (action.enabled) 1f else 0.38f,
        animationSpec = Motion.fade,
        label = "connectedAlpha"
    )

    // Split button only when the cell has *both* an action and variants to offer.
    val split = action.onClick != null && action.menuContent != null

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = cellHeight)
                .graphicsLayer { this.alpha = alpha }
                .clip(shape)
                .background(plate)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .pressScale(interactions, pressedScale = 0.96f)
                    .clickable(
                        interactionSource = interactions,
                        indication = ripple(bounded = true),
                        enabled = action.enabled,
                        role = if (action.selected) Role.Checkbox else Role.Button,
                        onClick = {
                            val action0 = action.onClick
                            if (action0 != null) action0() else menuOpen = true
                        }
                    )
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.contentDescription ?: action.label,
                    tint = tint,
                    modifier = Modifier.size(21.dp)
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = action.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (filled || action.selected) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Medium
                    },
                    color = tint,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // The split button's trailing edge. One chevron, its own hit area, no
            // second label: on a 360 dp bar the difference between "Add page" and
            // "Add page, but from the album" has to be cheaper than a whole cell.
            if (split) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(26.dp)
                        .heightIn(min = cellHeight)
                        .clickable(
                            enabled = action.enabled,
                            role = Role.Button,
                            onClickLabel = "More ${action.label} options",
                            onClick = { menuOpen = true }
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = tint.copy(alpha = 0.70f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        val menu = action.menuContent
        if (menu != null) {
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                // Anchored to the cell. A cell in a bottom bar has nowhere to open
                // downward without leaving the screen, so it opens upward.
            ) {
                menu(dismiss = { menuOpen = false })
            }
        }
    }
}
