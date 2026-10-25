package com.example.docscanner.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.docscanner.model.ImageScale
import com.example.docscanner.model.PageOrientation
import com.example.docscanner.model.PdfExportConfig
import com.example.docscanner.model.ScannedPage
import java.io.File
import java.io.FileOutputStream

/**
 * Multi-page PDF compiler.
 *
 * Pages are **real page bitmaps** from [PageRenderer.bitmapForExport] — the
 * already-baked JPEG or PNG (warp, rotation, filter and signature applied).
 * Nothing here invents content.
 *
 * Two engines, chosen per export:
 *
 * * `android.graphics.pdf.PdfDocument` — the platform writer — for an ordinary
 *   export. It is what every previous round shipped, so the default output stays
 *   byte-for-byte what it always was.
 * * [PdfWriter] when the user asked for a **password**, a **smaller file**, or a
 *   **merge**. `PdfDocument` can neither encrypt nor choose an image encoding, and
 *   its lossless page images are the reason a photographed scan runs to several
 *   megabytes. [PdfWriter] writes the same layout (both paths share
 *   [pdfPageGeometry]) with JPEG or Flate page images, and can encrypt.
 *
 * Blocking. Always call this from `Dispatchers.IO`.
 */
object PdfGenerator {

    /**
     * @param title document title, used for the file name and the PDF's metadata.
     * @param pages pages to export, **in order**. A merge is just the concatenation
     *   of several documents' pages, so it needs no separate code path.
     * @return the written PDF in `cacheDir/exports`.
     */
    fun generatePdf(
        context: Context,
        title: String,
        pages: List<ScannedPage>,
        config: PdfExportConfig
    ): File {
        val output = outputFile(context, title, config)
        if (config.password.isBlank() && config.compression.isLossless) {
            writeWithPlatform(context, pages, config, output)
        } else {
            writeWithPdfWriter(context, title, pages, config, output)
        }
        return output
    }

    // ------------------------------------------------------------- platform

    private fun writeWithPlatform(
        context: Context,
        pages: List<ScannedPage>,
        config: PdfExportConfig,
        output: File
    ) {
        val pdfDocument = PdfDocument()
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 9f
            textAlign = Paint.Align.CENTER
        }
        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(38, 0, 0, 0)
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val totalPages = pages.size

        try {
            for (index in pages.indices) {
                val scannedPage = pages[index]
                val bitmap = PageRenderer.bitmapForExport(context, scannedPage, config.maxImageDimensionPx)
                val geometry = pdfPageGeometry(
                    config,
                    bitmap?.width ?: 0,
                    bitmap?.height ?: 0
                )
                val pageInfo = PdfDocument.PageInfo
                    .Builder(geometry.paperWidth, geometry.paperHeight, index + 1)
                    .create()
                val pdfPage = pdfDocument.startPage(pageInfo)
                val canvas = pdfPage.canvas
                canvas.drawColor(Color.WHITE)

                val draw = geometry.draw
                if (bitmap != null && draw != null) {
                    canvas.save()
                    geometry.clip?.let { clip ->
                        // Without this the cropped overflow would print into the
                        // margins — and, on the last page, off the sheet.
                        canvas.clipRect(clip)
                    }
                    canvas.drawBitmap(bitmap, null, draw, Paint(Paint.FILTER_BITMAP_FLAG))
                    canvas.restore()
                } else if (bitmap == null) {
                    // Visible, honest failure rather than a silently blank sheet.
                    val notice = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.LTGRAY
                        textSize = 12f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText(
                        "Page ${index + 1} image unavailable",
                        geometry.paperWidth / 2f,
                        geometry.paperHeight / 2f,
                        notice
                    )
                }

                scannedPage.watermarkText?.takeIf { it.isNotBlank() }?.let { watermark ->
                    watermarkPaint.textSize = geometry.paperWidth * WATERMARK_FRACTION
                    canvas.save()
                    canvas.rotate(
                        WATERMARK_ANGLE_DEGREES,
                        geometry.paperWidth / 2f,
                        geometry.paperHeight / 2f
                    )
                    canvas.drawText(
                        watermark,
                        geometry.paperWidth / 2f,
                        geometry.paperHeight / 2f,
                        watermarkPaint
                    )
                    canvas.restore()
                }

                if (config.includePageNumbers) {
                    canvas.drawText(
                        "${index + 1} / $totalPages",
                        geometry.paperWidth / 2f,
                        geometry.footerBaseline,
                        footerPaint
                    )
                }

                pdfDocument.finishPage(pdfPage)
            }

            FileOutputStream(output).use { stream -> pdfDocument.writeTo(stream) }
        } finally {
            pdfDocument.close()
        }
    }

    // ------------------------------------------------------------ own writer

    /**
     * The path that can encrypt and compress.
     *
     * One page is decoded, laid out, encoded and **recycled** before the next one
     * starts. That is what makes a hundred-page merge possible on a phone without
     * holding a hundred bitmaps, and it is safe here in a way it is not in the
     * platform path: the encoded bytes are in hand before anything is recycled, so
     * no renderer can still be waiting on the bitmap.
     */
    private fun writeWithPdfWriter(
        context: Context,
        title: String,
        pages: List<ScannedPage>,
        config: PdfExportConfig,
        output: File
    ) {
        val writer = PdfWriter(title, config.password)
        val total = pages.size
        writer.writeTo(output) { addPage ->
            pages.forEachIndexed { index, page ->
                val owned = ArrayList<Bitmap>(3)
                try {
                    val source = PageRenderer.bitmapForExport(context, page, config.maxImageDimensionPx)
                    if (source == null) {
                        val geometry = pdfPageGeometry(config, 0, 0)
                        addPage(
                            planFor(
                                geometry = geometry,
                                image = null,
                                index = index,
                                total = total,
                                includePageNumbers = config.includePageNumbers,
                                notice = "Page ${index + 1} image unavailable"
                            )
                        )
                        return@forEachIndexed
                    }
                    owned += source

                    // Geometry comes from the *full-resolution* page, before any
                    // compression downscale: the compression control may not move a
                    // page on its sheet.
                    val geometry = pdfPageGeometry(config, source.width, source.height)
                    var working = source

                    if (config.compression.pixelScale < 1f) {
                        val scaled = PdfImages.scaleDown(source, config.compression.pixelScale)
                        if (scaled !== source) {
                            owned += scaled
                            working = scaled
                        }
                    }

                    val watermark = page.watermarkText?.takeIf { it.isNotBlank() }
                    val draw = geometry.draw
                    if (watermark != null && draw != null && draw.width() > 0f) {
                        // Points → the page bitmap's own pixels, so the watermark is
                        // the same fraction of the sheet as the canvas path draws.
                        val pixelsPerPoint = working.width / draw.width()
                        val composed = PdfImages.composeWatermark(
                            source = working,
                            text = watermark,
                            fontSizePx = geometry.paperWidth * WATERMARK_FRACTION * pixelsPerPoint
                        )
                        if (composed !== working) {
                            owned += composed
                            working = composed
                        }
                    }

                    val image = if (config.compression.isLossless) {
                        PdfImages.encodeLossless(working)
                    } else {
                        PdfImages.encodeJpeg(working, config.compression.jpegQuality)
                    }

                    addPage(
                        planFor(
                            geometry = geometry,
                            image = image,
                            index = index,
                            total = total,
                            includePageNumbers = config.includePageNumbers,
                            notice = null
                        )
                    )
                } finally {
                    for (bitmap in owned) {
                        if (!bitmap.isRecycled) bitmap.recycle()
                    }
                }
            }
        }
    }

    private fun planFor(
        geometry: PdfPageGeometry,
        image: PdfPageImage?,
        index: Int,
        total: Int,
        includePageNumbers: Boolean,
        notice: String?
    ): PdfPagePlan {
        val draw = geometry.draw
        return PdfPagePlan(
            widthPt = geometry.paperWidth.toFloat(),
            heightPt = geometry.paperHeight.toFloat(),
            image = image,
            left = draw?.left ?: 0f,
            top = draw?.top ?: 0f,
            drawWidth = draw?.width() ?: 0f,
            drawHeight = draw?.height() ?: 0f,
            clip = if (image != null) geometry.clip else null,
            footerBaseline = if (includePageNumbers) geometry.footerBaseline else null,
            footer = if (includePageNumbers) "${index + 1} / $total" else null,
            notice = notice
        )
    }

    // ---------------------------------------------------------------- naming

    /**
     * The name a plain export would get, before uniquifying.
     *
     * Settings → PDF → Document Name is a prefix, which is what makes a folder of
     * exports sort by whatever the user chose. A name typed in the share sheet
     * replaces the whole thing — that is the point of typing it.
     */
    fun suggestedName(title: String, config: PdfExportConfig): String {
        val safeTitle = sanitiseFileName(title).take(60).ifBlank { "document" }
        val prefix = sanitiseFileName(config.fileBaseName).take(40)
        return if (prefix.isBlank()) safeTitle else "${prefix}_$safeTitle"
    }

    /** Keeps file names to characters every filesystem and share target accepts. */
    fun sanitiseFileName(raw: String): String =
        raw.trim().replace(Regex("[^a-zA-Z0-9_-]+"), "_").trim('_')

    private fun outputFile(context: Context, title: String, config: PdfExportConfig): File {
        val exportDir = ImageStore.exportsDir(context)
        val requested = sanitiseFileName(config.fileName).take(80).ifBlank {
            suggestedName(title, config)
        }
        // Two exports of the same document on the same day are normal; a fifth
        // `_2026-10-25` suffix in the file name is not. Only an actual collision
        // costs a suffix.
        var candidate = File(exportDir, "$requested.pdf")
        var counter = 2
        while (candidate.exists()) {
            candidate = File(exportDir, "${requested}_$counter.pdf")
            counter++
        }
        return candidate
    }

    /** Fraction of the page width the watermark is drawn at. Shared by both engines. */
    private const val WATERMARK_FRACTION = 0.11f

    /** Shared by both engines so a watermark reads the same whichever wrote the PDF. */
    private const val WATERMARK_ANGLE_DEGREES = -35f
}

/**
 * Where one page lands on its sheet, in PostScript points with the origin at the
 * **top left** (the space `Canvas` draws in).
 *
 * Extracted so the platform writer and [PdfWriter] cannot drift apart: an export
 * with a password must place the same page in the same rectangle as an export
 * without one. Every number here is the one the previous revision computed inline.
 */
internal data class PdfPageGeometry(
    val paperWidth: Int,
    val paperHeight: Int,
    /** Where the page image is drawn, or `null` when there is no image to draw. */
    val draw: RectF?,
    /** Printable box, when the image overflows it. */
    val clip: RectF?,
    /** Footer baseline measured from the top of the page. */
    val footerBaseline: Float
)

internal fun pdfPageGeometry(
    config: PdfExportConfig,
    imageWidth: Int,
    imageHeight: Int
): PdfPageGeometry {
    val presetWidth = config.paperSize.widthPt
    val presetHeight = config.paperSize.heightPt
    val margin = config.margins.marginPt.toFloat()

    // Orientation is decided per page, which is the whole point of "Auto": a
    // landscape photo of a receipt should not be squeezed onto a portrait sheet.
    // Presets keep their native axis (a business card is landscape, a receipt roll
    // is portrait).
    val landscape = when (config.pageOrientation) {
        PageOrientation.PORTRAIT -> false
        PageOrientation.LANDSCAPE -> true
        PageOrientation.AUTO -> imageWidth > imageHeight
    }
    val paperWidth = if (landscape) maxOf(presetWidth, presetHeight) else minOf(presetWidth, presetHeight)
    val paperHeight = if (landscape) minOf(presetWidth, presetHeight) else maxOf(presetWidth, presetHeight)

    val printableLeft = margin
    val printableTop = margin
    val printableRight = paperWidth - margin
    val printableBottom = if (config.includePageNumbers) {
        paperHeight - margin - 16f
    } else {
        paperHeight - margin
    }
    val printableWidth = printableRight - printableLeft
    val printableHeight = printableBottom - printableTop

    val draw = if (imageWidth > 0 && imageHeight > 0 && printableWidth > 0f && printableHeight > 0f) {
        val sourceWidth = imageWidth.toFloat()
        val sourceHeight = imageHeight.toFloat()
        val (destWidth, destHeight) = when (config.imageScale) {
            // Whole page visible, aspect kept, bands on one axis.
            ImageScale.FIT -> {
                val scale = minOf(printableWidth / sourceWidth, printableHeight / sourceHeight)
                sourceWidth * scale to sourceHeight * scale
            }
            // Cover the sheet; overflow is clipped, never squeezed.
            ImageScale.FILL -> {
                val scale = maxOf(printableWidth / sourceWidth, printableHeight / sourceHeight)
                sourceWidth * scale to sourceHeight * scale
            }
            // 1 image pixel per PDF point (1/72 in), centred.
            ImageScale.ORIGINAL -> sourceWidth to sourceHeight
        }
        val left = printableLeft + (printableWidth - destWidth) / 2f
        val top = printableTop + (printableHeight - destHeight) / 2f
        RectF(left, top, left + destWidth, top + destHeight)
    } else {
        null
    }

    return PdfPageGeometry(
        paperWidth = paperWidth,
        paperHeight = paperHeight,
        draw = draw,
        clip = if (draw != null && config.imageScale != ImageScale.FIT) {
            RectF(printableLeft, printableTop, printableRight, printableBottom)
        } else {
            null
        },
        footerBaseline = (paperHeight - margin / 2f - 2f)
            .coerceIn(paperHeight - 20f, paperHeight - 6f)
    )
}
