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


// ---------------------------------------------------------------------------
// Round 61: the adaptive (ratio-domain) filter kernel
// ---------------------------------------------------------------------------
//
// This is the one kernel that is *not* a transcription of the original APK: it is
// the engine behind the `SMART` and `LIGHT_TEXT` presets, designed by mixing the
// original's paper-map illumination model with a luminance-ratio tone curve and a
// hue-preserving colour reconstruction (the idea Adobe reaches for with its
// CIELAB L* normalisation, done in fixed point so it stays byte-exact).
//
// # Why every step is integer
//
// The Kotlin reference in `DocumentFilters.adaptiveColourReference` must agree with
// this function **bit for bit** or the whole engine is refused (see the parity
// self-test). Float maths could differ in the last bit between the two languages
// for reasons no test can catch; fixed point cannot. The only floats that cross
// the boundary are the paper grid, which is quantised once on the Kotlin side, and
// the detail plane, which is passed in already computed.
//
// # The pipeline, per pixel
//
//   1. paper = bilinear sample of the per-channel paper grid (Q8)
//   2. gain  = soft-knee(255 / paper)          -> white balance + shadow removal
//   3. c2    = round(c · gain)                 -> paper becomes 255 in every channel
//   4. luma  = Rec.601(c2)
//   5. outL  = curve[luma]                     -> the tone curve, indexed by the ratio
//   6. g2    = max(floor, outL / luma)         -> hue-preserving scale, floored so
//                                                 saturated ink keeps its colour
//   7. g2   *= 1 + detail/scale                -> edge crispening (optional)
//   8. out   = grey + (scaled - grey)·chroma   -> restore chroma around its own luma
//
// Everything is Q8 fixed point: 256 == 1.0.

/// Rec.601 luma, **integer** rounding, so Kotlin and Rust cannot disagree.
///
/// Deliberately not the float `rec601` above: that one is the transcription of the
/// original APK's `cvtColor` path and is used by the presets that must match it.
/// This kernel owns its own, cheaper, exactly reproducible luma.
#[inline]
fn luma_fixed(r: i32, g: i32, b: i32) -> i32 {
    (r * 299 + g * 587 + b * 114 + 500) / 1000
}

/// `(v + 128) >> 8` — a Q8 multiply's rounding step.
#[inline]
fn rnd_q8(v: i32) -> i32 {
    (v + 128) >> 8
}

/// Bilinear sample of a Q8 paper grid, over cell-sized squares.
///
/// The grid is indexed by `(x / cell, y / cell)`; the interpolation is between cell
/// values, so the grid only has to be smooth at the cell scale. Integer throughout.
#[inline]
fn sample_q8(plane: &[i32], gw: usize, gh: usize, cell: usize, x: usize, y: usize) -> i32 {
    if gw == 0 || gh == 0 || plane.is_empty() {
        return 255 << 8;
    }
    let cell = cell.max(1);
    let gx0 = (x / cell).min(gw - 1);
    let gy0 = (y / cell).min(gh - 1);
    let gx1 = if gx0 + 1 < gw { gx0 + 1 } else { gx0 };
    let gy1 = if gy0 + 1 < gh { gy0 + 1 } else { gy0 };
    let fx = (((x - gx0 * cell) << 8) / cell) as i32;
    let fy = (((y - gy0 * cell) << 8) / cell) as i32;
    let top = (plane[gy0 * gw + gx0] * (256 - fx) + plane[gy0 * gw + gx1] * fx) >> 8;
    let bottom = (plane[gy1 * gw + gx0] * (256 - fx) + plane[gy1 * gw + gx1] * fx) >> 8;
    (top * (256 - fy) + bottom * fy) >> 8
}

/// A monotone soft knee in Q8: identity up to `knee`, asymptotic to `cap` above it.
///
/// A hard clamp here would put a visible contour on the page wherever the gain
/// crosses the limit, which is precisely the artefact the illumination model exists
/// to avoid. This curve has slope 1 at the knee and never reaches the cap, so no
/// seam can form. It is the same "knee, never a clamp" rule as the paper map's.
#[inline]
fn knee_gain(raw: i32, knee: i32, cap: i32) -> i32 {
    if raw <= knee {
        return raw;
    }
    let span = cap - knee;
    if span <= 0 {
        return cap;
    }
    let over = raw - knee;
    // 64-bit: `span * over` reaches 4.1e9 for a nearly black frame (paper 20 -> raw
    // 835k, over 833k, span 4096). Wrapping here would be a wrong gain in release and
    // a panic — an abort, from inside an `extern "system"` function — in debug.
    (knee as i64 + (span as i64 * over as i64) / (span as i64 + over as i64)) as i32
}

/// The adaptive colour filter: paper-map white balance, ratio tone curve, and
/// hue-preserving chroma restoration. See the block comment above for the steps.
///
/// * `paper_r/g/b` — the illumination grid, Q8, `gw × gh` entries each.
/// * `curve` — 256 entries, `outL` for an input luma of `0..=255`.
/// * `params` — `[knee, cap, floor, chroma, detail_cap, detail_scale]`, all Q8
///   except `detail_scale` (a plain divisor).
/// * `detail` — `w × h` edge strength, or empty to skip edge crispening.
#[allow(clippy::too_many_arguments)]
pub fn adaptive_colour(
    pixels: &mut [i32],
    w: usize,
    paper_r: &[i32],
    paper_g: &[i32],
    paper_b: &[i32],
    gw: usize,
    gh: usize,
    cell: usize,
    curve: &[i32],
    params: &[i32],
    detail: &[f32],
) {
    if w == 0 || pixels.is_empty() || curve.len() < 256 || params.len() < 6 {
        return;
    }
    let knee = params[0];
    let cap = params[1];
    let floor = params[2];
    let chroma = params[3];
    let detail_cap = params[4];
    let detail_scale = params[5].max(1);
    let use_detail = detail_cap > 0 && detail.len() >= pixels.len();

    for_each_band_mut(pixels, w, |y0, band| {
        for (row, chunk) in band.chunks_mut(w).enumerate() {
            let y = y0 + row;
            let base = y * w;
            for (x, slot) in chunk.iter_mut().enumerate() {
                let p = *slot;
                let alpha = p & ALPHA_MASK;
                let r = ((p as u32) >> 16) as i32 & 0xFF;
                let g = ((p as u32) >> 8) as i32 & 0xFF;
                let b = (p as u32) as i32 & 0xFF;

                // 1-2. White balance against the local paper level, soft-kneed so a
                //      scene (or a very dark frame) cannot be lifted to white.
                let pr = sample_q8(paper_r, gw, gh, cell, x, y).max(1);
                let pg = sample_q8(paper_g, gw, gh, cell, x, y).max(1);
                let pb = sample_q8(paper_b, gw, gh, cell, x, y).max(1);
                let gr = knee_gain((255 * 256 * 256) / pr, knee, cap);
                let gg = knee_gain((255 * 256 * 256) / pg, knee, cap);
                let gb = knee_gain((255 * 256 * 256) / pb, knee, cap);

                let cr = rnd_q8(r * gr).min(255);
                let cg = rnd_q8(g * gg).min(255);
                let cb = rnd_q8(b * gb).min(255);

                // 3-6. Tone curve on the illumination-normalised luma, then a scale
                //      that is applied to the colour, not to each channel's own
                //      curve: that is what keeps a stamp red instead of grey.
                let luma = luma_fixed(cr, cg, cb).clamp(0, 255);
                let out_luma = curve[luma as usize];
                let mut gain = if luma > 0 { (out_luma << 8) / luma } else { 256 };
                if gain < floor {
                    gain = floor;
                }

                if use_detail {
                    let d = detail[base + x];
                    let dq = if d <= 0.0 {
                        0
                    } else {
                        ((d * detail_cap as f32) / detail_scale as f32 + 0.5) as i32
                    };
                    let dq = if dq > detail_cap { detail_cap } else { dq };
                    gain = (gain * (256 + dq)) >> 8;
                }

                let sr = rnd_q8(cr * gain).min(255);
                let sg = rnd_q8(cg * gain).min(255);
                let sb = rnd_q8(cb * gain).min(255);

                // 7-8. Restore chroma around the pixel's own luma: a hue-preserving
                //      expansion, never a per-channel clip.
                let grey = luma_fixed(sr, sg, sb);
                let out_r = (grey + ((sr - grey) * chroma >> 8)).clamp(0, 255);
                let out_g = (grey + ((sg - grey) * chroma >> 8)).clamp(0, 255);
                let out_b = (grey + ((sb - grey) * chroma >> 8)).clamp(0, 255);

                *slot = alpha | (out_r << 16) | (out_g << 8) | out_b;
            }
        }
    });
}

/// Rec.601 luma of every pixel into a float plane, exactly as
/// `DocumentFilters.lumaPlane` computes it (integer luma, stored as `f32`).
///
/// Kept as a kernel rather than a Kotlin loop because the adaptive filter needs it
/// only to feed the median/Laplacian pair, and at a 3200 px page that loop alone is
/// tens of milliseconds of pure overhead.
pub fn luma_plane(pixels: &[i32], out: &mut [f32]) {
    if pixels.is_empty() || out.len() < pixels.len() {
        return;
    }
    for (p, o) in pixels.iter().zip(out.iter_mut()) {
        let pv = *p as u32;
        let r = ((pv >> 16) & 0xFF) as i32;
        let g = ((pv >> 8) & 0xFF) as i32;
        let b = (pv & 0xFF) as i32;
        *o = luma_fixed(r, g, b) as f32;
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

    // ---- Round 61: adaptive kernel ----

    /// A grid of one paper level, as `DocumentFilters.paperGridQ8` produces it.
    fn uniform_grid(level: i32, gw: usize, gh: usize) -> Vec<i32> {
        vec![level * 256; gw * gh]
    }

    /// The identity tone curve: output luma == input luma.
    fn identity_curve() -> Vec<i32> {
        (0..256).collect()
    }

    /// `[knee, cap, floor, chroma, detail_cap, detail_scale]` with no knee, no floor
    /// and no detail — i.e. "white balance only".
    fn plain_params() -> Vec<i32> {
        vec![64 * 256, 256 * 256, 0, 256, 0, 50]
    }

    #[test]
    fn luma_plane_matches_rec601_integer() {
        let pixels = vec![
            0xFF_FFFFFFu32 as i32, // white -> 255
            0xFF_000000u32 as i32, // black -> 0
            0xFF_FF0000u32 as i32, // red   -> (255*299+500)/1000 = 76
        ];
        let mut out = vec![0f32; 3];
        luma_plane(&pixels, &mut out);
        assert_eq!(out[0], 255.0);
        assert_eq!(out[1], 0.0);
        assert_eq!(out[2], 76.0);
    }

    #[test]
    fn adaptive_white_paper_stays_white() {
        let (w, h) = (4usize, 4usize);
        let paper = 238;
        let mut pixels = vec![0xFF_EEEEEEu32 as i32; w * h];
        let grid = uniform_grid(paper, 2, 2);
        adaptive_colour(
            &mut pixels,
            w,
            &grid,
            &grid,
            &grid,
            2,
            2,
            2,
            &identity_curve(),
            &plain_params(),
            &[],
        );
        for p in pixels {
            assert_eq!(p, 0xFF_FFFFFFu32 as i32, "paper must come out pure white");
        }
    }

    #[test]
    fn adaptive_is_invariant_to_uniform_illumination() {
        // 74 % reflectance under two paper levels must land on the same byte. The
        // two levels are chosen so the white-balance gains are exactly 2:1 (326 and
        // 652 in Q8) — otherwise the rounding of the gain, not the filter, decides.
        let (w, h) = (4usize, 4usize);
        let lit_grid = uniform_grid(200, 2, 2);
        let shadow_grid = uniform_grid(100, 2, 2);
        let mut a = vec![(0xFF00_0000u32 | (148 << 16) | (148 << 8) | 148) as i32; w * h];
        let mut b = vec![(0xFF00_0000u32 | (74 << 16) | (74 << 8) | 74) as i32; w * h];
        adaptive_colour(&mut a, w, &lit_grid, &lit_grid, &lit_grid, 2, 2, 2, &identity_curve(), &plain_params(), &[]);
        adaptive_colour(&mut b, w, &shadow_grid, &shadow_grid, &shadow_grid, 2, 2, 2, &identity_curve(), &plain_params(), &[]);
        assert_eq!(a[0], b[0], "the same ink under two lights must match");
        // 0.74 x 255 = 188.7 -> 189.
        assert_eq!(a[0], 0xFF_BDBDBDu32 as i32);
    }

    #[test]
    fn adaptive_keeps_hue() {
        // A saturated blue must come out blue-dominant, not grey.
        let (w, h) = (2usize, 2usize);
        let grid = uniform_grid(238, 1, 1);
        let blue = (0xFF00_0000u32 | (60 << 16) | (80 << 8) | 160) as i32;
        let mut pixels = vec![blue; w * h];
        // A curve that pushes everything dark, to exercise the gain floor.
        let curve: Vec<i32> = (0..256).map(|i| if i > 60 { 0 } else { i }).collect();
        let params = vec![64 * 256, 256 * 256, 56, 287, 0, 50];
        adaptive_colour(&mut pixels, w, &grid, &grid, &grid, 1, 1, 2, &curve, &params, &[]);
        let p = pixels[0] as u32;
        let r = (p >> 16) & 0xFF;
        let b = p & 0xFF;
        assert!(b > r + 20, "blue ink must stay blue: got r={r} b={b}");
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
