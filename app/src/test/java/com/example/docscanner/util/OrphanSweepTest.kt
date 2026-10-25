package com.example.docscanner.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the two rules that make [ImageStore.sweepOrphans] safe to run on every
 * launch.
 *
 * The sweep deletes files, so a bug here is not a glitch — it is a lost scan. The
 * two ways to get it wrong are both covered:
 *
 * 1. Treating a page as unreferenced because the *model* only names its baked file.
 *    The pristine capture is exactly what the next re-edit renders from, so it has
 *    to stay even though nothing points at it.
 * 2. Deleting a file the camera wrote a moment ago and has not committed yet.
 *
 * [isOrphanedPageFile] is the pure decision the sweep uses, so these run in
 * `testDebugUnitTest` with no `Context` and no disk.
 */
class OrphanSweepTest {

    private val now = 1_700_000_000_000L
    private val grace = 10 * 60 * 1000L

    /** Comfortably past the grace window: a file from an earlier session. */
    private val old = now - 24 * 60 * 60 * 1000L

    @Test
    fun `an old file nothing references is swept`() {
        assertTrue(
            isOrphanedPageFile(
                filePath = "/data/pages/orphan.jpg",
                lastModified = old,
                referencedPaths = emptySet(),
                now = now,
                graceMs = grace
            )
        )
    }

    @Test
    fun `every name a referenced page can occupy is kept`() {
        val id = "page-1"
        val referenced = setOf(
            "/data/pages/$id.jpg",
            "/data/pages/${id}_edited.jpg",
            "/data/pages/${id}_edited.png"
        )
        referenced.forEach { path ->
            assertFalse(
                "referenced file must never be swept: $path",
                isOrphanedPageFile(path, old, referenced, now, grace)
            )
        }
    }

    @Test
    fun `the pristine capture is kept even though the model names the bake`() {
        // This is the real shape of an edited page: `imageUri` points at the bake,
        // `originalImageUri` at the pristine file. The sweep is handed all three
        // names, so the pristine file survives.
        val referenced = setOf(
            "/data/pages/p.jpg",
            "/data/pages/p_edited.jpg"
        )
        assertFalse(isOrphanedPageFile("/data/pages/p.jpg", old, referenced, now, grace))
    }

    @Test
    fun `a file written a moment ago is kept even when nothing references it yet`() {
        // The camera writes the page on the shutter press, before any document
        // exists. A sweep that ran in that window would delete the shot.
        val fresh = now - 2_000L
        assertFalse(
            isOrphanedPageFile("/data/pages/just-captured.jpg", fresh, emptySet(), now, grace)
        )
    }

    @Test
    fun `the grace window is measured from the newest allowed file`() {
        val justInside = now - grace
        val justOutside = now - grace - 1L
        assertFalse(isOrphanedPageFile("/data/pages/a.jpg", justInside, emptySet(), now, grace))
        assertTrue(isOrphanedPageFile("/data/pages/b.jpg", justOutside, emptySet(), now, grace))
    }

    @Test
    fun `a file with a timestamp in the future is kept`() {
        // Clocks move backwards (timezone fixes, NTP corrections). A file stamped
        // ahead of "now" must not be treated as ancient.
        val future = now + 60_000L
        assertFalse(isOrphanedPageFile("/data/pages/c.jpg", future, emptySet(), now, grace))
    }
}
