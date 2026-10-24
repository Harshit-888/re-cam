# Agent Changelog

Append-only. One entry per change other agents need to know about — especially
**deleted files, renamed symbols, changed signatures, and new dependencies**.
Newest entry at the top.

---

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
