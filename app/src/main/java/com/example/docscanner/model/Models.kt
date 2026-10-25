package com.example.docscanner.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
enum class FilterType(val label: String) {
    ORIGINAL("Original"),
    MAGIC_COLOR("Magic Color"),
    CLEAR_BW("Clear B&W"),
    GRAYSCALE("Grayscale"),
    LIGHTEN("Lighten")
}

/**
 * Paper presets in PostScript points (1 pt = 1/72 in), which is the unit
 * [android.graphics.pdf.PdfDocument.PageInfo] expects.
 *
 * `BUSINESS_CARD` is 85.6 x 54 mm (ISO/IEC 7810 ID-1, the credit-card / ID-card
 * size) rounded to whole points.
 */
@Serializable
enum class PaperSize(val label: String, val widthPt: Int, val heightPt: Int) {
    A4("A4 (210 × 297 mm)", 595, 842),
    LETTER("US Letter (8.5 × 11 in)", 612, 792),
    LEGAL("US Legal (8.5 × 14 in)", 612, 1008),
    A3("A3 (297 × 420 mm)", 842, 1191),
    A5("A5 (148 × 210 mm)", 420, 595),
    EXECUTIVE("Executive (7.25 × 10.5 in)", 522, 756),
    BUSINESS_CARD("Business Card (85.6 × 54 mm)", 243, 153),
    RECEIPT("Receipt (80 mm roll)", 226, 600)
}

@Serializable
enum class PdfMargin(val label: String, val marginPt: Int) {
    NONE("No Margin", 0),
    COMPACT("Compact (10 pt)", 10),
    STANDARD("Standard (25 pt)", 25),
    WIDE("Wide (40 pt)", 40)
}

/**
 * Four document corners in **normalised page space**, where (0,0) is the
 * top-left of the full image and (1,1) is the bottom-right.
 *
 * The default is the full frame. An inset default would silently crop every
 * captured page, which is what the previous revision did.
 */
@Immutable
@Serializable
data class QuadCorners(
    val topLeftX: Float = 0f,
    val topLeftY: Float = 0f,
    val topRightX: Float = 1f,
    val topRightY: Float = 0f,
    val bottomRightX: Float = 1f,
    val bottomRightY: Float = 1f,
    val bottomLeftX: Float = 0f,
    val bottomLeftY: Float = 1f
) {
    /** True when the quad covers the whole frame, so no warp is needed. */
    val isFullFrame: Boolean
        get() = topLeftX <= 0.001f && topLeftY <= 0.001f &&
            topRightX >= 0.999f && topRightY <= 0.001f &&
            bottomRightX >= 0.999f && bottomRightY >= 0.999f &&
            bottomLeftX <= 0.001f && bottomLeftY >= 0.999f

    companion object {
        val FULL_FRAME = QuadCorners()

        fun inset(fraction: Float) = QuadCorners(
            topLeftX = fraction, topLeftY = fraction,
            topRightX = 1f - fraction, topRightY = fraction,
            bottomRightX = 1f - fraction, bottomRightY = 1f - fraction,
            bottomLeftX = fraction, bottomLeftY = 1f - fraction
        )
    }
}

/**
 * The library's data classes are annotated [@Immutable] on purpose.
 *
 * Every field is a `val` and the repository always replaces a document wholesale
 * (`doc.copy(pages = doc.pages + page)`) instead of mutating a list, so the
 * promise is true. Without the annotation Compose infers these types as
 * *unstable* — `List<ScannedPage>` is an interface, and the compiler cannot know
 * the instance never changes — which makes every card in the library unskippable:
 * one keystroke in the search field recomposed every visible thumbnail, and a
 * fling re-ran the date formatting for a dozen cards per frame. With it, a card
 * only recomposes when its own document or snippet actually changed.
 */
@Immutable
@Serializable
data class ScannedPage(
    val id: String,
    val documentId: String,
    val pageIndex: Int,
    /**
     * Absolute filesystem path of the JPEG the UI should display. After the
     * user edits the page this points at the `_edited.jpg` bake.
     */
    val imageUri: String,
    /**
     * Pristine capture, kept so repeated edits never compound JPEG loss.
     * `null` means the page has never been edited; [imageUri] is authoritative.
     */
    val originalImageUri: String? = null,
    val filterType: FilterType = FilterType.MAGIC_COLOR,
    val rotationDegrees: Float = 0f,
    val corners: QuadCorners = QuadCorners(),
    val ocrText: String = "",
    val watermarkText: String? = null,
    val signatureOverlayUri: String? = null
)

@Immutable
@Serializable
data class Document(
    val id: String,
    val title: String,
    val createdDate: Long,
    val modifiedDate: Long,
    val folderId: String? = null,
    val isTrash: Boolean = false,
    val pages: List<ScannedPage> = emptyList(),
    val tags: List<String> = emptyList(),
    val notes: String = ""
)

@Immutable
@Serializable
data class Folder(
    val id: String,
    val name: String,
    val colorHex: Long = 0xFF6750A4,
    val createdDate: Long = System.currentTimeMillis()
)

@Serializable
enum class DocumentSortOption(val label: String) {
    MODIFIED_NEWEST("Recently Modified"),
    MODIFIED_OLDEST("Oldest Modified"),
    CREATED_NEWEST("Recently Created"),
    TITLE_AZ("Name (A to Z)"),
    TITLE_ZA("Name (Z to A)")
}

/**
 * How the page images are encoded into the PDF.
 *
 * [ORIGINAL] is lossless: the page bitmap is Flate-compressed exactly as
 * `android.graphics.pdf.PdfDocument` writes it, so a 300 DPI photo page is
 * several megabytes. The other levels are JPEG at a known quality and pixel
 * scale, which is what actually makes a scan small enough to attach to an email.
 *
 * They are named for what they are for, not with an invented "quality %":
 * "78 %" tells nobody anything, and the number would be a lie the moment the
 * encoder changed.
 */
enum class PdfCompression(
    val label: String,
    val detail: String,
    /** JPEG quality when the level is lossy; ignored for [ORIGINAL]. */
    val jpegQuality: Int,
    /** Fraction of the export resolution the page is sampled at. */
    val pixelScale: Float
) {
    ORIGINAL("Original", "Lossless, largest file", 0, 1f),
    BALANCED("Balanced", "Looks the same, far smaller", 85, 1f),
    SMALL("Small", "Good for email", 70, 0.8f),
    TINY("Tiny", "Smallest, soft small text", 50, 0.6f);

    val isLossless: Boolean get() = this == ORIGINAL
}

/**
 * PDF export options.
 *
 * [password] is honoured by [com.example.docscanner.util.PdfWriter], not by
 * `PdfDocument` — the platform writer cannot encrypt, so a passphrase switches
 * the export to the app's own PDF writer. [compression] picks the encoding; the
 * default keeps the platform writer and its byte-identical output.
 */
data class PdfExportConfig(
    val paperSize: PaperSize = PaperSize.A4,
    val margins: PdfMargin = PdfMargin.STANDARD,
    val maxImageDimensionPx: Int = 3200,
    val includePageNumbers: Boolean = true,
    /** Per-page axis. [PageOrientation.AUTO] follows the scanned image's shape. */
    val pageOrientation: PageOrientation = PageOrientation.AUTO,
    /** How the page image is placed inside the printable area. */
    val imageScale: ImageScale = ImageScale.FIT,
    /** File name prefix from Settings → PDF → Document Name. Blank = none. */
    val fileBaseName: String = "",
    /**
     * Name typed in the share sheet, without the `.pdf` suffix. Blank keeps the
     * settings-derived name, so the field is an override and not a new source of
     * truth.
     */
    val fileName: String = "",
    /** Passphrase the PDF asks for when it is opened. Blank = not encrypted. */
    val password: String = "",
    val compression: PdfCompression = PdfCompression.ORIGINAL
)

/**
 * On-disk root of the persisted library. Bump [schemaVersion] on any breaking
 * model change and handle the migration in `LibraryStore`.
 */
@Serializable
data class LibrarySnapshot(
    val schemaVersion: Int = 1,
    val folders: List<Folder> = emptyList(),
    val documents: List<Document> = emptyList()
)
