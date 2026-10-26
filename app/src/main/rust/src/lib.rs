//! JNI surface for the Rust pixel kernels.
//!
//! # Why the arrays are copied rather than pinned
//!
//! `GetPrimitiveArrayCritical` would pin instead of copy, but it forbids any
//! other JNI call while held and is the classic source of fatal JVM crashes when
//! used wrongly. These entry points instead read with
//! `get_*_array_region` into a `Vec`, compute on the `Vec`, and write back with
//! `set_*_array_region`. One extra copy of a few megabytes is a rounding error
//! next to the seconds these kernels save, and the code stays obviously correct.
//!
//! # Failure contract
//!
//! The pixel kernels return `0` on success and `-1` on any failure (bad length,
//! JNI error). The one entry point that returns a new array — the search matcher
//! at the bottom of this file — returns `null` instead. Either way, the Kotlin
//! side (`NativeFilters`, `NativeSearch`) treats failure as "use the Kotlin
//! implementation", so the app never depends on a native call succeeding.
//!
//! The two ABIs are versioned **separately** (`ABI_VERSION` for the pixels,
//! `SEARCH_ABI_VERSION` for the matcher), because they change for different
//! reasons and a stale `.so` should be refused by the half that is stale.
//!
//! # ABI version history
//!
//! * v1 — initial: `medianBlurGray`, `median3x3`, `laplacian3x3`
//! * v2 — Round 30: added `meanPool`, `applyLut`, `rec601InPlace`,
//!         `gaussianBlur3`, `boxBlur3`, `boxBlurInPlace`
//!
//! The Kotlin side declares these as members of an `object`, so the second
//! parameter is the singleton reference (`JObject`), not a class.

mod kernels;
mod search;

use jni::objects::{IntoRaw, JByteArray, JFloatArray, JIntArray, JObject};
use jni::sys::{jint, jintArray};
use jni::JNIEnv;

/// Bumped whenever a kernel's output changes, so the Kotlin side can refuse to
/// use a stale `.so` left over in `jniLibs/`.
///
/// Bumped to 2 in Round 30: five new kernels added (meanPool, applyLut,
/// rec601InPlace, gaussianBlur3, boxBlur3/boxBlurInPlace).
const ABI_VERSION: jint = 2;

#[no_mangle]
pub extern "system" fn Java_com_example_docscanner_util_NativeFilters_nativeVersion(
    _env: JNIEnv<'_>,
    _this: JObject<'_>,
) -> jint {
    ABI_VERSION
}

#[no_mangle]
pub extern "system" fn Java_com_example_docscanner_util_NativeFilters_nativeMedianBlurGray(
    mut env: JNIEnv<'_>,
    _this: JObject<'_>,
    pixels: JIntArray<'_>,
    w: jint,
    h: jint,
    ksize: jint,
) -> jint {
    match median_blur_gray(&mut env, &pixels, w, h, ksize) {
        Ok(()) => 0,
        Err(()) => -1,
    }
}

fn median_blur_gray(
    env: &mut JNIEnv<'_>,
    pixels: &JIntArray<'_>,
    w: jint,
    h: jint,
    ksize: jint,
) -> Result<(), ()> {
    if w <= 0 || h <= 0 || ksize <= 0 {
        return Err(());
    }
    let (w, h, ksize) = (w as usize, h as usize, ksize as usize);
    let len = env.get_array_length(pixels).map_err(|_| ())? as usize;
    if len != w * h {
        return Err(());
    }

    let mut buffer = vec![0i32; len];
    env.get_int_array_region(pixels, 0, &mut buffer)
        .map_err(|_| ())?;

    kernels::median_blur_gray(&mut buffer, w, h, ksize);

    env.set_int_array_region(pixels, 0, &buffer)
        .map_err(|_| ())?;
    Ok(())
}

#[no_mangle]
pub extern "system" fn Java_com_example_docscanner_util_NativeFilters_nativeMedian3x3(
    mut env: JNIEnv<'_>,
    _this: JObject<'_>,
    src: JFloatArray<'_>,
    out: JFloatArray<'_>,
    w: jint,
    h: jint,
) -> jint {
    match median3x3(&mut env, &src, &out, w, h) {
        Ok(()) => 0,
        Err(()) => -1,
    }
}

fn median3x3(
    env: &mut JNIEnv<'_>,
    src: &JFloatArray<'_>,
    out: &JFloatArray<'_>,
    w: jint,
    h: jint,
) -> Result<(), ()> {
    let (src, mut computed) = read_plane(env, src, w, h)?;
    kernels::median3x3(&src, &mut computed, w as usize, h as usize);
    env.set_float_array_region(out, 0, &computed)
        .map_err(|_| ())?;
    Ok(())
}

#[no_mangle]
pub extern "system" fn Java_com_example_docscanner_util_NativeFilters_nativeLaplacian3x3(
    mut env: JNIEnv<'_>,
    _this: JObject<'_>,
    plane: JFloatArray<'_>,
    out: JFloatArray<'_>,
    w: jint,
    h: jint,
) -> jint {
    match laplacian3x3(&mut env, &plane, &out, w, h) {
        Ok(()) => 0,
        Err(()) => -1,
    }
}

fn laplacian3x3(
    env: &mut JNIEnv<'_>,
    plane: &JFloatArray<'_>,
    out: &JFloatArray<'_>,
    w: jint,
    h: jint,
) -> Result<(), ()> {
    let (plane, mut computed) = read_plane(env, plane, w, h)?;
    kernels::laplacian3x3(&plane, &mut computed, w as usize, h as usize);
    env.set_float_array_region(out, 0, &computed)
        .map_err(|_| ())?;
    Ok(())
}

/// Copies a `w × h` float plane out of the JVM and allocates a same-sized output.
fn read_plane(
    env: &mut JNIEnv<'_>,
    array: &JFloatArray<'_>,
    w: jint,
    h: jint,
) -> Result<(Vec<f32>, Vec<f32>), ()> {
    if w <= 0 || h <= 0 {
        return Err(());
    }
    let len = env.get_array_length(array).map_err(|_| ())? as usize;
    if len != (w as usize) * (h as usize) {
        return Err(());
    }
    let mut buffer = vec![0f32; len];
    env.get_float_array_region(array, 0, &mut buffer)
        .map_err(|_| ())?;
    let out = vec![0f32; len];
    Ok((buffer, out))
}

// ---------------------------------------------------------------------------
// Round 30: new JNI entry points
// ---------------------------------------------------------------------------

/// Pools `pixels` (w × h ARGB plane) into `gw × gh` cells of size `cell × cell`,
/// writing per-channel means into three pre-allocated float arrays (r, g, b).
///
/// Signature (flattened):
///   `nativeMeanPool(pixels, w, h, cell, gw, gh, rOut, gOut, bOut): Int`
#[no_mangle]
pub extern "system" fn Java_com_example_docscanner_util_NativeFilters_nativeMeanPool(
    mut env: JNIEnv<'_>,
    _this: JObject<'_>,
    pixels: JIntArray<'_>,
    w: jint,
    h: jint,
    cell: jint,
    gw: jint,
    gh: jint,
    r_out: JFloatArray<'_>,
    g_out: JFloatArray<'_>,
    b_out: JFloatArray<'_>,
) -> jint {
    match mean_pool_jni(&mut env, &pixels, w, h, cell, gw, gh, &r_out, &g_out, &b_out) {
        Ok(()) => 0,
        Err(()) => -1,
    }
}

#[allow(clippy::too_many_arguments)]
fn mean_pool_jni(
    env: &mut JNIEnv<'_>,
    pixels: &JIntArray<'_>,
    w: jint,
    h: jint,
    cell: jint,
    gw: jint,
    gh: jint,
    r_out: &JFloatArray<'_>,
    g_out: &JFloatArray<'_>,
    b_out: &JFloatArray<'_>,
) -> Result<(), ()> {
    if w <= 0 || h <= 0 || cell <= 0 || gw <= 0 || gh <= 0 {
        return Err(());
    }
    let (wu, hu, cellu, gwu, ghu) = (
        w as usize,
        h as usize,
        cell as usize,
        gw as usize,
        gh as usize,
    );
    let plen = env.get_array_length(pixels).map_err(|_| ())? as usize;
    if plen != wu * hu {
        return Err(());
    }
    let grid_len = gwu * ghu;
    let rlen = env.get_array_length(r_out).map_err(|_| ())? as usize;
    if rlen < grid_len {
        return Err(());
    }

    let mut pixel_buf = vec![0i32; plen];
    env.get_int_array_region(pixels, 0, &mut pixel_buf)
        .map_err(|_| ())?;

    let mut r_buf = vec![0f32; grid_len];
    let mut g_buf = vec![0f32; grid_len];
    let mut b_buf = vec![0f32; grid_len];

    kernels::mean_pool(
        &pixel_buf, wu, hu, cellu, gwu, ghu, &mut r_buf, &mut g_buf, &mut b_buf,
    );

    env.set_float_array_region(r_out, 0, &r_buf).map_err(|_| ())?;
    env.set_float_array_region(g_out, 0, &g_buf).map_err(|_| ())?;
    env.set_float_array_region(b_out, 0, &b_buf).map_err(|_| ())?;
    Ok(())
}

/// Applies a 256-entry LUT to every ARGB pixel in `pixels` in-place.
///
/// `lut` must be a `jint[]` of exactly 256 elements with values in `0..=255`.
#[no_mangle]
pub extern "system" fn Java_com_example_docscanner_util_NativeFilters_nativeApplyLut(
    mut env: JNIEnv<'_>,
    _this: JObject<'_>,
    pixels: JIntArray<'_>,
    lut: JIntArray<'_>,
) -> jint {
    match apply_lut_jni(&mut env, &pixels, &lut) {
        Ok(()) => 0,
        Err(()) => -1,
    }
}

fn apply_lut_jni(
    env: &mut JNIEnv<'_>,
    pixels: &JIntArray<'_>,
    lut: &JIntArray<'_>,
) -> Result<(), ()> {
    let lut_len = env.get_array_length(lut).map_err(|_| ())? as usize;
    if lut_len < 256 {
        return Err(());
    }
    let plen = env.get_array_length(pixels).map_err(|_| ())? as usize;
    if plen == 0 {
        return Ok(());
    }
    let mut lut_buf = vec![0i32; lut_len];
    env.get_int_array_region(lut, 0, &mut lut_buf)
        .map_err(|_| ())?;
    let mut pixel_buf = vec![0i32; plen];
    env.get_int_array_region(pixels, 0, &mut pixel_buf)
        .map_err(|_| ())?;

    kernels::apply_lut(&mut pixel_buf, &lut_buf);

    env.set_int_array_region(pixels, 0, &pixel_buf)
        .map_err(|_| ())?;
    Ok(())
}

/// Converts an ARGB pixel plane to grayscale in-place using Rec.601.
///
/// Equivalent to `DocumentFilters.applyGrayscale`'s first loop (before the median).
#[no_mangle]
pub extern "system" fn Java_com_example_docscanner_util_NativeFilters_nativeRec601InPlace(
    mut env: JNIEnv<'_>,
    _this: JObject<'_>,
    pixels: JIntArray<'_>,
) -> jint {
    match rec601_inplace_jni(&mut env, &pixels) {
        Ok(()) => 0,
        Err(()) => -1,
    }
}

fn rec601_inplace_jni(env: &mut JNIEnv<'_>, pixels: &JIntArray<'_>) -> Result<(), ()> {
    let plen = env.get_array_length(pixels).map_err(|_| ())? as usize;
    if plen == 0 {
        return Ok(());
    }
    let mut buf = vec![0i32; plen];
    env.get_int_array_region(pixels, 0, &mut buf)
        .map_err(|_| ())?;
    kernels::rec601_inplace(&mut buf);
    env.set_int_array_region(pixels, 0, &buf)
        .map_err(|_| ())?;
    Ok(())
}

/// Separable [1 2 1]/4 Gaussian blur on a `w × h` float plane.  Result in `out`.
#[no_mangle]
pub extern "system" fn Java_com_example_docscanner_util_NativeFilters_nativeGaussianBlur3(
    mut env: JNIEnv<'_>,
    _this: JObject<'_>,
    src: JFloatArray<'_>,
    out: JFloatArray<'_>,
    w: jint,
    h: jint,
) -> jint {
    match gaussian_blur3_jni(&mut env, &src, &out, w, h) {
        Ok(()) => 0,
        Err(()) => -1,
    }
}

fn gaussian_blur3_jni(
    env: &mut JNIEnv<'_>,
    src: &JFloatArray<'_>,
    out: &JFloatArray<'_>,
    w: jint,
    h: jint,
) -> Result<(), ()> {
    let (src_buf, mut out_buf) = read_plane(env, src, w, h)?;
    kernels::gaussian_blur3(&src_buf, &mut out_buf, w as usize, h as usize);
    env.set_float_array_region(out, 0, &out_buf)
        .map_err(|_| ())?;
    Ok(())
}

/// Separable 3×3 box-mean blur on a `w × h` float plane.  Result in `out`.
#[no_mangle]
pub extern "system" fn Java_com_example_docscanner_util_NativeFilters_nativeBoxBlur3(
    mut env: JNIEnv<'_>,
    _this: JObject<'_>,
    src: JFloatArray<'_>,
    out: JFloatArray<'_>,
    w: jint,
    h: jint,
) -> jint {
    match box_blur3_jni(&mut env, &src, &out, w, h) {
        Ok(()) => 0,
        Err(()) => -1,
    }
}

fn box_blur3_jni(
    env: &mut JNIEnv<'_>,
    src: &JFloatArray<'_>,
    out: &JFloatArray<'_>,
    w: jint,
    h: jint,
) -> Result<(), ()> {
    let (src_buf, mut out_buf) = read_plane(env, src, w, h)?;
    kernels::box_blur3(&src_buf, &mut out_buf, w as usize, h as usize);
    env.set_float_array_region(out, 0, &out_buf)
        .map_err(|_| ())?;
    Ok(())
}

/// Separable 3×3 box blur in-place on a `w × h` float plane.
#[no_mangle]
pub extern "system" fn Java_com_example_docscanner_util_NativeFilters_nativeBoxBlurInPlace(
    mut env: JNIEnv<'_>,
    _this: JObject<'_>,
    plane: JFloatArray<'_>,
    w: jint,
    h: jint,
) -> jint {
    match box_blur_inplace_jni(&mut env, &plane, w, h) {
        Ok(()) => 0,
        Err(()) => -1,
    }
}

fn box_blur_inplace_jni(
    env: &mut JNIEnv<'_>,
    plane: &JFloatArray<'_>,
    w: jint,
    h: jint,
) -> Result<(), ()> {
    if w <= 0 || h <= 0 {
        return Err(());
    }
    let len = env.get_array_length(plane).map_err(|_| ())? as usize;
    if len != (w as usize) * (h as usize) {
        return Err(());
    }
    let mut buf = vec![0f32; len];
    env.get_float_array_region(plane, 0, &mut buf)
        .map_err(|_| ())?;
    kernels::box_blur_inplace(&mut buf, w as usize, h as usize);
    env.set_float_array_region(plane, 0, &buf)
        .map_err(|_| ())?;
    Ok(())
}

// ---------------------------------------------------------------------------
// Magic Search — vocabulary matcher (src/search.rs)
// ---------------------------------------------------------------------------
//
// A pure function over two byte arrays: the index's vocabulary (length-prefixed
// UTF-8) and the words the user typed, in the same format. It answers "which
// vocabulary entry does each word match, and how closely", which is the only part
// of a search whose cost grows with the library instead of with the query.
//
// This is an *optimisation* of `DocumentSearch.expandReference`, which stays the
// definition. `NativeSearch` compares the two on the device before using this, so
// a wrong answer here can only ever mean "the Kotlin implementation runs" — never
// a wrong search result.
//
// Returns a flattened (query, vocabulary, code) triple array, or null on any
// failure. Kotlin treats null exactly like an absent library.

/// Bumped whenever the triples this produces change.
const SEARCH_ABI_VERSION: jint = 1;

#[no_mangle]
pub extern "system" fn Java_com_example_docscanner_util_NativeSearch_nativeVersion(
    _env: JNIEnv<'_>,
    _this: JObject<'_>,
) -> jint {
    SEARCH_ABI_VERSION
}

#[no_mangle]
pub extern "system" fn Java_com_example_docscanner_util_NativeSearch_nativeExpand(
    mut env: JNIEnv<'_>,
    _this: JObject<'_>,
    vocabulary: JByteArray<'_>,
    queries: JByteArray<'_>,
    max_distance: jint,
) -> jintArray {
    match expand_matches(&mut env, &vocabulary, &queries, max_distance) {
        Some(array) => array.into_raw(),
        None => std::ptr::null_mut(),
    }
}

fn expand_matches<'local>(
    env: &mut JNIEnv<'local>,
    vocabulary: &JByteArray<'_>,
    queries: &JByteArray<'_>,
    max_distance: jint,
) -> Option<JIntArray<'local>> {
    if max_distance < 0 {
        return None;
    }
    let vocabulary_bytes = read_bytes(env, vocabulary)?;
    let query_bytes = read_bytes(env, queries)?;
    let vocabulary_words = search::parse_packed(&vocabulary_bytes)?;
    let query_words = search::parse_packed(&query_bytes)?;

    let triples = search::expand(&vocabulary_words, &query_words, max_distance as usize);

    let array = env.new_int_array(triples.len() as jint).ok()?;
    env.set_int_array_region(&array, 0, &triples).ok()?;
    Some(array)
}

/// Copies a byte array out of the JVM as unsigned bytes.
///
/// `get_byte_array_region` hands back `i8`; the conversion is deliberate and
/// lossless (`as u8` reinterprets the same bits), because the payload is UTF-8.
fn read_bytes(env: &mut JNIEnv<'_>, array: &JByteArray<'_>) -> Option<Vec<u8>> {
    let len = env.get_array_length(array).ok()? as usize;
    let mut buffer = vec![0i8; len];
    env.get_byte_array_region(array, 0, &mut buffer).ok()?;
    Some(buffer.into_iter().map(|byte| byte as u8).collect())
}
