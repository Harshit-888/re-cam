package com.example.docscanner.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import com.example.docscanner.model.AppSettings
import com.example.docscanner.model.Document
import com.example.docscanner.model.FilterType
import com.example.docscanner.model.Folder
import com.example.docscanner.model.LibrarySnapshot
import com.example.docscanner.model.QuadCorners
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.util.BackupArchive
import com.example.docscanner.util.CardComposition
import com.example.docscanner.util.DocumentDetector
import com.example.docscanner.util.ImageStore
import com.example.docscanner.util.PageImageLoader
import com.example.docscanner.util.OcrEngine
import com.example.docscanner.util.StartupWork
import com.example.docscanner.util.ThumbnailStore
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Single source of truth for documents and folders.
 *
 * UDF contract: every mutation goes through a method here, updates a
 * [MutableStateFlow], and is persisted by [LibraryStore] as a side effect.
 * Screens never mutate models directly.
 *
 * **No global singleton.** Instances are created by `MainActivity` with the
 * application context and passed down; see `AGENTS.md` §2.6.
 *
 * The constructor reads `library.json` synchronously — a few KB on an empty library,
 * and tens to hundreds of KB once pages carry recognised text. That is deliberate: the
 * first composition then sees the restored library instead of flashing an empty state.
 * It runs on `Dispatchers.IO` (see `LibraryViewModel`) and is timed, so its cost is
 * visible as `library.json read: … in X ms` on the `DocScannerPerf` tag rather than
 * being guessed at.
 */
/** How many pages one background text-recognition pass may handle. */
private const val OCR_BACKFILL_BATCH = 24

/**
 * How long a paused backfill waits before asking again whether it may run.
 *
 * Text recognition is the most CPU-hungry thing this app does in the background
 * (an ML Kit inference per page, plus a 2400 px decode). Letting it run while the
 * user flings the library is the difference between a smooth list and a list that
 * "feels laggy even though nothing changed" — the frames are dropped by contention,
 * not by the dashboard.
 */
private const val OCR_PAUSE_POLL_MS = 250L

/**
 * How long the app must be untouched before the orphan sweep runs.
 *
 * Page files that no document points at — a discarded scan, an import that failed
 * halfway, a capture interrupted by a crash — otherwise stay on disk forever at
 * one to three megabytes each. The sweep is a directory listing plus a handful of
 * `delete` calls, so it is not the expensive part of start-up on its own; what
 * made it one was *when* it ran. A flat 5 s timer put a recursive listing and a
 * batch of unlinks on the same IO threads as the cover decodes and the text
 * backfill, in the same second, every launch. It now waits for a real pause
 * (`StartupWork`), which on a library someone is actively scrolling can be a
 * while — the files are not going anywhere.
 */
private const val ORPHAN_SWEEP_IDLE_MS = 6_000L

/** A4 at 200 dpi: readable when printed, cheap to compose. */
private const val A4_PX_WIDTH = 1654
private const val A4_PX_HEIGHT = 2339

/** Each card is scaled into a half-page cell, so decoding larger is waste. */
private const val CARD_SOURCE_DIMENSION = 1600

/**
 * Decode size for the automatic border pass.
 *
 * `DocumentDetector` reduces whatever it is given to 360 px wide before it looks at
 * it, so a 900 px decode is already four times more than the geometry can use —
 * and the extra is not free: a page is 1–3 MB on disk and this pass runs over a
 * whole import at once. Large enough that the border survives the downsample with
 * a few pixels of ramp, small enough that ten pages do not cost ten full decodes.
 */
private const val AUTO_BORDER_DETECT_DIMENSION = 900

class ScannerRepository(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val store = LibraryStore(appContext, scope)
    private val settingsStore = SettingsStore(appContext, scope)

    private val restored: LibrarySnapshot? = loadLibrary()

    private val _settings = MutableStateFlow(settingsStore.load() ?: AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _folders = MutableStateFlow(
        restored?.folders?.takeIf { it.isNotEmpty() } ?: defaultFolders()
    )
    val folders: StateFlow<List<Folder>> = _folders.asStateFlow()

    private val _documents = MutableStateFlow(restored?.documents ?: emptyList())
    val documents: StateFlow<List<Document>> = _documents.asStateFlow()

    init {
        // Settings persist separately from the library: a theme toggle must not
        // rewrite every document, and the theme is needed before the first frame.
        scope.launch {
            _settings.collect { settingsStore.schedule(it) }
        }

        // Persist every subsequent change.
        //
        // `combine` emits once immediately, which is what normalises a fresh
        // install's default folders to disk. On every *other* launch that first
        // emission is a snapshot identical to the file that was just read, and
        // writing it back means serialising the whole library — every page's OCR
        // text included — moments after start-up, on the IO threads the cover
        // decodes are queued behind. So the first emission is only written when
        // there was nothing to read.
        val restoredSnapshot = restored
        var firstEmission = true
        scope.launch {
            combine(_documents, _folders) { documents, folders ->
                LibrarySnapshot(
                    schemaVersion = LibraryStore.CURRENT_SCHEMA_VERSION,
                    folders = folders,
                    documents = documents
                )
            }.collect { snapshot ->
                if (firstEmission) {
                    firstEmission = false
                    if (restoredSnapshot == null) store.schedule(snapshot)
                } else {
                    store.schedule(snapshot)
                }
            }
        }

        // Delete page files nothing references. Late, off the main thread, and
        // after the restored library is known: see ORPHAN_SWEEP_IDLE_MS.
        scope.launch {
            StartupWork.runWhenQuiet(ORPHAN_SWEEP_IDLE_MS, "orphan-sweep") {
                val startedAt = System.nanoTime()
                val removed = ImageStore.sweepOrphans(appContext, referencedPageFiles())
                val ms = (System.nanoTime() - startedAt) / 1_000_000
                StartupWork.trace("orphan-sweep removed $removed file(s) in $ms ms")
            }
        }
    }

    /**
     * Reads `library.json`, timed.
     *
     * The read is blocking and happens before the first composition, so its cost is
     * exactly the cost of the launch screen: on a library with a thousand pages the
     * JSON carries a page entry and an OCR string each, and the parse is the one
     * piece of start-up that cannot be deferred. Logged so that a "the app takes ten
     * seconds to open" report can be answered with a number rather than a guess:
     *
     *     adb logcat -s DocScannerPerf
     */
    private fun loadLibrary(): LibrarySnapshot? {
        val startedAt = System.nanoTime()
        val snapshot = store.load()
        val ms = (System.nanoTime() - startedAt) / 1_000_000
        val pages = snapshot?.documents?.sumOf { it.pages.size } ?: 0
        StartupWork.trace(
            "library.json read: ${snapshot?.documents?.size ?: 0} doc(s), $pages page(s) in $ms ms"
        )
        return snapshot
    }

    // ------------------------------------------------------------- reads

    fun getDocumentById(id: String): Document? = _documents.value.find { it.id == id }

    /** Current settings snapshot, for call sites outside composition. */
    fun currentSettings(): AppSettings = _settings.value

    // ---------------------------------------------------------- settings

    /**
     * Applies [transform] to the current settings and persists the result.
     *
     * The single write path for preferences; screens never hold their own copy,
     * so a change made in Settings is visible to the camera, the editor and the
     * PDF sheet the moment it happens.
     */
    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        _settings.update(transform)
    }

    // --------------------------------------------------------- documents

    /** Adds a document. Page ids are assigned here; caller-supplied ones are replaced. */
    fun addDocument(title: String, folderId: String? = null, pages: List<ScannedPage>): Document {
        val now = System.currentTimeMillis()
        val docId = UUID.randomUUID().toString()
        val formattedPages = pages.mapIndexed { index, page ->
            page.copy(documentId = docId, pageIndex = index)
        }
        val newDoc = Document(
            id = docId,
            title = title.ifBlank { "Scan ${now % 10000}" },
            createdDate = now,
            modifiedDate = now,
            folderId = folderId,
            pages = formattedPages
        )
        _documents.update { listOf(newDoc) + it }
        return newDoc
    }

    fun updateDocumentTitle(id: String, newTitle: String) {
        _documents.update { docs ->
            docs.map {
                if (it.id == id) {
                    it.copy(title = newTitle, modifiedDate = System.currentTimeMillis())
                } else {
                    it
                }
            }
        }
    }

    fun moveDocumentToFolder(docId: String, folderId: String?) {
        _documents.update { docs ->
            docs.map {
                if (it.id == docId) {
                    it.copy(folderId = folderId, modifiedDate = System.currentTimeMillis())
                } else {
                    it
                }
            }
        }
    }

    fun setDocumentNotes(docId: String, notes: String) {
        _documents.update { docs ->
            docs.map {
                if (it.id == docId) {
                    it.copy(notes = notes, modifiedDate = System.currentTimeMillis())
                } else {
                    it
                }
            }
        }
    }

    // ------------------------------------------------------------- pages

    /**
     * Rewrites one page of one document.
     *
     * **`modifiedDate` moves only when the page actually changed.** It used to move on
     * every call, and the library is sorted by it (newest first, the default), so any
     * code path that re-supplied values that were already stored moved the document to
     * the top of the home screen. There are such paths by design: the crop editor
     * persists the geometry of the page it is leaving on every page switch, so walking
     * through a document's pages — looking, changing nothing — re-sorted the library
     * after every tap. "Recently modified" has to mean modified.
     */
    fun updatePage(docId: String, pageId: String, update: (ScannedPage) -> ScannedPage) {
        _documents.update { docs ->
            docs.map { doc ->
                if (doc.id != docId) return@map doc
                var changed = false
                val pages = doc.pages.map { page ->
                    if (page.id != pageId) {
                        page
                    } else {
                        val next = update(page)
                        if (next != page) changed = true
                        next
                    }
                }
                if (changed) {
                    doc.copy(pages = pages, modifiedDate = System.currentTimeMillis())
                } else {
                    doc
                }
            }
        }
    }

    /**
     * Publishes **several** already-baked pages of one document in one emission.
     *
     * Every `updatePage` replaces the whole document list, which is what the library
     * persistence and the dashboard observe — so a loop of *n* calls is *n* list
     * rebuilds, *n* recompositions of every page card, and *n* scheduled writes of the
     * entire library (`library.json` carries each page's recognised text). Rotating a
     * ten-page document costs ten bakes; it should not also cost ten serialisations of
     * the library, and on a document with recognised text it did.
     *
     * The bakes themselves stay per page: that is the part with a progress bar, and
     * batching the *emissions* must not make the screen look frozen. This is the same
     * rule as [setPageCorners] and [setPageOcrTextsBulk] — batch the write, never the
     * work.
     *
     * @param replacements page id to the page that should replace it, as returned by
     *   `PageRenderer.bake`. Ids that are no longer in the document are ignored, so a
     *   page deleted while its bake was in flight cannot resurrect it.
     */
    fun updatePages(docId: String, replacements: Map<String, ScannedPage>) {
        if (replacements.isEmpty()) return
        rewritePages(docId) { page -> replacements[page.id] ?: page }
    }

    /** Applies [transform] to every page of one document, in one emission. */
    fun updateAllPages(docId: String, transform: (ScannedPage) -> ScannedPage) {
        rewritePages(docId, transform)
    }

    /**
     * The shared body of the two batch writes.
     *
     * `modifiedDate` moves only when a page actually changed, so a batch that turned
     * out to be a no-op does not re-sort the library and re-stamp the card.
     */
    private fun rewritePages(docId: String, transform: (ScannedPage) -> ScannedPage) {
        _documents.update { docs ->
            docs.map { doc ->
                if (doc.id != docId) return@map doc
                var changed = false
                val pages = doc.pages.map { page ->
                    val next = transform(page)
                    if (next != page) changed = true
                    next
                }
                if (changed) {
                    doc.copy(pages = pages, modifiedDate = System.currentTimeMillis())
                } else {
                    doc
                }
            }
        }
    }

    fun addPageToDocument(docId: String, page: ScannedPage) {
        _documents.update { docs ->
            docs.map { doc ->
                if (doc.id == docId) {
                    doc.copy(
                        pages = doc.pages + page.copy(
                            documentId = docId,
                            pageIndex = doc.pages.size
                        ),
                        modifiedDate = System.currentTimeMillis()
                    )
                } else {
                    doc
                }
            }
        }
    }

    fun deletePage(docId: String, pageId: String) {
        var deleted: ScannedPage? = null
        _documents.update { docs ->
            docs.mapNotNull { doc ->
                if (doc.id != docId) return@mapNotNull doc
                val page = doc.pages.find { it.id == pageId }
                if (page != null) deleted = page
                val remaining = doc.pages
                    .filterNot { it.id == pageId }
                    .mapIndexed { index, p -> p.copy(pageIndex = index) }
                // Deleting the last page used to leave the document behind with an
                // empty page list: a husk that still took a card in the library, had
                // no cover, could not be exported to PDF, and could only be removed
                // by trashing it by hand. A document is its pages, so it goes too.
                if (remaining.isEmpty()) {
                    null
                } else {
                    doc.copy(pages = remaining, modifiedDate = System.currentTimeMillis())
                }
            }
        }
        // `deletePageFiles` removes all three names a page can occupy (capture,
        // JPEG bake, lossless bake), so an emptied document has nothing left to
        // clean up beyond the record this update already removed.
        deleted?.let { page -> scope.launch { ImageStore.deletePageFiles(appContext, page) } }
    }

    /**
     * Moves a page within its document and renumbers the rest.
     *
     * Previously advertised as "Live on Device" in the README while not existing
     * at all; this is the real implementation.
     */
    fun movePage(docId: String, fromIndex: Int, toIndex: Int) {
        _documents.update { docs ->
            docs.map { doc ->
                if (doc.id != docId) {
                    doc
                } else if (fromIndex !in doc.pages.indices || toIndex !in doc.pages.indices || fromIndex == toIndex) {
                    doc
                } else {
                    val reordered = doc.pages.toMutableList().apply {
                        add(toIndex, removeAt(fromIndex))
                    }
                    doc.copy(
                        pages = reordered.mapIndexed { index, p -> p.copy(pageIndex = index) },
                        modifiedDate = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    /**
     * Duplicates pages inside their document, keeping their order.
     *
     * The copies land **immediately after the block they came from**, which is where a
     * "copy these pages" gesture promises they will be; appending them to the end would
     * make the user hunt for them. Files are copied per page (see
     * [ImageStore.copyPageFiles]) so deleting either copy cannot delete the other's
     * pixels.
     *
     * @return the ids of the new pages, in order — empty if nothing could be copied.
     */
    fun duplicatePages(docId: String, pageIds: List<String>): List<String> {
        if (pageIds.isEmpty()) return emptyList()
        val doc = getDocumentById(docId) ?: return emptyList()
        val originals = doc.pages.filter { it.id in pageIds }
        if (originals.isEmpty()) return emptyList()
        // The whole document is touched at once: one copy pass, one library emission.
        val copies = ArrayList<ScannedPage>(originals.size)
        originals.forEach { page ->
            val copy = ImageStore.copyPageFiles(
                context = appContext,
                source = page,
                newPageId = UUID.randomUUID().toString()
            )
            if (copy != null) copies.add(copy)
        }
        if (copies.isEmpty()) return emptyList()

        _documents.update { docs ->
            docs.map { d ->
                if (d.id != docId) return@map d
                val rebuilt = ArrayList<ScannedPage>(d.pages.size + copies.size)
                // Right after the *last* selected page: a run of copies reads as one
                // block even when the selection was tapped out of order.
                val lastSelected = d.pages.indexOfLast { it.id in pageIds }
                d.pages.forEachIndexed { index, page ->
                    rebuilt.add(page)
                    if (index == lastSelected) rebuilt.addAll(copies)
                }
                d.copy(
                    pages = rebuilt.mapIndexed { index, p -> p.copy(pageIndex = index) },
                    modifiedDate = System.currentTimeMillis()
                )
            }
        }
        return copies.map { it.id }
    }

    /**
     * Moves pages to another document, appending them in their current order.
     *
     * Files travel with the records — a page's paths are absolute and keyed by page id,
     * not by document — so this is a bookkeeping move, not a copy. A source document
     * left with no pages is removed, the same rule [deletePage] follows.
     */
    fun movePagesToDocument(sourceDocId: String, pageIds: List<String>, targetDocId: String) {
        if (pageIds.isEmpty() || sourceDocId == targetDocId) return
        val source = getDocumentById(sourceDocId) ?: return
        if (getDocumentById(targetDocId) == null) return
        val moving = source.pages.filter { it.id in pageIds }
        if (moving.isEmpty()) return

        _documents.update { docs ->
            docs.mapNotNull { d ->
                when (d.id) {
                    sourceDocId -> {
                        val left = d.pages.filterNot { it.id in pageIds }
                        if (left.isEmpty()) {
                            null
                        } else {
                            d.copy(
                                pages = left.mapIndexed { index, p ->
                                    p.copy(pageIndex = index, documentId = sourceDocId)
                                },
                                modifiedDate = System.currentTimeMillis()
                            )
                        }
                    }
                    targetDocId -> d.copy(
                        pages = (d.pages + moving.map { it.copy(documentId = targetDocId) })
                            .mapIndexed { index, p -> p.copy(pageIndex = index) },
                        modifiedDate = System.currentTimeMillis()
                    )
                    else -> d
                }
            }
        }
    }

    /**
     * Moves **several** pages at once so the block lands at [targetIndex].
     *
     * This is what dragging a multi-page selection in the grid calls, and the
     * semantics are the ones a drag implies rather than the ones a list operation
     * would give:
     *
     * - the moved pages keep their **relative order**, because they travelled
     *   together and re-ordering them on the way would be a second, unasked edit;
     * - [targetIndex] is where the *block* starts in the final list;
     * - everything else closes the gap and keeps its own order.
     *
     * A no-op when nothing would actually move, so a drag that ends where it started
     * does not bump `modifiedDate` and re-sort the library.
     */
    fun movePages(docId: String, pageIds: List<String>, targetIndex: Int) {
        if (pageIds.isEmpty()) return
        _documents.update { docs ->
            docs.map { doc ->
                if (doc.id != docId) return@map doc
                val moving = doc.pages.filter { it.id in pageIds }
                if (moving.isEmpty()) return@map doc
                val rest = doc.pages.filterNot { it.id in pageIds }
                // Clamped against the *post-removal* list: dropping a block at the end
                // means the end of what is left, not an index that no longer exists.
                val destination = targetIndex.coerceIn(0, rest.size)
                val reordered = rest.toMutableList().apply {
                    addAll(destination, moving)
                }
                if (reordered.map { it.id } == doc.pages.map { it.id }) return@map doc
                doc.copy(
                    pages = reordered.mapIndexed { index, page -> page.copy(pageIndex = index) },
                    modifiedDate = System.currentTimeMillis()
                )
            }
        }
    }

    /**
     * Stores the geometry of several pages of one document in a **single** library
     * update.
     *
     * The multi-page border pass detects every page at once; writing each result
     * through [updatePage] would emit one library snapshot per page, and every
     * snapshot re-sorts the library and repaints the dashboard underneath. One
     * update for the whole pass is the difference between a single flicker and a
     * visibly busy background.
     *
     * `modifiedDate` moves once — and only if a page actually changed. The background
     * pass skips pages the user has already cropped, so on a document that was cropped
     * by hand while the detection ran there may be nothing left to apply; bumping the
     * date then would re-sort the library and move a card to the top of the home
     * screen for an edit nobody made.
     */
    fun setPageCorners(
        docId: String,
        cornersByPage: Map<String, QuadCorners>,
        /**
         * Leave pages that already have corners alone.
         *
         * Set by the background border pass, which works from a list of *uncropped*
         * pages: the detection of page 4 can finish after the user has already
         * cropped page 4 by hand, and a late write would silently undo their work.
         * The check has to be inside the update to be worth anything — comparing
         * against a snapshot taken before the pass would have the same race.
         */
        onlyWhereUncropped: Boolean = false
    ) {
        if (cornersByPage.isEmpty()) return
        _documents.update { docs ->
            docs.map { doc ->
                if (doc.id != docId) {
                    doc
                } else {
                    var changed = false
                    val pages = doc.pages.map { page ->
                        val replacement = cornersByPage[page.id]
                        when {
                            replacement == null -> page
                            onlyWhereUncropped && !page.corners.isFullFrame -> page
                            replacement == page.corners -> page
                            else -> {
                                changed = true
                                page.copy(corners = replacement)
                            }
                        }
                    }
                    if (changed) {
                        doc.copy(pages = pages, modifiedDate = System.currentTimeMillis())
                    } else {
                        doc
                    }
                }
            }
        }
    }

    /**
     * Finds the page border of every page of [docId] that has never been cropped.
     *
     * This is the "after the fact" half of border detection. A camera capture gets a
     * border immediately — the viewfinder outlines it live, and the review screen
     * detects it again at full resolution — but a page that arrives any other way
     * (the gallery picker on the dashboard, the gallery button inside a document, a
     * photo taken in another app) used to land with `QuadCorners.FULL_FRAME` and stay
     * that way until somebody opened the editor. Imported pages looked different
     * from scanned ones for no reason the user could see.
     *
     * Same detector, same 360 px working resolution, same confidence rules as the
     * review pass — so an imported photo and a scanned one of the same page end up
     * with the same crop. Only *unconfident* pages are left alone, and a page the
     * user has already cropped is never touched (see `onlyWhereUncropped`).
     *
     * Fire-and-forget on the repository's scope, like [indexTextInBackground]: the
     * import must not wait for it, and the result has to survive the screen that
     * started it being popped.
     */
    fun detectBordersInBackground(docId: String) {
        val pending = _documents.value
            .firstOrNull { it.id == docId }
            ?.pages
            ?.filter {
                it.corners.isFullFrame &&
                    (!it.originalImageUri.isNullOrBlank() || it.imageUri.isNotBlank())
            }
            ?: return
        if (pending.isEmpty()) return
        scope.launch {
            val found = LinkedHashMap<String, QuadCorners>()
            for (page in pending) {
                val source = page.originalImageUri?.takeIf { it.isNotBlank() } ?: page.imageUri
                if (source.isBlank()) continue
                val bitmap = ImageStore.loadBitmap(
                    appContext,
                    source,
                    AUTO_BORDER_DETECT_DIMENSION
                ) ?: continue
                // `finally` so a cancelled pass frees its decode: `scope` is the
                // repository's, and a bitmap kept alive by an abandoned coroutine is
                // a megabyte nobody will collect.
                val detection = try {
                    DocumentDetector.detect(bitmap, appContext)
                } finally {
                    bitmap.recycle()
                }
                if (!detection.corners.isFullFrame) found[page.id] = detection.corners
            }
            if (found.isEmpty()) return@launch
            setPageCorners(docId, found, onlyWhereUncropped = true)
        }
    }

    /**
     * Stores recognised page text.
     *
     * Deliberately not routed through [updatePage]: that bumps `modifiedDate`, and
     * background indexing would then reorder the library while the user is looking
     * at it. Text recognition is metadata about a page, not an edit to it.
     */
    fun setPageOcrText(docId: String, pageId: String, text: String) =
        setPageOcrTexts(docId, mapOf(pageId to text))

    /**
     * Stores recognised text for several pages of one document in a **single**
     * library update.
     *
     * This is the difference between one recomposition and N: `documents` is what
     * the home screen collects, so every emission re-sorts the library, rebuilds
     * the formatted date labels and re-runs the visible cards. Recognition is
     * background work that must not be able to make the list stutter, so it is
     * batched per document and applied once.
     */
    fun setPageOcrTexts(docId: String, texts: Map<String, String>) {
        if (texts.isEmpty()) return
        _documents.update { docs ->
            docs.map { doc ->
                if (doc.id != docId) {
                    doc
                } else {
                    doc.copy(
                        pages = doc.pages.map { page ->
                            texts[page.id]?.let { page.copy(ocrText = it) } ?: page
                        }
                    )
                }
            }
        }
    }

    /**
     * Applies recognised text for **many** documents in one library update.
     *
     * The batched sibling of [setPageOcrTexts]: one emission for a whole backfill
     * instead of one per document. See `backfillTextInBackground`.
     */
    fun setPageOcrTextsBulk(textsByDoc: Map<String, Map<String, String>>) {
        if (textsByDoc.isEmpty()) return
        _documents.update { docs ->
            docs.map { doc ->
                val texts = textsByDoc[doc.id]
                if (texts.isNullOrEmpty()) {
                    doc
                } else {
                    doc.copy(
                        pages = doc.pages.map { page ->
                            texts[page.id]?.let { page.copy(ocrText = it) } ?: page
                        }
                    )
                }
            }
        }
    }

    /**
     * Recognises the text of every page of [docId] that does not have it yet.
     *
     * Fire-and-forget on the repository's own scope: a capture must never wait for
     * recognition, and the work has to outlive the camera screen that started it.
     * Search is the only consumer, so a failure here costs nothing but a missing
     * result.
     */
    fun indexTextInBackground(docId: String) {
        val pending = _documents.value
            .firstOrNull { it.id == docId }
            ?.pages
            ?.filter { it.ocrText.isBlank() && it.imageUri.isNotBlank() }
            ?: return
        if (pending.isEmpty()) return
        scope.launch {
            val texts = pending.mapNotNull { page ->
                OcrEngine.readPage(page.imageUri)?.let { page.id to it }
            }.toMap()
            setPageOcrTexts(docId, texts)
        }
    }

    /**
     * Recognises text for pages that predate the feature, or that failed last run.
     *
     * Bounded to one batch per app start: a large library must not turn launching
     * the app into a long CPU burn. Pages still without text remain searchable by
     * title and tags.
     */
    fun backfillTextInBackground(
        limit: Int = OCR_BACKFILL_BATCH,
        /**
         * Checked between pages. While it returns `true` the pass waits, so work
         * that nobody is waiting for never competes with a scroll.
         */
        isBusy: () -> Boolean = { false }
    ) {
        val pending = _documents.value
            .filterNot { it.isTrash }
            .flatMap { doc ->
                doc.pages
                    .filter { it.ocrText.isBlank() && it.imageUri.isNotBlank() }
                    .map { doc.id to it }
            }
            .take(limit)
        if (pending.isEmpty()) return
        StartupWork.trace("ocr-backfill: ${pending.size} page(s) queued")
        scope.launch {
            val startedAt = System.nanoTime()
            var done = 0
            val recognised = LinkedHashMap<String, MutableMap<String, String>>()
            pending.groupBy({ it.first }, { it.second }).forEach { (docId, pages) ->
                val texts = recognised.getOrPut(docId) { LinkedHashMap() }
                for (page in pages) {
                    // Before every *page*, not every document.
                    //
                    // This is the difference between "recognition politely waits"
                    // and "recognition runs straight through a scroll": one page
                    // is an ML Kit inference plus a 2400 px decode, so a batch of
                    // eight pages belonging to one document used to spend several
                    // seconds of CPU that the next frame needed, having checked for
                    // a gesture only once, at the start.
                    while (isBusy()) delay(OCR_PAUSE_POLL_MS)
                    OcrEngine.readPage(page.imageUri)?.let { texts[page.id] = it }
                    done++
                }
            }
            // One library update for the whole batch, not one per document.
            //
            // `documents` is what the dashboard collects, so every emission re-sorts
            // the library, rebuilds the date labels and re-runs every visible card.
            // Applying per document meant up to twenty-four of those inside ten
            // seconds — each one a full list repaint during the exact window this
            // round exists to keep quiet. The text is only useful once it is
            // searchable anyway, and there is nothing to show in the meantime.
            //
            // The cost of batching: a process killed mid-pass loses the pages it had
            // already recognised, and they are picked up on the next launch. That is
            // the right trade for a background pass nobody is waiting for.
            setPageOcrTextsBulk(recognised)
            val ms = (System.nanoTime() - startedAt) / 1_000_000
            StartupWork.trace(
                "ocr-backfill finished: $done page(s) in ${recognised.size} doc(s), $ms ms"
            )
        }
    }

    // ------------------------------------------------------------- trash

    fun moveToTrash(docId: String) {
        _documents.update { docs ->
            docs.map {
                if (it.id == docId) {
                    it.copy(isTrash = true, modifiedDate = System.currentTimeMillis())
                } else {
                    it
                }
            }
        }
    }

    fun restoreFromTrash(docId: String) {
        _documents.update { docs ->
            docs.map {
                if (it.id == docId) {
                    it.copy(isTrash = false, modifiedDate = System.currentTimeMillis())
                } else {
                    it
                }
            }
        }
    }

    /** Removes the document and its page files from disk. */
    fun deletePermanently(docId: String) {
        var pages: List<ScannedPage> = emptyList()
        _documents.update { docs ->
            docs.find { it.id == docId }?.let { pages = it.pages }
            docs.filterNot { it.id == docId }
        }
        if (pages.isNotEmpty()) {
            scope.launch { ImageStore.deleteDocumentFiles(appContext, pages) }
        }
    }

    fun emptyTrash() {
        val trashed = _documents.value.filter { it.isTrash }
        _documents.update { docs -> docs.filterNot { it.isTrash } }
        if (trashed.isNotEmpty()) {
            scope.launch {
                trashed.forEach { ImageStore.deleteDocumentFiles(appContext, it.pages) }
            }
        }
    }

    // ----------------------------------------------------------- folders

    fun addFolder(name: String, colorHex: Long) {
        val folder = Folder(id = UUID.randomUUID().toString(), name = name, colorHex = colorHex)
        _folders.update { it + folder }
    }

    fun renameFolder(folderId: String, name: String) {
        _folders.update { folders ->
            folders.map { if (it.id == folderId) it.copy(name = name) else it }
        }
    }

    fun deleteFolder(folderId: String) {
        _folders.update { folders -> folders.filterNot { it.id == folderId } }
        _documents.update { docs ->
            docs.map { if (it.folderId == folderId) it.copy(folderId = null) else it }
        }
    }

    // -------------------------------------------------------- file hygiene

    /**
     * Deletes the page files of pages that were captured but never committed.
     *
     * The camera writes each page to `files/pages` the moment the shutter fires —
     * before any document exists — so leaving the session without pressing Done
     * used to leave full-resolution JPEGs behind with nothing pointing at them.
     * Deliberately on the repository's scope, not the screen's: the screen is
     * already being disposed when this is called.
     */
    fun discardPages(pages: List<ScannedPage>) {
        if (pages.isEmpty()) return
        val snapshot = pages.toList()
        scope.launch { ImageStore.deleteDocumentFiles(appContext, snapshot) }
    }

    /**
     * Every file a document in the library — including the trash — still needs.
     *
     * All three names per page, not just `imageUri`: the pristine capture is not
     * referenced by the model once a page has been baked, and it is exactly the
     * source the next re-edit needs. Sweeping on `imageUri` alone would quietly
     * destroy every original in the library.
     */
    private fun referencedPageFiles(): Set<String> {
        val paths = HashSet<String>()
        _documents.value.forEach { doc ->
            doc.pages.forEach { page ->
                paths += ImageStore.pageFile(appContext, page.id).absolutePath
                paths += ImageStore.editedFile(appContext, page.id).absolutePath
                paths += ImageStore.editedLosslessFile(appContext, page.id).absolutePath
                listOfNotNull(page.imageUri, page.originalImageUri, page.signatureOverlayUri)
                    .filterNot { it.startsWith("content://") }
                    .filter { it.contains('/') }
                    .forEach { paths += it }
            }
        }
        return paths
    }

    // ------------------------------------------------------- backup & restore

    /** The library exactly as it stands, without waiting for the write debounce. */
    fun snapshotNow(): LibrarySnapshot = LibrarySnapshot(
        schemaVersion = LibraryStore.CURRENT_SCHEMA_VERSION,
        folders = _folders.value,
        documents = _documents.value
    )

    /**
     * Writes the library to [uri] as a `.zip` (see [BackupArchive]).
     *
     * Runs on IO; the caller is a coroutine started from the Settings screen. A
     * failure throws — the UI turns it into a message. Writing a *partial* archive
     * and reporting success is the one outcome that must not happen, so nothing here
     * swallows an exception.
     */
    suspend fun exportBackup(uri: Uri): BackupArchive.Summary = withContext(Dispatchers.IO) {
        val snapshot = snapshotNow()
        val out = appContext.contentResolver.openOutputStream(uri)
            ?: throw IOException("Could not open the chosen file for writing")
        out.use { stream ->
            BackupArchive.create(
                output = stream,
                snapshot = snapshot,
                pagesDir = ImageStore.pagesDir(appContext),
                signaturesDir = ImageStore.signaturesDir(appContext)
            )
        }
    }

    /**
     * Restores a `.zip` backup from [uri], **merged** into the current library rather
     * than replacing it: a backup taken last week knows nothing about this morning's
     * scans. Documents and folders that only exist locally are kept; ones the backup
     * also has take the backup's version.
     *
     * The archive is copied to the cache first, validated and extracted to a staging
     * directory, and only then applied — so a truncated download or a corrupted zip
     * fails without touching a single file of the live library.
     */
    suspend fun importBackup(uri: Uri): BackupArchive.Summary = withContext(Dispatchers.IO) {
        val input = appContext.contentResolver.openInputStream(uri)
            ?: throw IOException("Could not open the chosen file")
        val stagedArchive = File(appContext.cacheDir, "restore-${System.nanoTime()}.zip")
        try {
            input.use { source ->
                stagedArchive.outputStream().use { target -> source.copyTo(target) }
            }
            val restored = BackupArchive.restore(
                archive = stagedArchive,
                pagesDir = ImageStore.pagesDir(appContext),
                signaturesDir = ImageStore.signaturesDir(appContext),
                stagingParent = appContext.filesDir
            )
            var committed = false
            try {
                val local = snapshotNow()
                val localIds = local.documents.mapTo(HashSet()) { it.id }
                if (restored.snapshot.documents.any { it.id in localIds }) {
                    throw IOException("Backup contains documents already in this library. Restore cancelled to protect local edits.")
                }
                val merged = BackupArchive.merge(local, restored.snapshot)
                restored.commit()
                committed = true
                // Publish only after the pixels are in place: a page that appears in
                // the library before its file exists renders as a blank thumbnail and
                // a failed export.
                _folders.value = merged.folders
                _documents.value = merged.documents
                store.writeNowBlocking(merged)

                // Thumbnails are keyed by path *and* the file's length and mtime, so a
                // changed file produces a new key — but the memo of that key is keyed
                // by path alone, and it was computed against the file this restore just
                // overwrote. Forget those, and drop decoded bitmaps still in memory.
                restored.snapshot.documents.forEach { document ->
                    document.pages.forEach { page ->
                        ThumbnailStore.invalidate(page.imageUri)
                        ThumbnailStore.invalidate(page.originalImageUri)
                        ThumbnailStore.invalidate(page.signatureOverlayUri)
                    }
                }
                PageImageLoader.clear()
                restored.summary
            } finally {
                // A failed move leaves staged files behind; the live library is
                // untouched either way, so this only reclaims space.
                if (!committed) restored.discard()
            }
        } finally {
            stagedArchive.delete()
        }
    }

    // ------------------------------------------------------------- ID card

    /**
     * Front and back of an ID card on one A4 page, as a new document.
     *
     * The original app does this as a two-step capture mode; this does it from two
     * pages that already exist, so it works on a re-scan as well as a fresh one and
     * touches nothing about how the camera captures. The layout arithmetic is
     * [CardComposition] (unit-tested); this is only the drawing.
     *
     * The page is written as **PNG**: a card is flat colour and small text, exactly
     * the content JPEG rings around, and `AGENTS.md` already makes lossless the rule
     * for pages that are not photographs. The composed page is stored under the same
     * `_edited.png` name every re-bake uses, so the orphan sweep and thumbnail keys
     * treat it as an ordinary page.
     *
     * @return the new document, or `null` when either image cannot be read.
     */
    suspend fun createIdCardDocument(
        frontPageId: String,
        backPageId: String,
        title: String,
        folderId: String? = null
    ): Document? = withContext(Dispatchers.IO) {
        val allPages = _documents.value.flatMap { it.pages }
        val front = allPages.firstOrNull { it.id == frontPageId } ?: return@withContext null
        val back = allPages.firstOrNull { it.id == backPageId } ?: return@withContext null

        val frontBitmap = ImageStore.loadBitmap(appContext, front.imageUri, CARD_SOURCE_DIMENSION)
            ?: return@withContext null
        val backBitmap = ImageStore.loadBitmap(appContext, back.imageUri, CARD_SOURCE_DIMENSION)
        if (backBitmap == null) {
            frontBitmap.recycle()
            return@withContext null
        }

        val composed = try {
            val page = CardComposition.Size(A4_PX_WIDTH, A4_PX_HEIGHT)
            val placement = CardComposition.placement(
                page = page,
                front = CardComposition.Size(frontBitmap.width, frontBitmap.height),
                back = CardComposition.Size(backBitmap.width, backBitmap.height)
            ) ?: return@withContext null

            Bitmap.createBitmap(A4_PX_WIDTH, A4_PX_HEIGHT, Bitmap.Config.ARGB_8888).also { target ->
                val canvas = Canvas(target)
                // Paper, not transparency: the PNG is embedded in a PDF, and a
                // transparent sheet composites to whatever the viewer decides is
                // behind it.
                canvas.drawColor(Color.WHITE)
                val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
                canvas.drawBitmap(frontBitmap, null, placement.front.toRect(), paint)
                canvas.drawBitmap(backBitmap, null, placement.back.toRect(), paint)
            }
        } finally {
            frontBitmap.recycle()
            backBitmap.recycle()
        }

        try {
            val pageId = UUID.randomUUID().toString()
            val file = ImageStore.editedLosslessFile(appContext, pageId)
            ImageStore.writePng(composed, file)
            addDocument(
                title = title,
                folderId = folderId,
                pages = listOf(
                    ScannedPage(
                        id = pageId,
                        documentId = "", // assigned by addDocument
                        pageIndex = 0,
                        imageUri = file.absolutePath,
                        // Already composed: do not run a filter over it.
                        filterType = FilterType.ORIGINAL,
                        corners = QuadCorners()
                    )
                )
            )
        } finally {
            if (!composed.isRecycled) composed.recycle()
        }
    }

    private fun CardComposition.Box.toRect(): Rect = Rect(left, top, right, bottom)

    // -------------------------------------------------------- lifecycle

    /**
     * Flushes the last edit and cancels the persistence scope.
     *
     * Two things were wrong with this and both were on the user's path out of the
     * app, which is the worst place for them:
     *
     * 1. **It blocked the main thread.** `close()` runs from `ViewModel.onCleared`,
     *    i.e. on the UI thread of an Activity that is finishing, and it called
     *    `runBlocking` around `json.encodeToString` of the *entire* library — a
     *    snapshot that carries every page's recognised text. On a scanned library
     *    that is megabytes of string building while the window is trying to close.
     * 2. **It wrote unconditionally.** Every mutation is persisted through a 150 ms
     *    debounce, so nine times out of ten the file was already up to date and the
     *    whole encode-and-write was pure waste.
     *
     * The flush now runs on IO in a scope of its own (the repository's own scope is
     * about to be cancelled) and only when something is actually dirty. The trade is
     * honest: an edit made in the last 150 ms of a session that is *killed*, not
     * merely finished, can be lost. `onCleared` fires when the screen finishes, which
     * gives the write seconds to land.
     */
    fun close() {
        val snapshot = LibrarySnapshot(
            schemaVersion = LibraryStore.CURRENT_SCHEMA_VERSION,
            folders = _folders.value,
            documents = _documents.value
        )
        val settings = _settings.value
        val dirty = store.isDirty || settingsStore.isDirty
        scope.cancel()
        if (!dirty) return
        StartupWork.trace("close: flushing the last edit")
        finalFlush.launch {
            runCatching { store.flush(snapshot) }
            runCatching { settingsStore.flush(settings) }
        }
    }

    /**
     * The one write on the way out, off the main thread.
     *
     * Deliberately *not* a child of [scope]: that scope is cancelled on the line
     * above, which is the point of `close()`.
     */
    private val finalFlush = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun defaultFolders(): List<Folder> = listOf(
        Folder(id = "folder_work", name = "Work & Contracts", colorHex = 0xFF3F51B5),
        Folder(id = "folder_tax", name = "Tax & Financials", colorHex = 0xFF4CAF50),
        Folder(id = "folder_personal", name = "Personal & ID", colorHex = 0xFFFF9800)
    )
}
