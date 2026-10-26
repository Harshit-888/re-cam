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
 * layout, the density or the pixels being drawn — and it never claims
 * `FIXED_SOURCE` behaviour, which would forbid the platform from stepping the rate
 * down when this app cannot sustain it. Claiming a rate and then missing vsyncs is
 * worse than not asking at all.
 *
 * # Which field to set, per API level (read off AOSP, not remembered)
 *
 * `Window` has no `setFrameRate`; that method is on `Surface`, and a Compose
 * activity has no public handle on the window's surface before it is attached. The
 * sanctioned pre-attach route is `WindowManager.LayoutParams`, and the two relevant
 * fields do **not** behave the same way:
 *
 * | API | what to set | why |
 * | :--- | :--- | :--- |
 * | 34+ (`UPSIDE_DOWN_CAKE`) | `preferredRefreshRate` only | documented as "use in favour of `preferredDisplayModeId` for applications that want to specify the refresh rate, but do not want to specify a preference for any other displayMode properties (e.g. resolution)" — and from 34 it accepts any rate, the OS picks the closest match |
 * | 23–33 | `preferredDisplayModeId` | window-local and reliable. `preferredRefreshRate` before 34 "will be applied to the **display's default mode**", i.e. it can change the panel's mode for every app, not just this window |
 *
 * The trap: **`preferredRefreshRate` is ignored whenever `preferredDisplayModeId`
 * is set.** Setting both — which an earlier revision of this file did — looks like
 * asking twice and is actually asking once, via the field that pins the resolution.
 * So exactly one of them is written, ever.
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

        // Only ask when it is actually faster, so a device already at its best mode
        // never pays for a window relayout.
        if (fastest.refreshRate > safeRefreshRate(display) + 1f) {
            val attributes = window.attributes
            var changed = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Rate only — resolution untouched, because that is what was asked
                // for. Any display-mode preference must be cleared first: the mode
                // id wins over the rate and would silently make this a no-op.
                if (attributes.preferredDisplayModeId != 0) {
                    attributes.preferredDisplayModeId = 0
                    changed = true
                }
                if (attributes.preferredRefreshRate != fastest.refreshRate) {
                    attributes.preferredRefreshRate = fastest.refreshRate
                    changed = true
                }
            } else if (fastest.modeId > 0) {
                // Window-local on older releases. `preferredRefreshRate` is not
                // used here on purpose: before 34 it applies to the *display's*
                // default mode, which is a system-wide change from a scanner app.
                if (attributes.preferredDisplayModeId != fastest.modeId) {
                    attributes.preferredDisplayModeId = fastest.modeId
                    changed = true
                }
            } else if (attributes.preferredRefreshRate != fastest.refreshRate) {
                // No usable mode id: the rate hint is all that is left.
                attributes.preferredRefreshRate = fastest.refreshRate
                changed = true
            }

            if (changed) {
                window.attributes = attributes
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
