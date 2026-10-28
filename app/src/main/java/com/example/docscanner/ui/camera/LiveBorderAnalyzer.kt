package com.example.docscanner.ui.camera

import android.os.SystemClock
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.docscanner.util.DocumentQuad
import com.example.docscanner.util.GreyFrame
import com.example.docscanner.util.GreyFrames
import com.example.docscanner.util.LiveBorderTracker
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Feeds the viewfinder's border outline: one `DocumentQuad` detection per analysed
 * frame, smoothed by [LiveBorderTracker].
 *
 * ## Why the cheap detector, and not the model
 *
 * The post-capture path runs `MlBorderDetector` (the original app's
 * `border_detect_224.tflite`) *and* `DocumentQuad`, because it runs once and the
 * user is about to look at the result. The viewfinder runs continuously, so it uses
 * the pure-geometry pass only: no TFLite interpreter, no 224×224 tensor, no
 * per-frame allocation beyond one luma array. The two detectors agree on the
 * overwhelming majority of pages — the model exists to rescue the hard ones — and a
 * live outline that is occasionally a few pixels off is *advisory*, while a live
 * outline that costs 30 ms of a 16 ms frame budget is a stutter the user feels.
 *
 * The crop is re-detected from the full-resolution capture after the shutter, so a
 * rough live outline can never become a rough crop.
 *
 * ## Cost
 *
 * * **Resolution.** The frame is box-averaged down to [ANALYSIS_WIDTH] px wide
 *   before anything else happens. `DocumentDetector` uses 360 for the same
 *   reason: at 360 the page edge is still 2–3 px of ramp, and everything above it
 *   is paid for in milliseconds and buys no accuracy.
 * * **Rate.** At most one detection per [MIN_INTERVAL_MS]. CameraX hands over
 *   frames as fast as the sensor produces them; `STRATEGY_KEEP_ONLY_LATEST` means
 *   the backlog is dropped at the source rather than queued here, and the interval
 *   caps the CPU at ~6 detections a second. By budget arithmetic that is a few
 *   milliseconds of one core, six times a second, against a preview that is
 *   otherwise idle — but it is *arithmetic*, not a measurement: the honest number
 *   comes from a device trace (`DocScannerPerf`, `dumpsys gfxinfo`).
 * * **Nothing is retained.** The luma array is local to the frame, and the
 *   `ImageProxy` is always closed, including on the early return.
 */
class LiveBorderAnalyzer(
    /**
     * Called on the analysis thread with the state to draw and the width ÷ height
     * of the frame it was measured on.
     *
     * The aspect is part of the callback because the outline is only placeable if
     * the view knows the shape of the frame it came from: a stream that is not 4:3
     * is letterboxed inside the viewfinder, and normalised corners mapped as if it
     * filled the box land off the page.
     */
    private val onState: (LiveBorderTracker.State, Float) -> Unit
) : ImageAnalysis.Analyzer {

    private val tracker = LiveBorderTracker()
    private var lastRunAt = 0L

    /** Set by the screen when the camera is unbound; stops all further work. */
    @Volatile
    var stopped: Boolean = false

    override fun analyze(image: ImageProxy) {
        try {
            if (stopped) return
            val now = SystemClock.elapsedRealtime()
            if (now - lastRunAt < MIN_INTERVAL_MS) return
            lastRunAt = now

            val grey = greyOf(image) ?: return
            val upright = GreyFrames.rotate(grey, image.imageInfo.rotationDegrees)
            val found = DocumentQuad.detect(upright.pixels, upright.width, upright.height)
            val state = tracker.onFrame(found?.corners, found?.sidesFound ?: 0)
            val aspect = upright.width.toFloat() / upright.height.toFloat()
            if (!stopped) onState(state, aspect)
        } catch (_: Throwable) {
            // Detection is an aid. A frame that cannot be read (a vendor YUV layout,
            // a closed image, a 0×0 buffer) drops the outline for that frame and
            // nothing else: `LiveBorderTracker` decays visibility on its own, so a
            // stream of failures ends with no outline rather than a stale one.
        } finally {
            runCatching { image.close() }
        }
    }

    fun reset() = tracker.reset()

    // ------------------------------------------------------------ frame → luma

    /**
     * The Y plane of a YUV_420_888 frame, box-averaged down to [ANALYSIS_WIDTH]
     * and normalised to 0–255.
     *
     * Only the Y plane is read. Colour buys a border detector nothing: the page
     * edge is a *luminance* step, which is exactly why `DocumentQuad` works in
     * greyscale in the first place.
     */
    private fun greyOf(image: ImageProxy): GreyFrame? {
        if (image.format != android.graphics.ImageFormat.YUV_420_888) return null
        val srcW = image.width
        val srcH = image.height
        if (srcW < MIN_SOURCE || srcH < MIN_SOURCE) return null

        val plane = image.planes.firstOrNull() ?: return null
        val buffer = plane.buffer
        if (buffer == null || !buffer.hasRemaining()) return null
        // The row stride is *not* the width: camera buffers are padded to an
        // alignment (often 64 or 128 px), so reading it as w would shear the image
        // progressively — and a sheared frame is exactly the kind of input that
        // makes a detector look random.
        val rowStride = plane.rowStride
        if (rowStride <= 0) return null

        // The buffer is read through a `duplicate()`: camera2 hands the same
        // `ByteBuffer` back with each frame, and its position may already be ahead
        // of zero. Reading absolute indices needs a copy of the position/limit, not
        // a copy of the pixels.
        val step = max(1, (srcW.toFloat() / ANALYSIS_WIDTH).roundToInt())
        val outW = (srcW / step).coerceAtLeast(1)
        val outH = (srcH / step).coerceAtLeast(1)
        if (outW < MIN_SOURCE || outH < MIN_SOURCE) return null

        val out = FloatArray(outW * outH)
        val bytes = buffer.duplicate()
        val limit = bytes.limit()
        for (oy in 0 until outH) {
            for (ox in 0 until outW) {
                // Box average: at step ≥ 2 this is a real downsample rather than a
                // point sample, so a 1 px page edge cannot be skipped between
                // sampled pixels — the exact failure that makes an outline jump
                // between nothing and a page.
                var sum = 0
                var count = 0
                val baseY = oy * step
                val baseX = ox * step
                for (dy in 0 until step) {
                    val y = baseY + dy
                    if (y >= srcH) break
                    val rowStart = y * rowStride
                    for (dx in 0 until step) {
                        val x = baseX + dx
                        if (x >= srcW) break
                        val index = rowStart + x
                        if (index in 0 until limit) {
                            sum += bytes.get(index).toInt() and 0xFF
                            count++
                        }
                    }
                }
                out[oy * outW + ox] = if (count > 0) sum.toFloat() / count else 0f
            }
        }
        return GreyFrame(outW, outH, out)
    }

    companion object {
        /** Detection width. Matches `DocumentDetector.WORK_WIDTH` for continuity. */
        const val ANALYSIS_WIDTH = 360

        /** Floor for pixels per detection ≈ 6 per second. */
        const val MIN_INTERVAL_MS = 150L

        /** Smaller than this and a page edge cannot be resolved at all. */
        private const val MIN_SOURCE = 48
    }
}
