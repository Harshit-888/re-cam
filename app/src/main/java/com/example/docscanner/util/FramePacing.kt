package com.example.docscanner.util

import android.content.Context
import android.os.Build
import android.view.Display
import android.view.Window

/**
 * Asks the display to run this app at its fastest mode.
 *
 * # Why an app can sit at 60 fps on a 120 Hz phone
 *
 * A high-refresh panel does not hand its fastest mode to every app. The compositor
 * picks a mode per window from `WindowManager.LayoutParams.preferredDisplayModeId`
 * (and, from API 30, from the frame rate the window declares), and an app that
 * declares nothing gets the safe default — 60 Hz — because most apps cannot sustain
 * more and a dropped frame is more visible than a steady lower rate.
 *
 * So "the app is not running at 120 fps" has two possible causes, and they need
 * opposite fixes:
 *
 * 1. **The display was never asked for 120.** Nothing in this app ever set a
 *    preferred mode, so on many devices the answer was 60 before the first frame
 *    was drawn. That is what this file fixes.
 * 2. **The app cannot fill 120.** Then the honest fix is to make each frame
 *    cheaper, and [PerfTrace] now measures against the *real* budget so that can
 *    be proven rather than guessed.
 *
 * It is deliberately conservative: it only ever selects a mode **with the same
 * physical resolution**, so unlocking the refresh rate can never change the
 * layout, the density or the pixels being drawn — and it asks with
 * `FRAME_RATE_COMPATIBILITY_DEFAULT` rather than `FIXED_SOURCE`, which leaves the
 * system free to step down if this app cannot sustain the rate. Claiming
 * `FIXED_SOURCE` and then missing vsyncs is worse than not asking at all.
 */
object FramePacing {

    /** What the panel is doing, and what it could do. */
    data class Rate(val currentHz: Float, val maxHz: Float) {
        val isLatchedDown: Boolean get() = maxHz > currentHz + 1f
    }

    /**
     * Selects the fastest same-resolution mode and records the resulting rate for
     * [PerfTrace]. Returns the rate the app should budget against.
     *
     * Safe to call more than once, and safe on a device with a single mode (then it
     * is a no-op that just reports 60).
     */
    fun apply(window: Window): Float {
        val display = displayOf(window) ?: return DEFAULT_HZ
        val fastest = fastestMode(display) ?: return safeRefreshRate(display)

        // Only ask for a mode when it is actually faster, so a device that is
        // already at its best mode never pays for a window relayout.
        if (fastest.refreshRate > safeRefreshRate(display) + 1f && fastest.modeId >= 0) {
            val attributes = window.attributes
            if (attributes.preferredDisplayModeId != fastest.modeId) {
                attributes.preferredDisplayModeId = fastest.modeId
                window.attributes = attributes
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // DEFAULT, not FIXED_SOURCE: this states the rate the content is
            // produced at and lets the compositor keep its ability to step down.
            runCatching {
                window.setFrameRate(fastest.refreshRate, Window.FRAME_RATE_COMPATIBILITY_DEFAULT)
            }
        }
        return fastest.refreshRate
    }

    /** The rate the panel is running at right now, for budgeting and for the logs. */
    fun currentRate(window: Window): Rate {
        val display = displayOf(window) ?: return Rate(DEFAULT_HZ, DEFAULT_HZ)
        val current = safeRefreshRate(display)
        val fastest = fastestMode(display)
        return Rate(current, maxOf(current, fastest?.refreshRate ?: current))
    }

    /**
     * The fastest supported mode that keeps the current physical resolution.
     *
     * Resolutions are compared rather than mode ids because the ids are opaque and
     * vendor-defined; two modes of the same size are the same picture, only at
     * different rates.
     */
    private fun fastestMode(display: Display): Display.Mode? = runCatching {
        val current = display.mode
        display.supportedModes
            .asSequence()
            .filter {
                it.physicalWidth == current.physicalWidth &&
                    it.physicalHeight == current.physicalHeight
            }
            .maxByOrNull { it.refreshRate }
    }.getOrNull()

    /** The live refresh rate of the current mode, or 60 when it cannot be read. */
    private fun safeRefreshRate(display: Display): Float = runCatching {
        display.mode.refreshRate.takeIf { it in 1f..360f } ?: display.refreshRate
    }.getOrNull()?.takeIf { it in 1f..360f } ?: DEFAULT_HZ

    @Suppress("DEPRECATION")
    private fun displayOf(window: Window): Display? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.context.display
        } else {
            window.windowManager.defaultDisplay
        }
    }.getOrNull()

    /** Best-effort rate for a context with no window (used by [PerfTrace.install]). */
    @Suppress("DEPRECATION")
    fun rateOf(context: Context): Float = runCatching {
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager).defaultDisplay
        }
        display?.let { safeRefreshRate(it) }
    }.getOrNull() ?: DEFAULT_HZ

    /** 60: the rate every Android device supports, and the safe assumption. */
    private const val DEFAULT_HZ = 60f
}
