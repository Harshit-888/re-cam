package com.example.docscanner.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BurstMode
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Filter1
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.util.DocumentNaming
import com.example.docscanner.util.ImageStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
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

    val capturedPages = remember { mutableStateListOf<ScannedPage>() }
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
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }

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
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
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

    // ---- gallery import ---------------------------------------------------
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            uris.forEach { uri ->
                val pageId = UUID.randomUUID().toString()
                val savedPath = withContext(Dispatchers.IO) {
                    ImageStore.importFromUri(context, uri, pageId)
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

        val pageId = UUID.randomUUID().toString()
        val tempFile = File(context.cacheDir, "capture_$pageId.jpg")
        val options = ImageCapture.OutputFileOptions.Builder(tempFile).build()

        capture.takePicture(
            options,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    scope.launch {
                        val savedPath = withContext(Dispatchers.IO) {
                            ImageStore.importFromFile(context, tempFile, pageId)
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
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        }

        // No scrim / guide frame: the preview is full-bleed.
        //
        // The old overlay drew a dark window with a pulsing dashed border. It was
        // decorative only (never part of the captured JPEG), and it actively hurt
        // document detection: it taught users to fill the frame right past the
        // paper edge, so there was no page boundary left in the photo for
        // DocumentDetector to find.

        // Top bar.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.6f)
                )
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.clickable { isBatchMode = !isBatchMode }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isBatchMode) Icons.Default.BurstMode else Icons.Default.Filter1,
                        contentDescription = null,
                        tint = Color(0xFF4FD8EB),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isBatchMode) "Batch Mode" else "Single Mode",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = {
                    flashMode = when (flashMode) {
                        FlashMode.AUTO -> FlashMode.ON
                        FlashMode.ON -> FlashMode.OFF
                        FlashMode.OFF -> FlashMode.AUTO
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.6f)
                )
            ) {
                val (icon, tint) = when (flashMode) {
                    FlashMode.ON -> Icons.Default.FlashOn to Color(0xFFFFD700)
                    FlashMode.OFF -> Icons.Default.FlashOff to Color.White
                    FlashMode.AUTO -> Icons.Default.FlashAuto to Color.White
                }
                Icon(icon, contentDescription = "Flash mode", tint = tint)
            }
        }

        // Page counter.
        AnimatedVisibility(
            visible = capturedPages.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 6.dp
            ) {
                Text(
                    text = "${capturedPages.size} ${if (capturedPages.size == 1) "page" else "pages"} captured",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Permission / error messaging.
        if (!hasCameraPermission || statusMessage != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.75f),
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
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Grant permission")
                        }
                    }
                }
            }
        }

        // Bottom controls.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        Icons.Outlined.PhotoLibrary,
                        contentDescription = "Import from gallery",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(if (isCapturing) Color(0xFF4FD8EB) else Color.White)
                        .clickable { capturePage() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.Camera,
                            contentDescription = "Capture page",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                if (capturedPages.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = { commitScan() },
                        shape = RoundedCornerShape(20.dp),
                        colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            "Done (${capturedPages.size})",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(54.dp))
                }
            }
        }
    }
}
