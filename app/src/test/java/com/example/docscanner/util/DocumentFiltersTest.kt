package com.example.docscanner.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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

    @Test
    fun documentCurve_pinsBothEnds() {
        val curve = DocumentFilters.documentCurveLut()

        assertEquals("shadows must reach pure black", 0, curve[0])
        assertEquals("paper must reach pure white", 255, curve[255])
        assertEquals("highlight noise must snap to white", 255, curve[240])
    }

    @Test
    fun documentCurve_isMonotonic() {
        val curve = DocumentFilters.documentCurveLut()
        for (v in 1..255) {
            assertTrue("curve went backwards at $v", curve[v] >= curve[v - 1])
        }
    }

    @Test
    fun documentCurve_deepensMidtonesWithoutCrushingThem() {
        val curve = DocumentFilters.documentCurveLut()

        // Print at mid-grey must get darker so it reads as ink, but not so dark
        // that anti-aliased glyph edges disappear.
        assertTrue("mid-tone should darken", curve[128] < 128)
        assertTrue("mid-tone should survive", curve[128] > 60)
        assertTrue("near-white must stay light", curve[230] >= 240)
    }

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
    @Test
    fun gatedUnsharp_leavesAFlatPlaneAlone() {
        val plane = FloatArray(40 * 40) { 173f }

        val out = DocumentFilters.gatedUnsharp(plane, 40, 40, amount = 0.6f, passes = 2, floor = 3f)

        for (i in plane.indices) {
            assertEquals("flat plane must be bit-identical, index $i", 173f, out[i], 1e-4f)
        }
    }

    /**
     * A strong edge must actually be steepened. A step from 90 to 180 is an 45-level
     * detail term, far above the 3-level floor, so the boost applies at full
     * strength on the pixels either side of the step.
     */
    @Test
    fun gatedUnsharp_steepensAStepEdge() {
        val w = 32
        val h = 8
        val plane = FloatArray(w * h) { i -> if ((i % w) < w / 2) 90f else 180f }

        val out = DocumentFilters.gatedUnsharp(plane, w, h, amount = 0.6f, passes = 2, floor = 3f)

        val left = 4 * w + (w / 2 - 1)          // last dark pixel
        val right = 4 * w + (w / 2)             // first bright pixel
        assertTrue("dark side of the edge must darken: ${out[left]}", out[left] < plane[left] - 2f)
        assertTrue("bright side must brighten: ${out[right]}", out[right] > plane[right] + 2f)
        // ...and the flat interiors must not move.
        assertEquals(90f, out[4 * w + 1], 1e-3f)
        assertEquals(180f, out[4 * w + w - 2], 1e-3f)
    }

    /** Detail just under the floor is ignored; detail well over it is boosted. */
    @Test
    fun gatedUnsharp_ignoresDetailBelowTheFloor() {
        val w = 16
        val h = 16
        // A 2-level ripple: below SHARPEN_NOISE_FLOOR, i.e. exactly the grain case.
        val plane = FloatArray(w * h) { i -> 150f + if ((i % 2) == 0) 1f else -1f }

        val out = DocumentFilters.gatedUnsharp(plane, w, h, amount = 0.6f, passes = 2, floor = 3f)

        for (i in plane.indices) {
            assertEquals("sub-floor ripple must not be amplified, index $i", plane[i], out[i], 0.5f)
        }
    }

    @Test
    fun noiseEstimate_isZeroOnAFlatField() {
        val plane = FloatArray(30 * 30) { 200f }

        assertEquals(0f, DocumentFilters.noiseEstimate(plane, 30, 30, passes = 2), 0.001f)
    }

    /**
     * Checkerboard noise has a residual equal to its amplitude, so the median
     * absolute residual should come out at that amplitude — not at the mean, and
     * not scaled by the blur.
     */
    @Test
    fun noiseEstimate_readsTheAmplitudeOfCheckerboardNoise() {
        val w = 24
        val h = 24
        val amplitude = 12f
        val plane = FloatArray(w * h) { i ->
            val x = i % w
            val y = i / w
            160f + if ((x + y) % 2 == 0) amplitude else -amplitude
        }

        val noise = DocumentFilters.noiseEstimate(plane, w, h, passes = 2)

        assertTrue("expected roughly $amplitude, got $noise", noise in (amplitude * 0.5f)..(amplitude * 1.5f))
    }

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

    private fun rgb(r: Int, g: Int, b: Int): Int =
        (0xFF shl 24) or (r shl 16) or (g shl 8) or b
}
