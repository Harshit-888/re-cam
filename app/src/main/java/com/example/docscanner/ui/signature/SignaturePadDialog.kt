package com.example.docscanner.ui.signature

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignaturePadDialog(
    onDismiss: () -> Unit,
    onSignatureConfirmed: (String) -> Unit
) {
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var strokeWidth by remember { mutableFloatStateOf(6f) }
    val inkColors = listOf(0xFF000000, 0xFF0D47A1, 0xFFB71C1C)
    var selectedInkColor by remember { mutableStateOf(inkColors.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add Digital Signature", fontWeight = FontWeight.Bold)
                TextButton(onClick = {
                    strokes.clear()
                    currentStroke = emptyList()
                }) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Interactive Signature Drawing Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentStroke = listOf(offset)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentStroke = currentStroke + change.position
                                    },
                                    onDragEnd = {
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

                        // Draw finished strokes
                        strokes.forEach { strokePoints ->
                            if (strokePoints.size > 1) {
                                val path = Path().apply {
                                    moveTo(strokePoints.first().x, strokePoints.first().y)
                                    for (i in 1 until strokePoints.size) {
                                        lineTo(strokePoints[i].x, strokePoints[i].y)
                                    }
                                }
                                drawPath(path, color = Color(selectedInkColor), style = strokeStyle)
                            }
                        }

                        // Draw active stroke
                        if (currentStroke.size > 1) {
                            val path = Path().apply {
                                moveTo(currentStroke.first().x, currentStroke.first().y)
                                for (i in 1 until currentStroke.size) {
                                    lineTo(currentStroke[i].x, currentStroke[i].y)
                                }
                            }
                            drawPath(path, color = Color(selectedInkColor), style = strokeStyle)
                        }
                    }

                    if (strokes.isEmpty() && currentStroke.isEmpty()) {
                        Text(
                            text = "Sign here with your finger or stylus",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray,
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ink Color & Stroke Width Controls
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
                                        if (selectedInkColor == colorVal) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        CircleShape
                                    )
                                    .clickable { selectedInkColor = colorVal }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Stroke Thickness: ${strokeWidth.toInt()} pt", style = MaterialTheme.typography.labelSmall)
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
                onClick = {
                    onSignatureConfirmed("signature_${System.currentTimeMillis()}")
                },
                enabled = strokes.isNotEmpty()
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
