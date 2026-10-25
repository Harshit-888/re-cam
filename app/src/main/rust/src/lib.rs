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
//! Every entry point returns `0` on success and `-1` on any failure (bad length,
//! JNI error). The Kotlin side declares these as members of an `object`, so the
//! second parameter is the singleton reference (`JObject`), not a class.
//!
//! `NativeFilters` in Kotlin treats `-1` as "use the Kotlin implementation", so the
//! app never depends on a native call succeeding.

mod kernels;

use jni::objects::{JFloatArray, JIntArray, JObject};
use jni::sys::jint;
use jni::JNIEnv;

/// Bumped whenever a kernel's output changes, so the Kotlin side can refuse to
/// use a stale `.so` left over in `jniLibs/`.
const ABI_VERSION: jint = 1;

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
