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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.ImageBitmap
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
import com.example.docscanner.util.DocumentFilters
import com.example.docscanner.util.ImageProcessingUtils
import com.example.docscanner.util.ImageStore
import com.example.docscanner.util.PageRenderer
import com.example.docscanner.util.DocumentDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/** Smallest allowed gap between opposing edges, in normalised units. */
private const val MIN_GAP = 0.02f

/**
 * Perspective correction + enhancement editor.
 *
 * Two modes, driven by [reviewMode]:
 *
 * - **Review mode** (`reviewMode = true`) — the post-capture step the original app
 *   had. Entered straight from the camera after *Done*, it auto-detects the
 *   document borders on the first frame, lets the user fine-tune the four
 *   corners and pick a filter, then walks through every captured page with
 *   *Save & Next* before landing on the document.
 * - **Single-page mode** — reached from the document screen or the viewer to
 *   re-edit one page; *Save* returns where you came from.
 *
 * Edits are baked non-destructively by [PageRenderer] from the pristine capture.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropAndFilterScreen(
    docId: String,
    pageId: String,
    repository: ScannerRepository,
    onDone: () -> Unit,
    reviewMode: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val documents by repository.documents.collectAsState()
    val document = remember(documents, docId) { documents.find { it.id == docId } }
    val pages = document?.pages ?: emptyList()

    // The editor walks the document itself in review mode instead of pushing a
    // new navigation entry per page.
    var activePageId by remember(pageId) { mutableStateOf(pageId) }
    val activeIndex = pages.indexOfFirst { it.id == activePageId }
    // Guard against activeIndex == -1 (page not found yet): activeIndex + 1 would
    // otherwise resolve to the FIRST page and the editor would loop forever.
    val nextPageId = if (reviewMode && activeIndex >= 0) {
        pages.getOrNull(activeIndex + 1)?.id
    } else {
        null
    }

    var selectedFilter by remember { mutableStateOf(FilterType.MAGIC_COLOR) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var corners by remember { mutableStateOf(QuadCorners.FULL_FRAME) }
    // In review mode the borders are the point of the screen, so open on them.
    var isCropMode by remember { mutableStateOf(reviewMode) }
    var isSaving by remember { mutableStateOf(false) }
    var isAutoDetecting by remember { mutableStateOf(false) }
    var detectionFailed by remember { mutableStateOf(false) }
    var loadedPageId by remember { mutableStateOf<String?>(null) }

    // Unfiltered, downscaled source used for both detection and previewing.
    var previewSource by remember { mutableStateOf<Bitmap?>(null) }
    // The filtered preview the user actually sees (real filter, not a ColorMatrix).
    var previewBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var previewAspect by remember { mutableFloatStateOf(0.707f) }
    var isFiltering by remember { mutableStateOf(false) }

    // Load (and, for a fresh capture, auto-detect) the active page.
    LaunchedEffect(activePageId) {
        val current = repository.getDocumentById(docId)
            ?.pages?.find { it.id == activePageId } ?: return@LaunchedEffect

        selectedFilter = current.filterType
        rotation = current.rotationDegrees
        corners = current.corners

        val decoded = withContext(Dispatchers.IO) {
            ImageStore.loadBitmap(
                context,
                current.originalImageUri ?: current.imageUri,
                1400
            )
        }
        if (decoded == null) {
            loadedPageId = activePageId
            return@LaunchedEffect
        }

        previewSource = decoded
        previewAspect = decoded.width.toFloat() / decoded.height.toFloat()

        // Auto-detect only for pages the user has not cropped yet, and never
        // override saved corners.
        if (current.corners.isFullFrame) {
            isAutoDetecting = true
            val detection = withContext(Dispatchers.Default) { DocumentDetector.detect(decoded) }
            isAutoDetecting = false
            corners = detection.corners
            // Tell the user when the quad is a frame-edge default rather than a
            // real edge fit, instead of silently showing nothing.
            detectionFailed = !detection.confident
        } else {
            detectionFailed = false
        }

        loadedPageId = activePageId

        // Review mode leads with the borders; a single-page edit leads with the
        // photo so re-filtering does not require dismissing the handles.
        isCropMode = reviewMode
    }

    // Render the selected filter for real, at preview resolution. Using the same
    // DocumentFilters pipeline as the bake means what you see is what gets saved.
    LaunchedEffect(previewSource, selectedFilter) {
        val source = previewSource
        if (source == null) {
            previewBitmap = null
            return@LaunchedEffect
        }
        if (selectedFilter == FilterType.ORIGINAL) {
            previewBitmap = source.asImageBitmap()
            return@LaunchedEffect
        }
        isFiltering = true
        // runCatching: an OutOfMemoryError mid-filter must not strand the UI in a
        // permanent "filtering…" state; fall back to the unfiltered page.
        val filtered = withContext(Dispatchers.Default) {
            runCatching { DocumentFilters.applyPreview(source, selectedFilter) }.getOrNull()
        }
        previewBitmap = (filtered ?: source).asImageBitmap()
        isFiltering = false
    }

    fun saveAndContinue() {
        if (isSaving) return
        val current = repository.getDocumentById(docId)
            ?.pages?.find { it.id == activePageId }
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
                repository.updatePage(docId, activePageId) { baked }
            }
            isSaving = false
            if (nextPageId != null) {
                activePageId = nextPageId
            } else {
                onDone()
            }
        }
    }

    val pageLabel = when {
        pages.isEmpty() -> "Page"
        activeIndex >= 0 -> "Page ${activeIndex + 1} of ${pages.size}"
        else -> "Page"
    }
    val saveLabel = when {
        nextPageId != null -> "Save & Next"
        reviewMode -> "Finish"
        else -> "Save"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (isCropMode) "Adjust borders" else "Enhance & filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            pageLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = if (reviewMode) "Skip review" else "Back"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { saveAndContinue() }, enabled = !isSaving) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(saveLabel, fontWeight = FontWeight.Bold)
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
                        .padding(vertical = 10.dp, horizontal = 16.dp)
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
                        IconButton(
                            onClick = { rotation = ImageProcessingUtils.normalizeRotation(rotation - 90f) }
                        ) {
                            Icon(Icons.Default.RotateLeft, contentDescription = "Rotate left")
                        }
                        IconButton(
                            onClick = { rotation = ImageProcessingUtils.normalizeRotation(rotation + 90f) }
                        ) {
                            Icon(Icons.Default.RotateRight, contentDescription = "Rotate right")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!isCropMode) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(FilterType.entries.toList(), key = { it.name }) { filter ->
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter },
                                    label = { Text(filter.label) },
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { corners = QuadCorners.FULL_FRAME }) {
                                Icon(Icons.Default.Fullscreen, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Full page")
                            }
                            TextButton(
                                onClick = {
                                    val source = previewSource ?: return@TextButton
                                    scope.launch {
                                        isAutoDetecting = true
                                        val detection = withContext(Dispatchers.Default) {
                                            DocumentDetector.detect(source)
                                        }
                                        corners = detection.corners
                                        detectionFailed = !detection.confident
                                        isAutoDetecting = false
                                    }
                                },
                                enabled = previewSource != null && !isAutoDetecting
                            ) {
                                if (isAutoDetecting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Auto detect")
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
            val pageReady = loadedPageId == activePageId
            if (isFiltering && image != null) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(2.dp)
                )
            }
            // Tell the user when auto-detection could not find a real page edge, so
            // a full-frame quad never looks like a silent failure.
            if (isCropMode && detectionFailed && image != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                    ) {
                        Text(
                            "Borders not detected — drag the corners",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        IconButton(
                            onClick = { detectionFailed = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            if (image == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (pageReady) "This page's image is missing." else "Loading page…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val swapAxes = rotation.roundToInt() % 180 != 0
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
                            contentDescription = "Page preview",
                            contentScale = ContentScale.FillBounds,
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
                                val quad = Path().apply {
                                    moveTo(points[0].x, points[0].y)
                                    lineTo(points[1].x, points[1].y)
                                    lineTo(points[2].x, points[2].y)
                                    lineTo(points[3].x, points[3].y)
                                    close()
                                }
                                // Dim outside the document, outline it, mark corners.
                                drawPath(quad, color = Color(0xFF4FD8EB).copy(alpha = 0.10f))
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
