package com.example.docscanner.ui.viewer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Undo
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
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
import com.example.docscanner.model.MarkupTool
import com.example.docscanner.ui.editor.A4_ASPECT
import com.example.docscanner.ui.editor.MarkupInkColors
import com.example.docscanner.ui.editor.MarkupInkSurface
import com.example.docscanner.ui.editor.MarkupSession
import com.example.docscanner.ui.editor.MarkupWidthLabels
import com.example.docscanner.ui.editor.MarkupWidths
import com.example.docscanner.ui.editor.inkLabel
import com.example.docscanner.ui.editor.persistMarkup
import com.example.docscanner.ui.editor.rotateBitmap
import com.example.docscanner.ui.editor.toRotatableBitmap
import com.example.docscanner.ui.pdf.PdfExportSheet
import com.example.docscanner.ui.signature.SignaturePadDialog
import com.example.docscanner.util.PageRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    onNavigateToCrop: (pageId: String) -> Unit
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

    // ---- annotate, in place ---------------------------------------------------
    //
    // Annotate used to push a route: the page you were reading disappeared, a new
    // screen with its own app bar opened, and the ink you drew was on a *different*
    // copy of the same pixels. Nothing about that was necessary — the ink lands in
    // `ScannedPage.markup` and the bake rewrites the same file the reader is already
    // showing. So the shelf now swaps in place: the way out is the same gesture as
    // every other shelf, and the page never leaves your eyes.
    var annotating by remember(currentPage?.id) { mutableStateOf(false) }
    // Bumped on cancel/save so the next session starts from the stored strokes rather
    // than from whatever was half-drawn last time.
    var annotateEpoch by remember(currentPage?.id) { mutableIntStateOf(0) }
    val markup = remember(currentPage?.id, annotateEpoch) {
        MarkupSession(currentPage?.markup.orEmpty())
    }
    var isSavingMarkup by remember(currentPage?.id) { mutableStateOf(false) }

    // The page shrinks a little while the taller annotate shelf is up, animated so the
    // reader does not jump: the shelf grows from the bottom and the paper makes room
    // for it in the same 220 ms.
    val pageBottomInset by animateDpAsState(
        targetValue = if (annotating) AnnotateShelfInset else ReaderShelfInset,
        animationSpec = tween(220, easing = Motion.EaseOut),
        label = "viewerPageBottomInset"
    )

    // Back leaves annotate mode, not the page: the shelf is a mode, and the platform
    // back gesture is how a mode is left.
    BackHandler(enabled = annotating && !isSavingMarkup) {
        annotating = false
        annotateEpoch++
    }

    fun saveMarkup() {
        val page = currentPage ?: return
        if (isSavingMarkup) return
        isSavingMarkup = true
        val strokes = markup.strokes
        scope.launch {
            // The bake is a full-resolution decode, warp and filter pass — off the main
            // thread, or the shelf stutters while the toast-free save runs.
            withContext(Dispatchers.IO) {
                persistMarkup(context, repository, docId, page.id, strokes)
            }
            isSavingMarkup = false
            annotating = false
            annotateEpoch++
            Toast.makeText(context, "Annotation saved", Toast.LENGTH_SHORT).show()
        }
    }

    // ---- rotate --------------------------------------------------------------
    //
    // A rotate is a *bake*: decode the pristine page, warp, filter, encode, write. That
    // is hundreds of milliseconds on a real page, and until it finished the page sat
    // there unchanged — which is the whole of the "rotate is slow" report. It is not
    // made faster by cheating on the bake (the pixels must stay exactly what the
    // pipeline produces); it is made to *feel* instant, by turning the page already
    // decoded on screen the moment the finger lifts and letting the bake replace it
    // when it lands.
    var isRotating by remember(currentPage?.id) { mutableStateOf(false) }
    // The turned preview, and the bitmap it was turned from — the bake's arrival is
    // detected by the *identity* of the newly loaded page, which is exactly when the
    // preview stops being needed.
    var optimisticPage by remember(currentPage?.id) { mutableStateOf<ImageBitmap?>(null) }
    var rotateBaseline by remember(currentPage?.id) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(pageImage) {
        val baseline = rotateBaseline
        if (baseline != null && pageImage != null && pageImage !== baseline) {
            optimisticPage = null
            rotateBaseline = null
            isRotating = false
        }
    }

    val shownPage = optimisticPage ?: pageImage

    fun rotateCurrentPage() {
        val page = currentPage ?: return
        if (isRotating) return
        val source = pageImage ?: return
        isRotating = true
        scale = 1f
        offset = Offset.Zero
        scope.launch {
            // A 90° turn of the decoded page is a memory copy, not a pipeline pass: it
            // costs a few milliseconds even at 1600 px, and it happens before the first
            // frame after the tap.
            val turned = withContext(Dispatchers.Default) {
                source.toRotatableBitmap()
                    ?.let { runCatching { rotateBitmap(it, 90) }.getOrNull() }
                    ?.asImageBitmap()
            }
            if (turned != null) {
                optimisticPage = turned
                rotateBaseline = source
            }
            withContext(Dispatchers.IO) {
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
                baked != null
            }.also { bakedOk ->
                // Only a *failed* bake clears the turned preview here. A successful one
                // keeps it up until the re-decoded page arrives, so the reader never
                // flickers back to the unturned bitmap for a frame.
                if (!bakedOk && rotateBaseline === source) {
                    optimisticPage = null
                    rotateBaseline = null
                }
            }
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
            annotating -> {
                // Annotate, on the page you are reading. The surface is sized to the
                // page's own aspect so a stroke lands where the finger was at bake
                // resolution — the ink is normalised against this box.
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 6.dp, end = 6.dp, top = 68.dp, bottom = pageBottomInset)
                ) {
                    val image = shownPage
                    val aspect = image?.let { it.width.toFloat() / it.height.toFloat() }
                        ?: A4_ASPECT
                    val surfaceWidth = minOf(maxWidth, maxHeight * aspect)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        MarkupInkSurface(
                            session = markup,
                            image = image,
                            modifier = Modifier.size(
                                width = surfaceWidth,
                                height = surfaceWidth / aspect
                            )
                        )
                    }
                }
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
                        targetState = shownPage,
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
                                    bottom = pageBottomInset
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
            // Two shelves, one slot.
            //
            // Annotate used to be a route: tapping it pushed a screen, the page you
            // were reading vanished under a new app bar, and back meant something
            // different than it does here. It is a *mode of this screen*, so it gets
            // this screen's bottom shelf — swapped, not stacked, and swapped **from the
            // bottom**, which is where it came from conceptually and where the thumb
            // already is.
            AnimatedContent(
                targetState = annotating,
                transitionSpec = {
                    if (targetState) {
                        // Annotate settings rise into the slot the verbs were in.
                        (
                            slideInVertically(
                                animationSpec = tween(240, easing = Motion.EaseOut),
                                initialOffsetY = { it }
                            ) + fadeIn(tween(150))
                            ).togetherWith(fadeOut(tween(120)))
                    } else {
                        // They drop back out and the verbs fade in place — a reader
                        // shelf that slid in from below would read as a new bar being
                        // thrown at the user, when in fact nothing moved but a mode.
                        fadeIn(tween(180)).togetherWith(
                            slideOutVertically(
                                animationSpec = tween(200, easing = Motion.EaseIn),
                                targetOffsetY = { it }
                            ) + fadeOut(tween(120))
                        )
                    }
                },
                label = "viewerShelf"
            ) { editing ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (editing) {
                        AnnotateShelf(
                            markup = markup,
                            isSaving = isSavingMarkup,
                            onCancel = {
                                annotating = false
                                annotateEpoch++
                            },
                            onSave = { saveMarkup() }
                        )
                    } else {
                        if (pages.size > 1) {
                            ExpressivePager(
                                index = safeIndex,
                                count = pages.size,
                                onPrev = { currentPageIndex = (safeIndex - 1).coerceAtLeast(0) },
                                onNext = {
                                    currentPageIndex = (safeIndex + 1).coerceAtMost(pages.size - 1)
                                },
                                contentColor = Color.White,
                                // The rail is white-on-dark, not the theme accent: the
                                // accent is tuned for a light surface and disappears
                                // against a photograph.
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
                                    // No route: the shelf below becomes the annotate
                                    // settings, over the page that was already open.
                                    onClick = {
                                        annotateEpoch++
                                        annotating = true
                                    }
                                ),
                                ConnectedAction(
                                    label = "Rotate",
                                    icon = Icons.Default.RotateRight,
                                    onClick = { rotateCurrentPage() },
                                    enabled = !isRotating
                                ),
                                // The rare actions live behind this cell's menu instead
                                // of a fifth button: sign and export apply here, delete
                                // does not.
                                ConnectedAction(
                                    label = "More",
                                    icon = Icons.Default.MoreVert,
                                    // The cell is the menu: no action of its own, no
                                    // chevron.
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
                                                Icon(
                                                    Icons.Outlined.PictureAsPdf,
                                                    contentDescription = null
                                                )
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
                            // No theme accent down here: `primary` is tuned for a light
                            // surface and would flash purple through a translucent bar
                            // over a photograph.
                            accentColor = Color.White,
                            onAccentColor = Color.Black,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }

    // Signing stays a dialog — it is one decision with a small pad. Annotating is the
    // shelf above, on this page; both re-bake on save, so the viewer, the grid thumbnail
    // and the next PDF all read the same file.

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

/**
 * Bottom inset the page keeps clear for the reader's shelf.
 *
 * The page is laid out *above* the bar rather than under it: a page whose last line
 * sits beneath a translucent slab is a line you cannot read.
 */
private val ReaderShelfInset = 132.dp

/**
 * The same inset while the annotate shelf is up.
 *
 * Taller, because annotate is three rows — tools, ink, actions — instead of one. The
 * difference is animated by the caller, so the page makes room instead of jumping.
 */
private val AnnotateShelfInset = 214.dp

/**
 * Annotate settings, in the reader's own shelf.
 *
 * Three rows and no app bar: what the ink *is* (tool), what it looks like (colour and
 * weight), and what to do with it (undo, redo, clear, cancel, save). Every control is a
 * cell of the same connected group the reader's verbs use, so the shelf that replaces
 * the verbs is visibly the same shelf — not a second, differently-built toolbar that
 * happens to sit where the first one was.
 *
 * Everything here writes into one [MarkupSession]; the drawing itself is
 * [MarkupInkSurface], over the page, and the store is the same `persistMarkup` the
 * full-screen editor uses. Nothing about what gets saved changes with the host.
 */
@Composable
private fun AnnotateShelf(
    markup: MarkupSession,
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    // Whiteout paints the paper, so a pen colour has no meaning while it is selected:
    // the swatches grey out rather than silently accepting a tap that would do nothing.
    val inkEnabled = markup.tool != MarkupTool.WHITEOUT

    // The shelf answers the finger too. A mode that arrived from the bottom can be
    // *pushed* back down: dragging the handle moves the whole shelf with the thumb, and
    // past 84 dp it leaves — which is the same grammar as a bottom sheet, applied where
    // the user already is instead of pushing a sheet over the page.
    var dragY by remember { mutableFloatStateOf(0f) }
    val dragScope = rememberCoroutineScope()
    val settle: () -> Unit = {
        dragScope.launch {
            animate(
                initialValue = dragY,
                targetValue = 0f,
                animationSpec = tween(200, easing = Motion.EaseOut)
            ) { value, _ -> dragY = value }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = dragY
                // Fades as it is pulled away, so it cannot look like the bar detached
                // from the screen while it is being moved.
                alpha = (1f - (dragY / (AnnotateShelfInset.toPx() * 1.6f))).coerceIn(0f, 1f)
            }
            .padding(top = 2.dp, bottom = 2.dp)
    ) {
        // The grab handle: 36 × 4 dp of white at 30 %, the same one Google's own
        // sheets use, and the whole 24 dp strip is the target rather than the bar.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dy ->
                            change.consume()
                            dragY = (dragY + dy).coerceAtLeast(0f)
                        },
                        onDragEnd = {
                            if (dragY > 84f) {
                                dragY = 0f
                                onCancel()
                            } else {
                                settle()
                            }
                        },
                        onDragCancel = { settle() }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(DocShapes.pill)
                    .background(Color.White.copy(alpha = 0.30f))
            )
        }

        ConnectedButtonGroup(
            actions = listOf(
                ConnectedAction(
                    label = "Pen",
                    selected = markup.tool == MarkupTool.PEN,
                    onClick = { markup.tool = MarkupTool.PEN }
                ),
                ConnectedAction(
                    label = "Marker",
                    selected = markup.tool == MarkupTool.HIGHLIGHTER,
                    onClick = { markup.tool = MarkupTool.HIGHLIGHTER }
                ),
                ConnectedAction(
                    label = "Erase",
                    selected = markup.tool == MarkupTool.WHITEOUT,
                    onClick = { markup.tool = MarkupTool.WHITEOUT }
                )
            ),
            containerColor = ViewerPlate,
            contentColor = Color.White,
            accentColor = Color.White,
            onAccentColor = Color.Black,
            cellHeight = 40.dp
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarkupInkColors.forEach { ink ->
                val interactions = remember { MutableInteractionSource() }
                val chosen = inkEnabled && ink == markup.colorArgb
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = interactions,
                            indication = ripple(color = Color.White.copy(alpha = 0.22f)),
                            enabled = inkEnabled,
                            onClickLabel = inkLabel(ink),
                            role = Role.RadioButton,
                            onClick = { markup.colorArgb = ink }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer { alpha = if (inkEnabled) 1f else 0.35f }
                            .clip(CircleShape)
                            .background(Color(ink))
                            .border(
                                width = if (chosen) 3.dp else 1.dp,
                                color = if (chosen) Color.White else Color.White.copy(alpha = 0.35f),
                                shape = CircleShape
                            )
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            // A second connected group rather than a slider: three discrete weights are
            // three decisions, and a slider you cannot hit mid-drag is worse than three
            // cells you can.
            ConnectedButtonGroup(
                actions = MarkupWidths.mapIndexed { index, fraction ->
                    ConnectedAction(
                        label = MarkupWidthLabels[index],
                        selected = markup.widthFraction == fraction,
                        enabled = inkEnabled,
                        onClick = { markup.widthFraction = fraction }
                    )
                },
                modifier = Modifier.weight(1f),
                containerColor = ViewerPlate,
                contentColor = Color.White,
                accentColor = Color.White,
                onAccentColor = Color.Black,
                cellHeight = 38.dp
            )
        }

        Spacer(Modifier.height(8.dp))

        ConnectedButtonGroup(
            actions = listOf(
                ConnectedAction(
                    label = "",
                    icon = Icons.Default.Undo,
                    contentDescription = "Undo stroke",
                    enabled = markup.canUndo,
                    onClick = { markup.undo() }
                ),
                ConnectedAction(
                    label = "",
                    icon = Icons.Default.Redo,
                    contentDescription = "Redo stroke",
                    enabled = markup.canRedo,
                    onClick = { markup.redo() }
                ),
                ConnectedAction(
                    label = "",
                    icon = Icons.Default.Delete,
                    contentDescription = "Clear all ink",
                    enabled = markup.canUndo,
                    onClick = { markup.clear() }
                ),
                ConnectedAction(
                    label = "",
                    icon = Icons.Default.Close,
                    contentDescription = "Cancel annotation",
                    enabled = !isSaving,
                    onClick = onCancel
                ),
                ConnectedAction(
                    label = "",
                    icon = Icons.Default.Check,
                    contentDescription = if (isSaving) "Saving annotation" else "Save annotation",
                    enabled = !isSaving,
                    emphasized = true,
                    onClick = onSave
                )
            ),
            containerColor = ViewerPlate,
            contentColor = Color.White,
            // The save cell is the one filled cell, and it is filled **white** with
            // black ink — the same snapping content colour the component gives every
            // filled cell, which is what stopped these toggles blinking out.
            accentColor = Color.White,
            onAccentColor = Color.Black,
            cellHeight = 44.dp
        )
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
