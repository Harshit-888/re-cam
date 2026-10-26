package com.example.docscanner.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.docscanner.R

/**
 * What the app shows while its library is being read off disk.
 *
 * It is not decoration. Opening the app used to draw a light, empty window — the
 * platform's default `windowBackground` — for as long as the repository took to
 * parse `library.json`, then snap to the library. That reads as "the app is
 * broken" on a fast phone and as "the app is slow" on a slow one, and both are
 * wrong: the work is a few kilobytes of JSON. The fix is twofold, and this is the
 * visible half: the repository is now built off the main thread (see
 * `MainActivity`), and while it is being built the user sees the app's own mark
 * instead of an empty window.
 *
 * Three details are deliberate:
 *
 * * **The colours are literals, not `MaterialTheme` colours.** This composes before
 *   the user's theme is known — the theme lives in the settings the repository is
 *   still loading — so it matches the window background and the Android 12 system
 *   splash instead of guessing.
 * * **It fades in rather than appearing.** A splash that is already fully drawn
 *   when the first frame lands looks like a flash on a warm start.
 * * **Nothing here waits on anything.** It has no timer of its own; `MainActivity`
 *   removes it the moment the repository reports ready, so it can never be the
 *   reason a launch feels slow.
 */
@Composable
fun LaunchSplash() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "launchSplash"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BrandInk, BrandBlue)))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer { alpha = progress },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // The launcher icon's own margins are part of the adaptive-icon
            // contract (the launcher may mask the outer ring), so the visible mark
            // inside it is ~52 % of the drawable. Sizing it large here is what makes
            // the page-and-scan-line read at the intended weight.
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(190.dp)
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Doc Scanner",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Scan · crop · search",
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(30.dp))
            // A quiet indeterminate bar: it says "working", not "12 %".
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.18f))
            ) {
                Box(
                    modifier = Modifier
                        .width(38.dp * progress + 18.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(BrandAmber)
                )
            }
        }
    }
}

/** Window background colour, top. Kept in step with `res/values/colors.xml`. */
private val BrandInk = Color(0xFF06223A)

/** Window background colour, bottom. */
private val BrandBlue = Color(0xFF0A5AA0)

/** The scanner's own accent — the same amber as `ScanAccentLight`. */
private val BrandAmber = Color(0xFFFFB020)
