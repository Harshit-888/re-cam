# Original Base APK Filter & Image Pipeline Specification

> **Context for Arena.ai & Antigravity**: Reverse-engineered ground truth extracted directly from `base.apk` (`com.indymobileapp.document.scanner`). All unmodified assets, raw resources, smali bytecode, and decompiled Java files are bundled in [`docs/original_apk_pipeline_artifacts.zip`](original_apk_pipeline_artifacts.zip). Use this specification to match the original app's exact behavior in our clean-room Kotlin implementation.

---

## 1. High-Level Architecture & Findings

1. **No External LUTs**:
   - The original app does **not** use 3D LUTs (`.cube`, `.bin`, `.png` color lookup tables).
   - There are zero LUT assets in `assets/` or `res/raw/`. All filtering is strictly algorithmic.
2. **Native OpenCV + TFLite Core**:
   - The app delegates core heavy lifting to OpenCV 4 (`libopencv_java4.so`) and a custom native library (`libimageproc.so`) via JNI (`PSOpenCV.java`).
   - Border detection utilizes a bundled neural network: `assets/border_detect_224.tflite` (224×224 quantized TFLite model) coupled with `PSOpenCV.detectPageBorder()` and IoU validation ($\ge 0.96$).
3. **Dual Dispatcher Design**:
   - Decompiled classes `bx.smali` (single page) and `bx3.smali` (batch processing) dispatch to three processor classes:
     - `qx3`: Original (passthrough) & Grayscale (`Imgproc.cvtColor`).
     - `tw3`: Lighten / Photo mode (`PSOpenCV.enhancePhoto`).
     - `lu3`: Document Level 1 (Magic Color) and Level 2 (Clear B&W) (`PSOpenCV.enhanceDocument`).

---

## 2. Pipeline Execution Order

```mermaid
graph TD
    A["Raw Captured / Imported Bitmap"] --> B["1. Perspective Warp (PSOpenCV.perspectiveTransformRect)"]
    B --> C["2. Image Classification & Range Estimation (fa1.H)"]
    C --> D{"3. Preset Switch (cv3.enhanceType)"}
    D -->|"Original"| E["qx3: Return untouched bitmap (or cvtColor to Gray)"]
    D -->|"Lighten (Photo)"| F["tw3: enhancePhoto(blackPoint, whitePoint, colorMode)"]
    D -->|"Magic Color (Doc Level 1)"| G["lu3: enhanceDocument(level=1, colorMode) -> returns paper bgColor"]
    D -->|"Clear B&W (Doc Level 2)"| H["lu3: enhanceDocument(level=2, colorMode) -> returns paper bgColor"]
```

---

## 3. Image Analysis & Histogram Estimation (`fa1.H`)

Before filtering, every page passes through `fa1.H(bitmap)` to extract image metadata ([PSImageEstimationInfoBean](file:///d:/Desktop/apk-rev/re-workspace/out/jadx/base_20261024_213011/sources/com/indymobile/app/model/PSImageEstimationInfoBean.java)):

1. **Downsample**:
   - The bitmap is scaled to **$100 \times 100$ pixels** (`Bitmap.createScaledBitmap(src, 100, 100, true)`), yielding exactly 10,000 sample pixels.
2. **4 Histograms** (256 bins each):
   - Luminance: $L = (R + G + B) / 3$
   - Red channel $R$
   - Green channel $G$
   - Blue channel $B$
3. **Document vs. Photo Pixel Classification**:
   - Constant thresholds (from `wv3.smali`):
     - `b = 80`, `c = 80`, `d = 70`
     - `e = 60`, `f = 100`, `g = 120`
   - **Document pixel condition**:
     $$R \ge 80 \quad \text{and} \quad G \ge 80 \quad \text{and} \quad B \ge 70$$
     $$|R - B| \le 110 \quad \text{and} \quad (B - 20) \le G \le (R + 20)$$
   - **Photo pixel condition**:
     $$R \ge 60 \quad \text{and} \quad G \ge 100 \quad \text{and} \quad B \ge 120$$
     $$R \le G \le B$$
   - **Classification Rule**:
     $$\text{docRatio} = \frac{\text{docPixels}}{\text{totalPixels}} \times 100.0$$
     $$\text{photoRatio} = \frac{\text{photoPixels}}{\text{totalPixels}} \times 100.0$$
     - If $\text{docRatio} > 60.0\%$ (or $\text{photoRatio} > 60.0\%$), classified as `PSImageTypeDocumentImage`.
     - Otherwise classified as `PSImageTypePhotoImage`.
4. **Dynamic Range Calculation (`blackPoint` & `whitePoint`)**:
   - **`blackPoint`**: Walk luminance histogram from 0 upwards until cumulative count $\ge 200$ (exactly the **2.0% shadow cutoff**).
   - **`whitePoint`**: Walk luminance histogram from 255 downwards until cumulative count $\ge 200$ (exactly the **top 2.0% highlight cutoff**).

---

## 4. Filter Implementation Details

### Presets Enum (`cv3.smali`):
- `kPSEnhanceTypeOriginal` (1, `"original"`)
- `kPSEnhanceTypeLighten` (2, `"photo"`)
- `kPSEnhanceTypeDocumentLevel1` (3, `"doc"`) ➔ **Magic Color**
- `kPSEnhanceTypeDocumentLevel2` (4, `"clear"`) ➔ **Clear B&W**

### Color Modes Enum (`yu3.smali`):
- `kPSColorTypeColor` (0, `"color"`) ➔ param = 1
- `kPSColorTypeGrayScale` (1, `"gray"`) ➔ param = 2

---

### A. Original Preset (`qx3.smali`)
- **Color mode**: Returns bitmap unaltered.
- **Grayscale mode**: Applies OpenCV `Imgproc.cvtColor`:
  - `COLOR_RGBA2GRAY` (or `COLOR_BGR2GRAY`)
  - Converted back to 8-bit ARGB_8888 bitmap.

### B. Lighten Preset (`tw3.smali`)
- Native call:
  ```java
  PSOpenCV.enhancePhoto(context, matSrc, matDst, (int)blackPoint, (int)whitePoint, colorMode);
  ```
- **Algorithm**:
  - Linear contrast/brightness stretch between the estimated `blackPoint` and `whitePoint`.
  - Maps pixel $I$ to $\text{clamp}\left(\frac{I - \text{blackPoint}}{\text{whitePoint} - \text{blackPoint}} \times 255, 0, 255\right)$.
  - Keeps photo color balance while brightening shadows.

### C. Magic Color / Document Level 1 (`lu3.smali`)
- Native call:
  ```java
  int packedBg = PSOpenCV.enhanceDocument(context, matSrc, matDst, level = 1, colorMode = 1);
  ```
- **Algorithm in C++ / OpenCV**:
  1. Paper background estimation (spatial illumination normalization):
     - Uses local morphology (dilation / max filtering) to estimate local paper brightness.
     - Divides local pixel values by the background estimate to remove cast shadows and gradients.
  2. White balance against estimated paper background color:
     - Unpacks returned `packedBg` as `(R << 16) | (G << 8) | B` (stored in `PSDocumentFilterInfo.bgColor`).
  3. Tone-curve enhancement:
     - Darkens text/ink strokes while pinning paper highlights to pure white (255).
     - Modest saturation boost for colored stamps, logos, and signatures.

### D. Clear B&W / Document Level 2 (`lu3.smali`)
- Native call:
  ```java
  int packedBg = PSOpenCV.enhanceDocument(context, matSrc, matDst, level = 2, colorMode = 2);
  ```
- **Algorithm in C++ / OpenCV**:
  1. Illumination normalization (same as Level 1).
  2. Dynamic adaptive thresholding / Otsu binarization over normalized luminance.
  3. Soft anti-aliased edge ramp (or morphological smoothing) so letter glyphs remain crisp without jagged 1-bit artifacts.

---

## 5. Border Detection Algorithm (`qt3.smali` & `assets/border_detect_224.tflite`)

1. Input image downsampled and fed to `border_detect_224.tflite`.
2. Initial quadrilateral extracted and refined with `PSOpenCV.detectPageBorder()`.
3. Validated with `PSOpenCV.calculateIOU(context, quad1, quad2)`:
   - Must achieve **IoU $\ge 0.96$** to be accepted as automated convergence.
   - Otherwise, user manual adjust corners mode is triggered.

---

## 6. Native ELF Analysis (`libimageproc.so`)

The native library `libimageproc.so` (138,664 bytes, ARM64-v8a) was extracted from device split `split_config.arm64_v8a.apk` and analyzed.

### Binary Profile
- **Architecture**: ELF 64-bit LSB shared object, ARM aarch64, Android API 24, NDK r28c.
- **Stripped**: Stripped (`.symtab` stripped; `.dynsym` present at offset `0x000002f8`, size `0x0f00`).
- **Text Section**: `.text` at `0x00008bb4`, size `0x00017ad0` (97,000 bytes).
- **Dependencies**: Dynamically links against `libopencv_java4.so` (23,465,088 bytes), `libc++_shared.so`, `libc.so`, `libm.so`.

### UND (Imported) Symbol Breakdown (77 total)
- **28 OpenCV Symbols** (`_ZN2cv*`):
  `cv::LUT`, `cv::cvtColor`, `cv::GaussianBlur`, `cv::medianBlur`, `cv::resize`, `cv::Laplacian`, `cv::erode`, `cv::dilate`, `cv::Canny`, `cv::HoughLines`, `cv::findContours`, `cv::drawContours`, `cv::approxPolyDP`, `cv::arcLength`, `cv::contourArea`, `cv::isContourConvex`, `cv::operator>=`, `cv::mixChannels`, `cv::line`, `cv::getPerspectiveTransform`, `cv::warpPerspective`, `cv::Mat::create`, `cv::Mat::release`.
- **17 libc++ Symbols**: `std::*` vectors/allocators, `operator new`/`delete`, `__cxa_throw`, `__cxa_allocate_exception`.
- **32 libc / libm / POSIX**: `malloc`, `free`, `memcpy`, `acos`, `atan2f`, `pthread_rwlock_*`, `dl_iterate_phdr`.

### Exported JNI Function Table
| Virtual Address | Size (bytes) | JNI Symbol | Role |
|:---|:---|:---|:---|
| `0x0000f5d4` | 608 | `Java_com_indymobile_app_PSOpenCV_enhancePhoto` | Photo / Lighten filter |
| `0x00010178` | 5,168 | `Java_com_indymobile_app_PSOpenCV_enhanceDocument` | Magic Color & Clear B&W |
| `0x000115a8` | 3,068 | `Java_com_indymobile_app_PSOpenCV_enhanceSignature` | Signature stamp isolation |
| `0x0000f834` | 392 | `Java_com_indymobile_app_PSOpenCV_signatureFilter` | Signature thresholding |
| `0x0001ab68` | 6,748 | `Java_com_indymobile_app_PSOpenCV_detectPageBorder` | Contour & Hough boundary detection |
| `0x0001c660` | 216 | `Java_com_indymobile_app_PSOpenCV_perspectiveTransformRect` | Perspective warp |
| `0x0001c738` | 584 | `Java_com_indymobile_app_PSOpenCV_calculateIOU` | Quad intersection-over-union |

---

## 7. Decompiled Native C Implementations

### `enhancePhoto` (Lighten Filter Algorithm)
Native function disassembled from `0x0000f5d4`:
```c
JNIEXPORT void JNICALL Java_com_indymobile_app_PSOpenCV_enhancePhoto(
    JNIEnv *env, jclass clazz, jobject context,
    jlong srcMatAddr, jlong dstMatAddr,
    jint blackPoint, jint whitePoint, jint colorMode)
{
    cv::Mat* src = (cv::Mat*)srcMatAddr;
    cv::Mat* dst = (cv::Mat*)dstMatAddr;

    // Build 256-entry dynamic 1D LUT in memory
    cv::Mat lut(1, 256, CV_8UC1);
    uint8_t* p = lut.ptr();
    double range = (double)(whitePoint - blackPoint);
    if (range <= 0.0) range = 1.0;

    for (int v = 0; v < 256; v++) {
        if (v <= blackPoint) {
            p[v] = 0;
        } else if (v >= whitePoint) {
            p[v] = 255;
        } else {
            double scaled = ((double)(v - blackPoint) / range) * 255.0 + 0.5;
            p[v] = (uint8_t)std::clamp((int)scaled, 0, 255);
        }
    }

    // Apply lookup table via OpenCV
    cv::LUT(*src, lut, *dst);

    // Grayscale post-processing if colorMode == 2
    if (colorMode == 2) {
        cv::cvtColor(*dst, *dst, cv::COLOR_BGR2GRAY);
    }
}
```

### `enhanceDocument` (Magic Color & Clear B&W Algorithm)
Native function disassembled from `0x00010178`:
1. Instantiates `ColorizingRadius` illumination model.
2. Computes adaptive kernel radius via `computeRadius(width, height, dpi, level)`.
3. Runs morphological operations: `cv::dilate` followed by `cv::erode` (opening) to construct a background illumination surface.
4. Divides foreground channels by background surface, normalizing shadows and page curvature.
5. Level 1 (Magic Color): Sharpens midtone contrast, preserving stamp/logo chromaticity; returns `ColorizingRadius::getBackgroundColor()` as packed `0x00RRGGBB`.
6. Level 2 (Clear B&W): Applies Otsu/adaptive binarization on normalized luminance, maintaining anti-aliased edge transitions.

---

## 8. Actionable Guidance for Clean-Room Reimplementation (`DocumentFilters.kt`)

1. **Dynamic Range / Histogram Sampling**:
   - Scale sample to $100 \times 100$ pixels.
   - Walk cumulative histogram from 0 to find `blackPoint` ($\ge 200$ samples / 2%).
   - Walk cumulative histogram from 255 downwards to find `whitePoint` ($\ge 200$ samples / 2%).
2. **Lighten Preset**:
   - Use exact linear interpolation formula: $\text{clamp}\left(\frac{v - \text{blackPoint}}{\text{whitePoint} - \text{blackPoint}} \times 255.0 + 0.5, 0, 255\right)$.
3. **Magic Color Preset**:
   - Perform illumination normalization (box/min-max or morphological dilation/erosion) before tone curves.
   - Do not hardcode static LUTs; paper background color varies per document and must be estimated dynamically.
4. **Clear B&W Preset**:
   - Normalize paper background before thresholding, preventing dark gradient corners in unevenly lit documents.

