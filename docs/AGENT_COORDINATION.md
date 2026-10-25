# Agent Coordination Protocol — Arena.ai ⟷ Antigravity

> **Two AI agents build this app in parallel.** Arena.ai works through Git on the
> `arena/*` branch; Antigravity works directly in the local IDE. They share one
> working tree. This document is the contract that keeps their edits from
> colliding. Read it before you touch a file.

---

## 1. The three rules

1. **Claim before you edit.** Add a row to §4 *In-flight changes* naming the
   files you are about to touch, **before** you touch them. Remove the row when
   your change is committed or abandoned.
2. **Never reformat a file you did not claim.** No "clean up imports across the
   project", no `ktfmt`/`ktlint` sweeps, no whitespace-only commits. Reformatting
   makes every other agent's diff conflict.
3. **Additive beats destructive.** New file > new function > new parameter with a
   default > changed signature > renamed symbol > deleted file. Prefer the
   left-hand side of that list. **Deleting or renaming a symbol in §3 requires a
   note in `docs/AGENTS_CHANGELOG.md` first.**

## 2. Branch & handoff mechanics

| | Arena.ai | Antigravity |
| :--- | :--- | :--- |
| Writes to | `arena/bdeaf5fa-re-cam` branch, pushed to `origin` | local working tree |
| Verifies by | code review only — **no JDK / Android SDK in the Arena sandbox** | `./gradlew assembleDebug` + device install |
| Must do before pulling | — | `git stash` or commit local work first |

**Arena cannot compile.** Every Arena change is reviewed-by-eye only. Antigravity
is the build authority: if `./gradlew assembleDebug` fails, that failure is the
source of truth and Arena's change gets fixed, not reverted blindly.

**Antigravity, before you `git pull` Arena's branch:** commit or stash your local
work. If a merge conflict appears in a file, Arena's commit message names the
rationale — read it before resolving.

## 3. Integration contract — symbols other code depends on

These are the shared seams between the two agents. **Do not rename, re-order
parameters of, or change the return type of** anything in this table without
adding a changelog entry and updating every caller in the same commit.

| Symbol | Kind | Semantics |
| :--- | :--- | :--- |
| `ScannerRepository(context)` | ctor | Reads `library.json` on construction. No global singleton. |
| `ScannerRepository.documents` / `.folders` | `StateFlow` | Single source of truth for the whole UI |
| `ScannerRepository.movePage(docId, from, to)` | fun | Page reordering |
| `ScannedPage.imageUri` | field | **Absolute filesystem path** of the display JPEG (may be the edited bake) |
| `ScannedPage.originalImageUri` | field | Pristine original JPEG; set on first bake. `null` = never edited |
| `ImageStore.resolveFile(ctx, imageUri)` | fun | Tolerant resolver (abs path / `file://` / bare filename). `null` for un-imported `content://` |
| `ImageStore.pageFile(ctx, pageId)` | fun | Canonical `filesDir/pages/<pageId>.jpg` |
| `ImageStore.loadBitmap(ctx, imageUri, w, h)` | fun | Sampled decode, orientation-corrected |
| `rememberPageImage(imageUri, w, h)` | `@Composable` | Async decode → `ImageBitmap?` |
| `PageThumbnail(page, modifier)` | `@Composable` | Standard page preview with icon fallback |
| `ImageProcessingUtils.applyEnhancements(src, filter, rotation, corners)` | fun | warp → rotate → filter, in that order |
| `DocumentFilters.apply(src, filter, maxDimension)` | fun | The real filter pipeline. Returns `src` for ORIGINAL/failure — compare identity before recycling |
| `DocumentFilters.applyPreview(src, filter)` | fun | Same pipeline at preview resolution (WYSIWYG) |
| `DocumentDetector.detect(bitmap)` | fun | **Preferred.** `Detection(corners, confident)` — boundary-scan + RANSAC; never throws |
| `DocumentDetector.detectQuad(bitmap)` | fun | Compatibility shim for `detect().corners` |
| `DocumentFilters.stretchPoints(hist)` | fun | Clamped black/white points; the blow-out guard |
| `PerspectiveWarp.detectQuad(bitmap)` | fun | Thin delegate to `DocumentDetector` (kept for compatibility) |
| `PerspectiveWarp.warp(src, corners)` | fun | `Matrix.setPolyToPoly` projective warp |
| `PdfGenerator.generatePdf(ctx, doc, config, bitmapProvider)` | fun | Blocking; **call from `Dispatchers.IO` only** |
| `SignaturePadDialog(onDismiss, onSignatureConfirmed)` | `@Composable` | Callback receives a **PNG path**, not an opaque token |
| `CropAndFilterScreen(docId, pageId, repository, onDone, reviewMode)` | `@Composable` | `reviewMode = true` opens on **Borders** with auto-detection and walks the document page by page |
| `DocumentNaming.defaultScanTitle()` | fun | **The only** sanctioned source of scan titles — it quotes pattern literals |
| `QuadCorners.FULL_FRAME` | const | Means "no warp". Auto-detect falls back to it rather than cropping |
| ~~`ImageProcessingUtils.getColorMatrix`~~ | **removed** | Was a global `ColorMatrix`; superseded by `DocumentFilters` |
| ~~`ImageProcessingUtils.getComposeColorFilter`~~ | **removed** | Editor now previews a real filtered bitmap |

## 4. In-flight changes

Keep this table short. Delete your row when the change lands.

| Agent | Status | Files | Intent |
| :--- | :--- | :--- | :--- |
| Antigravity | ✅ committed | `docs/ORIGINAL_APP_FILTER_SPEC.md`, `docs/AGENTS_CHANGELOG.md` | Reverse-engineered native filter spec & decompiled algorithms |
| Arena.ai | ✅ committed | `util/DocumentNaming.kt`, `ui/editor/CropAndFilterScreen.kt`, `Navigation.kt`, `NavigationKeys.kt`, `ui/dashboard/DashboardScreen.kt`, `ui/detail/DocumentDetailScreen.kt`, `ui/camera/ScannerCameraScreen.kt`, `util/PerspectiveWarp.kt`, `app/build.gradle.kts`, tests + docs | Done-button crash fix, post-capture review flow, folder move, note editing |
| Arena.ai | ✅ merged (1.7) | `util/DocumentFilters.kt`, `ui/editor/CropAndFilterScreen.kt`, `DocumentFiltersTest.kt`, `app/build.gradle.kts`, docs | Magic Color made uniform (mean-pooled estimator, soft knee), Clear B&W gets a clamped Sauvola refinement, Borders tap auto-detects, preview is warped+filtered. No public signature changed; `saturateGain`/`softKnee`/`boxMeanAndStd`/`sauvolaWindow` are new and `internal`. |
| Arena.ai | ✅ merged (1.8) | `util/DocumentFilters.kt`, `ui/editor/CropAndFilterScreen.kt`, `util/PageRenderer.kt`, `util/ImageProcessingUtils.kt`, `DocumentFiltersTest.kt`, `app/build.gradle.kts`, docs | 1.8 — Clear B&W: 3 % decision ramp on a sharpened, noise-gated plane; editor preview now renders at the *bake* dimension on the *warped* bitmap, which also fixes the recycled-bitmap blank preview and the stale (uncropped) photo after editing borders. **Additive signature change:** `bake`/`bakeBlocking`/`applyEnhancements` gained `maxDimension` with a default; `bakeDimension(quality)` added. `gatedUnsharp`/`noiseEstimate`/`adaptiveSharpenAmount` are new and `internal`. |

| Arena.ai | ✅ merged & verified (1.9) | `util/DocumentFilters.kt`, `util/ImageStore.kt`, `util/PageRenderer.kt`, `util/ImageProcessingUtils.kt`, `model/AppSettings.kt`, `model/Models.kt`, `ui/editor/CropAndFilterScreen.kt`, `DocumentFiltersTest.kt`, `app/build.gradle.kts`, docs | 1.9 — saved pages and exported PDFs are **3200 px ≈ 299 DPI** instead of 187 (capture cap, bake cap and export cap all moved to 3200), Clear B&W is cut with 3 × 3 coverage anti-aliasing instead of a level ramp, and bi-level pages are saved as PNG so the PDF has no JPEG ringing. **Touches four constants that must move together:** `ImageStore.MAX_CAPTURE_DIMENSION`, `DocumentFilters.MAX_PROCESS_DIMENSION`, `ImageQuality.HIGH.maxDimension`, `ExportResolution.HIGH.maxImageDimensionPx`. `saveEditedPage` gained `lossless` (default false); new internal helpers `bands`/`coverageCut`/`bilinearAt`/`medianOf9InPlace`/`boxBlurInPlace`/`noiseFromResidual`; `BW_RAMP_FRACTION`/`BW_MIN_RAMP` deleted. No public signature broken. |
| Arena.ai | ✅ merged & verified | `util/DocumentFilters.kt`, `util/DocumentFiltersTest.kt`, docs | Native divide table fix from `libimageproc.so` (`0x105e0`–`0x10734`): signed row difference, complemented response byte, 56 tests passing. |
| Arena.ai & Antigravity | ✅ merged & verified (Round 14) | `theme/*`, `ui/*`, `docs/*` | Material 3 Expressive UI overhaul across all screens. Fixed `FiniteAnimationSpec` types, `Modifier.entrance` extension, and `Check` icon import. All unit tests pass, `app-debug.apk` built. |
| Arena.ai & Antigravity | ✅ merged & verified (Round 15) | `app/build.gradle.kts`, `util/NativeFilters.kt`, `util/DocumentFilters.kt`, `app/src/main/rust/*`, `ui/*`, docs | Optional Rust pixel kernels behind byte-for-byte parity gate + UI scroll/dialog polish. Fixed NativeFilters Log.i JVM test initialization, in-process PATH lookup and Configuration Cache compatibility. 56 unit tests pass, `app-debug.apk` built. |

## 5. File ownership map

Rebuilt 2026-10-25 after the "make it real" pass.

| Area | Files | Notes |
| :--- | :--- | :--- |
| App shell | `MainActivity.kt`, `Navigation.kt`, `NavigationKeys.kt` | Repository is created in `MainActivity` and passed down — **do not re-introduce a singleton** |
| Domain | `model/Models.kt` | `@Serializable`; changes need a `schemaVersion` bump in `LibraryStore` |
| Data | `data/ScannerRepository.kt`, `data/LibraryStore.kt` | All mutation goes through the repository, which persists |
| Core utils | `util/ImageStore.kt`, `util/PageImageLoader.kt`, `util/PerspectiveWarp.kt`, `util/ImageProcessingUtils.kt`, `util/PdfGenerator.kt` | Pure/stateless; safe to call from any dispatcher except noted |
| Shared UI | `ui/common/PageThumbnail.kt` | Use this instead of writing a new thumbnail loader |
| Screens | `ui/<feature>/*Screen.kt` | Screens receive `repository` as a **parameter**, never construct it |

## 6. Where new features go

`AGENTS.md` §4 lists the roadmap. When Antigravity or Arena picks one up:

1. Claim it in §4 above.
2. Put the file exactly where `AGENTS.md` §5 says it goes
   (`util/OcrManager.kt`, `ui/editor/CleanupBgCanvas.kt`, `util/NamingTemplateEngine.kt`,
   `util/BackupRestoreManager.kt`).
3. Append one line to `docs/AGENTS_CHANGELOG.md`.

## 6b. Round 4 — zoom (claimed by Arena, 2026-10-25)

Arena owns these two files for the zoom work; do not edit them concurrently:

- `app/src/main/java/com/example/docscanner/ui/camera/ScannerCameraScreen.kt`
  (pinch + zoom pill; zoom goes through `cameraControl.setZoomRatio` so the
  captured JPEG is zoomed, not just the preview)
- `app/src/main/java/com/example/docscanner/ui/editor/CropAndFilterScreen.kt`
  (one `awaitEachGesture` handler: single finger drags a corner, single finger
  elsewhere pans when zoomed, two fingers pinch)

## 6c. Settings screen (claimed by Arena, 2026-10-25)

Touched by the same change, all committed together:

- New: `ui/settings/SettingsScreen.kt`, `data/SettingsStore.kt`,
  `model/AppSettings.kt`, `util/GalleryExporter.kt`,
  `app/src/test/.../model/AppSettingsTest.kt`
- Changed: `MainActivity.kt` (theme from settings + system bars),
  `theme/Theme.kt` (accent), `Navigation.kt` / `NavigationKeys.kt`
  (`SettingsNav`), `ui/dashboard/DashboardScreen.kt` (settings icon +
  system-picker scan path), `ui/pdf/PdfExportSheet.kt`, `util/PdfGenerator.kt`,
  `util/PageRenderer.kt`, `util/ImageStore.kt`, `ui/camera/ScannerCameraScreen.kt`,
  `ui/editor/CropAndFilterScreen.kt`, `ui/detail/DocumentDetailScreen.kt`,
  `AndroidManifest.xml`.

Rules for this area:
1. New preference → add the field to `AppSettings` **and** a reader in the same
   commit. `AGENTS.md` §2.7: no row may persist a value nothing consumes.
2. Screens read `repository.settings` (StateFlow) or
   `repository.currentSettings()` inside callbacks — never a local copy.
3. Do not move settings into `library.json`; it would rewrite the whole library
   on every toggle.

## 6d. Document filter internals (claimed by Arena, 2026-10-25, round 13)

All inside `app/src/main/java/com/example/docscanner/util/DocumentFilters.kt` and its
test — no other file touched:

- **Signature change:** `ToneTables.divide(paper, value)` → `divide(row, value)`.
  `row` is `|src − paper|`, not the paper level (spec §13.4b).
- **Semantics change:** `divideLookup(row, value)` now returns
  `255 − response[min(2000, row·2000/(value+1))]·255/2000`. Anything calling it with
  a paper level as the first argument is wrong.
- **New internal seams:** `levelTwoChannel(table, paper, src)`, `toneTables()`.
- **Deleted** (were unreachable, all replaced by the transcription):
  `sharpenLuminance`, `localThresholds`, `gainLimits`, `buildThresholdField`,
  `sharpenDecisionInPlace`, `gatedUnsharp`, `noiseEstimate`, `documentCurveLut`,
  `coverageCut`, `normalise`, `medianHistogram`, plus the 10 tests that only covered
  them.
- `applyDocumentColor` / `applyDocumentBw` are transcriptions again: the gamma,
  saturation, Sauvola, Otsu and unsharp stages are **gone**. Do not reintroduce them
  without a disassembly citation — spec §13 is the source of truth.
- `applyGrayscale(pixels, w, h)` now applies `medianBlur(…, 9)` after desaturating.

Arena verified the arithmetic with numpy only. **No build or device run here** — the
last verified build is still Antigravity's.

## 6e. Round 14 — UI/UX overhaul (claimed by Arena, 2026-10-25)

Scope: **presentation only**. `util/DocumentFilters.kt`, its test file and all
saving/export paths are untouched, so nothing in §6d or spec §13 moves.

### New shared files — please reuse, don't fork

- `theme/Shape.kt` — `Shapes` + `DocShapes` (`pill`, `page`, `card`, `cardSmall`,
  `sheet`, **`dialog`** (new), `fab`, `field`, `iconButton`, `thumbnail`,
  `pressed()`).
- `theme/Motion.kt` — `Motion.*` specs, `Modifier.pressScale(interactionSource,
  pressedScale)`, `Modifier.entrance(index)`.
- `ui/common/Expressive.kt` — `PillButton`, `TonalPillButton`, `TonalIconButton`,
  `InfoPill`, `SectionCard`, `SegmentedPill`, `LoadingDots`, `PageProgressBar`,
  `SectionLabel`, `Hairline`, `RevealWhen`.

### Conventions a new screen must follow

1. `TopAppBar` colours from `TopAppBarDefaults.topAppBarColors(containerColor =
   surface, titleContentColor = onSurface)`; no tonal elevation app bars.
2. Content sits on `surfaceContainerLow` cards with `DocShapes.cardSmall`/`card`.
3. Every tappable surface: `remember { MutableInteractionSource() }` +
   `pressScale(source)` + `clip(shape)` + `clickable(source, ripple())`.
4. Metadata as `InfoPill`; section signposts as `SectionLabel` (uppercase
   `labelSmall`) separated by whitespace, not dividers.
5. Primary action = accent-filled pill (`DocAccents.scan` / `onScan` in capture
   contexts, `colorScheme.primary` elsewhere).

### Signature/behaviour changes other code may notice

- `CropAndFilterScreen(reviewMode = true)` is the camera path: it starts on
  **Borders** and requires a per-page border pass before Filters. The detail-screen
  path keeps `reviewMode = false` and starts on Filters.
- `PdfExportSheet` / `SignaturePadDialog` / `TrashScreen` / `PageViewerScreen`
  keep their public signatures — call sites are unchanged apart from imports.
- No new dependencies; everything is stable `material3` + `compose.foundation`
  (the M3 Expressive `MotionScheme`/`MaterialShapes`/`ButtonGroup` APIs are not in
  the pinned BOM, so the look is built from those primitives).

**Not built here** (sandbox has no JDK/Android SDK). Last verified build is still
Antigravity's in §7 — please run `assembleDebug testDebugUnitTest` before merging.

## 6f. Rust pixel kernels (claimed by Arena, 2026-10-25, round 15)

Files touched: `app/build.gradle.kts`, `.gitignore`, `README.md`, new
`app/src/main/rust/`, new `util/NativeFilters.kt`, `util/DocumentFilters.kt`
(kernel dispatch + parity self-test only), plus three UI fixes (`PageViewerScreen`,
`PdfExportSheet`, `SignaturePadDialog`, `TrashScreen`).

### What changed in DocumentFilters

- Every ported kernel now has two bodies: the public function dispatches, and the
  Kotlin implementation is kept as `medianBlurGrayReference` / `median3x3Reference`
  / `laplacian3x3Reference` (all `private`). **Do not delete the reference bodies** —
  they are the parity oracle and the fallback.
- `nativeFiltersReady()` / `verifyNativeFilters()` are new and `internal`. The
  self-test runs once, lazily, on the first filter call (i.e. off the main thread).
- No filter maths changed. If the self-test passes, output is byte-identical by
  construction; if it fails, the old code runs.

### Build system

- New task `:app:buildRustKernels` (an `Exec`), hooked to `preBuild`, guarded by
  `onlyIf { cargo && cargo-ndk present }`. Absent toolchain → skipped, no failure.
- It writes `app/src/main/jniLibs/arm64-v8a/libdocscanner_native.so` (+ `x86_64`),
  which is **git-ignored**. Do not commit it (AGENTS.md §2.5).
- Escape hatch if the crate does not compile yet: `-x buildRustKernels`.
- ABI guard: `NativeFilters.EXPECTED_ABI` must match `ABI_VERSION` in `lib.rs`.
  Bump both when a kernel's output changes, or a stale `.so` will be refused.

### Verification status

The Rust crate has **never been compiled** (no Rust toolchain or NDK in the Arena
sandbox). Treat the first `cargo ndk` run as a compile-check of the `jni` 0.21 API
usage in `lib.rs`. The Kotlin side is safe regardless: `System.loadLibrary` failure
and the byte-for-byte self-test both fall back to Kotlin.

## 6g. Home-screen performance + title (claimed by Arena, 2026-10-25, round 16)

Files: `ui/dashboard/DashboardScreen.kt`, `util/PageImageLoader.kt`. No signature
changes; `PageImageLoader.clear()` gained an optional `Context` parameter
(`clear()` still compiles unchanged).

- `DashboardScreen` no longer uses `LargeTopAppBar`, `TopAppBarDefaults.exitUntil
  CollapsedScrollBehavior` or `nestedScroll`. **Do not reintroduce a scroll
  behavior on the home screen** — it re-measures the app bar every frame and it
  animates the app name, which the user explicitly asked to stop.
- The title is **"Doc Scanner"**. It is the product name now; `README`/Play listing
  references to "Library" mean the list, not the screen title.
- Lazy items must not use `Modifier.entrance(...)`: it composes an animation *and*
  an alpha layer per item and re-runs whenever a card scrolls into view. Use it on
  static header content only.
- `PageImageLoader` now maintains a disk thumbnail cache under `cacheDir/thumbs`
  for requests ≤ 1024 px. Anything larger (viewer, editor) deliberately bypasses
  it. If you add a screen that shows pages at ≥ 1024 px, expect a full decode.

## 6h. Border detection + filter speed (claimed by Arena, 2026-10-25, round 17)

Files: `util/DocumentDetector.kt` (rewritten internals, same public API),
`util/DocumentFilters.kt`, `ui/camera/ScannerCameraScreen.kt`.

- `DocumentDetector.detect()` / `detectQuad()` / `Detection(corners, confident)`
  are unchanged. Everything else inside the object was replaced — if you were
  relying on a private constant, it is gone.
- **Do not** reintroduce a "first strong gradient from the frame edge" scan to this
  file. That is the algorithm that produced confident crops through the middle of
  a page. The invariant the current one depends on:
  - sides are found on a *closed + blurred* background image, never on raw
    gradients, because ink must not look like paper;
  - a side with no candidate means the page runs off the frame → frame edge;
  - an unchanged `confident = false` means "invite manual adjustment".
- Camera: both `Preview` and `ImageCapture` are pinned to 4:3 and the viewfinder is
  a 3:4 box. If you add a second preview surface or change the resolution
  selector, keep the two use cases on the same `ResolutionSelector` — a preview in
  a different aspect than the capture is what made framing unreliable.
- `DocumentFilters.medianBlurGray` is the histogram implementation and is **not**
  routed to Rust any more (it is faster than the native comparison-based median).
  The Rust kernel is still parity-checked against it. If you touch either, run
  `verifyNativeFilters()` coverage in the unit tests.
- `DocumentFilters.apply` logs slow filters at `Log.i("DocumentFilters", …)`.

## 6i. What Antigravity must provision (Arena, 2026-10-25, round 18)

Search over a scan's own words is implemented: pages are recognised on device
right after they are saved (`util/OcrEngine.kt`, `ScannerRepository.indexTextInBackground`),
and the home screen ranks results from an inverted index (`util/DocumentSearch.kt`).
Everything below is what the *environment* needs — no code changes.

### 1. Rust toolchain — native filter kernels (optional, but wanted)

`app/build.gradle.kts` already wires `:app:buildRustKernels` and skips it silently
when the toolchain is absent, so a machine without Rust still builds a complete,
correct app (the filters then run the Kotlin reference). To get the kernels:

```bash
rustup toolchain install stable --profile minimal
rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
cargo install cargo-ndk --locked
sdkmanager "ndk;27.0.12077973"
export ANDROID_NDK_HOME="$ANDROID_SDK_ROOT/ndk/27.0.12077973"
```

Then `./gradlew :app:assembleDebug` — `preBuild` depends on the Rust task — and
check the artifacts exist:

```bash
ls -l app/src/main/jniLibs/*/libdocscanner_native.so
```

Success criteria, in logcat after applying a filter once:

```
adb logcat -s DocumentFilters NativeFilters
I DocumentFilters: Rust kernels verified byte-identical to the Kotlin reference
```

If it says "unavailable or mismatched", the app silently keeps using Kotlin. That
is a *correct* app, not a broken one, but it means the `.so` was not loaded (wrong
ABI, missing NDK) or a kernel drifted. **Never** "fix" a mismatch by loosening the
comparison — the byte-for-byte check is the entire safety argument.

**Do not commit** `app/src/main/jniLibs/`: it is generated and git-ignored.

### 2. ML Kit text recognition — nothing to do

Added as a Gradle dependency (`com.google.mlkit:text-recognition:16.0.1`, bundled
Latin model, pinned in `gradle/libs.versions.toml`). The first build downloads a
few MB from Google Maven; the model ships **inside the APK**, so recognition works
offline and never depends on a Play Services update. Expect the APK to grow by
roughly that amount — do not swap this for the Play-Services variant to save size,
it would make search fail offline.

Verification: scan a page with text, wait a second, open the viewer's text sheet —
the recognised text is there; searching one of those words on the home screen
returns the document with the matching sentence under its title.

### 3. Semantic search model — NEXT INCREMENT, download when asked

Not wired yet, deliberately: inference cannot be verified in the Arena sandbox, and
an unverified model path is exactly how wrong output ships. When the next round
implements the re-rank it needs one asset:

- file: `app/src/main/assets/models/universal_sentence_encoder.tflite`
- source: MediaPipe TextEmbedder — Universal Sentence Encoder (float32):
  `https://storage.googleapis.com/mediapipe-models/text_embedder/universal_sentence_encoder/float32/latest/universal_sentence_encoder.tflite`
  (confirm the URL on the MediaPipe model page; it should be a ~6 MB `.tflite` —
  if the size is wildly different, stop and ask instead of guessing)
- record `sha256sum` of the downloaded file in this section before it is used
- `app/src/main/assets/models/` is git-ignored: the asset is *produced*, never
  committed (AGENTS.md §2.5)
- the build will also need `androidResources { noCompress += "tflite" }` so the
  file can be memory-mapped, plus a MediaPipe Tasks Text dependency. Both land in
  the same change that uses them, not before — an unused dependency is a liability.

### 4. Behaviour notes for the next agent

- `ScannerRepository.setPageOcrText` no longer bumps `modifiedDate`. It used to go
  through `updatePage`, so background recognition reordered the library while the
  user watched it.
- Recognition is bounded to one batch of 24 pages per app start
  (`OCR_BACKFILL_BATCH`). Keep that bound: an unbounded sweep turns launching the
  app on a large library into a CPU burn.
- `DocumentSearch.Index` is rebuilt when `documents` changes, never when the query
  changes. Reuse it the same way if another screen gets a search field.
- Search results are ordered by relevance, so the sort control is ignored while a
  query is active. That is intentional.

## 7. Known-good state

Last verified build: **Verified by Antigravity** on 2026-10-25 (`./gradlew assembleDebug testDebugUnitTest` passed).
Fixed DocumentDetector.kt compiler error; verified Settings screen, slide transitions, zoom, and test suite. APK built successfully at `app/build/outputs/apk/debug/app-debug.apk` (versionCode 5, versionName 1.4).


