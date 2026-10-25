package com.example.docscanner.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.docscanner.model.ScannedPage
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
    // Seeded from the memory cache: a page that has already been decoded anywhere
    // in this process paints in the first frame instead of composing a placeholder
    // first. That is the whole difference between a library that flickers grey while
    // you scroll and one that simply moves.
    val cached = remember(imageUri, reqWidth, reqHeight) {
        PageImageLoader.peek(imageUri, reqWidth, reqHeight)?.asImageBitmap()
    }
    // Always re-resolve, even when seeded: an already-cached page answers from the
    // memory cache with no decode, and a page whose `imageUri` changed (a re-bake)
    // must not be left showing the old bitmap.
    val image by produceState(initialValue = cached, imageUri, reqWidth, reqHeight) {
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
    fallbackIconSize: Dp = 40.dp,
    /**
     * Colour shown *while there is no image to draw*, and nothing after that.
     *
     * The alternative — painting a tile-sized background behind every cover — is
     * pure overdraw: on a phone that is roughly half a megapixel of fill per tile
     * that the photo then covers completely, eight times over on a scrolling
     * screen. A settings row never pays that, and it is a real part of why a grid
     * of photos feels heavier to scroll than a list of text.
     */
    placeholder: Color = Color.Transparent
) {
    val image = rememberPageImage(page.imageUri, reqWidth, reqHeight)

    // Deliberately no fade-in.
    //
    // This used to animate alpha from 0 to 1 over 180 ms *per tile*. During a fling
    // a dozen tiles start that animation every second, and each one keeps a running
    // animation on the frame clock plus a layer write — work that lands exactly when
    // the screen has the least time to spare, on the one screen that must not
    // stutter. A photo appearing crisply on its own tile is normal; a screen full of
    // photos fading in and out is jank with a nice name.
    Box(
        modifier = if (image == null) modifier.background(placeholder) else modifier,
        contentAlignment = Alignment.Center
    ) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = "Page ${page.pageIndex + 1}",
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
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
