package com.example.docscanner.ui.camera

import android.Manifest
import android.media.MediaActionSound
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import com.example.docscanner.theme.DocShapes
import com.example.docscanner.theme.LocalDocAccents
import com.example.docscanner.theme.Motion
import com.example.docscanner.theme.pressScale
import com.example.docscanner.ui.common.PageThumbnail
import com.example.docscanner.ui.common.PillButton
import com.example.docscanner.ui.common.SegmentedPill
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.util.DocumentNaming
import com.example.docscanner.util.GalleryExporter
import com.example.docscanner.util.ImageStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID

/** Flash behaviour for capture + torch state for the live preview. */
private enum class FlashMode { AUTO, ON, OFF }

/**
 * Live document capture.
 *
 * This screen renders a real CameraX preview into a [PreviewView] host and saves
 * every shutter press as a JPEG through [ImageStore] (EXIF-corrected and stored in
 * `filesDir/pages`). The previous revision drew a black box, faked a shutter, and
 * recorded a path that never existed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerCameraScreen(
    targetDocId: String? = null,
    repository: ScannerRepository,
    onFinishScan: (docId: String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val accents = LocalDocAccents.current
    val capturedPages = remember { mutableStateListOf<ScannedPage>() }
    // Settings are read through repository.currentSettings() at the moment of use
    // (not copied into composition state) so a change made on the Settings screen
    // applies to the very next shutter press.
    val shutterSound = remember { runCatching { MediaActionSound() }.getOrNull() }
    DisposableEffect(Unit) {
        onDispose { runCatching { shutterSound?.release() } }
    }
    var isBatchMode by remember { mutableStateOf(true) }
    var flashMode by remember { mutableStateOf(FlashMode.AUTO) }
    var isCapturing by remember { mutableStateOf(false) }
    // Transient, auto-dismissing feedback ("saved, gallery copy failed").
    var statusMessage by remember { mutableStateOf<String?>(null) }
    // A failure that will not fix itself by waiting: camera bind error.
    var cameraError by remember { mutableStateOf<String?>(null) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val previewView = remember {
        // FIT_CENTER, not FILL_CENTER: if a device refuses 4:3 and the stream comes
        // back in some other shape, FIT_CENTER letterboxes it inside the 3:4 host
        // so the user still sees exactly what will be captured. FILL_CENTER would
        // crop the preview and hand back a wider photo than the one framed.
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FIT_CENTER }
    }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }

    // ---- zoom ------------------------------------------------------------
    // Pinch drives `cameraControl.setZoomRatio`, not a scaled preview: the sensor
    // crop changes, so the captured JPEG is zoomed too. Scaling only the preview
    // would zoom the viewfinder and then hand back an unzoomed photo.
    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var minZoomRatio by remember { mutableFloatStateOf(1f) }
    var maxZoomRatio by remember { mutableFloatStateOf(1f) }

    // ---- bind CameraX -----------------------------------------------------
    // `bindAttempt` is part of the key so the error card can offer a real retry:
    // a camera that is briefly busy (another app still closing) is the single
    // most common bind failure, and leaving the screen to fix it is a poor answer.
    var bindAttempt by remember { mutableStateOf(0) }
    DisposableEffect(hasCameraPermission, bindAttempt) {
        if (!hasCameraPermission) {
            onDispose { }
        } else {
            val providerFuture = ProcessCameraProvider.getInstance(context)
            val executor = ContextCompat.getMainExecutor(context)
            var provider: ProcessCameraProvider? = null

            providerFuture.addListener(
                {
                    try {
                        val resolved = providerFuture.get().also { provider = it }
                        // 4:3 for both use cases.
                        //
                        // The sensor's native shape is 4:3, so asking for it is also
                        // the only way to avoid a crop: any other ratio is produced by
                        // cutting the frame down, which throws away exactly the
                        // border pixels the detector needs.
                        val resolution = ResolutionSelector.Builder()
                            .setAspectRatioStrategy(
                                AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
                            )
                            .build()
                        val preview = Preview.Builder()
                            .setResolutionSelector(resolution)
                            .build()
                            .also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .setResolutionSelector(resolution)
                            .build()

                        resolved.unbindAll()
                        val camera = resolved.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture
                        )
                        imageCapture = capture
                        boundCamera = camera
                    } catch (t: Throwable) {
                        cameraError = t.message ?: "unknown error"
                    }
                },
                executor
            )

            onDispose {
                runCatching { provider?.unbindAll() }
                imageCapture = null
                boundCamera = null
            }
        }
    }

    // ---- apply flash / torch whenever the mode changes --------------------
    LaunchedEffect(flashMode, boundCamera, imageCapture) {
        val camera = boundCamera ?: return@LaunchedEffect
        val capture = imageCapture ?: return@LaunchedEffect
        capture.flashMode = when (flashMode) {
            FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
            FlashMode.ON -> ImageCapture.FLASH_MODE_ON
            FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
        }
        // Torch only makes sense for the live preview when the user forced flash on.
        runCatching {
            if (camera.cameraInfo.hasFlashUnit()) {
                camera.cameraControl.enableTorch(flashMode == FlashMode.ON)
            }
        }
    }

    // ---- discover the zoom range ------------------------------------------
    // `cameraInfo.zoomState` is a LiveData that is often still null immediately
    // after binding, so poll briefly instead of assuming the device cannot zoom.
    LaunchedEffect(boundCamera) {
        val camera = boundCamera ?: return@LaunchedEffect
        repeat(15) {
            val state = camera.cameraInfo.zoomState.value
            if (state != null) {
                maxZoomRatio = state.maxZoomRatio
                minZoomRatio = state.minZoomRatio.coerceAtLeast(1f)
                zoomRatio = zoomRatio.coerceIn(minZoomRatio, maxOf(minZoomRatio, maxZoomRatio))
                return@LaunchedEffect
            }
            delay(100)
        }
    }

    // ---- apply zoom whenever it changes -----------------------------------
    //
    // *Not* keyed on `zoomRatio`, and not one camera call per touch event.
    //
    // A pinch produces a new zoom value on every pointer move — at 120 Hz that is
    // up to 120 IPC round-trips a second into the camera service, each one queued
    // behind the last, so the preview ended up chasing the finger and kept drifting
    // after it stopped. `snapshotFlow` + `conflate` keeps only the newest value and
    // the short pause after each call caps the rate at ~30/s. Zoom is a smooth
    // optical change; 30 Hz is invisible, and the last value always lands because
    // `conflate` hands over the newest rather than dropping it.
    LaunchedEffect(boundCamera, minZoomRatio, maxZoomRatio) {
        val camera = boundCamera ?: return@LaunchedEffect
        val upper = maxOf(minZoomRatio, maxZoomRatio)
        snapshotFlow { zoomRatio }
            .conflate()
            .collect { requested ->
                val target = requested.coerceIn(minZoomRatio, upper)
                runCatching { camera.cameraControl.setZoomRatio(target) }
                delay(ZOOM_APPLY_INTERVAL_MS)
            }
    }

    // ---- gallery import ---------------------------------------------------
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            uris.forEach { uri ->
                val pageId = UUID.randomUUID().toString()
                val quality = repository.currentSettings().imageQuality
                val savedPath = withContext(Dispatchers.IO) {
                    ImageStore.importFromUri(
                        context = context,
                        uri = uri,
                        pageId = pageId,
                        maxDimension = quality.maxDimension,
                        quality = quality.jpegQuality
                    )
                }
                if (savedPath != null) {
                    capturedPages.add(
                        ScannedPage(
                            id = pageId,
                            documentId = targetDocId ?: "",
                            pageIndex = capturedPages.size,
                            imageUri = savedPath
                        )
                    )
                } else {
                    statusMessage = "Could not import one of the selected images"
                }
            }
        }
    }

    // ---- finalise a scan into the library ---------------------------------
    fun commitScan() {
        if (capturedPages.isEmpty()) return
        val docId = if (targetDocId != null) {
            capturedPages.forEach { repository.addPageToDocument(targetDocId, it) }
            targetDocId
        } else {
            // NOTE: DocumentNaming quotes the literal "Scan". Passing a bare
            // "Scan yyyy-MM-dd HH:mm" here threw IllegalArgumentException inside
            // SimpleDateFormat and crashed the app on the first Done press.
            repository.addDocument(
                DocumentNaming.defaultScanTitle(),
                null,
                capturedPages.toList()
            ).id
        }
        // Recognise the text in the background: search only works on pages whose
        // words have been read, and nothing should make the user wait for that.
        repository.indexTextInBackground(docId)
        onFinishScan(docId)
    }

    // ---- capture feedback --------------------------------------------------
    // The shutter has to answer *before* the file exists: a capture takes a few
    // hundred milliseconds to write, and a button that does nothing for that long
    // reads as broken. Three cues cost almost nothing and are honest about what
    // has happened — the image is in the session list, not yet on disk.
    val haptics = LocalHapticFeedback.current
    val flashAlpha = remember { Animatable(0f) }
    val galleryBump = remember { Animatable(1f) }
    // The viewfinder dips and settles on every shutter press, the way a physical
    // shutter's mirror does: a 1.5 % scale, eight frames, read inside a graphics
    // layer so it never touches composition. It is the cheapest possible "we heard
    // you" and it makes the flash look intentional rather than like a rendering
    // glitch.
    val viewfinderDip = remember { Animatable(1f) }

    // The chrome arrives a beat after the preview does, rising into place.
    //
    // A camera that opens with every control already flat on screen feels like a
    // settings page; the eye has nothing to follow. One 260 ms entrance — 10 dp of
    // rise and a fade, read in a graphics layer so it never re-measures — is the
    // difference between "the screen appeared" and "the camera opened". It runs once
    // per screen, not per frame of anything.
    val chromeIn = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        chromeIn.animateTo(1f, tween(durationMillis = 260, delayMillis = 60, easing = Motion.EaseOut))
    }
    LaunchedEffect(capturedPages.size) {
        // A page just landed: the gallery tile takes it with a small spring, so
        // the newest page announces itself without a toast.
        if (capturedPages.isEmpty()) return@LaunchedEffect
        galleryBump.snapTo(0.84f)
        galleryBump.animateTo(1f, Motion.bouncy)
    }

    // One line of coaching on the way in. There is deliberately no guide frame
    // drawn over the preview (see the note in the layout below), so this says the
    // same thing once, then gets out of the way for good.
    var showHint by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(HINT_MS)
        showHint = false
    }

    // Transient messages retire themselves. A snackbar that has to be dismissed
    // is a dialog in disguise.
    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            delay(STATUS_MS)
            statusMessage = null
        }
    }

    // ---- shutter ----------------------------------------------------------
    fun capturePage() {
        val capture = imageCapture ?: run {
            statusMessage = "Camera is still starting up"
            return
        }
        if (isCapturing) return
        isCapturing = true
        statusMessage = null

        // Fires on the press, not on the save: the point is to confirm the press.
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            flashAlpha.snapTo(FLASH_PEAK_ALPHA)
            flashAlpha.animateTo(0f, tween(durationMillis = FLASH_MS, easing = LinearEasing))
        }
        scope.launch {
            viewfinderDip.animateTo(
                targetValue = 0.985f,
                animationSpec = tween(durationMillis = 90, easing = Motion.EaseOut)
            )
            viewfinderDip.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 420f))
        }

        // Settings → Scan → Shutter sound. Many devices play their own click that
        // an app cannot suppress; this only adds ours when the row is on.
        if (repository.currentSettings().shutterSound) {
            runCatching { shutterSound?.play(MediaActionSound.SHUTTER_CLICK) }
        }

        val pageId = UUID.randomUUID().toString()
        val tempFile = File(context.cacheDir, "capture_$pageId.jpg")
        val options = ImageCapture.OutputFileOptions.Builder(tempFile).build()

        capture.takePicture(
            options,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    scope.launch {
                        val quality = repository.currentSettings().imageQuality
                        val savedPath = withContext(Dispatchers.IO) {
                            ImageStore.importFromFile(
                                context = context,
                                sourceFile = tempFile,
                                pageId = pageId,
                                maxDimension = quality.maxDimension,
                                quality = quality.jpegQuality
                            )
                        }
                        if (savedPath == null) {
                            statusMessage = "Capture failed: could not save image"
                            isCapturing = false
                            return@launch
                        }
                        capturedPages.add(
                            ScannedPage(
                                id = pageId,
                                documentId = targetDocId ?: "",
                                pageIndex = capturedPages.size,
                                imageUri = savedPath
                            )
                        )

                        // Settings → Scan → Auto-save to Gallery. A gallery copy is a
                        // courtesy, never a reason to fail the capture: GalleryExporter
                        // swallows its own errors and reports success as a Boolean.
                        if (repository.currentSettings().autoSaveToGallery) {
                            val copied = withContext(Dispatchers.IO) {
                                GalleryExporter.saveToGallery(
                                    context,
                                    File(savedPath),
                                    GalleryExporter.pageFileName()
                                )
                            }
                            if (!copied) {
                                statusMessage = "Page saved, but the gallery copy failed"
                            }
                        }

                        isCapturing = false
                        if (!isBatchMode) commitScan()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    statusMessage = "Capture failed: ${exception.message ?: "unknown error"}"
                    isCapturing = false
                    runCatching { tempFile.delete() }
                }
            }
        )
    }

    // ---- leaving with unsaved pages ---------------------------------------
    var showDiscardDialog by remember { mutableStateOf(false) }
    fun requestClose() {
        if (capturedPages.isEmpty()) onClose() else showDiscardDialog = true
    }
    // System back follows the same rule as the close button. While the discard
    // dialog is up it closes the dialog instead, which is what back should do.
    BackHandler {
        if (showDiscardDialog) showDiscardDialog = false else requestClose()
    }

    // ---- UI ---------------------------------------------------------------
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        if (hasCameraPermission) {
            Box(
                // A 4:3 sensor shown in portrait is 3:4. The viewfinder is exactly
                // that shape and centred, with the surrounding area black: showing
                // the stream edge-to-edge on a 20:9 phone meant the frame the user
                // composed was not the frame that got captured.
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .graphicsLayer {
                        scaleX = viewfinderDip.value
                        scaleY = viewfinderDip.value
                    }
                    .pointerInput(maxZoomRatio) {
                        if (maxZoomRatio <= 1.0001f) return@pointerInput
                        detectTransformGestures(panZoomLock = true) { _, _, zoomChange, _ ->
                            if (zoomChange != 1f) {
                                zoomRatio = (zoomRatio * zoomChange)
                                    .coerceIn(1f, maxZoomRatio)
                            }
                        }
                    }
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // No guide frame over the preview, deliberately.
            //
            // The old overlay drew a dark window with a pulsing dashed border. It
            // was decorative only (never part of the captured JPEG), and it hurt
            // document detection: it taught users to fill the frame right past the
            // paper edge, leaving no page boundary in the photo to find. The
            // one-line hint below says the same thing without training the wrong
            // composition.

            // Scrims. The chrome sits over a white page far more often than over a
            // dark desk, and white-on-white is unreadable. A soft gradient keeps
            // the preview edge-to-edge *and* guarantees contrast where the controls
            // live — a flat bar would cut the viewfinder in half.
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(190.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.62f), Color.Transparent)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.74f))
                        )
                    )
            )

            // The frame the user pressed for, held for a fifth of a second.
            // Cameras have done this since film because it is the only
            // confirmation that can arrive before the file does. Driven through
            // `graphicsLayer` so the animation never re-enters composition.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = flashAlpha.value }
                    .background(Color.White)
            )
        }

        // ---- top chrome -------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = chromeIn.value
                    translationY = (1f - chromeIn.value) * -10.dp.toPx()
                }
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassButton(
                icon = Icons.Default.Close,
                contentDescription = "Close camera",
                onClick = { requestClose() }
            )

            // Equal weights either side of the toggle put it on the true centre
            // line, where a mode control belongs.
            Spacer(Modifier.weight(1f))

            ModeToggle(
                isBatch = isBatchMode,
                onChange = { isBatchMode = it }
            )

            Spacer(Modifier.weight(1f))

            // The icon cross-fades as the mode cycles instead of swapping between
            // frames: flash is a three-state control on a screen where every other
            // control gives feedback, and a glyph that pops is the one thing that
            // reads as unresponsive.
            AnimatedContent(
                targetState = flashMode,
                transitionSpec = {
                    (fadeIn(tween(140)) + scaleIn(tween(140), initialScale = 0.86f))
                        .togetherWith(fadeOut(tween(90)) + scaleOut(tween(90), targetScale = 0.86f))
                },
                label = "flashMode"
            ) { mode ->
                GlassButton(
                    icon = when (mode) {
                        FlashMode.ON -> Icons.Default.FlashOn
                        FlashMode.OFF -> Icons.Default.FlashOff
                        FlashMode.AUTO -> Icons.Default.FlashAuto
                    },
                    contentDescription = when (mode) {
                        FlashMode.ON -> "Flash on"
                        FlashMode.OFF -> "Flash off"
                        FlashMode.AUTO -> "Flash auto"
                    },
                    tint = if (mode == FlashMode.ON) accents.scan else Color.White,
                    onClick = {
                        flashMode = when (mode) {
                            FlashMode.AUTO -> FlashMode.ON
                            FlashMode.ON -> FlashMode.OFF
                            FlashMode.OFF -> FlashMode.AUTO
                        }
                    }
                )
            }
        }

        // ---- page counter / first-run hint ------------------------------------
        // One slot, one message: the counter appears only once a page exists, by
        // which time the hint has already retired, so the two never collide.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 62.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = capturedPages.isNotEmpty(),
                enter = fadeIn() + scaleIn(initialScale = 0.85f),
                exit = fadeOut() + scaleOut(targetScale = 0.85f)
            ) {
                Surface(
                    shape = DocShapes.pill,
                    color = accents.scan,
                    contentColor = accents.onScan,
                    shadowElevation = 8.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // The count rolls rather than swaps: one digit changing is a
                        // 30 ms animation, and it is the only progress feedback on a
                        // screen with no progress bar.
                        AnimatedContent(
                            targetState = capturedPages.size,
                            transitionSpec = {
                                // The default `sizeTransform` clips and animates the
                                // box as the label changes width ("9 pages" → "10
                                // pages"), which is exactly what is wanted here.
                                (slideInVertically { height -> height } + fadeIn())
                                    .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                            },
                            label = "pageCount"
                        ) { count ->
                            Text(
                                text = "$count ${if (count == 1) "page" else "pages"} scanned",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                            )
                        }
                        // Undo, right where the count is.
                        //
                        // A capture is committed to the session the moment the file
                        // exists, and the only way back used to be: finish the scan,
                        // open the document, delete the page. A bad shot is noticed
                        // *now*, while the camera is still pointed at the desk — so
                        // the escape hatch belongs here. It removes the file as well;
                        // `discardPages` is the same call the close dialog uses.
                        // Hairline between the count and the action, so the pill
                        // reads as one object with two parts rather than two pills
                        // that happen to touch.
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(18.dp)
                                .background(accents.onScan.copy(alpha = 0.28f))
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(DocShapes.pill)
                                .clickable(
                                    onClickLabel = "Remove the last page",
                                    role = Role.Button,
                                    onClick = {
                                        val last = capturedPages.removeLastOrNull() ?: return@clickable
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        repository.discardPages(listOf(last))
                                        statusMessage = "Last page removed"
                                    }
                                )
                                .padding(start = 12.dp, end = 14.dp, top = 8.dp, bottom = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Undo,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                "Undo",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showHint && capturedPages.isEmpty() && hasCameraPermission,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = DocShapes.pill,
                    color = Color.Black.copy(alpha = 0.45f),
                    contentColor = Color.White
                ) {
                    Text(
                        text = "Fill the frame with the page",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // ---- transient message ------------------------------------------------
        var lastStatus by remember { mutableStateOf("") }
        LaunchedEffect(statusMessage) { statusMessage?.let { lastStatus = it } }
        AnimatedVisibility(
            visible = statusMessage != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 28.dp, end = 28.dp, bottom = 214.dp)
        ) {
            Surface(
                shape = DocShapes.pill,
                color = Color.Black.copy(alpha = 0.86f),
                contentColor = Color.White,
                shadowElevation = 6.dp
            ) {
                Text(
                    text = lastStatus,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }

        // ---- permission / error states ---------------------------------------
        if (!hasCameraPermission) {
            CameraPermissionCard(
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onImport = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (cameraError != null) {
            CameraErrorCard(
                message = cameraError ?: "",
                onRetry = {
                    cameraError = null
                    bindAttempt++
                },
                onImport = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onClose = onClose,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // ---- bottom controls --------------------------------------------------
        if (hasCameraPermission && cameraError == null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .graphicsLayer {
                        alpha = chromeIn.value
                        translationY = (1f - chromeIn.value) * 14.dp.toPx()
                    }
                    .navigationBarsPadding()
                    .padding(bottom = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Zoom stops, Google-Camera style: discrete, tappable, and honest
                // about what the sensor can do. Pinch still works on the preview.
                val stops = remember(maxZoomRatio) { zoomStops(maxZoomRatio) }
                AnimatedVisibility(visible = stops.size > 1) {
                    Surface(
                        shape = DocShapes.pill,
                        color = Color.Black.copy(alpha = 0.48f),
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(3.dp)
                        ) {
                            stops.forEach { stop ->
                                ZoomStopChip(
                                    label = zoomLabel(stop),
                                    selected = kotlin.math.abs(zoomRatio - stop) < ZOOM_SELECTED_EPSILON,
                                    onClick = {
                                        zoomRatio = stop.coerceIn(minZoomRatio, maxZoomRatio)
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 26.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gallery: the last page captured once there is one, otherwise
                    // the import affordance. Tapping either opens the picker, so a
                    // mis-tap costs the user a picker, never their scan.
                    GalleryTarget(
                        lastPage = capturedPages.lastOrNull(),
                        count = capturedPages.size,
                        scale = galleryBump.value,
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    ShutterButton(
                        isCapturing = isCapturing,
                        enabled = hasCameraPermission,
                        onClick = { capturePage() }
                    )

                    // The right slot mirrors the gallery tile's footprint, which is
                    // what keeps the shutter on the exact centre line. A wide
                    // labelled button here would visibly shove it left.
                    // The finish affordance appears the moment there is something to
                    // finish, and it pops in with the same scale the shutter uses. The
                    // slot itself never changes size, so the shutter cannot drift off
                    // centre while it animates.
                    Box(contentAlignment = Alignment.Center) {
                        this@Row.AnimatedVisibility(
                            visible = capturedPages.isNotEmpty(),
                            // `spring()` as a *scale* enter spec wants
                            // `Animatable`-style visibility thresholds, which the
                            // default `SpringSpec` does not provide for
                            // `EnterTransition`; a fast tween is the honest choice
                            // here and settles in the same 140 ms.
                            enter = fadeIn(tween(120)) + scaleIn(tween(180), initialScale = 0.6f),
                            exit = fadeOut(tween(100)) + scaleOut(tween(140), targetScale = 0.6f)
                        ) {
                            FinishButton(onClick = { commitScan() })
                        }
                    }
                }
            }
        }

        // ---- discard confirmation --------------------------------------------
        if (showDiscardDialog) {
            AlertDialog(
                onDismissRequest = { showDiscardDialog = false },
                shape = DocShapes.dialog,
                title = { Text("Discard this scan?") },
                text = {
                    Text(
                        "${capturedPages.size} " +
                            (if (capturedPages.size == 1) "page has" else "pages have") +
                            " not been added to your library yet."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showDiscardDialog = false
                        // The pages were written to disk as they were captured
                        // (that is what makes the shutter feel instant), so
                        // discarding has to remove them. Nothing else will: no
                        // document points at these files. The repository owns the
                        // delete because this screen is being disposed.
                        repository.discardPages(capturedPages.toList())
                        capturedPages.clear()
                        onClose()
                    }) {
                        Text("Discard", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDiscardDialog = false }) {
                        Text("Keep scanning")
                    }
                }
            )
        }
    }
}

/**
 * Batch / single segmented control.
 *
 * Two states that change what the shutter does, so they are shown as one control
 * with a moving selection rather than as a button that cycles: a cycle makes the
 * user read the label every time to know what the next tap will do.
 *
 * It is the app's [SegmentedPill], not a camera-shaped copy of it. The copy that
 * used to live here faded a white background in and out under each label — the same
 * job, done without the thumb travelling, so switching modes read as two separate
 * colour changes rather than one object moving. One control, one behaviour, and the
 * glass palette comes in as parameters.
 */
@Composable
private fun ModeToggle(isBatch: Boolean, onChange: (Boolean) -> Unit) {
    SegmentedPill(
        options = listOf("Single", "Batch"),
        selectedIndex = if (isBatch) 1 else 0,
        onSelect = { index -> onChange(index == 1) },
        segmentHeight = 34.dp,
        container = Color.Black.copy(alpha = 0.44f),
        thumbColor = Color.White.copy(alpha = 0.94f),
        selectedContent = Color.Black,
        content = Color.White.copy(alpha = 0.82f),
        thumbElevation = 0.dp
    )
}

/** One tap target per zoom stop. Selected state is a filled accent, not a tint. */
@Composable
private fun ZoomStopChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val accents = LocalDocAccents.current
    val interactions = remember { MutableInteractionSource() }
    val container by animateColorAsState(
        targetValue = if (selected) accents.scan else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "zoomChipContainer"
    )
    val content by animateColorAsState(
        targetValue = if (selected) accents.onScan else Color.White.copy(alpha = 0.88f),
        animationSpec = tween(durationMillis = 200),
        label = "zoomChipContent"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 46.dp, height = 44.dp)
            .clip(DocShapes.pill)
            .background(container)
            .clickable(
                interactionSource = interactions,
                indication = ripple(),
                role = Role.RadioButton,
                onClick = onClick
            )
    ) {
        Text(
            text = label,
            color = content,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/** Translucent circular control that stays legible over any preview. */
@Composable
private fun GlassButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = Color.White
) {
    val interactions = remember { MutableInteractionSource() }
    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.42f),
        modifier = Modifier
            .size(46.dp)
            .pressScale(interactions)
            .clip(CircleShape)
            .clickable(interactionSource = interactions, indication = ripple(), onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * The capture button.
 *
 * A white ring, a gap that shows the preview through it, and a white disc: the
 * shape every phone camera has used for fifteen years, because it reads as a
 * physical shutter instead of a button. It compresses under the finger, and while
 * a capture is in flight the ring sweeps amber — that is the one moment the user
 * needs to know the app heard them and is working.
 */
@Composable
private fun ShutterButton(
    isCapturing: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val innerScale by animateFloatAsState(
        targetValue = when {
            isCapturing -> 0.84f
            pressed -> 0.88f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 700f),
        label = "shutterInner"
    )
    val ringAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.35f,
        label = "shutterEnabled"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(84.dp)
            .graphicsLayer { alpha = ringAlpha }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactions,
                indication = null,
                enabled = enabled && !isCapturing,
                onClickLabel = "Capture page",
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 3.dp.toPx()
            drawCircle(
                color = Color.White.copy(alpha = 0.38f),
                radius = size.minDimension / 2f - stroke / 2f,
                style = Stroke(width = stroke)
            )
        }

        if (isCapturing) {
            SpinningRing(modifier = Modifier.fillMaxSize())
        }

        Box(
            modifier = Modifier
                .size(62.dp)
                .graphicsLayer {
                    scaleX = innerScale
                    scaleY = innerScale
                }
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/** Amber arc sweeping the shutter ring while a capture is being written. */
@Composable
private fun SpinningRing(modifier: Modifier = Modifier) {
    val accents = LocalDocAccents.current
    val transition = rememberInfiniteTransition(label = "shutterSpin")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing)
        ),
        label = "shutterSpinAngle"
    )
    Canvas(modifier = modifier) {
        val stroke = 3.5.dp.toPx()
        drawArc(
            color = accents.scan,
            startAngle = angle,
            sweepAngle = 96f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(stroke / 2f, stroke / 2f),
            size = androidx.compose.ui.geometry.Size(
                size.width - stroke,
                size.height - stroke
            ),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

/** Left-hand target: the last page captured, or the photo library. */
@Composable
private fun GalleryTarget(
    lastPage: ScannedPage?,
    count: Int,
    scale: Float,
    onClick: () -> Unit
) {
    val accents = LocalDocAccents.current
    val interactions = remember { MutableInteractionSource() }
    BadgedBox(
        badge = {
            if (count > 0) {
                Badge(
                    containerColor = accents.scan,
                    contentColor = accents.onScan
                ) {
                    Text("$count")
                }
            }
        },
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Surface(
            shape = DocShapes.thumbnail,
            color = Color.Black.copy(alpha = 0.42f),
            modifier = Modifier
                .size(54.dp)
                .pressScale(interactions)
                .clip(DocShapes.thumbnail)
                .clickable(
                    interactionSource = interactions,
                    indication = ripple(),
                    onClickLabel = "Choose from gallery",
                    onClick = onClick
                )
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (lastPage != null) {
                    PageThumbnail(
                        page = lastPage,
                        reqWidth = 180,
                        reqHeight = 220,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Outlined.PhotoLibrary,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Finish the capture set and move on to the border pass.
 *
 * A filled accent circle the same size as the gallery tile, so the bottom row is
 * symmetrical and the shutter stays dead centre. The count it used to carry now
 * lives in two clearer places: the badge on the tile and the counter under the
 * chrome.
 */
@Composable
private fun FinishButton(onClick: () -> Unit) {
    val accents = LocalDocAccents.current
    val interactions = remember { MutableInteractionSource() }
    Surface(
        shape = CircleShape,
        color = accents.scan,
        contentColor = accents.onScan,
        shadowElevation = 6.dp,
        modifier = Modifier
            .size(54.dp)
            .pressScale(interactions, pressedScale = 0.92f)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactions,
                indication = ripple(),
                onClickLabel = "Finish scan",
                onClick = onClick
            )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Finish scan",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * What the screen shows instead of a black rectangle when there is no camera to
 * look through: why it needs one, how to grant it, and a way to keep working
 * without it.
 */
@Composable
private fun CameraPermissionCard(
    onRequestPermission: () -> Unit,
    onImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accents = LocalDocAccents.current
    Column(
        modifier = modifier.padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = accents.scan.copy(alpha = 0.16f),
            modifier = Modifier.size(84.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = accents.scan,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        Text(
            "Camera access needed",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Doc Scanner uses the camera only to photograph pages. You can also " +
                "import photos you have already taken.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.72f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        PillButton(
            text = "Allow camera",
            icon = Icons.Default.PhotoCamera,
            container = accents.scan,
            content = accents.onScan,
            onClick = onRequestPermission
        )
        Spacer(Modifier.height(12.dp))
        PillButton(
            text = "Import photos",
            icon = Icons.Outlined.PhotoLibrary,
            container = Color.White.copy(alpha = 0.14f),
            content = Color.White,
            onClick = onImport
        )
    }
}

/** A camera that will not open is a dead end unless the user can still get in. */
@Composable
private fun CameraErrorCard(
    message: String,
    onRetry: () -> Unit,
    onImport: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.10f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Camera unavailable",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.66f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(22.dp))
        PillButton(
            text = "Try again",
            icon = Icons.Default.Refresh,
            onClick = onRetry
        )
        Spacer(Modifier.height(12.dp))
        PillButton(
            text = "Import photos instead",
            icon = Icons.Outlined.PhotoLibrary,
            container = Color.White.copy(alpha = 0.14f),
            content = Color.White,
            onClick = onImport
        )
        Spacer(Modifier.height(6.dp))
        TextButton(onClick = onClose) {
            Text("Close", color = Color.White.copy(alpha = 0.8f))
        }
    }
}

// ---- constants ------------------------------------------------------------

/** How long the first-run hint stays before it retires for good. */
private const val HINT_MS = 2800L

/** How long a transient status message is shown before it dismisses itself. */
private const val STATUS_MS = 4200L

/** Peak white of the shutter flash, and how long it takes to clear. */
private const val FLASH_PEAK_ALPHA = 0.80f
private const val FLASH_MS = 260

/** Zoom ratios closer than this to a stop count as that stop being selected. */
private const val ZOOM_SELECTED_EPSILON = 0.06f

/**
 * Minimum gap between camera zoom calls while a pinch is in flight.
 *
 * 32 ms is ~31 Hz: above the point where a stepped zoom is visible, well below the
 * point where the camera service starts queueing requests faster than it can serve
 * them.
 */
private const val ZOOM_APPLY_INTERVAL_MS = 32L

/**
 * The zoom stops worth offering: whole stops the sensor can reach, plus the
 * sensor's own limit so nothing is unreachable.
 */
private fun zoomStops(maxZoom: Float): List<Float> {
    if (maxZoom <= 1.15f) return emptyList()
    val stops = ArrayList<Float>(4)
    stops.add(1f)
    listOf(2f, 5f, 10f).forEach { candidate ->
        if (candidate < maxZoom - ZOOM_SELECTED_EPSILON) stops.add(candidate)
    }
    if (maxZoom > stops.last() + ZOOM_SELECTED_EPSILON) stops.add(maxZoom)
    return stops
}

/** "2×" for whole stops, "3.7×" for a sensor limit that is not round. */
private fun zoomLabel(ratio: Float): String {
    val rounded = kotlin.math.round(ratio)
    return if (kotlin.math.abs(ratio - rounded) < ZOOM_SELECTED_EPSILON) {
        String.format(Locale.US, "%.0f\u00d7", ratio)
    } else {
        String.format(Locale.US, "%.1f\u00d7", ratio)
    }
}
