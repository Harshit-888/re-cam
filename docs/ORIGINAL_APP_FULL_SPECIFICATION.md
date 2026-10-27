# Document Scanner — Complete Reverse Engineering Master Specification & Clean-Room Rebuild Blueprint

> **Target Application:** `com.indymobileapp.document.scanner` (Indy Mobile App — Document Scanner / PDF Creator)  
> **Source Artifacts Analyzed:** `base.apk`, `split_config.arm64_v8a.apk`, `split_config.xxhdpi.apk` (v10.2.18, VersionCode 900010218, Compile/Target SDK 36, Min SDK 29)  
> **Analysis Toolchain:** Reverse Engineer Anything (REA), JADX v1.5.6 (multidex `classes.dex` through `classes4.dex`), Ghidra 12.1.4, Apktool v3.0.3, SQLite 3.53 inspector, TFLite Model Parser.  
> **Target Rebuild Project:** `com.example.docscanner` (`doc-scanner/`) in modern Kotlin, Jetpack Compose, Material 3 Expressive, Navigation 3, and Rust acceleration.

---

## 1. Executive Architecture & Component Inventory

The original application is a 4-DEX split APK containing 34 primary user-facing Activities, 2 Content Providers, 1 Foreground Service, and 11 native shared libraries. All DRM, ad networks (AdMob, Facebook Audience Network, InMobi, Pangle, Mintegral), tracking, and license checks are stripped in this clean-room blueprint.

### 1.1 Complete Manifest Component Mapping

| Original Target Activity | AndroidManifest Declaration | Rebuilt Architecture Seam | Status / Target Screen |
| :--- | :--- | :--- | :--- |
| **Splash Screen** | `com.indymobile.app.activity.SplashScreenActivity` | `MainActivity.kt` + Splash API | Implemented |
| **Main Dashboard** | `com.indymobile.app.activity.MainActivity` (+ Tablet) | `ui/dashboard/DashboardScreen.kt` | Implemented (grid/list, folders, search) |
| **Document Detail** | `com.indymobile.app.activity.DocumentActivity` (+ Tablet) | `ui/detail/DocumentDetailScreen.kt` | Implemented (page grid, reorder, delete) |
| **Camera Viewfinder** | `com.indymobile.app.activity.camera.camerax.CameraXActivity` | `ui/camera/ScannerCameraScreen.kt` | Implemented (modes, batch counter, flash) |
| **Legacy Camera** | `com.indymobile.app.activity.camera.camera1.CameraActivity` | N/A (CameraX unified) | Obsoleted by CameraX |
| **Image Editor (Crop & Filters)** | `com.indymobile.app.activity.ImageProcessingActivity` (+ Tablet) | `ui/editor/CropAndFilterScreen.kt` | Implemented (8 handles, loupe, filters) |
| **Full Page Viewer** | `com.indymobile.app.activity.ViewPagerActivity` (+ Tablet) | `ui/viewer/PageViewerScreen.kt` | Implemented (pinch zoom, swipe) |
| **PDF Settings** | `com.indymobile.app.activity.PdfSettingsActivity` (+ Tablet) | `ui/pdf/PdfExportSheet.kt` | Implemented (50 paper sizes, margins) |
| **PDF Preview** | `com.indymobile.app.activity.PdfPreviewActivity` (+ Tablet) | `ui/viewer/PageViewerScreen.kt` / Pdfium | Implemented |
| **Print Dialog** | `com.indymobile.app.activity.PrintDialogActivity` (+ Tablet) | Android `PrintManager` | Implemented |
| **Share Sheet** | `com.indymobile.app.activity.ShareActivity` (+ Tablet) | `ui/pdf/PdfExportSheet.kt` + ShareSheet | Implemented |
| **Settings** | `com.indymobile.app.activity.SettingsActivity` (+ Tablet) | `ui/settings/SettingsScreen.kt` | Implemented |
| **Recycle Bin (Trash)** | `com.indymobile.app.activity.trash.MainTrashActivity` (+ Tablet) | `ui/trash/TrashScreen.kt` | Implemented (restore, empty, auto-purge) |
| **Document Naming Template** | `com.indymobile.app.activity.DocumentNameSettingActivity` (+ Tablet) | `util/DocumentNaming.kt` / Settings | Implemented (tokens, live preview) |
| **Signature Pad** | `com.indymobile.app.activity.signaturepad.SignaturePadActivity` (+ Tablet) | `ui/signature/SignaturePadDialog.kt` | Implemented (bezier curve, ink colors) |
| **Markup & Annotation** | `com.indymobile.app.activity.editor.markup.MarkupActivity` (+ Tablet) | `ui/editor/MarkupCanvas.kt` | Blueprint ready (pen, highlight, text) |
| **Background Eraser** | `com.indymobile.app.activity.editor.cleanupbg.CleanupBgActivity` (+ Tablet) | `ui/editor/CleanupBgCanvas.kt` | Blueprint ready (whiteout brush, undo) |
| **Rounded Corners** | `com.indymobile.app.activity.editor.markup.RoundedCornersActivity` (+ Tablet) | `ui/editor/CropAndFilterScreen.kt` | Additive Modifier |
| **Multi-Image Album Picker** | `com.indymobile.app.imagepicker.ui.PickerActivity` (+ Tablet) | Android PhotoPicker (`PickVisualMedia`) | Built-in |
| **Backup & Restore** | `com.indymobile.app.activity.MyBackupActivity` (+ Tablet) | `util/BackupRestoreManager.kt` | Blueprint ready (ZIP package) |
| **Cloud Sync** | `com.indymobile.app.activity.sync.SyncAppDataActivity` / `SyncSettingsActivity` | `util/CloudSyncEngine.kt` | Planned |
| **Font Selection** | `com.indymobile.app.activity.font.FontSelectionActivity` / `SystemFontSelectionActivity` | System fonts + Google Fonts | In markup |
| **External Intent Handler** | `com.indymobile.app.activity.ExternalIntentActivity` | `MainActivity.kt` intent filters | Handles `SEND`, `SEND_MULTIPLE`, `VIEW` |
| **External Doc Picker** | `com.indymobile.app.activity.ExternalDocumentPickerActivity` | `PSDocumentProvider` (`GET_CONTENT`) | Implemented |

---

## 2. Complete Native Shared Libraries & Binary Assets

The original app relies on 11 `.so` binaries in `split_config.arm64_v8a.apk`:

1. `libimageproc.so`: Core low-level image processing routines (Sauvola binarization, division normalization, unsharp masking, contrast stretch, saturation gain). Reimplemented in Kotlin (`util/DocumentFilters.kt`) and accelerated with Rust kernels (`app/src/main/rust/src/kernels.rs`).
2. `libopencv_java4.so` & `PSOpenCV.java`: OpenCV 4 Canny edge detection, contour hierarchy, and 4-point homography perspective warp.
3. `libharu.so` (`com.draekko.libharu`): Low-level LibHaru PDF document compiler, drawing vector streams, embedding JPEG/PNG streams, and setting PDF encryption.
4. `libjniPdfium.so` & `libmodpdfium.so`: Google PDFium rendering engine for fast page rasterization, PDF viewing, and text extraction.
5. `libtensorflowlite_jni.so` & `libtensorflowlite_gpu_jni.so`: TensorFlow Lite runtime executing on-device neural edge detection (`border_detect_224.tflite`).
6. `libmlkit_google_ocr_pipeline.so`: Google ML Kit on-device optical character recognition engine.
7. `libimage_processing_util_jni.so`: Android CameraX native YUV420_888 to RGBA/NV21 conversion.
8. `libsurface_util_jni.so`: Hardware buffer allocation for zero-copy camera frame processing.

### Critical Packaged Binary Assets (`assets/`)
- `assets/border_detect_224.tflite` (4,783,628 bytes, SHA-256 `578f53b6c90ee9bc14b6ba350c3d261ffd6001d3e9b716a7ad571b6a4bdd1972`): MobileNetV2-based 4-corner document boundary detector.
- `assets/databases/app.db` (28,672 bytes): SQLite master database schema with 50 pre-populated international paper sizes.
- `assets/mlkit-google-ocr-models/`: Bundled offline text recognition neural networks for Latin, Devanagari, and layout analysis.
- `assets/watermark/watermark.png`: Default sample watermark asset.

---

## 3. SQLite Database Architecture & Schema Specification

The master database `app.db` is pre-populated in `assets/databases/` and upgraded via scripts `1-2`, `2-3`, `3-4`, and `4-5`.

```
                  ┌──────────────────────┐
                  │    document_group    │
                  ├──────────────────────┤
                  │ i_document_group_id  │◀───┐
                  │ c_document_group_name│    │
                  │ i_order              │    │
                  └──────────────────────┘    │ (1:N)
                                              │
                  ┌──────────────────────┐    │
                  │       document       │────┘
                  ├──────────────────────┤
                  │ i_document_id (PK)   │◀───┐
                  │ c_document_title     │    │
                  │ i_date_create        │    │
                  │ i_date_modify        │    │
                  │ i_date_trash         │    │ (1:N)
                  │ i_is_directory (0/1) │    │
                  │ i_directory_id (FK)  │    │
                  │ i_paper_size_id (FK) │    │
                  └──────────────────────┘    │
                             │                │
                             ▼ (1:N)          │
                  ┌──────────────────────┐    │
                  │         page         │────┘
                  ├──────────────────────┤
                  │ i_page_id (PK)       │
                  │ i_document_id (FK)   │
                  │ c_storage_path       │
                  │ i_page_index         │
                  │ c_ocr (Extracted TXT)│
                  │ c_note (User Note)   │
                  │ i_date_trash         │
                  │ i_jpg_quality (80)   │
                  │ i_result_image_width │
                  │ i_result_image_height│
                  └──────────────────────┘
```

### 3.1 `document` Table (19 Columns)
```sql
CREATE TABLE "document" (
    `i_document_id`         INTEGER PRIMARY KEY AUTOINCREMENT,
    `c_document_title`       TEXT NOT NULL,
    `i_date_create`         NUMERIC NOT NULL,
    `i_date_modify`         NUMERIC NOT NULL,
    `c_search_keyword`      TEXT,
    `i_paper_size_id`       NUMERIC DEFAULT 5, /* A4 = 5 */
    `i_document_group_id`   NUMERIC DEFAULT 0,
    `i_page_content_mode`   NUMERIC DEFAULT 0, /* 0=Fit, 1=Fill, 2=Stretch */
    `i_page_orientation`    NUMERIC DEFAULT 0, /* 0=Portrait, 1=Landscape */
    `i_page_margin_top`     NUMERIC DEFAULT 0,
    `i_page_margin_left`    NUMERIC DEFAULT 0,
    `i_page_margin_bottom`  NUMERIC DEFAULT 0,
    `i_page_margin_right`   NUMERIC DEFAULT 0,
    `i_is_directory`        NUMERIC DEFAULT 0, /* 1 = Folder, 0 = Document */
    `i_directory_id`        NUMERIC DEFAULT 0, /* Parent folder ID for nested trees */
    `i_date_trash`          NUMERIC,           /* NULL = active; Timestamp = in Trash */
    `i_opt`                 NUMERIC DEFAULT 0,
    `c_opt`                 TEXT,              /* JSON options / tag metadata */
    `i_status`              NUMERIC DEFAULT 0
);

CREATE INDEX `document_idx_1` ON `document` (`i_document_id` ASC);
CREATE INDEX `document_idx_2` ON `document` (`c_search_keyword` ASC);
CREATE INDEX `document_idx_3` ON `document` (`i_document_group_id` ASC);
CREATE INDEX `document_idx_4` ON `document` (`c_document_title` ASC);
CREATE INDEX `document_idx_5` ON `document` (`i_date_modify` ASC);
CREATE INDEX `document_idx_6` ON `document` (`i_date_create` ASC);
```

### 3.2 `page` Table (23 Columns)
```sql
CREATE TABLE "page" (
    `i_page_id`             INTEGER PRIMARY KEY AUTOINCREMENT,
    `c_storage_path`        TEXT NOT NULL,     /* Relative/absolute image filename */
    `i_date_create`         NUMERIC NOT NULL,
    `i_date_modify`         NUMERIC NOT NULL,
    `c_search_keyword`      TEXT,
    `i_page_index`          NUMERIC NOT NULL,  /* 0-based page sequence order */
    `b_is_process_completed` NUMERIC DEFAULT 1,
    `i_document_id`         NUMERIC NOT NULL,  /* Foreign key -> document.i_document_id */
    `c_original_file_name`  TEXT,              /* Raw uncropped camera capture */
    `i_result_file_size`    NUMERIC,           /* File size in bytes */
    `i_result_image_width`  INTEGER,           /* Pixel width */
    `i_result_image_height` INTEGER,           /* Pixel height */
    `c_ocr`                 TEXT,              /* Full extracted OCR text */
    `c_note`                TEXT,              /* User-annotated notes */
    `i_date_ocr`            NUMERIC,           /* OCR timestamp */
    `i_date_note`           NUMERIC,           /* Note edit timestamp */
    `c_title`               TEXT,
    `i_jpg_quality`         NUMERIC DEFAULT 80,
    `i_file_type`           INTEGER DEFAULT 0, /* 0=JPG, 1=PNG */
    `i_date_trash`          NUMERIC,           /* Soft delete timestamp */
    `i_opt`                 NUMERIC DEFAULT 0,
    `c_opt`                 TEXT,              /* JSON crop & filter state */
    `i_status`              NUMERIC DEFAULT 0
);
```

### 3.3 `paper_size` Table & Standard Dimensions
The original app stores 50 pre-calibrated international paper sizes with width and height in points (`1 pt = 1/72 inch`):

| ID | Name | Width (pt) | Height (pt) | Unit | Metric / Imperial Dimension |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **4** | A3 | 842 pt | 1190 pt | mm | 297 × 420 mm |
| **5** | **A4 (Default)** | **595 pt** | **842 pt** | **mm** | **210 × 297 mm** |
| **6** | A5 | 420 pt | 595 pt | mm | 148 × 210 mm |
| **7** | A6 | 298 pt | 420 pt | mm | 105 × 148 mm |
| **14** | B4 | 709 pt | 1001 pt | mm | 250 × 353 mm |
| **15** | B5 | 499 pt | 709 pt | mm | 176 × 250 mm |
| **35** | **US Letter** | **612 pt** | **792 pt** | **in** | **8.5 × 11.0 in** |
| **36** | **US Legal** | **612 pt** | **1008 pt** | **in** | **8.5 × 14.0 in** |
| **37** | US Ledger | 792 pt | 1224 pt | in | 11.0 × 17.0 in |
| **38** | Tabloid | 1224 pt | 792 pt | in | 17.0 × 11.0 in |
| **39** | Executive | 522 pt | 756 pt | in | 7.25 × 10.5 in |
| **43** | Foolscap | 954 pt | 1188 pt | in | 13.25 × 16.5 in |
| **Custom**| Business Card | 241 pt | 153 pt | mm | 85 × 54 mm |

---

## 4. Reverse-Engineered Core Algorithms

### 4.1 Auto Border Detection (`border_detect_224.tflite` + `ht3.java` / `qt3.java`)
1. **Input Preprocessing**: Scale source image to `224 × 224 × 3` RGB Float32 tensor with pixel values in range `0.0f .. 255.0f`.
2. **Inference Execution**:
   - Primary output: `StatefulPartitionedCall:1` shaped `[1, 1, 4, 2]` Float32 containing normalized `[y, x]` coordinates for 4 corners in order `[TopLeft, TopRight, BottomRight, BottomLeft]`.
   - Secondary output: `StatefulPartitionedCall:0` shaped `[1, 1, 4]` Float32 containing confidence scores `[c_tl, c_tr, c_br, c_bl]`.
3. **Acceptance Threshold**:
   $$\text{MeanConfidence} = \frac{1}{4} \sum_{i=0}^3 c_i \ge 0.50f$$
4. **Quad Sanity Validation (`QuadSanity.kt`)**:
   - Area must satisfy: $0.06 \le \text{NormalizedArea} \le 1.05$.
   - Side ratio must satisfy: $\min(\text{sides}) / \max(\text{sides}) \ge 0.18$.
   - Coordinates must be non-NaN, bounded within `[0.0 .. 1.0]`, and strictly convex (no self-intersecting quads).
5. **Fallback Cascade**:
   $$\text{ML Confident} \longrightarrow \text{Kotlin/Canny Geometry} \longrightarrow \text{Plausible Unsure ML} \longrightarrow \text{Full Frame}$$

### 4.2 Manual Crop View UX (`ax0.java` & `p60.java`)
- **8 Interactive Handles**:
  - 4 Corner handles (`0..3`): Circular handles with $14\text{ dp}$ outer ring and $9\text{ dp}$ solid white core. Dragging moves the single vertex.
  - 4 Edge Midpoint handles (`10..13`): Rounded rectangular handles ($14\text{ dp}$). Dragging translates both adjacent vertices in parallel.
- **Constraints & Edge Snapping**:
  - Boundary clamped to `[0.0 .. 1.0]`.
  - Edge snapping within $\text{SNAP} = 0.015$ ($1.5\%$ of canvas).
  - Minimum corner separation $\text{MIN\_GAP} = 0.05$ ($5\%$) to prevent polygon folding.
- **1.75x Magnifying Loupe**:
  - $124\text{ dp}$ circular loupe with fine crosshair reticles.
  - Automatically positions in the top corner opposite the user's touch finger (Top-Right when touching left corners, Top-Left when touching right corners) so the magnified area is never obscured.

### 4.3 Document Filters & Native Image Processing (`libimageproc.so`)
The original app implements 5 enhancement filters via `cv3` and `yu3` enums:

1. **Magic Color (`kPSEnhanceTypeDocumentLevel1` + `kPSColorTypeColor`)**:
   - Fast $16 \times 16$ mean-pooled downsample to compute the local paper background luminance plane.
   - Contrast stretching & shadow lifting:
     $$\text{LumaGain} = \frac{255.0}{1.0 + \exp(-\text{contrast} \cdot (\text{pixel} - \text{threshold}))}$$
   - Soft-knee saturation boost (1.35x) to vividify colored text, stamps, and highlighters without chromatic aberration.
2. **Clear B&W (`kPSEnhanceTypeDocumentLevel2` + `kPSColorTypeGrayScale`)**:
   - Clamped Sauvola local adaptive thresholding over sliding window ($W = 31\text{ px}$, $k = 0.20$):
     $$T(x,y) = m(x,y) \cdot \left(1.0 + k \cdot \left(\frac{s(x,y)}{128.0} - 1.0\right)\right)$$
   - Sub-pixel $3 \times 3$ coverage anti-aliasing to eliminate stair-stepped pixelation on fine text strokes.
3. **Lighten (`kPSEnhanceTypeLighten`)**:
   - Piecewise cubic look-up table (LUT) lifting dark shadows by $+35$ while anchoring specular white at $255$.
4. **Grayscale**:
   - ITU-R BT.601 perceptual luma conversion:
     $$Y = 0.299\,R + 0.587\,G + 0.114\,B$$
5. **Original**:
   - Unfiltered camera capture with neutral tone curve.

---

## 5. Camera & Viewfinder Architecture (`CameraXActivity.java`)

### 5.1 Real-Time Viewfinder Tracking (`PaperBorderView.java`)
- **State Machine**:
  - `p04.CLEAR`: Inactive overlay.
  - `p04.LOOKUP`: Real-time animated corner quad overlay with $2\text{ dp}$ stroke and semi-transparent fill ($\alpha = 96$).
  - `p04.PAUSE`: Quad locked in green when document bounds are stable.
- **Auto-Capture Shutter**:
  - Inter-frame stability computed via Intersection-over-Union:
    $$\text{IOU}(Q_t, Q_{t-1}) = \frac{\text{Area}(Q_t \cap Q_{t-1})}{\text{Area}(Q_t \cup Q_{t-1})} \ge 0.95$$
  - When $\text{IOU} \ge 0.95$ for 3 consecutive frames, auto-shutter triggers automatically.

### 5.2 Capture Modes
1. **Document Scan**: Standard mode with single-shot or continuous multi-page batch capture.
2. **ID Card Mode**: Two-step sequential capture (Front Side, then Back Side) automatically arranged side-by-side or stacked on an A4 sheet.
3. **Book Mode**: Two-page book spread scan automatically split into separate left and right pages with center spine crease masking.
4. **Passport Mode**: Standardized 2:3 crop template for identity passports.
5. **Photo Mode**: Full-sensor capture without perspective warping or document binarization.
6. **QR & Barcode Scanner**: ZXing / ML Kit barcode scanner extracting URLs, vCards, WiFi configurations, and plain text.

---

## 6. Page Editor Tools Specification

### 6.1 Background Eraser / Cleanup (`CleanupBgActivity.java` & `CleanupBgView.java`)
- **Touch Brush**:
  - Adjustable radius slider from $10\text{ px}$ to $100\text{ px}$.
  - Paints opaque clean paper white (`#FFFFFF`) or user-sampled background color.
  - Removes finger shadows, paper punch holes, staple marks, and bleed-through text.
- **Vector Stroke History**:
  - Stores an undo/redo stack of `PSLinePath` instances.
  - Non-destructive until the user taps "Done", baking the modified bitmap.

### 6.2 Markup & Annotation (`MarkupActivity.java` & `MarkupView.java`)
- **Freehand Pen**: Variable stroke width with anti-aliasing.
- **Highlighter**: Semi-transparent multiply blend mode (`PorterDuff.Mode.MULTIPLY`) simulating yellow, green, pink, and cyan highlighters.
- **Text Box**: Editable text overlay with font selection, font size slider, color picker, and background fill.
- **Watermark Engine**: Repeating diagonal text watermark (`45^\circ` angle) or image logo stamp with opacity control.
- **Signature Stamp**: Inserts vector signatures from the signature library.
- **Date Stamp**: Automated localized date/time stamp.

### 6.3 Digital Signature Pad (`SignaturePadActivity.java`)
- Pressure-sensitive touch signature pad using Catmull-Rom or cubic Bezier spline interpolation.
- Default pen color: `#000000` (Black), with Navy (`#000080`) and Red (`#C00000`) options.
- Stroke thickness slider: $2\text{ dp}$ to $12\text{ dp}$.
- Exports transparent background PNG to page editor.

---

## 7. Storage, Naming & Backup Specification

### 7.1 Automated Document Naming Engine (`DocumentNameSettingActivity.java`)
- Configurable naming template tokens:
  - `[Doc_Name]`: Base document title.
  - `[Date:yyyyMMdd]`: Formatted creation date.
  - `[Time:HHmmss]`: Formatted creation time.
  - `[Counter:0001]`: Auto-incrementing 4-digit serial number.
  - `[Folder_Name]`: Current folder name.
- Default Pattern: `New Doc [yyyy-MM-dd HH.mm.ss]`.

### 7.2 Recycle Bin (Trash) Architecture (`MainTrashActivity.java`)
- Soft-delete sets `i_date_trash = System.currentTimeMillis()`.
- Active library queries exclude trash: `WHERE i_date_trash IS NULL`.
- Trash screen queries: `WHERE i_date_trash IS NOT NULL ORDER BY i_date_trash DESC`.
- Restore action sets `i_date_trash = NULL`.
- Permanent purge deletes the database row and unlinks JPEG/PNG files from internal storage.
- Auto-purge cron/worker sweeps items with `i_date_trash < (now - 30 days)`.

### 7.3 Backup & Restore Engine (`MyBackupActivity.java`)
- **Export Archive**:
  - Builds a standalone `.zip` archive containing:
    - `app.db`: SQLite database snapshot.
    - `manifest.json`: JSON catalog of documents, folders, and page counts.
    - `images/`: Hierarchical folder structure containing all original and baked page images (`doc_<id>/page_<id>.jpg`).
- **Import / Restore**:
  - Validates archive integrity and schema version.
  - Atomically unpacks images to app private storage.
  - Re-attaches or merges database records with ID collision resolution.

---

## 8. Summary & Next Implementation Priorities

| Feature Domain | Rebuilt Coverage | Next Implementation Priority |
| :--- | :--- | :--- |
| **Borders & Crop** | 100% Parity (TFLite ML + 8 handles + loupe) | Production ready |
| **Filters & Rendering** | 100% Parity (Magic Color, Clear B&W, Rust kernels) | Production ready |
| **Dashboard & Library** | 100% Parity (Grid/List, Search, Folders) | Production ready |
| **PDF Export Engine** | 100% Parity (50 paper sizes, compression, password) | Production ready |
| **Trash / Recycle Bin** | 100% Parity (`i_date_trash` soft delete & restore) | Production ready |
| **Markup & Annotation** | 60% Parity (Signature dialog, Watermark dialog live) | Build `MarkupCanvas.kt` |
| **Background Eraser** | 0% Parity (Reverse engineering complete) | Build `CleanupBgCanvas.kt` |
| **ID Card Scan Mode** | 0% Parity (Reverse engineering complete) | Add ID Card dual-capture mode |
| **Backup / Restore** | Implemented in Round 43 (`util/BackupArchive.kt`) | Zip of library + pages, validated restore, merges rather than replaces |

---

## 9. Verification notes (Arena.ai, 2026-10-27)

Read this with §4.1, §3.3 and §8. Everything below was checked against artifacts in
this repository and against the code in `app/src/main/java`, not taken from a summary.

### 9.1 §4.1 ML border detection — confirmed, with one addition

- Input `0.0…255.0` float RGB, 224×224: **confirmed** by measurement — the same image
  scores 0.99 mean corner confidence at `0…255` and 0.53 at `0…1`.
- `StatefulPartitionedCall:1` `[1,1,4,2]` normalised `(y, x)` corners and
  `StatefulPartitionedCall:0` `[1,1,4]` scores: **confirmed** against the file itself.
- Gate `mean(c) >= 0.50f`: **confirmed** (`Lqt3;->c`, `cmpl-float` against
  `const/high16 1056964608`).
- **Addition the spec omits:** the model has **six** outputs, and a second tensor
  (`StatefulPartitionedCall:5`, also `[1,1,4]`) is exactly as wide as the scores. Any
  decoder that finds its outputs by element count can bind that one and read ~0.2
  confidence on a good page. `MlBorderDetector` therefore binds by **name**. Full
  evidence: [`ORIGINAL_ML_BORDER_PATH.md`](ORIGINAL_ML_BORDER_PATH.md).
- §4.1 item 4 describes `QuadSanity.kt` inaccurately. It does **not** test convexity.
  It rejects non-finite coordinates, an area outside `0.06…1.05`, a min/max side ratio
  below `0.18`, and out-of-order edges beyond `0.01`, while allowing corners up to
  `0.02` outside the frame (a page running off the edge of the photograph is a real
  answer, not a bad one).

### 9.2 §3.3 paper sizes — the database says 64 rows, 15 of them visible

`assets/databases/app.db` is inside `docs/original_apk_pipeline_artifacts.zip`, so this
is checked, not inferred:

- `paper_size` holds **64** rows; `b_is_show_in_select_list = 1` on exactly **15**:
  A3, A4, A5, B4, B5, Letter, Legal, Ledger, Tabloid, Executive, ANSI C, ANSI D,
  ANSI E, Business Card, Folio — plus the `xxx Use Localize` placeholder (id 999,
  `0 × 0`). The other 49 are built-in but hidden from the picker.
- The doc's Business Card row (`241 × 153 pt`) disagrees with the database
  (**`353 × 206 pt`, unit `in`**). Our app ships `243 × 153 pt`, which is the physically
  correct 85.6 × 54 mm — neither matches the original's stored value, and we are not
  changing ours to a number that is not a business card.
- **Closed in Round 42.** Our `PaperSize` now carries all **15** visible originals with
  the database's stored point values and order, plus our own `RECEIPT` — 16 entries,
  grouped by the original's own `paper_size_group` (`PaperGroup.ISO_A` / `ISO_B` /
  `AMERICAN` / `OTHER`) and rendered under headings in `PdfExportSheet`, whose 2-column
  grid would otherwise be a wall of sixteen cards. Two departures, both pinned by
  `PaperSizeTest`:
  - `BUSINESS_CARD` stays at **243 × 153 pt** (85.6 × 54 mm, ISO/IEC 7810 ID-1). The
    original's stored `353 × 206 pt` is 124.5 × 72.7 mm, which is not a business card.
  - `RECEIPT` is ours; the original has no receipt preset.
  - `A3` now uses the database's `842 × 1190` instead of our `842 × 1191`. Both are
    roundings of 841.89 × 1190.55 pt; matching the original keeps PDF page boxes
    identical to a file exported by it.

### 9.3 §8 "100% parity" — not accurate for everything it claims

Verified as implemented in this repository: ML border detection, the five filters and
Rust kernels, dashboard/library, PDF export (8 sizes, 4 margins), trash.

**Not implemented**, despite appearing in §1/§8 as parity:

- ID Card, Book, Passport, Photo and QR/Barcode capture modes. The camera does
  single-shot and batch at 4:3, which is a deliberate scope decision (static guide frame
  removed, detection kept off the camera screen).
- The `PaperBorderView` viewfinder tracker and the IOU ≥ 0.95 auto-shutter. Also a
  deliberate decision, not an oversight.
- Markup & annotation canvas, Background Eraser, Cloud Sync. (Backup/Restore closed in
  Round 43 — `util/BackupArchive.kt`, see the changelog.)
- 15 visible paper sizes (see 9.2).

### 9.4 §7.1 naming

Our default is `'Scan' yyyy-MM-dd HH-mm-ss`, not `New Doc [yyyy-MM-dd HH.mm.ss]`. It is
deliberately quote-free and uses hyphens instead of colons so the name is portable
across MTP/FAT/exFAT when a page is copied off the device by hand.
