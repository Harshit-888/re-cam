package com.example.docscanner.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BrandingWatermark
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
    var showSignatureDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showWatermarkDialog by remember { mutableStateOf(false) }

    // Deleting a page is destructive and irreversible — the capture, the JPEG bake
    // and the lossless bake all go — and it used to happen on a single tap of a
    // menu item, with no confirmation and no undo. The page is named in the dialog
    // because "delete page 3" is a decision you can check and "delete" is not.
    var pagePendingDelete by remember { mutableStateOf<ScannedPage?>(null) }

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
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit title and notes")
                    }
                    IconButton(onClick = { showPdfExportSheet = true }) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = "Export PDF")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
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
                    // Four equal cells with the icon above the label. Side-by-side
                    // icon + text did not fit on a 360dp phone: the Row ran out of
                    // width at the last child, "Share" wrapped onto a second line and
                    // its pill grew taller than the other three actions.
                    DocumentActionButton(
                        label = "Add page",
                        icon = Icons.Default.AddAPhoto,
                        onClick = { onAddPageCamera(docId) },
                        modifier = Modifier.weight(1f)
                    )
                    DocumentActionButton(
                        label = "Sign",
                        icon = Icons.Outlined.Draw,
                        onClick = { showSignatureDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    DocumentActionButton(
                        label = "Mark",
                        icon = Icons.Outlined.BrandingWatermark,
                        onClick = { showWatermarkDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    DocumentActionButton(
                        label = "Export",
                        icon = Icons.Default.Share,
                        onClick = { showPdfExportSheet = true },
                        primary = true,
                        modifier = Modifier.weight(1f)
                    )
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
                        onViewClick = { onNavigateToViewer(page.id) },
                        onCropClick = { onNavigateToCrop(page.id) },
                        onDeleteClick = { pagePendingDelete = page },
                        onRotateClick = {
                            // Re-bake so the rotation is visible immediately in the
                            // grid, the viewer and the PDF — not just stored as a flag.
                            scope.launch {
                                val current = repository.getDocumentById(docId)
                                    ?.pages?.find { it.id == page.id } ?: return@launch
                                val baked = PageRenderer.bake(
                                    context,
                                    current.copy(
                                        rotationDegrees = (current.rotationDegrees + 90f) % 360f
                                    )
                                )
                                if (baked != null) {
                                    repository.updatePage(docId, page.id) { baked }
                                }
                            }
                        },
                        onMoveBackward = { repository.movePage(docId, index, index - 1) },
                        onMoveForward = { repository.movePage(docId, index, index + 1) }
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

    if (showSignatureDialog) {
        SignaturePadDialog(
            onDismiss = { showSignatureDialog = false },
            onSignatureConfirmed = { signaturePath ->
                showSignatureDialog = false
                scope.launch {
                    val target = repository.getDocumentById(docId)?.pages?.firstOrNull()
                        ?: return@launch
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
fun PageGridItem(
    page: ScannedPage,
    canMoveBackward: Boolean,
    canMoveForward: Boolean,
    onViewClick: () -> Unit,
    onCropClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRotateClick: () -> Unit,
    onMoveBackward: () -> Unit,
    onMoveForward: () -> Unit,
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
            .clickable(interactionSource = interactions, indication = ripple(), onClick = onViewClick)
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
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMoveBackward, enabled = canMoveBackward, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ArrowLeft, contentDescription = "Move page earlier", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onRotateClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onCropClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Crop, contentDescription = "Crop and filter", modifier = Modifier.size(18.dp))
                }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", modifier = Modifier.size(18.dp))
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
