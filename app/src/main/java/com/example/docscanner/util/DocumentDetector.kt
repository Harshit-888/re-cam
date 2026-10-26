package com.example.docscanner.util

import android.graphics.Bitmap
import com.example.docscanner.model.QuadCorners
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Document-border detection: finds the four corners of the page in a photo.
 *
 * This object owns the *bitmap* half of the job — scale the capture down, read its
 * pixels, turn them into brightness — and hands the geometry to [DocumentQuad],
 * which has no Android type in it and is therefore covered by `DocumentQuadTest`.
 *
 * ## History
 * 1. **Brightness bounding box** — could not express perspective at all.
 * 2. **Extreme-point quad over Sobel edges** — the page edge competed with
 *    interior text, so a page that filled the frame produced an invalid quad and
 *    silently fell back to "no crop".
 * 3. **First-edge-from-the-frame scan** — better on tilted pages, but it took the
 *    *first* strong gradient along a row, so dense text won over the paper edge.
 * 4. **Background-step detection** — the photo is reduced to a "background" image
 *    (grey, closed morphologically to erase ink, box blurred) and each side is
 *    located as the 50 % level crossing between the paper and whatever it lies on.
 *    Ink cannot satisfy that test, so a text block cannot masquerade as a page.
 *    This is the algorithm still used, now in [DocumentQuad].
 *
 * ## Why the level crossing, and not the strongest gradient
 * A page edge is a step blurred by the optics. The 50 % crossing of a blurred step
 * is exactly the step position, so interpolation between two pixels gives a
 * sub-pixel, unbiased answer. The maximum of a finite difference is *not*
 * unbiased: across the ramp of a blurred step the difference is flat for several
 * pixels, so its peak wanders by up to half a centimetre on the working image.
 *
 * ## Run-off sides
 * When the page continues past the frame edge there is no crossing to find, and
 * the side stays unfitted. [DocumentQuad] then substitutes the frame edge, which
 * is the geometrically correct answer. That is why [Detection.confident] separates
 * "found three or four real sides" from "some sides are assumptions".
 *
 * Detection never throws. Everything is validated for convexity, area, side length
 * and slope before it is returned.
 */
object DocumentDetector {

    /** Resolution the geometry runs at. Going wider buys nothing measurable. */
    const val WORK_WIDTH = 360

    /** Below this the source is too small to say anything useful. */
    private const val MIN_SOURCE_SIDE = 48

    /** Result of a detection attempt. */
    data class Detection(
        val corners: QuadCorners,
        /**
         * True when at least three of the four sides were fitted from real
         * background steps. False means one or more sides fell back to the frame
         * edge, or nothing plausible was found — the UI should invite manual
         * adjustment.
         */
        val confident: Boolean,
        /**
         * How many sides were found as real edges, 0–4.
         *
         * Exposed because "the borders are wrong" and "the borders were not found
         * at all" need different words in the UI, and because it is the first thing
         * worth printing in a bug report. `0` with [quadCorners] full frame means
         * either a page filling the frame or a featureless photo.
         */
        val sidesFound: Int = 0
    )

    /**
     * Detects the document quad.
     *
     * @return the quad plus a confidence flag. Never throws; on any failure it
     *   returns [QuadCorners.FULL_FRAME] with `confident = false`.
     */
    fun detect(src: Bitmap): Detection = try {
        detectInternal(src)
    } catch (_: Throwable) {
        failed()
    }

    /** Convenience for call sites that only need the corners. */
    fun detectQuad(src: Bitmap): QuadCorners = detect(src).corners

    // ------------------------------------------------------------------ core

    private fun failed() = Detection(QuadCorners.FULL_FRAME, confident = false, sidesFound = 0)

    private fun detectInternal(src: Bitmap): Detection {
        if (src.isRecycled) return failed()
        val luma = lumaOf(src) ?: return failed()
        val found = DocumentQuad.detect(luma.pixels, luma.width, luma.height) ?: return failed()
        return Detection(
            corners = found.corners,
            // Zero sides is the featureless-frame answer: the page fills the frame,
            // or there is no page. Both are correctly "nothing to crop", so it is
            // not reported as a failure the user has to fix by hand.
            confident = found.sidesFound >= 3 || found.sidesFound == 0,
            sidesFound = found.sidesFound
        )
    }

    /** Brightness of the working-resolution copy of [src], row-major. */
    private fun lumaOf(src: Bitmap): Luma? {
        if (src.width < MIN_SOURCE_SIDE || src.height < MIN_SOURCE_SIDE) return null
        val workingWidth = min(src.width, WORK_WIDTH)
        val workingHeight = max(
            MIN_SOURCE_SIDE,
            (src.height * (workingWidth.toFloat() / src.width)).roundToInt()
        )
        if (workingWidth < MIN_SOURCE_SIDE || workingHeight < MIN_SOURCE_SIDE) return null
        val scaled = if (workingWidth == src.width && workingHeight == src.height) {
            src
        } else {
            try {
                Bitmap.createScaledBitmap(src, workingWidth, workingHeight, true)
            } catch (_: Throwable) {
                return null
            }
        }
        val w = scaled.width
        val h = scaled.height
        val pixels = IntArray(w * h)
        try {
            scaled.getPixels(pixels, 0, w, 0, 0, w, h)
        } catch (_: Throwable) {
            return null
        } finally {
            if (scaled !== src && !scaled.isRecycled) scaled.recycle()
        }
        val luma = FloatArray(w * h)
        for (i in pixels.indices) {
            val p = pixels[i]
            luma[i] = 0.299f * ((p ushr 16) and 0xFF) +
                0.587f * ((p ushr 8) and 0xFF) +
                0.114f * (p and 0xFF)
        }
        return Luma(w, h, luma)
    }

    private class Luma(val width: Int, val height: Int, val pixels: FloatArray)
}
