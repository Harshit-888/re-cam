package com.example.docscanner.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.theme.Motion
import com.example.docscanner.util.PageImageLoader

/**
 * Decodes a page's `imageUri` asynchronously and exposes it as an [ImageBitmap].
 *
 * Returns `null` while decoding, and also when the file is missing or the path is
 * an un-imported `content://` URI — callers should render a placeholder in that
 * case rather than a blank box.
 */
@Composable
fun rememberPageImage(
    imageUri: String?,
    reqWidth: Int = 600,
    reqHeight: Int = 800
): ImageBitmap? {
    val context = LocalContext.current.applicationContext
    val image by produceState<ImageBitmap?>(initialValue = null, imageUri, reqWidth, reqHeight) {
        value = PageImageLoader.load(context, imageUri, reqWidth, reqHeight)?.asImageBitmap()
    }
    return image
}

/**
 * Standard page preview: the real scanned pixels, with an icon fallback when the
 * bitmap cannot be decoded. Use this everywhere a page is shown so no screen
 * invents its own loader.
 */
@Composable
fun PageThumbnail(
    page: ScannedPage,
    modifier: Modifier = Modifier,
    reqWidth: Int = 600,
    reqHeight: Int = 800,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackIconSize: Dp = 40.dp
) {
    val image = rememberPageImage(page.imageUri, reqWidth, reqHeight)

    // A decode lands a few frames after the card does, so without this the tile
    // pops from a grey placeholder to a photo in a single frame - the most
    // unfinished-looking moment in a scrolling list. Fading the pixels in over
    // 180 ms makes the same latency read as a deliberate reveal.
    val reveal by animateFloatAsState(
        targetValue = if (image != null) 1f else 0f,
        animationSpec = Motion.fade,
        label = "pageReveal"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = "Page ${page.pageIndex + 1}",
                contentScale = contentScale,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = reveal }
            )
        } else {
            Icon(
                imageVector = Icons.Default.Article,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                modifier = Modifier.size(fallbackIconSize)
            )
        }
    }
}
