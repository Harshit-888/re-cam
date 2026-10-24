package com.example.docscanner.data

import android.content.Context
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
     * install) or the file is unreadable. Blocking; called once from the
     * repository constructor.
     */
    fun load(): LibrarySnapshot? = try {
        val file = libraryFile
        if (!file.exists() || file.length() == 0L) {
            null
        } else {
            // Explicit serializer rather than the reified extension: no extra
            // import to get wrong, and it compiles against every 1.x runtime.
            json.decodeFromString(LibrarySnapshot.serializer(), file.readText())
        }
    } catch (_: Throwable) {
        // Corrupt or from an incompatible revision: start clean rather than crash.
        null
    }

    /** Queues a snapshot write. Returns immediately. */
    fun schedule(snapshot: LibrarySnapshot) {
        pending.value = snapshot
    }

    /** Flushes immediately. Used by backup/restore and on explicit user export. */
    suspend fun writeNowBlocking(snapshot: LibrarySnapshot) = withContext(Dispatchers.IO) {
        writeNow(snapshot)
    }

    private fun writeNow(snapshot: LibrarySnapshot) {
        try {
            val target = libraryFile
            val temp = File(target.parentFile, "$FILE_NAME.tmp")
            temp.writeText(json.encodeToString(LibrarySnapshot.serializer(), snapshot))
            if (target.exists()) target.delete()
            if (!temp.renameTo(target)) {
                // Rename can fail across some filesystems; fall back to a copy.
                target.writeText(temp.readText())
                temp.delete()
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
