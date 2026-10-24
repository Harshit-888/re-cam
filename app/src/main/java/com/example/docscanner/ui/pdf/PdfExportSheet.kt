package com.example.docscanner.ui.pdf

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.docscanner.model.Document
import com.example.docscanner.model.PaperSize
import com.example.docscanner.model.PdfExportConfig
import com.example.docscanner.model.PdfMargin
import com.example.docscanner.util.PageRenderer
import com.example.docscanner.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfExportSheet(
    document: Document,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedPaperSize by remember { mutableStateOf(PaperSize.A4) }
    var selectedMargin by remember { mutableStateOf(PdfMargin.STANDARD) }
    var includePageNumbers by remember { mutableStateOf(true) }
    var isGenerating by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Export as PDF",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${document.title} • ${document.pages.size} pages",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Paper size", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PaperSize.entries.toList(), key = { it.name }) { size ->
                    FilterChip(
                        selected = selectedPaperSize == size,
                        onClick = { selectedPaperSize = size },
                        label = { Text(size.label) },
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Margins", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PdfMargin.entries.toList(), key = { it.name }) { margin ->
                    FilterChip(
                        selected = selectedMargin == margin,
                        onClick = { selectedMargin = margin },
                        label = { Text(margin.label) },
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Footer page numbers", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "e.g. 1 / ${document.pages.size} at the bottom centre",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = includePageNumbers,
                    onCheckedChange = { includePageNumbers = it }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                enabled = !isGenerating && document.pages.isNotEmpty(),
                onClick = {
                    if (document.pages.isEmpty()) return@Button
                    isGenerating = true
                    val config = PdfExportConfig(
                        paperSize = selectedPaperSize,
                        margins = selectedMargin,
                        includePageNumbers = includePageNumbers
                    )
                    scope.launch {
                        val result = runCatching {
                            withContext(Dispatchers.IO) {
                                PdfGenerator.generatePdf(
                                    context = context,
                                    document = document,
                                    config = config,
                                    bitmapProvider = { page ->
                                        PageRenderer.bitmapForExport(
                                            context,
                                            page,
                                            config.maxImageDimensionPx
                                        )
                                    }
                                )
                            }
                        }
                        isGenerating = false
                        result
                            .onSuccess { file ->
                                sharePdf(context, file, document.title)
                                onDismiss()
                            }
                            .onFailure { error ->
                                Toast.makeText(
                                    context,
                                    "Export failed: ${error.message ?: "unknown error"}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Compiling pages…", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate & Share PDF", fontWeight = FontWeight.Bold)
                }
            }

            if (document.pages.isEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Add at least one page before exporting.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Shares the PDF through the system sheet.
 *
 * Requires the `FileProvider` declared in `AndroidManifest.xml` with
 * `res/xml/file_paths.xml`; without it every share threw and silently fell back
 * to the Toast, which is exactly what the previous revision did.
 */
private fun sharePdf(context: Context, file: File, title: String) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share document PDF"))
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "PDF saved to ${file.absolutePath}\nSharing unavailable: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}
