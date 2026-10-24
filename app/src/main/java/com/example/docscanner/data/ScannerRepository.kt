package com.example.docscanner.data

import android.content.Context
import com.example.docscanner.model.Document
import com.example.docscanner.model.Folder
import com.example.docscanner.model.LibrarySnapshot
import com.example.docscanner.model.ScannedPage
import com.example.docscanner.util.ImageStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
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
class ScannerRepository(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val store = LibraryStore(appContext, scope)

    private val restored: LibrarySnapshot? = store.load()

    private val _folders = MutableStateFlow(
        restored?.folders?.takeIf { it.isNotEmpty() } ?: defaultFolders()
    )
    val folders: StateFlow<List<Folder>> = _folders.asStateFlow()

    private val _documents = MutableStateFlow(restored?.documents ?: emptyList())
    val documents: StateFlow<List<Document>> = _documents.asStateFlow()

    init {
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

    fun setPageOcrText(docId: String, pageId: String, text: String) {
        updatePage(docId, pageId) { it.copy(ocrText = text) }
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
