package com.example.docscanner.util

import android.graphics.BitmapFactory
import com.example.docscanner.model.PdfCompression
import com.example.docscanner.model.ScannedPage
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * How big the PDF will be, before writing it.
 *
 * The export sheet asks the user to choose between "Original", "Balanced",
 * "Small" and "Tiny" — four words that describe a compression setting, not a
 * consequence. The consequence people actually weigh ("can I email this?") is a
 * number, and until now the only way to get it was to export twice.
 *
 * ## How this works
 *
 * It does not re-encode anything. For a sample of pages it reads two facts that
 * are already on disk — the file's **byte size** and the image's **pixel count** —
 * and extrapolates the same encoder the PDF writer will run:
 *
 * | step | what is used |
 * | :--- | :--- |
 * | bytes per pixel of the stored page | `length / (w × h)`, i.e. how well *this* user's scans compress |
 * | pixels the PDF will embed | the page's own aspect, capped at the export resolution, times the level's `pixelScale` |
 * | encoder | PNG for [PdfCompression.ORIGINAL], JPEG at the level's `jpegQuality` otherwise |
 *
 * Anchoring on the stored page rather than a fixed "documents are about 400 KB"
 * constant is what makes the estimate usable across content: a bi-level page of
 * black text compresses to a tenth of a photo of the same size, and the stored
 * file already knows which one it is. Pages are baked by this app at
 * [ImageStore.JPEG_QUALITY], so the JPEG factor is a ratio against a known
 * quality rather than an absolute guess.
 *
 * ## What it is not
 *
 * A prediction to the byte. It is within roughly a third either way on a single
 * page and tighter over a document, which is what "≈" in the UI means. Two things
 * it cannot see: a watermark composed onto the page at export time (it adds a few
 * percent) and pages that have been deleted from disk since the library was
 * loaded (those fall back to a per-page default). It never changes what is
 * written — [PdfGenerator] is untouched — and it never blocks the sheet: the
 * numbers arrive asynchronously and the cards simply show them a frame later.
 */
object PdfSizeEstimate {

    /**
     * How many pages are measured.
     *
     * A scan session is homogeneous — the same camera, the same filter, the same
     * paper — so twenty-four pages describe a two-hundred-page library as well as
     * measuring all of it, and the measurement stays one directory-stat pass
     * instead of hundreds.
     */
    private const val SAMPLE_LIMIT = 24

    /**
     * Bytes of PDF structure around each embedded image: the page object, the
     * content stream, the footer text and its font reference. ~1.6 KB is what an
     * A4 page with one image and a page number costs, so a ten-page file adds
     * ~16 KB — irrelevant at 40 MB and not irrelevant for a one-page receipt.
     */
    private const val PDF_OVERHEAD_PER_PAGE = 1_600L

    /**
     * A PNG of a photographic page is a good deal larger than the JPEG this app
     * stored it as; a PNG of a bi-level page is much smaller. 2.2 is the middle of
     * that range for document scans, and it is only used when the stored page is
     * *not* already a PNG (see `ImageStore.editedLosslessFile`).
     */
    private const val PNG_OVER_JPEG = 2.2f

    /**
     * JPEG bytes fall off faster than quality: quality 50 is not "half of 92".
     * Fitting the standard quantisation tables, `(q / 92) ^ 3.3` tracks the
     * encoder's output closely enough for the three levels the sheet offers —
     * 0.78 for quality 85, 0.40 for 70, 0.13 for 50.
     */
    private const val QUALITY_CURVE = 3.3f

    /** Bytes per pixel of a page whose size could not be read: a normal colour scan. */
    private const val FALLBACK_SOURCE_BYTES_PER_PX = 0.14f

    /** Sanity box for `length / pixels`, so one odd file cannot skew a document. */
    private const val MIN_SOURCE_BYTES_PER_PX = 0.01f
    private const val MAX_SOURCE_BYTES_PER_PX = 1.2f

    /**
     * Estimated size in **bytes** of the PDF, per compression level, for [pages].
     *
     * @param maxImageDimensionPx the export resolution cap from Settings → PDF
     *   (300/200/150 DPI). Pages larger than this are downscaled before encoding,
     *   and so is the estimate.
     * @return one entry per [PdfCompression] level; empty when there is nothing to
     *   measure, which the UI renders as "no number" rather than "0 KB".
     */
    suspend fun estimate(
        pages: List<ScannedPage>,
        maxImageDimensionPx: Int
    ): Map<PdfCompression, Long> = withContext(Dispatchers.IO) {
        if (pages.isEmpty()) return@withContext emptyMap()

        val sample = if (pages.size <= SAMPLE_LIMIT) pages else sampleAcross(pages)
        val perLevelBytes = HashMap<PdfCompression, Double>(PdfCompression.entries.size)
        PdfCompression.entries.forEach { perLevelBytes[it] = 0.0 }
        var measured = 0

        for (page in sample) {
            val facts = readPage(page) ?: continue
            measured++
            for (level in PdfCompression.entries) {
                val pixels = facts.exportPixels(maxImageDimensionPx, level.pixelScale)
                perLevelBytes[level] = perLevelBytes.getValue(level) +
                    pixels * bytesPerPixel(level, facts.sourceBytesPerPixel, facts.isPng)
            }
        }

        if (measured == 0) {
            // Nothing readable on disk: fall back to a per-page average so the
            // cards still say something. Still four different numbers, still in the
            // right order, just not about this library in particular.
            return@withContext PdfCompression.entries.associateWith { level ->
                val pixels = FALLBACK_PAGE_PIXELS * level.pixelScale * level.pixelScale
                val bytes = pixels *
                    bytesPerPixel(level, FALLBACK_SOURCE_BYTES_PER_PX, sourceIsPng = false)
                (bytes * pages.size).toLong() + pages.size * PDF_OVERHEAD_PER_PAGE
            }
        }

        val scale = pages.size.toDouble() / measured
        PdfCompression.entries.associateWith { level ->
            val mean = perLevelBytes.getValue(level) * scale
            mean.toLong() + pages.size * PDF_OVERHEAD_PER_PAGE
        }
    }

    /**
     * Formats [bytes] the way the sheet shows it: **KB below one megabyte, MB
     * above**, one decimal for megabytes.
     *
     * Decimal units, not binary: every file manager on the phone the user will send
     * this from shows a megabyte as 1,000 KB, and an estimate that disagrees with
     * the share sheet by 5% for no visible reason reads as a bug.
     */
    fun format(bytes: Long): String {
        if (bytes < 1_000_000L) {
            val kb = ((bytes + 500L) / 1_000L).coerceAtLeast(1L)
            return "$kb KB"
        }
        val mb = bytes / 1_000_000.0
        return if (mb < 100.0) {
            String.format(Locale.US, "%.1f MB", mb)
        } else {
            "${mb.toInt()} MB"
        }
    }

    // --------------------------------------------------------------- internals

    /** The only two facts the estimate needs from a stored page. */
    private class PageFacts(
        val width: Int,
        val height: Int,
        val bytes: Long,
        val isPng: Boolean
    ) {
        val longEdge: Int get() = max(width, height)
        val sourcePixels: Double get() = width.toDouble() * height.toDouble()

        val sourceBytesPerPixel: Float
            get() = if (sourcePixels <= 0.0) {
                FALLBACK_SOURCE_BYTES_PER_PX
            } else {
                (bytes / sourcePixels).toFloat()
                    .coerceIn(MIN_SOURCE_BYTES_PER_PX, MAX_SOURCE_BYTES_PER_PX)
            }

        /**
         * Pixels the PDF will actually encode for this page at [pixelScale].
         *
         * The export first caps the long edge at [cap] (the resolution setting) and
         * then applies the level's own downscale — the same order
         * `PdfGenerator`/`PdfImages.scaleDown` uses, so a Tiny page is not modelled
         * as if it were scaled twice.
         */
        fun exportPixels(cap: Int, pixelScale: Float): Double {
            if (longEdge <= 0) return 0.0
            val capScale = min(1f, cap.toFloat() / longEdge)
            val applied = capScale * pixelScale
            return sourcePixels * applied * applied
        }
    }

    private fun readPage(page: ScannedPage): PageFacts? = try {
        val file = File(page.imageUri.removePrefix("file://"))
        if (!file.isFile) {
            null
        } else {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)
            val width = options.outWidth
            val height = options.outHeight
            if (width <= 0 || height <= 0) {
                null
            } else {
                PageFacts(
                    width = width,
                    height = height,
                    bytes = file.length(),
                    // The editor writes the flat/bi-level filters through
                    // `ImageStore.editedLosslessFile`, so a stored PNG is already
                    // the lossless size and must not be multiplied again.
                    isPng = file.extension.equals("png", ignoreCase = true)
                )
            }
        }
    } catch (_: Throwable) {
        // An estimate is never worth a crash on a share sheet.
        null
    }

    private fun bytesPerPixel(level: PdfCompression, source: Float, sourceIsPng: Boolean): Double =
        if (level.isLossless) {
            (source * if (sourceIsPng) 1f else PNG_OVER_JPEG).toDouble()
        } else {
            val ratio = (level.jpegQuality / ImageStore.JPEG_QUALITY.toFloat())
                .pow(QUALITY_CURVE)
            (source * ratio).toDouble()
        }

    /**
     * Evenly spread sample instead of "the first twenty-four pages".
     *
     * A library is usually several scan sessions — a chapter of text, then a batch
     * of photos — and taking only the head of the list would estimate the whole
     * file from whichever kind happened to be scanned first.
     */
    private fun sampleAcross(pages: List<ScannedPage>): List<ScannedPage> {
        val step = pages.size.toDouble() / SAMPLE_LIMIT
        return (0 until SAMPLE_LIMIT).map { pages[(it * step).toInt().coerceAtMost(pages.lastIndex)] }
    }

    /** A 300 DPI A4 page — used only when no stored page can be read. */
    private const val FALLBACK_PAGE_PIXELS = 3_200.0 * 2_260.0
}
