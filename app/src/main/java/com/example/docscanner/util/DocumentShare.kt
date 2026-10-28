package com.example.docscanner.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.docscanner.model.AppSettings
import com.example.docscanner.model.Document
import com.example.docscanner.model.PdfCompression
import com.example.docscanner.model.PdfExportConfig
import com.example.docscanner.model.PdfMargin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Sharing **documents**, without opening the export sheet first.
 *
 * The library's selection bar had exactly one verb — merge — which meant "send these
 * three scans to someone" was a four-step detour: open each document, export it, share
 * it, come back. Sharing from a selection is one tap, and the files are produced by the
 * same [PdfGenerator] with the same settings-derived defaults the export sheet starts
 * from, so what leaves the app is byte-for-byte what an export would have produced.
 *
 * Deliberately **not** a merge: three documents shared as three PDFs arrive as three
 * attachments, which is what "share these documents" means. Merging them is the other
 * cell on the same bar.
 */
object DocumentShare {

    /**
     * The defaults Settings → PDF supplies to the export sheet.
     *
     * Duplicated *on purpose* rather than reaching into `PdfExportSheet`: this is model
     * code and must be usable without Compose, and the alternative — a Compose function
     * building a config — would have made sharing depend on the sheet being composed.
     * Compression stays [PdfCompression.ORIGINAL] because the sheet's own default is
     * lossless unless a merge overrides it, and a share must not silently shrink
     * someone's pages.
     */
    fun exportConfig(settings: AppSettings): PdfExportConfig = PdfExportConfig(
        paperSize = settings.pageSize,
        margins = PdfMargin.STANDARD,
        includePageNumbers = true,
        maxImageDimensionPx = settings.resolution.maxImageDimensionPx,
        pageOrientation = settings.pageOrientation,
        imageScale = settings.imageScale,
        fileBaseName = settings.defaultDocumentName
    )

    /**
     * Writes one PDF per document and hands them to the system share sheet.
     *
     * @return how many files were shared. `0` means nothing could be written and the
     *   caller should say so rather than opening an empty chooser.
     */
    suspend fun shareDocuments(
        context: Context,
        documents: List<Document>,
        settings: AppSettings
    ): Int = withContext(Dispatchers.IO) {
        if (documents.isEmpty()) return@withContext 0
        val config = exportConfig(settings)
        val files = ArrayList<File>(documents.size)
        documents.forEach { document ->
            if (document.pages.isEmpty()) return@forEach
            val name = PdfGenerator.sanitiseFileName(
                document.title.ifBlank { "document" }
            ).take(60).ifBlank { "document" }
            val written = runCatching {
                PdfGenerator.generatePdf(
                    context = context,
                    // One file per document, so the title has to be the document's
                    // own — "Shared document" three times in a chooser is unreadable.
                    title = name,
                    pages = document.pages,
                    config = config
                )
            }.getOrNull()
            if (written != null) files.add(written)
        }
        if (files.isEmpty()) return@withContext 0

        withContext(Dispatchers.Main) {
            runCatching { share(context, files) }.onFailure { error ->
                Toast.makeText(
                    context,
                    "Sharing unavailable: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        files.size
    }

    /** [Intent.ACTION_SEND_MULTIPLE] — several PDFs in one sheet, not several sheets. */
    private fun share(context: Context, files: List<File>) {
        val authority = "${context.packageName}.fileprovider"
        val uris = files.map { file ->
            FileProvider.getUriForFile(context, authority, file)
        }
        val intent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uris.first())
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "application/pdf"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(
            Intent.createChooser(intent, "Share ${uris.size} PDF${if (uris.size == 1) "" else "s"}")
        )
    }
}
