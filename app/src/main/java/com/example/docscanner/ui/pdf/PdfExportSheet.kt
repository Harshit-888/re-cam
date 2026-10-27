package com.example.docscanner.ui.pdf

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
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
import com.example.docscanner.ui.common.Hairline
import com.example.docscanner.ui.common.InfoPill
import com.example.docscanner.ui.common.LoadingDots
import com.example.docscanner.ui.common.SectionLabel
import com.example.docscanner.util.PdfGenerator
import com.example.docscanner.util.PdfSizeEstimate
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** How long a selection colour takes to settle. Short, because it follows a tap. */
private const val CHOICE_MS = 140

/**
 * The share sheet: everything about the PDF is decided here, in one place.
 *
 * It takes **pages**, not a document, because a merge is just the pages of
 * several documents in a row — the same sheet, the same engine, one file out.
 *
 * ## Why this was rebuilt
 *
 * The previous revision expressed three of its five choices as `SegmentedPill`,
 * which divides the available width evenly between its options. That works for
 * "Borders | Filters" and fails completely here: `PaperSize` has **eight** options
 * whose labels carry their dimensions ("US Letter (8.5 × 11 in)"), so every segment
 * was about 34 dp wide and every label was clipped to a fragment — "No Man",
 * "Compac". `PdfMargin` and `PdfCompression` had the same treatment at four
 * options, which is why the sheet read as a wall of unlabelled bars. Beyond that,
 * six identical uppercase eyebrows over six equally weighted blocks gave the eye
 * nowhere to land: nothing said which choices matter every time (file size,
 * password) and which are set once and forgotten (paper, margins).
 *
 * The sheet is now four weighted sections plus the name, with the two decisions
 * that matter on every export carrying the visual weight:
 *
 * * **Paper** collapses to one row that states the current value and expands into
 *   a two-column grid of real paper sizes — progressive disclosure with the answer
 *   still on screen, instead of eight chips competing with everything else.
 * * **File size** is a 2 × 2 grid of cards, because "smaller file" is the question
 *   people actually have, and the answer to it should be visible without a scroll.
 * * **Password** is a single tappable row that opens a field, with the warning
 *   shown *before* it is needed rather than as red text after an empty field.
 *
 * Nothing about the produced PDF changed: same settings, same defaults, same
 * `PdfGenerator` call. This is the sheet, not the file.
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
    // Collapsed until asked for: eight paper sizes are a one-time decision, and
    // eight chips on arrival is what made this sheet feel like a form.
    var paperExpanded by remember { mutableStateOf(false) }

    val totalPages = pages.size
    val safeName = remember(fileName) {
        PdfGenerator.sanitiseFileName(fileName).take(80).ifBlank { "document" }
    }
    val passwordReady = !protect || password.isNotBlank()
    val canExport = !isGenerating && totalPages > 0 && passwordReady
    // The one line that explains the *chosen* size, instead of four lines that
    // explain all of them at once.
    val compressionSummary = remember(compression) { compressionMeta(compression) }

    // What each level would actually produce, in KB/MB.
    //
    // Measured off disk (page bytes and pixel counts, see [PdfSizeEstimate]) the
    // first time the sheet opens and then left alone: the numbers do not depend on
    // the paper size or the margins, only on the pages and the export resolution.
    // `produceState` keeps that measurement off the composition, so the sheet opens
    // at full speed and the four numbers appear a frame later instead of a scroll
    // later.
    val sizeEstimates by produceState<Map<PdfCompression, Long>>(
        initialValue = emptyMap(),
        pages,
        settings.resolution
    ) {
        value = PdfSizeEstimate.estimate(pages, baseConfig.maxImageDimensionPx)
    }
    val selectedSize = sizeEstimates[compression]?.let(PdfSizeEstimate::format)

    // A swipe-down is a dismissal, and dismissing is what cancels the export: the
    // writing coroutine belongs to this composition's scope, so a sheet that slides
    // away mid-write takes the job with it and can leave a half-written file behind.
    // Blocking the *gesture* is honest — the button says what is happening and the
    // caption asks for the screen to stay open — where silently cancelling is not.
    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = { value ->
            !(isGenerating && value == SheetValue.Hidden)
        }
    )

    ModalBottomSheet(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = DocShapes.sheet
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                // A sheet is measured against the *smallest* screen it runs on, and
                // this one carries a name field and a password field that the
                // keyboard pushes up. `imePadding` keeps the CTA reachable, and the
                // scroll keeps everything reachable on a short phone.
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 18.dp)
        ) {
            ExportHeader(
                title = title,
                totalPages = totalPages,
                mergeSources = mergeSources,
                resolutionLabel = settings.resolution.label,
                protect = protect
            )

            Spacer(Modifier.height(20.dp))

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
                            if (index > 0) Spacer(Modifier.height(8.dp))
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
            SectionLabel("Paper")
            PaperSizeChooser(
                selected = selectedPaperSize,
                expanded = paperExpanded,
                onToggle = { paperExpanded = !paperExpanded },
                onSelect = { size ->
                    selectedPaperSize = size
                    // Picking a size *is* the answer to the question this row asks,
                    // so the grid folds away again. Leaving it open would keep the
                    // rest of the sheet below the fold for no reason.
                    paperExpanded = false
                }
            )

            Spacer(Modifier.height(18.dp))

            // ---- file size ----------------------------------------------------
            // The decision people actually make on every export, so it gets cards
            // rather than a line of chips.
            SectionLabel("File size")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompressionCard(
                        level = PdfCompression.ORIGINAL,
                        selected = compression == PdfCompression.ORIGINAL,
                        sizeLabel = sizeEstimates[PdfCompression.ORIGINAL]?.let(PdfSizeEstimate::format),
                        onClick = { compression = PdfCompression.ORIGINAL },
                        modifier = Modifier.weight(1f)
                    )
                    CompressionCard(
                        level = PdfCompression.BALANCED,
                        selected = compression == PdfCompression.BALANCED,
                        sizeLabel = sizeEstimates[PdfCompression.BALANCED]?.let(PdfSizeEstimate::format),
                        onClick = { compression = PdfCompression.BALANCED },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompressionCard(
                        level = PdfCompression.SMALL,
                        selected = compression == PdfCompression.SMALL,
                        sizeLabel = sizeEstimates[PdfCompression.SMALL]?.let(PdfSizeEstimate::format),
                        onClick = { compression = PdfCompression.SMALL },
                        modifier = Modifier.weight(1f)
                    )
                    CompressionCard(
                        level = PdfCompression.TINY,
                        selected = compression == PdfCompression.TINY,
                        sizeLabel = sizeEstimates[PdfCompression.TINY]?.let(PdfSizeEstimate::format),
                        onClick = { compression = PdfCompression.TINY },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                // The chosen level's *total*, next to the line that explains it:
                // "12 pages ≈ 24.6 MB" answers the question the section asks, and it
                // changes the moment the choice does.
                if (selectedSize == null) {
                    compressionSummary
                } else {
                    val unit = if (totalPages == 1) "page" else "pages"
                    "$totalPages $unit ≈ $selectedSize · $compressionSummary"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(Modifier.height(18.dp))

            // ---- layout -------------------------------------------------------
            SectionLabel("Layout")
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = DocShapes.cardSmall,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    // Margins as a 2 × 2 of equal chips. Four equal *segments* was
                    // the original mistake ("No Man…", "Compac…"); one row of
                    // natural-width chips does not fit four labels on a 360 dp phone
                    // either — it leaves a single orphan chip on a second line, which
                    // reads as a mistake. A deliberate 2 × 2 is stable at every font
                    // scale, and matches the grid the paper sizes use.
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PdfMargin.entries.chunked(2).forEach { pair ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                pair.forEach { margin ->
                                    ChoiceChip(
                                        label = margin.shortLabel,
                                        selected = margin == selectedMargin,
                                        onClick = { selectedMargin = margin },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Hairline()
                    ToggleRow(
                        title = "Footer page numbers",
                        subtitle = "1 / $totalPages at the bottom centre",
                        checked = includePageNumbers,
                        onToggle = { includePageNumbers = !includePageNumbers }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // ---- password -----------------------------------------------------
            SectionLabel("Password")
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = DocShapes.cardSmall,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    ToggleRow(
                        title = "Require a password",
                        subtitle = if (protect) {
                            "Needed to open the PDF. There is no recovery."
                        } else {
                            "Anyone with the file can open it"
                        },
                        checked = protect,
                        onToggle = {
                            protect = !protect
                            if (!protect) {
                                password = ""
                                passwordVisible = false
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = protect,
                        enter = fadeIn(tween(CHOICE_MS)) + expandVertically(tween(CHOICE_MS)),
                        exit = fadeOut(tween(CHOICE_MS)) + shrinkVertically(tween(CHOICE_MS))
                    ) {
                        Column(Modifier.padding(bottom = 12.dp)) {
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
                                when {
                                    password.isBlank() -> "Enter the password to continue."
                                    password.length < 4 -> "Short. Four characters or more is worth it."
                                    else -> "Letters, numbers and symbols."
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

            Spacer(Modifier.height(20.dp))

            ExportCta(
                label = when {
                    isGenerating && totalPages > 4 -> "Encoding $totalPages pages…"
                    isGenerating -> "Compiling pages…"
                    mergeSources.size > 1 -> "Merge & share PDF"
                    else -> "Generate & share PDF"
                },
                // What the button will actually do, in the user's words, under the
                // button: the sheet's last line before it commits.
                caption = when {
                    isGenerating -> "Keep this screen open until it finishes"
                    totalPages == 0 -> "Nothing to export yet"
                    !passwordReady -> "Add a password to continue"
                    mergeSources.size > 1 -> "$totalPages pages from ${mergeSources.size} documents"
                    else -> "$totalPages pages · ${selectedPaperSize.shortLabel} · " +
                        compression.label
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

            Spacer(Modifier.height(10.dp))
            Text(
                // Both engines are named because they differ: a passphrase needs the
                // app's writer, and it is the only thing that changes the output.
                buildString {
                    append(settings.pageOrientation.label)
                    append(" · ")
                    append(settings.imageScale.label)
                    append(" · written by ")
                    // The platform writer can neither encrypt nor compress, so a
                    // password *or* any lossy level switches engines. Say which one
                    // is about to write the file.
                    append(
                        if (protect || !compression.isLossless) {
                            "the app's PDF writer"
                        } else {
                            "the system PDF engine"
                        }
                    )
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------- header

/**
 * Icon, title and the three facts that qualify it.
 *
 * The one thing that changes here as the user works is the lock chip, so the
 * header doubles as a status line: turn protection on and it says so, above
 * everything else.
 */
@Composable
private fun ExportHeader(
    title: String,
    totalPages: Int,
    mergeSources: List<Document>,
    resolutionLabel: String,
    protect: Boolean
) {
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
                    InfoPill(resolutionLabel)
                }
                if (protect) {
                    InfoPill(
                        "Locked",
                        icon = Icons.Default.Lock,
                        container = MaterialTheme.colorScheme.primaryContainer,
                        content = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

// --------------------------------------------------------------- paper chooser

/**
 * The current paper on one row, the other seven behind it.
 *
 * A collapsed row is not a hidden control — it states the value it holds and
 * looks tappable (a chevron, a container, haptic-free ripple) — so a user who
 * never opens it still knows which size they are getting.
 */
@Composable
private fun PaperSizeChooser(
    selected: PaperSize,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSelect: (PaperSize) -> Unit
) {
    val interactions = remember { MutableInteractionSource() }
    val chevron by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(180),
        label = "paperChevron"
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = DocShapes.cardSmall,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(DocShapes.cardSmall)
                    .clickable(
                        interactionSource = interactions,
                        indication = ripple(),
                        // It opens a list, it does not toggle anything: a button that
                        // says what it does is what a screen reader needs here.
                        onClickLabel = if (expanded) "Hide paper sizes" else "Choose paper size",
                        onClick = onToggle
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        selected.shortLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        selected.dimensionsLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    if (expanded) "Done" else "Change",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Default.ExpandMore,
                    // Decorative: the row is one clickable, and its `onClickLabel`
                    // already says what a tap does. A description here would be
                    // announced twice.
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(chevron)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(CHOICE_MS)) + expandVertically(tween(CHOICE_MS)),
                exit = fadeOut(tween(CHOICE_MS)) + shrinkVertically(tween(CHOICE_MS))
            ) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Two columns: a paper name plus its dimensions does not fit
                    // three-up on a small phone, and a wrapped grid reads worse than
                    // a taller one. Sixteen presets need headings, or the list is a
                    // wall of cards — the groups are the original's own
                    // (`paper_size_group` in its `app.db`).
                    PaperSize.byGroup.forEach { (group, sizes) ->
                        Text(
                            group.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                        sizes.chunked(2).forEach { pair ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                pair.forEach { size ->
                                    PaperOption(
                                        size = size,
                                        selected = size == selected,
                                        onClick = { onSelect(size) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // Keeps a lone last card the same width as the rest
                                // instead of stretching it across the row.
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaperOption(
    size: PaperSize,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactions = remember { MutableInteractionSource() }
    val container by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        animationSpec = tween(CHOICE_MS),
        label = "paperContainer"
    )
    val content = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Column(
        modifier = modifier
            .height(54.dp)
            .pressScale(interactions)
            .clip(DocShapes.thumbnail)
            .background(container)
            // `selectable`, not `clickable`: it publishes *which* option is chosen to
            // accessibility services, so TalkBack says "A4, selected" instead of
            // reading four identical buttons. The colour alone is the only other
            // signal, and colour is not one.
            .selectable(
                selected = selected,
                interactionSource = interactions,
                indication = ripple(),
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                size.shortLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (selected) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Text(
            size.dimensionsLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) content.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ------------------------------------------------------------- size / chips

/**
 * One compression level as a card you can compare at a glance.
 *
 * Top to bottom: the promise, **the number**, then the detail. "Balanced" alone
 * never answered the only question the section asks — how big is the file going to
 * be — so the estimate gets its own line, in the accent colour and one step above
 * the detail text in weight. It is the row people scan; the words are what they
 * read once they have picked.
 *
 * The selected card is the only one with a filled container and a check, so the
 * current answer is unmistakable in a grid of four.
 */
@Composable
private fun CompressionCard(
    level: PdfCompression,
    selected: Boolean,
    /** "2.4 MB" once the estimate is in; `null` while it is still being measured. */
    sizeLabel: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactions = remember { MutableInteractionSource() }
    val container by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(CHOICE_MS),
        label = "compressionContainer"
    )
    val content = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Column(
        modifier = modifier
            // Three lines now: label, estimate, up to two lines of detail. A
            // *minimum* rather than a fixed height, because the estimate line and
            // the detail wrap differently at a 1.3x font scale on a 320 dp phone —
            // and a clipped third line is exactly the failure the previous revision
            // shipped. The row still puts both cards at the same height in practice:
            // they carry the same structure.
            .heightIn(min = 88.dp)
            .pressScale(interactions)
            .clip(DocShapes.cardSmall)
            .background(container)
            .selectable(
                selected = selected,
                interactionSource = interactions,
                indication = ripple(),
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                level.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = content,
                maxLines = 1,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (selected) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        // The estimate, on its own line.
        //
        // A non-breaking space is reserved while the measurement is in flight so the
        // four cards do not grow a line under the user's finger when the numbers
        // arrive a frame later. `≈` because it is an estimate: the number comes from
        // measuring the stored pages, not from encoding them.
        Text(
            if (sizeLabel == null) "\u00A0" else "≈ $sizeLabel",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) content.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary,
            maxLines = 1,
            softWrap = false
        )
        Spacer(Modifier.height(2.dp))
        Text(
            level.detail,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) content.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** A pill that states a choice, sized to its own text. */
@Composable
private fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactions = remember { MutableInteractionSource() }
    val container by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        animationSpec = tween(CHOICE_MS),
        label = "chipContainer"
    )
    val content = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = modifier
            // 40 dp tall: a comfortable target for a two-word label, and tall enough
            // that the ripple is not the only feedback.
            .height(40.dp)
            .pressScale(interactions)
            .clip(DocShapes.pill)
            .background(container)
            .selectable(
                selected = selected,
                interactionSource = interactions,
                indication = ripple(),
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = content,
            maxLines = 1
        )
    }
}

/**
 * A whole row that is a switch.
 *
 * The switch is not the only target: tapping the title, the subtitle or the space
 * between them flips it, because a 52 dp switch on the right edge is a small thing
 * to aim at with a thumb, and this row is 300 dp wide. The switch is drawn with
 * `onCheckedChange = null` so the row owns the interaction and a tap on the switch
 * is not handled twice.
 */
@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DocShapes.thumbnail)
            // `toggleable` announces "on/off" and the Switch role; a plain clickable
            // row reads as an unnamed button that happens to contain a switch.
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = { onToggle() }
            )
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = null)
    }
}

// ------------------------------------------------------------------- helpers

/** "US Letter (8.5 × 11 in)" → "US Letter" and "8.5 × 11 in". */
private val PaperSize.shortLabel: String
    get() = label.substringBefore('(').trim()

private val PaperSize.dimensionsLabel: String
    get() = label.substringAfter('(', missingDelimiterValue = "").trimEnd(')').trim()

/**
 * "Compact (10 pt)" → "Compact".
 *
 * The point value is a real setting, but it is not what anyone is choosing
 * between; the chip says the word and the row's own state says which is on.
 */
private val PdfMargin.shortLabel: String
    get() = label.substringBefore('(').trim()

/** What the chosen level actually does, in one line, from the enum's own numbers. */
private fun compressionMeta(level: PdfCompression): String {
    val pixels = (level.pixelScale * 100).roundToInt()
    val resolution = if (pixels >= 100) "every pixel" else "$pixels% of the pixels"
    return if (level.isLossless) {
        "Lossless PNG page images · $resolution · largest file"
    } else {
        "JPEG quality ${level.jpegQuality} · $resolution · smaller file"
    }
}

/**
 * The sheet's one primary action.
 *
 * Built by hand rather than from `PillButton` because it carries two states — idle
 * with a share glyph, busy with breathing dots — plus a caption that says what the
 * tap will produce. A pill can only host a static label.
 */
@Composable
private fun ExportCta(
    label: String,
    caption: String,
    busy: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactions = remember { MutableInteractionSource() }
    val enabledAlpha by animateFloatAsState(if (enabled) 1f else 0.45f, label = "ctaAlpha")
    Column {
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
        Spacer(Modifier.height(8.dp))
        Text(
            caption,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled || busy) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.error
            },
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
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
