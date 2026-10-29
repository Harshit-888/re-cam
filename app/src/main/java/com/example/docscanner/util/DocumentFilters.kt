package com.example.docscanner.util

import android.graphics.Bitmap
import android.util.Log
import com.example.docscanner.model.FilterType
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
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
 * | Magic Color | `lu3` → `enhanceDocument(level = 1)` | paper map → divide table → Laplacian-blended ink curve, per channel |
 * | Clear B&W | `lu3` → `enhanceDocument(level = 2)` | paper map → divide table through curve A, then grayscale |
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
 * processing routines were decompiled (§13), so the step lists below are a
 * transcription rather than a reconstruction: a block-pooled paper map, a
 * 256 × 256 divide table built from the response curve, and — for level 1 only —
 * a `medianBlur(3)` + `Laplacian` edge strength that blends the two ink curves.
 * There is no `cv::threshold` anywhere in `enhanceDocument`; the binarisation of
 * Clear B&W falls out of the divide table itself.
 *
 * What the disassembly does **not** settle is listed in spec §13.10 (which of the
 * two curves is the flat-area end of level 1's blend, and a few table details);
 * those readings are marked INFERRED wherever they are used.
 *
 * Blocking CPU work — call from `Dispatchers.Default`/`IO`, never composition.
 */
object DocumentFilters {

    /**
     * Longest edge processed for a baked page — **3200 px**, i.e. ~299 DPI on A4
     * with the standard 25 pt margin.
     *
     * This number is why the saved PDF looks sharp when zoomed, and it is not
     * arbitrary. A4 with that margin gives a 770.8 pt (10.7 in) tall image box, so
     * 300 DPI needs `10.7 × 300 ≈ 3212` px: 3200 lands within a third of a percent
     * of a real 300 DPI scan. At the previous 2000 px the PDF carried **187 DPI**,
     * and no amount of sharpening can fix a page that is missing a third of its
     * pixels — magnifying it shows the pixels, which is exactly the report.
     *
     * The memory cost is real: an ARGB plane at this size is ~29 MB, so the
     * pipeline below never holds more than two full planes and one scratch array.
     * `ImageStore.MAX_CAPTURE_DIMENSION` and `ImageQuality.HIGH` are matched to
     * this value; changing one without the others only moves the bottleneck.
     */
    const val MAX_PROCESS_DIMENSION = 3200

    // ==================================================================================
    // Transcribed from libimageproc.so §13 — the original's document pipeline.
    //
    // Addresses in the comments refer to docs/ORIGINAL_APP_FILTER_SPEC.md §13 and are
    // offsets in the stripped ARM64 library. Anything marked INFERRED is not settled
    // by the disassembly and is recorded in §13.10 instead of being presented as fact.
    // ==================================================================================

    /** `0x101e8`: cell radius `= sqrt(rows·cols / 8000)` — derived from pixel count. */
    internal const val CELL_PIXELS_PER_RADIUS = 8000.0

    /** `0x1020c`: the radius is floored at 14 px. */
    internal const val CELL_MIN_RADIUS = 14

    /** `0x102f8`: exponent of curve A (used by level 2, and as the "detail" end of level 1). */
    private const val CURVE_A_EXPONENT = 0.625

    /** `.rodata 0x4550`: exponent of curve B (the base end of level 1). */
    private const val CURVE_B_EXPONENT = 0.8

    /** `0x3fe666…` = 0.7: exponent of the 2001-entry response table. */
    private const val RESPONSE_EXPONENT = 0.7

    /** `0x105e0`: the response table is 2000 entries plus a sentinel. */
    internal const val RESPONSE_LENGTH = 2000

    /** `0x10f60`: `alpha = min(1, laplacian / 50)`. 50.0 = 0x4049_0000_0000_0000. */
    private const val DETAIL_ALPHA_SCALE = 50f

    /** Longest edge used for the live editor preview. */
    const val PREVIEW_DIMENSION = 900

    /** Filters slower than this are logged, so a slow device can be diagnosed. */
    private const val SLOW_FILTER_MS = 400L

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

    /** Median passes applied to the grid by the adaptive filters (see `median3Grid`). */
    private const val PAPER_MEDIAN_PASSES = 2

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

    /**
     * Sub-samples per axis used to give the B&W cut a coverage value.
     *
     * The threshold itself is a hard decision, so a glyph edge lands on one side
     * or the other per pixel and a diagonal stroke becomes a staircase. Averaging
     * 3 × 3 sub-samples of the *decision plane* across the pixel footprint turns
     * that into a coverage value, which is what makes an edge smooth without
     * softening it: only the pixels the contour actually passes through get grey.
     *
     * Measured against a 16 × 16 supersampled ground truth of a rotated stroke:
     * RMS edge error 0.044 → **0.019** at 3 × 3 (4 × 4 only reaches 0.019 → not
     * worth 78 % more samples), and 94–100 % of the resulting grey pixels fall
     * within one pixel of the true contour, so the softening is *only* on the
     * edge, never a halo. 2 × 2 is measurably worse (0.022).
     *
     * This replaces the old level-domain ramp (3 % of the threshold): a ramp
     * cannot express sub-pixel geometry, it just widens the transition in
     * proportion to the local contrast.
     */
    private const val AA_SUPERSAMPLE = 3

    /**
     * Rows per band in the parallel passes; below this the hand-off costs more
     * than it saves. 96 rows of a 3200 px page is ~300 k pixels of work.
     */
    private const val MIN_ROWS_PER_BAND = 96

    /**
     * Working resolution for the Sauvola threshold field.
     *
     * The local threshold is a *smooth* field — its window is 1/16 of the page, so
     * at 3200 px it spans 200 px and simply cannot vary within a 10 px cell. So it
     * is computed on a ~320 px grid and sampled bilinearly at full resolution.
     * This is what keeps the page size affordable: the exact full-resolution
     * version needs two FloatArrays for mean/σ plus two *DoubleArray* summed-area
     * tables (~150 MB at 3200 px) and is what forced the old 2000 px cap.
     *
     * Verified equivalent: against the exact full-resolution field, the upsampled
     * one differs by 6–7 levels on average but changes the binarised result by at
     * most **0.006 IoU** on clean, shadowed and dark noisy pages.
     */
    private const val THRESHOLD_WORK_DIM = 320

    /**
     * Stride used when sampling the median-denoised plane for Otsu's threshold.
     *
     * Otsu needs a histogram, not a plane. Every 3rd pixel in each axis is 1/9 of
     * the page (~800 k samples at 3200 px) — far more than the 256-bin histogram
     * can resolve — and it removes the full-resolution median pass entirely.
     */
    private const val OTSU_SAMPLE_STRIDE = 3

    /**
     * Unsharp applied to the *decision* luminance before thresholding.
     *
     * Thresholding a slightly soft luminance gives soft-looking glyphs even
     * though the output is binary: the transition wanders over two or three
     * pixels, so stems come out ragged and thin strokes break up. Boosting the
     * stroke edge first straightens that transition.
     *
     * Measured through the whole documented pipeline (illumination normalise →
     * median → Sauvola) against a ground-truth glyph mask, three synthetic pages:
     *
     * | page                | shipped  | with this | grey share |
     * |---------------------|----------|-----------|------------|
     * | clean               | 0.820    | **0.866** | 0.70 → 0.22 % |
     * | shadowed to 35 %, soft | 0.762 | **0.785** | 1.49 → 0.63 % |
     * | dark 20 %, noisy    | 0.758    | 0.758     | 1.46 → 0.65 % |
     *
     * 0.6 rather than a stronger setting: a sweep of 0.6 / 0.8 / 1.0 across those
     * pages moved mean IoU only 0.8013 ↔ 0.8003, while 0.8 and above measurably
     * roughened the ink boundary on the noisy page (1.12× → 1.18× the true edge
     * length). The narrow ramp below does most of the visible sharpening; this is
     * the refinement on top, so it is tuned for "never hurts" rather than for the
     * last thousandth of accuracy on a clean page.
     */
    private const val BW_SHARPEN_AMOUNT = 0.6f

    /**
     * Box passes used to build the low-pass term. [boxBlur3] is a 3 × 3 mean, so
     * two passes give a 5 × 5 triangular kernel (σ ≈ 1.15 px) — wide enough to
     * boost stroke edges rather than single-pixel grain, narrow enough not to halo
     * them.
     *
     * One pass (3 × 3) measurably amplified grain on dark, noisy frames and made
     * them worse, not sharper. Three passes measured 0.8013 mean IoU against
     * 0.8033 for two — the third pass buys nothing and costs a full plane sweep.
     */
    private const val BW_SHARPEN_PASSES = 2

    /**
     * Detail below this amplitude is left alone — a soft dead-zone on the unsharp.
     *
     * Without it, sharpening is a noise amplifier: the edge boost and the grain
     * boost are the same operation. 3 levels is just above sensor/JPEG noise on a
     * normally exposed page.
     */
    private const val SHARPEN_NOISE_FLOOR = 3f

    /**
     * Page noise (median absolute high-frequency residual) over which sharpening
     * fades out entirely, and under which it is applied in full.
     *
     * A noisier page does not have crisper edges to recover; it has more grain to
     * amplify. Measured through the full pipeline: a clean page estimates 1.0, a
     * shadowed soft-focus page 4.0, a dark noisy frame 6.0, so a page at the top of
     * that range gets ~40 % of the amount and a genuinely grainy one gets none.
     * That fade is what makes the strength safe: at 1.0 instead of 0.6 the clean
     * page gains 0.003 while the noisy page loses 0.013 (0.758 → 0.745).
     */
    private const val SHARPEN_NOISE_LOW = 4f
    private const val SHARPEN_NOISE_HIGH = 9f

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
     *
     * **Not the gate the adaptive filters use.** [surfaceIsPaper] replaced it in Round 62
     * for the white-balance decision, because these thresholds are absolute and a shadow
     * moves a page across them — a page with a 3x hand shadow scores 41 % here and is
     * called a scene, after which no shadow is removed. Kept because it is the
     * transcription of `wv3`'s rule and it is tested; see [SURFACE_BRIGHT_PERCENT] for
     * the measurements.
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
        maxDimension: Int = MAX_PROCESS_DIMENSION,
        failOnError: Boolean = false
    ): Bitmap {
        if (type == FilterType.ORIGINAL) return src
        if (src.isRecycled) {
            if (failOnError) throw IllegalStateException("Cannot filter a recycled bitmap")
            return src
        }
        val (w, h, pixels) = rgbaOf(src, maxDimension) ?: run {
            if (failOnError) throw IllegalStateException("Could not read image pixels for filtering")
            return src
        }

        val startedAt = System.nanoTime()
        try {
            when (type) {
                FilterType.LIGHTEN -> applyLinearStretch(pixels, estimate(src))
                FilterType.GRAYSCALE -> applyGrayscale(pixels, w, h)
                FilterType.MAGIC_COLOR -> applyDocumentColor(pixels, w, h)
                FilterType.CLEAR_BW -> applyDocumentBw(pixels, w, h)
                FilterType.SMART -> applyAdaptive(pixels, w, h, AdaptiveMode.SMART)
                FilterType.LIGHT_TEXT -> applyAdaptive(pixels, w, h, AdaptiveMode.LIGHT_TEXT)
                FilterType.ORIGINAL -> return src
            }
        } catch (error: Throwable) {
            // A filter must never be able to take the capture down with it — but
            // an OutOfMemoryError must not be mistaken for "no change": returning
            // the unfiltered source there saves a page with no filter at all.
            if (error is OutOfMemoryError || failOnError) throw error
            return src
        }
        // A single line, only when a filter is slow enough to be felt. This is the
        // only measurement that exists on a real device, so it stays.
        val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000
        if (elapsedMs >= SLOW_FILTER_MS) {
            Log.i(TAG, "${type.name} took ${elapsedMs}ms at ${w}x$h")
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
        if (nativeFiltersReady() && NativeFilters.applyLut(pixels, lut)) return
        applyLinearStretchReference(pixels, lut)
    }

    private fun applyLinearStretchReference(pixels: IntArray, lut: IntArray) {
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
    private fun applyGrayscale(pixels: IntArray, w: Int, h: Int) {
        // `qx3.smali`: desaturate with `cvtColor(…, COLOR_BGRA2GRAY)` and then
        // **`medianBlur(…, 9)`** — a 9-tap median, not the 3-tap used inside level 1.
        // The port had no median at all, which is why our "Grayscale" was grainier
        // than the original's.
        if (!nativeFiltersReady() || !NativeFilters.rec601InPlace(pixels)) {
            applyGrayscaleReference(pixels)
        }
        medianBlurGray(pixels, w, h, 9)
    }

    private fun applyGrayscaleReference(pixels: IntArray) {
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
     * Square median filter over the grey level of an ARGB plane, window `ksize × ksize`.
     *
     * `cv::medianBlur` with ksize 9 takes a 9 × 9 window, i.e. radius 4. The window is
     * gathered into a small reusable buffer and selection-sorted — 81 values per pixel
     * is affordable at page sizes and keeps the allocation count at one.
     */
    internal fun medianBlurGray(pixels: IntArray, w: Int, h: Int, ksize: Int) {
        if (ksize < 3 || w < 3 || h < 3) return
        // Deliberately not routed through Rust. Both implementations return the
        // median of the same multiset, so the output is identical either way, but a
        // comparison-based median is O(k²) per pixel in *any* language while the
        // histogram below is O(k). At 3200 px the difference is a filter that takes
        // tens of seconds versus one that takes a fraction of one.
        medianBlurGrayReference(pixels, w, h, ksize)
    }

    /**
     * Median over the grey plane, by sliding histogram.
     *
     * A 9 × 9 window is re-sorted from scratch per pixel by the reference
     * implementation this replaced — 81 values, partially selection-sorted, i.e.
     * about 1600 comparisons per pixel and 12 *billion* on a full page. Here the
     * window's tone histogram is updated as it slides (one column leaves, one
     * enters) and the median position is tracked incrementally, so a pixel costs
     * a couple of dozen operations.
     *
     * The result is bit-identical to the selection median — the median of an
     * odd-sized multiset is unique — which is why this doubles as the parity
     * reference for the Rust kernel (`NativeFilters.medianBlurGray` is still
     * checked against it on first use).
     *
     * Edge handling matches the reference: coordinates are clamped, so the pixels
     * at the border are counted as many times as the window asks for them.
     */
    private fun medianBlurGrayReference(pixels: IntArray, w: Int, h: Int, ksize: Int) {
        if (ksize < 3 || w < 3 || h < 3) return
        val radius = ksize / 2
        val source = IntArray(w * h) { greyOf(pixels[it]) }
        val total = ksize * ksize
        val middle = total / 2
        bands(h) { from, to ->
            // Per-band scratch: the bands run concurrently, so a shared histogram
            // would be a data race that only shows up on multi-core devices.
            val window = IntArray(256)
            for (y in from until to) {
                window.fill(0)
                // Histogram of the window centred on x = 0.
                for (dy in -radius..radius) {
                    val row = (y + dy).coerceIn(0, h - 1) * w
                    for (dx in -radius..radius) {
                        window[source[row + dx.coerceIn(0, w - 1)]]++
                    }
                }
                var median = 128
                // `below` is the number of window pixels strictly darker than the
                // current median bin; the invariant below <= middle < below + window[median]
                // is what makes the median readable straight off the histogram.
                var below = 0
                for (bin in 0 until median) below += window[bin]
                for (x in 0 until w) {
                    if (x > 0) {
                        val leavingColumn = (x - 1 - radius).coerceIn(0, w - 1)
                        val enteringColumn = (x + radius).coerceIn(0, w - 1)
                        for (dy in -radius..radius) {
                            val row = (y + dy).coerceIn(0, h - 1) * w
                            val leaving = source[row + leavingColumn]
                            window[leaving]--
                            if (leaving < median) below--
                            val entering = source[row + enteringColumn]
                            window[entering]++
                            if (entering < median) below++
                        }
                    }
                    while (median < 255 && below + window[median] <= middle) {
                        below += window[median]
                        median++
                    }
                    while (median > 0 && below > middle) {
                        median--
                        below -= window[median]
                    }
                    val p = pixels[y * w + x]
                    pixels[y * w + x] =
                        (p and ALPHA_MASK) or (median shl 16) or (median shl 8) or median
                }
            }
        }
    }

    /**
     * *Magic Color* — `lu3` → `enhanceDocument(level = 1, colorMode = 1)`.
     *
     * A transcription of the native routine at `0x00010178`, not a reconstruction:
     * every step below is an instruction sequence recovered from
     * `docs/original_apk_binaries/libimageproc.so`, documented in
     * `docs/ORIGINAL_APP_FILTER_SPEC.md` §13.6.
     *
     * 1. **Paper level.** A block-pooled illumination estimate, [buildPaperMap],
     *    giving one paper value per channel per block (§13.3).
     * 2. **Divide.** `divide[paper][value]` — the 256 × 256 table built at load time
     *    from `response[min(1999, value·2000/(paper+1))]·255/2000` (§13.4). This is
     *    the whole illumination correction; there is no closing, no gamma.
     * 3. **Edge strength.** `medianBlur(gray, 3)` then `Laplacian(CV_8U, ksize 3,
     *    scale 1.0, delta 0)`; `alpha = min(1, lap / 50.0)` (§13.6).
     * 4. **Curve mix.** Per channel, `base ± alpha·(curveA − curveB)` on the sign of
     *    `value − paper` — the `fmsub`/`fmadd`/`fcsel` triple at `0x110b0`–`0x11124`.
     *
     * What used to be here — gamma with pinned endpoints, a saturation boost, an
     * unsharp mask — was this port's own invention. None of it exists in the native
     * code, and all of it is gone, because it is what made the output diverge.
     */
    private fun applyDocumentColor(pixels: IntArray, w: Int, h: Int) {
        // Level 1, transcribed: §13.6.
        //
        //   gray = cvtColor(src, BGR2GRAY); medianBlur(gray, 3);
        //   detail = Laplacian(gray, CV_8U, ksize 3, scale 1.0, delta 0)
        //   per pixel: alpha = min(1, detail/50), d = src - paper, per channel
        //              row = curveB[|d|] + alpha*(curveA[|d|] - curveB[|d|])
        //              out = divide[row][src]
        //
        // Note what is *not* here: no gamma curve, no saturation boost, no Sauvola,
        // no noise-adaptive unsharp. Those were this port's own inventions and are
        // what made it diverge from the original in the first place.
        val table = ToneTables.INSTANCE
        val surface = buildPaperMap(pixels, w, h)
        val luma = FloatArray(w * h)
        bands(h) { from, to ->
            for (i in from * w until to * w) {
                val p = pixels[i]
                luma[i] = rec601((p ushr 16) and 0xFF, (p ushr 8) and 0xFF, p and 0xFF).toFloat()
            }
        }
        // cv::medianBlur(gray, 3) then cv::Laplacian(...3×3, scale 1).
        val detail = laplacian3x3(median3x3(luma, w, h), w, h)

        bands(h) { from, to ->
            for (y in from until to) {
                var i = y * w
                for (x in 0 until w) {
                    val p = pixels[i]
                    val srcR = (p ushr 16) and 0xFF
                    val srcG = (p ushr 8) and 0xFF
                    val srcB = p and 0xFF
                    val paperR = max(PAPER_EPSILON, sample(surface.r, surface, x, y)).roundToInt()
                    val paperG = max(PAPER_EPSILON, sample(surface.g, surface, x, y)).roundToInt()
                    val paperB = max(PAPER_EPSILON, sample(surface.b, surface, x, y)).roundToInt()
                    val alpha = min(1f, detail[i] / DETAIL_ALPHA_SCALE)

                    val r = colourise(table, paperR, srcR, alpha)
                    val g = colourise(table, paperG, srcG, alpha)
                    val b = colourise(table, paperB, srcB, alpha)
                    pixels[i] = (p and ALPHA_MASK) or (r shl 16) or (g shl 8) or b
                    i++
                }
            }
        }
    }

    /**
     * One channel of level 1, following `0x10f90`–`0x1112c`.
     *
     * The native code looks both curves up by the *magnitude* of `src − paper`,
     * negating the result when the index was negative (an antisymmetric table), then
     * blends them by the edge strength:
     *
     * ```
     * row = curveB[|d|] + alpha · (curveA[|d|] − curveB[|d|])
     * out = divide[|row|][src]
     * ```
     *
     * The blend happens **on the divide row, not on the output**. That is the whole
     * mechanism of "sharpens midtone contrast": a stronger Laplacian pushes the row
     * up, which makes the divide land darker, so edges gain contrast while flat areas
     * keep their tone — and because it is per channel, a red stamp stays red instead
     * of being greyed out.
     *
     * INFERRED: which of the two tables is the `alpha == 0` end of the blend (§13.10
     * item 3). Both are looked up by the same index one register apart; B is used
     * here, which keeps flat areas lighter and lets edges do the darkening.
     */
    private fun colourise(table: ToneTables, paper: Int, src: Int, alpha: Float): Int {
        val delta = src - paper
        val magnitude = (if (delta < 0) -delta else delta).coerceIn(0, 255)
        val a = table.curveA[magnitude]
        val b = table.curveB[magnitude]
        val row = (b + alpha * (a - b)).roundToInt().coerceIn(0, 255)
        return table.divide(row, src)
    }

    /**
     * One channel of level 2: `divide[curveA(|src − paper|)][src]`.
     *
     * Split out so the composition can be pinned by unit tests — this is the whole
     * filter, and the difference between "paper is white and ink is black" and "the
     * page is washed out" lives entirely in these two lookups.
     */
    internal fun levelTwoChannel(table: ToneTables, paper: Int, src: Int): Int =
        table.divide(table.curveA[abs(src - paper).coerceIn(0, 255)], src)

    /**
     * *Clear B&W* — `enhanceDocument(level = 2, colorMode = 2)`.
     *
     * Level 2 is the same divide table as level 1 followed by **curve A**, and then
     * a grayscale conversion (§13.7). That is the entire filter. There is no Otsu,
     * no adaptive threshold and no soft ramp: `enhanceDocument` never calls
     * `cv::threshold`, and the library does not import it. How dark the ink lands is
     * decided by the response curve alone.
     *
     * The 3×3 median that used to sit before the threshold has also gone: level 2
     * has no Laplacian and no denoise step in the native code, so adding one changed
     * the output — thinner strokes, loss of fine print — for no modelled reason.
     */
    private fun applyDocumentBw(pixels: IntArray, w: Int, h: Int) {
        // Level 2, transcribed: §13.7.
        //
        // Per channel: row = curveA(|src − paper|), out = divide[row][src], then a
        // grayscale conversion. There is **no Otsu and no adaptive threshold anywhere
        // in `enhanceDocument`** — the library does not import `cv::threshold`, and
        // its only CV_8U→binary step is the LUT in `enhancePhoto`. The binarisation
        // comes out of the divide table itself: as soon as a pixel sits meaningfully
        // below its paper level the response saturates and the byte lands on 0.
        val table = ToneTables.INSTANCE
        val surface = buildPaperMap(pixels, w, h)
        bands(h) { from, to ->
            for (y in from until to) {
                var i = y * w
                for (x in 0 until w) {
                    val p = pixels[i]
                    val paperR = max(PAPER_EPSILON, sample(surface.r, surface, x, y)).roundToInt()
                    val paperG = max(PAPER_EPSILON, sample(surface.g, surface, x, y)).roundToInt()
                    val paperB = max(PAPER_EPSILON, sample(surface.b, surface, x, y)).roundToInt()
                    // Level 2 has no Laplacian, so there is no alpha to blend with and
                    // the row is curve A of the difference alone.
                    val srcR = (p ushr 16) and 0xFF
                    val srcG = (p ushr 8) and 0xFF
                    val srcB = p and 0xFF
                    val r = levelTwoChannel(table, paperR, srcR)
                    val g = levelTwoChannel(table, paperG, srcG)
                    val b = levelTwoChannel(table, paperB, srcB)
                    // colorType == 2 (the app's Clear B&W preset) → cvtColor(dst, dst,
                    // BGR2GRAY) at 0x114c4.
                    val v = rec601(r, g, b)
                    pixels[i] = (p and ALPHA_MASK) or (v shl 16) or (v shl 8) or v
                    i++
                }
            }
        }
    }


    // ==================================================================================
    // Round 61 — the adaptive (ratio-domain) filters: `SMART` and `LIGHT_TEXT`
    // ==================================================================================
    //
    // Everything above is a transcription of the original APK's native library, and it
    // stays that way: the five original presets are untouched, byte for byte.
    //
    // These two are new, and they exist because the transcription has two blind spots
    // that only show up on a real page:
    //
    //   1. **It is blind to faint ink.** The divide table is indexed by the *absolute*
    //      difference |src − paper|, so a mark 18 levels below paper (a light pencil
    //      line, a fax, a thermal-receipt header at 92 % reflectance) maps to 247 —
    //      almost paper. Measured, not guessed: `docs/FILTER_COMPARISON_ADOBE_VS_DOC_SCANNER.md`
    //      §4.1. Adobe ships a whole preset ("Light Text") for exactly this case.
    //   2. **It clips colour channel by channel.** Each channel gets its own divide, so
    //      a blue pen (60, 80, 160) comes out (0, 0, 78): the red and green channels are
    //      driven to zero and the mark reads as saturated navy rather than blue.
    //
    // The adaptive engine fixes both by moving the tone mapping into the **ratio
    // domain** — the idea Adobe reaches for with its CIELAB L*-only normalisation —
    // while keeping this app's illumination model and its punch:
    //
    //   paper(c)      per-channel block-pooled illumination surface (unchanged)
    //   gain(c)       soft-knee(255 / paper(c))     ← shadow + tint removal, bounded
    //   c2(c)         round(c · gain(c))            ← the paper is now 255 everywhere
    //   luma          Rec.601(c2), integer
    //   outL          curve[luma]                   ← the preset's tone curve
    //   g             max(floor, outL / luma) · (1 + detail)  ← one scale for all channels
    //   out(c)        grey + (round(c2 · g) − grey) · chroma   ← hue-preserving chroma
    //
    // Three properties follow, and each is tested:
    //
    // * **Illumination invariance is exact.** Two captures of the same ink under
    //   different light normalise to the same byte, because the curve is indexed by a
    //   ratio rather than by a level difference. (The transcribed filter drifts: 141 →
    //   168 for the same pencil under a shadow.)
    // * **Hue survives.** One scale applied to all three channels cannot invent a
    //   colour; the chroma term then expands (or, for a scene, contracts) saturation
    //   around the pixel's own luma instead of clipping channels independently.
    // * **A scene is not repainted white.** [surfaceIsPaper] selects a near-identity
    //   curve and caps the white-balance gain at `GAIN_CAP_SCENE`, which is what those
    //   constants were for. (Round 62 replaced `classifiesAsDocument` here: its
    //   absolute thresholds called a page with a hand shadow a scene, so the shadow was
    //   never removed at all. See [SURFACE_BRIGHT_PERCENT].)
    //
    // All of it is fixed point (Q8 = 256), so the Rust kernel and the Kotlin reference
    // below agree byte for byte and the parity self-test can be exact.

    /** Which tone curve the adaptive engine uses. */
    internal enum class AdaptiveMode {
        /** Everyday documents: punchy, faint-ink aware, colour-preserving. */
        SMART,

        /** Faint documents: pencil, faxes, thermal receipts, faded carbon copies. */
        LIGHT_TEXT
    }

    /** The preset curves, built once. `ratioCurve` is pure and cheap but not free. */
    private val smartCurve: IntArray by lazy { ratioCurve(0.60f, 0.98f, 1.6f) }
    // Round 63: Light Text used to start at 0.78 of paper with a gamma of 1.5, which
    // made any background the surface could not flatten come out mid-grey — a page at
    // 0.92 of paper mapped to 141. Its job is to keep *faint marks visible*, not to
    // grey the paper, and those two are only in conflict if the curve is doing the
    // flattening. It is not any more (the clamp is), so the curve can be gentle in the
    // toe and reach white well before paper: 0.70 → 0.94, linear, which keeps a mark at
    // 85 % of paper at 159 and a mark at 80 % at 106, while anything the clamp did not
    // already whiten is within a few per cent of it.
    private val lightTextCurve: IntArray by lazy { ratioCurve(0.70f, 0.94f, 1.0f) }
    private val sceneCurve: IntArray by lazy { ratioCurve(0.02f, 0.99f, 1.0f) }

    /** Lowest gain a pixel may be scaled by, so saturated ink keeps its colour. */
    private const val ADAPTIVE_FLOOR_SMART = 56      // 0.22
    private const val ADAPTIVE_FLOOR_LIGHT = 64      // 0.25
    private const val ADAPTIVE_FLOOR_SCENE = 128     // 0.50

    /** Chroma expansion around the pixel's own luma (Q8, 256 = unchanged). */
    private const val ADAPTIVE_CHROMA_SMART = 287    // 1.12x
    private const val ADAPTIVE_CHROMA_LIGHT = 307    // 1.20x
    private const val ADAPTIVE_CHROMA_SCENE = 256    // unchanged

    /** `255 · 256 · 256`: the numerator of the Q8 white-balance gain, `255 / paper`. */
    private const val WHITE_Q8_OVER_PAPER = 255 * 256 * 256

    /**
     * The document/scene gate, in two numbers. A frame is a **page** when:
     *
     *   - fewer than this percentage of its pixels are >5 % **brighter than their own
     *     estimated surface** (paper is the ceiling of a page), **or**
     *   - more than [SURFACE_MARKED_PERCENT] of its pixels are local marks below three
     *     quarters of that surface (text, ruling, a stamp — the safety term).
     *
     * Both are **ratios against the surface at that pixel**, so both are immune to
     * exposure and to a shadow: the same page under a 3x hand shadow measures the same
     * as under even light. That is exactly where `classifiesAsDocument` failed — it
     * applies absolute thresholds to the whole frame, so a page with a 3x shadow scored
     * 41 % and flipped to "scene", after which no shadow was removed at all (the
     * shadowed half of the frame stayed 123 levels grey). It also *accepted* a lit room
     * at 61 %, because a beige wall looks like paper under those rules. Both are
     * measured, not guessed: `python3 tools/shadow_probe.py`.
     *
     * | frame | brighter | marked | verdict |
     * |---|---|---|---|
     * | flat page | 0.0 % | 10.7 % | document |
     * | page, soft shadow | 0.0 % | 10.7 % | document |
     * | page, 3.2x hard shadow | 1.4 % | 10.7 % | document |
     * | page, 8x hard shadow | 1.4 % | 10.7 % | document |
     * | page, lamp glare + shadow | 1.8 % | 10.9 % | document |
     * | photograph, landscape | 4.2 % | 3.7 % | **scene** |
     * | photograph, room | 5.3 % | 1.2 % | **scene** |
     *
     * Why either test is enough: a page has no content brighter than its paper, so the
     * first test alone answers it — except for a specular reflection, which is the one
     * genuinely-brighter thing a page can contain, and which is why the second test
     * exists. A photograph fails the first test at every content boundary (its
     * estimate sits *between* light and dark) and has no ink, so it fails both.
     */
    private const val SURFACE_BRIGHT_PERCENT = 3

    /** Local marks (below 3/4 of the surface) that classify a frame as a page anyway. */
    private const val SURFACE_MARKED_PERCENT = 8

    /** Samples the gate takes; ~20k is plenty for a ratio, and it is off the hot path. */
    private const val SURFACE_SAMPLE_TARGET = 20_000f

    /** Edge crispening: gain boost of up to this much where the Laplacian is strong. */
    private const val ADAPTIVE_DETAIL_SMART = 56     // up to +22 %
    private const val ADAPTIVE_DETAIL_SCALE = 50     // same divisor the level-1 blend uses

    /**
     * `t = (x − lo) / (hi − lo)`, clamped, raised to [gamma] — one entry per luma.
     *
     * `lo` and `hi` are ratios of paper brightness: below `lo` a pixel is ink (0), at
     * or above `hi` it is paper (255). The distance between them is the filter's
     * sensitivity to faint ink, and it is the only knob that trades "clean paper"
     * against "a pencil line you can actually see".
     */
    internal fun ratioCurve(lo: Float, hi: Float, gamma: Float): IntArray {
        val span = (hi - lo).coerceAtLeast(1e-4f)
        return IntArray(256) { index ->
            val x = index / 255f
            val t = ((x - lo) / span).coerceIn(0f, 1f)
            (255f * t.pow(gamma)).roundToInt().coerceIn(0, 255)
        }
    }

    /** Rec.601 luma with **integer** rounding — the adaptive engine's own, not [rec601]. */
    internal fun lumaFixed(r: Int, g: Int, b: Int): Int =
        (r * 299 + g * 587 + b * 114 + 500) / 1000

    /** `(v + 128) >> 8` — the rounding step of a Q8 multiply. */
    private fun rndQ8(v: Int): Int = (v + 128) shr 8

    /**
     * A monotone soft knee in Q8: identity up to [knee], asymptotic to [cap].
     *
     * A clamp would leave a contour wherever the gain crosses it; this has slope 1 at
     * the knee and never reaches the cap, so no seam can form on the page.
     */
    internal fun kneeGain(rawQ8: Int, kneeQ8: Int, capQ8: Int): Int {
        if (rawQ8 <= kneeQ8) return rawQ8
        val span = capQ8 - kneeQ8
        if (span <= 0) return capQ8
        val over = rawQ8 - kneeQ8
        // Widened to Long: `span * over` reaches 4.1e9 for a nearly black frame
        // (paper 20 → raw 835k, over 833k, span 4096), which would wrap an Int and
        // silently produce a wrong gain. The quotient itself always fits.
        return (kneeQ8 + (span.toLong() * over) / (span + over)).toInt()
    }

    /** Bilinear sample of a Q8 paper grid. Mirrors `kernels::sample_q8` exactly. */
    internal fun samplePaperQ8(
        plane: IntArray,
        gw: Int,
        gh: Int,
        cell: Int,
        x: Int,
        y: Int
    ): Int {
        if (gw <= 0 || gh <= 0 || plane.isEmpty()) return 255 shl 8
        val step = cell.coerceAtLeast(1)
        val gx0 = (x / step).coerceIn(0, gw - 1)
        val gy0 = (y / step).coerceIn(0, gh - 1)
        val gx1 = if (gx0 + 1 < gw) gx0 + 1 else gx0
        val gy1 = if (gy0 + 1 < gh) gy0 + 1 else gy0
        val fx = (((x - gx0 * step) shl 8) / step)
        val fy = (((y - gy0 * step) shl 8) / step)
        val top = (plane[gy0 * gw + gx0] * (256 - fx) + plane[gy0 * gw + gx1] * fx) shr 8
        val bottom = (plane[gy1 * gw + gx0] * (256 - fx) + plane[gy1 * gw + gx1] * fx) shr 8
        return (top * (256 - fy) + bottom * fy) shr 8
    }

    /** The illumination grid, quantised to Q8. Three planes of `gw × gh`. */
    internal fun paperGridQ8(map: PaperMap): Array<IntArray> = arrayOf(
        IntArray(map.gw * map.gh) { (max(0f, map.r[it]) * 256f + 0.5f).toInt() },
        IntArray(map.gw * map.gh) { (max(0f, map.g[it]) * 256f + 0.5f).toInt() },
        IntArray(map.gw * map.gh) { (max(0f, map.b[it]) * 256f + 0.5f).toInt() }
    )

    /**
     * Is this frame one lit flat surface — a page — rather than a place?
     *
     * Asked in the divisor's own units: every sampled pixel is compared with the paper
     * level estimated *at that pixel*, and two fractions are counted — how many are
     * significantly **above** it, and how many are **local marks** below it. See
     * [SURFACE_BRIGHT_PERCENT] for the measurement, the thresholds and the reason this
     * replaces the absolute-threshold classifier.
     *
     * Only two multiplications per sample and no division: the surface luma stays in Q8
     * and both tests are integer inequalities.
     *
     * @return `true` when the frame should be treated as a document.
     */
    internal fun surfaceIsPaper(
        pixels: IntArray,
        w: Int,
        paperR: IntArray,
        paperG: IntArray,
        paperB: IntArray,
        gw: Int,
        gh: Int,
        cell: Int
    ): Boolean {
        if (w <= 0 || pixels.isEmpty()) return true
        val stride = max(1, ceil(sqrt(pixels.size / SURFACE_SAMPLE_TARGET)).toInt())
        var brighter = 0
        var marked = 0
        var sampled = 0
        var index = 0
        while (index < pixels.size) {
            val p = pixels[index]
            val x = index % w
            val y = index / w
            val luma = lumaFixed((p ushr 16) and 0xFF, (p ushr 8) and 0xFF, p and 0xFF)
            // Luma of the surface, still in Q8: a linear combination of Q8 values, so
            // the comparisons below need no division.
            val paperLumaQ8 = (
                299 * max(0, samplePaperQ8(paperR, gw, gh, cell, x, y)) +
                    587 * max(0, samplePaperQ8(paperG, gw, gh, cell, x, y)) +
                    114 * max(0, samplePaperQ8(paperB, gw, gh, cell, x, y)) + 500
                ) / 1000
            // `luma > paper · 1.05`: 5 % is one level at paper 20 and 12 levels at paper
            // 240, which is the right shape because it is a ratio.
            if (luma.toLong() * 25_600L > paperLumaQ8.toLong() * 105L) brighter++
            // `luma < paper · 0.75`.
            if (luma.toLong() * 1024L < paperLumaQ8.toLong() * 3L) marked++
            sampled++
            index += stride
        }
        if (sampled == 0) return true
        return brighter * 100 < sampled * SURFACE_BRIGHT_PERCENT ||
            marked * 100 > sampled * SURFACE_MARKED_PERCENT
    }

    /** Rec.601 luma into [out]; Rust when verified, else the loop below. */
    internal fun lumaPlane(pixels: IntArray, out: FloatArray) {
        if (nativeFiltersReady() && NativeFilters.lumaPlane(pixels, out)) return
        lumaPlaneReference(pixels, out)
    }

    private fun lumaPlaneReference(pixels: IntArray, out: FloatArray) {
        for (i in pixels.indices) {
            val p = pixels[i]
            out[i] = lumaFixed((p ushr 16) and 0xFF, (p ushr 8) and 0xFF, p and 0xFF).toFloat()
        }
    }

    /**
     * The adaptive filter, applied in place.
     *
     * [mode] selects the curve; whether the surface is paper ([surfaceIsPaper]) selects
     * the curve family and the white-balance bounds.
     */
    private fun applyAdaptive(pixels: IntArray, w: Int, h: Int, mode: AdaptiveMode) {
        // The surface is needed by both the gate and the kernel, so it is built once,
        // edge-preserving: see `median3Grid`.
        val surface = buildPaperMap(pixels, w, h, edgePreserving = true)
        val grid = paperGridQ8(surface)
        val cell = surface.block.toInt()
        val document = surfaceIsPaper(
            pixels, w, grid[0], grid[1], grid[2], surface.gw, surface.gh, cell
        )

        val curve = if (document) {
            when (mode) {
                AdaptiveMode.SMART -> smartCurve
                AdaptiveMode.LIGHT_TEXT -> lightTextCurve
            }
        } else {
            sceneCurve
        }
        val kneeQ8 = ((if (document) GAIN_KNEE_DOCUMENT else GAIN_KNEE_SCENE) * 256f).roundToInt()
        val capQ8 = ((if (document) GAIN_CAP_DOCUMENT else GAIN_CAP_SCENE) * 256f).roundToInt()
        val floorQ8 = when {
            !document -> ADAPTIVE_FLOOR_SCENE
            mode == AdaptiveMode.LIGHT_TEXT -> ADAPTIVE_FLOOR_LIGHT
            else -> ADAPTIVE_FLOOR_SMART
        }
        val chromaQ8 = when {
            !document -> ADAPTIVE_CHROMA_SCENE
            mode == AdaptiveMode.LIGHT_TEXT -> ADAPTIVE_CHROMA_LIGHT
            else -> ADAPTIVE_CHROMA_SMART
        }
        // Round 64: the edge crispening used to be a gain boost *inside* the kernel
        // (up to +22 %, before the tone curve). It is now a separate, later stage that
        // darkens the ink side of an edge after the paper has been clamped — see
        // `finishAdaptive`. The kernel's own detail hook stays wired (and stays zero)
        // because the Rust ABI still has the parameter.
        val detail = FloatArray(0)

        // The fine-scale correction, applied to the image before the kernel divides by
        // the coarse surface. Background only: see `flattenIllumination`.
        if (document) {
            flattenIllumination(pixels, w, h, surface, buildFineMap(pixels, w, h, surface))
        }

        val params = intArrayOf(kneeQ8, capQ8, floorQ8, chromaQ8, 0, ADAPTIVE_DETAIL_SCALE)
        val usedNative = nativeFiltersReady() && NativeFilters.adaptiveColour(
            pixels, w, grid[0], grid[1], grid[2],
            surface.gw, surface.gh, cell,
            curve, params, detail
        )
        if (!usedNative) {
            adaptiveColourReference(
                pixels, w, grid[0], grid[1], grid[2],
                surface.gw, surface.gh, cell,
                curve, params, detail
            )
        }

        // Paper to white, then sharpen inward. Documents only: a photograph must keep
        // its highlights, and there is no "paper" in it to clamp to.
        if (document) {
            finishAdaptive(
                pixels, w, h,
                clampPercent = if (mode == AdaptiveMode.LIGHT_TEXT) PAPER_CLAMP_LIGHT_PERCENT else PAPER_CLAMP_PERCENT,
                sharpenQ8 = FINISH_SHARPEN_Q8
            )
        }
    }

    /**
     * The Kotlin reference for `kernels::adaptive_colour` — the same arithmetic, in
     * the same order, on integers only. The parity self-test compares the two byte for
     * byte, so this function is the definition and Rust is the optimisation.
     */
    @Suppress("LongParameterList")
    internal fun adaptiveColourReference(
        pixels: IntArray,
        w: Int,
        paperR: IntArray,
        paperG: IntArray,
        paperB: IntArray,
        gw: Int,
        gh: Int,
        cell: Int,
        curve: IntArray,
        params: IntArray,
        detail: FloatArray
    ) {
        if (w <= 0 || curve.size < 256 || params.size < 6) return
        val knee = params[0]
        val cap = params[1]
        val floor = params[2]
        val chroma = params[3]
        val detailCap = params[4]
        val detailScale = max(1, params[5])
        val useDetail = detailCap > 0 && detail.size >= pixels.size
        val h = pixels.size / w
        bands(h) { from, to ->
            for (y in from until to) {
                var i = y * w
                for (x in 0 until w) {
                    val p = pixels[i]
                    val alpha = p and ALPHA_MASK
                    val r = (p ushr 16) and 0xFF
                    val g = (p ushr 8) and 0xFF
                    val b = p and 0xFF

                    val pr = max(1, samplePaperQ8(paperR, gw, gh, cell, x, y))
                    val pg = max(1, samplePaperQ8(paperG, gw, gh, cell, x, y))
                    val pb = max(1, samplePaperQ8(paperB, gw, gh, cell, x, y))
                    val gr = kneeGain(WHITE_Q8_OVER_PAPER / pr, knee, cap)
                    val gg = kneeGain(WHITE_Q8_OVER_PAPER / pg, knee, cap)
                    val gb = kneeGain(WHITE_Q8_OVER_PAPER / pb, knee, cap)

                    val cr = min(255, rndQ8(r * gr))
                    val cg = min(255, rndQ8(g * gg))
                    val cb = min(255, rndQ8(b * gb))

                    val luma = lumaFixed(cr, cg, cb).coerceIn(0, 255)
                    val outLuma = curve[luma]
                    var gain = if (luma > 0) (outLuma shl 8) / luma else 256
                    if (gain < floor) gain = floor

                    if (useDetail) {
                        val d = detail[i]
                        var dq = if (d <= 0f) 0 else (d * detailCap / detailScale + 0.5f).toInt()
                        if (dq > detailCap) dq = detailCap
                        gain = (gain * (256 + dq)) shr 8
                    }

                    val sr = min(255, rndQ8(cr * gain))
                    val sg = min(255, rndQ8(cg * gain))
                    val sb = min(255, rndQ8(cb * gain))

                    val grey = lumaFixed(sr, sg, sb)
                    val outR = (grey + (((sr - grey) * chroma) shr 8)).coerceIn(0, 255)
                    val outG = (grey + (((sg - grey) * chroma) shr 8)).coerceIn(0, 255)
                    val outB = (grey + (((sb - grey) * chroma) shr 8)).coerceIn(0, 255)

                    pixels[i] = alpha or (outR shl 16) or (outG shl 8) or outB
                    i++
                }
            }
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
    internal class PaperMap(
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
    /**
     * @param edgePreserving smooth the grid with a median instead of a box blur.
     *
     * Default `false`, because that is the transcribed pipeline and the five original
     * presets must keep their exact output. The adaptive filters pass `true`: see
     * `median3Grid` for the measurement that motivated it — a box blur smears a hard
     * shadow edge across the grid, and the divide then leaves a grey band along it.
     */
    private fun buildPaperMap(
        pixels: IntArray,
        w: Int,
        h: Int,
        edgePreserving: Boolean = false
    ): PaperMap {
        // §13.3: the original derives its cell size from the pixel count, not from a
        // fixed working width. `PAPER_WORK_DIM 320` was this port's guess at it.
        val cell = cellRadius(w, h) + 1
        val gw = max(1, (w + cell - 1) / cell)
        val gh = max(1, (h + cell - 1) / cell)

        val pooled = meanPool(pixels, w, h, cell, gw, gh)
        val gridR = closeDarkStructure(pooled, 0, gw, gh, edgePreserving)
        val gridG = closeDarkStructure(pooled, 1, gw, gh, edgePreserving)
        val gridB = closeDarkStructure(pooled, 2, gw, gh, edgePreserving)

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
    private fun closeDarkStructure(
        pooled: Array<FloatArray>,
        channel: Int,
        gw: Int,
        gh: Int,
        edgePreserving: Boolean
    ): FloatArray {
        val plane = pooled[channel]
        val radius = paperRadius(gw, gh)
        var grid = windowOp(plane, gw, gh, radius, dilate = true)
        grid = windowOp(grid, gw, gh, radius, dilate = false)
        if (edgePreserving) {
            // Two median passes, not three box passes. See [median3Grid].
            repeat(PAPER_MEDIAN_PASSES) { grid = median3Grid(grid, gw, gh) }
        } else {
            repeat(3) { grid = boxBlur3(grid, gw, gh) }
        }
        return grid
    }

    /**
     * 3×3 median of a paper grid — the smoothing that keeps a shadow edge a step.
     *
     * The box blur this replaces was there to remove *cell-scale* noise (a cell whose
     * mean was dragged down by ink the closing could not reach), and it does that. But
     * a box blur is isotropic: across a hard shadow edge it produces a three-cell ramp
     * in the estimated surface, so on one side of the edge the divide under-corrects —
     * which is a visible grey band running alongside every crisp shadow (a phone's own
     * body, a hand, a door frame). Measured with `tools/shadow_probe.py` on a page
     * with a 3.2x shadow, paper error inside a ±3-cell band around the edge:
     *
     * | smoothing | mean | worst |
     * |---|---|---|
     * | box ×3 (before) | 24.2 | 112 |
     * | median ×2 (now) | **0.3** | **1** |
     *
     * A median is the standard answer for this: it removes isolated bad cells exactly
     * like a blur does, and it reproduces a step instead of averaging it. It cannot
     * smooth sub-cell detail — there is none, the grid *is* the estimate — and on a
     * flat page both are identities, so nothing else changes.
     *
     * Everything below `0` and above `255` is impossible in this grid: the pool and
     * the closing only ever produce means and maxima of 8-bit values.
     */
    internal fun median3Grid(plane: FloatArray, gw: Int, gh: Int): FloatArray {
        val out = FloatArray(plane.size)
        val window = IntArray(9)
        for (y in 0 until gh) {
            for (x in 0 until gw) {
                var count = 0
                for (dy in -1..1) {
                    val row = (y + dy).coerceIn(0, gh - 1) * gw
                    for (dx in -1..1) {
                        val value = plane[row + (x + dx).coerceIn(0, gw - 1)]
                        window[count++] = (value + 0.5f).toInt()
                    }
                }
                // Nine elements: an insertion sort is the cheapest thing that is
                // obviously correct, and it allocates nothing.
                for (i in 1 until 9) {
                    val value = window[i]
                    var j = i - 1
                    while (j >= 0 && window[j] > value) {
                        window[j + 1] = window[j]
                        j--
                    }
                    window[j + 1] = value
                }
                out[y * gw + x] = window[4].toFloat()
            }
        }
        return out
    }

    // ==================================================================================
    // Round 64 — the background, taken apart
    // ==================================================================================
    //
    // Round 62 fixed the *decision* (is this a page?) and the *smoothing* (a median
    // instead of three box passes). The report that followed was: shadows are still
    // not clean, `LIGHT_TEXT` darkens the whole page, and the text is not sharp.
    //
    // All three are the same defect seen from three directions: **the paper estimate
    // is spatially band-limited.** It is one number per cell (~1 % of the page), so
    // it cannot follow anything faster than a cell — and a shadow edge, a page curl,
    // the shadow of the phone's own body and the shading under a hand are all faster
    // than that. Where the estimate lags, the divide under-corrects, and what is left
    // is grey. `LIGHT_TEXT` then makes it look worse, because its curve started at
    // 0.78 of paper: a background left at 0.9 of paper landed in the steep part of the
    // curve and came out mid-grey — and a pencil, at 0.82 of paper, came out at **58**,
    // i.e. as dark as ink. The curve was never doing the background's job; it was.
    //
    // Measured with `tools/ink_probe.py` — synthetic pages at 480x360 with the same
    // relative cell size as a 3200 px capture, rendered through a lens-like 5-tap blur
    // so an edge has a width to sharpen. "paper" is the mean deviation of clean paper
    // from white (paper with no content within 3 px); "seam" is the same inside 1.5
    // cells of a shadow edge; "ink"/"faint" are the rendered stroke and pencil levels;
    // "edge" is the 10-90 % transition width across a bar, in pixels. Smart Scan,
    // before → after:
    //
    // | scene | paper | seam | ink | faint | edge |
    // |---|---|---|---|---|---|
    // | flat | 0.0 → 0.0 | 0.0 → 0.0 | 15.7 → 11.4 | 113.6 → 109.6 | 1.80 → 0.00 px |
    // | gradient | 0.7 → 0.0 | 0.1 → 0.0 | 15.4 → 11.1 | 113.3 → 109.4 | 1.80 → 0.90 px |
    // | hand shadow | 1.9 → 0.6 | 46.9 → 15.2 | 15.4 → 11.2 | 112.7 → 108.6 | 1.80 → 0.00 px |
    // | vignette | 4.3 → 0.2 | — | 15.7 → 11.1 | 117.9 → 114.3 | 1.80 → 0.00 px |
    // | lamp + glare | 1.5 → 1.0 | 39.2 → 27.4 | 15.4 → 11.2 | 113.9 → 109.8 | 1.80 → 0.00 px |
    // | shadow + cast | 1.3 → 0.3 | 29.8 → 7.8 | 15.4 → 11.2 | 113.3 → 109.2 | 1.80 → 0.00 px |
    //
    // and Light Text, same tool, against round 62:
    //
    // | scene | paper | seam | ink | faint |
    // |---|---|---|---|---|
    // | hand shadow | 3.3 → 0.2 | 67.6 → 5.6 | 17.7 → 12.7 | 58.5 → 129.7 |
    // | vignette | 9.2 → 0.0 | — | 17.9 → 12.6 | 63.5 → 137.9 |
    // | shadow + cast | 2.6 → 0.0 | 53.5 → 0.0 | 17.6 → 12.7 | 58.0 → 130.2 |
    //
    // Four additions, each of which can be removed on its own:
    //
    // 1. [buildFineMap] + [flattenIllumination] — a second estimate on a cell a fifth
    //    as wide, used *only as the ratio to the coarse one*, and only on background
    //    pixels. It says "the coarse surface is 20 % too bright here", which is
    //    exactly what a shadow edge is.
    // 2. [finishAdaptive] — a paper **clamp**: a page has no content brighter than its
    //    own paper except a specular glint, so everything within a few per cent of
    //    white is set to white. That is what makes the background *uniform* instead
    //    of merely light, and it is Adobe's `L_norm = min(100, L/L_bg·100)` taken
    //    literally at the top end.
    // 3. The same pass sharpens **inward**, from a local *mean* and not a Laplacian:
    //    a pixel darker than its neighbourhood gets darker, a brighter one does not
    //    get brighter (it is already clamped). A 3x3 Laplacian is the second
    //    derivative, so it is exactly zero in the middle of a ramp — which is what a
    //    lens-blurred stroke edge *is* — and it moved the measured edge width by
    //    0.00 px. A triangle blur minus the pixel is a first derivative of the
    //    darkness plane: it is large on the ink side of a ramp and on thin strokes,
    //    and zero inside a solid block and on paper.
    // 4. `lightTextCurve` is rebuilt (see below). Its old toe turned a pencil into
    //    ink; with the clamp doing the whitening, it only has to separate a mark from
    //    paper.

    /** The fine surface: same estimator, a fifth of the cell, kept for its ratios. */
    internal class FineMap(
        val planes: Array<FloatArray>,
        val gw: Int,
        val gh: Int,
        val cell: Int
    )

    /** Fine cell is this fraction of the coarse one. */
    private const val FINE_CELL_DIVISOR = 5

    /** Fraction of the darkest samples discarded when pooling a fine cell. */
    private const val FINE_TRIM_PERCENT = 35

    /** `256`: the fixed-point one used by every multiply in this section. */
    private const val Q_ONE = 256

    /** Bounds on the fine/coarse correction. Outside these it is an artifact. */
    private const val FINE_RATIO_LOW = 0.85f
    private const val FINE_RATIO_HIGH = 1.15f

    /**
     * How close to the *fine* surface a pixel must be to count as background.
     *
     * Gated on the fine estimate, not the coarse one: at a shadow edge a background
     * pixel is far below the coarse surface (which is what is wrong with it), and
     * looks like ordinary background only next to the fine one.
     */
    private const val FINE_INK_GATE = 0.86f

    /**
     * Per-cell **trimmed mean**: the average of all but the darkest [FINE_TRIM_PERCENT]
     * of the cell's samples, from a 256-bin histogram so there is no sort.
     *
     * A plain mean is dragged toward ink, which is the reason a dense paragraph keeps
     * its paper grey; a percentile rejects the ink but quantises, and a quantised
     * surface leaves a harder seam at a light step than a smooth one (measured: 51.8
     * against 18.4 at cell scale). Trimming the dark tail keeps the mean's smoothness
     * and gains the ink rejection, which is what the *fine* pass needs.
     *
     * Used only for the fine grid. The coarse grid keeps its mean pool: it is the one
     * the parity tests and the five transcribed presets are built on, and at cell
     * scale the closing already removes the ink a mean is biased by.
     */
    internal fun trimmedPool(
        pixels: IntArray,
        w: Int,
        h: Int,
        cell: Int,
        gw: Int,
        gh: Int,
        dropPercent: Int = FINE_TRIM_PERCENT
    ): Array<FloatArray> {
        val out = Array(3) { FloatArray(gw * gh) }
        val hist = IntArray(256)
        for (gy in 0 until gh) {
            val y0 = min(h - 1, gy * cell)
            val y1 = min(h, y0 + cell)
            for (gx in 0 until gw) {
                val x0 = min(w - 1, gx * cell)
                val x1 = min(w, x0 + cell)
                val index = gy * gw + gx
                for (channel in 0 until 3) {
                    val shift = 16 - channel * 8
                    var count = 0
                    for (y in y0 until y1) {
                        var i = y * w + x0
                        repeat(x1 - x0) {
                            hist[(pixels[i] ushr shift) and 0xFF]++
                            count++
                            i++
                        }
                    }
                    if (count > 0) {
                        val cut = count * dropPercent / 100
                        var seen = 0
                        var sum = 0L
                        var kept = 0
                        for (value in 0..255) {
                            val bin = hist[value]
                            if (bin == 0) continue
                            var take = bin
                            if (seen < cut) take -= min(take, cut - seen)
                            seen += bin
                            if (take > 0) {
                                sum += value.toLong() * take
                                kept += take
                            }
                        }
                        out[channel][index] = if (kept > 0) sum.toFloat() / kept else 0f
                    }
                    // Clear only what was touched: a full 256-bin wipe per cell costs
                    // more than the pooling itself on a 213k-cell grid.
                    for (y in y0 until y1) {
                        var i = y * w + x0
                        repeat(x1 - x0) {
                            hist[(pixels[i] ushr shift) and 0xFF] = 0
                            i++
                        }
                    }
                }
            }
        }
        return out
    }

    /**
     * The fine surface for [coarse]: painted with [trimmedPool], closed with the same
     * reach *in pixels* as the coarse one, then median-smoothed.
     *
     * The closing radius is the load-bearing number. Anything narrower than a coarse
     * cell — a pencil line, a stroke of body text — has to be erased by it, or the
     * fine estimate reports the mark's own darkness as "the paper here is darker",
     * and the correction bleaches the mark away.
     */
    internal fun buildFineMap(pixels: IntArray, w: Int, h: Int, coarse: PaperMap): FineMap {
        val cell = max(2, (coarse.block.toInt()) / FINE_CELL_DIVISOR)
        val gw = max(1, (w + cell - 1) / cell)
        val gh = max(1, (h + cell - 1) / cell)
        // One coarse cell, in fine cells. Measured: at half of this a pencil line next
        // to a paragraph is *not* erased — the neighbouring text caps the local maximum
        // the closing can reach — so the fine estimate reports the mark's own darkness
        // and the correction bleaches it (208 -> 229 before the curve), while the seam
        // is the same. At a full cell the mark is erased and the seam is unchanged.
        val radius = max(2, coarse.block.toInt() / cell)
        val planes = trimmedPool(pixels, w, h, cell, gw, gh)
        for (channel in 0 until 3) {
            var plane = windowOp(planes[channel], gw, gh, radius, dilate = true)
            plane = windowOp(plane, gw, gh, radius, dilate = false)
            repeat(2) { plane = median3Grid(plane, gw, gh) }
            planes[channel] = plane
        }
        return FineMap(planes, gw, gh, cell)
    }

    /** Bilinear sample of one plane of a [FineMap]. Same rule as [sample]. */
    internal fun sampleFine(map: FineMap, channel: Int, x: Int, y: Int): Float {
        val grid = map.planes[channel]
        val block = map.cell.toFloat()
        val fx = ((x + 0.5f) / block - 0.5f).coerceIn(0f, (map.gw - 1).toFloat())
        val fy = ((y + 0.5f) / block - 0.5f).coerceIn(0f, (map.gh - 1).toFloat())
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
     * Applies the fine/coarse ratio to background pixels, **in place, before the
     * kernel**.
     *
     * This is a multiplicative correction on the image rather than on the surface, and
     * that is deliberate: the kernel divides by the coarse grid, and multiplying the
     * image by `coarse/fine` here leaves the kernel computing exactly `pixel/fine`.
     * Doing it on the pixels keeps `adaptive_colour` — the Rust kernel, ABI 3, with its
     * byte-exact Kotlin reference — completely untouched.
     *
     * One scalar per pixel (from the green channel, the one a neutral detector
     * trusts) is applied to all three, so the correction cannot shift hue.
     */
    internal fun flattenIllumination(pixels: IntArray, w: Int, h: Int, coarse: PaperMap, fine: FineMap) {
        val low = (FINE_RATIO_LOW * Q_ONE).roundToInt()
        val high = (FINE_RATIO_HIGH * Q_ONE).roundToInt()
        val gate = FINE_INK_GATE
        var i = 0
        for (y in 0 until h) {
            for (x in 0 until w) {
                val p = pixels[i]
                val r = (p ushr 16) and 0xFF
                val g = (p ushr 8) and 0xFF
                val b = p and 0xFF
                val coarseG = max(1f, sample(coarse.g, coarse, x, y))
                val fineG = max(1f, sampleFine(fine, 1, x, y))
                val luma = lumaFixed(r, g, b)
                if (luma >= gate * fineG) {
                    // The *surface* ratio, clamped where the probe measured it. The
                    // multiplier applied here is its reciprocal, because the kernel is
                    // about to divide by the coarse surface: pixel · (coarse/fine) /
                    // coarse is pixel / fine, which is what we want it to compute.
                    val ratioQ8 = ((fineG / coarseG) * 256f).roundToInt().coerceIn(low, high)
                    val mult = (Q_ONE * Q_ONE + ratioQ8 / 2) / ratioQ8
                    if (mult != Q_ONE) {
                        val nr = (r * mult + 128) shr 8
                        val ng = (g * mult + 128) shr 8
                        val nb = (b * mult + 128) shr 8
                        pixels[i] = (p and ALPHA_MASK) or
                            (min(255, nr) shl 16) or (min(255, ng) shl 8) or min(255, nb)
                    }
                }
                i++
            }
        }
    }

    /**
     * Paper level, in luma, above which a pixel is certainly paper (percent).
     *
     * 90, not 100 and not 96. After the surface work above, what is left of the
     * background is a wash a few levels deep plus, at a hard shadow edge, one penumbra
     * column; clamping at 90 turns both into exactly white. Measured with
     * `tools/ink_probe.py report` — clean-paper deviation from white, mean / worst,
     * `current` → `r63`:
     *
     * | scene | before | after |
     * |---|---|---|
     * | flat | 0.0 / 0 | 0.0 / 0 |
     * | gradient | 0.7 / 45 | **0.0 / 0** |
     * | hand shadow | 1.9 / 178 | **0.6 / 115** |
     * | vignette | 4.3 / 189 | **0.2 / 110** |
     * | lamp + glare | 1.5 / 220 | 1.0 / 255 |
     * | shadow + cast | 1.3 / 128 | **0.3 / 80** |
     *
     * Glare is the exception, and it is honest: its specular region is over-exposed
     * past any paper level, so a few pixels come out of the curve at 0 and the
     * 40-level sharpening cap pushes one of them further. It is one pixel of 170k.
     */
    private const val PAPER_CLAMP_PERCENT = 90

    /**
     * Light Text clamps at the same level as Smart Scan.
     *
     * It briefly clamped higher (97 %) on the reasoning that a faint mark lives close
     * to paper and must not be whitened. That is wrong now that the curve is gentle:
     * the clamp is what makes the background white, and leaving the background to a
     * curve that starts at 0.70 means a page at 0.92 of paper still comes out grey.
     * Measured, hand shadow: 3.3 levels off white at round 62, 12.4 with the 97 %
     * clamp, **0.2** here.
     */
    private const val PAPER_CLAMP_LIGHT_PERCENT = PAPER_CLAMP_PERCENT

    /** How many 3x3 box means make the sharpening's blur. Two: a triangle 5-tap. */
    private const val SHARPEN_PASSES = 2

    /** High-pass level above which a pixel is an edge rather than sensor noise. */
    private const val FINISH_NOISE_GATE = 3f

    /** How much of the excess high-pass is folded back into the pixel, Q8. */
    private const val FINISH_SHARPEN_Q8 = 230      // 0.9

    /** Above this luma a pixel is paper, and sharpening it only draws outlines. */
    private const val SHARPEN_MAX_LUMA = 210f

    /** Most a single pixel may be darkened by the sharpening, in levels. */
    private const val SHARPEN_MAX_DROP = 40f

    /**
     * The last stage: clamp paper to white, then sharpen inward.
     *
     * Both operations only ever *darken* a pixel, which is what makes them safe to
     * apply after the tone curve: the paper is already at 255 and cannot ring, and no
     * halo can form on the bright side of an edge because there is nowhere brighter
     * to go. Measured ring energy (paper one pixel outside the optical reach of any
     * content, mean deviation): <= 1.0 level with the sharpening on, <= 0.9 off.
     *
     * @param clampPercent paper level (as a percentage of white, in luma) above which
     *   a pixel is set to pure white. 0 disables the clamp.
     * @param sharpenQ8 strength of the inward sharpening, Q8; 0 disables it.
     */
    internal fun finishAdaptive(
        pixels: IntArray,
        w: Int,
        h: Int,
        clampPercent: Int = PAPER_CLAMP_PERCENT,
        sharpenQ8: Int = FINISH_SHARPEN_Q8
    ) {
        if (pixels.size < w * h || w < 3 || h < 3) return
        val luma = FloatArray(w * h)
        lumaPlane(pixels, luma)
        val clampLevel = clampPercent * 255 / 100
        // The high-pass is only needed where it can change something. `boxBlurInPlace`
        // twice is a triangle 5-tap (sigma ~1.3 px, the width of a lens-blurred edge);
        // the signal is `blur - v`, positive where the pixel is darker than its
        // neighbourhood.
        val high = if (sharpenQ8 > 0) {
            val blurred = luma.copyOf()
            repeat(SHARPEN_PASSES) { boxBlurInPlace(blurred, w, h) }
            for (i in blurred.indices) blurred[i] -= luma[i]
            blurred
        } else {
            FloatArray(0)
        }
        var i = 0
        for (y in 0 until h) {
            for (x in 0 until w) {
                val p = pixels[i]
                val v = luma[i]
                if (clampPercent > 0 && v >= clampLevel) {
                    pixels[i] = p or 0x00FFFFFF
                    i++
                    continue
                }
                if (sharpenQ8 > 0 && y > 0 && y < h - 1 && x > 0 && x < w - 1) {
                    val d = high[i]
                    // Three bounds, all load-bearing. The signal is `blur - v`, so it
                    // is positive only where the pixel is darker than its
                    // neighbourhood: paper beside a stroke has a *negative* one, and
                    // is left alone (that was the black outline around every glyph
                    // before the sign was fixed). A pixel above `SHARPEN_MAX_LUMA` is
                    // paper whatever the blur says. And the darkening per pixel is
                    // capped, so a hard edge cannot be driven to black.
                    if (d > FINISH_NOISE_GATE && v > 12f && v < SHARPEN_MAX_LUMA) {
                        val drop = min(
                            SHARPEN_MAX_DROP,
                            (d - FINISH_NOISE_GATE) * sharpenQ8 / 512f
                        )
                        val target = max(0f, v - drop)
                        val ratio = target / v
                        val r = (((p ushr 16) and 0xFF) * ratio).roundToInt().coerceIn(0, 255)
                        val g = (((p ushr 8) and 0xFF) * ratio).roundToInt().coerceIn(0, 255)
                        val b = ((p and 0xFF) * ratio).roundToInt().coerceIn(0, 255)
                        pixels[i] = (p and ALPHA_MASK) or (r shl 16) or (g shl 8) or b
                    }
                }
                i++
            }
        }
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
        if (nativeFiltersReady() && NativeFilters.meanPool(pixels, w, h, cell, gw, gh, outR, outG, outB)) {
            return arrayOf(outR, outG, outB)
        }
        return meanPoolReference(pixels, w, h, cell, gw, gh, outR, outG, outB)
    }

    private fun meanPoolReference(
        pixels: IntArray,
        w: Int,
        h: Int,
        cell: Int,
        gw: Int,
        gh: Int,
        outR: FloatArray,
        outG: FloatArray,
        outB: FloatArray
    ): Array<FloatArray> {
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
     * Scales a sharpening amount down as the page gets noisier.
     *
     * Full strength below [SHARPEN_NOISE_LOW], nothing above
     * [SHARPEN_NOISE_HIGH]. A noisy page has no crisper edge to recover, only more
     * grain to amplify.
     */
    internal fun adaptiveSharpenAmount(base: Float, noise: Float): Float {
        val span = (SHARPEN_NOISE_HIGH - SHARPEN_NOISE_LOW).coerceAtLeast(1f)
        val factor = ((SHARPEN_NOISE_HIGH - noise) / span).coerceIn(0f, 1f)
        return base * factor
    }

    /**
     * Separable 3×3 Gaussian `[1 2 1] / 4` on a full-resolution plane.
     *
     * Stands in for `cv::GaussianBlur(…, Size(3,3), 0)`, which the library imports
     * and which the unsharp mask needs as its low-pass term.
     */
    internal fun gaussianBlur3(src: FloatArray, w: Int, h: Int): FloatArray {
        val out = FloatArray(src.size)
        if (nativeFiltersReady() && NativeFilters.gaussianBlur3(src, out, w, h)) return out
        return gaussianBlur3Reference(src, w, h)
    }

    private fun gaussianBlur3Reference(src: FloatArray, w: Int, h: Int): FloatArray {
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
        if (nativeFiltersReady() && NativeFilters.median3x3(src, out, w, h)) return out
        return median3x3Reference(src, w, h)
    }

    /** Pure-Kotlin 3×3 median: the parity reference for the Rust kernel. */
    private fun median3x3Reference(src: FloatArray, w: Int, h: Int): FloatArray {
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
        val out = FloatArray(src.size)
        if (nativeFiltersReady() && NativeFilters.boxBlur3(src, out, w, h)) return out
        return boxBlur3Reference(src, w, h)
    }

    private fun boxBlur3Reference(src: FloatArray, w: Int, h: Int): FloatArray {
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

    // ==================================================================================
    // Parallelism
    // ==================================================================================

    /**
     * Cores to spread a pass over. Capped at 4: past that the memory bandwidth of
     * three big planes saturates and the extra threads only add contention, and a
     * phone that reduces its big-core count under thermal load cannot lose as much.
     */
    private val bandThreads = Runtime.getRuntime().availableProcessors().coerceIn(1, 4)

    /**
     * Shared, daemon, created once. A pool per call would cost more than the work
     * it parallelises, and daemon threads keep it from outliving the process.
     */
    private val bandPool: ExecutorService by lazy {
        Executors.newFixedThreadPool(
            bandThreads,
            ThreadFactory { runnable ->
                Thread(runnable, "doc-filter").apply { isDaemon = true }
            }
        )
    }

    /**
     * Runs [body] over horizontal row bands of a [h]-row image.
     *
     * Every pass that uses this is per-pixel and writes only the rows it owns, so
     * the bands never touch the same element. The join is synchronous: when this
     * returns, the whole image is written and the writes are visible to the
     * caller (the `Future.get` gives the happens-before edge).
     *
     * `internal` so the unit tests can prove that every row is visited exactly
     * once — a band bug would silently skip rows and leave them black.
     */
    internal fun bands(h: Int, body: (from: Int, to: Int) -> Unit) {
        val threads = min(bandThreads, max(1, h / MIN_ROWS_PER_BAND))
        if (threads <= 1) {
            body(0, h)
            return
        }
        val chunk = (h + threads - 1) / threads
        val futures = ArrayList<Future<*>>(threads)
        for (t in 0 until threads) {
            val from = t * chunk
            if (from >= h) break
            val to = min(h, from + chunk)
            // Explicit Runnable: `submit { }` is ambiguous between Runnable and
            // Callable<Unit> overloads at the call site.
            futures.add(bandPool.submit(Runnable { body(from, to) }))
        }
        var failure: Throwable? = null
        for (future in futures) {
            try {
                future.get()
            } catch (e: ExecutionException) {
                if (failure == null) failure = e.cause ?: e
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                if (failure == null) failure = e
            }
        }
        failure?.let { throw it }
    }

    // ==================================================================================
    // Clear B&W internals
    // ==================================================================================

    /**
     * The local threshold, held on a coarse grid and sampled bilinearly.
     *
     * See [THRESHOLD_WORK_DIM]: the field is smooth at this scale, so storing it at
     * full resolution costs 65 MB more and changes nothing.
     */
    private class ThresholdField(
        val values: FloatArray,
        val gw: Int,
        val gh: Int,
        val block: Float,
        val global: Float
    ) {
        /** Threshold at pixel coordinates, bilinear between grid cells. */
        fun at(x: Float, y: Float): Float {
            if (gw <= 1 && gh <= 1) return values[0]
            val gx = ((x + 0.5f) / block - 0.5f).coerceIn(0f, (gw - 1).toFloat())
            val gy = ((y + 0.5f) / block - 0.5f).coerceIn(0f, (gh - 1).toFloat())
            val x0 = gx.toInt().coerceIn(0, gw - 1)
            val y0 = gy.toInt().coerceIn(0, gh - 1)
            val x1 = min(gw - 1, x0 + 1)
            val y1 = min(gh - 1, y0 + 1)
            val tx = gx - x0
            val ty = gy - y0
            val top = values[y0 * gw + x0] + (values[y0 * gw + x1] - values[y0 * gw + x0]) * tx
            val bottom = values[y1 * gw + x0] + (values[y1 * gw + x1] - values[y1 * gw + x0]) * tx
            return top + (bottom - top) * ty
        }
    }

    /** Bilinear sample of a plane at fractional pixel coordinates, clamped. */
    internal fun bilinearAt(plane: FloatArray, w: Int, h: Int, x: Float, y: Float): Float {
        val cx = x.coerceIn(0f, (w - 1).toFloat())
        val cy = y.coerceIn(0f, (h - 1).toFloat())
        val x0 = cx.toInt()
        val y0 = cy.toInt()
        val x1 = min(w - 1, x0 + 1)
        val y1 = min(h - 1, y0 + 1)
        val tx = cx - x0
        val ty = cy - y0
        val top = plane[y0 * w + x0] + (plane[y0 * w + x1] - plane[y0 * w + x0]) * tx
        val bottom = plane[y1 * w + x0] + (plane[y1 * w + x1] - plane[y1 * w + x0]) * tx
        return top + (bottom - top) * ty
    }

    /**
     * Median of nine values, sorting the caller's scratch array in place.
     *
     * In place because this runs per sample over a strided grid: allocating a
     * 9-element array per sample is ~800 k allocations on a 3200 px page, which is
     * pure garbage-collector work.
     */
    internal fun medianOf9InPlace(values: FloatArray): Float {
        for (i in 1 until values.size) {
            val key = values[i]
            var j = i - 1
            while (j >= 0 && values[j] > key) {
                values[j + 1] = values[j]
                j--
            }
            values[j + 1] = key
        }
        return values[values.size / 2]
    }

    /**
     * 3 × 3 box blur, in place.
     *
     * Horizontal is trivially in place (keep one saved value); vertical keeps the
     * previous row in a `w`-long scratch array. Together that is the difference
     * between two extra 29 MB planes and 12 KB of scratch on a 3200 px page — the
     * reason the page size can be raised at all.
     */
    internal fun boxBlurInPlace(plane: FloatArray, w: Int, h: Int) {
        if (nativeFiltersReady() && NativeFilters.boxBlurInPlace(plane, w, h)) return
        boxBlurInPlaceReference(plane, w, h)
    }

    private fun boxBlurInPlaceReference(plane: FloatArray, w: Int, h: Int) {
        for (y in 0 until h) {
            val row = y * w
            var left = plane[row]
            for (x in 0 until w) {
                val centre = plane[row + x]
                val right = if (x + 1 < w) plane[row + x + 1] else centre
                plane[row + x] = (left + centre + right) / 3f
                left = centre
            }
        }
        val up = FloatArray(w)
        val current = FloatArray(w)
        System.arraycopy(plane, 0, up, 0, w)
        for (y in 0 until h) {
            val row = y * w
            System.arraycopy(plane, row, current, 0, w)
            val hasDown = y + 1 < h
            val downRow = (y + 1) * w
            for (x in 0 until w) {
                val down = if (hasDown) plane[downRow + x] else current[x]
                plane[row + x] = (up[x] + current[x] + down) / 3f
            }
            System.arraycopy(current, 0, up, 0, w)
        }
    }

    /**
     * Median absolute high-frequency residual of [plane] against [blurred] — the
     * page's noise figure, used to fade sharpening out on grainy frames.
     */
    internal fun noiseFromResidual(plane: FloatArray, blurred: FloatArray): Float {
        val histogram = IntArray(256)
        for (i in plane.indices) {
            histogram[abs(plane[i] - blurred[i]).roundToInt().coerceIn(0, 255)]++
        }
        val target = plane.size / 2
        var seen = 0
        for (bin in 0 until 256) {
            seen += histogram[bin]
            if (seen > target) return bin.toFloat()
        }
        return 255f
    }

    // ==================================================================================
    // §13 tables and primitives
    // ==================================================================================

    /**
     * §13.3: the original's working cell size for the background estimate.
     *
     * `R = max(14, (int)sqrt(rows·cols / 8000))` at `0x101e8`–`0x1020c`. Because it is
     * derived from the pixel *count*, the same page gets the same relative cell size
     * at any capture resolution — which is why raising the process dimension in 1.9
     * did not have to retune anything.
     */
    internal fun cellRadius(w: Int, h: Int): Int {
        val raw = sqrt((w.toDouble() * h) / CELL_PIXELS_PER_RADIUS).toInt()
        return max(CELL_MIN_RADIUS, raw)
    }

    /**
     * `round(scale · ½ · (1 − cos(π·(index/length)^exponent)))` — the shape every table
     * in §13.3 uses. Evaluated in double and rounded with `frinta`, matching the
     * original (`0x102e8`–`0x105dc`).
     */
    internal fun raisedCosine(index: Int, length: Int, scale: Double, exponent: Double): Int {
        val x = (index.toDouble() / length).pow(exponent)
        val value = scale * 0.5 * (1.0 - cos(Math.PI * x))
        return value.roundToInt().coerceIn(0, 255)
    }

    /** §13.3 curve A (0.625) or curve B (0.8), 256 entries, each `0..255`. */
    internal fun toneCurve(exponent: Double): IntArray =
        IntArray(256) { raisedCosine(it, 255, 255.0, exponent) }

    /**
     * §13.3's 2001-entry response table: `0..2000`, with `[2000] = 2000` as the
     * sentinel the original writes past the end (`0x105e8`).
     */
    internal fun responseValue(index: Int): Int =
        if (index >= RESPONSE_LENGTH) RESPONSE_LENGTH else
            (1000.0 * (1.0 - cos(Math.PI * (index.toDouble() / RESPONSE_LENGTH).pow(RESPONSE_EXPONENT))))
                .roundToInt()

    /**
     * §13.4's divide lookup, transcribed from the table builder at `0x10640`–`0x10734`.
     *
     * The builder writes, for every `row` and `value`:
     *
     * ```
     * index = min(2000, row · 2000 / (value + 1))
     * q     = response[index] · 255 / 2000
     * table[row][value] = 255 - q
     * ```
     *
     * Three details are easy to get wrong, and all three change the picture:
     *
     * * The **numerator is the row**, the denominator is the value — `row · 2000 /
     *   (value + 1)`, not `value · 2000 / (row + 1)`.
     * * The row is the **signed difference between the pixel and its paper level**
     *   (`|src − paper|`, §13.4b), *not* the paper level itself.
     * * The stored byte is **complemented**. The code computes `q`, then `sub w11,
     *   w11, #1` on a value the magic multiply has already made negative, and stores
     *   with `strb` — i.e. `(−q − 1) & 0xFF = 255 − q`. Reading it as `q` inverts the
     *   whole filter: paper comes out black and ink white.
     *
     * `index` is capped at **2000**, not 1999: `response` has 2001 entries and the
     * builder's own sentinel sets `[2000] = 2000`, so the cap lands exactly on it.
     *
     * What this produces, with a paper level of 238: paper → 255, anything 120 or
     * darker → 0–1, and the ramp lives between 150 and 238. That is the punch of the
     * original's document modes, and it is why a linear `value · 255 / paper`
     * normalisation (what this port used to do) looked so flat by comparison.
     */
    internal fun divideLookup(row: Int, value: Int): Int {
        val index = min(
            RESPONSE_LENGTH,
            (row.coerceIn(0, 255) * RESPONSE_LENGTH) / (value.coerceIn(0, 255) + 1)
        )
        return 255 - (responseValue(index) * 255) / RESPONSE_LENGTH
    }

    /**
     * `cv::Laplacian(src, dst, CV_8U, ksize=3, scale=1.0, delta=0, BORDER_REFLECT_101)`
     * followed by an 8-bit saturating store — i.e. the negative half-plane clamps to
     * zero, which is what makes the result usable as an edge-strength weight.
     *
     * Kernel for `ksize == 3` is `[0 1 0; 1 -4 1; 0 1 0]`. Edges replicate
     * (`BORDER_REFLECT_101` ≈ clamp for a 1-pixel border).
     */
    internal fun laplacian3x3(plane: FloatArray, w: Int, h: Int): FloatArray {
        val out = FloatArray(plane.size)
        if (nativeFiltersReady() && NativeFilters.laplacian3x3(plane, out, w, h)) return out
        return laplacian3x3Reference(plane, w, h)
    }

    /** Pure-Kotlin 3×3 Laplacian: the parity reference for the Rust kernel. */
    private fun laplacian3x3Reference(plane: FloatArray, w: Int, h: Int): FloatArray {
        val out = FloatArray(plane.size)
        bands(h) { from, to ->
            for (y in from until to) {
                val row = y * w
                val up = (if (y > 0) y - 1 else y) * w
                val down = (if (y < h - 1) y + 1 else y) * w
                for (x in 0 until w) {
                    val left = if (x > 0) row + x - 1 else row + x
                    val right = if (x < w - 1) row + x + 1 else row + x
                    val v = plane[up + x] + plane[down + x] + plane[left] + plane[right] -
                        4f * plane[row + x]
                    out[row + x] = v.coerceIn(0f, 255f)
                }
            }
        }
        return out
    }

    /** Rec.601 grey level of a packed ARGB pixel. */
    internal fun greyOf(pixel: Int): Int =
        rec601((pixel ushr 16) and 0xFF, (pixel ushr 8) and 0xFF, pixel and 0xFF)

    /**
     * The §13.3 tables, built once.
     *
     * The 256 × 256 divide table is 64 KiB of bytes and is shared by both levels, so
     * building it per filter call would be wasteful; the curves are 2 KiB more.
     */
    /** Test seam for the lazily built tone tables; production paths use [ToneTables.INSTANCE]. */
    internal fun toneTables(): ToneTables = ToneTables.INSTANCE

    internal class ToneTables private constructor() {
        val curveA: IntArray = toneCurve(CURVE_A_EXPONENT)
        val curveB: IntArray = toneCurve(CURVE_B_EXPONENT)
        private val divideTable: Array<ByteArray> =
            Array(256) { row -> ByteArray(256) { value -> divideLookup(row, value).toByte() } }

        /** `divide[row][value]`, row being `|src − paper|` (see [divideLookup]). */
        fun divide(row: Int, value: Int): Int =
            divideTable[row.coerceIn(0, 255)][value.coerceIn(0, 255)].toInt() and 0xFF

        companion object {
            val INSTANCE: ToneTables by lazy { ToneTables() }
        }
    }

    // ---- Rust kernels ------------------------------------------------------

    /**
     * Whether the Rust kernels may be used in this process.
     *
     * Computed once, on first use, by running each kernel and the Kotlin
     * reference side by side on a synthetic page and comparing the results **byte
     * for byte**. One differing byte and the native path stays off for the rest of
     * the session: this app would rather be slower than different.
     *
     * The cost is a 96 x 64 pass through three kernels, i.e. microseconds, and it
     * happens off the main thread because every caller here already runs on a
     * background dispatcher.
     */
    private val nativeAvailable: Boolean by lazy { verifyNativeFilters() }

    /** Test seam: true when the native path verified and will be used. */
    internal fun nativeFiltersReady(): Boolean = nativeAvailable

    private fun verifyNativeFilters(): Boolean {
        if (!NativeFilters.isLoaded) return false
        val verdict = runCatching {
            val w = SYNTHETIC_W
            val h = SYNTHETIC_H
            val page = syntheticPage(w, h)

            // 1. Grey median — the 9-tap pass behind Grayscale.
            val referenceMedian = page.copyOf()
            val nativeMedianGrey = page.copyOf()
            medianBlurGrayReference(referenceMedian, w, h, 9)
            if (!NativeFilters.medianBlurGray(nativeMedianGrey, w, h, 9)) return@runCatching false
            if (!nativeMedianGrey.contentEquals(referenceMedian)) return@runCatching false

            // 2. 3x3 median of the luma plane, then 3. its Laplacian: the exact
            //    order level 1 uses them in, so an error in either shows up here.
            val luma = FloatArray(w * h) { greyOf(page[it]).toFloat() }
            val referenceSmoothed = median3x3Reference(luma, w, h)
            val nativeSmoothed = FloatArray(w * h)
            if (!NativeFilters.median3x3(luma, nativeSmoothed, w, h)) return@runCatching false
            if (!nativeSmoothed.contentEquals(referenceSmoothed)) return@runCatching false

            val referenceEdge = laplacian3x3Reference(referenceSmoothed, w, h)
            val nativeEdge = FloatArray(w * h)
            if (!NativeFilters.laplacian3x3(referenceSmoothed, nativeEdge, w, h)) {
                return@runCatching false
            }
            if (!nativeEdge.contentEquals(referenceEdge)) return@runCatching false

            // 4. LUT stretch (Lighten filter).
            val lut = linearStretchLut(40, 215)
            val refLutPixels = page.copyOf()
            val natLutPixels = page.copyOf()
            applyLinearStretchReference(refLutPixels, lut)
            if (!NativeFilters.applyLut(natLutPixels, lut)) return@runCatching false
            if (!natLutPixels.contentEquals(refLutPixels)) return@runCatching false

            // 5. Rec.601 in-place (Grayscale first pass).
            val refGrayPixels = page.copyOf()
            val natGrayPixels = page.copyOf()
            applyGrayscaleReference(refGrayPixels)
            if (!NativeFilters.rec601InPlace(natGrayPixels)) return@runCatching false
            if (!natGrayPixels.contentEquals(refGrayPixels)) return@runCatching false

            // 6. Mean pool (paper map accumulation).
            val cell = 8
            val gw = w / cell
            val gh = h / cell
            val refPool = meanPoolReference(page, w, h, cell, gw, gh, FloatArray(gw * gh), FloatArray(gw * gh), FloatArray(gw * gh))
            val natR = FloatArray(gw * gh)
            val natG = FloatArray(gw * gh)
            val natB = FloatArray(gw * gh)
            if (!NativeFilters.meanPool(page, w, h, cell, gw, gh, natR, natG, natB)) return@runCatching false
            for (i in 0 until gw * gh) {
                if (abs(refPool[0][i] - natR[i]) > 0.01f ||
                    abs(refPool[1][i] - natG[i]) > 0.01f ||
                    abs(refPool[2][i] - natB[i]) > 0.01f
                ) return@runCatching false
            }

            // 7. Gaussian blur 3x3.
            val refGauss = gaussianBlur3Reference(luma, w, h)
            val natGauss = FloatArray(w * h)
            if (!NativeFilters.gaussianBlur3(luma, natGauss, w, h)) return@runCatching false
            for (i in luma.indices) {
                if (abs(refGauss[i] - natGauss[i]) > 0.01f) return@runCatching false
            }

            // 8. Box blur 3x3.
            val refBox = boxBlur3Reference(luma, w, h)
            val natBox = FloatArray(w * h)
            if (!NativeFilters.boxBlur3(luma, natBox, w, h)) return@runCatching false
            for (i in luma.indices) {
                if (abs(refBox[i] - natBox[i]) > 0.01f) return@runCatching false
            }

            // 9. Box blur in-place.
            val refBoxInPlace = luma.copyOf()
            val natBoxInPlace = luma.copyOf()
            boxBlurInPlaceReference(refBoxInPlace, w, h)
            if (!NativeFilters.boxBlurInPlace(natBoxInPlace, w, h)) return@runCatching false
            for (i in luma.indices) {
                if (abs(refBoxInPlace[i] - natBoxInPlace[i]) > 0.01f) return@runCatching false
            }

            // 10. Luma plane — the adaptive engine's index into its curve.
            val colourPage = syntheticColourPage(w, h)
            val refLuma = FloatArray(w * h)
            val natLuma = FloatArray(w * h)
            lumaPlaneReference(colourPage, refLuma)
            if (!NativeFilters.lumaPlane(colourPage, natLuma)) return@runCatching false
            if (!natLuma.contentEquals(refLuma)) return@runCatching false

            // 11. The adaptive kernel itself, on the same page, in both presets and
            //     with and without edge crispening. `contentEquals` is exact on
            //     purpose: the kernel is integer-only, so any disagreement is a bug
            //     in one of the two, not float drift.
            // The same surface the filter builds, so parity covers what ships.
            val surface = buildPaperMap(colourPage, w, h, edgePreserving = true)
            val grid = paperGridQ8(surface)
            val adaptiveCell = surface.block.toInt()
            val detailPlane = run {
                val smoothed = median3x3Reference(natLuma, w, h)
                laplacian3x3Reference(smoothed, w, h)
            }
            for (case in adaptiveParityCases()) {
                val refPixels = colourPage.copyOf()
                adaptiveColourReference(
                    refPixels, w, grid[0], grid[1], grid[2],
                    surface.gw, surface.gh, adaptiveCell, case.curve, case.params, case.detail
                )
                val natPixels = colourPage.copyOf()
                val ok = NativeFilters.adaptiveColour(
                    natPixels, w, grid[0], grid[1], grid[2],
                    surface.gw, surface.gh, adaptiveCell, case.curve, case.params, case.detail
                )
                if (!ok) return@runCatching false
                if (!natPixels.contentEquals(refPixels)) return@runCatching false
            }

            true
        }.getOrDefault(false)

        Log.i(
            TAG,
            if (verdict) {
                // Honest wording: the integer kernels are compared with
                // `contentEquals`, but the float planes (the paper map and the
                // three blurs) are compared within 0.01, because two f32 pipelines
                // can disagree in the last bit. Saying "byte-identical" for all
                // nine would be a claim this code cannot make.
                "Rust kernels verified (integer kernels byte-identical, float planes within 0.01, adaptive kernel byte-identical)"
            } else {
                "Rust kernels unavailable or mismatched - using the Kotlin reference"
            }
        )
        return verdict
    }

    /** Deterministic page: light gradient, hard dark bars, mild per-pixel noise. */
    private fun syntheticPage(w: Int, h: Int): IntArray = IntArray(w * h) { index ->
        val x = index % w
        val y = index / w
        val ground = 200 + (x * 40) / w
        val inBar = y % 8 in 2..4 && x % 24 in 3..20
        val base = if (inBar) 40 + (y * 3) % 25 else ground
        val noise = (x * 31 + y * 17) % 7 - 3
        val level = (base + noise).coerceIn(0, 255)
        (0xFF shl 24) or (level shl 16) or (level shl 8) or level
    }

    /**
     * Deterministic **colour** page: the greyscale one plus ink that is not grey.
     *
     * The adaptive kernel applies one gain to all three channels and then re-expands
     * chroma around the pixel's own luma, so a grey-only test would never exercise
     * the parts that can differ.
     */
    private fun syntheticColourPage(w: Int, h: Int): IntArray = IntArray(w * h) { index ->
        val x = index % w
        val y = index / w
        // Inlined rather than calling `syntheticPage(w, h)[index]`: indexing into a
        // freshly built array inside the loop would allocate one page per pixel.
        val ground = 200 + (x * 40) / w
        val inBar = y % 8 in 2..4 && x % 24 in 3..20
        val base = if (inBar) 40 + (y * 3) % 25 else ground
        val level = (base + (x * 31 + y * 17) % 7 - 3).coerceIn(0, 255)
        val grey = (0xFF shl 24) or (level shl 16) or (level shl 8) or level
        val r = (grey ushr 16) and 0xFF
        when {
            // A blue-pen stroke: dark in red and green, mid in blue.
            y % 16 in 9..11 && x % 32 in 4..27 -> (0xFF shl 24) or (20 shl 16) or (70 shl 8) or 170
            // A highlighter band: bright, warm, and above paper in red.
            y % 24 in 16..21 ->
                (0xFF shl 24) or
                    ((r + 6).coerceAtMost(255) shl 16) or
                    ((r - 14).coerceAtLeast(0) shl 8) or
                    (r - 70).coerceAtLeast(0)
            else -> grey
        }
    }

    /** One `adaptiveColour` parity case. */
    private class AdaptiveCase(val curve: IntArray, val params: IntArray, val detail: FloatArray)

    private fun adaptiveParityCases(): List<AdaptiveCase> {
        val grey = syntheticColourPage(SYNTHETIC_W, SYNTHETIC_H)
        val luma = FloatArray(grey.size)
        lumaPlaneReference(grey, luma)
        val detail = laplacian3x3Reference(median3x3Reference(luma, SYNTHETIC_W, SYNTHETIC_H), SYNTHETIC_W, SYNTHETIC_H)
        val kDoc = (GAIN_KNEE_DOCUMENT * 256f).roundToInt()
        val cDoc = (GAIN_CAP_DOCUMENT * 256f).roundToInt()
        val kScene = (GAIN_KNEE_SCENE * 256f).roundToInt()
        val cScene = (GAIN_CAP_SCENE * 256f).roundToInt()
        return listOf(
            AdaptiveCase(
                smartCurve,
                intArrayOf(kDoc, cDoc, ADAPTIVE_FLOOR_SMART, ADAPTIVE_CHROMA_SMART, ADAPTIVE_DETAIL_SMART, ADAPTIVE_DETAIL_SCALE),
                detail
            ),
            AdaptiveCase(
                lightTextCurve,
                intArrayOf(kDoc, cDoc, ADAPTIVE_FLOOR_LIGHT, ADAPTIVE_CHROMA_LIGHT, 0, ADAPTIVE_DETAIL_SCALE),
                FloatArray(0)
            ),
            AdaptiveCase(
                sceneCurve,
                intArrayOf(kScene, cScene, ADAPTIVE_FLOOR_SCENE, ADAPTIVE_CHROMA_SCENE, 0, ADAPTIVE_DETAIL_SCALE),
                FloatArray(0)
            )
        )
    }

    private const val SYNTHETIC_W = 96
    private const val SYNTHETIC_H = 64
    private const val TAG = "DocumentFilters"

    private const val ALPHA_MASK = -0x1000000
}
