package com.example.docscanner.ui.common

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max

/**
 * A page you can pinch, pan, and swipe between.
 *
 * One gesture, written once. The editor already had a hand-rolled pinch inside its
 * border canvas and the filter preview had none at all — which is exactly how the
 * filter stage ended up unable to zoom while the crop stage could: the zoom lived
 * inside one canvas and the other never got it. Anything that shows a page and wants
 * to be magnified should use this rather than grow a third copy.
 *
 * ## What a finger can do
 *
 * | Gesture | Result |
 * |:---|:---|
 * | Two fingers, pinch | Zoom between 1× and [MAX_PAGE_ZOOM], around the pinch centre |
 * | One finger, zoomed in | Pan, clamped so the page never leaves the frame |
 * | One finger, at 1×, horizontal | Previous / next page — the whole point of a pager |
 * | One finger, at 1×, vertical | Nothing. A vertical drag is not a page turn, and treating it as one makes the viewer feel twitchy |
 *
 * ## Why the swipe only works at 1×
 *
 * Zoomed in, a horizontal drag is the only way to reach the right-hand edge of the page.
 * Hijacking it to turn the page would make a zoomed page impossible to read. So the
 * page turn lives where there is nothing to pan — exactly where the user has nothing
 * else to lose.
 *
 * ## Why the state is hoisted instead of owned here
 *
 * Callers reset the zoom when the page changes, and the editor shows a zoom badge and a
 * reset control in its own chrome. A composable that owned `zoom` could not offer
 * either. This one reads through [rememberUpdatedState] so a pinch never restarts the
 * gesture block (which is the bug that made corner dragging do nothing in an earlier
 * round), and reports every change up through [onZoomPanChange].
 *
 * @param contentSize the on-screen size of the thing being transformed, when it is
 *   smaller than the frame (a letterboxed page). Pan is clamped to *its* overflow, so
 *   a page with grey margins around it can never be dragged off into those margins.
 *   Defaults to the full frame.
 * @param onSwipePrev / [onSwipeNext] pass `null` to disable that direction — at the
 *   first or last page, a swipe should do nothing rather than bounce.
 */
@Composable
fun ZoomablePageView(
    zoom: Float,
    pan: Offset,
    onZoomPanChange: (zoom: Float, pan: Offset) -> Unit,
    modifier: Modifier = Modifier,
    onSwipePrev: (() -> Unit)? = null,
    onSwipeNext: (() -> Unit)? = null,
    contentSize: Size? = null,
    maxZoom: Float = MAX_PAGE_ZOOM,
    content: @Composable BoxScope.() -> Unit
) {
    val latestZoom by rememberUpdatedState(zoom)
    val latestPan by rememberUpdatedState(pan)
    val report by rememberUpdatedState(onZoomPanChange)
    val prev by rememberUpdatedState(onSwipePrev)
    val next by rememberUpdatedState(onSwipeNext)
    val limit by rememberUpdatedState(maxZoom)
    val bounds by rememberUpdatedState(contentSize)

    Box(
        modifier = modifier
            .clipToBounds()
            .pointerInput(Unit) {
                awaitEachGesture {
                    // Only the *arrival* of a finger matters here; the movement is
                    // read from the events below.
                    awaitFirstDown(requireUnconsumed = false)
                    var multiTouch = false
                    var horizontalTravel = 0f
                    var verticalTravel = 0f
                    var moved = false
                    val touchSlop = viewConfiguration.touchSlop
                    val frameWidth = size.width.toFloat()
                    val frameHeight = size.height.toFloat()

                    val boxWidth = bounds?.width?.takeIf { it > 0f } ?: frameWidth
                    val boxHeight = bounds?.height?.takeIf { it > 0f } ?: frameHeight

                    fun clampPan(scale: Float, candidate: Offset): Offset {
                        // A zoomed page may be panned by exactly the amount that is
                        // off-screen, no further: half the overflow on each axis.
                        val overflow = max(0f, scale - 1f)
                        val maxX = overflow * boxWidth / 2f
                        val maxY = overflow * boxHeight / 2f
                        return Offset(
                            candidate.x.coerceIn(-maxX, maxX),
                            candidate.y.coerceIn(-maxY, maxY)
                        )
                    }

                    // The transform is mirrored locally for the life of the gesture.
                    // Reading the parent's state back on every pointer event would
                    // read whatever the last *recomposition* saw — a frame stale — so
                    // a pinch would repeatedly apply its change to an older zoom and
                    // silently lose most of the magnification. The mirror is seeded
                    // from the parent (the only other writer) and reported back out.
                    var localZoom = latestZoom
                    var localPan = latestPan

                    var event: PointerEvent
                    do {
                        event = awaitPointerEvent()
                        val pressed = event.changes.count { it.pressed }

                        if (pressed >= 2) {
                            multiTouch = true
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()
                            if (zoomChange != 1f || panChange != Offset.Zero) {
                                val before = localZoom
                                val after = (before * zoomChange).coerceIn(1f, limit)
                                val ratio = after / before
                                // Anchored on the pinch centre, so the pixels under the
                                // fingers stay under the fingers.
                                val centroid = event.calculateCentroid(useCurrent = false)
                                val local = Offset(
                                    centroid.x - frameWidth / 2f,
                                    centroid.y - frameHeight / 2f
                                )
                                val candidate = if (after <= 1.0001f) {
                                    Offset.Zero
                                } else {
                                    localPan * ratio + local * (1f - ratio) + panChange
                                }
                                localZoom = after
                                localPan = clampPan(after, candidate)
                                report(localZoom, localPan)
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            }
                        } else if (pressed == 1) {
                            val change = event.changes.firstOrNull { it.pressed } ?: continue
                            if (localZoom > 1.001f) {
                                val delta = change.position - change.previousPosition
                                if (delta != Offset.Zero) {
                                    localPan = clampPan(localZoom, localPan + delta)
                                    report(localZoom, localPan)
                                    change.consume()
                                }
                            } else {
                                // Accumulate rather than reading the total: a gesture
                                // that wanders and comes back has not swiped.
                                horizontalTravel += change.position.x - change.previousPosition.x
                                verticalTravel += change.position.y - change.previousPosition.y
                                if (abs(horizontalTravel) > touchSlop ||
                                    abs(verticalTravel) > touchSlop
                                ) {
                                    moved = true
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    // ---- decide whether this was a page turn ----------------------
                    if (!multiTouch && moved && localZoom <= 1.001f) {
                        val distance = abs(horizontalTravel)
                        val deliberatelyHorizontal =
                            distance > abs(verticalTravel) * SWIPE_AXIS_BIAS
                        if (deliberatelyHorizontal && distance > SWIPE_THRESHOLD_DP.dp.toPx()) {
                            if (horizontalTravel < 0f) next?.invoke() else prev?.invoke()
                        }
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoom
                    scaleY = zoom
                    translationX = pan.x
                    translationY = pan.y
                },
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

/**
 * How far a page magnifies before the pan clamp takes over.
 *
 * Matches the editor's `MAX_ZOOM` and the border canvas: the + button, the pinch and
 * the zoom percentage all have to agree on where the ceiling is, or the button stays
 * enabled at the point nothing more can happen.
 */
const val MAX_PAGE_ZOOM = 5f

/**
 * How far a horizontal drag must travel to turn the page.
 *
 * 56 dp is a little over a thumb's width — far enough that a tap-and-wobble is not a
 * page turn, close enough that a flick does not need to be dragged across the screen.
 */
private const val SWIPE_THRESHOLD_DP = 56

/**
 * How much more horizontal than vertical a drag must be to count.
 *
 * At 1.4, a diagonal drag still turns the page; at 1.0, a slightly sloped vertical drag
 * would too, and the viewer would flip pages while the user was trying to scroll.
 */
private const val SWIPE_AXIS_BIAS = 1.4f
