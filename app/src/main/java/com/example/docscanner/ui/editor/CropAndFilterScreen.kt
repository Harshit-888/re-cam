package com.example.docscanner.ui.editor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.draw.clipToBounds
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.FilterType
import com.example.docscanner.model.QuadCorners
import com.example.docscanner.util.DocumentFilters
import com.example.docscanner.util.ImageProcessingUtils
import com.example.docscanner.util.ImageStore
import com.example.docscanner.util.PageRenderer
import com.example.docscanner.util.PerspectiveWarp
import com.example.docscanner.util.DocumentDetector
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * How long the filter strip must rest before a full-resolution preview render.
 *
 * Swiping across the chips changes the selection every frame; each render is a
 * 2000 px pass and the previous bitmap stays on screen meanwhile, so the delay
 * only prevents wasted work.
 */
private const val PREVIEW_SETTLE_MS = 140L

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

    // Preview zoom + pan for precise corner placement. `zoom`/`pan` are applied on
    // a layer OUTSIDE the rotation layer, so panning always moves the content the
    // way the finger moved (a rotation-inner pan would drag diagonally at 90°).
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var grabbedCorner by remember { mutableStateOf<Int?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var isAutoDetecting by remember { mutableStateOf(false) }
    var detectionFailed by remember { mutableStateOf(false) }
    var loadedPageId by remember { mutableStateOf<String?>(null) }
    // True once the user has dragged a corner on this page. Auto-detection on a
    // Borders tap must never throw that work away.
    var cornersEdited by remember { mutableStateOf(false) }

    // Unfiltered, downscaled source used for both detection and previewing.
    var previewSource by remember { mutableStateOf<Bitmap?>(null) }
    // The filtered preview the user actually sees (real filter, not a ColorMatrix).
    var previewBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var previewAspect by remember { mutableFloatStateOf(0.707f) }
    var isFiltering by remember { mutableStateOf(false) }
    var previewFailed by remember { mutableStateOf(false) }
    // Longest edge the bake will produce; the preview is rendered at this size so
    // that what the user judges is what gets saved.
    var bakeDimension by remember { mutableIntStateOf(DocumentFilters.MAX_PROCESS_DIMENSION) }

    // Load (and, for a fresh capture, auto-detect) the active page.
    LaunchedEffect(activePageId) {
        val current = repository.getDocumentById(docId)
            ?.pages?.find { it.id == activePageId } ?: return@LaunchedEffect

        selectedFilter = current.filterType
        rotation = current.rotationDegrees
        corners = current.corners
        zoom = 1f
        pan = Offset.Zero

        // Decode at the resolution the page will actually be *saved* at, not at a
        // thumbnail size: a 1400 px source filtered for the preview can never show
        // what a 2000 px bake looks like, which is exactly why the preview read as
        // soft next to the PDF.
        val bakeEdge = PageRenderer.bakeDimension(repository.currentSettings().imageQuality)
        val decoded = withContext(Dispatchers.IO) {
            ImageStore.loadBitmap(
                context,
                current.originalImageUri ?: current.imageUri,
                bakeEdge
            )
        }
        if (decoded == null) {
            loadedPageId = activePageId
            return@LaunchedEffect
        }

        bakeDimension = bakeEdge
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

    // Render the selected filter for real, at the *bake* resolution, on the page
    // as it will be saved — i.e. after the perspective correction. Same
    // warp-then-filter order as PageRenderer, at the same size, so the preview and
    // the saved PDF are the same image.
    LaunchedEffect(previewSource, selectedFilter, corners, isCropMode, bakeDimension) {
        val source = previewSource
        if (source == null) {
            previewBitmap = null
            return@LaunchedEffect
        }
        // While the corners are being placed the user needs to see the whole
        // frame, unwarped, to aim at. The corrected result is shown as soon as
        // the Filters tab is selected.
        if (isCropMode) {
            previewBitmap = source.asImageBitmap()
            previewAspect = source.width.toFloat() / source.height.toFloat()
            previewFailed = false
            return@LaunchedEffect
        }
        isFiltering = true
        previewFailed = false

        // Settle before paying for a full-size render: swiping along the filter
        // strip changes `selectedFilter` every frame and each render is ~2000 px.
        delay(PREVIEW_SETTLE_MS)

        // runCatching: an OutOfMemoryError mid-filter must not strand the UI in a
        // permanent "filtering…" state.
        val jpegQuality = repository.currentSettings().imageQuality.jpegQuality
        val rendered = withContext(Dispatchers.Default) {
            runCatching {
                val warped = PerspectiveWarp.warp(source, corners)
                var filtered = DocumentFilters.apply(warped, selectedFilter, bakeDimension)
                // DocumentFilters returns its input unchanged for ORIGINAL and on
                // any failure, so only recycle intermediates that are neither the
                // source nor the result. Recycling `warped` unconditionally was a
                // real bug: it handed the UI a recycled bitmap and blanked the
                // preview.
                if (warped !== source && warped !== filtered) warped.recycle()

                // The save writes a JPEG at this quality, and a JPEG is lossy —
                // at Medium (88) the difference is visible in small text. Showing
                // the pre-encode pixels would let a softer setting look better
                // than it saves, which is exactly the judgement the preview exists
                // for. Round-trip through the same encoder so the screen and the
                // file are the same image, and recycle the unencoded copy.
                val encoded = runCatching {
                    ByteArrayOutputStream().use { stream ->
                        if (filtered.compress(Bitmap.CompressFormat.JPEG, jpegQuality, stream)) {
                            stream.toByteArray()
                        } else {
                            null
                        }
                    }
                }.getOrNull()
                val decoded = encoded?.let {
                    runCatching { BitmapFactory.decodeByteArray(it, 0, it.size) }.getOrNull()
                }
                if (decoded != null) {
                    if (filtered !== source) filtered.recycle()
                    filtered = decoded
                }
                filtered
            }.getOrNull()
        }

        val shown = rendered ?: source
        // A silent fall back to the uncropped source is what made "no filter
        // preview" so confusing; say so instead.
        previewFailed = rendered == null
        // The warp changes the aspect ratio, and the frame below is sized from
        // this value — so it has to track what is actually on screen or the page
        // is drawn stretched.
        previewAspect = shown.width.toFloat() / shown.height.toFloat()
        previewBitmap = shown.asImageBitmap()
        isFiltering = false
    }

    /**
     * Runs border detection and applies the result.
     *
     * Shared by the first frame of a review, the *Auto detect* button and the
     * *Borders* tab, so all three behave identically. Never overwrites corners the
     * user has already dragged unless [force] is set.
     */
    fun detectBorders(force: Boolean = false) {
        val source = previewSource ?: return
        if (isAutoDetecting) return
        if (!force && cornersEdited) return
        scope.launch {
            isAutoDetecting = true
            val detection = withContext(Dispatchers.Default) { DocumentDetector.detect(source) }
            corners = detection.corners
            cornersEdited = false
            detectionFailed = !detection.confident
            isAutoDetecting = false
        }
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
            val quality = repository.currentSettings().imageQuality
            val baked = PageRenderer.bake(
                context = context,
                page = current.copy(
                    filterType = selectedFilter,
                    rotationDegrees = rotation,
                    corners = corners
                ),
                jpegQuality = quality.jpegQuality,
                maxDimension = PageRenderer.bakeDimension(quality)
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
                            onClick = {
                                isCropMode = true
                                // Opening the Borders tab detects the edges for
                                // you; the quad is a starting point to nudge, not
                                // something to drag out from the frame edges.
                                detectBorders()
                            },
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
                            TextButton(onClick = {
                                corners = QuadCorners.FULL_FRAME
                                cornersEdited = false
                            }) {
                                Icon(Icons.Default.Fullscreen, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Full page")
                            }
                            TextButton(
                                onClick = { detectBorders(force = true) },
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
            // If a full-resolution filter render fails (memory pressure on a big
            // page), say so rather than quietly showing the unfiltered frame and
            // letting the user think the filter did nothing.
            if (!isCropMode && previewFailed && image != null && !isFiltering) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                    ) {
                        Text(
                            "Preview could not be rendered at full size — showing the unfiltered page",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        IconButton(
                            onClick = { previewFailed = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
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

                    val density = LocalDensity.current
                    val frameWpx = with(density) { frameWidth.toPx() }
                    val frameHpx = with(density) { frameHeight.toPx() }

                    // Panning is bounded by what rotation actually puts on screen:
                    // a 90° turn swaps the effective width and height.
                    val extentX = if (swapAxes) frameHpx else frameWpx
                    val extentY = if (swapAxes) frameWpx else frameHpx

                    fun clampPanFor(scale: Float, candidate: Offset): Offset {
                        val maxX = (max(0f, scale - 1f) * extentX) / 2f
                        val maxY = (max(0f, scale - 1f) * extentY) / 2f
                        return Offset(
                            candidate.x.coerceIn(-maxX, maxX),
                            candidate.y.coerceIn(-maxY, maxY)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds()
                            // One gesture handler for everything: one finger drags a
                            // corner (or pans when zoomed), two fingers pinch.
                            .pointerInput(isCropMode, rotation, frameWpx, frameHpx) {
                                val frameW = frameWidth.toPx()
                                val frameH = frameHeight.toPx()
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                val rotateRad = (rotation * PI / 180.0).toFloat()

                                /**
                                 * Screen → canvas coordinates, inverting translation,
                                 * scale and rotation. Mirrors the layer order below:
                                 * screen = centre + pan + zoom · R(rotation) · (canvas − centre)
                                 */
                                fun screenToCanvas(point: Offset): Offset {
                                    val vx = point.x - centerX - pan.x
                                    val vy = point.y - centerY - pan.y
                                    val rad = -rotateRad
                                    val c = cos(rad)
                                    val s = sin(rad)
                                    val rx = (vx * c - vy * s) / zoom
                                    val ry = (vx * s + vy * c) / zoom
                                    return Offset(rx + frameW / 2f, ry + frameH / 2f)
                                }

                                fun grabCornerAt(point: Offset): Int? {
                                    if (!isCropMode) return null
                                    val canvasPoint = screenToCanvas(point)
                                    val nx = canvasPoint.x / frameW
                                    val ny = canvasPoint.y / frameH
                                    if (nx < -0.15f || nx > 1.15f || ny < -0.15f || ny > 1.15f) return null
                                    val distances = listOf(
                                        distSq(nx, ny, corners.topLeftX, corners.topLeftY),
                                        distSq(nx, ny, corners.topRightX, corners.topRightY),
                                        distSq(nx, ny, corners.bottomRightX, corners.bottomRightY),
                                        distSq(nx, ny, corners.bottomLeftX, corners.bottomLeftY)
                                    )
                                    return distances.withIndex().minByOrNull { it.value }
                                        ?.takeIf { it.value < GRAB_THRESHOLD }
                                        ?.index
                                }

                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    grabbedCorner = grabCornerAt(down.position)
                                    var multiTouch = false

                                    var event: PointerEvent
                                    do {
                                        event = awaitPointerEvent()
                                        val pressed = event.changes.count { it.pressed }

                                        if (pressed >= 2) {
                                            // Pinch. Once a second finger lands, the
                                            // gesture stays a pinch even if one lifts,
                                            // so the corner never jumps mid-pinch.
                                            multiTouch = true
                                            grabbedCorner = null

                                            val zoomChange = event.calculateZoom()
                                            val panChange = event.calculatePan()
                                            if (zoomChange != 1f || panChange != Offset.Zero) {
                                                val newZoom = (zoom * zoomChange).coerceIn(1f, MAX_ZOOM)
                                                val k = newZoom / zoom
                                                // Keep the pinch centroid stationary.
                                                val centroid = event.calculateCentroid(useCurrent = false)
                                                val u = Offset(centroid.x - centerX, centroid.y - centerY)
                                                val candidate = if (newZoom <= 1.0001f) {
                                                    Offset.Zero
                                                } else {
                                                    pan * k + u * (1f - k) + panChange
                                                }
                                                zoom = newZoom
                                                pan = clampPanFor(newZoom, candidate)
                                                event.changes.forEach {
                                                    if (it.positionChanged()) it.consume()
                                                }
                                            }
                                        } else if (pressed == 1 && !multiTouch) {
                                            val change = event.changes.first { it.pressed }
                                            val grabbed = grabbedCorner
                                            if (grabbed != null) {
                                                // Corner positions are stored normalised
                                                // to the unrotated, unscaled frame, so
                                                // zoom and rotation change nothing here.
                                                val canvasPoint = screenToCanvas(change.position)
                                                val nx = (canvasPoint.x / frameW).coerceIn(0f, 1f)
                                                val ny = (canvasPoint.y / frameH).coerceIn(0f, 1f)
                                                corners = when (grabbed) {
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
                                                cornersEdited = true
                                                change.consume()
                                            } else if (zoom > 1.001f) {
                                                // One finger pans the zoomed page.
                                                val delta = change.position - change.previousPosition
                                                if (delta != Offset.Zero) {
                                                    pan = clampPanFor(zoom, pan + delta)
                                                    change.consume()
                                                }
                                            }
                                        }
                                    } while (event.changes.any { it.pressed })

                                    grabbedCorner = null
                                }
                            }
                    ) {
                        // Zoom/pan live outside the rotation so a drag always moves
                        // content in the direction the finger moved.
                        Box(
                            modifier = Modifier
                                .size(frameWidth, frameHeight)
                                .align(Alignment.Center)
                                .graphicsLayer {
                                    scaleX = zoom
                                    scaleY = zoom
                                    translationX = pan.x
                                    translationY = pan.y
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationZ = rotation }
                            ) {
                                Image(
                                    bitmap = image,
                                    contentDescription = "Page preview",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    // The canvas is inside the zoom layer, so strokes and
                                    // handles are divided by the scale to keep a constant
                                    // on-screen size while zoomed in.
                                    val invZoom = 1f / zoom
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
                                        drawPath(quad, color = Color(0xFF4FD8EB).copy(alpha = 0.10f))
                                        drawPath(
                                            path = quad,
                                            color = Color(0xFF4FD8EB),
                                            style = Stroke(width = 3.dp.toPx() * invZoom)
                                        )
                                        points.forEachIndexed { index, point ->
                                            val isActive = grabbedCorner == index
                                            drawCircle(
                                                color = Color.White,
                                                radius = (if (isActive) 13.dp else 11.dp).toPx() * invZoom,
                                                center = point
                                            )
                                            drawCircle(
                                                color = Color(0xFF0061A4),
                                                radius = (if (isActive) 9.dp else 7.dp).toPx() * invZoom,
                                                center = point
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Zoom controls: pinch works, but buttons are discoverable and
                    // give exact steps when lining up a corner.
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val newZoom = (zoom / ZOOM_FACTOR).coerceAtLeast(1f)
                                    zoom = newZoom
                                    pan = if (newZoom <= 1.0001f) Offset.Zero else clampPanFor(newZoom, pan)
                                },
                                enabled = zoom > 1.001f,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Zoom out",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "${(zoom * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(46.dp),
                                textAlign = TextAlign.Center
                            )
                            IconButton(
                                onClick = {
                                    val newZoom = (zoom * ZOOM_FACTOR).coerceAtMost(MAX_ZOOM)
                                    zoom = newZoom
                                    pan = clampPanFor(newZoom, pan)
                                },
                                enabled = zoom < MAX_ZOOM - 0.001f,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Zoom in",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    zoom = 1f
                                    pan = Offset.Zero
                                },
                                enabled = zoom > 1.001f || pan != Offset.Zero,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.FitScreen,
                                    contentDescription = "Fit to screen",
                                    modifier = Modifier.size(18.dp)
                                )
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

/** Largest preview magnification in the border/filter editor. */
private const val MAX_ZOOM = 5f

/** Multiplier applied by the +/- zoom buttons. */
private const val ZOOM_FACTOR = 1.6f

/**
 * Squared normalised distance within which a touch counts as grabbing a corner.
 * 0.05 ≈ 22 % of the frame width, which stays comfortable at 1x and effectively
 * grows as the user zooms in.
 */
private const val GRAB_THRESHOLD = 0.05f
