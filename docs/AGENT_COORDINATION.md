# Agent Coordination Protocol — Arena.ai ⟷ Antigravity

> **Two AI agents build this app in parallel.** Arena.ai works through Git on the
> `arena/*` branch; Antigravity works directly in the local IDE. They share one
> working tree. This document is the contract that keeps their edits from
> colliding. Read it before you touch a file.

---

## 1. The three rules

1. **Claim before you edit.** Add a row to §4 _In-flight changes_ naming the
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

|                        | Arena.ai                                                         | Antigravity                                |
| :--------------------- | :--------------------------------------------------------------- | :----------------------------------------- |
| Writes to              | the current `arena/*` branch, pushed to `origin`                 | local working tree                         |
| Verifies by            | code review only — **no JDK / Android SDK in the Arena sandbox** | `./gradlew assembleDebug` + device install |
| Must do before pulling | —                                                                | `git stash` or commit local work first     |

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

| Symbol                                                                   | Kind          | Semantics                                                                                        |
| :----------------------------------------------------------------------- | :------------ | :----------------------------------------------------------------------------------------------- |
| `ScannerRepository(context)`                                             | ctor          | Reads `library.json` on construction. No global singleton.                                       |
| `ScannerRepository.documents` / `.folders`                               | `StateFlow`   | Single source of truth for the whole UI                                                          |
| `ScannerRepository.movePage(docId, from, to)`                            | fun           | Page reordering                                                                                  |
| `ScannedPage.imageUri`                                                   | field         | **Absolute filesystem path** of the display JPEG (may be the edited bake)                        |
| `ScannedPage.originalImageUri`                                           | field         | Pristine original JPEG; set on first bake. `null` = never edited                                 |
| `ImageStore.resolveFile(ctx, imageUri)`                                  | fun           | Tolerant resolver (abs path / `file://` / bare filename). `null` for un-imported `content://`    |
| `ImageStore.pageFile(ctx, pageId)`                                       | fun           | Canonical `filesDir/pages/<pageId>.jpg`                                                          |
| `ImageStore.loadBitmap(ctx, imageUri, w, h)`                             | fun           | Sampled decode, orientation-corrected                                                            |
| `rememberPageImage(imageUri, w, h)`                                      | `@Composable` | Async decode → `ImageBitmap?`                                                                    |
| `PageThumbnail(page, modifier)`                                          | `@Composable` | Standard page preview with icon fallback                                                         |
| `ImageProcessingUtils.applyEnhancements(src, filter, rotation, corners)` | fun           | warp → rotate → filter, in that order                                                            |
| `DocumentFilters.apply(src, filter, maxDimension)`                       | fun           | The real filter pipeline. Returns `src` for ORIGINAL/failure — compare identity before recycling |
| `DocumentFilters.applyPreview(src, filter)`                              | fun           | Same pipeline at preview resolution (WYSIWYG)                                                    |
| `DocumentDetector.detect(bitmap, context)`                               | fun           | **Preferred.** `Detection(corners, confident)` — TFLite ML model + geometric fallback; never throws |
| `DocumentDetector.detectQuad(bitmap, context)`                           | fun           | Compatibility shim for `detect().corners`                                                        |
| `DocumentFilters.stretchPoints(hist)`                                    | fun           | Clamped black/white points; the blow-out guard                                                   |
| `PerspectiveWarp.detectQuad(bitmap)`                                     | fun           | Thin delegate to `DocumentDetector` (kept for compatibility)                                     |
| `PerspectiveWarp.warp(src, corners)`                                     | fun           | `Matrix.setPolyToPoly` projective warp                                                           |
| `PdfGenerator.generatePdf(ctx, doc, config, bitmapProvider)`             | fun           | Blocking; **call from `Dispatchers.IO` only**                                                    |
| `SignaturePadDialog(onDismiss, onSignatureConfirmed)`                    | `@Composable` | Callback receives a **PNG path**, not an opaque token                                            |
| `CropAndFilterScreen(docId, pageId, repository, onDone, reviewMode)`     | `@Composable` | `reviewMode = true` opens on **Borders** with auto-detection and walks the document page by page |
| `DocumentNaming.defaultScanTitle()`                                      | fun           | **The only** sanctioned source of scan titles — it quotes pattern literals                       |
| `QuadCorners.FULL_FRAME`                                                 | const         | Means "no warp". Auto-detect falls back to it rather than cropping                               |
| ~~`ImageProcessingUtils.getColorMatrix`~~                                | **removed**   | Was a global `ColorMatrix`; superseded by `DocumentFilters`                                      |
| ~~`ImageProcessingUtils.getComposeColorFilter`~~                         | **removed**   | Editor now previews a real filtered bitmap                                                       |

## 4. In-flight changes

Keep this table short. Delete your row when the change lands.

| Agent       | Status          | Files                                                                                                                                                                                                                                                                           | Intent                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| :---------- | :-------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Arena.ai & Antigravity | ✅ merged & verified (Round 53) | `ui/common/ConnectedButtonGroup.kt`, `ui/detail/DocumentDetailScreen.kt`, `ui/viewer/PageViewerScreen.kt`, `ui/editor/MarkupStudio.kt` (new), `ui/editor/MarkupScreen.kt`, `ui/dashboard/DashboardScreen.kt`, `ui/pdf/PdfExportSheet.kt`, `ui/camera/ScannerCameraScreen.kt`, `util/DocumentShare.kt` (new), `util/ImageStore.kt`, `data/ScannerRepository.kt`, `Navigation.kt`, docs | Seven reported defects: connected page-tile verbs + hold-to-collapse, Copy/Move on hold, dashboard multi-select **Share**, in-place annotate shelf in the reader, instant rotate, collapsing dashboard header, export-sheet microinteractions, camera preview lifted 32 dp. Chrome/layout/motion only. See §6ap. |
| Arena.ai & Antigravity | ✅ merged & verified (Round 55) | `ui/common/ConnectedButtonGroup.kt`, `ui/common/ExpressiveAction.kt`, `ui/viewer/PageViewerScreen.kt`, docs | Annotate shelf control visibility fix (pen/erase/thin/bold): removed label-bounded ripple, dimmed ink only when disabled, M3 SegmentedButtons for tool/weight, translate-only entrance for action bar. Tested and verified. See §6ar. |
| Arena.ai & Antigravity | ✅ merged & verified (Round 54) | `ui/common/ConnectedButtonGroup.kt`, `ui/viewer/PageViewerScreen.kt`, `ui/camera/ScannerCameraScreen.kt`, `ui/dashboard/DashboardScreen.kt`, `ui/detail/DocumentDetailScreen.kt`, `ui/common/PageThumbnail.kt`, `theme/Shape.kt`, `util/ImageStore.kt`, `Navigation.kt`, docs | Second pass on the seven Round-53 reports: even shelf spacing, no crossfade on a filled cell, measured page inset, id-tracked page in the reader, capture button out of the FAB slot, outward seam under the search field, disabled (not hidden) tile verbs, half-screen move sheet, copied pages keeping their pixels. Chrome/layout/motion only. See §6aq. |
| Antigravity | ✅ committed | indymobile/*, docs/AGENTS_CHANGELOG.md, docs/AGENT_COORDINATION.md | Complete Indiana Mobile (Target 1) RE artifacts: base APK, native libs, Ghidra C sources, JADX Java sources, pipeline smali/java, and REA inspection report for Arena context. |
| Antigravity | ✅ committed    | `docs/ORIGINAL_APP_FILTER_SPEC.md`, `docs/AGENTS_CHANGELOG.md`                                                                                                                                                                                                                  | Reverse-engineered native filter spec & decompiled algorithms                                                                                                                                                                                                                                                                                                                                                                                                                            |
| Arena.ai    | ✅ committed    | `util/DocumentNaming.kt`, `ui/editor/CropAndFilterScreen.kt`, `Navigation.kt`, `NavigationKeys.kt`, `ui/dashboard/DashboardScreen.kt`, `ui/detail/DocumentDetailScreen.kt`, `ui/camera/ScannerCameraScreen.kt`, `util/PerspectiveWarp.kt`, `app/build.gradle.kts`, tests + docs | Done-button crash fix, post-capture review flow, folder move, note editing                                                                                                                                                                                                                                                                                                                                                                                                               |
| Arena.ai    | ✅ merged (1.7) | `util/DocumentFilters.kt`, `ui/editor/CropAndFilterScreen.kt`, `DocumentFiltersTest.kt`, `app/build.gradle.kts`, docs                                                                                                                                                           | Magic Color made uniform (mean-pooled estimator, soft knee), Clear B&W gets a clamped Sauvola refinement, Borders tap auto-detects, preview is warped+filtered. No public signature changed; `saturateGain`/`softKnee`/`boxMeanAndStd`/`sauvolaWindow` are new and `internal`.                                                                                                                                                                                                           |
| Arena.ai    | ✅ merged (1.8) | `util/DocumentFilters.kt`, `ui/editor/CropAndFilterScreen.kt`, `util/PageRenderer.kt`, `util/ImageProcessingUtils.kt`, `DocumentFiltersTest.kt`, `app/build.gradle.kts`, docs                                                                                                   | 1.8 — Clear B&W: 3 % decision ramp on a sharpened, noise-gated plane; editor preview now renders at the _bake_ dimension on the _warped_ bitmap, which also fixes the recycled-bitmap blank preview and the stale (uncropped) photo after editing borders. **Additive signature change:** `bake`/`bakeBlocking`/`applyEnhancements` gained `maxDimension` with a default; `bakeDimension(quality)` added. `gatedUnsharp`/`noiseEstimate`/`adaptiveSharpenAmount` are new and `internal`. |

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
| Arena.ai & Antigravity | ✅ merged & verified (Round 25) | `data/LibraryStore.kt`, `data/SettingsStore.kt`, `data/ScannerRepository.kt`, `util/ImageStore.kt`, `util/PageImageLoader.kt`, `ui/camera/ScannerCameraScreen.kt`, `ui/editor/CropAndFilterScreen.kt`, `ui/dashboard/DashboardScreen.kt`, `ui/detail/DocumentDetailScreen.kt`, `app/src/test/.../OrphanSweepTest.kt`, docs | Data-loss fixes (atomic library writes, flush on close, discarded scans and orphan page files deleted, editor geometry saved on exit), a prefetch priority gate, and a wider/earlier startup warm-up. Fixed `isOrphanedPageFile` grace boundary. See §6p. |
| Arena.ai & Antigravity | ✅ merged & verified (Round 26) | `model/Models.kt`, `util/PdfGenerator.kt` (rewritten), `util/PdfWriter.kt` (new), `util/PdfSecurity.kt` (new), `ui/pdf/PdfExportSheet.kt`, `ui/dashboard/DashboardScreen.kt`, `ui/detail/DocumentDetailScreen.kt`, `app/src/test/.../PdfWriterTest.kt` + `PdfSecurityTest.kt` (new), docs | Password-protected PDFs, choose-the-size compression, rename at share time, and multi-select → merge from the library. **`PdfExportSheet` and `PdfGenerator.generatePdf` signatures changed** — see the changelog. See §6q. |
| Arena.ai & Antigravity | ✅ merged & verified (Round 27) | `util/DocumentSearch.kt`, `util/NativeSearch.kt` (new), `app/src/main/rust/src/search.rs` + `lib.rs`, `ui/dashboard/DashboardScreen.kt`, `ui/settings/SettingsScreen.kt`, `app/src/test/.../MagicSearchTest.kt` (new), docs | "Magic Search": typo/prefix/stem/synonym/phrase/year matching with highlighted snippets, and the vocabulary walk in Rust behind a parity gate. **`SearchBarField` renamed to `MagicSearchField`.** Fixed `vocabulary.asList()` on `expandReference`. See §6r. |
| Arena.ai & Antigravity | ✅ merged & verified (Round 29) | `util/FramePacing.kt`, `util/FrameBudget.kt`, `util/PerfTrace.kt`, `MainActivity.kt`, `theme/Theme.kt`, `util/PageImageLoader.kt`, `ui/dashboard/DashboardScreen.kt`, `app/src/test/.../FrameBudgetTest.kt`, docs | Dynamic high-refresh display pacing (fastest same-resolution mode), display-aware frame budgets & jank detection, stable card callbacks, and RGB_565 tile decodes. Fixed `FramePacing.kt` display mode/refresh rate application. See §6s. |
| Antigravity | ✅ merged & verified (Round 30) | `app/src/main/rust/src/kernels.rs`, `app/src/main/rust/src/lib.rs`, `util/NativeFilters.kt`, `util/DocumentFilters.kt` | Rust acceleration of 6 more expensive filter loops: `meanPool` (paper map), `applyLut` (Lighten), `rec601InPlace` (Grayscale), `gaussianBlur3`, `boxBlur3`, `boxBlurInPlace` — all behind byte-for-byte parity gates with Kotlin reference fallbacks. ABI bumped to 2. `verifyNativeFilters` now checks all 9 kernels. All unit tests pass, `app-debug.apk` built (65,687,518 bytes). Reviewed in §6u. |
| Arena.ai & Antigravity | ✅ merged & verified (Round 33) | `data/LibraryViewModel.kt` (new), `MainActivity.kt`, `Navigation.kt`, `data/ScannerRepository.kt`, `theme/Motion.kt`, `ui/dashboard/DashboardScreen.kt`, `ui/detail/DocumentDetailScreen.kt`, `ui/editor/CropAndFilterScreen.kt`, `ui/viewer/PageViewerScreen.kt`, `ui/camera/ScannerCameraScreen.kt`, `ui/signature/SignaturePadDialog.kt`, `ui/common/Expressive.kt`, docs | Six behaviour bugs (empty documents, unconfirmed page delete, detail dead-end, stale selection/no-op merge, silent rename discard, per-rotation repository rebuild), five hot paths moved to draw-phase reads / layer transforms / coalesced IPC, four motion corrections. Fixed compilation issues in DashboardScreen, DocumentDetailScreen, and PageViewerScreen. All unit tests pass, `app-debug.apk` built. See §6v. |
| Arena.ai & Antigravity | ✅ merged & verified (Rounds 34 & 35) | `ui/pdf/PdfExportSheet.kt` (rebuilt), `ui/common/Expressive.kt`, `ui/camera/ScannerCameraScreen.kt`, `ui/dashboard/DashboardScreen.kt`, `ui/detail/DocumentDetailScreen.kt`, `ui/trash/TrashScreen.kt`, `ui/viewer/PageViewerScreen.kt`, `docs/design/*` | Export sheet UI/UX rebuilt (collapsible paper grid, 2x2 cards, whole-row toggles, TalkBack semantics), `SegmentedPill` unified across screens, UI motion on state transitions (top bar, lists, viewer, camera), and camera page counter Undo. Fixed `ScannerCameraScreen.kt` compilation errors. All unit tests pass, `app-debug.apk` built. See §6w & §6x. |
| Arena.ai & Antigravity | ✅ merged & verified (Rounds 36 & 37) | `util/CropHandles.kt` (new), `util/PdfSizeEstimate.kt` (new), `util/StartupWork.kt` (new), `ui/editor/CropAndFilterScreen.kt`, `ui/dashboard/DashboardScreen.kt`, `ui/pdf/PdfExportSheet.kt`, `data/ScannerRepository.kt`, `MainActivity.kt`, tests + docs | PDF export sheet size estimate (KB/MB), cold-start quiet gate (`StartupWork`), in-filter page switcher (shared `PageStrip`), and crop handle drag physics/touch-slop (`CropHandles`). All unit tests pass, `app-debug.apk` built. See §6y & §6z. |
| Arena.ai & Antigravity | ✅ merged & verified (Rounds 38 & 39) | `app/build.gradle.kts`, `util/MlBorderDetector.kt`, `util/QuadSanity.kt` (new), `util/DocumentDetector.kt`, `MainActivity.kt`, tests + docs | TFLite border detection uncompressed (`noCompress`), dynamic tensor parsing, `QuadSanity` plausibility validation, `VerifyBorderModelTask` build-time check. Fixed `VerifyBorderModelTask` Gradle configuration-cache compatibility. All unit tests pass, `app-debug.apk` built. See §6aa & §6ab. |
| Antigravity | ✅ committed | `docs/ORIGINAL_APP_FULL_SPECIFICATION.md`, `docs/AGENTS_CHANGELOG.md` | Master reverse engineering specification: 34 original activities, SQLite schemas, 50 paper sizes, camera states (`PaperBorderView`), and editor tools (`CleanupBgView`, `MarkupView`, `SignaturePad`). |

## 5. File ownership map

Rebuilt 2026-10-25 after the "make it real" pass.

| Area | Files | Notes |
| :--- | :--- | :--- |
| App shell | `MainActivity.kt`, `Navigation.kt`, `NavigationKeys.kt`, `data/LibraryViewModel.kt` | The repository is created by `LibraryViewModel` (Round 33; it used to be a `MainActivity` field, which rebuilt and leaked it on every rotation) and passed down as a `StateFlow` — **do not re-introduce a singleton**, and do not give it to `Application` |
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
- Lazy items must not use `Modifier.entrance(...)`: it composes an animation _and_
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
  - sides are found on a _closed + blurred_ background image, never on raw
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
Everything below is what the _environment_ needs — no code changes.

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
is a _correct_ app, not a broken one, but it means the `.so` was not loaded (wrong
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
- `app/src/main/assets/models/` is git-ignored: the asset is _produced_, never
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
   await _outside_ the `decodeDispatcher` limiter — `limitedParallelism` holds its
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
ever written a thumbnail. PNG pages (_Clear B&W_, _Grayscale_) have no cheap
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
   last visible _item_ with `collectLatest` restarts it constantly and cancels its
   own decodes.

### Rule of thumb for this screen

If a change adds work — any work — to the _first frame of a tile_, it is a
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

1. Camera → _Done_ → review opens on page 1 with its borders already detected.
2. ~450 ms later a background pass detects **every other page that has never been
   cropped** (≤ `AUTO_DETECT_LIMIT = 40`), writing them in one repository update.
3. The page strip shows three states: filled = the user confirmed it, accent = the
   detector was confident, amber **"!"** = it was not. Work the exclamation marks,
   not the page numbers.
4. _Continue to filters_ → one filter → one bake over all pages, with progress.

### Rules that make it safe (do not break these)

- **Auto-detection never touches a page whose corners are not `isFullFrame`.** That
  single condition is what stops the pass from silently re-cropping a page the user
  already adjusted — in the pass, in the on-load detection, and at write time.
- **The pass writes once, via `setPageCorners`.** Per-page `updatePage` calls would
  emit one library snapshot per page (the lesson from the OCR backfill).
- **The pass decodes at `DETECT_ALL_DIMENSION = 720`** and recycles each bitmap in a
  `finally`. Do not raise it to preview size: the detector only reads 360 px.
- **`PageChip(needsReview = …)` must keep distinguishing _confident_ from _user
  confirmed_.** Showing an unconfident detection as "done" is a lie the user only
  finds in the exported PDF.
- **Leaving a page is signing it off** (`persistGeometry` → `borderedPages[id] =
true`). That is the only way a flagged page becomes clean, which is why the
  primary button routes _to_ flagged pages instead of past them: `everyPageBordered
-> "Continue to filters"`, otherwise `"Check page N"` for the first page still
  flagged, otherwise `"Next page (i of N)"`. Do not let a flagged page be marked
  done by the batch pass — a page the detector was unsure about and nobody looked
  at must still show amber.
- The primary action jumps straight to filters once `everyPageBordered`.

### Deferred, deliberately

Per-page filters (a different filter per page within one batch) are **not**
implemented: one filter is applied to the document. That matches the requested flow
("… _then_ apply filters") and the single bake pass. If per-page filters are wanted,
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
4. **On disk:** one 768 px JPEG per page; `ThumbnailStore.read` scales _inside_ the
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

| when         | work                                                     | thread                      |
| :----------- | :------------------------------------------------------- | :-------------------------- |
| composition  | first screen's tiles request covers                      | main, then decode (2 slots) |
| +48 ms       | `WARM_ROWS = 6` rows warmed into the memory cache        | decode (1 slot)             |
| +1.2 s, idle | stored thumbnails built for pages that predate the store | IO                          |
| +3 s, idle   | ML Kit text backfill (pauses the moment a scroll starts) | IO                          |
| +5 s         | orphan page-file sweep                                   | IO                          |

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
Arena's sandbox to _validate the file format_ and are not part of the app.

### Why there is a second PDF engine

`android.graphics.pdf.PdfDocument` cannot encrypt, and it always embeds page
images losslessly — which is exactly why a photographed page becomes a
multi-megabyte attachment. There is also no PDF library anywhere in the original
APK (`classes*.dex` contains only `com.shockwave.pdfium`, a _viewer_), so there was
nothing to copy: `util/PdfWriter.kt` is written from the specification.

The platform engine is still the default. `PdfGenerator.generatePdf` picks:

| condition                                | engine        | why                                                                           |
| :--------------------------------------- | :------------ | :---------------------------------------------------------------------------- |
| no password, `compression == ORIGINAL`   | `PdfDocument` | byte-identical to every previous round — no regression on the ordinary export |
| password, or any lossy level, or a merge | `PdfWriter`   | it can encrypt and it chooses the image encoding                              |

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
   device. `PdfSecurityTest` pins the _exact_ `/O`, `/U` and per-object key bytes
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
3. Anything that looks wrong in the _new_ path only: the platform path is
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

## 6r. Magic Search + the Rust vocabulary matcher (Arena, 2026-10-25, round 27)

### What to build

Nothing new to provision — the matcher lives in the crate that already exists, so
the build is unchanged:

```
cargo install cargo-ndk                      # once
rustup target add aarch64-linux-android x86_64-linux-android
export ANDROID_NDK_HOME=/path/to/ndk
./gradlew assembleDebug                      # buildRustKernels runs, or is skipped
```

Without the toolchain everything still works: `NativeSearch.isLoaded` is false and
`DocumentSearch.expandReference` answers every query. That is not a degraded mode —
it is the same code the JVM tests run.

`search.rs` adds **no dependency**, no global state and no new JNI surface beyond two
functions (`nativeVersion`, `nativeExpand`). It is the smallest thing that could do
the job: one `Vec<i32>` per call, `std` only.

### Why this one is worth native code

Search has exactly one loop whose cost grows with the _library_ rather than with the
query: expanding each typed word into the index's vocabulary (prefixes and
near-spellings). On a 3,000-page library that vocabulary is tens of thousands of
words. Everything else in a query is postings lookups that already scale with the
number of matches.

### The contract that must not drift

| fact                                                                              | where                             |
| :-------------------------------------------------------------------------------- | :-------------------------------- |
| `DocumentSearch.expandReference` is the **definition** of the walk                | `util/DocumentSearch.kt`          |
| `search.rs::expand` must produce the **same triples in the same order**           | `app/src/main/rust/src/search.rs` |
| order is **vocabulary outer, query inner**                                        | both                              |
| codes: `0` exact, `-1` prefix, `1`/`2` edit distance                              | both                              |
| an entry matches at most one code per query, priority exact → prefix → distance   | both                              |
| `SEARCH_ABI_VERSION` (1) is versioned **separately** from the pixel `ABI_VERSION` | `lib.rs`, `NativeSearch.kt`       |

The golden triple vector (`invoce`/`invoi`/`aadhar`/`dob`/`totl` over an 18-word
vocabulary) is asserted in **both** `search.rs`'s `#[cfg(test)]` module and
`MagicSearchTest.theVocabularyWalkProducesTheGoldenTriples`. If the two ever disagree,
one of the two tests fails — and on device the parity check refuses the kernel before
a user ever sees a wrong result.

### Do not undo

- **The length gates on fuzzy matching.** One edit needs a 5-character word, two need 8. Without them "tax" matches "toy" and the search stops being trustworthy.
- **The `stem()` pairing.** The index and the query are stemmed by the _same_
  function; changing one side silently halves recall.
- **Postings for titles and tags.** They used to be an O(documents) `contains` scan per
  term per keystroke — the reason a big library felt slow while typing.
- **Candidate-restricted phrase and snippet work.** The phrase check used to scan
  every page in the library on every keystroke.
- **`NativeSearch.isLoaded` must stay side-effect free.** It is initialised in JVM
  unit tests; no `android.util.Log` call may appear on that path.

### How to verify on a device

1. `./gradlew testDebugUnitTest` — 20 new tests, all pure JVM, no device.
2. Settings → **Performance (debug)** prints `Search kernel: Rust (verified against
the Kotlin reference)` when the `.so` loaded _and_ the parity check passed, and
   `Kotlin reference` otherwise. Both are correct; the first is faster.
3. Type `invoce`, `invoi`, `invoices`, `aadhaar` and `"pan card"` against a library
   that contains the correct spellings — each should find the document, and the
   matched words in the snippet should be emphasised.
4. Type `-draft` after a word to confirm exclusion, and `2024` alone to confirm the
   year path.

## 6s. Running at the panel's rate: FramePacing, FrameBudget and the draw-path cleanup (Arena, 2026-10-26, round 29)

### What changed, and the one thing that did not

The report said the app was not reaching 120 fps. Two independent causes, both fixed:

1. **The instrumentation could not see it.** Every threshold in `PerfTrace` was a
   60 Hz constant, so on a 120 Hz panel a frame could take 16 ms — twice its 8.33 ms
   slot — and still count as on time. All of that maths now lives in
   `util/FrameBudget.kt` and is derived from the rate the display actually gave us.
2. **Nothing ever asked for the mode.** The window took whatever the system had
   chosen, which on many panels is 60 Hz until an app asks otherwise.
   `util/FramePacing.kt` now sets `preferredDisplayModeId` (fastest mode **at the same
   physical resolution**) and `setFrameRate(hz, FRAME_RATE_COMPATIBILITY_DEFAULT)`.

**No rendering changed.** Filters, geometry, colours, typography, PDF bytes, OCR and
search ranking are byte-identical to round 28.

### Files

| file                                | what it is for                                                                   |
| :---------------------------------- | :------------------------------------------------------------------------------- |
| `util/FramePacing.kt`               | chooses and requests the display mode; `apply`, `currentRate`, `rateOf`          |
| `util/FrameBudget.kt`               | **pure JVM**, no Android types — budget, missed, jank, histogram p90, fps        |
| `util/PerfTrace.kt`                 | display-aware monitor; every threshold now comes from `FrameBudget`              |
| `MainActivity.kt`                   | `FramePacing.apply(window)` before the first frame, then `setDisplayRefreshRate` |
| `theme/Theme.kt`                    | `colorScheme`/`accents` are `remember`ed instead of rebuilt per call             |
| `util/PageImageLoader.kt`           | `configFor(w,h)`; `RGB_565` for tiles ≤ 1024 px                                  |
| `ui/dashboard/DashboardScreen.kt`   | selection map, one stable callback set per screen                                |
| `ui/detail/DocumentDetailScreen.kt` | date pill's `SimpleDateFormat` remembered                                        |
| `ui/settings/SettingsScreen.kt`     | legend prints panel Hz and ms/frame                                              |

### The contract that must not drift

| fact                                                                       | where                                              |
| :------------------------------------------------------------------------- | :------------------------------------------------- |
| budget = `1e9 / clamp(hz, 30, 240)`                                        | `FrameBudget.budgetNs`                             |
| missed = interval > **1.5×** budget                                        | `FrameBudget.MISSED_FRAME_FACTOR`                  |
| jank window = p90 ≥ **1.25×** budget **or** max ≥ 2× budget                | `FrameBudget.isJankyWindow`                        |
| the rate used for thresholds is the one **granted**, not the one requested | `MainActivity` → `PerfTrace.setDisplayRefreshRate` |
| `RGB_565` only where `maxOf(w,h) ≤ 1024`                                   | `PageImageLoader.OPAQUE_TILE_LIMIT`                |
| card callbacks are `(String) -> Unit`, one instance per screen             | `DashboardScreen`                                  |

`isMissed(60f, 16ms) == false` and `isMissed(120f, 16ms) == true` is pinned in
`FrameBudgetTest`: it is the exact blindness that made round 28 look smooth.

### Do not undo

- **`FRAME_RATE_COMPATIBILITY_DEFAULT`, never `FIXED_SOURCE`.** Fixed-source stops
  the system from dropping the rate on a static screen and costs battery for nothing.
- **Same-resolution modes only** in `FramePacing.fastestMode`. A different size
  reallocates the window surface during launch.
- **`RGB_565` must stay at ≤ 1024 px.** Scanned text is what bands in 565; the page
  viewer (1600×2200), the editor and the lossless PNG bake must get `ARGB_8888`.
- **The remembered theme palettes.** Rebuilding a `ColorScheme` per call invalidates
  every colour-derived state in the app, which is most of it.
- **The stable dashboard callbacks.** Inline lambdas in a `LazyVerticalGrid` item are a
  changed parameter on every recomposition; the card then re-runs thumbnail lookup
  and text layout for a document that did not change.

### How to verify on a device

1. `./gradlew testDebugUnitTest` — `FrameBudgetTest` is 12 pure-JVM tests, no device.
2. `adb logcat -s DocScannerPerf` while flinging the library. A healthy line on a
   120 Hz panel is `fps` within a frame or two of `hz`, `missed=0`, `atBudget` in the
   high 90s. `hz=60` on a 120 Hz panel means the panel offered no other mode at the
   same resolution — not a bug in this code.
3. Settings → **Performance (debug)** shows the same numbers without adb, plus the
   ms/frame the current rate implies.
4. If `sync`/`layout` dominate the stage breakdown, it is still composition. If
   `draw`/`cmd`/`gpu` dominate, it is pixels and textures — that is the only case
   where the native cover pipeline is worth measuring.

### Still open

- `.entrance()` on the home header is not yet keyed to entry, so it can re-run.
- Thumbnail **cache-miss** decode is still Kotlin; the Rust cover pipeline is
  deliberately not written until a device report says decode is the bottleneck.
- `ImageStore.loadBitmap` recycle audit is unfinished.

## 6t. Launch, the brand mark, and border detection (Arena, 2026-10-26, round 31)

### What the user reported

Three symptoms, one launch, one detector, one gesture: a white empty screen then a
stuttering library; auto-detected borders wrong; dragging a corner doing nothing.

### Files and what each one owns

| file | responsibility |
| :--- | :--- |
| `ui/common/LaunchSplash.kt` *(new)* | the mark + name shown while the library loads |
| `res/drawable/launch_background.xml` *(new)*, `res/values/colors.xml` *(new)*, `res/values-v31/themes.xml` *(new)* | the brand gradient behind the first frame and, on Android 12+, behind the system splash |
| `res/drawable/ic_launcher_foreground.xml`, `ic_launcher_background.xml` | **the app's own mark** — these were the Android Studio template |
| `util/DocumentQuad.kt` *(new)* | **all** border geometry; pure JVM, no Android type |
| `util/DocumentDetector.kt` | `Bitmap` → luma → `DocumentQuad`; the public `Detection` API |
| `ui/editor/CropAndFilterScreen.kt` | the handles, and the gesture that moves them |
| `MainActivity.kt` | builds the repository off the main thread, cross-fades splash → app |
| `ui/dashboard/DashboardScreen.kt` | `LAUNCH_QUIET_MS` gating for every background pass |

### The launch, and why it was white

`ScannerRepository` reads and parses `library.json` in its constructor. It used to be
constructed **inside composition**, so that parse ran on the UI thread before the
first frame could be drawn: no frame until it finished, every frame behind it queued,
and the window showed the platform's default `windowBackground` — flat light grey —
which is the white screen. Three changes, in order of importance: the repository is
built on `Dispatchers.IO`; the window background is now the brand gradient; nothing
in the background starts for the first 2.2 s (`LAUNCH_QUIET_MS`), and the OCR backfill
checks for a gesture before every *page* rather than every document.

### The detector's contract that must not drift

| fact | where |
| :--- | :--- |
| the geometry has **no Android type** and is unit-tested on the JVM | `util/DocumentQuad.kt`, `DocumentQuadTest.kt` |
| each side is scanned across the **whole span**, walking in from the frame edge | `DocumentQuad.scanSide` |
| **both polarities** are always tried; bright-on-dark first | `DocumentQuad.detect` |
| step thresholds 0.18 / 0.30 / 0.10 of the background range, best-fitted-sides wins, early exit at 4 | `THRESHOLD_FRACTIONS` |
| `sidesFound = 0` + full frame = "nothing to crop", **not** a failure | `DocumentDetector.detectInternal` |
| RANSAC is seeded deterministically: pressing "detect" twice must not move the quad | `DocumentQuad.fitSide` |

### Do not undo

- **The off-centre scan.** Limiting a side's search to the half of the frame nearest
  to it is what produced a crop displaced by half the frame on an off-centre page.
  It looks like a harmless optimisation; it is the bug.
- **Trying both polarities.** Gating the inverted one on "the whole frame is dark"
  makes every dark document on a light surface undetectable.
- **`rememberUpdatedState` in `BorderCanvas`.** `pointerInput` does not re-create its
  block when its keys are unchanged, so anything captured by value inside it is
  frozen at the composition that created the block. Reading `grabbedCorner` from the
  parameter is why the handles never moved. Hit-testing has the same trap: it must use
  the current pan/zoom or handles become ungrabbable after a pinch.
- **`NonCancellable` around the repository construction.** Without it, a rotation
  during the load can leave a repository with a live scope and no owner.
- **The first-emission guard in `ScannerRepository.init`.** Removing it makes every
  launch rewrite the whole library file for no reason, on the threads the cover
  decodes are queued behind.

### How to verify on a device

1. `./gradlew testDebugUnitTest` — `DocumentQuadTest` is 11 pure-JVM tests, no device,
   no Robolectric. It synthesises photographs (desk gradient, perspective page, ink,
   deterministic noise) and asserts corner positions to within 2.5 % of the frame.
2. Cold start with the app killed: the mark and "Doc Scanner" appear immediately on
   the brand gradient, never a white window, then the library.
3. Photograph a page at an angle, off to one side of the frame → *Borders* should
   land on the paper on all four sides. Photograph a dark page (a menu board) on a
   light table → it should detect at all, which it never used to.
4. Drag a corner handle: it must follow the finger, and snap flat to a frame edge
   when dragged within ~1 % of it. Pinch to zoom first, then drag — that used to be
   impossible.
5. Scroll immediately after launch. `adb logcat -s DocScannerPerf` should show
   `pageDecodes` barely moving during the first gesture; if it spikes, the warm loop
   or the prefetcher is decoding behind the finger.

## 6u. Round 30 reviewed: the six Rust kernels, and three corrections (Arena, 2026-10-26, round 32)

Antigravity's Round 30 added six native kernels behind the existing parity gate and
bumped the ABI to 2. This section records what it does, what it does **not** do, and
the three things reviewing it changed.

### What it actually speeds up

`meanPool`, `applyLut`, `rec601InPlace`, `gaussianBlur3`, `boxBlur3` and
`boxBlurInPlace` are the loops inside `DocumentFilters.apply` — the **bake** path.
They make "Save & Next", export and the editor's filter preview finish sooner. They
do not touch a single frame of the library scroll or of launch: if the complaint is
"the list stutters", this work is not the answer, and the two rounds that were about
that were about the main-thread parse, background contention and per-frame
allocation.

Scope, threading and dependencies are all sound: `std::thread::scope` bands capped
at `available_parallelism() - 2` and at 6, no new crate, `panic = "abort"`, and every
call site is `if (nativeFiltersReady() && NativeFilters.xxx(...)) return` with a
Kotlin reference underneath.

### What I checked by reading it (nothing here was compiled)

| kernel | the property that matters | verdict |
| :--- | :--- | :--- |
| `box_blur_inplace` | in-place horizontal pass must read the **stale** left neighbour, and the vertical pass must use two scratch rows — an "obvious" rewrite of this silently changes every pixel | matches the Kotlin reference line for line |
| `mean_pool` | cell bounds `min(h-1, gy*cell)` / `min(h, y0+cell)`, and `count` per cell | matches; `i64` accumulators cannot overflow where Kotlin's `Int` could at large cells |
| `rec601_inplace` | rounding | `floor(v + 0.5)` equals Kotlin's `roundToInt` for the non-negative inputs here, and the f32 multiply/add order is identical, so the exact `contentEquals` gate should pass on device |
| `median_blur_gray` | is it on a production path? | **no** — Kotlin deliberately keeps the O(k) histogram median. The gate verifies it anyway, which costs microseconds and is fine |

### Three corrections

1. **`FramePacing` was asking for the refresh rate in a field that is ignored when
   the other one is set.** AOSP `WindowManager.LayoutParams` says of
   `preferredRefreshRate`: *"This value is ignored if `preferredDisplayModeId` is
   set"*, and *"This should be used in favor of `preferredDisplayModeId` for
   applications that want to specify the refresh rate, but do not want to specify a
   preference for any other displayMode properties (e.g., resolution)"*. Round 30 set
   both. Now exactly one is written: **API ≤ 33 → `preferredDisplayModeId`**
   (window-local; before 34 the rate field "will be applied to the display's default
   mode", i.e. a system-wide change from a scanner app), **API 34+ →
   `preferredRefreshRate` only**, with any mode id explicitly cleared so the rate can
   take effect.
   Note also that `Window` has **no** `setFrameRate` — that is `Surface#setFrameRate`,
   and a Compose activity has no public handle on the window's surface before attach.
   Removing that call in Round 30 was correct; `LayoutParams` is the only sanctioned
   pre-attach route.
2. **`PerfTrace` reported the rate that was requested, not the rate granted.** A
   refused request read as `hz=120 fps≈60`, which looks like an app that cannot keep
   up when it is a display that never changed mode. `setRateProvider` is installed by
   `MainActivity` and `syncDisplayRate()` re-reads the panel before each line —
   per gesture, never per frame.
3. **The parity log said "byte-identical" for all nine kernels.** It is true of the
   integer ones (`contentEquals`) and not of the float planes (paper map, three
   blurs), which are compared within 0.01. The line now says which is which.

### Do not undo

- **Exactly one of `preferredDisplayModeId` / `preferredRefreshRate`.** Setting both
  is not asking twice.
- **Same-resolution modes only**, still. A mode with a different size reallocates the
  window surface mid-launch.
- **`syncDisplayRate()` stays off the frame path.** It is a window-manager call; once
  per gesture is enough and once per frame would be the bug.
- **Every native call keeps its `return` guard and its Kotlin reference.** The gate is
  the only thing standing between a compiler quirk and a changed scan.

## 6v. Round 33: six bugs, five hot paths, four motion corrections (Arena, 2026-10-26, round 33)

Source-only round. **Nothing compiled** — no JDK, no Android SDK, no `cargo` here — so
treat every number below as a claim about the code, not a measurement. Two of these
change behaviour other screens can see; they are flagged first.

### Behaviour other code can observe

- **`ScannerRepository.deletePage` now deletes the document when the page was the
  last one.** The signature is unchanged, so no call site needs touching, but
  `DocumentDetailScreen` (and anything else sitting on a `docId`) will see that
  document disappear from `documents`. The detail screen navigates back when that
  happens; anything new that holds a `docId` must tolerate the same.
- **`MainActivity` no longer owns the repository.** It is
  `data/LibraryViewModel.kt` (`AndroidViewModel`) now:
  `val repository: StateFlow<ScannerRepository?>`, `null` until the library has been
  read, closed in `onCleared()`. A rotation reuses the loaded library instead of
  rebuilding it — which also means **the launch screen is no longer replayed on a
  rotation**. `AGENTS.md` §2.6 still holds: the repository is Activity-scoped, not a
  process singleton. Do not move it to `Application`.

### The bug six

1. A document could reach zero pages and stay in the library (no cover, no export, no
   way to delete it from the UI).
2. Page delete was one tap, permanent, unconfirmed.
3. `DocumentDetailScreen` dead-ended on "Document not found".
4. `selection` kept ids that were no longer listed: the count lied, and **Merge could
   silently do nothing**. Selection is pruned to `activeDocs`; the merge handler always
   either merges or ends selection.
5. Rename closed the dialog, saved the notes and discarded a blank title silently.
6. Every rotation rebuilt the repository and leaked the previous one (`close()` only
   ran when `isFinishing`, while `onCreate` built a new instance each time).

### The five hot paths

| Where | Was | Now |
| :--- | :--- | :--- |
| `BorderCanvas` corner drag | every pointer move hoisted a quad into the editor's state → full screen recomposition, and `LaunchedEffect(previewSource, …)` re-baked the preview **per frame** | `liveQuad` is local, written in the gesture, read **only in the draw lambda**, committed once on release |
| `PageViewerScreen` pinch | `graphicsLayer(scaleX = scale, …)` — composition-phase read, layer rebuilt per frame | lambda form; the zoom badge is its own composable fed the `FloatState`, with `derivedStateOf` on the integer percent |
| Camera pinch | one `setZoomRatio` IPC **per touch event** | `snapshotFlow { zoomRatio }.conflate()` + a 32 ms floor ≈ 31 Hz, newest value always applied |
| `SegmentedPill` thumb | animated `Dp` through `Modifier.offset` — a layout pass per frame | `translationX` in `graphicsLayer` |
| `SignaturePadDialog` | `currentStroke = currentStroke + point` — a full copy of a growing list per move, and a composition-phase read | `SnapshotStateList` append (O(1)) + `derivedStateOf` boolean for the blank hint |

### The four motion corrections (skill repo: `animation-performance`, `review-animations`)

- `Motion.fade` was **`LinearEasing`** → `Motion.EaseOut` = `cubic-bezier(0.23, 1,
  0.32, 1)`. Linear is for constant motion, never for a fade.
- `Modifier.entrance` was `.alpha(p)` **plus** `graphicsLayer { translationY }` — two
  layers, two offscreen buffers — → one `graphicsLayer { alpha; translationY }`.
- `Motion.press` was `dampingRatio = 0.55`: every button in the app wobbled on release.
  0.8 now; `bouncy` is still there for one-off moments.
- `Navigation.kt` used one symmetric `FastOutSlowInEasing` for both screens. Arriving
  is `Motion.EaseOut`, leaving is `Motion.EaseIn` (the one legitimate `ease-in`: it is
  on its way out).

### Do not undo

- **Do not put `corners` back into the drag path.** `liveQuad` in `BorderCanvas` is the
  whole reason a drag costs a redraw instead of a recomposition. If a new feature needs
  the live quad, add it as a draw-phase read or a `derivedStateOf`; hoisting it back
  into the screen is what made the drag heavy in the first place.
- **Do not key a `LaunchedEffect` on `zoomRatio`** in the camera screen; that re-creates
  the IPC-per-frame problem. Add to the `snapshotFlow` if a new consumer is needed.
- **Do not use the parameter form of `graphicsLayer`** for anything a gesture moves.
- **Do not read a `SnapshotStateList` in composition** to decide something per point;
  wrap it in `derivedStateOf`.
- **`close()` stays in `LibraryViewModel.onCleared()` only.** Calling it from
  `onDestroy` as well would tear down the instance the next Activity reuses.
- **Still nothing to compile here.** `./gradlew testDebugUnitTest` and
  `assembleDebug` remain the gate, and only a machine with a JDK can run them.

## 6w. Round 34: the export sheet rebuilt (Arena, 2026-10-26, round 34)

Owner complaint, verbatim: *"the export pdf page where all the size and password
settings are listed is absolutley wrong with the ui/ux"*. It was, and the cause is
worth remembering because it is one line away in any other sheet.

### The defect

`SegmentedPill` divides the available width **evenly** between its options. That is
correct for the two-option control it was designed for ("Borders | Filters") and wrong
for `PaperSize.entries`, which has **eight** options whose labels carry their
dimensions. On a 360 dp phone each segment received ~34 dp, so every label rendered as
a fragment — "A4…", "US…", "Ex…". `PdfMargin` and `PdfCompression` had the same
treatment at four options ("No Man…", "Compac…"). Six identical uppercase eyebrows over
six equally weighted blocks finished the job: nothing on the sheet said which choices
are made on every export (file size, password) and which are set once (paper, margins).

**Rule going forward:** `SegmentedPill` is for two to four options with short, fixed
labels. Anything driven by an enum with a long or variable label needs its own
control. Do not pass a model enum's `label` straight into a segmented control.

### What it is now

| Section | Control |
| :--- | :--- |
| Paper | one row showing the current size and its dimensions; expands to a 2-column grid of all 8; choosing one folds it away |
| File size | 2 × 2 cards — label plus what it costs, check on the chosen one, one line under the grid explaining the level from the enum's own numbers |
| Layout | margins as a 2 × 2 of equal chips, then a full-width toggle row for footer page numbers |
| Password | a full-width toggle row; the field and its warning expand in place |
| Action | the CTA plus a caption naming exactly what will be written: "12 pages · A4 · Balanced" |

`docs/design/pdf_export_sheet.png` (and `render_pdf_sheet.py`) shows before, after, and
after-with-the-grid-open. It is a **drawing** that mirrors the dp values, not a
screenshot — nothing in this sandbox can build the app.

### Behaviour changes an agent could notice

- **The sheet can no longer be swiped away while it is writing.** Dismissal cancelled
  the export coroutine (it belongs to the sheet's composition scope) and could leave a
  half-written file. `rememberModalBottomSheetState(confirmValueChange = …)` returns
  `false` for `Hidden` while `isGenerating`.
- **Every choice reports its selected state to accessibility services**
  (`selectable(..., Role.RadioButton)` instead of `clickable`), and the toggle rows use
  `toggleable(..., Role.Switch)`. If you add a choice control to this sheet, do the same
  — colour is not a state.
- The footer now names the engine correctly: a password **or** any lossy file-size level
  switches to the app's own `PdfWriter`.

`PdfExportSheet(...)`'s signature is unchanged; `PdfGenerator`, `PdfWriter`,
`PdfExportConfig` and every default are untouched, so the file produced for the same
choices is identical.

### Do not undo

- **Do not put `PaperSize`, `PdfMargin` or `PdfCompression` back into `SegmentedPill`.**
- **Do not remove the CTA caption.** It is the only place the sheet states what the tap
  produces before it happens.
- **Do not replace `selectable`/`toggleable` with `clickable`.** That is what made four
  identical buttons with no announced state.
- **Still uncompiled here.** The `PdfExportSheet` rewrite is source-only in this
  sandbox; `./gradlew testDebugUnitTest assembleDebug` is the gate.

## 6x. Round 35: one segmented control, motion on every state change, camera polish (Arena, 2026-10-26, round 35)

### The shared control changed shape

`ui/common/Expressive.kt › SegmentedPill` is now the app's **only** small segmented
selector, and it takes its palette as parameters:

```
SegmentedPill(options, selectedIndex, onSelect, modifier,
              segmentHeight = 38.dp, container, thumbColor,
              selectedContent, content, thumbElevation)
```

The camera used to carry a private copy (`ModeToggle`/`ModeSegment`) that faded a
background in and out under each label instead of moving a thumb. It is deleted; the
camera calls `SegmentedPill` with its glass colours. **If you add a two- or three-state
selector, use this one** — and if you need different colours, add parameters, not a
second implementation.

Behaviour now, for everyone: the thumb travels on `spring(dampingRatio = 0.78f,
stiffness = 520f)` as a layer translation, labels cross-fade over 180 ms, each segment
has a 0.96 press-scale, and each is `selectable(..., Role.RadioButton)`.

### Motion inventory added this round

All draw-phase or placement; none of them re-measures.

| Screen | Animation |
| :--- | :--- |
| Dashboard top bar | `AnimatedContent` swap between the app bar and the selection bar (180 ms fade + 1/6-height lift) |
| Dashboard FAB | scale 0.8 → 1 in, back out, instead of appearing between frames |
| Dashboard selection bar | **contents** rise 18 dp and fade; the bar's height is deliberately not animated, because Scaffold passes it to the grid as content padding |
| Detail page grid, recycle bin | `Modifier.animateItem()` — a moved page moves, a removed row closes the gap |
| Viewer | page change crossfades 180 ms |
| Camera | chrome entrance (top −10 dp, bottom +14 dp), shutter dip 0.985 → 1, flash icon morph, rolling page count, spring-in finish button |

### Camera: one functional change

The page-counter pill now carries an **Undo** segment
(`repository.discardPages(last)`, plus haptic and a status line). Do not put a
confirmation dialog on it: it is a one-page, local, instantly repeatable action, and the
close dialog still guards the *whole* session (`capturedPages` non-empty).

### Design artifacts

`docs/design/render_pdf_sheet.py` now renders **both** palettes
(`pdf_export_sheet.png`, `pdf_export_sheet_dark.png`). Two bugs in that script were
fixed and are worth knowing if you extend it: colour defaults were bound at import time
(so a palette switch had no effect — pass `None` and resolve inside the function), and
the backdrop was hard-coded to the light tone.

### Do not undo

- **Do not animate a `bottomBar`'s height on the dashboard.** Scaffold turns it into
  content padding; the grid re-places every frame. Animate the contents.
- **Do not add a second segmented control implementation.** See above.
- **Do not make `animateItem()` opt-in per screen** for lists whose items move or
  disappear; it is two words and the placement phase absorbs it.
- **Still uncompiled in this sandbox** (no JDK/SDK). Round 34's sheet and everything in
  this round want `./gradlew testDebugUnitTest assembleDebug` on a machine that has one.

## 6y. Round 36: output size on the file-size chooser, and startup work waits for the user (Arena, 2026-10-27, round 36)

### New shared pieces

| symbol | file | contract |
| :--- | :--- | :--- |
| `StartupWork` | `util/StartupWork.kt` | the app's **only** way to schedule speculative background work: `awaitQuiet(ms, reason)`, `runWhenQuiet(...)`, `dispatcher` (single thread), `markInteraction()`, `setForeground()`, `setScrolling()` |
| `PdfSizeEstimate` | `util/PdfSizeEstimate.kt` | `estimate(pages, maxImageDimensionPx): Map<PdfCompression, Long>`, `format(bytes): String` |

**If you need to run something nobody is waiting for, gate it on `StartupWork`. Do not
add `delay(…)` to defer launch work** — that is the defect this round removed (see
below). If you add a new speculative pass, call `awaitQuiet` first and check
`isUndisturbedSince` between items.

### Fixed delays that were deleted

`DashboardScreen`: `LAUNCH_QUIET_MS` (2 200), `OCR_START_DELAY_MS` (3 000),
`THUMBNAIL_PRIME_DELAY_MS` (1 200), `WARM_START_DELAY_MS` (48) are **gone**, replaced by
`OCR_IDLE_MS` (2 500), `REPAIR_IDLE_MS` (4 000) and `withFrameNanos`.
`ScannerRepository.ORPHAN_SWEEP_DELAY_MS` is now **`ORPHAN_SWEEP_IDLE_MS` (6 000)** — same
job, idle-gated instead of timed.

### Why (for whoever reads the next "it lags on launch" report)

The three passes started at 48 ms, 3.4 s and 5.2 s after composition, and the orphan
sweep at 5 s — all unconditional. The 5.2 s pass is ML Kit: one inference + a 2400 px
decode per page (up to 24), each page followed by a full `library.json` write and a
dashboard emission. That is the ~10 s of jank. Nothing about it was the grid.

Measured, not asserted: `adb logcat -s DocScannerPerf` prints
`library.json read: … in X ms`, `launch: library ready … ms`,
`[startup] ocr-backfill allowed after … ms idle`, `ocr-backfill finished: … in X ms`,
`orphan-sweep removed … in X ms`. Ask for those five lines before changing anything here.

Also removed: the per-document OCR apply and `OCR_BATCH_SPACING_MS`. The backfill uses
the new `ScannerRepository.setPageOcrTextsBulk` and emits the dashboard **once** per
batch. A per-document apply plus a sleep is the pattern that repainted the list two
dozen times during launch — do not bring it back.

### PDF sheet: what is safe to touch

`PdfSizeEstimate` is read-only and is **not** in the write path — `PdfGenerator`,
`PdfWriter`, `PdfImages`, geometry and filters are unchanged, and the estimate cannot
change the bytes of a PDF. `CompressionCard(level, selected, sizeLabel, onClick,
modifier)` now requires `sizeLabel`; it is `heightIn(min = 88.dp)` with label / estimate
/ detail rows, and the sheet prints `12 pages ≈ 6.6 MB · …` under the grid.

### Do not undo

- **Do not reintroduce a fixed delay for OCR, thumbnail repair or the orphan sweep.**
  A timer fires whether or not the user is mid-gesture; that is how the ten seconds
  happened.
- **Do not call `StartupWork.markInteraction()` from more than one place** — one root
  `pointerInput` on the `Initial` pass is deliberate: it sees every gesture and consumes
  none.
- **Do not do the size measurement in composition.** It stats files; it belongs in
  `produceState`/IO, which is where it is.
- **Do not add a second size-formatting helper.** KB below 1 MB, MB above, decimal
  units — `PdfSizeEstimate.format`.
- Mock regenerated for both palettes; if you touch the card, re-run
  `python3 docs/design/render_pdf_sheet.py` from the **repo root**.

## 6z. Round 37: a page strip in the filter stage, and corner handles that behave (Arena, 2026-10-27, round 37)

### New shared pieces

| symbol | file | contract |
| :--- | :--- | :--- |
| `PageStrip` | `ui/editor/CropAndFilterScreen.kt` (private) | the editor's **only** page switcher; used by the borders *and* filters stage, scrolls to the active page |
| `CropHandles` | `util/CropHandles.kt` | `move(quad, corner, dx, dy)`, `isSound(quad)`, `MIN_GAP = 0.02f`, `SNAP = 0.012f` — the corner-drag arithmetic |

If you add another stage or another sheet to the editor, **use `PageStrip`**; do not
redraw the chip row. A stage without it is a stage where a multi-page document cannot
change pages, which is exactly the Round 37 bug.

### What changed in the editor

- `FilterControls` grew `activePageId`, `borderedPages`, `needsCheck`, `onSelectPage`.
- Switching pages in the filter stage keeps the user's filter choice
  (`keepFilterOnNextLoad`) instead of adopting the target page's stored filter.
- A page switch clears `previewBitmap` first, so the loading state is honest.
- `BorderCanvas` grabs handles within `HANDLE_GRAB_DP` (28 dp, divided by the zoom,
  measured in canvas pixels) and drags them **relatively** after `touchSlop`.

### Do not undo

- **Do not put a corner back under the finger on pointer-down.** A tap must not move a
  corner, and a drag must not jump by the grab offset. Both were the report.
- **Do not hit-test handles in normalised coordinates.** The frame is not square; a
  normalised radius is anisotropic and (at the old 0.05) a fifth of the page.
- **Do not duplicate `MIN_GAP`/`SNAP` in the canvas.** Use `CropHandles`; the test
  drives the same numbers the gesture does.
- **Do not remove `PageStrip` from one stage.** See above.
- Uncompiled here: `./gradlew testDebugUnitTest assembleDebug` is still owed.

## 6am. Round 50: expressive bar, multi-page selection, motion pass (Arena, 2026-10-28)

**New:** `ui/common/ExpressiveAction.kt`.
**Edited:** `ui/common/Expressive.kt`, `ui/detail/DocumentDetailScreen.kt`,
`ui/viewer/PageViewerScreen.kt`, `ui/dashboard/DashboardScreen.kt`.

- **Use `ExpressiveActionBar` / `ConnectedButtonGroup` / `ExpressivePager` for any new
  bottom bar.** They are the app's expressive action language: a connected group built to
  Google's shape tokens inside a tonal container, with a pager that has a progress rail.
  Do not hand-build another row of icon+label cells. (`ExpressiveActionItem` from this
  round was **deleted in R51** — see §6an.)
- **Selection in the document grid is many-page.** `selectedPageIds: Set<String>`.
  Tapping adds/removes; the bar has three states (document / one page / several). If you
  add an action, decide which state it belongs to — `Crop` and `Annotate` are
  single-page on purpose.
- **Deleted private symbols** (no §3 impact, nothing outside their files used them):
  `DocumentActionButton`, `DocumentActionContent`, `ACTION_HEIGHT` (detail screen),
  `ViewerPageStep`, `ViewerActionButton` (viewer, added in R49).
- **Renamed local state:** `pagePendingDelete: ScannedPage?` → `pagesPendingDelete:
  List<ScannedPage>?`, `rotatePage(page)` → `rotatePages(list)` + `rotateOne(page)`.
- **`exportOnly: List<ScannedPage>?`** is how a subset is exported. Do not route a
  batch export through `showPdfExportSheet` — that exports the whole document.
- **Motion rules used here:** press → `Motion.press`; tap pop → `Motion.bouncy`;
  colour/fade → `Motion.fade` / 120–180 ms tweens; arrivals → `Motion.emphasized`.
  Animate `alpha`, `translation` and `scale` **only** — never a layout property, and
  never a `Modifier.offset`/size in a loop.

### ⚠️ Repo hygiene (found while re-reading the tree — not changed)

1. **293 MB of committed binaries** in `adobe-scan/` (+ 49 MB `indymobile/apks/base.apk`,
   + 81 MB `docs/original_apk_binaries/`). Two files are ~65 MB and one is 49 MB —
   approaching GitHub's 100 MB limit, and every clone pays for it. AGENTS.md §2.5 says
   no binaries. Recommend release assets or external storage, text artifacts only.
2. **The Adobe material cannot ship.** `libMagicClean.so`, the three `.tflite` models
   and the decompiled sources are Adobe-proprietary. They are legitimate as *behavioural
   reference* (thresholds, state machines, tensor shapes) but the weights and the code
   cannot be linked into this app without a licence, and §2 forbids decompiled code in
   shipping paths. Port the algorithm, never the artifact.
3. `app/src/main/assets/border_detect_224.tflite` is tracked while `.gitignore` ignores
   `app/src/main/assets/models/` — the build's `VerifyBorderModelTask` is the only thing
   keeping that honest. Fine today; worth remembering if the asset moves.

## 6al. Round 49: the reader's bottom shelf and swipe paging (Arena, 2026-10-27)

**Edited:** `ui/common/ZoomablePageView.kt` (signature change), `ui/viewer/PageViewerScreen.kt`.

- **`ZoomablePageView(zoom: () -> Float, pan: () -> Offset, …)`** — pass `{ state }`,
  never `state`. Reading the value at the call site recomposes the caller on every
  pinch frame; the lambda is read in the draw layer instead. Do not "simplify" this
  back to plain parameters.
- **The viewer no longer has its own gesture.** `detectTransformGestures` was removed
  from `PageViewerScreen`; do not reintroduce a per-screen pinch. Pinch/pan/swipe now
  behave identically in the viewer and the editor's filter preview.
- **The viewer's bottom shelf is one container** (pager row + Crop/Annotate/Rotate/More).
  Adding an action means adding a cell there, not a second floating pill.
- **Rotate/crop are no longer in the viewer's top chrome.** The top strip is
  back / title / OCR only.
- **Sign and annotate act on the page being read**, like the document screen — never
  on `pages.first()`.
- Uncompiled here: `./gradlew testDebugUnitTest assembleDebug` is still owed, and
  pinch/swipe/shelf need a device pass.

## 6ak. Round 48: filter preview zoom + swipe, contextual detail bar (Arena, 2026-10-27)

**New:** `ui/common/ZoomablePageView.kt`.
**Edited:** `ui/editor/CropAndFilterScreen.kt`, `ui/detail/DocumentDetailScreen.kt`.

- **Do not add a third pinch gesture.** `ZoomablePageView(zoom, pan, onZoomPanChange,
  onSwipePrev, onSwipeNext, contentSize, maxZoom)` is the shared one. The border canvas
  keeps its own (it has four grabbed handles and `HANDLE_GRAB_DP`), but any *other*
  page view should use this. The filter preview's inability to zoom came from having
  no gesture at all, not from a broken one.
- **`zoom`/`pan` in `CropAndFilterScreen` are now genuinely shared by both stages.**
  They are reset on stage change and in `goToPage()`. If you add a third stage or a
  new way to change pages, reset the zoom there too.
- **Swipe is gated on `zoom <= 1.001` and on a single finger.** Zoomed in, horizontal
  drags must pan. Do not "fix" this by allowing swipe at any zoom.
- **`PageGridItem(page, canMoveBackward, canMoveForward, selected = false,
  onSelectClick = {}, onViewClick, …)`** — two new optional params. Existing call
  sites compile untouched.
- **Detail bottom bar is selection-driven.** Document state: `Add page` + `Export PDF`.
  Page state: `Crop`, `Annotate`, `Sign`, `More`. Adding an action means deciding which
  state it belongs to, not appending a fifth cell. Long-press selects a page; `Back`
  clears the selection.
- **`signatureOverlayUri` is applied to the picked page**, not page 1. Do not
  reintroduce a document-level `firstOrNull()` signature bake.
- Uncompiled here: `./gradlew testDebugUnitTest assembleDebug` is still owed, and
  pinch/swipe/selection need a device pass.

## 6aj. Round 47: UI/UX audit (Arena, 2026-10-27)

**New:** `docs/UI_UX_REVIEW.md`, `ui/common/TapTarget.kt`.
**Edited:** `ui/detail/DocumentDetailScreen.kt`, `ui/editor/MarkupDialog.kt`,
`ui/camera/ScannerCameraScreen.kt`, `ui/pdf/PdfExportSheet.kt`.

- **`TapTarget(onClick, label, selected, size)`** is the way to make a small visual
  pressable from here on. Do not put `clickable` directly on a drawing under 48 dp —
  that is exactly the bug this round fixed, in two places.
- Anything that sizes an `IconButton` below 48 dp shrinks its **touch target**, not
  just its visual: `IconButton` already applies `minimumInteractiveComponentSize` and
  an explicit `size()` overrides it. Leave the modifier off.
- Motion constants stay in `theme/Motion.kt`; `PdfExportSheet` gained a local
  `EXIT_MS = 100` because its accordions are sheet-local. Exits are faster than
  enters throughout — keep it that way in new code.
- Nothing visual changed outside these four files.

## 6ai. Round 46: annotation (Arena, 2026-10-27)

**New:** `model/Markup.kt`, `util/MarkupRenderer.kt`, `ui/editor/MarkupDialog.kt`,
`test/.../model/MarkupTest.kt`. **Edited:** `model/Models.kt` (`ScannedPage.markup`),
`util/PageRenderer.kt` (replay step), `ui/detail/DocumentDetailScreen.kt` (menu item).

- **`ScannedPage.markup` is persisted data, not pixels.** If you add another
  destructive-looking step to `PageRenderer`, put it **before** the markup replay;
  annotations are the top layer by definition.
- `MarkupStroke.widthFraction` is relative to the **short side** of the page, so a
  stroke keeps its apparent weight in either orientation. Don't change that to width
  or height alone without checking the portrait/landscape pair in `MarkupTest`.
- `MarkupRenderer.draw(canvas, strokes, width, height)` is the single drawing path.
  The preview and the bake must both go through it or they will drift; there is a
  `draw(bitmap, strokes)` overload for the bake.
- Anything that already copies a `ScannedPage` (crop, rotate, signature, watermark)
  keeps the markup automatically, because it is a field on the page.

## 6ah. Round 45: print (Arena, 2026-10-27)

**New:** `util/PdfPrinter.kt`. **Edited:** `ui/pdf/PdfExportSheet.kt` only.

- `PdfPrinter.print(context, file, jobName, paperSize)` opens the system print dialog
  for a PDF already on disk. It returns `false` rather than throwing; callers show a
  message.
- It needs an **Activity** context, and it cannot print an encrypted PDF. Both are
  checked by `canPrint()` and surfaced in the sheet before the attempt.
- `PaperSize` is the only geometry input: points → mils (`× 1000/72`). If another paper
  table is ever added, printing follows it automatically.
- `PdfExportSheet` now shares one `generateThen` path between share and print. If you
  change the export config, change it there — not in two places.

## 6ag. Round 44: ID card composition (Arena, 2026-10-27)

**New:** `util/CardComposition.kt`, `test/.../util/CardCompositionTest.kt`.
**Edited:** `data/ScannerRepository.kt` (one new suspend method + three file-level
constants), `ui/detail/DocumentDetailScreen.kt` (one app-bar action, one dialog).

- `Repository.createIdCardDocument(frontPageId, backPageId, title, folderId)` returns the
  new `Document?`. It writes `pages/<newId>_edited.png`, so a composed page is
  indistinguishable from a re-baked page to the orphan sweep and thumbnail store.
- `CardComposition` takes page sizes in **pixels** of whatever bitmap is being drawn
  into; it has no notion of points or dpi. A4 at 200 dpi is `1654 × 2339`.
- The composed page uses `FilterType.ORIGINAL` deliberately: it is already finished, and
  any filter over it would double-process the card.
- Camera capture semantics are untouched. If a two-step (front, then back) capture flow
  is added later, it should call the same repository method.

## 6af. Round 43: backup & restore (Arena, 2026-10-27)

**New:** `util/BackupArchive.kt`, `test/.../util/BackupArchiveTest.kt`.
**Edited:** `data/ScannerRepository.kt` (three new methods, nothing changed),
`ui/settings/SettingsScreen.kt` (two new parameters with defaults, one new section),
`Navigation.kt` (wires the two callbacks).

- `SettingsScreen` gained `onExportBackup: suspend (Uri) -> String` and
  `onImportBackup: suspend (Uri) -> String`, both defaulted, so any other call site
  still compiles.
- `ScannerRepository.exportBackup(uri)` / `importBackup(uri)` are **suspend** and run on
  IO. Import merges; it never deletes a local-only document.
- Archive format: `library.json` + `pages/*` + `signatures/*` + `backup.json`
  (`format`/`formatVersion`/`schemaVersion`/files with SHA-256). If you change
  `LibrarySnapshot`, bump `BackupArchive.FORMAT_VERSION` only if the *archive* layout
  changes — a schema change alone is covered by `schemaVersion`.
- App settings are not backed up. If that is wanted later it belongs in a separate
  entry, not in `library.json`.
- Nothing has been compiled here. The core is host-testable:
  `./gradlew testDebugUnitTest --tests '*BackupArchiveTest'`.

## 6ae. Round 42: sixteen paper presets, grouped (Arena, 2026-10-27)

**Touched:** `model/Models.kt`, `ui/pdf/PdfExportSheet.kt`, new
`test/.../model/PaperSizeTest.kt`, docs. Settings screen untouched.

- `PaperSize` gained a `group: PaperGroup` parameter. **Any code that constructs a
  `PaperSize` positionally must now pass four arguments** — nothing in the repo did,
  but check yours. Reading `widthPt` / `heightPt` / `label` is unchanged.
- Eight new entries (`B4`, `B5`, `LEDGER`, `TABLOID`, `ANSI_C`, `ANSI_D`, `ANSI_E`,
  `FOLIO`); `A3` moved from `1191` to `1190` pt to match the original's database.
  `BUSINESS_CARD` deliberately stays 243 × 153.
- **Do not render `PaperSize.entries` in a segmented control.** Sixteen segments is
  the failure mode described at the top of `Expressive.kt`; use `PaperSize.byGroup`
  (heading + 2-column cards) or the scrolling `ChoiceDialog`.
- Enum names for the eight old entries are unchanged, so persisted settings survive.
  Renaming one would silently reset it — `PaperSizeTest` pins the names.

## 6ad. Round 41: reconciliation of the master spec (Arena, 2026-10-27)

**No code touched.** `docs/ORIGINAL_APP_FULL_SPECIFICATION.md` gained a §9 section; that
is the only change, plus a cross-link from `docs/ORIGINAL_ML_BORDER_PATH.md`.

Three things worth knowing before §8 of that spec is used as a work queue:

1. The border model has **six** outputs and `StatefulPartitionedCall:5` is as wide as the
   scores (`[1,1,4]`). Bind by name. (§9.1, `ORIGINAL_ML_BORDER_PATH.md`.)
2. `app.db` has 64 paper sizes, **15** of them visible in the original picker; our enum
   has 8. Adding them means redesigning the two surfaces that enumerate the enum
   (`PdfExportSheet` 2-column grid, `SettingsScreen` segmented control), so it is a
   picker-screen task, not an enum edit.
3. §8's "100% parity" applies to borders, filters, dashboard, PDF and trash only.
   Capture modes, viewfinder tracking/auto-shutter, markup, background eraser,
   backup/restore and cloud sync are absent — the first two deliberately.

## 6ac. Round 40: the ML border model's real tensor layout (Arena, 2026-10-27)

**Claimed:** `util/MlBorderDetector.kt` output binding only. No other file touched.
Full evidence in `docs/ORIGINAL_ML_BORDER_PATH.md`.

- The shipped `border_detect_224.tflite` has **six** outputs. Only two are used:
  `StatefulPartitionedCall:1` `[1,1,4,2]` (8 floats, four corners as `(y, x)`) and
  `StatefulPartitionedCall:0` `[1,1,4]` (4 floats, one score per corner).
  `StatefulPartitionedCall:5` is also `[1,1,4]` and is **not** the scores — a size-based
  lookup can pick it.
- Binding is now by name, with an unambiguous-size fallback, and a refusal + log when
  neither is possible. Anything that reads `MlBorderDetector` sees the same public API
  (`detect(bitmap, context): MlResult?`); nothing to change on your side.
- Input encoding is `0…255`, coords are normalised `0…1`, gate is `mean >= 0.5f`, and the
  original detects on a bitmap downscaled to fit 640×640 with native OpenCV as its
  fallback (we use the Kotlin geometry pass there — unchanged).
- If you rebuild: the one new log line is `[ml] corners = StatefulPartitionedCall:1
  [1, 1, 4, 2], scores = StatefulPartitionedCall:0 [1, 1, 4]`. If you see the
  `[ml] cannot tell the corner tensor…` line instead, the asset changed.

## 6ab. Round 39: the ML quad is validated, the decoder stops assuming, and the build checks the model (Arena, 2026-10-27)

### The contract the ML path now keeps

```kotlin
MlBorderDetector.detect(bitmap, context): MlResult?
// corners, meanConfidence, confident (= confident AND plausible), rejection (shape problem)
QuadSanity.rejection(quad): String?   // null when the quad is a believable page
QuadSanity.isPlausible(quad): Boolean
```

`DocumentDetector.detect(src, context)` uses them in this order: **ML when confident and
plausible → Kotlin geometry → plausible-but-unsure ML → full frame.**

### Rules for the next agent

- **`detection.corners` is applied even when `confident == false`** in
  `CropAndFilterScreen` (an unsure quad is a starting point). Anything that produces a
  quad therefore has to be *plausible*, not merely *reported*. That is what
  `QuadSanity` is for — do not remove the check, and do not route a new detector around
  it.
- **Confidence ≠ plausibility.** Keep them separate in `MlResult`: merging them throws away
  the model's useful output on hard pages (where it is unsure but right).
- **Do not re-add `getOutputIndex("StatefulPartitionedCall:…")` or fixed output shapes.**
  Outputs are found by element count (8 and 4) and arrays are built from the tensor's own
  shape. Names and ranks differ between exports of the same graph.
- **`MlBorderDetector` has no `init()` and no static `Context`.** Pass the context to
  `detect`. A static context is a singleton by another name (§2.6).
- **Byte buffers are reused and `detect` is `@Synchronized`.** Do not call the interpreter
  from two threads, and do not share the scratch buffers by making the object stateless
  again without measuring: a 24-page batch allocated 24 × ~600 KB per pass before this.
- **`verifyBorderModel` runs before every build.** If you move the `.tflite` to the
  ignored `assets/models/`, keep that task (update its path) or the app will quietly lose
  ML detection and ship a build that looks fine.
- **`build.gradle.kts` test heap is 512m on purpose.** The test JVM is not the Gradle
  daemon; shrinking it only relocates an OOM into a confusing test failure.

### Still open, needs a decision

The 4.78 MB model is committed (AGENTS.md §2.5 says it should be provisioned). Guarded by
`verifyBorderModel`; sha256 pinned in `app/build.gradle.kts` and
`578f53b6…bdd1972`. No ABI filtering, so TFLite's four ABIs are all packaged.

## 6aa. Round 38 review: ML border detection must be able to load, and must be observable (Arena, 2026-10-27)

### Two rules that now hold

1. **`androidResources { noCompress += "tflite" }` is mandatory** for any model loaded
   through `AssetManager.openFd()` + `FileChannel.map()` (i.e. `MlBorderDetector`). aapt2
   deflates `.tflite` by default, `openFd` then throws, and the detector falls back to the
   Kotlin geometry pass with no visible difference. Added in `app/build.gradle.kts` with
   the reason spelled out. The same applies to any model added later.
2. **`MlBorderDetector` logs under `DocScannerPerf`** (debug only, `PerfTrace.isEnabled`):
   the model's real input/output tensor names and shapes on load, one line per detection
   (`accepted` / `low confidence` with the mean score and the quad), and one line per
   failure class with the exception message. If a device test looks like "auto-detect
   didn't change", those three lines are the answer; without them the ML path and the
   geometry path are indistinguishable.

### Do not undo

- **Do not remove `noCompress += "tflite"`.** It is the difference between the model
  loading and not. (And do not "fix" a load failure by switching to `assets.open()` +
  `readBytes()`: that copies 4.7 MB through the heap on every process start.)
- **Do not re-add a permanent failure latch.** A failed load is retried on the next
  detection; only the log line is throttled. One transient failure must not disable ML
  for the session.
- **Do not assume `StatefulPartitionedCall:0/1`.** The names and the `[1,1,4,2]` /
  `[1,1,4]` output shapes are hard-coded from `ht3.java`; the `[ml] model loaded` line
  prints what the file actually has. If they differ, `detect()` needs the real names, not
  a bigger `catch`.
- **A confident ML quad bypasses `DocumentQuad.validate`.** It goes into `Detection`
  unvalidated (area ratio, side ratios, ordering). Check real pages before trusting it.

### Known-open, needs a decision, not code

The 4.78 MB `app/src/main/assets/border_detect_224.tflite` is **committed**, against
`AGENTS.md` §2.5, `.gitignore` and §6i (ML assets are produced, never committed; the
ignored location is `assets/models/`). Left in place on purpose: removing it deletes the
file from every checkout, including the one being tested. To comply —
`git rm --cached app/src/main/assets/border_detect_224.tflite`, ignore
`app/src/main/assets/*.tflite`, keep the file on disk, and record its source.
sha256: `578f53b6c90ee9bc14b6ba350c3d261ffd6001d3e9b716a7ad571b6a4bdd1972`.

## 7. Known-good state

Last verified build: **Verified by Antigravity** on 2026-10-27 (`./gradlew testDebugUnitTest assembleDebug` passed — Rounds 36 & 37).
Rounds 36 & 37 (Arena & Antigravity, 2026-10-27): Verified source changes for PDF size estimates (PdfSizeEstimate.kt), cold-start quiet gate (StartupWork.kt), in-filter page switching (PageStrip), and robust crop handle dragging (CropHandles.kt). All unit tests pass (`BUILD SUCCESSFUL in 30s`), `app-debug.apk` built (66,209,649 bytes).
Rounds 34 & 35 (Arena & Antigravity, 2026-10-26): Verified source changes for PDF export sheet redesign, shared SegmentedPill, state change animations, and camera page Undo. Fixed `ScannerCameraScreen.kt` compilation errors (missing `width` import and `this@Row.AnimatedVisibility` explicit receiver). All unit tests pass (`BUILD SUCCESSFUL in 28s`), `app-debug.apk` built (66,209,649 bytes).
Round 33 (Arena & Antigravity, 2026-10-26): Verified Round 33 source changes. Fixed 3 compilation errors: `DashboardScreen.kt` (moved `visibleIds` after `activeDocs` declaration), `DocumentDetailScreen.kt` (added missing `LaunchedEffect` import), and `PageViewerScreen.kt` (added missing `@Composable` annotation to `ViewerPageStep`). All unit tests pass (`BUILD SUCCESSFUL in 24s`), `app-debug.apk` built (66,209,649 bytes).
Round 30: Added 6 new Rust kernels (meanPool, applyLut, rec601InPlace, gaussianBlur3, boxBlur3, boxBlurInPlace), bumped ABI to 2, and wired Kotlin dispatch with reference fallback for every kernel. All unit tests pass, `app-debug.apk` built (65,687,518 bytes).

## 8. Performance Optimization Analysis (GitHub Copilot, 2026-10-27)

> **Note**: This section documents optimization opportunities identified during a full workspace review. These are **analysis only** — no implementation has been done. Per user request, these thoughts are recorded here for other agents to review before any changes are made.

### 8a. REA (Reverse Engineering Agent) — TypeScript/Node.js MCP Server

**Architecture**: Worker-process isolation for JavaScript/web analysis, lazy provider loading, binary session composition with multiple providers (Hopper, Ghidra, IDA, Kuna).

#### High-Impact Opportunities

| Area                               | Current State                                                                                                                   | Optimization Opportunity                                                                                                                                                                                 | Risk                                                                              |
| ---------------------------------- | ------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------- |
| **Worker Request Handling**        | Sequential processing in `JavaScriptAnalysisWorkerServer.ts` — one job at a time with `serveJavaScriptAnalysisWorker` loop      | Add a small worker pool (2-4 workers) behind a request queue. The `analyzeJob` function is already stateless per-request.                                                                                | Low — worker protocol already supports concurrent requests via separate processes |
| **Memory Pressure Management**     | Manual `gc()` calls + heap monitoring in `JavaScriptAnalysisWorkerServer.ts:180-200`                                            | Replace reactive GC with proactive memory budgeting: track allocation rate, pre-emptively yield before pressure spikes. The `checkpointJavaScriptAnalysis` (8ms deadline) already provides yield points. | Low — uses existing checkpoint infrastructure                                     |
| **Transfer Protocol Byte Budgets** | Conservative retention: `bytes * 8 + records * 64` in `JavaScriptAnalysisTransfer.ts:45`                                        | Profile actual retention ratios; the 8x multiplier may over-provision by 2-4x. Consider adaptive budget based on observed compression ratios.                                                            | Medium — affects transfer admission; needs benchmarking                           |
| **Provider Lazy Loading**          | `LazyAnalysisProvider` loads on first client creation; `AnalysisProviderRegistry` uses `SharedStartupAttempt` for deduplication | Add provider warm-up for known target types (e.g., pre-load Hopper for Mach-O, Ghidra for ELF). Registry already tracks capabilities — can predict likely providers.                                     | Low — additive, preserves lazy-loading fallback                                   |
| **Turbo Build Cache**              | 7-day / 500MB cache in `turbo.json`                                                                                             | Increase cache TTL for stable tasks (typecheck, lint); add remote cache (S3/GCS) for CI. Current local-only cache misses on fresh clones.                                                                | Low — configuration only                                                          |
| **Vitest Parallelism**             | `maxWorkers: 2` in `vitest.config.ts`                                                                                           | Increase to logical CPU count for CPU-bound test suites (domain, services). Keep low for integration tests that spawn processes.                                                                         | Low — test-only change                                                            |

#### Medium-Impact Opportunities

- **Binary Session Composition**: `createBinarySession` in `composition/binary.ts` creates providers sequentially. Could parallelize provider discovery (`candidates()` calls) since they're independent.
- **Artifact Extraction**: `mcp_rea_extract_artifact` used in pipeline — consider streaming extraction for large APKs instead of full temp directory.
- **TypeScript Compilation**: `tsc --noEmit` for typecheck — consider `--incremental` with composite projects for faster incremental checks.

---

### 8b. doc-scanner — Android Document Scanner (Kotlin/Gradle)

**Architecture**: Jetpack Compose, Material 3 Expressive, CameraX, ML Kit, Rust kernels via JNI. Gradle 9.1.0, AGP 9.0.1, Kotlin 2.3.20.

#### High-Impact Opportunities

| Area                          | Current State                                                          | Optimization Opportunity                                                                                                                                     | Risk                                |
| ----------------------------- | ---------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------- |
| **Gradle Parallel Execution** | `org.gradle.parallel=true` **commented out** in `gradle.properties:22` | Enable parallel project execution. The project is decoupled (single `:app` module + root), so this is safe. Expected 20-30% configuration+execution speedup. | Low — standard Gradle feature       |
| **JVM Heap for Daemon**       | `-Xmx1536m` in `gradle.properties:6`                                   | Increase to `-Xmx3g` or `-Xmx4g` for large builds (Rust + Kotlin + Compose). Current heap may cause GC thrashing during `:app:compileDebugKotlin`.           | Low — more memory, faster builds    |
| **Configuration Cache**       | Already enabled (`org.gradle.configuration-cache=true`)                | Good — keep enabled. Verify all tasks are cache-compatible (no `doLast` with file I/O).                                                                      |
| **Kotlin Daemon**             | `-Xmx768m` in `gradle.properties:7`                                    | Increase to `-Xmx1536m` — Kotlin compilation is memory-intensive with Compose compiler.                                                                      |
| **Rust Kernel Build**         | `buildRustKernels` task runs on every `preBuild`                       | Cache Rust artifacts via `cargo` cache + Gradle build cache. Current: re-compiles on every clean. Add `cargo` cache directory to Gradle cache.               | Medium — requires cargo cache setup |
| **Benchmark Build Type**      | `benchmark` type exists (non-debuggable, debug-signed)                 | Excellent — already implemented. Ensure CI uses `assembleBenchmark` for performance validation.                                                              |

#### Medium-Impact Opportunities

- **Compose Compiler Reports**: Enable `-Pcompose.compiler.metrics=true` to identify recomposition hotspots.
- **Baseline Profile**: §6m notes this as "biggest lever" — requires physical device. Add `:baselineprofile` module when device available.
- **RGB_565 for Thumbnails**: Already implemented in Round 29 (`PageImageLoader.OPAQUE_TILE_LIMIT = 1024`). Verify it's active in release builds.
- **Thumbnail Prefetch**: Round 24 added warm-up rows — consider increasing `WARM_ROWS` from 6 to 8-10 for larger screens.

#### Already Optimized (Rounds 20-30)

- ✅ Thumbnail cache (`ThumbnailStore`) — 768px JPEG, written at save time
- ✅ Frame pacing for 120Hz displays (`FramePacing.kt`, `FrameBudget.kt`)
- ✅ Rust acceleration of 9 filter kernels (ABI v2)
- ✅ Single-flight image loader with memory cache
- ✅ Row-quantised prefetch, memoised thumbnail keys
- ✅ Overdraw elimination (transparent Scaffold, footer-only backgrounds)
- ✅ Stable callbacks in `LazyVerticalGrid` (no inline lambdas)
- ✅ Atomic library writes, debounced persistence

---

### 8c. re-workspace — APK Reverse Engineering Pipeline (PowerShell/Bash)

**Architecture**: 8-step sequential pipeline in `re_pipeline.ps1` — archive → JADX → apktool → native extraction → REA analysis → Ghidra decompilation.

#### High-Impact Opportunities

| Area                         | Current State                                                                | Optimization Opportunity                                                                                                                                                                                                             | Risk                               |
| ---------------------------- | ---------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ---------------------------------- |
| **Pipeline Parallelization** | Steps 1-4 sequential; Steps 5-7 (REA + Ghidra per `.so`) sequential per file | **Embarrassingly parallel**: each `.so` analysis is independent. Run REA + Ghidra for all `.so` files in parallel (PowerShell `Start-Job` or `ForEach-Object -Parallel`). Expected 4-8x speedup on multi-core for native-heavy APKs. | Low — independent operations       |
| **JADX + apktool Parallel**  | Step 2 (JADX) then Step 3 (apktool)                                          | These are independent — run concurrently. Both read the APK, write to separate output dirs.                                                                                                                                          | Low — no shared outputs            |
| **REA Analysis Caching**     | REA re-analyzes same `.so` on every run                                      | Cache REA reports keyed by `.so` SHA-256. Skip if unchanged. REA already outputs deterministic YAML.                                                                                                                                 | Low — additive caching layer       |
| **Ghidra Headless Reuse**    | New Ghidra project per `.so` (`temp_proj` deleted after)                     | Reuse single Ghidra project for all `.so` files in one APK. Ghidra supports multiple programs per project. Avoids JVM startup overhead per file.                                                                                     | Medium — Ghidra project management |
| **Temp Directory Cleanup**   | `$tempZipDir` created/deleted per run                                        | Use persistent temp with cleanup policy; avoid repeated `tar.exe` extraction for unchanged APKs.                                                                                                                                     | Low                                |

#### Medium-Impact Opportunities

- **PowerShell 7+ / pwsh**: Migrate from Windows PowerShell 5.1 to PowerShell 7+ for `ForEach-Object -Parallel`, better performance, cross-platform.
- **Structured Logging**: Replace `Write-Host` with structured JSON logs for pipeline observability and CI integration.
- **Incremental APK Analysis**: Track APK hash; skip entire pipeline if APK unchanged (unless forced).

---

### 8d. Cross-Project Synergies

| Synergy                | Description                                                                                                           |
| ---------------------- | --------------------------------------------------------------------------------------------------------------------- |
| **REA ↔ re-workspace** | re-workspace calls `rea` CLI as subprocess. REA's worker pool (8a) would directly accelerate re-workspace Steps 5-6.  |
| **doc-scanner ↔ REA**  | doc-scanner's Rust kernels could be analyzed by REA for verification/parity checking (Ghidra decompilation of `.so`). |
| **Shared Tooling**     | All three use JDK 21, Android SDK. Centralize tool versions in one place (e.g., `tools/versions.toml`).               |

---

### 8e. Prioritized Action Plan (If Implementation Approved)

| Priority | Task                                          | Project      | Effort        | Expected Gain                |
| -------- | --------------------------------------------- | ------------ | ------------- | ---------------------------- |
| 1        | Enable `org.gradle.parallel=true`             | doc-scanner  | 1 line        | 20-30% build speedup         |
| 2        | Increase Gradle/Kotlin daemon heaps           | doc-scanner  | 2 lines       | Reduced GC pauses            |
| 3        | Parallelize `.so` analysis in re_pipeline.ps1 | re-workspace | ~30 lines     | 4-8x native analysis speedup |
| 4        | Add REA worker pool (2-4 workers)             | REA          | ~50 lines     | 2-4x JS analysis throughput  |
| 5        | Cache REA/Ghidra results by SHA-256           | re-workspace | ~40 lines     | Skip unchanged analyses      |
| 6        | Adaptive transfer byte budgets                | REA          | ~20 lines     | Reduced memory pressure      |
| 7        | Baseline Profile for doc-scanner              | doc-scanner  | New module    | 20-40% first-run frame time  |
| 8        | Cargo cache for Rust kernels                  | doc-scanner  | Gradle config | Faster Rust builds           |

---

### 8f. Validation Requirements

Before any optimization lands:

1. **REA**: Run `npm run test:ci` (full test suite + coverage) + `npm run verify:javascript:retained-facts`
2. **doc-scanner**: `./gradlew testDebugUnitTest assembleBenchmark` + device scroll test (`adb logcat -s DocScannerPerf`)
3. **re-workspace**: Run pipeline on 3+ APKs (small/medium/large native lib count) and compare wall time

---

**Recorded by**: GitHub Copilot (Nemotron 3 Ultra)  
**Date**: 2026-10-27  
**Status**: Analysis only — awaiting review by Arena.ai and Antigravity before any implementation

## 6an. Round 51: connected button group, real `SegmentedButton` (Arena, 2026-10-28)

**New:** `ui/common/ConnectedButtonGroup.kt`.
**Edited:** `ui/common/ExpressiveAction.kt`, `ui/detail/DocumentDetailScreen.kt`,
`ui/viewer/PageViewerScreen.kt`, `ui/editor/CropAndFilterScreen.kt`.

- **Read before touching any bar.** `ConnectedButtonGroup` implements Google's connected
  group shape rule, quoted from `material-components-android`:
  `lib/java/com/google/android/material/button/res/values/button_group_tokens.xml` and
  `res/xml/m3expressive_connected_buttons_inner_corner_size_state_list.xml`.

  | Part | Token | Value |
  | :--- | :--- | :--- |
  | outer corners | `ShapeAppearance.M3.Comp.ButtonGroup.Connected.Small.Container.Shape` | Full (pill) |
  | inner corners, rest | `shapeCornerSizeSmall` | 8 dp |
  | inner corners, pressed | `shapeCornerSizeExtraSmall` | **4 dp** (tighter, not rounder) |
  | inner corners, selected | state list | 50 % |
  | spacing between cells | `m3_comp_button_group_connected_small_between_space` | 2 dp |

  `MaterialButtonGroup.updateChildShapes()` is the Views implementation of the same
  override — first visible child keeps its outer left corners, last keeps outer right,
  everything else is overridden, and RTL swaps the left/right bits.
- **Anatomy deviation, deliberate:** ours stacks icon over label (four labelled actions
  on a 360 dp bar do not fit side by side); Google's puts them in a row and ships the rest
  in an overflow menu. If a bar ever grows an overflow menu, un-stack.
- **Deleted:** `ExpressiveActionItem` (`ui/common/ExpressiveAction.kt`) and
  `ViewerMoreAction` (`ui/viewer/PageViewerScreen.kt`). Nothing else referenced them.
  `ExpressiveActionBar`, `ExpressivePager`, `BarHairline` are unchanged.
- **`ConnectedAction.menuContent`** is the split-button pattern folded into a cell. With
  `onClick` as well, the body runs the action and a **26 dp chevron** at the trailing edge
  opens the menu; with `onClick == null` the cell *is* the menu and there is no chevron.
  Items are `ColumnScope.(dismiss: () -> Unit)` — call `dismiss()` first in each item's
  `onClick`, because `DropdownMenuItem` does not close its own menu.
- **`SegmentedPill` in `ui/common/Expressive.kt` is still live** for the camera's
  Single/Batch toggle (it needs the glass palette injected). The editor's BORDERS/FILTERS
  switch is now the real `SingleChoiceSegmentedButtonRow` + `SegmentedButton` with
  `SegmentedButtonDefaults.itemShape(index, count)`.
- **Do not bump `material3` to `1.5.0-beta01`** to get `ButtonGroup`, `SplitButtonLayout`,
  `ToggleButton` or `HorizontalFloatingToolbar`. Verified against the `androidx-main` API
  snapshots: those symbols do not exist in the 1.3/1.4 lines, and this project does not
  take a beta for a parity build. `LoadingIndicator` at 1.4.0-beta03 is
  `@ExperimentalMaterial3ExpressiveApi`. Re-check before assuming any of them.
- **New gallery-import path in the detail screen:** `ActivityResultContracts
  .PickMultipleVisualMedia` → `ImageStore.importFromUri` (same decoder and quality cap as
  capture) → `repository.addPageToDocument` → `repository.indexTextInBackground`. A page
  can now join an existing document without the camera screen.
- **MCA is the Views library** (`AGENTS.md` §2 forbids View code here). Mine it for
  anatomy, thresholds and tokens only — never paste its Java.
- Uncompiled here: `./gradlew testDebugUnitTest assembleDebug` is still owed.

## 6ao. Round 52: annotate screen, drag reorder, Files-style camera (Arena, 2026-10-28)

**New:** `ui/editor/MarkupScreen.kt` (route), `ScannedPage.revision`,
`ScannerRepository.movePages`, `MarkupNav`.
**Deleted:** `ui/editor/MarkupDialog.kt`, `ui/common/Expressive.kt::SegmentedPill`.

- **`SegmentedPill` is gone. Use `ConnectedButtonGroup` for every segmented control.**
  Label-only cells are supported (`ConnectedAction.icon = null`); a `selected` cell fills
  with the accent and relaxes to a pill. Do not reintroduce a second segmented control.
- **A page's pixels change without its URI changing.** `PageRenderer.bake` rewrites
  `_edited.jpg` in place and bumps `ScannedPage.revision`. Anything that caches, decodes
  or keys on a page image **must pass the revision** (`rememberPageImage(..., revision)`,
  `PageThumbnail` does it for you). Forgetting it is how rotate, filter and annotation
  changes went invisible.
- **Reorder is a drag now.** `Repository.movePages(docId, ids, targetIndex)` moves a
  block; `targetIndex` counts the list *without* the moved pages. `PageGridItem` owns the
  gesture (`detectDragGesturesAfterLongPress`) and the destination maths lives in
  `DocumentDetailScreen` (`updatePageDrag`): the hovered slot and the commit index are
  two different coordinate systems — do not collapse them back into one.
- **Long press in the page grid adds to the selection and never removes.** It is also the
  start of a drag; a long press that un-picked the page would leave the drag nothing to
  move. Tap still toggles.
- **Annotating is a route, not a dialog.** `MarkupNav(docId, pageId)`; the screen owns the
  stroke history and re-bakes on save. Signature and PDF export are still dialogs.
- **`CropAndFilterNav(..., borders = true)`** is what the Crop button passes. Do not
  "simplify" it back to the default stage: landing on filters after tapping Crop was the
  bug.
- **Camera chrome is Files by Google's scanner shape:** two top controls, one rounded
  control sheet at the bottom. Capture semantics are load-bearing and unchanged — 4:3
  selector, `FIT_CENTER`, `MAXIMIZE_QUALITY`, flash/torch, shutter sound, auto-save,
  batch vs single commit, discard cleanup.
- **Border-handle state must stay stable.** `BorderCanvas`'s `liveQuad` is
  `remember { }` with no keys *on purpose*; `pointerInput` does not restart when its keys
  do not change, so keying the state on `corners` silently breaks every drag after the
  first. Outside updates are adopted by the `LaunchedEffect(corners)`.
- Uncompiled here: `./gradlew testDebugUnitTest assembleDebug` is still owed.

## 6ap. Round 53: connected verbs, in-place annotate, instant rotate, share a selection (Arena, 2026-10-28)

**New:** `ui/editor/MarkupStudio.kt`, `util/DocumentShare.kt`,
`ScannerRepository.duplicatePages`, `ScannerRepository.movePagesToDocument`,
`ImageStore.copyPageFiles`.

- **`ConnectedButtonGroup` cells now provide `LocalContentColor` and snap their tint.**
  That component was the cause of the camera's Single/Batch switch and the reader's More
  button "glitching or disappearing": a dark theme ripple on a black sheet, plus a label
  tint that animated through white-on-white while the selected plate crossfaded. Do not
  reintroduce an animated content colour on a filled cell, and always pass
  `onAccentColor` when the accent is white.
- **`ConnectedAction(label = "", icon = …)` is an icon-only cell** (20 dp glyph, no label,
  no vertical padding). That is what the page tiles use; labelled cells are unchanged.
- **Annotating is a mode of the reader, not a route.** `PageViewerScreen` no longer takes
  `onNavigateToMarkup`; the shelf swaps in place and the drawing surface is
  `MarkupInkSurface(session, image, modifier)` from `MarkupStudio.kt`. `MarkupNav` is
  still registered for the document grid's tile menu. `MarkupScreen` keeps its own
  full-screen chrome but imports the shared palette/constants — **do not re-declare
  `INK_COLORS`/`WIDTHS`/`inkLabel`, they live in `MarkupStudio.kt` now.**
- **`MarkupInkSurface` is sized by its caller and must match the page's aspect ratio.**
  Strokes are normalised against the box they are drawn in; a letterboxed box puts the
  ink somewhere else on the bake.
- **Rotate is optimistic, not faster.** The decoded bitmap is turned in memory
  (`rotateBitmap`) and the real bake runs on `Dispatchers.IO`; the preview is replaced
  when the re-decoded page arrives (identity of the new `ImageBitmap`). Never shorten the
  bake itself — the pixels are the contract.
- **The library header condenses on scroll-up** (`snapshotFlow` + two thresholds, so it
  cannot flicker against its own layout change). ~~Its facts move into
  `MagicSearchField(caption = …)`~~ — **superseded in Round 54**: the caption was removed
  from the field and the facts became one line in the app bar's title column. See §6aq.
  Do not add a `caption` parameter back to `MagicSearchField`.
- **Sharing a selection does not merge it.** `DocumentShare.shareDocuments` writes one
  PDF per document at the Settings → PDF defaults and sends them with
  `ACTION_SEND_MULTIPLE`. The merge cell is unchanged.
- Uncompiled here: `./gradlew testDebugUnitTest assembleDebug` is still owed.

## 6aq. Round 54: the second pass on the same seven reports (Arena, 2026-10-28)

Round 53 shipped and the user re-opened five of the seven items as "still broken". Every
one of them had the same shape: the *animation* was blamed and the *layout* was guilty.
Read this section before touching any of these files again.

**New file:** none. **New type:** `DocShapes.outwardTop` (`theme/Shape.kt`).

### The rules this round established

- **A filled cell must never crossfade its plate or its ink.** `ConnectedButtonGroup`
  now computes `plate`/`tint` directly for the filled/discrete states and animates a
  separate ink-coloured press layer on top. This is the root cause of every
  "the buttons disappear when I toggle" report (camera Single/Batch, reader More,
  annotate tool switch) — a mid-grey frame with black ink on it. Do not reintroduce
  `animateColorAsState` across the `emphasized || selected` boundary.
- **A menu must be owned by the group, never by a cell.** `openMenuIndex` +
  one `DropdownMenu` anchored to the group's own `Box`. A popup declared inside a
  weighted cell is anchored to a box whose position is re-resolved whenever the bar's
  contents change, which is what made the More cell blink out for the instant it was
  tapped.
- **Measure the shelf, do not guess its height.** `ExpressiveActionBar` reports its own
  height through `onSizeChanged`; the page's bottom inset is
  `animateDpAsState(measured + ShelfGap)`. `ReaderShelfInset = 132.dp` survives only as
  the first-frame guess. This is what makes the card's edge spacing even on every
  screen shape.
- **One margin: `ShelfPadding = 8.dp`** is the bar's outer padding, the inner frame and
  the gap between rows. The annotate shelf's grab handle is a **12 dp band** with the
  4 dp dash at its top, so the card edge → dash and dash → first control gaps are both
  8 dp. A 28 dp strip with a centred dash leaves ~18 dp of unexplained air at the top.
- **The drag to dismiss the annotate shelf lives on the whole shelf**, not on the handle
  strip. `detectVerticalDragGestures` claims the gesture after slop, so swatch taps are
  unaffected.
- **Never animate away a control that sits inside the item under the finger.** The page
  tile's verb strip keeps its height and goes `enabled = false` (38 % via the cell's own
  alpha) while anything is selected; `PageGridItem` takes `verbsEnabled`. Collapsing it
  reflowed every tile below the held one.
- **`Scaffold`'s `floatingActionButton` slot is positioned relative to the bottom bar.**
  The capture button therefore moved into the content `Box` with `Alignment.BottomEnd`
  and `navigationBarsPadding()`; a bottom bar appearing under it cannot make it lag.
- **A page is tracked by id in the reader, never by index.** `currentPageId` +
  `goToPage(index)`; the index is resolved against the live list every frame. Keying the
  seed state on `pages.size` threw the resolved page away whenever a copy/move changed
  the length.
- **A re-bake must not blank the frame.** `rememberPageImage` retains the last bitmap for
  the same `imageUri` and stands it in while the new revision decodes. Without it every
  rotate flashed the fallback for a frame; the reader's optimistic preview only hid it
  in the reader, not in the grid behind.
- **`ImageStore.copyPageFiles` builds the copy's thumbnail from the copied file**
  (`newUri`), not from the pristine capture — otherwise a copied page showed its
  unfiltered pixels while its record still said `MAGIC_COLOR`, i.e. "Copy loses the
  filter".
- **`DocShapes.outwardTop`** is the concave-cornered shape under the library's search
  field (twin `quadraticTo` shoulders). Use it only for a surface that *continues* the
  screen under a fixed header.

### Behaviour changes a reviewer should check

- Library: the app bar title is `Column { "Doc Scanner"; AnimatedVisibility(condensed)
  { summary } }`; `MagicSearchField` no longer takes `caption`, is a fixed 54 dp and no
  longer animates its height. The `headerSummary` string is hoisted **above** `Scaffold`.
- Library: the grid sits in a `Box` clipped with `DocShapes.outwardTop` on
  `surfaceContainerLow`.
- Detail: the move picker is `MovePagesSheet` (half height, search last in the animation),
  and picking a target now navigates into it via the new
  `DocumentDetailScreen(onNavigateToDocument = …)` parameter — `Navigation.kt` wires it to
  `addDetailIfNeeded`.
- Reader: `AnnotateShelf` is draggable anywhere, its handle is a 12 dp band, its three
  groups are all `surfaceContainerLowest` on `primary` with `onPrimary` ink.

Uncompiled here: `./gradlew testDebugUnitTest assembleDebug` is still owed by Antigravity.

## 6ar. Round 55: the annotate shelf's disappearing controls (Arena, 2026-10-28)

Reported on the Round-54 build: in the reader, with the annotate shelf open, **Pen,
Erase, Thin and Bold disappear when tapped and come back when held**. Five independent
ways for the same words to stop being drawn, all of them now removed. Do not
reintroduce any of them; each one is a control that can hide itself.

1. **An ink-coloured ripple, bounded to the label's own rectangle.**
   `ConnectedButtonGroup`'s cell used `ripple(color = tint.copy(alpha = 0.22f))` on the
   `Column` that *is* the label. A tap therefore laid a wash of the label's colour over
   the label. Cells are now `indication = null`; the press is the 14 % ink state layer
   that was already drawn *under* the content (`pressLayer`). This is also what
   `MaterialButtonGroup` does — a state layer, not a ripple.
2. **A whole-cell `graphicsLayer { alpha }` for the disabled state.** It faded the
   *plate* with the ink, so a disabled cell on a white card became 38 % white — i.e.
   absent, not dimmed. Disabled now dims the **ink only** (`inkAlpha`, applied to the
   content `Column` and to the split chevron); the plate is always opaque.
3. **The eraser disabled the width row.** `AnnotateShelf` had `inkEnabled = tool !=
   WHITEOUT` wired into the width cells and the swatches. Choosing Erase therefore
   greyed out Thin/Medium/Bold — the exact buttons in the report. **No control on the
   shelf is disabled any more**: weight applies to a whiteout stroke as much as to a
   pen, and a colour chosen while erasing is simply the colour of the next stroke.
4. **`ExpressiveActionBar` owned an entrance that could hide it.** Its `remember {
   Animatable(0f) }` + `LaunchedEffect(Unit)` drove `alpha` from 0 to 1 — a bar that is
   invisible by default and only visible because an effect ran. Any recreation of the
   composable restarts that from zero, and every control in the bar blinks out. The
   entrance is now **translate-only**; a translation cannot hide anything.
5. **`AnimatedContent` clips to its animating size.** The two shelves have different
   heights, so during a swap the incoming shelf's lower rows are clipped away. The
   transform is now built explicitly:
   `ContentTransform(enter, exit, sizeTransform = SizeTransform(clip = false))`. Written
   as the constructor because `a togetherWith b using x` binds `using` to the **else
   branch only** — a trap that would have silently left the annotate direction clipped.

### The switches are no longer connected-group cells

`SingleChoiceSegmentedButtonRow` + `SegmentedButton` (the same control `MarkupScreen`
uses) for the tool row and the weight row; the action row (undo/redo/clear/cancel/save)
stays a `ConnectedButtonGroup`. A single-choice switch and an action bar are different
controls: the group's cell is a plate + state layer + ripple + press scale + a shape that
animates through three radii, which is a lot of machinery for two words and five ways to
lose them. `pressScale` is gone from the cell for the same reason (it was the third
animating layer on one affordance, and it scaled content inside a clipped pill 2 dp from
its neighbour).

### Also in this round

- The shelf's drag target is the **20 dp handle band** again (8 dp above the dash, 4 dp
  of dash, 8 dp below — the same 8 dp as everything else), not the whole shelf. A
  `pointerInput` drag detector wrapped around a row of buttons competes with their click
  handlers in the same pass, so a tap that drifts is claimed by the drag and the click is
  cancelled.
- Swatches: `indication = null`, a 12 % squeeze of the dot on press, ring always drawn.
  A colour is never disabled.
- `MarkupScreen` still hides its swatch row while erasing (`if (tool != WHITEOUT)`). That
  is a *layout* change in a full-screen editor, and it was not reported; the reader keeps
  a fixed three-row card instead, because a row appearing and disappearing changes the
  bar's height under the finger.

Uncompiled here: `./gradlew testDebugUnitTest assembleDebug` is still owed by Antigravity.
