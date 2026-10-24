# Agent Changelog

Append-only. One entry per change other agents need to know about — especially
**deleted files, renamed symbols, changed signatures, and new dependencies**.
Newest entry at the top.

---

## 2026-10-25 (b) — Arena.ai — Crash fix + post-capture review flow

### 🐞 Root cause of the "press Done → crash" bug
`ScannerCameraScreen.commitScan()` built a new document's title with

```kotlin
SimpleDateFormat("Scan yyyy-MM-dd HH:mm", Locale.getDefault())
```

`"Scan yyyy-MM-dd HH:mm"` is a **pattern**, not a literal. `S`, `a`, `d`, `y`,
`H`, `m` are legal pattern letters, but **`c` and `n` are not**, so the
constructor threw `IllegalArgumentException: Illegal pattern character 'c'`.
It fired at exactly one moment: **Done on a brand-new document**, because the
`targetDocId != null` branch (adding pages to an existing document) never built a
title. That is why capture worked, the counter advanced, and only *Done* crashed.

Fixed by never inlining a pattern again: `util/DocumentNaming.kt` owns the
pattern (`"'Scan' yyyy-MM-dd HH-mm-ss"` — literal quoted, `-` instead of `:` so
the title is filename-safe) and falls back instead of throwing.
`DocumentNamingTest` locks the behaviour, including a test that asserts the *old*
pattern still throws, so nobody "simplifies" the quotes away.

> **Rule for all agents:** any `SimpleDateFormat` pattern containing a literal
> word must single-quote it. Put patterns in `DocumentNaming`, not in screens.

### ✨ New: post-capture review screen (the flow the original app had)
After *Done*, the camera now opens the editor in **review mode** instead of
jumping to the document:

1. Borders are **auto-detected** on entry (`PerspectiveWarp.detectQuad`).
2. Four draggable corners, live filter preview, 90° rotation.
3. **Save & Next** bakes the page and advances to the next captured page;
   the last page becomes **Finish** and lands on the document screen.

### Signature changes
| Symbol | Before | After |
| :--- | :--- | :--- |
| `CropAndFilterScreen(...)` | `(docId, pageId, repository, onDone)` | **added `reviewMode: Boolean = false`** (last param) |
| `CropAndFilterNav` | `(docId, pageId)` | **`(docId, pageId, review: Boolean = false)`** |
| `PerspectiveWarp.detectQuad` | fell back to a 6 % inset | **falls back to `FULL_FRAME`** — never silently crops when detection is inconclusive |
| `DocumentGridCard` / `DocumentListCard` | `(document, onClick, onDelete)` | **added `onMoveToFolder: () -> Unit`** |

### New files
| File | Purpose |
| :--- | :--- |
| `util/DocumentNaming.kt` | Single owner of title patterns; quotes literals, never throws |
| `app/src/test/.../util/DocumentNamingTest.kt` | First real unit test in the repo; guards the crash pattern |

### Other fixes in this pass
- **"Move to folder" now exists.** `ScannerRepository.moveDocumentToFolder` was
  implemented but unreachable from the UI, so folder chips could only ever filter
  seeded data. Both dashboard card menus have *Move to folder…* +
  `MoveToFolderDialog`.
- **Notes are editable.** `Document.notes` rendered in a banner but nothing could
  set it. The rename dialog is now *Edit document* (title + notes).
- **Duplicate back-stack key removed.** Adding a page to an existing document
  produced `[…, DocumentDetailNav(d), DocumentDetailNav(d)]`; the camera's finish
  handler now reaches the review screen and `addDetailIfNeeded()` prevents a
  second entry with the same key.
- `versionCode 1 → 2`, `versionName 1.0 → 1.1` so a device can be checked for the
  right build.

---

## 2026-10-25 — Antigravity — Build verified & daemon memory configured

- Verified compilation and built `app-debug.apk` (`./gradlew assembleDebug` SUCCESS in 1m 17s).
- Added `kotlin.daemon.jvmargs=-Xmx2048m` and bounded Gradle daemon metaspace to 512m in `gradle.properties` to prevent JVM native allocation failure during Compose + Kotlin 2.3.20 compilation on Windows.
- All new dependencies (CameraX 1.4.2, kotlinx-serialization-json 1.9.0) resolved and compiled cleanly without version bumps needed.

## 2026-10-25 — Arena.ai — "Make it real" pass


**Why:** the app was a high-fidelity UI shell. No code path ever read, wrote or
displayed a real image. Camera, PDF pages, thumbnails and the viewer were all
painted placeholders, and nothing survived process death. Details are in the
Arena audit; this entry records what actually moved.

### New dependencies
- `androidx.camera:camera-core / camera-camera2 / camera-lifecycle / camera-view:1.4.2`
  → real capture in `ui/camera/ScannerCameraScreen.kt`.
- `org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0` → explicit now that
  `LibraryStore` serialises the library itself.
- `androidx.exifinterface` **not** added: `android.media.ExifInterface(InputStream)`
  covers API 24+, and minSdk is now 26.

### New files
| File | Purpose |
| :--- | :--- |
| `util/ImageStore.kt` | Canonical file layout: `filesDir/pages/<pageId>.jpg`, `filesDir/signatures/<id>.png`, `cacheDir/exports/`. Import, orient, downscale, delete. |
| `util/PageImageLoader.kt` | Sampled bitmap decode + `LruCache` + `rememberPageImage()` composable. Cache key includes file mtime. |
| `util/PerspectiveWarp.kt` | Real 4-point projective warp via `Matrix.setPolyToPoly`. Replaces the decorative crop quad. |
| `data/LibraryStore.kt` | `library.json` snapshot persistence, atomic tmp+rename, 150 ms coalescing writer. |
| `ui/common/PageThumbnail.kt` | Shared thumbnail composable with icon fallback. |
| `app/proguard-rules.pro` | **Was referenced by `app/build.gradle.kts` but missing — `assembleRelease` could not run.** |
| `app/src/main/res/xml/file_paths.xml` | Backs the new `FileProvider`. |
| `docs/AGENT_COORDINATION.md` | The Arena ⟷ Antigravity contract. |

### Signature changes (update your call sites)
| Symbol | Before | After |
| :--- | :--- | :--- |
| `ScannerRepository` | `ScannerRepository()` + `ScannerRepository.instance` | **`ScannerRepository(context: Context)`** — the `instance` singleton is **gone** |
| `ScannerRepository.updatePage` | `(docId, pageId, update)` | unchanged |
| `ScannerRepository.movePage` | — | **new** `(docId: String, fromIndex: Int, toIndex: Int)` |
| `ImageProcessingUtils.applyEnhancements` | `(src, filterType, rotationDegrees)` | **`(src, filterType, rotationDegrees, corners: QuadCorners? = null)`** |
| `PdfExportConfig` | had `qualityPercent`, `password` | **both removed** — `PdfDocument` cannot encrypt, and nothing consumed either |
| `SignaturePadDialog.onSignatureConfirmed` | received `"signature_<ts>"` token | receives a **PNG absolute path** |
| `MainNavigation()` | no params | **`MainNavigation(repository: ScannerRepository)`** |
| `PaperSize` | had `RECEIPT`, `BUSINESS_CARD(240,144)` | **added `EXECUTIVE(522,756)`**, `BUSINESS_CARD` corrected to `241×153`, `RECEIPT` kept → 8 presets |
| `QuadCorners()` | defaulted to a 5 % inset | defaults to **full frame** `(0,0)-(1,1)`; the old inset silently cropped every page |

### Deleted files
| File | Why | To restore |
| :--- | :--- | :--- |
| `ui/main/MainScreen.kt` | Unreachable Android-studio starter template. `MainActivity` → `MainNavigation` never referenced it. | `git log --diff-filter=D -- ui/main/MainScreen.kt` |
| `ui/main/MainScreenViewModel.kt` | Same; also introduced the forbidden `DataRepository` singleton pattern. | " |
| `data/DataRepository.kt` | Same. A generic `Flow<List<String>>` unrelated to scanned documents. | " |
| `app/src/test/.../MainScreenViewModelTest.kt` | Tested the deleted template. | " |
| `app/src/androidTest/.../MainScreenTest.kt` | Tested the deleted template. | " |

> The repo currently has **no automated tests**. The next quality task is a real
> unit-test suite for `LibraryStore` round-tripping and `ScannerRepository`
> mutations. Don't let this stay empty.

### Behaviour changes
- `ScannerRepository` starts **empty** instead of seeding three fake documents
  (`mock_contract_p1` etc.). The existing `EmptyStateView` is now reachable and
  correct. Default folders (Work / Tax / Personal) are still created on a fresh
  install.
- Editing a page is now **non-destructive**: the pristine JPEG stays in
  `ImageStore.pageFile()`, the bake lands in `<pageId>_edited.jpg`, and
  `originalImageUri` preserves the source so re-editing never compounds quality
  loss.
- PDF export runs on `Dispatchers.IO` and embeds **real page bitmaps** through
  `ImageStore`. `createSamplePageBitmap()` is gone.
- `FileProvider` is declared in the manifest, so sharing actually works instead
  of always falling into the `catch` Toast.
- `DocScannerTheme(dynamicColor = false)` by default — the Material 3 Expressive
  palette in `theme/Color.kt` is now actually used on API 31+.

### Documentation corrected to match code
- minSdk **24 → 26** (code + docs now agree, docs were right).
- Kotlin documented as **2.3.20** (`libs.versions.toml` is the single source of
  truth; `AGENTS.md` and `README.md` said 2.2.0).
- "Page reordering: Live on Device" was false — now actually implemented.
- Paper-size table in `README.md` now lists all 8 presets with real dimensions.

### Coordination notes for Antigravity
- `ScannerRepository` is constructed in `MainActivity` and passed to
  `MainNavigation(repository)`. **Do not add a static/singleton accessor back** —
  `AGENTS.md` §2.6 forbids it and it is what made the old code untestable.
- Any new screen must accept `repository: ScannerRepository` as a parameter.
- If `assembleDebug` fails on a CameraX or serialization version, bump the
  version in `gradle/libs.versions.toml` only — Arena guessed those version
  numbers because it has no Android SDK and could not resolve them.
