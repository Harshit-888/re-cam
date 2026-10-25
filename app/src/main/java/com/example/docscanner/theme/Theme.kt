package com.example.docscanner.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.docscanner.model.AccentColor

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    surfaceDim = SurfaceDimDark,
    surfaceBright = SurfaceBrightDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inversePrimary = InversePrimaryDark,
    scrim = ScrimDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    surfaceDim = SurfaceDimLight,
    surfaceBright = SurfaceBrightLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight,
    scrim = ScrimLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

/**
 * The two colours that are *not* part of the Material scheme but belong to this
 * product: the capture accent (amber) and the corner-handle colour used over a
 * photograph in the editor. Kept in one place so the camera and the editor agree.
 */
data class DocAccents(
    val scan: Color,
    val onScan: Color,
    val handleFill: Color,
    val handleRing: Color
)

val LocalDocAccents = staticCompositionLocalOf {
    DocAccents(
        scan = ScanAccentLight,
        onScan = OnScanAccentLight,
        handleFill = Color(0xFF0061A4),
        handleRing = Color.White
    )
}

@Composable
fun DocScannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    /**
     * Off by default: the palette in `Color.kt` is this app's identity, and dynamic
     * colour was silently overriding it on API 31+.
     */
    dynamicColor: Boolean = false,
    /** Accent chosen in Settings → Theme → Color. */
    accent: AccentColor = AccentColor.BLUE,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme.withAccent(accent, dark = true)
        else -> LightColorScheme.withAccent(accent, dark = false)
    }

    val accents = if (darkTheme) {
        DocAccents(
            scan = ScanAccentDark,
            onScan = OnScanAccentDark,
            handleFill = colorScheme.primary,
            handleRing = SurfaceContainerLowestDark
        )
    } else {
        DocAccents(
            scan = ScanAccentLight,
            onScan = OnScanAccentLight,
            handleFill = colorScheme.primary,
            handleRing = Color.White
        )
    }

    CompositionLocalProvider(LocalDocAccents provides accents) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

/**
 * Overrides only the four primary slots. Everything else (surfaces, secondary,
 * tertiary) stays on the palette in `Color.kt`, so an accent change re-tints
 * buttons, chips and highlights without re-theming the whole app — and without
 * the contrast guesswork of a fully generated seed scheme.
 */
private fun ColorScheme.withAccent(accent: AccentColor, dark: Boolean): ColorScheme = copy(
    primary = Color(if (dark) accent.darkPrimary else accent.lightPrimary),
    onPrimary = Color(if (dark) accent.darkOnPrimary else accent.lightOnPrimary),
    primaryContainer = Color(if (dark) accent.darkPrimaryContainer else accent.lightPrimaryContainer),
    onPrimaryContainer = Color(if (dark) accent.darkOnPrimaryContainer else accent.lightOnPrimaryContainer)
)
