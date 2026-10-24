package com.example.docscanner.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    val document = remember(documents, docId) { documents.find { it.id == docId } }

    var showPdfExportSheet by remember { mutableStateOf(false) }
    var showSignatureDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showWatermarkDialog by remember { mutableStateOf(false) }

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
                            text = "${document.pages.size} pages • " +
                                SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                                    .format(Date(document.modifiedDate)),
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
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit title and notes")
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

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(document.pages, key = { it.id }) { page ->
                    val index = document.pages.indexOfFirst { it.id == page.id }
                    PageGridItem(
                        page = page,
                        canMoveBackward = index > 0,
                        canMoveForward = index >= 0 && index < document.pages.lastIndex,
                        onViewClick = { onNavigateToViewer(page.id) },
                        onCropClick = { onNavigateToCrop(page.id) },
                        onDeleteClick = { repository.deletePage(docId, page.id) },
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
            document = document,
            onDismiss = { showPdfExportSheet = false }
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
            title = { Text("Edit document") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        label = { Text("Notes") },
                        minLines = 3,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            repository.updateDocumentTitle(docId, newTitle)
                        }
                        repository.setDocumentNotes(docId, newNotes)
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
        var watermarkText by remember {
            mutableStateOf(document.pages.firstOrNull()?.watermarkText ?: "CONFIDENTIAL")
        }
        AlertDialog(
            onDismissRequest = { showWatermarkDialog = false },
            title = { Text("Set Watermark Text") },
            text = {
                OutlinedTextField(
                    value = watermarkText,
                    onValueChange = { watermarkText = it },
                    label = { Text("Watermark (e.g. COPY, CONFIDENTIAL)") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
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
    canMoveBackward: Boolean,
    canMoveForward: Boolean,
    onViewClick: () -> Unit,
    onCropClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRotateClick: () -> Unit,
    onMoveBackward: () -> Unit,
    onMoveForward: () -> Unit
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

                if (!page.watermarkText.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
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
