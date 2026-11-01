# Whole-app performance & Rust audit

> **Status: static audit only. No code changed, no signature changed, no file deleted.**
> **Author: Muse AI (independent reviewer/verifier — no JDK / Android SDK in its sandbox, like Arena).**
> **Base pulled: `main` at `b62e8ea` — "Merge branch 'arena/02d1301d-re-cam': integrate safety fixes and record launch diagnosis §5 measurement split (H001, H002)", 2026-10-30.**
> **Trigger: owner asked, after pulling the latest changes, for a fresh in-depth study of performance issues and Rust / speedy improvements across the whole app.**
> **Verification so far: static reading only. No `./gradlew` build, no unit tests run by Muse, no device, no benchmark, no profiler trace. Every speed claim below is therefore a ranked hypothesis with a measurement step, not a measured result — exactly as the handoff rules require.**

## What changed since the last Muse audit (087cf89 → b62e8ea)

Pulled changes, verified from the git log, not assumed:

- Muse's launch diagnosis (`b6a0cc9`, `docs/LAUNCH_DIAGNOSIS.md`) was merged into `main`.
- Arena's safety fixes (from Muse's earlier bug inspection) were merged: corrupt
  `library.json` now fails closed instead of silently resetting, backup restore
  refuses overlapping document IDs / files and rolls back staged files, filter
  failures / rotation OOM / missing signatures are no longer swallowed, batch
  filter failures are reported, gallery publishing checks the MediaStore update
  count. Antigravity has since recorded, in `docs/AGENT_HANDOFF.md`, 233 unit
  tests passing and `assembleDebug` BUILD SUCCESSFUL for that patch.
- Arena added `docs/AGENT_HANDOFF.md` (H001 safety, H002 launch measurement
  split — H002 is still BLOCKED on a device, debug + benchmark APKs) and added
  Muse AI to `AGENTS.md` §0 as an independent reviewer/verifier. The earlier
  "Muse is not in the protocol" gap is therefore closed in `AGENTS.md` §0 and
  in `docs/AGENT_COORDINATION.md` §2 / title; §6be's note to the contrary is
  now out of date and should not be quoted as current.
- **No launch optimisation and no Rust change were in that merge.** The Rust
  state below is therefore unchanged since Round 61 / Round 30. H002's
  measurement split is still the un-done prerequisite for any launch claim.

## The one-sentence verdict

This is already a well-tuned app for scrolling and launch — thumbnail store,
2-slot decode pool, idle-gated background work, keyed grid, batched library
emissions — but the **bake is still the dominant CPU cost, and it is a
memory-bandwidth and copy problem, not a single slow function**. Rust helps
where it is used, but it only covers about half of a SMART / LIGHT_TEXT bake,
it is not guaranteed to ship, and every stage around it still copies a whole
3200 px page. Optimise the pipeline, not just one kernel.

All sizes below use the app's own constants: a page is up to 3200 px on its
long edge (`DocumentFilters.MAX_PROCESS_DIMENSION`), so a full page is roughly
7–8 megapixels, ~30 MB as an `IntArray` and ~30 MB as a `FloatArray` plane.
Any "one more full-image pass" is therefore tens of megabytes of traffic,
even before any arithmetic.

## 1. Rust today: what is accelerated, what is not, and what it costs to use

### 1.1 Accelerated (ABI 3, `NativeFilters`, `app/src/main/rust/src/kernels.rs`)

`medianBlurGray`, `median3x3`, `laplacian3x3`, `meanPool`, `applyLut`,
`rec601InPlace`, `gaussianBlur3`, `boxBlur3`, `boxBlurInPlace`, `lumaPlane`,
`adaptiveColour`. Separately, search vocabulary expansion is Rust
(`NativeSearch`, SEARCH ABI 1, `search.rs`).

Rust banding is real parallelism: `band_count()` uses
`cores - 2`, clamped to 1..6, via `std::thread::scope`. Kotlin's
`DocumentFilters.bands()` caps at 4 threads. The two caps are inconsistent,
but they never run at the same time (filter stages are sequential), so this
is a tuning note, not a bug.

Parity is the design's strength and its tax: `DocumentFilters.verifyNativeFilters()`
runs every kernel against its Kotlin reference on a synthetic 96×64 page on
first use, byte-exact for integer kernels, within 0.01 for float planes. If
any kernel disagrees, the native path stays off for the whole session. That
is the right safety rule — and it means any new Rust kernel must ship with
its Kotlin reference kept, byte-for-byte, or it will silently never run.

### 1.2 Kotlin-only, and run on every adaptive bake (the Rust gap)

These are **not** in Rust, and they are not small:

- `DocumentFilters.buildFineMap` + `trimmedPool`: a fine surface at 1/5 the
  coarse cell, with a 35% dark-side trim per fine cell, then a dilate/erode
  closing (`windowOp`, Kotlin) and two `median3Grid` passes (Kotlin, grid
  only — the grid itself is small, the *pooling* over full-resolution pixels
  is not). `trimmedPool` walks every pixel three times (once per channel),
  with a histogram per cell, and then walks them again to clear the histogram.
- `DocumentFilters.flattenIllumination`: a full per-pixel pass over the
  whole page, bilinear-sampling fine and coarse surfaces per pixel, before
  the adaptive kernel even runs.
- `DocumentFilters.finishAdaptive`: luma via Rust, two `boxBlurInPlace`
  passes via Rust, then a full per-pixel Kotlin loop for the paper clamp
  and inward sharpening.
- `DocumentFilters.applyDocumentColor` / `applyDocumentBw` (the original
  MAGIC_COLOR / CLEAR_BW presets): a full per-pixel Kotlin loop per page,
  sampling the paper surface per pixel per channel (`sample()`), with
  `colourise` / `levelTwoChannel` table lookups. Only their luma / median /
  Laplacian helpers are Rust; the per-pixel body is not.

In other words, a SMART bake today is roughly: Rust mean-pool → Kotlin
fine-pool → Kotlin flatten → Rust adaptive → Rust luma + blurs → Kotlin
finish. That is **five to six full-image passes**, each with its own
intermediate arrays, plus the JNI copies in §2. Porting one more isolated
kernel will not change that shape. Fusing stages will.

### 1.3 The Rust .so is not guaranteed to ship — fix that first, or measure it

`app/build.gradle.kts`: `buildRustKernels` runs only if `cargo` **and**
`cargo-ndk` are on PATH. Otherwise it is silently skipped. `jniLibs/` is
git-ignored and is not in the repo. `NativeFilters.isLoaded` then falls
back to Kotlin, with only a `Log.i` line to show for it.

Consequence: two "identical" builds can have very different filter speed,
and nobody on the device can tell which path they are on. Before any new
kernel is written, the next agent should:

1. Record, on the actual Antigravity build machine, whether the .so is
   produced, for which ABIs, and what `NativeFilters.isLoaded` reports at
   runtime (log it under `DocScannerPerf`, not just `Log.i`).
2. Make the Rust toolchain a checked CI / build-machine requirement, or
   prebuild and hash-verify the .so for arm64-v8a (and x86_64 if shipped).
   A silent Kotlin fallback is acceptable as a safety net; it is not
   acceptable as an unmeasured default.
3. Do not remove the Kotlin references. They are the parity definition,
   and they are the fallback. Removing them to "save space" would break
   both guarantees.

## 2. The hidden tax: JNI copies and whole-image copies

Rust's own `lib.rs` comment is honest about this: every JNI call copies
with `get_*_array_region` into a `Vec`, computes, and copies back with
`set_*_array_region`, deliberately avoiding `GetPrimitiveArrayCritical`
for crash safety. That trade is correct for a compute-heavy kernel.
It is **not** a rounding error for a memory-bound kernel.

Per 3200 px bake, the copy traffic already includes, in order:

- `ImageStore.loadBitmap`: sampled decode of the pristine page.
- `PerspectiveWarp.warp`: a new full ARGB bitmap, drawn via Canvas.
- Rotation in `ImageProcessingUtils.applyEnhancements`: another full bitmap
  via `Bitmap.createBitmap(..., Matrix)`.
- `DocumentFilters.apply` → `rgbaOf`: `getPixels` into a fresh `IntArray`
  (~30 MB), plus a scaled working bitmap if the source was larger.
- Each Rust kernel: copy in, Vec, copy out. Luma, median, Laplacian,
  mean-pool, adaptive, plus finish's luma and two blurs — each crossing
  is tens of megabytes.
- `DocumentFilters.apply` return: `Bitmap.createBitmap` + `setPixels`
  (~30 MB again).
- Signature overlay (`drawSignatureOverlay`): another full bitmap.
- Markup: a copy to a mutable bitmap if the result was not already mutable.
- Save: JPEG compress or PNG encode of the full bitmap, plus a thumbnail
  write from the same bitmap.

Peak "planes" are quoted in `PageRenderer` as ~95 MB. Transient traffic
across the whole bake is several times that, and it is what the garbage
collector and the memory bus actually feel. A professional optimisation
here is not "make the loop faster" — it is "make fewer copies".

Concrete, in priority order within this section:

- **Fuse, don't add kernels.** The biggest Rust win available is a single
  fused Rust call for an adaptive bake that takes the pixels and the
  paper grids once, and does flatten → adaptive → clamp/sharpen inside
  Rust, with scratch buffers allocated once in Rust and reused across
  calls. That removes at least two JNI crossings and two Kotlin
  full-image passes. It needs a new ABI (4), a Kotlin reference for the
  fused result, and a parity test — do not ship it without those.
- **For simple memory-bound kernels (LUT, Rec.601), the copy dominates.**
  Either fuse them into the pass that already has the pixels, or accept
  that their Rust speedup is small and measure it before claiming it.
  Do not add a new JNI crossing for a kernel whose arithmetic is cheaper
  than its copy.
- **Reuse scratch, don't re-allocate per bake.** Luma, detail, blurred,
  high-pass and grid planes are currently fresh `FloatArray`/`IntArray`
  allocations per bake. A bounded scratch pool (one per concurrent bake,
  cleared on OOM) is a legitimate speed-for-space trade under the owner's
  stated preference, and it directly reduces GC pauses during batch bakes.
- **Avoid the signature / markup extra bitmaps where possible.** Draw the
  signature and markup directly onto the filtered bitmap when it is
  already mutable and owned by the bake (it usually is — `createBitmap`
  returns a mutable bitmap), instead of `drawSignatureOverlay`'s
  unconditional new bitmap and `PageRenderer`'s conditional markup copy.
  Only copy when the bitmap is shared or immutable. This is a small,
  safe win; it is not a rewrite.

## 3. Whole-app hot paths, ranked by where time actually goes

### P0 — Batch bakes are sequential (editor, highest user-visible win after filters)

`CropAndFilterScreen.applyFilterToPages` bakes pages **one at a time**,
awaiting each `PageRenderer.bake` in a loop, then publishes once (that
publish-once part is already good — it avoids ten `library.json` writes
for ten pages).

A 10-page filter change therefore costs the sum of ten bakes, on one
decode/filter chain, while the progress sheet sits at "page N of 10".
Parallelising bakes is the obvious win — and the obvious way to OOM:
each bake peaks at tens of megabytes of planes plus bitmaps.

Safe form: a bounded parallel bake (2 concurrent bakes, not N), with
per-bake memory accounting, falling back to sequential on OOM, and
keeping the single final publish. Do not parallelise by simply launching
N coroutines. Measure on a mid-range device with a 10-page batch before
raising the bound above 2.

### P1 — Editor preview is rendered at 2400 px for a ~1000 px display

`CropAndFilterScreen`: preview source is decoded at up to
`PREVIEW_RENDER_DIMENSION = 2400`, the full preview pass runs
`applyEnhancements` (warp → rotate → filter) at that size, with a
140 ms settle delay, and then — for non-lossless filters — the preview
is JPEG-encoded and decoded again so the preview matches the save's
softening. Filter swatches are correctly tiny (260 px), but the main
preview is not.

2400 px is 2.4× the display width in pixels and roughly 5–6× the pixel
count of a 1000 px preview. That is the most expensive "preview" in the
app, and it runs on every filter tap (after the settle delay), every
corner move, and every page switch.

Fix, in order: (a) render the on-screen preview at display size
(≈1000–1200 px long edge, device-dependent), and only render at bake
size on save; (b) keep the JPEG round-trip only as a quality toggle,
not on every preview frame — or cache the encoded preview per
(filter, corners, rotation) triple so a tap back to a previous filter
does not re-render. The "preview must predict the save" rule is real,
but it does not require previewing at save resolution.

### P2 — PDF export re-decodes and re-encodes what is already a JPEG

`PdfGenerator` has two paths. The platform path (`PdfDocument`) never
recycles page bitmaps (a known issue from the earlier inspection, still
present — `PdfGenerator` was not in the Arena safety merge). The custom
`PdfWriter` path recycles, but both paths decode the already-baked page
to a bitmap and then re-encode it: `PdfImages.encodeJpeg` via
`Bitmap.compress`, or `encodeLossless` via a per-row `getPixels` +
`Deflater` loop (single-threaded, `DEFAULT_COMPRESSION`).

For the common case — no watermark, no downscale, JPEG page already on
disk at the requested quality — the fastest correct export is to embed
the file's JPEG bytes directly (DCT passthrough), with no decode and no
re-encode. That is loss-free, faster, and smaller than a re-encode.
Only fall back to decode → encode when a watermark, downscale, rotation,
or lossless conversion actually requires new pixels.

Similarly, `encodeLossless` should not be the default surprise: it is
per-row Kotlin + Deflater, and for a photographic page it is both slow
and large. Keep it for CLEAR_BW / Grayscale (where PNG is genuinely
smaller and exact), and make the export sheet's default the JPEG path
unless the user chose lossless.

### P3 — OCR is the most expensive background pass, and it is still on the launch-adjacent path

`OcrEngine.readPage`: decode at up to 2400 px, then ML Kit inference, on
`Dispatchers.Default`, per page, with no tried/failed state (blank pages
are re-recognised every launch — also noted in the earlier inspection).
Backfill is capped at 24 pages per launch and idle-gated (see the launch
diagnosis, H3), but the per-page cost itself has not changed.

Speed options, in order of safety: (a) record a tried/failed/no-text
state so a blank page is not re-decoded and re-inferred forever;
(b) lower the OCR decode size and measure accuracy loss (2400 px is
generous for body text; ML Kit normalises its input — test 1600 px
against a fixed set of pages before changing it); (c) batch / pipeline
OCR so decode of page N+1 overlaps inference of page N, instead of
decode-then-infer serially per page. Do not move OCR onto the UI thread
or into the bake path to "save a pass" — it is background work by design.

### P4 — Search index is rebuilt on every library emission while searching

`DocumentSearch.Index` tokenises and stems every page's OCR text, builds
postings, lowercases titles and pages, and packs the vocabulary, in its
constructor. `DashboardScreen` rebuilds it in a `LaunchedEffect(documents,
queryActive)` whenever `documents` changes and a query is active.

During an OCR backfill, `documents` emits per batch (now bulk, which
helps), but any emission while the user is typing a query still rebuilds
the whole index. The Rust vocabulary walk (`NativeSearch.expand`) only
accelerates the *walk*, not the *build*.

Fix: incremental index updates (only re-index changed documents), or at
minimum debounce index rebuilds and cache the index keyed by the
library's revision, not by every emission. This is a medium win, and
only matters for large libraries with OCR text — measure index-build
time on a 500-page library before optimising it further.

### P5 — Camera live border: cheap by design, keep it that way

`LiveBorderAnalyzer` is already rate-limited (150 ms, keep-only-latest),
uses only the Y plane, and `DocumentQuad` works at 360 px working width
with RANSAC at 200 iterations per side. That is the right shape: do not
"optimise" it by running the TFLite model per frame. The model
(`MlBorderDetector`, 4.7 MB, memory-mapped) is correctly reserved for
the single post-capture / editor detection, with a geometry fallback.

The one watch item: `DocumentQuad` tries up to 3 thresholds × 2
polarities, and RANSAC is O(iterations × candidates) per side. At
360 px that is small, but if anyone raises the working width for
"accuracy", live detection cost rises quadratically. Keep the working
width capped, and keep the model out of the live path.

### P6 — Library, dashboard, and viewer: already tuned, do not re-tune blindly

- Dashboard grid is keyed, single content type, no per-item entrance
  animation, date labels memoised per document, search debounced and
  off the main thread, prefetch row-quantised with a 1-slot gate.
- `PageImageLoader`: 2-slot decode pool, single-flight dedup, thumbnail
  fast path, `RGB_565` for tiles, 1/5-heap cache.
- Viewer uses `rememberPageImage` with retained last frame.

These are good. The remaining dashboard cost is the one already named
in the launch diagnosis: the whole-library `library.json` parse on
construction, and a full re-sort on every `documents` emission. For
very large libraries, a sorted-insertion list and a lightweight launch
index (see `LAUNCH_DIAGNOSIS.md` P4) matter more than any further
tile tuning. Do not add more prefetch rows or a larger bitmap cache
without a scroll-jank trace showing a cache miss — that would spend
memory for no measured win.

## 4. What Rust should NOT be used for (professional restraint)

- **Do not port the UI, the repository, PDF layout, or backup ZIP to Rust.**
  Those are I/O, layout, and Android-framework problems. Rust wins on
  dense, parallel, allocation-free pixel / numeric loops. It loses on
  anything that needs `Context`, `Bitmap`, `Canvas`, or MediaStore, and
  every JNI crossing for such work is pure overhead.
- **Do not use Rust to "fix" the launch parse.** `library.json` parsing
  is a data-model problem (split the index from OCR, or move to Room),
  not a CPU problem. A faster JSON parser in Rust would still block the
  splash on the full model, and it would add a native dependency to the
  most safety-critical path in the app.
- **Do not add rayon or a large Rust dependency tree.** `Cargo.toml`
  deliberately has only `jni`. That keeps the .so in the hundreds of
  KB and the toolchain stable. A rayon port would be faster to write
  and harder to parity-test, and `std::thread::scope` banding already
  gives the parallelism this workload needs.
- **Do not use `GetPrimitiveArrayCritical` to "save a copy" without a
  crash audit.** The current copy-based JNI contract is a safety choice.
  If a fused kernel ever needs pinning, it needs a dedicated review of
  every JNI call held while pinned, and a fallback to copy on any error.
  Copy cost is the price of that safety; fuse stages instead of
  breaking the contract.

## 5. Ranked plan for the next agent

| Rank | Work | Why it wins | Risk / cost |
| :--- | :--- | :---------- | :---------- |
| 1 | **Make Rust shipping measurable** (§1.3): log `isLoaded` + ABI + .so presence under `DocScannerPerf`, check the build machine's cargo-ndk, hash-verify the .so | Without this, every Rust claim is conditional and untestable | Low code, needs Antigravity's build machine |
| 2 | **Bounded parallel batch bakes** (§3 P0): 2 concurrent bakes, OOM fallback to sequential, single final publish kept | 10-page filter change is the most visible batch cost in the app | Medium — memory accounting, progress reporting |
| 3 | **Preview at display size** (§3 P1): render editor preview at ~1000–1200 px, cache per (filter, corners, rotation), keep bake-size render for save only | Every filter tap / corner move currently pays a 2400 px bake | Low–medium — preview/save drift must be tested |
| 4 | **Fuse the adaptive bake in Rust** (§1.2, §2): one JNI call for flatten → adaptive → clamp/sharpen, scratch reused, new ABI 4, Kotlin reference + parity test kept | Removes 2+ full-image passes and 2+ JNI crossings from every SMART / LIGHT_TEXT bake | High — parity, ABI bump, must not change a pixel without a test |
| 5 | **Port the original-preset per-pixel bodies to Rust** (§1.2): `applyDocumentColor` / `applyDocumentBw` bodies, same parity rule | MAGIC_COLOR / CLEAR_BW are still Kotlin per-pixel at 3200 px | High — same parity discipline as (4), do after (4) |
| 6 | **PDF DCT passthrough** (§3 P2): embed JPEG bytes directly when no watermark / downscale / conversion is needed; recycle in the platform path | Export of a large document currently decodes + re-encodes every page | Medium — PDF correctness tests, watermark/downscale fallbacks |
| 7 | **OCR tried-state + decode-size test** (§3 P3): never re-infer a blank page; test 1600 px vs 2400 px accuracy on a fixed set | OCR is the most expensive background pass and repeats forever on blank pages | Low–medium — accuracy must be measured, not assumed |
| 8 | **Incremental search index** (§3 P4): re-index only changed documents, or cache by library revision | Only matters for large OCR libraries while searching | Medium — index correctness tests |

Explicitly **not** in this plan, and why: a Rust JSON parser for launch
(§4), rayon (§4), per-frame ML border detection (§3 P5), larger
prefetch / bitmap cache without a miss trace (§3 P6), and any new JNI
crossing for a memory-bound kernel (§2).

## 6. Measurement the next agent must run before claiming a speedup

No speedup in this document is measured. The next agent must, on a
real device, on the benchmark APK (not debug), with a fixed set of
pages (same 3200 px page, same filter, same batch size):

1. Bake time per filter (ORIGINAL, LIGHTEN, GRAYSCALE, MAGIC_COLOR,
   CLEAR_BW, SMART, LIGHT_TEXT), with Rust .so present and with it
   deliberately absent (Kotlin fallback), to get the real Rust factor.
2. Batch time for a 10-page filter change, sequential vs 2-parallel.
3. Editor preview time at 2400 px vs display size, per filter tap.
4. PDF export time and output size for a 20-page document, platform vs
   custom writer, JPEG passthrough vs re-encode.
5. OCR time per page at 2400 px vs 1600 px, with accuracy (words correct)
   on the same pages — time alone is not a win if text is lost.
6. Cold-start and scroll traces from `LAUNCH_DIAGNOSIS.md` §5, so a
   filter optimisation is not mistaken for a launch optimisation.

Record device, build type, page size, filter, batch size, and whether
the Rust .so was present, with every number. A speedup without those
five fields is not a result.

## 7. Prompt for the next agent (copy / adapt)

> Pull `main` at `b62e8ea` and read `docs/PERF_RUST_AUDIT.md` before
> touching anything. This is a static audit — no number in it is
> measured. Run §6 first, on the benchmark APK, with the Rust .so
> present and absent, and report the numbers. Start with §5 ranks 1–3
> (Rust shipping check, bounded parallel batch bakes, display-size
> preview). Do not start a fused Rust kernel (rank 4) without its
> Kotlin reference and parity test, and do not bump the ABI without a
> changelog entry and a fallback test. Claim your files in
> `docs/AGENT_COORDINATION.md` §4 before editing, add a handoff
> message in `docs/AGENT_HANDOFF.md`, and add a changelog entry for
> any signature / ABI change. Antigravity is the build authority:
> `./gradlew testDebugUnitTest assembleDebug` plus the §6 device
> numbers are owed before any of this is called faster. Do not touch
> `adobe-scan/` without the owner's explicit authorisation.
