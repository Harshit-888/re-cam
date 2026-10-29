package com.example.docscanner.data

import android.app.Application
import android.os.Process
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.docscanner.util.StartupWork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Owns the one [ScannerRepository] and survives a configuration change with it.
 *
 * Two lifetimes had to be separated, and only a ViewModel separates them:
 *
 * * **A rotation** must not restart the app. The repository used to be built in
 *   `onCreate`, so every rotation re-read and re-parsed `library.json`, re-decoded
 *   every cover and replayed the launch screen — a second of work and a splash the
 *   user did not ask for, for a change of orientation. A ViewModel outlives the
 *   Activity instance, so the library, its persistence scope and its debounced
 *   writes all simply carry on.
 * * **Leaving the app** must stop it. [onCleared] runs when the Activity really
 *   finishes — not on a rotation — and flushes the last edit before cancelling the
 *   persistence scope. The repository is therefore still owned by this screen
 *   (`AGENTS.md` §2.6), never by a process-wide singleton.
 *
 * Construction stays off the main thread: reading the library is the first thing
 * the app does and the thread that owes the system a frame within 8 ms must not be
 * the one doing it. `NonCancellable` keeps a teardown mid-load from leaving a
 * half-built repository with a live scope and no owner.
 */
class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val _repository = MutableStateFlow<ScannerRepository?>(null)

    /** `null` until the library has been read — that is the launch screen. */
    val repository: StateFlow<ScannerRepository?> = _repository.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    init {
        viewModelScope.launch {
            val startedAt = SystemClock.elapsedRealtime()
            val built = try {
                withContext(NonCancellable + Dispatchers.IO) {
                    ScannerRepository(getApplication<Application>())
                }
            } catch (error: java.io.IOException) {
                _loadError.value = error.message ?: "The library could not be read."
                return@launch
            }
            // Cleared while we were reading (the user backed out during launch): the
            // value would never be observed, and `onCleared` has already run, so the
            // repository has to be closed here or its scope outlives the app screen.
            if (!viewModelScope.isActive) {
                built.close()
                return@launch
            }
            // ## Hand over as soon as the library exists
            //
            // There used to be a `SPLASH_MIN_MS = 320` floor here — the launch screen
            // was held for at least a third of a second "so a warm start shows a logo
            // rather than a one-frame flash". That traded a real third of a second of
            // every launch for an aesthetic that `MainActivity`'s 300 ms `Crossfade`
            // already provides: the splash is faded *out* over the content, so a warm
            // start is a crossfade, not a flash, and no floor is needed to make it one.
            val elapsed = SystemClock.elapsedRealtime() - startedAt
            val sinceProcessStart = SystemClock.uptimeMillis() - Process.getStartUptimeMillis()
            StartupWork.trace(
                "launch: library ready ${elapsed} ms after the ViewModel was created, " +
                    "$sinceProcessStart ms after process start"
            )
            _repository.value = built
        }
    }

    override fun onCleared() {
        _repository.value?.close()
        _repository.value = null
    }
}
