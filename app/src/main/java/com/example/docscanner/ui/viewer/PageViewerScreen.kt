package com.example.docscanner.ui.viewer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.theme.DocShapes
import com.example.docscanner.theme.pressScale
import com.example.docscanner.ui.common.PillButton
import com.example.docscanner.ui.common.rememberPageImage
import com.example.docscanner.util.PageRenderer
import kotlinx.coroutines.launch

/**
 * Full-bleed page reader.
 *
 * A page viewer is a reading surface: opaque app bars steal vertical space from the
 * thing the user came to look at. So the chrome floats over a dark backdrop — a glass
 * back button, a title pill, three glass actions — and the page itself runs edge to
 * edge with only enough inset to clear the glass.
 */
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

    // Held as the state object as well as the delegated value: the zoom badge needs
    // to observe it *by reference* so it can subscribe on its own without the parent
    // reading the float (and therefore recomposing on every pinch frame).
    val scaleState = remember { mutableFloatStateOf(1f) }
    var scale by scaleState
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showOcrSheet by remember { mutableStateOf(false) }

    // Reference the decoded bitmap so the composable recomposes after a re-bake.
    val pageImage = rememberPageImage(currentPage?.imageUri, reqWidth = 1600, reqHeight = 2200)

    LaunchedEffect(safeIndex) {
        scale = 1f
        offset = Offset.Zero
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ViewerBackdrop)
    ) {
        // ---- the page ---------------------------------------------------------
        when {
            currentPage == null -> {
                Text(
                    "This document has no pages.",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            pageImage == null -> {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val next = (scale * zoom).coerceIn(1f, MAX_VIEWER_ZOOM)
                                scale = next
                                // Clamp the pan to the part of the page that is
                                // actually outside the viewport. Without this the
                                // page can be flung into empty space and the user
                                // has to guess their way back.
                                val maxX = (size.width * (next - 1f)) / 2f
                                val maxY = (size.height * (next - 1f)) / 2f
                                offset = if (next > 1f) {
                                    Offset(
                                        (offset.x + pan.x).coerceIn(-maxX, maxX),
                                        (offset.y + pan.y).coerceIn(-maxY, maxY)
                                    )
                                } else {
                                    Offset.Zero
                                }
                            }
                        }
                        // The **lambda** form of `graphicsLayer`.
                        //
                        // `graphicsLayer(scaleX = scale, …)` reads `scale` and
                        // `offset` during composition, so every frame of a pinch
                        // recomposed this screen, and a changed parameter also makes
                        // Compose rebuild the layer. Reading them inside the block
                        // instead defers both to the draw phase: the gesture now
                        // updates a layer and nothing above it is recomposed at all.
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = pageImage,
                        contentDescription = "Page ${safeIndex + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp, vertical = 68.dp)
                    )
                }
            }
        }

        // ---- floating top chrome ---------------------------------------------
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ViewerGlassButton(
                icon = Icons.Default.ArrowBack,
                contentDescription = "Back",
                onClick = onNavigateBack
            )
            Spacer(Modifier.width(10.dp))
            Surface(
                shape = DocShapes.pill,
                color = Color.Black.copy(alpha = 0.45f),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = document?.title ?: "Document",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            ViewerGlassButton(
                icon = Icons.Default.RotateRight,
                contentDescription = "Rotate",
                onClick = {
                    val page = currentPage ?: return@ViewerGlassButton
                    scope.launch {
                        val baked = PageRenderer.bake(
                            context,
                            page.copy(rotationDegrees = (page.rotationDegrees + 90f) % 360f)
                        )
                        if (baked != null) {
                            repository.updatePage(docId, page.id) { baked }
                            scale = 1f
                            offset = Offset.Zero
                        }
                    }
                }
            )
            Spacer(Modifier.width(8.dp))
            ViewerGlassButton(
                icon = Icons.Default.Crop,
                contentDescription = "Crop and filter",
                onClick = { currentPage?.let { onNavigateToCrop(it.id) } }
            )
            Spacer(Modifier.width(8.dp))
            ViewerGlassButton(
                icon = Icons.Outlined.TextFields,
                contentDescription = "Recognised text",
                onClick = { showOcrSheet = true }
            )
        }

        // ---- floating pager ---------------------------------------------------
        if (pages.size > 1) {
            Surface(
                shape = DocShapes.pill,
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    ViewerPageStep(
                        icon = Icons.Default.ChevronLeft,
                        description = "Previous page",
                        enabled = safeIndex > 0,
                        onClick = { currentPageIndex = (safeIndex - 1).coerceAtLeast(0) }
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Text(
                            "${safeIndex + 1} / ${pages.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                        // Nothing in this composable reads `scale` any more, so a
                        // pinch recomposes none of it. The badge subscribes to the
                        // state object itself and re-reads only when the whole
                        // percentage changes — a few times a pinch instead of once
                        // a frame.
                        ViewerZoomBadge(scaleState = scaleState)
                    }
                    ViewerPageStep(
                        icon = Icons.Default.ChevronRight,
                        description = "Next page",
                        enabled = safeIndex < pages.size - 1,
                        onClick = { currentPageIndex = (safeIndex + 1).coerceAtMost(pages.size - 1) }
                    )
                }
            }
        }
    }

    if (showOcrSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOcrSheet = false },
            shape = DocShapes.sheet
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 22.dp, end = 22.dp, bottom = 22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "PAGE TEXT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Recognised text",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = { showOcrSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = DocShapes.cardSmall,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val text = currentPage?.ocrText.orEmpty()
                    Text(
                        text = text.ifBlank {
                            "No text on this page yet. OCR (ML Kit) is the next roadmap item — " +
                                "until then this field is empty."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (text.isBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.padding(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    PillButton(
                        text = "Copy to clipboard",
                        icon = Icons.Default.ContentCopy,
                        enabled = !currentPage?.ocrText.isNullOrBlank(),
                        onClick = {
                            val text = currentPage?.ocrText.orEmpty()
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Page text", text))
                            Toast.makeText(context, "Text copied", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

/** How far the viewer magnifies a page before the pan clamp takes over. */
private const val MAX_VIEWER_ZOOM = 4f

/** Dark, slightly warm backdrop: a page reads as paper against it. */
private val ViewerBackdrop = Color(0xFF101215)

@Composable
private fun ViewerGlassButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactions = remember { MutableInteractionSource() }
    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.45f),
        modifier = Modifier
            .size(42.dp)
            .pressScale(interactions)
            .clip(CircleShape)
            .clickable(interactionSource = interactions, indication = ripple(), onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

/**
 * The live zoom percentage, shown only while the page is actually zoomed.
 *
 * Split out of the viewer so the state read is *its* state read: a read inside a
 * bigger composable invalidates that whole composable, and a pinch would have
 * recomposed the top chrome, the bottom bar and the page subtree with it. The
 * `derivedStateOf` narrows it further — a pinch produces hundreds of scale values
 * per second but only a handful of distinct whole percentages.
 */
@Composable
private fun ViewerZoomBadge(scaleState: FloatState) {
    val shown by remember(scaleState) {
        derivedStateOf { (scaleState.floatValue * 100).toInt() }
    }
    if (shown > 101) {
        Text(
            "$shown%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ViewerPageStep(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Icon(
            icon,
            contentDescription = description,
            tint = Color.White.copy(alpha = if (enabled) 1f else 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}
