package com.example.docscanner.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.example.docscanner.model.MarkupPoint
import com.example.docscanner.model.MarkupStroke
import com.example.docscanner.model.MarkupTool

/**
 * Paints [MarkupStroke]s onto a bitmap.
 *
 * Shared by the editor preview and the bake, so what is drawn on screen is what is
 * saved — the one rule in this app that has caused the most rework when broken.
 *
 * Two choices worth stating:
 *
 * * **Smoothing is quadratic through segment midpoints**, the same curve the signature
 *   pad uses. Raw `lineTo` on pointer samples shows visible corners at slow finger
 *   speeds, because a finger emits few samples and they are far apart.
 * * **The highlighter is translucent and multiplied, not opaque.** Ink at full opacity
 *   covers the text it is supposed to highlight, which is the difference between a
 *   highlighter and a marker.
 */
object MarkupRenderer {

    /** Alpha for [MarkupTool.HIGHLIGHTER]. Enough to read as a block, not to hide text. */
    const val HIGHLIGHT_ALPHA = 0.38f

    /** Paper colour painted by [MarkupTool.WHITEOUT]; the page is white. */
    private const val PAPER = 0xFFFFFFFF.toInt()

    /**
     * Draws [strokes] onto [canvas], whose coordinate space is [width] × [height].
     *
     * Called identically from the Compose preview and the offscreen bake, so the two
     * cannot diverge: the preview simply passes its own canvas size.
     */
    fun draw(
        canvas: Canvas,
        strokes: List<MarkupStroke>,
        width: Int,
        height: Int
    ) {
        if (width <= 0 || height <= 0) return
        strokes.forEach { stroke ->
            if (!stroke.isDrawable) return@forEach
            canvas.drawPath(
                pathFor(stroke, width, height),
                paintFor(stroke, width, height)
            )
        }
    }

    /** Convenience for the bake: draws onto [bitmap] in place. */
    fun draw(bitmap: Bitmap, strokes: List<MarkupStroke>) {
        if (strokes.isEmpty() || bitmap.isRecycled) return
        draw(Canvas(bitmap), strokes, bitmap.width, bitmap.height)
    }

    /**
     * Builds the smoothed path in page pixels.
     *
     * Normalised coordinates are multiplied by the page size here and nowhere else, so
     * there is exactly one place to get the mapping wrong.
     */
    fun pathFor(stroke: MarkupStroke, width: Int, height: Int): Path {
        val path = Path()
        val points = stroke.points
        if (points.isEmpty()) return path

        fun px(point: MarkupPoint) = Pair(point.x * width, point.y * height)

        val (startX, startY) = px(points.first())
        path.moveTo(startX, startY)
        if (points.size == 1) {
            // A single point is a dot: a zero-length segment with a round cap, which
            // is how a tap should read rather than as nothing at all.
            path.lineTo(startX + 0.01f, startY)
            return path
        }

        for (index in 1 until points.size) {
            val (previousX, previousY) = px(points[index - 1])
            val (currentX, currentY) = px(points[index])
            val midX = (previousX + currentX) / 2f
            val midY = (previousY + currentY) / 2f
            path.quadraticBezierTo(previousX, previousY, midX, midY)
            if (index == points.lastIndex) path.lineTo(currentX, currentY)
        }
        return path
    }

    fun paintFor(stroke: MarkupStroke, width: Int, height: Int): Paint {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = stroke.widthPx(width, height)

        when (stroke.tool) {
            MarkupTool.PEN -> {
                paint.color = stroke.colorArgb
            }
            MarkupTool.HIGHLIGHTER -> {
                paint.color = stroke.colorArgb
                paint.alpha = (HIGHLIGHT_ALPHA * 255).toInt()
                // Multiply, so a yellow highlighter over black text darkens the paper
                // and leaves the glyphs readable instead of tinting them.
                paint.xfermode = android.graphics.PorterDuffXfermode(
                    android.graphics.PorterDuff.Mode.MULTIPLY
                )
            }
            MarkupTool.WHITEOUT -> {
                paint.color = PAPER
                paint.style = Paint.Style.STROKE
            }
        }
        return paint
    }
}
