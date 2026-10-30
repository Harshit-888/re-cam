package com.example.docscanner.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * The presets, in shelf order.
 *
 * The first five are the original APK's own ladder, transcribed one-to-one — see
 * [com.example.docscanner.util.DocumentFilters]. The last two are this app's, and
 * they are **appended**, never interleaved: `FilterType` is `@Serializable` and
 * stored on every [ScannedPage], so existing pages must keep resolving to the same
 * constant whatever happens at the end of the list.
 *
 * * [SMART] — the adaptive engine in colour. Same illumination divide as
 *   [MAGIC_COLOR], but the tone curve runs on the *ratio* domain, so faint ink
 *   (a pencil at 92 % of paper reflectance) darkens instead of vanishing, and a
 *   single gain per pixel keeps hues where they were instead of clipping channels
 *   independently. This is the everyday preset.
 * * [LIGHT_TEXT] — the same engine tuned for documents that are faint all over:
 *   pencil, fax, thermal receipt, faded carbon copy. Lighter knee, wider curve
 *   window, more chroma retained.
 */
@Serializable
enum class FilterType(val label: String) {
    ORIGINAL("Original"),
    MAGIC_COLOR("Magic Color"),
    CLEAR_BW("Clear B&W"),
    GRAYSCALE("Grayscale"),
    LIGHTEN("Lighten"),
    SMART("Smart Scan"),
    LIGHT_TEXT("Light Text")
}

/**
 * Grouping for the paper chooser, mirroring the original app's
 * `paper_size_group` table (`assets/databases/app.db`, groups 1, 2 and 6). The
 * original calls them "ISO A paper sizes", "ISO B paper sizes" and "American
 * paper sizes"; headers in a phone sheet want the short form.
 */
enum class PaperGroup(val label: String) {
    ISO_A("ISO A"),
    ISO_B("ISO B"),
    AMERICAN("American"),
    OTHER("Other")
}

/**
 * Paper presets in PostScript points (1 pt = 1/72 in), which is the unit
 * [android.graphics.pdf.PdfDocument.PageInfo] expects.
 *
 * The sizes are the ones the original app marks
 * `b_is_show_in_select_list = 1` in `app.db` — fifteen of its sixty-four rows —
 * with its stored point values verbatim, in its stored order. Two deliberate
 * departures, both recorded in `docs/ORIGINAL_APP_FULL_SPECIFICATION.md` §9.2:
 *
 * * `BUSINESS_CARD` is 85.6 x 54 mm (ISO/IEC 7810 ID-1, the credit-card /
 *   ID-card size) rounded to whole points. The original stores 353 x 206 pt for
 *   it, which is 124.5 x 72.7 mm — not a business card, and not what its own
 *   label would promise.
 * * `RECEIPT` is ours; the original has no receipt preset.
 *
 * Adding an entry is safe for saved settings — kotlinx.serialization stores the
 * enum *name*, not its ordinal — but **renaming one is not**: it silently resets
 * that setting. `PaperSizeTest` pins the names.
 */
@Serializable
enum class PaperSize(
    val label: String,
    val widthPt: Int,
    val heightPt: Int,
    val group: PaperGroup
) {
    // ISO A
    A3("A3 (297 × 420 mm)", 842, 1190, PaperGroup.ISO_A),
    A4("A4 (210 × 297 mm)", 595, 842, PaperGroup.ISO_A),
    A5("A5 (148 × 210 mm)", 420, 595, PaperGroup.ISO_A),

    // ISO B
    B4("B4 (250 × 353 mm)", 709, 1001, PaperGroup.ISO_B),
    B5("B5 (176 × 250 mm)", 499, 709, PaperGroup.ISO_B),

    // American — the original's order and values, including the landscape
    // Tabloid and the ANSI engineering sizes.
    LETTER("US Letter (8.5 × 11 in)", 612, 792, PaperGroup.AMERICAN),
    LEGAL("US Legal (8.5 × 14 in)", 612, 1008, PaperGroup.AMERICAN),
    LEDGER("Ledger (11 × 17 in)", 792, 1224, PaperGroup.AMERICAN),
    TABLOID("Tabloid (17 × 11 in)", 1224, 792, PaperGroup.AMERICAN),
    EXECUTIVE("Executive (7.25 × 10.5 in)", 522, 756, PaperGroup.AMERICAN),
    ANSI_C("ANSI C (22 × 17 in)", 1584, 1224, PaperGroup.AMERICAN),
    ANSI_D("ANSI D (34 × 22 in)", 2448, 1584, PaperGroup.AMERICAN),
    ANSI_E("ANSI E (44 × 34 in)", 3168, 2448, PaperGroup.AMERICAN),
    BUSINESS_CARD("Business Card (85.6 × 54 mm)", 243, 153, PaperGroup.AMERICAN),
    FOLIO("Folio (8.5 × 13 in)", 612, 936, PaperGroup.AMERICAN),

    // Ours
    RECEIPT("Receipt (80 mm roll)", 226, 600, PaperGroup.OTHER);

    companion object {
        /** Declaration order, grouped — what every chooser should render. */
        val byGroup: List<Pair<PaperGroup, List<PaperSize>>>
            get() = entries.groupBy { it.group }.toList()
    }
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
    /**
     * True once text recognition has been attempted for the current
     * image, regardless of the result. This is what separates "never
     * tried" from "tried and blank / failed" — `ocrText` alone cannot,
     * because a blank page and a failed recognition both leave it
     * blank. Round 70.1 (Arena H004): without this, the backfill
     * queue re-selected the same blank/failing front pages on every
     * launch and starved later pages, and Round 70's batch cut from
     * 24 to 8 made that starvation worse. Pages loaded from an older
     * library.json get the default `false`; pages with non-blank
     * `ocrText` are still skipped by the backfill filter, so no
     * already-recognised page is re-attempted on upgrade. Reset to
     * `false` on bake (see `PageRenderer.bakeBlocking`), so a page
     * whose pixels changed gets another chance.
     */
    val ocrAttempted: Boolean = false,
    val watermarkText: String? = null,
    val signatureOverlayUri: String? = null,
    /**
     * Hand annotations, in page order, drawn **on top of** the baked page. Kept as
     * data rather than painted into the file so a later filter change does not erase
     * them — see [MarkupStroke].
     */
    val markup: List<MarkupStroke> = emptyList(),
    /**
     * Bumped every time the page's pixels are rewritten to [imageUri].
     *
     * The file name does not change between re-bakes (`<id>_edited.jpg` is reused
     * on purpose, so the page never accumulates files), which means `imageUri`
     * alone cannot tell a viewer that the page it is showing is stale. Anything
     * that caches or keys on a page's image must include this.
     */
    val revision: Int = 0
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
