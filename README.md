# DocScanner (Modern Clean-Room Android Document Scanner)

A modern, high-performance Android document scanner application built from scratch with **Kotlin 2.2**, **Jetpack Compose (BOM 2026.03.01)**, and **Material 3 Expressive UI**. Clean-room engineered to replace legacy document scanners with modern architecture, no advertising SDKs, and zero DRM dependencies.

Verified and fully functional on **Android 12+ (API 31–36)**.

---

## 🚀 Key Features & Implemented Workflows

1. **Dashboard & Organization (`ui/dashboard/`)**:
   - Real-time instant search across document titles and tags.
   - Filter chips for folders (All, Business, Personal, Receipts, ID Cards, Legal).
   - Dense Grid vs. List view switcher with animated transition.
   - Sorting by Date Modified, Alphabetical (A–Z), and Page Count.
   - Extended Floating Action Button (FAB) for Camera and Batch Scanning.

2. **Camera & Viewfinder (`ui/camera/`)**:
   - CameraX integration with real-time document alignment quad overlay.
   - Batch capture counter for continuous multi-page scanning.
   - Flash modes: Torch, Auto, Off.
   - Direct system photo gallery album picker.

3. **Perspective Crop & 5 ColorMatrix Filters (`ui/editor/`)**:
   - Interactive 4-corner draggable polygon perspective warp canvas.
   - Hardware-accelerated real-time color filters (`util/ImageProcessingUtils.kt`):
     - **Original**: Natural camera colors.
     - **Magic Color** (Doc Level 1): Saturation boost & shadow flattening.
     - **Clear B&W** (Doc Level 2): Extreme luminance contrast for crisp document text.
     - **Grayscale**: Perceptual 8-bit monochromatic conversion.
     - **Lighten**: Dark background lifting preserving ink density.
   - 90° lossless rotation.

4. **Multi-Page Document Detail Manager (`ui/detail/`)**:
   - Multi-page grid preview with thumbnail indicators.
   - Page reordering, rotation, deletion, and append new scans.
   - One-tap actions: Add Digital Signature, Add Watermark, and Export PDF.

5. **Fullscreen Inspector (`ui/viewer/`)**:
   - High-resolution pan & pinch-to-zoom (up to 4x).
   - Horizontal swipe paging across scanned pages.
   - Page details and OCR bottom sheet.

6. **Digital Handwritten Signature Pad (`ui/signature/`)**:
   - Pressure-sensitive drawing canvas with smooth quad bezier paths.
   - Pen stroke thickness slider.
   - Ink color options: Executive Black, Deep Blue, Crimson Red.

7. **Multi-Page PDF Compiler & Sizing Engine (`util/PdfGenerator.kt` & `ui/pdf/`)**:
   - Hardware-accelerated PDF generation using Android `PdfDocument`.
   - **7 Standard Paper Sizing Presets**:
     - A4 (595 × 842 pt)
     - US Letter (612 × 792 pt)
     - US Legal (612 × 1008 pt)
     - A3 (842 × 1191 pt)
     - A5 (420 × 595 pt)
     - Executive (522 × 756 pt)
     - Business Card (241 × 153 pt)
   - Margin configurations: None, Normal, Wide.
   - Page numbering (Header / Footer presets).
   - Direct integration with Android system ShareSheet.

8. **Recycle Bin / Trash System (`ui/trash/`)**:
   - Soft deletion tracking.
   - Instant document restoration to active folders.
   - Permanent document purge.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: 100% Jetpack Compose with Material 3 Expressive UI
- **Navigation**: Jetpack Navigation 3 (Type-Safe navigation keys)
- **Architecture Pattern**: Unidirectional Data Flow (UDF) with Kotlin `StateFlow` and Repository pattern
- **Image Processing**: Android `ColorMatrix` hardware pipelines + Android Graphics Canvas
- **Build System**: Gradle 9.1.0, Android Gradle Plugin (AGP) 9.0.1, Kotlin 2.2.0
- **Min SDK / Compile SDK**: Min SDK 26 (Android 8.0) • Compile/Target SDK 36 (Android 16)

---

## 📖 Complete Specification & AI Blueprint

For the complete reverse-engineering catalog, decompiled activity mappings, and AI model context schema, see:
- [`doc_scanner_architecture_spec.html`](./doc_scanner_architecture_spec.html)

---

## 🔨 Building & Running

### Requirements
- Java JDK 21+
- Android SDK 36 (Build Tools 36.0.0)

### Build Debug APK:
```bash
./gradlew assembleDebug
```

The output APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Install directly to connected device:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
