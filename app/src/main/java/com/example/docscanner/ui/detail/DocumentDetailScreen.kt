package com.example.docscanner.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.Document
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.ui.pdf.PdfExportSheet
import com.example.docscanner.ui.signature.SignaturePadDialog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    docId: String,
    repository: ScannerRepository = ScannerRepository.instance,
    onNavigateBack: () -> Unit,
    onNavigateToCrop: (pageId: String) -> Unit,
    onNavigateToViewer: (pageId: String) -> Unit,
    onAddPageCamera: (docId: String) -> Unit
) {
    val documents by repository.documents.collectAsState()
    val document = remember(documents, docId) { documents.find { it.id == docId } }

    var showPdfExportSheet by remember { mutableStateOf(false) }
    var showSignatureDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showWatermarkDialog by remember { mutableStateOf(false) }
    var selectedPageForAction by remember { mutableStateOf<ScannedPage?>(null) }

    if (document == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Document not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = document.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${document.pages.size} pages • ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(document.modifiedDate))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Rename")
                    }
                    IconButton(onClick = { showPdfExportSheet = true }) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = "Export PDF")
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onAddPageCamera(docId) }) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Page")
                    }
                    TextButton(onClick = { showSignatureDialog = true }) {
                        Icon(Icons.Outlined.Draw, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Signature")
                    }
                    TextButton(onClick = { showWatermarkDialog = true }) {
                        Icon(Icons.Outlined.BrandingWatermark, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Watermark")
                    }
                    Button(
                        onClick = { showPdfExportSheet = true },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share")
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
            // Notes banner if present
            if (document.notes.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = document.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Grid of Scanned Pages
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(document.pages, key = { it.id }) { page ->
                    PageGridItem(
                        page = page,
                        onViewClick = { onNavigateToViewer(page.id) },
                        onCropClick = { onNavigateToCrop(page.id) },
                        onDeleteClick = { repository.deletePage(docId, page.id) },
                        onRotateClick = {
                            repository.updatePage(docId, page.id) {
                                it.copy(rotationDegrees = (it.rotationDegrees + 90f) % 360f)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showPdfExportSheet) {
        PdfExportSheet(
            document = document,
            onDismiss = { showPdfExportSheet = false }
        )
    }

    if (showSignatureDialog) {
        SignaturePadDialog(
            onDismiss = { showSignatureDialog = false },
            onSignatureConfirmed = { signatureUri ->
                document.pages.firstOrNull()?.let { firstPage ->
                    repository.updatePage(docId, firstPage.id) {
                        it.copy(signatureOverlayUri = signatureUri)
                    }
                }
                showSignatureDialog = false
            }
        )
    }

    if (showRenameDialog) {
        var newTitle by remember { mutableStateOf(document.title) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Document") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            repository.updateDocumentTitle(docId, newTitle)
                        }
                        showRenameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showWatermarkDialog) {
        var watermarkText by remember { mutableStateOf(document.pages.firstOrNull()?.watermarkText ?: "CONFIDENTIAL") }
        AlertDialog(
            onDismissRequest = { showWatermarkDialog = false },
            title = { Text("Set Watermark Text") },
            text = {
                Column {
                    OutlinedTextField(
                        value = watermarkText,
                        onValueChange = { watermarkText = it },
                        label = { Text("Watermark (e.g. COPY, CONFIDENTIAL)") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        document.pages.forEach { page ->
                            repository.updatePage(docId, page.id) {
                                it.copy(watermarkText = watermarkText.ifBlank { null })
                            }
                        }
                        showWatermarkDialog = false
                    }
                ) {
                    Text("Apply to All Pages")
                }
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
    onViewClick: () -> Unit,
    onCropClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRotateClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Article,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )

                // Filter Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = page.filterType.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Page Number Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Page ${page.pageIndex + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Watermark badge if applied
                page.watermarkText?.let {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "WATERMARK",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Per page bottom toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRotateClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onCropClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Crop, contentDescription = "Crop & Filter", modifier = Modifier.size(18.dp))
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
                            text = { Text("Delete Page") },
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
