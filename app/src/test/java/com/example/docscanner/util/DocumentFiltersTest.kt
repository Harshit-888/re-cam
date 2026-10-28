package com.example.docscanner.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

/**
 * Pins [DocumentFilters] to the behaviour recovered from the original APK.
 *
 * Everything here is pure JVM maths — no `Bitmap` — so it runs in
 * `testDebugUnitTest`. The point is to catch the kinds of off-by-one that make a
 * filter "almost" match: a black point that lands one bin early, a stretch that
 * maps paper to 254, an Otsu threshold that lands on the paper peak and blackens
 * the page.
 *
 * The expected numbers come from `fa1.H` in
 * `docs/original_apk_pipeline_artifacts.zip` and from the native `enhancePhoto`
 * disassembly in `docs/ORIGINAL_APP_FILTER_SPEC.md` §7 — not from eyeballing
 * output.
 */
class DocumentFiltersTest {

    // ---------------------------------------------------------------- estimation

    @Test
    fun estimation_usesTheTwoPercentCutoffsFromTheOriginal() {
        // 10 000 samples: 200 ink at 40 (exactly 2 %), 9 800 paper at 245.
        val pixels = IntArray(10_000) { index ->
            if (index < 200) rgb(40, 40, 40) else rgb(245, 245, 245)
        }

        val estimation = DocumentFilters.estimationFromPixels(pixels)

        // `fa1.H` increments the index in the same iteration that reaches 2 %, so
        // the black point is one bin ABOVE the crossing bin.
        assertEquals("black point must mirror fa1.H's post-increment", 41, estimation.blackPoint)
        // ...and decrements before exiting the white loop, so it is one bin BELOW.
        assertEquals("white point must mirror fa1.H's pre-decrement", 244, estimation.whitePoint)
    }

    @Test
    fun estimation_classifiesAWhitePageAsADocument() {
        val pixels = IntArray(10_000) { rgb(250, 250, 248) }

        val estimation = DocumentFilters.estimationFromPixels(pixels)

        assertTrue(estimation.documentImage)
        assertEquals(100f, estimation.documentRatio, 0.001f)
    }

    @Test
    fun estimation_classifiesASkyGradientAsAPhoto() {
        // R <= G <= B with R >= 60, G >= 100, B >= 120 is the photo test in fa1.H.
        val pixels = IntArray(10_000) { rgb(60, 100, 120) }

        val estimation = DocumentFilters.estimationFromPixels(pixels)

        assertTrue(estimation.documentImage)
        assertEquals(100f, estimation.photoRatio, 0.001f)
    }

    @Test
    fun estimation_doesNotTreatADarkFrameAsADocument() {
        // Below every threshold in wv3: neither ratio reaches 60 %.
        val pixels = IntArray(10_000) { rgb(12, 14, 18) }

        val estimation = DocumentFilters.estimationFromPixels(pixels)

        assertFalse(estimation.documentImage)
    }

    @Test
    fun estimation_survivesAnEmptyPixelArray() {
        val estimation = DocumentFilters.estimationFromPixels(IntArray(0))

        assertEquals(0, estimation.blackPoint)
        assertEquals(255, estimation.whitePoint)
        assertFalse(estimation.documentImage)
    }

    // ------------------------------------------------------------------- lighten

    @Test
    fun linearStretch_mapsTheEndpointsExactly() {
        val lut = DocumentFilters.linearStretchLut(blackPoint = 40, whitePoint = 200)

        assertEquals("black point must map to 0", 0, lut[40])
        assertEquals("white point must map to 255", 255, lut[200])
        assertEquals("below black clips to 0", 0, lut[0])
        assertEquals("above white clips to 255", 255, lut[255])
        // Midpoint of 40..200 is 120 -> (80 / 160) * 255 = 127.5 -> 128.
        assertEquals(128, lut[120])
    }

    @Test
    fun linearStretch_followsTheNativeBranchOrder() {
        // Native: if (v <= black) 0; else if (v >= white) 255; else round(...).
        // The first branch must win when the two thresholds collide.
        val lut = DocumentFilters.linearStretchLut(blackPoint = 128, whitePoint = 128)

        assertEquals("v == black must take the first branch", 0, lut[128])
        assertEquals(255, lut[129])
        for (v in 1..255) {
            assertTrue("LUT went backwards at $v", lut[v] >= lut[v - 1])
        }
    }

    @Test
    fun linearStretch_invertedPointsBecomeAHardThresholdNotABlackPage() {
        // This is the case the old implementation "fixed" by skipping the filter:
        // under 2 % ink both cutoffs land on the paper spike, so whitePoint < blackPoint.
        // The native code sets range = 1.0 and lets the first branch win, which
        // thresholds at blackPoint instead of inverting into a black page.
        val lut = DocumentFilters.linearStretchLut(blackPoint = 232, whitePoint = 200)

        assertEquals("everything at or below black goes to 0", 0, lut[100])
        assertEquals(0, lut[232])
        assertEquals("above black becomes paper", 255, lut[240])
        assertTrue("the page must not be painted black", lut.count { it == 255 } > 0)
    }

    // -------------------------------------------------------------- ink curve
    // ------------------------------------------------------------------ grayscale

    @Test
    fun rec601_matchesOpenCvCvtColor() {
        // The exact definition Imgproc.cvtColor(BGR2GRAY) uses.
        assertEquals(255, DocumentFilters.rec601(255, 255, 255))
        assertEquals(0, DocumentFilters.rec601(0, 0, 0))
        assertEquals(76, DocumentFilters.rec601(255, 0, 0))
        assertEquals(150, DocumentFilters.rec601(0, 255, 0))
        assertEquals(29, DocumentFilters.rec601(0, 0, 255))
    }

    // ------------------------------------------------- native kernels (spec §6/§7)

    @Test
    fun median3x3_removesASingleHotPixelAndKeepsEdges() {
        // cv::medianBlur(…, 3) runs on the normalised plane before Otsu, so it has
        // to kill speckle without rounding off glyph edges.
        val w = 5
        val h = 5
        val plane = FloatArray(w * h) { 200f }
        plane[2 * w + 2] = 0f            // a lone dust speck on the paper

        val out = DocumentFilters.median3x3(plane, w, h)

        assertEquals("a 1 px speck must vanish", 200f, out[2 * w + 2], 0.01f)

        // A straight vertical edge at x = 2 must not move.
        val edge = FloatArray(w * h) { i -> if (i % w < 2) 0f else 255f }
        val edgeOut = DocumentFilters.median3x3(edge, w, h)
        assertEquals(0f, edgeOut[1 * w + 1], 0.01f)
        assertEquals(255f, edgeOut[1 * w + 3], 0.01f)
    }

    @Test
    fun gaussianBlur3_isANormalisedBinomialKernel() {
        val w = 7
        val h = 7

        // Flat field in, flat field out: the [1 2 1]/4 rows and columns sum to 1.
        val flat = DocumentFilters.gaussianBlur3(FloatArray(w * h) { 123f }, w, h)
        for (value in flat) assertEquals(123f, value, 0.01f)

        // A single hot pixel spreads to its 3x3 neighbourhood with weights
        // 1,2,1 / 2,4,2 / 1,2,1 over 16 — the classic binomial-3 response.
        val spike = FloatArray(w * h) { 0f }
        spike[3 * w + 3] = 1600f
        val out = DocumentFilters.gaussianBlur3(spike, w, h)
        assertEquals("4/16", 400f, out[3 * w + 3], 0.01f)
        assertEquals("2/16 orthogonal", 200f, out[3 * w + 2], 0.01f)
        assertEquals("1/16 diagonal", 100f, out[2 * w + 2], 0.01f)
        assertEquals("outside the 3x3", 0f, out[1 * w + 1], 0.01f)
    }

    @Test
    fun paperRadius_scalesWithThePageSoTheReachIsScaleFree() {
        // computeRadius(w, h, dpi, level) is the one unrecovered constant. Whatever
        // its formula, the reach must be a fraction of the page: a fixed radius
        // cannot close over a 60 px text line and a 6 px one at the same time.
        val small = DocumentFilters.paperRadius(200, 260)   // ~200 px thumbnail
        val medium = DocumentFilters.paperRadius(240, 320)  // 1200 px page
        val large = DocumentFilters.paperRadius(223, 289)   // 2000 px page

        assertTrue("radius must never be 0", small >= 1)
        assertTrue("small pages still need a window", small >= 2)
        assertTrue("large pages must close over body text", large >= 4)

        // Expressed as a fraction of the page, the three cases must agree, which
        // is precisely what the old fixed radius of 1 did not do.
        val smallFraction = small / 260f
        val mediumFraction = medium / 320f
        val largeFraction = large / 289f

        for (fraction in listOf(smallFraction, mediumFraction, largeFraction)) {
            assertTrue("reach $fraction is outside ~1-3 % of the page", fraction in 0.012f..0.030f)
        }
        assertTrue(
            "reach was not scale-free: ${largeFraction / smallFraction}",
            largeFraction / smallFraction < 1.5f
        )
    }

    // ------------------------------------------------- exposure-invariant classify

    @Test
    fun classify_callsAFlatLitPageADocument() {
        // A page: bright, neutral, flat.
        val page = IntArray(400) { rgb(232, 230, 226) }
        assertTrue(DocumentFilters.classifiesAsDocument(page))
    }

    @Test
    fun classify_isInvariantToExposure() {
        // wv3's thresholds are absolute (R >= 80 ...), so without normalisation a
        // page shot at half exposure would be misread as a photo. The whole point
        // of CLASSIFY_LUMA_TARGET is that these two answer the same.
        val full = IntArray(400) { rgb(232, 230, 226) }
        val dim = IntArray(400) { rgb(81, 80, 79) }   // same page, 35 % exposure

        assertTrue(DocumentFilters.classifiesAsDocument(full))
        assertTrue("a dim page must still be a page", DocumentFilters.classifiesAsDocument(dim))
    }

    @Test
    fun classify_doesNotCallADarkRoomADocument() {
        // The frame this was written for: a photo of a lit laptop screen in a dark
        // room. Most of it is near-black, the rest is a small bright rectangle.
        // Neutral-and-bright flatters this frame, so it is the tight case.
        val pixels = IntArray(1600) { index ->
            val x = index % 40
            val y = index / 40
            if (x in 8..30 && y in 12..27) rgb(228, 226, 220) else rgb(18, 18, 20)
        }
        assertFalse(
            "a dark scene must not be treated as a page",
            DocumentFilters.classifiesAsDocument(pixels)
        )
    }

    @Test
    fun classify_doesNotCallALandscapeADocument() {
        // The real scene risk: sky (which wv3's photo rule accepts) plus dark
        // ground (which both rules reject). A landscape must not have its shadows
        // divided out, or the ground turns white.
        val pixels = IntArray(1600) { index ->
            if (index < 800) rgb(150, 190, 235) else rgb(52, 84, 40)
        }
        assertFalse(
            "a landscape must not be treated as a page",
            DocumentFilters.classifiesAsDocument(pixels)
        )
    }

    @Test
    fun classify_survivesAnEmptyArray() {
        assertFalse(DocumentFilters.classifiesAsDocument(IntArray(0)))
    }

    // ------------------------------------------------- saturation instead of a clamp

    @Test
    fun softKnee_isTheIdentityBelowTheKnee() {
        // Everything up to the knee must be applied exactly, or a page's shadows
        // stop dividing out.
        val knee = 8f
        val cap = 24f
        for (gain in listOf(1f, 1.2f, 3.7f, 7.99f, knee)) {
            assertEquals("gain $gain", gain, DocumentFilters.softKnee(gain, knee, cap), 1e-4f)
        }
    }

    @Test
    fun softKnee_isContinuousAtTheKnee() {
        // The previous design used a hard max(floor, surface), which put a visible
        // contour where the two behaviours met. Across the knee the curve must
        // keep advancing at unit slope — a jump would be a step, and a flat spot
        // would be a kink.
        val knee = 8f
        val cap = 24f
        val epsilon = 1e-2f
        val rise = DocumentFilters.softKnee(knee + epsilon, knee, cap) -
            DocumentFilters.softKnee(knee - epsilon, knee, cap)
        assertEquals("no step at the knee", 2f * epsilon, rise, 1e-3f)
        assertEquals(knee, DocumentFilters.softKnee(knee, knee, cap), 1e-4f)
    }

    @Test
    fun softKnee_slopeIsOneAtTheKnee() {
        // Unit slope means no visible seam: the lift continues at the same rate
        // across the knee before easing off.
        val knee = 8f
        val cap = 24f
        val delta = 1e-2f
        val slope = (DocumentFilters.softKnee(knee + delta, knee, cap) -
            DocumentFilters.softKnee(knee, knee, cap)) / delta
        assertEquals(1f, slope, 0.02f)
    }

    @Test
    fun softKnee_neverExceedsTheCapAndAlwaysIncreases() {
        val knee = 1.2f
        val cap = 2f
        var previous = 0f
        var gain = 0.1f
        while (gain < 200f) {
            val eased = DocumentFilters.softKnee(gain, knee, cap)
            assertTrue("monotonic at $gain", eased >= previous - 1e-5f)
            assertTrue("capped at $gain: $eased", eased <= cap + 1e-4f)
            previous = eased
            gain *= 1.3f
        }
        // ...and it does get close to the cap, rather than stopping short of it.
        assertTrue(DocumentFilters.softKnee(500f, knee, cap) > cap * 0.95f)
    }

    @Test
    fun saturateGain_saturatesADarkSurfaceInsteadOfBlowingItOut() {
        // A surface at 15/255 needs a gain of 17. Below a document knee of 8 that
        // is eased, not applied: the pixel is lifted but not to white.
        val docKnee = 8f
        val docCap = 24f
        val lifted = DocumentFilters.saturateGain(15f, 15f, docKnee, docCap)
        assertTrue("must be brighter than the source", lifted > 100)
        assertFalse("must not be forced to white", lifted == 255)

        // A bright surface needs almost no gain and is passed through.
        assertEquals(250, DocumentFilters.saturateGain(250f, 255f, docKnee, docCap))

        // The scene limits are far tighter: the same pixel barely moves.
        val scene = DocumentFilters.saturateGain(15f, 15f, 1.2f, 2f)
        assertTrue("scene gain must stay near 2x, got $scene", scene in 25..32)
    }

    @Test
    fun saturateGain_survivesAZeroSurface() {
        // A black pixel whose surface collapsed to zero must not divide by zero.
        val value = DocumentFilters.saturateGain(0f, 0f, 8f, 24f)
        assertEquals(0, value)
    }

    // ------------------------------------------------------------ B&W local pass

    @Test
    fun boxMeanAndStd_matchesADirectComputation() {
        // Integral-image statistics have to agree with the naive loops, or the
        // local threshold drifts by a constant of the image.
        val w = 9
        val h = 7
        val plane = FloatArray(w * h) { index -> ((index * 37) % 251).toFloat() }
        val window = 5
        val (mean, std) = DocumentFilters.boxMeanAndStd(plane, w, h, window)!!

        val half = window / 2
        for (y in 0 until h) {
            for (x in 0 until w) {
                val x0 = (x - half).coerceAtLeast(0)
                val x1 = (x + half + 1).coerceAtMost(w)
                val y0 = (y - half).coerceAtLeast(0)
                val y1 = (y + half + 1).coerceAtMost(h)
                var sum = 0.0
                var sumSq = 0.0
                var count = 0
                for (yy in y0 until y1) {
                    for (xx in x0 until x1) {
                        val v = plane[yy * w + xx].toDouble()
                        sum += v
                        sumSq += v * v
                        count++
                    }
                }
                val m = sum / count
                val expectedStd = kotlin.math.sqrt((sumSq / count) - m * m)
                assertEquals("mean at $x,$y", m.toFloat(), mean[y * w + x], 1e-3f)
                assertEquals("std at $x,$y", expectedStd.toFloat(), std[y * w + x], 1e-3f)
            }
        }
    }

    @Test
    fun boxMeanAndStd_reportsZeroDeviationOnAFlatField() {
        // A blank margin has no text/paper decision in it, which is what the
        // SAUVOLA_MIN_STD gate keys on.
        val plane = FloatArray(64) { 200f }
        val (mean, std) = DocumentFilters.boxMeanAndStd(plane, 8, 8, 5)!!
        for (value in mean) assertEquals(200f, value, 1e-3f)
        for (value in std) assertEquals(0f, value, 1e-3f)
    }

    @Test
    fun sauvolaWindow_scalesWithThePageAndSkipsTinyOnes() {
        // A local window narrower than a glyph is worse than no local pass, so
        // tiny inputs take the global threshold instead.
        assertEquals("tiny page", 1, DocumentFilters.sauvolaWindow(100, 120))

        // A live preview is smaller than the bake, so the window must shrink with
        // it rather than staying at a page-sized constant.
        val preview = DocumentFilters.sauvolaWindow(900, 1200)
        val bake = DocumentFilters.sauvolaWindow(1500, 2000)
        assertTrue("preview window must be odd, got $preview", preview % 2 == 1)
        assertTrue("bake window must be odd, got $bake", bake % 2 == 1)
        assertTrue("window must scale with the page: $preview vs $bake", bake > preview)
        assertTrue("bake window out of range: $bake", bake in 90..130)
    }

    // ---------------------------------------------------------------------- otsu

    @Test
    fun otsu_splitsABimodalHistogram() {
        val hist = IntArray(256)
        for (v in 20..60) hist[v] = 20        // dark ink
        for (v in 200..240) hist[v] = 20      // paper

        val threshold = DocumentFilters.otsuThreshold(hist)

        assertTrue("threshold $threshold should sit between ink and paper", threshold in 60..200)
    }

    @Test
    fun otsu_fallsBackOnBlankHistogram() {
        assertEquals(128, DocumentFilters.otsuThreshold(IntArray(256)))
    }

    @Test
    fun otsu_doesNotCollapseToAPeak() {
        // A page that is 95 % paper with a little text must still threshold near
        // the text, not at the paper level (which would output solid black).
        val hist = IntArray(256)
        for (v in 30..70) hist[v] = 2
        for (v in 210..250) hist[v] = 40

        val threshold = DocumentFilters.otsuThreshold(hist)
        assertTrue("threshold $threshold must be below paper level", threshold < 210)
    }

    // ------------------------------------------------------- B&W stroke sharpening

    /**
     * A flat field has no high-frequency content, so the gate must pass it through
     * untouched — this is the property that stops sharpening from amplifying grain.
     */
    /**
     * A strong edge must actually be steepened. A step from 90 to 180 is an 45-level
     * detail term, far above the 3-level floor, so the boost applies at full
     * strength on the pixels either side of the step.
     */
    /** Detail just under the floor is ignored; detail well over it is boosted. */
    /**
     * Checkerboard noise has a residual equal to its amplitude, so the median
     * absolute residual should come out at that amplitude — not at the mean, and
     * not scaled by the blur.
     */
    @Test
    fun adaptiveSharpenAmount_isFullAtLowNoiseAndZeroAtHighNoise() {
        val base = 0.6f

        assertEquals("clean page must get full strength", base, DocumentFilters.adaptiveSharpenAmount(base, 1f), 1e-4f)
        assertEquals("shipping threshold must get full strength", base, DocumentFilters.adaptiveSharpenAmount(base, 4f), 1e-4f)
        assertEquals("grainy page must get nothing", 0f, DocumentFilters.adaptiveSharpenAmount(base, 9f), 1e-4f)
        assertEquals("beyond the fade must stay at nothing", 0f, DocumentFilters.adaptiveSharpenAmount(base, 14f), 1e-4f)
        // Monotonic in between: more noise, less sharpening.
        val samples = (0..20).map { DocumentFilters.adaptiveSharpenAmount(base, it * 0.5f) }
        assertEquals(samples, samples.sortedDescending())
        // Halfway through the fade is half strength.
        assertEquals(base / 2f, DocumentFilters.adaptiveSharpenAmount(base, 6.5f), 1e-4f)
    }

    // ------------------------------------------------- page size, bands, coverage AA

    /**
     * The PDF is a raster page, so its sharpness at zoom is its DPI. A4 with the
     * standard 25 pt margin leaves a 770.8 pt (10.7 in) image box, so 300 DPI needs
     * `10.7 × 300 ≈ 3212` px. Anything much below that is the "pixelated when I
     * zoom into the PDF" report.
     */
    @Test
    fun processDimension_reachesThreeHundredDpiOnA4() {
        val neededFor300Dpi = (770.8f / 72f * 300f).roundToInt()   // 3212
        assertTrue(
            "MAX_PROCESS_DIMENSION=${DocumentFilters.MAX_PROCESS_DIMENSION} is under " +
                "the $neededFor300Dpi px A4 needs for 300 DPI",
            DocumentFilters.MAX_PROCESS_DIMENSION >= neededFor300Dpi - 20
        )
    }

    /**
     * A band split that skipped or double-processed rows would leave them black —
     * silently, and only on multi-core devices.
     */
    @Test
    fun bands_visitsEveryRowExactlyOnce() {
        val h = 1000
        val counts = IntArray(h)
        DocumentFilters.bands(h) { from, to ->
            for (y in from until to) counts[y]++
        }
        for (y in 0 until h) {
            assertEquals("row $y must be visited once", 1, counts[y])
        }
    }

    @Test
    fun bands_handlesAnImageShorterThanOneBand() {
        val visits = ArrayList<Int>()
        DocumentFilters.bands(5) { from, to -> synchronized(visits) { visits.add(to - from) } }
        assertEquals("a 5-row image must still be covered", 5, visits.sum())
    }

    @Test
    fun bilinearAt_interpolatesAndClamps() {
        val w = 4
        val h = 2
        val plane = floatArrayOf(
            0f, 100f, 0f, 70f,
            0f, 0f, 0f, 0f
        )

        assertEquals(0f, DocumentFilters.bilinearAt(plane, w, h, 0f, 0f), 1e-4f)
        assertEquals(100f, DocumentFilters.bilinearAt(plane, w, h, 1f, 0f), 1e-4f)
        assertEquals("midpoint", 50f, DocumentFilters.bilinearAt(plane, w, h, 0.5f, 0f), 1e-4f)
        // Outside the plane must clamp to the edge pixel, not wrap.
        assertEquals(70f, DocumentFilters.bilinearAt(plane, w, h, 9f, 0f), 1e-4f)
        assertEquals(0f, DocumentFilters.bilinearAt(plane, w, h, -9f, 0f), 1e-4f)
    }

    @Test
    fun medianOf9InPlace_returnsTheMiddleValue() {
        val values = floatArrayOf(9f, 1f, 8f, 2f, 7f, 3f, 6f, 4f, 5f)
        assertEquals(5f, DocumentFilters.medianOf9InPlace(values), 1e-4f)
        assertEquals("must not allocate a copy", 5f, values[4], 1e-4f)
    }

    @Test
    fun boxBlurInPlace_leavesAFlatPlaneUntouched() {
        val w = 12
        val h = 9
        val plane = FloatArray(w * h) { 173f }

        DocumentFilters.boxBlurInPlace(plane, w, h)

        for (i in plane.indices) assertEquals("index $i", 173f, plane[i], 1e-4f)
    }

    /**
     * A single bright pixel must be spread to its 3 × 3 neighbourhood with weight
     * 1/9 and nothing may be lost — this is the property that makes the in-place
     * version a drop-in for the allocating one.
     */
    @Test
    fun boxBlurInPlace_spreadsAnImpulseWithoutLosingEnergy() {
        val w = 7
        val h = 7
        val plane = FloatArray(w * h)
        plane[3 * w + 3] = 90f

        DocumentFilters.boxBlurInPlace(plane, w, h)

        assertEquals("centre", 10f, plane[3 * w + 3], 1e-4f)
        assertEquals("left", 10f, plane[3 * w + 2], 1e-4f)
        assertEquals("diagonal", 10f, plane[2 * w + 2], 1e-4f)
        assertEquals("far corner untouched", 0f, plane[0], 1e-4f)
        assertEquals("total energy", 90f, plane.sum(), 1e-3f)
    }

    @Test
    fun noiseFromResidual_readsTheResidualAmplitude() {
        val w = 20
        val h = 20
        val plane = FloatArray(w * h) { i -> if (i % 2 == 0) 106f else 94f }
        val flat = FloatArray(w * h) { 100f }

        assertEquals(0f, DocumentFilters.noiseFromResidual(flat, flat), 1e-4f)
        assertEquals(6f, DocumentFilters.noiseFromResidual(plane, flat), 1e-4f)
    }

    /**
     * The whole point of the coverage cut: interiors stay perfectly bi-level while
     * the contour gets grey, and that grey must be *only* on the contour.
     */
    // -------------------------------------- transcribed from libimageproc.so (§13)

    /**
     * §13.3: `R = max(14, sqrt(rows·cols/8000))`. The floor is what keeps a thumbnail
     * from producing a one-cell grid; the square-root law is what makes the cell size
     * resolution-independent.
     */
    @Test
    fun cellRadius_followsThePixelsPerCellLawAndFloorsAtFourteen() {
        assertEquals(14, DocumentFilters.cellRadius(64, 64))
        // 3200 × 2400 = 7.68 M px → sqrt(960) = 30.98 → 30
        assertEquals(30, DocumentFilters.cellRadius(3200, 2400))
        // Four times the area → twice the radius: the rule scales with the count.
        // sqrt(6400·4800/8000) = sqrt(3840) = 61.97 → 61.
        assertEquals(61, DocumentFilters.cellRadius(6400, 4800))
    }

    /** §13.3: the curves are monotonic, hit 0 and 255, and A lies above B in between. */
    @Test
    fun toneCurves_matchTheRaisedCosineDefinition() {
        val a = DocumentFilters.toneCurve(0.625)
        val b = DocumentFilters.toneCurve(0.8)

        assertEquals(256, a.size)
        assertEquals("curve must start at 0", 0, a[0])
        assertEquals("curve must end at 255", 255, a[255])
        for (i in 1 until 256) {
            assertTrue("curve A must be monotonic at $i", a[i] >= a[i - 1])
            assertTrue("curve B must be monotonic at $i", b[i] >= b[i - 1])
        }
        // A (0.625) climbs faster than B (0.8), which is what lets level 1 interpolate
        // between them by edge strength.
        assertTrue("A must sit above B at midtone", a[64] > b[64])
        // Spot values from the definition, checked against the same double maths.
        assertEquals(96, a[64])
        assertEquals(63, b[64])
    }

    @Test
    fun responseTable_endsAtItsSentinel() {
        assertEquals(0, DocumentFilters.responseValue(0))
        assertEquals(2000, DocumentFilters.responseValue(2000))
        assertEquals("past the end must stay at the sentinel", 2000, DocumentFilters.responseValue(5000))
        assertTrue("must be monotonic", DocumentFilters.responseValue(1000) > DocumentFilters.responseValue(500))
    }

    /**
     * §13.4: the divide lookup must send paper to white, ink to dark, and never invert
     * the order. The opposite row/column assignment saturates everything to 255 — this
     * test is what pins the orientation.
     */
    @Test
    fun levelTwoChannel_makesItAClearBlackAndWhiteScan() {
        val table = DocumentFilters.toneTables()
        // Printed ink on typical paper must reach pure black, not mid-grey: this is
        // the failure the old compose order produced (ink 45 came out at ~98).
        assertEquals("ink must be black", 0, DocumentFilters.levelTwoChannel(table, 238, 45))
        assertEquals("paper must be white", 255, DocumentFilters.levelTwoChannel(table, 238, 238))
        // Paper noise of a few counts must not turn into speckle.
        assertTrue(
            "paper ripple must stay white, got ${DocumentFilters.levelTwoChannel(table, 238, 230)}",
            DocumentFilters.levelTwoChannel(table, 238, 230) >= 240
        )
        // Light grey sits on the ramp rather than snapping, so anti-aliased glyph
        // edges survive as edges.
        val grey = DocumentFilters.levelTwoChannel(table, 238, 200)
        assertTrue("the ramp must be gradual here, got $grey", grey in 150..210)
    }

    @Test
    fun divideLookup_turnsPaperWhiteAndInkBlack() {
        // Anchors taken from the transcribed table builder ($13.4): the row is
        // |src - paper|, and the stored byte is complemented.
        assertEquals("paper on paper must be white", 255, DocumentFilters.divideLookup(0, 238))
        assertEquals("a pixel far below its paper must clip to black", 0, DocumentFilters.divideLookup(238, 0))
        assertEquals("deep ink is black too", 0, DocumentFilters.divideLookup(193, 45))
        // The ramp lives close to the paper level; 8 counts of JPEG ripple must not
        // become grey speckle.
        assertTrue("paper noise must stay white", DocumentFilters.divideLookup(8, 230) >= 240)

        // Monotonic in the pixel value for a fixed row.
        var previous = -1
        for (v in 0..255) {
            val out = DocumentFilters.divideLookup(120, v)
            assertTrue("must not invert at $v", out >= previous)
            previous = out
        }
        // Monotonic the other way in the row: a larger difference is never lighter.
        previous = 256
        for (row in 0..255) {
            val out = DocumentFilters.divideLookup(row, 200)
            assertTrue("a bigger row must not lighten at $row", out <= previous)
            previous = out
        }
    }

    @Test
    fun divideLookup_isComplementedNotInverted() {
        // Reading the stored byte as `q` rather than `255 - q` is the mistake that
        // makes paper black; this pins the direction so it cannot regress.
        assertTrue(DocumentFilters.divideLookup(0, 255) == 255)
        assertTrue(DocumentFilters.divideLookup(200, 240) < 128)
        assertTrue(DocumentFilters.divideLookup(0, 1) == 255)
    }

    /**
     * §13.6: `Laplacian(CV_8U)` — negative responses saturate to 0, which is what makes
     * the result usable as an edge weight `alpha = min(1, lap/50)`.
     */
    @Test
    fun laplacian3x3_clampsNegativesAndRespondsToEdges() {
        val w = 16
        val h = 4
        val flat = FloatArray(w * h) { 100f }
        for (v in DocumentFilters.laplacian3x3(flat, w, h)) {
            assertEquals("a flat plane must have no response", 0f, v, 1e-4f)
        }

        val step = FloatArray(w * h) { i -> if ((i % w) < w / 2) 40f else 200f }
        val lap = DocumentFilters.laplacian3x3(step, w, h)
        // [0 1 0; 1 -4 1; 0 1 0]: a pixel darker than its neighbourhood responds
        // POSITIVELY, so the high response sits on the ink side of an edge —
        // 3·40 + 200 − 4·40 = 160.
        assertEquals(160f, lap[2 * w + w / 2 - 1], 1e-4f)
        // The bright side is negative and saturates to 0 rather than wrapping.
        assertEquals(0f, lap[2 * w + w / 2], 1e-4f)
        // Interiors are flat.
        assertEquals(0f, lap[2 * w + 1], 1e-4f)
        assertEquals(0f, lap[2 * w + w - 2], 1e-4f)
    }

    /** `qx3.smali`: the Grayscale preset is desaturate **+ medianBlur(9)**. */
    @Test
    fun medianBlurGray_removesSaltAndPepperAndKeepsEdges() {
        val w = 21
        val h = 21
        val pixels = IntArray(w * h) { grey(120) }
        pixels[10 * w + 10] = grey(250)          // a single hot pixel
        pixels[5 * w + 5] = grey(0)              // and a single dead one

        DocumentFilters.medianBlurGray(pixels, w, h, 9)

        assertEquals("hot pixel must be gone", 120, DocumentFilters.greyOf(pixels[10 * w + 10]))
        assertEquals("dead pixel must be gone", 120, DocumentFilters.greyOf(pixels[5 * w + 5]))
    }

    // ------------------------------------------------- the adaptive engine (R61)

    /**
     * One adaptive pass over a single pixel on a flat paper surface.
     *
     * The paper grid is one cell, so the bilinear sample degenerates to that cell's
     * value — the same arithmetic as a page, without building one.
     */
    private fun adaptivePixel(paper: Int, pixel: Int, lightText: Boolean = false): Int {
        val (curve, params) = if (lightText) {
            DocumentFilters.ratioCurve(0.78f, 0.99f, 1.5f) to intArrayOf(2048, 6144, 64, 307, 0, 50)
        } else {
            DocumentFilters.ratioCurve(0.60f, 0.98f, 1.6f) to intArrayOf(2048, 6144, 56, 287, 0, 50)
        }
        val pixels = intArrayOf(pixel)
        val grid = intArrayOf(paper * 256)
        DocumentFilters.adaptiveColourReference(
            pixels, 1, grid, grid, grid, 1, 1, 1, curve, params, FloatArray(0)
        )
        return pixels[0]
    }

    /**
     * The headline claim of the adaptive filter, pinned to a number.
     *
     * A mark at 92 % of paper reflectance — a light pencil line, a thermal receipt
     * header — is what Magic Color cannot see: its divide table is indexed by the
     * *absolute* difference from paper, so 219 on 238 paper lands at 248, one step
     * off white. The ratio-domain curve reaches it.
     */
    @Test
    fun adaptive_seesFaintInkThatMagicColorCannot() {
        val mark = adaptivePixel(paper = 238, pixel = grey(219))
        val out = DocumentFilters.greyOf(mark)
        assertTrue("faint ink must darken visibly, got $out", out <= 200)
        assertTrue("faint ink must stay distinguishable from paper", out < 240)
    }

    @Test
    fun adaptive_rendersPaperPureWhite() {
        // Anything at or above the curve's white point is paper, exactly 255.
        assertEquals(255, DocumentFilters.greyOf(adaptivePixel(238, grey(238))))
        assertEquals(255, DocumentFilters.greyOf(adaptivePixel(238, grey(247))))
        assertEquals(255, DocumentFilters.greyOf(adaptivePixel(150, grey(150))))
    }

    /**
     * Two captures of the same ink under different light must land on the same
     * byte, which is the whole point of dividing by the paper surface. The papers
     * are 238 / 150 / 90 — not the exact-ratio pair the Rust kernel test uses, so
     * one level of slack absorbs the rounding of the Q8 gain itself.
     */
    @Test
    fun adaptive_isInvariantToPaperLevel() {
        val out = listOf(238, 150, 90).map { paper ->
            DocumentFilters.greyOf(adaptivePixel(paper, grey((paper * 0.92f).roundToInt())))
        }
        val spread = out.maxOrNull()!! - out.minOrNull()!!
        assertTrue("same ink, three lights, one byte: $out", spread <= 6)
    }

    /** One gain per pixel: a blue pen must stay blue-dominant, not grey. */
    @Test
    fun adaptive_keepsHue() {
        val out = adaptivePixel(238, rgb(60, 80, 160))
        val r = (out ushr 16) and 0xFF
        val g = (out ushr 8) and 0xFF
        val b = out and 0xFF
        assertTrue("blue must stay blue: r=$r g=$g b=$b", b > r + 20 && b > g + 15)
    }

    /** The curve's own endpoints, so a retune cannot silently invert it. */
    @Test
    fun ratioCurve_isZeroBelowLoAndFullAtHi() {
        val curve = DocumentFilters.ratioCurve(0.60f, 0.98f, 1.6f)
        assertEquals("everything below the black ratio is ink", 0, curve[0])
        assertEquals(0, curve[(0.55f * 255).roundToInt()])
        assertEquals("at the white ratio it is paper", 255, curve[255])
        assertEquals(255, curve[(0.98f * 255).roundToInt()])
        // Monotone: a retune that is not monotone would posterise the page.
        for (i in 1 until curve.size) {
            assertTrue("curve must be monotone at $i", curve[i] >= curve[i - 1])
        }
    }

    // -------------------------------- the surface estimator and the gate (R62)

    /**
     * The box blur this replaced smeared a hard shadow edge across the grid, which is
     * the grey band alongside every crisp shadow. A median removes the same isolated
     * bad cells and reproduces a step.
     */
    @Test
    fun median3Grid_removesASpeckAndKeepsAStep() {
        val gw = 9
        val gh = 5
        val flat = FloatArray(gw * gh) { 120f }
        flat[2 * gw + 4] = 250f
        val cleaned = DocumentFilters.median3Grid(flat, gw, gh)
        assertEquals("an isolated hot cell must be gone", 120f, cleaned[2 * gw + 4], 0f)
        assertEquals("and nothing else may move", 120f, cleaned[0], 0f)

        // A vertical step at x = 4: a paper level of 200 against 100.
        val step = FloatArray(gw * gh) { i -> if ((i % gw) < 4) 200f else 100f }
        var grid = step
        repeat(2) { grid = DocumentFilters.median3Grid(grid, gw, gh) }
        for (y in 0 until gh) {
            assertEquals("lit side must survive the step", 200f, grid[y * gw + 3], 0f)
            assertEquals("shadow side must survive the step", 100f, grid[y * gw + 4], 0f)
        }
    }

    /**
     * The gate that decides whether shadows are removed at all.
     *
     * A page in a hard shadow is still a page: its paper is the brightest thing in it.
     * The absolute-threshold classifier called this frame a scene (41 %) and then no
     * shadow was removed — the bug this test exists to prevent from coming back.
     */
    @Test
    fun surfaceGate_acceptsAPageThatIsHalfInShadow() {
        val w = 256
        val h = 64
        val cell = 4
        val gw = w / cell
        val gh = h / cell
        // Left half lit paper, right half the same paper at half the light.
        val pixels = IntArray(w * h) { i ->
            if ((i % w) < (w / 2)) grey(200) else grey(100)
        }
        val gridR = IntArray(gw * gh) { i -> if ((i % gw) < (gw / 2)) 200 * 256 else 100 * 256 }
        val gridG = gridR.copyOf()
        val gridB = gridR.copyOf()

        assertTrue(
            "a shadowed page is a page",
            DocumentFilters.surfaceIsPaper(pixels, w, gridR, gridG, gridB, gw, gh, cell)
        )

        // Same frame with ink: still a page, and now by both halves of the rule.
        for (i in pixels.indices) {
            if ((i / w) % 8 in 3..4 && (i % w) % 8 in 2..6 && (i % w) < (w - 4)) {
                pixels[i] = grey(if ((i % w) < (w / 2)) 40 else 20)
            }
        }
        assertTrue(
            "ink must not change the verdict",
            DocumentFilters.surfaceIsPaper(pixels, w, gridR, gridG, gridB, gw, gh, cell)
        )
    }

    /** A scene: content that is brighter than its own neighbourhood, and no ink. */
    @Test
    fun surfaceGate_rejectsAFrameWithContentBrighterThanItsSurface() {
        val w = 64
        val h = 64
        val gw = 4
        val gh = 4
        val cell = 16
        // Every surface cell says 120; a 16x16 block of the frame is 240 — a window
        // against a wall. That is 6.25 % of the frame, above the 3 % limit.
        val pixels = IntArray(w * h) { i ->
            val x = i % w
            val y = i / w
            grey(if (x < 16 && y < 16) 240 else 120)
        }
        val flatGrid = IntArray(gw * gh) { 120 * 256 }
        assertFalse(
            "a bright region above its own surface is not paper",
            DocumentFilters.surfaceIsPaper(pixels, w, flatGrid, flatGrid, flatGrid, gw, gh, cell)
        )
    }

    /** An empty frame must not be able to break the gate. */
    @Test
    fun surfaceGate_defaultsToDocumentOnAnEmptyFrame() {
        val empty = IntArray(0)
        assertTrue(
            DocumentFilters.surfaceIsPaper(empty, 0, empty, empty, empty, 0, 0, 1)
        )
    }

    // ------------------------------------ the background and the finish (R63)

    /**
     * The trimmed pool is what lets the fine surface reject ink without quantising.
     *
     * A cell that is 30 % ink has a mean of 200 * 0.7 + 20 * 0.3 = 146 — a 27 % error
     * in the paper estimate, which is 27 % of shadow the divide cannot remove. Dropping
     * the darkest third (the ink) gives paper back exactly.
     */
    @Test
    fun trimmedPool_rejectsInkAndKeepsThePaperLevel() {
        val paper = 200
        val ink = 20
        // 100 samples: 70 paper, 30 ink, shuffled deterministically.
        val values = IntArray(100) { i -> if (i % 10 < 7) paper else ink }
        val w = 10
        val h = 10
        val pixels = IntArray(100) { i -> grey(values[i]) }
        val plane = DocumentFilters.trimmedPool(pixels, w, h, 10, 1, 1, dropPercent = 35)

        assertEquals("paper must survive the trim exactly", paper.toFloat(), plane[1][0], 0.01f)

        val mean = values.average()
        assertTrue("a plain mean would be dragged down to $mean", mean < 160)
    }

    /**
     * Paper noise is symmetric, so the trim must not bias the level.
     *
     * A 35 % trim on a 64-pixel cell drops all of the 127s and nothing else, so the
     * estimate is 128.5 rather than 128 — half a level, which is inside the noise the
     * estimator is measuring and far below the ~10 % a shadow correction moves.
     */
    @Test
    fun trimmedPool_isUnbiasedOnFlatPaper() {
        val pixels = IntArray(64) { i -> grey(128 + (i % 3) - 1) }   // 127..129
        val plane = DocumentFilters.trimmedPool(pixels, 8, 8, 8, 1, 1)
        assertEquals(128f, plane[0][0], 2f)
    }

    /**
     * The fine surface has to see a step that is *narrower than a coarse cell*.
     *
     * That is the whole reason it exists: the coarse grid is one number per cell, so a
     * shadow edge inside a cell is invisible to it, and the divide leaves a grey seam.
     */
    @Test
    fun fineMap_seesAShadowBandNarrowerThanACoarseCell() {
        val w = 80
        val h = 64
        // 32 px wide: wider than twice the fine closing radius, so the closing cannot
        // fill it, but still inside a single 8 px *coarse* cell's blind spot (the
        // coarse grid here is one cell, i.e. maximally blind).
        val bandFrom = 24
        val bandTo = 56
        val lit = 240
        val band = 200
        val pixels = IntArray(w * h) { i ->
            val x = i % w
            grey(if (x in bandFrom until bandTo) band else lit)
        }
        val flat = FloatArray(1) { lit.toFloat() }
        val coarse = DocumentFilters.PaperMap(flat, flat, flat, 1, 1, block = 8f)

        val fine = DocumentFilters.buildFineMap(pixels, w, h, coarse)
        assertEquals(
            "the fine cell must be a fifth of the coarse one",
            2f,
            fine.cell.toFloat(),
            0.01f
        )
        val inside = DocumentFilters.sampleFine(fine, 1, (bandFrom + bandTo) / 2, h / 2)
        val outside = DocumentFilters.sampleFine(fine, 1, 4, h / 2)
        assertTrue("the band must read as darker paper, got $inside", inside < outside - 20f)
    }

    /**
     * The correction lifts a background the coarse surface over-estimated, and leaves
     * everything else — including ink — alone.
     */
    @Test
    fun flatten_brightensAnOverEstimatedBackgroundAndNeverTouchesInk() {
        val w = 40
        val h = 20
        val dim = 200
        val lit = 240
        val ink = 30
        // Left half is paper in shadow, right half is paper in the light; one pixel of
        // ink sits in the shadowed half.
        val pixels = IntArray(w * h) { i ->
            val x = i % w
            grey(if (x < w / 2) dim else lit)
        }
        val inkIndex = 5 * w + 5
        pixels[inkIndex] = grey(ink)

        // The coarse grid believes the whole frame is at the lit level: exactly the
        // failure being corrected.
        val flatCoarse = FloatArray(1) { lit.toFloat() }
        val coarse = DocumentFilters.PaperMap(flatCoarse, flatCoarse, flatCoarse, 1, 1, block = 8f)
        // The fine grid knows the truth.
        val fineR = FloatArray(w) { x -> if (x < w / 2) dim.toFloat() else lit.toFloat() }
        val finePlanes = arrayOf(fineR, fineR, fineR)
        val fine = DocumentFilters.FineMap(finePlanes, w, 1, 1)

        DocumentFilters.flattenIllumination(pixels, w, h, coarse, fine)

        val shadow = DocumentFilters.greyOf(pixels[5 * w + 2])
        val bright = DocumentFilters.greyOf(pixels[5 * w + w - 3])
        assertTrue("shadowed paper must be lifted, got $shadow", shadow > dim + 10)
        // The multiplier is the reciprocal of a 0.85 floor, i.e. at most 1/0.85.
        assertTrue("the correction is bounded, got $shadow", shadow <= (dim * 1.18f) + 1)
        assertEquals("lit paper must not move", lit, bright)
        assertEquals(
            "a pixel a long way below the surface is ink and must be untouched",
            ink,
            DocumentFilters.greyOf(pixels[inkIndex])
        )
    }

    /** The clamp: near-white becomes exactly white, and nothing else moves. */
    @Test
    fun finish_clampsPaperToWhiteAndLeavesMidtones() {
        val w = 9
        val h = 9
        val pixels = IntArray(w * h) { grey(150) }
        pixels[4 * w + 4] = grey(244)
        DocumentFilters.finishAdaptive(pixels, w, h, clampPercent = 90, sharpenQ8 = 0)
        assertEquals("244 is paper at a 90 % clamp", 255, DocumentFilters.greyOf(pixels[4 * w + 4]))
        assertEquals("a midtone must not be clamped", 150, DocumentFilters.greyOf(pixels[0]))
    }

    /**
     * The sharpening only darkens, and only on the ink side of an edge.
     *
     * Sharpening the brightness plane would put a bright halo on the paper side of
     * every stroke, which is the ringing this design exists to avoid: the paper is
     * already at the clamp and a brighter pixel would clip.
     */
    @Test
    fun finish_sharpensInwardOnly() {
        val w = 15
        val h = 15
        val ink = 60
        val paper = 200
        val pixels = IntArray(w * h) { i -> grey(if ((i % w) < 7) ink else paper) }
        DocumentFilters.finishAdaptive(pixels, w, h, clampPercent = 0, sharpenQ8 = 230)

        val inkSide = DocumentFilters.greyOf(pixels[7 * w + 6])
        val paperSide = DocumentFilters.greyOf(pixels[7 * w + 7])
        assertTrue("the ink side of the edge darkens, got $inkSide", inkSide < ink)
        assertEquals("the paper side must not brighten or darken", paper, paperSide)
        assertTrue(
            "the darkening is capped, got $inkSide",
            inkSide >= ink - 41
        )
    }

    /** A flat plane has no Laplacian, so the finish must not be able to change it. */
    @Test
    fun finish_isIdentityOnAFlatPlaneApartFromTheClamp() {
        val w = 11
        val h = 11
        val pixels = IntArray(w * h) { grey(170) }
        val before = pixels.copyOf()
        DocumentFilters.finishAdaptive(pixels, w, h, clampPercent = 90, sharpenQ8 = 230)
        assertTrue(before.contentEquals(pixels))
    }

    /**
     * Light Text's new curve: gentle in the toe, white well before paper.
     *
     * The old one started at 0.78 of paper, so a background the surface left at 0.92
     * came out at 141 — the "Light Text greys the whole page" report.
     */
    @Test
    fun lightTextCurve_keepsFaintMarksAndReachesWhiteBeforePaper() {
        val curve = DocumentFilters.ratioCurve(0.70f, 0.94f, 1.0f)
        assertEquals("at the toe a pixel is ink", 0, curve[0])
        assertEquals("at the shoulder it is paper", 255, curve[240])
        assertEquals("85 % of paper stays visible", 159f, curve[(0.85f * 255).toInt()].toFloat(), 3f)
        assertEquals("80 % of paper is clearly a mark", 106f, curve[(0.80f * 255).toInt()].toFloat(), 3f)
        for (i in 1..255) {
            assertTrue("the curve must never invert at $i", curve[i] >= curve[i - 1])
        }
    }

    private fun grey(v: Int): Int = (0xFF shl 24) or (v shl 16) or (v shl 8) or v

    private fun rgb(r: Int, g: Int, b: Int): Int =
        (0xFF shl 24) or (r shl 16) or (g shl 8) or b
}
