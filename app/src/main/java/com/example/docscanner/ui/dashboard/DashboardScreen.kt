package com.example.docscanner.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.Document
import com.example.docscanner.model.DocumentSortOption
import com.example.docscanner.model.Folder
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: ScannerRepository = ScannerRepository.instance,
    onNavigateToCamera: (String?) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToTrash: () -> Unit
) {
    val documents by repository.documents.collectAsState()
    val folders by repository.folders.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFolderId by remember { mutableStateOf<String?>(null) }
    var sortOption by remember { mutableStateOf(DocumentSortOption.MODIFIED_NEWEST) }
    var isGridView by remember { mutableStateOf(true) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Filter active (non-trash) documents matching search and folder
    val activeDocs = remember(documents, searchQuery, selectedFolderId, sortOption) {
        documents
            .filterNot { it.isTrash }
            .filter { doc ->
                val matchesFolder = selectedFolderId == null || doc.folderId == selectedFolderId
                val matchesSearch = searchQuery.isBlank() ||
                        doc.title.contains(searchQuery, ignoreCase = true) ||
                        doc.tags.any { it.contains(searchQuery, ignoreCase = true) } ||
                        doc.pages.any { it.ocrText.contains(searchQuery, ignoreCase = true) }
                matchesFolder && matchesSearch
            }
            .sortedWith { a, b ->
                when (sortOption) {
                    DocumentSortOption.MODIFIED_NEWEST -> b.modifiedDate.compareTo(a.modifiedDate)
                    DocumentSortOption.MODIFIED_OLDEST -> a.modifiedDate.compareTo(b.modifiedDate)
                    DocumentSortOption.CREATED_NEWEST -> b.createdDate.compareTo(a.createdDate)
                    DocumentSortOption.TITLE_AZ -> a.title.compareTo(b.title, ignoreCase = true)
                    DocumentSortOption.TITLE_ZA -> b.title.compareTo(a.title, ignoreCase = true)
                }
            }
    }

    val trashCount = remember(documents) { documents.count { it.isTrash } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "DocScanner",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            if (isGridView) Icons.Outlined.ViewList else Icons.Outlined.GridView,
                            contentDescription = "Toggle View"
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
                                    } else null,
                                    onClick = {
                                        sortOption = option
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = onNavigateToTrash) {
                        BadgedBox(
                            badge = {
                                if (trashCount > 0) {
                                    Badge { Text("$trashCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Trash")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToCamera(null) },
                icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                text = { Text("Scan Document", fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(20.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Expressive Search Bar
            SearchBarField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Folder Filter Chips Carousel
            FolderFilterRow(
                folders = folders,
                selectedFolderId = selectedFolderId,
                onSelectFolder = { selectedFolderId = it },
                onAddFolderClick = { showFolderDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Document Count & Active Folder Status Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${activeDocs.size} documents",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = sortOption.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Documents Content (Grid or List)
            if (activeDocs.isEmpty()) {
                EmptyStateView(
                    isSearch = searchQuery.isNotBlank(),
                    onStartScan = { onNavigateToCamera(null) }
                )
            } else if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(activeDocs, key = { it.id }) { doc ->
                        DocumentGridCard(
                            document = doc,
                            onClick = { onNavigateToDetail(doc.id) },
                            onDelete = { repository.moveToTrash(doc.id) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(activeDocs, key = { it.id }) { doc ->
                        DocumentListCard(
                            document = doc,
                            onClick = { onNavigateToDetail(doc.id) },
                            onDelete = { repository.moveToTrash(doc.id) }
                        )
                    }
                }
            }
        }
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

@Composable
fun SearchBarField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search title, tags, or OCR text...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = modifier
    )
}

@Composable
fun FolderFilterRow(
    folders: List<Folder>,
    selectedFolderId: String?,
    onSelectFolder: (String?) -> Unit,
    onAddFolderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        item {
            FilterChip(
                selected = selectedFolderId == null,
                onClick = { onSelectFolder(null) },
                label = { Text("All Documents") },
                shape = RoundedCornerShape(16.dp)
            )
        }
        items(folders, key = { it.id }) { folder ->
            FilterChip(
                selected = selectedFolderId == folder.id,
                onClick = { onSelectFolder(if (selectedFolderId == folder.id) null else folder.id) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(folder.colorHex), CircleShape)
                    )
                },
                label = { Text(folder.name) },
                shape = RoundedCornerShape(16.dp)
            )
        }
        item {
            AssistChip(
                onClick = onAddFolderClick,
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
                label = { Text("New Folder") },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun DocumentGridCard(
    document: Document,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            // Thumbnail container with page count badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(56.dp)
                )

                // Page count pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${document.pages.size} p.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Move to Trash") },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateFormat.format(Date(document.modifiedDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DocumentListCard(
    document: Document,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${document.pages.size} pages",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateFormat.format(Date(document.modifiedDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Move to Trash") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    isSearch: Boolean,
    onStartScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            if (isSearch) Icons.Default.SearchOff else Icons.Default.DocumentScanner,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isSearch) "No documents match your query" else "No documents yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isSearch) "Try searching for a different keyword or clear filters." else "Tap Scan Document to capture your first page.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        if (!isSearch) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onStartScan,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Scanning")
            }
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
        title = { Text("New Folder") },
        text = {
            Column {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Pick Folder Color:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colors.forEach { colorVal ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .clickable { selectedColor = colorVal }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == colorVal) {
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
            Button(
                onClick = { if (folderName.isNotBlank()) onConfirm(folderName, selectedColor) },
                enabled = folderName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
