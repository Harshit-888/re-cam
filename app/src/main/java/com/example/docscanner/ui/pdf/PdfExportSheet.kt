package com.example.docscanner.ui.pdf

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.docscanner.model.AppSettings
import com.example.docscanner.model.Document
import com.example.docscanner.model.PaperSize
import com.example.docscanner.model.PdfExportConfig
import com.example.docscanner.model.PdfMargin
import com.example.docscanner.theme.DocShapes
import com.example.docscanner.theme.pressScale
import com.example.docscanner.ui.common.InfoPill
import com.example.docscanner.ui.common.LoadingDots
import com.example.docscanner.ui.common.SectionLabel
import com.example.docscanner.ui.common.SegmentedPill
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
    settings: AppSettings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Settings → PDF supplies the defaults; the controls below are a per-export
    // override that starts from them and is deliberately not persisted.
    var selectedPaperSize by remember { mutableStateOf(settings.pageSize) }
    var selectedMargin by remember { mutableStateOf(PdfMargin.STANDARD) }
    var includePageNumbers by remember { mutableStateOf(true) }
    var isGenerating by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = DocShapes.sheet
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 26.dp)
        ) {
            // ---- header -------------------------------------------------------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(DocShapes.iconButton)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "EXPORT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        document.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        InfoPill(
                            "${document.pages.size} ${if (document.pages.size == 1) "page" else "pages"}",
                            icon = Icons.Default.Description
                        )
                        InfoPill(settings.resolution.label)
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // ---- paper --------------------------------------------------------
            SectionLabel("Paper size")
            SegmentedPill(
                options = PaperSize.entries.map { it.label },
                selectedIndex = PaperSize.entries.indexOf(selectedPaperSize),
                onSelect = { selectedPaperSize = PaperSize.entries[it] },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))

            // ---- margins ------------------------------------------------------
            SectionLabel("Margins")
            SegmentedPill(
                options = PdfMargin.entries.map { it.label },
                selectedIndex = PdfMargin.entries.indexOf(selectedMargin),
                onSelect = { selectedMargin = PdfMargin.entries[it] },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))

            // ---- output -------------------------------------------------------
            SectionLabel("Output")
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = DocShapes.cardSmall,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Footer page numbers",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "1 / ${document.pages.size} at the bottom centre",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = includePageNumbers,
                        onCheckedChange = { includePageNumbers = it }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "${settings.pageOrientation.label} · ${settings.imageScale.label} image scale",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )

            if (document.pages.isEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Add at least one page before exporting.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(22.dp))

            ExportCta(
                label = if (isGenerating) "Compiling pages…" else "Generate & share PDF",
                busy = isGenerating,
                enabled = !isGenerating && document.pages.isNotEmpty(),
                onClick = {
                    if (document.pages.isEmpty() || isGenerating) return@ExportCta
                    isGenerating = true
                    val config = PdfExportConfig(
                        paperSize = selectedPaperSize,
                        margins = selectedMargin,
                        includePageNumbers = includePageNumbers,
                        maxImageDimensionPx = settings.resolution.maxImageDimensionPx,
                        pageOrientation = settings.pageOrientation,
                        imageScale = settings.imageScale,
                        fileBaseName = settings.defaultDocumentName
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
                                sharePdf(
                                    context = context,
                                    file = file,
                                    title = document.title,
                                    emailTo = settings.emailTo,
                                    emailSubject = settings.emailSubject
                                )
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
                }
            )
        }
    }
}

/**
 * The sheet's one primary action.
 *
 * Built by hand rather than from [PillButton] because it carries two states —
 * idle with a share glyph, busy with breathing dots — and a pill can only host a
 * static label.
 */
@Composable
private fun ExportCta(
    label: String,
    busy: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactions = remember { MutableInteractionSource() }
    val enabledAlpha by animateFloatAsState(if (enabled) 1f else 0.45f, label = "ctaAlpha")
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = DocShapes.pill,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .alpha(enabledAlpha)
            .pressScale(interactions)
            .clip(DocShapes.pill)
            .clickable(
                enabled = enabled,
                interactionSource = interactions,
                indication = ripple(),
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (busy) {
                LoadingDots(color = MaterialTheme.colorScheme.onPrimary, dotSize = 7.dp)
                Spacer(Modifier.width(10.dp))
            } else {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(9.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
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
private fun sharePdf(
    context: Context,
    file: File,
    title: String,
    emailTo: String,
    emailSubject: String
) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            // Settings → Default Email. Mail clients read these two extras and
            // pre-fill the compose screen; other share targets ignore them.
            putExtra(Intent.EXTRA_SUBJECT, emailSubject.ifBlank { title })
            if (emailTo.isNotBlank()) {
                putExtra(
                    Intent.EXTRA_EMAIL,
                    emailTo.split(',', ';', ' ')
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .toTypedArray()
                )
            }
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
