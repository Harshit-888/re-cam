package com.example.docscanner.ui.camera

import android.Manifest
import android.media.MediaActionSound
import android.content.pm.PackageManager
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import com.example.docscanner.theme.DocShapes
import com.example.docscanner.theme.LocalDocAccents
import com.example.docscanner.theme.pressScale
import com.example.docscanner.ui.common.PageThumbnail
import com.example.docscanner.ui.common.PillButton
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BurstMode
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Filter1
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
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
    var statusMessage by remember { mutableStateOf<String?>(null) }

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
    DisposableEffect(hasCameraPermission) {
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
                        statusMessage = "Camera unavailable: ${t.message ?: "unknown error"}"
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
            kotlinx.coroutines.delay(100)
        }
    }

    // ---- apply zoom whenever it changes -----------------------------------
    LaunchedEffect(zoomRatio, boundCamera, minZoomRatio, maxZoomRatio) {
        val camera = boundCamera ?: return@LaunchedEffect
        val target = zoomRatio.coerceIn(minZoomRatio, maxOf(minZoomRatio, maxZoomRatio))
        runCatching { camera.cameraControl.setZoomRatio(target) }
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

    // ---- shutter ----------------------------------------------------------
    fun capturePage() {
        val capture = imageCapture ?: run {
            statusMessage = "Camera is still starting up"
            return
        }
        if (isCapturing) return
        isCapturing = true
        statusMessage = null

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
        }

        // No scrim and no guide frame: what the user frames is what gets
        // captured, so there is nothing to line up against.
        //
        // The old overlay drew a dark window with a pulsing dashed border. It was
        // decorative only (never part of the captured JPEG), and it actively hurt
        // document detection: it taught users to fill the frame right past the
        // paper edge, so there was no page boundary left in the photo for
        // DocumentDetector to find.

        // ---- chrome -----------------------------------------------------------
        // Everything over the preview is "glass": translucent, high-contrast in
        // both themes, and never white-on-white against a bright page.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassButton(
                icon = Icons.Default.Close,
                contentDescription = "Close camera",
                onClick = onClose
            )

            Spacer(Modifier.weight(1f))

            GlassPill(
                onClick = { isBatchMode = !isBatchMode },
                highlight = isBatchMode
            ) {
                Icon(
                    if (isBatchMode) Icons.Default.BurstMode else Icons.Default.Filter1,
                    contentDescription = null,
                    tint = if (isBatchMode) accents.scan else Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isBatchMode) "Batch" else "Single",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            GlassButton(
                icon = when (flashMode) {
                    FlashMode.ON -> Icons.Default.FlashOn
                    FlashMode.OFF -> Icons.Default.FlashOff
                    FlashMode.AUTO -> Icons.Default.FlashAuto
                },
                contentDescription = "Flash mode",
                tint = if (flashMode == FlashMode.ON) accents.scan else Color.White,
                onClick = {
                    flashMode = when (flashMode) {
                        FlashMode.AUTO -> FlashMode.ON
                        FlashMode.ON -> FlashMode.OFF
                        FlashMode.OFF -> FlashMode.AUTO
                    }
                }
            )
        }

        // Live count, right under the chrome — the one number the user needs while
        // shooting a stack of pages.
        AnimatedVisibility(
            visible = capturedPages.isNotEmpty(),
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 64.dp)
        ) {
            Surface(
                shape = DocShapes.pill,
                color = accents.scan,
                contentColor = accents.onScan,
                shadowElevation = 8.dp
            ) {
                Text(
                    text = "${capturedPages.size} ${if (capturedPages.size == 1) "page" else "pages"}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        if (!hasCameraPermission || statusMessage != null) {
            Surface(
                shape = DocShapes.cardSmall,
                color = Color.Black.copy(alpha = 0.78f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = statusMessage
                            ?: "Camera permission is required to scan documents.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!hasCameraPermission) {
                        Spacer(Modifier.height(14.dp))
                        PillButton(
                            text = "Grant permission",
                            container = accents.scan,
                            content = accents.onScan,
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                        )
                    }
                }
            }
        }

        // ---- bottom controls --------------------------------------------------
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Zoom: pinch on the preview, or use these for discoverability and
            // precision. Only shown when the device actually supports zoom.
            AnimatedVisibility(visible = maxZoomRatio > 1.01f) {
                Surface(
                    shape = DocShapes.pill,
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        ZoomIcon(
                            icon = Icons.Default.Remove,
                            description = "Zoom out",
                            enabled = zoomRatio > 1.001f,
                            onClick = { zoomRatio = (zoomRatio - ZOOM_STEP).coerceAtLeast(1f) }
                        )
                        Surface(
                            shape = DocShapes.pill,
                            color = if (zoomRatio > 1.001f) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.White.copy(alpha = 0.12f)
                            },
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .clickable { zoomRatio = 1f }
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f×", zoomRatio),
                                color = if (zoomRatio > 1.001f) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    Color.White
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                        ZoomIcon(
                            icon = Icons.Default.Add,
                            description = "Zoom in",
                            enabled = zoomRatio < maxZoomRatio - 0.001f,
                            onClick = {
                                zoomRatio = (zoomRatio + ZOOM_STEP).coerceAtMost(maxZoomRatio)
                            }
                        )
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
                // Gallery: the last capture when there is one, otherwise the import
                // affordance. Tapping either opens the picker.
                GalleryTarget(
                    lastPage = capturedPages.lastOrNull(),
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

                if (capturedPages.isNotEmpty()) {
                    DoneButton(count = capturedPages.size, onClick = { commitScan() })
                } else {
                    Spacer(Modifier.size(64.dp))
                }
            }
        }
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
            .size(44.dp)
            .pressScale(interactions)
            .clip(CircleShape)
            .clickable(interactionSource = interactions, indication = ripple(), onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun GlassPill(
    onClick: () -> Unit,
    highlight: Boolean,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val interactions = remember { MutableInteractionSource() }
    Surface(
        shape = DocShapes.pill,
        color = if (highlight) Color.Black.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.38f),
        modifier = Modifier
            .height(40.dp)
            .pressScale(interactions, pressedScale = 0.94f)
            .clip(DocShapes.pill)
            .clickable(interactionSource = interactions, indication = ripple(), onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp),
            content = content
        )
    }
}

@Composable
private fun ZoomIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(DocShapes.pill)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Icon(
            icon,
            contentDescription = description,
            tint = Color.White.copy(alpha = if (enabled) 1f else 0.35f),
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * The capture button.
 *
 * Two rings with the accent colour between them: the outer ring is the target,
 * the inner disc is the press. It compresses under the finger with a spring, which
 * is the cheapest way to make a shutter feel mechanical rather than like a button.
 */
@Composable
private fun ShutterButton(
    isCapturing: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val accents = LocalDocAccents.current
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val innerScale by animateFloatAsState(
        targetValue = when {
            isCapturing -> 0.72f
            pressed -> 0.86f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 700f),
        label = "shutterInner"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(84.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactions,
                indication = null,
                enabled = enabled && !isCapturing,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ring = 3.5.dp.toPx()
            drawCircle(color = Color.White.copy(alpha = 0.92f), radius = size.minDimension / 2f - ring / 2f, style = Stroke(ring))
            drawCircle(
                color = accents.scan,
                radius = size.minDimension / 2f - ring * 2.6f,
                style = Stroke(ring * 1.6f)
            )
        }
        Box(
            modifier = Modifier
                .size(60.dp)
                .graphicsLayer {
                    scaleX = innerScale
                    scaleY = innerScale
                }
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (isCapturing) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = Color.Black,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/** Left-hand target: the last page captured, or the photo library. */
@Composable
private fun GalleryTarget(lastPage: ScannedPage?, onClick: () -> Unit) {
    val interactions = remember { MutableInteractionSource() }
    Surface(
        shape = DocShapes.thumbnail,
        color = Color.Black.copy(alpha = 0.42f),
        modifier = Modifier
            .size(52.dp)
            .pressScale(interactions)
            .clip(DocShapes.thumbnail)
            .clickable(interactionSource = interactions, indication = ripple(), onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (lastPage != null) {
                PageThumbnail(
                    page = lastPage,
                    reqWidth = 160,
                    reqHeight = 200,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Outlined.PhotoLibrary,
                    contentDescription = "Import from gallery",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/** Finish the capture set and move on to the border pass. */
@Composable
private fun DoneButton(count: Int, onClick: () -> Unit) {
    val accents = LocalDocAccents.current
    val interactions = remember { MutableInteractionSource() }
    Surface(
        shape = DocShapes.pill,
        color = accents.scan,
        contentColor = accents.onScan,
        shadowElevation = 8.dp,
        modifier = Modifier
            .height(48.dp)
            .pressScale(interactions, pressedScale = 0.94f)
            .clip(DocShapes.pill)
            .clickable(interactionSource = interactions, indication = ripple(), onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Done ($count)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Zoom increment for the +/- buttons, in camera zoom-ratio units. */
private const val ZOOM_STEP = 0.5f
