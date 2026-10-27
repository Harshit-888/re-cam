package com.example.docscanner.ui.editor

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.docscanner.model.MarkupHistory
import com.example.docscanner.model.MarkupPoint
import com.example.docscanner.model.MarkupStroke
import com.example.docscanner.model.MarkupTool
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.theme.DocShapes
import com.example.docscanner.util.ImageStore
import com.example.docscanner.util.MarkupRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Draws on a page: pen, highlighter and whiteout, with undo and redo.
 *
 * The strokes come back to the caller rather than being painted into a file here. The
 * page keeps them as data (`ScannedPage.markup`) and [PageRenderer] replays them on
 * every bake, which is what makes annotation survive a filter change.
 *
 * The canvas is drawn at a modest preview size but every point is stored **normalised**,
 * so a stroke made here lands identically on the 3200-px bake.
 */
@Composable
fun MarkupDialog(
    page: ScannedPage,
    onDismiss: () -> Unit,
    onSave: (List<MarkupStroke>) -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember(page.id) { mutableStateOf<Bitmap?>(null) }
    var failed by remember(page.id) { mutableStateOf(false) }

    LaunchedEffect(page.id, page.imageUri) {
        failed = false
        bitmap = withContext(Dispatchers.IO) {
            ImageStore.loadBitmap(context, page.imageUri, PREVIEW_DIMENSION)
        }
        failed = bitmap == null
    }

    val history = remember(page.id) { MarkupHistory(page.markup) }
    // Bumped on every history change so the canvas and the button states recompose.
    // The history itself is a plain class, deliberately: it is model code and is
    // unit-tested without Compose.
    var revision by remember(page.id) { mutableStateOf(0) }
    val current = remember(page.id) { mutableStateListOf<Offset>() }

    var tool by remember(page.id) { mutableStateOf(MarkupTool.PEN) }
    var colorArgb by remember(page.id) { mutableStateOf(INK_COLORS.first()) }
    var widthFraction by remember(page.id) { mutableStateOf(WIDTHS[1]) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    // `MarkupHistory` is plain model code (unit-tested without Compose), so it cannot
    // be observed directly. These re-read it whenever `revision` moves — the same
    // approach the signature pad takes with its stroke list.
    val strokes = remember(revision) { history.strokes }
    val canUndo = remember(revision) { history.canUndo }
    val canRedo = remember(revision) { history.canRedo }

    fun commitStroke() {
        if (current.size < 2 || canvasSize.width <= 0 || canvasSize.height <= 0) {
            current.clear()
            return
        }
        history.add(
            MarkupStroke(
                tool = tool,
                colorArgb = colorArgb,
                widthFraction = widthFraction,
                points = current.map {
                    MarkupPoint(
                        x = (it.x / canvasSize.width).coerceIn(0f, 1f),
                        y = (it.y / canvasSize.height).coerceIn(0f, 1f)
                    )
                }
            )
        )
        current.clear()
        revision++
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = DocShapes.dialog,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Annotate",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            history.undo()
                            revision++
                        },
                        enabled = canUndo
                    ) { Icon(Icons.Default.Undo, contentDescription = "Undo", modifier = Modifier.size(20.dp)) }
                    TextButton(
                        onClick = {
                            history.redo()
                            revision++
                        },
                        enabled = canRedo
                    ) { Icon(Icons.Default.Redo, contentDescription = "Redo", modifier = Modifier.size(20.dp)) }
                }

                Spacer(Modifier.height(8.dp))

                // The page, with the ink drawn over it at preview resolution.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(
                            bitmap?.let { it.width.toFloat() / it.height.toFloat() } ?: (0.707f)
                        )
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, DocShapes.cardSmall)
                        .onSizeChanged { canvasSize = it }
                        .pointerInput(tool, colorArgb, widthFraction) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    current.clear()
                                    current.add(offset)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    current.add(change.position)
                                },
                                onDragEnd = { commitStroke() },
                                onDragCancel = { commitStroke() }
                            )
                        }
                ) {
                    val image = bitmap
                    if (image == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (failed) {
                                Text(
                                    "Could not open this page",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            }
                        }
                    } else {
                        Image(
                            bitmap = image.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            fun paint(strokes: List<MarkupStroke>) {
                                // The same renderer, the same maths, the same curve as
                                // the bake: this is the preview's whole guarantee.
                                drawIntoCanvas { canvas ->
                                    MarkupRenderer.draw(
                                        canvas = canvas.nativeCanvas,
                                        strokes = strokes,
                                        width = size.width.toInt(),
                                        height = size.height.toInt()
                                    )
                                }
                            }

                            paint(strokes)

                            if (current.size >= 2) {
                                paint(
                                    listOf(
                                        MarkupStroke(
                                            tool = tool,
                                            colorArgb = colorArgb,
                                            widthFraction = widthFraction,
                                            points = current.map {
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

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MarkupTool.entries.forEach { option ->
                        FilterChip(
                            selected = tool == option,
                            onClick = { tool = option },
                            label = { Text(option.label, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }

                if (tool != MarkupTool.WHITEOUT) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        INK_COLORS.forEach { ink ->
                            val selectedInk = ink == colorArgb
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(ink))
                                    .border(
                                        width = if (selectedInk) 3.dp else 1.dp,
                                        color = if (selectedInk) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable { colorArgb = ink }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WIDTHS.forEachIndexed { index, fraction ->
                        FilterChip(
                            selected = widthFraction == fraction,
                            onClick = { widthFraction = fraction },
                            label = {
                                Text(
                                    listOf("Thin", "Medium", "Bold")[index],
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    TextButton(
                        onClick = {
                            history.clear()
                            revision++
                        },
                        enabled = canUndo
                    ) { Text("Clear") }
                }

                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { onSave(history.strokes) }) { Text("Done") }
                }
            }
        }
    }
}

/** Preview decode size: the page is shown a third of a phone wide at most. */
private const val PREVIEW_DIMENSION = 1400

private val INK_COLORS = listOf(
    0xFF111111.toInt(),
    0xFFC62828.toInt(),
    0xFF1565C0.toInt(),
    0xFFF9A825.toInt()
)

/** Fractions of the page's short side. */
private val WIDTHS = listOf(0.004f, 0.008f, 0.016f)
