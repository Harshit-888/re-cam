package com.example.docscanner.util

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.example.docscanner.model.PaperSize
import com.example.docscanner.model.PdfExportConfig
import java.io.File

/**
 * Sends a generated PDF to the system print service.
 *
 * The app already produces a PDF for export, so printing does not need a second
 * renderer: the framework accepts a PDF the same way any other document, and the
 * adapter's whole job is to copy the bytes into the descriptor the print service
 * hands back. That also means what comes out of the printer is byte-identical to what
 * the share sheet would have sent — one code path, one appearance.
 *
 * ## The two things that are easy to get wrong
 *
 * * **The paper size has to be given in mils** (thousandths of an inch), not points.
 *   Getting that wrong by a factor of ~14 prints the document at the wrong size, and
 *   the preview is the only place it shows.
 * * **A password-protected PDF cannot be printed this way.** The print framework opens
 *   the file itself to render it, and an encrypted file fails there, not here — the
 *   user would see a generic "cannot print". [canPrint] exists so the sheet can say so
 *   before it is attempted.
 */
object PdfPrinter {

    /** Points are 1/72 inch; [PrintAttributes.MediaSize] wants 1/1000 inch. */
    private const val MILS_PER_POINT = 1000.0 / 72.0

    /** `PrintManager` needs a real Activity; a service context prints nothing. */
    fun canPrint(context: Context, config: PdfExportConfig): Boolean =
        context is Activity && config.password.isBlank()

    /**
     * Opens the print dialog for [file].
     *
     * @return `false` when printing could not be started — not an `Activity`, no print
     *   service installed, or the framework threw. The caller shows a message; nothing
     *   here is worth crashing over.
     */
    fun print(
        context: Context,
        file: File,
        jobName: String,
        paperSize: PaperSize
    ): Boolean {
        val activity = context as? Activity ?: return false
        val manager = activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            ?: return false
        if (!file.isFile || file.length() == 0L) return false

        return try {
            val media = PrintAttributes.MediaSize(
                paperSize.name.lowercase(),
                paperSize.name,
                (paperSize.widthPt * MILS_PER_POINT).toInt(),
                (paperSize.heightPt * MILS_PER_POINT).toInt()
            )
            val attributes = PrintAttributes.Builder()
                // The margins are already baked into the page geometry by
                // PdfGenerator; asking the print framework for more would add a second
                // margin on top of the first.
                .setMediaSize(media)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .setResolution(PrintAttributes.Resolution("pdf", "PDF", 300, 300))
                .build()

            manager.print(jobName, PdfFileAdapter(file), attributes)
            true
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Copies [file] into the print service's descriptor.
     *
     * `AutoCloseOutputStream` rather than a bare `FileOutputStream`: the descriptor is
     * owned by the framework, and wrapping it means it is closed exactly once, on every
     * path including the failing one.
     */
    private class PdfFileAdapter(private val file: File) : PrintDocumentAdapter() {

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes?,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }
            val info = PrintDocumentInfo.Builder(file.name)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                // The framework asks for a page count up front; a PDF already knows
                // its own, and claiming a number here that turns out to be wrong makes
                // the preview show phantom pages.
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build()
            callback.onLayoutFinished(info, newAttributes != oldAttributes)
        }

        override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback
        ) {
            try {
                // The whole document is written in one pass: the print framework
                // cannot ask for a page range of a PDF it has not copied yet, and a
                // scanned document is a few megabytes, not a few hundred.
                ParcelFileDescriptor.AutoCloseOutputStream(destination).use { out ->
                    file.inputStream().use { input -> input.copyTo(out, DEFAULT_BUFFER_SIZE) }
                }
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (t: Throwable) {
                callback.onWriteFailed(t.message ?: "Could not read the PDF")
            }
        }
    }
}
