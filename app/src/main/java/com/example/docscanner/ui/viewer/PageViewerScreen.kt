package com.example.docscanner.ui.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.FilterType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageViewerScreen(
    docId: String,
    pageId: String,
    repository: ScannerRepository = ScannerRepository.instance,
    onNavigateBack: () -> Unit,
    onNavigateToCrop: (pageId: String) -> Unit
) {
    val document = remember(docId) { repository.getDocumentById(docId) }
    val pages = remember(document) { document?.pages ?: emptyList() }
    var currentPageIndex by remember {
        mutableIntStateOf(pages.indexOfFirst { it.id == pageId }.coerceAtLeast(0))
    }

    val currentPage = pages.getOrNull(currentPageIndex)
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showOcrSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${document?.title ?: "Document"} (${currentPageIndex + 1}/${pages.size})",
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
                    IconButton(onClick = {
                        currentPage?.let { p ->
                            repository.updatePage(docId, p.id) {
                                it.copy(rotationDegrees = (it.rotationDegrees + 90f) % 360f)
                            }
                        }
                    }) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Rotate")
                    }
                    IconButton(onClick = {
                        currentPage?.let { onNavigateToCrop(it.id) }
                    }) {
                        Icon(Icons.Default.Crop, contentDescription = "Crop & Filter")
                    }
                    IconButton(onClick = { showOcrSheet = true }) {
                        Icon(Icons.Outlined.TextFields, contentDescription = "OCR Text")
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
                        onClick = {
                            if (currentPageIndex > 0) {
                                currentPageIndex--
                                scale = 1f
                                offset = Offset.Zero
                            }
                        },
                        enabled = currentPageIndex > 0
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                    }

                    Text(
                        text = "Page ${currentPageIndex + 1} of ${pages.size}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            if (currentPageIndex < pages.size - 1) {
                                currentPageIndex++
                                scale = 1f
                                offset = Offset.Zero
                            }
                        },
                        enabled = currentPageIndex < pages.size - 1
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF1E1E1E)),
            contentAlignment = Alignment.Center
        ) {
            if (currentPage != null) {
                // High-Res Zoomable & Pannable Document View
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
                            translationY = offset.y,
                            rotationZ = currentPage.rotationDegrees
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Simulated clean paper sheet
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .aspectRatio(0.707f) // Standard A4 aspect ratio
                    ) {
                        val paperColor = when (currentPage.filterType) {
                            FilterType.ORIGINAL -> Color(0xFFF9F7F1)
                            FilterType.MAGIC_COLOR -> Color(0xFFFFFFFF)
                            FilterType.CLEAR_BW -> Color(0xFFFFFFFF)
                            FilterType.GRAYSCALE -> Color(0xFFEEEEEE)
                            FilterType.LIGHTEN -> Color(0xFFFFFFFF)
                        }

                        val inkColor = when (currentPage.filterType) {
                            FilterType.ORIGINAL -> Color(0xFF2C3E50)
                            FilterType.MAGIC_COLOR -> Color(0xFF0D47A1)
                            FilterType.CLEAR_BW -> Color(0xFF000000)
                            FilterType.GRAYSCALE -> Color(0xFF333333)
                            FilterType.LIGHTEN -> Color(0xFF424242)
                        }

                        drawRect(color = paperColor)

                        // Sample Document Lines
                        val lines = 12
                        for (i in 1..lines) {
                            val y = size.height * (i.toFloat() / (lines + 2))
                            drawLine(
                                color = inkColor.copy(alpha = if (i == 1) 0.85f else 0.45f),
                                start = Offset(size.width * 0.1f, y),
                                end = Offset(size.width * 0.9f, y),
                                strokeWidth = if (i == 1) 10f else 4f
                            )
                        }

                        // Signature overlay if present
                        if (currentPage.signatureOverlayUri != null) {
                            drawLine(
                                color = Color(0xFF0D47A1),
                                start = Offset(size.width * 0.55f, size.height * 0.82f),
                                end = Offset(size.width * 0.85f, size.height * 0.80f),
                                strokeWidth = 8f
                            )
                        }
                    }
                }
            }
        }
    }

    // OCR Recognized Text Bottom Sheet
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
                        "Recognized OCR Text",
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
                    Text(
                        text = currentPage?.ocrText?.ifBlank { "No text detected on this page." }
                            ?: "No text detected on this page.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
