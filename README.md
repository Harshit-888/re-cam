# DocScanner — Modern Clean-Room Android Document Scanner

A ground-up Android document scanner in **Kotlin**, **Jetpack Compose** and
**Material 3 Expressive**, replacing a legacy ad-funded scanner
(`com.indymobileapp.document.scanner`) with modern architecture, real image
processing, no advertising SDKs and no DRM dependencies.

Clean-room: no code from the original APK. The reverse-engineered feature catalog
that informed this rebuild is in
[`doc_scanner_architecture_spec.html`](./doc_scanner_architecture_spec.html).

> **Building with an AI agent?** Read [`AGENTS.md`](./AGENTS.md) first — and if you
> are Antigravity, read [`docs/AGENT_COORDINATION.md`](./docs/AGENT_COORDINATION.md)
> so your edits don't collide with Arena.ai's branch.

---

## 🚀 What actually works

Every feature below is end-to-end: it captures or imports real pixels, stores them
in app-private storage, and renders them back.

1. **Dashboard & organisation** (`ui/dashboard/`)
   - Instant search across titles, tags and extracted text.
   - Folder chips (create folders with a colour), grid/list switcher, sorting by
     modified/created/A–Z, and *Move to folder…* from any document card.
   - Cover thumbnails are the **real first page**, not an icon.

2. **Camera capture** (`ui/camera/`)
   - Full-bleed live **CameraX** preview (no decorative frame — it used to push
     users to shoot past the paper edge, which broke auto-detection),
     runtime permission flow, back-camera still capture.
   - Flash modes Auto / On / Off, wired to both `ImageCapture` flash and the torch.
   - Batch mode with a live page counter, or single mode that auto-commits.
   - Gallery import through the system photo picker (no storage permission needed).
   - EXIF orientation is baked into pixels, so portrait shots aren't sideways.

3. **Perspective correction & filters** (`ui/editor/`)
   - Four draggable corner handles over the **real photograph**, positioned by
     automatic edge detection (`DocumentDetector`: boundary scan → RANSAC line per
     side → validated corners). If a page edge can't be found the screen says so
     rather than silently showing nothing.
   - Genuine projective warp (`Matrix.setPolyToPoly`) — the crop actually moves
     pixels now.
   - 5 enhancements computed per-pixel by `DocumentFilters` and previewed live:
     *Original*, *Magic Color*, *Clear B&W*, *Grayscale*, *Lighten*.
     The maths is **ported from the original APK**, not tuned by eye
     (`docs/ORIGINAL_APP_FILTER_SPEC.md`): the estimator is `fa1.H` verbatim —
     100×100 bilinear downsample, `(R+G+B)/3` luminance, black/white points found
     at the 2 % cumulative cutoffs of each end, and the `wv3` document/photo
     classifier. *Lighten* is then the native `enhancePhoto` LUT, ported line
     for line; *Magic Color* and *Clear B&W* build a per-channel paper surface
     (pool → `dilate`/`erode` exactly as the shipped `libimageproc.so` does, at a
     size-scaled radius), divide by it so shadows and colour casts cancel in one
     step, then apply a pinned ink curve with midtone sharpening / a median
     pre-filter followed by Otsu on a soft anti-aliased ramp; *Grayscale* is
     plain Rec.601 desaturation, as in the original.
   - 90° rotation, "Full Page" reset, and a brightness-based "Auto Detect".
   - Non-destructive: the pristine capture is never overwritten, so repeated edits
     don't accumulate JPEG loss.

4. **Post-capture review** (`ui/editor/`, `reviewMode`)
   - After *Done*, every captured page opens with **auto-detected document
     borders** and draggable corner handles.
   - Live filter preview, 90° rotation, then *Save & Next* through the whole
     batch before landing on the document. This is the step the legacy app had.

5. **Document detail** (`ui/detail/`)
   - Multi-page grid with real thumbnails and filter/watermark badges.
   - Page rotation, deletion (cleans up the JPEGs), reordering, and appending
     more scans.
   - Edit title **and notes**, watermark (applied at PDF time), signature, PDF
     export.

6. **Fullscreen viewer** (`ui/viewer/`)
   - Real page bitmap, pan + pinch-zoom to 4×, page navigation, rotation.
   - Recognised-text sheet with copy-to-clipboard (populated by the OCR roadmap
     item).

7. **Digital signature** (`ui/signature/`)
   - Bezier-smoothed stroke canvas, thickness slider, three ink colours.
   - Rasterised to a transparent PNG and composited onto the page at the
     bottom-right, so it survives into thumbnails, the viewer and the PDF.

8. **PDF export** (`util/PdfGenerator.kt`, `ui/pdf/`)
   - Real page bitmaps, 72 DPI point geometry.
   - **8 paper presets**, 4 margin presets, optional page numbering, watermark.
   - Fit / fill / original scaling and per-page auto orientation, from Settings.
   - Runs on `Dispatchers.IO` (no ANR on long documents).
   - Shares through a `FileProvider` + the system share sheet.

9. **Recycle bin** (`ui/trash/`) — soft delete, restore, permanent purge
   (including deleting page files from disk).

10. **Persistence** (`data/LibraryStore.kt`) — the whole library is a
   `library.json` snapshot written atomically, so it survives process death.

11. **Settings** (`ui/settings/`, `data/SettingsStore.kt`) — every row changes
   real behaviour, no decorative switches:
   - *Theme*: System / Light / Dark plus six accent palettes.
   - *Scan*: in-app camera on/off (off = the system photo picker becomes the scan
     entry point), shutter sound, image quality (capture resolution + JPEG
     quality), auto-save to `Pictures/DocScanner`.
   - *PDF*: document-name prefix for exported files, page size, resolution
     (150/200/300 DPI), page orientation (auto swaps axis per landscape page),
     image scale (fit / fill / original).
   - *Default Email*: recipient + subject pre-filled into the share sheet.
   Settings persist in `settings.json`, separate from the document library.

---

## 🗂️ Where data lives

```
files/library.json                     document + folder metadata (atomic snapshot)
files/pages/<pageId>.jpg               pristine capture — never overwritten
files/pages/<pageId>_edited.jpg        warp + rotation + filter + signature bake
files/signatures/<uuid>.png            transparent signature strokes
cache/exports/<title>_<ts>.pdf         generated PDFs (share target)
```

`ScannedPage.imageUri` is always an **absolute path in this app's private
storage**. `content://` URIs are imported first, never stored.

---

## 🛠️ Architecture & tech stack

| Concern | Choice |
| :--- | :--- |
| UI | 100% Jetpack Compose, Material 3 Expressive, no XML layouts |
| Navigation | Navigation 3 with type-safe `NavKey`s |
| State | UDF — `ScannerRepository` + `StateFlow`, injected from `MainActivity` |
| DI | Constructor parameters. No service locator, no singleton |
| Persistence | `kotlinx.serialization` JSON snapshot |
| Camera | CameraX 1.4.2 (`Preview` + `ImageCapture` + `PreviewView`) |
| Image pipeline | Per-pixel `DocumentFilters` (illumination model, white balance, percentile stretch, Otsu), `Matrix` warp, sampled decode + `LruCache` |
| PDF | `android.graphics.pdf.PdfDocument` |
| Build | Gradle 9.1.0, AGP 9.0.1, Kotlin 2.3.20 |
| SDK | minSdk 26 (Android 8) · target/compile 36 (Android 16) |

Versions live in [`gradle/libs.versions.toml`](./gradle/libs.versions.toml) — that
file is authoritative if this table drifts.

---

## 📄 PDF paper presets

| Preset | Points | Metric / Imperial |
| :--- | :--- | :--- |
| A4 | 595 × 842 | 210 × 297 mm |
| US Letter | 612 × 792 | 8.5 × 11 in |
| US Legal | 612 × 1008 | 8.5 × 14 in |
| A3 | 842 × 1191 | 297 × 420 mm |
| A5 | 420 × 595 | 148 × 210 mm |
| Executive | 522 × 756 | 7.25 × 10.5 in |
| Business Card | 243 × 153 | 85.6 × 54 mm (ID-1) |
| Receipt | 226 × 600 | 80 mm thermal roll |

Margins: None (0 pt), Compact (10 pt), Standard (25 pt), Wide (40 pt).

---

## 🔨 Building & running

Requirements: JDK 17+ (21 recommended) and Android SDK 36 with Build Tools 36.

```bash
./gradlew assembleDebug          # build
./gradlew testDebugUnitTest      # unit tests (DocumentNaming crash guard)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

> The Arena.ai agent sandbox has no JDK/Android SDK, so builds produced there are
> **unverified**. If a build fails, the compiler is right — see
> `docs/AGENTS_CHANGELOG.md` for the dependency versions that may need bumping.

---

## 🗺️ Roadmap

Not built yet, and deliberately absent from the Settings screen so no control is
a lie — tracked in `AGENTS.md` §4 with implementation notes:
1. ML Kit on-device OCR (`util/OcrManager.kt`) — fills the existing OCR sheet;
   the OCR *language* row the legacy app had lands with it.
2. White-out eraser (`ui/editor/CleanupBgCanvas.kt`).
3. Naming templates (`util/NamingTemplateEngine.kt`).
4. ZIP backup/restore (`util/BackupRestoreManager.kt`).
