package com.example.docscanner.ui.editor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.FilterType
import com.example.docscanner.model.QuadCorners
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.theme.DocShapes
import com.example.docscanner.theme.LocalDocAccents
import com.example.docscanner.theme.Motion
import com.example.docscanner.theme.pressScale
import com.example.docscanner.ui.common.InfoPill
import com.example.docscanner.ui.common.LoadingDots
import com.example.docscanner.ui.common.PageProgressBar
import com.example.docscanner.ui.common.PageThumbnail
import com.example.docscanner.ui.common.PillButton
import com.example.docscanner.ui.common.SegmentedPill
import com.example.docscanner.ui.common.TonalIconButton
import com.example.docscanner.util.CropHandles
import com.example.docscanner.util.DocumentDetector
import com.example.docscanner.util.ImageProcessingUtils
import com.example.docscanner.util.ImageStore
import com.example.docscanner.util.PageRenderer
import com.example.docscanner.util.PerspectiveWarp
import java.io.ByteArrayOutputStream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Longest edge decoded for the automatic border pass.
 *
 * The detector itself works on a 360 px grey copy, so this only has to carry
 * enough detail that a page edge survives the down-take; 720 is twice the working
 * resolution and keeps the pass to a few megabytes per page instead of the ~15 MB
 * a preview-sized decode would take.
 */
private const val DETECT_ALL_DIMENSION = 720

/** How long the first page gets the machine to itself before the pass starts. */
private const val AUTO_DETECT_START_DELAY_MS = 450L

/**
 * Pages the automatic pass will look at.
 *
 * Bounded for the same reason the OCR backfill is: a 200-page document must not
 * turn opening the editor into a long burst of decoding. Pages past the limit keep
 * the old behaviour — the strip walks them, and each one auto-detects as it is
 * brought on screen.
 */
private const val AUTO_DETECT_LIMIT = 40

/**
 * How long the filter strip must rest before a full-resolution preview render.
 *
 * Tapping through filters changes the selection every frame; each render is a
 * multi-megapixel pass and the previous bitmap stays on screen meanwhile, so the
 * delay only prevents wasted work.
 */
private const val PREVIEW_SETTLE_MS = 140L

/**
 * Longest edge rendered for the on-screen preview.
 *
 * The frame is about 1000 px wide, so 2400 is already supersampled for it while
 * the *file* stays at the bake size (3200). Rendering at bake size for a preview
 * costs 2.5x for pixels the display cannot show.
 */
private const val PREVIEW_RENDER_DIMENSION = 2400

/** Long edge of the tiny render behind each filter swatch, in pixels. */
private const val FILTER_THUMB_DIMENSION = 260

/**
 * The two stages of a scan.
 *
 * Splitting borders from filters is the whole point of this screen: borders are a
 * *geometry* decision that must be made per page and is cheap, while filters are a
 * *look* decision that is made once and is expensive to bake. Doing them together
 * forces the user to re-decide the look on every page and pays the bake cost
 * before the user knows how many pages there are.
 */
private enum class Stage(val label: String) {
    BORDERS("Borders"),
    FILTERS("Filters")
}

/**
 * Perspective correction + enhancement editor.
 *
 * Two ways in:
 *
 * - **Review** (`reviewMode = true`) — straight from the camera after *Done*, this
 *   becomes a two-stage flow. Stage 1 walks every captured page: adjust the four
 *   corners (auto-detected first), tap *Next page*, repeat. Stage 2 picks one
 *   filter and applies it to the whole document in a single bake, with progress.
 * - **Single page** — reached from the document screen to re-edit one page; the
 *   same two stages are available as tabs and *Save* bakes just this page.
 *
 * Edits are baked non-destructively by [PageRenderer] from the pristine capture,
 * and corner edits are persisted as soon as they are made so leaving the screen
 * early never loses work.
 */
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
    val accents = LocalDocAccents.current

    // Capture quality is read in three places — the preview decode, the preview
    // bake and the save bake — and they must agree, so it is read once here
    // instead of three times through `repository.currentSettings()`.
    val settings by repository.settings.collectAsState()
    val imageQuality = settings.imageQuality

    val documents by repository.documents.collectAsState()
    val document = remember(documents, docId) { documents.find { it.id == docId } }
    val pages = document?.pages ?: emptyList()

    var activePageId by remember(pageId) { mutableStateOf(pageId) }
    val activeIndex = pages.indexOfFirst { it.id == activePageId }
    val hasNextPage = activeIndex >= 0 && activeIndex < pages.lastIndex

    // Review leads with borders; a single-page edit leads with the filter, because
    // re-editing a page is almost always about the look, not the crop.
    var stage by remember(reviewMode) {
        mutableStateOf(if (reviewMode) Stage.BORDERS else Stage.FILTERS)
    }

    var selectedFilter by remember { mutableStateOf(FilterType.MAGIC_COLOR) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var corners by remember { mutableStateOf(QuadCorners.FULL_FRAME) }

    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var grabbedCorner by remember { mutableStateOf<Int?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var isAutoDetecting by remember { mutableStateOf(false) }
    var detectionFailed by remember { mutableStateOf(false) }
    // How many of the four borders were real edges. "Nothing found" and "three of
    // four found" both need saying, and they need different words: one is a
    // failure, the other is a page whose fourth side runs off the frame.
    var sidesFound by remember { mutableIntStateOf(0) }
    var loadedPageId by remember { mutableStateOf<String?>(null) }
    var cornersEdited by remember { mutableStateOf(false) }

    /**
     * Whether the next page load keeps the filter the user has chosen.
     *
     * Loading a page normally adopts that page's own filter — right when the screen
     * opens, wrong when the user is walking the strip in the filter stage to compare
     * one look across pages: each tap silently threw the choice away and put back
     * the stored one, so "the same filter on every page" could not be previewed at
     * all.
     */
    var keepFilterOnNextLoad by remember { mutableStateOf(false) }

    // Pages whose borders the user has confirmed in this session.
    val borderedPages = remember(docId) { mutableStateMapOf<String, Boolean>() }

    // Results of the automatic border pass over the *other* pages, plus its
    // progress. Local to the session: the corners themselves are persisted, this
    // map only exists so the strip can show what was confident and what was not.
    val autoBorders = remember(docId) {
        mutableStateMapOf<String, DocumentDetector.Detection>()
    }
    var isDetectingAll by remember { mutableStateOf(false) }
    var detectedCount by remember { mutableIntStateOf(0) }
    var detectTargetCount by remember { mutableIntStateOf(0) }

    // Batch bake progress: null when idle, otherwise the page being baked.
    var applyingPage by remember { mutableStateOf<Int?>(null) }
    var applyingTotal by remember { mutableIntStateOf(0) }

    var previewSource by remember { mutableStateOf<Bitmap?>(null) }
    var previewBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var previewAspect by remember { mutableFloatStateOf(0.707f) }
    var isFiltering by remember { mutableStateOf(false) }
    var previewFailed by remember { mutableStateOf(false) }
    var bakeDimension by remember { mutableIntStateOf(
        com.example.docscanner.util.DocumentFilters.MAX_PROCESS_DIMENSION
    ) }

    // One tiny filtered render per filter, so the strip shows the real look on the
    // real page instead of a coloured rectangle.
    var filterThumbs by remember { mutableStateOf<Map<FilterType, ImageBitmap>>(emptyMap()) }

    val isBordersStage = stage == Stage.BORDERS

    // ---- load the active page ------------------------------------------------
    LaunchedEffect(activePageId) {
        val current = repository.getDocumentById(docId)
            ?.pages?.find { it.id == activePageId } ?: return@LaunchedEffect

        // Drop the previous page's pixels now, rather than leaving them under the
        // new page's number until this one has arrived. Switching pages is a normal
        // thing to do in the filter stage (the strip is right there), and a frame of
        // page 2 labelled "Page 3 of 12" is exactly the kind of lying preview that
        // makes an editor feel broken. The loading state says "Loading page…".
        previewBitmap = null

        if (!keepFilterOnNextLoad) selectedFilter = current.filterType
        keepFilterOnNextLoad = false
        rotation = current.rotationDegrees
        corners = current.corners
        zoom = 1f
        pan = Offset.Zero
        cornersEdited = false

        val bakeEdge = PageRenderer.bakeDimension(imageQuality)
        val decoded = withContext(Dispatchers.IO) {
            ImageStore.loadBitmap(
                context,
                current.originalImageUri ?: current.imageUri,
                minOf(bakeEdge, PREVIEW_RENDER_DIMENSION)
            )
        }
        if (decoded == null) {
            loadedPageId = activePageId
            return@LaunchedEffect
        }

        bakeDimension = bakeEdge
        previewSource = decoded
        previewAspect = decoded.width.toFloat() / decoded.height.toFloat()

        // Auto-detect only for pages the user has never cropped. A page that
        // already has corners keeps them: re-entering the editor must not silently
        // re-crop a page the user was happy with.
        if (current.corners.isFullFrame) {
            isAutoDetecting = true
            val detection = withContext(Dispatchers.Default) { DocumentDetector.detect(decoded, context) }
            isAutoDetecting = false
            corners = detection.corners
            detectionFailed = !detection.confident
            sidesFound = detection.sidesFound
        } else {
            detectionFailed = false
            sidesFound = 4
        }

        loadedPageId = activePageId
    }

    /**
     * Detects the borders of every page that has never been cropped — once, when
     * the review flow opens.
     *
     * The pass exists because the batch flow used to make the user walk each page
     * by hand ("Next page", adjust, repeat) to reach a decision the detector can
     * make on its own for most pages. Now the pages that are clean are already
     * fixed by the time the user looks at the second one, and the only pages left
     * to think about are the ones the detector was *not* confident about — which
     * the strip marks in amber.
     *
     * The page being displayed is deliberately excluded: it runs the same detector
     * as part of loading (see above) and writing it from here would race with the
     * user's hands.
     */
    LaunchedEffect(docId, reviewMode) {
        if (!reviewMode) return@LaunchedEffect
        // The first page's preview and its filter swatches are what the user is
        // looking at; this pass starts only once they have had the CPU to appear.
        delay(AUTO_DETECT_START_DELAY_MS)
        val targets = repository.getDocumentById(docId)?.pages.orEmpty()
            .filter { it.corners.isFullFrame && it.id != activePageId }
            .take(AUTO_DETECT_LIMIT)
        if (targets.isEmpty()) return@LaunchedEffect

        isDetectingAll = true
        detectTargetCount = targets.size
        detectedCount = 0
        val found = LinkedHashMap<String, DocumentDetector.Detection>()
        try {
            for (page in targets) {
                val bitmap = withContext(Dispatchers.IO) {
                    ImageStore.loadBitmap(
                        context,
                        page.originalImageUri ?: page.imageUri,
                        DETECT_ALL_DIMENSION
                    )
                }
                if (bitmap != null) {
                    // `finally` so a cancelled pass still frees its decode: the
                    // user backing out of the screen must not leave a handful of
                    // 720 px bitmaps waiting for the collector.
                    val detection = try {
                        withContext(Dispatchers.Default) { DocumentDetector.detect(bitmap, context) }
                    } finally {
                        bitmap.recycle()
                    }
                    if (!detection.corners.isFullFrame) found[page.id] = detection
                }
                detectedCount++
            }
        } finally {
            isDetectingAll = false
        }

        if (found.isEmpty()) return@LaunchedEffect

        // Only pages that are *still* un-cropped get written: the user may have
        // started adjusting one of them while this ran.
        val stillUntouched = repository.getDocumentById(docId)?.pages.orEmpty()
            .filter { it.corners.isFullFrame && found.containsKey(it.id) }
            .map { it.id }
        if (stillUntouched.isEmpty()) return@LaunchedEffect

        autoBorders.putAll(found.filterKeys { it in stillUntouched })
        repository.setPageCorners(
            docId,
            stillUntouched.associateWith { id -> found.getValue(id).corners }
        )
        // Only the confident ones are signed off. An unconfident detection still
        // gives the page a usable quad (so a batch bake always has one), but it is
        // not a decision the user made, so the page stays open and shows amber.
        stillUntouched
            .filter { found[it]?.confident == true }
            .forEach { borderedPages[it] = true }
    }

    // ---- filter swatches -----------------------------------------------------
    // Keyed on the stage, not on `corners`. The drag no longer hoists a corner on
    // every pointer move (see `liveQuad` in `BorderCanvas`), so this is not what
    // keeps the drag smooth any more — but it is still right: the swatches are
    // full five-filter renders of the warped page, they cannot be seen from the
    // borders stage, and re-rendering them the moment a handle is released is a
    // frame of work nobody asked for. The stage change is the only moment they
    // become visible, so it is the only moment they need to be fresh.
    LaunchedEffect(previewSource, stage) {
        val source = previewSource ?: return@LaunchedEffect
        val thumbs = withContext(Dispatchers.Default) {
            runCatching {
                val scale = FILTER_THUMB_DIMENSION.toFloat() / max(source.width, source.height)
                val small = if (scale < 1f) {
                    Bitmap.createScaledBitmap(
                        source,
                        (source.width * scale).roundToInt().coerceAtLeast(1),
                        (source.height * scale).roundToInt().coerceAtLeast(1),
                        true
                    )
                } else {
                    source
                }
                // Warp then filter, exactly as the save does — the swatch is a
                // promise about the file, so it cannot skip the correction.
                val warped = PerspectiveWarp.warp(small, corners)
                val out = LinkedHashMap<FilterType, ImageBitmap>()
                for (filter in FilterType.entries) {
                    val filtered = com.example.docscanner.util.DocumentFilters
                        .apply(warped, filter, FILTER_THUMB_DIMENSION)
                    out[filter] = filtered.asImageBitmap()
                }
                // Not recycled: `DocumentFilters.apply` returns its input for
                // ORIGINAL, so the ORIGINAL swatch aliases `warped`. These are
                // ~260 px bitmaps created once per page, and the GC is a better
                // owner than a manual free that can pull pixels out from under a
                // live ImageBitmap.
                out
            }.getOrNull()
        }
        if (thumbs != null) filterThumbs = thumbs
    }

    // ---- full-size preview ---------------------------------------------------
    LaunchedEffect(previewSource, selectedFilter, corners, rotation, stage, bakeDimension) {
        val source = previewSource
        if (source == null) {
            previewBitmap = null
            return@LaunchedEffect
        }
        if (isBordersStage) {
            // On the borders stage the user needs the whole, uncropped frame to aim
            // at; the correction is previewed on the filters stage.
            previewBitmap = source.asImageBitmap()
            previewAspect = source.width.toFloat() / source.height.toFloat()
            previewFailed = false
            return@LaunchedEffect
        }
        isFiltering = true
        previewFailed = false
        delay(PREVIEW_SETTLE_MS)

        val quality = imageQuality
        val lossless = selectedFilter == FilterType.CLEAR_BW ||
            selectedFilter == FilterType.GRAYSCALE
        val previewDimension = minOf(bakeDimension, PREVIEW_RENDER_DIMENSION)

        val rendered = withContext(Dispatchers.Default) {
            runCatching {
                // applyEnhancements is the *same* function the save runs (warp,
                // rotate, filter), so the preview cannot drift from the file.
                var filtered = ImageProcessingUtils.applyEnhancements(
                    src = source,
                    filterType = selectedFilter,
                    rotationDegrees = rotation,
                    corners = corners,
                    maxDimension = previewDimension
                )
                // Re-encode through the same encoder the save uses: at Medium (88)
                // JPEG softens small text, and showing pre-encode pixels would let a
                // softer setting look better than it saves.
                if (!lossless && filtered !== source) {
                    val encoded = ByteArrayOutputStream().use { stream ->
                        if (filtered.compress(Bitmap.CompressFormat.JPEG, quality.jpegQuality, stream)) {
                            stream.toByteArray()
                        } else {
                            null
                        }
                    }
                    val decoded = encoded?.let {
                        runCatching { BitmapFactory.decodeByteArray(it, 0, it.size) }.getOrNull()
                    }
                    if (decoded != null) {
                        filtered.recycle()
                        filtered = decoded
                    }
                }
                filtered
            }.getOrNull()
        }

        val shown = rendered ?: source
        previewFailed = rendered == null
        previewAspect = shown.width.toFloat() / shown.height.toFloat()
        previewBitmap = shown.asImageBitmap()
        isFiltering = false
    }

    /** Runs border detection and applies it, unless the user has already nudged. */
    fun detectBorders(force: Boolean = false) {
        val source = previewSource ?: return
        if (isAutoDetecting) return
        if (!force && cornersEdited) return
        scope.launch {
            isAutoDetecting = true
            val detection = withContext(Dispatchers.Default) { DocumentDetector.detect(source, context) }
            corners = detection.corners
            cornersEdited = false
            detectionFailed = !detection.confident
            sidesFound = detection.sidesFound
            isAutoDetecting = false
        }
    }

    /**
     * Persists the geometry of the current page — corners and rotation — without
     * baking.
     *
     * This is what makes a multi-page border pass safe: the pixels are left alone,
     * so the bake still starts from the pristine capture, but the decision survives
     * a page switch, a process death or leaving the screen.
     */
    fun persistGeometry() {
        repository.updatePage(docId, activePageId) {
            it.copy(corners = corners, rotationDegrees = rotation)
        }
        borderedPages[activePageId] = true
    }

    // Leaving the screen must not throw away a corner the user dragged.
    //
    // `persistGeometry` runs on a page switch, on Continue and before a bake — but
    // not on system back or the close button, which used to drop the drag silently
    // (the pixels were never touched, so nothing looked wrong until the page was
    // reopened). Compared against the stored geometry so an exit with nothing to
    // save does not bump `modifiedDate` and re-sort the library.
    DisposableEffect(docId) {
        onDispose {
            val stored = repository.getDocumentById(docId)
                ?.pages
                ?.find { it.id == activePageId }
                ?: return@onDispose
            if (stored.corners != corners || stored.rotationDegrees != rotation) {
                // Written straight to the repository rather than through
                // `persistGeometry`: that also marks the page as signed off in
                // `borderedPages`, and this composable is being disposed — mutating
                // the session's state map on the way out buys nothing and is the one
                // write here that does not need to happen.
                repository.updatePage(docId, activePageId) {
                    it.copy(corners = corners, rotationDegrees = rotation)
                }
            }
        }
    }

    fun goToPage(index: Int) {
        val target = pages.getOrNull(index) ?: return
        if (target.id == activePageId) return
        persistGeometry()
        // Browsing pages with a filter on is comparing looks; browsing them with
        // corners in hand is deciding crops. Only the first one keeps the choice
        // (see `keepFilterOnNextLoad`).
        keepFilterOnNextLoad = stage == Stage.FILTERS
        activePageId = target.id
    }

    fun advanceOrFinish() {
        persistGeometry()
        if (hasNextPage) {
            activePageId = pages[activeIndex + 1].id
        } else {
            // Every page has borders now; the only decision left is the look.
            stage = Stage.FILTERS
        }
    }

    /** Bakes [targets] with the chosen filter, one page at a time, with progress. */
    fun applyFilterToPages(targets: List<ScannedPage>) {
        if (isSaving || targets.isEmpty()) return
        // The visible page may carry un-persisted crop/rotation (the single-page
        // path never switches pages, so nothing else saves it).
        persistGeometry()
        isSaving = true
        scope.launch {
            applyingTotal = targets.size
            val quality = imageQuality
            val dimension = PageRenderer.bakeDimension(quality)
            targets.forEachIndexed { index, page ->
                applyingPage = index
                // The page in the repository carries the corners saved during the
                // border pass; only the filter is new here.
                val latest = repository.getDocumentById(docId)
                    ?.pages?.find { it.id == page.id } ?: page
                val baked = PageRenderer.bake(
                    context = context,
                    page = latest.copy(filterType = selectedFilter),
                    jpegQuality = quality.jpegQuality,
                    maxDimension = dimension
                )
                if (baked != null) {
                    repository.updatePage(docId, page.id) { baked }
                }
            }
            applyingPage = null
            isSaving = false
            onDone()
        }
    }

    val pageLabel = when {
        pages.isEmpty() -> "Page"
        activeIndex >= 0 -> "Page ${activeIndex + 1} of ${pages.size}"
        else -> "Page"
    }

    // True once every page has a usable quad — set by the automatic pass, by the
    // user walking the strip, or by both. It is what lets the primary button stop
    // asking for a page-by-page walk that has already happened.
    val everyPageBordered = pages.isNotEmpty() &&
        pages.all { borderedPages[it.id] == true }
    // A page the detector was unsure about stays open until the user signs it off
    // (which happens when they leave it — see persistGeometry).
    val needsCheck: (ScannedPage) -> Boolean = { page ->
        autoBorders[page.id]?.confident == false && borderedPages[page.id] != true
    }
    val uncertainPages = pages.count { needsCheck(it) }
    val firstUncertainIndex = pages.indexOfFirst { needsCheck(it) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            EditorHeader(
                title = if (isBordersStage) "Adjust borders" else "Choose a filter",
                subtitle = if (isBordersStage) {
                    when {
                        isDetectingAll ->
                            "Finding borders · $detectedCount of $detectTargetCount pages"
                        everyPageBordered && uncertainPages == 0 ->
                            "All ${pages.size} borders found · tap a page to adjust"
                        uncertainPages > 0 ->
                            "$pageLabel · $uncertainPages need${if (uncertainPages == 1) "s" else ""} a check"
                        else -> "$pageLabel · tap a corner to fine-tune"
                    }
                } else {
                    "$pageLabel · applied to every page"
                },
                onClose = onDone,
                leadingIcon = if (!reviewMode && stage == Stage.FILTERS) {
                    Icons.Default.ArrowBack
                } else {
                    Icons.Default.Close
                },
                trailing = {
                    if (reviewMode && isBordersStage && pages.size > 1) {
                        TextButton(onClick = { persistGeometry(); stage = Stage.FILTERS }) {
                            Text("Skip", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            )
        },
        bottomBar = {
            AnimatedContent(
                targetState = stage,
                transitionSpec = { fadeIn(Motion.emphasized) togetherWith fadeOut(Motion.emphasized) },
                label = "stageControls"
            ) { current ->
                when (current) {
                    Stage.BORDERS -> BordersControls(
                        stage = current,
                        onStageChange = {
                            // Leaving the border pass by tapping the segmented
                            // control counts as confirming this page's corners.
                            if (it != Stage.BORDERS) persistGeometry()
                            stage = it
                        },
                        pages = pages,
                        activePageId = activePageId,
                        borderedPages = borderedPages,
                        needsCheck = needsCheck,
                        zoom = zoom,
                        isAutoDetecting = isAutoDetecting,
                        canDetect = previewSource != null,
                        onSelectPage = { index -> goToPage(index) },
                        onZoomIn = { zoom = (zoom * ZOOM_FACTOR).coerceAtMost(MAX_ZOOM) },
                        onZoomOut = {
                            val next = (zoom / ZOOM_FACTOR).coerceAtLeast(1f)
                            zoom = next
                            if (next <= 1.0001f) pan = Offset.Zero
                        },
                        onResetZoom = { zoom = 1f; pan = Offset.Zero },
                        onFullPage = {
                            corners = QuadCorners.FULL_FRAME
                            cornersEdited = false
                            detectionFailed = false
                        },
                        onAutoDetect = { detectBorders(force = true) },
                        onRotateLeft = {
                            rotation = ImageProcessingUtils.normalizeRotation(rotation - 90f)
                        },
                        onRotateRight = {
                            rotation = ImageProcessingUtils.normalizeRotation(rotation + 90f)
                        },
                        // The button always offers the *next thing that needs a
                        // human*: a flagged page if there is one, otherwise the next
                        // page in order, otherwise the filter.
                        primaryLabel = when {
                            everyPageBordered -> "Continue to filters"
                            firstUncertainIndex >= 0 && firstUncertainIndex != activeIndex ->
                                "Check page ${firstUncertainIndex + 1}"
                            hasNextPage ->
                                "Next page (${(activeIndex + 2).coerceAtMost(pages.size)} of ${pages.size})"
                            else -> "Continue to filters"
                        },
                        primaryIcon = if (everyPageBordered) {
                            Icons.Default.AutoFixHigh
                        } else {
                            Icons.Default.ArrowForward
                        },
                        onPrimary = {
                            when {
                                // Every quad is decided: jumping straight to the
                                // filter keeps the promise the automatic pass made
                                // instead of walking pages nobody needs to see.
                                everyPageBordered -> {
                                    persistGeometry()
                                    stage = Stage.FILTERS
                                }
                                firstUncertainIndex >= 0 && firstUncertainIndex != activeIndex ->
                                    // goToPage persists the page being left, so a
                                    // flagged page is only ever signed off by having
                                    // been on screen and left — never in passing.
                                    goToPage(firstUncertainIndex)
                                else -> advanceOrFinish()
                            }
                        }
                    )

                    Stage.FILTERS -> FilterControls(
                        stage = current,
                        onStageChange = { stage = it },
                        pages = pages,
                        activePageId = activePageId,
                        borderedPages = borderedPages,
                        needsCheck = needsCheck,
                        onSelectPage = { index -> goToPage(index) },
                        selectedFilter = selectedFilter,
                        onSelectFilter = { selectedFilter = it },
                        thumbs = filterThumbs,
                        primaryLabel = when {
                            isSaving -> "Applying…"
                            !reviewMode -> "Save page"
                            pages.size > 1 -> "Apply to all ${pages.size} pages"
                            else -> "Apply filter"
                        },
                        primaryIcon = Icons.Default.Check,
                        isSaving = isSaving,
                        onPrimary = {
                            if (reviewMode) {
                                applyFilterToPages(pages)
                            } else {
                                val current = repository.getDocumentById(docId)
                                    ?.pages?.find { it.id == activePageId }
                                if (current == null) onDone() else applyFilterToPages(listOf(current))
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            val image = previewBitmap
            val pageReady = loadedPageId == activePageId
            when {
                image == null -> LoadingPage(pageReady = pageReady)

                isBordersStage -> BorderCanvas(
                    image = image,
                    aspect = previewAspect,
                    corners = corners,
                    rotation = rotation,
                    zoom = zoom,
                    pan = pan,
                    grabbedCorner = grabbedCorner,
                    onGrabbedCornerChange = { grabbedCorner = it },
                    onCornersChange = { corners = it; cornersEdited = true },
                    onZoomPanChange = { newZoom, newPan -> zoom = newZoom; pan = newPan }
                )

                else -> FilterPreview(image = image, aspect = previewAspect)
            }

            // Status banners live at the top of the canvas so they never cover the
            // primary action.
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isFiltering && image != null) {
                    InfoPill(
                        text = "Rendering preview…",
                        container = MaterialTheme.colorScheme.secondaryContainer,
                        content = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                if (!isBordersStage && previewFailed && image != null && !isFiltering) {
                    InfoPill(
                        text = "Preview fell back to the unfiltered page",
                        container = MaterialTheme.colorScheme.errorContainer,
                        content = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                if (isBordersStage && detectionFailed && image != null) {
                    InfoPill(
                        text = "No page edge found — drag the corners",
                        container = MaterialTheme.colorScheme.tertiaryContainer,
                        content = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                } else if (isBordersStage && !isAutoDetecting && image != null && sidesFound in 1..2) {
                    // Not a failure: a page photographed on a surface the same
                    // colour as itself really does have one or two invisible sides.
                    // Saying so is more useful than a silent, slightly wrong crop.
                    InfoPill(
                        text = "Only $sidesFound of 4 borders found — check the corners",
                        container = MaterialTheme.colorScheme.secondaryContainer,
                        content = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                if (isAutoDetecting) {
                    InfoPill(
                        text = "Finding the page…",
                        container = MaterialTheme.colorScheme.secondaryContainer,
                        content = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            applyingPage?.let { index ->
                ApplyingOverlay(current = index + 1, total = applyingTotal)
            }
        }
    }
}

/** Header: close/back, a two-line title, and an optional trailing action. */
@Composable
private fun EditorHeader(
    title: String,
    subtitle: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClose: () -> Unit,
    trailing: @Composable () -> Unit = {}
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TonalIconButton(
                icon = leadingIcon,
                contentDescription = "Close",
                onClick = onClose,
                container = MaterialTheme.colorScheme.surfaceContainerHigh,
                size = 42.dp
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            trailing()
        }
    }
}

/** The page as captured, with the four handles over it. */
@Composable
private fun BorderCanvas(
    image: ImageBitmap,
    aspect: Float,
    corners: QuadCorners,
    rotation: Float,
    zoom: Float,
    pan: Offset,
    grabbedCorner: Int?,
    onGrabbedCornerChange: (Int?) -> Unit,
    onCornersChange: (QuadCorners) -> Unit,
    onZoomPanChange: (Float, Offset) -> Unit
) {
    val accents = LocalDocAccents.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        val swapAxes = rotation.roundToInt() % 180 != 0
        val visualAspect = if (swapAxes) 1f / aspect else aspect
        val availableAspect = maxWidth / maxHeight
        val visualWidth = if (availableAspect > visualAspect) maxHeight * visualAspect else maxWidth
        val visualHeight = if (availableAspect > visualAspect) maxHeight else maxWidth / visualAspect
        val frameWidth = if (swapAxes) visualHeight else visualWidth
        val frameHeight = if (swapAxes) visualWidth else visualHeight

        val density = LocalDensity.current
        val frameWpx = with(density) { frameWidth.toPx() }
        val frameHpx = with(density) { frameHeight.toPx() }

        fun clampPanFor(scale: Float, candidate: Offset): Offset {
            val extentX = if (swapAxes) frameHpx else frameWpx
            val extentY = if (swapAxes) frameWpx else frameHpx
            val maxX = (max(0f, scale - 1f) * extentX) / 2f
            val maxY = (max(0f, scale - 1f) * extentY) / 2f
            return Offset(candidate.x.coerceIn(-maxX, maxX), candidate.y.coerceIn(-maxY, maxY))
        }

        // The quad being dragged, owned here rather than by the screen.
        //
        // `corners` is a parameter, so every pointer move used to hoist a new value
        // into the editor's own state — and that recomposed the *whole* screen on
        // every touch event: the header, the page strip (a `LazyRow`), the action
        // rows, the banners and this canvas, sixty to a hundred and twenty times a
        // second. None of them change while a handle moves.
        //
        // The live quad stays local, is written only inside the gesture, read only
        // inside the draw lambda, and handed up **once**, when the finger lifts.
        // A state read in the draw phase invalidates draw alone, so a drag now
        // costs one canvas redraw per frame instead of a screen recomposition.
        val liveQuad = remember(corners) { mutableStateOf(corners) }
        var activeCornerBeingDragged by remember { mutableStateOf<Int?>(null) }
        var touchIsOnLeftSide by remember { mutableStateOf(false) }

        // Everything the gesture reads, read *through a state holder* rather than
        // captured by value.
        //
        // This is the whole reason dragging a handle did nothing. `pointerInput`'s
        // block is kept for as long as its keys do not change — and the keys here
        // are the rotation and the frame size, which do not change while a handle
        // is being dragged. So the block went on seeing the `corners`, `zoom` and
        // above all `grabbedCorner` values from the composition that created it:
        // `grabbedCorner` was `null` for the entire gesture, the corner branch was
        // never entered, and the finger moved a handle that could not move.
        // `rememberUpdatedState` keeps one stable object while always returning the
        // current value. The quad is *not* read through a holder while dragging —
        // `liveQuad` above owns that — but the gesture still needs to know what the
        // screen last committed, and that is exactly what a holder is for.
        val latestCorners = rememberUpdatedState(corners)
        val latestZoom = rememberUpdatedState(zoom)
        val latestPan = rememberUpdatedState(pan)
        val reportGrabbed = rememberUpdatedState(onGrabbedCornerChange)
        val reportCorners = rememberUpdatedState(onCornersChange)
        val reportZoomPan = rememberUpdatedState(onZoomPanChange)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(rotation, frameWpx, frameHpx) {
                    val frameW = frameWidth.toPx()
                    val frameH = frameHeight.toPx()
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val rotateRad = (rotation * PI / 180.0).toFloat()

                    /**
                     * Screen → canvas coordinates, inverting translation, scale and
                     * rotation. Mirrors the layer order below:
                     * `screen = centre + pan + zoom · R(rotation) · (canvas − centre)`
                     */
                    fun screenToCanvas(point: Offset): Offset {
                        val panNow = latestPan.value
                        val zoomNow = latestZoom.value
                        val vx = point.x - centerX - panNow.x
                        val vy = point.y - centerY - panNow.y
                        val rad = -rotateRad
                        val c = cos(rad)
                        val s = sin(rad)
                        val rx = (vx * c - vy * s) / zoomNow
                        val ry = (vx * s + vy * c) / zoomNow
                        return Offset(rx + frameW / 2f, ry + frameH / 2f)
                    }

                    /**
                     * Hit-tests 8 handles: 4 corners (0..3) and 4 edge midpoints (10..13),
                     * matching the original app's CropView (ax0.java / p60.java).
                     */
                    fun grabHandleAt(point: Offset): Int? {
                        val canvasPoint = screenToCanvas(point)
                        val cornerTolerance = (HANDLE_GRAB_DP.dp.toPx() / latestZoom.value).let { it * it }
                        val edgeTolerance = ((HANDLE_GRAB_DP.dp.toPx() * 1.35f) / latestZoom.value).let { it * it }
                        val quad = liveQuad.value
                        val xs = floatArrayOf(
                            quad.topLeftX, quad.topRightX, quad.bottomRightX, quad.bottomLeftX
                        )
                        val ys = floatArrayOf(
                            quad.topLeftY, quad.topRightY, quad.bottomRightY, quad.bottomLeftY
                        )
                        var bestCorner = -1
                        var bestCornerDist = Float.MAX_VALUE
                        for (index in 0..3) {
                            val dx = canvasPoint.x - xs[index] * frameW
                            val dy = canvasPoint.y - ys[index] * frameH
                            val distance = dx * dx + dy * dy
                            if (distance < bestCornerDist) {
                                bestCornerDist = distance
                                bestCorner = index
                            }
                        }
                        if (bestCornerDist <= cornerTolerance) return bestCorner

                        // Check edge midpoints
                        val edgeMids = listOf(
                            CropHandles.EDGE_TOP to Offset((xs[0] + xs[1]) / 2f * frameW, (ys[0] + ys[1]) / 2f * frameH),
                            CropHandles.EDGE_RIGHT to Offset((xs[1] + xs[2]) / 2f * frameW, (ys[1] + ys[2]) / 2f * frameH),
                            CropHandles.EDGE_BOTTOM to Offset((xs[3] + xs[2]) / 2f * frameW, (ys[3] + ys[2]) / 2f * frameH),
                            CropHandles.EDGE_LEFT to Offset((xs[0] + xs[3]) / 2f * frameW, (ys[0] + ys[3]) / 2f * frameH)
                        )
                        var bestEdge = -1
                        var bestEdgeDist = Float.MAX_VALUE
                        for ((handleId, mid) in edgeMids) {
                            val dx = canvasPoint.x - mid.x
                            val dy = canvasPoint.y - mid.y
                            val dist = dx * dx + dy * dy
                            if (dist < bestEdgeDist) {
                                bestEdgeDist = dist
                                bestEdge = handleId
                            }
                        }
                        if (bestEdgeDist <= edgeTolerance) return bestEdge

                        return null
                    }

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val grabbed = grabHandleAt(down.position)
                        reportGrabbed.value(grabbed)
                        var multiTouch = false
                        var dragging = false
                        val touchSlop = viewConfiguration.touchSlop

                        var event: PointerEvent
                        do {
                            event = awaitPointerEvent()
                            val pressed = event.changes.count { it.pressed }

                            if (pressed >= 2) {
                                multiTouch = true
                                activeCornerBeingDragged = null
                                reportGrabbed.value(null)
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                if (zoomChange != 1f || panChange != Offset.Zero) {
                                    val zoomBefore = latestZoom.value
                                    val newZoom = (zoomBefore * zoomChange).coerceIn(1f, MAX_ZOOM)
                                    val k = newZoom / zoomBefore
                                    val centroid = event.calculateCentroid(useCurrent = false)
                                    val u = Offset(centroid.x - centerX, centroid.y - centerY)
                                    val candidate = if (newZoom <= 1.0001f) {
                                        Offset.Zero
                                    } else {
                                        latestPan.value * k + u * (1f - k) + panChange
                                    }
                                    reportZoomPan.value(newZoom, clampPanFor(newZoom, candidate))
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                            } else if (pressed == 1 && !multiTouch) {
                                val change = event.changes.firstOrNull { it.pressed }
                                if (change == null) continue
                                if (grabbed != null) {
                                    if (!dragging &&
                                        (change.position - down.position).getDistance() > touchSlop
                                    ) {
                                        dragging = true
                                    }
                                    if (dragging) {
                                        if (grabbed in 0..3) {
                                            activeCornerBeingDragged = grabbed
                                            touchIsOnLeftSide = change.position.x <= size.width / 2f
                                        } else {
                                            activeCornerBeingDragged = null
                                        }
                                        val from = screenToCanvas(change.previousPosition)
                                        val to = screenToCanvas(change.position)
                                        val dx = (to.x - from.x) / frameW
                                        val dy = (to.y - from.y) / frameH
                                        liveQuad.value = CropHandles.move(
                                            quad = liveQuad.value,
                                            handle = grabbed,
                                            dx = dx,
                                            dy = dy
                                        )
                                    }
                                    change.consume()
                                } else if (latestZoom.value > 1.001f) {
                                    val delta = change.position - change.previousPosition
                                    if (delta != Offset.Zero) {
                                        val zoomNow = latestZoom.value
                                        reportZoomPan.value(
                                            zoomNow,
                                            clampPanFor(zoomNow, latestPan.value + delta)
                                        )
                                        change.consume()
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        activeCornerBeingDragged = null
                        reportGrabbed.value(null)
                        val committed = latestCorners.value
                        if (liveQuad.value != committed) {
                            reportCorners.value(liveQuad.value)
                        }
                    }
                }
        ) {
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
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val invZoom = 1f / zoom
                        val shown = liveQuad.value
                        val points = listOf(
                            Offset(shown.topLeftX * w, shown.topLeftY * h),
                            Offset(shown.topRightX * w, shown.topRightY * h),
                            Offset(shown.bottomRightX * w, shown.bottomRightY * h),
                            Offset(shown.bottomLeftX * w, shown.bottomLeftY * h)
                        )
                        val quad = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            lineTo(points[1].x, points[1].y)
                            lineTo(points[2].x, points[2].y)
                            lineTo(points[3].x, points[3].y)
                            close()
                        }
                        drawPath(quad, color = accents.handleFill.copy(alpha = 0.10f))
                        drawPath(
                            path = quad,
                            color = accents.handleFill,
                            style = Stroke(width = 2.5.dp.toPx() * invZoom)
                        )

                        // 4 corner handles (circles)
                        points.forEachIndexed { index, point ->
                            val active = grabbedCorner == index
                            drawCircle(
                                color = accents.handleRing,
                                radius = (if (active) 14.dp else 11.dp).toPx() * invZoom,
                                center = point
                            )
                            drawCircle(
                                color = accents.handleFill,
                                radius = (if (active) 9.dp else 7.dp).toPx() * invZoom,
                                center = point
                            )
                        }

                        // 4 edge midpoint handles (rounded rectangles matching original app ax0.java)
                        val edgeHandles = listOf(
                            CropHandles.EDGE_TOP to Offset((points[0].x + points[1].x) / 2f, (points[0].y + points[1].y) / 2f),
                            CropHandles.EDGE_RIGHT to Offset((points[1].x + points[2].x) / 2f, (points[1].y + points[2].y) / 2f),
                            CropHandles.EDGE_BOTTOM to Offset((points[3].x + points[2].x) / 2f, (points[3].y + points[2].y) / 2f),
                            CropHandles.EDGE_LEFT to Offset((points[0].x + points[3].x) / 2f, (points[0].y + points[3].y) / 2f)
                        )
                        val edgeSize = 14.dp.toPx() * invZoom
                        val cornerRad = 3.5.dp.toPx() * invZoom
                        edgeHandles.forEach { (handleId, mid) ->
                            val active = grabbedCorner == handleId
                            val sz = if (active) edgeSize * 1.35f else edgeSize
                            val half = sz / 2f
                            val rect = androidx.compose.ui.geometry.Rect(mid.x - half, mid.y - half, mid.x + half, mid.y + half)
                            drawRoundRect(
                                color = accents.handleRing,
                                topLeft = rect.topLeft,
                                size = rect.size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRad, cornerRad)
                            )
                            val innerMargin = 2.dp.toPx() * invZoom
                            drawRoundRect(
                                color = accents.handleFill,
                                topLeft = Offset(rect.left + innerMargin, rect.top + innerMargin),
                                size = androidx.compose.ui.geometry.Size(rect.width - innerMargin * 2f, rect.height - innerMargin * 2f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRad * 0.7f, cornerRad * 0.7f)
                            )
                        }
                    }
                }
            }

            // 1.5x Magnifying loupe with crosshair lines (matching original app's ax0.java:562-626)
            val cornerDragging = activeCornerBeingDragged
            if (cornerDragging != null && cornerDragging in 0..3) {
                val q = liveQuad.value
                val (normX, normY) = when (cornerDragging) {
                    CropHandles.TOP_LEFT -> q.topLeftX to q.topLeftY
                    CropHandles.TOP_RIGHT -> q.topRightX to q.topRightY
                    CropHandles.BOTTOM_RIGHT -> q.bottomRightX to q.bottomRightY
                    CropHandles.BOTTOM_LEFT -> q.bottomLeftX to q.bottomLeftY
                    else -> 0f to 0f
                }
                MagnifyingLoupe(
                    image = image,
                    cornerNormX = normX,
                    cornerNormY = normY,
                    isTouchOnLeft = touchIsOnLeftSide
                )
            }
        }
    }
}

/**
 * 1.5x Magnifying loupe with crosshair lines (matching original app's ax0.java:562-626).
 *
 * Appears when any corner handle is being dragged. Positions itself at the top corner
 * opposite the finger touch (top-right if finger is on left half, top-left if finger is
 * on right half), so the user's hand never blocks visibility of the alignment.
 */
@Composable
private fun MagnifyingLoupe(
    image: ImageBitmap,
    cornerNormX: Float,
    cornerNormY: Float,
    isTouchOnLeft: Boolean,
    modifier: Modifier = Modifier
) {
    val accents = LocalDocAccents.current
    val loupeDiameter = 124.dp
    val alignment = if (isTouchOnLeft) Alignment.TopEnd else Alignment.TopStart

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = alignment
    ) {
        Surface(
            modifier = Modifier.size(loupeDiameter),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shadowElevation = 10.dp,
            border = BorderStroke(3.dp, accents.handleFill)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val zoomFactor = 1.75f

                val imgW = image.width.toFloat()
                val imgH = image.height.toFloat()

                val cornerPxX = cornerNormX * imgW
                val cornerPxY = cornerNormY * imgH

                val ovalPath = Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
                }

                clipPath(ovalPath) {
                    withTransform({
                        translate(
                            left = cx - cornerPxX * zoomFactor,
                            top = cy - cornerPxY * zoomFactor
                        )
                        scale(scaleX = zoomFactor, scaleY = zoomFactor, pivot = Offset.Zero)
                    }) {
                        drawImage(image)
                    }
                }

                // Crosshairs
                val strokeW = 1.5.dp.toPx()
                drawLine(
                    color = accents.handleFill,
                    start = Offset(0f, cy),
                    end = Offset(size.width, cy),
                    strokeWidth = strokeW
                )
                drawLine(
                    color = accents.handleFill,
                    start = Offset(cx, 0f),
                    end = Offset(cx, size.height),
                    strokeWidth = strokeW
                )
                // Center reticle ring
                drawCircle(
                    color = accents.handleFill,
                    radius = 4.dp.toPx(),
                    style = Stroke(width = strokeW)
                )
            }
        }
    }
}

/** The corrected, filtered page — the same pixels the bake will write. */
@Composable
private fun FilterPreview(image: ImageBitmap, aspect: Float) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Explicit size rather than aspectRatio + fillMaxSize: two sizing modifiers
        // competing over the same constraints is how previews end up stretched.
        val availableAspect = maxWidth / maxHeight
        val width = if (availableAspect > aspect) maxHeight * aspect else maxWidth
        val height = width / aspect
        Surface(
            shape = DocShapes.page,
            shadowElevation = 10.dp,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = Modifier.size(width = width, height = height)
        ) {
            Image(
                bitmap = image,
                contentDescription = "Filtered page preview",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun LoadingPage(pageReady: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        LoadingDots()
        Spacer(Modifier.height(14.dp))
        Text(
            if (pageReady) "This page's image is missing" else "Loading page…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Both stages share one segmented control, so the flow is always visible. */
@Composable
private fun StageSwitch(
    stage: Stage,
    pageCount: Int,
    onSelect: (Stage) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SegmentedPill(
            options = listOf(Stage.BORDERS.label, Stage.FILTERS.label),
            selectedIndex = if (stage == Stage.BORDERS) 0 else 1,
            onSelect = { index -> onSelect(if (index == 0) Stage.BORDERS else Stage.FILTERS) },
            modifier = Modifier.weight(1f)
        )
        if (pageCount > 1) {
            Spacer(Modifier.width(10.dp))
            InfoPill(text = "$pageCount pages")
        }
    }
}

/** Borders stage controls: page strip, geometry actions, primary advance. */
@Composable
private fun BordersControls(
    stage: Stage,
    onStageChange: (Stage) -> Unit,
    pages: List<ScannedPage>,
    activePageId: String,
    borderedPages: Map<String, Boolean>,
    needsCheck: (ScannedPage) -> Boolean = { false },
    zoom: Float,
    isAutoDetecting: Boolean,
    canDetect: Boolean,
    onSelectPage: (Int) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    onFullPage: () -> Unit,
    onAutoDetect: () -> Unit,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    primaryLabel: String,
    primaryIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onPrimary: () -> Unit
) {
    EditorSheet {
        StageSwitch(stage = stage, pageCount = pages.size, onSelect = onStageChange)
        Spacer(Modifier.height(14.dp))

        if (pages.size > 1) {
            PageStrip(
                pages = pages,
                activePageId = activePageId,
                borderedPages = borderedPages,
                needsCheck = needsCheck,
                onSelectPage = onSelectPage
            )
            Spacer(Modifier.height(14.dp))
        }

        // Geometry tools, in the order the eye reads them: the two decisions that
        // change the quad come first, rotation after.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TonalGeoButton(
                label = "Full page",
                icon = Icons.Default.Fullscreen,
                onClick = onFullPage,
                modifier = Modifier.weight(1f)
            )
            TonalGeoButton(
                label = if (isAutoDetecting) "Finding…" else "Auto detect",
                icon = Icons.Default.AutoFixHigh,
                onClick = onAutoDetect,
                enabled = canDetect && !isAutoDetecting,
                modifier = Modifier.weight(1f)
            )
            TonalIconButton(
                icon = Icons.Default.RotateLeft,
                contentDescription = "Rotate left",
                onClick = onRotateLeft,
                container = MaterialTheme.colorScheme.surfaceContainerHigh,
                size = 46.dp
            )
            TonalIconButton(
                icon = Icons.Default.RotateRight,
                contentDescription = "Rotate right",
                onClick = onRotateRight,
                container = MaterialTheme.colorScheme.surfaceContainerHigh,
                size = 46.dp
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ZoomCluster(
                zoom = zoom,
                onZoomIn = onZoomIn,
                onZoomOut = onZoomOut,
                onReset = onResetZoom,
                modifier = Modifier.weight(1f)
            )
            PillButton(
                text = primaryLabel,
                onClick = onPrimary,
                icon = primaryIcon,
                modifier = Modifier.weight(1.4f)
            )
        }
    }
}

/**
 * Filters stage controls: the page strip, live swatches and one apply action.
 *
 * The page strip is **not** border-pass furniture. Leaving the border stage used to
 * remove the only way to change pages in the whole editor: a document with twelve
 * scans opened on page one, and the filter stage could show, compare and apply to
 * page one and nothing else. The strip is the editor's page switcher, so it appears
 * in both stages, in the same place, with the same tick/amber markers.
 */
@Composable
private fun FilterControls(
    stage: Stage,
    onStageChange: (Stage) -> Unit,
    pages: List<ScannedPage>,
    activePageId: String,
    borderedPages: Map<String, Boolean>,
    needsCheck: (ScannedPage) -> Boolean = { false },
    onSelectPage: (Int) -> Unit,
    selectedFilter: FilterType,
    onSelectFilter: (FilterType) -> Unit,
    thumbs: Map<FilterType, ImageBitmap>,
    primaryLabel: String,
    primaryIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isSaving: Boolean,
    onPrimary: () -> Unit
) {
    EditorSheet {
        StageSwitch(stage = stage, pageCount = pages.size, onSelect = onStageChange)
        Spacer(Modifier.height(14.dp))

        if (pages.size > 1) {
            PageStrip(
                pages = pages,
                activePageId = activePageId,
                borderedPages = borderedPages,
                needsCheck = needsCheck,
                onSelectPage = onSelectPage
            )
            Spacer(Modifier.height(14.dp))
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(FilterType.entries.toList(), key = { it.name }) { filter ->
                FilterSwatch(
                    filter = filter,
                    thumb = thumbs[filter],
                    selected = filter == selectedFilter,
                    onClick = { onSelectFilter(filter) }
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        PillButton(
            text = primaryLabel,
            onClick = onPrimary,
            icon = primaryIcon,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** A filter option: the real filtered page as the swatch, not a colour block. */
@Composable
private fun FilterSwatch(
    filter: FilterType,
    thumb: ImageBitmap?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactions = remember { MutableInteractionSource() }
    val ringWidth by animateFloatAsState(if (selected) 3f else 0f, spring(), label = "swatchRing")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(78.dp)
            .pressScale(interactions, pressedScale = 0.94f)
            .clip(DocShapes.thumbnail)
            .clickable(interactionSource = interactions, indication = ripple(), onClick = onClick)
            .padding(4.dp)
    ) {
        Surface(
            shape = DocShapes.thumbnail,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (thumb != null) {
                    Image(
                        bitmap = thumb,
                        contentDescription = filter.label,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LoadingDots(dotSize = 5.dp)
                }
                if (ringWidth > 0.5f) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .border(
                                width = ringWidth.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = DocShapes.thumbnail
                            )
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            filter.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * The editor's page switcher — one strip, both stages.
 *
 * A single strip rather than a copy per stage because it is the *same* control in
 * both places: same chips, same tick/amber markers, same behaviour. Leaving the
 * border pass used to remove it entirely (see `FilterControls`).
 *
 * It also follows the page you are on. With more than a handful of scans the strip
 * scrolls, and a selected chip off-screen is the same problem as no strip at all —
 * so when the active page changes, the strip brings its chip into view.
 */
@Composable
private fun PageStrip(
    pages: List<ScannedPage>,
    activePageId: String,
    borderedPages: Map<String, Boolean>,
    needsCheck: (ScannedPage) -> Boolean,
    onSelectPage: (Int) -> Unit
) {
    val state = rememberLazyListState()
    val activeIndex = pages.indexOfFirst { it.id == activePageId }
    LaunchedEffect(activeIndex, pages.size) {
        if (activeIndex < 0) return@LaunchedEffect
        val visible = state.layoutInfo.visibleItemsInfo.any { it.index == activeIndex }
        if (!visible) state.animateScrollToItem(activeIndex)
    }
    LazyRow(
        state = state,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(pages, key = { _, page -> page.id }) { index, page ->
            PageChip(
                page = page,
                index = index,
                selected = page.id == activePageId,
                done = borderedPages[page.id] == true,
                // A confident detection is a tick; an unconfident one is a warning,
                // not a success. Showing both as "done" would be a lie the user only
                // discovers in the exported PDF.
                needsReview = needsCheck(page),
                onClick = { onSelectPage(index) }
            )
        }
    }
}

/**
 * One page in the border pass: a thumbnail, its number, and how sure we are.
 *
 * Three states, and the third is the point of the whole strip: a page the user has
 * confirmed is *filled*, a page the detector was confident about is *outlined in the
 * accent colour*, and a page it was not sure about is *amber* with a "!" — so the
 * order to work through is visible at a glance instead of being page order.
 */
@Composable
private fun PageChip(
    page: ScannedPage,
    index: Int,
    selected: Boolean,
    done: Boolean,
    needsReview: Boolean,
    onClick: () -> Unit
) {
    val interactions = remember { MutableInteractionSource() }
    val ring by animateFloatAsState(if (selected) 2.5f else 0f, spring(), label = "pageRing")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(56.dp)
            .pressScale(interactions, pressedScale = 0.92f)
            .clip(DocShapes.thumbnail)
            .clickable(interactionSource = interactions, indication = ripple(), onClick = onClick)
    ) {
        Surface(
            shape = DocShapes.thumbnail,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
        ) {
            Box {
                PageThumbnail(
                    page = page,
                    reqWidth = 160,
                    reqHeight = 220,
                    modifier = Modifier.fillMaxSize()
                )
                if (ring > 0.5f) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .border(ring.dp, MaterialTheme.colorScheme.primary, DocShapes.thumbnail)
                    )
                }
                val accents = LocalDocAccents.current
                val badgeColor = when {
                    needsReview -> accents.scan
                    done -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.85f)
                }
                val badgeContent = when {
                    needsReview -> accents.onScan
                    done -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Surface(
                    shape = DocShapes.pill,
                    color = badgeColor,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            if (needsReview) "!" else "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = if (needsReview) FontWeight.Bold else FontWeight.Normal,
                            color = badgeContent
                        )
                    }
                }
            }
        }
    }
}

/** Small labelled tool button used for Full page / Auto. */
@Composable
private fun TonalGeoButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactions = remember { MutableInteractionSource() }
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = DocShapes.iconButton,
        modifier = modifier
            .height(46.dp)
            .pressScale(interactions, pressedScale = 0.95f)
            .clip(DocShapes.iconButton)
            .clickable(
                interactionSource = interactions,
                indication = ripple(),
                enabled = enabled,
                onClick = onClick
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** −, percentage, + and fit, in one pill. */
@Composable
private fun ZoomCluster(
    zoom: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = DocShapes.pill,
        modifier = modifier.height(46.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize()
        ) {
            ZoomStep(Icons.Default.Remove, "Zoom out", onZoomOut, enabled = zoom > 1.001f)
            Text(
                "${(zoom * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.width(42.dp),
                textAlign = TextAlign.Center
            )
            ZoomStep(Icons.Default.Add, "Zoom in", onZoomIn, enabled = zoom < MAX_ZOOM - 0.001f)
            ZoomStep(Icons.Default.FitScreen, "Fit", onReset, enabled = zoom > 1.001f)
        }
    }
}

@Composable
private fun ZoomStep(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(38.dp)
            .clip(DocShapes.pill)
            .pressScale(interactions, pressedScale = 0.9f)
            .clickable(
                interactionSource = interactions,
                indication = ripple(),
                enabled = enabled,
                onClick = onClick
            )
    ) {
        Icon(
            icon,
            contentDescription = description,
            modifier = Modifier.size(17.dp),
            tint = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            }
        )
    }
}

/** The control surface shared by both stages. */
@Composable
private fun EditorSheet(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = DocShapes.sheet,
        shadowElevation = 14.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            content = content
        )
    }
}

/** Full-screen progress while the document is baked. */
@Composable
private fun ApplyingOverlay(current: Int, total: Int) {
    val accents = LocalDocAccents.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = DocShapes.card,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shadowElevation = 18.dp,
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoadingDots(color = accents.scan, dotSize = 10.dp)
                Spacer(Modifier.height(18.dp))
                Text(
                    "Applying filters",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Page $current of $total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(18.dp))
                PageProgressBar(
                    progress = if (total == 0) 0f else current.toFloat() / total,
                    modifier = Modifier.width(180.dp)
                )
            }
        }
    }
}

/** Largest preview magnification in the border editor. */
private const val MAX_ZOOM = 5f

/** Multiplier applied by the +/- zoom buttons. */
private const val ZOOM_FACTOR = 1.6f

/**
 * How close a touch has to be to a handle to grab it, in **screen dp**.
 *
 * A fingertip is ~9 mm, the handles are drawn at 22–28 dp, and the old test was a
 * normalised 0.05 (≈ 22 % of the frame per axis) — which is why a drag started from
 * the middle of the page used to seize whichever corner was nearest. 28 dp radius is
 * a little larger than the dot itself (easy to hit, still unambiguous) and, unlike
 * the old constant, it is converted to canvas pixels through the zoom, so it stays
 * the same size under the finger at 1x and at 5x.
 */
private const val HANDLE_GRAB_DP = 28f

/**
 * Edge snapping and the minimum gap now live with the drag arithmetic they belong
 * to: `CropHandles.SNAP` and `CropHandles.MIN_GAP`. They moved there so a unit test
 * can drive the same numbers the canvas does — a duplicated constant is how the
 * handles and their hit test drifted apart in the first place.
 */
