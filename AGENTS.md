# 🤖 AGENTS.md — AI Engineering & Development Directives

> **MANDATORY CONTEXT FOR ALL AI AGENTS (Antigravity, Arena.ai, Cursor, Copilot, Claude Code, etc.)**
> Read this document completely before generating code, proposing architectural changes, or continuing development in this repository.
>
> ⚠️ **Two agents work on this repo at once.** Read §0 before touching any file.

---

## 🤝 0. MULTI-AGENT COORDINATION PROTOCOL (READ FIRST)

This repository is edited concurrently by **Antigravity** (local IDE, can build)
and **Arena.ai** (Git branch, cannot build). They share one working tree.

**The full contract lives in [`docs/AGENT_COORDINATION.md`](docs/AGENT_COORDINATION.md). Non-negotiables:**

1. **Claim before you edit.** Add a row to the *In-flight changes* table in
   `docs/AGENT_COORDINATION.md` naming the files you're about to touch, before
   you touch them. Remove the row when you're done.
2. **Never reformat a file you didn't claim.** No project-wide import sorting, no
   `ktlint`/`ktfmt` sweeps, no whitespace-only commits. That is what makes
   concurrent diffs conflict.
3. **Additive beats destructive.** New file > new function > new defaulted
   parameter > changed signature > renamed symbol > deleted file.
4. **Never rename or change the signature of a §3 "Integration contract" symbol**
   without a `docs/AGENTS_CHANGELOG.md` entry and all call sites updated in the
   same commit.
5. **Log every deletion and signature change** in `docs/AGENTS_CHANGELOG.md`.

**Arena.ai has no JDK and no Android SDK.** It cannot run `./gradlew`. Any build
result it reports is unverified. **Antigravity is the build authority**: if a
build fails, the compiler output wins over Arena's reasoning.

---

## 🎯 1. Project Mission & North Star

This repository (`re-cam` / `DocScanner`) is a **clean-room, ground-up rewrite** of a legacy Android document scanner (`com.indymobileapp.document.scanner`).

The goal is a state-of-the-art, privacy-respecting, high-performance Android document scanner that feels ultra-premium using **Material 3 Expressive UI** and **Jetpack Compose**, completely free of third-party advertising SDKs, tracking libraries, and obsolete architecture.

---

## 🚫 2. STRICT GUARDRAILS (What You MUST NOT Do)

1. **NO Legacy Android XML UI**:
   - **NEVER** write XML layouts, `findViewById`, `ViewBinding`, or `DataBinding`.
   - **NEVER** create legacy `Activity` subclasses for individual screens. The app is a modern **Single-Activity Jetpack Compose** app (`MainActivity.kt`).
2. **NO Java Code**:
   - The entire codebase is **100% Kotlin**. Do not introduce `.java` source files.
3. **NO Ad Networks or Tracking SDKs**:
   - The original app contained massive ad networks (AdMob, AppLovin, ByteDance/Pangle, InMobi). **Do NOT add any ad networks or invasive tracking.**
4. **NO DRM or License Bypassing**:
   - Do not implement any license cracks or bypasses. All business logic must be clean-room developed.
5. **DO NOT Pollute the Git Repository**:
   - Do not commit binaries, `.apk` files, decompiled smali dumps, Ghidra files, or build artifacts (`build/`, `.gradle/`).
6. **NO Global Singletons / Spaghetti State**:
   - No `object AppGraph`, no `by lazy` repository accessors, no static mutable state. The `ScannerRepository` is constructed **once** in `MainActivity`, passed to `MainNavigation(repository)`, and then passed to screens **as a parameter**. Every screen signature takes `repository: ScannerRepository`. Unidirectional Data Flow via `StateFlow` only.
7. **NO Placeholder Data in Shipping Paths**:
   - Do not paint fake pages, fake thumbnails, fake text lines, or fake scan artifacts to make a screenshot look good. If pixels aren't coming from `ImageStore`, something is wrong. The previous revision of this app failed exactly this way.

---

## 🏗️ 3. Architectural Rules & Principles

**`gradle/libs.versions.toml` is the single source of truth for versions.** The table below mirrors it; if they disagree, the TOML wins.

| Layer | Standard | Implementation Reference |
| :--- | :--- | :--- |
| **Language & Toolchain** | Kotlin 2.3.20 • AGP 9.0.1 • Gradle 9.1.0 • minSdk 26 • compileSdk 36 | [`app/build.gradle.kts`](app/build.gradle.kts) |
| **UI Toolkit** | Jetpack Compose BOM 2026.03.01 | [`theme/Theme.kt`](app/src/main/java/com/example/docscanner/theme/Theme.kt) |
| **Design System** | **Material 3 Expressive** (tonal containers, dynamic colors, spring motion, pill chips) | [`theme/Color.kt`](app/src/main/java/com/example/docscanner/theme/Color.kt) |
| **Navigation** | Jetpack Navigation 3 (Type-Safe `NavKey`s) | [`Navigation.kt`](app/src/main/java/com/example/docscanner/Navigation.kt) |
| **State Management** | UDF via `StateFlow`, repository injected down the tree | [`data/ScannerRepository.kt`](app/src/main/java/com/example/docscanner/data/ScannerRepository.kt) |
| **Persistence** | `library.json` snapshot, atomic write, coalesced | [`data/LibraryStore.kt`](app/src/main/java/com/example/docscanner/data/LibraryStore.kt) |
| **Preferences** | `settings.json`, same atomic-write pattern, exposed as `repository.settings` | [`data/SettingsStore.kt`](app/src/main/java/com/example/docscanner/data/SettingsStore.kt) |
| **Domain Models** | Immutable `@Serializable` Kotlin data classes | [`model/Models.kt`](app/src/main/java/com/example/docscanner/model/Models.kt) |
| **Image Storage** | `filesDir/pages/<pageId>.jpg` + `_edited.jpg` bakes | [`util/ImageStore.kt`](app/src/main/java/com/example/docscanner/util/ImageStore.kt) |
| **Image Loading** | Sampled decode + `LruCache` + `rememberPageImage()` | [`util/PageImageLoader.kt`](app/src/main/java/com/example/docscanner/util/PageImageLoader.kt) |
| **Perspective Correction** | `Matrix.setPolyToPoly` 4-point projective warp | [`util/PerspectiveWarp.kt`](app/src/main/java/com/example/docscanner/util/PerspectiveWarp.kt) |
| **Image Processing** | Ported from the original APK: `fa1.H` estimator (100×100, 2 % cutoffs, `(R+G+B)/3`), *Lighten* = linear stretch, document filters = paper-max normalisation + ink curve, Otsu B&W (**not** a global `ColorMatrix`) | [`util/ImageProcessingUtils.kt`](app/src/main/java/com/example/docscanner/util/ImageProcessingUtils.kt) |
| **Camera** | CameraX 1.4.2 (`PreviewView` in `AndroidView`) | [`ui/camera/ScannerCameraScreen.kt`](app/src/main/java/com/example/docscanner/ui/camera/ScannerCameraScreen.kt) |
| **PDF Engine** | Native `android.graphics.pdf.PdfDocument` @ 72 DPI, **IO dispatcher only** | [`util/PdfGenerator.kt`](app/src/main/java/com/example/docscanner/util/PdfGenerator.kt) |

### Image data-flow contract (do not break)

```
CameraX / gallery
      │  ImageStore.importFromUri() / takePicture()
      ▼
filesDir/pages/<pageId>.jpg        ← pristine, never overwritten
      │  editor "Save" bakes warp → rotate → DocumentFilters (real per-pixel)
      ▼
filesDir/pages/<pageId>_edited.jpg ← becomes ScannedPage.imageUri
      │  originalImageUri keeps the pristine path for repeat edits
      ▼
PageImageLoader → PageThumbnail / viewer / PdfGenerator
```

`ScannedPage.imageUri` **must** be an absolute filesystem path in this app's
private storage. Never store a `content://` URI in it — import first.

---

## 📊 4. Current State Matrix

### ✅ Working end-to-end
- **Dashboard** (`ui/dashboard/`): instant search over titles/tags/OCR text, folder chips, grid vs list, sorting, extended FAB, folder creation, and *Move to folder…* on every document card.
- **Document detection** (`util/DocumentDetector.kt`): edge-clamped Sobel → per-row/column **boundary scan** → **RANSAC** line per side → intersect → validate. A side with no visible boundary falls back to the frame edge. Returns `Detection(corners, confident)`; the editor tells the user when it is not confident instead of failing silently.
- **Filters** (`util/DocumentFilters.kt`): *Magic Color* (shadow removal + white balance + single clamped luminance stretch + saturation), *Clear B&W* (Otsu, asymmetric ramp, 3×3 despeckle), *Grayscale*, *Lighten*, *Original*. The editor previews the **real** filter at reduced resolution, so preview == baked output.
- **Camera** (`ui/camera/`): real CameraX preview, runtime permission, flash (auto/on/off via torch), batch counter, gallery import, JPEG capture to `ImageStore`.
- **Post-capture review** (`ui/editor/CropAndFilterScreen.kt`, `reviewMode = true`): the step the original app had — *Done* opens each captured page with **auto-detected document borders**, draggable corners, live filter preview and rotation, then *Save & Next* through the batch before landing on the document.
- **Perspective Crop & 5 Filters** (`ui/editor/`): draggable 4-corner quad backed by a **real projective warp**, 90° rotation, 5 filters rendered through `DocumentFilters`, non-destructive bake.
- **Auto document detection** (`util/DocumentDetector.kt`): boundary scan → RANSAC four-line fit → validated corners. Runs on every freshly captured page and is visible immediately (review mode opens on Borders).
- **Document Detail** (`ui/detail/`): multi-page grid with **real thumbnails**, page rotation, deletion, reordering, append scans, title **and notes** editing.
- **Fullscreen Viewer** (`ui/viewer/`): real page bitmaps, pan and pinch-to-zoom to 4x, page navigation, OCR text sheet.
- **Signature** (`ui/signature/`): bezier stroke canvas rasterised to a transparent PNG, composited onto pages at PDF export time.
- **PDF Engine** (`util/PdfGenerator.kt` + `ui/pdf/`): 8 paper presets, 4 margin presets, page numbering, watermark, real page bitmaps, `FileProvider` share sheet, runs off the main thread.
- **Recycle Bin** (`ui/trash/`): soft delete, restore, permanent purge (deletes page files too).
- **Persistence** (`data/LibraryStore.kt`): the library survives process death.
- **Settings** (`ui/settings/SettingsScreen.kt` + `data/SettingsStore.kt`): theme
  mode + 6 accented palettes, in-app-camera toggle (off routes scanning through
  the system photo picker), shutter sound, image quality (capture dimension +
  JPEG quality), auto-save to `Pictures/DocScanner`, and PDF defaults (file-name
  prefix, page size, DPI, orientation, image scale) plus default email
  recipient/subject. Persisted in `settings.json`; UDF via
  `ScannerRepository.settings` + `updateSettings`. Rows whose feature does not
  exist (OCR language, camera engine, cloud sync) are **not** shown.

### 🚀 Roadmap: Next Features to Build (Priority Order)

Pick exclusively from this list. Each one assumes the image pipeline above works.

#### Priority 0 (already scoped): Settings rows that were deliberately omitted
* **OCR language** — add to the settings screen in the same commit as
  `util/OcrManager.kt`, not before: the row would have nothing to read it.
* **Camera engine** — CameraX is the only engine wired; a picker with one option
  is noise.
* **Sync to Cloud / Sync Settings** — needs an account + transport; parked.

#### Priority 1: Google ML Kit On-Device OCR (`util/OcrManager.kt`)
* **Task**: Integrate `com.google.mlkit:text-recognition:16.0.1`.
* **Workflow**:
  1. `InputImage.fromBitmap(bitmap, 0)` on the decoded `imageUri` bitmap.
  2. Write `visionText.text` into `ScannedPage.ocrText` via `repository.updatePage`.
  3. Surface it in `ui/viewer/PageViewerScreen.kt`'s existing OCR sheet with a "Copy to clipboard" button.
  * Note: the sheet already renders `ocrText`; today it is only ever populated by the user pasting text.

#### Priority 2: White-out / Background Eraser (`ui/editor/CleanupBgCanvas.kt`)
* **Task**: Eraser brush for smudges, finger shadows, punch holes.
* **Workflow**: drag-tracked Compose `Canvas` over the real page bitmap → white (or sampled) circles at 10–50 dp radius → undo/redo stack → bake into the `_edited.jpg` like the filter pass.

#### Priority 3: Configurable Naming Templates (`util/NamingTemplateEngine.kt`)
* **Task**: Replace "New Scan" / "Scanned Doc" with a template engine.
* **Workflow**: support `{PREFIX}`, `{DATE:yyyy-MM-dd}`, `{TIME:HHmmss}`, `{COUNT}`. Defaults today are hardcoded in `ScannerCameraScreen` — that is the only place to change.

#### Priority 4: Local ZIP Backup & Restore (`util/BackupRestoreManager.kt`)
* **Task**: Export/import whole collections as `.zip`.
* **Workflow**: `manifest.json` (serialise `LibrarySnapshot` from `LibraryStore`) + `pages/*.jpg` + `signatures/*.png`. Restore must re-point paths at the new install's `filesDir`.

---

## 🔧 4b. If the build breaks after the 2026-10-25 refactor

That change (Arena.ai, `docs/AGENTS_CHANGELOG.md`) was **not compile-verified** —
the Arena sandbox has no JDK or Android SDK. Diagnose in this order:

1. **Dependency resolution fails** → the pinned versions were guesses. Bump
   `cameraX` / `kotlinxSerializationJson` in `gradle/libs.versions.toml` to the
   latest versions your SDK can resolve. Nothing else needs to change.
2. **`LibrarySnapshot.serializer()` unresolved** → the serialization plugin is not
   applied. Check `alias(libs.plugins.kotlin.serialization)` in
   `app/build.gradle.kts` (it must also be declared `apply false` in the root).
3. **`LocalLifecycleOwner` unresolved** → it must come from
   `androidx.lifecycle.compose`, not the deprecated
   `androidx.compose.ui.platform` path.
4. **CameraX symbol errors** (`surfaceProvider`, `flashMode`, `hasFlashUnit`) →
   version drift; adjust to the API in the version you resolved rather than
   downgrading the whole pipeline.
5. **Material icon unresolved** → `material-icons-extended` must stay a
   dependency; the new screens use `FilterAlt`, `ContentCopy`, `BurstMode`, etc.
6. **Anything else** → the compiler is authoritative. Fix forward; do not revert
   the image pipeline to placeholder rendering. If a revert seems necessary, log
   why in `docs/AGENTS_CHANGELOG.md` first so the other agent doesn't re-add it.

Runtime smoke test after it builds, in this order (each step depends on the last):

1. Grant camera permission → preview appears (not a black box).
2. Capture → the page appears in the dashboard with a **real thumbnail**.
3. Kill from recents, reopen → the document is **still there**.
4. Press **Done** → the review screen opens with borders auto-detected; *Save & Next*
   walks every captured page, *Finish* lands on the document.
5. Open the page → crop/filter in the editor → Save → thumbnail updates.
6. Long-press a card menu → *Move to folder…* → the folder chip filters it in.
7. Export PDF → share sheet opens with a real PDF (not a Toast).

---

## 🧪 5. Validation Checklist for AI Agents

Before declaring any feature complete:

1. **Compilation**: run `./gradlew assembleDebug` and verify **BUILD SUCCESSFUL**.
   * Arena.ai cannot do this — say so explicitly rather than claiming a build passed.
2. **Unit tests**: run `./gradlew testDebugUnitTest`. `DocumentNamingTest` guards a
   crash that shipped once: an unquoted literal inside a `SimpleDateFormat`
   pattern (`"Scan yyyy-MM-dd"`) throws `Illegal pattern character 'c'`.
3. **Real pixels**: confirm the feature renders a bitmap that originated from a
   file in `filesDir/pages/`. Opening the screen must show the captured document,
   not an icon or a drawn placeholder.
4. **Restart test**: kill the app from recents, reopen, and confirm the document
   is still there (`LibraryStore` round-trip).
5. **File Placement**:
   - Screens live in `ui/<feature>/<Feature>Screen.kt`.
   - Shared composables live in `ui/common/`.
   - Domain models live in `model/Models.kt`.
   - Repository operations live in `data/ScannerRepository.kt`.
   - Pure processing utilities live in `util/`.
6. **Coordination**: claimed the files in `docs/AGENT_COORDINATION.md` §4 and
   appended to `docs/AGENTS_CHANGELOG.md` if you deleted or renamed anything.
7. **Architecture Spec**: refer to [`doc_scanner_architecture_spec.html`](doc_scanner_architecture_spec.html) for
   the reverse-engineered feature catalog, paper dimensions, and activity mappings —
   but treat this file as authoritative where they disagree.
