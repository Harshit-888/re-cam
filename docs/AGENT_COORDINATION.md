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

## 7. Known-good state

Last verified build: **Verified by Antigravity** on 2026-10-25 (`./gradlew assembleDebug testDebugUnitTest` passed).
Fixed DocumentDetector.kt compiler error; verified Settings screen, slide transitions, zoom, and test suite. APK built successfully at `app/build/outputs/apk/debug/app-debug.apk` (versionCode 5, versionName 1.4).


