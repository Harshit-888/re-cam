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
     modified/created/A–Z.
   - Cover thumbnails are the **real first page**, not an icon.

2. **Camera capture** (`ui/camera/`)
   - Live **CameraX** preview, runtime permission flow, back-camera still capture.
   - Flash modes Auto / On / Off, wired to both `ImageCapture` flash and the torch.
   - Batch mode with a live page counter, or single mode that auto-commits.
   - Gallery import through the system photo picker (no storage permission needed).
   - EXIF orientation is baked into pixels, so portrait shots aren't sideways.

3. **Perspective correction & filters** (`ui/editor/`)
   - Four draggable corner handles over the **real photograph**.
   - Genuine projective warp (`Matrix.setPolyToPoly`) — the crop actually moves
     pixels now.
   - 5 `ColorMatrix` enhancements previewed live over the photo: *Original*,
     *Magic Color*, *Clear B&W*, *Grayscale*, *Lighten*.
   - 90° rotation, "Full Page" reset, and a brightness-based "Auto Detect".
   - Non-destructive: the pristine capture is never overwritten, so repeated edits
     don't accumulate JPEG loss.

4. **Document detail** (`ui/detail/`)
   - Multi-page grid with real thumbnails and filter/watermark badges.
   - Page rotation, deletion (cleans up the JPEGs), reordering, and appending
     more scans.
   - Rename, watermark (applied at PDF time), signature, PDF export.

5. **Fullscreen viewer** (`ui/viewer/`)
   - Real page bitmap, pan + pinch-zoom to 4×, page navigation, rotation.
   - Recognised-text sheet with copy-to-clipboard (populated by the OCR roadmap
     item).

6. **Digital signature** (`ui/signature/`)
   - Bezier-smoothed stroke canvas, thickness slider, three ink colours.
   - Rasterised to a transparent PNG and composited onto the page at the
     bottom-right, so it survives into thumbnails, the viewer and the PDF.

7. **PDF export** (`util/PdfGenerator.kt`, `ui/pdf/`)
   - Real page bitmaps, 72 DPI point geometry, aspect-preserving fit.
   - **8 paper presets**, 4 margin presets, optional page numbering, watermark.
   - Runs on `Dispatchers.IO` (no ANR on long documents).
   - Shares through a `FileProvider` + the system share sheet.

8. **Recycle bin** (`ui/trash/`) — soft delete, restore, permanent purge
   (including deleting page files from disk).

9. **Persistence** (`data/LibraryStore.kt`) — the whole library is a
   `library.json` snapshot written atomically, so it survives process death.

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
| Image pipeline | `Matrix` / `Canvas` / `ColorMatrix`, sampled decode + `LruCache` |
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
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

> The Arena.ai agent sandbox has no JDK/Android SDK, so builds produced there are
> **unverified**. If a build fails, the compiler is right — see
> `docs/AGENTS_CHANGELOG.md` for the dependency versions that may need bumping.

---

## 🗺️ Roadmap

Both are tracked in `AGENTS.md` §4 with implementation notes:
1. ML Kit on-device OCR (`util/OcrManager.kt`) — fills the existing OCR sheet.
2. White-out eraser (`ui/editor/CleanupBgCanvas.kt`).
3. Naming templates (`util/NamingTemplateEngine.kt`).
4. ZIP backup/restore (`util/BackupRestoreManager.kt`).
