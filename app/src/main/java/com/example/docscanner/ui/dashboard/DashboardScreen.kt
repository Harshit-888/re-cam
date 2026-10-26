package com.example.docscanner.ui.dashboard

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.Document
import com.example.docscanner.model.DocumentSortOption
import com.example.docscanner.model.Folder
import com.example.docscanner.model.PdfCompression
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.theme.DocShapes
import com.example.docscanner.theme.entrance
import com.example.docscanner.theme.pressScale
import com.example.docscanner.ui.common.InfoPill
import com.example.docscanner.ui.common.PageThumbnail
import com.example.docscanner.ui.common.PillButton
import com.example.docscanner.ui.common.SectionLabel
import com.example.docscanner.ui.common.TonalPillButton
import com.example.docscanner.ui.pdf.PdfExportSheet
import com.example.docscanner.util.DocumentNaming
import com.example.docscanner.util.DocumentSearch
import com.example.docscanner.util.ImageStore
import com.example.docscanner.util.PageImageLoader
import com.example.docscanner.util.PerfTrace
import com.example.docscanner.util.ThumbnailStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The library.
 *
 * The dashboard answers three questions in the order a returning user asks them:
 * *what did I scan recently* (the grid), *where is that one document* (search and
 * folders), and *can I scan something right now* (the capture action, always in
 * reach). Everything else — sort, layout, trash — is deliberately quiet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: ScannerRepository,
    onNavigateToCamera: (String?) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val documents by repository.documents.collectAsState()
    val folders by repository.folders.collectAsState()
    val settings by repository.settings.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Settings → Scan → "Use in-app camera" off: scanning goes straight to the
    // system photo picker and each picked image becomes a page. Same import path
    // as the camera screen's gallery button, so quality settings apply here too.
    val systemPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val quality = settings.imageQuality
        scope.launch {
            val pages = withContext(Dispatchers.IO) {
                uris.mapNotNull { uri ->
                    importPickedImage(context, uri, quality.maxDimension, quality.jpegQuality)
                }
            }
            if (pages.isEmpty()) {
                Toast.makeText(context, "Could not import the selected images", Toast.LENGTH_LONG).show()
                return@launch
            }
            val created = repository.addDocument(DocumentNaming.defaultScanTitle(), null, pages)
            onNavigateToDetail(created.id)
        }
    }

    val startScan: () -> Unit = {
        if (settings.useInAppCamera) {
            onNavigateToCamera(null)
        } else {
            systemPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFolderId by rememberSaveable { mutableStateOf<String?>(null) }
    var sortOption by rememberSaveable { mutableStateOf(DocumentSortOption.MODIFIED_NEWEST) }
    var isGridView by rememberSaveable { mutableStateOf(true) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var moveDocId by remember { mutableStateOf<String?>(null) }

    // Multi-select for merging.
    //
    // `selection` is an ordered **list**, not a set: the tap order is the order the
    // pages land in the merged PDF, and the badge on each card shows that order.
    // Choosing order by tapping is the cheapest way to make it explicit without
    // adding a reorder screen; the sheet shows the result before anything is
    // written.
    var isSelecting by remember { mutableStateOf(false) }
    var selection by remember { mutableStateOf<List<String>>(emptyList()) }
    var mergeRequest by remember { mutableStateOf<List<Document>>(emptyList()) }
    val toggleSelection: (String) -> Unit = { id ->
        selection = if (selection.contains(id)) selection - id else selection + id
    }
    val endSelection: () -> Unit = {
        isSelecting = false
        selection = emptyList()
    }
    // Back leaves selection, not the screen. Without this the gesture that
    // everyone uses to cancel a mode would navigate away from the library with
    // documents still checked.
    BackHandler(enabled = isSelecting) { endSelection() }

    // The index is rebuilt when the library changes, never when the query does: a
    // keystroke is now an index lookup instead of a scan of every page's text.
    //
    // It is also built only once someone searches, and off the frame thread.
    // Constructing it tokenises every page's recognised text, and the one-time
    // OCR backfill emits a new library snapshot per page — building it in
    // composition meant the home screen paid for a full re-index of the library
    // two dozen times at startup, on the thread that draws the list.
    val queryActive = searchQuery.isNotBlank()
    var searchIndex by remember { mutableStateOf<DocumentSearch.Index?>(null) }
    LaunchedEffect(documents, queryActive) {
        if (!queryActive) return@LaunchedEffect
        searchIndex = withContext(Dispatchers.Default) { DocumentSearch.Index(documents) }
    }

    // Ranking runs off the composition, after a debounce.
    //
    // It used to run inside `remember(searchQuery)`, which meant every single
    // keystroke paid for a whole BM25 pass on the frame thread — measured in
    // dropped frames on a library of a few hundred pages, and wasted entirely
    // because "d", "do", "doc" were each ranked in turn. The debounce skips the
    // intermediate prefixes; the dispatcher keeps even the final pass off the
    // thread that has to draw the caret.
    var searchHits by remember { mutableStateOf<List<DocumentSearch.Hit>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    LaunchedEffect(searchIndex, searchQuery) {
        val query = searchQuery
        if (query.isBlank()) {
            searchHits = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        val index = searchIndex
        if (index == null) {
            // The very first search of the session still has its index being
            // built. Stay in the "searching" state so the empty library does not
            // claim that nothing matched for those few frames.
            isSearching = true
            return@LaunchedEffect
        }
        isSearching = true
        delay(SEARCH_DEBOUNCE_MS)
        searchHits = withContext(Dispatchers.Default) { index.search(query) }
        isSearching = false
    }

    // The full hit, not just its text: a result now carries the ranges that matched,
    // so the card can emphasise the words instead of showing an unexplained sentence.
    val hitsByDocument = remember(searchHits) {
        searchHits.mapNotNull { hit -> hit.snippet?.let { hit.document.id to hit } }.toMap()
    }

    // Keyed on *whether* a query is active, not on the query text: while the user
    // is typing, `searchHits` has not changed yet, so the list must stay exactly
    // as it was rather than being re-filtered and re-sorted for every character.
    val activeDocs = remember(documents, searchHits, queryActive, selectedFolderId, sortOption) {
        val inFolder = if (!queryActive) {
            documents.filterNot { it.isTrash }
        } else {
            // Relevance order is the point of a search; the sort control does not
            // get to re-alphabetise the results.
            searchHits.map { it.document }
        }
        inFolder
            .filter { doc -> selectedFolderId == null || doc.folderId == selectedFolderId }
            .let { docs ->
                if (queryActive) {
                    docs
                } else {
                    docs.sortedWith { a, b ->
                        when (sortOption) {
                            DocumentSortOption.MODIFIED_NEWEST -> b.modifiedDate.compareTo(a.modifiedDate)
                            DocumentSortOption.MODIFIED_OLDEST -> a.modifiedDate.compareTo(b.modifiedDate)
                            DocumentSortOption.CREATED_NEWEST -> b.createdDate.compareTo(a.createdDate)
                            DocumentSortOption.TITLE_AZ -> a.title.compareTo(b.title, ignoreCase = true)
                            DocumentSortOption.TITLE_ZA -> b.title.compareTo(a.title, ignoreCase = true)
                        }
                    }
                }
            }
    }

    // One formatted label per document per library change, instead of one per
    // card per frame: `SimpleDateFormat.format` allocates a String and a Date on
    // every call, and a fling asks for a dozen cards a second.
    val dateLabels = remember(documents) {
        val short = SimpleDateFormat("MMM d", Locale.getDefault())
        val long = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
        documents.associate { doc ->
            val date = Date(doc.modifiedDate)
            doc.id to DateLabels(short.format(date), long.format(date))
        }
    }

    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()

    // The cover's pixel size is derived from the screen, not guessed.
    //
    // It used to be a literal 440 x 560. A two-column cell is ~150-180 dp wide,
    // which is 460-630 px at 3x-3.5x — so every cover was *upscaled* into its card
    // and looked soft. It also has to agree with the request the prefetcher makes,
    // otherwise the prefetched bitmap is a cache miss.
    val coverSize = rememberGridCoverSize()
    val listCoverSize = rememberListCoverSize()

    // Warm the memory cache for the tiles around the viewport.
    //
    // This is the difference between "the decoder is chasing the scroll" and "the
    // scroll finds pixels". Three details keep it from *causing* jank instead:
    //
    // * it fires once per **row** that crosses the viewport edge, not once per
    //   item, so a fling does not restart it every other frame;
    // * each prefetch is `launch`ed instead of awaited, so a row that takes 40 ms
    //   to decode never delays the next row's tracking;
    // * a tile already in memory costs one `LruCache` lookup and no coroutine at
    //   all — which is the common case when scrolling back up.
    val docsForPrefetch = rememberUpdatedState(activeDocs)
    LaunchedEffect(gridState, listState, isGridView, coverSize, listCoverSize) {
        // `collect`'s lambda is a `FlowCollector`, not the effect's scope, so the
        // scope has to be taken before it.
        val scope = this
        val perRow = if (isGridView) GRID_COLUMNS else 1
        val size = if (isGridView) coverSize else listCoverSize
        snapshotFlow {
            val (first, last) = if (isGridView) {
                val items = gridState.layoutInfo.visibleItemsInfo
                (items.firstOrNull()?.index ?: -1) to (items.lastOrNull()?.index ?: -1)
            } else {
                val items = listState.layoutInfo.visibleItemsInfo
                (items.firstOrNull()?.index ?: -1) to (items.lastOrNull()?.index ?: -1)
            }
            // Quantised to whole rows: the window only moves when the user has
            // actually scrolled past a row.
            (first / perRow) to (last / perRow)
        }
            .distinctUntilChanged()
            .collect { (firstRow, lastRow) ->
                if (lastRow < 0) return@collect
                val docs = docsForPrefetch.value
                // Downward first: that is the direction a fling usually goes, and
                // it gets first use of the two decode slots.
                for (row in (lastRow + 1)..(lastRow + PREFETCH_ROWS)) {
                    prefetchRow(scope, context, docs, row, perRow, size)
                }
                for (row in (firstRow - PREFETCH_ROWS)..(firstRow - 1)) {
                    prefetchRow(scope, context, docs, row, perRow, size)
                }
            }
    }

    // Fill the memory cache for the first screens as soon as the library exists.
    //
    // Prefetch alone is one step behind a fast flick: it only starts for a row that
    // has already left the top of the screen, so the first gesture after launch used
    // to meet grey tiles all the way down — nothing beyond the first screen was in
    // memory, and a decode takes tens of milliseconds. A settings row is never blank
    // because there is nothing to decode; this is the same promise for a photo.
    //
    // Cheap by construction: twelve covers at ~6 ms each on the decode threads,
    // with a `peek` skip for anything already warm (which is everything the first
    // screen just loaded). It is deliberately *not* gated on
    // idleness — the whole point is to finish before the first fling starts — and it
    // is keyed on the library so a newly scanned document warms its own cover.
    LaunchedEffect(activeDocs, isGridView, coverSize, listCoverSize) {
        if (activeDocs.isEmpty()) return@LaunchedEffect
        // Let the first frame, its layout and its own tile requests go first: the
        // loop decodes one page at a time and must not delay the screen itself.
        delay(WARM_START_DELAY_MS)
        val perRow = if (isGridView) GRID_COLUMNS else 1
        val size = if (isGridView) coverSize else listCoverSize
        val last = (perRow * WARM_ROWS - 1).coerceAtMost(activeDocs.lastIndex)
        for (index in 0..last) {
            val cover = activeDocs.getOrNull(index)?.pages?.firstOrNull()?.imageUri
                ?: continue
            if (PageImageLoader.peek(cover, size.width, size.height) != null) continue
            PageImageLoader.load(context, cover, size.width, size.height)
        }
    }

    // Recognise text for pages that predate search (bounded to one batch per app
    // start). Indexing is what makes a scan's words findable at all — the PDFs this
    // app writes contain no text layer.
    //
    // Started late and paused during a scroll. Recognition is an ML Kit inference
    // per page plus a 2400 px decode; running that alongside the first fling is
    // exactly what made the library feel slow right after launch even though the
    // dashboard itself was doing less work than before.
    LaunchedEffect(Unit) {
        delay(OCR_START_DELAY_MS)
        repository.backfillTextInBackground(
            isBusy = { gridState.isScrollInProgress || listState.isScrollInProgress }
        )
    }

    // Tell the debug frame monitor when a gesture starts and ends, so it can print
    // one line per scroll instead of a rolling window that averages the idle time
    // in. No-op in release builds.
    LaunchedEffect(gridState, listState) {
        snapshotFlow { gridState.isScrollInProgress || listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling -> PerfTrace.setScrolling(scrolling) }
    }

    // Pages saved before thumbnails existed still have to be decoded in full the
    // first time they appear. Build their thumbnails in the gaps instead: the work
    // only runs while the list is *idle*, and `collectLatest` cancels it the moment
    // a scroll starts (the loader checks for cancellation between pages), so the
    // repair can never be what makes the fling stutter.
    LaunchedEffect(gridState, listState) {
        snapshotFlow { gridState.isScrollInProgress || listState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest { scrolling ->
                if (scrolling) return@collectLatest
                delay(THUMBNAIL_PRIME_DELAY_MS)
                val uris = repository.documents.value
                    .asSequence()
                    .filterNot { it.isTrash }
                    .flatMap { it.pages.asSequence() }
                    .map { it.imageUri }
                    .toList()
                // Chunks, with a pause between them: a long repair pass must not
                // turn into the CPU pressure this round exists to remove. Each
                // chunk starts by skipping what is already warm.
                var written = 1
                while (written > 0) {
                    written = PageImageLoader.primeThumbnails(context, uris)
                    if (written > 0) delay(PRIME_CHUNK_PAUSE_MS)
                }
            }
    }

    val trashCount = remember(documents) { documents.count { it.isTrash } }
    val pageCount = remember(activeDocs) { activeDocs.sumOf { it.pages.size } }

    // A pinned bar, deliberately.
    //
    // This was a `LargeTopAppBar` with `exitUntilCollapsedScrollBehavior`: it
    // re-measured the bar, cross-faded two title styles and pushed a new content
    // inset through the grid on *every* scroll frame — the most expensive thing a
    // scrolling screen can do — and it slid the app name back down whenever the
    // list was pulled up. The title now simply stays where it is.
    Scaffold(
        // Transparent, deliberately: `Scaffold` is itself a full-screen `Surface`,
        // and the theme root in `MainActivity` already painted this exact colour
        // behind it. Two opaque full-screen rects per frame is 2 x 2.6 MPx of
        // overdraw on a 1080 x 2400 display — about 1-2 ms of fill rate on a
        // mid-range GPU, every frame of every scroll, for a colour nobody sees.
        // `surface` and `background` are the same value in both palettes, so this
        // changes nothing visually; the root Surface still paints the screen.
        containerColor = Color.Transparent,
        topBar = {
            // Selection replaces the bar rather than adding a second one: the title
            // slot becomes the count, the only action that matters (select all) moves
            // into it, and the app name comes back the moment selection ends.
            if (isSelecting) {
                SelectionTopBar(
                    count = selection.size,
                    allSelected = activeDocs.isNotEmpty() && selection.size == activeDocs.size,
                    onCancel = endSelection,
                    onToggleAll = {
                        selection = if (activeDocs.isNotEmpty() && selection.size == activeDocs.size) {
                            emptyList()
                        } else {
                            activeDocs.map { it.id }
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            "Doc Scanner",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { isGridView = !isGridView }) {
                            Icon(
                                if (isGridView) Icons.Outlined.ViewList else Icons.Outlined.GridView,
                                contentDescription = if (isGridView) "Show as list" else "Show as grid"
                            )
                        }
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Outlined.Sort, contentDescription = "Sort")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DocumentSortOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.label) },
                                        trailingIcon = if (option == sortOption) {
                                            { Icon(Icons.Default.Check, contentDescription = null) }
                                        } else {
                                            null
                                        },
                                        onClick = {
                                            sortOption = option
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = onNavigateToTrash) {
                            BadgedBox(
                                badge = {
                                    if (trashCount > 0) {
                                        Badge { Text("$trashCount") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Trash")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        bottomBar = {
            // The action bar takes the FAB's place while selecting: "merge these" is
            // the one thing selection is for, and putting it under the thumb is the
            // whole ergonomic point of a bottom bar.
            if (isSelecting) {
                SelectionActionBar(
                    count = selection.size,
                    mergeEnabled = selection.size >= 2,
                    onMerge = {
                        val docs = selection.mapNotNull { id -> documents.firstOrNull { it.id == id } }
                        if (docs.size >= 2) mergeRequest = docs
                    }
                )
            }
        },
        floatingActionButton = {
            // An extended FAB that reads as the app's one primary verb. The pill
            // shape and the amber-free primary keep it distinct from the cards.
            // Hidden while selecting so two competing actions never share a corner.
            if (!isSelecting) {
                CaptureFab(onClick = startScan)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MagicSearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .entrance(0)
            )

            FolderFilterRow(
                folders = folders,
                selectedFolderId = selectedFolderId,
                onSelectFolder = { selectedFolderId = it },
                onAddFolderClick = { showFolderDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .entrance(1)
            )

            // A row of facts, not a sentence: documents · pages · current sort.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .entrance(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoPill("${activeDocs.size} ${if (activeDocs.size == 1) "document" else "documents"}")
                if (pageCount > 0) {
                    InfoPill("$pageCount ${if (pageCount == 1) "page" else "pages"}")
                }
                Spacer(Modifier.weight(1f))
                Text(
                    sortOption.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(12.dp))

            when {
                activeDocs.isEmpty() -> {
                    // A search that has not answered yet has nothing to report.
                    // Showing "Nothing matches" for the two frames between the
                    // keystroke and the ranked result reads as a glitch.
                    if (!isSearching) {
                        EmptyStateView(
                            isSearch = searchQuery.isNotBlank(),
                            onStartScan = startScan,
                            onTryQuery = { searchQuery = it }
                        )
                    }
                }

                isGridView -> LazyVerticalGrid(
                    columns = GridCells.Fixed(GRID_COLUMNS),
                    state = gridState,
                    contentPadding = PaddingValues(
                        start = GRID_H_PADDING,
                        end = GRID_H_PADDING,
                        top = 4.dp,
                        bottom = 108.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(GRID_H_GAP),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        count = activeDocs.size,
                        key = { index -> activeDocs[index].id },
                        // Reuse slots by type. Without `contentType` the grid tears
                        // down and rebuilds a card every time one crosses the
                        // viewport edge, which is what made fast scrolling stutter.
                        contentType = { "grid-card" }
                    ) { index ->
                        val doc = activeDocs[index]
                        // No per-item entrance animation: cards that fade and
                        // slide in as you scroll re-run that animation for every
                        // item that appears, so the list never settles.
                        DocumentGridCard(
                            document = doc,
                            dateLabel = dateLabels[doc.id]?.short.orEmpty(),
                            coverSize = coverSize,
                            snippet = hitsByDocument[doc.id]?.snippet,
                            snippetHighlights = hitsByDocument[doc.id]?.highlights.orEmpty(),
                            onClick = {
                                if (isSelecting) toggleSelection(doc.id) else onNavigateToDetail(doc.id)
                            },
                            onLongClick = {
                                if (isSelecting) {
                                    toggleSelection(doc.id)
                                } else {
                                    isSelecting = true
                                    selection = listOf(doc.id)
                                }
                            },
                            onDelete = { repository.moveToTrash(doc.id) },
                            onMoveToFolder = { moveDocId = doc.id },
                            selectionMode = isSelecting,
                            selected = selection.contains(doc.id),
                            selectionOrder = selection.indexOf(doc.id) + 1
                        )
                    }
                }

                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 108.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = activeDocs,
                        key = { it.id },
                        contentType = { "list-card" }
                    ) { doc ->
                        DocumentListCard(
                            document = doc,
                            dateLabel = dateLabels[doc.id]?.long.orEmpty(),
                            snippet = hitsByDocument[doc.id]?.snippet,
                            snippetHighlights = hitsByDocument[doc.id]?.highlights.orEmpty(),
                            onClick = {
                                if (isSelecting) toggleSelection(doc.id) else onNavigateToDetail(doc.id)
                            },
                            onLongClick = {
                                if (isSelecting) {
                                    toggleSelection(doc.id)
                                } else {
                                    isSelecting = true
                                    selection = listOf(doc.id)
                                }
                            },
                            onDelete = { repository.moveToTrash(doc.id) },
                            onMoveToFolder = { moveDocId = doc.id },
                            selectionMode = isSelecting,
                            selected = selection.contains(doc.id),
                            selectionOrder = selection.indexOf(doc.id) + 1
                        )
                    }
                }
            }
        }
    }

    moveDocId?.let { docId ->
        val current = documents.find { it.id == docId }
        MoveToFolderDialog(
            folders = folders,
            currentFolderId = current?.folderId,
            onDismiss = { moveDocId = null },
            onSelect = { folderId ->
                repository.moveDocumentToFolder(docId, folderId)
                moveDocId = null
            }
        )
    }

    if (showFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showFolderDialog = false },
            onConfirm = { name, color ->
                repository.addFolder(name, color)
                showFolderDialog = false
            }
        )
    }

    // A merge is the pages of the chosen documents, in tap order, through the same
    // sheet as any other export — so paper, margins, page numbers, name, password
    // and compression all apply to it too. Nothing is written until the sheet's
    // button is pressed, and the sheet lists the order first.
    if (mergeRequest.isNotEmpty()) {
        val mergePages = remember(mergeRequest) { mergeRequest.flatMap { it.pages } }
        PdfExportSheet(
            title = mergeRequest.first().title,
            pages = mergePages,
            settings = settings,
            mergeSources = mergeRequest,
            // Ten documents at lossless page images is a hundred-megabyte
            // attachment. The compression control is visible in the sheet and is
            // not overridden afterwards, so this is a default, not a decision made
            // for the user.
            defaultCompression = PdfCompression.BALANCED,
            onDismiss = {
                mergeRequest = emptyList()
                endSelection()
            }
        )
    }
}

/**
 * The bar that replaces the app name while documents are being picked.
 *
 * Kept separate from the library bar so neither has a conditional inside it: the
 * title slot, the actions and the meaning of "back" are different in the two
 * states, and a bar with a mode switch in it is a bar with two bugs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    count: Int,
    allSelected: Boolean,
    onCancel: () -> Unit,
    onToggleAll: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                if (count == 0) "Select documents" else "$count selected",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancel selection")
            }
        },
        actions = {
            IconButton(onClick = onToggleAll) {
                Icon(
                    Icons.Default.SelectAll,
                    contentDescription = if (allSelected) "Clear selection" else "Select all"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Bottom action bar for selection: what is picked, and the one thing to do with it.
 *
 * A `PillButton` rather than a second bar-level button, so the primary action is
 * the same object as every other primary action in the app.
 */
@Composable
private fun SelectionActionBar(
    count: Int,
    mergeEnabled: Boolean,
    onMerge: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 22.dp, end = 20.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (count == 0) "Nothing selected" else "$count selected",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    when {
                        count == 0 -> "Pick two or more documents"
                        count == 1 -> "Pick one more to merge"
                        else -> "Tap order sets the page order"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(12.dp))
            PillButton(
                text = "Merge PDF",
                icon = Icons.Default.CallMerge,
                enabled = mergeEnabled,
                onClick = onMerge
            )
        }
    }
}

/**
 * The check on a selected card.
 *
 * The number *is* the check: in a merge the order is the whole question, so the
 * badge answers it in place instead of making the user hold an order in their head.
 *
 * Drawn straight into the existing thumbnail overlay — no `Surface`, no fade, no
 * per-tile animation. A checkbox that animates is a checkbox that janks a fling.
 */
@Composable
private fun SelectionBadge(order: Int, modifier: Modifier = Modifier) {
    val selected = order > 0
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    // Same tonal disc as the overflow button it replaces, so an
                    // unselected card looks deliberate rather than half-finished.
                    MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.85f)
                }
            )
    ) {
        if (selected) {
            Text(
                "$order",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/**
 * Starts decodes for the covers of one row, without waiting for them.
 *
 * `launch` on the caller's scope, not `prefetch` inline: the collector has to stay
 * responsive to the next row boundary, and a decode takes tens of milliseconds.
 * Tiles already in memory are skipped, so a row the user is scrolling back over
 * costs nothing at all.
 */
private fun prefetchRow(
    scope: CoroutineScope,
    context: Context,
    docs: List<Document>,
    row: Int,
    perRow: Int,
    size: IntSize
) {
    if (row < 0) return
    val start = row * perRow
    if (start > docs.lastIndex) return
    val end = (start + perRow - 1).coerceAtMost(docs.lastIndex)
    for (index in start..end) {
        val cover = docs.getOrNull(index)?.pages?.firstOrNull()?.imageUri ?: continue
        if (PageImageLoader.peek(cover, size.width, size.height) != null) continue
        scope.launch { PageImageLoader.prefetch(context, cover, size.width, size.height) }
    }
}

/** Pre-formatted dates for one document, built once per library change. */
private data class DateLabels(val short: String, val long: String)

/**
 * Example queries offered when a search finds nothing.
 *
 * Chosen to demonstrate the three things the field understands beyond a plain
 * word: a quoted phrase, tolerance for a misspelling, and a bare year. Tapping one
 * fills the field, so the lesson is a search the user can then edit rather than a
 * tooltip.
 */
private val MAGIC_EXAMPLES = listOf("invoice", "\"pan card\"", "2024")

/** Columns in the library grid. The cover size below is derived from it. */
private const val GRID_COLUMNS = 2

/** Horizontal inset of the grid. Shared with [rememberGridCoverSize]. */
private val GRID_H_PADDING = 20.dp

/** Gap between the two grid columns. Shared with [rememberGridCoverSize]. */
private val GRID_H_GAP = 14.dp

/** Width / height of a grid cover. Shared with [rememberGridCoverSize]. */
private const val COVER_ASPECT = 0.78f

/**
 * Rows of covers to decode beyond the viewport, in both directions.
 *
 * Two rows: far enough that a fling finds warm tiles, close enough that a
 * cancelled prefetch costs almost nothing. Counted in **rows**, not items, so the
 * window moves when the user has actually crossed a row rather than on every
 * item — and it is symmetric, because scrolling back up is as common as scrolling
 * down and costs the same decode.
 */
private const val PREFETCH_ROWS = 2

/**
 * Rows of covers warmed into the memory cache as soon as the library is idle.
 *
 * This is the first-fling fix. Prefetch only starts once a row has *left* the top
 * of the screen, which is one step behind a fast flick: the first gesture after
 * launch used to find grey tiles all the way down, because nothing beyond the
 * first screen was in memory yet and a decode takes tens of milliseconds. A
 * settings row never has that problem — its content is already there. Warming a
 * screen and a half up front makes the first fling find pixels the same way every
 * later one does.
 */
private const val WARM_ROWS = 6

/**
 * How long the library gets to itself before background text recognition starts.
 *
 * Long enough for launch, the first paint and the first scroll to be over; short
 * enough that search finds older pages within seconds of opening the app.
 */
private const val OCR_START_DELAY_MS = 3000L

/**
 * How long the list has to be still before legacy thumbnails are built.
 *
 * Long enough that it never fires between two flicks of the same scroll gesture;
 * short enough that a library is warm by the time anyone browses it.
 */
private const val THUMBNAIL_PRIME_DELAY_MS = 1200L

/**
 * How long the first screen gets before its neighbours are warmed into memory.
 *
 * Three frames. The tiles that are already on screen have issued their own loads
 * during the first composition, so this loop cannot preempt them however early it
 * starts; it only has to wait long enough that the first frame is not competing
 * with it for the decode threads.
 */
private const val WARM_START_DELAY_MS = 48L

/** Pause between thumbnail-repair chunks, so the decoder cannot saturate a core. */
private const val PRIME_CHUNK_PAUSE_MS = 350L

/** Pixel box of one grid cover, derived from the window instead of guessed. */
@Composable
private fun rememberGridCoverSize(): IntSize {
    val density = LocalDensity.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    return remember(density, screenWidthDp) {
        val cellWidthPx = with(density) {
            ((screenWidthDp.dp - GRID_H_PADDING * 2 - GRID_H_GAP) / GRID_COLUMNS).roundToPx()
        }
        val cellHeightPx = (cellWidthPx / COVER_ASPECT).roundToInt()
        // Capped at the stored thumbnail's edge: a larger request could not be
        // served by ThumbnailStore, so it would decode the whole page instead —
        // and on any phone the cover is smaller than this anyway.
        IntSize(
            cellWidthPx.coerceIn(1, ThumbnailStore.LONG_EDGE),
            cellHeightPx.coerceIn(1, ThumbnailStore.LONG_EDGE)
        )
    }
}

/**
 * How long typing has to pause before a query is ranked.
 *
 * Long enough to skip the prefixes of a word the user is still typing, short
 * enough that the result list feels like it is following the keyboard. At 120 ms
 * a normal typist never sees a stale list, and a slow search never runs twice for
 * one word.
 */
private const val SEARCH_DEBOUNCE_MS = 120L

/** The one action the whole app is for, sized and shaped to be unmissable. */
@Composable
private fun CaptureFab(onClick: () -> Unit) {
    val interactions = remember { MutableInteractionSource() }
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = DocShapes.fab,
        shadowElevation = 12.dp,
        modifier = Modifier
            .pressScale(interactions, pressedScale = 0.94f)
            .clip(DocShapes.fab)
            .clickable(interactionSource = interactions, indication = ripple(), onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                "Scan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * The Magic Search field.
 *
 * A sparkle, not a magnifier, and a name that says what it does: this field finds a
 * document when the query is a prefix, a plural, a misspelling or a different way of
 * writing the same word (see [com.example.docscanner.util.DocumentSearch]). A
 * magnifier would promise an exact search and then quietly be cleverer than that.
 */
@Composable
fun MagicSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = scheme.surfaceContainerHigh,
        unfocusedContainerColor = scheme.surfaceContainerHigh,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent
    )
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                "Magic Search",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            // A sparkle rather than a magnifier: this field also matches words the
            // user did not type exactly, and a magnifier would promise an exact
            // search. It takes the accent colour once there is a query.
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = if (query.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            AnimatedVisibility(visible = query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        shape = DocShapes.pill,
        textStyle = MaterialTheme.typography.bodyMedium,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {}),
        // Remembered: the search field recomposes on every keystroke, and building
        // a fresh `TextFieldColors` each time is an allocation per character for
        // a value that only changes with the theme.
        colors = fieldColors,
        modifier = modifier.height(54.dp)
    )
}

/**
 * Folder filter as pills with a sliding selection.
 *
 * A `FilterChip` shows selection with a tint; a pill that fills with colour and
 * tightens under the finger shows it with *weight*, which survives being next to
 * six other pills.
 */
@Composable
fun FolderFilterRow(
    folders: List<Folder>,
    selectedFolderId: String?,
    onSelectFolder: (String?) -> Unit,
    onAddFolderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        item {
            FolderPill(
                label = "All",
                selected = selectedFolderId == null,
                onClick = { onSelectFolder(null) }
            )
        }
        items(folders, key = { it.id }) { folder ->
            FolderPill(
                label = folder.name,
                selected = selectedFolderId == folder.id,
                color = Color(folder.colorHex),
                onClick = {
                    onSelectFolder(if (selectedFolderId == folder.id) null else folder.id)
                }
            )
        }
        item {
            FolderPill(
                label = "New folder",
                selected = false,
                leadingIcon = Icons.Default.Add,
                onClick = onAddFolderClick,
                tonal = true
            )
        }
    }
}

@Composable
private fun FolderPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color? = null,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    tonal: Boolean = false
) {
    val interactions = remember { MutableInteractionSource() }
    val container by androidx.compose.animation.animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.primary
            tonal -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = androidx.compose.animation.core.tween(220),
        label = "folderPillColor"
    )
    val content by androidx.compose.animation.animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.onPrimary
            tonal -> MaterialTheme.colorScheme.onSecondaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = androidx.compose.animation.core.tween(220),
        label = "folderPillContent"
    )
    Surface(
        color = container,
        contentColor = content,
        shape = DocShapes.pill,
        modifier = Modifier
            .pressScale(interactions, pressedScale = 0.94f)
            .clip(DocShapes.pill)
            .clickable(interactionSource = interactions, indication = ripple(), onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            if (color != null) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.width(8.dp))
            }
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

/** Thumbnail box in a list row. Its pixel request is derived from these. */
private val LIST_COVER_WIDTH = 52.dp
private val LIST_COVER_HEIGHT = 66.dp

/**
 * Pixel box of a list row's tile.
 *
 * A literal 160 x 200 was 46 % larger than the 52 x 66 dp box on a 3x display —
 * a wasted decode and a wasted cache entry, per row.
 */
@Composable
private fun rememberListCoverSize(): IntSize {
    val density = LocalDensity.current
    return remember(density) {
        with(density) {
            IntSize(LIST_COVER_WIDTH.roundToPx(), LIST_COVER_HEIGHT.roundToPx())
        }
    }
}

/** Grid card: the page itself is the card, with the metadata in a quiet footer. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentGridCard(
    document: Document,
    dateLabel: String,
    coverSize: IntSize,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMoveToFolder: () -> Unit,
    onLongClick: () -> Unit = {},
    snippet: String? = null,
    /** Ranges inside [snippet] that matched, for emphasis. */
    snippetHighlights: List<IntRange> = emptyList(),
    selectionMode: Boolean = false,
    selected: Boolean = false,
    /** 1-based position in the merge order; 0 when not selected. */
    selectionOrder: Int = 0
) {
    var showMenu by remember { mutableStateOf(false) }
    val interactions = remember { MutableInteractionSource() }

    // A `Box`, not a `Surface`, no press-scale, and no background under the photo.
    //
    // `Surface` composes semantics, clip, background and elevation nodes for every
    // tile that scrolls in, and `Modifier.pressScale` is a `composed` factory — the
    // pattern Compose's own docs warn about inside lazy lists — which adds a nested
    // composition and a running animation per item. Neither buys anything visible
    // on a photo tile: the ripple already says "pressed".
    //
    // Nor is the card filled behind its own cover. The card colour is painted by
    // the footer, the only part of the card a photo does not already cover; filling
    // the whole card and then drawing a half-megapixel thumbnail on top is one
    // extra full-tile fill per card, eight-ish of them per frame of a fling. Where
    // the photo has not decoded yet, `PageThumbnail`'s `placeholder` paints exactly
    // the colour that used to be underneath it, so nothing looks different.
    val coverColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val cover = document.pages.firstOrNull()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DocShapes.card)
            // Long-press starts selection, the gesture every gallery uses. Same node
            // count as `clickable`, so a scrolling tile costs exactly what it did.
            .combinedClickable(
                interactionSource = interactions,
                indication = ripple(),
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(COVER_ASPECT)
            ) {
                if (cover != null) {
                    PageThumbnail(
                        page = cover,
                        modifier = Modifier.fillMaxSize(),
                        reqWidth = coverSize.width,
                        reqHeight = coverSize.height,
                        fallbackIconSize = 44.dp,
                        placeholder = coverColor
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(coverColor))
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.Center)
                    )
                }
                if (document.pages.size > 1) {
                    InfoPill(
                        text = "${document.pages.size} pages",
                        container = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    )
                }
                if (selectionMode) {
                    SelectionBadge(
                        order = if (selected) selectionOrder else 0,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    )
                } else {
                    Box(modifier = Modifier.align(Alignment.TopEnd)) {
                        TonalMenuButton(onClick = { showMenu = true })
                        DocumentMenu(
                            expanded = showMenu,
                            onDismiss = { showMenu = false },
                            onMoveToFolder = onMoveToFolder,
                            onDelete = onDelete
                        )
                    }
                }
            }

            Column(
                Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 12.dp)
            ) {
                Text(
                    document.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                if (snippet == null) {
                    Text(
                        dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                } else {
                    // Why this document matched: the sentence around the hit, with
                    // the matched words marked. Without it a search result is just a
                    // thumbnail the user has to re-open.
                    SnippetText(
                        text = snippet,
                        highlights = snippetHighlights,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        highlight = MaterialTheme.colorScheme.primary,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

/** List card: a compact row that keeps the thumbnail's shape language. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentListCard(
    document: Document,
    dateLabel: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMoveToFolder: () -> Unit,
    onLongClick: () -> Unit = {},
    snippet: String? = null,
    /** Ranges inside [snippet] that matched, for emphasis. */
    snippetHighlights: List<IntRange> = emptyList(),
    selectionMode: Boolean = false,
    selected: Boolean = false,
    selectionOrder: Int = 0
) {
    var showMenu by remember { mutableStateOf(false) }
    val interactions = remember { MutableInteractionSource() }
    val listCoverSize = rememberListCoverSize()

    // Same reasoning as `DocumentGridCard`: a list row that scrolls should cost a
    // list row, not a Surface plus a composed modifier plus two clips.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DocShapes.cardSmall)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .combinedClickable(
                interactionSource = interactions,
                indication = ripple(),
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = LIST_COVER_WIDTH, height = LIST_COVER_HEIGHT)
                    .clip(DocShapes.thumbnail),
                contentAlignment = Alignment.Center
            ) {
                val cover = document.pages.firstOrNull()
                if (cover != null) {
                    PageThumbnail(
                        page = cover,
                        modifier = Modifier.fillMaxSize(),
                        reqWidth = listCoverSize.width,
                        reqHeight = listCoverSize.height,
                        fallbackIconSize = 22.dp,
                        placeholder = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                } else {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    document.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${document.pages.size} ${if (document.pages.size == 1) "page" else "pages"} · " +
                        dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (snippet != null) {
                    Spacer(Modifier.height(2.dp))
                    SnippetText(
                        text = snippet,
                        highlights = snippetHighlights,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        highlight = MaterialTheme.colorScheme.primary,
                        maxLines = 2
                    )
                }
            }

            if (selectionMode) {
                SelectionBadge(
                    order = if (selected) selectionOrder else 0,
                    modifier = Modifier.padding(end = 12.dp)
                )
            } else {
                Box {
                    TonalMenuButton(onClick = { showMenu = true })
                    DocumentMenu(
                        expanded = showMenu,
                        onDismiss = { showMenu = false },
                        onMoveToFolder = onMoveToFolder,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

/**
 * A result's matching passage, with the words that matched emphasised.
 *
 * Built as one `AnnotatedString` rather than a row of `Text`s: the passage has to
 * wrap as a sentence, and splitting it into spans would let the highlighter change
 * where the line breaks fall. `remember` keyed on the ranges because a card
 * recomposes on every scroll frame of a fling and rebuilding the string per frame
 * is exactly the kind of per-tile work the library spent three rounds removing.
 */
@Composable
private fun SnippetText(
    text: String,
    highlights: List<IntRange>,
    color: Color,
    highlight: Color,
    maxLines: Int
) {
    if (highlights.isEmpty()) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
        return
    }
    val annotated = remember(text, highlights, highlight) {
        buildAnnotatedString {
            var cursor = 0
            for (range in highlights) {
                val start = range.first.coerceIn(0, text.length)
                val end = (range.last + 1).coerceIn(start, text.length)
                if (start > cursor) append(text.substring(cursor, start))
                if (end > start) {
                    withStyle(SpanStyle(color = highlight, fontWeight = FontWeight.SemiBold)) {
                        append(text.substring(start, end))
                    }
                }
                cursor = maxOf(cursor, end)
            }
            if (cursor < text.length) append(text.substring(cursor))
        }
    }
    Text(
        annotated,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

/** Small round overflow affordance that does not shout on top of a thumbnail. */
@Composable
private fun TonalMenuButton(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(6.dp)
            .size(30.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.85f))
            .clickable(onClick = onClick)
    ) {
        Icon(
            Icons.Default.MoreVert,
            contentDescription = "Options",
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun DocumentMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onMoveToFolder: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("Move to folder…") },
            leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null) },
            onClick = {
                onDismiss()
                onMoveToFolder()
            }
        )
        DropdownMenuItem(
            text = { Text("Move to trash") },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
            onClick = {
                onDismiss()
                onDelete()
            }
        )
    }
}

/**
 * Empty state with a drawn illustration.
 *
 * A single grey icon is the visual equivalent of a shrug. Three overlapping page
 * shapes say "documents live here" in the app's own shape language, and cost
 * nothing to ship — no asset, no density buckets, no download.
 */
@Composable
fun EmptyStateView(
    isSearch: Boolean,
    onStartScan: () -> Unit,
    /** Fills the search field with an example. Absent on screens without a search. */
    onTryQuery: ((String) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmptyPagesArt(isSearch = isSearch)
        Spacer(Modifier.height(26.dp))
        Text(
            if (isSearch) "Nothing matched" else "Your library is empty",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (isSearch) {
                "Magic Search ignores spelling and word endings, so a near miss " +
                    "usually still finds it. Fewer words works better than more."
            } else {
                "Scan a page and it will appear here, ready to sign, export or share."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (isSearch && onTryQuery != null) {
            Spacer(Modifier.height(20.dp))
            // Three examples, each teaching one thing the field can do that a plain
            // `contains` could not: a phrase, a typo-tolerant word, and a year.
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                // Three pills are wider than a small phone; scrolling keeps every
                // example reachable instead of clipping the last one.
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                MAGIC_EXAMPLES.forEach { example ->
                    TonalPillButton(
                        text = example,
                        onClick = { onTryQuery(example) }
                    )
                }
            }
        }
        if (!isSearch) {
            Spacer(Modifier.height(26.dp))
            PillButton(
                text = "Scan your first page",
                icon = Icons.Default.CameraAlt,
                onClick = onStartScan
            )
        }
    }
}

/** Three stacked pages, drawn in theme colours. */
@Composable
private fun EmptyPagesArt(isSearch: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    val container = MaterialTheme.colorScheme.primaryContainer
    val surface = MaterialTheme.colorScheme.surfaceContainerLowest
    val outline = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier = Modifier.size(width = 190.dp, height = 150.dp)) {
        val pageW = size.width * 0.44f
        val pageH = size.height * 0.74f
        val radius = 12.dp.toPx()

        fun page(center: Offset, angle: Float, fill: Color) {
            rotate(degrees = angle, pivot = center) {
                val topLeft = Offset(center.x - pageW / 2f, center.y - pageH / 2f)
                val p = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            left = topLeft.x,
                            top = topLeft.y,
                            right = topLeft.x + pageW,
                            bottom = topLeft.y + pageH,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius)
                        )
                    )
                }
                drawPath(p, color = fill)
                drawPath(p, color = outline, style = Stroke(width = 1.5.dp.toPx()))
                // Ruled lines, so it reads as text rather than a blank card.
                val lines = 4
                repeat(lines) { index ->
                    val y = topLeft.y + pageH * (0.26f + index * 0.16f)
                    drawLine(
                        color = outline,
                        start = Offset(topLeft.x + pageW * 0.16f, y),
                        end = Offset(topLeft.x + pageW * (if (index == lines - 1) 0.55f else 0.84f), y),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        page(Offset(size.width * 0.34f, size.height * 0.52f), -12f, container.copy(alpha = 0.7f))
        page(Offset(size.width * 0.66f, size.height * 0.52f), 12f, container.copy(alpha = 0.85f))
        page(Offset(size.width * 0.5f, size.height * 0.5f), 0f, surface)

        if (isSearch) {
            drawCircle(
                color = primary.copy(alpha = 0.9f),
                radius = 7.dp.toPx(),
                center = Offset(size.width * 0.78f, size.height * 0.24f)
            )
        }
    }
}

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: Long) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    val colors = listOf(0xFF0061A4, 0xFF006874, 0xFF4CAF50, 0xFFFF9800, 0xFFE91E63, 0xFF9C27B0)
    var selectedColor by remember { mutableStateOf(colors.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DocShapes.card,
        title = { Text("New folder", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder name") },
                    singleLine = true,
                    shape = DocShapes.field,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(18.dp))
                SectionLabel("Colour")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colors.forEach { colorVal ->
                        val isSelected = selectedColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .clickable { selectedColor = colorVal }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            PillButton(
                text = "Create",
                enabled = folderName.isNotBlank(),
                onClick = { if (folderName.isNotBlank()) onConfirm(folderName, selectedColor) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Picks the folder a document belongs to. "No folder" clears the assignment,
 * which is how a document also leaves a folder.
 */
@Composable
fun MoveToFolderDialog(
    folders: List<Folder>,
    currentFolderId: String?,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DocShapes.card,
        title = { Text("Move to folder", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                FolderOptionRow(
                    label = "No folder",
                    colorHex = null,
                    selected = currentFolderId == null,
                    onClick = { onSelect(null) }
                )
                folders.forEach { folder ->
                    FolderOptionRow(
                        label = folder.name,
                        colorHex = folder.colorHex,
                        selected = currentFolderId == folder.id,
                        onClick = { onSelect(folder.id) }
                    )
                }
                if (folders.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No folders yet. Create one with the New folder chip.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun FolderOptionRow(
    label: String,
    colorHex: Long?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DocShapes.pill)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (colorHex != null) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(colorHex), CircleShape)
            )
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Current folder",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Decodes one picked image into a page file at the configured quality.
 *
 * Nothing is downscaled behind the user's back beyond the quality preset they
 * chose in Settings, and the returned page carries a real on-disk path — the same
 * contract [com.example.docscanner.ui.camera.ScannerCameraScreen] uses.
 */
private fun importPickedImage(
    context: Context,
    uri: android.net.Uri,
    maxDimension: Int,
    jpegQuality: Int
): ScannedPage? {
    val pageId = UUID.randomUUID().toString()
    val path = ImageStore.importFromUri(context, uri, pageId, maxDimension, jpegQuality)
        ?: return null
    return ScannedPage(
        id = pageId,
        documentId = "",
        pageIndex = 0,
        imageUri = path
    )
}
