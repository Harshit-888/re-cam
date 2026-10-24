package com.example.docscanner.ui.viewer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.ui.common.rememberPageImage
import com.example.docscanner.util.PageRenderer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageViewerScreen(
    docId: String,
    pageId: String,
    repository: ScannerRepository,
    onNavigateBack: () -> Unit,
    onNavigateToCrop: (pageId: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val documents by repository.documents.collectAsState()
    val document = remember(documents, docId) { documents.find { it.id == docId } }
    val pages = remember(document) { document?.pages ?: emptyList() }

    var currentPageIndex by remember(pages.size) {
        mutableIntStateOf(pages.indexOfFirst { it.id == pageId }.coerceAtLeast(0))
    }
    val safeIndex = currentPageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
    val currentPage = pages.getOrNull(safeIndex)

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showOcrSheet by remember { mutableStateOf(false) }

    // Reference the decoded bitmap so the composable recomposes after a re-bake.
    val pageImage = rememberPageImage(currentPage?.imageUri, reqWidth = 1600, reqHeight = 2200)

    LaunchedEffect(safeIndex) {
        scale = 1f
        offset = Offset.Zero
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${document?.title ?: "Document"} (${safeIndex + 1}/${pages.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val page = currentPage ?: return@IconButton
                            scope.launch {
                                val baked = PageRenderer.bake(
                                    context,
                                    page.copy(
                                        rotationDegrees =
                                            (page.rotationDegrees + 90f) % 360f
                                    )
                                )
                                if (baked != null) {
                                    repository.updatePage(docId, page.id) { baked }
                                    scale = 1f
                                    offset = Offset.Zero
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Rotate")
                    }
                    IconButton(
                        onClick = { currentPage?.let { onNavigateToCrop(it.id) } }
                    ) {
                        Icon(Icons.Default.Crop, contentDescription = "Crop and filter")
                    }
                    IconButton(onClick = { showOcrSheet = true }) {
                        Icon(Icons.Outlined.TextFields, contentDescription = "Recognised text")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { currentPageIndex = (safeIndex - 1).coerceAtLeast(0) },
                        enabled = safeIndex > 0
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous page")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Page ${safeIndex + 1} of ${pages.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (scale > 1.01f) {
                            Text(
                                text = "${(scale * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = { currentPageIndex = (safeIndex + 1).coerceAtMost(pages.size - 1) },
                        enabled = safeIndex < pages.size - 1
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next page")
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF141414)),
            contentAlignment = Alignment.Center
        ) {
            when {
                currentPage == null -> {
                    Text(
                        "This document has no pages.",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                pageImage == null -> {
                    CircularProgressIndicator(color = Color.White)
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 4f)
                                    offset = if (scale > 1f) {
                                        Offset(offset.x + pan.x, offset.y + pan.y)
                                    } else {
                                        Offset.Zero
                                    }
                                }
                            }
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = pageImage,
                            contentDescription = "Page ${safeIndex + 1}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showOcrSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOcrSheet = false },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recognised Text",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showOcrSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val text = currentPage?.ocrText.orEmpty()
                    Text(
                        text = text.ifBlank {
                            "No text on this page yet. OCR (ML Kit) is the next roadmap item — " +
                                "until then this field is empty."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    enabled = !currentPage?.ocrText.isNullOrBlank(),
                    onClick = {
                        val text = currentPage?.ocrText.orEmpty()
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Page text", text))
                        Toast.makeText(context, "Text copied", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Copy to clipboard")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
