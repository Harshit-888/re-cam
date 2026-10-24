package com.example.docscanner.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the pure, Android-free maths inside [DocumentFilters] — the tone curve,
 * the histogram percentile lookup and Otsu's threshold.
 *
 * The full filters need a `Bitmap` and therefore an instrumented test; these
 * helpers are where an off-by-one quietly ruins output (e.g. a stretch that maps
 * paper to 254 instead of 255, or an Otsu threshold that lands on a histogram
 * peak and turns the page solid black).
 */
class DocumentFiltersTest {

    @Test
    fun curveLut_zeroStrength_isIdentity() {
        val lut = DocumentFilters.curveLut(0f)
        for (v in 0..255) {
            assertEquals("identity mismatch at $v", v, lut[v])
        }
    }

    @Test
    fun curveLut_pinsBothEnds() {
        val lut = DocumentFilters.curveLut(0.55f)
        assertEquals("black must stay black", 0, lut[0])
        assertEquals("paper must stay pure white", 255, lut[255])
    }

    @Test
    fun curveLut_isMonotonic() {
        val lut = DocumentFilters.curveLut(0.55f)
        for (v in 1..255) {
            assertTrue("curve went backwards at $v", lut[v] >= lut[v - 1])
        }
    }

    @Test
    fun curveLut_darkensMidtones() {
        // Smoothstep around the midpoint pulls shadow detail down, which is what
        // makes print look black rather than grey.
        val lut = DocumentFilters.curveLut(0.55f)
        assertTrue("mid-grey should darken", lut[128] < 128)
        assertTrue("near-white should stay light", lut[235] >= 230)
    }

    @Test
    fun percentile_returnsExpectedValues() {
        val hist = IntArray(256)
        // 10 samples at 10, 80 at 100, 10 at 250.
        hist[10] = 10
        hist[100] = 80
        hist[250] = 10

        assertEquals(10, DocumentFilters.percentile(hist, 0.0f))
        assertEquals(100, DocumentFilters.percentile(hist, 0.5f))
        assertEquals(250, DocumentFilters.percentile(hist, 0.999f))
    }

    @Test
    fun percentile_handlesEmptyHistogram() {
        assertEquals(0, DocumentFilters.percentile(IntArray(256), 0.5f))
    }

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
}
