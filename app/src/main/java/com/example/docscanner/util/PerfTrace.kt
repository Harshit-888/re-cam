package com.example.docscanner.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import android.view.Choreographer
import java.util.concurrent.atomic.AtomicInteger

/**
 * Frame-time and image-work counters, for debug builds only.
 *
 * This exists because the home screen's jank was being argued about instead of
 * measured. A `Choreographer` callback already runs on every frame, so recording
 * the interval between them is one subtraction per frame; the numbers it prints
 * say whether a slow scroll is *frames* (layout/composition/draw) or *work the app
 * scheduled off the main thread that is now fighting for CPU* (image decoding) —
 * which are fixed in completely different ways.
 *
 * ```
 * adb logcat -s DocScannerPerf
 * ```
 *
 * A healthy fling on a 60 Hz device prints `p90≈17ms max≈30ms`; the counters show
 * whether covers came from the memory cache (the common case), from the stored
 * thumbnail, or from a full page decode (which should be near zero once a library
 * has been primed).
 *
 * Release builds never install the callback: [install] checks the debug flag, and
 * the counters are incremented only while [installed] is true, so the cost in
 * production is a single volatile read per image load.
 */
object PerfTrace {

    private const val TAG = "DocScannerPerf"

    /** Frames per reported window: two seconds at 60 Hz. */
    private const val WINDOW = 120

    /** p90 above this means the scroll was visibly uneven. */
    private const val JANK_P90_NS = 18_000_000L

    /** A single frame over this is a dropped frame at 60 Hz. */
    private const val JANK_MAX_NS = 30_000_000L

    @Volatile
    private var installed = false

    private val frameTimes = LongArray(WINDOW)
    private var filled = 0
    private var lastFrameNanos = 0L
    private var windows = 0

    private val cacheHits = AtomicInteger()
    private val thumbDecodes = AtomicInteger()
    private val pageDecodes = AtomicInteger()

    /** Starts the frame monitor. No-op on release builds, and idempotent. */
    fun install(context: Context) {
        if (installed) return
        val debuggable =
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (!debuggable) return
        synchronized(this) {
            if (installed) return
            installed = true
        }
        val choreographer = Choreographer.getInstance()
        choreographer.postFrameCallback(object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                recordFrame(frameTimeNanos)
                choreographer.postFrameCallback(this)
            }
        })
        Log.i(TAG, "frame monitor on, window = $WINDOW frames")
    }

    fun onCacheHit() {
        if (installed) cacheHits.incrementAndGet()
    }

    /** A cover came from a stored thumbnail: the fast path. */
    fun onThumbnailDecode() {
        if (installed) thumbDecodes.incrementAndGet()
    }

    /** A cover came from a full page decode: the path this round set out to remove. */
    fun onPageDecode() {
        if (installed) pageDecodes.incrementAndGet()
    }

    private fun recordFrame(frameTimeNanos: Long) {
        val previous = lastFrameNanos
        lastFrameNanos = frameTimeNanos
        if (previous == 0L) return
        frameTimes[filled++] = frameTimeNanos - previous
        if (filled == WINDOW) {
            filled = 0
            report()
        }
    }

    private fun report() {
        val sorted = frameTimes.copyOf(WINDOW)
        sorted.sort()
        val p50 = sorted[WINDOW / 2]
        val p90 = sorted[(WINDOW * 9) / 10]
        val max = sorted[WINDOW - 1]
        val janky = p90 >= JANK_P90_NS || max >= JANK_MAX_NS

        // A clean window is only worth a line occasionally, so a healthy session
        // does not fill the log with noise that hides the interesting window.
        windows++
        if (!janky && windows % 5 != 0) return

        val detail = "frames=$WINDOW p50=${ms(p50)} p90=${ms(p90)} max=${ms(max)} " +
            "cacheHits=${cacheHits.getAndSet(0)} thumbs=${thumbDecodes.getAndSet(0)} " +
            "pageDecodes=${pageDecodes.getAndSet(0)}"
        if (janky) {
            Log.i(TAG, "JANK $detail")
        } else {
            Log.i(TAG, "smooth $detail")
        }
    }

    private fun ms(nanos: Long): String = "${nanos / 1_000_000}ms"
}
