package com.example.docscanner.util

import android.graphics.Bitmap
import com.example.docscanner.model.FilterType
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Document enhancement rebuilt against the **decompiled original APK**
 * (`com.indymobileapp.document.scanner`), not against guesses.
 *
 * Ground truth lives in `docs/ORIGINAL_APP_FILTER_SPEC.md` and
 * `docs/original_apk_pipeline_artifacts.zip`. What the bytecode actually says:
 *
 * | Original preset | Bytecode | Implementation |
 * |---|---|---|
 * | Original | `qx3` colour branch | passthrough |
 * | Grayscale | `qx3` gray branch → `Imgproc.cvtColor(BGR2GRAY)` | Rec.601 luminance, **no** enhancement |
 * | Lighten | `tw3` → `enhancePhoto(blackPoint, whitePoint, colorMode)` | linear stretch between the estimated points |
 * | Magic Color | `lu3` → `enhanceDocument(level = 1)` | illumination normalisation → paper white balance → ink curve → saturation |
 * | Clear B&W | `lu3` → `enhanceDocument(level = 2)` | illumination normalisation → Otsu → soft edge ramp |
 *
 * Three things in the previous revision were simply **not in the original** and
 * are gone:
 *
 * 1. Every filter ran a percentile stretch. In the original only *Lighten* uses
 *    the estimated black/white points — `enhanceDocument` is not even passed
 *    them. Applying a stretch to Magic Color is what made it look flat.
 * 2. The points were clamped to `[110, 205]`. The original walks the histogram
 *    with no clamp at all (2 % of pixels from each end).
 * 3. *Grayscale* was being enhanced. The original only desaturates.
 *
 * The two document filters delegate to native C++ (`libimageproc.so`), which was
 * not part of the recovered artifacts, so their tone curve is a reconstruction of
 * the documented behaviour rather than a transcription. Those knobs are isolated
 * in the "reconstruction" block below so calibration only touches one place.
 *
 * Blocking CPU work — call from `Dispatchers.Default`/`IO`, never composition.
 */
object DocumentFilters {

    /** Longest edge processed for a baked page (~200 DPI on A4). */
    const val MAX_PROCESS_DIMENSION = 2000

    /** Longest edge used for the live editor preview. */
    const val PREVIEW_DIMENSION = 900

    // ==================================================================================
    // Transcribed verbatim from the original APK — do not "improve" these.
    // ==================================================================================

    /** `fa1.H` downsamples to exactly 100 × 100 = 10 000 samples. */
    internal const val ESTIMATION_SIDE = 100

    /**
     * `fa1.H`: both points are found by walking the histogram until 200 samples
     * have accumulated. 200 / 10 000 = **2 %**, hard-coded in `:goto_2` / `:goto_3`.
     */
    internal const val PERCENTILE_COUNT = 200

    /** `wv3` fields b…g, read from its static initialiser. */
    internal const val DOC_MIN_R = 80   // wv3.b
    internal const val DOC_MIN_G = 80   // wv3.c
    internal const val DOC_MIN_B = 70   // wv3.d
    internal const val PHOTO_MIN_R = 60 // wv3.e
    internal const val PHOTO_MIN_G = 100 // wv3.f
    internal const val PHOTO_MIN_B = 120 // wv3.g

    /** `0x6e` in `fa1.H`: |R − B| must not exceed this for a "document" pixel. */
    internal const val DOC_MAX_RB_DIFF = 110

    /** `0x14` in `fa1.H`: green must sit within ±20 of the red/blue ordering. */
    internal const val CHROMA_TOLERANCE = 20

    /** `60.0f` in `fa1.H`: either ratio above this classifies the image. */
    internal const val CLASSIFICATION_RATIO = 60f

    // ==================================================================================
    // Reconstruction of the native `enhanceDocument` (libimageproc.so was not recovered).
    // Calibrate here if a comparison against the original ever disagrees.
    // ==================================================================================

    /**
     * Paper map block size on the short edge. Large enough that a block containing
     * print still finds paper (the map is a *maximum*, so text never pulls it
     * down), small enough to follow a torch hotspot across a page.
     */
    private const val PAPER_BLOCK_DIVISOR = 20
    private const val PAPER_BLOCK_MIN = 8

    /**
     * Ceiling on the illumination gain. A patch must never be amplified more than
     * this, or a dark corner (or a finger over the lens) turns into white paper.
     */
    private const val MAX_ILLUMINATION_GAIN = 4.5f

    /** Tones at or below this fraction of white are already ink. */
    private const val CURVE_BLACK_FLOOR = 0.06f

    /** Tones at or above this fraction of white become pure paper. */
    private const val CURVE_WHITE_CEIL = 0.93f

    /** >1 deepens mid-tones (ink); the endpoints are pinned, so paper stays white. */
    private const val CURVE_GAMMA = 1.28f

    /** Chroma boost for stamps, logos and signatures in *Magic Color*. */
    private const val MAGIC_COLOR_SATURATION = 1.18f

    /** Anti-aliasing band around the B&W threshold, as a fraction of it. */
    private const val BW_RAMP_FRACTION = 0.06f

    /** Never make the B&W ramp narrower than this, or edges turn to jaggies. */
    private const val BW_MIN_RAMP = 6f

    /**
     * Below this black→white span, *Lighten* leaves the page alone.
     *
     * This is the one deliberate deviation from the original. `fa1.H` has no
     * guard against `blackPoint > whitePoint`, and that happens whenever ink
     * covers less than 2 % of the page: both cutoffs then land on the paper spike,
     * the range inverts, and a naive stretch turns a nearly blank page solid
     * black. A page with that little ink has nothing to lighten, so identity is
     * both safer and closer to the intent.
     */
    private const val MIN_LIGHTEN_SPAN = 8

    // ==================================================================================
    // Estimation — the exact algorithm behind fa1.H
    // ==================================================================================

    /**
     * What `PSImageEstimationInfoBean` carries back from `fa1.H`.
     *
     * @param blackPoint first luminance bin whose cumulative count reached 2 %.
     * @param whitePoint last luminance bin whose cumulative count from the top
     *   reached 2 % (note: one bin *below* the crossing bin — the original
     *   decrements before exiting the loop, and this mirrors that exactly).
     * @param documentImage `docRatio > 60 % || photoRatio > 60 %`.
     */
    data class Estimation(
        val blackPoint: Int,
        val whitePoint: Int,
        val documentImage: Boolean,
        val documentRatio: Float,
        val photoRatio: Float
    )

    /**
     * Runs the estimation on a [Bitmap] the way the original does: bilinear
     * downscale to 100 × 100, then per-pixel analysis.
     */
    fun estimate(src: Bitmap): Estimation {
        if (src.isRecycled) return estimationFromPixels(IntArray(0))
        val scaled = try {
            Bitmap.createScaledBitmap(src, ESTIMATION_SIDE, ESTIMATION_SIDE, true)
        } catch (_: Throwable) {
            null
        }
        if (scaled == null) return estimationFromPixels(IntArray(0))

        return try {
            val pixels = IntArray(ESTIMATION_SIDE * ESTIMATION_SIDE)
            scaled.getPixels(pixels, 0, ESTIMATION_SIDE, 0, 0, ESTIMATION_SIDE, ESTIMATION_SIDE)
            estimationFromPixels(pixels)
        } catch (_: Throwable) {
            estimationFromPixels(IntArray(0))
        } finally {
            if (scaled !== src && !scaled.isRecycled) scaled.recycle()
        }
    }

    /**
     * The whole of `fa1.H` after downscaling, as a pure function so it can be
     * unit-tested on the JVM.
     */
    internal fun estimationFromPixels(pixels: IntArray): Estimation {
        val histogram = IntArray(256)
        var documentPixels = 0
        var photoPixels = 0
        var total = 0

        for (p in pixels) {
            val r = (p ushr 16) and 0xFF
            val g = (p ushr 8) and 0xFF
            val b = p and 0xFF

            // Document pixel: neutral-ish and not too dark.
            if (r >= DOC_MIN_R && g >= DOC_MIN_G && b >= DOC_MIN_B &&
                (r - b) <= DOC_MAX_RB_DIFF &&
                g >= b - CHROMA_TOLERANCE && g <= r + CHROMA_TOLERANCE
            ) {
                documentPixels++
            }
            // Photo pixel: blue-dominant highlight, i.e. a sky-like gradient.
            if (r >= PHOTO_MIN_R && g >= PHOTO_MIN_G && b >= PHOTO_MIN_B &&
                r <= g && g <= b
            ) {
                photoPixels++
            }

            // `(R + G + B) / 3` with integer division, exactly as compiled.
            val luminance = (r + g + b) / 3
            histogram[luminance]++
            total++
        }

        if (total == 0) {
            return Estimation(
                blackPoint = 0,
                whitePoint = 255,
                documentImage = false,
                documentRatio = 0f,
                photoRatio = 0f
            )
        }

        val documentRatio = documentPixels * 100f / total
        val photoRatio = photoPixels * 100f / total

        // Walk up from black until 2 % of the samples have accumulated.
        var accumulated = 0
        var index = 0
        while (accumulated < PERCENTILE_COUNT && index < 256) {
            accumulated += histogram[index]
            index++
        }
        val blackPoint = index.coerceIn(0, 255)

        // Walk down from white the same way. The original decrements in the loop
        // body, so the surviving index is one below the crossing bin.
        accumulated = 0
        var upper = 255
        while (accumulated < PERCENTILE_COUNT && upper > 0) {
            accumulated += histogram[upper]
            upper--
        }
        val whitePoint = upper.coerceIn(0, 255)

        return Estimation(
            blackPoint = blackPoint,
            whitePoint = whitePoint,
            documentImage = documentRatio > CLASSIFICATION_RATIO ||
                photoRatio > CLASSIFICATION_RATIO,
            documentRatio = documentRatio,
            photoRatio = photoRatio
        )
    }

    // ==================================================================================
    // Public entry point
    // ==================================================================================

    /**
     * Enhances [src].
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

        try {
            when (type) {
                FilterType.LIGHTEN -> applyLinearStretch(pixels, estimate(src))
                FilterType.GRAYSCALE -> applyGrayscale(pixels)
                FilterType.MAGIC_COLOR -> applyDocumentColor(pixels, w, h)
                FilterType.CLEAR_BW -> applyDocumentBw(pixels, w, h)
                FilterType.ORIGINAL -> return src
            }
        } catch (_: Throwable) {
            // A filter must never be able to take the capture down with it.
            return src
        }

        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also {
            it.setPixels(pixels, 0, w, 0, 0, w, h)
        }
    }

    /** Convenience wrapper for live previews at a reduced resolution. */
    fun applyPreview(src: Bitmap, type: FilterType): Bitmap =
        apply(src, type, PREVIEW_DIMENSION)

    // ==================================================================================
    // Filters
    // ==================================================================================

    /**
     * *Lighten* — `tw3` → `PSOpenCV.enhancePhoto(blackPoint, whitePoint, colorMode)`.
     *
     * The original's photo filter is a **plain linear stretch** between the two
     * estimated points, applied equally to R, G and B so colour balance is kept.
     * No illumination model, no curve: that is what makes it "lighten" rather than
     * "document".
     */
    private fun applyLinearStretch(pixels: IntArray, estimation: Estimation) {
        if (estimation.whitePoint - estimation.blackPoint < MIN_LIGHTEN_SPAN) return
        val lut = linearStretchLut(estimation.blackPoint, estimation.whitePoint)
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = lut[(p ushr 16) and 0xFF]
            val g = lut[(p ushr 8) and 0xFF]
            val b = lut[p and 0xFF]
            pixels[i] = (p and ALPHA_MASK) or (r shl 16) or (g shl 8) or b
        }
    }

    /**
     * *Grayscale* — `qx3` gray branch, i.e. `Imgproc.cvtColor(BGR2GRAY)` and
     * nothing else.
     *
     * Deliberately **not** enhanced: the original's grayscale preset desaturates,
     * and users pick it precisely to get the untouched tonal range in mono.
     */
    private fun applyGrayscale(pixels: IntArray) {
        for (i in pixels.indices) {
            val p = pixels[i]
            val v = rec601(
                (p ushr 16) and 0xFF,
                (p ushr 8) and 0xFF,
                p and 0xFF
            )
            pixels[i] = (p and ALPHA_MASK) or (v shl 16) or (v shl 8) or v
        }
    }

    /**
     * *Magic Color* — `lu3` → `enhanceDocument(level = 1, colorMode = 1)`.
     *
     * Reconstruction of the native routine, in the order the spec describes:
     *
     * 1. **Paper map.** A per-channel block *maximum* — paper is the brightest
     *    thing in any patch that also contains print, so a max rejects ink instead
     *    of averaging it in. Blurred twice so the block grid cannot show through.
     * 2. **Divide by it.** `I / paper · 255` removes shadows *and* the light's
     *    colour cast in one step, because the map is per channel — this is the
     *    "white balance against the estimated paper background" the native code
     *    reports back as `bgColor`.
     * 3. **Ink curve.** A gamma with pinned endpoints: the top 7 % of tones (paper
     *    noise, JPEG ripple) snap to pure white and the bottom 6 % snap to black,
     *    so print reads black instead of grey.
     * 4. **Saturation.** Modest, about luminance, so stamps and signatures keep
     *    their colour while the paper stays neutral.
     */
    private fun applyDocumentColor(pixels: IntArray, w: Int, h: Int) {
        val paper = buildPaperMap(pixels, w, h)
        val curve = documentCurveLut()
        val normalised = IntArray(3)

        var index = 0
        for (y in 0 until h) {
            for (x in 0 until w) {
                val p = pixels[index]
                normalise(p, paper, x, y, normalised)
                var r = curve[normalised[0]]
                var g = curve[normalised[1]]
                var b = curve[normalised[2]]

                val luma = 0.299f * r + 0.587f * g + 0.114f * b
                // Boost chroma, but never past 255: a saturated red stamp would
                // otherwise clip its brightest channel and lose all internal
                // detail. Scaling the boost down where it would clip keeps the
                // stamp's texture while still making it redder.
                val peak = maxOf(r, g, b).toFloat()
                val saturation = if (peak > luma) {
                    min(MAGIC_COLOR_SATURATION, (255f - luma) / (peak - luma))
                } else {
                    MAGIC_COLOR_SATURATION
                }
                r = (luma + (r - luma) * saturation).roundToInt().coerceIn(0, 255)
                g = (luma + (g - luma) * saturation).roundToInt().coerceIn(0, 255)
                b = (luma + (b - luma) * saturation).roundToInt().coerceIn(0, 255)

                pixels[index] = (p and ALPHA_MASK) or (r shl 16) or (g shl 8) or b
                index++
            }
        }
    }

    /**
     * *Clear B&W* — `lu3` → `enhanceDocument(level = 2, colorMode = 2)`.
     *
     * Illumination normalisation first (same paper map), then Otsu over the
     * normalised luminance, then a soft ramp so glyph edges stay anti-aliased
     * instead of turning into 1-bit jaggies. A 3×3 majority vote removes isolated
     * dust specks and fills pinholes, which the spec calls morphological smoothing.
     */
    private fun applyDocumentBw(pixels: IntArray, w: Int, h: Int) {
        val paper = buildPaperMap(pixels, w, h)
        val lum = FloatArray(w * h)
        val normalised = IntArray(3)

        var index = 0
        val histogram = IntArray(256)
        for (y in 0 until h) {
            for (x in 0 until w) {
                normalise(pixels[index], paper, x, y, normalised)
                val value = 0.299f * normalised[0] +
                    0.587f * normalised[1] +
                    0.114f * normalised[2]
                lum[index] = value
                histogram[value.roundToInt().coerceIn(0, 255)]++
                index++
            }
        }

        val threshold = otsuThreshold(histogram).toFloat()
        val ramp = max(BW_MIN_RAMP, threshold * BW_RAMP_FRACTION)
        var lowCut = (threshold - ramp / 2f).coerceIn(0f, 255f)
        var highCut = (threshold + ramp / 2f).coerceIn(0f, 255f)
        if (highCut - lowCut < 1f) {
            lowCut = (threshold - 0.5f).coerceAtLeast(0f)
            highCut = (threshold + 0.5f).coerceAtMost(255f)
        }
        val span = (highCut - lowCut).coerceAtLeast(1f)

        for (y in 0 until h) {
            val rowUp = if (y > 0) (y - 1) * w else 0
            val row = y * w
            val rowDown = if (y < h - 1) (y + 1) * w else (h - 1) * w
            for (x in 0 until w) {
                val i = row + x
                val xm = if (x > 0) x - 1 else 0
                val xp = if (x < w - 1) x + 1 else w - 1

                var paperNeighbours = 0
                if (lum[rowUp + xm] >= threshold) paperNeighbours++
                if (lum[rowUp + x] >= threshold) paperNeighbours++
                if (lum[rowUp + xp] >= threshold) paperNeighbours++
                if (lum[row + xm] >= threshold) paperNeighbours++
                if (lum[row + x] >= threshold) paperNeighbours++
                if (lum[row + xp] >= threshold) paperNeighbours++
                if (lum[rowDown + xm] >= threshold) paperNeighbours++
                if (lum[rowDown + x] >= threshold) paperNeighbours++
                if (lum[rowDown + xp] >= threshold) paperNeighbours++

                val value = lum[i]
                val out = when {
                    paperNeighbours >= 6 -> 255   // clear paper majority: kill ink specks
                    paperNeighbours <= 3 -> 0     // clear ink majority: fill pinholes
                    value >= highCut -> 255
                    value <= lowCut -> 0
                    else -> (((value - lowCut) / span) * 255f).roundToInt().coerceIn(0, 255)
                }
                pixels[i] = (pixels[i] and ALPHA_MASK) or (out shl 16) or (out shl 8) or out
            }
        }
    }

    // ==================================================================================
    // Paper map / normalisation
    // ==================================================================================

    /** A coarse per-channel paper-white map, sampled bilinearly at any pixel. */
    private class PaperMap(
        val r: FloatArray,
        val g: FloatArray,
        val b: FloatArray,
        val gw: Int,
        val gh: Int,
        val block: Float
    )

    private fun buildPaperMap(pixels: IntArray, w: Int, h: Int): PaperMap {
        val block = max(PAPER_BLOCK_MIN, min(w, h) / PAPER_BLOCK_DIVISOR).toFloat()
        val gw = max(1, ceil(w / block).toInt())
        val gh = max(1, ceil(h / block).toInt())

        val gridR = FloatArray(gw * gh)
        val gridG = FloatArray(gw * gh)
        val gridB = FloatArray(gw * gh)

        for (gy in 0 until gh) {
            val y0 = (gy * block).toInt().coerceIn(0, h - 1)
            val y1 = min(h, ((gy + 1) * block).toInt().coerceAtLeast(y0 + 1))
            for (gx in 0 until gw) {
                val x0 = (gx * block).toInt().coerceIn(0, w - 1)
                val x1 = min(w, ((gx + 1) * block).toInt().coerceAtLeast(x0 + 1))
                var peakR = 0
                var peakG = 0
                var peakB = 0
                for (y in y0 until y1) {
                    val row = y * w
                    for (x in x0 until x1) {
                        val p = pixels[row + x]
                        val r = (p ushr 16) and 0xFF
                        val g = (p ushr 8) and 0xFF
                        val b = p and 0xFF
                        if (r > peakR) peakR = r
                        if (g > peakG) peakG = g
                        if (b > peakB) peakB = b
                    }
                }
                val cell = gy * gw + gx
                gridR[cell] = peakR.toFloat()
                gridG[cell] = peakG.toFloat()
                gridB[cell] = peakB.toFloat()
            }
        }

        // Two passes of a 3×3 box blur: smooth the block grid, keep the local level.
        var smoothedR = gridR
        var smoothedG = gridG
        var smoothedB = gridB
        repeat(2) {
            smoothedR = boxBlur3(smoothedR, gw, gh)
            smoothedG = boxBlur3(smoothedG, gw, gh)
            smoothedB = boxBlur3(smoothedB, gw, gh)
        }

        return PaperMap(smoothedR, smoothedG, smoothedB, gw, gh, block)
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

    private fun sample(grid: FloatArray, map: PaperMap, x: Int, y: Int): Float {
        val fx = ((x + 0.5f) / map.block - 0.5f).coerceIn(0f, (map.gw - 1).toFloat())
        val fy = ((y + 0.5f) / map.block - 0.5f).coerceIn(0f, (map.gh - 1).toFloat())
        val x0 = fx.toInt().coerceIn(0, map.gw - 1)
        val y0 = fy.toInt().coerceIn(0, map.gh - 1)
        val x1 = min(map.gw - 1, x0 + 1)
        val y1 = min(map.gh - 1, y0 + 1)
        val tx = fx - x0
        val ty = fy - y0

        val i00 = y0 * map.gw + x0
        val i01 = y0 * map.gw + x1
        val i10 = y1 * map.gw + x0
        val i11 = y1 * map.gw + x1
        val top = grid[i00] + (grid[i01] - grid[i00]) * tx
        val bottom = grid[i10] + (grid[i11] - grid[i10]) * tx
        return top + (bottom - top) * ty
    }

    /** Paper level below which the gain ceiling would be exceeded. */
    private const val PAPER_FLOOR = 255f / MAX_ILLUMINATION_GAIN

    /**
     * `I / paper · 255` per channel, clamped into 0…255.
     *
     * Writes into [out] rather than allocating: this runs once per pixel on a
     * 2000 px page. [out] is caller-owned, so a preview render and a bake running
     * on different dispatchers cannot corrupt each other.
     */
    private fun normalise(pixel: Int, paper: PaperMap, x: Int, y: Int, out: IntArray) {
        val paperR = max(PAPER_FLOOR, sample(paper.r, paper, x, y))
        val paperG = max(PAPER_FLOOR, sample(paper.g, paper, x, y))
        val paperB = max(PAPER_FLOOR, sample(paper.b, paper, x, y))

        out[0] = (((pixel ushr 16) and 0xFF) * 255f / paperR).roundToInt().coerceIn(0, 255)
        out[1] = (((pixel ushr 8) and 0xFF) * 255f / paperG).roundToInt().coerceIn(0, 255)
        out[2] = ((pixel and 0xFF) * 255f / paperB).roundToInt().coerceIn(0, 255)
    }

    // ==================================================================================
    // Pure helpers — `internal` so DocumentFiltersTest can cover them on the JVM.
    // ==================================================================================

    /**
     * The lookup table for *Lighten*: `clamp((I − black) / (white − black) × 255)`.
     *
     * Exactly the transfer function the spec attributes to `enhancePhoto`, with no
     * curve layered on top.
     */
    internal fun linearStretchLut(blackPoint: Int, whitePoint: Int): IntArray {
        val span = max(1, whitePoint - blackPoint)
        return IntArray(256) { value ->
            (((value - blackPoint).toFloat() / span) * 255f).roundToInt().coerceIn(0, 255)
        }
    }

    /**
     * The ink curve used by the two document filters.
     *
     * Rescales so that [CURVE_WHITE_CEIL] becomes pure white and
     * [CURVE_BLACK_FLOOR] becomes pure black, then applies [CURVE_GAMMA] to deepen
     * everything in between. Endpoints are pinned, so monotonic.
     */
    internal fun documentCurveLut(): IntArray {
        val lo = CURVE_BLACK_FLOOR
        val hi = CURVE_WHITE_CEIL
        return IntArray(256) { value ->
            val t = ((value / 255f) - lo) / (hi - lo)
            (t.coerceIn(0f, 1f).pow(CURVE_GAMMA) * 255f).roundToInt().coerceIn(0, 255)
        }
    }

    /** Rec.601 luminance, the definition `Imgproc.cvtColor(BGR2GRAY)` uses. */
    internal fun rec601(r: Int, g: Int, b: Int): Int =
        (0.299f * r + 0.587f * g + 0.114f * b).roundToInt().coerceIn(0, 255)

    /**
     * Otsu's threshold over a luminance histogram.
     *
     * Falls back to [DEFAULT_OTSU] for a blank histogram rather than 0, which would
     * turn a blank page solid black.
     */
    internal fun otsuThreshold(histogram: IntArray): Int {
        var total = 0L
        var sum = 0.0
        for (i in 0 until 256) {
            total += histogram[i]
            sum += i.toDouble() * histogram[i]
        }
        if (total == 0L) return DEFAULT_OTSU

        var sumB = 0.0
        var weightB = 0L
        var best = 0
        var bestVariance = -1.0

        for (t in 0 until 256) {
            weightB += histogram[t]
            if (weightB == 0L) continue
            val weightF = total - weightB
            if (weightF == 0L) break

            sumB += t.toDouble() * histogram[t]
            val meanB = sumB / weightB
            val meanF = (sum - sumB) / weightF
            val variance = weightB.toDouble() * weightF * (meanB - meanF) * (meanB - meanF)
            if (variance > bestVariance) {
                bestVariance = variance
                best = t
            }
        }
        return best
    }

    /** Threshold for a histogram with no samples; 0 would blacken a blank page. */
    private const val DEFAULT_OTSU = 128

    // ==================================================================================
    // Pixel access
    // ==================================================================================

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

    private const val ALPHA_MASK = -0x1000000
}
