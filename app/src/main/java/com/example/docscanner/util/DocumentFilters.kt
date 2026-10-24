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

    /** Where the black point is sampled from the luminance histogram. */
    private const val BLACK_PERCENTILE = 0.02f

    /** Where the white point is sampled from the luminance histogram. */
    private const val PAPER_PERCENTILE = 0.90f

    /**
     * Upper bound on the black point. Without it, a page with very little ink
     * pushes the "black" point into the paper mass and the stretch blows out.
     */
    private const val MAX_BLACK_POINT = 110

    /** Lower bound on the white point, so a dark photo is not brightened to white. */
    private const val MIN_WHITE_POINT = 205

    /** Ink is cut here (fraction of the Otsu threshold) — hard, for deep blacks. */
    private const val INK_CUT_FACTOR = 0.95f

    /** Paper reaches pure white here — earlier than the mirror of the ink cut. */
    private const val PAPER_CUT_FACTOR = 1.05f

    /** Narrowest anti-aliasing band kept around the B&W threshold. */
    private const val MIN_BW_SPAN = 8f

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
     * *Magic Color*: shadow-free colour with a punchy, hue-stable tonal range —
     * the filter that should look like the legacy app's default.
     *
     * Two things here are deliberate corrections over the first version:
     *
     * 1. **One stretch for all channels.** The first version stretched R, G and B
     *    independently with a different low/high percentile each. Three different
     *    gain factors on the same paper pixel produce a colour cast (whites going
     *    pink or green). Contrast belongs on luminance; only white balance is
     *    per-channel.
     * 2. **Both stretch points are clamped** by [stretchPoints]. An unbounded low
     *    percentile is unstable when ink covers less than that percentile: the
     *    "black" point lands inside the paper mass, the gain explodes, and the page
     *    blows out to white.
     */
    private fun magicColor(pixels: IntArray, background: Background, w: Int, h: Int) {
        val stride = if (pixels.size > 400_000) 3 else 1

        // ---- pass 1: grey-world white balance ------------------------------
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
        // Clamped: a page that is legitimately tinted (a form, a photographed
        // object) must not be neutralised into grey.
        val wbR = (meanAll / meanR).coerceIn(0.86f, 1.18f)
        val wbG = (meanAll / meanG).coerceIn(0.86f, 1.18f)
        val wbB = (meanAll / meanB).coerceIn(0.86f, 1.18f)

        // ---- pass 2: ONE luminance histogram --------------------------------
        val lumHist = IntArray(256)
        y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                val p = pixels[y * w + x]
                val gain = gainFor(background.at(x, y))
                val r = ((p ushr 16) and 0xFF) * gain * wbR
                val g = ((p ushr 8) and 0xFF) * gain * wbG
                val b = (p and 0xFF) * gain * wbB
                val l = (0.299f * r + 0.587f * g + 0.114f * b).toInt().coerceIn(0, 255)
                lumHist[l]++
                x += stride
            }
            y += stride
        }

        val (low, high) = stretchPoints(lumHist)
        val scale = 255f / max(MIN_STRETCH_RANGE, high - low).toFloat()
        val curve = curveLut(MAGIC_COLOR_CURVE)
        val saturation = MAGIC_COLOR_SATURATION

        // ---- pass 3: apply --------------------------------------------------
        y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                val index = y * w + x
                val p = pixels[index]
                val alpha = p ushr 24
                val gain = gainFor(background.at(x, y))

                var ri = curve[((((p ushr 16) and 0xFF) * gain * wbR - low) * scale)
                    .toInt().coerceIn(0, 255)]
                var gi = curve[((((p ushr 8) and 0xFF) * gain * wbG - low) * scale)
                    .toInt().coerceIn(0, 255)]
                var bi = curve[(((p and 0xFF) * gain * wbB - low) * scale)
                    .toInt().coerceIn(0, 255)]

                // Saturation around Rec.601 luminance: stamps, signatures and
                // highlighter regain chroma without shifting the paper white.
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

    /** *Grayscale*: shadow-free luminance with a clamped full-range stretch. */
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

        val (low, high) = stretchPoints(hist)
        val scale = 255f / max(MIN_STRETCH_RANGE, high - low).toFloat()
        val curve = curveLut(GRAYSCALE_CURVE)

        for (i in pixels.indices) {
            var v = ((lum[i] - low) * scale).toInt().coerceIn(0, 255)
            v = curve[v]
            pixels[i] = (pixels[i] and ALPHA_MASK) or (v shl 16) or (v shl 8) or v
        }
    }

    /**
     * *Clear B&W*: Otsu threshold over the shadow-normalised luminance.
     *
     * Two corrections over the first version:
     *
     * 1. **Asymmetric ramp.** The old ramp was symmetric around the threshold, so
     *    paper noise *just* above the threshold stayed pure black — visible
     *    speckle all over the page. Ink is now cut hard and paper whites out
     *    earlier, which is what a scanner should do.
     * 2. **Majority despeckle.** A 3×3 vote removes isolated dots (dust, JPEG
     *    mosquito noise) and small pinholes in the paper, while pixels inside the
     *    anti-aliasing band keep their ramped value so glyph edges stay smooth.
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

        val threshold = otsuThreshold(hist).toFloat()
        var lowT = threshold * INK_CUT_FACTOR
        var highT = threshold * PAPER_CUT_FACTOR
        if (highT - lowT < MIN_BW_SPAN) {
            lowT = (threshold - MIN_BW_SPAN / 2f).coerceAtLeast(0f)
            highT = (threshold + MIN_BW_SPAN / 2f).coerceAtMost(255f)
        }
        val span = (highT - lowT).coerceAtLeast(1f)

        // Despeckle straight from `lum` so no extra full-frame buffer is needed.
        for (y in 0 until h) {
            val rowUp = if (y > 0) (y - 1) * w else 0
            val row = y * w
            val rowDown = if (y < h - 1) (y + 1) * w else (h - 1) * w
            for (x in 0 until w) {
                val i = row + x
                val xm = if (x > 0) x - 1 else 0
                val xp = if (x < w - 1) x + 1 else w - 1

                // How many of the 3x3 neighbourhood are paper?
                var white = 0
                if (lum[rowUp + xm] >= threshold) white++
                if (lum[rowUp + x] >= threshold) white++
                if (lum[rowUp + xp] >= threshold) white++
                if (lum[row + xm] >= threshold) white++
                if (lum[row + x] >= threshold) white++
                if (lum[row + xp] >= threshold) white++
                if (lum[rowDown + xm] >= threshold) white++
                if (lum[rowDown + x] >= threshold) white++
                if (lum[rowDown + xp] >= threshold) white++

                val v = lum[i]
                val out = when {
                    // A clear majority of paper wins: kills isolated ink dots.
                    white >= 6 -> 255
                    // A clear majority of ink wins: fills pinholes in glyphs.
                    white <= 3 -> 0
                    // Ambiguous — keep the ramped value so glyph edges stay smooth.
                    v >= highT -> 255
                    v <= lowT -> 0
                    else -> (((v - lowT) / span) * 255f).toInt().coerceIn(0, 255)
                }
                pixels[i] = (pixels[i] and ALPHA_MASK) or (out shl 16) or (out shl 8) or out
            }
        }
    }

    // ------------------------------------------------------------- pure helpers
    // `internal` so DocumentFiltersTest can cover them on the JVM without Android.

    /**
     * Black and white points for a tonal stretch, derived from a luminance
     * histogram.
     *
     * Both points are clamped, which is the fix for the "washed out on a lightly
     * printed page" problem: if ink covers less than [blackPercentile] of the
     * pixels, an unclamped low percentile lands inside the paper mass, the
     * resulting gain is enormous, and mid-tones blow out to white. [blackCap]
     * prevents that; [whiteFloor] prevents over-brightening a genuinely dark photo.
     *
     * `internal` so it can be unit-tested on the JVM without Android.
     */
    internal fun stretchPoints(
        histogram: IntArray,
        blackPercentile: Float = BLACK_PERCENTILE,
        paperPercentile: Float = PAPER_PERCENTILE,
        blackCap: Int = MAX_BLACK_POINT,
        whiteFloor: Int = MIN_WHITE_POINT
    ): Pair<Int, Int> {
        var low = percentile(histogram, blackPercentile).coerceAtMost(blackCap)
        var high = percentile(histogram, paperPercentile).coerceAtLeast(whiteFloor)
        if (high - low < MIN_STRETCH_RANGE) {
            low = (high - MIN_STRETCH_RANGE).coerceAtLeast(0)
            if (high - low < MIN_STRETCH_RANGE) {
                high = (low + MIN_STRETCH_RANGE).coerceAtMost(255)
            }
        }
        return low to high
    }

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
    private const val MAGIC_COLOR_CURVE = 0.62f
    private const val MAGIC_COLOR_SATURATION = 1.15f
    private const val LIGHTEN_CURVE = 0.15f
    private const val LIGHTEN_STRENGTH = 0.72f
    private const val GRAYSCALE_CURVE = 0.42f
}
