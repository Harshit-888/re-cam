# Agent Coordination Protocol — Arena.ai ⟷ Antigravity

> **Two AI agents build this app in parallel.** Arena.ai works through Git on the
> `arena/*` branch; Antigravity works directly in the local IDE. They share one
> working tree. This document is the contract that keeps their edits from
> colliding. Read it before you touch a file.

---

## 1. The three rules

1. **Claim before you edit.** Add a row to §4 *In-flight changes* naming the
   files you are about to touch, **before** you touch them. Remove the row when
   your change is committed or abandoned.
2. **Never reformat a file you did not claim.** No "clean up imports across the
   project", no `ktfmt`/`ktlint` sweeps, no whitespace-only commits. Reformatting
   makes every other agent's diff conflict.
3. **Additive beats destructive.** New file > new function > new parameter with a
   default > changed signature > renamed symbol > deleted file. Prefer the
   left-hand side of that list. **Deleting or renaming a symbol in §3 requires a
   note in `docs/AGENTS_CHANGELOG.md` first.**

## 2. Branch & handoff mechanics

| | Arena.ai | Antigravity |
| :--- | :--- | :--- |
| Writes to | `arena/a1adc686-re-cam` branch, pushed to `origin` | local working tree |
| Verifies by | code review only — **no JDK / Android SDK in the Arena sandbox** | `./gradlew assembleDebug` + device install |
| Must do before pulling | — | `git stash` or commit local work first |

**Arena cannot compile.** Every Arena change is reviewed-by-eye only. Antigravity
is the build authority: if `./gradlew assembleDebug` fails, that failure is the
source of truth and Arena's change gets fixed, not reverted blindly.

**Antigravity, before you `git pull` Arena's branch:** commit or stash your local
work. If a merge conflict appears in a file, Arena's commit message names the
rationale — read it before resolving.

## 3. Integration contract — symbols other code depends on

These are the shared seams between the two agents. **Do not rename, re-order
parameters of, or change the return type of** anything in this table without
adding a changelog entry and updating every caller in the same commit.

| Symbol | Kind | Semantics |
| :--- | :--- | :--- |
| `ScannerRepository(context)` | ctor | Reads `library.json` on construction. No global singleton. |
| `ScannerRepository.documents` / `.folders` | `StateFlow` | Single source of truth for the whole UI |
| `ScannerRepository.movePage(docId, from, to)` | fun | Page reordering |
| `ScannedPage.imageUri` | field | **Absolute filesystem path** of the display JPEG (may be the edited bake) |
| `ScannedPage.originalImageUri` | field | Pristine original JPEG; set on first bake. `null` = never edited |
| `ImageStore.resolveFile(ctx, imageUri)` | fun | Tolerant resolver (abs path / `file://` / bare filename). `null` for un-imported `content://` |
| `ImageStore.pageFile(ctx, pageId)` | fun | Canonical `filesDir/pages/<pageId>.jpg` |
| `ImageStore.loadBitmap(ctx, imageUri, w, h)` | fun | Sampled decode, orientation-corrected |
| `rememberPageImage(imageUri, w, h)` | `@Composable` | Async decode → `ImageBitmap?` |
| `PageThumbnail(page, modifier)` | `@Composable` | Standard page preview with icon fallback |
| `ImageProcessingUtils.applyEnhancements(src, filter, rotation, corners)` | fun | warp → rotate → filter, in that order |
| `DocumentFilters.apply(src, filter, maxDimension)` | fun | The real filter pipeline. Returns `src` for ORIGINAL/failure — compare identity before recycling |
| `DocumentFilters.applyPreview(src, filter)` | fun | Same pipeline at preview resolution (WYSIWYG) |
| `DocumentDetector.detect(bitmap)` | fun | **Preferred.** `Detection(corners, confident)` — boundary-scan + RANSAC; never throws |
| `DocumentDetector.detectQuad(bitmap)` | fun | Compatibility shim for `detect().corners` |
| `DocumentFilters.stretchPoints(hist)` | fun | Clamped black/white points; the blow-out guard |
| `PerspectiveWarp.detectQuad(bitmap)` | fun | Thin delegate to `DocumentDetector` (kept for compatibility) |
| `PerspectiveWarp.warp(src, corners)` | fun | `Matrix.setPolyToPoly` projective warp |
| `PdfGenerator.generatePdf(ctx, doc, config, bitmapProvider)` | fun | Blocking; **call from `Dispatchers.IO` only** |
| `SignaturePadDialog(onDismiss, onSignatureConfirmed)` | `@Composable` | Callback receives a **PNG path**, not an opaque token |
| `CropAndFilterScreen(docId, pageId, repository, onDone, reviewMode)` | `@Composable` | `reviewMode = true` opens on **Borders** with auto-detection and walks the document page by page |
| `DocumentNaming.defaultScanTitle()` | fun | **The only** sanctioned source of scan titles — it quotes pattern literals |
| `QuadCorners.FULL_FRAME` | const | Means "no warp". Auto-detect falls back to it rather than cropping |
| ~~`ImageProcessingUtils.getColorMatrix`~~ | **removed** | Was a global `ColorMatrix`; superseded by `DocumentFilters` |
| ~~`ImageProcessingUtils.getComposeColorFilter`~~ | **removed** | Editor now previews a real filtered bitmap |

## 4. In-flight changes

Keep this table short. Delete your row when the change lands.

| Agent | Status | Files | Intent |
| :--- | :--- | :--- | :--- |
| Antigravity | ✅ merged | `gradle.properties`, docs | Build verified; Kotlin daemon memory |
| Arena.ai | ✅ committed | `util/DocumentNaming.kt`, `ui/editor/CropAndFilterScreen.kt`, `Navigation.kt`, `NavigationKeys.kt`, `ui/dashboard/DashboardScreen.kt`, `ui/detail/DocumentDetailScreen.kt`, `ui/camera/ScannerCameraScreen.kt`, `util/PerspectiveWarp.kt`, `app/build.gradle.kts`, tests + docs | Done-button crash fix, post-capture review flow, folder move, note editing |

## 5. File ownership map

Rebuilt 2026-10-25 after the "make it real" pass.

| Area | Files | Notes |
| :--- | :--- | :--- |
| App shell | `MainActivity.kt`, `Navigation.kt`, `NavigationKeys.kt` | Repository is created in `MainActivity` and passed down — **do not re-introduce a singleton** |
| Domain | `model/Models.kt` | `@Serializable`; changes need a `schemaVersion` bump in `LibraryStore` |
| Data | `data/ScannerRepository.kt`, `data/LibraryStore.kt` | All mutation goes through the repository, which persists |
| Core utils | `util/ImageStore.kt`, `util/PageImageLoader.kt`, `util/PerspectiveWarp.kt`, `util/ImageProcessingUtils.kt`, `util/PdfGenerator.kt` | Pure/stateless; safe to call from any dispatcher except noted |
| Shared UI | `ui/common/PageThumbnail.kt` | Use this instead of writing a new thumbnail loader |
| Screens | `ui/<feature>/*Screen.kt` | Screens receive `repository` as a **parameter**, never construct it |

## 6. Where new features go

`AGENTS.md` §4 lists the roadmap. When Antigravity or Arena picks one up:

1. Claim it in §4 above.
2. Put the file exactly where `AGENTS.md` §5 says it goes
   (`util/OcrManager.kt`, `ui/editor/CleanupBgCanvas.kt`, `util/NamingTemplateEngine.kt`,
   `util/BackupRestoreManager.kt`).
3. Append one line to `docs/AGENTS_CHANGELOG.md`.

## 7. Known-good state

Last verified build: **Verified by Antigravity** on 2026-10-25 (`./gradlew assembleDebug testDebugUnitTest` passed).
Document filters + edge quad detection verified. APK built successfully at `app/build/outputs/apk/debug/app-debug.apk` (versionCode 3, versionName 1.2).


