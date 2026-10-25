package com.example.docscanner.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.docscanner.model.Document
import com.example.docscanner.model.ImageScale
import com.example.docscanner.model.PageOrientation
import com.example.docscanner.model.PdfExportConfig
import com.example.docscanner.model.ScannedPage
import java.io.File
import java.io.FileOutputStream

/**
 * Multi-page PDF compiler.
 *
 * Pages are **real page bitmaps** supplied by [bitmapProvider] — normally
 * [PageRenderer.bitmapForExport], which returns the already-baked JPEG (warp,
 * rotation, filter and signature applied). Nothing here invents content.
 *
 * `android.graphics.pdf.PdfDocument` cannot encrypt output, which is why
 * [PdfExportConfig] carries no password field.
 *
 * Blocking. Always call this from `Dispatchers.IO`.
 */
object PdfGenerator {

    /**
     * @param bitmapProvider page -> bitmap to draw, or `null` to leave the page
     *   blank (a real possibility if the JPEG is missing on disk).
     * @return the written PDF in `cacheDir/exports`.
     */
    fun generatePdf(
        context: Context,
        document: Document,
        config: PdfExportConfig,
        bitmapProvider: (ScannedPage) -> Bitmap?
    ): File {
        val pdfDocument = PdfDocument()
        val presetWidth = config.paperSize.widthPt
        val presetHeight = config.paperSize.heightPt
        val margin = config.margins.marginPt

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

        val pages = document.pages
        val totalPages = pages.size

        try {
            for (index in pages.indices) {
                val scannedPage = pages[index]
                val bitmap = bitmapProvider(scannedPage)

                // Orientation is decided per page, which is the whole point of
                // "Auto": a landscape photo of a receipt should not be squeezed
                // onto a portrait sheet. Presets keep their native axis
                // (a business card is landscape, a receipt roll is portrait).
                val landscape = when (config.pageOrientation) {
                    PageOrientation.PORTRAIT -> false
                    PageOrientation.LANDSCAPE -> true
                    PageOrientation.AUTO -> bitmap != null && bitmap.width > bitmap.height
                }
                val paperWidth = if (landscape) maxOf(presetWidth, presetHeight) else minOf(presetWidth, presetHeight)
                val paperHeight = if (landscape) minOf(presetWidth, presetHeight) else maxOf(presetWidth, presetHeight)

                val pageInfo = PdfDocument.PageInfo.Builder(paperWidth, paperHeight, index + 1).create()
                val pdfPage = pdfDocument.startPage(pageInfo)
                val canvas = pdfPage.canvas

                canvas.drawColor(Color.WHITE)

                if (bitmap != null) {
                    val printableLeft = margin.toFloat()
                    val printableTop = margin.toFloat()
                    val printableRight = (paperWidth - margin).toFloat()
                    val printableBottom = if (config.includePageNumbers) {
                        (paperHeight - margin - 16).toFloat()
                    } else {
                        (paperHeight - margin).toFloat()
                    }
                    val printableWidth = printableRight - printableLeft
                    val printableHeight = printableBottom - printableTop

                    if (printableWidth > 0f && printableHeight > 0f) {
                        val imageWidth = bitmap.width.toFloat()
                        val imageHeight = bitmap.height.toFloat()
                        val (destWidth, destHeight) = when (config.imageScale) {
                            // Whole page visible, aspect kept, bands on one axis.
                            ImageScale.FIT -> {
                                val scale = minOf(printableWidth / imageWidth, printableHeight / imageHeight)
                                imageWidth * scale to imageHeight * scale
                            }
                            // Cover the sheet; overflow is clipped, never squeezed.
                            ImageScale.FILL -> {
                                val scale = maxOf(printableWidth / imageWidth, printableHeight / imageHeight)
                                imageWidth * scale to imageHeight * scale
                            }
                            // 1 image pixel per PDF point (1/72 in), centred.
                            ImageScale.ORIGINAL -> imageWidth to imageHeight
                        }
                        val destLeft = printableLeft + (printableWidth - destWidth) / 2f
                        val destTop = printableTop + (printableHeight - destHeight) / 2f

                        canvas.save()
                        if (config.imageScale != ImageScale.FIT) {
                            // Without this the cropped overflow would print into the
                            // margins — and, on the last page, off the sheet.
                            canvas.clipRect(
                                printableLeft,
                                printableTop,
                                printableRight,
                                printableBottom
                            )
                        }
                        canvas.drawBitmap(
                            bitmap,
                            null,
                            RectF(destLeft, destTop, destLeft + destWidth, destTop + destHeight),
                            Paint(Paint.FILTER_BITMAP_FLAG)
                        )
                        canvas.restore()
                    }
                } else {
                    // Visible, honest failure rather than a silently blank sheet.
                    val notice = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.LTGRAY
                        textSize = 12f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText(
                        "Page ${index + 1} image unavailable",
                        paperWidth / 2f,
                        paperHeight / 2f,
                        notice
                    )
                }

                scannedPage.watermarkText?.takeIf { it.isNotBlank() }?.let { watermark ->
                    watermarkPaint.textSize = paperWidth * 0.11f
                    canvas.save()
                    canvas.rotate(-35f, paperWidth / 2f, paperHeight / 2f)
                    canvas.drawText(watermark, paperWidth / 2f, paperHeight / 2f, watermarkPaint)
                    canvas.restore()
                }

                if (config.includePageNumbers) {
                    val footerY = (paperHeight - margin / 2f - 2f)
                        .coerceIn(paperHeight - 20f, paperHeight - 6f)
                    canvas.drawText("${index + 1} / $totalPages", paperWidth / 2f, footerY, footerPaint)
                }

                pdfDocument.finishPage(pdfPage)
            }

            val exportDir = ImageStore.exportsDir(context)
            val safeTitle = sanitiseFileName(document.title).take(60).ifBlank { "document" }
            // Settings → PDF → Document Name is the file name prefix, so exports
            // land in a folder sorted by whatever the user picked.
            val prefix = sanitiseFileName(config.fileBaseName).take(40)
            val baseName = if (prefix.isBlank()) safeTitle else "${prefix}_$safeTitle"
            val outputFile = File(exportDir, "${baseName}_${System.currentTimeMillis()}.pdf")

            FileOutputStream(outputFile).use { stream -> pdfDocument.writeTo(stream) }
            return outputFile
        } finally {
            pdfDocument.close()
        }
    }

    /** Keeps file names to characters every filesystem and share target accepts. */
    private fun sanitiseFileName(raw: String): String =
        raw.trim().replace(Regex("[^a-zA-Z0-9_-]+"), "_").trim('_')

}
