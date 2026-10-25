package com.example.docscanner.ui.pdf

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.docscanner.model.AppSettings
import com.example.docscanner.model.Document
import com.example.docscanner.model.PaperSize
import com.example.docscanner.model.PdfCompression
import com.example.docscanner.model.PdfExportConfig
import com.example.docscanner.model.PdfMargin
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.theme.DocShapes
import com.example.docscanner.theme.pressScale
import com.example.docscanner.ui.common.InfoPill
import com.example.docscanner.ui.common.LoadingDots
import com.example.docscanner.ui.common.SectionLabel
import com.example.docscanner.ui.common.SegmentedPill
import com.example.docscanner.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * The share sheet: everything about the PDF is decided here, in one place.
 *
 * It takes **pages**, not a document, because a merge is just the pages of
 * several documents in a row — the same sheet, the same engine, one file out.
 *
 * @param title what the file is called before the user renames it, and the PDF's
 *   metadata title.
 * @param pages pages in the order they will be written.
 * @param mergeSources the documents being merged, when this is a merge. Shown as
 *   the order preview, and the reason the eyebrow says MERGE.
 * @param defaultCompression merges default to [PdfCompression.BALANCED]: ten
 *   documents at lossless page images is a hundred-megabyte attachment, which is
 *   the opposite of the thing the user asked for. The control is right there and
 *   is never overridden behind their back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfExportSheet(
    title: String,
    pages: List<ScannedPage>,
    settings: AppSettings,
    onDismiss: () -> Unit,
    mergeSources: List<Document> = emptyList(),
    defaultCompression: PdfCompression = PdfCompression.ORIGINAL
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Settings → PDF supplies the defaults; the controls below are a per-export
    // override that starts from them and is deliberately not persisted.
    val baseConfig = remember(settings) {
        PdfExportConfig(
            paperSize = settings.pageSize,
            margins = PdfMargin.STANDARD,
            includePageNumbers = true,
            maxImageDimensionPx = settings.resolution.maxImageDimensionPx,
            pageOrientation = settings.pageOrientation,
            imageScale = settings.imageScale,
            fileBaseName = settings.defaultDocumentName
        )
    }

    var selectedPaperSize by remember { mutableStateOf(settings.pageSize) }
    var selectedMargin by remember { mutableStateOf(PdfMargin.STANDARD) }
    var includePageNumbers by remember { mutableStateOf(true) }
    var compression by remember { mutableStateOf(defaultCompression) }
    var fileName by remember { mutableStateOf(PdfGenerator.suggestedName(title, baseConfig)) }
    var protect by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }

    val totalPages = pages.size
    val safeName = remember(fileName) {
        PdfGenerator.sanitiseFileName(fileName).take(80).ifBlank { "document" }
    }
    val passwordReady = !protect || password.isNotBlank()
    val canExport = !isGenerating && totalPages > 0 && passwordReady

    ModalBottomSheet(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = DocShapes.sheet
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                // A sheet is measured against the *smallest* screen it runs on, and
                // this one now carries a name field and a password field that the
                // keyboard pushes up. `imePadding` keeps the CTA reachable, and the
                // scroll keeps everything reachable on a short phone.
                .imePadding()
                .verticalScroll(rememberScrollState())
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
                        if (mergeSources.isEmpty()) Icons.Default.PictureAsPdf else Icons.Default.CallMerge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (mergeSources.isEmpty()) "EXPORT" else "MERGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        InfoPill(
                            "$totalPages ${if (totalPages == 1) "page" else "pages"}",
                            icon = Icons.Default.Description
                        )
                        if (mergeSources.size > 1) {
                            InfoPill("${mergeSources.size} documents")
                        } else {
                            InfoPill(settings.resolution.label)
                        }
                        if (protect) {
                            InfoPill("Protected", icon = Icons.Default.Lock)
                        }
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // ---- name ---------------------------------------------------------
            SectionLabel("File name")
            OutlinedTextField(
                value = fileName,
                onValueChange = { fileName = it.take(80) },
                singleLine = true,
                shape = DocShapes.field,
                textStyle = MaterialTheme.typography.bodyMedium,
                trailingIcon = {
                    Text(
                        ".pdf",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                // What the file will actually be called: the field is sanitised, and
                // a share target shows exactly this. Better to see it here than to
                // wonder why the name lost its spaces.
                "Saved as $safeName.pdf",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp)
            )

            if (mergeSources.size > 1) {
                Spacer(Modifier.height(18.dp))
                SectionLabel("Merged in this order")
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = DocShapes.cardSmall,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        mergeSources.forEachIndexed { index, document ->
                            if (index > 0) {
                                Spacer(Modifier.height(8.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(22.dp)
                                )
                                Text(
                                    document.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "${document.pages.size} pp",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

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

            // ---- file size ----------------------------------------------------
            SectionLabel("File size")
            SegmentedPill(
                options = PdfCompression.entries.map { it.label },
                selectedIndex = PdfCompression.entries.indexOf(compression),
                onSelect = { compression = PdfCompression.entries[it] },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                compression.detail,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
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
                            "1 / $totalPages at the bottom centre",
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

            Spacer(Modifier.height(18.dp))

            // ---- password -----------------------------------------------------
            SectionLabel("Password")
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = DocShapes.cardSmall,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Require a password",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "The PDF asks for it when it is opened",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = protect,
                            onCheckedChange = { enabled ->
                                protect = enabled
                                if (!enabled) {
                                    password = ""
                                    passwordVisible = false
                                }
                            }
                        )
                    }

                    AnimatedVisibility(visible = protect) {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { typed ->
                                    // Printable ASCII only: revision-3 passwords are
                                    // compared as raw bytes, and the sheet must not
                                    // accept a character the crypto cannot represent.
                                    password = typed.filter { it.code in 32..126 }.take(64)
                                },
                                singleLine = true,
                                label = { Text("Password") },
                                shape = DocShapes.field,
                                textStyle = MaterialTheme.typography.bodyMedium,
                                visualTransformation = if (passwordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) {
                                                Icons.Default.VisibilityOff
                                            } else {
                                                Icons.Default.Visibility
                                            },
                                            contentDescription = if (passwordVisible) {
                                                "Hide password"
                                            } else {
                                                "Show password"
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                if (password.isBlank()) {
                                    "Enter the password to continue."
                                } else if (password.length < 4) {
                                    "Short. Four characters or more is worth it."
                                } else {
                                    "Letters, numbers and symbols. There is no way to recover it."
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (password.isBlank()) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            if (totalPages == 0) {
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
                label = when {
                    isGenerating && totalPages > 4 -> "Encoding $totalPages pages…"
                    isGenerating -> "Compiling pages…"
                    mergeSources.size > 1 -> "Merge & share PDF"
                    else -> "Generate & share PDF"
                },
                busy = isGenerating,
                enabled = canExport,
                onClick = {
                    if (!canExport) return@ExportCta
                    isGenerating = true
                    val config = baseConfig.copy(
                        paperSize = selectedPaperSize,
                        margins = selectedMargin,
                        includePageNumbers = includePageNumbers,
                        compression = compression,
                        fileName = fileName,
                        password = if (protect) password else ""
                    )
                    scope.launch {
                        val result = runCatching {
                            withContext(Dispatchers.IO) {
                                PdfGenerator.generatePdf(
                                    context = context,
                                    title = title,
                                    pages = pages,
                                    config = config
                                )
                            }
                        }
                        isGenerating = false
                        result
                            .onSuccess { file ->
                                sharePdf(
                                    context = context,
                                    file = file,
                                    title = file.nameWithoutExtension,
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

            if (compression.isLossless && !protect) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Lossless pages, written by the system PDF engine.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/**
 * The sheet's one primary action.
 *
 * Built by hand rather than from `PillButton` because it carries two states —
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
        val sizeMb = file.length() / (1024.0 * 1024.0)
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
        // The size is the one number the compression control is about, and it can
        // only be measured, never estimated: say what was actually written.
        Toast.makeText(
            context,
            String.format(Locale.US, "%s · %.1f MB", file.name, sizeMb),
            Toast.LENGTH_SHORT
        ).show()
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "PDF saved to ${file.absolutePath}\nSharing unavailable: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}
