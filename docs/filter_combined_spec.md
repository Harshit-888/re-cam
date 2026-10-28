# The combined filter: `SMART` and `LIGHT_TEXT`

Round 61. Two new presets, built out of what already worked in this app plus the one
idea from Adobe Scan's ladder that this app was missing. They are **additive**: the
five original presets still run the same transcribed code, byte for byte.

* Kotlin — `DocumentFilters.applyAdaptive` (dispatch), `adaptiveColourReference`
  (the reference implementation and the definition of the filter).
* Rust — `kernels::adaptive_colour`, `kernels::luma_plane`, ABI 3,
  `NativeFilters.nativeAdaptiveColour` / `nativeLumaPlane`.
* Specification in executable form — `tools/filter_ref.py`. Every number in this
  document is printed by it.
* Tests — `DocumentFiltersTest.adaptive_*` and `.surfaceGate_*` / `median3Grid_*`
  (JVM, no device), `kernels.rs` round-61 tests (Rust), and
  `DocumentFilters.verifyNativeFilters` cases 10–11 (on device, byte-for-byte parity
  between the two implementations before either is trusted).
* Estimator and gate evidence — `tools/shadow_probe.py`, which ports the paper-map
  pipeline and the classifier so both can be measured without a device.

## 1. Why a new filter at all

`docs/FILTER_COMPARISON_ADOBE_VS_DOC_SCANNER.md` measures the gap. Two findings
drove this work:

1. **This app's filters are blind to faint ink.** Magic Color divides the page by an
   estimated paper surface, which is the right illumination model, but it then
   indexes its tone curve by the *absolute* difference from paper. A mark 18 levels
   below paper — a pencil line, a thermal receipt header, a fax at 92 % reflectance
   — maps to 248. One step off white. Clear B&W reaches it only because it
   binarises, which destroys everything else in the frame. Adobe ships a whole
   preset (`Light Text`) for this case; the transcribed pipeline had no equivalent.
2. **Each channel is clipped independently.** Every channel gets its own divide and
   its own curve, so a blue pen (60, 80, 160) comes out (0, 0, 78): red and green
   are driven to zero and the mark reads as saturated navy. Nothing in the pipeline
   preserves hue.

Neither is a bug in the transcription — the original APK behaves this way. It is a
limit of the design.

## 2. What was reused, and what was added

Reused unchanged:

* the illumination model — `buildPaperMap`: mean pool → dilate/erode closing, per
  channel, then a bilinear sample at every pixel;
* the edge pair — 3×3 median of the luma plane, then the Laplacian — as an
  edge-strength estimate, exactly as the level-1 blend uses them;
* the Rust/JNI arrangement: Kotlin is the definition, Rust is the fast path, and a
  one-off self-test refuses the native library rather than trusting it.

Added:

* a **soft knee** on the white-balance gain, so a dark frame or a scene cannot be
  lifted to white (Adobe's "don't repaint a photo as a page" behaviour);
* a tone curve indexed by the **ratio** `pixel / paper` instead of by the difference;
* **one** scale per pixel applied to all three channels, with chroma restored around
  the pixel's own luma afterwards;
* fixed-point arithmetic throughout, so the two implementations can be compared for
  equality rather than for closeness;
* **an edge-preserving surface** (Round 62): two 3×3 medians of the grid instead of
  three box passes, so a hard shadow stays a step in the estimate;
* **a relative document/scene gate** (Round 62): two fractions measured against the
  surface at each pixel, replacing an absolute-threshold classifier that called a
  shadowed page a scene.

## 3. The pipeline

Before the per-pixel work, the surface is estimated once per frame:

| stage | what it does | why that way |
|---|---|---|
| mean pool | the frame into `cellRadius(w,h)+1` px cells, per channel | the surface is low-frequency by construction |
| closing | dilate then erode, radius 2.5 % of the grid | fills ink, staples, folds with the paper level around them |
| **median ×2** | two 3×3 medians of the grid | removes the same bad cells a blur would, **without smearing a shadow edge** — the R61 box blur left a three-cell ramp in the estimate, i.e. a grey band alongside every crisp shadow. Measured, band error: box×3 **24.2 / 112** vs median×2 **0.3 / 1** |
| gate | `surfaceIsPaper`: two ratios per sampled pixel | see below |

Per pixel, all Q8 (256 == 1.0), in this order:

| # | Step | Expression | Why |
|---|---|---|---|
| 1 | paper | `sample_q8(grid)` | The local paper level; bilinear between grid cells, so the correction is smooth and cannot show the grid. |
| 2 | gain | `knee(255·256·256 / paper, knee, cap)` | White balance + shadow removal. The knee is monotone with slope 1 at the knee and never reaches the cap, so no contour can form where the limit engages. |
| 3 | normalised | `c2 = round(c · gain)` | Paper becomes 255 in every channel — this is the step that makes the filter illumination-invariant. |
| 4 | luma | `Rec.601(c2)`, integer | One scalar to index the curve with. |
| 5 | curve | `outL = curve[luma]` | The preset's tone curve, and the only place the presets differ in *shape*. |
| 6 | scale | `g = max(floor, outL·256 / luma)` | **One** gain for R, G and B. A single scale cannot invent a colour, which is what keeps stamps red and pens blue. `floor` stops saturated ink from being crushed to black. |
| 7 | crispening | `g · (1 + detail/cap)` on documents only | Up to +22 % local contrast where the Laplacian is strong. Off for `LIGHT_TEXT` (faint originals are grainy) and off for scenes. |
| 8 | ink | `sc = round(c2 · g)` | |
| 9 | chroma | `out = grey + (sc − grey)·chroma`, `grey = Rec.601(sc)` | Re-expands (or, for a scene, leaves alone) saturation around the pixel's own luma. |

Steps 1–3 are the illumination model this app already had. Steps 4–9 are the part
that replaces "per-channel curve on the absolute difference".

### Constants

`DocumentFilters` owns them; `tools/filter_ref.py` mirrors them.

| Name | Value | Meaning |
|---|---|---|
| `smartCurve` | `ratioCurve(0.60, 0.98, 1.6)` | Below 60 % of paper is ink; at 98 % of paper is paper; `gamma` 1.6 is the mid-tone contrast. |
| `lightTextCurve` | `ratioCurve(0.78, 0.99, 1.5)` | A narrower, higher window, so a document whose *paper* is grey still reads as paper while grey ink still separates. |
| `sceneCurve` | `ratioCurve(0.02, 0.99, 1.0)` | Near-identity: a photograph is white-balanced and nothing else. |
| `GAIN_KNEE_DOCUMENT` / `GAIN_CAP_DOCUMENT` | 8 / 24 (2048 / 6144 Q8) | Documents: gain may reach 8× before the knee, asymptote 24×. |
| `GAIN_KNEE_SCENE` / `GAIN_CAP_SCENE` | 1.2 / 2.0 (307 / 512 Q8) | Scenes: at most 2×, so a dark room is not repainted white. |
| `ADAPTIVE_FLOOR_SMART` | 56 (0.22) | Lowest scale a document pixel may be multiplied by. |
| `ADAPTIVE_FLOOR_LIGHT` | 64 (0.25) | Ditto, a little higher: faint documents are noisier. |
| `ADAPTIVE_FLOOR_SCENE` | 128 (0.50) | A scene's blacks must stay where they are. |
| `ADAPTIVE_CHROMA_SMART` | 287 (1.12×) | Saturation restored after the crush. |
| `ADAPTIVE_CHROMA_LIGHT` | 307 (1.20×) | Faint colour marks need more. |
| `ADAPTIVE_CHROMA_SCENE` | 256 (1.0×) | Untouched. |
| `ADAPTIVE_DETAIL_SMART` | 56 (up to +22 %) | Edge crispening. |
| `ADAPTIVE_DETAIL_SCALE` | 50 | Divider for the Laplacian, the same one the level-1 blend uses. |
| `PAPER_MEDIAN_PASSES` | 2 | Median passes over the grid (adaptive filters only; the five transcribed presets keep their box blur). |
| `SURFACE_BRIGHT_PERCENT` | 3 % | Above this fraction of pixels brighter than their own surface, a frame is a scene. |
| `SURFACE_MARKED_PERCENT` | 8 % | Above this fraction of local marks (below ¾ of the surface), it is a page regardless. |

### The gate

`surfaceIsPaper` asks one question — *is this frame a lit flat surface?* — twice, in
the divisor's own units:

| statistic | page | photograph | why |
|---|---|---|---|
| brighter than the surface (>1.05×) | 0.0 – 1.8 % | 4.2 – 5.3 % | paper is the ceiling of a page; in a photo the estimate sits *between* light and dark, so every content boundary contributes |
| local marks (<0.75×) | 10.7 – 10.9 % | 1.2 – 3.7 % | text and ruling are marks on a surface; a scene's dark regions are their own surface |

A frame is a page if the first is under 3 % **or** the second is over 8 %: the first
test is the definition, the second rescues a page whose specular reflection is the one
genuinely brighter thing it can contain. Both are ratios, so both are immune to
exposure and to a shadow — the same page under a 3× hand shadow measures 1.4 %, exactly
as under even light. Thresholds and measurements: `python3 tools/shadow_probe.py`.

This replaced `classifiesAsDocument` in Round 62. That function applies *absolute*
thresholds to the whole frame, so a page with a 3× shadow scored 41 % and flipped to
scene — and then no shadow was removed at all — while a lit room with a beige wall
scored 61 % and was treated as a page. Both are reproduced in the probe.

## 4. Measured behaviour

Generated by `python3 tools/filter_ref.py all`.

### 4.1 Tone: what ink becomes what grey (paper 238)

| mark | refl. | SMART | LIGHT_TEXT | SCENE |
|---|---|---|---|---|
| 255 | 1.07 | 255 | 255 | 255 |
| 247 | 1.04 | 255 | 255 | 255 |
| 238 | 1.00 | 255 | 255 | 255 |
| 234 | 0.98 | 255 | 237 | 252 |
| **219** | **0.92** | **190** | **134** | **236** |
| 200 | 0.84 | 121 | 54 | 215 |
| 176 | 0.74 | 50 | 47 | 189 |
| 141 | 0.59 | 33 | 38 | 150 |
| 131 | 0.55 | 31 | 35 | 139 |
| 76 | 0.32 | 18 | 20 | 78 |
| 0 | 0.00 | 0 | 0 | 0 |

The bold row is the acceptance criterion: **92 % reflectance goes from 248 (Magic
Color) to 190 under `SMART`**, while 238 paper goes to exactly 255. Clear B&W can
only reach 231 there by turning the whole frame into two levels.

### 4.2 Illumination invariance: same ink, three paper levels

| ink | paper 238 | paper 150 | paper 90 | spread |
|---|---|---|---|---|
| **SMART** | | | | |
| 0.98 | 255 | 255 | 251 | 4 |
| 0.92 | 190 | 190 | 195 | 5 |
| 0.74 | 50 | 52 | 55 | 5 |
| 0.59 | 33 | 33 | 33 | 0 |
| 0.32 | 18 | 18 | 18 | 0 |
| **LIGHT_TEXT** | | | | |
| 0.98 | 237 | 237 | 231 | 6 |
| 0.92 | 134 | 134 | 140 | 6 |
| 0.74 | 47 | 47 | 48 | 1 |
| 0.59 | 38 | 38 | 38 | 0 |
| 0.32 | 20 | 21 | 21 | 1 |

The divide stage is exactly invariant; the residual spread above 74 % comes from
the curve edge, where the ratio sits within a level of the white point and the Q8
gain's own rounding decides the last bit. (The Rust test avoids that region and
asserts *exact* equality, using a paper pair whose gains are exactly 2:1.) For
comparison, the transcribed filter drifts 141 → 168 for the same pencil under a
shadow — 27 levels.

### 4.3 Colour: one gain, not three clips

| mark | luma | SMART | LIGHT_TEXT |
|---|---|---|---|
| blue pen (60, 80, 160) | 83 | (13, 18, 39) | (14, 21, 46) |
| red stamp (200, 60, 60) | 102 | (49, 12, 12) | (59, 13, 13) |
| yellow highlighter (240, 220, 120) | 215 | (194, 177, 87) | (121, 109, 50) |
| green signature (80, 150, 90) | 122 | (17, 35, 20) | (19, 41, 22) |

Against the transcribed filter's (0, 0, 78) for the same blue pen: the hue survives
because all three channels were scaled by the same number.

### 4.4 Shadow removal: paper must come out 255 (Round 62)

Measured with `python3 tools/shadow_probe.py` — the whole estimator, on synthetic
frames at 900 × 675 (`cell` 15, grid 60 × 45). Error is in **output levels on paper**,
so 0 is perfect and 123 is a shadow you can see.

| frame | R61 (absolute gate, box×3) | R62 (relative gate, median×2) |
|---|---|---|
| flat page | 1.0 / 1 | 1.0 / 1 |
| page, soft gradient (236 → 94) | 0.2 / 9 | 0.1 / 8 |
| page, 3.2× hard shadow: **shadowed half** | **123.0 / 123** | **0.0 / 0** |
| page, 3.2× hard shadow: band at the edge | 65.2 / 138 | **0.3 / 1** |
| page, 8× hard shadow: shadowed half | 197.0 / 197 | **0.0 / 0** |
| page, 8× hard shadow: band at the edge | 102.0 / 204 | **0.3 / 1** |
| page, corner falloff to 45 % | 1.2 / 28 | 1.4 / 26 |
| page, lamp glare + shadow (whole frame) | 46.8 / 139 | **0.7 / 73** |

`mean / worst`. The two failures R61 had, both fixed:

1. **The gate.** A page with a 3.2× hand shadow scored 41 % on the absolute-threshold
   classifier, so the whole frame was treated as a scene: gain capped at 2×, and the
   shadowed half stayed 123 levels grey. The relative gate reads 1.4 % on the same
   frame and treats it as a page.
2. **The smoothing.** A box blur turns the shadow edge into a three-cell ramp in the
   surface, so the divide under-corrects along it: 65 levels of grey in a band the
   width of a finger. A median reproduces the step: 0.3 levels.

## 5. Decisions, including the ones that were reversed

**The mid-tone worry was wrong.** An earlier draft flagged the 74 % → 50 / 55 % → 31
compression as a possible "two-tone pencil" artefact and proposed `gamma` 1.35. It
is not an artefact, and `gamma` stays at 1.6:

* the mapping is monotone and *expanding* (local slope ≈ 2.8× at 50 % of paper), so
  no input levels are merged — the usual cause of posterisation;
* the flat region below the curve's black point is not flat: the gain floor takes
  over there and turns it into a linear tail (160 → 37, 141 → 33, 131 → 31, 76 → 18),
  which is continuous with the curve where they meet;
* lowering `gamma` to 1.35 moves 92 % reflectance from 190 to 199 for no benefit.

**A knee, never a clamp.** A hard limit on the white-balance gain would put a
visible contour on the page wherever a shadow crossed it. The knee has slope 1 at
the knee and never reaches the cap, which is the same rule the paper map's closing
already follows.

**A floor, not a rescale.** Crushing ink to black would destroy hue (every channel
scaled by ~0 is grey). The floor keeps a minimum scale of 0.22, which is why deep
ink lands at 18–33 rather than 0 — dark enough to read as ink, light enough to keep
its colour.

**The Round-61 gate was wrong, and it took a probe to see it.** R61 chose the
document/scene bounds with `classifiesAsDocument`. That function is a faithful
transcription of the original APK's `wv3` rule, and it is *absolute*: a pixel counts as
paper if all three channels clear 80 after one global gain. Shadows break it in both
directions — a page with a 3× shadow falls to 41 % and is called a scene (so no shadow
is removed), and a lit room with a beige wall reaches 61 % and is called a page. The
replacement asks the question the divisor already answers, in the divisor's own units
(`tools/shadow_probe.py` has both, side by side).

**A median, not a blur, on the surface.** The three box passes were there to remove
cell-scale noise, which a median also does — and a median is edge-preserving, which a
blur is not. The cost is nil (the grid is ~60 × 45 cells) and the effect on a flat page
is the identity, so nothing else moved. The five transcribed presets keep the blur:
changing it would change *their* output, which is not this round's business — it is a
one-line change if it is ever wanted.

**No new `FilterType` in the middle.** `FilterType` is `@Serializable` and stored on
every `ScannedPage`; the two new presets are **appended**. Existing pages keep
resolving to the same constants.

**Save format unchanged.** `SMART` and `LIGHT_TEXT` are colour filters and save to
JPEG at the user's quality setting, like Magic Color. They are not added to the
lossless (PNG) set that Clear B&W and Grayscale use: they keep tone, and lossless
would double the page size for no visible gain.

## 6. Limits — what this does not do

* **No neural network.** Adobe's `CNNDocClassification` / `CNNShadowRemoval` are
  downloaded at runtime and cannot be reproduced here. `surfaceIsPaper` is a
  statistics-based stand-in, and the illumination model is geometric.
* **The gate is a heuristic with a measured margin, not a proof.** Pages measure
  0.0–1.8 % against a 3 % limit; photographs 4.2–5.3 %. A page consisting of nothing
  but several crisp shadow edges and no ink at all — a blank sheet under a slatted
  blind — can cross 3 %, and then it relies on the ink term, which a blank page does
  not have. It is a scanner: the failure is chosen to be "treats an odd frame as a
  page", never "leaves the shadow in".
* **No tint removal.** Adobe estimates a per-page cast (`LABColorPixel`, `mIsTintRemoved`) and
  removes it. Here the per-channel paper grid already neutralises a cast that is
  *uniform*, but a cast that changes across the page is only partly corrected.
* **No dewarping, no page mask, no document-type binarisation.** Out of scope for a
  filter; these belong to the capture pipeline.
* **`SCENE` is a safety valve, not a preset.** It has no `FilterType`; it is what a
  frame that `surfaceIsPaper` rejects gets instead of being repainted white.

## 7. Verifying

```sh
python3 tools/filter_ref.py all                  # the kernel: tone, shadows, colour
python3 tools/shadow_probe.py                    # the surface and the gate, R61 vs R62
python3 tools/shadow_probe.py variants           # what each candidate estimator does
./gradlew testDebugUnitTest                      # DocumentFiltersTest.adaptive_* / surfaceGate_*
cargo test --manifest-path app/src/main/rust/Cargo.toml
```

On a device, `DocumentFilters.verifyNativeFilters` runs once at first use and
compares the two implementations on a synthetic colour page: luma plane, then the
adaptive kernel in all three presets, with and without edge crispening, by
`contentEquals`. Any mismatch, or a stale `.so` (`EXPECTED_ABI = 3`), and the Kotlin
reference runs instead — the filter is never wrong, only slower.
