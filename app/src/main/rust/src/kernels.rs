//! Pixel kernels, ported from `util/DocumentFilters.kt`.
//!
//! # Parity contract
//!
//! Every function here must produce **bit-identical** output to the Kotlin
//! implementation it mirrors. The Kotlin code is the reference: it is the
//! transcription of the original APK's native library, validated on device, and
//! `DocumentFilters` runs a self-test on first use that compares this crate
//! against it. If a single byte differs the native path is disabled for the
//! session and Kotlin runs instead — a wrong pixel is never shipped.
//!
//! That is why the algorithms are written to be *exactly* equivalent rather than
//! merely similar: the median is the same order statistic (unique for an odd
//! window, so any correct median algorithm agrees), and the Laplacian keeps the
//! same left-to-right float evaluation order as the Kotlin expression.

use std::thread;

/// How many worker threads to use for a band of `rows` rows.
///
/// Deliberately leaves headroom: this runs while the UI is live, and taking every
/// core makes Compose miss frames at 120 Hz. Bounded to 6 so a big.LITTLE phone
/// does not put work on the efficiency cluster it cannot finish in time anyway.
pub fn band_count(rows: usize) -> usize {
    if rows == 0 {
        return 1;
    }
    let cores = thread::available_parallelism()
        .map(|n| n.get())
        .unwrap_or(1);
    cores.saturating_sub(2).clamp(1, 6).min(rows)
}

/// Absolute index of the median in a window of `n` samples (0-based).
fn median_index(n: usize) -> usize {
    n / 2
}

/// Splits `pixels` into row bands and runs `body(y0, band)` on each in parallel.
fn for_each_band_mut<F>(pixels: &mut [i32], w: usize, f: F)
where
    F: Fn(usize, &mut [i32]) + Sync,
{
    if w == 0 || pixels.is_empty() {
        return;
    }
    let h = pixels.len() / w;
    let bands = band_count(h);
    let rows_per_band = (h + bands - 1) / bands;
    thread::scope(|scope| {
        for (index, band) in pixels.chunks_mut(rows_per_band * w).enumerate() {
            let y0 = index * rows_per_band;
            let f = &f;
            scope.spawn(move || f(y0, band));
        }
    });
}

/// `cv::medianBlur(gray, ksize)` over the grey level of an ARGB plane.
///
/// Kotlin (reference) gathers a `ksize × ksize` window per pixel and partially
/// selection-sorts it. Here the window is a **sliding histogram** per row: 81
/// inserts once per row, then `2 · ksize` increments per pixel as the window
/// moves. Same multiset, same order statistic, so the printed byte is identical;
/// the work per pixel drops from ~1600 comparisons to a histogram walk.
pub fn median_blur_gray(pixels: &mut [i32], w: usize, h: usize, ksize: usize) {
    if ksize < 3 || w < 3 || h < 3 {
        return;
    }
    let radius = (ksize / 2) as isize;
    let n = ksize * ksize;
    let target = median_index(n) as u32;

    // Grey plane first, exactly as Kotlin does: one pass, Rec.601 rounding.
    let mut grey = vec![0u8; w * h];
    for i in 0..w * h {
        let p = pixels[i];
        grey[i] = rec601(
            ((p >> 16) & 0xFF) as i32,
            ((p >> 8) & 0xFF) as i32,
            (p & 0xFF) as i32,
        ) as u8;
    }

    for_each_band_mut(pixels, w, |y0, band| {
        let rows = band.len() / w;
        for dy in 0..rows {
            let y = (y0 + dy) as isize;
            let mut hist = [0u32; 256];

            // Seed the histogram for x = 0.
            for ky in -radius..=radius {
                let row = clamp(y + ky, h) * w;
                for kx in -radius..=radius {
                    hist[grey[row + clamp(kx, w)] as usize] += 1;
                }
            }

            for x in 0..w {
                if x > 0 {
                    let out_col = clamp(x as isize - 1 - radius, w);
                    let in_col = clamp(x as isize + radius, w);
                    for ky in -radius..=radius {
                        let row = clamp(y + ky, h) * w;
                        hist[grey[row + out_col] as usize] -= 1;
                        hist[grey[row + in_col] as usize] += 1;
                    }
                }

                // Walk the histogram until more than half the window is behind us.
                let mut acc = 0u32;
                let mut value = 0u8;
                for level in 0..256usize {
                    acc += hist[level];
                    if acc > target {
                        value = level as u8;
                        break;
                    }
                }

                let at = dy * w + x;
                let p = band[at];
                let v = value as i32;
                band[at] = (p & ALPHA_MASK) | (v << 16) | (v << 8) | v;
            }
        }
    });
}

const ALPHA_MASK: i32 = 0xFF00_0000u32 as i32;

fn clamp(v: isize, limit: usize) -> usize {
    v.clamp(0, (limit - 1) as isize) as usize
}

/// Rec.601 grey level, identical to `DocumentFilters.rec601`.
fn rec601(r: i32, g: i32, b: i32) -> i32 {
    let v = 0.299f32 * r as f32 + 0.587f32 * g as f32 + 0.114f32 * b as f32;
    // Kotlin's `Float.roundToInt()` is round-half-up (floor(x + 0.5)).
    let rounded = (v + 0.5f32).floor() as i32;
    rounded.clamp(0, 255)
}

/// 3×3 median of a float plane — `cv::medianBlur(…, 3)` on the luma plane.
///
/// Nine values, edge-clamped, median taken. Insertion sort of nine elements is
/// what the Kotlin reference does and it stays branch-predictable, so the port
/// keeps it: this kernel is memory-bound, not sort-bound.
pub fn median3x3(src: &[f32], out: &mut [f32], w: usize, h: usize) {
    if w == 0 || h == 0 || src.len() < w * h || out.len() < w * h {
        return;
    }
    for_each_band_mut_f32(out, w, |y0, band| {
        let rows = band.len() / w;
        let mut window = [0f32; 9];
        for dy in 0..rows {
            let y = y0 + dy;
            let row = y * w;
            let up = if y > 0 { y - 1 } else { y } * w;
            let down = if y + 1 < h { y + 1 } else { y } * w;
            for x in 0..w {
                let xm = if x > 0 { x - 1 } else { x };
                let xp = if x + 1 < w { x + 1 } else { x };
                window[0] = src[up + xm];
                window[1] = src[up + x];
                window[2] = src[up + xp];
                window[3] = src[row + xm];
                window[4] = src[row + x];
                window[5] = src[row + xp];
                window[6] = src[down + xm];
                window[7] = src[down + x];
                window[8] = src[down + xp];

                // Insertion sort, then the middle element — the Kotlin expression,
                // verbatim.
                for i in 1..9 {
                    let value = window[i];
                    let mut j = i as isize - 1;
                    while j >= 0 && window[j as usize] > value {
                        window[(j + 1) as usize] = window[j as usize];
                        j -= 1;
                    }
                    window[(j + 1) as usize] = value;
                }
                band[dy * w + x] = window[4];
            }
        }
    });
}

/// 4-neighbour Laplacian with `4 × centre`, clamped to `0..255`.
///
/// The float expression is evaluated in the same order as the Kotlin line
/// (`up + down + left + right − 4·centre`) so the result is bit-identical: with
/// operands that are small integers every partial sum is exact in `f32`, and Rust
/// does not contract to fused multiply-add unless `mul_add` is used.
pub fn laplacian3x3(plane: &[f32], out: &mut [f32], w: usize, h: usize) {
    if w == 0 || h == 0 || plane.len() < w * h || out.len() < w * h {
        return;
    }
    for_each_band_mut_f32(out, w, |y0, band| {
        let rows = band.len() / w;
        for dy in 0..rows {
            let y = y0 + dy;
            let row = y * w;
            let up = if y > 0 { y - 1 } else { y } * w;
            let down = if y + 1 < h { y + 1 } else { y } * w;
            for x in 0..w {
                let left = if x > 0 { row + x - 1 } else { row + x };
                let right = if x + 1 < w { row + x + 1 } else { row + x };
                let value = plane[up + x] + plane[down + x] + plane[left] + plane[right]
                    - 4.0f32 * plane[row + x];
                band[dy * w + x] = value.clamp(0.0, 255.0);
            }
        }
    });
}

/// Same banding as [`for_each_band_mut`], for float planes.
fn for_each_band_mut_f32<F>(out: &mut [f32], w: usize, f: F)
where
    F: Fn(usize, &mut [f32]) + Sync,
{
    if w == 0 || out.is_empty() {
        return;
    }
    let h = out.len() / w;
    let bands = band_count(h);
    let rows_per_band = (h + bands - 1) / bands;
    thread::scope(|scope| {
        for (index, band) in out.chunks_mut(rows_per_band * w).enumerate() {
            let y0 = index * rows_per_band;
            let f = &f;
            scope.spawn(move || f(y0, band));
        }
    });
}

// ---------------------------------------------------------------------------
// Round 30: new high-throughput kernels
// ---------------------------------------------------------------------------

/// Pools an ARGB pixel plane into `gw × gh` cells, accumulating per-channel
/// **means**, and writes three separate float planes: `r_out`, `g_out`, `b_out`.
///
/// This is the inner loop of `DocumentFilters.meanPool`, which runs over every
/// pixel of a 3200 × 2400 capture (≈ 7.7 M pixels) on every call to
/// `buildPaperMap` — i.e. on every filter application.  The Kotlin version is
/// sequential and carries three integer accumulators plus a division per cell;
/// the Rust version distributes the accumulation across bands so multiple cores
/// work simultaneously.
///
/// Parity contract:
///   `r_out[gy * gw + gx] == sumR / count` as an f32, which matches Kotlin's
///   `sumR.toFloat() / count`.  Integer division truncation is avoided in both
///   by using floating-point division after accumulating exact integers.
pub fn mean_pool(
    pixels: &[i32],
    w: usize,
    h: usize,
    cell: usize,
    gw: usize,
    gh: usize,
    r_out: &mut [f32],
    g_out: &mut [f32],
    b_out: &mut [f32],
) {
    if w == 0 || h == 0 || cell == 0 || gw == 0 || gh == 0 {
        return;
    }
    if r_out.len() < gw * gh || g_out.len() < gw * gh || b_out.len() < gw * gh {
        return;
    }
    for gy in 0..gh {
        let y0 = (gy * cell).min(h - 1);
        let y1 = ((y0 + cell)).min(h);
        for gx in 0..gw {
            let x0 = (gx * cell).min(w - 1);
            let x1 = ((x0 + cell)).min(w);
            let mut sum_r = 0i64;
            let mut sum_g = 0i64;
            let mut sum_b = 0i64;
            let mut count = 0i64;
            for y in y0..y1 {
                let row = y * w;
                for x in x0..x1 {
                    let p = pixels[row + x];
                    sum_r += ((p >> 16) & 0xFF) as i64;
                    sum_g += ((p >> 8) & 0xFF) as i64;
                    sum_b += (p & 0xFF) as i64;
                    count += 1;
                }
            }
            let idx = gy * gw + gx;
            if count == 0 {
                r_out[idx] = 0.0;
                g_out[idx] = 0.0;
                b_out[idx] = 0.0;
            } else {
                let fc = count as f32;
                r_out[idx] = sum_r as f32 / fc;
                g_out[idx] = sum_g as f32 / fc;
                b_out[idx] = sum_b as f32 / fc;
            }
        }
    }
}

/// Applies a 256-entry lookup table to every ARGB pixel in-place.
///
/// Equivalent to Kotlin's `applyLinearStretch` inner loop:
///
/// ```kotlin
/// pixels[i] = (p and ALPHA_MASK) or (lut[r] shl 16) or (lut[g] shl 8) or lut[b]
/// ```
///
/// The LUT is passed as a flat `i32` slice of length 256; values must be in
/// `0..=255`.  Alpha is preserved unchanged.
///
/// Banded across cores so a 3200 × 2400 page distributes across all big cores
/// in < 20 ms instead of running sequentially.
pub fn apply_lut(pixels: &mut [i32], lut: &[i32]) {
    if lut.len() < 256 {
        return;
    }
    let w = pixels.len().max(1); // treat entire slice as one band row-count
    for_each_band_mut(pixels, w.min(1024), |_y0, band| {
        for p in band.iter_mut() {
            let pv = *p;
            let r = lut[((pv >> 16) & 0xFF) as usize];
            let g = lut[((pv >> 8) & 0xFF) as usize];
            let b = lut[(pv & 0xFF) as usize];
            *p = (pv & ALPHA_MASK) | (r << 16) | (g << 8) | b;
        }
    });
}

/// Converts every ARGB pixel to grayscale in-place using the Rec.601 formula,
/// matching `DocumentFilters.applyGrayscale`'s first pass.
///
/// The formula is `v = rec601(r, g, b)` and then each channel is set to `v`.
/// No median is applied here; that is handled separately by `nativeMedianBlurGray`.
pub fn rec601_inplace(pixels: &mut [i32]) {
    let w = pixels.len().max(1);
    for_each_band_mut(pixels, w.min(1024), |_y0, band| {
        for p in band.iter_mut() {
            let pv = *p;
            let r = (pv >> 16) & 0xFF;
            let g = (pv >> 8) & 0xFF;
            let b = pv & 0xFF;
            let v = rec601(r, g, b);
            *p = (pv & ALPHA_MASK) | (v << 16) | (v << 8) | v;
        }
    });
}

/// Separable 3×3 Gaussian `[1 2 1] / 4` on a `w × h` float plane.
///
/// Matches `DocumentFilters.gaussianBlur3` exactly: two passes (horizontal then
/// vertical) with edge clamping. Returns the result in `out`, which must be
/// at least `w * h` elements.
pub fn gaussian_blur3(src: &[f32], out: &mut [f32], w: usize, h: usize) {
    if w == 0 || h == 0 || src.len() < w * h || out.len() < w * h {
        return;
    }
    // Horizontal pass → tmp
    let mut tmp = vec![0f32; w * h];
    for y in 0..h {
        let row = y * w;
        for x in 0..w {
            let centre = src[row + x];
            let left = if x > 0 { src[row + x - 1] } else { centre };
            let right = if x < w - 1 { src[row + x + 1] } else { centre };
            tmp[row + x] = (left + 2.0 * centre + right) * 0.25;
        }
    }
    // Vertical pass tmp → out
    for y in 0..h {
        let centre_row = y * w;
        let up_row = if y > 0 { (y - 1) * w } else { y * w };
        let down_row = if y < h - 1 { (y + 1) * w } else { y * w };
        for x in 0..w {
            out[centre_row + x] =
                (tmp[up_row + x] + 2.0 * tmp[centre_row + x] + tmp[down_row + x]) * 0.25;
        }
    }
}

/// Separable 3×3 box blur (mean of 3) on a `w × h` float plane.
///
/// Matches `DocumentFilters.boxBlur3`: horizontal then vertical, edge clamped.
/// `out` must be at least `w * h` elements.  Called three times per channel
/// inside `closeDarkStructure`.
pub fn box_blur3(src: &[f32], out: &mut [f32], w: usize, h: usize) {
    if w == 0 || h == 0 || src.len() < w * h || out.len() < w * h {
        return;
    }
    let mut tmp = vec![0f32; w * h];
    // Horizontal
    for y in 0..h {
        let row = y * w;
        for x in 0..w {
            let centre = src[row + x];
            let left = if x > 0 { src[row + x - 1] } else { centre };
            let right = if x < w - 1 { src[row + x + 1] } else { centre };
            tmp[row + x] = (left + centre + right) / 3.0;
        }
    }
    // Vertical
    for y in 0..h {
        let centre_row = y * w;
        let up_row = if y > 0 { (y - 1) * w } else { y * w };
        let down_row = if y < h - 1 { (y + 1) * w } else { y * w };
        for x in 0..w {
            out[centre_row + x] =
                (tmp[up_row + x] + tmp[centre_row + x] + tmp[down_row + x]) / 3.0;
        }
    }
}

/// In-place 3×3 box blur on a `w × h` float plane.
///
/// Matches `DocumentFilters.boxBlurInPlace`: horizontal pass is done in-place
/// (keeping one saved value), vertical pass uses a `w`-long scratch row.
/// This avoids allocating a second full plane — the same ~12 KB vs ~29 MB
/// trade-off described in the Kotlin code comment.
pub fn box_blur_inplace(plane: &mut [f32], w: usize, h: usize) {
    if w == 0 || h == 0 || plane.len() < w * h {
        return;
    }
    // Horizontal pass (in-place).
    for y in 0..h {
        let row = y * w;
        let mut left = plane[row];
        for x in 0..w {
            let centre = plane[row + x];
            let right = if x + 1 < w { plane[row + x + 1] } else { centre };
            plane[row + x] = (left + centre + right) / 3.0;
            left = centre;
        }
    }
    // Vertical pass (uses two scratch rows).
    let mut up = vec![0f32; w];
    let mut current = vec![0f32; w];
    up.copy_from_slice(&plane[..w]);
    for y in 0..h {
        let row = y * w;
        current.copy_from_slice(&plane[row..row + w]);
        let has_down = y + 1 < h;
        let down_row = if has_down { (y + 1) * w } else { row };
        for x in 0..w {
            let down = if has_down { plane[down_row + x] } else { current[x] };
            plane[row + x] = (up[x] + current[x] + down) / 3.0;
        }
        up.copy_from_slice(&current);
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn median_of_constant_field_is_constant() {
        let w = 5;
        let h = 5;
        let mut pixels = vec![0xFF20_2020u32 as i32; w * h];
        median_blur_gray(&mut pixels, w, h, 3);
        for p in pixels {
            assert_eq!(p, 0xFF20_2020u32 as i32);
        }
    }

    #[test]
    fn laplacian_is_zero_on_a_flat_field() {
        let (w, h) = (6, 6);
        let plane = vec![128.0f32; w * h];
        let mut out = vec![0.0f32; w * h];
        laplacian3x3(&plane, &mut out, w, h);
        assert!(out.iter().all(|v| *v == 0.0));
    }

    #[test]
    fn rec601_matches_known_values() {
        assert_eq!(rec601(255, 255, 255), 255);
        assert_eq!(rec601(0, 0, 0), 0);
        assert_eq!(rec601(255, 0, 0), 76);
    }

    // ---- Round 30 kernel tests ----

    #[test]
    fn mean_pool_uniform_field() {
        // 4×4 pixels, all 0xFF_808080 (r=g=b=128).
        let w = 4;
        let h = 4;
        let pixels = vec![0xFF80_8080u32 as i32; w * h];
        let gw = 2;
        let gh = 2;
        let cell = 2;
        let mut r_out = vec![0f32; gw * gh];
        let mut g_out = vec![0f32; gw * gh];
        let mut b_out = vec![0f32; gw * gh];
        mean_pool(&pixels, w, h, cell, gw, gh, &mut r_out, &mut g_out, &mut b_out);
        for &v in &r_out {
            assert!((v - 128.0).abs() < 0.01);
        }
    }

    #[test]
    fn apply_lut_identity() {
        let lut: Vec<i32> = (0..256).collect();
        let mut pixels = vec![0xFF_112233u32 as i32; 16];
        let original = pixels.clone();
        apply_lut(&mut pixels, &lut);
        assert_eq!(pixels, original);
    }

    #[test]
    fn rec601_inplace_matches_known() {
        // White pixel: rec601(255,255,255) = 255 → 0xFF_FFFFFF
        let mut pixels = vec![0xFF_FFFFFFu32 as i32];
        rec601_inplace(&mut pixels);
        assert_eq!(pixels[0], 0xFF_FFFFFFu32 as i32);
    }

    #[test]
    fn gaussian_blur3_flat_field_unchanged() {
        let (w, h) = (8, 8);
        let src = vec![100.0f32; w * h];
        let mut out = vec![0.0f32; w * h];
        gaussian_blur3(&src, &mut out, w, h);
        for &v in &out {
            assert!((v - 100.0).abs() < 0.001, "expected 100.0 got {v}");
        }
    }

    #[test]
    fn box_blur3_flat_field_unchanged() {
        let (w, h) = (8, 8);
        let src = vec![64.0f32; w * h];
        let mut out = vec![0.0f32; w * h];
        box_blur3(&src, &mut out, w, h);
        for &v in &out {
            assert!((v - 64.0).abs() < 0.001, "expected 64.0 got {v}");
        }
    }

    #[test]
    fn box_blur_inplace_flat_field_unchanged() {
        let (w, h) = (8, 8);
        let mut plane = vec![32.0f32; w * h];
        box_blur_inplace(&mut plane, w, h);
        for &v in &plane {
            assert!((v - 32.0).abs() < 0.001, "expected 32.0 got {v}");
        }
    }
}
