package com.example.docscanner.data

import android.content.Context
import com.example.docscanner.model.Document
import com.example.docscanner.model.Folder
import com.example.docscanner.model.LibrarySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

/**
 * Persists the whole library as a single `library.json` snapshot.
 *
 * Chosen over Room because the entire model is already `@Serializable`, the
 * library is small (metadata only — page pixels live as JPEGs on disk), and a
 * snapshot makes the future ZIP backup/restore feature a one-liner.
 *
 * Writes are **coalesced**: every mutation schedules a write, but bursts within
 * [WRITE_DEBOUNCE_MS] collapse into one. Writes go to a temp file first and are
 * renamed into place, so a kill mid-write cannot truncate the library.
 */
class LibraryStore(
    context: Context,
    scope: CoroutineScope
) {
    private val appContext = context.applicationContext
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val pending = MutableStateFlow<LibrarySnapshot?>(null)

    /**
     * The document and folder lists of the snapshot that is on disk (or queued), by
     * **identity**.
     *
     * `combine` in `ScannerRepository` emits once immediately, and that first
     * emission is the very lists this store has just read: re-serialising them would
     * mean encoding the whole library — every page's recognised text — moments after
     * start-up, on the IO threads the cover decodes are queued behind. The repository
     * already skips that one write; this is the same guarantee for every *other*
     * no-op emission (a settings-like change that rebuilt the snapshot without
     * touching either list, two mutations that happened to land on equal content).
     * Comparing references is O(1) and cannot be wrong: a list whose contents changed
     * is always a new instance.
     */
    private var queuedDocuments: List<Document>? = null
    private var queuedFolders: List<Folder>? = null

    /**
     * `true` while there is something on disk that is older than the last change.
     *
     * Read by `ScannerRepository.close()`: the flush on the way out used to rewrite
     * the entire library unconditionally, on the main thread, even when the debounce
     * had already persisted everything — the one moment in the app's life where the
     * user is watching for the home screen to appear.
     */
    @Volatile
    var isDirty: Boolean = false
        private set

    /** The JSON file backing the library. Also the `manifest.json` source. */
    val libraryFile: File
        get() = File(appContext.filesDir, FILE_NAME)

    init {
        scope.launch {
            pending.filterNotNull().collectLatest { snapshot ->
                delay(WRITE_DEBOUNCE_MS)
                withContext(Dispatchers.IO) { writeNow(snapshot) }
            }
        }
    }

    /**
     * Reads the snapshot, or `null` when nothing has been persisted yet (fresh
     * install). An unreadable existing file throws to prevent accidental overwrite. Blocking; called once from the
     * repository constructor.
     */
    fun load(): LibrarySnapshot? = try {
        val file = libraryFile
        if (!file.exists()) {
            null
        } else {
            // Explicit serializer rather than the reified extension: no extra
            // import to get wrong, and it compiles against every 1.x runtime.
            json.decodeFromString(LibrarySnapshot.serializer(), file.readText()).also {
                // What is on disk *is* what these instances are, so an emission that
                // hands them straight back is not a change and must not be written.
                queuedDocuments = it.documents
                queuedFolders = it.folders
            }
        }
    } catch (error: Exception) {
        // Fail closed: neither persistence nor the orphan sweep may run on an
        // empty library when an existing snapshot cannot be read.
        throw IOException("The library could not be read. Its files have not been changed.", error)
    }

    /**
     * Queues a snapshot write. Returns immediately.
     *
     * An emission that hands back the **same two list instances** as the last one is
     * not a change and costs nothing: `combine` in `ScannerRepository` emits once
     * immediately, and that first emission is the very lists this store has just read
     * off disk. Comparing references is O(1) and cannot be fooled — a list whose
     * contents changed is always a new instance.
     */
    fun schedule(snapshot: LibrarySnapshot) {
        if (snapshot.documents === queuedDocuments && snapshot.folders === queuedFolders) return
        queuedDocuments = snapshot.documents
        queuedFolders = snapshot.folders
        isDirty = true
        pending.value = snapshot
    }

    /**
     * Writes whatever is queued **now**, skipping the debounce.
     *
     * Suspends on the caller's thread only for the directory hop; the encode and the
     * write happen on IO. Used by the final flush on the way out of the app.
     */
    suspend fun flush(snapshot: LibrarySnapshot) = withContext(Dispatchers.IO) {
        if (snapshot.documents === queuedDocuments && snapshot.folders === queuedFolders &&
            !isDirty
        ) {
            return@withContext
        }
        queuedDocuments = snapshot.documents
        queuedFolders = snapshot.folders
        writeNow(snapshot)
    }

    /** Flushes immediately. Used by backup/restore and on explicit user export. */
    suspend fun writeNowBlocking(snapshot: LibrarySnapshot) = withContext(Dispatchers.IO) {
        // This *is* the state on disk now, so the emission that follows a restore
        // must not queue a second write of the same thing.
        queuedDocuments = snapshot.documents
        queuedFolders = snapshot.folders
        writeNow(snapshot)
    }

    /**
     * Serialises writes.
     *
     * Two writers can reach [writeNow] at once: the debounce collector, and the
     * final flush from `ScannerRepository.close()`. Both write `library.json.tmp`
     * before renaming it into place, so two overlapping writes interleave inside one
     * file and the rename publishes the mixture — a truncated library, produced by
     * the act of closing the app. Cancellation does not interrupt a blocking write,
     * so cancelling the scope is not enough to stop it.
     */
    private val writeLock = Any()

    private fun writeNow(snapshot: LibrarySnapshot) = synchronized(writeLock) {
        try {
            val target = libraryFile
            val temp = File(target.parentFile, "$FILE_NAME.tmp")
            temp.writeText(json.encodeToString(LibrarySnapshot.serializer(), snapshot))
            // Rename *over* the target. On a normal filesystem that is atomic: the
            // old library is readable right up to the instant the new one replaces
            // it. This used to delete the target first, which left a window — a
            // kill, a crash, the OS reclaiming the process — where the library was
            // simply gone and the next launch started from an empty library.
            if (!temp.renameTo(target)) {
                // Do not fall back to truncating the live file: a failed or
                // interrupted copy would destroy the last readable snapshot.
                throw IOException("Could not atomically replace library snapshot")
            }
            // Only clear the flag for the snapshot that is still the queued one: a
            // write that was already in flight when a newer change arrived must not
            // make the newer one look persisted. Otherwise the flush on the way out
            // skips it and that edit is lost.
            if (snapshot.documents === queuedDocuments && snapshot.folders === queuedFolders) {
                isDirty = false
            }
        } catch (_: Throwable) {
            // Losing a debounce write must never crash the UI. The next mutation
            // re-schedules.
        }
    }

    companion object {
        const val FILE_NAME = "library.json"
        const val CURRENT_SCHEMA_VERSION = 1
        private const val WRITE_DEBOUNCE_MS = 150L
    }
}
