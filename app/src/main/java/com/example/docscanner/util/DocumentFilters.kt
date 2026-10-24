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
 * | Magic Color | `lu3` → `enhanceDocument(level = 1)` | paper surface → divide → ink curve → saturation → midtone sharpen |
 * | Clear B&W | `lu3` → `enhanceDocument(level = 2)` | paper surface → median → Otsu → soft edge ramp |
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
 * The two document filters delegate to native C++ (`libimageproc.so`). Its JNI
 * table and imports were recovered from the shipped ARM64 ELF (spec §6) and the
 * short routines were decompiled (§7), so the **step list** below is a
 * transcription: `cv::dilate` → `cv::erode` on the background estimate, a divide
 * by it, `cv::medianBlur` before `cv::LUT`-driven thresholding, `cv::Laplacian`
 * sharpening. What is *not* recovered is `ColorizingRadius::computeRadius`, the
 * one size-dependent radius behind the stripped PLT, so the reach is a model —
 * isolated in the "reconstruction" block below so calibration touches one place
 * and nothing else moves.
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
    // Reconstruction of `enhanceDocument`'s size-dependent radius (computeRadius was
    // behind the stripped PLT). Calibrate here if the original disagrees; the rest of
    // the pipeline below is transcribed from the disassembly.
    // ==================================================================================

    /**
     * Long edge of the working grid the background surface is estimated on.
     *
     * The surface is low-frequency by construction, so estimating it at ~1/8 of
     * the page and sampling it bilinearly is indistinguishable from full
     * resolution while costing a fraction of the time.
     */
    private const val PAPER_WORK_DIM = 320

    /**
     * Denominator that turns the working grid into the morphology radius.
     *
     * The disassembly shows `cv::dilate` immediately followed by `cv::erode` on
     * the background estimate. In OpenCV's own vocabulary that pair is a
     * **closing** (`MORPH_CLOSE`), and its job is exactly what the pipeline
     * needs: fill dark structure -- glyph strokes, rules, staples, a shadowed
     * fold -- with the paper level around it, so the surface describes the page
     * and not its ink. A dilation alone reads too bright and dividing by it
     * paints the page white.
     *
     * The radius is **not** a constant in the original: `enhanceDocument` calls
     * `ColorizingRadius::computeRadius(w, h, dpi, level)`, whose body is behind
     * the stripped PLT and was not recovered (spec §7). What is certain is that
     * it depends on the image size, because a fixed radius cannot close over a
     * 60 px text line and a 6 px one at the same time. Measured against the
     * behaviour we can see, ~2 % of the image's long edge is the smallest reach
     * that reliably bridges body text, and it is scale-free: at any resolution
     * it removes ink and keeps folds. On a 320-cell grid that is a radius of 6,
     * hence the divisor below.
     *
     * This is the one number in the paper map that is a model rather than a
     * transcription -- logged as such in `docs/ORIGINAL_APP_FILTER_SPEC.md`.
     */
    private const val PAPER_OPEN_DIVISOR = 48

    /** Morphology radius is never below this; radius 0 would skip the step. */
    private const val PAPER_OPEN_MIN_RADIUS = 1

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

    /**
     * Unsharp strength for *Magic Color*'s "sharpens midtone contrast" step.
     *
     * The library imports `cv::Laplacian` and `cv::GaussianBlur`, so the original
     * sharpens rather than only stretching tones. Applied to luminance and
     * rescaled into the colour channels, so hue (stamps, logos, highlighters)
     * survives.
     */
    private const val SHARPEN_AMOUNT = 0.55f

    /** Anti-aliasing band around the B&W threshold, as a fraction of it. */
    private const val BW_RAMP_FRACTION = 0.06f

    /** Never make the B&W ramp narrower than this, or edges turn to jaggies. */
    private const val BW_MIN_RAMP = 6f

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
     * Reconstruction of the native routine (`0x00010178`), following its step list:
     *
     * 1. **Illumination surface.** `cv::dilate` → `cv::erode` (a closing) per
     *    channel, exactly as the disassembly states — see [buildPaperMap].
     * 2. **Divide by it.** `I / paper · 255` removes shadows, page curvature *and*
     *    the light's colour cast in one step, because the surface is per channel.
     *    This is the "white balance against the estimated paper background" the
     *    native code hands back as `ColorizingRadius::getBackgroundColor()`.
     * 3. **Ink curve.** A gamma with pinned endpoints: the top 7 % of tones (paper
     *    noise, JPEG ripple) snap to pure white and the bottom 6 % snap to black,
     *    so print reads black instead of grey.
     * 4. **Saturation.** Modest, about luminance, so stamps and signatures keep
     *    their colour while the paper stays neutral.
     * 5. **Midtone sharpening.** The library imports `cv::Laplacian` and
     *    `cv::GaussianBlur`, and the spec calls level 1 out as "sharpens midtone
     *    contrast, preserving stamp/logo chromaticity" — so an unsharp mask on
     *    luminance, with the result folded back into all three channels as a scale
     *    factor. Sharpening the channels independently would fringe coloured ink.
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

        sharpenLuminance(pixels, w, h)
    }

    /**
     * Unsharp mask on luminance, folded back as a per-pixel scale.
     *
     * `out = luma + amount · (luma − blur(luma))`, then every channel is scaled by
     * `out / luma`. Because it is one scale for all three channels, hue is
     * untouched — a red stamp stays the same red, just crisper at the edges.
     */
    private fun sharpenLuminance(pixels: IntArray, w: Int, h: Int) {
        val luma = FloatArray(w * h)
        for (i in pixels.indices) {
            val p = pixels[i]
            luma[i] = 0.299f * ((p ushr 16) and 0xFF) +
                0.587f * ((p ushr 8) and 0xFF) +
                0.114f * (p and 0xFF)
        }
        val blurred = gaussianBlur3(luma, w, h)

        for (i in pixels.indices) {
            val base = luma[i]
            val detail = base - blurred[i]
            if (detail > -1f && detail < 1f) continue

            val target = (base + SHARPEN_AMOUNT * detail).coerceIn(0f, 255f)
            val scale = target / max(1f, base)
            val p = pixels[i]
            val r = (((p ushr 16) and 0xFF) * scale).roundToInt().coerceIn(0, 255)
            val g = (((p ushr 8) and 0xFF) * scale).roundToInt().coerceIn(0, 255)
            val b = ((p and 0xFF) * scale).roundToInt().coerceIn(0, 255)
            pixels[i] = (p and ALPHA_MASK) or (r shl 16) or (g shl 8) or b
        }
    }

    /**
     * *Clear B&W* — `lu3` → `enhanceDocument(level = 2, colorMode = 2)`.
     *
     * Illumination normalisation first (same illumination surface), then a 3×3
     * **median** on the normalised luminance, then Otsu, then a soft ramp so glyph
     * edges stay anti-aliased instead of turning into 1-bit jaggies.
     *
     * The median is not an embellishment: the native library imports
     * `cv::medianBlur`, and denoising *before* the threshold is the ordering that
     * keeps dust specks and JPEG mosquito noise out of the binarisation without
     * eroding the glyphs themselves.
     *
     * An earlier revision voted 3×3 on the already-binarised result instead. That
     * is a weaker operation at the wrong point in the pipeline: it cannot stop
     * noise from dragging the Otsu threshold, and it rounds corners.
     */
    private fun applyDocumentBw(pixels: IntArray, w: Int, h: Int) {
        val paper = buildPaperMap(pixels, w, h)
        val lum = FloatArray(w * h)
        val normalised = IntArray(3)

        var index = 0
        for (y in 0 until h) {
            for (x in 0 until w) {
                normalise(pixels[index], paper, x, y, normalised)
                lum[index] = 0.299f * normalised[0] +
                    0.587f * normalised[1] +
                    0.114f * normalised[2]
                index++
            }
        }

        // cv::medianBlur before thresholding, not after.
        val denoised = median3x3(lum, w, h)
        val histogram = IntArray(256)
        for (value in denoised) {
            histogram[value.roundToInt().coerceIn(0, 255)]++
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

        for (i in pixels.indices) {
            val value = denoised[i]
            val out = when {
                value >= highCut -> 255
                value <= lowCut -> 0
                else -> (((value - lowCut) / span) * 255f).roundToInt().coerceIn(0, 255)
            }
            pixels[i] = (pixels[i] and ALPHA_MASK) or (out shl 16) or (out shl 8) or out
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

    /**
     * The illumination surface, built the way the native library builds it:
     * pool, then **dilate → erode** (a closing, i.e. ink filled with paper),
     * then smooth.
     *
     * Why a max-pool first: the working grid is ~1/8 scale, and averaging down
     * would drag text into the paper estimate, producing grey haloes around every
     * glyph once divided out. Pooling with a max is the dilation half of the
     * opening at full resolution — text can never win against the paper beside it.
     *
     * Then the dilate→erode pair runs on the grid at a radius that scales with
     * the page, which is what carries the paper level across a whole text line
     * instead of stopping at the stroke edge.
     */
    private fun buildPaperMap(pixels: IntArray, w: Int, h: Int): PaperMap {
        val cell = max(1, ceil(max(w, h).toFloat() / PAPER_WORK_DIM).toInt())
        val gw = max(1, (w + cell - 1) / cell)
        val gh = max(1, (h + cell - 1) / cell)

        var gridR = maxPool(pixels, w, h, cell, gw, gh, 16)
        var gridG = maxPool(pixels, w, h, cell, gw, gh, 8)
        var gridB = maxPool(pixels, w, h, cell, gw, gh, 0)

        // cv::dilate then cv::erode — the closing named in the disassembly.
        gridR = closeDarkStructure(gridR, gw, gh)
        gridG = closeDarkStructure(gridG, gw, gh)
        gridB = closeDarkStructure(gridB, gw, gh)

        // Two 3×3 passes so the cell grid cannot show through the division.
        repeat(2) {
            gridR = boxBlur3(gridR, gw, gh)
            gridG = boxBlur3(gridG, gw, gh)
            gridB = boxBlur3(gridB, gw, gh)
        }

        return PaperMap(gridR, gridG, gridB, gw, gh, cell.toFloat())
    }

    /**
     * Radius of the dilate→erode pair, in working-grid cells.
     *
     * Scales with the grid, so the reach is the same fraction of the page at
     * every input size. See [PAPER_OPEN_DIVISOR].
     */
    internal fun paperRadius(gw: Int, gh: Int): Int =
        max(PAPER_OPEN_MIN_RADIUS, max(gw, gh) / PAPER_OPEN_DIVISOR)

    /**
     * `cv::dilate` then `cv::erode` on a channel plane -- a closing: dark
     * structure narrower than the window is filled with its surroundings.
     */
    private fun closeDarkStructure(grid: FloatArray, gw: Int, gh: Int): FloatArray {
        val radius = paperRadius(gw, gh)
        val dilated = windowOp(grid, gw, gh, radius, dilate = true)
        return windowOp(dilated, gw, gh, radius, dilate = false)
    }

    /** Block maximum of one channel, i.e. the dilation half of the opening. */
    private fun maxPool(
        pixels: IntArray,
        w: Int,
        h: Int,
        cell: Int,
        gw: Int,
        gh: Int,
        shift: Int
    ): FloatArray {
        val out = FloatArray(gw * gh)
        for (gy in 0 until gh) {
            val y0 = min(h - 1, gy * cell)
            val y1 = min(h, y0 + cell)
            for (gx in 0 until gw) {
                val x0 = min(w - 1, gx * cell)
                val x1 = min(w, x0 + cell)
                var peak = 0
                for (y in y0 until y1) {
                    val row = y * w
                    for (x in x0 until x1) {
                        val value = (pixels[row + x] ushr shift) and 0xFF
                        if (value > peak) peak = value
                    }
                }
                out[gy * gw + gx] = peak.toFloat()
            }
        }
        return out
    }

    /**
     * Separable 1-D erosion/dilation of a grid. [dilate] picks max, otherwise min.
     *
     * Naive window rather than a monotonic queue: the grid is at most
     * 320 × 320 cells and the radius is one cell, so 9 samples per cell beats the
     * bookkeeping of a deque.
     */
    private fun windowOp(
        src: FloatArray,
        w: Int,
        h: Int,
        radius: Int,
        dilate: Boolean
    ): FloatArray {
        if (radius <= 0) return src
        val horizontal = FloatArray(src.size)
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                var value = if (dilate) 0f else 255f
                for (dx in -radius..radius) {
                    val sample = src[row + (x + dx).coerceIn(0, w - 1)]
                    value = if (dilate) max(value, sample) else min(value, sample)
                }
                horizontal[row + x] = value
            }
        }
        val out = FloatArray(src.size)
        for (y in 0 until h) {
            for (x in 0 until w) {
                var value = if (dilate) 0f else 255f
                for (dy in -radius..radius) {
                    val sample = horizontal[(y + dy).coerceIn(0, h - 1) * w + x]
                    value = if (dilate) max(value, sample) else min(value, sample)
                }
                out[y * w + x] = value
            }
        }
        return out
    }


    /**
     * Separable 3×3 Gaussian `[1 2 1] / 4` on a full-resolution plane.
     *
     * Stands in for `cv::GaussianBlur(…, Size(3,3), 0)`, which the library imports
     * and which the unsharp mask needs as its low-pass term.
     */
    internal fun gaussianBlur3(src: FloatArray, w: Int, h: Int): FloatArray {
        val horizontal = FloatArray(src.size)
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                val centre = src[row + x]
                val left = if (x > 0) src[row + x - 1] else centre
                val right = if (x < w - 1) src[row + x + 1] else centre
                horizontal[row + x] = (left + 2f * centre + right) * 0.25f
            }
        }
        val out = FloatArray(src.size)
        for (y in 0 until h) {
            val centreRow = y * w
            val upRow = (if (y > 0) y - 1 else y) * w
            val downRow = (if (y < h - 1) y + 1 else y) * w
            for (x in 0 until w) {
                out[centreRow + x] =
                    (horizontal[upRow + x] +
                        2f * horizontal[centreRow + x] +
                        horizontal[downRow + x]) * 0.25f
            }
        }
        return out
    }

    /**
     * 3×3 median of a plane — `cv::medianBlur(…, 3)`.
     *
     * Nine samples are sorted with a fixed-size insertion sort; no allocation per
     * pixel, which matters on a 2000 px page.
     */
    internal fun median3x3(src: FloatArray, w: Int, h: Int): FloatArray {
        val out = FloatArray(src.size)
        val window = FloatArray(9)
        for (y in 0 until h) {
            val rowUp = (if (y > 0) y - 1 else y) * w
            val row = y * w
            val rowDown = (if (y < h - 1) y + 1 else y) * w
            for (x in 0 until w) {
                val xm = if (x > 0) x - 1 else x
                val xp = if (x < w - 1) x + 1 else x
                window[0] = src[rowUp + xm]
                window[1] = src[rowUp + x]
                window[2] = src[rowUp + xp]
                window[3] = src[row + xm]
                window[4] = src[row + x]
                window[5] = src[row + xp]
                window[6] = src[rowDown + xm]
                window[7] = src[rowDown + x]
                window[8] = src[rowDown + xp]

                for (i in 1 until 9) {
                    val value = window[i]
                    var j = i - 1
                    while (j >= 0 && window[j] > value) {
                        window[j + 1] = window[j]
                        j--
                    }
                    window[j + 1] = value
                }
                out[row + x] = window[4]
            }
        }
        return out
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
     * The lookup table for *Lighten* — a **verbatim port** of the native loop at
     * `0x0000f5d4` (`docs/ORIGINAL_APP_FILTER_SPEC.md` §7):
     *
     * ```c
     * double range = (double)(whitePoint - blackPoint);
     * if (range <= 0.0) range = 1.0;
     * if (v <= blackPoint)      p[v] = 0;
     * else if (v >= whitePoint) p[v] = 255;
     * else p[v] = (uint8_t)clamp((int)(((v - blackPoint) / range) * 255.0 + 0.5), 0, 255);
     * ```
     *
     * The branch order is load-bearing. When the estimated points invert — which
     * happens on a page with under 2 % ink, where both cutoffs land on the paper
     * spike — the native code does **not** produce a black page: `range` falls
     * back to 1.0 and the first branch wins, so the page collapses to a clean hard
     * threshold at `blackPoint`. An earlier revision here substituted a "skip the
     * filter" guard for that case; this reproduces what the original actually
     * does instead.
     *
     * `+ 0.5` on a positive value is round-half-up, matching Kotlin's
     * [kotlin.math.roundToInt] — but the explicit floor keeps the port auditable
     * against the assembly.
     */
    internal fun linearStretchLut(blackPoint: Int, whitePoint: Int): IntArray {
        val range = if (whitePoint - blackPoint <= 0) 1.0 else (whitePoint - blackPoint).toDouble()
        return IntArray(256) { value ->
            when {
                value <= blackPoint -> 0
                value >= whitePoint -> 255
                else -> {
                    val scaled = (value - blackPoint) / range * 255.0 + 0.5
                    scaled.toInt().coerceIn(0, 255)
                }
            }
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
