package com.example.docscanner.ui.dashboard

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.Document
import com.example.docscanner.model.DocumentSortOption
import com.example.docscanner.model.Folder
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.theme.DocShapes
import com.example.docscanner.theme.entrance
import com.example.docscanner.theme.pressScale
import com.example.docscanner.ui.common.InfoPill
import com.example.docscanner.ui.common.PageThumbnail
import com.example.docscanner.ui.common.PillButton
import com.example.docscanner.ui.common.SectionLabel
import com.example.docscanner.util.DocumentNaming
import com.example.docscanner.util.DocumentSearch
import com.example.docscanner.util.ImageStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
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

    // Recognise text for pages that predate search (bounded to one batch per app
    // start). Indexing is what makes a scan's words findable at all — the PDFs this
    // app writes contain no text layer.
    LaunchedEffect(Unit) { repository.backfillTextInBackground() }

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

    // The index is rebuilt when the library changes, never when the query does: a
    // keystroke is now an index lookup instead of a scan of every page's text.
    val searchIndex = remember(documents) { DocumentSearch.Index(documents) }
    val searchHits = remember(searchIndex, searchQuery) {
        if (searchQuery.isBlank()) emptyList() else searchIndex.search(searchQuery)
    }
    val snippetsByDocument = remember(searchHits) {
        searchHits.mapNotNull { hit -> hit.snippet?.let { hit.document.id to it } }.toMap()
    }

    val activeDocs = remember(documents, searchHits, searchQuery, selectedFolderId, sortOption) {
        val inFolder = if (searchQuery.isBlank()) {
            documents.filterNot { it.isTrash }
        } else {
            // Relevance order is the point of a search; the sort control does not
            // get to re-alphabetise the results.
            searchHits.map { it.document }
        }
        inFolder
            .filter { doc -> selectedFolderId == null || doc.folderId == selectedFolderId }
            .let { docs ->
                if (searchQuery.isNotBlank()) {
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
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
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
        },
        floatingActionButton = {
            // An extended FAB that reads as the app's one primary verb. The pill
            // shape and the amber-free primary keep it distinct from the cards.
            CaptureFab(onClick = startScan)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBarField(
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
                activeDocs.isEmpty() -> EmptyStateView(
                    isSearch = searchQuery.isNotBlank(),
                    onStartScan = startScan
                )

                isGridView -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 108.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
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
                            snippet = snippetsByDocument[doc.id],
                            onClick = { onNavigateToDetail(doc.id) },
                            onDelete = { repository.moveToTrash(doc.id) },
                            onMoveToFolder = { moveDocId = doc.id }
                        )
                    }
                }

                else -> LazyColumn(
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
                            snippet = snippetsByDocument[doc.id],
                            onClick = { onNavigateToDetail(doc.id) },
                            onDelete = { repository.moveToTrash(doc.id) },
                            onMoveToFolder = { moveDocId = doc.id }
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
}

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

@Composable
fun SearchBarField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                "Search documents",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
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
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        ),
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

/** Grid card: the page itself is the card, with the metadata in a quiet footer. */
@Composable
fun DocumentGridCard(
    document: Document,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMoveToFolder: () -> Unit,
    snippet: String? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    val interactions = remember { MutableInteractionSource() }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = DocShapes.card,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactions, pressedScale = 0.975f)
            .clip(DocShapes.card)
            .clickable(interactionSource = interactions, indication = ripple(), onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.78f)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                val cover = document.pages.firstOrNull()
                if (cover != null) {
                    PageThumbnail(
                        page = cover,
                        modifier = Modifier.fillMaxSize(),
                        reqWidth = 360,
                        reqHeight = 460,
                        fallbackIconSize = 44.dp
                    )
                } else {
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

            Column(Modifier.padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 12.dp)) {
                Text(
                    document.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                if (snippet == null) {
                    Text(
                        dateFormat.format(Date(document.modifiedDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                } else {
                    // Why this document matched: the sentence around the hit. Without
                    // it a search result is just a thumbnail the user has to re-open.
                    Text(
                        snippet,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** List card: a compact row that keeps the thumbnail's shape language. */
@Composable
fun DocumentListCard(
    document: Document,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMoveToFolder: () -> Unit,
    snippet: String? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()) }
    val interactions = remember { MutableInteractionSource() }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = DocShapes.cardSmall,
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactions, pressedScale = 0.98f)
            .clip(DocShapes.cardSmall)
            .clickable(interactionSource = interactions, indication = ripple(), onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 52.dp, height = 66.dp)
                    .clip(DocShapes.thumbnail)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                val cover = document.pages.firstOrNull()
                if (cover != null) {
                    PageThumbnail(
                        page = cover,
                        modifier = Modifier.fillMaxSize(),
                        reqWidth = 160,
                        reqHeight = 200,
                        fallbackIconSize = 22.dp
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
                        dateFormat.format(Date(document.modifiedDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (snippet != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        snippet,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

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

/** Small round overflow affordance that does not shout on top of a thumbnail. */
@Composable
private fun TonalMenuButton(onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.85f),
        modifier = Modifier
            .padding(6.dp)
            .size(30.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Options",
                modifier = Modifier.size(17.dp)
            )
        }
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
    onStartScan: () -> Unit
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
            if (isSearch) "Nothing matches that search" else "Your library is empty",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (isSearch) {
                "Try a different word, or clear the filters above."
            } else {
                "Scan a page and it will appear here, ready to sign, export or share."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
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
