package com.example.docscanner.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.FilterType
import com.example.docscanner.model.QuadCorners
import com.example.docscanner.util.ImageProcessingUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropAndFilterScreen(
    docId: String,
    pageId: String,
    repository: ScannerRepository = ScannerRepository.instance,
    onDone: () -> Unit
) {
    val document = remember(docId) { repository.getDocumentById(docId) }
    val initialPage = remember(document, pageId) { document?.pages?.find { it.id == pageId } }

    var selectedFilter by remember { mutableStateOf(initialPage?.filterType ?: FilterType.MAGIC_COLOR) }
    var rotation by remember { mutableFloatStateOf(initialPage?.rotationDegrees ?: 0f) }
    var currentCorners by remember { mutableStateOf(initialPage?.corners ?: QuadCorners()) }
    var isCropMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isCropMode) "Adjust Perspective Borders" else "Enhance & Filter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            repository.updatePage(docId, pageId) { page ->
                                page.copy(
                                    filterType = selectedFilter,
                                    rotationDegrees = rotation,
                                    corners = currentCorners
                                )
                            }
                            onDone()
                        }
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    // Mode Toggle: Crop vs Filter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        FilterChip(
                            selected = !isCropMode,
                            onClick = { isCropMode = false },
                            leadingIcon = { Icon(Icons.Outlined.AutoFixHigh, contentDescription = null) },
                            label = { Text("Filters") },
                            shape = RoundedCornerShape(16.dp)
                        )
                        FilterChip(
                            selected = isCropMode,
                            onClick = { isCropMode = true },
                            leadingIcon = { Icon(Icons.Outlined.Crop, contentDescription = null) },
                            label = { Text("Borders & Crop") },
                            shape = RoundedCornerShape(16.dp)
                        )
                        IconButton(onClick = { rotation = (rotation - 90f) % 360f }) {
                            Icon(Icons.Default.RotateLeft, contentDescription = "Rotate Left")
                        }
                        IconButton(onClick = { rotation = (rotation + 90f) % 360f }) {
                            Icon(Icons.Default.RotateRight, contentDescription = "Rotate Right")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isCropMode) {
                        // Filter Selection Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterType.entries.forEach { filter ->
                                val isSelected = selectedFilter == filter
                                OutlinedButton(
                                    onClick = { selectedFilter = filter },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = if (isSelected) {
                                        ButtonDefaults.outlinedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    } else {
                                        ButtonDefaults.outlinedButtonColors()
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        filter.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    } else {
                        // Crop Quick-Reset Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { currentCorners = QuadCorners(0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f) }) {
                                Icon(Icons.Default.Fullscreen, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Full Page")
                            }
                            TextButton(onClick = { currentCorners = QuadCorners(0.08f, 0.08f, 0.92f, 0.08f, 0.92f, 0.92f, 0.08f, 0.92f) }) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Auto Detect")
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            // Interactive Document Canvas with live simulated page & perspective handles
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Document preview canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(isCropMode) {
                            if (!isCropMode) return@pointerInput
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val canvasWidth = size.width.toFloat()
                                val canvasHeight = size.height.toFloat()

                                val touchX = change.position.x / canvasWidth
                                val touchY = change.position.y / canvasHeight

                                // Find nearest corner to move
                                val dTL = distSq(touchX, touchY, currentCorners.topLeftX, currentCorners.topLeftY)
                                val dTR = distSq(touchX, touchY, currentCorners.topRightX, currentCorners.topRightY)
                                val dBR = distSq(touchX, touchY, currentCorners.bottomRightX, currentCorners.bottomRightY)
                                val dBL = distSq(touchX, touchY, currentCorners.bottomLeftX, currentCorners.bottomLeftY)

                                val minD = minOf(dTL, dTR, dBR, dBL)
                                if (minD < 0.08f) { // Within touch grab threshold
                                    currentCorners = when (minD) {
                                        dTL -> currentCorners.copy(topLeftX = touchX.coerceIn(0f, 0.5f), topLeftY = touchY.coerceIn(0f, 0.5f))
                                        dTR -> currentCorners.copy(topRightX = touchX.coerceIn(0.5f, 1f), topRightY = touchY.coerceIn(0f, 0.5f))
                                        dBR -> currentCorners.copy(bottomRightX = touchX.coerceIn(0.5f, 1f), bottomRightY = touchY.coerceIn(0.5f, 1f))
                                        else -> currentCorners.copy(bottomLeftX = touchX.coerceIn(0f, 0.5f), bottomLeftY = touchY.coerceIn(0.5f, 1f))
                                    }
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // Draw paper surface with filter color
                    val paperColor = when (selectedFilter) {
                        FilterType.ORIGINAL -> Color(0xFFF9F7F1)
                        FilterType.MAGIC_COLOR -> Color(0xFFFFFFFF)
                        FilterType.CLEAR_BW -> Color(0xFFFFFFFF)
                        FilterType.GRAYSCALE -> Color(0xFFEBEBEB)
                        FilterType.LIGHTEN -> Color(0xFFFFFFFF)
                    }

                    val inkColor = when (selectedFilter) {
                        FilterType.ORIGINAL -> Color(0xFF2C3E50)
                        FilterType.MAGIC_COLOR -> Color(0xFF1A237E)
                        FilterType.CLEAR_BW -> Color(0xFF000000)
                        FilterType.GRAYSCALE -> Color(0xFF333333)
                        FilterType.LIGHTEN -> Color(0xFF424242)
                    }

                    val tl = Offset(currentCorners.topLeftX * w, currentCorners.topLeftY * h)
                    val tr = Offset(currentCorners.topRightX * w, currentCorners.topRightY * h)
                    val br = Offset(currentCorners.bottomRightX * w, currentCorners.bottomRightY * h)
                    val bl = Offset(currentCorners.bottomLeftX * w, currentCorners.bottomLeftY * h)

                    // Draw perspective quadrilateral paper shape
                    val quadPath = Path().apply {
                        moveTo(tl.x, tl.y)
                        lineTo(tr.x, tr.y)
                        lineTo(br.x, br.y)
                        lineTo(bl.x, bl.y)
                        close()
                    }

                    // Shadow & paper fill
                    drawPath(quadPath, color = paperColor)

                    // Simulated document lines
                    val steps = 8
                    for (i in 1..steps) {
                        val fraction = i / (steps + 1).toFloat()
                        val lineStart = Offset(
                            tl.x + (bl.x - tl.x) * fraction + 20f,
                            tl.y + (bl.y - tl.y) * fraction
                        )
                        val lineEnd = Offset(
                            tr.x + (br.x - tr.x) * fraction - 20f,
                            tr.y + (br.y - tr.y) * fraction
                        )
                        drawLine(
                            color = inkColor.copy(alpha = if (i == 1) 0.9f else 0.4f),
                            start = lineStart,
                            end = lineEnd,
                            strokeWidth = if (i == 1) 6f else 3f
                        )
                    }

                    // In crop mode, draw interactive corner handles and border lines
                    if (isCropMode) {
                        drawPath(
                            path = quadPath,
                            color = Color(0xFF0061A4),
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // 4 Interactive corner drag handles
                        listOf(tl, tr, br, bl).forEach { cornerPt ->
                            drawCircle(
                                color = Color.White,
                                radius = 14.dp.toPx(),
                                center = cornerPt
                            )
                            drawCircle(
                                color = Color(0xFF0061A4),
                                radius = 10.dp.toPx(),
                                center = cornerPt
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun distSq(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x1 - x2
    val dy = y1 - y2
    return dx * dx + dy * dy
}
