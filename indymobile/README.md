# 📱 IndyMobile Reverse Engineering Evidence & Decompiled Blueprints (`indymobile/`)

> **Context for Arena.ai & Antigravity Engineers**
> This directory contains the complete raw reverse-engineering artifacts, decompiled C algorithms, decompiled Java shims, and native binaries for **IndyMobile Document Scanner** (`com.indymobileapp.document.scanner`), the primary commercial baseline for DocScanner.
>
> Use these raw reference sources to accurately reconstruct image processing filters, border detection, markup tools, and camera mechanics into modern Kotlin / Jetpack Compose / Rust NDK.

---

## 📂 Directory Index

```
indymobile/
├── README.md                          # This document
├── apks/                              # Original production package
│   └── base.apk                       # 48.2 MB production base APK
├── native_libs/                       # Extracted 64-bit ARM ELF shared objects
│   ├── libimageproc.so                # 138 KB (Core OpenCV edge detection & warp engine)
│   ├── libnative-filters.so           # 23.7 KB (Fast enhancement & shadow removal)
│   └── libnms.so                      # 302 KB (Non-maximum suppression boundary clustering)
├── decompiled_c/                      # Ghidra C decompilations of libimageproc.so (262 functions)
├── decompiled_java/                   # JADX decompiled sources of com/indymobile/app (75 classes)
├── pipeline/                          # Raw pipeline interfaces, Smali bytecode & Manifest
└── reports/                           # REA analysis & inspection reports
```

---

## 🔬 Key Subsystems for Reconstruction

### 1. Native Image Processing (`decompiled_c/`)
These C files show the exact native algorithms previously called through JNI:

| Algorithm | Ghidra Decompiled Source | Notes / Rust Parity Target |
| :--- | :--- | :--- |
| **Document Border Detection** | `Java_com_indymobile_app_PSOpenCV_detectPageBorder.c` | Canny, HoughLines, contour polygon approximation (`approxPolyDP.c`), quad angle checks. |
| **Document / Magic Filter** | `Java_com_indymobile_app_PSOpenCV_enhanceDocument.c` | Mean-pooling paper map, division table, adaptive thresholding, contrast stretching. |
| **Photo Filter** | `Java_com_indymobile_app_PSOpenCV_enhancePhoto.c` | Saturation enhancement and RGB gamma curve mapping. |
| **Signature Extraction** | `Java_com_indymobile_app_PSOpenCV_enhanceSignature.c` | Binarization, paper texture suppression, ink stroke thickening. |
| **Signature Filter** | `Java_com_indymobile_app_PSOpenCV_signatureFilter.c` | Alpha mask generation for stamping onto documents. |
| **Perspective Warp** | `Java_com_indymobile_app_PSOpenCV_perspectiveTransformRect.c` | 3x3 projective transform matrix calculation and bilinear interpolation. |
| **IOU Calculation** | `calculateIOU.c` | Intersection-over-Union bounding polygon sanity checks. |

### 2. Decompiled Application Architecture (`decompiled_java/`)
These Java classes show the original application structure:

- **JNI Bridge**: `PSOpenCV.java` (Native method declarations).
- **Camera Subsystem** (`activity/camera/`): CameraX configuration, frame analysis loop, orientation lock, autofocus.
- **Editor Subsystem** (`activity/editor/`): Quad handle positioning, rotation, filter switching.
- **Markup / Annotations** (`activity/editor/markup/`): Freehand pen, highlighter, text annotations, eraser, stamp placement.
- **Data Models** (`model/`): Page metadata, document ordering, folder hierarchies.
- **Utilities** (`util/`): File storage paths, bitmap cache, PDF export pipeline.

### 3. Pipeline Definitions (`pipeline/`)
- `AndroidManifest.xml`: Complete manifest declaring activities, file providers, and permissions.
- `pipeline_java/`: Clean Java bean definitions (`PSDocumentProcessInfo.java`, `PSDocumentFilterInfo.java`, `PSPage.java`, `PSRect.java`, `PSPoint.java`).
- `pipeline_smali/`: Dalvik bytecode for low-level method calling signatures.

---

## 🤝 Complementary Targets
- For **Adobe Scan** models, shims, and SODS engine (`libMagicClean.so`), see [`../adobe-scan/`](../adobe-scan/).
- For additional Dalvik DEX bytecode and split APKs, see [`../docs/original_apk_binaries/`](../docs/original_apk_binaries/).
