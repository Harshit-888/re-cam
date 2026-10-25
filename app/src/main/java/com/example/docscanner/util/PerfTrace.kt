package com.example.docscanner.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Choreographer
import android.view.FrameMetrics
import android.view.Window
import java.util.Locale
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

    /**
     * Where a frame's time went, per gesture.
     *
     * The frame-interval numbers answer *"was it janky"*; they do not answer *"what
     * was doing the work"*, and those two have completely different fixes. A photo
     * grid can be slow because the app spends the frame in composition and layout
     * (our code, fixable by making tiles cheaper) or because the GPU is uploading
     * and sampling textures on the render thread (not our code at all, fixable only
     * by decoding smaller or drawing less). Guessing between them has already cost
     * two rounds, so the platform's own per-frame breakdown is recorded instead:
     * `sync` + `layout` are the app's own work, `draw` + `command` + `gpu` are the
     * render side.
     *
     * Only collected while a gesture is running, so the log stays one readable line
     * per scroll.
     */
    private var gestureSyncNs = 0L
    private var gestureLayoutNs = 0L
    private var gestureDrawNs = 0L
    private var gestureCommandNs = 0L
    private var gestureGpuNs = 0L
    private var gestureSamples = 0

    private var metricsWindow: Window? = null
    private var metricsListener: Window.OnFrameMetricsAvailableListener? = null

    private val frameTimes = LongArray(WINDOW)
    private var filled = 0
    private var lastFrameNanos = 0L
    private var windows = 0

    private val cacheHits = AtomicInteger()
    private val thumbDecodes = AtomicInteger()
    private val pageDecodes = AtomicInteger()

    // ---- per-gesture scroll accounting ------------------------------------
    // The window below reports *all* frames, most of which happen while the
    // screen is idle. A scroll is the only thing being complained about, so it
    // gets its own tally, printed once when the finger lifts: one line per
    // gesture, with the numbers that decide whether there is a problem at all.
    @Volatile
    private var scrolling = false
    private var gestureFrames = 0
    private var gestureJank = 0
    private var gestureWorstNs = 0L
    private var gestureTotalNs = 0L

    /** Told by the list when a scroll gesture starts and ends. */
    fun setScrolling(value: Boolean) {
        if (!installed) return
        if (value == scrolling) return
        if (value) {
            gestureFrames = 0
            gestureJank = 0
            gestureWorstNs = 0L
            gestureTotalNs = 0L
            gestureSyncNs = 0L
            gestureLayoutNs = 0L
            gestureDrawNs = 0L
            gestureCommandNs = 0L
            gestureGpuNs = 0L
            gestureSamples = 0
            scrolling = true
        } else {
            scrolling = false
            reportGesture()
        }
    }

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

    /**
     * Starts collecting per-frame stage timings from [window].
     *
     * Called by the Activity (this is the only place the app has a `Window`); a
     * no-op on release builds and when already attached. The metrics arrive on the
     * main looper a frame or two late, which is irrelevant for a per-gesture
     * average.
     */
    fun attachWindow(window: Window) {
        if (!installed || metricsWindow === window) return
        val listener = Window.OnFrameMetricsAvailableListener { _, metrics, _ ->
            // Frames outside a gesture are idle frames and say nothing about a
            // scroll, so they are not counted.
            if (scrolling) {
                gestureSamples++
                gestureSyncNs += metrics.getMetric(FrameMetrics.SYNC_DURATION)
                gestureLayoutNs += metrics.getMetric(FrameMetrics.LAYOUT_MEASURE_DURATION)
                gestureDrawNs += metrics.getMetric(FrameMetrics.DRAW_DURATION)
                gestureCommandNs += metrics.getMetric(FrameMetrics.COMMAND_DURATION)
                // `gpu` needs HWUI timing, which only exists from 31 and is -1 when
                // the device does not report it; counting a -1 would poison the
                // average.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val gpu = metrics.getMetric(FrameMetrics.GPU_DURATION)
                    if (gpu > 0L) gestureGpuNs += gpu
                }
            }
        }
        window.addOnFrameMetricsAvailableListener(
            listener,
            Handler(Looper.getMainLooper())
        )
        metricsWindow = window
        metricsListener = listener
    }

    /** Stops collecting. Safe to call for a window that was never attached. */
    fun detachWindow(window: Window) {
        val listener = metricsListener ?: return
        if (metricsWindow !== window) return
        window.removeOnFrameMetricsAvailableListener(listener)
        metricsWindow = null
        metricsListener = null
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
        val delta = frameTimeNanos - previous
        frameTimes[filled++] = delta
        if (filled == WINDOW) {
            filled = 0
            report()
        }
        if (scrolling) {
            gestureFrames++
            gestureTotalNs += delta
            if (delta > gestureWorstNs) gestureWorstNs = delta
            // 24 ms is the threshold the Android runtime itself uses for a
            // "slow frame" on a 60 Hz display.
            if (delta > 24_000_000L) gestureJank++
        }
    }

    /**
     * One line per gesture. On a 60 Hz phone, `avg` should be ≈16.7 ms and `jank`
     * should be 0; anything else is the thing to fix, and it says how much.
     */
    private fun reportGesture() {
        if (gestureFrames == 0) return
        val average = gestureTotalNs / gestureFrames
        val verdict = if (gestureJank == 0) "smooth" else "JANK"
        // Where the frame went, averaged per frame. The first four add up to roughly
        // the frame's main-thread cost; a large `draw`/`gpu` with a small `sync`/
        // `layout` means the pixels are the problem, not the composition.
        val stages = if (gestureSamples > 0) {
            val n = gestureSamples.toLong()
            " stages(sync/layout/draw/cmd/gpu)=" +
                "${decimalMs(gestureSyncNs / n)}/${decimalMs(gestureLayoutNs / n)}/" +
                "${decimalMs(gestureDrawNs / n)}/${decimalMs(gestureCommandNs / n)}/" +
                "${decimalMs(gestureGpuNs / n)}"
        } else {
            ""
        }
        Log.i(
            TAG,
            "$verdict scroll frames=$gestureFrames slow=$gestureJank " +
                "avg=${ms(average)} worst=${ms(gestureWorstNs)}$stages " +
                // Read, never reset: the two-second window report owns the reset,
                // so a gesture line must not steal its counts.
                "cacheHits=${cacheHits.get()} thumbs=${thumbDecodes.get()} " +
                "pageDecodes=${pageDecodes.get()}"
        )
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

    /**
     * One decimal place: a stage average of 0.4 ms must not print as `0ms`, or a
     * frame that spent its time in the GPU would look like a frame that spent
     * nothing anywhere. `Locale.US` so the separator is always a dot.
     */
    private fun decimalMs(nanos: Long): String =
        String.format(Locale.US, "%.1f", nanos / 1_000_000.0)
}
