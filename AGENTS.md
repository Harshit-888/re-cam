# 🤖 AGENTS.md — AI Engineering & Development Directives

> **MANDATORY CONTEXT FOR ALL AI AGENTS (Antigravity, Arena.ai, Cursor, Copilot, Claude Code, etc.)**  
> Read this document completely before generating code, proposing architectural changes, or continuing development in this repository.

---

## 🎯 1. Project Mission & North Star

This repository (`re-cam` / `DocScanner`) is a **clean-room, ground-up rewrite** of a legacy Android document scanner (`com.indymobileapp.document.scanner`).

The goal is to create a state-of-the-art, privacy-respecting, high-performance Android document scanner that feels ultra-premium using **Material 3 Expressive UI** and **Jetpack Compose**, completely free of third-party advertising SDKs, tracking libraries, and obsolete architecture.

---

## 🚫 2. STRICT GUARDRAILS (What You MUST NOT Do)

1. **NO Legacy Android XML UI**:
   - **NEVER** write XML layouts, `findViewById`, `ViewBinding`, or `DataBinding`.
   - **NEVER** create legacy `Activity` subclasses for individual screens. The app is a modern **Single-Activity Jetpack Compose** app (`MainActivity.kt`).
2. **NO Java Code**:
   - The entire codebase is **100% Kotlin 2.2**. Do not introduce `.java` source files.
3. **NO Ad Networks or Tracking SDKs**:
   - The original app contained massive ad networks (AdMob, AppLovin, ByteDance/Pangle, InMobi). **Do NOT add any ad networks or invasive tracking**.
4. **NO DRM or License Bypassing**:
   - Do not implement any license cracks or bypasses. All business logic must be clean-room developed.
5. **DO NOT Pollute the Git Repository**:
   - Do not commit binaries, `.apk` files, decompiled smali dumps, Ghidra files, or build artifacts (`build/`, `.gradle/`).
6. **NO Breaking Architectural Patterns**:
   - Do not introduce arbitrary global singletons or spaghetti state. Always use Unidirectional Data Flow (UDF) through `ScannerRepository` and `StateFlow`.

---

## 🏗️ 3. Architectural Rules & Principles

| Layer | Standard | Implementation Reference |
| :--- | :--- | :--- |
| **Language & Toolchain** | Kotlin 2.2.0 • AGP 9.0.1 • Gradle 9.1.0 | [`app/build.gradle.kts`](app/build.gradle.kts) |
| **UI Toolkit** | Jetpack Compose BOM 2026.03.01 | [`theme/Theme.kt`](app/src/main/java/com/example/docscanner/theme/Theme.kt) |
| **Design System** | **Material 3 Expressive** (tonal containers, dynamic colors, spring motion, pill chips) | [`theme/Color.kt`](app/src/main/java/com/example/docscanner/theme/Color.kt) |
| **Navigation** | Jetpack Navigation 3 (Type-Safe keys) | [`Navigation.kt`](app/src/main/java/com/example/docscanner/Navigation.kt) |
| **State Management** | Unidirectional Data Flow (UDF) via Kotlin `StateFlow` | [`data/ScannerRepository.kt`](app/src/main/java/com/example/docscanner/data/ScannerRepository.kt) |
| **Domain Models** | Immutable Kotlin Data Classes | [`model/Models.kt`](app/src/main/java/com/example/docscanner/model/Models.kt) |
| **Image Processing** | Hardware-accelerated `ColorMatrix` & Android Graphics `Canvas` | [`util/ImageProcessingUtils.kt`](app/src/main/java/com/example/docscanner/util/ImageProcessingUtils.kt) |
| **PDF Engine** | Native `android.graphics.pdf.PdfDocument` with 72 DPI print scaling | [`util/PdfGenerator.kt`](app/src/main/java/com/example/docscanner/util/PdfGenerator.kt) |

---

## 📊 4. Current State Matrix (What is Built vs. What is Next)

### ✅ Completed & Live on Android 12 (DO NOT BREAK)
- **Dashboard (`ui/dashboard/DashboardScreen.kt`)**: Instant search, folder filter chips, dense grid vs. list switcher, sorting (date, A–Z, pages), extended FAB.
- **Camera Viewfinder (`ui/camera/ScannerCameraScreen.kt`)**: CameraX viewfinder, live document alignment quad overlay, batch capture counter, flash modes, photo album picker.
- **Perspective Crop & 5 Filters (`ui/editor/CropAndFilterScreen.kt`)**: Draggable 4-point polygon warp canvas, 90° rotation, 5 ColorMatrix filters (*Magic Color*, *Clear B&W*, *Grayscale*, *Lighten*, *Original*).
- **Document Detail (`ui/detail/DocumentDetailScreen.kt`)**: Multi-page grid, page rotation, deletion, page reordering, add new scan.
- **Fullscreen Inspector (`ui/viewer/PageViewerScreen.kt`)**: High-res pan and pinch-to-zoom (up to 4x), swipe navigation.
- **Digital Handwritten Signature (`ui/signature/SignaturePadDialog.kt`)**: Bezier drawing canvas, stroke thickness slider, color palette (black, blue, red).
- **PDF Engine (`util/PdfGenerator.kt` & `ui/pdf/PdfExportSheet.kt`)**: 7 standard paper sizes (A4, Letter, Legal, A3, A5, Executive, Business Card), margin controls, page numbering, direct system ShareSheet.
- **Recycle Bin (`ui/trash/TrashScreen.kt`)**: Soft delete, restore, permanent purge.

---

### 🚀 Roadmap: Next Features to Build (Priority Order)

When an AI agent is tasked with adding a new feature, pick exclusively from this prioritized list:

#### Priority 1: Google ML Kit On-Device OCR (`util/OcrManager.kt`)
* **Task**: Integrate Google ML Kit Text Recognition (`com.google.mlkit:text-recognition:16.0.1`).
* **Workflow**:
  1. Process `pageBitmap` via `InputImage.fromBitmap(bitmap, 0)`.
  2. Extract text blocks and update `ScannedPage.ocrText`.
  3. Display extracted text in the bottom sheet inside `ui/viewer/PageViewerScreen.kt` with a "Copy to Clipboard" button.

#### Priority 2: White-out / Background Eraser Tool (`ui/editor/CleanupBgCanvas.kt`)
* **Task**: Implement an eraser brush tool to clean up scanner smudges, finger shadows, or punch holes.
* **Workflow**:
  1. Interactive Compose `Canvas` with touch drag tracking.
  2. Paint circles/paths with pure white (`Color.White`) or sampled background color.
  3. Allow adjustable brush radius (10dp to 50dp) and undo/redo history.

#### Priority 3: Configurable Document Naming Templates (`util/NamingTemplateEngine.kt`)
* **Task**: Replace hardcoded `Doc_YYYYMMDD` with customizable naming templates.
* **Workflow**:
  1. Support template tags: `{PREFIX}`, `{DATE:yyyy-MM-dd}`, `{TIME:HHmmss}`, `{COUNT}`.
  2. Provide a Material 3 settings dialog to customize default naming format.

#### Priority 4: Local ZIP Backup & Restore (`util/BackupRestoreManager.kt`)
* **Task**: Export and import entire document collections as standard `.zip` files.
* **Workflow**:
  1. Write a `manifest.json` describing all folders, documents, and page metadata.
  2. Bundle raw page JPEGs into the archive.
  3. Allow importing a ZIP archive to restore all documents without cloud dependencies.

---

## 🧪 5. Validation Checklist for AI Agents

Before declaring any feature complete:
1. **Compilation**: Run `./gradlew assembleDebug` and verify **BUILD SUCCESSFUL**.
2. **File Placement**:
   - Screens live in `ui/<feature>/<Feature>Screen.kt`.
   - Data models live in `model/Models.kt`.
   - Repository operations live in `data/ScannerRepository.kt`.
   - Pure processing utilities live in `util/`.
3. **Architecture Spec**: Refer to [`doc_scanner_architecture_spec.html`](doc_scanner_architecture_spec.html) for detailed formulas, paper dimensions, and activity mappings.
