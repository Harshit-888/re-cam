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
 * The expected numbers were derived from `fa1.H` in
 * `docs/original_apk_pipeline_artifacts.zip`, not chosen by eye.
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
    fun linearStretch_neverDividesByZero() {
        val lut = DocumentFilters.linearStretchLut(blackPoint = 128, whitePoint = 128)

        // Degenerate range: the LUT must still be total and monotonic.
        for (v in 1..255) {
            assertTrue("LUT went backwards at $v", lut[v] >= lut[v - 1])
        }
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
