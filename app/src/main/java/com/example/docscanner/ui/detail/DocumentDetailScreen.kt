package com.example.docscanner.ui.detail

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import com.example.docscanner.theme.DocShapes
import com.example.docscanner.theme.Motion
import com.example.docscanner.theme.pressScale
import com.example.docscanner.ui.common.ConnectedAction
import com.example.docscanner.ui.common.ConnectedButtonGroup
import com.example.docscanner.ui.common.ExpressiveActionBar
import com.example.docscanner.ui.common.InfoPill
import com.example.docscanner.ui.common.PillButton
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.outlined.BrandingWatermark
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.ui.common.PageThumbnail
import com.example.docscanner.ui.pdf.PdfExportSheet
import com.example.docscanner.ui.signature.SignaturePadDialog
import com.example.docscanner.util.ImageStore
import com.example.docscanner.util.PageRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    docId: String,
    repository: ScannerRepository,
    onNavigateBack: () -> Unit,
    onNavigateToCrop: (pageId: String) -> Unit,
    onNavigateToViewer: (pageId: String) -> Unit,
    onNavigateToMarkup: (pageId: String) -> Unit,
    onAddPageCamera: (docId: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val documents by repository.documents.collectAsState()
    val settings by repository.settings.collectAsState()
    val document = remember(documents, docId) { documents.find { it.id == docId } }

    var showPdfExportSheet by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showWatermarkDialog by remember { mutableStateOf(false) }
    var showIdCardDialog by remember { mutableStateOf(false) }
    var idCardBusy by remember { mutableStateOf(false) }

    // Which page the bottom bar is currently about.
    //
    // The bar used to be four fixed buttons — Add page, Sign, Mark, Export — that
    // said the same thing whether the user had opened a page or not. Sign in
    // particular did not: it stamped page 1 no matter which page you were looking
    // at. A document and a page are different objects with different verbs, so the
    // bar now follows the selection: nothing picked means document verbs, a page
    // picked means page verbs, several picked means batch verbs.
    //
    // Long-press to start selecting, exactly like the library's document grid — one
    // gesture for "act on this thing" in both places. It is its own mode rather than
    // a side effect of tapping a card because tapping a card already means "open it",
    // and a bar that silently changes under your thumb after you come back from the
    // viewer is a bar you cannot trust.
    //
    // A **set**, not a single id. This was a `String?` and it produced the bug worth
    // fixing: long-press page 1, then long-press page 2, and page 1's tick vanished —
    // because page 2 had *replaced* it. Selecting pages one at a time and losing the
    // ones you already picked is not a selection model, it is a cursor.
    var selectedPageIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Signing targets a page, so the dialog needs to know which one was open.
    var signatureTargetId by remember { mutableStateOf<String?>(null) }

    // Exporting a picked subset is a different job from exporting the document, so it
    // gets its own slot: `null` means "the whole document", a list means "exactly
    // these pages". Sharing the boolean above would have made the batch bar's Export
    // quietly export all twelve pages.
    var exportOnly by remember { mutableStateOf<List<ScannedPage>?>(null) }

    // Deleting is destructive and irreversible — the capture, the JPEG bake and the
    // lossless bake all go — and it used to happen on a single tap of a menu item,
    // with no confirmation and no undo. It now takes a *list*, so a batch delete is
    // one confirmation naming a count rather than N dialogs in a row.
    var pagesPendingDelete by remember { mutableStateOf<List<ScannedPage>?>(null) }

    // Resolved from the live page list, not stored as objects: a rotate or an annotate
    // replaces the page, and a snapshot would then act on stale data. Ordered by the
    // document, so "page 3, page 5" is what the user sees, not tap order.
    val selectedPages = remember(document?.pages, selectedPageIds) {
        document?.pages?.filter { it.id in selectedPageIds }.orEmpty()
    }
    val singleSelectedPage = selectedPages.singleOrNull()

    // A deleted, moved-away or re-baked page list can leave the selection pointing at
    // ids that no longer exist. Intersecting is cheap and means the bar can never show
    // page verbs for a page that is not there.
    LaunchedEffect(document?.pages, selectedPageIds) {
        val alive = document?.pages?.map { it.id }?.toSet() ?: return@LaunchedEffect
        if (!alive.containsAll(selectedPageIds)) {
            selectedPageIds = selectedPageIds.intersect(alive)
        }
    }

    // Back leaves selection, not the screen — the same rule as the library grid.
    BackHandler(enabled = selectedPageIds.isNotEmpty()) { selectedPageIds = emptySet() }

    // Gallery import into *this* document. Until now the picker only existed inside
    // the camera screen, so adding a page from the album meant starting a new scan and
    // hoping the app knew where to put it. The decoder is the same one the camera uses
    // (`ImageStore.importFromUri`), so the page lands with the same cap and quality as
    // a capture.
    val galleryImport = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            val quality = repository.currentSettings().imageQuality
            uris.forEach { uri ->
                val pageId = UUID.randomUUID().toString()
                val savedPath = withContext(Dispatchers.IO) {
                    runCatching {
                        ImageStore.importFromUri(
                            context = context,
                            uri = uri,
                            pageId = pageId,
                            maxDimension = quality.maxDimension,
                            quality = quality.jpegQuality
                        )
                    }.getOrNull()
                }
                if (savedPath != null) {
                    repository.addPageToDocument(
                        docId,
                        ScannedPage(
                            id = pageId,
                            documentId = docId,
                            pageIndex = 0,
                            imageUri = savedPath
                        )
                    )
                }
            }
            // Search only finds pages whose words have been read, and an imported page
            // is no different from a captured one.
            repository.indexTextInBackground(docId)
        }
    }

    // ---- drag to reorder ----------------------------------------------------
    //
    // Ordering used to be two icon buttons and a pair of menu items — "move earlier"
    // and "move later", one step at a time. Putting page 9 first meant eight taps, and
    // on a multi-page selection it could not be done at all. Dragging is the gesture
    // the grid already suggests, and it moves a whole selection in one go.
    val gridState = rememberLazyGridState()
    var dragPageId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    // The slot under the finger (highlight only) and where the block would land in
    // the list *without* the moving pages (what is actually committed). Two numbers
    // because they are two coordinate systems, and mixing them up silently reorders
    // the wrong pages.
    var dropIndex by remember { mutableIntStateOf(-1) }
    var dropDestination by remember { mutableIntStateOf(-1) }
    // Where the dragged tile started, in the grid viewport's coordinates — the anchor
    // the finger's displacement is added to.
    var dragAnchor by remember { mutableStateOf(Rect.Zero) }

    fun startPageDrag(pageId: String) {
        // The index comes from the layout, not from the tile's captured position: this
        // callback is invoked from a `pointerInput` block that outlives any single
        // composition, and a stale index after a reorder would highlight the wrong slot.
        val info = gridState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == pageId }
            ?: return
        dragAnchor = Rect(
            offset = Offset(info.offset.x.toFloat(), info.offset.y.toFloat()),
            size = Size(info.size.width.toFloat(), info.size.height.toFloat())
        )
        dragOffset = Offset.Zero
        dropIndex = info.index
        // Nothing to commit until the finger actually reaches another slot: picking a
        // tile up and putting it back must not reorder anything.
        dropDestination = -1
        dragPageId = pageId
    }

    /**
     * Which slot the finger is over, and on which side of its midpoint.
     *
     * Midpoint, not "the item it is over": dropping on the top half of a tile means
     * "before this one" and the bottom half means "after it", which is what makes a
     * single-row nudge possible without aiming at the gap between two tiles.
     */
    fun updatePageDrag(delta: Offset) {
        val dragging = dragPageId ?: return
        val pages = document?.pages ?: return
        dragOffset += delta
        val centre = Offset(
            dragAnchor.left + dragAnchor.width / 2f + dragOffset.x,
            dragAnchor.top + dragAnchor.height / 2f + dragOffset.y
        )
        val hit = gridState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
            val left = item.offset.x.toFloat()
            val top = item.offset.y.toFloat()
            centre.x >= left && centre.x < left + item.size.width &&
                centre.y >= top && centre.y < top + item.size.height
        } ?: return
        if (hit.key == dragging) return
        val movingIds = selectedPageIds.ifEmpty { setOf(dragging) }
        // Destination in the coordinates of the list *without* the moving pages: how
        // many pages that are staying sit before the hovered slot, plus one if the
        // finger is past its midpoint.
        val past = centre.y >= hit.offset.y + hit.size.height / 2f
        val stayingBefore = pages.take(hit.index).count { it.id !in movingIds }
        dropIndex = hit.index
        dropDestination = (stayingBefore + if (past) 1 else 0).coerceIn(0, pages.size)
    }

    fun commitPageDrag() {
        val dragging = dragPageId ?: return
        val destination = dropDestination
        val movingIds = selectedPageIds.ifEmpty { setOf(dragging) }
        dragPageId = null
        dragOffset = Offset.Zero
        dropIndex = -1
        dropDestination = -1
        if (destination < 0) return
        val pages = document?.pages ?: return
        // The block keeps the order the pages already had: they travelled together.
        val ordered = pages.map { it.id }.filter { it in movingIds }
        repository.movePages(docId, ordered, destination)
    }

    fun cancelPageDrag() {
        dragPageId = null
        dragOffset = Offset.Zero
        dropIndex = -1
        dropDestination = -1
    }

    /** Adds or removes one page. Tapping a ticked page un-ticks it. */
    fun toggleSelection(pageId: String) {
        selectedPageIds = if (pageId in selectedPageIds) {
            selectedPageIds - pageId
        } else {
            selectedPageIds + pageId
        }
    }

    // One page at a time, and a page that cannot be re-baked is skipped rather than
    // abandoning the batch: rotating nine pages and silently doing none because the
    // tenth has a missing file is worse than rotating nine.
    suspend fun rotateOne(page: ScannedPage) {
        val current = repository.getDocumentById(docId)
            ?.pages?.find { it.id == page.id } ?: return
        val baked = PageRenderer.bake(
            context,
            current.copy(rotationDegrees = (current.rotationDegrees + 90f) % 360f)
        )
        if (baked != null) {
            repository.updatePage(docId, page.id) { baked }
        }
    }

    // Re-bake so the rotation is visible immediately in the grid, the viewer and the
    // PDF — not just stored as a flag. One definition, because the card's rotate
    // button, the selection bar's rotate and the viewer's rotate must not drift apart.
    fun rotatePages(pages: List<ScannedPage>) {
        if (pages.isEmpty()) return
        scope.launch {
            pages.forEach { page ->
                rotateOne(page)
            }
        }
    }

    // The document can vanish underneath this screen: the last page was deleted,
    // it was trashed or purged from another screen, or a restore replaced it.
    // Landing on "Document not found" is a dead end that needs a manual back
    // press; leaving is what the user was going to do anyway.
    LaunchedEffect(document == null) {
        if (document == null) onNavigateBack()
    }

    if (document == null) {
        // One frame, while the effect above runs. Matching the app background
        // avoids a flash of unrelated colour.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // While pages are selected the title says *which*: the bar below
                    // has room for verbs, not for the subject they apply to, and
                    // "Rotate" with no subject is a decision made blind. One page is
                    // named; several are counted.
                    val title = when {
                        selectedPages.size > 1 -> "${selectedPages.size} pages selected"
                        singleSelectedPage != null ->
                            "Page ${singleSelectedPage.pageIndex + 1} of ${document.pages.size}"
                        else -> document.title
                    }
                    // The count animates instead of jumping: ticking a fourth page
                    // should look like the number became four, not like a different
                    // string appeared.
                    AnimatedContent(
                        targetState = title,
                        transitionSpec = {
                            (fadeIn(tween(160)) + slideInVertically(tween(200)) { it / 3 })
                                .togetherWith(fadeOut(tween(100)))
                        },
                        label = "detailTitle"
                    ) { current ->
                        Text(
                            text = current,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedPageIds.isNotEmpty()) {
                                selectedPageIds = emptySet()
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            if (selectedPageIds.isNotEmpty()) {
                                Icons.Default.Close
                            } else {
                                Icons.Default.ArrowBack
                            },
                            contentDescription = if (selectedPageIds.isNotEmpty()) {
                                "Clear selection"
                            } else {
                                "Back"
                            }
                        )
                    }
                },
                actions = {
                    // While selecting, the app bar belongs to the selection: select all,
                    // and nothing else. Leaving rename/ID-card/PDF next to a live
                    // selection is how a tap meant for "tick this page" turns into an
                    // export dialog.
                    AnimatedContent(
                        targetState = selectedPageIds.isNotEmpty(),
                        transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(100)) },
                        label = "detailActions"
                    ) { selecting ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selecting) {
                                val allSelected = selectedPages.size == document.pages.size
                                IconButton(
                                    onClick = {
                                        selectedPageIds = if (allSelected) {
                                            emptySet()
                                        } else {
                                            document.pages.map { it.id }.toSet()
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.SelectAll,
                                        contentDescription = if (allSelected) {
                                            "Clear selection"
                                        } else {
                                            "Select all pages"
                                        }
                                    )
                                }
                            } else {
                                IconButton(onClick = { showRenameDialog = true }) {
                                    Icon(
                                        Icons.Outlined.Edit,
                                        contentDescription = "Edit title and notes"
                                    )
                                }
                                IconButton(onClick = { showPdfExportSheet = true }) {
                                    Icon(
                                        Icons.Outlined.PictureAsPdf,
                                        contentDescription = "Export PDF"
                                    )
                                }
                                // Only meaningful for exactly two pages: front and
                                // back. Shown rather than hidden so the feature is
                                // discoverable, and disabled with a reason rather than
                                // lying about what a tap will do.
                                IconButton(
                                    onClick = { showIdCardDialog = true },
                                    enabled = document.pages.size == 2 && !idCardBusy
                                ) {
                                    Icon(
                                        Icons.Outlined.CreditCard,
                                        contentDescription = if (document.pages.size == 2) {
                                            "Combine the two pages as an ID card"
                                        } else {
                                            "ID card needs exactly two pages"
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Three bars, one height, one fade — document verbs, one page's verbs,
            // several pages' verbs. All three are built from the same expressive
            // action bar, so a cell never moves house when the selection changes
            // width; only the set of cells does.
            //
            // Keyed on *ids*, never on `ScannedPage` objects: rotating or annotating
            // replaces the object, and keying on the object would re-run the
            // transition — a fading bar under a tap that never asked for one.
            AnimatedContent(
                targetState = detailBarMode(selectedPageIds),
                transitionSpec = {
                    (fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 6 })
                        .togetherWith(fadeOut(tween(120)))
                },
                label = "detailActionBar"
            ) { mode ->
                // Resolved from the mode's ids, so the bar that is leaving keeps
                // acting on what it was composed for until it has faded out.
                val page = (mode as? DetailBarMode.Single)?.let { key ->
                    document.pages.firstOrNull { it.id == key.id }
                }
                val batch = (mode as? DetailBarMode.Multiple)?.let { key ->
                    document.pages.filter { it.id in key.ids }
                }

                ExpressiveActionBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when {
                            batch != null && batch.size > 1 -> {
                                // Several pages, so only verbs that make sense for
                                // several pages. Crop and annotate are per-page
                                // decisions and quietly applying one page's crop to a
                                // set would be the worst kind of batch action.
                                ConnectedButtonGroup(
                                    actions = listOf(
                                        ConnectedAction(
                                            label = "Rotate",
                                            icon = Icons.Default.RotateRight,
                                            onClick = { rotatePages(batch) }
                                        ),
                                        ConnectedAction(
                                            label = "Export",
                                            icon = Icons.Outlined.PictureAsPdf,
                                            onClick = { exportOnly = batch }
                                        ),
                                        ConnectedAction(
                                            label = "Delete",
                                            icon = Icons.Default.Delete,
                                            onClick = { pagesPendingDelete = batch },
                                            tintColor = MaterialTheme.colorScheme.error
                                        )
                                    )
                                )
                            }

                            page != null -> {
                                // The page's own verbs, in the order a scan needs
                                // them: fix the crop and the look, add ink, sign it —
                                // then everything rare in More. One connected group,
                                // so the four cells read as one object with four
                                // ends rather than four separate buttons.
                                ConnectedButtonGroup(
                                    actions = listOf(
                                        ConnectedAction(
                                            label = "Crop",
                                            icon = Icons.Outlined.Crop,
                                            onClick = {
                                                selectedPageIds = emptySet()
                                                onNavigateToCrop(page.id)
                                            }
                                        ),
                                        ConnectedAction(
                                            label = "Annotate",
                                            icon = Icons.Outlined.Draw,
                                            onClick = {
                                                selectedPageIds = emptySet()
                                                onNavigateToMarkup(page.id)
                                            }
                                        ),
                                        ConnectedAction(
                                            label = "Sign",
                                            icon = Icons.Default.Create,
                                            onClick = { signatureTargetId = page.id }
                                        ),
                                        ConnectedAction(
                                            label = "More",
                                            icon = Icons.Default.MoreVert,
                                            // No `onClick`: this cell *is* the menu, so
                                            // there is no chevron and no second tap.
                                            menuContent = { dismiss ->
                                                DropdownMenuItem(
                                                    text = { Text("Open full screen") },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.OpenInFull,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    onClick = {
                                                        dismiss()
                                                        val id = page.id
                                                        selectedPageIds = emptySet()
                                                        onNavigateToViewer(id)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Rotate 90°") },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.RotateRight,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    onClick = {
                                                        dismiss()
                                                        rotatePages(listOf(page))
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Watermark all pages") },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Outlined.BrandingWatermark,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    onClick = {
                                                        dismiss()
                                                        showWatermarkDialog = true
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            "Delete page",
                                                            color = MaterialTheme.colorScheme.error
                                                        )
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.Delete,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    },
                                                    onClick = {
                                                        dismiss()
                                                        pagesPendingDelete = listOf(page)
                                                    }
                                                )
                                            }
                                        )
                                    )
                                )
                            }

                            else -> {
                                // Nothing is picked, so only two things can be true of
                                // the document: it can grow, or it can become a PDF.
                                // Sign and Mark used to sit here and both were lies at
                                // this level — Mark stamps every page, and Sign stamped
                                // page 1 regardless of which page you were reading.
                                ConnectedButtonGroup(
                                    actions = listOf(
                                        ConnectedAction(
                                            label = "Add page",
                                            icon = Icons.Default.AddAPhoto,
                                            onClick = { onAddPageCamera(docId) },
                                            // Tapping the cell still goes straight to
                                            // the camera — the common case stays one
                                            // tap. The chevron on its trailing edge is a
                                            // split button: the gallery used to be
                                            // reachable only by starting a *new* scan
                                            // from the camera screen, never into the
                                            // document already open.
                                            menuContent = { dismiss ->
                                                DropdownMenuItem(
                                                    text = { Text("Take a photo") },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.AddAPhoto,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    onClick = {
                                                        dismiss()
                                                        onAddPageCamera(docId)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Import from gallery") },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.PhotoLibrary,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    onClick = {
                                                        dismiss()
                                                        galleryImport.launch(
                                                            PickVisualMediaRequest(
                                                                ActivityResultContracts
                                                                    .PickVisualMedia.ImageOnly
                                                            )
                                                        )
                                                    }
                                                )
                                            }
                                        ),
                                        ConnectedAction(
                                            label = "Export PDF",
                                            icon = Icons.Outlined.PictureAsPdf,
                                            onClick = { showPdfExportSheet = true },
                                            emphasized = true
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // A row of facts about the document, then the pages themselves. The
            // title lives in the app bar, so the body starts with substance.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Scrollable, not fixed: a long date in a verbose locale plus a
                    // "Watermarked" pill is wider than a small phone, and pills that
                    // get clipped look like a layout bug rather than an overflow.
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoPill(
                    text = "${document.pages.size} ${if (document.pages.size == 1) "page" else "pages"}",
                    container = MaterialTheme.colorScheme.primaryContainer,
                    content = MaterialTheme.colorScheme.onPrimaryContainer
                )
                // Remembered, not built in composition: `SimpleDateFormat` parses
                // its pattern and loads a `DateFormatSymbols` when constructed, and
                // the previous line did that on every recomposition of this header.
                // Keyed on the date, because that is the only thing that changes it.
                val modifiedLabel = remember(document.modifiedDate) {
                    SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        .format(Date(document.modifiedDate))
                }
                InfoPill(text = modifiedLabel)
                if (document.pages.any { !it.watermarkText.isNullOrBlank() }) {
                    InfoPill(
                        text = "Watermarked",
                        container = MaterialTheme.colorScheme.tertiaryContainer,
                        content = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            if (document.notes.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    shape = DocShapes.cardSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = document.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // `itemsIndexed`, not `indexOfFirst` per item: the old form scanned
                // the page list once per visible tile on every recomposition, which
                // is quadratic in the only list a long scan makes long.
                itemsIndexed(document.pages, key = { _, page -> page.id }) { index, page ->
                    PageGridItem(
                        // Reorder and delete are animated by placement, not by content:
                        // `animateItem()` moves the surviving tiles to their new slots
                        // and fades the removed one, so a page move reads as a move
                        // instead of as the whole grid blinking. The offset is applied
                        // in the placement phase, which is a layer translation — it
                        // never re-runs the tile's layout.
                        modifier = Modifier.animateItem(),
                        page = page,
                        selected = page.id in selectedPageIds,
                        dragging = dragPageId == page.id,
                        dragOffset = if (dragPageId == page.id) dragOffset else Offset.Zero,
                        dropTarget = dragPageId != null && dropIndex == index &&
                            dragPageId != page.id,
                        onDragStart = { startPageDrag(page.id) },
                        onDrag = { delta -> updatePageDrag(delta) },
                        onDragEnd = { commitPageDrag() },
                        onDragCancel = { cancelPageDrag() },
                        // Tap opens the page — unless something is already picked, in
                        // which case tap *toggles* (the library grid's rule, so the two
                        // grids in the app behave the same way). Long-press toggles
                        // either way.
                        onViewClick = {
                            if (selectedPageIds.isEmpty()) {
                                onNavigateToViewer(page.id)
                            } else {
                                toggleSelection(page.id)
                            }
                        },
                        // Long press starts a selection and never removes a page from
                        // one — it is also how a drag begins, and a long press that
                        // un-picked the page would leave the drag with nothing to move.
                        //
                        // Holding an *unselected* page makes it the selection, so a
                        // drag moves exactly what was held. Holding a page that is
                        // already picked keeps the whole set, so a drag moves all of
                        // it. Tapping a picked page still un-picks it.
                        onSelectClick = {
                            if (page.id !in selectedPageIds) {
                                selectedPageIds = setOf(page.id)
                            }
                        },
                        onCropClick = { onNavigateToCrop(page.id) },
                        onDeleteClick = { pagesPendingDelete = listOf(page) },
                        onRotateClick = { rotatePages(listOf(page)) },
                        onAnnotateClick = { onNavigateToMarkup(page.id) }
                    )
                }
            }
        }
    }

    if (showPdfExportSheet || exportOnly != null) {
        val pagesToExport = exportOnly ?: document.pages
        PdfExportSheet(
            // The title says which pages are in the file, because a PDF of three
            // pages named exactly like the twelve-page document is a file you cannot
            // tell apart in a share sheet.
            title = if (exportOnly != null) {
                "${document.title} (${pagesToExport.size} of ${document.pages.size} pages)"
            } else {
                document.title
            },
            pages = pagesToExport,
            settings = settings,
            onDismiss = {
                showPdfExportSheet = false
                exportOnly = null
            }
        )
    }

    // Confirmation for a delete. One dialog for one page and for a batch: asking N
    // times for N pages is not more careful, it is just slower. Naming the count —
    // and saying when the last page takes the whole document with it — is what makes
    // the decision checkable.
    pagesPendingDelete?.let { pending ->
        val deletesWholeDocument = pending.size >= document.pages.size
        AlertDialog(
            onDismissRequest = { pagesPendingDelete = null },
            title = {
                Text(
                    when {
                        pending.size > 1 -> "Delete ${pending.size} pages?"
                        deletesWholeDocument -> "Delete this scan?"
                        else -> "Delete this page?"
                    }
                )
            },
            text = {
                Text(
                    when {
                        // "The other 0 stay" is the sentence this ordering exists to
                        // avoid: deleting every page deletes the document, and that is
                        // the thing worth saying out loud.
                        deletesWholeDocument && pending.size > 1 ->
                            "This is every page in \"${document.title}\", so the whole " +
                                "document is deleted. This cannot be undone."
                        deletesWholeDocument ->
                            "This is the only page in \"${document.title}\", so the whole " +
                                "document is deleted. This cannot be undone."
                        pending.size > 1 ->
                            "Pages ${pending.joinToString(", ") { "${it.pageIndex + 1}" }} " +
                                "are deleted permanently, along with their edits. " +
                                "The other ${document.pages.size - pending.size} stay."
                        else ->
                            "Page ${pending.first().pageIndex + 1} of \"${document.title}\" " +
                                "is deleted permanently. The other " +
                                "${document.pages.size - 1} pages stay."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val doomed = pending
                        pagesPendingDelete = null
                        // Selection first: the bar must not spend a frame offering
                        // verbs for pages that are about to stop existing.
                        selectedPageIds = emptySet()
                        doomed.forEach { repository.deletePage(docId, it.id) }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pagesPendingDelete = null }) { Text("Keep") }
            }
        )
    }

    // A signature is a page-level mark, and it used to be stamped on page 1 of the
    // document regardless of which page was open — so signing a 5-page contract put
    // your name on the wrong sheet. It now signs the page you picked and says so.
    signatureTargetId?.let { targetId ->
        SignaturePadDialog(
            onDismiss = { signatureTargetId = null },
            onSignatureConfirmed = { signaturePath ->
                signatureTargetId = null
                scope.launch {
                    // Re-read: the page may have been rotated or re-cropped while the
                    // dialog was open, and baking a stale copy would undo that.
                    val target = repository.getDocumentById(docId)
                        ?.pages?.find { it.id == targetId } ?: return@launch
                    val baked = PageRenderer.bake(
                        context,
                        target.copy(signatureOverlayUri = signaturePath)
                    )
                    if (baked != null) {
                        repository.updatePage(docId, target.id) { baked }
                    }
                }
            }
        )
    }

    if (showIdCardDialog) {
        val front = document.pages.getOrNull(0)
        val back = document.pages.getOrNull(1)
        AlertDialog(
            onDismissRequest = { showIdCardDialog = false },
            shape = DocShapes.dialog,
            title = {
                Text(
                    "Combine as ID card?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    "Page 1 becomes the front and page 2 the back, side by side on one " +
                        "A4 page. The original pages are kept and a new document is " +
                        "created.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showIdCardDialog = false
                        if (front == null || back == null) return@TextButton
                        idCardBusy = true
                        scope.launch {
                            // A failure here is a failed write, not a crash: the page
                            // may be unreadable or the disk full.
                            val created = try {
                                repository.createIdCardDocument(
                                    frontPageId = front.id,
                                    backPageId = back.id,
                                    title = "${document.title} (ID card)",
                                    folderId = document.folderId
                                )
                            } catch (t: Throwable) {
                                null
                            } finally {
                                idCardBusy = false
                            }
                            Toast.makeText(
                                context,
                                if (created != null) {
                                    "Created ${created.title}"
                                } else {
                                    "Could not read one of the two pages"
                                },
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                ) { Text("Combine") }
            },
            dismissButton = {
                TextButton(onClick = { showIdCardDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showRenameDialog) {
        var newTitle by remember { mutableStateOf(document.title) }
        var newNotes by remember { mutableStateOf(document.notes) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            shape = DocShapes.dialog,
            title = {
                Text(
                    "Edit document",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        shape = DocShapes.field,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        label = { Text("Notes") },
                        minLines = 3,
                        shape = DocShapes.field,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                PillButton(
                    text = "Save",
                    // A blank title used to be a silent no-op: the dialog closed, the
                    // notes were saved, and the title the user had just cleared their
                    // way to was quietly thrown away. Disabling Save says so before
                    // the tap instead of after it.
                    enabled = newTitle.isNotBlank(),
                    onClick = {
                        // Surrounding whitespace is a typing artefact, never a title.
                        repository.updateDocumentTitle(docId, newTitle.trim())
                        repository.setDocumentNotes(docId, newNotes)
                        showRenameDialog = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showWatermarkDialog) {
        var watermarkText by remember {
            mutableStateOf(document.pages.firstOrNull()?.watermarkText ?: "CONFIDENTIAL")
        }
        AlertDialog(
            onDismissRequest = { showWatermarkDialog = false },
            shape = DocShapes.dialog,
            title = {
                Text(
                    "Set watermark",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                OutlinedTextField(
                    value = watermarkText,
                    onValueChange = { watermarkText = it },
                    label = { Text("Watermark (e.g. COPY, CONFIDENTIAL)") },
                    singleLine = true,
                    shape = DocShapes.field,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                PillButton(
                    text = "Apply to all pages",
                    onClick = {
                        document.pages.forEach { page ->
                            repository.updatePage(docId, page.id) {
                                it.copy(watermarkText = watermarkText.ifBlank { null })
                            }
                        }
                        showWatermarkDialog = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showWatermarkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * What the bottom bar is currently about.
 *
 * Deliberately keyed by **ids**, not by `ScannedPage`: a rotate or an annotate
 * replaces the page object, and a key that changes identity on every re-bake would
 * re-run the bar's transition under a tap that never asked for one.
 */
private sealed interface DetailBarMode {
    /** Nothing picked: the document's own verbs. */
    data object Document : DetailBarMode

    /** One page picked. */
    data class Single(val id: String) : DetailBarMode

    /** Several pages picked. Ids are sorted so the key is stable whatever the tap order. */
    data class Multiple(val ids: List<String>) : DetailBarMode
}

private fun detailBarMode(selected: Set<String>): DetailBarMode = when {
    selected.isEmpty() -> DetailBarMode.Document
    selected.size == 1 -> DetailBarMode.Single(selected.first())
    else -> DetailBarMode.Multiple(selected.toList().sorted())
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun PageGridItem(
    page: ScannedPage,
    selected: Boolean = false,
    /** This tile is the one under the finger right now. */
    dragging: Boolean = false,
    /** Draw-only translation of the dragging tile, in pixels. */
    dragOffset: Offset = Offset.Zero,
    /** The tile the dragged page(s) would land on. */
    dropTarget: Boolean = false,
    onSelectClick: () -> Unit = {},
    onViewClick: () -> Unit,
    onCropClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRotateClick: () -> Unit,
    onAnnotateClick: () -> Unit,
    onDragStart: () -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    // Selection is animated in *this* composable, not applied as a sudden boolean:
    // the tick springs in, the wash fades in, the ring fades in. All three are
    // draw-phase properties, so ticking a page costs no layout — which matters on a
    // grid where the user is tapping down a column of cards.
    val ringAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = Motion.fade,
        label = "selectRing"
    )
    val washAlpha by animateFloatAsState(
        targetValue = if (selected) 0.10f else 0f,
        animationSpec = Motion.fade,
        label = "selectWash"
    )

    val interactions = remember { MutableInteractionSource() }
    Surface(
        shape = DocShapes.cardSmall,
        // `surfaceContainerLow` sits one step above the page behind it, so the card
        // reads as a card in both themes. The tile used to be the *same* colour as
        // the screen in dark mode, which made a grid of photos look like one flat
        // sheet with buttons floating on it.
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = if (dragging) 6.dp else 0.dp,
        shadowElevation = if (dragging) 12.dp else 0.dp,
        modifier = modifier
            .fillMaxWidth()
            // The drag is a layer translation: the tile follows the finger without
            // re-laying out the grid, and while it moves it is the top-most thing on
            // screen — a card sliding *under* its neighbours would read as broken.
            .graphicsLayer {
                translationX = dragOffset.x
                translationY = dragOffset.y
                scaleX = if (dragging) 1.04f else 1f
                scaleY = if (dragging) 1.04f else 1f
            }
            .zIndex(if (dragging) 1f else 0f)
            .pressScale(interactions, pressedScale = 0.97f)
            .clip(DocShapes.cardSmall)
            // Drawn inside the clip, so selecting a card never changes where any
            // other card sits — a selection that shifts the grid looks like a
            // reorder. The ring *fades in* rather than being switched on: a border
            // that blinks is the half of the old selection feel that made the tick
            // look like it was jumping between cards instead of accumulating.
            .then(
                Modifier.border(
                    width = if (dropTarget) 3.dp else 2.dp,
                    color = if (dropTarget) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = ringAlpha)
                    },
                    shape = DocShapes.cardSmall
                )
            )
            // Long-press picks the page, the same gesture that starts selection in
            // the library. Same node count as `clickable`, so a scrolling grid costs
            // exactly what it did.
            .combinedClickable(
                interactionSource = interactions,
                indication = ripple(),
                onClick = onViewClick,
                onLongClick = onSelectClick
            )
            // Hold, then move. The long-press above picks the page (or keeps an
            // existing selection); this picks it up. Both fire on the same press —
            // `combinedClickable` cancels its click once the finger travels, so a drag
            // is never also an open.
            .pointerInput(page.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDrag = { change, amount ->
                        change.consume()
                        onDrag(amount)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragCancel() }
                )
            }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                PageThumbnail(
                    page = page,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(0.dp)),
                    reqWidth = 400,
                    reqHeight = 560
                )

                InfoPill(
                    text = page.filterType.label,
                    container = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.9f),
                    content = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )

                InfoPill(
                    text = "Page ${page.pageIndex + 1}",
                    container = MaterialTheme.colorScheme.primaryContainer,
                    content = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )

                if (!page.watermarkText.isNullOrBlank()) {
                    InfoPill(
                        text = "WATERMARK",
                        container = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                        content = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }

                // Centred, not in a corner: all four corners already carry a pill
                // (look, watermark, page number), and a check badge that covers the
                // page number tells you less than the page number did.
                //
                // The wash is always present and animates its alpha — adding and
                // removing a full-size Box per tick would rebuild the tile's content
                // on every selection change for something that is a colour.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = washAlpha))
                )
                androidx.compose.animation.AnimatedVisibility(
                    visible = selected,
                    enter = scaleIn(Motion.bouncy) + fadeIn(Motion.fade),
                    exit = scaleOut(Motion.press) + fadeOut(tween(120)),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shadowElevation = 6.dp,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            // The page's own verbs, as Material 3 filled tonal icon buttons: they
            // carry a tonal container a step off the card in both light and dark, so
            // the row reads as controls rather than as part of the photo.
            //
            // Reordering is gone from this row on purpose — the tile itself drags now,
            // and an arrow that moved a page one slot at a time was the slowest
            // possible way to do what a drag does in one gesture.
            //
            // `minimumInteractiveComponentSize` is relaxed to 36 dp: four 48 dp targets
            // do not fit across a half-width card on a 360 dp phone, and the old row
            // overflowed and clipped its last button.
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 36.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GridAction(
                        icon = Icons.Outlined.Draw,
                        description = "Annotate",
                        onClick = onAnnotateClick
                    )
                    GridAction(
                        icon = Icons.Default.RotateRight,
                        description = "Rotate",
                        onClick = onRotateClick
                    )
                    GridAction(
                        icon = Icons.Outlined.Crop,
                        description = "Crop and filter",
                        onClick = onCropClick
                    )
                    Box {
                        GridAction(
                            icon = Icons.Default.MoreVert,
                            description = "More",
                            onClick = { showMenu = true }
                        )
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Annotate") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Draw, contentDescription = null)
                                },
                                onClick = {
                                    showMenu = false
                                    onAnnotateClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete page") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * One compact tonal icon button in a grid tile.
 *
 * A thin wrapper so all four share size, shape and colour, and so the tonal container
 * and pressed shape come from Material rather than from a copy of it.
 */
@Composable
private fun GridAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(36.dp)
    ) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(18.dp))
    }
}
