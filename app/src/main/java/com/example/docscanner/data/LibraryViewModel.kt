package com.example.docscanner.data

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Shortest time the launch screen is shown, so a warm start does not flash. */
private const val SPLASH_MIN_MS = 320L

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

    init {
        viewModelScope.launch {
            val startedAt = SystemClock.elapsedRealtime()
            val built = withContext(NonCancellable + Dispatchers.IO) {
                ScannerRepository(getApplication<Application>())
            }
            // Cleared while we were reading (the user backed out during launch): the
            // value would never be observed, and `onCleared` has already run, so the
            // repository has to be closed here or its scope outlives the app screen.
            if (!viewModelScope.isActive) {
                built.close()
                return@launch
            }
            // Hold the launch screen briefly even when the read was instant, so a
            // warm start shows a logo rather than a one-frame flash.
            val elapsed = SystemClock.elapsedRealtime() - startedAt
            if (elapsed < SPLASH_MIN_MS) delay(SPLASH_MIN_MS - elapsed)
            _repository.value = built
        }
    }

    override fun onCleared() {
        _repository.value?.close()
        _repository.value = null
    }
}
