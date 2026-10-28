package com.example.docscanner.ui.viewer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.theme.DocShapes
import com.example.docscanner.theme.Motion
import com.example.docscanner.theme.pressScale
import com.example.docscanner.ui.common.BarHairline
import com.example.docscanner.ui.common.ConnectedAction
import com.example.docscanner.ui.common.ConnectedButtonGroup
import com.example.docscanner.ui.common.ExpressiveActionBar
import com.example.docscanner.ui.common.ExpressivePager
import com.example.docscanner.ui.common.PillButton
import com.example.docscanner.ui.common.ZoomablePageView
import com.example.docscanner.ui.common.rememberPageImage
import com.example.docscanner.ui.pdf.PdfExportSheet
import com.example.docscanner.ui.signature.SignaturePadDialog
import com.example.docscanner.util.PageRenderer
import kotlinx.coroutines.launch

/**
 * Full-bleed page reader.
 *
 * A page viewer is a reading surface: opaque app bars steal vertical space from the
 * thing the user came to look at. So the chrome floats over a dark backdrop — a glass
 * back button, a title pill, the recognised-text action — and the page itself runs
 * edge to edge with only enough inset to clear the chrome.
 *
 * The bottom shelf is the exception: it holds where you are (`3 / 8`, live zoom) and
 * the page's verbs (crop, annotate, rotate, more). They are in one container rather
 * than two floating pills because two stacked pills at the bottom is twice the space
 * taken from the page and two things to aim at.
 *
 * Pinch, pan and swipe come from `ZoomablePageView`, the same gesture the editor's
 * filter preview uses — swiping is gated on being at 1x, so a zoomed page can still be
 * panned sideways to its edge.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageViewerScreen(
    docId: String,
    pageId: String,
    repository: ScannerRepository,
    onNavigateBack: () -> Unit,
    onNavigateToCrop: (pageId: String) -> Unit,
    onNavigateToMarkup: (pageId: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val documents by repository.documents.collectAsState()
    val settings by repository.settings.collectAsState()
    val document = remember(documents, docId) { documents.find { it.id == docId } }
    val pages = remember(document) { document?.pages ?: emptyList() }

    var currentPageIndex by remember(pages.size) {
        mutableIntStateOf(pages.indexOfFirst { it.id == pageId }.coerceAtLeast(0))
    }
    val safeIndex = currentPageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
    val currentPage = pages.getOrNull(safeIndex)

    // Held as the state object as well as the delegated value: the zoom badge needs
    // to observe it *by reference* so it can subscribe on its own without the parent
    // reading the float (and therefore recomposing on every pinch frame). `scale` is
    // passed to `ZoomablePageView` as a *lambda*, so this whole screen is read only by
    // the badge and by the layer block that applies the transform.
    val scaleState = remember { mutableFloatStateOf(1f) }
    var scale by scaleState
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showOcrSheet by remember { mutableStateOf(false) }
    var showSignPad by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }

    // Reference the decoded bitmap so the composable recomposes after a re-bake.
    val pageImage = rememberPageImage(
        imageUri = currentPage?.imageUri,
        reqWidth = 1600,
        reqHeight = 2200,
        // A rotate re-bakes to the same path, so the revision is the only thing
        // that tells this screen the pixels changed.
        revision = currentPage?.revision ?: 0
    )

    LaunchedEffect(safeIndex) {
        scale = 1f
        offset = Offset.Zero
    }

    // Re-bake so the rotation is real everywhere — the viewer, the grid and the PDF —
    // and so the crossfade has a new bitmap to land on. Reset the transform afterwards:
    // the page just turned, and keeping a 2x pan from the old orientation points it at
    // a part of the page that no longer exists there.
    // True while a rotate is being baked. The button turns into a spinner for that
    // window, because a page that stays still for half a second after a tap reads as
    // "the button did nothing" — the exact complaint this fixes.
    var isRotating by remember(currentPage?.id) { mutableStateOf(false) }

    fun rotateCurrentPage() {
        val page = currentPage ?: return
        if (isRotating) return
        isRotating = true
        scale = 1f
        offset = Offset.Zero
        scope.launch {
            val latest = repository.getDocumentById(docId)
                ?.pages?.find { it.id == page.id }
            val baked = latest?.let {
                runCatching {
                    PageRenderer.bake(
                        context,
                        it.copy(rotationDegrees = (it.rotationDegrees + 90f) % 360f)
                    )
                }.getOrNull()
            }
            if (baked != null) repository.updatePage(docId, page.id) { baked }
            isRotating = false
        }
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
                // One gesture for the whole app: pinch, clamped pan, and a
                // horizontal swipe that turns the page — but only at 1x, because
                // zoomed in a horizontal drag is the only way to reach the edge of
                // the page. See `ZoomablePageView`.
                //
                // `zoom = { scale }` rather than `scale`: the transform is read in
                // the draw phase, so a pinch updates a layer and recomposes neither
                // this page nor either chrome bar. The old hand-written gesture
                // existed to buy exactly that, and it is why this screen never got
                // swipe: the two features lived in the same lambda and only one
                // could win.
                ZoomablePageView(
                    zoom = { scale },
                    pan = { offset },
                    onZoomPanChange = { newScale, newOffset ->
                        scale = newScale
                        offset = newOffset
                    },
                    // A one-page document gets no swipe at all, and neither end of a
                    // longer one wraps around: a page turn that lands you back at the
                    // start reads as a bug.
                    onSwipePrev = if (safeIndex > 0) {
                        { currentPageIndex = safeIndex - 1 }
                    } else {
                        null
                    },
                    onSwipeNext = if (safeIndex < pages.lastIndex) {
                        { currentPageIndex = safeIndex + 1 }
                    } else {
                        null
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Pages swap with a crossfade rather than a cut.
                    //
                    // Tapping next used to replace one bitmap with another in a
                    // single frame; at 120 Hz that is 8 ms of nothing, which reads as
                    // a glitch rather than a page turn. 180 ms of crossfade is the
                    // whole transition — no slide, because the page still arrives
                    // from a tap as often as from a swipe, and a slide from a tap
                    // would imply movement that did not happen.
                    Crossfade(
                        targetState = pageImage,
                        animationSpec = tween(durationMillis = 180),
                        label = "pageSwap",
                        modifier = Modifier.fillMaxSize()
                    ) { bitmap ->
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Page ${safeIndex + 1}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                // Asymmetric on purpose: the action bar at the bottom
                                // is taller than the glass strip at the top, and a
                                // page whose last line sits under it is a page you
                                // cannot read.
                                .padding(
                                    start = 6.dp,
                                    end = 6.dp,
                                    top = 68.dp,
                                    bottom = 132.dp
                                )
                        )
                    }
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
            // Rotate and crop used to live up here. They moved down to the action
            // bar with the rest of the page verbs: an action that changes the page
            // belongs with the other actions that change the page, and the top strip
            // is now purely "which document am I in, and take me out".
            ViewerGlassButton(
                icon = Icons.Outlined.TextFields,
                contentDescription = "Recognised text",
                onClick = { showOcrSheet = true }
            )
        }

        // ---- bottom shelf: where you are, and what you can do here -----------
        //
        // One container, built from the app's expressive action bar: pager first
        // because it is the thing you use most while reading, then the page's verbs.
        // The bar is translucent so the page keeps running underneath it — an opaque
        // slab here would crop the paper, which is the opposite of what a reader is
        // for.
        ExpressiveActionBar(
            containerColor = Color.Black.copy(alpha = 0.55f),
            // Round on *all* corners: this bar floats with a margin around it, so the
            // sheet shape (top-only) would leave two square bottom corners hanging in
            // the middle of the page.
            shape = DocShapes.card,
            shadowElevation = 0.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            if (pages.size > 1) {
                ExpressivePager(
                    index = safeIndex,
                    count = pages.size,
                    onPrev = { currentPageIndex = (safeIndex - 1).coerceAtLeast(0) },
                    onNext = {
                        currentPageIndex = (safeIndex + 1).coerceAtMost(pages.size - 1)
                    },
                    contentColor = Color.White,
                    // The rail is white-on-dark, not the theme accent: the accent is
                    // tuned for a light surface and disappears against a photograph.
                    railColor = Color.White.copy(alpha = 0.20f),
                    accentColor = Color.White,
                    trailing = { ViewerZoomBadge(scaleState = scaleState) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 2.dp)
                )
                BarHairline(color = Color.White.copy(alpha = 0.08f))
            }

            ConnectedButtonGroup(
                actions = listOf(
                    ConnectedAction(
                        label = "Crop",
                        icon = Icons.Default.Crop,
                        onClick = { currentPage?.let { onNavigateToCrop(it.id) } }
                    ),
                    ConnectedAction(
                        label = "Annotate",
                        icon = Icons.Outlined.Draw,
                        onClick = { currentPage?.let { onNavigateToMarkup(it.id) } }
                    ),
                    ConnectedAction(
                        label = if (isRotating) "Rotating" else "Rotate",
                        icon = Icons.Default.RotateRight,
                        onClick = { rotateCurrentPage() },
                        enabled = !isRotating
                    ),
                    // The rare actions live behind this cell's menu instead of a fifth
                    // button: sign and export apply here, delete does not.
                    ConnectedAction(
                        label = "More",
                        icon = Icons.Default.MoreVert,
                        // The cell is the menu: no action of its own, no chevron.
                        menuContent = { dismiss ->
                            DropdownMenuItem(
                                text = { Text("Sign this page") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Draw, contentDescription = null)
                                },
                                onClick = {
                                    dismiss()
                                    showSignPad = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export PDF") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
                                },
                                onClick = {
                                    dismiss()
                                    showExportSheet = true
                                }
                            )
                        }
                    )
                ),
                containerColor = ViewerPlate,
                contentColor = Color.White,
                // No theme accent down here: `primary` is tuned for a light surface and
                // would flash purple through a translucent bar over a photograph.
                accentColor = Color.White,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }
    }

    // Signing stays a dialog — it is one decision with a small pad — while annotating
    // is a full screen of its own now (`MarkupScreen`); both re-bake on save, so the
    // viewer, the grid thumbnail and the next PDF all read the same file.

    if (showSignPad) {
        // Signed onto the page you are reading — the same rule as the document
        // screen, where signing used to stamp page 1 of the document regardless.
        val targetId = currentPage?.id
        SignaturePadDialog(
            onDismiss = { showSignPad = false },
            onSignatureConfirmed = { signaturePath ->
                showSignPad = false
                if (targetId == null) return@SignaturePadDialog
                scope.launch {
                    val latest = repository.getDocumentById(docId)
                        ?.pages?.find { it.id == targetId } ?: return@launch
                    val baked = PageRenderer.bake(
                        context,
                        latest.copy(signatureOverlayUri = signaturePath)
                    )
                    if (baked != null) {
                        repository.updatePage(docId, targetId) { baked }
                    }
                }
            }
        )
    }

    if (showExportSheet) {
        PdfExportSheet(
            title = document?.title ?: "Document",
            pages = pages,
            settings = settings,
            onDismiss = { showExportSheet = false }
        )
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

/**
 * The plate behind an icon in the reader's bar.
 *
 * A dark page behind a translucent black bar is not enough contrast on its own for a
 * 19 dp glyph, so each action sits on a 14 % white plate: the icon reads as a control
 * rather than as part of the photograph.
 */
private val ViewerPlate = Color.White.copy(alpha = 0.14f)

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
    // Fades and springs in rather than appearing: the badge is the only feedback
    // that a pinch *did* something while the fingers are still down, and a value that
    // pops into existence reads as a glitch in the chrome.
    AnimatedVisibility(
        visible = shown > 101,
        enter = fadeIn(Motion.fade) + scaleIn(Motion.bouncy, initialScale = 0.7f),
        exit = fadeOut(tween(120)) + scaleOut(tween(140), targetScale = 0.7f)
    ) {
        Text(
            "$shown%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
