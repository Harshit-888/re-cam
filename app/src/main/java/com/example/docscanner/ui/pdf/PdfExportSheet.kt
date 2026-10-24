package com.example.docscanner.ui.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.docscanner.util.PdfGenerator
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfExportSheet(
    document: Document,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedPaperSize by remember { mutableStateOf(PaperSize.A4) }
    var selectedMargin by remember { mutableStateOf(PdfMargin.STANDARD) }
    var includePageNumbers by remember { mutableStateOf(true) }
    var isGenerating by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                        "${document.title} (${document.pages.size} pages)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Paper Size Dropdown / Carousel
            Text("Paper Size Preset:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(PaperSize.A4, PaperSize.LETTER, PaperSize.LEGAL).forEach { size ->
                    FilterChip(
                        selected = selectedPaperSize == size,
                        onClick = { selectedPaperSize = size },
                        label = { Text(size.name) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Page Margins Selector
            Text("Page Margins:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(PdfMargin.NONE, PdfMargin.COMPACT, PdfMargin.STANDARD).forEach { margin ->
                    FilterChip(
                        selected = selectedMargin == margin,
                        onClick = { selectedMargin = margin },
                        label = { Text(margin.label) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Include Page Numbers Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Footer Page Numbers", style = MaterialTheme.typography.bodyMedium)
                    Text("e.g. 1 / 3 at bottom center", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = includePageNumbers,
                    onCheckedChange = { includePageNumbers = it }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Button
            Button(
                onClick = {
                    isGenerating = true
                    try {
                        val config = PdfExportConfig(
                            paperSize = selectedPaperSize,
                            margins = selectedMargin,
                            includePageNumbers = includePageNumbers
                        )

                        // Generate PDF with simulated high-res document page renderings
                        val pdfFile = PdfGenerator.generatePdf(
                            context = context,
                            document = document,
                            config = config,
                            bitmapProvider = { page ->
                                createSamplePageBitmap(page.pageIndex + 1, document.title)
                            }
                        )

                        // Share via Intent
                        sharePdfFile(context, pdfFile, document.title)
                        onDismiss()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isGenerating = false
                    }
                },
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate & Share PDF", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun createSamplePageBitmap(pageNum: Int, title: String): Bitmap {
    val width = 1200
    val height = 1600
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 48f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    canvas.drawText(title, 80f, 160f, paint)

    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 32f
    }
    canvas.drawText("Page $pageNum - High Resolution Document Scan", 80f, 220f, subPaint)

    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        strokeWidth = 4f
    }
    for (y in 300..1400 step 70) {
        canvas.drawLine(80f, y.toFloat(), (width - 80).toFloat(), y.toFloat(), linePaint)
    }

    return bitmap
}

private fun sharePdfFile(context: Context, file: File, title: String) {
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
        context.startActivity(Intent.createChooser(shareIntent, "Share Document PDF"))
    } catch (e: Exception) {
        // Fallback for simple share without fileprovider
        Toast.makeText(context, "Saved PDF to ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }
}
