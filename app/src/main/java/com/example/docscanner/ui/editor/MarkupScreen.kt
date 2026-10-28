package com.example.docscanner.ui.editor

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.model.MarkupHistory
import com.example.docscanner.model.MarkupPoint
import com.example.docscanner.model.MarkupStroke
import com.example.docscanner.model.MarkupTool
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.theme.DocShapes
import com.example.docscanner.ui.common.TapTarget
import com.example.docscanner.util.ImageStore
import com.example.docscanner.util.MarkupRenderer
import com.example.docscanner.util.PageRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Draws on a page: pen, highlighter and whiteout, with undo and redo.
 *
 * ## Why this is a screen and not a dialog
 *
 * It used to be a `Dialog` — a rounded card over the document, with the page squeezed
 * into `fillMaxWidth(0.96f)` and a column of chips underneath it. Annotating a page is
 * not a confirmation; it is the most precise gesture in the app, and it was being done
 * in the smallest box on the screen with the rest of the app still visibly behind it.
 * As a route it gets the whole display, the platform's app bar, and back navigation
 * that means what back always means.
 *
 * Everything that made the dialog work is unchanged: the strokes come back as
 * **data** (`ScannedPage.markup`), points are stored normalised, and every bake replays
 * them — which is what lets a filter change later without erasing the ink.
 */
@Composable
fun MarkupScreen(
    docId: String,
    pageId: String,
    repository: ScannerRepository,
    onClose: () -> Unit
) {
    val documents by repository.documents.collectAsState()
    val page = remember(documents, docId, pageId) {
        documents.find { it.id == docId }?.pages?.find { it.id == pageId }
    }

    if (page == null) {
        // One frame, while the document loads (or if the page was deleted while the
        // route was being pushed).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        }
        return
    }

    // The editor below assumes a page that exists: the stroke history is seeded from
    // it exactly once, so it must never be constructed from a null page.
    MarkupEditor(
        docId = docId,
        page = page,
        repository = repository,
        onClose = onClose
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarkupEditor(
    docId: String,
    page: ScannedPage,
    repository: ScannerRepository,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pageId = page.id

    var bitmap by remember(pageId) { mutableStateOf<Bitmap?>(null) }
    var failed by remember(pageId) { mutableStateOf(false) }
    var isSaving by remember(pageId) { mutableStateOf(false) }

    LaunchedEffect(pageId, page.imageUri, page.revision) {
        failed = false
        bitmap = withContext(Dispatchers.IO) {
            ImageStore.loadBitmap(context, page.imageUri, PREVIEW_DIMENSION)
        }
        failed = bitmap == null
    }

    val history = remember(pageId) { MarkupHistory(page.markup) }
    // Bumped on every history change so the canvas and the button states recompose.
    // The history itself is a plain class, deliberately: it is model code and is
    // unit-tested without Compose.
    var revision by remember(pageId) { mutableStateOf(0) }
    val current = remember(pageId) { mutableStateListOf<Offset>() }

    var tool by remember(pageId) { mutableStateOf(MarkupTool.PEN) }
    var colorArgb by remember(pageId) { mutableStateOf(MarkupInkColors.first()) }
    var widthFraction by remember(pageId) { mutableStateOf(MarkupWidths[1]) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val strokes = remember(revision) { history.strokes }
    val canUndo = remember(revision) { history.canUndo }
    val canRedo = remember(revision) { history.canRedo }

    fun commitStroke() {
        if (current.size < 2 || canvasSize.width <= 0 || canvasSize.height <= 0) {
            current.clear()
            return
        }
        history.add(
            MarkupStroke(
                tool = tool,
                colorArgb = colorArgb,
                widthFraction = widthFraction,
                points = current.map {
                    MarkupPoint(
                        x = (it.x / canvasSize.width).coerceIn(0f, 1f),
                        y = (it.y / canvasSize.height).coerceIn(0f, 1f)
                    )
                }
            )
        )
        current.clear()
        revision++
    }

    fun save() {
        if (isSaving) return
        isSaving = true
        val target = page
        scope.launch {
            repository.updatePage(docId, target.id) { it.copy(markup = history.strokes) }
            // Re-read after the write: the page may have been rotated or re-cropped
            // while this screen was open, and baking a stale copy would undo it.
            val latest = repository.getDocumentById(docId)
                ?.pages?.find { it.id == target.id }
            if (latest != null) {
                val baked = runCatching { PageRenderer.bake(context, latest) }.getOrNull()
                if (baked != null) repository.updatePage(docId, target.id) { baked }
            }
            isSaving = false
            onClose()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Annotate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Draw on page ${page.pageIndex + 1}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose, enabled = !isSaving) {
                        Icon(Icons.Default.Close, contentDescription = "Close without saving")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { history.undo(); revision++ },
                        enabled = canUndo && !isSaving
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo")
                    }
                    IconButton(
                        onClick = { history.redo(); revision++ },
                        enabled = canRedo && !isSaving
                    ) {
                        Icon(Icons.Default.Redo, contentDescription = "Redo")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = DocShapes.sheet,
                shadowElevation = 14.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // Material 3's segmented button: three exclusive tools, one
                    // connected control, the system's own selected indicator.
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        MarkupTool.entries.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = tool == option,
                                onClick = { tool = option },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = MarkupTool.entries.size
                                ),
                                label = {
                                    Text(
                                        option.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1
                                    )
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    // ## The colour row is always there — it is not a child of the tool
                    //
                    // This block used to be wrapped in `if (tool != MarkupTool.WHITEOUT)`.
                    // Choosing **Erase** therefore *deleted* the four colour buttons from
                    // the bar, and they only came back once another tool was chosen: a
                    // whole row of controls removed by a different control, which is the
                    // "the annotate buttons disappear when I tap another button" report in
                    // its most literal form. It also changed the bar's height, so switching
                    // to the eraser reflowed the page above it.
                    //
                    // Nothing needs it gone. `MarkupRenderer` paints a whiteout as the
                    // paper colour and ignores `colorArgb` completely (see
                    // `MarkupRenderer.paintFor`), so a colour picked while erasing is
                    // simply the colour the next pen or marker stroke uses — which is
                    // exactly how the reader's annotate shelf already behaves. A control
                    // that is present and harmless beats a control that vanishes.
                    //
                    // The visible dot is 30 dp; the *target* is 48 dp, because a
                    // colour picker you miss by 6 dp is a colour you did not choose.
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MarkupInkColors.forEach { ink ->
                            TapTarget(
                                onClick = { colorArgb = ink },
                                label = inkLabel(ink),
                                selected = ink == colorArgb
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(ink))
                                        .border(
                                            width = if (ink == colorArgb) 3.dp else 1.dp,
                                            color = if (ink == colorArgb) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outlineVariant
                                            },
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // A second row of the same component — `SegmentedButton` only
                        // exists inside a segmented-button row scope, which is also
                        // what gives it the connected corners.
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                            MarkupWidths.forEachIndexed { index, fraction ->
                                SegmentedButton(
                                    selected = widthFraction == fraction,
                                    onClick = { widthFraction = fraction },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = MarkupWidths.size
                                    ),
                                    label = {
                                        Text(
                                            MarkupWidthLabels[index],
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1
                                        )
                                    }
                                )
                            }
                        }
                        TextButton(
                            onClick = { history.clear(); revision++ },
                            enabled = canUndo && !isSaving
                        ) { Text("Clear") }
                    }

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = { save() },
                        enabled = !isSaving,
                        shape = DocShapes.pill,
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Saving page…")
                        } else {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Save annotation", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
            contentAlignment = Alignment.Center
        ) {
            // The page, with the ink drawn over it at preview resolution. It fills the
            // space it is given rather than being locked to a dialog's width — that is
            // the entire reason this is a screen.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(
                        bitmap?.let { it.width.toFloat() / it.height.toFloat() } ?: A4_ASPECT
                    )
                    .background(Color.White)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        DocShapes.cardSmall
                    )
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(tool, colorArgb, widthFraction) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                current.clear()
                                current.add(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                current.add(change.position)
                            },
                            onDragEnd = { commitStroke() },
                            onDragCancel = { commitStroke() }
                        )
                    }
            ) {
                val image = bitmap
                if (image == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (failed) {
                            Text(
                                "Could not open this page",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                    }
                } else {
                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        fun paint(strokes: List<MarkupStroke>) {
                            // The same renderer, the same maths, the same curve as the
                            // bake: this is the preview's whole guarantee.
                            drawIntoCanvas { canvas ->
                                MarkupRenderer.draw(
                                    canvas = canvas.nativeCanvas,
                                    strokes = strokes,
                                    width = size.width.toInt(),
                                    height = size.height.toInt()
                                )
                            }
                        }

                        paint(strokes)

                        if (current.size >= 2) {
                            paint(
                                listOf(
                                    MarkupStroke(
                                        tool = tool,
                                        colorArgb = colorArgb,
                                        widthFraction = widthFraction,
                                        points = current.map {
                                            MarkupPoint(
                                                x = if (size.width > 0) it.x / size.width else 0f,
                                                y = if (size.height > 0) it.y / size.height else 0f
                                            )
                                        }
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Preview decode size for the full-screen editor: the page is drawn a phone wide at most. */
private const val PREVIEW_DIMENSION = 1400
