package com.example.docscanner.ui.viewer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import kotlinx.coroutines.CancellationException
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

    // ## Which page is on screen, tracked by *id*
    //
    // This was an index in a `remember(pages.size)` block, and that key is the whole
    // "I tap page 4 and it opens page 1" bug. Copy or move a page and the list length
    // changes; `pages.size` is then a *different* number, the remembered index is
    // discarded, and the screen re-seeds from `pageId` — the id the navigation passed
    // in when the document was opened, which points at page 1. So the page the finger
    // landed on was resolved correctly for one frame and then thrown away.
    //
    // Worse, keying on the *size* also means a reorder that keeps the length (drag one
    // page past another) never re-seeds and never notices the page under the viewer
    // changed. An id cannot go stale that way: it is resolved against the live list on
    // every recomposition, and a missing id (page moved out) falls back to the page the
    // caller asked for rather than to a far-away index.
    var currentPageId by remember(pageId) { mutableStateOf(pageId) }
    val trackedIndex = pages.indexOfFirst { it.id == currentPageId }
    val safeIndex = (
        if (trackedIndex >= 0) {
            trackedIndex
        } else {
            pages.indexOfFirst { it.id == pageId }
        }
        ).coerceIn(0, (pages.size - 1).coerceAtLeast(0))
    val currentPage = pages.getOrNull(safeIndex)
    // One way to turn a page, used by the swipe handlers, the pager and the shelf, so
    // the three cannot disagree about what "next" means.
    fun goToPage(index: Int) {
        pages.getOrNull(index)?.let { currentPageId = it.id }
    }

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

    // The page's bottom inset is **measured**, not guessed.
    //
    // It used to be two hard-coded numbers (132 dp for the reader's shelf, 214 dp for
    // the annotate one). Both were wrong on a real phone for two reasons: the shelf's
    // height depends on the font scale, and `navigationBarsPadding` adds an inset that
    // is 0, 24 or 48 dp depending on the device. The result was a bar floating with a
    // hand's width of dead black under it on one phone and clipping the page's last line
    // on another — the "empty space below the page" report. Measuring the bar and
    // feeding its height back as the page's inset makes the two agree on every device,
    // and the *animation* between sizes is what keeps the page from jumping when the
    // shelf swaps.
    var shelfHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val shelfHeight = with(density) { shelfHeightPx.toDp() }
    // A little air between the paper and the bar, the same 8 dp the bar keeps from the
    // screen edges.
    val pageBottomInset by animateDpAsState(
        targetValue = if (shelfHeightPx == 0) ReaderShelfInset else shelfHeight + ShelfGap,
        animationSpec = tween(220, easing = Motion.EaseOut),
        label = "viewerPageBottomInset"
    )

    // Back leaves annotate mode, not the page: the shelf is a mode, and the platform
    // back gesture is how a mode is left. It is *trapped* while the bake runs, because
    // leaving the screen would cancel the coroutine that is still writing the page.
    BackHandler(enabled = annotating) {
        if (!isSavingMarkup) {
            annotating = false
            annotateEpoch++
        }
    }

    fun saveMarkup() {
        val page = currentPage ?: return
        if (isSavingMarkup) return
        isSavingMarkup = true
        val strokes = markup.strokes
        scope.launch {
            // ## The save state is cleared in `finally`, because it locks the screen
            //
            // `isSavingMarkup` does three things at once: it disables the shelf's Cancel
            // cell, it makes the `BackHandler` above a no-op, and it makes this function
            // return immediately. So a throw out of the bake — `persistMarkup` writes the
            // strokes as data and then re-bakes, and `ImageStore.saveEditedPage` can fail
            // on a full disk — used to leave the reader **trapped in annotate mode for the
            // life of the screen**: Cancel dimmed, back ignored, save ignored. The ink is
            // still safe (the strokes are written before the bake), so the honest outcome
            // is a message and a working shelf, not a dead one.
            var saved = false
            try {
                // The bake is a full-resolution decode, warp and filter pass — off the main
                // thread, or the shelf stutters while the toast-free save runs.
                withContext(Dispatchers.IO) {
                    persistMarkup(context, repository, docId, page.id, strokes)
                }
                saved = true
            } catch (cancelled: CancellationException) {
                // The screen is going away; nothing to report, and nothing to unlock.
                throw cancelled
            } catch (_: Throwable) {
                saved = false
            } finally {
                isSavingMarkup = false
            }
            if (saved) {
                annotating = false
                annotateEpoch++
                Toast.makeText(context, "Annotation saved", Toast.LENGTH_SHORT).show()
            } else {
                // The strokes are already stored, so this is not "your ink is gone"; it is
                // "the page has not been re-rendered yet".
                Toast.makeText(
                    context,
                    "Annotation saved; the page could not be re-rendered",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ---- rotate --------------------------------------------------------------
    //
    // A rotate is a *bake*: decode the pristine page, warp, filter, encode, write — a
    // few hundred milliseconds of real work on a real page. It is not made faster by
    // cheating on the bake (the pixels are the contract); it is made to *feel* instant,
    // and then made to stop *twitching* while it lands.
    //
    // Three separate defects, three separate causes:
    //
    // 1. **The delay.** The page used to sit unchanged until the bake finished. It now
    //    turns in memory the moment the finger lifts, and the bake replaces it later.
    // 2. **The button's jitter.** The cell was disabled and relabelled while the bake
    //    ran — the label, the glyph and the whole plate faded to 38 % and back, twice
    //    per tap. The cell is now never disabled: a tap during a bake queues one more
    //    turn, which is what a user pressing it twice actually meant.
    // 3. **The image's jitter.** Two things happened after the bake landed: the decode
    //    of the new file reset `pageImage` to null (a spinner over the page), and the
    //    crossfade then animated *between two copies of the same page*, which reads as
    //    a blur-and-settle. Both are gone: `shownPage` covers the decode gap, and a
    //    change that keeps the page id swaps with no animation at all.
    // One coroutine drains the turn queue; `queued` is at most one, so a second tap
    // means "one more quarter turn", and a fifth tap during the same bake does not spin
    // the page a full circle behind the user's back.
    var baking by remember(currentPage?.id) { mutableStateOf(false) }
    var queuedTurns by remember(currentPage?.id) { mutableIntStateOf(0) }
    // The turned pixels, and the frame they were turned from. Held as a pair so the
    // preview can be dropped the moment the real pixels exist.
    var optimisticPage by remember(currentPage?.id) { mutableStateOf<ImageBitmap?>(null) }
    var rotateBaseline by remember(currentPage?.id) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(pageImage) {
        // A *different* bitmap means the new pixels are on screen and the preview has
        // done its job. Identity, not equality — two identical frames of the same page
        // must not count.
        val baseline = rotateBaseline
        if (baseline != null && pageImage != null && pageImage !== baseline) {
            optimisticPage = null
            rotateBaseline = null
        }
    }

    val shownPage = optimisticPage ?: pageImage

    fun rotateCurrentPage() {
        val page = currentPage ?: return
        if (queuedTurns < 1) queuedTurns++
        if (baking) return
        baking = true
        scale = 1f
        offset = Offset.Zero
        scope.launch {
            while (queuedTurns > 0) {
                queuedTurns--
                // 1. Turn what is on screen *right now* — the previous optimistic frame
                // if there is one, so two taps turn 180° rather than re-turning the
                // original and landing back where it started. A 90° turn of a decoded
                // page is a memory copy: it is on screen in the next frame.
                val source = optimisticPage ?: pageImage
                val turned = source?.let { frame ->
                    withContext(Dispatchers.Default) {
                        frame.toRotatableBitmap()
                            ?.let { runCatching { rotateBitmap(it, 90) }.getOrNull() }
                            ?.asImageBitmap()
                    }
                }
                if (turned != null) {
                    optimisticPage = turned
                    rotateBaseline = source
                }
                // 2. Bake, on the untouched pipeline, off the main thread.
                val bakedOk = withContext(Dispatchers.IO) {
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
                }
                if (!bakedOk) {
                    // A failed bake must not leave a turned preview on screen for ever:
                    // it would be a lie about what the file contains.
                    optimisticPage = null
                    rotateBaseline = null
                    queuedTurns = 0
                    Toast.makeText(context, "Could not rotate this page", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            baking = false
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
            // `shownPage`, not `pageImage`: a re-bake (rotate, annotate) makes the
            // loader return null for a frame or two while it decodes the new file, and a
            // spinner over a page that is already on screen was half of the "loading
            // jitter after rotation".
            shownPage == null -> {
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
                        { goToPage(safeIndex - 1) }
                    } else {
                        null
                    },
                    onSwipeNext = if (safeIndex < pages.lastIndex) {
                        { goToPage(safeIndex + 1) }
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
                    // A *page turn* crossfades; a *re-bake* does not.
                    //
                    // A plain crossfade over the bitmap alone cannot tell the two apart, so
                    // every rotate ended with 180 ms of blur between two copies of the
                    // same page — the second half of the "loading jitter". Keying on a
                    // frame that carries the page id lets the transition answer the only
                    // question that matters: did the page change, or did its pixels?
                    AnimatedContent(
                        targetState = PageFrame(
                            bitmap = shownPage,
                            pageId = currentPage?.id.orEmpty()
                        ),
                        transitionSpec = {
                            if (initialState.pageId == targetState.pageId) {
                                // Same page, new pixels: swap. The turn was already
                                // animated by the finger, and animating the bake on top
                                // of it is what made it look like a reload.
                                EnterTransition.None togetherWith ExitTransition.None
                            } else {
                                fadeIn(tween(180)) togetherWith fadeOut(tween(180))
                            }
                        },
                        label = "pageSwap",
                        modifier = Modifier.fillMaxSize()
                    ) { frame ->
                        val bitmap = frame.bitmap
                        if (bitmap != null) {
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
            // The card's own surface **follows the app's theme**, and it is the same
            // colour in both states on purpose.
            //
            // While reading, the shelf floats over a photograph, so a theme surface is
            // the only tone that is guaranteed to be legible against whatever the page
            // happens to be — the old 55 % black was tuned for a white page and looked
            // like a hole over a dark one. While annotating, the shelf holds real form
            // controls (tool and weight switches, colour swatches, undo/redo) and those
            // are theme controls: painting them on black was why they read as a
            // different app's widgets. One surface, the app's own, in both modes.
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            // Round on *all* corners: this bar floats with a margin around it, so the
            // sheet shape (top-only) would leave two square bottom corners hanging in
            // the middle of the page.
            shape = DocShapes.card,
            shadowElevation = 0.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                // Measured, so the page's inset above can be the *truth* rather than a
                // guess, and so the card's frame is the same on every device.
                .onSizeChanged { newSize ->
                    if (newSize.height > 0 && kotlin.math.abs(shelfHeightPx - newSize.height) > 1) {
                        shelfHeightPx = newSize.height
                    }
                }
                // One margin, used by the page inset too: the card floats 8 dp off the
                // screen's sides and 8 dp off the navigation bar, and every gap inside
                // it is the same 8 dp.
                .padding(horizontal = ShelfPadding, vertical = ShelfPadding)
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
                    // ## `SizeTransform(clip = false)`, built explicitly
                    //
                    // `AnimatedContent` animates its own size and, by default, **clips its
                    // content to the size it currently has**. These two shelves are
                    // different heights — the annotate shelf is roughly twice the verbs
                    // shelf — so for the ~200 ms of a swap the incoming shelf's lower rows
                    // sit outside the animating box and are clipped clean off. That is a
                    // whole row of controls being *drawn nowhere*, and the surface behind
                    // them is opaque, so there is nothing to hide.
                    //
                    // Written as the `ContentTransform` constructor rather than
                    // `a togetherWith b using x`: `using` binds to whatever expression it
                    // is chained onto, which in an `if/else` is the **else branch only** —
                    // i.e. the in-to-annotate direction, the one that needs it most, would
                    // silently keep the default clipping. Naming the four parts of the
                    // transform makes that impossible to reintroduce.
                    val base = if (targetState) {
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
                    ContentTransform(
                        targetContentEnter = base.targetContentEnter,
                        initialContentExit = base.initialContentExit,
                        sizeTransform = SizeTransform(clip = false)
                    )
                },
                label = "viewerShelf"
            ) { editing ->
                // The shelf's own inner frame. `padding(ShelfPadding)` on the six edges
                // this Column can touch — top, bottom and the two sides — is what makes
                // the card's edge and its contents evenly spaced: the distance from the
                // card's rounded corner to the first button is the same as the distance
                // between two rows, which is the same as the gap between two cells.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = ShelfPadding,
                            end = ShelfPadding,
                            top = if (editing) 0.dp else ShelfPadding,
                            bottom = ShelfPadding
                        )
                ) {
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
                                onPrev = { goToPage(safeIndex - 1) },
                                onNext = { goToPage(safeIndex + 1) },
                                // Theme ink on the theme surface: white-on-dark died
                                // with the black bar.
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                railColor = MaterialTheme.colorScheme.onSurface
                                    .copy(alpha = 0.16f),
                                accentColor = MaterialTheme.colorScheme.primary,
                                trailing = { ViewerZoomBadge(scaleState = scaleState) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            // The hairline sits in the same 8 dp rhythm as everything
                            // else, rather than 2 dp from the pager and 4 dp from the
                            // verbs.
                            Spacer(Modifier.height(ShelfPadding))
                            BarHairline(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                            Spacer(Modifier.height(ShelfPadding))
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
                                    // It never says "Rotating", never dims and is
                                    // never disabled.
                                    //
                                    // Dimming the whole cell to 38 % was the "loading
                                    // jitter" in this button: the label and the glyph
                                    // faded, the plate stayed put, and then everything
                                    // snapped back when the bake finished. A tap now
                                    // turns the page immediately (see
                                    // `rotateCurrentPage`) and queues one more turn if
                                    // another tap arrives while the bake runs, so there
                                    // is nothing to wait for and nothing to grey out.
                                    label = "Rotate",
                                    icon = Icons.Default.RotateRight,
                                    onClick = { rotateCurrentPage() }
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
                            // Cells are *inset* into the card rather than painted on
                            // top of it: `surfaceContainerLowest` on
                            // `surfaceContainerHigh` is one step, which is the app's
                            // existing "container inside a container" pairing, and it
                            // gives the group a visible frame without a border.
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            accentColor = MaterialTheme.colorScheme.primary,
                            onAccentColor = MaterialTheme.colorScheme.onPrimary,
                            // Cells slightly taller than the M3 default: the bar has
                            // room (the page inset is measured now), and a 52 dp cell is
                            // a comfortable thumb target on a floating card.
                            cellHeight = 52.dp
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
                    // Same rule as the rotate path above: a failed bake must not leave
                    // a signature that is not on the page looking applied, and it must
                    // not escape this coroutine either (see the document screen).
                    val baked = runCatching {
                        PageRenderer.bake(
                            context,
                            latest.copy(signatureOverlayUri = signaturePath)
                        )
                    }.getOrNull()
                    if (baked != null) {
                        repository.updatePage(docId, targetId) { baked }
                    } else {
                        Toast.makeText(context, "Could not sign this page", Toast.LENGTH_SHORT)
                            .show()
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
 * First-frame guess for the shelf's height, before it has been measured.
 *
 * The page is laid out *above* the bar rather than under it: a page whose last line sits
 * beneath a translucent slab is a line you cannot read. One measure pass later the real
 * height replaces this (see `shelfHeightPx`), so the guess only has to be close enough
 * that the first frame does not look wrong.
 */
private val ReaderShelfInset = 132.dp

/**
 * Air between the paper and the shelf.
 *
 * The same 8 dp the shelf keeps from the screen edges and the same 8 dp between its own
 * rows: one distance used everywhere is what makes a floating bar look deliberate
 * instead of approximately placed.
 */
private val ShelfGap = 8.dp

/**
 * The inset every edge and every gap inside a shelf uses.
 *
 * A floating card has **one** margin. The bar sits 8 dp from the screen's sides, its
 * rows are 8 dp apart, and the first and last row are 8 dp from the card's own edge —
 * so the card reads as a container with a consistent frame around its contents rather
 * than as buttons that happened to land inside a shape.
 */
private val ShelfPadding = 8.dp

/**
 * Annotate settings, in the reader's own shelf.
 *
 * Three rows and no app bar: what the ink *is* (tool), what it looks like (colour and
 * weight), and what to do with it (undo, redo, clear, cancel, save).
 *
 * All three rows are one `ConnectedButtonGroup` each — the same cells the reader's verbs
 * use, because this shelf *replaces* that one and has to look like it. What changed when
 * the "the buttons disappear when I tap" report was fixed is inside the cell
 * (`ConnectedButtonGroup`) and inside this shelf (nothing is disabled by the tool
 * choice), **not** the control: the plate, the ink, the shapes and the sizes the user
 * sees are the ones that were already there.
 *
 * Everything here writes into one [MarkupSession]; the drawing itself is
 * [MarkupInkSurface], over the page, and the store is the same `persistMarkup` the
 * full-screen editor uses. Nothing about what gets saved changes with the host.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnnotateShelf(
    markup: MarkupSession,
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    // ## No control on this shelf is ever disabled
    //
    // Choosing the eraser used to disable the width group (a whiteout has no *colour*,
    // so the argument went, and a width group got switched off with it). Disabled cells
    // are painted at 38 % — and on a white card over a light bar that is not "dimmed",
    // it is *gone*. That is the second half of "thin and bold disappear when I tap
    // Erase": they were being disabled by a decision made two rows above them.
    //
    // Both rows are live now. Weight applies to the eraser as much as to the pen (a
    // fat whiteout is how you clear a line of print), and the swatches stay selectable
    // in every tool: a colour chosen while erasing is simply the colour the next stroke
    // will use. A control that is present but inert is worse than one that is present
    // and harmless.

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
                // ## Translation only — this shelf owns no alpha
                //
                // The drag used to fade the shelf as it was pulled down (`alpha =
                // 1 - dragY / inset * 3`). It is a *whole-shelf* alpha: one stale
                // `dragY` — a settle whose coroutine was cancelled by a recomposition
                // that left the composable mid-animation, a drag interrupted by the
                // page's own gesture, a `dragY` written by two scopes at once — and
                // every control on the shelf is drawn at partial opacity until the
                // shelf is re-entered. That is the same ownership mistake `Round 55`
                // removed from `ExpressiveActionBar`: **a persistent piece of chrome
                // must not own an animation that can hide it.**
                //
                // A translation cannot hide anything, at any value, so the drag keeps
                // its movement and loses the one mechanism on this shelf that could
                // make *all* of its buttons disappear at once. The mode still leaves
                // the screen the same way: past 84 dp it cancels, and the shelf swap
                // below slides it out.
            }
    ) {
        // ## The 20 dp band that is both the handle and the drag target
        //
        // The band is 8 dp above the dash, 4 dp of dash, 8 dp below it — so the gap from
        // the card's edge to the dash and the gap from the dash to the first control are
        // the same 8 dp the rest of the shelf uses. (It used to be a 28 dp strip with the
        // dash centred in it, which left ~18 dp of unexplained air at the top of the card
        // against 8 dp everywhere else — the "uneven edge-to-button spacing" report.)
        //
        // The drag lives here rather than on the whole shelf. A `pointerInput` drag
        // detector wrapped around a row of buttons sits in the same pass as their click
        // handlers: a tap that drifts a few pixels vertically is claimed by the drag and
        // the click is cancelled, so the control under the finger does nothing — or, with
        // the press state animating, *flickers*. A dedicated target cannot do that: the
        // buttons are siblings of this band, not children of it.
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
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
                .padding(top = ShelfPadding)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(DocShapes.pill)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
            )
        }

        // ## Tool and weight: the app's own connected group
        //
        // These two rows are `ConnectedButtonGroup`, the component every other shelf and
        // per-page action in the app uses — and the one they must stay: a behaviour report
        // is answered with a behaviour fix, not by swapping the control out.
        //
        // This is the third time that has had to be said (Round 55 swapped these rows and
        // Round 56 put them back; Round 66 swapped them again — see `docs/AGENT_COORDINATION.md`
        // §6as and §6bc). If a report says a button on this shelf vanishes, the answer is in
        // `ConnectedButtonGroup`'s cell, never in a different control.
        //
        // They were briefly M3 `SingleChoiceSegmentedButtonRow` + `SegmentedButton`, to get
        // rid of the animated tick. That traded one mechanism for three:
        //
        // * `SegmentedButton` carries `defaultMinSize(minWidth = 58.dp)`, so three cells
        //   cannot be narrower than 174 dp. This row shares its line with four 34 dp
        //   swatches, which leaves 160 dp on a 320 dp-wide window or in split screen — and
        //   what does not fit is **drawn outside the card**, i.e. a button disappears. The
        //   connected group has no minimum width: its cells are equal shares and shrink.
        // * the label was given a heavier weight when selected, which changes the text's
        //   width; the label has no `maxLines`, so a word that fits on one line at Medium
        //   can wrap to two at SemiBold — the row gets taller *when you tap it*, and the
        //   shelf height feeds the page inset above it.
        // * `SegmentedButtonDefaults.colors(...)` is constructed per call, so the six cells
        //   were being handed six brand-new colour objects on every recomposition of the
        //   shelf, defeating skipping for the whole row.
        //
        // The connected group answers all three by construction, and its cells already say
        // which one is chosen by *filling* — no glyph, no weight change, no minimum width.
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
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            accentColor = MaterialTheme.colorScheme.primary,
            onAccentColor = MaterialTheme.colorScheme.onPrimary,
            cellHeight = 44.dp
        )

        Spacer(Modifier.height(ShelfPadding))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Swatches on a 34 dp grid, so the four dots are spaced by the same rhythm as
            // the cells beside them instead of being a tight cluster that then collides
            // with the weight group.
            MarkupInkColors.forEach { ink ->
                val interactions = remember { MutableInteractionSource() }
                val chosen = ink == markup.colorArgb
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = interactions,
                            indication = ripple(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
                            ),
                            onClickLabel = inkLabel(ink),
                            role = Role.RadioButton,
                            onClick = { markup.colorArgb = ink }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(ink))
                            .border(
                                width = if (chosen) 3.dp else 1.dp,
                                color = if (chosen) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                                shape = CircleShape
                            )
                    )
                }
            }
            Spacer(Modifier.width(ShelfPadding))
            // A second connected group rather than a slider: three discrete weights are
            // three decisions, and a slider you cannot hit mid-drag is worse than three
            // cells you can.
            ConnectedButtonGroup(
                actions = MarkupWidths.mapIndexed { index, fraction ->
                    ConnectedAction(
                        label = MarkupWidthLabels[index],
                        selected = markup.widthFraction == fraction,
                        onClick = { markup.widthFraction = fraction }
                    )
                },
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                accentColor = MaterialTheme.colorScheme.primary,
                onAccentColor = MaterialTheme.colorScheme.onPrimary,
                cellHeight = 44.dp
            )
        }

        Spacer(Modifier.height(ShelfPadding))

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
                        // The save cell is the group's primary action: filled with the theme
                        // accent, with the theme's on-accent ink. Not an icon swap — a busy
                        // state that changes the *glyph* changes the button under the finger.
                        //
                        // **And it does not disable itself while saving.** `onSave` is
                        // `saveMarkup`, which returns immediately if a save is already
                        // running, so enabling it costs nothing — whereas disabling it dims
                        // the one cell the user just tapped, which is the "the button
                        // disappeared when I tapped Save" half of the report that the
                        // full-screen editor had too.
                        icon = Icons.Default.Check,
                        contentDescription = "Save annotation",
                        emphasized = true,
                        onClick = onSave
                    )
            ),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            accentColor = MaterialTheme.colorScheme.primary,
            onAccentColor = MaterialTheme.colorScheme.onPrimary,
            cellHeight = 48.dp
        )
    }
}

/**
 * What the page area paints: the pixels, and which page they belong to.
 *
 * The id is the whole point. A crossfade over the bitmap alone animates every re-bake
 * (rotate, annotate, sign) as if it were a page turn, which is what made a rotate end in
 * a blur-and-settle. With the id on the frame, a pixel swap is a swap and a page turn is
 * a turn.
 */
private data class PageFrame(val bitmap: ImageBitmap?, val pageId: String)

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
