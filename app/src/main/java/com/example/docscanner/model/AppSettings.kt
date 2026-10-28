package com.example.docscanner.model

import kotlinx.serialization.Serializable

/**
 * User-visible preferences, persisted in `settings.json` by
 * `data/SettingsStore.kt` and exposed through
 * [com.example.docscanner.data.ScannerRepository.settings].
 *
 * Every field here is honoured somewhere in the app — nothing is stored "for
 * later", because a settings row that writes a value nothing reads is a lie to
 * the user. Where a feature does not exist yet (OCR, cloud sync) there is no
 * field here at all.
 */
@Serializable
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentColor: AccentColor = AccentColor.BLUE,
    // ---- library
    /**
     * Whether the library shows the document grid or the list.
     *
     * This lives here rather than in the dashboard's `rememberSaveable` state because it
     * is a **preference**, not a transient view state: "keep whatever the user leaves"
     * means it has to survive the process, not just a rotation. `rememberSaveable`
     * restores it after a configuration change and after the system reclaims the
     * activity, but a fresh launch — the normal way an app is opened — starts from the
     * default again, so the user's choice silently reverted to grid every time they
     * closed the app.
     *
     * Default `true` keeps the library opening on the grid, which is what a new install
     * has always seen.
     */
    val libraryGridView: Boolean = true,
    // ---- scan
    val useInAppCamera: Boolean = true,
    val shutterSound: Boolean = false,
    val imageQuality: ImageQuality = ImageQuality.HIGH,
    val autoSaveToGallery: Boolean = false,
    // ---- pdf
    val defaultDocumentName: String = DEFAULT_DOCUMENT_NAME,
    val pageSize: PaperSize = PaperSize.A4,
    val resolution: ExportResolution = ExportResolution.HIGH,
    val pageOrientation: PageOrientation = PageOrientation.AUTO,
    val imageScale: ImageScale = ImageScale.FIT,
    // ---- default email
    val emailTo: String = "",
    val emailSubject: String = ""
) {
    companion object {
        const val DEFAULT_DOCUMENT_NAME = "Document"

        /** Longest accepted value for the text fields, keeps the PDF name sane. */
        const val MAX_TEXT_LENGTH = 80
    }
}

@Serializable
enum class ThemeMode(val label: String) {
    SYSTEM("System default"),
    LIGHT("Light"),
    DARK("Dark")
}

/**
 * Accent palettes.
 *
 * Each entry carries explicit primary/container pairs for both light and dark
 * instead of being generated from a seed: generating a Material scheme from a
 * seed needs `material-color-utilities`, and a hand-waved auto-generated pair is
 * how you end up with white text on a pale button.
 */
@Serializable
enum class AccentColor(
    val label: String,
    val lightPrimary: Long,
    val lightOnPrimary: Long,
    val lightPrimaryContainer: Long,
    val lightOnPrimaryContainer: Long,
    val darkPrimary: Long,
    val darkOnPrimary: Long,
    val darkPrimaryContainer: Long,
    val darkOnPrimaryContainer: Long
) {
    BLUE(
        label = "Blue",
        lightPrimary = 0xFF0061A4, lightOnPrimary = 0xFFFFFFFF,
        lightPrimaryContainer = 0xFFD1E4FF, lightOnPrimaryContainer = 0xFF001D36,
        darkPrimary = 0xFF9ECAFF, darkOnPrimary = 0xFF003258,
        darkPrimaryContainer = 0xFF00497D, darkOnPrimaryContainer = 0xFFD1E4FF
    ),
    PURPLE(
        label = "Purple",
        lightPrimary = 0xFF6750A4, lightOnPrimary = 0xFFFFFFFF,
        lightPrimaryContainer = 0xFFEADDFF, lightOnPrimaryContainer = 0xFF21005D,
        darkPrimary = 0xFFD0BCFF, darkOnPrimary = 0xFF381E72,
        darkPrimaryContainer = 0xFF4F378B, darkOnPrimaryContainer = 0xFFEADDFF
    ),
    TEAL(
        label = "Teal",
        lightPrimary = 0xFF00696E, lightOnPrimary = 0xFFFFFFFF,
        lightPrimaryContainer = 0xFF6FF6FE, lightOnPrimaryContainer = 0xFF002022,
        darkPrimary = 0xFF4CD9E2, darkOnPrimary = 0xFF00363A,
        darkPrimaryContainer = 0xFF004F53, darkOnPrimaryContainer = 0xFF6FF6FE
    ),
    GREEN(
        label = "Green",
        lightPrimary = 0xFF2E6A32, lightOnPrimary = 0xFFFFFFFF,
        lightPrimaryContainer = 0xFFB0F1AC, lightOnPrimaryContainer = 0xFF002204,
        darkPrimary = 0xFF95D592, darkOnPrimary = 0xFF00390A,
        darkPrimaryContainer = 0xFF13511A, darkOnPrimaryContainer = 0xFFB0F1AC
    ),
    AMBER(
        label = "Amber",
        lightPrimary = 0xFF7D5700, lightOnPrimary = 0xFFFFFFFF,
        lightPrimaryContainer = 0xFFFFDEA1, lightOnPrimaryContainer = 0xFF271900,
        darkPrimary = 0xFFF8BE48, darkOnPrimary = 0xFF422C00,
        darkPrimaryContainer = 0xFF5F4100, darkOnPrimaryContainer = 0xFFFFDEA1
    ),
    ROSE(
        label = "Rose",
        lightPrimary = 0xFF984061, lightOnPrimary = 0xFFFFFFFF,
        lightPrimaryContainer = 0xFFFFD9E2, lightOnPrimaryContainer = 0xFF3E001D,
        darkPrimary = 0xFFFFB0C8, darkOnPrimary = 0xFF5E1133,
        darkPrimaryContainer = 0xFF7B2949, darkOnPrimaryContainer = 0xFFFFD9E2
    )
}

/**
 * Capture quality: how far the app downsamples a capture and how hard it
 * compresses the JPEG it writes.
 *
 * [HIGH] matches [com.example.docscanner.util.ImageStore.MAX_CAPTURE_DIMENSION],
 * so the default path keeps every pixel CameraX gave us.
 */
@Serializable
enum class ImageQuality(
    val label: String,
    val maxDimension: Int,
    val jpegQuality: Int
) {
    HIGH("High", 3200, 95),
    MEDIUM("Medium", 2000, 88),
    LOW("Low", 1400, 80)
}

/**
 * PDF raster resolution: the longest edge of the bitmap embedded in the PDF.
 *
 * The numbers are derived, not guessed. On A4 with the standard 25 pt margin the
 * printable image box is 770.8 pt = 10.7 in tall, so 300 DPI needs
 * `10.7 × 300 ≈ 3212` px — [HIGH] is 3200. The previous values (2400 for "300
 * DPI") could never have been reached either, because the baked page itself was
 * capped at 2000 px; the real figure was **187 DPI**.
 *
 * A page cannot be exported sharper than it was baked, so these are ceilings, not
 * promises: [HIGH] is only honest while `ImageQuality.HIGH` and
 * `DocumentFilters.MAX_PROCESS_DIMENSION` stay at 3200 as well.
 */
@Serializable
enum class ExportResolution(val label: String, val maxImageDimensionPx: Int) {
    HIGH("300 DPI", 3200),
    MEDIUM("200 DPI", 2140),
    LOW("150 DPI", 1600)
}

/**
 * Paper orientation. [AUTO] swaps width and height per page when the scanned
 * image is landscape, so a wide receipt is not squashed onto a portrait sheet.
 */
@Serializable
enum class PageOrientation(val label: String) {
    AUTO("Auto Orientation"),
    PORTRAIT("Portrait"),
    LANDSCAPE("Landscape")
}

/** How the page bitmap is placed inside the printable area. */
@Serializable
enum class ImageScale(val label: String) {
    /** Whole page visible, aspect ratio kept, empty bands on one axis. */
    FIT("Fit width or height"),

    /** Fills the printable area, cropping the overflow — no white edges. */
    FILL("Fill page"),

    /** Drawn at 1 image pixel per PDF point, centred and clipped. */
    ORIGINAL("Original size")
}
