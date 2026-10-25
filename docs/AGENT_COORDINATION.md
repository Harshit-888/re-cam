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
| Writes to | the current `arena/*` branch, pushed to `origin` | local working tree |
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
| Arena.ai & Antigravity | ✅ merged & verified (Round 19) | `model/Models.kt`, `ui/dashboard/DashboardScreen.kt`, `ui/common/PageThumbnail.kt`, `util/PageImageLoader.kt`, `ui/camera/ScannerCameraScreen.kt`, docs | Home-screen smoothness (immutable models so cards skip, dates formatted once, search debounced off-thread, single-flight thumb loader) and redesigned camera chrome. Fixed SearchBarField TextFieldDefaults.colors invocation. All unit tests pass, `app-debug.apk` built (65.5 MB). |
| Arena.ai & Antigravity | ✅ merged & verified (Round 20) | `util/ThumbnailStore.kt` (new), `util/PerfTrace.kt` (new), `util/ImageStore.kt`, `util/PageImageLoader.kt`, `ui/dashboard/DashboardScreen.kt`, `data/ScannerRepository.kt`, `MainActivity.kt`, docs | Dedicated 768px ThumbnailStore, background priming, ML Kit scroll-pause, and PerfTrace. Fixed DashboardScreen visibleItemsInfo smart-casting. All unit tests pass, `app-debug.apk` built (65.5 MB). |
| Arena.ai & Antigravity | ✅ merged & verified (Round 21) | `ui/common/PageThumbnail.kt`, `ui/common/Expressive.kt`, `ui/dashboard/DashboardScreen.kt`, `util/PageImageLoader.kt`, `util/ThumbnailStore.kt`, docs | Library scroll raised to settings-list smoothness: no per-tile animations, no `Surface`/`composed` modifiers in lazy items, synchronous cache reads on the first frame, memoised thumbnail keys, row-quantised prefetch. See §6l. |
| Arena.ai & Antigravity | ✅ merged & verified (Round 22) | `ui/dashboard/DashboardScreen.kt`, `ui/common/PageThumbnail.kt`, `util/PerfTrace.kt`, docs | Overdraw removed (transparent Scaffold, footer-only card background, placeholder-only cover fill) and one perf log line per scroll gesture. See §6m. |
| Arena.ai & Antigravity | ✅ merged & verified (Round 23) | `ui/editor/CropAndFilterScreen.kt`, `data/ScannerRepository.kt`, docs | The batch border pass now auto-detects every page instead of making the user walk them; the strip marks the pages the detector was unsure about. Additive `setPageCorners`. Fixed `needsCheck` param in `BordersControls`. See §6n. |
| Arena.ai & Antigravity | ✅ merged & verified (Round 24) | `ui/dashboard/DashboardScreen.kt`, `util/ThumbnailStore.kt`, `util/PerfTrace.kt`, `MainActivity.kt`, `app/build.gradle.kts`, docs | First-fling warm-up, two-way non-blocking prefetch, single-pass thumbnail decode at the tile size, per-frame stage attribution and non-debuggable `benchmark` build type. Fixed `visibleItemsInfo` layout branch and `COMMAND_ISSUE_DURATION`. See §6o. |
| Arena.ai | 🚧 in review (Round 25) | `data/LibraryStore.kt`, `data/SettingsStore.kt`, `data/ScannerRepository.kt`, `util/ImageStore.kt`, `util/PageImageLoader.kt`, `ui/camera/ScannerCameraScreen.kt`, `ui/editor/CropAndFilterScreen.kt`, `ui/dashboard/DashboardScreen.kt`, `ui/detail/DocumentDetailScreen.kt`, `app/src/test/.../OrphanSweepTest.kt`, docs | Data-loss fixes (atomic library writes, flush on close, discarded scans and orphan page files deleted, editor geometry saved on exit), a prefetch priority gate, and a wider/earlier startup warm-up. See §6p. |

| Arena.ai | 🚧 in review (Round 26) | `model/Models.kt`, `util/PdfGenerator.kt` (rewritten), `util/PdfWriter.kt` (new), `util/PdfSecurity.kt` (new), `ui/pdf/PdfExportSheet.kt`, `ui/dashboard/DashboardScreen.kt`, `ui/detail/DocumentDetailScreen.kt`, `app/src/test/.../PdfWriterTest.kt` + `PdfSecurityTest.kt` (new), docs | Password-protected PDFs, choose-the-size compression, rename at share time, and multi-select → merge from the library. **`PdfExportSheet` and `PdfGenerator.generatePdf` signatures changed** — see the changelog. See §6q. |

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
- `PageImageLoader` maintains a disk thumbnail cache for requests ≤ its stored
  thumbnail size. Anything larger (viewer, editor) deliberately bypasses it. If
  you add a screen that shows pages at ≥ 768 px, expect a full decode.
  **Superseded in round 20 — see §6k:** the cache moved from `cacheDir/thumbs` to
  `filesDir/thumbs`, is written when the page is saved rather than on first
  display, and is shared by every UI size instead of being keyed by request size.

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

## 6j. Home smoothness + camera chrome (claimed by Arena, 2026-10-25, round 19)

### Nothing to provision

No new dependency, no model file, no native artefact, no NDK work. §6i is still the
open contract (Rust toolchain + the optional embedding asset) and none of it
changed in this round.

### Four invariants that this round depends on — please don't undo them

1. **`QuadCorners`, `ScannedPage`, `Document` and `Folder` are `@Immutable`.**
   Compose cannot prove `List<…>` immutable, so without the annotation every card
   in the library is unskippable and any recomposition of the dashboard redraws
   every visible thumbnail. If a field ever has to change, replace the instance
   (`copy(...)`) and emit a new list — the repository already does exactly that.
   Adding a `var` or a `MutableList` field would invalidate the annotation.
2. **`DocumentSearch.Index` is not built in composition.** It is built on the first
   query of a session, on `Dispatchers.Default`, inside a `LaunchedEffect`. The
   OCR backfill emits one library snapshot per page, so a `remember(documents)`
   index would rebuild the whole thing up to 24 times at startup.
3. **`PageImageLoader.load` is single-flight and must stay that way.** Waiters must
   await *outside* the `decodeDispatcher` limiter — `limitedParallelism` holds its
   permit for the whole coroutine body, so a waiter that parks inside it can
   deadlock the decoder it is waiting for.
4. **`PageImageLoader` returns shared, already-downscaled bitmaps.** Callers must
   not recycle or mutate them (`applyEnhancements` never recycles its source, which
   is why it is safe). The bitmap is sized to the request, so raising
   `reqWidth`/`reqHeight` at a call site raises memory per cached card.

### Behaviour notes

- `DocumentGridCard` and `DocumentListCard` take `dateLabel: String`; the dashboard
  formats every label once per library change (`DateLabels`). A new card call site
  must pass one rather than formatting a date inside the card.
- Camera bind failures set `cameraError` (persistent card) rather than
  `statusMessage` (self-dismissing pill). Keep the split: transient advice
  auto-retires, a dead viewfinder must not.
- The camera shows a discard confirmation when leaving with unsaved pages
  (`BackHandler` + close button → `requestClose()`).

## 6k. Library scroll jank, round 2 (claimed by Arena, 2026-10-25, round 20)

### Nothing to provision

No new dependency, no model asset, no native change, no NDK work. (Rust was
explicitly considered for this and rejected — see `docs/RUST_MIGRATION_PLAN.md` §7:
the scroll path has no pixel loop, and `BitmapFactory` is already C++.)

### The diagnosis, in one line

The library was decoding **whole pages to draw thumbnails**, because nothing had
ever written a thumbnail. PNG pages (*Clear B&W*, *Grayscale*) have no cheap
sampled decode, so each tile cost tens of milliseconds and ~30 MB of transient
allocation while the user scrolled — and background OCR plus ~24 full-library
emissions were running through the same first scroll.

### Contracts other code must not break

1. **`ThumbnailStore` is the only place page covers live.** `filesDir/thumbs`, one
   ~768 px JPEG per page, keyed `sha1(imageUri#length:mtime)`. Do not write
   thumbnails anywhere else, and do not cache them by page id — the mtime is what
   makes a re-bake invalidate the old cover with no bookkeeping.
2. **Any new page-write path must call `ThumbnailStore.writeFor(...)`** right after
   it writes the page file, while the bitmap is still in hand. The three that exist
   (`importFromUri`, `importFromFile`, `saveEditedPage`) all do. A page written
   without one is not broken — it falls back to a full decode the first time it is
   displayed, which is the exact cost this round removed.
3. **A request larger than `ThumbnailStore.LONG_EDGE` (768) deliberately bypasses
   thumbnails** and decodes the page: that is the editor and the viewer. Keep grid
   and strip requests under 768.
4. **`ImageStore.writeJpeg` / `writePng` must keep stamping `nextStamp()`** rather
   than `currentTimeMillis()`: both caches key on the mtime, and a same-millisecond
   re-bake would otherwise keep the pre-edit cover.
5. **`setPageOcrTexts` (plural) is the write path for recognition results** — one
   update per document. `setPageOcrText` still exists and delegates, but a loop of
   single-page calls is what made launch emit two dozen library snapshots.
6. **`backfillTextInBackground(isBusy = ...)` keeps being called with a scroll
   predicate** from the dashboard, and keeps its 3 s start delay. ML Kit inference
   on two cores is enough to drop frames in the list, and that is not the list's
   fault.

### `DocumentGridCard` signature change

`coverSize: IntSize` is now a **required** parameter (it used to be a hard-coded
`reqWidth = 440`, which was smaller than the tile it filled on a 3x display).
Compute it with `rememberGridCoverSize()` in the dashboard: it derives the cell from
`LocalConfiguration.screenWidthDp` and the grid's own padding/gap constants, so the
two cannot drift apart.

### Measuring it

`adb logcat -s DocScannerPerf` on a debug build. Every 120-frame window prints
frame percentiles plus how each cover was obtained; `JANK` lines are the ones to
look at, and a large `pageDecodes` count there means some page still has no stored
thumbnail.

## 6l. Library scroll, round 3 — "as smooth as settings" (Arena, 2026-10-25, round 21)

### Nothing to provision

No dependencies, no assets, no native changes, no APIs.

### What this round assumes (do not undo)

1. **`PageThumbnail` has no fade-in on purpose.** It costs a running animation per
   tile per appearance. If a fade is ever wanted back, it must not be a per-item
   `animateFloatAsState`.
2. **Lazy items must not use `Surface` or any `composed` modifier**
   (`pressScale`, `entrance`, …). `Box` + `clip` + `background` + `clickable(ripple)`
   is the house pattern for anything inside a `LazyColumn`/`LazyVerticalGrid`.
   `pressScale`/`Surface` stay fine on static chrome (buttons, pills, the FAB).
3. **`PageImageLoader.peek(...)` is the synchronous, composition-safe memory-cache
   read.** `rememberPageImage` seeds `produceState` with it so a tile paints its
   photo in its first frame. It is only correct because `ThumbnailStore` memoises
   the key; if that memo is removed, `peek` silently stops working.
4. **`ThumbnailStore.key` is memoised** and must be refreshed by `writeFor` (which
   re-stats on purpose). Anything that rewrites a page file outside `ImageStore`
   has to call `ThumbnailStore.invalidate(uri)`.
5. **The prefetch loop is quantised to rows and uses `collect`.** Keying it on the
   last visible *item* with `collectLatest` restarts it constantly and cancels its
   own decodes.

### Rule of thumb for this screen

If a change adds work — any work — to the *first frame of a tile*, it is a
regression on this screen even if it looks nicer. The user's benchmark is the
settings list, which does none.

## 6m. Overdraw + scroll telemetry (Arena, 2026-10-25, round 22)

### Nothing to provision, but one thing only Antigravity can do

Everything in this round is code. The one remaining performance lever **cannot be
built in Arena's sandbox** (no `adb`, no device):

**A baseline profile.** Roughly 20-40 % of first-run frame time on Android 7+, and
it AOT-compiles Compose itself rather than just app code.

1. `androidx.baselineprofile` plugin (Gradle) + a `com.android.test` module named
   `:baselineprofile` that depends on `:app`.
2. A `MacrobenchmarkRule` with `BaselineProfileRule` generating
   `GenerateBaselineProfile`, containing a `collectBaselineProfile` that starts
   `DashboardNav` and scrolls the library (`UiAutomator`: swipe up a few times,
   open a document, back).
3. `./gradlew :app:generateBaselineProfile` with a **release-like physical device**
   attached (emulators produce useless profiles).
4. Commit `app/src/main/baseline-prof.txt` (or the generated
   `:baselineprofile/src/main/baseline-prof.txt`) and keep `profileinstaller`
   installed in `:app`.

Please claim it in §4 before starting: it adds a Gradle module and a dependency.

### What this round assumes (do not undo)

1. **`Scaffold` on the dashboard is `Color.Transparent` on purpose.** The theme root
   `Surface` in `MainActivity` paints the screen; a second opaque full-screen rect
   is pure overdraw. If a screen ever stops painting its own background, give it
   one — do not re-enable the Scaffold fill.
2. **Card backgrounds belong to the footer, not the card.** `DocumentGridCard`
   paints `surfaceContainerLow` on its text column only; the cover area is bare and
   `PageThumbnail(placeholder = …)` supplies the grey until the photo exists.
   Painting a card-wide background again re-introduces one full-tile fill per tile
   per frame.
3. **`PageThumbnail(placeholder = …)` is placeholder-only** — it is applied when
   there is no image and nothing is drawn under the image.

### Measuring a scroll

`PerfTrace.setScrolling(...)` is wired to the dashboard's `isScrollInProgress`. On a
debug build each gesture prints exactly one line:

```
I DocScannerPerf: JANK scroll frames=214 slow=3 avg=17.4ms worst=41.2ms cacheHits=96 thumbs=8 pageDecodes=0
```

Read it in this order: `slow` (frames > 24 ms) → `pageDecodes` → `worst`. Any future
"the list is laggy" report should start with this line.

## 6n. Batch border pass (Arena, 2026-10-25, round 23)

### Nothing to provision

### The flow, as it now behaves

1. Camera → *Done* → review opens on page 1 with its borders already detected.
2. ~450 ms later a background pass detects **every other page that has never been
   cropped** (≤ `AUTO_DETECT_LIMIT = 40`), writing them in one repository update.
3. The page strip shows three states: filled = the user confirmed it, accent = the
   detector was confident, amber **"!"** = it was not. Work the exclamation marks,
   not the page numbers.
4. *Continue to filters* → one filter → one bake over all pages, with progress.

### Rules that make it safe (do not break these)

- **Auto-detection never touches a page whose corners are not `isFullFrame`.** That
  single condition is what stops the pass from silently re-cropping a page the user
  already adjusted — in the pass, in the on-load detection, and at write time.
- **The pass writes once, via `setPageCorners`.** Per-page `updatePage` calls would
  emit one library snapshot per page (the lesson from the OCR backfill).
- **The pass decodes at `DETECT_ALL_DIMENSION = 720`** and recycles each bitmap in a
  `finally`. Do not raise it to preview size: the detector only reads 360 px.
- **`PageChip(needsReview = …)` must keep distinguishing *confident* from *user
  confirmed*.** Showing an unconfident detection as "done" is a lie the user only
  finds in the exported PDF.
- **Leaving a page is signing it off** (`persistGeometry` → `borderedPages[id] =
  true`). That is the only way a flagged page becomes clean, which is why the
  primary button routes *to* flagged pages instead of past them: `everyPageBordered
  -> "Continue to filters"`, otherwise `"Check page N"` for the first page still
  flagged, otherwise `"Next page (i of N)"`. Do not let a flagged page be marked
  done by the batch pass — a page the detector was unsure about and nobody looked
  at must still show amber.
- The primary action jumps straight to filters once `everyPageBordered`.

### Deferred, deliberately

Per-page filters (a different filter per page within one batch) are **not**
implemented: one filter is applied to the document. That matches the requested flow
("… *then* apply filters") and the single bake pass. If per-page filters are wanted,
the bake loop in `applyFilterToPages` already iterates pages, so it is a UI change,
not a pipeline change.

## 6o. Dashboard: the first fling, and how to read a slow one (Arena, 2026-10-25, round 24)

### Nothing to provision

### What a tile now costs

1. **On screen:** the bitmap comes from the memory cache on the composition thread
   (`PageImageLoader.peek`) and is drawn 1:1, because the decoded bitmap is exactly
   the tile's pixel size.
2. **Just off screen:** prefetched two **rows** in each direction, launched without
   waiting for the previous row.
3. **At launch:** the first four rows are warmed into the memory cache 180 ms after
   the library appears (`WARM_ROWS` / `WARM_START_DELAY_MS`), so the first fling is
   not the one empty fling of the session.
4. **On disk:** one 768 px JPEG per page; `ThumbnailStore.read` scales *inside* the
   decode (`inDensity`/`inTargetDensity`), so no intermediate full-size bitmap.

### Rules (do not break these)

- **Never await a prefetch inside the row collector.** `prefetchRow` launches; the
  collector must stay free to see the next row boundary. Awaiting made prefetch lag
  one frame behind a fast fling.
- **Never let `ThumbnailStore.read` return something bigger than the request** when
  it can avoid it: the tile path is only cheap because the bitmap needs no
  resampling at draw time (see the `inDensity` block).
- `WARM_ROWS` work must stay **outside** the idle gate — the point is to finish
  before the first gesture, not after it.

### Reading a slow scroll (`adb logcat -s DocScannerPerf`)

The gesture line is:

```
[JANK|smooth] scroll frames=… slow=… avg=… worst=… stages(sync/layout/draw/cmd/gpu)=…/…/…/…/…ms cacheHits=… thumbs=… pageDecodes=…
```

- `sync` + `layout` large (say > 8 ms each): the cost is **composition/layout** —
  make the tile cheaper, or reduce how many tiles cross the viewport per frame.
- `draw` + `cmd` + `gpu` large with `sync`/`layout` small: the cost is **pixels** —
  texture size and fill rate. Next levers, in order: decode covers at 90 % of the
  cell (imperceptible, 20 % fewer bytes), then `RGB_565` for grid/list covers only
  (the bake and PDF paths must stay ARGB_8888).
- `pageDecodes` non-zero on a warm library: a fallback path is being taken, meaning
  a thumbnail is missing or its key changed.
- Everything small but `avg` still ~16.7 ms with `slow` > 0: the frames are being
  delivered late for a reason outside the app's main thread — check a non-debug
  build with `./gradlew assembleBenchmark` before optimising anything else.

### The debug-build question

`assembleDebug` produces a **debuggable** APK: interpreted/JIT'd code paths and live
Compose debug hooks. That penalty is multiplicative and hits the heaviest screen
first, which is the whole reason a photo grid can trail a text list in a debug build
and match it in release. The `benchmark` build type (non-debuggable, unminified,
debug-signed, same application id) is the honest comparison:

```
./gradlew assembleBenchmark
adb install -r app/build/outputs/apk/benchmark/app-benchmark.apk
```

## 6p. Storage safety, and the first fling (Arena, 2026-10-25, round 25)

### Nothing to provision

### The rules that now protect user data

- **`LibraryStore.writeNow` / `SettingsStore.writeNow` rename over the target; they
  never delete it first.** A delete-then-rename leaves an instant with no library on
  disk, and a kill inside it silently starts the app from an empty library.
- **`ScannerRepository.close()` flushes before it cancels its scope.** Every mutation
  is persisted through a 150 ms debounce, so cancelling without a flush loses the
  last edit. Do not "optimise" the blocking write away.
- **A page's file can have three names** — `pages/<id>.jpg`,
  `pages/<id>_edited.jpg`, `pages/<id>_edited.png`.
  `ScannerRepository.referencedPageFiles()` returns all of them for every page of
  every document (trash included) plus the model's own URIs. **Anything that builds
  a "keep" set must go through it**; sweeping on `imageUri` alone deletes the
  pristine capture of every edited page.
- **`ImageStore.sweepOrphans` keeps anything modified in the last ten minutes**
  (`ORPHAN_GRACE_MS`). The camera writes a page at the shutter press, before any
  document references it. The decision is `isOrphanedPageFile(...)`, pinned by
  `OrphanSweepTest`.
- **Discarding a scan must delete its pages**: `repository.discardPages(pages)`. The
  camera has already written them; nothing else will ever remove them.
- **The editor persists geometry on dispose** (`DisposableEffect`), not only on a page
  switch. It compares against the stored values first, so an exit with no edit does
  not move `modifiedDate`.

### Image loading priorities

- `PageImageLoader.prefetch` is gated to **one** decode slot. Visible tiles always
  have the other. Any future speculative loader must be gated the same way.
- `prefetch` re-checks `peek` before spending a slot, and the row collector in
  `DashboardScreen` skips hits itself — a `peek` hit must never cost a coroutine.

### First fling after launch

Order of events on a cold start, and what each step is allowed to cost:

| when | work | thread |
| :--- | :--- | :--- |
| composition | first screen's tiles request covers | main, then decode (2 slots) |
| +48 ms | `WARM_ROWS = 6` rows warmed into the memory cache | decode (1 slot) |
| +1.2 s, idle | stored thumbnails built for pages that predate the store | IO |
| +3 s, idle | ML Kit text backfill (pauses the moment a scroll starts) | IO |
| +5 s | orphan page-file sweep | IO |

None of it touches the main thread, and everything after the first row is cancellable
the moment a gesture begins.

### Still the biggest lever, and it needs a device

A debuggable APK runs interpreted/JIT'd code; the first scroll after launch is the
one that pays for compiling the hot path. `./gradlew assembleBenchmark` (see
`app/build.gradle.kts`) is the honest comparison, and a real **baseline profile**
(`androidx.baselineprofile` + a `:baselineprofile` module + a `BaselineProfileRule`
that starts the dashboard and swipes the library, generated on a physical device,
`baseline-prof.txt` committed) is the only thing that removes that cost instead of
hiding it. It cannot be produced in this sandbox: it needs `adb` and real hardware.

## 6q. PDF output: password, size, name, merge (Arena, 2026-10-25, round 26)

### Nothing to provision

No new dependency, no new asset, no new binary. `pypdf`/`pypdfium2` were used in
Arena's sandbox to *validate the file format* and are not part of the app.

### Why there is a second PDF engine

`android.graphics.pdf.PdfDocument` cannot encrypt, and it always embeds page
images losslessly — which is exactly why a photographed page becomes a
multi-megabyte attachment. There is also no PDF library anywhere in the original
APK (`classes*.dex` contains only `com.shockwave.pdfium`, a *viewer*), so there was
nothing to copy: `util/PdfWriter.kt` is written from the specification.

The platform engine is still the default. `PdfGenerator.generatePdf` picks:

| condition | engine | why |
| :--- | :--- | :--- |
| no password, `compression == ORIGINAL` | `PdfDocument` | byte-identical to every previous round — no regression on the ordinary export |
| password, or any lossy level, or a merge | `PdfWriter` | it can encrypt and it chooses the image encoding |

**Do not "simplify" this by deleting the platform path.** It is the only output
that a device has actually been tested against at 300 DPI, and it is the reason a
bug in the new writer can only ever affect an export the user explicitly opted
into.

### The one invariant both engines share

`pdfPageGeometry(config, imageWidth, imageHeight)` (in `util/PdfGenerator.kt`) is
the only place that decides where a page lands. Both engines call it. If you change
paper, margins, orientation, `ImageScale` or the footer position, change it there —
changing one engine's arithmetic would make an export with a password place the
page somewhere else than the same export without one.

Note that compression downscales the bitmap **after** the geometry is computed, so
`Tiny` cannot move a page: it only lowers the pixel density inside the same
rectangle.

### Bitmap ownership on the writer path (relates to audit bug 8)

The writer path decodes, lays out, encodes and **recycles** one page at a time. It
is safe there because the encoded bytes are in hand before anything is recycled.
The platform path still does not recycle (bug 8 stays open): `PdfDocument` may defer
rasterising a drawn bitmap until `writeTo`, so recycling after `finishPage` could
still blank a page. **Do not copy the writer path's `finally { recycle() }` into the
platform path.**

### Reading the scroll numbers without a cable

`PerfTrace` mirrors its per-gesture and per-window lines into two `StateFlow`s, and
`ui/settings/SettingsScreen.kt` renders them in a **Performance (debug)** card that
exists only when the app is debuggable. If a scroll is still not smooth, that card
is the whole diagnosis: `avg` near 17 ms with `slow=0` is a 60 Hz screen doing its
job; `draw`/`cmd`/`gpu` dominating the stage averages means pixels and textures,
not composition; `sync`/`layout` dominating means our own composition.

### What Antigravity should check

1. `./gradlew testDebugUnitTest` — the two new test files are pure JVM and need no
   device. `PdfSecurityTest` pins the *exact* `/O`, `/U` and per-object key bytes
   that PDFium, MuPDF and pypdf all accepted; if a refactor changes them the export
   stops opening, and the test says so first. `PdfWriterTest` writes a real file and
   walks its cross-reference table, because a PDF is a format with offsets in it and
   no compiler checks those.
2. `./gradlew assembleDebug`, then on a device:
   - export a scan with a password → open the shared PDF in Chrome or a reader →
     it must ask for the passphrase, and the passphrase must be accepted;
   - export the same scan at each of the four size levels → the toast prints the
     real file size, and `Balanced` should be dramatically smaller than `Original`;
   - long-press a card on the library screen → pick two → `Merge PDF` → the sheet
     lists the order → the output is one PDF with both documents' pages.
3. Anything that looks wrong in the *new* path only: the platform path is
   untouched, so compare against an `Original`/no-password export.

### Deliberate limitations

- The writer draws text with base-14 Helvetica, so it is printable-ASCII only.
  Page numbers, the failure notice and `/Title` go through it; **watermarks do
  not** — they are composited into the page bitmap so they keep their Unicode.
- `/R 3` RC4 128-bit is used instead of AES. It is what every reader supports
  without shipping a hand-written AES. It protects a shared file from being opened;
  it is not a vault.
- Merges default to `Balanced`. The sheet's compression control is visible and the
  default is never re-applied after the fact.

## 7. Known-good state

Last verified build: **Verified by Antigravity** on 2026-10-25 (`./gradlew assembleDebug testDebugUnitTest` passed).
Verified Rounds 21–24: Fixed `visibleItemsInfo` smart-cast in `DashboardScreen.kt`, `needsCheck` parameter in `CropAndFilterScreen.kt`, and `COMMAND_ISSUE_DURATION` in `PerfTrace.kt`. APK built successfully at `app/build/outputs/apk/debug/app-debug.apk` (65,384,497 bytes).


