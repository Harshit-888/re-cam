package com.example.docscanner.util

import android.graphics.Bitmap
import com.example.docscanner.model.FilterType
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

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
 * | Magic Color | `lu3` → `enhanceDocument(level = 1)` | paper surface → saturated divide → ink curve → saturation → midtone sharpen |
 * | Clear B&W | `lu3` → `enhanceDocument(level = 2)` | paper surface → median → Otsu, refined locally (Sauvola) → soft edge ramp |
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
     * Radius of the dilate→erode pair, as a divisor of the working grid's long edge.
     *
     * `~2.5 %` of the page. The disassembly shows `cv::dilate` immediately
     * followed by `cv::erode` on the background estimate — in OpenCV's vocabulary
     * a **closing** (`MORPH_CLOSE`) — whose job is to fill dark structure (glyph
     * strokes, rules, staples, a fold's shadow) with the paper level around it,
     * so the surface describes the page and not its ink.
     *
     * The radius is **not** a constant in the original: `enhanceDocument` calls
     * `ColorizingRadius::computeRadius(w, h, dpi, level)`, whose body sits behind
     * the stripped PLT (spec §7). What is certain is that it depends on the image
     * size. The shadow-removal literature agrees and is specific about why: the
     * window must be wider than about twice the stroke width, or ink survives
     * into the estimate and dividing by it hollows out the text. Measured here,
     * `~2.5 %` of the long edge clears body text at every resolution from 200 px
     * to 4000 px. See `docs/ORIGINAL_APP_FILTER_SPEC.md` §10.1.
     */
    private const val PAPER_CLOSE_DIVISOR = 40

    /** Morphology radius is never below this; radius 0 would skip the step. */
    private const val PAPER_OPEN_MIN_RADIUS = 1

    /** Lowest value the surface may take. Only stops a divide by ~zero. */
    private const val PAPER_EPSILON = 4f

    /**
     * Where the illumination gain stops being applied exactly, and how far it may
     * go. Below the knee the divide is exact; above it the gain saturates.
     *
     * A *hard* clamp here — `max(floor, surface)`, what 1.6 shipped — split the
     * frame into "lifted" and "not lifted" around one global number, which is
     * exactly the "it brightens some areas and leaves others alone" artefact. A
     * knee is monotonic, so there is no contour to see, and its slope is 1 at the
     * knee, so there is no seam either.
     *
     * Documents need a high knee: a hand shadow across a page is a factor of 7–8
     * in luminance and dividing it out is the entire point. A scene needs a low
     * one, or a dark room gets repainted white.
     */
    private const val GAIN_KNEE_DOCUMENT = 8f
    private const val GAIN_CAP_DOCUMENT = 24f
    private const val GAIN_KNEE_SCENE = 1.2f
    private const val GAIN_CAP_SCENE = 2.0f

    /**
     * Luminance the frame is normalised to before it is classified.
     *
     * `wv3`'s thresholds (`R ≥ 80`, `G ≥ 80`, `B ≥ 70`) are absolute, so they
     * answer "is this a bright flat surface", not "is this a page". A page
     * photographed at half exposure scores 33 % and would be misread as a photo;
     * scaling the frame up to this level before asking makes the question
     * exposure-invariant. Only ever brightens, never darkens.
     */
    private const val CLASSIFY_LUMA_TARGET = 240f

    /** Ceiling on that brightening, so a near-black frame cannot be blown up. */
    private const val CLASSIFY_MAX_GAIN = 8f

    /** Tones at or below this fraction of white are already ink. */
    private const val CURVE_BLACK_FLOOR = 0.06f

    /** Tones at or above this fraction of white become pure paper. */
    private const val CURVE_WHITE_CEIL = 0.93f

    /** >1 deepens mid-tones (ink); the endpoints are pinned, so paper stays white. */
    private const val CURVE_GAMMA = 1.35f

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

    /**
     * Sauvola's local-threshold parameters.
     *
     * `T = m·(1 + k·(σ/R − 1))` — the threshold tracks the local mean, pulled
     * down where local contrast is low. `k = 0.34` and `R = 128` are the values
     * Badekas et al. report as best after testing the range, and the same pair is
     * the common default. Included because a *local* threshold is what keeps
     * faded or broken strokes on a shadowed page from vanishing, which a global
     * cut cannot do.
     */
    private const val SAUVOLA_K = 0.34f
    private const val SAUVOLA_R = 128f

    /**
     * Under this local standard deviation the window holds no text/paper decision
     * at all, so the global threshold is used instead. Without the gate, Sauvola
     * on a blank margin lowers the threshold below the mean and speckles the page.
     */
    private const val SAUVOLA_MIN_STD = 6f

    /**
     * How far the local threshold may sit *below* Otsu's, as a multiple.
     *
     * A bounded refinement, not a replacement. Measured on pages whose
     * illumination is already normalised: unclamped Sauvola left 5 % of the paper
     * grey that plain Otsu got white, and letting it rise *above* Otsu turned ink
     * into grey. Lowering only is the asymmetry that makes it safe — it can
     * recover a faint stroke, and it cannot lose one.
     */
    private const val BW_LOCAL_CLAMP_LOW = 0.85f

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

            if (isDocumentPixel(r, g, b)) documentPixels++
            if (isPhotoPixel(r, g, b)) photoPixels++

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

    /** Document pixel, straight from `wv3`'s static initialiser. */
    private fun isDocumentPixel(r: Int, g: Int, b: Int): Boolean =
        r >= DOC_MIN_R && g >= DOC_MIN_G && b >= DOC_MIN_B &&
            (r - b) <= DOC_MAX_RB_DIFF &&
            g >= b - CHROMA_TOLERANCE && g <= r + CHROMA_TOLERANCE

    /** Photo pixel: blue-dominant highlight, i.e. a sky-like gradient. */
    private fun isPhotoPixel(r: Int, g: Int, b: Int): Boolean =
        r >= PHOTO_MIN_R && g >= PHOTO_MIN_G && b >= PHOTO_MIN_B && r <= g && g <= b

    /**
     * `wv3`'s verdict — "is this frame a lit flat surface or a scene?" — with the
     * exposure dependence taken out.
     *
     * The original computes this for every capture (`fa1.H` →
     * `PSImageEstimationInfoBean.imageType`) but the bake path never consults it,
     * so the port ignored it too. It is exactly the question the illumination
     * model needs answered, though: a page has to have its shadows divided out,
     * while a photo of a dark room must not, and the two are indistinguishable
     * from brightness alone.
     *
     * The classifier's thresholds are absolute, so the frame is first scaled to
     * [CLASSIFY_LUMA_TARGET]. Measured on the same synthetic page at full exposure
     * and at 35 %: `documentRatio` 83.1 % against 83.1 % (soft lighting), and
     * 67.5 % against 68.5 % (hard lighting) — i.e. exposure no longer changes the
     * answer, whereas without the scaling the 35 % copy scored 33 % and flipped to
     * "photo".
     *
     * Scaling only ever brightens and the gain is capped, so a genuinely dark
     * frame is not lifted into a false positive: the frame this was written for
     * (4000 × 3000, mean luminance 51) scores `documentRatio` 29.5 %,
     * `photoRatio` 4.9 %, and is correctly treated as a scene.
     */
    internal fun classifiesAsDocument(pixels: IntArray): Boolean {
        if (pixels.isEmpty()) return false

        // Sample a subset rather than every pixel: this is a ratio, so a few
        // hundred thousand samples of a 12 MP frame is far more than enough, and
        // it keeps the classifier off the hot path. sqrt() rather than a fixed
        // divisor so the cost grows with the square root of the frame.
        val stride = max(1, ceil(sqrt(pixels.size.toFloat() / 10_000f)).toInt())

        // The frame's own white level, as the 98th percentile of luminance: a
        // specular highlight (a lamp, a window) must not be mistaken for "this
        // frame is already bright enough".
        val histogram = IntArray(256)
        var samples = 0
        var index = 0
        while (index < pixels.size) {
            val p = pixels[index]
            histogram[(((p ushr 16) and 0xFF) + ((p ushr 8) and 0xFF) + (p and 0xFF)) / 3]++
            samples++
            index += stride
        }
        if (samples == 0) return false

        val cutoff = (samples * 0.98f).toInt()
        var seen = 0
        var peak = 255
        for (bin in 0 until 256) {
            seen += histogram[bin]
            if (seen > cutoff) {
                peak = bin
                break
            }
        }

        val gain = (CLASSIFY_LUMA_TARGET / max(1, peak)).coerceIn(1f, CLASSIFY_MAX_GAIN)

        var documentPixels = 0
        var photoPixels = 0
        var total = 0
        index = 0
        while (index < pixels.size) {
            val p = pixels[index]
            val r = (((p ushr 16) and 0xFF) * gain).roundToInt().coerceIn(0, 255)
            val g = (((p ushr 8) and 0xFF) * gain).roundToInt().coerceIn(0, 255)
            val b = ((p and 0xFF) * gain).roundToInt().coerceIn(0, 255)
            if (isDocumentPixel(r, g, b)) documentPixels++
            if (isPhotoPixel(r, g, b)) photoPixels++
            total++
            index += stride
        }
        if (total == 0) return false

        // Both rules, exactly as `documentImage` is defined in `wv3`. Note what a
        // *scene* looks like under those rules: a landscape is sky (which the
        // photo rule accepts) plus dark ground (which both rules reject), so it
        // scores well under 60 % and is correctly not a document. The frame this
        // was written for — a lit screen in a dark room — scores 29.5 %.
        val documentRatio = documentPixels * 100f / total
        val photoRatio = photoPixels * 100f / total
        return documentRatio > CLASSIFICATION_RATIO || photoRatio > CLASSIFICATION_RATIO
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
        val (knee, cap) = gainLimits(pixels)

        var index = 0
        for (y in 0 until h) {
            for (x in 0 until w) {
                val p = pixels[index]
                normalise(p, paper, x, y, knee, cap, normalised)
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
        val (knee, cap) = gainLimits(pixels)
        val lum = FloatArray(w * h)
        val normalised = IntArray(3)

        var index = 0
        for (y in 0 until h) {
            for (x in 0 until w) {
                normalise(pixels[index], paper, x, y, knee, cap, normalised)
                lum[index] = 0.299f * normalised[0] +
                    0.587f * normalised[1] +
                    0.114f * normalised[2]
                index++
            }
        }

        // cv::medianBlur before thresholding, not after: denoising the *input* is
        // what stops sensor and JPEG noise from dragging the threshold around.
        val denoised = median3x3(lum, w, h)
        val histogram = IntArray(256)
        for (value in denoised) {
            histogram[value.roundToInt().coerceIn(0, 255)]++
        }
        val global = otsuThreshold(histogram).toFloat()

        // Sauvola, clamped to a band around Otsu's answer.
        //
        // A global cut is right for a page whose illumination has already been
        // flattened, which is the case here — the normalisation above has done
        // that. What it cannot do is recover a stroke that is *locally* faint: a
        // pencil line, a thinned glyph, a fold. That is exactly what a local
        // standard-deviation threshold fixes, because it drops the threshold in
        // low-contrast neighbourhoods instead of holding one number for the page.
        //
        // It is kept on a short leash (±15 % of Otsu) for two reasons. Measured
        // unclamped on normalised pages it left grey where Otsu produced white,
        // and it needs a window wider than the text is tall, which is a guess
        // about resolution. Clamped, the failure mode is "within 15 % of Otsu"
        // rather than "speckled".
        val window = sauvolaWindow(w, h)
        val thresholds = if (window > 1) localThresholds(denoised, w, h, window, global) else null

        for (i in pixels.indices) {
            val value = denoised[i]
            val threshold = thresholds?.get(i) ?: global
            val ramp = max(BW_MIN_RAMP, threshold * BW_RAMP_FRACTION)
            var lowCut = (threshold - ramp / 2f).coerceIn(0f, 255f)
            var highCut = (threshold + ramp / 2f).coerceIn(0f, 255f)
            if (highCut - lowCut < 1f) {
                lowCut = (threshold - 0.5f).coerceAtLeast(0f)
                highCut = (threshold + 0.5f).coerceAtMost(255f)
            }
            val span = (highCut - lowCut).coerceAtLeast(1f)
            val out = when {
                value >= highCut -> 255
                value <= lowCut -> 0
                else -> (((value - lowCut) / span) * 255f).roundToInt().coerceIn(0, 255)
            }
            pixels[i] = (pixels[i] and ALPHA_MASK) or (out shl 16) or (out shl 8) or out
        }
    }

    /**
     * Sauvola window edge, in pixels, or 1 to skip the local pass.
     *
     * A local threshold needs a window wider than the tallest glyph stroke, and
     * that is a property of the page, not of the pixel grid — so it scales. At a
     * 2000 px long edge this is 125 px, about right for body text.
     */
    internal fun sauvolaWindow(w: Int, h: Int): Int {
        val raw = max(w, h) / 16
        if (raw < 15) return 1
        return if (raw % 2 == 0) raw + 1 else raw
    }

    /**
     * Per-pixel threshold: Sauvola inside the band around [global], clamped.
     *
     * Returns `null` only if the box statistics cannot be computed, in which case
     * the caller falls back to the global threshold.
     */
    private fun localThresholds(
        lum: FloatArray,
        w: Int,
        h: Int,
        window: Int,
        global: Float
    ): FloatArray? {
        val boxes = boxMeanAndStd(lum, w, h, window) ?: return null
        val mean = boxes.first
        val std = boxes.second
        val low = (global * BW_LOCAL_CLAMP_LOW).coerceAtLeast(1f)
        val out = FloatArray(lum.size)
        for (i in out.indices) {
            val sigma = std[i]
            val threshold = if (sigma < SAUVOLA_MIN_STD) {
                // Flat neighbourhood: there is no text/paper decision to be made
                // here, and Sauvola on a blank margin drops the threshold below
                // the mean and speckles the page.
                global
            } else {
                mean[i] * (1f + SAUVOLA_K * (sigma / SAUVOLA_R - 1f))
            }
            // Never above Otsu's answer: see BW_LOCAL_CLAMP_LOW.
            out[i] = threshold.coerceIn(low, global)
        }
        return out
    }

    /**
     * Mean and standard deviation over a [window] × [window] box, via summed-area
     * tables so the cost is independent of the window size.
     */
    internal fun boxMeanAndStd(
        plane: FloatArray,
        w: Int,
        h: Int,
        window: Int
    ): Pair<FloatArray, FloatArray>? {
        if (window < 3 || w < 1 || h < 1) return null
        val stride = w + 1
        val sum = DoubleArray(stride * (h + 1))
        val sumSq = DoubleArray(stride * (h + 1))
        for (y in 0 until h) {
            var rowSum = 0.0
            var rowSumSq = 0.0
            for (x in 0 until w) {
                val v = plane[y * w + x].toDouble()
                rowSum += v
                rowSumSq += v * v
                val i = (y + 1) * stride + (x + 1)
                sum[i] = sum[(y) * stride + (x + 1)] + rowSum
                sumSq[i] = sumSq[(y) * stride + (x + 1)] + rowSumSq
            }
        }

        fun rect(table: DoubleArray, x0: Int, y0: Int, x1: Int, y1: Int): Double =
            table[y1 * stride + x1] - table[y0 * stride + x1] -
                table[y1 * stride + x0] + table[y0 * stride + x0]

        val half = window / 2
        val mean = FloatArray(w * h)
        val std = FloatArray(w * h)
        for (y in 0 until h) {
            val y0 = (y - half).coerceAtLeast(0)
            val y1 = (y + half + 1).coerceAtMost(h)
            for (x in 0 until w) {
                val x0 = (x - half).coerceAtLeast(0)
                val x1 = (x + half + 1).coerceAtMost(w)
                val count = (x1 - x0) * (y1 - y0)
                if (count <= 0) continue
                val meanValue = rect(sum, x0, y0, x1, y1) / count
                val variance = (rect(sumSq, x0, y0, x1, y1) / count) - meanValue * meanValue
                val i = y * w + x
                mean[i] = meanValue.toFloat()
                std[i] = sqrt(max(0.0, variance)).toFloat()
            }
        }
        return mean to std
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

        val pooled = meanPool(pixels, w, h, cell, gw, gh)
        val gridR = closeDarkStructure(pooled, 0, gw, gh)
        val gridG = closeDarkStructure(pooled, 1, gw, gh)
        val gridB = closeDarkStructure(pooled, 2, gw, gh)

        return PaperMap(gridR, gridG, gridB, gw, gh, cell.toFloat())
    }

    /**
     * Radius of the dilate→erode pair, in working-grid cells.
     *
     * Scales with the grid, so the reach is the same fraction of the page at
     * every input size. See [PAPER_CLOSE_DIVISOR].
     */
    internal fun paperRadius(gw: Int, gh: Int): Int =
        max(PAPER_OPEN_MIN_RADIUS, max(gw, gh) / PAPER_CLOSE_DIVISOR)

    /**
     * `cv::dilate` then `cv::erode` on one channel of the pooled grid.
     *
     * The closing is what turns "the average brightness of this patch, ink
     * included" into "the brightness of the paper here": ink is narrower than the
     * window, so the dilation swallows it from the surrounding paper and the
     * erosion brings the level back down to the paper rather than the ink.
     *
     * Three box passes after that, because two left a visible cell structure on
     * hard-edged shadows (measured: shadowed paper 204 instead of 244).
     */
    private fun closeDarkStructure(pooled: Array<FloatArray>, channel: Int, gw: Int, gh: Int): FloatArray {
        val plane = pooled[channel]
        val radius = paperRadius(gw, gh)
        var grid = windowOp(plane, gw, gh, radius, dilate = true)
        grid = windowOp(grid, gw, gh, radius, dilate = false)
        repeat(3) { grid = boxBlur3(grid, gw, gh) }
        return grid
    }

    /**
     * Per-cell **mean** of the frame, returned as three channel planes.
     *
     * This used to be a per-cell maximum, on the reasoning that paper is the
     * brightest thing in a patch that also contains print. Measured against known
     * paper levels, a max is biased **upwards by two to three standard deviations
     * of sensor noise** — `bg/paper = 1.26` in a deep shadow, which is a 26 %
     * error exactly where the gain is largest. That error is why dividing a hand
     * shadow by the surface never reached white: the estimator was inflated.
     *
     * With a mean the bias drops to 1.02, and rejecting ink is left entirely to
     * the closing that follows — which is the correct division of labour, and is
     * what the background-estimation literature does. (Spec §10.1.)
     */
    private fun meanPool(
        pixels: IntArray,
        w: Int,
        h: Int,
        cell: Int,
        gw: Int,
        gh: Int
    ): Array<FloatArray> {
        val outR = FloatArray(gw * gh)
        val outG = FloatArray(gw * gh)
        val outB = FloatArray(gw * gh)
        for (gy in 0 until gh) {
            val y0 = min(h - 1, gy * cell)
            val y1 = min(h, y0 + cell)
            for (gx in 0 until gw) {
                val x0 = min(w - 1, gx * cell)
                val x1 = min(w, x0 + cell)
                var sumR = 0
                var sumG = 0
                var sumB = 0
                var count = 0
                for (y in y0 until y1) {
                    val row = y * w
                    for (x in x0 until x1) {
                        val p = pixels[row + x]
                        sumR += (p ushr 16) and 0xFF
                        sumG += (p ushr 8) and 0xFF
                        sumB += p and 0xFF
                        count++
                    }
                }
                val index = gy * gw + gx
                if (count == 0) {
                    outR[index] = 0f
                    outG[index] = 0f
                    outB[index] = 0f
                } else {
                    outR[index] = sumR.toFloat() / count
                    outG[index] = sumG.toFloat() / count
                    outB[index] = sumB.toFloat() / count
                }
            }
        }
        return arrayOf(outR, outG, outB)
    }

    /**
     * Separable 1-D erosion/dilation of a grid. [dilate] picks max, otherwise min.
     *
     * Naive window rather than a monotonic queue: the grid is at most 320 x 320
     * cells and the radius is a handful of cells, so the direct loop beats the
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

    /**
     * `I / paper · 255` per channel, with the gain **saturated** rather than
     * clamped.
     *
     * Below [knee] the division is applied exactly, so a page's shadows divide
     * out as they should. Above it the gain approaches [cap] asymptotically:
     *
     * ```
     * g' = knee + (cap − knee) · (1 − exp(−(g − knee) / (cap − knee)))
     * ```
     *
     * Three properties matter. It is continuous at the knee (`g' = knee`), its
     * slope there is 1 — so there is no seam to see — and it never exceeds [cap].
     * A hard `max(floor, surface)` gave the same upper bound but split the frame
     * into two behaviours along a visible contour.
     *
     * Writes into [out] rather than allocating: this runs once per pixel on a
     * 2000 px page. [out] is caller-owned, so a preview render and a bake running
     * on different dispatchers cannot corrupt each other.
     */
    private fun normalise(
        pixel: Int,
        paper: PaperMap,
        x: Int,
        y: Int,
        knee: Float,
        cap: Float,
        out: IntArray
    ) {
        val paperR = max(PAPER_EPSILON, sample(paper.r, paper, x, y))
        val paperG = max(PAPER_EPSILON, sample(paper.g, paper, x, y))
        val paperB = max(PAPER_EPSILON, sample(paper.b, paper, x, y))

        out[0] = saturateGain(((pixel ushr 16) and 0xFF).toFloat(), paperR, knee, cap)
        out[1] = saturateGain(((pixel ushr 8) and 0xFF).toFloat(), paperG, knee, cap)
        out[2] = saturateGain((pixel and 0xFF).toFloat(), paperB, knee, cap)
    }

    /** One channel of the saturated divide; see [normalise]. */
    internal fun saturateGain(value: Float, surface: Float, knee: Float, cap: Float): Int {
        val gain = 255f / max(PAPER_EPSILON, surface)
        val eased = softKnee(gain, knee, cap)
        return (value * eased).roundToInt().coerceIn(0, 255)
    }

    /**
     * The soft-knee curve on its own, exposed for testing.
     *
     * Identity at or below [knee]; continuous with unit slope there; asymptotic
     * to [cap]; strictly increasing everywhere.
     */
    internal fun softKnee(gain: Float, knee: Float, cap: Float): Float {
        if (cap <= knee) return gain.coerceAtMost(cap)
        if (gain <= knee) return gain
        val over = gain - knee
        val width = cap - knee
        return knee + width * (1f - exp(-over / width))
    }

    /** Knee and cap for a frame, from whether it reads as a document. */
    private fun gainLimits(pixels: IntArray): Pair<Float, Float> =
        if (classifiesAsDocument(pixels)) {
            GAIN_KNEE_DOCUMENT to GAIN_CAP_DOCUMENT
        } else {
            GAIN_KNEE_SCENE to GAIN_CAP_SCENE
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
