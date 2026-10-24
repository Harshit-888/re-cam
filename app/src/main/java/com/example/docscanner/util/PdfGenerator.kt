package com.example.docscanner.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.docscanner.model.Document
import com.example.docscanner.model.PdfExportConfig
import com.example.docscanner.model.ScannedPage
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    /**
     * Generates a multi-page PDF file from a Document and configuration
     */
    fun generatePdf(
        context: Context,
        document: Document,
        config: PdfExportConfig,
        bitmapProvider: (ScannedPage) -> Bitmap?
    ): File {
        val pdfDocument = PdfDocument()
        val paperWidth = config.paperSize.widthPt
        val paperHeight = config.paperSize.heightPt
        val margin = config.margins.marginPt

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }

        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(40, 0, 0, 0)
            textSize = 36f
            textAlign = Paint.Align.CENTER
        }

        val pages = document.pages
        val totalPages = pages.size

        for (i in pages.indices) {
            val pageInfo = PdfDocument.PageInfo.Builder(paperWidth, paperHeight, i + 1).create()
            val pdfPage = pdfDocument.startPage(pageInfo)
            val canvas = pdfPage.canvas

            // White page background
            canvas.drawColor(Color.WHITE)

            val scannedPage = pages[i]
            val bitmap = bitmapProvider(scannedPage)

            if (bitmap != null) {
                // Apply rotation and filter enhancements
                val enhancedBitmap = ImageProcessingUtils.applyEnhancements(
                    bitmap,
                    scannedPage.filterType,
                    scannedPage.rotationDegrees
                )

                // Calculate printable area
                val printableLeft = margin.toFloat()
                val printableTop = margin.toFloat()
                val printableRight = (paperWidth - margin).toFloat()
                val printableBottom = if (config.includePageNumbers) {
                    (paperHeight - margin - 20).toFloat()
                } else {
                    (paperHeight - margin).toFloat()
                }

                val printableWidth = printableRight - printableLeft
                val printableHeight = printableBottom - printableTop

                // Maintain aspect ratio
                val imgWidth = enhancedBitmap.width.toFloat()
                val imgHeight = enhancedBitmap.height.toFloat()
                val scale = minOf(printableWidth / imgWidth, printableHeight / imgHeight)

                val destWidth = imgWidth * scale
                val destHeight = imgHeight * scale
                val destLeft = printableLeft + (printableWidth - destWidth) / 2f
                val destTop = printableTop + (printableHeight - destHeight) / 2f

                val destRect = RectF(destLeft, destTop, destLeft + destWidth, destTop + destHeight)
                canvas.drawBitmap(enhancedBitmap, null, destRect, null)
            }

            // Draw optional watermark
            scannedPage.watermarkText?.let { watermark ->
                if (watermark.isNotBlank()) {
                    canvas.save()
                    canvas.rotate(-35f, paperWidth / 2f, paperHeight / 2f)
                    canvas.drawText(watermark, paperWidth / 2f, paperHeight / 2f, watermarkPaint)
                    canvas.restore()
                }
            }

            // Draw footer page number
            if (config.includePageNumbers) {
                val footerY = (paperHeight - margin / 2f - 4).coerceAtLeast((paperHeight - 14).toFloat())
                canvas.drawText("${i + 1} / $totalPages", paperWidth / 2f, footerY, textPaint)
            }

            pdfDocument.finishPage(pdfPage)
        }

        // Save output to app cache directory
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val safeTitle = document.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val outputFile = File(exportDir, "${safeTitle}_${System.currentTimeMillis()}.pdf")

        FileOutputStream(outputFile).use { fos ->
            pdfDocument.writeTo(fos)
        }
        pdfDocument.close()

        return outputFile
    }
}
