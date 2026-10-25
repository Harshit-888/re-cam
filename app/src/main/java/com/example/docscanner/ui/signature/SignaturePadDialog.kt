package com.example.docscanner.ui.signature

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.widget.Toast
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path as ComposePath
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.docscanner.theme.DocShapes
import com.example.docscanner.ui.common.PillButton
import com.example.docscanner.ui.common.TonalIconButton
import com.example.docscanner.ui.common.TonalPillButton
import com.example.docscanner.util.ImageStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Digital signature capture.
 *
 * Strokes are rendered to a **transparent PNG** in app storage and the absolute
 * path is handed back to the caller, which composites it onto the page via
 * [com.example.docscanner.util.PageRenderer]. The previous revision returned an
 * opaque `"signature_<timestamp>"` token that nothing could ever draw.
 *
 * Presented as a near-full-width dialog rather than a bottom sheet: signing wants
 * the largest comfortable writing area, and a sheet's drag-to-dismiss competes
 * with the pen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignaturePadDialog(
    onDismiss: () -> Unit,
    onSignatureConfirmed: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var strokeWidth by remember { mutableFloatStateOf(6f) }
    val inkColors = listOf(0xFF000000, 0xFF0D47A1, 0xFFB71C1C)
    var selectedInkColor by remember { mutableStateOf(inkColors.first()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var isSaving by remember { mutableStateOf(false) }

    fun rasterise(): Bitmap? {
        val size = canvasSize
        if (size.width <= 0 || size.height <= 0) return null
        val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = selectedInkColor.toInt()
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            this.strokeWidth = strokeWidth
        }

        strokes.forEach { points ->
            if (points.size > 1) {
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
                canvas.drawPath(path, paint)
            }
        }
        return bitmap
    }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = DocShapes.dialog,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // ---- header -------------------------------------------------
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "SIGNATURE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Add signature",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    TonalIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Close",
                        onClick = { if (!isSaving) onDismiss() }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ---- writing surface ---------------------------------------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Shrinks on short screens instead of pushing the Apply
                        // button out of the dialog.
                        .heightIn(min = 150.dp, max = 210.dp)
                        .height(210.dp)
                        .clip(DocShapes.cardSmall)
                        .background(Color.White)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            DocShapes.cardSmall
                        )
                ) {
                    ComposeCanvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { canvasSize = it }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset -> currentStroke = listOf(offset) },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentStroke = currentStroke + change.position
                                    },
                                    onDragEnd = {
                                        if (currentStroke.isNotEmpty()) {
                                            strokes.add(currentStroke)
                                            currentStroke = emptyList()
                                        }
                                    },
                                    onDragCancel = {
                                        if (currentStroke.isNotEmpty()) {
                                            strokes.add(currentStroke)
                                            currentStroke = emptyList()
                                        }
                                    }
                                )
                            }
                    ) {
                        val strokeStyle = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )

                        // A faint baseline: people sign on a line, and without one
                        // signatures drift to the top-left corner of the pad.
                        drawLine(
                            color = Color(0x1F000000),
                            start = Offset(size.width * 0.08f, size.height * 0.74f),
                            end = Offset(size.width * 0.92f, size.height * 0.74f),
                            strokeWidth = 2f
                        )

                        fun drawSmooth(points: List<Offset>) {
                            if (points.size < 2) return
                            val path = ComposePath().apply {
                                moveTo(points.first().x, points.first().y)
                                // Quadratic smoothing through segment midpoints.
                                for (i in 1 until points.size) {
                                    val previous = points[i - 1]
                                    val current = points[i]
                                    val midX = (previous.x + current.x) / 2f
                                    val midY = (previous.y + current.y) / 2f
                                    quadraticBezierTo(previous.x, previous.y, midX, midY)
                                }
                                val last = points.last()
                                lineTo(last.x, last.y)
                            }
                            drawPath(path, color = Color(selectedInkColor), style = strokeStyle)
                        }

                        strokes.forEach { drawSmooth(it) }
                        drawSmooth(currentStroke)
                    }

                    if (strokes.isEmpty() && currentStroke.isEmpty()) {
                        Text(
                            text = "Sign here",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0x33000000),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // ---- ink + thickness ---------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Ink",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.width(52.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        inkColors.forEach { colorVal ->
                            val selected = selectedInkColor == colorVal
                            InkSwatch(
                                color = Color(colorVal),
                                selected = selected,
                                onClick = { selectedInkColor = colorVal }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Weight",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.width(52.dp)
                    )
                    Slider(
                        value = strokeWidth,
                        onValueChange = { strokeWidth = it },
                        valueRange = 3f..14f,
                        steps = 5,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${strokeWidth.toInt()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(22.dp)
                    )
                }

                Spacer(Modifier.height(18.dp))

                // ---- actions -----------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TonalPillButton(
                        text = "Clear",
                        icon = Icons.Default.DeleteOutline,
                        enabled = !isSaving,
                        onClick = {
                            strokes.clear()
                            currentStroke = emptyList()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PillButton(
                        text = if (isSaving) "Saving…" else "Apply",
                        icon = Icons.Default.Check,
                        enabled = strokes.isNotEmpty() && !isSaving,
                        onClick = {
                            val bitmap = rasterise()
                            if (bitmap == null) {
                                Toast.makeText(
                                    context,
                                    "Signature area not ready",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@PillButton
                            }
                            isSaving = true
                            scope.launch {
                                val path = withContext(Dispatchers.IO) {
                                    ImageStore.saveSignature(context, bitmap)
                                }
                                bitmap.recycle()
                                isSaving = false
                                if (path != null) {
                                    onSignatureConfirmed(path)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Could not save signature",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** One ink colour: a filled disc with a ring when chosen. */
@Composable
private fun InkSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 26.dp else 24.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    shape = CircleShape
                )
        )
    }
}
