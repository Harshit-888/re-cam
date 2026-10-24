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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add Digital Signature", fontWeight = FontWeight.Bold)
                TextButton(
                    onClick = {
                        strokes.clear()
                        currentStroke = emptyList()
                    }
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
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
                            text = "Sign here with your finger or stylus",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ink Color:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        inkColors.forEach { colorVal ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorVal))
                                    .border(
                                        if (selectedInkColor == colorVal) 3.dp else 1.dp,
                                        if (selectedInkColor == colorVal) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.LightGray
                                        },
                                        CircleShape
                                    )
                                    .clickable { selectedInkColor = colorVal }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Stroke Thickness: ${strokeWidth.toInt()} pt",
                    style = MaterialTheme.typography.labelSmall
                )
                Slider(
                    value = strokeWidth,
                    onValueChange = { strokeWidth = it },
                    valueRange = 3f..14f,
                    steps = 5
                )
            }
        },
        confirmButton = {
            Button(
                enabled = strokes.isNotEmpty() && !isSaving,
                onClick = {
                    val bitmap = rasterise()
                    if (bitmap == null) {
                        Toast.makeText(context, "Signature area not ready", Toast.LENGTH_SHORT).show()
                        return@Button
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
                            Toast.makeText(context, "Could not save signature", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Done, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
