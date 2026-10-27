# Adobe Scan Reverse Engineering Artifacts & Extracted Binaries

This folder contains the complete disassembled, decompiled, and extracted assets of **Adobe Scan** (`com.adobe.scan.android`, Version 26.10.06) pulled directly from the target hardware device.

---

## Folder Contents

### 1. `models/`
Production TFLite models extracted from Adobe Scan assets:
- `capture_6_22_23.tflite` (2.48 MB): Real-time document boundary detection model.
  - Input: `[1, 320, 320, 4]` `UINT8` RGBA (Quantization scale: $1/255$, zero-point: 0)
  - Output: `[1, 13, 2]` `FLOAT32` (4 corners, 4 midpoints, 4 curvature anchors, visual centroid)
- `page_turn_2im_002.tflite` (1.16 MB): Recurrent CNN + LSTM multi-page burst/turn detection model.
- `resize_to_320.tflite` (7.8 KB): Hardware preprocessing resizer model.

### 2. `decompiled_src/`
Decompiled Java & Kotlin sources for key subsystems:
- `CameraCleanLiveEdgeDetectionAndroidShim.java`: Live camera frame tracking, 250ms interval timer, hysteresis confidence filter (1..3), auto-capture trigger at 2500ms.
- `CameraCleanUtils.java`: Bridge data structures (`BDWorkflow`, `CleaningLevel`, `CropConfidence`, `DewarpCurveOutput`, `ImageCleaningInput`).
- `CropView.java` (`ok6`): Pure Jetpack Compose crop overlay, 28dp touch target, edge midpoint parallel drag.
- `CropMagnifier.java` (`qj6`): 2.5x circular magnifying loupe (80dp diameter, 40dp radius, 3dp border, crosshairs).
- `MagicCleanEdgeDetection_m8d.java`: Filter dispatch, CIELAB shadow removal, JNI bridge to `libMagicClean.so`.
- `Page.java`: Session state tracking (quad crop coordinates, rotation, filter, annotations).
- `ReviewActivity.java` & `ReorderActivity.java`: Multi-page review carousel and drag-and-drop reordering grid.
- `Scan2PDFShim.java`: Searchable PDF compiler with invisible OCR text overlay (`3 Tr`).
- `SmartEraserAndroidShim.java`: Background inpainting and paper defect eraser.
- `AppDatabase_Impl.java` & `og2.java`: Room database schema (`FileDescriptor`).

### 3. `extracted/`
Raw extracted assets from the split APKs:
- `classes.dex`, `classes2.dex`, `classes3.dex`, `classes4.dex`: Dalvik bytecode files.
- `lib/arm64-v8a/`:
  - `libMagicClean.so` (41.9 MB): Adobe Sensei On-Device Services (SODS) engine with `AlgoFusion` (High Contrast + Low Contrast + Neural Network).
  - `libScan2PDF.so` (0.85 MB): Native PDF generator.
  - `libimage_processing_util_jni.so`: YUV to RGBA conversion utilities.

### 4. Split APK Packages
- `base.apk` (42.89 MB)
- `split_pdfEditClientResources.apk` (14.83 MB)
- `split_pdfEditClientResources.config.arm64_v8a.apk` (26.02 MB)
- `split_config.xxhdpi.apk` (2.4 MB)
- `split_config.en.apk` (0.4 MB)
- `split_config.arm64_v8a.apk.part0` (65.0 MB) & `split_config.arm64_v8a.apk.part1` (64.85 MB):
  - Split into two parts to comply with GitHub's 100MB single-file limit.
  - To reconstruct the original 129.85 MB APK:
    - **Windows**: `rejoin.bat` or `copy /b split_config.arm64_v8a.apk.part* split_config.arm64_v8a.apk`
    - **Linux / Mac**: `python rejoin.py` or `cat split_config.arm64_v8a.apk.part* > split_config.arm64_v8a.apk`
