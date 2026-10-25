package com.example.docscanner.data

import android.content.Context
import com.example.docscanner.model.AppSettings
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
 * Persists [AppSettings] as `settings.json`.
 *
 * Deliberately separate from [LibraryStore]: settings are read on every launch to
 * pick a theme before the first frame, and mixing them into the library snapshot
 * would mean a theme toggle dirties the document library. Same safety rules
 * though — debounced writes, temp file then rename, and any failure is swallowed
 * so a bad file can never crash startup.
 */
class SettingsStore(
    context: Context,
    scope: CoroutineScope
) {
    private val appContext = context.applicationContext
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val pending = MutableStateFlow<AppSettings?>(null)

    val settingsFile: File
        get() = File(appContext.filesDir, FILE_NAME)

    init {
        scope.launch {
            pending.filterNotNull().collectLatest { settings ->
                delay(WRITE_DEBOUNCE_MS)
                withContext(Dispatchers.IO) { writeNow(settings) }
            }
        }
    }

    /**
     * Reads the stored settings, or `null` on a fresh install / unreadable file.
     * Blocking; called once from the repository constructor so the first
     * composition already has the user's theme.
     */
    fun load(): AppSettings? = try {
        val file = settingsFile
        if (!file.exists() || file.length() == 0L) {
            null
        } else {
            json.decodeFromString(AppSettings.serializer(), file.readText())
        }
    } catch (_: Throwable) {
        // Corrupt or from a newer revision: fall back to defaults rather than crash.
        null
    }

    /** Queues a write. Returns immediately. */
    fun schedule(settings: AppSettings) {
        pending.value = settings
    }

    /** Writes immediately, bypassing the debounce. Used on the way out of the app. */
    suspend fun writeNowBlocking(settings: AppSettings) = withContext(Dispatchers.IO) {
        writeNow(settings)
    }

    private fun writeNow(settings: AppSettings) {
        try {
            val target = settingsFile
            val temp = File(target.parentFile, "$FILE_NAME.tmp")
            temp.writeText(json.encodeToString(AppSettings.serializer(), settings))
            // Rename over the target rather than deleting it first: see the note in
            // LibraryStore.writeNow. Deleting first is a window with no settings at
            // all, and a kill inside it silently resets the user's theme.
            if (!temp.renameTo(target)) {
                try {
                    temp.copyTo(target, overwrite = true)
                } finally {
                    temp.delete()
                }
            }
        } catch (_: Throwable) {
            // A lost settings write must never crash the UI; the next change reschedules.
        }
    }

    companion object {
        const val FILE_NAME = "settings.json"
        private const val WRITE_DEBOUNCE_MS = 150L
    }
}
