package com.example.docscanner.util

import android.graphics.Bitmap
import com.example.docscanner.model.FilterType
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Real document enhancement — **not** a global colour matrix.
 *
 * ## Why this exists
 * The previous implementation applied five hard-coded [android.graphics.ColorMatrix]
 * transforms (contrast ×1.35 + brightness +15 for *Magic Color*, contrast ×2.4 −140
 * for *Clear B&W*, …). A global matrix cannot do what a scanner must do, so the
 * results looked flat and washed out or clipped to pure black:
 *
 * - it cannot **remove shadows / uneven lighting** (a phone flash always creates a
 *   gradient across the page), and
 * - it cannot **white-balance** (indoor light is yellow, shade is blue), and
 * - ×2.4 contrast on a mid-grey page slams everything to 0 or 255.
 *
 * ## What it does instead
 * 1. **Illumination normalisation.** The paper brightness is estimated locally with
 *    a block-max (dilation) pyramid, blurred and bilinearly sampled. Dividing by it
 *    maps *every* patch of paper to the same white regardless of shadows —
 *    this is the single biggest quality difference.
 * 2. **Grey-world white balance** on the normalised result.
 * 3. **Percentile contrast stretch** (0.4 % → 99.6 %) so the page actually uses the
 *    full tonal range instead of a fixed contrast multiplier.
 * 4. **Saturation boost** for *Magic Color* (colour stamps, signatures, logos).
 * 5. **A smoothstep curve** to deepen ink while keeping paper at pure white.
 * 6. *Clear B&W* uses **Otsu thresholding on the normalised luminance** with a
 *    narrow ramp so glyph edges stay anti-aliased instead of turning into jaggies.
 *
 * Processing happens on a downscale of at most [MAX_PROCESS_DIMENSION] px on the
 * long edge. That is ~200 DPI for A4, keeps a bake under a few hundred
 * milliseconds, and avoids multi-megabyte per-pixel allocations on low-end
 * devices.
 *
 * Everything here is blocking CPU work: call it from `Dispatchers.Default`/`IO`,
 * never from composition.
 */
object DocumentFilters {

    /** Longest edge processed for a baked page. */
    const val MAX_PROCESS_DIMENSION = 2000

    /** Longest edge used for the live editor preview. */
    const val PREVIEW_DIMENSION = 900

    /** What normalised paper is mapped to, so white stays white but not clipped. */
    private const val TARGET_PAPER_WHITE = 244f

    /** Floor for the background estimate; prevents amplifying sensor noise. */
    private const val MIN_BACKGROUND = 26f

    /** Ceiling on the illumination gain for very dark patches. */
    private const val MAX_GAIN = 6f

    /** Never stretch a channel over a narrower range than this (avoids noise blow-up). */
    private const val MIN_STRETCH_RANGE = 12

    /**
     * Enhances [src] with the algorithms above.
     *
     * @return a **new** bitmap, except for [FilterType.ORIGINAL], where [src] is
     *   returned unchanged (callers must compare identity before recycling).
     */
    fun apply(
        src: Bitmap,
        type: FilterType,
        maxDimension: Int = MAX_PROCESS_DIMENSION
    ): Bitmap {
        if (type == FilterType.ORIGINAL || src.isRecycled) return src
        val (w, h, pixels) = rgbaOf(src, maxDimension) ?: return src

        val lum = FloatArray(w * h)
        var i = 0
        while (i < lum.size) {
            val p = pixels[i]
            lum[i] = 0.299f * ((p ushr 16) and 0xFF) +
                0.587f * ((p ushr 8) and 0xFF) +
                0.114f * (p and 0xFF)
            i++
        }
        val background = estimateBackground(lum, w, h)

        when (type) {
            FilterType.MAGIC_COLOR -> magicColor(pixels, background, w, h)
            FilterType.LIGHTEN -> lighten(pixels, background, w, h)
            FilterType.GRAYSCALE -> grayscale(pixels, lum, background, w, h)
            FilterType.CLEAR_BW -> clearBlackWhite(pixels, lum, background, w, h)
            FilterType.ORIGINAL -> Unit
        }

        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also {
            it.setPixels(pixels, 0, w, 0, 0, w, h)
        }
    }

    /** Convenience wrapper for live previews at a reduced resolution. */
    fun applyPreview(src: Bitmap, type: FilterType): Bitmap =
        apply(src, type, PREVIEW_DIMENSION)

    // ------------------------------------------------------------ pixel access

    private fun rgbaOf(src: Bitmap, maxDimension: Int): Triple<Int, Int, IntArray>? {
        if (maxDimension <= 0) return null
        val longest = max(src.width, src.height)
        val working: Bitmap = if (longest <= maxDimension) {
            src
        } else {
            val ratio = maxDimension.toFloat() / longest
            val w = max(2, (src.width * ratio).roundToInt())
            val h = max(2, (src.height * ratio).roundToInt())
            try {
                Bitmap.createScaledBitmap(src, w, h, true)
            } catch (_: Throwable) {
                return null
            }
        }
        val w = working.width
        val h = working.height
        if (w < 2 || h < 2) {
            if (working !== src) working.recycle()
            return null
        }
        val pixels = IntArray(w * h)
        return try {
            working.getPixels(pixels, 0, w, 0, 0, w, h)
            Triple(w, h, pixels)
        } catch (_: Throwable) {
            null
        } finally {
            if (working !== src && !working.isRecycled) working.recycle()
        }
    }

    // ------------------------------------------------- illumination estimation

    /**
     * A coarse paper-brightness map (~1 sample per [block] px) sampled bilinearly.
     * Kept small on purpose: a full-resolution float map would cost tens of MB.
     */
    private class Background(
        val values: FloatArray,
        val gw: Int,
        val gh: Int,
        val block: Int
    ) {
        fun at(x: Int, y: Int): Float {
            val fx = ((x + 0.5f) / block - 0.5f).coerceIn(0f, (gw - 1).toFloat())
            val fy = ((y + 0.5f) / block - 0.5f).coerceIn(0f, (gh - 1).toFloat())
            val x0 = fx.toInt().coerceIn(0, gw - 1)
            val y0 = fy.toInt().coerceIn(0, gh - 1)
            val x1 = min(gw - 1, x0 + 1)
            val y1 = min(gh - 1, y0 + 1)
            val tx = fx - x0
            val ty = fy - y0
            val i00 = y0 * gw + x0
            val i01 = y0 * gw + x1
            val i10 = y1 * gw + x0
            val i11 = y1 * gw + x1
            val top = values[i00] + (values[i01] - values[i00]) * tx
            val bottom = values[i10] + (values[i11] - values[i10]) * tx
            return top + (bottom - top) * ty
        }
    }

    /**
     * Block-**max** (dilation) rather than block-average: paper is the brightest
     * thing in any patch containing print, so the max rejects text and ink while
     * keeping the paper level. A blur then smooths the block grid, and the result
     * is upsampled on demand. This is a cheap "rolling ball" background model.
     */
    private fun estimateBackground(lum: FloatArray, w: Int, h: Int): Background {
        val block = max(6, max(w, h) / 72)
        val gw = max(1, (w + block - 1) / block)
        val gh = max(1, (h + block - 1) / block)
        var grid = FloatArray(gw * gh)

        for (gy in 0 until gh) {
            val y0 = gy * block
            val y1 = min(h, y0 + block)
            for (gx in 0 until gw) {
                val x0 = gx * block
                val x1 = min(w, x0 + block)
                var peak = 0f
                for (y in y0 until y1) {
                    val row = y * w
                    for (x in x0 until x1) {
                        val v = lum[row + x]
                        if (v > peak) peak = v
                    }
                }
                grid[gy * gw + gx] = peak
            }
        }

        repeat(2) { grid = boxBlur3(grid, gw, gh) }
        return Background(grid, gw, gh, block)
    }

    private fun boxBlur3(src: FloatArray, w: Int, h: Int): FloatArray {
        val horizontal = FloatArray(src.size)
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                val centre = src[row + x]
                val left = if (x > 0) src[row + x - 1] else centre
                val right = if (x < w - 1) src[row + x + 1] else centre
                horizontal[row + x] = (left + centre + right) / 3f
            }
        }
        val out = FloatArray(src.size)
        for (y in 0 until h) {
            val centreRow = y * w
            val upRow = (if (y > 0) y - 1 else y) * w
            val downRow = (if (y < h - 1) y + 1 else y) * w
            for (x in 0 until w) {
                out[centreRow + x] =
                    (horizontal[upRow + x] + horizontal[centreRow + x] + horizontal[downRow + x]) / 3f
            }
        }
        return out
    }

    private fun gainFor(background: Float): Float {
        val safe = if (background < MIN_BACKGROUND) MIN_BACKGROUND else background
        val gain = TARGET_PAPER_WHITE / safe
        return if (gain > MAX_GAIN) MAX_GAIN else gain
    }

    // ---------------------------------------------------------------- filters

    /**
     * *Magic Color*: shadow-free colour with balanced whites and punchy ink.
     * This is the filter that should look like the legacy app's default.
     */
    private fun magicColor(pixels: IntArray, background: Background, w: Int, h: Int) {
        val stride = if (pixels.size > 400_000) 3 else 1

        // Pass 1 — grey-world white balance from shadow-normalised channel means.
        var sumR = 0.0
        var sumG = 0.0
        var sumB = 0.0
        var count = 0
        var y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                val p = pixels[y * w + x]
                val gain = gainFor(background.at(x, y))
                sumR += ((p ushr 16) and 0xFF) * gain
                sumG += ((p ushr 8) and 0xFF) * gain
                sumB += (p and 0xFF) * gain
                count++
                x += stride
            }
            y += stride
        }
        if (count == 0) return

        val meanR = max(1f, (sumR / count).toFloat())
        val meanG = max(1f, (sumG / count).toFloat())
        val meanB = max(1f, (sumB / count).toFloat())
        val meanAll = (meanR + meanG + meanB) / 3f
        // Clamped: a page that is legitimately blue-tinted (a form, a photo) must
        // not be neutralised into grey.
        val wbR = (meanAll / meanR).coerceIn(0.84f, 1.20f)
        val wbG = (meanAll / meanG).coerceIn(0.84f, 1.20f)
        val wbB = (meanAll / meanB).coerceIn(0.84f, 1.20f)

        // Pass 2 — histograms after white balance, for the percentile stretch.
        val histR = IntArray(256)
        val histG = IntArray(256)
        val histB = IntArray(256)
        y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                val p = pixels[y * w + x]
                val gain = gainFor(background.at(x, y))
                val r = ((p ushr 16) and 0xFF) * gain * wbR
                val g = ((p ushr 8) and 0xFF) * gain * wbG
                val b = (p and 0xFF) * gain * wbB
                histR[r.toInt().coerceIn(0, 255)]++
                histG[g.toInt().coerceIn(0, 255)]++
                histB[b.toInt().coerceIn(0, 255)]++
                x += stride
            }
            y += stride
        }

        val lowR = percentile(histR, 0.004f)
        val highR = percentile(histR, 0.996f)
        val lowG = percentile(histG, 0.004f)
        val highG = percentile(histG, 0.996f)
        val lowB = percentile(histB, 0.004f)
        val highB = percentile(histB, 0.996f)

        val scaleR = 255f / max(MIN_STRETCH_RANGE, highR - lowR).toFloat()
        val scaleG = 255f / max(MIN_STRETCH_RANGE, highG - lowG).toFloat()
        val scaleB = 255f / max(MIN_STRETCH_RANGE, highB - lowB).toFloat()

        val curve = curveLut(MAGIC_COLOR_CURVE)
        val saturation = MAGIC_COLOR_SATURATION

        // Pass 3 — apply everything in one sweep.
        y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                val index = y * w + x
                val p = pixels[index]
                val alpha = p ushr 24
                val gain = gainFor(background.at(x, y))

                var ri = ((((p ushr 16) and 0xFF) * gain * wbR - lowR) * scaleR)
                    .toInt().coerceIn(0, 255)
                var gi = ((((p ushr 8) and 0xFF) * gain * wbG - lowG) * scaleG)
                    .toInt().coerceIn(0, 255)
                var bi = (((p and 0xFF) * gain * wbB - lowB) * scaleB)
                    .toInt().coerceIn(0, 255)

                ri = curve[ri]
                gi = curve[gi]
                bi = curve[bi]

                // Saturation boost around Rec.601 luminance, so colour elements
                // (stamps, signatures, highlighter) regain their chroma.
                val luma = 0.299f * ri + 0.587f * gi + 0.114f * bi
                ri = (luma + (ri - luma) * saturation).toInt().coerceIn(0, 255)
                gi = (luma + (gi - luma) * saturation).toInt().coerceIn(0, 255)
                bi = (luma + (bi - luma) * saturation).toInt().coerceIn(0, 255)

                pixels[index] = (alpha shl 24) or (ri shl 16) or (gi shl 8) or bi
                x++
            }
            y++
        }
    }

    /**
     * *Lighten*: partial illumination correction that keeps some of the original
     * shading and colour, for pages where full flattening looks unnatural (photos,
     * handwritten notes on tinted paper).
     */
    private fun lighten(pixels: IntArray, background: Background, w: Int, h: Int) {
        val curve = curveLut(LIGHTEN_CURVE)
        val strength = LIGHTEN_STRENGTH
        var y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                val index = y * w + x
                val p = pixels[index]
                val alpha = p ushr 24
                val gain = gainFor(background.at(x, y))
                val partial = 1f + (gain - 1f) * strength

                val ri = curve[(((p ushr 16) and 0xFF) * partial).toInt().coerceIn(0, 255)]
                val gi = curve[(((p ushr 8) and 0xFF) * partial).toInt().coerceIn(0, 255)]
                val bi = curve[((p and 0xFF) * partial).toInt().coerceIn(0, 255)]

                pixels[index] = (alpha shl 24) or (ri shl 16) or (gi shl 8) or bi
                x++
            }
            y++
        }
    }

    /** *Grayscale*: shadow-free luminance with a full-range stretch. */
    private fun grayscale(pixels: IntArray, lum: FloatArray, background: Background, w: Int, h: Int) {
        var y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                val i = y * w + x
                lum[i] *= gainFor(background.at(x, y))
                x++
            }
            y++
        }

        val hist = IntArray(256)
        for (v in lum) hist[v.toInt().coerceIn(0, 255)]++
        val low = percentile(hist, 0.005f)
        val high = percentile(hist, 0.995f)
        val scale = 255f / max(MIN_STRETCH_RANGE, high - low).toFloat()
        val curve = curveLut(GRAYSCALE_CURVE)

        for (i in pixels.indices) {
            var v = ((lum[i] - low) * scale).toInt().coerceIn(0, 255)
            v = curve[v]
            pixels[i] = (pixels[i] and ALPHA_MASK) or (v shl 16) or (v shl 8) or v
        }
    }

    /**
     * *Clear B&W*: Otsu threshold over the shadow-normalised luminance, with a
     * narrow linear ramp instead of a hard cut so text keeps anti-aliased edges.
     */
    private fun clearBlackWhite(
        pixels: IntArray,
        lum: FloatArray,
        background: Background,
        w: Int,
        h: Int
    ) {
        var y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                val i = y * w + x
                lum[i] *= gainFor(background.at(x, y))
                x++
            }
            y++
        }

        val hist = IntArray(256)
        for (v in lum) hist[v.toInt().coerceIn(0, 255)]++

        val threshold = otsuThreshold(hist)
        var lowT = threshold * 0.94f
        var highT = threshold * 1.08f
        if (highT - lowT < 6f) {
            lowT = threshold - 4f
            highT = threshold + 4f
        }
        val span = (highT - lowT).coerceAtLeast(1f)

        for (i in pixels.indices) {
            val v = lum[i]
            val out = when {
                v >= highT -> 255
                v <= lowT -> 0
                else -> (((v - lowT) / span) * 255f).toInt().coerceIn(0, 255)
            }
            pixels[i] = (pixels[i] and ALPHA_MASK) or (out shl 16) or (out shl 8) or out
        }
    }

    // ------------------------------------------------------------- pure helpers
    // `internal` so DocumentFiltersTest can cover them on the JVM without Android.

    /**
     * 256-entry tone curve. `strength = 0` is the identity; higher values apply a
     * smoothstep S-curve that darkens ink while pinning both ends (paper stays
     * pure white, ink stays black).
     */
    internal fun curveLut(strength: Float): IntArray {
        val lut = IntArray(256)
        for (v in 0..255) {
            val t = v / 255f
            val smooth = t * t * (3f - 2f * t)
            val mixed = t + (smooth - t) * strength
            lut[v] = (mixed * 255f).roundToInt().coerceIn(0, 255)
        }
        return lut
    }

    /** Value at [fraction] of the distribution described by [hist]. */
    internal fun percentile(hist: IntArray, fraction: Float): Int {
        var total = 0
        for (v in hist) total += v
        if (total <= 0) return 0
        val target = (total * fraction).toInt().coerceIn(0, total - 1)
        var accumulated = 0
        for (v in hist.indices) {
            accumulated += hist[v]
            if (accumulated > target) return v
        }
        return hist.size - 1
    }

    /** Otsu's method: the threshold maximising between-class variance. */
    internal fun otsuThreshold(hist: IntArray): Int {
        var total = 0L
        var sum = 0.0
        for (v in hist.indices) {
            total += hist[v]
            sum += v.toDouble() * hist[v]
        }
        if (total <= 0L) return 128

        var weightBackground = 0L
        var sumBackground = 0.0
        var best = 128
        var bestVariance = -1.0

        for (t in hist.indices) {
            weightBackground += hist[t]
            if (weightBackground == 0L) continue
            val weightForeground = total - weightBackground
            if (weightForeground == 0L) break

            sumBackground += t.toDouble() * hist[t]
            val meanBackground = sumBackground / weightBackground
            val meanForeground = (sum - sumBackground) / weightForeground
            val variance = weightBackground.toDouble() * weightForeground *
                (meanBackground - meanForeground) * (meanBackground - meanForeground)

            if (variance > bestVariance) {
                bestVariance = variance
                best = t
            }
        }
        return best
    }

    /** 0xFF000000 as a signed Int, written literally so it can stay `const`. */
    private const val ALPHA_MASK = -0x1000000

    // Tuning. Exposed as constants so they are easy to A/B on real captures.
    private const val MAGIC_COLOR_CURVE = 0.55f
    private const val MAGIC_COLOR_SATURATION = 1.18f
    private const val LIGHTEN_CURVE = 0.15f
    private const val LIGHTEN_STRENGTH = 0.72f
    private const val GRAYSCALE_CURVE = 0.35f
}
