package com.example.docscanner.ui.editor

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.MarkupHistory
import com.example.docscanner.model.MarkupPoint
import com.example.docscanner.model.MarkupStroke
import com.example.docscanner.model.MarkupTool
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.util.MarkupRenderer
import com.example.docscanner.util.PageRenderer

/**
 * The parts of annotation that are **not** chrome: the ink palette, the stroke state and
 * the surface it is drawn on.
 *
 * They used to live inside `MarkupScreen`, which was fine while annotating was a screen
 * of its own. The viewer now swaps its bottom shelf into annotate settings *in place*
 * (the ink and the page never leave the screen you are reading on), so the same stroke
 * model has to serve two hosts. Anything that decides **what gets stored** lives here
 * once; each host only decides what the controls look like.
 *
 * The normalised coordinates, the tool set, the undo rules and the bake all stay
 * exactly as they were — where a control is drawn is chrome, what it writes is not.
 */

/** Four inks, black first: the colour you want most of the time is the cheapest to hit. */
internal val MarkupInkColors = listOf(
    0xFF111111.toInt(),
    0xFFC62828.toInt(),
    0xFF1565C0.toInt(),
    0xFFF9A825.toInt()
)

/** Stroke widths as fractions of the page's short side. */
internal val MarkupWidths = listOf(0.004f, 0.008f, 0.016f)

/** Short names for the three widths, in the order of [MarkupWidths]. */
internal val MarkupWidthLabels = listOf("Thin", "Medium", "Bold")

/**
 * Spoken name for an ink colour. "Ink colour 1" would be useless to a screen-reader
 * user; the name is what makes the set navigable.
 */
internal fun inkLabel(argb: Int): String = when (argb) {
    0xFF111111.toInt() -> "Black ink"
    0xFFC62828.toInt() -> "Red ink"
    0xFF1565C0.toInt() -> "Blue ink"
    0xFFF9A825.toInt() -> "Yellow ink"
    else -> "Ink colour"
}

/**
 * One in-progress annotation session: the strokes so far, the tool settings, and the
 * finger currently down.
 *
 * Deliberately a plain class held by `remember`, not a `@Composable` state block: the
 * viewer needs to read `isEmpty` and `canUndo` from its *bar* while the canvas is a
 * different composable, and passing a `State<...>` triple around would make two
 * recomposition scopes agree by hand.
 *
 * The history itself is [MarkupHistory] — model code, unit-tested without Compose. This
 * class only mirrors its answers into Compose state so a toolbar button can react to
 * them; it never re-implements undo.
 */
@Stable
internal class MarkupSession(initial: List<MarkupStroke> = emptyList()) {

    private val history = MarkupHistory(initial)

    /** Snapshot of the committed strokes, replaced (never mutated) on every change. */
    var strokes by mutableStateOf(initial)
        private set
    var canUndo by mutableStateOf(history.canUndo)
        private set
    var canRedo by mutableStateOf(history.canRedo)
        private set

    /** The stroke under the finger, in pixels of the surface it is being drawn on. */
    val inProgress = mutableStateListOf<Offset>()

    var tool by mutableStateOf(MarkupTool.PEN)
    var colorArgb by mutableIntStateOf(MarkupInkColors.first())
    var widthFraction by mutableFloatStateOf(MarkupWidths[1])
    var canvasSize by mutableStateOf(IntSize.Zero)

    /** True once there is anything to save — the Save cell is not offered before that. */
    val isEmpty: Boolean get() = strokes.isEmpty() && inProgress.isEmpty()

    fun commitStroke() {
        val size = canvasSize
        if (inProgress.size < 2 || size.width <= 0 || size.height <= 0) {
            inProgress.clear()
            return
        }
        history.add(
            MarkupStroke(
                tool = tool,
                colorArgb = colorArgb,
                widthFraction = widthFraction,
                points = inProgress.map {
                    MarkupPoint(
                        x = (it.x / size.width).coerceIn(0f, 1f),
                        y = (it.y / size.height).coerceIn(0f, 1f)
                    )
                }
            )
        )
        inProgress.clear()
        sync()
    }

    fun undo() {
        history.undo()
        sync()
    }

    fun redo() {
        history.redo()
        sync()
    }

    fun clear() {
        history.clear()
        sync()
    }

    private fun sync() {
        strokes = history.strokes
        canUndo = history.canUndo
        canRedo = history.canRedo
    }
}

/**
 * The page with the ink drawn over it, and the drag that adds more ink.
 *
 * The preview paints through [MarkupRenderer] — the *same* renderer the bake uses — so
 * what the finger sees while drawing is what lands in the file. Points are normalised
 * on commit, so a stroke made here is in the right place on a 3200-px bake.
 *
 * @param image the page pixels to draw on. `null` shows the surface's own paper colour
 *   and accepts no input, which is what the first frame of a decode looks like.
 */
@Composable
internal fun MarkupInkSurface(
    session: MarkupSession,
    image: ImageBitmap?,
    modifier: Modifier = Modifier
) {
    // The *caller* sizes this box, and it must be sized to the page's aspect: a stroke
    // is normalised against the box it was drawn in, so a letterboxed box would put the
    // ink somewhere else on the bake.
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .onSizeChanged { session.canvasSize = it }
            .pointerInput(session.tool, session.colorArgb, session.widthFraction, image) {
                if (image == null) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        session.inProgress.clear()
                        session.inProgress.add(offset)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        session.inProgress.add(change.position)
                    },
                    onDragEnd = { session.commitStroke() },
                    onDragCancel = { session.commitStroke() }
                )
            }
    ) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            fun paint(strokes: List<MarkupStroke>) {
                drawIntoCanvas { canvas ->
                    MarkupRenderer.draw(
                        canvas = canvas.nativeCanvas,
                        strokes = strokes,
                        width = size.width.toInt(),
                        height = size.height.toInt()
                    )
                }
            }

            paint(session.strokes)

            val points = session.inProgress
            if (points.size >= 2) {
                paint(
                    listOf(
                        MarkupStroke(
                            tool = session.tool,
                            colorArgb = session.colorArgb,
                            widthFraction = session.widthFraction,
                            points = points.map {
                                MarkupPoint(
                                    x = if (size.width > 0) it.x / size.width else 0f,
                                    y = if (size.height > 0) it.y / size.height else 0f
                                )
                            }
                        )
                    )
                )
            }
        }
    }
}

/**
 * Stores the strokes and re-bakes the page, in that order.
 *
 * Re-reading the page after the write matters: it may have been rotated or re-cropped
 * while the ink was being drawn, and baking a stale copy would undo it.
 */
internal suspend fun persistMarkup(
    context: Context,
    repository: ScannerRepository,
    docId: String,
    pageId: String,
    strokes: List<MarkupStroke>
) {
    repository.updatePage(docId, pageId) { it.copy(markup = strokes) }
    val latest: ScannedPage = repository.getDocumentById(docId)
        ?.pages?.find { it.id == pageId } ?: throw IllegalStateException("Page no longer exists")
    val baked = PageRenderer.bake(context, latest)
        ?: throw IllegalStateException("Could not render annotation")
    repository.updatePage(docId, pageId) { baked }
}

/** A4's ratio, used for the first frame before the bitmap is decoded. */
internal const val A4_ASPECT = 0.707f

/** 90° turns of a decoded page, for the viewer's optimistic rotate. */
internal fun rotateBitmap(source: Bitmap, degrees: Int): Bitmap {
    if (degrees % 360 == 0) return source
    val matrix = android.graphics.Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

/** The Android bitmap behind an [ImageBitmap], for the rotate above. */
internal fun ImageBitmap.toRotatableBitmap(): Bitmap? = runCatching {
    asAndroidBitmap()
}.getOrNull()
