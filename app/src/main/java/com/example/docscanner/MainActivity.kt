package com.example.docscanner

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.ThemeMode
import com.example.docscanner.theme.DocScannerTheme
import com.example.docscanner.ui.common.LaunchSplash
import com.example.docscanner.util.FramePacing
import com.example.docscanner.util.PerfTrace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Shortest time the launch screen is shown, so a warm start does not flash. */
private const val SPLASH_MIN_MS = 320L

/** How long the launch screen takes to hand over to the library. */
private const val SPLASH_FADE_MS = 300

class MainActivity : ComponentActivity() {

    /**
     * Owned by the Activity, not a global singleton (see `AGENTS.md` §2.6).
     * `applicationContext` is used so the repository and its persistence scope
     * cannot outlive a leaked Activity reference.
     *
     * Built on a background thread — see [onCreate] — and null until then, which is
     * why it is no longer a `lazy` field.
     */
    @Volatile
    private var repository: ScannerRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Placeholder styles; the real ones are applied from the composed theme
        // below, because the user can override the system light/dark setting.
        enableEdgeToEdge()

        // Ask the display for its fastest mode *before* the first frame is laid
        // out. A high-refresh panel does not hand 120 Hz to an app that never asks
        // for it, and asking after the first frame would cost a mode switch in the
        // middle of the launch animation. See util/FramePacing.kt.
        val displayHz = FramePacing.apply(window)

        // Debug builds only: logs per-window frame times and how each cover was
        // obtained (memory / stored thumbnail / full page decode) under the
        // `DocScannerPerf` tag, plus the per-frame stage breakdown while a scroll is
        // running. A no-op in release. See util/PerfTrace.kt.
        PerfTrace.install(applicationContext)
        // The budget every frame is judged against comes from the panel that was
        // just selected — not from a 60 Hz constant.
        PerfTrace.setDisplayRefreshRate(displayHz)
        // …and every report re-asks the display what it is doing, so a request the
        // OEM compositor ignored is visible as `hz=60 fps=60` rather than hidden
        // behind the number we asked for. See PerfTrace.syncDisplayRate.
        PerfTrace.setRateProvider { FramePacing.currentRate(window).currentHz }
        // The stage breakdown needs a `Window`, which only the Activity has. Without
        // it the scroll line still reports frames and image work, just not where the
        // frame went.
        PerfTrace.attachWindow(window)

        setContent {
            // The repository used to be constructed *inside* composition, on the
            // main thread: `LibraryStore.load()` reads and parses `library.json`,
            // and until it returned there was no frame to draw, so the first thing
            // the app showed was an empty white window. Reading a few kilobytes of
            // JSON is not slow; doing it on the thread that owes the system a frame
            // within 8 ms is, because it delays *every* frame behind it — including
            // the ones the user scrolls with immediately afterwards, since the
            // parse, the first library emission and the first cover decodes all land
            // in the same window.
            //
            // Now it is built on an IO thread while [LaunchSplash] is on screen.
            // `NonCancellable` so that a rotation during the load cannot leave a
            // half-built repository with a live scope and no owner.
            var ready by remember { mutableStateOf<ScannerRepository?>(null) }
            LaunchedEffect(Unit) {
                val startedAt = android.os.SystemClock.elapsedRealtime()
                val built = withContext(NonCancellable + Dispatchers.IO) {
                    ScannerRepository(applicationContext)
                }
                repository = built
                val elapsed = android.os.SystemClock.elapsedRealtime() - startedAt
                if (elapsed < SPLASH_MIN_MS) delay(SPLASH_MIN_MS - elapsed)
                ready = built
            }

            Crossfade(
                targetState = ready,
                animationSpec = tween(durationMillis = SPLASH_FADE_MS),
                label = "launch"
            ) { repo ->
                if (repo == null) {
                    // The system bar icons follow the system theme here: the user's
                    // own override is one of the things still being read.
                    ApplySystemBarStyle(darkTheme = isSystemInDarkTheme())
                    LaunchSplash()
                } else {
                    AppContent(repository = repo)
                }
            }
        }
    }

    /** The app proper: theme from the restored settings, then navigation. */
    @Composable
    private fun AppContent(repository: ScannerRepository) {
        val settings by repository.settings.collectAsState()
        val systemDark = isSystemInDarkTheme()
        val darkTheme = when (settings.themeMode) {
            ThemeMode.SYSTEM -> systemDark
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }

        // The status/navigation bar icons have to follow the *app* theme, not the
        // system one: a light app on a dark phone would otherwise draw white status
        // icons on a white bar.
        ApplySystemBarStyle(darkTheme = darkTheme)

        DocScannerTheme(
            darkTheme = darkTheme,
            accent = settings.accentColor
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainNavigation(repository = repository)
            }
        }
    }

    @Composable
    private fun ApplySystemBarStyle(darkTheme: Boolean) {
        LaunchedEffect(darkTheme) {
            enableEdgeToEdge(
                statusBarStyle = if (darkTheme) {
                    SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                } else {
                    SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                },
                navigationBarStyle = if (darkTheme) {
                    SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                } else {
                    SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // The frame-metrics listener holds this Window; leaving it attached would
        // outlive the Activity it belongs to. The rate provider closes over the same
        // window, so it goes with it.
        PerfTrace.clearRateProvider()
        PerfTrace.detachWindow(window)
        // Configuration changes recreate the Activity but must not tear down the
        // repository mid-write; only a real finish ends its scope.
        if (isFinishing) {
            repository?.close()
        }
    }
}
