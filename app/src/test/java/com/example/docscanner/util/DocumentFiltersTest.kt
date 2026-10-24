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

    private fun rgb(r: Int, g: Int, b: Int): Int =
        (0xFF shl 24) or (r shl 16) or (g shl 8) or b
}
