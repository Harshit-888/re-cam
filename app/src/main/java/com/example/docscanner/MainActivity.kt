package com.example.docscanner

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.ui.Modifier
import com.example.docscanner.data.LibraryViewModel
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.ThemeMode
import com.example.docscanner.theme.DocScannerTheme
import com.example.docscanner.ui.common.LaunchSplash
import com.example.docscanner.util.FramePacing
import com.example.docscanner.util.PerfTrace

/** How long the launch screen takes to hand over to the library. */
private const val SPLASH_FADE_MS = 300

class MainActivity : ComponentActivity() {

    /**
     * Owns the repository, and outlives a configuration change with it.
     *
     * Still Activity-scoped rather than a process-wide singleton (`AGENTS.md` §2.6),
     * but no longer Activity-*instance*-scoped: rotating the phone used to rebuild
     * the repository from scratch — re-read `library.json`, re-decode every cover,
     * replay the launch screen — because the field lived in `onCreate`. The
     * ViewModel carries it across the rotation and flushes it when the screen
     * really finishes. See [LibraryViewModel].
     */
    private val library: LibraryViewModel by viewModels()

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
            // The repository is built on an IO thread while [LaunchSplash] is on
            // screen: `LibraryStore.load()` reads and parses `library.json`, and
            // doing that on the thread that owes the system a frame within 8 ms
            // delays every frame behind it — the parse, the first library emission
            // and the first cover decodes then all land in the same window.
            //
            // The state lives in the ViewModel, so on a rotation the library is
            // already there: no splash, no re-parse, straight back to the screen the
            // user was on.
            val ready by library.repository.collectAsState()

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
        // The repository is deliberately *not* closed here. A rotation destroys this
        // Activity instance but not the app, and the library must survive it;
        // `LibraryViewModel.onCleared` is the only place that flushes and cancels it,
        // and it runs when the screen is really finished. Closing here as well would
        // tear down the very instance the next Activity is about to reuse.
    }
}
