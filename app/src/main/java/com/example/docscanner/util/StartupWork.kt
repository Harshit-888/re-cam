package com.example.docscanner.util

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * When the work nobody is waiting for is allowed to run.
 *
 * ## The bug this exists for
 *
 * Opening the app cold stuttered for roughly ten seconds. The library screen itself
 * was doing very little — the stutter came from four background jobs that had each
 * decided that *launch* was the right moment to start:
 *
 * | job | used to start | cost |
 * | :--- | :--- | :--- |
 * | first-screen cover prefetch | 48 ms after composition | 6–8 full-page decodes |
 * | thumbnail repair (pages saved before thumbnails existed) | 2.2 s + 1.2 s | a decode + encode **per page** in the library |
 * | text recognition backfill | 2.2 s + 3 s | ML Kit model load, then an inference and a 2400 px decode per page, up to 24 pages |
 * | orphan sweep | 5 s | a directory listing plus deletes |
 *
 * Every one of them was individually reasonable. Together they arrive in the window
 * where the user is reading the list for the first time — which is the worst moment
 * in the app's life to spend four cores on work with no visible output.
 *
 * ## The rule
 *
 * Nothing speculative runs until the app has been **left alone** for a while: the
 * screen is in the foreground, no pointer has touched it, and the list is not
 * scrolling. Every touch resets the clock, so a user who is actively using the app
 * never has background work competing with them. When they put the phone down, the
 * work resumes by itself.
 *
 * It is deliberately *not* "run it later, once" — that is the pattern that produced
 * the problem (a fixed delay always fires, whether or not the user is mid-gesture).
 *
 * A second, smaller discipline: everything here runs on **one** thread
 * ([dispatcher]). The decode pool the visible tiles use is shared; a background pass
 * that occupies one worker leaves the rest to the frames.
 */
object StartupWork {

    private const val TAG = "DocScannerPerf"

    /** How often [awaitQuiet] re-checks. 100 ms is far below any perceptible delay. */
    private const val POLL_MS = 100L

    /**
     * One background thread for all speculative work.
     *
     * `limitedParallelism(1)` gives a view of `Dispatchers.IO` that never runs more
     * than one task at a time. A repair pass and a recognition pass therefore cannot
     * each grab a core and leave the frame with none.
     */
    val dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1)

    private val _foreground = MutableStateFlow(true)
    val foreground: StateFlow<Boolean> = _foreground.asStateFlow()

    /** `System.nanoTime()` of the last pointer-down/move seen anywhere in the app. */
    @Volatile
    private var lastInteractionNanos: Long = System.nanoTime()

    /**
     * Counts touches. Speculative loops sample it and abandon their pass when it
     * moves: the moment a finger lands, whatever the loop was doing stops being
     * useful and starts being contention.
     */
    @Volatile
    private var interactions: Long = 0L

    /** Set by the dashboard while a fling is in progress. */
    @Volatile
    private var scrolling: Boolean = false

    /**
     * Records that the user is touching the app.
     *
     * Called from a single `pointerInput` observer at the root of the composition, on
     * the `Initial` pass, so it sees every gesture in the app without consuming any
     * of them. It must stay this cheap: one volatile write per pointer event.
     */
    fun markInteraction() {
        lastInteractionNanos = System.nanoTime()
        interactions++
    }

    /** Snapshot for a loop that wants to stop as soon as the user touches the app. */
    fun interactionStamp(): Long = interactions

    /** `true` while the user has not touched anything since [stamp]. */
    fun isUndisturbedSince(stamp: Long): Boolean = interactions == stamp

    fun setForeground(isForeground: Boolean) {
        if (_foreground.value != isForeground) {
            _foreground.value = isForeground
            trace(if (isForeground) "foreground" else "background")
        }
    }

    /** The dashboard reports its scroll state here; a fling counts as interaction. */
    fun setScrolling(value: Boolean) {
        scrolling = value
        if (value) markInteraction()
    }

    /** Milliseconds since the last touch, for diagnostics. */
    fun idleMs(): Long = (System.nanoTime() - lastInteractionNanos) / 1_000_000L

    /**
     * Suspends until the app is in the foreground, not scrolling, and has been
     * untouched for [quietMs].
     *
     * Cancellable, so a caller inside a `collectLatest` stops waiting the moment the
     * user scrolls. Returns immediately when already quiet.
     *
     * @param reason short label used in the debug log, so a logcat line says *which*
     *   pass was allowed to start and after how long.
     */
    suspend fun awaitQuiet(quietMs: Long, reason: String) {
        var waited = 0L
        while (true) {
            val idle = idleMs()
            if (_foreground.value && !scrolling && idle >= quietMs) {
                if (waited > 0) {
                    trace("$reason allowed after ${waited + idle} ms idle")
                }
                return
            }
            delay(POLL_MS)
            waited += POLL_MS
        }
    }

    /**
     * Runs [block] on the single background thread once the app has been quiet for
     * [quietMs]. Returns `false` — without running anything — if the app went to the
     * background while waiting, which is the one case where there is no point
     * starting: the process is about to be frozen.
     */
    suspend fun runWhenQuiet(quietMs: Long, reason: String, block: suspend () -> Unit): Boolean {
        awaitQuiet(quietMs, reason)
        if (!_foreground.value) return false
        withContext(dispatcher) { block() }
        return true
    }

    /** Debug-build logging only: [PerfTrace] is a no-op in release. */
    fun trace(message: String) {
        if (PerfTrace.isEnabled) Log.i(TAG, "[startup] $message")
    }
}
