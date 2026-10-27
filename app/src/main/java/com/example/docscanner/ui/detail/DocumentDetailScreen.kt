package com.example.docscanner.ui.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import com.example.docscanner.theme.DocShapes
import com.example.docscanner.theme.pressScale
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
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInFull
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.ui.common.PageThumbnail
import com.example.docscanner.ui.pdf.PdfExportSheet
import com.example.docscanner.ui.signature.SignaturePadDialog
import com.example.docscanner.ui.editor.MarkupDialog
import com.example.docscanner.util.PageRenderer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    docId: String,
    repository: ScannerRepository,
    onNavigateBack: () -> Unit,
    onNavigateToCrop: (pageId: String) -> Unit,
    onNavigateToViewer: (pageId: String) -> Unit,
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
    var markupTarget by remember { mutableStateOf<ScannedPage?>(null) }
    var idCardBusy by remember { mutableStateOf(false) }

    // Which page the bottom bar is currently about.
    //
    // The bar used to be four fixed buttons — Add page, Sign, Mark, Export — that
    // said the same thing whether the user had opened a page or not. Sign in
    // particular did not: it stamped page 1 no matter which page you were looking
    // at. A document and a page are different objects with different verbs, so the
    // bar now follows the selection: nothing picked means document verbs, a page
    // picked means page verbs.
    //
    // Long-press to start selecting, exactly like the library's document grid — one
    // gesture for "act on this thing" in both places. It is its own mode rather than
    // a side effect of tapping a card because tapping a card already means "open it",
    // and a bar that silently changes under your thumb after you come back from the
    // viewer is a bar you cannot trust.
    var selectedPageId by remember { mutableStateOf<String?>(null) }

    // Signing targets a page now, so the dialog needs to know which one was open.
    var signatureTargetId by remember { mutableStateOf<String?>(null) }

    // Deleting a page is destructive and irreversible — the capture, the JPEG bake
    // and the lossless bake all go — and it used to happen on a single tap of a
    // menu item, with no confirmation and no undo. The page is named in the dialog
    // because "delete page 3" is a decision you can check and "delete" is not.
    var pagePendingDelete by remember { mutableStateOf<ScannedPage?>(null) }

    // Resolved from the live page list, not stored as an object: a rotate or an
    // annotate replaces the page, and a snapshot would then act on stale data.
    val selectedPage = remember(document?.pages, selectedPageId) {
        document?.pages?.firstOrNull { it.id == selectedPageId }
    }

    // A deleted, moved-away or re-baked page list can leave the selection pointing
    // at nothing. Clearing it here means the bar can never show page verbs for a
    // page that is not there.
    LaunchedEffect(document?.pages?.size, selectedPage?.id) {
        if (selectedPage == null && selectedPageId != null) selectedPageId = null
    }

    // Back leaves selection, not the screen — the same rule as the library grid.
    BackHandler(enabled = selectedPage != null) { selectedPageId = null }

    // Re-bake so the rotation is visible immediately in the grid, the viewer and the
    // PDF — not just stored as a flag. One definition, because the card's rotate
    // button and the selected page's More → Rotate must not drift apart.
    fun rotatePage(page: ScannedPage) {
        scope.launch {
            val current = repository.getDocumentById(docId)
                ?.pages?.find { it.id == page.id } ?: return@launch
            val baked = PageRenderer.bake(
                context,
                current.copy(rotationDegrees = (current.rotationDegrees + 90f) % 360f)
            )
            if (baked != null) {
                repository.updatePage(docId, page.id) { baked }
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
                    // While a page is selected the title says *which* page: the bar
                    // below has room for verbs, not for which page they apply to, and
                    // "Rotate" with no subject is the bug this round is fixing.
                    Text(
                        text = selectedPage?.let { "Page ${it.pageIndex + 1} of ${document.pages.size}" }
                            ?: document.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedPage != null) selectedPageId = null else onNavigateBack()
                        }
                    ) {
                        Icon(
                            if (selectedPage != null) Icons.Default.Close else Icons.Default.ArrowBack,
                            contentDescription = if (selectedPage != null) {
                                "Clear selection"
                            } else {
                                "Back"
                            }
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit title and notes")
                    }
                    IconButton(onClick = { showPdfExportSheet = true }) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = "Export PDF")
                    }
                    // Only meaningful for exactly two pages: front and back. Shown
                    // rather than hidden so the feature is discoverable, and disabled
                    // with a reason rather than lying about what a tap will do.
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Two bars, one height, one fade. Document verbs when nothing is picked;
            // page verbs when something is.
            // Keyed on the page *id*, not the page: rotating or annotating replaces
            // the `ScannedPage` object, and keying on the object would re-run the
            // transition — a fading bar under a tap that never asked for one.
            AnimatedContent(
                targetState = selectedPage?.id,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "detailActionBar"
            ) { key ->
                // Resolved from the lambda's key rather than from `selectedPage`
                // directly so the bar that is leaving keeps showing the page it was
                // about until it has faded out.
                val target = if (key == null) {
                    null
                } else {
                    document.pages.firstOrNull { it.id == key }
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = DocShapes.sheet,
                    shadowElevation = 14.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (target == null) {
                            // Nothing is picked, so only two things can be true of
                            // the document: it can grow, or it can become a PDF.
                            // Sign and Mark used to sit here and both were lies at
                            // this level — Mark stamps every page (it belongs with a
                            // page, in More), and Sign stamped page 1 only.
                            DocumentActionButton(
                                label = "Add page",
                                icon = Icons.Default.AddAPhoto,
                                onClick = { onAddPageCamera(docId) },
                                modifier = Modifier.weight(1f)
                            )
                            DocumentActionButton(
                                label = "Export PDF",
                                icon = Icons.Outlined.PictureAsPdf,
                                onClick = { showPdfExportSheet = true },
                                primary = true,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // The page's own verbs, in the order a scan needs them:
                            // fix the crop and the look, add ink, sign it — then
                            // everything rare in More.
                            DocumentActionButton(
                                label = "Crop",
                                icon = Icons.Outlined.Crop,
                                onClick = {
                                    val id = target.id
                                    selectedPageId = null
                                    onNavigateToCrop(id)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            DocumentActionButton(
                                label = "Annotate",
                                icon = Icons.Outlined.Draw,
                                onClick = { markupTarget = target },
                                modifier = Modifier.weight(1f)
                            )
                            DocumentActionButton(
                                label = "Sign",
                                icon = Icons.Default.Create,
                                onClick = { signatureTargetId = target.id },
                                modifier = Modifier.weight(1f)
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                var showPageMenu by remember(target.id) { mutableStateOf(false) }
                                DocumentActionButton(
                                    label = "More",
                                    icon = Icons.Default.MoreVert,
                                    onClick = { showPageMenu = true },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(
                                    expanded = showPageMenu,
                                    onDismissRequest = { showPageMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Open full screen") },
                                        leadingIcon = {
                                            Icon(Icons.Default.OpenInFull, contentDescription = null)
                                        },
                                        onClick = {
                                            showPageMenu = false
                                            val id = target.id
                                            selectedPageId = null
                                            onNavigateToViewer(id)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Rotate 90°") },
                                        leadingIcon = {
                                            Icon(Icons.Default.RotateRight, contentDescription = null)
                                        },
                                        onClick = {
                                            showPageMenu = false
                                            rotatePage(target)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Move earlier") },
                                        leadingIcon = {
                                            Icon(Icons.Default.ArrowLeft, contentDescription = null)
                                        },
                                        enabled = target.pageIndex > 0,
                                        onClick = {
                                            showPageMenu = false
                                            repository.movePage(
                                                docId,
                                                target.pageIndex,
                                                target.pageIndex - 1
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Move later") },
                                        leadingIcon = {
                                            Icon(Icons.Default.ArrowRight, contentDescription = null)
                                        },
                                        enabled = target.pageIndex < document.pages.lastIndex,
                                        onClick = {
                                            showPageMenu = false
                                            repository.movePage(
                                                docId,
                                                target.pageIndex,
                                                target.pageIndex + 1
                                            )
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
                                            showPageMenu = false
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
                                            showPageMenu = false
                                            pagePendingDelete = target
                                        }
                                    )
                                }
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
                        canMoveBackward = index > 0,
                        canMoveForward = index >= 0 && index < document.pages.lastIndex,
                        selected = selectedPage?.id == page.id,
                        // Tap opens the page — unless a page is already picked, in
                        // which case tap picks (the library grid's rule, so the two
                        // grids in the app behave the same way). Long-press picks
                        // either way.
                        onViewClick = {
                            if (selectedPage == null) {
                                onNavigateToViewer(page.id)
                            } else {
                                selectedPageId = page.id
                            }
                        },
                        onSelectClick = {
                            selectedPageId = if (selectedPage?.id == page.id) null else page.id
                        },
                        onCropClick = { onNavigateToCrop(page.id) },
                        onDeleteClick = { pagePendingDelete = page },
                        onRotateClick = { rotatePage(page) },
                        onMoveBackward = { repository.movePage(docId, index, index - 1) },
                        onMoveForward = { repository.movePage(docId, index, index + 1) },
                        onAnnotateClick = { markupTarget = page }
                    )
                }
            }
        }
    }

    if (showPdfExportSheet) {
        PdfExportSheet(
            title = document.title,
            pages = document.pages,
            settings = settings,
            onDismiss = { showPdfExportSheet = false }
        )
    }

    // Confirmation for a page delete. The last page is called out separately: it
    // removes the whole document, which is not obvious from a page-level menu.
    pagePendingDelete?.let { page ->
        val isLastPage = document.pages.size <= 1
        AlertDialog(
            onDismissRequest = { pagePendingDelete = null },
            title = { Text(if (isLastPage) "Delete this scan?" else "Delete this page?") },
            text = {
                Text(
                    if (isLastPage) {
                        "This is the only page in \"${document.title}\", so the whole " +
                            "document is deleted. This cannot be undone."
                    } else {
                        "Page ${page.pageIndex + 1} of \"${document.title}\" is deleted " +
                            "permanently. The other ${document.pages.size - 1} pages stay."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pagePendingDelete = null
                        repository.deletePage(docId, page.id)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pagePendingDelete = null }) { Text("Keep") }
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

    // Annotation is stored on the page as strokes and replayed by the renderer on
    // every bake, so it survives a filter change made later. The re-bake here is only
    // so the thumbnail and the viewer show the ink immediately.
    markupTarget?.let { target ->
        MarkupDialog(
            page = target,
            onDismiss = { markupTarget = null },
            onSave = { strokes ->
                markupTarget = null
                scope.launch {
                    val current = repository.getDocumentById(docId)
                        ?.pages?.find { it.id == target.id } ?: return@launch
                    repository.updatePage(docId, target.id) { it.copy(markup = strokes) }
                    val baked = runCatching {
                        PageRenderer.bake(context, current.copy(markup = strokes))
                    }.getOrNull()
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

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun PageGridItem(
    page: ScannedPage,
    canMoveBackward: Boolean,
    canMoveForward: Boolean,
    selected: Boolean = false,
    onSelectClick: () -> Unit = {},
    onViewClick: () -> Unit,
    onCropClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRotateClick: () -> Unit,
    onMoveBackward: () -> Unit,
    onMoveForward: () -> Unit,
    onAnnotateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val interactions = remember { MutableInteractionSource() }
    Surface(
        shape = DocShapes.cardSmall,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interactions, pressedScale = 0.97f)
            .clip(DocShapes.cardSmall)
            // Drawn inside the clip, so selecting a card never changes where any
            // other card sits — a selection that shifts the grid looks like a
            // reorder.
            .then(
                if (selected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, DocShapes.cardSmall)
                } else {
                    Modifier
                }
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
                if (selected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(44.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            // These four are the most-hit controls on the screen — rotate and crop get
            // used on nearly every page — and they used to be constrained to 32 dp,
            // which is the size of the *hit area*, not just the icon. `IconButton`
            // already reserves 48 dp around its 40 dp state layer; the explicit
            // `.size(32.dp)` was undoing that. Left unconstrained, the row is 16 dp
            // taller and every target is a finger wide.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMoveBackward, enabled = canMoveBackward) {
                    Icon(Icons.Default.ArrowLeft, contentDescription = "Move page earlier", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onRotateClick) {
                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onCropClick) {
                    Icon(Icons.Outlined.Crop, contentDescription = "Crop and filter", modifier = Modifier.size(18.dp))
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Move forward") },
                            leadingIcon = { Icon(Icons.Default.ArrowRight, contentDescription = null) },
                            enabled = canMoveForward,
                            onClick = {
                                showMenu = false
                                onMoveForward()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Move backward") },
                            leadingIcon = { Icon(Icons.Default.ArrowLeft, contentDescription = null) },
                            enabled = canMoveBackward,
                            onClick = {
                                showMenu = false
                                onMoveBackward()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Annotate") },
                            leadingIcon = { Icon(Icons.Outlined.Draw, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onAnnotateClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete page") },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
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

/**
 * One action in the document bottom bar: icon above label, fixed height.
 *
 * Both variants share the same height and padding so the primary Export button can
 * never end up taller or shorter than the three text actions next to it, and the
 * label is single-line so a long word can never wrap and inflate the row.
 */
@Composable
private fun DocumentActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false
) {
    val interactions = remember { MutableInteractionSource() }
    val container = if (primary) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val content = if (primary) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Surface(
        color = container,
        contentColor = content,
        shape = DocShapes.cardSmall,
        modifier = modifier
            .height(ACTION_HEIGHT)
            .pressScale(interactions, pressedScale = 0.95f)
            .clip(DocShapes.cardSmall)
            .clickable(interactionSource = interactions, indication = ripple(), onClick = onClick)
    ) {
        DocumentActionContent(label = label, icon = icon)
    }
}

@Composable
private fun DocumentActionContent(label: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Shared height for every bottom-bar action, so none can grow taller than its peers. */
private val ACTION_HEIGHT = 56.dp
