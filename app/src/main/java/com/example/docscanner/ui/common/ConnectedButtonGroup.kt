package com.example.docscanner.ui.common

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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
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
    /** Omit for a label-only cell — a segmented switch, not an action bar. */
    val icon: ImageVector? = null,
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

val ConnectedCellHeight: Dp = 56.dp
val ConnectedSpacing: Dp = 2.dp
val ConnectedInnerCorner: Dp = 8.dp
val ConnectedPressedInnerCorner: Dp = 4.dp

@Composable
fun ConnectedButtonGroup(
    actions: List<ConnectedAction>,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onAccentColor: Color = MaterialTheme.colorScheme.onPrimary,
    cellHeight: Dp = ConnectedCellHeight,
    /**
     * Gap between two cells. The M3 token is 2 dp; a bar that floats over a page can
     * afford a touch more, and the inset below has to match it or the group reads as
     * off-centre inside its card.
     */
    spacing: Dp = ConnectedSpacing
) {
    // One popup for the whole group, not one per cell.
    //
    // A `DropdownMenu` is a `Popup` in its own window, anchored to the bounds of the
    // composable that declares it. Declaring it inside a *weighted cell* of a bottom
    // bar means the popup is anchored to a box whose position is re-resolved every time
    // the bar's contents change — and in the reader that bar swaps shelves. The visible
    // symptom was the More cell appearing to blink out the instant it was tapped. One
    // menu owned by the group, anchored to the group, cannot do that.
    var openMenuIndex by remember { mutableStateOf<Int?>(null) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            // 2 dp, the `m3_comp_button_group_connected_small_between_space` token. The
            // gap is what lets each cell keep its own shape; at 0 the group would need a
            // single background and the pressed cell could not change shape at all.
            horizontalArrangement = Arrangement.spacedBy(spacing),
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
                    // The cell whose menu is open keeps its state layer, so the control
                    // that opened the menu still looks like the control that is open.
                    menuOpen = openMenuIndex == index,
                    onOpenMenu = { openMenuIndex = index },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        val openAction = openMenuIndex?.let { actions.getOrNull(it) }
        val menu = openAction?.menuContent
        if (menu != null) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { openMenuIndex = null }
            ) {
                menu { openMenuIndex = null }
            }
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
    /** This cell's menu is the one currently open. */
    menuOpen: Boolean,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()

    // The whole expressive trick, in one value: 8 dp at rest, 4 dp under the finger,
    // the full cell radius when it is the chosen one. `animateDpAsState` because the
    // corner is a `CornerSize` in dp — the 50 % token is realised as half the cell
    // height, which is exactly what 50 % of a corner resolves to on a square-ish cell.
    val innerCorner by animateDpAsState(
        targetValue = if (action.selected || action.emphasized) cellHeight / 2 else ConnectedInnerCorner,
        animationSpec = tween(durationMillis = 150),
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

    // A *selected* cell is filled with the accent, exactly as Google's connected
    // button group fills its checked item. `emphasized` is the same fill for an action
    // that is the primary one without being a selection.
    val filled = action.emphasized || action.selected

    // ## Why neither the fill nor the label is animated between states
    //
    // This is the whole of the "the buttons disappear when I toggle them" report, and
    // it survived one wrong fix, so it is written down exactly.
    //
    // Toggling a cell changes *two* things at once: the plate (dark container -> solid
    // accent) and the ink (light -> dark on a light accent). Compose animations do not
    // know they are coupled, so any duration at all produces frames in the middle where
    // the plate is half-way between a black sheet and a white fill — i.e. mid grey —
    // and the ink has already become black. Black on mid grey, for 100-160 ms, twice per
    // toggle. On the camera's Single/Batch switch it reads as the label blinking out; on
    // the reader's annotate shelf it reads as the button vanishing.
    //
    // Material 3 does not animate a selected item's container either: the checked item
    // switches. So both values are now computed directly — no crossfade across the
    // filled boundary, no window of low contrast — and the *press* is what animates,
    // as a state layer in the ink's own colour (which is dark-on-light when filled and
    // light-on-dark when not, so it can never wash the label out).
    val tint = if (filled) onAccentColor else (action.tintColor ?: contentColor)
    val plate = if (filled) accentColor else containerColor

    // ## The state layer keeps the ink it was painted with
    //
    // This is the flicker on the camera's Single/Batch switch — the one that survived
    // every fix to the fill — and it is not a fade problem, it is a *colour* problem.
    //
    // The layer fades from 14 % back to 0 after the finger lifts. The colour it was drawn
    // in used to be `tint`, which flips the instant the tap lands. So on the camera sheet
    // a tap on "Batch" went like this:
    //
    //   1. finger down — plate is the dark glass, `tint` is **white**, layer paints white.
    //   2. finger up   — `isBatch` flips, so the plate becomes **solid white** and `tint`
    //                    becomes **black** in the same frame.
    //   3. the layer is still at ~14 % and is now painted **black over the new white
    //      pill**, and fades out over the next ~160 ms.
    //
    // i.e. the control you just selected comes up grey and bleeds to white, twice per
    // toggle, right under the shutter — which reads as the button glitching or briefly
    // disappearing. Nothing about the fill was wrong; the *exit* was painted in the ink
    // of the state the user had not seen yet.
    //
    // So the layer's ink is captured when the finger goes down and held until the next
    // press. The exit can then only ever be the colour of the state being left: white
    // over a white pill is nothing, black over the dark glass is nothing. `tint` is still
    // what drives everything that is *visible at rest*, so the plate, the label and the
    // selected state are unchanged.
    var layerInk by remember(tint) { mutableStateOf(tint) }
    LaunchedEffect(pressed, tint) {
        if (pressed) layerInk = tint
    }
    val pressLayer by animateFloatAsState(
        targetValue = if (pressed || menuOpen) 0.14f else 0f,
        // Faster out than before (120 ms, was 160): the layer exists to acknowledge the
        // touch, and it has no business lingering over a control that has already
        // answered.
        animationSpec = tween(durationMillis = if (pressed) 80 else 120),
        label = "connectedPress"
    )
    // The *ink* dims when the cell is disabled; the plate never does.
    //
    // The old value faded the whole cell with `graphicsLayer { alpha = … }` on the Row,
    // plate included. On a light bar a 38 %-opaque white plate over the bar's own
    // surface is, for practical purposes, nothing: the cell stopped looking disabled and
    // started looking *absent*, and its label — the same 38 % — went with it. That is
    // how the annotate shelf's Thin/Medium/Bold "disappeared" whenever Erase was chosen
    // (choosing the eraser used to disable the width group). A disabled control still has
    // to be a control: it keeps its shape and its place, and only its ink goes quiet.
    val inkAlpha by animateFloatAsState(
        targetValue = if (action.enabled) 1f else 0.50f,
        animationSpec = Motion.fade,
        label = "connectedInkAlpha"
    )

    // Split button only when the cell has *both* an action and variants to offer.
    val split = action.onClick != null && action.menuContent != null

    // A cell with an icon and no label is a *compact* cell — the page grid's row of
    // verbs, where four labelled cells do not fit across half a phone. It is the same
    // component and the same shape tokens, just without the word under the glyph.
    val iconOnly = action.icon != null && action.label.isBlank()

    Box(modifier = modifier) {
        // The cell's content colour is *provided*, not inherited.
        //
        // Without this the ripple and any ambient `LocalContentColor` come from the
        // theme — dark ink — while the cell sits on a black bar or a black camera
        // sheet. The press indicator then paints a dark disc over the cell and it looks
        // like the control vanished for a moment. That was both the camera's
        // Single/Batch switch and the reader's More button "glitching": not a state
        // bug, a ripple in the wrong colour.
        CompositionLocalProvider(LocalContentColor provides tint) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = cellHeight)
                    .clip(shape)
                    .background(plate)
                    // The press state layer: the cell's own ink at 14 %, so it darkens a
                    // light fill and lightens a dark one. Drawn *over* the plate and
                    // *under* the content, and independent of the selected state.
                    .background(layerInk.copy(alpha = pressLayer))
            ) {
                val cellIcon = action.icon
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer { this.alpha = inkAlpha }
                        // ## No ripple, and no scale. The state layer *is* the feedback.
                        //
                        // Both of them painted over the label. The ripple was coloured
                        // with the cell's own ink (`tint.copy(alpha = 0.22f)`) and was
                        // bounded to this Column — i.e. exactly the rectangle the label
                        // occupies — so a tap laid a wash of the label's colour on top
                        // of the label. On the annotate shelf's label-only cells, where
                        // the word is the entire cell, that reads as the button blinking
                        // out under the finger and coming back: the "disappearing when
                        // tapped, appears when long pressed" report.
                        //
                        // `pressLayer` above is the M3 Expressive answer and it is
                        // already here: the cell's own ink at 14 %, drawn *under* the
                        // content. It cannot touch the label, it is symmetric in and out
                        // of the filled state, and it needs no indication at all.
                        //
                        // `pressScale` went with it. A 0.96 scale inside a clipped pill,
                        // on a cell whose neighbours are 2 dp away, is a half-scaling,
                        // half-clipped edge on every press — three layers animating for
                        // one affordance that the plate already shows.
                        .clickable(
                            interactionSource = interactions,
                            indication = null,
                            enabled = action.enabled,
                            role = if (action.selected) Role.Checkbox else Role.Button,
                            onClick = {
                                val action0 = action.onClick
                                if (action0 != null) action0() else onOpenMenu()
                            }
                        )
                        .padding(
                            horizontal = 4.dp,
                            vertical = if (iconOnly) 0.dp else 8.dp
                        )
                ) {
                    if (cellIcon != null) {
                        Icon(
                            imageVector = cellIcon,
                            contentDescription = action.contentDescription
                                ?: action.label.takeIf { it.isNotBlank() },
                            tint = tint,
                            modifier = Modifier.size(if (iconOnly) 20.dp else 21.dp)
                        )
                        if (!iconOnly) Spacer(Modifier.height(5.dp))
                    }
                    if (!iconOnly) {
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
                                onClick = onOpenMenu
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = tint.copy(alpha = 0.70f * inkAlpha),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

    }
}
