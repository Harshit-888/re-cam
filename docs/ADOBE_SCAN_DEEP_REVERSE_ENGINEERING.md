# Adobe Scan (com.adobe.scan.android) Deep Reverse Engineering Specification

> **Target Package**: `com.adobe.scan.android` (Version 26.10.06)  
> **Source Artifacts**: `base.apk`, `split_config.arm64_v8a.apk`, `split_pdfEditClientResources.apk`  
> **Analysis Methodology**: JADX Targeted Decompilation (`-XX:+UseParallelGC -Xmx1024m`), FlatBuffers binary schema parsing, PyElfTools dynamic symbol analysis, `aapt` resource extraction.  
> **Target Rebuild Project**: `doc-scanner` (Jetpack Compose, CameraX, OpenCV/ONNX/TFLite, Rust NDK)

---

## 1. Executive Architecture Overview

Adobe Scan is a state-of-the-art document scanner built with:
1. **Pure Jetpack Compose UI**: 100% of the UI (viewfinder, camera preview overlay, crop handles, 2.5x loupe, review carousel, filters dialog, reordering grid) is rendered in Jetpack Compose without legacy Android View XML layouts.
2. **Real-time Mobile Vision (LiteRT / TFLite)**: Sub-50ms inference on camera stream frames using quantized MobileNetV2 with direct RGBA uint8 byte inputs.
3. **Native C++ Computer Vision Engine (`libMagicClean.so` - 41.9 MB)**: Sensei On-Device Services (SODS) engine combining Classical Computer Vision (Canny/Sobel high contrast, gradient accumulation low contrast) with Neural Networks (AlgoFusion), CIELAB shadow suppression, curve dewarping, and adaptive cleanup.
4. **Searchable PDF Engine (`Scan2PDFShim` / `libScan2PDF.so`)**: Compiles compressed images into PDF format with an invisible OCR text layer (`3 Tr` rendering mode) positioned directly over word bounding boxes.

```
                           +-------------------------------------+
                           |      Android Jetpack Compose UI     |
                           | (CaptureActivity / CropView / Loupe)|
                           +------------------+------------------+
                                              |
                   +--------------------------+--------------------------+
                   |                                                     |
         [Real-Time Live Camera]                               [Captured High-Res Image]
                   |                                                     |
  CameraCleanLiveEdgeDetectionAndroidShim                       MagicCleanEdgeDetection
  - 250ms Timekeeper Loop                                       - m8d / SODS Bridge
  - Confidence Tracker (1..3)                                   - 384x384 Area Resizing
  - Torch & Shadow Detection                                    - CIELAB Shadow Removal
                   |                                                     |
                   v                                                     v
        +----------------------+                              +----------------------+
        | capture_6_22_23.tflite|                              |    libMagicClean.so  |
        | MobileNetV2 (320x320)|                              | (AlgoFusion: HC+LC+NN|
        +----------------------+                              +----------------------+
```

---

## 2. Machine Learning Models & Exact Tensor Signatures

Extracted and verified directly via FlatBuffers binary inspection from `base.apk`:

### 2.1 Live Boundary Detection Model (`capture_6_22_23.tflite`)
- **File Size**: 2,542,032 bytes (2.48 MB)
- **Architecture**: MobileNetV2 backbone with direct quadrilateral regression and curvature keypoint outputs.
- **Input Tensor (Verified via LiteRT runtime)**:
  - Name: `serving_default_input_2:0`
  - Index: `#0`
  - Shape: `[1, 320, 320, 4]` (Batch=1, Height=320, Width=320, Channels=4: **RGBA**)
  - Data Type: **`UINT8`**
  - Quantization: `scale = 0.003921568859368563` ($1/255$), `zero_point = 0`
  - **Zero-Copy Optimization**: Because quantization scale is $1/255$ and zero-point is 0, the model handles $x/255.0$ normalization internally on the NPU/GPU/CPU delegate. **No floating-point conversion is needed in Kotlin/C++**. CameraX ImageProxy RGBA bytes are passed directly into the interpreter buffer.
- **Output Tensor**:
  - Name: `StatefulPartitionedCall:0`
  - Index: `#189`
  - Shape: **`[1, 13, 2]`** (Batch=1, 13 Keypoints, $(x, y)$ coordinate pairs)
  - Data Type: **`FLOAT32`** (Normalized $[0.0, 1.0]$ coordinate space)
- **Semantic Breakdown of the 13 Output Keypoints**:
  - **Points 0..3 (Corners)**:
    - Point 0: Top-Left Corner $(x_0, y_0)$
    - Point 1: Top-Right Corner $(x_1, y_1)$
    - Point 2: Bottom-Right Corner $(x_2, y_2)$
    - Point 3: Bottom-Left Corner $(x_3, y_3)$
  - **Points 4..7 (Edge Midpoints)**:
    - Point 4: Top Edge Midpoint $(x_4, y_4)$
    - Point 5: Right Edge Midpoint $(x_5, y_5)$
    - Point 6: Bottom Edge Midpoint $(x_6, y_6)$
    - Point 7: Left Edge Midpoint $(x_7, y_7)$
  - **Points 8..11 (Curvature Anchors)**:
    - Spline/Bezier displacement anchors used by `CNNDewarpingGridCompute` for page curvature correction.
  - **Point 12 (Centroid / Confidence)**:
    - Document visual centroid $(c_x, c_y)$ used for stability verification.

### 2.2 Multi-Page Turn Detection Model (`page_turn_2im_002.tflite`)
- **File Size**: 1,190,024 bytes (1.16 MB)
- **Architecture**: Recurrent CNN with **LSTM** memory unit.
- **Input Tensors**:
  - `#0`: `input_2` $\rightarrow$ `[1, 2, 1, 1]` `UINT8`
  - `#1`: `lstm_hidden_all` $\rightarrow$ `[1, 256]` `FLOAT32` (Recurrent hidden state)
- **Output Tensors**:
  - `#186`: `Identity` $\rightarrow$ `[1, 2, 2]` `FLOAT32` (Page turn probability)
  - `#189`: `Identity_1` $\rightarrow$ `[1, 2, 9, 2]` `FLOAT32` (Frame optical flow deformation field)
  - `#190`: `Identity_2` $\rightarrow$ `[1, 256]` `FLOAT32` (Next recurrent hidden state)

### 2.3 Hardware Resizer Model (`resize_to_320.tflite`)
- **File Size**: 7,848 bytes
- **Input Tensor**: `[1, 1, 1, 4]` `UINT8`
- **Output Tensor**: `[1, 1, 1, 1]` `UINT8`

---

## 3. Real-Time Viewfinder Tracking Engine

Decompiled directly from `com.adobe.magic_clean.CameraCleanLiveEdgeDetectionAndroidShim`:

### 3.1 Timing & State Machine Loop
The live edge detector runs on a fixed-rate 250ms background timer (`mLiveBoundaryTimeKeeper`):
```java
// Scheduled at fixed rate: interval = 250ms
this.mLiveBoundaryTimeKeeperInterval = 250L;
this.mLiveBoundaryUpdateInterval = 750L;           // 3 ticks: polygon update
this.mLiveBoundaryHintUpdateInterval = 1000L;       // 4 ticks: text hint update
this.mLiveBoundaryCaptureEnableInterval = 1500L;    // 6 ticks: hold-for-capture arm
// Auto-capture fires at (mLiveBoundaryCaptureEnableInterval + 1000L) = 2500ms (10 ticks)
```

```
[Camera Frame] (every ~33ms)
       │
       ▼ (if 750ms tick elapsed)
[GetBoundariesInLivePreview] ──► TFLite Inference (capture_6_22_23.tflite)
       │
       ▼
[AreCurrentCornersWithinTolerance] (tolerance d = 0.2, stability threshold = 0.1)
  ├─► TRUE  ──► mBoundaryConfidence++
  └─► FALSE ──► Confidence Decay: (conf > 5 ? conf >> 1 : conf - 1)
       │
       ▼
[Evaluate Display & Capture Thresholds]
  ├─► conf >= DisplayThreshold (1 or 2)  ──► Render Blue Compose Boundary Polygon
  ├─► conf >= CaptureThreshold (3 or 4)  ──► Start Capture Hold Counter (1500ms)
  │                                           UI Hint: "Hold steady"
  └─► timer reaches 2500ms               ──► Trigger Shutter (ReadyForCapture)
```

### 3.2 Dynamic Confidence Thresholding
To adapt across low-end and high-end hardware, thresholds scale dynamically based on frame latency:
- If inference time $t_{\text{frame}} > 0.7\text{s}$:
  - `mBoundaryConfidenceForDisplayThreshold = 1`
  - `mBoundaryConfidenceForCaptureThreshold = 3`
- If inference time $0.3\text{s} < t_{\text{frame}} \le 0.7\text{s}$:
  - `mBoundaryConfidenceForDisplayThreshold = 2`
  - `mBoundaryConfidenceForCaptureThreshold = 4`

### 3.3 Stability Verification & Dummy Edge Filter
- **Tolerance Formula**:
  $$\max_{i \in [0, 3]} \left( |x_i - x_{i, \text{prev}}|, |y_i - y_{i, \text{prev}}| \right) \le 0.1$$
- **Screen-Clamp Dummy Edge Rejection (`CheckIfQuadHasDummyEdge`)**:
  Rejects any quad where a full edge is pinned to screen boundaries:
  - $(y_0 \approx 0 \land y_1 \approx 0) \lor (x_1 \approx 1 \land x_2 \approx 1) \lor (y_2 \approx 1 \land y_3 \approx 1) \lor (x_3 \approx 0 \land x_0 \approx 0)$.

### 3.4 Glare & Low-Light Flash Automation
- **Low Light Detection**: Evaluates ambient scene lux; if `mBrightnessFromCamera <= 5.0` lux for $\ge 2$ consecutive frames, torch is automatically requested (`kLiveBoundaryTorchHintTorchOn`).
- **Shadow Detection**: Native `imageHasDarkShadow(bmp, corners)`.
- **Torch Glare Suppression**: Native `imageHasGlareAfterTorch(bmpNoTorch, cornersNoTorch, bmpWithTorch, cornersWithTorch)`. If specular highlights saturate document text, torch is immediately turned off (`kLiveBoundaryTorchHintTorchOff`).

---

## 4. Native C++ Computer Vision Architecture (`libMagicClean.so`)

### 4.1 Boundary Detection AlgoFusion
Inside `MagicClean::BDParamsAlgoFusion`, three distinct edge detection techniques are blended:
1. **`AlgoHC` (High Contrast)**: Classical gradient operators (Sobel/Canny) detecting strong document-to-table contrast boundaries.
2. **`AlgoLC` (Low Contrast)**: Intensity accumulation and localized histogram analysis across white paper on light desks.
3. **`AlgoNN` (Neural Network)**: Output probability heatmaps from the TFLite MobileNetV2 segmentation model.

### 4.2 Document Profiles
Dedicated parameter tuning exists per document category:
- `BDParamsDocTypeDocument`: Standard rectangular quadrilateral constraints.
- `BDParamsDocTypeBook`: Dual-quad detection (`mCCornersInfoVec[0]` and `mCCornersInfoVec[1]`) with spine detection.
- `BDParamsDocTypeForm`: Rigid aspect ratio anchoring for standardized 8.5x11 and A4 forms.
- `BDParamsDocTypeWhiteboard`: High aspect ratio tolerance, perspective warp compensation.
- `BDParamsDocTypeBusinessCard`: 3.5:2 aspect ratio lock.

### 4.3 Dewarping Mesh (`DewarpDocumentsAndroidShim`)
For curved books and receipts, Adobe calculates non-linear edge vectors:
1. Computes Spline/Bezier vectors along top, bottom, left, and right:
   - `mPointsVecTop`, `mPointsVecBottom`, `mPointsVecLeft`, `mPointsVecRight`.
2. Generates a 2D dewarping displacement grid:
   - `mDewarpingGrid` (`float[]`), with dimensions `mDewarpingGridWidth` $\times$ `mDewarpingGridHeight`.
3. Warps bitmap using bicubic surface sampling into a flat rectified plane.

---

## 5. Enhancement Filters & CIELAB Shadow Removal

Decompiled from `MagicCleanEdgeDetection.kt` (`m8d`) and `CameraCleanUtils.java`:

### 5.1 Filter Modes (`CleaningLevel`)
```kotlin
object CleaningLevel {
    const val ORIGINAL = 0       // Level 0: Raw camera capture
    const val AUTO_CLEAN = 2     // Level 2: Magic Color (Shadow removal + Color boost)
    const val WHITEBOARD = 4     // Level 4: Aggressive background whitening + text sharpening
    const val GRAYSCALE = 6      // Level 6: High contrast monochrome document
    const val LIGHT_TEXT = 8     // Level 8: Low-contrast ink / pencil boost
}
```

### 5.2 CIELAB Shadow Removal Algorithm
Adobe Scan processes document enhancement in **CIELAB ($L^*, a^*, b^*$) color space** (`MagicClean::CALABBitmap`):
1. **Color Separation**: Separates Luminance ($L^* \in [0, 100]$) from Chrominance ($a^*, b^*$).
2. **Background Luminance Surface Estimation**:
   - Calculates local background luminance map $L_{\text{bg}}(x, y)$ using a large-kernel bilateral/morphological opening.
   - Extracts `LABColorPixel mTintPixel`: determines the document's inherent paper tint (e.g. cream, aged yellow, or pure white).
3. **Luminance Normalization**:
   $$L_{\text{norm}}(x, y) = \min\left(100, \frac{L(x, y)}{L_{\text{bg}}(x, y)} \times 100\right)$$
   This flattens shadows completely without altering ink pigment.
4. **Text Sharpening & Color Recovery**:
   - Chrominance channels ($a^*, b^*$) are multiplied by a localized color boost factor $\gamma_{\text{chroma}} \approx 1.2$ for high-saturation stamps, signatures, and colored logos, while near-neutral paper background is clamped to pure white $(L^*=100, a^*=0, b^*=0)$.

---

## 6. Manual Crop & Magnifying Loupe Mechanics

Decompiled from `CropView.kt` (`ok6`) and `CropMagnifier.kt` (`qj6`):

### 6.1 Hit Testing & Touch Targeting
- **Touch Radius**: `ypfVar.q1(28.0f)` $\rightarrow$ **28.dp touch radius** ($28^2 = 784$ distance squared).
- **Corner Dragging**:
  - Touching within 28.dp of any corner moves only that single corner.
- **Edge Midpoint Parallel Dragging**:
  - Adobe computes the midpoint of each of the 4 edges:
    $$\mathbf{P}_{\text{mid}} = 0.5 \times (\mathbf{P}_i + \mathbf{P}_{(i+1)\%4})$$
  - Touching within 28.dp of $\mathbf{P}_{\text{mid}}$ selects **both adjacent corners**:
    Dragging the edge translates both corners simultaneously, preserving the edge orientation!
- **D-Pad / Keyboard Nudging**:
  - Arrow key press moves selected corner by `10.0f` px.
  - `Shift + Arrow key` moves selected corner by `50.0f` px.

### 6.2 Magnifying Loupe Specs (`CropMagnifier`)
- **Magnification Factor**: Exactly **2.5x** (`f4 = 2.5f`).
- **Lens Geometry**:
  - Outer Diameter: **80.dp** (`i6k.q(txdVar, 80.0f)`).
  - Lens Radius: **40.dp** (`da8Var.q1(40.0f)`).
  - Lens Border Ring: **3.dp** stroke (`yok(da8Var.q1(3.0f))`), colored in theme primary color.
- **Crosshairs**:
  - Horizontal line through circle center from $-40\text{dp}$ to $+40\text{dp}$.
  - Vertical line through circle center from $-40\text{dp}$ to $+40\text{dp}$.
  - Stroke width: half of border ring (`1.5dp`).
- **Shader Transform**:
  $$\mathbf{T}_{\text{shader}} = \mathbf{T}(\text{center}) \times \mathbf{S}(2.5, 2.5) \times \mathbf{T}(-\text{corner})$$
  Clipped to circle radius 40.dp with `Canvas.clipPath`.

---

## 7. Searchable PDF Generation (`Scan2PDFShim`)

Decompiled from `com.adobe.scan2pdf.Scan2PDFShim` and `Scan2PDFUtils`:
- **Input Struct (`CreatePDFInput`)**:
  - `byte[] mImageBuffer`: High-res JPEG compressed buffer.
  - `int mImageWidth, mImageHeight`: Image pixel dimensions.
  - `int mPageWidth, mPageHeight`: PDF points dimensions (e.g., A4: $595 \times 842$ pt).
  - `ArrayList<OCRResult.OCRRegionResults> mOCRRegionResults`: OCR word bounding boxes, baseline offsets, and UTF-8 strings.
  - `String mPDFFilePath`: Output file destination.
- **Rendering**:
  - Inserts image as `/XObject /Subtype /Image`.
  - Injects invisible text layer with text rendering mode `3 Tr` (Neither fill nor stroke text, invisible searchable font) positioned exactly over the image word bounding boxes.

---

## 8. Complete Activity Navigation & Topology

Decompiled from `AndroidManifest.xml` and Activity coordinators:

```
[SplashActivity]
       │
       ▼
[CaptureActivity] ◄──────────────────────────────────┐
       │ (Auto/Manual Snap)                          │
       ├──────────────► [DocumentDetectionActivity]  │
       │                (Batch Photo Crop)           │
       │                                             │
       ▼                                             │
[ReviewActivity] ────────────────────────────────────┘
       │ (Bottom Navigation Bar Actions)
       ├─► [Add Page] ────────► [CaptureActivity]
       ├─► [Reorder] ─────────► [ReorderActivity] (Drag & drop grid)
       ├─► [Crop] ────────────► [CropView] (28dp midpoint drag + 2.5x loupe)
       ├─► [Rotate] ──────────► Inline 90° clockwise cycle
       ├─► [Filter] ──────────► [FilterOptionsDialog] (0, 2, 4, 6, 8)
       ├─► [Clean Up] ────────► [EraserActivity / MagicEraserActivity]
       ├─► [Markup] ──────────► [MarkupActivity / MarkupShapesActivity]
       ├─► [Resize] ──────────► [ResizeActivity] (A4, Letter, Legal, etc.)
       ├─► [Delete] ──────────► Inline page removal
       └─► [Save PDF] ────────► [Scan2PDFShim] ──► [FileBrowserActivity]
```

---

## 9. SQLite Persistence & Scan Session Schema

Decompiled from `AppDatabase_Impl` (`og2`) and `Page.kt`:

### 9.1 Room SQLite Table
```sql
CREATE TABLE IF NOT EXISTS `FileDescriptor` (
    `mPrimaryKey` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `uri_path` TEXT,
    `modified_date` INTEGER NOT NULL,
    `is_document` INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS `index_FileDescriptor_uri_path` ON `FileDescriptor` (`uri_path`);
```

### 9.2 In-Flight Scan Session Model (`document.json`)
Each page within an active session contains the following state:
```json
{
  "path": "/data/user/0/com.adobe.scan.android/cache/scan_101_1.jpg",
  "crop_points": [
    {"x": 0.082, "y": 0.115},
    {"x": 0.914, "y": 0.098},
    {"x": 0.942, "y": 0.884},
    {"x": 0.061, "y": 0.895}
  ],
  "rotation": 0,
  "filter": "auto",
  "brightness": 0.0,
  "contrast": 0.0,
  "capture_mode": "DOCUMENT",
  "page_scale": "A4",
  "dewarped": false,
  "manual_crop": false,
  "book_mode_position": "none",
  "eraser_filename": null,
  "markup_filename": null
}
```

---

## 10. Editor Suite Tools

### 10.1 Smart Cleanup & Inpainting (`SmartEraserAndroidShim`)
- `initializeSmartEraser(EraserInput, EraserOutput)`:
  - Takes the captured document bitmap and computes a synthetic background inpainting layer (`mBackgroundBmp`).
  - As the user brushes over defects, fingers, or unwanted text, the brush replaces pixels with `mBackgroundBmp` or samples surrounding paper color via eyedropper.

### 10.2 Page Resize Presets (`ResizeActivity`)
Supported dimensions in PostScript points ($72\text{ pt} = 1\text{ inch}$):
- **Auto-fit**: Preserves original aspect ratio of cropped quad.
- **US Letter**: $612 \times 792\text{ pt}$ (Portrait) / $792 \times 612\text{ pt}$ (Landscape).
- **US Legal**: $612 \times 1008\text{ pt}$ (Portrait) / $1008 \times 612\text{ pt}$ (Landscape).
- **A4**: $595 \times 842\text{ pt}$ (Portrait) / $842 \times 595\text{ pt}$ (Landscape).
- **A3**: $842 \times 1191\text{ pt}$ (Portrait) / $1191 \times 842\text{ pt}$ (Landscape).
- **A5**: $420 \times 595\text{ pt}$ (Portrait) / $595 \times 420\text{ pt}$ (Landscape).

### 10.3 Markup & Annotations (`MarkupActivity`, `MarkupShapesActivity`)
- Freehand pen with thickness and opacity controls.
- Semi-transparent text highlighter.
- Geometric shapes (rectangle, circle, arrow, line) with customizable fill and border color.

---

## 11. Specialized Capture Modes

1. **Document**: Default single/multi-page scanning with quadrilateral edge detection.
2. **Book Mode**:
   - Detects book spine in center.
   - Splits viewfinder into two separate page crops (`mCCornersInfoVec[0]` and `mCCornersInfoVec[1]`).
   - Automatically orders left page (`book_mode_position = "left"`) followed by right page (`book_mode_position = "right"`).
3. **ID Card Mode**:
   - Dual-step capture: "Scan front of ID card" $\rightarrow$ "Scan back of ID card".
   - Automatically composites both sides onto a single page with aligned layout.
4. **Whiteboard Mode**:
   - Perspective warp compensation for off-axis viewing angles.
   - Aggressive thresholding (Filter Level 4) removing ambient reflections and whiteboard glare.
5. **Business Card Mode**:
   - Enforces 3.5:2 aspect ratio lock.
   - Triggers automatic OCR contact parsing with direct export to Android Contacts via vCard (`AddContactActivity`).

---

## 12. Concrete Rebuild Parity Checklist for `doc-scanner`

| Feature Area | Current `doc-scanner` | Adobe Scan Parity Implementation |
|---|---|---|
| **Viewfinder Loop** | Raw camera preview | 250ms interval timer with 1..3 confidence accumulator and tolerance threshold $0.1$. |
| **Model** | OpenCV Canny contours | Integrate `capture_6_22_23.tflite` (320x320 MobileNetV2) with UINT8 `[1, 320, 320, 4]` input and `[1, 13, 2]` output. |
| **Edge Dragging** | Corner handles only | Midpoint edge handles (28dp touch radius) translating both adjacent corners simultaneously. |
| **Magnifier Loupe**| Rectangular preview | 80dp circular lens, 2.5x zoom, 3dp border, with centered horizontal & vertical crosshairs. |
| **Color Filters** | Basic RGB thresholds | CIELAB shadow removal ($L^*$ channel normalization) + Paper tint subtraction + Color boost. |
| **Hints Banner** | Static text | Progressive hints: "Looking for document" $\rightarrow$ "Hold steady" $\rightarrow$ Auto-capture trigger at 2.5s. |
| **Cleanup Tool** | Not implemented | Smart Eraser with clean paper background synthesis. |
| **Paper Presets** | Default A4 only | Auto-fit, Letter, Legal, A3, A4, A5 with portrait/landscape toggles. |
| **PDF Searchability**| Image-only PDF | Injected invisible text layer over OCR bounding boxes. |
