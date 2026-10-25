package com.example.docscanner.data

import android.content.Context
import com.example.docscanner.model.AppSettings
import com.example.docscanner.model.Document
import com.example.docscanner.model.Folder
import com.example.docscanner.model.QuadCorners
import com.example.docscanner.model.LibrarySnapshot
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.util.ImageStore
import com.example.docscanner.util.OcrEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

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
 * The constructor reads `library.json` synchronously (a few KB of JSON). That is
 * deliberate: it means the first composition already sees the restored library
 * instead of flashing an empty state.
 */
/** How many pages one background text-recognition pass may handle. */
private const val OCR_BACKFILL_BATCH = 24

/**
 * Pause between the documents of a backfill pass.
 *
 * Each applied result emits a new library snapshot, and every snapshot is a
 * recomposition of the home screen plus a rewrite of the whole `library.json`
 * (which contains all of this text). Unspaced, a fresh install re-indexed the
 * library two dozen times inside the first seconds of use — while the user was
 * scrolling it. One short gap per *document* is invisible; one per page was not.
 */
private const val OCR_BATCH_SPACING_MS = 220L

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

class ScannerRepository(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val store = LibraryStore(appContext, scope)
    private val settingsStore = SettingsStore(appContext, scope)

    private val restored: LibrarySnapshot? = store.load()

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

        // Persist every subsequent change. `combine` emits once immediately, which
        // also normalises a fresh install's default folders to disk.
        scope.launch {
            combine(_documents, _folders) { documents, folders ->
                LibrarySnapshot(
                    schemaVersion = LibraryStore.CURRENT_SCHEMA_VERSION,
                    folders = folders,
                    documents = documents
                )
            }.collect { store.schedule(it) }
        }
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

    fun updatePage(docId: String, pageId: String, update: (ScannedPage) -> ScannedPage) {
        _documents.update { docs ->
            docs.map { doc ->
                if (doc.id == docId) {
                    doc.copy(
                        pages = doc.pages.map { page -> if (page.id == pageId) update(page) else page },
                        modifiedDate = System.currentTimeMillis()
                    )
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
            docs.map { doc ->
                if (doc.id == docId) {
                    val page = doc.pages.find { it.id == pageId }
                    if (page != null) deleted = page
                    doc.copy(
                        pages = doc.pages
                            .filterNot { it.id == pageId }
                            .mapIndexed { index, p -> p.copy(pageIndex = index) },
                        modifiedDate = System.currentTimeMillis()
                    )
                } else {
                    doc
                }
            }
        }
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
     * Stores the geometry of several pages of one document in a **single** library
     * update.
     *
     * The multi-page border pass detects every page at once; writing each result
     * through [updatePage] would emit one library snapshot per page, and every
     * snapshot re-sorts the library and repaints the dashboard underneath. One
     * update for the whole pass is the difference between a single flicker and a
     * visibly busy background.
     *
     * `modifiedDate` moves once, because the document genuinely did change.
     */
    fun setPageCorners(docId: String, cornersByPage: Map<String, QuadCorners>) {
        if (cornersByPage.isEmpty()) return
        _documents.update { docs ->
            docs.map { doc ->
                if (doc.id != docId) {
                    doc
                } else {
                    doc.copy(
                        pages = doc.pages.map { page ->
                            cornersByPage[page.id]?.let { page.copy(corners = it) } ?: page
                        },
                        modifiedDate = System.currentTimeMillis()
                    )
                }
            }
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
        scope.launch {
            pending.groupBy({ it.first }, { it.second }).forEach { (docId, pages) ->
                // Yield to the UI before starting a page, not after: a recognition
                // pass that begins mid-fling is 200 ms of the CPU the next frame
                // needed.
                while (isBusy()) delay(OCR_PAUSE_POLL_MS)
                val texts = pages.mapNotNull { page ->
                    OcrEngine.readPage(page.imageUri)?.let { page.id to it }
                }.toMap()
                setPageOcrTexts(docId, texts)
                // Let the frame, the JSON write and the index settle before the
                // next document lands on top of them.
                delay(OCR_BATCH_SPACING_MS)
            }
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

    // -------------------------------------------------------- lifecycle

    /**
     * Cancels the persistence scope. Call from the owning Activity's
     * `onDestroy` only when finishing (not on configuration change).
     */
    fun close() {
        scope.cancel()
    }

    private fun defaultFolders(): List<Folder> = listOf(
        Folder(id = "folder_work", name = "Work & Contracts", colorHex = 0xFF3F51B5),
        Folder(id = "folder_tax", name = "Tax & Financials", colorHex = 0xFF4CAF50),
        Folder(id = "folder_personal", name = "Personal & ID", colorHex = 0xFFFF9800)
    )
}
