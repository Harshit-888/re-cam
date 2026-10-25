package com.example.docscanner.util

import android.util.Log

/**
 * The Rust pixel kernels (`app/src/main/rust`), behind a contract that cannot
 * change a single output pixel without us knowing.
 *
 * # How the safety works
 *
 * This object only *exposes* the native calls; it never decides that they are
 * correct. `DocumentFilters` runs a one-off **parity self-test** on first use —
 * every kernel against the Kotlin implementation it mirrors, on the same
 * synthetic page, compared byte for byte — and only then routes work here. If the
 * library is missing (no Rust toolchain at build time, wrong ABI, unsupported
 * device) or the self-test disagrees by one byte, the app keeps using Kotlin and
 * scans exactly as it did before.
 *
 * That is deliberate: the filters are the part of this app that must not drift,
 * so the fast path is opt-in and continuously proven rather than assumed.
 *
 * # Why `region` copies instead of pinned arrays
 *
 * See the note at the top of `lib.rs`: the JNI calls copy in and out rather than
 * pinning a `GetPrimitiveArrayCritical` region. A few extra megabytes of `memcpy`
 * is nothing next to the seconds these kernels save, and it removes the classic
 * source of fatal JVM crashes.
 */
internal object NativeFilters {

    private const val TAG = "NativeFilters"
    private const val LIBRARY = "docscanner_native"

    /**
     * Bumped whenever a kernel's output changes. A stale `.so` left in
     * `src/main/jniLibs/` from an older checkout reports the old number and is
     * refused, instead of silently producing different pixels.
     */
    private const val EXPECTED_ABI = 1

    /**
     * True only when the shared object loaded *and* reports the ABI this Kotlin
     * code was written against. Every failure mode — no NDK, no Rust toolchain,
     * missing library, wrong ABI — resolves to `false` and the Kotlin path.
     */
    val isLoaded: Boolean = runCatching {
        System.loadLibrary(LIBRARY)
        nativeVersion() == EXPECTED_ABI
    }.getOrElse { error ->
        runCatching { Log.i(TAG, "Rust kernels unavailable (${error.javaClass.simpleName}: ${error.message})") }
        false
    }

    /**
     * In-place `ksize × ksize` median over the grey level of an ARGB plane.
     *
     * @return `true` when the kernel ran; `false` means "caller must fall back",
     *   never "the pixels are wrong".
     */
    fun medianBlurGray(pixels: IntArray, w: Int, h: Int, ksize: Int): Boolean =
        runCatching { nativeMedianBlurGray(pixels, w, h, ksize) == 0 }.getOrDefault(false)

    /** `out` receives the 3×3 median of `src`; both are `w × h` planes. */
    fun median3x3(src: FloatArray, out: FloatArray, w: Int, h: Int): Boolean =
        runCatching { nativeMedian3x3(src, out, w, h) == 0 }.getOrDefault(false)

    /** `out` receives the clamped 3×3 Laplacian of `plane`. */
    fun laplacian3x3(plane: FloatArray, out: FloatArray, w: Int, h: Int): Boolean =
        runCatching { nativeLaplacian3x3(plane, out, w, h) == 0 }.getOrDefault(false)

    // ---- JNI ---------------------------------------------------------------
    // Names must match `Java_com_example_docscanner_util_NativeFilters_*` in
    // lib.rs. The receiver is the singleton instance, not a class reference.

    private external fun nativeVersion(): Int

    private external fun nativeMedianBlurGray(pixels: IntArray, w: Int, h: Int, ksize: Int): Int

    private external fun nativeMedian3x3(src: FloatArray, out: FloatArray, w: Int, h: Int): Int

    private external fun nativeLaplacian3x3(plane: FloatArray, out: FloatArray, w: Int, h: Int): Int
}
