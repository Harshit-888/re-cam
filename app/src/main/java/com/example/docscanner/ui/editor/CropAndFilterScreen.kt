package com.example.docscanner.ui.editor

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.FilterType
import com.example.docscanner.model.QuadCorners
import com.example.docscanner.util.ImageProcessingUtils
import com.example.docscanner.util.ImageStore
import com.example.docscanner.util.PageRenderer
import com.example.docscanner.util.PerspectiveWarp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Perspective correction + enhancement editor.
 *
 * Shows the **real captured page**, previews the active `ColorMatrix` filter
 * live over those pixels, and on *Save* bakes warp → rotation → filter into a new
 * JPEG through [PageRenderer] (non-destructively, from the pristine original).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropAndFilterScreen(
    docId: String,
    pageId: String,
    repository: ScannerRepository,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val page = remember(docId, pageId) {
        repository.getDocumentById(docId)?.pages?.find { it.id == pageId }
    }

    var selectedFilter by remember { mutableStateOf(page?.filterType ?: FilterType.MAGIC_COLOR) }
    var rotation by remember { mutableFloatStateOf(page?.rotationDegrees ?: 0f) }
    var corners by remember { mutableStateOf(page?.corners ?: QuadCorners.FULL_FRAME) }
    var isCropMode by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    var previewBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var previewAspect by remember { mutableFloatStateOf(0.707f) }
    var detectSource by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(docId, pageId) {
        val current = repository.getDocumentById(docId)?.pages?.find { it.id == pageId }
            ?: return@LaunchedEffect
        val decoded = withContext(Dispatchers.IO) {
            ImageStore.loadBitmap(
                context,
                current.originalImageUri ?: current.imageUri,
                1600
            )
        }
        if (decoded != null) {
            detectSource = decoded
            previewBitmap = decoded.asImageBitmap()
            previewAspect = decoded.width.toFloat() / decoded.height.toFloat()
        }
    }

    fun save() {
        if (isSaving) return
        val current = repository.getDocumentById(docId)?.pages?.find { it.id == pageId }
        if (current == null) {
            onDone()
            return
        }
        isSaving = true
        scope.launch {
            val baked = PageRenderer.bake(
                context,
                current.copy(
                    filterType = selectedFilter,
                    rotationDegrees = rotation,
                    corners = corners
                )
            )
            if (baked != null) {
                repository.updatePage(docId, pageId) { baked }
            }
            isSaving = false
            onDone()
        }
    }

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
                    TextButton(onClick = { save() }, enabled = !isSaving) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = !isCropMode,
                            onClick = { isCropMode = false },
                            leadingIcon = { Icon(Icons.Outlined.FilterAlt, contentDescription = null) },
                            label = { Text("Filters") },
                            shape = RoundedCornerShape(16.dp)
                        )
                        FilterChip(
                            selected = isCropMode,
                            onClick = { isCropMode = true },
                            leadingIcon = { Icon(Icons.Outlined.Crop, contentDescription = null) },
                            label = { Text("Borders") },
                            shape = RoundedCornerShape(16.dp)
                        )
                        IconButton(onClick = {
                            rotation = ImageProcessingUtils.normalizeRotation(rotation - 90f)
                        }) {
                            Icon(Icons.Default.RotateLeft, contentDescription = "Rotate left")
                        }
                        IconButton(onClick = {
                            rotation = ImageProcessingUtils.normalizeRotation(rotation + 90f)
                        }) {
                            Icon(Icons.Default.RotateRight, contentDescription = "Rotate right")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isCropMode) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(FilterType.entries.toList(), key = { it.name }) { filter ->
                                val isSelected = selectedFilter == filter
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedFilter = filter },
                                    label = { Text(filter.label) },
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { corners = QuadCorners.FULL_FRAME }) {
                                Icon(Icons.Default.Fullscreen, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Full Page")
                            }
                            TextButton(
                                onClick = {
                                    val source = detectSource
                                    if (source != null) {
                                        scope.launch {
                                            corners = withContext(Dispatchers.Default) {
                                                PerspectiveWarp.detectQuad(source)
                                            }
                                        }
                                    }
                                },
                                enabled = detectSource != null
                            ) {
                                Icon(Icons.Outlined.Crop, contentDescription = null)
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
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            contentAlignment = Alignment.Center
        ) {
            val image = previewBitmap
            if (image == null) {
                CircularProgressIndicator()
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val swapAxes = rotation.roundToInt() % 180 != 0
                    // Fit the *rotated* bounding box inside the available space.
                    val visualAspect = if (swapAxes) 1f / previewAspect else previewAspect
                    val availableAspect = maxWidth / maxHeight
                    val visualWidth = if (availableAspect > visualAspect) maxHeight * visualAspect else maxWidth
                    val visualHeight = if (availableAspect > visualAspect) maxHeight else maxWidth / visualAspect
                    val frameWidth = if (swapAxes) visualHeight else visualWidth
                    val frameHeight = if (swapAxes) visualWidth else visualHeight

                    Box(
                        modifier = Modifier
                            .size(frameWidth, frameHeight)
                            .align(Alignment.Center)
                            .graphicsLayer { rotationZ = rotation }
                    ) {
                        Image(
                            bitmap = image,
                            contentDescription = "Scanned page preview",
                            contentScale = ContentScale.FillBounds,
                            colorFilter = ImageProcessingUtils.getComposeColorFilter(selectedFilter).takeIf {
                                selectedFilter != FilterType.ORIGINAL
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        var grabbedCorner by remember { mutableStateOf<Int?>(null) }

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(isCropMode) {
                                    if (!isCropMode) return@pointerInput
                                    detectDragGestures(
                                        onDragStart = { start ->
                                            val nx = start.x / size.width
                                            val ny = start.y / size.height
                                            val distances = listOf(
                                                distSq(nx, ny, corners.topLeftX, corners.topLeftY),
                                                distSq(nx, ny, corners.topRightX, corners.topRightY),
                                                distSq(nx, ny, corners.bottomRightX, corners.bottomRightY),
                                                distSq(nx, ny, corners.bottomLeftX, corners.bottomLeftY)
                                            )
                                            grabbedCorner = distances
                                                .withIndex()
                                                .minByOrNull { it.value }
                                                ?.takeIf { it.value < 0.05f }
                                                ?.index
                                        },
                                        onDragEnd = { grabbedCorner = null },
                                        onDragCancel = { grabbedCorner = null },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val nx = (change.position.x / size.width).coerceIn(0f, 1f)
                                            val ny = (change.position.y / size.height).coerceIn(0f, 1f)
                                            corners = when (grabbedCorner) {
                                                0 -> corners.copy(
                                                    topLeftX = clamp(nx, 0f, corners.topRightX - MIN_GAP),
                                                    topLeftY = clamp(ny, 0f, corners.bottomLeftY - MIN_GAP)
                                                )
                                                1 -> corners.copy(
                                                    topRightX = clamp(nx, corners.topLeftX + MIN_GAP, 1f),
                                                    topRightY = clamp(ny, 0f, corners.bottomRightY - MIN_GAP)
                                                )
                                                2 -> corners.copy(
                                                    bottomRightX = clamp(nx, corners.bottomLeftX + MIN_GAP, 1f),
                                                    bottomRightY = clamp(ny, corners.topRightY + MIN_GAP, 1f)
                                                )
                                                3 -> corners.copy(
                                                    bottomLeftX = clamp(nx, 0f, corners.bottomRightX - MIN_GAP),
                                                    bottomLeftY = clamp(ny, corners.topLeftY + MIN_GAP, 1f)
                                                )
                                                else -> corners
                                            }
                                        }
                                    )
                                }
                        ) {
                            val w = size.width
                            val h = size.height
                            val points = listOf(
                                Offset(corners.topLeftX * w, corners.topLeftY * h),
                                Offset(corners.topRightX * w, corners.topRightY * h),
                                Offset(corners.bottomRightX * w, corners.bottomRightY * h),
                                Offset(corners.bottomLeftX * w, corners.bottomLeftY * h)
                            )

                            if (isCropMode) {
                                // Dim everything outside the quad.
                                val quad = Path().apply {
                                    moveTo(points[0].x, points[0].y)
                                    lineTo(points[1].x, points[1].y)
                                    lineTo(points[2].x, points[2].y)
                                    lineTo(points[3].x, points[3].y)
                                    close()
                                }
                                drawPath(quad, color = Color.White.copy(alpha = 0.08f))
                                drawPath(
                                    path = quad,
                                    color = Color(0xFF4FD8EB),
                                    style = Stroke(width = 3.dp.toPx())
                                )

                                points.forEachIndexed { index, point ->
                                    val isActive = grabbedCorner == index
                                    drawCircle(
                                        color = Color.White,
                                        radius = if (isActive) 13.dp.toPx() else 11.dp.toPx(),
                                        center = point
                                    )
                                    drawCircle(
                                        color = Color(0xFF0061A4),
                                        radius = if (isActive) 9.dp.toPx() else 7.dp.toPx(),
                                        center = point
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Smallest allowed gap between opposing edges, in normalised units. */
private const val MIN_GAP = 0.02f

/**
 * `coerceIn` throws when `min > max`. Dragging one corner past its neighbour used
 * to produce exactly that and crash the editor; this orders the bounds first.
 */
private fun clamp(value: Float, first: Float, second: Float): Float {
    val low = minOf(first, second)
    val high = maxOf(first, second)
    return value.coerceIn(low, high)
}

private fun distSq(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x1 - x2
    val dy = y1 - y2
    return dx * dx + dy * dy
}
