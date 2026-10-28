package com.example.docscanner.ui.camera

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.docscanner.theme.LocalDocAccents
import com.example.docscanner.util.LiveBorderTracker
import com.example.docscanner.util.SceneRect

/**
 * The live page outline: four moving corners, drawn over the viewfinder.
 *
 * ## What it is, and what it is not
 *
 * It is the *detected page boundary*, redrawn as the page moves in frame. It is not
 * the crop: the warp happens on the full-resolution capture, where the border is
 * re-detected. That division is deliberate, and it is why this overlay can be
 * cheap — see [LiveBorderAnalyzer].
 *
 * ## Why an outline, and not the old dark window
 *
 * The screen used to draw a fixed dark mask with a pulsing dashed rectangle. It was
 * decorative, and it taught the wrong thing: users filled the frame right past the
 * paper edge, leaving no boundary in the photo to find. A mask that *tracks the
 * page* teaches the right thing — line the outline up on the paper and the crop
 * will be that page. It also answers the question the user actually has while
 * framing ("is it seeing my page?"), which a static frame never could.
 *
 * ## Drawing rules
 *
 * * **Two strokes, one path.** A wide, soft black stroke under a thin white one.
 *   Paper is bright and desks are dark; a single-colour line disappears on one of
 *   them. This is the same reason the chrome carries a gradient scrim.
 * * **Corner ticks, not dashes.** The four corner marks are the part that has to be
 *   readable at a glance, and they are drawn *inside* the quad along the two
 *   adjacent edges, so they never add width to the outline itself.
 * * **Alpha only.** Appearing is a 180 ms fade of the whole layer — no scale, no
 *   bounce, no per-frame recomposition of the layout. The quad already moves; the
 *   animation must not add its own motion to it.
 * * **Never on the capture.** The outline lives in the view hierarchy, so the JPEG
 *   is the frame the sensor saw, not the frame the overlay drew on.
 */
@Composable
fun LiveBorderOverlay(
    state: LiveBorderTracker.State?,
    frameAspect: Float,
    modifier: Modifier = Modifier
) {
    val accents = LocalDocAccents.current
    val shown = state?.visible == true
    val alpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "liveBorderAlpha"
    )
    if (alpha <= 0.01f || state == null) return

    val corners = state.corners

    Canvas(modifier = modifier) {
        val scene = SceneRect.fitCenter(size.width, size.height, frameAspect)
        // A quad that is still the full frame has nothing to place: the tracker
        // only raises `visible` for real quads, but a defensive early return keeps
        // a stray state from drawing a rectangle around the whole viewfinder.
        if (corners.isFullFrame) return@Canvas
        val points = floatArrayOf(
            scene.x(corners.topLeftX), scene.y(corners.topLeftY),
            scene.x(corners.topRightX), scene.y(corners.topRightY),
            scene.x(corners.bottomRightX), scene.y(corners.bottomRightY),
            scene.x(corners.bottomLeftX), scene.y(corners.bottomLeftY)
        )
        val path = Path().apply {
            moveTo(points[0], points[1])
            lineTo(points[2], points[3])
            lineTo(points[4], points[5])
            lineTo(points[6], points[7])
            close()
        }

        val halo = 5.dp.toPx()
        val line = 2.dp.toPx()
        // Under-stroke: keeps the outline visible on a white page, where a bare
        // white line would be invisible.
        drawPath(
            path = path,
            color = Color.Black.copy(alpha = 0.30f * alpha),
            style = Stroke(width = halo, join = StrokeJoin.Round)
        )
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.94f * alpha),
            style = Stroke(width = line, join = StrokeJoin.Round)
        )

        drawCornerTicks(points, accents.scan.copy(alpha = alpha))
    }
}

/**
 * Four right-angle marks, one per corner, drawn along the edges they meet.
 *
 * 22 dp long, capped round and slightly thicker than the outline so the corners
 * read as the anchors of the shape — the same convention every scanner viewfinder
 * uses, and the reason the outline can be thin without looking weak.
 */
private fun DrawScope.drawCornerTicks(points: FloatArray, color: Color) {
    val length = 22.dp.toPx()
    val width = 3.5.dp.toPx()
    val cap = StrokeCap.Round
    for (corner in 0 until 4) {
        val cx = points[corner * 2]
        val cy = points[corner * 2 + 1]
        val next = (corner + 1) % 4
        val previous = (corner + 3) % 4
        drawEdgeTick(Offset(cx, cy), Offset(points[next * 2], points[next * 2 + 1]), length, width, cap, color)
        drawEdgeTick(Offset(cx, cy), Offset(points[previous * 2], points[previous * 2 + 1]), length, width, cap, color)
    }
}

private fun DrawScope.drawEdgeTick(
    corner: Offset,
    toward: Offset,
    length: Float,
    width: Float,
    cap: StrokeCap,
    color: Color
) {
    val dx = toward.x - corner.x
    val dy = toward.y - corner.y
    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
    if (distance < 1f) return
    // Never longer than the edge it sits on: on a small quad (a distant page) a
    // fixed 22 dp tick would meet its neighbour and turn four marks into a box.
    val reach = minOf(length, distance * 0.42f)
    drawLine(
        color = color,
        start = corner,
        end = Offset(corner.x + dx / distance * reach, corner.y + dy / distance * reach),
        strokeWidth = width,
        cap = cap
    )
}
