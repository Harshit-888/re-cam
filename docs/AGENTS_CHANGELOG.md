# Agent Changelog

Append-only. One entry per change other agents need to know about — especially
**deleted files, renamed symbols, changed signatures, and new dependencies**.
Newest entry at the top.

## 2026-10-25 (u) — Antigravity — Verified native divide table fix & rebuilt APK

- Merged commit `32234d4` from `arena/cf5b35a2-re-cam` into `main` and pushed to `origin/main`.
- Executed unit test suite: all 56 unit tests passed cleanly with 0 failures (`.\gradlew.bat testDebugUnitTest` in 17s).
- Successfully compiled and packaged `app-debug.apk` (21,826,671 bytes).

## 2026-10-25 (t) — Arena.ai — The divide table was indexed by the wrong thing

### The finding
Round 13's premise was that `enhanceDocument` is a transcription problem. It was —
but the transcription in (previous) §13.4 had three errors, and together they made
**every document filter wash the page out**. Measured on the old code: ink at 50 on
paper 238 came out at **98**, i.e. mid-grey text; light print at 110 came out at
**218**, i.e. nearly invisible. That is not a tuning difference, it is the wrong
function.

The disassembly of the table builder (`0x105e0`–`0x10734`) is now decoded exactly:

```c
divide[row][col] = 255 - response[min(2000, row*2000/(col+1))] * 255 / 2000;
```

Three things were wrong before:

| | wrong | correct |
|---|---|---|
| ratio | `value·2000/(row+1)` | `row·2000/(value+1)` |
| `row` means | the paper level | `\|src − paper\|` — the difference |
| stored byte | `q` | `255 − q` (the `−1` before `strb`, on a value the negative magic multiply already flipped) |

The evidence is falsifying rather than aesthetic: the wrong readings either saturate
*every* ink pixel to white or turn paper black. The corrected one gives paper → 255
and ink → 0 at every plausible paper level.

### What changed in code
- `DocumentFilters.divideLookup(row, value)` — rewrite; `ToneTables.divide(row, value)`
  param renamed from `paper`.
- `DocumentFilters.colourise` — the alpha blend now happens **on the divide row**
  (`row = B[|d|] + α(A[|d|] − B[|d|])`), which is what `0x110b0`–`0x11124` does, instead
  of on the output. Stronger edge → bigger row → darker, which is the documented
  "sharpens midtone contrast, preserves chromaticity".
- `applyDocumentBw` — `divide[curveA(|src − paper|)][src]` per channel, then grey.
- New `DocumentFilters.levelTwoChannel(table, paper, src)` and
  `DocumentFilters.toneTables()` as test seams.
- **Deleted** the leftover approximation machinery, all of it unreachable:
  `sharpenLuminance`, `localThresholds`, `gainLimits`, `buildThresholdField`,
  `sharpenDecisionInPlace`, `gatedUnsharp`, `noiseEstimate`, `documentCurveLut`,
  `coverageCut`, `normalise`, `medianHistogram`. **Deleted the 10 tests** that only
  exercised those. Added 2. Suite is now **56 tests** repo-wide, down from 58.
- `medianBlurGray` no longer shares one window buffer across threads (a data race).

### Validation (no device here — this is numpy, not a build)
- Synthetic page, paper 236 with a 45 % left-to-right shadow gradient plus text:
  paper out **255 (max error 1)**, text out **0**, 75 % of the page white, 6.3 % pure
  black. The closing recovers the paper level through the shadow.
- Colour preservation (level 1): stamp `(190,40,45)` on paper `(232,232,232)` →
  `(187,0,0)`.
- 46 numeric assertions in `DocumentFiltersTest` re-derived and checked against a
  faithful emulation of the Kotlin.

### Still not recovered
`PSBlockDigest::algorithm4`, `findSmallestRadius`'s predicate, and the level-1
blend order (A vs B at α = 0) — see spec §13.10. `PaperMap` remains our stand-in for
the first two.

## 2026-10-25 (s) — Antigravity — Added original APK binaries for Arena.ai

- Added `docs/original_apk_binaries/` containing `libimageproc.so` (138 KB), `classes.dex` (10 MB), `classes2.dex` (9.2 MB), `classes3.dex` (7.0 MB), and `classes4.dex` (7.0 MB) directly extracted from the original release splits (`base.apk` and `split_config.arm64_v8a.apk`).
- Added index `docs/original_apk_binaries/README.md` for direct bytecode and symbol inspection.

## 2026-10-25 (r) — Antigravity — Verified 1.9 build & full unit test suite

- Pulled and merged PR #9 (`arena/cfada147-re-cam`, version 1.9 / versionCode 10).
- Executed unit test suite: all 58 unit tests passed cleanly with 0 failures (`.\gradlew.bat testDebugUnitTest` in 40s).
- Successfully compiled and packaged `app-debug.apk` (21,826,671 bytes).
- Pushed merged `main` to `origin/main`.

## 2026-10-25 (q) — Arena.ai — 300 DPI pages, and a B&W cut that is smooth *and* sharp

### The report
Zooming into the **saved PDF** shows pixelated, rough-edged text. Not the
preview — the file.

### It was arithmetic, not tuning
A PDF page is a raster, so its sharpness at zoom is its DPI, and the chain had
three caps stacked on top of each other:

| stage | long edge | DPI on A4 |
|---|---|---|
| capture (`ImageStore.MAX_CAPTURE_DIMENSION`) | 2600 | 243 |
| bake (`DocumentFilters.MAX_PROCESS_DIMENSION`) | **2000** | **187** |
| export setting "300 DPI" (`ExportResolution.HIGH`) | 2400 — unreachable | 187 |

`bitmapForExport` loads the already-baked page, so the export setting could never
exceed what the bake had thrown away. Every stage now agrees on **3200 px**, which
on A4 with the standard 25 pt margin (a 770.8 pt = 10.7 in image box) is
`3200 / 10.7 = ` **299 DPI** — a real 300 DPI scan, to within a third of a percent.
`ExportResolution` was relabelled to match: 3200 / 2140 / 1600 for 300 / 200 / 150
DPI.

### 3200 px forced a memory redesign, which also made it faster
The old Clear B&W path held a full-resolution luminance plane, a full median
plane, a full decision plane, *and* two FloatArrays of windowed mean/σ — and
`boxMeanAndStd` built two `DoubleArray` summed-area tables, i.e. **~150 MB at
3200 px** on its own. That is why the cap was 2000. Four changes remove it:

1. **Otsu** is computed from a 1/9 strided sample of the median-denoised plane
   (~800 k samples for a 256-bin histogram), so the full median pass is gone.
2. **The Sauvola field** is computed on a ~320 px grid and sampled bilinearly.
   A local threshold whose window is 1/16 of the page simply cannot vary inside a
   10 px cell. Verified equivalent: it differs from the exact full-resolution
   field by 6–7 levels on average, and by at most **0.006 IoU** on clean,
   shadowed and dark noisy pages.
3. **The blur is in place** (rolling scalar horizontally, one saved row
   vertically) instead of allocating two more planes.
4. **The passes are band-parallel** across up to 4 cores, with a shared daemon
   pool. Every pass is per-pixel and writes only its own rows.

Peak working set is now ~95 MB for a 3200 px page. A device that still cannot
allocate it retries at 3/4 scale (2400 px ≈ 225 DPI) instead of failing the save
— and an `OutOfMemoryError` is no longer swallowed into "return the unfiltered
page", which is what previously made a memory failure look like "the filter did
nothing".

### Rough edges: the cut had no sub-pixel information
A threshold is a hard decision, so a diagonal stroke becomes a staircase. The old
3 %-of-threshold ramp was an attempt to soften it, but a ramp is expressed in
*levels*: it widens with the local contrast and cannot encode where the contour
passes through the pixel.

The cut now computes **coverage**: 3 × 3 sub-samples of the decision plane per
pixel, but *only for the pixels the contour touches* (a dilate-by-1 of the hard
mask, a few per cent of the page), so the interior stays exactly 0 or 255 and the
AA pass costs roughly a sixth of a plain cut.

Measured against a 16 × 16 supersampled ground truth (rotated stroke + ring), RMS
edge error:

| cut | RMS vs true | AA pixels (true: 356) |
|---|---|---|
| hard clip (1.8) | 0.0441 | 0 |
| 2 × 2 coverage | 0.0219 | 216 |
| **3 × 3 coverage (shipping)** | **0.0190** | **290** |
| 4 × 4 coverage | 0.0186 | 330 |
| gradient/SDF-normalised | 0.0357 | 590 (over-spreads) |

At 3× zoom (what a PDF viewer does) the RMS is 0.044 → 0.019. On real synthetic
pages, **94–100 %** of the grey pixels land within one pixel of the true contour —
the softening is on the edge, never a halo. The level-domain ramp constants
(`BW_RAMP_FRACTION`, `BW_MIN_RAMP`) are deleted; they are now redundant *and* were
the wrong shape.

### Clear B&W now saves losslessly
The PDF embeds the decoded page bitmap losslessly (Flate), so JPEG ringing around
glyphs was not merely present in the file — it was permanent. Binary text is the
worst case for an 8 × 8 DCT and the best case for DEFLATE, so `CLEAR_BW` and
`GRAYSCALE` pages are written as **PNG** (`ImageStore.saveEditedPage(lossless =
true)`, `_edited.png`), typically smaller than the JPEG they replace. Old pages
keep their `.jpg` — `imageUri` is an absolute path, not a naming convention — and
`deletePageFiles` removes both.

The editor's preview encodes a JPEG round-trip only when the save is a JPEG, so
for the lossless filters the preview is now *exactly* the file's pixels.

### Signature changes for other agents
- `ImageStore.saveEditedPage(...)` gained `lossless: Boolean = false` (additive);
  new `ImageStore.writePng`, `ImageStore.editedLosslessFile`.
- `ImageStore.MAX_CAPTURE_DIMENSION` 2600 → 3200; `ImageQuality.HIGH` 2600 → 3200,
  `MEDIUM` 1900 → 2000; `ExportResolution` 2400/1600/1200 → 3200/2140/1600;
  `PdfExportConfig.maxImageDimensionPx` default 2000 → 3200.
- `DocumentFilters.MAX_PROCESS_DIMENSION` 2000 → **3200**. These five numbers must
  move together; changing one only moves the bottleneck.
- New `internal` helpers: `bands`, `coverageCut`, `bilinearAt`, `medianOf9InPlace`,
  `boxBlurInPlace`, `noiseFromResidual`. `median3x3` and `noiseEstimate` remain
  (still covered by tests) but are no longer on the Clear B&W path.
- The editor preview renders at `PREVIEW_RENDER_DIMENSION` (2400) rather than at
  the bake size; the file is still 3200. Both are resolution-independent by
  construction (window and AA scale with the page).

### Tests
`DocumentFiltersTest` +10 (48 total): the process dimension is checked against the
300 DPI requirement arithmetically; `bands` must visit every row exactly once (a
band bug would silently leave rows black, and only on multi-core devices);
`bilinearAt` interpolates and clamps; `medianOf9InPlace` returns the middle value;
the in-place blur leaves a flat plane untouched, spreads an impulse to 1/9 and
conserves energy; `noiseFromResidual` reads the residual amplitude; and
`coverageCut` keeps interiors exactly bi-level while producing ~85 (one third) on
a contour pixel and **no grey at all** on a uniform plane. Every expected value was
checked against a numpy transcription before being written down.

## 2026-10-25 (p) — Arena.ai — Sharp Clear B&W, and a preview that is the saved page

### The report
Three things, after using 1.7 on a real document: *Clear B&W* text is still not
sharp; adjusting the borders and then opening **Filters** shows the old photo, not
the cropped one; and the filter preview does not look like what gets saved, so it
is impossible to tell which filter is best.

The first was tuning. The second and third were **bugs introduced in 1.7** — a
recycled bitmap and a preview rendered at a different size from the bake.

### 1. "Soft text" was a wide threshold ramp, not a weak threshold
The 1.7 B&W threshold had a 6 %-of-threshold transition band — ~10 grey levels
either side of the cut. Every glyph edge landed inside it and came out
anti-aliased, which reads as blur no matter how correct the threshold is.

The band is now **3 % with a 3-level floor**, and the *decision* plane is sharpened
before it is cut: an unsharp mask with a soft dead-zone below 3 levels (so grain
is not amplified) and a strength that fades 4 → 9 as the page's own noise estimate
rises (so a grainy frame gets less, not more). Thresholding a sharpened plane
straightens the transition instead of leaving it wandering over three pixels.

Measured through the whole pipeline (normalise → median → Sauvola) against a
ground-truth glyph mask, with the anti-aliased ("grey") pixel share:

| page | 1.7 IoU | now IoU | grey 1.7 → now |
|---|---|---|---|
| clean | 0.820 | **0.866** | 0.70 % → **0.22 %** |
| shadowed to 35 %, soft focus | 0.762 | **0.785** | 1.49 % → **0.63 %** |
| dark 20 %, noisy | 0.758 | 0.758 | 1.46 % → **0.65 %** |

A sweep of the strength (0.6 / 0.8 / 1.0), the pass count (2 / 3) and the floor
(2 / 3 / 4) moved mean IoU by less than 0.004 in total, so the shipped values are
the ones that never lose accuracy on the noisy page rather than the ones that win
a thousandth on a clean one. **Do not** raise the amount to recover the last
0.003 on clean pages — it costs 0.013 on noisy ones.

### 2. The blank preview was a recycled bitmap
`CropAndFilterScreen` warped the source and then recycled it in a `finally`, but
`DocumentFilters.apply` returns its **input unchanged** for `ORIGINAL` and on any
failure — so for those paths the recycled bitmap was the one handed to the UI.
That is the "filter preview disappears" report.

- Intermediates are now recycled only when they are neither the source nor the
  result. `apply`'s "returns its input" contract is documented on both sides.
- A failed render no longer falls back silently. It used to show the **unwarped**
  frame, which looked like the filter had done nothing; it now shows an error
  banner ("Preview could not be rendered at full size") over the honest source
  frame.

### 3. The preview is now the saved image, at the saved size
Previously: the editor decoded at **1400 px**, filtered at **900 px**
(`PREVIEW_DIMENSION`), and saved at **2000 px** (`MAX_PROCESS_DIMENSION`) from a
capture of up to 2600 px. So the preview was a 2.2× smaller render, upscaled into
its frame — softer, with a different noise response, than the PDF.

Now the editor decodes at the bake dimension, renders the filter at that same
dimension, on the **warped** bitmap, in the same order `PageRenderer` uses. One
`LaunchedEffect` keyed on `(source, filter, corners, mode, dimension)` renders
after a 140 ms settle so that swiping the chips does not queue a full-size render
per frame.

The render is also round-tripped through `Bitmap.compress(JPEG, quality)` before
display, so the preview shows the *encoded* pixels the save will contain — at
Medium (88) the difference in small text is visible, and a preview that hides it
would make Medium look better than it saves.

That also fixes **"Filters shows the uncropped photo"**: `previewAspect` is taken
from the bitmap actually displayed, so the frame is sized to the *warped* page,
and switching from the border tab to the filter tab re-renders through the same
effect.

### Signature changes for other agents
- `PageRenderer.bake(...)` / `bakeBlocking(...)` gained
  `maxDimension: Int = DocumentFilters.MAX_PROCESS_DIMENSION`.
- `ImageProcessingUtils.applyEnhancements(...)` gained the same parameter.
- **New** `PageRenderer.bakeDimension(quality: ImageQuality): Int` — the single
  source of truth for "how big is the saved page". `min(quality.maxDimension,
  MAX_PROCESS_DIMENSION)`, i.e. HIGH (2600) is filtered at 2000 rather than
  silently costing a third more memory per plane; MEDIUM (1900) is honoured.
  Anything that renders a page for display must use this, or the preview drifts
  from the save again.
- New `internal` helpers on `DocumentFilters`: `gatedUnsharp`,
  `noiseEstimate`, `adaptiveSharpenAmount`. No public signature changed.

### Tests
`DocumentFiltersTest` +6: the gate is an exact identity on a flat plane and on
sub-floor ripple, steepens a real step, `noiseEstimate` returns 0 on a flat field
and ~the amplitude on checkerboard noise, and the adaptive amount is full at 4,
half at 6.5, zero at 9 and beyond, monotonic in between. Values checked against a
numpy transcription of `boxBlur3`/`gatedUnsharp` before being written down.

## 2026-10-25 (o) — Arena.ai — Magic Color: one continuous curve, no lifted/not-lifted split

### The report
*Lighten* is good. *Magic Color* "lightens some areas and leaves some areas as it
is"; the original does not do that. *Clear B&W* "needs more improvement".

![source | 1.6 | now](filter_magic_uniformity.jpg)

*Left: source, with a hard shadow across the bottom-right. Middle: 1.6 — the
shadow quadrant stays grey. Right: now — one continuous curve, no quadrant left
behind.*

### Root cause: a hard clamp, not a per-region decision
1.6 bounded the divide with `max(floor, surface)`, where the floor is **one
global number** (`0.62 ×` the frame's lit level). On the reported frame that
split the picture by threshold: **73.9 %** of pixels sat below it and got a flat
~1.6× lift, the other 26 % were above it and quietly stopped being lifted. That
is the artefact, exactly — not two different algorithms choosing regions, but one
number switching the lift on and off, with the boundary visible.

Replacing that with `softKnee` — identity below the knee, unit slope at it,
asymptotic to the cap above it — removes the switch. Nothing in the frame is
treated by two different rules.

### It was not enough on its own: the estimator was biased
With the clamp removed the deep shadow still came out grey (205 instead of 255).
The cause was upstream. The generator pooled each cell with a **maximum**, on the
reasoning that paper is the brightest thing in a patch containing print. Measured
against a known paper level, a max is biased **up by 2–3 σ of sensor noise**:
`bg/paper = 1.26` in a deep shadow, which is a 26 % error exactly where the gain
is largest, and 1.91 on the real scan. Pooling with a **mean** drops that to
1.02, and rejecting ink is left to the closing that follows — the correct
division of labour, and what the background-estimation literature does.

| change | deep-shadow paper | paper p5 | paper std |
|---|---|---|---|
| 1.6 (hard floor, max pool) | 106 | 62 | 60 |
| soft knee, max pool | 205 | 160 | — |
| soft knee, **mean pool** | **247** | **243** | **17** |

A third box pass after the closing also matters: two left the cell grid visible
on a hard-edged shadow.

### Clear B&W
Sauvola as a **bounded refinement** of Otsu, not a replacement:
`T = m·(1 + k·(σ/R − 1))`, `k = 0.34`, `R = 128`, window `max(W,H)/16` clamped to
an odd ≥ 15, computed from integral images so the cost does not grow with the
window. Two guards, both from measurement:

- under `σ < 6` the window holds no text/paper decision, so Otsu's answer is used
  (ungated Sauvola speckles blank margins);
- the local threshold may only go **below** Otsu, never above
  (`[0.85·Otsu, Otsu]`). Measured unclamped, Sauvola left 5 % of already
  normalised paper grey that plain Otsu got white, and a threshold above Otsu
  turned ink grey. The asymmetry means it can recover a faint stroke and cannot
  lose one.

Also: document gain knee 6.5 → **8**, cap 16 → **24** (deep-shadow paper
244 → 247, B&W paper purity 97.3 % → 98.3 %), gamma 1.28 → **1.35** (ink 41.5 →
38.1, paper p5 unchanged).

### Editor: the two things asked for
- **Tapping *Borders* now detects them.** The chip calls the same
  `detectBorders()` as the Auto detect button, so the quad is a starting point to
  nudge rather than something to drag out from the frame edges. It will not
  overwrite corners the user has already moved (`cornersEdited`), and *Full page*
  clears that flag.
- **The preview now shows the corrected, filtered page.** It runs
  `PerspectiveWarp.warp` → `DocumentFilters.applyPreview`, the same
  warp-then-filter order as `PageRenderer.bake`, so the preview and the saved
  file agree wherever the quad is not the full frame. The Borders tab still shows
  the uncropped frame, because that is what you aim at.

### Known trade-off, not hidden
A divide-based shadow removal brightens whatever is darkest locally, so a *printed
photograph* on the page has its shadows lifted too. Both 1.6 and this revision do
it; the fix for it is per-region protection (detect photo blocks, skip the
divide inside them), which is not in this change. Deep-shadow noise is also
amplified by the same gain — visible as grain on dark stamps.

### Tests
`softKnee` (identity below, continuity, unit slope, cap, monotonicity),
`saturateGain` (document vs scene, zero surface), `boxMeanAndStd` (against a
naive double loop, and zero deviation on a flat field), `sauvolaWindow` (odd,
scaling, tiny inputs). 8 new; every expectation was checked against a Python
transcription of the Kotlin.

### Version
**1.7 (code 8)**.

## 2026-10-25 (n) — Arena.ai — Document filters no longer blow up photos taken in the dark

### The report
A photograph of a glowing laptop screen in a dark room, saved as *Magic Color*,
came out with **13 % of the page at pure white** (a grey mock-up of the desktop
where the screen was, everything around it white). The original APK handles the
same kind of frame. Data from the scanned page and from the 4000 × 3000 source:
mean luminance **51/255**, 1.0 % of pixels at ≥ 250, **49 %** at ≤ 30.

### Why it happened — a step we did not have
`enhanceDocument` divides the frame by its own illumination surface,
`I / paper · 255`. On a page that is the whole point: a 30 % shadow has to become
paper. On a dark scene it is catastrophic, because the divide is **selective**.
The screen reads 230 and the room around it reads 15, so the surface follows the
room down into the dark, every *moderately* dark pixel (20–70) is amplified past
250, and the genuinely black pixels stay black. That is exactly the "grey
ghost" the PDF showed — the filter is not doing nothing, it is doing the right
operation on the wrong frame.

The original computes `imageType` (`hv3.PSImageTypeDocumentImage` /
`PSImageTypePhotoImage`) for **every** capture in `fa1.H` and carries it in
`PSImageEstimationInfoBean`. I traced `bx.smali`'s bake path (lines 127–195) and
it branches only on `enhanceType` — `Original` → `qx3`, `Lighten` → `tw3`,
level 1/2 → `lu3`. **`imageType` is never read**, so there is no explicit gate to
copy; the original's native code simply never runs away on a dark frame the way
ours does. Since the gate is not the difference, the fix had to be inside our
illumination model.

### The fix
The divide is now bounded, per channel, by how dark the surface is allowed to
get relative to the frame's own paper level:

| Frame | Bound | Why |
|---|---|---|
| reads as a document | `0.25 ×` lit level | a page must keep its shadow removal |
| reads as a scene | `0.62 ×` lit level | the gain ceiling stops a dark frame being repainted |

"Lit level" is the 95th percentile of the smoothed surface per channel — not the
maximum (one specular highlight is not the paper) and not the mean (a large dark
print block drags it down).

The frame type comes from `wv3`'s own rules — the ones we had ported and never
used. **`wv3`'s thresholds are absolute** (`R ≥ 80`, `G ≥ 80`, `B ≥ 70`), so a
page at half exposure scored 33 % and would have been misread as a photo.
`classifiesAsDocument` therefore scales the frame to a target luminance before
asking, which makes the question exposure-invariant; the gain is capped, so a
genuinely dark frame is not lifted into a false positive.

### Measured
| | source | before | after |
|---|---|---|---|
| mean luminance | 51.2 | 93.2 | **53.0** |
| ≥ 250 (blown out) | 1.0 % | 13.2 % | **2.5 %** |
| ≤ 30 (blacks kept) | 49.0 % | 39.1 % | **52.4 %** |

![source | Magic Color before | after](filter_fix_dark_scene.jpg)

*Left: the capture. Middle: what 1.5 shipped — the desktop area is a white
mock-up. Right: after the fix.*

That frame classifies as a scene at **29.5 %** (doc rule) / 4.9 % (photo rule).
Documents are unaffected: a synthetic page with ink and a 45 % shadow gradient
still lands paper at 249 and ink at 41.5, at full, 60 % and 35 % exposure —
because the *classification* is exposure-invariant even where the enhancements
are not.

### Tests
`classify_callsAFlatLitPageADocument`, `classify_isInvariantToExposure` (232 vs
the same page at 81), `classify_doesNotCallADarkRoomADocument`,
`classify_doesNotCallALandscapeADocument` (sky + dark ground scores photo 50 %,
under the 60 % bar), `classify_survivesAnEmptyArray`.

### Known limit, documented not papered over
A page shot at 35 % exposure still comes out at 180 rather than white, because
`MAX_ILLUMINATION_GAIN = 4.5` clamps the divide before the relative bound is
reached. Lowering it would turn a page that is mostly a dark printed photo
white, so it stays. `Lighten` (`enhancePhoto`'s linear stretch) is the right tool
for a frame that dark, and it is already exact.

### Version
**1.6 (code 7)**.

## 2026-10-25 (m) — Antigravity — Unit test executor memory limit & build verified

- Configured `testOptions.unitTests.all { maxParallelForks = 1; jvmArgs("-Xmx512m") }` in `app/build.gradle.kts` to prevent parallel test executor forks from exhausting host paging memory on high-core CPUs.
- Verified test suite: all 28 unit tests passed with 0 failures (`testDebugUnitTest`).
- Rebuilt `app-debug.apk` (21,810,287 bytes).

## 2026-10-25 (l) — Arena.ai — Filters corrected against the native disassembly

### What changed and why
The filters now follow the **actual native code**, not the documented behaviour
of it. `libimageproc.so` was disassembled (spec §6–§8, entry (k)) and three
things I had reconstructed by reasoning turned out to be wrong:

| I had | The disassembly says | Now |
|---|---|---|
| Skip *Lighten* when `blackPoint > whitePoint` | `range = wp - bp; if (range <= 0) range = 1.0;` then the `v <= bp` branch wins | verbatim port, no guard |
| Dilation only for the paper map (block max) | `cv::dilate` **then** `cv::erode` — a closing that fills ink with paper | max-pool, then dilate → erode |
| 3×3 majority vote on the **binarised** result | `cv::medianBlur` runs **before** the threshold | median on the normalised plane, then Otsu |

### Transcribed exactly (do not "improve")
- **`enhancePhoto`** (`0x0000f5d4`, 608 B) — ported line for line; see
  `linearStretchLut`. The `range <= 0` fallback and the branch order together
  mean an inverted pair thresholds hard at `blackPoint` instead of blackening the
  page, which is what the original does. `DocumentFiltersTest` pins all three
  branches.
- **`enhanceDocument` step list** — background estimate, divide, tone curve,
  `cv::medianBlur` before the threshold, `cv::Laplacian`/`cv::GaussianBlur`
  sharpening of midtone contrast. Each step has a comment naming the import that
  justifies it, so nobody re-adds a step that is not in the ELF.
- **No static LUT tables in the binary** — the tone mapping is computed at
  runtime, so the curve stays a parameterised gamma with pinned endpoints rather
  than a copied table.

### The one number that is still a model
`enhanceDocument` calls `ColorizingRadius::computeRadius(w, h, dpi, level)`,
which lives behind the stripped PLT; its body was not recovered. What is certain
is that the radius depends on size — a fixed radius cannot close over a 60 px
text line and a 6 px one at the same time.

A fixed radius of 1 (my first port of that step) closes over nothing but
single-pixel specks, and the divide then washes text out entirely: on a synthetic
page with a 50 % illumination gradient, ink came out at 255. Measured on the same
page, **~2 % of the long edge** is the smallest reach that bridges body text, and
it is scale-free — `paperRadius()` derives it from the working grid
(`PAPER_OPEN_DIVISOR = 48`). With it, across 200 px → 2000 px inputs:

| Input | Cell | Radius | Paper luminance (min/avg) | Ink (avg) |
|---|---|---|---|---|
| 200 × 260 | 1 | 5 | 250 / 255 | 52 |
| 600 × 800 | 3 | 5 | 252 / 255 | 51 |
| 1200 × 1600 | 5 | 6 | 250 / 255 | 51 |
| 2000 × 2600 | 9 | 6 | 250 / 255 | 48 |

*Clear B&W* on the 600 × 800 page: **100 %** of paper lands on pure white and
95.7 % of ink on pure black, the remainder being anti-aliased edges inside the
ramp — the ramp is doing its job, not the threshold.

That constant is the only unverified number left in the pipeline, and it is
isolated so calibration touches one line.

### Tests
`linearStretch` branch semantics (endpoints, `range <= 0`, inverted pair),
`median3x3` (speck removal, edge preservation), `gaussianBlur3` (flat-field
invariance and the 1:2:1 × 1:2:1 weights), `paperRadius` (scale invariance), plus
the earlier estimation, classification, curve and Otsu coverage. Removed the
assertion for the deleted span guard.

### Version
**1.5 (code 6)** — unchanged from the previous entry in this PR.

## 2026-10-25 (k) — Antigravity — Native libimageproc.so disassembled & exact OpenCV filter algorithms extracted

- Extracted ARM64 native library `libimageproc.so` (138,664 bytes) from connected device split APK `split_config.arm64_v8a.apk`.
- Disassembled and analyzed all 7 exported JNI functions (`enhancePhoto`, `enhanceDocument`, `enhanceSignature`, `signatureFilter`, `detectPageBorder`, `perspectiveTransformRect`, `calculateIOU`).
- Verified zero static LUT tables exist; `enhancePhoto` computes a dynamic 256-entry linear stretch LUT in RAM (`cv::LUT`), while `enhanceDocument` performs morphological background estimation (`ColorizingRadius` via `cv::dilate`/`cv::erode`) to divide paper illumination.
- Updated `docs/ORIGINAL_APP_FILTER_SPEC.md` with binary symbol profile (77 UND symbols, 28 OpenCV primitives), decompiled C source logic, and actionable guidance for Kotlin reimplementation in `DocumentFilters.kt`.

## 2026-10-25 (j) — Antigravity — Reverse-engineered filter spec from original base APK

- Added `docs/ORIGINAL_APP_FILTER_SPEC.md` documenting the exact image pipeline reverse-engineered from the original `base.apk`.
- Details exact operation order, dynamic range calculation (`blackPoint`/`whitePoint` from 100x100 histogram 2.0% cutoffs), document vs. photo color classification thresholds, filter dispatch mapping (`cv3`/`yu3` to `PSOpenCV`), and border detection architecture (`border_detect_224.tflite` + IoU >= 0.96f).

## 2026-10-25 (i) — Antigravity — DocumentDetector compilation fix & build verified


- Fixed compiler error in `DocumentDetector.kt`: `quadFromBoundaries` returned `Pair<QuadCorners, Boolean>?` while caller accessed `.corners` and `.confident`. Changed `quadFromBoundaries` to return `Detection?` directly (`Detection(it, scan.fittedSides >= 3)`).
- Verified build and unit test suite: `./gradlew assembleDebug testDebugUnitTest` passed cleanly in 35s.
- Built `app-debug.apk` (versionCode 5, versionName 1.4).

## 2026-10-25 (h) — Arena.ai — Working Settings screen


### Why
The app had no settings screen at all: the legacy APK's Settings (Theme / Scan /
PDF / Default Email / OCR / Sync) had never been ported, and the dashboard had no
entry point to one. This adds the page **and** the behaviour behind every row.

### New files
- `model/AppSettings.kt` — `@Serializable` settings model plus `ThemeMode`,
  `AccentColor`, `ImageQuality`, `ExportResolution`, `PageOrientation`,
  `ImageScale`.
- `data/SettingsStore.kt` — `settings.json`, same debounced + atomic write
  pattern as `LibraryStore`. Separate file so a theme toggle never rewrites the
  document library, and so the theme is available before the first frame.
- `ui/settings/SettingsScreen.kt` — sectioned page matching the legacy layout.
- `util/GalleryExporter.kt` — MediaStore insert (API 29+, `IS_PENDING`) and a
  legacy `Pictures/DocScanner` + `MediaScannerConnection` path for API 26–28.
- `app/src/test/.../model/AppSettingsTest.kt` — defaults, JSON round-trip,
  unknown-key tolerance (upgrade safety), accent contrast, preset ordering.

### Every row does something
| Row | Effect |
|---|---|
| Choose theme | `DocScannerTheme(darkTheme = …)`; status/nav bar icon polarity stays in sync via `enableEdgeToEdge(SystemBarStyle…)` |
| Color | Overrides the four primary slots of the scheme; six hand-checked palettes |
| Use in-app camera | Off = dashboard scan opens the **system photo picker** and imports into a new document |
| Shutter sound | `MediaActionSound.SHUTTER_CLICK` on capture |
| Image quality | Capture downscale target + JPEG quality, applied on capture, gallery import and editor bakes |
| Auto-save to Gallery | Copies each captured page to `Pictures/DocScanner` |
| Document Name | PDF file-name prefix (`Document_<title>_<ts>.pdf`) |
| Page Size | Initial selection in the export sheet |
| Resolution | Embedded bitmap longest edge (150/200/300 DPI) |
| Page Orientation | Per-page axis: auto swaps landscape pages |
| Image Scale | Fit (contain) / Fill (cover + clip) / Original (1 px = 1 pt) |
| Default Email To / Subject | `EXTRA_EMAIL` / `EXTRA_SUBJECT` on the share intent |

### Deliberately **not** added
*OCR Language* (no OCR yet), *Camera Engine* (CameraX is the only engine),
*Image Picker for Scanning* (we always use the system picker), *Sync to Cloud*
(no transport). A row that persists a value nothing reads is a fake control —
`AGENTS.md` §2.7. They are listed under roadmap Priority 0.

### Signature changes (update call sites)
- `PdfExportSheet(document, settings, onDismiss)` — new `settings` parameter.
- `PdfExportConfig(+ pageOrientation, imageScale, fileBaseName)`.
- `ImageStore.importFromUri / importFromFile / createPageFromUri /
  saveEditedPage` gained `maxDimension` / `quality` parameters with defaults
  equal to the previous constants, so existing call sites are unchanged.
- `PageRenderer.bake / bakeBlocking(…, jpegQuality = ImageStore.JPEG_QUALITY)`.
- `DocScannerTheme(+ accent: AccentColor = AccentColor.BLUE)`.
- `DashboardScreen(+ onNavigateToSettings: () -> Unit)`.
- `ScannerRepository(+ settings: StateFlow<AppSettings>, + updateSettings(),
  + currentSettings())`.

### Manifest
`WRITE_EXTERNAL_STORAGE` with `maxSdkVersion="28"`, requested from the
auto-save row only, on Android 8/9 only.

### Version
**1.4 (code 5)**.

## 2026-10-25 (g) — Arena.ai — Shared-axis screen transitions

- `Navigation.kt`: `NavDisplay` was using its default transition, which reads as a
  hard cut. It now gets explicit specs:
  - `transitionSpec` (push): incoming screen slides in from the trailing edge at
    full width while the outgoing screen slides a **quarter** of the way out —
    the parallax is what turns a screen swap into a sense of depth.
  - `popTransitionSpec` (back): the exact reverse, so the screen you came from
    settles back where you left it.
  - `predictivePopTransitionSpec`: same as pop, so the edge-swipe preview during a
    predictive back gesture matches where letting go actually lands.
- Duration **320 ms**, `FastOutSlowInEasing` — Material's standard easing.
- Implemented with top-level builders only (`slideInHorizontally`,
  `slideOutHorizontally`, `togetherWith`), never `slideIntoContainer`, so the code
  does not depend on the type of scope Navigation 3 hands the spec.
- Transition specs live in `private fun navForwardTransition()` /
  `navBackTransition()`; there are no per-destination overrides yet, so every
  route shares the same motion.

## 2026-10-25 (f) — Arena.ai — Share button no longer grows

- `ui/detail/DocumentDetailScreen.kt` bottom bar: the four actions were
  `TextButton`s (icon + label side by side) plus one filled `Button` in a
  `SpaceAround` row. On a 360 dp phone the row runs out of width at the **last**
  child, so "Share" was measured with almost no width, wrapped its label onto a
  second line, and the pill grew taller than the three actions beside it.
- Rebuilt as four `Modifier.weight(1f)` cells, icon above label, every cell at a
  shared **56 dp** height (`ACTION_HEIGHT`), label `maxLines = 1`,
  `softWrap = false`, ellipsis. Width per cell now depends only on the screen
  width, never on the label length, so no child can be squeezed again.
- New private composables `DocumentActionButton` / `DocumentActionContent`;
  `Share` keeps its filled primary treatment, the other three stay text buttons.

## 2026-10-25 (e) — Arena.ai — Zoom while capturing and while adjusting borders

### Camera: pinch-to-zoom that actually reaches the JPEG
- New `Box` around `AndroidView` in `ui/camera/ScannerCameraScreen.kt` with
  `detectTransformGestures(panZoomLock = true)`; pinch multiplies `zoomRatio`.
- Zoom is applied through **`cameraControl.setZoomRatio`**, i.e. a sensor crop —
  not a scaled preview `View`. That is the difference between "the viewfinder
  looks zoomed" and "the saved photo is zoomed".
- `minZoomRatio` / `maxZoomRatio` come from `camera.cameraInfo.zoomState`, which
  CameraX fills in asynchronously after binding, so a short polling
  `LaunchedEffect` reads them instead of assuming the device cannot zoom.
  Everything is clamped to that range; the pinch handler and buttons both honour it.
- Discoverable control: a translucent pill above the shutter with `−`, `1.0×`,
  `+` and a reset button. Hidden entirely when `maxZoomRatio <= 1.01f`, so
  fixed-focus devices see no dead UI. Tapping the factor label resets to 1×.
- `ZOOM_STEP = 0.5f`.

### Editor: pinch / pan / zoom in border-adjust and filter preview
- `ui/editor/CropAndFilterScreen.kt` now renders through two nested `Box`es:
  outer = `scaleX/scaleY` + `translationX/translationY`, inner = `rotationZ`.
  Zoom therefore works with a rotated page and panning always follows the finger.
- One unified `awaitEachGesture` handler replaced `detectDragGestures`:
  - one finger started on a corner handle (within `GRAB_THRESHOLD`, normalised)
    moves that corner, exactly as before;
  - one finger anywhere else pans once `zoom > 1`;
  - two fingers pinch around `calculateCentroid(useCurrent = false)`, so the point
    between the fingers stays put while scaling.
  - once a gesture becomes multi-touch it never falls back to corner dragging,
    so lifting one finger out of a pinch cannot fling a corner across the page.
- `screenToCanvas()` inverts translation → scale → rotation, so a finger maps to
  the right normalised corner coordinate at any zoom and rotation.
- Pan is clamped to `(zoom − 1) · extent / 2`, with the extents swapped when the
  rotation is a multiple of 90°, so the page can never be dragged off-screen.
- Corner handles and the quad stroke are divided by the zoom factor while
  drawing, so they keep a constant on-screen size instead of ballooning at 5×.
- Buttons: `−` / percentage / `+` / `FitScreen` reset at the bottom centre;
  `MAX_ZOOM = 5f`, `ZOOM_FACTOR = 1.6f`.
- Zoom and pan reset when the page changes — no bleed between pages.

### Coordination notes for Antigravity
- Only two files changed: `ui/camera/ScannerCameraScreen.kt` and
  `ui/editor/CropAndFilterScreen.kt`. Do not re-run `detectDragGestures` there.
- If a device reports `maxZoomRatio` but pinching does nothing, the effect that
  calls `setZoomRatio` is keyed on `zoomRatio`; check that `boundCamera` is set
  (it is assigned inside the CameraX bind callback).

## 2026-10-25 (d) — Arena.ai — Punchy filters, working detection, clean viewfinder

Follow-up to user testing: *"the magic color and black and white still seems a bit
off… the border detection is not working and also remove the static border on the
camera when capturing photos."*

### 🐞 Why detection *looked* like it never ran
`CropAndFilterScreen` only drew the quad when `isCropMode` was true, and
`isCropMode` started `false` in **review mode** — the post-capture screen the user
was judging. Detection could succeed and the borders would still be invisible
until "Borders" was tapped. The screen now opens in Borders mode in review mode.

### Detector: boundary scan replaces extreme points
`DocumentDetector` rewritten **again** (see the file header for the three
generations). The previous Sobel→extreme-point version failed on the most common
capture: the page fills the frame, so (a) Sobel skipped the 1-px frame border
where the page edge lived, and (b) interior text edges outnumbered the page edge,
so the extreme points landed on text → invalid quad → silent `FULL_FRAME`.

Now:
1. Sobel with **clamped indices**, so an edge on the frame border still produces a
   gradient.
2. Lower, percentile-based threshold (`max(18, p75×0.85, p97×0.18)`).
3. **Boundary scan** — per row, first strong edge from the left and last from the
   right; per column, first from the top and last from the bottom. Scanning inward
   from the frame edge hits the paper boundary before any text.
4. **RANSAC line fit per side** (220 deterministic iterations, 1.2 % tolerance,
   ≥14 inliers) then least-squares refit on inliers — survives curled corners and
   stray shadows.
5. Intersect the four lines. **A side with no visible boundary falls back to the
   frame edge** (the page runs past the frame), which is geometrically correct
   rather than a failure.
6. Validate slopes (|a| ≤ 1.1), convexity, bounds, side lengths, area.
7. Extreme-point quad retained as a fallback for small/rotated documents.

New `DocumentDetector.detect()` returns `Detection(corners, confident)`, where
`confident` is true only when ≥3 sides were fitted from real edges. The editor
shows *"Borders not detected — drag the corners"* when it is false.

### Filters: colour cast + blow-out fixed, tuned punchier
- **One stretch for all channels.** Magic Color previously stretched R, G and B
  independently with different percentiles — three different gains on the same
  paper pixel = pink/green whites. Contrast now comes from a single **luminance**
  histogram; only white balance is per-channel.
- **Clamped stretch points** (`stretchPoints`, new and unit-tested): 2 % black
  point capped at 110, 90 % white point floored at 205. An unbounded black point
  was the "washed out on a lightly printed page" bug — with little ink the
  percentile landed inside the paper mass and the gain blew mid-tones to white.
- Punchier: `MAGIC_COLOR_CURVE 0.55 → 0.62`, saturation `1.18 → 1.15` (more
  contrast, less chroma push), `GRAYSCALE_CURVE 0.35 → 0.42`.
- **B&W despeckle.** The symmetric ramp left paper noise just above the threshold
  pure black → speckle. Now `INK_CUT_FACTOR 0.95` / `PAPER_CUT_FACTOR 1.05`
  (asymmetric, paper whites out early) plus a **3×3 majority vote** read straight
  from the luminance buffer (no extra full-frame allocation): ≥6 paper neighbours
  → white, ≤3 → black, otherwise keep the ramped value so glyph edges stay
  anti-aliased.

### Camera: guide frame removed
Both decorative overlays are gone from `ScannerCameraScreen` — the dark scrim with
the cut-out window and the pulsing dashed frame — along with 12 now-unused imports.
They were never part of the captured JPEG, and they were actively harmful: they
taught users to fill the frame right past the paper edge, leaving no boundary for
the detector to find.

### Signature changes
| Symbol | Before | After |
| :--- | :--- | :--- |
| `DocumentDetector.detect` | — | **new** `(Bitmap) -> Detection` |
| `DocumentDetector.Detection` | — | **new** `data class(corners, confident)` |
| `DocumentDetector.detectQuad` | `(Bitmap) -> QuadCorners` | unchanged signature, now delegates to `detect().corners` |
| `DocumentFilters.stretchPoints` | — | **new** internal, unit-tested |
| `CropAndFilterScreen` `isCropMode` | started `false` | **starts `reviewMode`** |

### Tests
`DocumentFiltersTest` gains four cases for `stretchPoints`: normal page, the
barely-printed clamp, the minimum-range guard, and the dark-photo floor.

`versionCode 4`, `versionName 1.3`.

---

## 2026-10-25 (d) — Antigravity — Filters test fix & build verified

- Fixed `DocumentFiltersTest.curveLut_darkensMidtones`: updated assertion to verify shadow/ink darkening (`lut[96] < 96`) rather than the midpoint index 128 (since standard smoothstep $3t^2 - 2t^3$ has inflection at $t=0.5$, meaning $t = 128/255 > 0.5$ preserves midpoint while pulling down values $t < 0.5$).
- Verified all 13 unit tests passed (`testDebugUnitTest` 13/13).
- Built and verified APK (`app-debug.apk`, versionCode 3, versionName 1.2).

## 2026-10-25 (c) — Arena.ai — Real filters + real border detection


### Why
The user reported: *"the filters effect are not good at all, make them perfect like
the original app, also the auto detection of pages and border."* Both were
algorithmic gaps, not tuning.

### Filters — replaced a global `ColorMatrix` with a pixel pipeline
`ImageProcessingUtils.getColorMatrix()` applied five fixed matrices (e.g. *Magic
Color* = contrast ×1.35 + brightness +15, *Clear B&W* = desaturate + contrast ×2.4
− 140). A global matrix **cannot**:
- remove shadows or a torch hotspot, because the correction has to vary spatially;
- white-balance, because indoor light is yellow and shade is blue;
- avoid clipping, because ×2.4 contrast maps a mid-grey page to 0 or 255.

New `util/DocumentFilters.kt` per filter:
1. **Illumination normalisation** — paper brightness is estimated with a
   block-**max** (dilation) pyramid (max rejects ink, keeps paper), blurred, and
   sampled bilinearly; dividing by it maps every patch of paper to the same white.
2. **Grey-world white balance** on the normalised result (clamped to ±20 % so a
   genuinely tinted page is not neutralised).
3. **Percentile stretch** 0.4 %→99.6 % per channel instead of a fixed multiplier.
4. **Saturation boost** (1.18) around Rec.601 luminance for *Magic Color*.
5. **Smoothstep S-curve** to deepen ink while pinning paper at 255.
6. *Clear B&W* → **Otsu threshold** over the normalised luminance with a narrow
   ramp, so glyph edges stay anti-aliased instead of jagged.
7. *Lighten* → partial normalisation (72 %) that keeps some shading for photos and
   tinted paper.

Processing is capped at `MAX_PROCESS_DIMENSION = 2000` px on the long edge
(~240 DPI A4), which keeps a bake to a few hundred ms and bounds memory.
`android:largeHeap="true"` was added: the pipeline holds an ARGB `IntArray` plus a
luminance `FloatArray` for the whole page (~2 × 4 bytes × pixels).

### Editor preview is now WYSIWYG
The editor used `ImageProcessingUtils.getComposeColorFilter()` — a `ColorMatrix`
`ColorFilter` that looked **nothing like** the baked output. It now renders the
real filter via `DocumentFilters.applyPreview()` at 900 px, with a 2 dp progress
bar while it computes. Preview and PDF therefore match.

### Border detection — replaced a brightness bounding box with an edge quad
`PerspectiveWarp.detectQuad()` sampled a 64×48 grid and returned the axis-aligned
box of "bright enough" cells. That cannot express perspective (it cuts off two
corners of an angled page and keeps desk at the other two), keys on absolute
brightness (a white desk defeats it), and returned `FULL_FRAME` whenever the page
filled the frame.

New `util/DocumentDetector.kt`:
1. Downscale to 360 px, blur, **Sobel** gradients.
2. Percentile edge threshold (`sorted.p90 × 0.55`, floored at 26).
3. **Extreme-point quad** — TL minimises `x+y`, BR maximises it, TR maximises
   `x−y`, BL minimises it. Adapts to perspective and ignores interior text edges.
4. **Robust least-squares line per side** (only points 10–90 % along that side,
   within a 5.5 % band), with one MAD-based outlier rejection pass and refit.
5. **Intersect adjacent lines** for well-conditioned corners.
6. Validate convexity, bounds, side lengths and area (12 %–99.5 %); anything
   doubtful returns `FULL_FRAME`. Never throws.

### Signature changes
| Symbol | Before | After |
| :--- | :--- | :--- |
| `ImageProcessingUtils.getColorMatrix` | returned `ColorMatrix?` | **removed** — use `DocumentFilters.apply` |
| `ImageProcessingUtils.getComposeColorFilter` | returned Compose `ColorFilter?` | **removed** — editor previews a real bitmap |
| `ImageProcessingUtils.applyFilter` | — | **new** `(src, filterType, maxDimension)` |
| `PerspectiveWarp.detectQuad` | 120-line brightness heuristic | delegates to `DocumentDetector.detectQuad`; same signature |
| `DocumentFilters.apply` | — | **new** — returns `src` itself for ORIGINAL/failure |
| `DocumentFilters.applyPreview` | — | **new** |
| `DocumentDetector.detectQuad` | — | **new** |

### New files
| File | Purpose |
| :--- | :--- |
| `util/DocumentFilters.kt` | Illumination model, white balance, stretch, Otsu, tone curve |
| `util/DocumentDetector.kt` | Sobel + extreme-point quad + robust line fit + validation |
| `app/src/test/.../util/DocumentFiltersTest.kt` | JVM tests for `curveLut`, `percentile`, `otsuThreshold` |

### Version
`versionCode 3`, `versionName 1.2`.

---

## 2026-10-25 (c) — Antigravity — TimeZone hermetic test fix & build verified

- Added `timeZone: TimeZone = TimeZone.getDefault()` to `DocumentNaming.defaultScanTitle()` so formatters default to local device time while unit tests can explicitly assert under `UTC`.
- Fixed `DocumentNamingTest.defaultScanTitle_quotesLiteralText` which previously failed in non-UTC runner environments.
- Build & test pass verified: `./gradlew assembleDebug testDebugUnitTest` passed in 20s. `app-debug.apk` built successfully.

## 2026-10-25 (b) — Arena.ai — Crash fix + post-capture review flow


### 🐞 Root cause of the "press Done → crash" bug
`ScannerCameraScreen.commitScan()` built a new document's title with

```kotlin
SimpleDateFormat("Scan yyyy-MM-dd HH:mm", Locale.getDefault())
```

`"Scan yyyy-MM-dd HH:mm"` is a **pattern**, not a literal. `S`, `a`, `d`, `y`,
`H`, `m` are legal pattern letters, but **`c` and `n` are not**, so the
constructor threw `IllegalArgumentException: Illegal pattern character 'c'`.
It fired at exactly one moment: **Done on a brand-new document**, because the
`targetDocId != null` branch (adding pages to an existing document) never built a
title. That is why capture worked, the counter advanced, and only *Done* crashed.

Fixed by never inlining a pattern again: `util/DocumentNaming.kt` owns the
pattern (`"'Scan' yyyy-MM-dd HH-mm-ss"` — literal quoted, `-` instead of `:` so
the title is filename-safe) and falls back instead of throwing.
`DocumentNamingTest` locks the behaviour, including a test that asserts the *old*
pattern still throws, so nobody "simplifies" the quotes away.

> **Rule for all agents:** any `SimpleDateFormat` pattern containing a literal
> word must single-quote it. Put patterns in `DocumentNaming`, not in screens.

### ✨ New: post-capture review screen (the flow the original app had)
After *Done*, the camera now opens the editor in **review mode** instead of
jumping to the document:

1. Borders are **auto-detected** on entry (`PerspectiveWarp.detectQuad`).
2. Four draggable corners, live filter preview, 90° rotation.
3. **Save & Next** bakes the page and advances to the next captured page;
   the last page becomes **Finish** and lands on the document screen.

### Signature changes
| Symbol | Before | After |
| :--- | :--- | :--- |
| `CropAndFilterScreen(...)` | `(docId, pageId, repository, onDone)` | **added `reviewMode: Boolean = false`** (last param) |
| `CropAndFilterNav` | `(docId, pageId)` | **`(docId, pageId, review: Boolean = false)`** |
| `PerspectiveWarp.detectQuad` | fell back to a 6 % inset | **falls back to `FULL_FRAME`** — never silently crops when detection is inconclusive |
| `DocumentGridCard` / `DocumentListCard` | `(document, onClick, onDelete)` | **added `onMoveToFolder: () -> Unit`** |

### New files
| File | Purpose |
| :--- | :--- |
| `util/DocumentNaming.kt` | Single owner of title patterns; quotes literals, never throws |
| `app/src/test/.../util/DocumentNamingTest.kt` | First real unit test in the repo; guards the crash pattern |

### Other fixes in this pass
- **"Move to folder" now exists.** `ScannerRepository.moveDocumentToFolder` was
  implemented but unreachable from the UI, so folder chips could only ever filter
  seeded data. Both dashboard card menus have *Move to folder…* +
  `MoveToFolderDialog`.
- **Notes are editable.** `Document.notes` rendered in a banner but nothing could
  set it. The rename dialog is now *Edit document* (title + notes).
- **Duplicate back-stack key removed.** Adding a page to an existing document
  produced `[…, DocumentDetailNav(d), DocumentDetailNav(d)]`; the camera's finish
  handler now reaches the review screen and `addDetailIfNeeded()` prevents a
  second entry with the same key.
- `versionCode 1 → 2`, `versionName 1.0 → 1.1` so a device can be checked for the
  right build.

---

## 2026-10-25 — Antigravity — Build verified & daemon memory configured

- Verified compilation and built `app-debug.apk` (`./gradlew assembleDebug` SUCCESS in 1m 17s).
- Added `kotlin.daemon.jvmargs=-Xmx2048m` and bounded Gradle daemon metaspace to 512m in `gradle.properties` to prevent JVM native allocation failure during Compose + Kotlin 2.3.20 compilation on Windows.
- All new dependencies (CameraX 1.4.2, kotlinx-serialization-json 1.9.0) resolved and compiled cleanly without version bumps needed.

## 2026-10-25 — Arena.ai — "Make it real" pass


**Why:** the app was a high-fidelity UI shell. No code path ever read, wrote or
displayed a real image. Camera, PDF pages, thumbnails and the viewer were all
painted placeholders, and nothing survived process death. Details are in the
Arena audit; this entry records what actually moved.

### New dependencies
- `androidx.camera:camera-core / camera-camera2 / camera-lifecycle / camera-view:1.4.2`
  → real capture in `ui/camera/ScannerCameraScreen.kt`.
- `org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0` → explicit now that
  `LibraryStore` serialises the library itself.
- `androidx.exifinterface` **not** added: `android.media.ExifInterface(InputStream)`
  covers API 24+, and minSdk is now 26.

### New files
| File | Purpose |
| :--- | :--- |
| `util/ImageStore.kt` | Canonical file layout: `filesDir/pages/<pageId>.jpg`, `filesDir/signatures/<id>.png`, `cacheDir/exports/`. Import, orient, downscale, delete. |
| `util/PageImageLoader.kt` | Sampled bitmap decode + `LruCache` + `rememberPageImage()` composable. Cache key includes file mtime. |
| `util/PerspectiveWarp.kt` | Real 4-point projective warp via `Matrix.setPolyToPoly`. Replaces the decorative crop quad. |
| `data/LibraryStore.kt` | `library.json` snapshot persistence, atomic tmp+rename, 150 ms coalescing writer. |
| `ui/common/PageThumbnail.kt` | Shared thumbnail composable with icon fallback. |
| `app/proguard-rules.pro` | **Was referenced by `app/build.gradle.kts` but missing — `assembleRelease` could not run.** |
| `app/src/main/res/xml/file_paths.xml` | Backs the new `FileProvider`. |
| `docs/AGENT_COORDINATION.md` | The Arena ⟷ Antigravity contract. |

### Signature changes (update your call sites)
| Symbol | Before | After |
| :--- | :--- | :--- |
| `ScannerRepository` | `ScannerRepository()` + `ScannerRepository.instance` | **`ScannerRepository(context: Context)`** — the `instance` singleton is **gone** |
| `ScannerRepository.updatePage` | `(docId, pageId, update)` | unchanged |
| `ScannerRepository.movePage` | — | **new** `(docId: String, fromIndex: Int, toIndex: Int)` |
| `ImageProcessingUtils.applyEnhancements` | `(src, filterType, rotationDegrees)` | **`(src, filterType, rotationDegrees, corners: QuadCorners? = null)`** |
| `PdfExportConfig` | had `qualityPercent`, `password` | **both removed** — `PdfDocument` cannot encrypt, and nothing consumed either |
| `SignaturePadDialog.onSignatureConfirmed` | received `"signature_<ts>"` token | receives a **PNG absolute path** |
| `MainNavigation()` | no params | **`MainNavigation(repository: ScannerRepository)`** |
| `PaperSize` | had `RECEIPT`, `BUSINESS_CARD(240,144)` | **added `EXECUTIVE(522,756)`**, `BUSINESS_CARD` corrected to `241×153`, `RECEIPT` kept → 8 presets |
| `QuadCorners()` | defaulted to a 5 % inset | defaults to **full frame** `(0,0)-(1,1)`; the old inset silently cropped every page |

### Deleted files
| File | Why | To restore |
| :--- | :--- | :--- |
| `ui/main/MainScreen.kt` | Unreachable Android-studio starter template. `MainActivity` → `MainNavigation` never referenced it. | `git log --diff-filter=D -- ui/main/MainScreen.kt` |
| `ui/main/MainScreenViewModel.kt` | Same; also introduced the forbidden `DataRepository` singleton pattern. | " |
| `data/DataRepository.kt` | Same. A generic `Flow<List<String>>` unrelated to scanned documents. | " |
| `app/src/test/.../MainScreenViewModelTest.kt` | Tested the deleted template. | " |
| `app/src/androidTest/.../MainScreenTest.kt` | Tested the deleted template. | " |

> The repo currently has **no automated tests**. The next quality task is a real
> unit-test suite for `LibraryStore` round-tripping and `ScannerRepository`
> mutations. Don't let this stay empty.

### Behaviour changes
- `ScannerRepository` starts **empty** instead of seeding three fake documents
  (`mock_contract_p1` etc.). The existing `EmptyStateView` is now reachable and
  correct. Default folders (Work / Tax / Personal) are still created on a fresh
  install.
- Editing a page is now **non-destructive**: the pristine JPEG stays in
  `ImageStore.pageFile()`, the bake lands in `<pageId>_edited.jpg`, and
  `originalImageUri` preserves the source so re-editing never compounds quality
  loss.
- PDF export runs on `Dispatchers.IO` and embeds **real page bitmaps** through
  `ImageStore`. `createSamplePageBitmap()` is gone.
- `FileProvider` is declared in the manifest, so sharing actually works instead
  of always falling into the `catch` Toast.
- `DocScannerTheme(dynamicColor = false)` by default — the Material 3 Expressive
  palette in `theme/Color.kt` is now actually used on API 31+.

### Documentation corrected to match code
- minSdk **24 → 26** (code + docs now agree, docs were right).
- Kotlin documented as **2.3.20** (`libs.versions.toml` is the single source of
  truth; `AGENTS.md` and `README.md` said 2.2.0).
- "Page reordering: Live on Device" was false — now actually implemented.
- Paper-size table in `README.md` now lists all 8 presets with real dimensions.

### Coordination notes for Antigravity
- `ScannerRepository` is constructed in `MainActivity` and passed to
  `MainNavigation(repository)`. **Do not add a static/singleton accessor back** —
  `AGENTS.md` §2.6 forbids it and it is what made the old code untestable.
- Any new screen must accept `repository: ScannerRepository` as a parameter.
- If `assembleDebug` fails on a CameraX or serialization version, bump the
  version in `gradle/libs.versions.toml` only — Arena guessed those version
  numbers because it has no Android SDK and could not resolve them.
