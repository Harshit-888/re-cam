package com.example.docscanner.ui.camera

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.FilterType
import com.example.docscanner.model.ScannedPage
import java.util.UUID

@Composable
fun ScannerCameraScreen(
    targetDocId: String? = null,
    repository: ScannerRepository = ScannerRepository.instance,
    onFinishScan: (docId: String) -> Unit,
    onClose: () -> Unit
) {
    val capturedPages = remember { mutableStateListOf<ScannedPage>() }
    var isBatchMode by remember { mutableStateOf(true) }
    var flashMode by remember { mutableStateOf(0) } // 0: Auto, 1: On, 2: Off
    var isShutterPressed by remember { mutableStateOf(false) }

    // Multi-photo gallery picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                capturedPages.add(
                    ScannedPage(
                        id = UUID.randomUUID().toString(),
                        documentId = targetDocId ?: "",
                        pageIndex = capturedPages.size,
                        imageUri = uri.toString(),
                        filterType = FilterType.MAGIC_COLOR
                    )
                )
            }
        }
    }

    // Viewfinder pulsing corner animation
    val infiniteTransition = rememberInfiniteTransition(label = "viewfinder")
    val borderPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Document Guide Viewfinder Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val padX = size.width * 0.08f
            val padY = size.height * 0.16f
            val rectWidth = size.width - padX * 2
            val rectHeight = size.height - padY * 2

            // Dark semi-transparent scrim around document
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = size
            )

            // Transparent document target cutout
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(padX, padY),
                size = Size(rectWidth, rectHeight),
                cornerRadius = CornerRadius(24f, 24f),
                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
            )

            // Animated glowing edge border
            drawRoundRect(
                color = Color(0xFF4FD8EB).copy(alpha = borderPulse),
                topLeft = Offset(padX, padY),
                size = Size(rectWidth, rectHeight),
                cornerRadius = CornerRadius(24f, 24f),
                style = Stroke(
                    width = 4.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 20f), 0f)
                )
            )
        }

        // Top Controls Bar
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
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }

            // Batch mode pill toggle
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

            // Flash mode toggle
            IconButton(
                onClick = { flashMode = (flashMode + 1) % 3 },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.6f))
            ) {
                val (flashIcon, flashColor) = when (flashMode) {
                    1 -> Icons.Default.FlashOn to Color(0xFFFFD700)
                    2 -> Icons.Default.FlashOff to Color.White
                    else -> Icons.Default.FlashAuto to Color.White
                }
                Icon(flashIcon, contentDescription = "Flash", tint = flashColor)
            }
        }

        // Live Capture Counter Banner
        AnimatedVisibility(
            visible = capturedPages.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp)
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

        // Bottom Camera Controls
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
                // Gallery import button
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
                        contentDescription = "Import Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Shutter Button with press animation
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(if (isShutterPressed) Color(0xFF4FD8EB) else Color.White)
                        .clickable {
                            isShutterPressed = true
                            // Capture simulated page
                            val newPage = ScannedPage(
                                id = UUID.randomUUID().toString(),
                                documentId = targetDocId ?: "",
                                pageIndex = capturedPages.size,
                                imageUri = "captured_scan_${System.currentTimeMillis()}",
                                filterType = FilterType.MAGIC_COLOR
                            )
                            capturedPages.add(newPage)

                            if (!isBatchMode) {
                                // In single mode, directly save and proceed
                                val finalDocId = if (targetDocId != null) {
                                    repository.addPageToDocument(targetDocId, newPage)
                                    targetDocId
                                } else {
                                    val newDoc = repository.addDocument("Scanned Doc", null, listOf(newPage))
                                    newDoc.id
                                }
                                onFinishScan(finalDocId)
                            }
                            isShutterPressed = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Camera,
                        contentDescription = "Capture",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Done / Finish Button
                if (capturedPages.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = {
                            val finalDocId = if (targetDocId != null) {
                                capturedPages.forEach { repository.addPageToDocument(targetDocId, it) }
                                targetDocId
                            } else {
                                val newDoc = repository.addDocument("New Scan", null, capturedPages.toList())
                                newDoc.id
                            }
                            onFinishScan(finalDocId)
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
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
