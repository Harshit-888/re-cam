package com.example.docscanner.util

/**
 * The Rust vocabulary matcher (`app/src/main/rust/src/search.rs`), behind the same
 * rule the pixel kernels follow: **the native path is opt-in and proven, never
 * assumed**.
 *
 * # What it accelerates, and why it is worth a JNI call
 *
 * Magic Search answers a query by expanding each typed word into the index's own
 * words: prefixes (`invoic` → `invoice`), and spelling variants within a typo
 * (`invoce` → `invoice`). That means comparing the query against **every word in
 * the library** — the largest structure the index holds, and the only part of a
 * search whose cost grows with the library rather than with the query.
 *
 * It is a pure function of two byte arrays, so it is the ideal shape for native
 * code: no state, no allocation shared with Kotlin, no pixels.
 *
 * # How correctness is guaranteed
 *
 * [DocumentSearch.expandReference] is the **definition**; this is an
 * optimisation of it. On first use the two are run against a fixed vocabulary and
 * compared as arrays — same triples, same order. Only if they are identical does
 * any query use the native path. A missing library, a wrong ABI, a device without
 * the ABI, or a single differing triple all resolve to "use the Kotlin
 * implementation", which is the same code the tests exercise on the JVM.
 *
 * The Kotlin side therefore cannot be made *wrong* by this file; it can only be
 * made faster.
 */
internal object NativeSearch {

    private const val LIBRARY = "docscanner_native"

    /**
     * Bumped whenever the triples the kernel produces change. A stale `.so` from an
     * older checkout reports the old number and is refused rather than quietly
     * answering queries differently.
     */
    private const val EXPECTED_ABI = 1

    /**
     * True only when the shared object loaded *and* reports this ABI.
     *
     * No `Log` call here on purpose: this object is initialised in JVM unit tests
     * too, where `android.util.Log` is a stub that throws. A missing `.so` is a
     * normal state (the toolchain is optional), not an error worth a log line.
     */
    val isLoaded: Boolean = runCatching {
        System.loadLibrary(LIBRARY)
        nativeVersion() == EXPECTED_ABI
    }.getOrDefault(false)

    /**
     * `null` = not yet checked, `true` = native output matched the reference,
     * `false` = mismatch or exception, use Kotlin for the rest of the process.
     */
    @Volatile
    private var parity: Boolean? = null

    /**
     * True once the kernel has been proven against the reference *and* is being
     * used. Read by the debug diagnostics card in Settings, so "is Rust actually
     * running on this build" is answerable without a logcat cable.
     */
    val isActive: Boolean get() = isLoaded && parity == true

    /**
     * Vocabulary matches for [queries], as flattened `(query, vocabulary, code)`
     * triples, or `null` when the Kotlin reference should be used.
     *
     * `code`: `0` exact, `-1` prefix, `1`/`2` edit distance.
     */
    fun expand(packedVocabulary: ByteArray, queries: List<String>, maxDistance: Int): IntArray? {
        if (!isLoaded) return null
        if (parity == null) parity = selfTest()
        if (parity != true) return null
        return runCatching {
            nativeExpand(packedVocabulary, DocumentSearch.packStrings(queries), maxDistance)
        }.getOrNull()
    }

    /**
     * Runs the kernel and the reference over a fixed vocabulary and compares the
     * output arrays.
     *
     * The sample deliberately covers every branch: an exact hit, a prefix, a
     * one-edit typo, a two-edit typo, a word that is *not* within the distance
     * (which must be absent from both), and a non-ASCII word — the last one is what
     * catches an implementation that silently compares bytes instead of characters.
     */
    internal fun selfTest(): Boolean = runCatching {
        val vocabulary = listOf(
            "aadhaar", "aadhar", "bill", "billing", "birth", "card", "date", "dob",
            "invoice", "invoices", "ledger", "pan", "pancard", "receipt", "receipts",
            "return", "tax", "total", "कर"
        )
        val queries = listOf("invoce", "invoi", "aadhar", "dob", "totl", "कर")

        val expected = DocumentSearch.expandReference(vocabulary, queries, MAX_DISTANCE)
            .flatMap { listOf(it.queryIndex, it.vocabularyIndex, it.code) }
            .toIntArray()
        val actual = nativeExpand(
            DocumentSearch.packStrings(vocabulary),
            DocumentSearch.packStrings(queries),
            MAX_DISTANCE
        )
        expected.contentEquals(actual)
    }.getOrDefault(false)

    /** The distance the parity check runs at; the same one queries use. */
    private const val MAX_DISTANCE = 2

    // ---- JNI ---------------------------------------------------------------
    // Names must match `Java_com_example_docscanner_util_NativeSearch_*` in lib.rs.
    // The receiver is the singleton instance, not a class reference.

    private external fun nativeVersion(): Int

    private external fun nativeExpand(
        vocabulary: ByteArray,
        queries: ByteArray,
        maxDistance: Int
    ): IntArray
}
