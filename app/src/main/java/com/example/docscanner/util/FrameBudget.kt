package com.example.docscanner.util

import java.util.Locale

/**
 * The arithmetic behind [PerfTrace], separated so it can be *tested*.
 *
 * Frame timing used to be a set of constants: a "slow" frame was over 24 ms and a
 * janky window had a p90 over 18 ms — both derived from 60 Hz and both wrong on a
 * 120 Hz panel, where the slot is 8.33 ms. The fix is not a different constant; it
 * is deriving every threshold from the rate the display actually reports.
 *
 * That makes the numbers a pure function of `(refreshRate, intervals)`, which means
 * they can be checked on the JVM instead of argued about on a phone. There is no
 * Android type in this file for exactly that reason.
 */
internal object FrameBudget {

    /** Slowest and fastest rates worth believing; anything else falls back to 60. */
    private const val MIN_HZ = 30f
    private const val MAX_HZ = 240f

    /** The rate every Android device supports, and the safe assumption. */
    const val DEFAULT_HZ = 60f

    /** A frame is late once it takes half a slot longer than its budget. */
    const val MISSED_FRAME_FACTOR = 1.5f

    /** A window whose p90 exceeds this multiple of the budget was visibly uneven. */
    const val JANK_P90_FACTOR = 1.25f

    /** One vsync slot, in nanoseconds. */
    fun budgetNs(hz: Float): Long =
        (1_000_000_000.0 / hz.coerceIn(MIN_HZ, MAX_HZ)).toLong()

    /**
     * The interval above which a frame missed its slot.
     *
     * 1.5 x budget is where the compositor had to hold the previous frame for an
     * extra vsync: the shortest delay a user can see, at any refresh rate. It is
     * the same idea as Android's own "slow frame" definition, expressed as a
     * multiple instead of a fixed 24 ms.
     */
    fun missedThresholdNs(hz: Float): Long =
        // Double, not Float: a `Long * Float` product rounds at ~16.7 M, which is
        // inside the range of these values, and rounding the threshold changes
        // whether a frame exactly on it counts as late.
        (budgetNs(hz) * MISSED_FRAME_FACTOR.toDouble()).toLong()

    /** True when a frame interval took longer than its slot allowed. */
    fun isMissed(hz: Float, intervalNs: Long): Boolean = intervalNs > missedThresholdNs(hz)

    /** True when a whole window's p90 says the scroll was uneven. */
    fun isJankyWindow(hz: Float, p90Ns: Long, maxNs: Long): Boolean =
        p90Ns >= (budgetNs(hz) * JANK_P90_FACTOR.toDouble()).toLong() ||
            maxNs >= budgetNs(hz) * 2

    /** 1 ms histogram bucket for an interval, clamped into the histogram. */
    fun bucketOf(intervalNs: Long, buckets: Int): Int =
        (intervalNs / 1_000_000L).toInt().coerceIn(0, buckets - 1)

    /**
     * 90th-percentile frame interval in milliseconds, from a 1 ms histogram, or
     * `-1` when nothing was recorded.
     *
     * Bucket `i` holds intervals in `[i, i+1)` ms, so `i` **is** the millisecond
     * figure: a bucket of 8.33 ms frames is reported as 8 ms, not 9.
     *
     * `-1` rather than `0` because "no frames" and "instant frames" must not print
     * the same thing.
     */
    fun percentile90Ms(histogram: IntArray): Int {
        val total = histogram.sum()
        if (total == 0) return -1
        // ceil(0.9 * total), in integer arithmetic.
        val target = (total * 9 + 9) / 10
        var cumulative = 0
        for (bucket in histogram.indices) {
            cumulative += histogram[bucket]
            if (cumulative >= target) return bucket
        }
        return histogram.size - 1
    }

    /** Frames per second implied by a mean frame interval. */
    fun fpsOf(meanIntervalNs: Long): Double =
        if (meanIntervalNs <= 0L) 0.0 else 1_000_000_000.0 / meanIntervalNs

    /** One decimal place, always with a dot, for a log line. */
    fun decimal(value: Double): String = String.format(Locale.US, "%.1f", value)

    /** Milliseconds to one decimal place, for a log line. */
    fun decimalMs(nanos: Long): String = String.format(Locale.US, "%.1f", nanos / 1_000_000.0)
}
