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
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
 * ```bash
 * adb logcat -s DocScannerPerf
 * ```
 *
 * # Budgets come from the panel, not from a constant
 *
 * The previous revision compared every frame against a **60 Hz** budget: a "slow"
 * frame was anything over 24 ms and a janky window had a p90 over 18 ms. On a
 * 120 Hz display the slot is 8.33 ms, so every frame between 8.3 and 16.6 ms — a
 * dropped frame, twice as long as it should have been — was reported as `smooth`.
 * The instrumentation could not see the problem it was pointed at.
 *
 * So the budget is now read from the display ([setDisplayRefreshRate], called by
 * `MainActivity` after `FramePacing.apply`), and every number is reported relative
 * to it:
 *
 * | metric | meaning |
 * | :--- | :--- |
 * | `hz` | the rate the panel is running at, as reported by the platform |
 * | `fps` | 1e9 / mean frame interval — what was actually delivered |
 * | `atBudget` | share of intervals that fitted in **1.5 x budget**, i.e. frames that did not miss a vsync |
 * | `missed` | the complement: frames that took more than one and a half slots |
 * | `p90` | 90th-percentile interval, from a 1 ms histogram |
 *
 * A perfect 120 Hz fling reads `hz=120 fps≈120 atBudget=100% missed=0 p90=8ms`.
 *
 * The same lines are mirrored into [lastGesture] and [lastWindow] and rendered by
 * Settings → Performance, because a number that needs `adb`, a cable and a
 * computer to read is a number that never gets reported.
 *
 * Release builds never install the callback: [install] checks the debug flag, and
 * the counters are incremented only while [installed] is true, so the cost in
 * production is a single volatile read per image load.
 */
object PerfTrace {

    private const val TAG = "DocScannerPerf"

    /** Frames per reported window: one second at 120 Hz, two at 60. */
    private const val WINDOW = 120

    @Volatile
    private var installed = false

    /**
     * The panel's rate. Set from the window's display; 60 until something says
     * otherwise, because 60 is the one rate every Android device supports and a
     * wrong-but-safe budget beats an optimistic one that hides jank.
     */
    @Volatile
    private var refreshRateHz: Float = 60f

    /** One vsync slot, in nanoseconds, for the current display. */
    private val frameBudgetNs: Long
        get() = FrameBudget.budgetNs(refreshRateHz)

    /** The interval above which a frame missed its slot. */
    private val missedThresholdNs: Long
        get() = FrameBudget.missedThresholdNs(refreshRateHz)

    /**
     * The two lines the monitor would have printed, for the debug readout in
     * Settings.
     *
     * The log is the right channel for a number you want to analyse; it is the
     * wrong channel for a number you want to *report*, because it needs a cable,
     * a computer and an unlocked bootloader. The same strings are mirrored here so
     * anyone can scroll the library, open Settings and read what happened.
     */
    private val _lastGesture = MutableStateFlow<String?>(null)
    val lastGesture: StateFlow<String?> = _lastGesture.asStateFlow()

    private val _lastWindow = MutableStateFlow<String?>(null)
    val lastWindow: StateFlow<String?> = _lastWindow.asStateFlow()

    /** True when the frame monitor is running: debug builds only. */
    val isEnabled: Boolean get() = installed

    /** The rate being budgeted against, for the Settings readout. */
    val displayHz: Float get() = refreshRateHz

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

    /**
     * Per-gesture interval histogram, in 1 ms buckets.
     *
     * A fling can deliver thousands of frames; storing each interval to sort them
     * would mean a growing array on the frame path — the one thing that must not
     * allocate. A fixed 100-bucket histogram gives an exact-enough p90 in O(1)
     * memory, and `p90 = 8ms` is precise enough to act on.
     */
    private val gestureHistogram = IntArray(HISTOGRAM_BUCKETS)

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
    private var gestureMissed = 0
    private var gestureWorstNs = 0L
    private var gestureTotalNs = 0L
    private var gestureAtBudget = 0

    /** Told by the list when a scroll gesture starts and ends. */
    fun setScrolling(value: Boolean) {
        if (!installed) return
        if (value == scrolling) return
        if (value) {
            resetGesture()
            scrolling = true
        } else {
            scrolling = false
            reportGesture()
        }
    }

    private fun resetGesture() {
        gestureFrames = 0
        gestureMissed = 0
        gestureWorstNs = 0L
        gestureTotalNs = 0L
        gestureAtBudget = 0
        gestureSyncNs = 0L
        gestureLayoutNs = 0L
        gestureDrawNs = 0L
        gestureCommandNs = 0L
        gestureGpuNs = 0L
        gestureSamples = 0
        gestureHistogram.fill(0)
    }

    /**
     * Sets the rate every frame is judged against.
     *
     * Called by `MainActivity` once `FramePacing` has asked the display for its
     * fastest mode — the rate that comes back is the one the app actually gets, not
     * the one the panel could theoretically do.
     */
    fun setDisplayRefreshRate(hz: Float) {
        if (hz <= 1f) return
        if (kotlin.math.abs(hz - refreshRateHz) < 0.5f) return
        refreshRateHz = hz
        if (installed) {
            Log.i(
                TAG,
                "display ${hz.toInt()} Hz, frame budget ${decimalMs(frameBudgetNs)} ms"
            )
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
        // A best-effort rate before the Activity has a window; `MainActivity`
        // refines it immediately afterwards with the real mode.
        setDisplayRefreshRate(FramePacing.rateOf(context))
        val choreographer = Choreographer.getInstance()
        choreographer.postFrameCallback(object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                recordFrame(frameTimeNanos)
                choreographer.postFrameCallback(this)
            }
        })
        Log.i(
            TAG,
            "frame monitor on, window = $WINDOW frames, budget = ${decimalMs(frameBudgetNs)} ms"
        )
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
                gestureCommandNs += metrics.getMetric(FrameMetrics.COMMAND_ISSUE_DURATION)
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

    /** A cover came from a full page decode: the path the scroll work set out to remove. */
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
            if (delta <= missedThresholdNs) gestureAtBudget++ else gestureMissed++
            gestureHistogram[FrameBudget.bucketOf(delta, HISTOGRAM_BUCKETS)]++
        }
    }

    /**
     * One line per gesture.
     *
     * Read it as: `atBudget` is the score, `missed` is how many frames were late,
     * and the stage breakdown says whose work those late frames were. On a 120 Hz
     * phone the target is `fps≈120 atBudget=100% missed=0`.
     */
    private fun reportGesture() {
        if (gestureFrames == 0) return
        val average = gestureTotalNs / gestureFrames
        val verdict = if (gestureMissed == 0) "smooth" else "JANK"
        val atBudget = gestureAtBudget * 100 / gestureFrames
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
        val line = "$verdict scroll hz=${refreshRateHz.toInt()} " +
            "fps=${FrameBudget.decimal(FrameBudget.fpsOf(average))} " +
            "frames=$gestureFrames atBudget=$atBudget% missed=$gestureMissed " +
            "p90=${p90Label()} avg=${decimalMs(average)} worst=${decimalMs(gestureWorstNs)}$stages " +
            // Read, never reset: the two-second window report owns the reset,
            // so a gesture line must not steal its counts.
            "cacheHits=${cacheHits.get()} thumbs=${thumbDecodes.get()} " +
            "pageDecodes=${pageDecodes.get()}"
        Log.i(TAG, line)
        _lastGesture.value = line
    }

    private fun report() {
        val sorted = frameTimes.copyOf(WINDOW)
        sorted.sort()
        val p50 = sorted[WINDOW / 2]
        val p90 = sorted[(WINDOW * 9) / 10]
        val max = sorted[WINDOW - 1]
        // Both thresholds scale with the panel: 1.25 x budget at p90 is a window
        // that visibly hitched, and one frame over two slots is a dropped frame
        // whatever the refresh rate is.
        val janky = FrameBudget.isJankyWindow(refreshRateHz, p90, max)

        // A clean window is only worth a line occasionally, so a healthy session
        // does not fill the log with noise that hides the interesting window.
        windows++
        if (!janky && windows % 5 != 0) return

        val detail = "hz=${refreshRateHz.toInt()} frames=$WINDOW " +
            "fps=${FrameBudget.decimal(FrameBudget.fpsOf(p50))} p50=${decimalMs(p50)} " +
            "p90=${decimalMs(p90)} max=${decimalMs(max)} " +
            "cacheHits=${cacheHits.getAndSet(0)} thumbs=${thumbDecodes.getAndSet(0)} " +
            "pageDecodes=${pageDecodes.getAndSet(0)}"
        val line = if (janky) "JANK $detail" else "smooth $detail"
        Log.i(TAG, line)
        _lastWindow.value = line
    }

    /**
     * 90th-percentile frame interval for the gesture, from the 1 ms histogram.
     *
     * `8ms` for a p90 that lives in the 8 ms bucket is as precise as a frame time
     * needs to be, and it costs no allocation on the frame path. Arithmetic lives in
     * [FrameBudget] so it can be tested without a device.
     */
    private fun p90Label(): String {
        val p90 = FrameBudget.percentile90Ms(gestureHistogram)
        return when {
            p90 < 0 -> "0ms"
            p90 >= HISTOGRAM_BUCKETS -> ">${HISTOGRAM_BUCKETS - 1}ms"
            else -> "${p90}ms"
        }
    }

    /**
     * One decimal place: a stage average of 0.4 ms must not print as `0ms`, or a
     * frame that spent its time in the GPU would look like a frame that spent
     * nothing anywhere. `Locale.US` so the separator is always a dot.
     */
    private fun decimalMs(nanos: Long): String = FrameBudget.decimalMs(nanos)

    /** 1 ms buckets up to 99 ms; anything slower lands in the last one. */
    private const val HISTOGRAM_BUCKETS = 100
}
