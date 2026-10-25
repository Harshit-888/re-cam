package com.example.docscanner

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.ThemeMode
import com.example.docscanner.theme.DocScannerTheme
import com.example.docscanner.util.PerfTrace

class MainActivity : ComponentActivity() {

    /**
     * Owned by the Activity, not a global singleton (see `AGENTS.md` §2.6).
     * `applicationContext` is used so the repository and its persistence scope
     * cannot outlive a leaked Activity reference.
     */
    private val repository: ScannerRepository by lazy {
        ScannerRepository(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Placeholder styles; the real ones are applied from the composed theme
        // below, because the user can override the system light/dark setting.
        enableEdgeToEdge()

        // Debug builds only: logs per-window frame times and how each cover was
        // obtained (memory / stored thumbnail / full page decode) under the
        // `DocScannerPerf` tag, plus the per-frame stage breakdown while a scroll is
        // running. A no-op in release. See util/PerfTrace.kt.
        PerfTrace.install(applicationContext)
        // The stage breakdown needs a `Window`, which only the Activity has. Without
        // it the scroll line still reports frames and image work, just not where the
        // frame went.
        PerfTrace.attachWindow(window)

        setContent {
            val settings by repository.settings.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // The status/navigation bar icons have to follow the *app* theme, not
            // the system one: a light app on a dark phone would otherwise draw
            // white status icons on a white bar.
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
        // outlive the Activity it belongs to.
        PerfTrace.detachWindow(window)
        // Configuration changes recreate the Activity but must not tear down the
        // repository mid-write; only a real finish ends its scope.
        if (isFinishing) {
            repository.close()
        }
    }
}
