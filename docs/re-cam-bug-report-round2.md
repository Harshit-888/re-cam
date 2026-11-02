# re-cam — Bug Report, Round 2 (static code inspection, no fixes applied)

- **Repository:** `Harshit-888/re-cam`
- **Head inspected:** `3c1e01c` — "fix(perf): address Arena H004 review with Round 70.3 corrective changes & device verification" (main, 2026-11-01)
- **App:** Android / Kotlin, single-activity Jetpack Compose document scanner (`com.example.docscanner`)
- **Scope inspected:** `app/src/main/java` (69 main Kotlin files, ~31,357 lines), plus `AndroidManifest.xml` and the test tree for coverage gaps.
- **Method:** Static reading only. **No code was changed, no bug was fixed, and nothing was pushed to the repository.** This file was created locally for verification with Arena (and then Antigravity for device verification).
- **What this is NOT:** Not a device test, a build run, or a crash log. Items marked "needs device verification" are code-confirmed in mechanism but need a device run for the end-user symptom. Items marked "confirmed from code" follow directly from the code path.
- **Relation to Round 1:** This is a *new-bugs-only* report. It deliberately does **not** re-report the B1–B23 list from `re-cam-bug-report.md` (2026-10-30, head `087cf89`) or the Arena Round 70.2 / 70.3 findings (PageToken incompleteness, CAS publication flags, copy fallback to pristine pixels, benchmark denial untested, fixture SHA-256 missing, manual OCR tests, §15/§11 evidence missing). Absence from this file does **not** mean an old item is fixed — it means it was already reported elsewhere. One item was checked and *dropped* after cross-checking: a claimed "edits during backup restore are silently dropped" race does not hold at `3c1e01c`, because `importBackup()` takes its local snapshot *after* the (slow) restore extraction, refuses a restore whose documents overlap local IDs, and publishes the merged folders/documents back-to-back with no suspension point in between (see "Checked and not reported").

## How to use this file with Arena

For each bug below, give Arena: (1) the file + function, (2) the "Evidence" quote/paraphrase, and (3) the "Verify" step. Ask it to confirm or refute that exact path and classify it blocking vs non-blocking — not to re-audit the whole repo from scratch. A bug is only closed when Arena can point to the line that prevents it, or to a device reproduction that fails. Antigravity then runs the device verifications; no merge or release claim should rest on this static review alone.

## Severity key

- **P1 — Wrong output / lost work / deleted content reappearing:** the app reports success but the result is wrong, incomplete, or a deletion is undone.
- **P2 — Reliability / privacy / performance:** works most of the time, fails or leaks in an edge case, or is unguarded by tests.

---

## P1 — Wrong output / lost work

### C1. Search resurrects trashed documents: deleted scans reappear, openable, shareable, mergeable
- **Files:** `ui/dashboard/DashboardScreen.kt`, `DashboardScreen()` — `activeDocs` (lines ~376–390); search index built at lines ~313–316.
- **Evidence:** The non-search path filters trash, the search path does not: `if (!queryActive) documents.filterNot { it.isTrash } else searchHits.map { it.document }`, with no `isTrash` filter afterwards (only a `folderId` filter). The index is built from the unfiltered `documents` list, so trashed docs are indexed and ranked like live ones. Selection is pruned to `activeDocs`, so a trashed hit can also be multi-selected and shared/merged.
- **Impact:** A user who deletes a document sees it back the moment they search for its title or OCR words, can open it, and can export it. Deletion looks broken, and deleted content is re-exposed.
- **Verify (device):** Create a doc titled e.g. "TaxTest", move it to trash, then search "TaxTest" on the dashboard. It must not appear. Also try long-press → merge/share from the search result.
- **Confidence:** Confirmed from code.

### C2. Warp failure silently saves the unwarped page as a successful crop
- **Files:** `util/PerspectiveWarp.kt`, `warp()`; consumed by `util/ImageProcessingUtils.kt`, `applyEnhancements()`; saved by `util/PageRenderer.kt`, `bakeBlocking()`.
- **Evidence:** `warp()` returns the input bitmap unchanged when an output edge is below `MIN_OUTPUT_EDGE`, when the quad area is too small, when `matrix.setPolyToPoly(...)` fails, or when the output allocation throws `OutOfMemoryError`. `applyEnhancements()` has no flag distinguishing "warped" from "warp gave up and handed back the input", and `bakeBlocking()` then runs rotate/filter/signature/markup on it and writes it via `saveEditedPage()`, returning a page record that still carries the user's `corners`.
- **Impact:** A degenerate/crossed/near-collinear quad (e.g. from an older library, or a sliver the handle clamps allow) or a warp-allocation OOM produces a saved file with **no crop at all**, while the record claims the crop. In a batch bake there is no preview for the non-active pages, so the user discovers it only later — and re-baking with the same corners fails the same way, so the crop can never be applied. This is a *different* silent-downgrade path from the removed 3200→2400 step-down, which is now fail-fast in `renderEnhanced`.
- **Verify:** Unit test — call `PerspectiveWarp.warp()` on a bitmap with a quad whose edge is <16 px, or a crossed quad that makes `setPolyToPoly` fail, and assert the result is `=== src` while the caller would still save it. Device — force low memory during a 3200 px warp and check the saved file is unwarped despite `corners` being set.
- **Confidence:** Confirmed from code (degenerate/`setPolyToPoly` paths); OOM trigger needs a device/low-memory run.

### C3. Markup does not rotate with the page, so ink drawn before a rotation detaches from the text
- **Files:** All rotate paths + `util/PageRenderer.kt`, `bakeBlocking()`; `util/MarkupRenderer.kt`, `draw()`/`pathFor()`.
- **Evidence:** Rotate bakes `latest.copy(rotationDegrees = (latest.rotationDegrees + 90f) % 360f)` and nothing else (viewer, detail `bakeRotated()`), and editor rotate buttons only change the `rotation` state persisted verbatim. The bake pipeline is warp → rotate → filter, and `MarkupRenderer.draw(result, page.markup)` then replays the stored strokes unchanged onto the dimension-swapped output bitmap. Markup points are normalised fractions (`point.x * width`, `point.y * height`) with no rotation transform applied.
- **Impact:** A stroke stored at (0.1, 0.1) stays at (0.1, 0.1) after a 90° rotation, while the document content that was top-left is now top-right — the ink sits over different text. The signature does not have this problem (composited after rotation, at the new bottom-right), so a signed-and-annotated page comes back with the signature in the right place and the ink in the wrong place. Every re-rotation compounds the mismatch, and the pre-rotation placement cannot be recovered because the points were never transformed. (Ink drawn *after* a rotation, in the rotated frame, is not affected in the same way — the bug is specifically pre-rotation ink.)
- **Verify (device):** Annotate a distinctive corner/word on a page, rotate it once in the viewer, and compare ink position against the same word before and after.
- **Confidence:** Confirmed from code.

### C4. Copy variant (different from the known one): a missing pristine file makes edited pixels masquerade as the copy's pristine capture
- **Files:** `util/ImageStore.kt`, `copyPageFiles()`.
- **Evidence:** `val pristine = (source.originalImageUri?.let { resolveFile(context, it) } ?: resolveFile(context, source.imageUri)) ?: return null`. If the pristine file is gone, the *edited* file is silently adopted as "pristine" and copied to `pageFile(newPageId)`. `isEdited` is still true (the image URI contains `_edited`), so the same edited file is also copied as the new page's edited bake, with `newOriginalUri = pristineTarget`. The copy's record carries `filterType`, `corners`, `rotationDegrees`, `markup` and `signatureOverlayUri` verbatim.
- **Impact:** The copy's record claims a pristine original, but that file already has the warp, rotation and filter baked in. Re-editing the copy re-applies those same corners/rotation/filter *on top of already-transformed pixels* — double crop, double rotation, double filter — while the UI presents it as a clean non-destructive copy. This is the mirror image of the already-known regression (edited file missing → pristine pixels shown with edited metadata); here edited pixels are labelled pristine. Arena's branch copy (refuse missing files) would close both, but on `main` this variant is still live.
- **Verify:** Edit a page (crop + MAGIC_COLOR), delete only its pristine file, duplicate the page, then re-bake the copy with a different filter and confirm the output is filtered-twice / cropped-twice versus a copy made while the pristine file existed.
- **Confidence:** Confirmed from code.

### C5. Auto border pass writes corners *after* a batch bake, leaving record and pixels diverged
- **Files:** `ui/editor/CropAndFilterScreen.kt`, review-mode auto-detect `LaunchedEffect` + `applyFilterToPages()`.
- **Evidence:** The review-mode pass detects up to 40 untouched pages in the background; at the end it computes `stillUntouched` (pages whose repo `corners.isFullFrame`) and calls `repository.setPageCorners(...)`. `applyFilterToPages()` calls `persistGeometry()` only for the active page, then bakes each page from its repo state. A bake does **not** change `corners` (`PageRenderer` returns the input page's corners unchanged), so a page baked full-frame still qualifies as "still untouched". Nothing gates Save on the pass finishing: the borders-stage "Skip" button and the stage switch move straight to filters while the pass is still running.
- **Impact:** Bake 10 pages full-frame, the pass finishes a second later and writes detected quads into their records — now the record says "cropped" but the file is uncropped. The next filter change (or any re-bake) suddenly crops the page, with no user action on borders in between. The same pattern exists in miniature for the single-page load detect if the user saves before detection returns and the exit-save then persists the late detection.
- **Verify (device):** Review-mode, 8–10 pages, tap Skip to filters immediately and Save all, wait for the pass to finish, reopen the editor and confirm corners changed without pixels changing; then change the filter and watch the page crop.
- **Confidence:** Confirmed from code; timing window needs a device run (wider with many pages / a slow device).

### C6. Print ignores the export orientation, so a landscape PDF is printed on portrait media (and vice versa)
- **Files:** `util/PdfPrinter.kt`, `PdfPrinter.print()`; called from `ui/pdf/PdfExportSheet.kt` (~line 642).
- **Evidence:** `print(context, file, jobName, paperSize)` builds `PrintAttributes.MediaSize` directly from `paperSize.widthPt` / `paperSize.heightPt`, with no orientation parameter and no swap. The print call passes only `paperSize = selectedPaperSize` — the sheet's orientation choice is never passed. Meanwhile `PdfGenerator.pdfPageGeometry()` swaps paper width/height for `PageOrientation.LANDSCAPE`, so the PDF pages themselves are landscape while the print media stays in the preset's native orientation. The reverse also holds: a natively-landscape preset (e.g. TABLOID 1224×792, BUSINESS_CARD 243×153) printed from a portrait export gets landscape media.
- **Impact:** Printed output is rotated / scaled / clipped against the wrong media size, even though the on-screen PDF is correct. The user has no control in the print path to fix it.
- **Verify (device):** Export the same document as A4 portrait and as A4 landscape, print each via "Print instead", and compare the system print preview / printed page orientation and scaling. (Per-page `AUTO` mixed orientation cannot be represented by one media size — the fix target is the explicit portrait/landscape setting, not `AUTO`.)
- **Confidence:** Confirmed from code.

### C7. Multi-document share silently drops empty or failed documents, and the caller treats any partial success as full success
- **Files:** `util/DocumentShare.kt`, `shareDocuments()`; caller `ui/dashboard/DashboardScreen.kt` (~line 843).
- **Evidence:** `if (document.pages.isEmpty()) return@forEach` — skipped with no count, log, or error. `runCatching { PdfGenerator.generatePdf(...) }.getOrNull()`; only `if (written != null) files.add(written)` — a failed generation is swallowed the same way. The function returns `files.size`, and the dashboard caller only distinguishes `shared == 0` ("Nothing to share") from `shared > 0`, in which case it just ends selection. There is no "shared 2 of 3" path.
- **Impact:** Select 3 documents, one empty or one whose PDF generation throws, and the chooser shows 2 attachments with no warning that the third never left the phone. This is exactly the "reports success with missing content" class.
- **Verify:** Share a selection containing one empty document and one normal document, and separately one document whose page file is missing; assert the UI reports the skipped/failed document instead of silently ending selection.
- **Confidence:** Confirmed from code.

### C8. Password characters outside printable ASCII are silently stripped, so the set password is not the typed password
- **Files:** `ui/pdf/PdfExportSheet.kt`, password field `onValueChange` (~line 490); crypto in `util/PdfSecurity.kt`, `padPassword()`.
- **Evidence:** `password = typed.filter { it.code in 32..126 }.take(64)` — every non-ASCII character is dropped in place, with no warning, rejection, or display of the effective password. `PdfSecurity` then hashes whatever survived. This is *distinct* from the already-known >32-character truncation (same file, the `.take(64)` plus `PdfSecurity`'s 32-byte truncation): that is a length cut; this is character-set loss that changes even a short password.
- **Impact:** A user typing e.g. `pässwörd123` actually protects the PDF with `psswrd123`. The recipient using the password as told to them cannot open the file, and the sender has no way to tell from the sheet that mangling happened. A fully non-ASCII password is blocked (it filters to blank, so `passwordReady` fails), but a mixed password is silently accepted in mangled form.
- **Verify:** Set a password containing an accented/non-ASCII character, export, then try to open the PDF with the typed password (should fail) and with the ASCII-stripped password (should succeed) — and check that the sheet never warned.
- **Confidence:** Confirmed from code.

### C9. `library.json` from a newer app version is silently downgraded on the next write
- **Files:** `data/LibraryStore.kt`, `load()`; `CURRENT_SCHEMA_VERSION = 1`; `model/Models.kt`, `LibrarySnapshot` ("Bump `schemaVersion` on any breaking model change and handle the migration in `LibraryStore`").
- **Evidence:** `load()` decodes with `ignoreUnknownKeys = true` and never inspects `schemaVersion` at all. The backup path *does* guard this — `BackupArchive.restore()` refuses `archivedSnapshot.schemaVersion > CURRENT_SCHEMA_VERSION` — but the main library load has no equivalent check. So a `library.json` written by a newer build (new fields, renamed semantics) loads "successfully" with the new fields dropped, and the next debounced write re-serialises only the current model, permanently destroying the newer data.
- **Impact:** Downgrading the app (or restoring a `library.json` from a newer install by hand) can silently strip fields the user cannot get back, with no warning and no backup copy made first.
- **Verify (unit test):** Write a `library.json` with `schemaVersion: 99` plus an unknown field, load it through `LibraryStore`, trigger a write, and confirm whether the unknown field survives or is silently dropped.
- **Confidence:** Confirmed from code.

### C10. Exporting over an existing backup destroys the old backup before the new one is known good
- **Files:** `data/ScannerRepository.kt`, `exportBackup()` (lines ~1174–1185).
- **Evidence:** `val out = appContext.contentResolver.openOutputStream(uri)` with no mode truncates the target first; only then does `BackupArchive.create()` stream the library into it. If creation throws midway (disk full, a page file vanishing mid-read, SAF failure), the user is left with a partial zip at that URI and the previous good backup is already gone. `SettingsScreen` does toast "Backup failed" in that case, so this is not a false success — it is destruction of the only good copy.
- **Impact:** The exact moment a backup fails is the moment the user most needs the previous backup, and it has just been overwritten.
- **Verify (device):** Export to a URI that already holds a valid backup, force a mid-export failure (e.g. fill storage / remove a page file mid-write), and inspect what remains at that URI.
- **Confidence:** Confirmed from code path plus standard `ContentResolver` truncate semantics.

### C11. Signature dots vanish: a dot-only (or dotted) signature saves as a blank PNG, reported as success
- **Files:** `ui/signature/SignaturePadDialog.kt`, `rasterise()` and the preview `drawSmooth()`.
- **Evidence:** `rasterise()` only draws strokes with `points.size > 1`. Preview `drawSmooth()` also returns early for `points.size < 2`. Apply is enabled on `strokes.isNotEmpty()`, so a single tap *counts* as a signature for enabling the button, then is dropped at rasterise time. The resulting transparent PNG is saved via `ImageStore.saveSignature()` and the caller bakes it as a normal signature record.
- **Impact:** A signature that is only a dot, and — more commonly — the dot over an "i", a full stop, or a dot accent inside an otherwise normal signature, is silently erased. The page record now carries a `signatureOverlayUri` pointing at a blank image, so the document looks unsigned (or partly unsigned) while the user believes they signed it. Contrast `MarkupRenderer.pathFor()`, which deliberately renders a single point as a dot.
- **Verify (device):** Sign with a single dot, Apply, open the page and the saved signature PNG, and confirm both are blank. Unit test — feed a one-point stroke list through the rasterise path and assert the output has no non-transparent pixels.
- **Confidence:** Confirmed from code.

### C12. "N pages copied" toast even when the copies never land in the library (concurrent-delete race)
- **Files:** `data/ScannerRepository.kt`, `duplicatePages()` (lines ~533–571); caller `ui/detail/DocumentDetailScreen.kt`, `copyPages()` (lines ~437–452).
- **Evidence:** `duplicatePages()` snapshots the document, then does slow IO (`ImageStore.copyPageFiles` per page, on `Dispatchers.IO` from the caller), then inserts with a plain `_documents.update`. If the document was deleted in that window, the `map` matches nothing and the copies are dropped — yet the function still returns `copies.map { it.id }`, and the caller toasts `"N pages copied"` (only an empty return produces "Could not copy those pages"). If all selected pages were deleted mid-copy, `lastSelected` is `-1`, the `index == lastSelected` branch never fires, and the copies are again dropped while still reported as success. If the document was trashed mid-copy, the copies land inside the trashed document and are invisible on the dashboard.
- **Impact:** User believes pages were duplicated; they are either gone (files left as orphans) or hidden in the bin. No error is shown in any of these cases.
- **Verify (device):** Select several pages, tap copy, immediately navigate back and trash (or delete) the document before the copy finishes; check the toast against what is actually in the library.
- **Confidence:** Mechanism confirmed from code; the window is narrow (it needs a concurrent delete during the copy IO), so this is a race, not an every-time failure.

---

## P2 — Reliability / privacy / performance

### C13. Multi-PDF share sets no `ClipData`, so URI permission for all attachments is not reliably granted
- **Files:** `util/DocumentShare.kt`, `share()` (~lines 97–116).
- **Evidence:** The `ACTION_SEND_MULTIPLE` intent puts all URIs only in `EXTRA_STREAM` (`putParcelableArrayListExtra`) and adds `FLAG_GRANT_READ_URI_PERMISSION`, but never calls `setClipData()` with the URIs. The single-file sheet path does not have this exposure, since a single `EXTRA_STREAM` URI is the well-supported case.
- **Impact:** On targets that resolve grants from `ClipData` rather than the `EXTRA_STREAM` list, some attachments can fail to open with a permission error while the first opens fine — a share that looks successful but delivers unreadable attachments.
- **Verify (device):** Share 3 documents at once to Gmail and to Drive/Files, and open every attachment from the receiving app; repeat with a target known to enforce `ClipData` grants. A fix should set `ClipData` containing all URIs in addition to the flag.
- **Confidence:** Mechanism confirmed from code; recipient behaviour needs device verification (it is target-dependent).

### C14. Search exclusion (`-word`) is AND, not OR, and matches substrings, not words
- **Files:** `util/DocumentSearch.kt`, `Index.search()` line ~450 and `documentContainsAll()` lines ~568–576.
- **Evidence:** `candidates.removeAll { documentContainsAll(it, excluded) }`, and `documentContainsAll` returns `stems.all { haystack.contains(it) }` — a raw substring test on un-tokenised, un-stemmed title+page+tag text, requiring **every** excluded stem. The comment above line 450 says `-draft` "removes a document that contains the excluded word *anywhere*, which is the only reading that matches what people mean by it" — but the code removes a document only when it contains every excluded stem.
- **Impact:** `invoice -draft -old` keeps a document containing only "draft" and one containing only "old". Conversely `-pan` wrongly removes documents containing "company", "span", or "pancard"; `-art` removes "part"/"article"/"heart". Users cannot trust exclusion to hide things.
- **Verify (unit test, no device needed):** Index docs A ("invoice draft"), B ("invoice old"), C ("invoice company"), D ("invoice tax"). Search `invoice -draft -old`: expect only C, D — A and B survive today. Search `invoice -pan`: expect C survives — today it is removed.
- **Confidence:** Confirmed from code.

### C15. Fuzzy and synonym search hits lose their snippet (Magic Search's headline feature silently degrades to a date label)
- **Files:** `util/DocumentSearch.kt`, `highlightNeedles()` line ~589 and `snippet()` lines ~608+; `ui/dashboard/DashboardScreen.kt`, `hitsByDocument` lines ~352–354.
- **Evidence:** Snippet needles are only the raw typed terms plus their stems, and `snippet()` finds them with `lowered.indexOf(needle)`. A fuzzy hit ("invoce" → page says "invoice") or a synonym hit ("aadhar" → page says "aadhaar") has **no** needle that occurs in the page text, so `snippet()` returns `null` for every page, and `hitsByDocument` drops the hit (`mapNotNull { hit.snippet?.let … }`). The card then falls back to the date label. Prefix hits work (the prefix is a substring); typo/synonym hits — the two "magic" cases — do not.
- **Impact:** Exactly the searches marketed as magic show the least explanation: a bare thumbnail and date, with no visible reason the document matched.
- **Verify (device or unit test):** Page containing "invoice", search "invoce" (1-edit typo). Expect: result with a snippet highlighting "invoice". Currently: result with no snippet. Repeat with "aadhar" against a page saying "aadhaar".
- **Confidence:** Confirmed from code.

### C16. OCR retry starvation: failed pages never get a retry slot while new untried pages keep arriving (remaining fairness hole)
- **Files:** `data/ScannerRepository.kt`, `backfillTextInBackground()` lines ~993–995.
- **Evidence:** `val untried = allCandidates.filter { it.second.ocrFailedAttempts == 0 }`, `val failed = allCandidates.filter { it.second.ocrFailedAttempts in 1..2 }`, `val pending = (untried + failed).take(limit)` with `limit = OCR_BACKFILL_BATCH = 8`. Retries are strictly behind **all** untried pages. The old starvation (blank front pages blocking later pages) is fixed, but this is its mirror image: a page that failed transiently (e.g. ML Kit not ready on first launch, attempts = 1–2, `ocrAttempted = false`) gets zero slots on every launch as long as the library has ≥8 untried pages — which is true whenever the user keeps scanning. The single-doc `indexTextInBackground()` path does not cap, so only the dashboard backfill starves.
- **Impact:** Transiently-failed pages stay unsearchable indefinitely on an actively-growing library, with no user-visible signal; the bounded-retry design (max 3) never gets to run.
- **Verify (device):** Force one OCR failure (e.g. first-launch model-not-ready, or a temporarily unreadable file later replaced), then add 8+ new unscanned pages and relaunch repeatedly. Expect: the failed page is retried within a few launches. Currently: it is never selected while untried pages exist.
- **Confidence:** Confirmed from code (queue order); the "model-not-ready" trigger itself needs a device test.

### C17. OCR is Latin-script only, so non-Latin pages are permanently marked "no text" and never retried
- **Files:** `util/OcrEngine.kt`, `recognizer` line ~56 and `readPageResult()` line ~76; marking in `ScannerRepository.kt` `setPageOcrTexts` (`NoText -> ocrAttempted = true`).
- **Evidence:** `TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)` — ML Kit's Latin recognizer; no Devanagari/Chinese/Japanese/Korean recognizer is ever constructed anywhere in `app/src/main/java` (grep for `TextRecognizerOptions` hits only this file). A Devanagari page therefore decodes and recognises to empty text → `OcrResult.NoText` → `ocrAttempted = true`, and backfill/index skip it forever (only a re-bake resets it). Meanwhile the search side assumes non-Latin words exist: `NativeSearch.selfTest()` vocabulary includes "कर".
- **Impact:** Hindi/Devanagari scans (Aadhaar-type documents, the app's own synonym examples) are silently unsearchable, with no indication that OCR even attempted a script it cannot read.
- **Verify (device):** Scan a clean Hindi/Devanagari page, wait for backfill, check its `ocrText` is empty and a search for a word on the page finds nothing; relaunch and confirm it is never retried.
- **Confidence:** Confirmed from code for the recognizer choice and the attempted-marking; ML Kit's exact behaviour on Devanagari input needs the device test.

### C18. "Auto-save to Gallery" silently does nothing for every import path except in-app camera capture
- **Files:** `ui/dashboard/DashboardScreen.kt`, `systemPickerLauncher` lines ~191–212; `ui/camera/ScannerCameraScreen.kt`, gallery import lines ~397–430; `ui/detail/DocumentDetailScreen.kt`, gallery import lines ~231–250. Sole `saveToGallery` call site: `ScannerCameraScreen.kt:573` (inside the `OnImageSaved` capture callback only).
- **Evidence:** Repo-wide grep shows `GalleryExporter.saveToGallery` is called exactly once — in the camera-capture path, guarded by `repository.currentSettings().autoSaveToGallery`. The dashboard picker path (used when "Use in-app camera" is off — the setting's own companion path) builds pages via `importPickedImage()` and creates the document with no gallery step; the camera screen's own gallery button and the detail screen's "add page from album" path likewise never call it. The settings row promises "Keep a copy of every captured page in Pictures/DocScanner".
- **Impact:** A user who enables auto-save and then imports from the gallery gets no `Pictures/DocScanner` copies and no warning — the backup they think they have does not exist.
- **Verify (device):** Enable Settings → Auto-save to Gallery, then (a) turn off in-app camera and import via the picker, (b) import from the gallery button in the camera screen, (c) add a page from the detail screen. Check `Pictures/DocScanner` after each: currently empty for all three; camera capture is the only path that writes.
- **Confidence:** Confirmed from code.

### C19. "Delete permanently" does not delete a page's older baked versions until the next cold start
- **Files:** `util/ImageStore.kt`, `deletePageFiles()` (lines ~552–570); `data/ScannerRepository.kt`, orphan-sweep launch (repository `init`, lines ~180–190).
- **Evidence:** `deletePageFiles()` deletes only the current `imageUri`, `originalImageUri`, and the three fixed names (`<id>.jpg`, `<id>_edited.jpg`, `<id>_edited.png`). Since Round 70.3, every bake writes a *new versioned* file (`ImageStore.saveEditedPage()`: `<pageId>_edited_<timestamp>_<uuid8>.<ext>`), so a page edited three times has two superseded bakes that `deletePageFiles` never touches. Those are only removed by `sweepOrphans()`, which is launched exactly once, in the repository `init` — never after a delete.
- **Impact:** After "Delete permanently" or "Empty recycle bin", earlier versions of the deleted scans' pixels remain on disk for the rest of the session, and indefinitely if the app is not cold-started again (or if the sweep's 10-minute grace has not elapsed for a recent bake). A user deleting sensitive scans gets a weaker guarantee than the UI promises.
- **Confidence:** Confirmed from code.

### C20. `SettingsStore`'s own rename fallback can corrupt `settings.json` (distinct from the known corrupt-file reset)
- **Files:** `data/SettingsStore.kt`, `writeNow()` (lines ~102–118).
- **Evidence:** Unlike `LibraryStore.writeNow()`, which throws rather than fall back, the settings writer does this on rename failure: `if (!temp.renameTo(target)) { try { temp.copyTo(target, overwrite = true) } finally { temp.delete() } }`. `copyTo(overwrite = true)` truncates and rewrites the live file in place — the exact non-atomic pattern the surrounding comments say was removed because "a kill inside it silently resets the user's theme." A kill, crash, or storage error mid-copy leaves a partial `settings.json`; the next `load()` returns `null` on the decode failure and the defaults are then persisted over it.
- **Impact:** A transient rename failure (the situation the fallback exists for) can turn a settings write into a settings wipe, via the store's own code rather than a pre-existing corrupt file.
- **Verify (unit test):** Force `renameTo` to fail (e.g. a read-only target directory in a JVM test harness) and kill/interrupt mid-copy, then load and check whether settings survive.
- **Confidence:** Confirmed from code; the mid-copy kill itself needs a device/test-harness check.

### C21. Concurrent OCR index + backfill passes duplicate work and burn the bounded-retry budget
- **Files:** `data/ScannerRepository.kt`, `indexTextInBackground()` and `backfillTextInBackground()`; callers `ui/camera/ScannerCameraScreen.kt:448`, `ui/detail/DocumentDetailScreen.kt:267`, `ui/dashboard/DashboardScreen.kt:569`.
- **Evidence:** Both passes select the same pages (`ocrText.isBlank() && !ocrAttempted`) and neither claims them: there is no in-flight set, lock, or coordination between the two. The dashboard backfill only pauses on scroll/touch (`isBusy`), so opening a document while backfill is running starts `indexTextInBackground` for the same pages concurrently. Each pass then runs a full 2400 px decode + ML Kit inference per shared page, and each failed pass applies its own `ocrFailedAttempts + 1` — so one bad session (e.g. ML Kit model not ready at first launch) can consume 2 of the 3 bounded attempts, instead of the 1 the Round 70.3 design promises.
- **Impact:** Wasted CPU/battery in the launch window, and persistently failing pages reaching attempted-final sooner than designed, after which backfill never retries them.
- **Verify (device):** With logging on OCR starts, open a document while the dashboard backfill is mid-batch and check whether the same page is recognised twice; with a failing OCR reader, check the attempt count after one such session.
- **Confidence:** Mechanism confirmed from code; concurrent overlap timing needs a device check.

### C22. Restoring a library with an empty folder list silently injects default folders and strands documents
- **Files:** `data/ScannerRepository.kt`, `_folders` initialisation (lines ~136–138) and the first-emission persistence rule (init block: the first `combine` emission is written only when `restoredSnapshot == null`).
- **Evidence:** `private val _folders = MutableStateFlow(restored?.folders?.takeIf { it.isNotEmpty() } ?: defaultFolders())`. A `library.json` whose `folders` list is empty (older export, hand-edited file, or a backup that carried no folders) is treated exactly like a fresh install for folders — but because `restored != null`, the injected defaults are *not* persisted. Documents whose `folderId` points at a folder that no longer exists then appear only under "All" (dashboard filter: `selectedFolderId == null || doc.folderId == selectedFolderId`) and under no folder chip, with no indication that their folder is gone. And the first later edit persists the invented defaults as if the user had created "Work & Contracts", "Tax & Financials", and "Personal & ID" themselves.
- **Impact:** Quiet reorganisation of the library after a restore: documents seemingly lose their folders, and phantom default folders appear on the next save.
- **Verify (unit test):** Build a repository from a snapshot with `folders = []` and documents carrying a `folderId`, and check the folder list shown and where the documents are reachable.
- **Confidence:** Confirmed from code.

### C23. Signature ink colour and weight are global, not per-stroke: changing them mid-signature rewrites earlier strokes
- **Files:** `ui/signature/SignaturePadDialog.kt`, `rasterise()`.
- **Evidence:** The stroke list stores only point lists (`strokes: List<List<Offset>>`); there is no colour or width per stroke. `rasterise()` builds one `Paint` from the *current* `selectedInkColor` and `strokeWidth` and draws every stroke with it, and the preview does the same — so preview and file agree, but both disagree with what the user actually drew.
- **Impact:** Sign the first half in black thin, switch to red bold for the second half, and the saved signature is entirely red bold. This is a fidelity bug, not a crash: a signature is an identity mark, and silently restyling its first half changes what the user attested. Markup, by contrast, stores tool/colour/width on every `MarkupStroke`.
- **Verify (device):** Draw in black, switch ink to red mid-signature, save, and inspect the PNG: the first strokes are red.
- **Confidence:** Confirmed from code.

### C24. Filter swatches ignore rotation, so they show rotated pages sideways
- **Files:** `ui/editor/CropAndFilterScreen.kt`, filter-thumb `LaunchedEffect` (lines ~528–535).
- **Evidence:** Thumbs are `PerspectiveWarp.warp(small, corners)` then `DocumentFilters.apply(...)` for each filter. No rotation step. The main preview (lines ~580–587) and the save pipeline (`PageRenderer`: warp → rotate → filter) both apply rotation between warp and filter. The comment at line ~527 claims "Warp then filter, exactly as the save does" — that is only true for unrotated pages.
- **Impact:** On any page rotated 90°/270°, every filter swatch is sideways (and at the wrong aspect) while the main preview is upright. The filter *look* is mostly rotation-invariant, so this misleads orientation judgement rather than filter choice, but it breaks the swatch's stated promise ("what the file will look like") and can make a user distrust or mis-pick a filter on a rotated page.
- **Verify (device):** Rotate a page 90° in the editor, open the filters stage, and compare swatch orientation to the main preview.
- **Confidence:** Confirmed from code.

### C25. A deliberate "Full page" choice is discarded on re-entering the editor
- **Files:** `ui/editor/CropAndFilterScreen.kt`, `onFullPage` + page-load auto-detect.
- **Evidence:** "Fit"/Full page sets `corners = QuadCorners.FULL_FRAME` and `cornersEdited = false`. On page load, `if (current.corners.isFullFrame)` runs auto-detection and assigns `corners = detection.corners`.
- **Impact:** `FULL_FRAME` is used for two different meanings — "never cropped" and "user explicitly chose no crop" — and there is no flag distinguishing them. A user who taps Fit, saves, and reopens the editor finds the crop snapped back to the detected quad; if they then save (e.g. only intending a filter change), the page gets cropped after all, undoing their explicit decision. The decision is visible on reopen, so this is lost work rather than silent corruption, but the re-detection is indistinguishable from a fresh page.
- **Verify (device):** Crop a page, tap Fit, save, reopen the editor, and confirm the corners are back to the detected quad rather than full frame.
- **Confidence:** Confirmed from code.

### C26. After a permanent camera-permission denial, the camera screen has no recovery path — the retry button is dead and there is no Settings link
- **Files:** `ui/camera/ScannerCameraScreen.kt`, permission launcher + `CameraPermissionCard`.
- **Evidence:** `permissionLauncher` only updates `hasCameraPermission`; a `LaunchedEffect(Unit)` launches the request once. The permission card's only action is `permissionLauncher.launch(Manifest.permission.CAMERA)` again. A grep of the file finds no `shouldShowRequestPermissionRationale` check and no Settings / `ACTION_APPLICATION_DETAILS_SETTINGS` intent anywhere in the screen.
- **Impact:** Once the user denies with "don't ask again", tapping the card's request button makes the system auto-deny with no visible dialog, the card stays up forever, and the only in-app escape is gallery import. The user is never told they must re-enable the permission in system Settings.
- **Verify (device):** Deny camera permission permanently, reopen the camera screen, tap the permission card's request button, and confirm no dialog appears and no Settings affordance is offered.
- **Confidence:** Confirmed from code (absence of a Settings path); permanent-denial behaviour needs a device check.

### C27. `ThumbnailStore.writeBitmap` uses a fixed temp name and ignores the JPEG compress result — unlike `ImageStore`'s UUID temp + compress check
- **Files:** `util/ThumbnailStore.kt`, `writeBitmap()` (lines ~193–210); contrast `util/ImageStore.kt`, `writeAtomically()` (UUID temp, throws on compress failure).
- **Evidence:** `val temp = File(target.parentFile, target.name + ".tmp")` — fixed per key, so two concurrent writers for the same key share one temp file and can interleave/truncate each other before rename. `scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, stream)` — return value ignored, then `flush()` and `renameTo(target)` publish whatever is in the temp file, including an empty/truncated JPEG on compress failure. `renameTo` failure is handled (temp deleted, `false`), compress failure is not. Repair is then blocked: `PageImageLoader.primeThumbnails()` skips a page when `ThumbnailStore.exists()` is true, and `exists()` is `isFile` only — a 0-byte thumbnail counts as "exists", so repair never rewrites it, while `ThumbnailStore.read()` rejects 0-length files. Every tile for that page falls back to a full page decode instead.
- **Impact:** Wrong behaviour is limited (reads reject empty files, so stale/wrong pixels are not shown), but the cost is a permanently cold tile — full-page decodes on every scroll — plus a corrupt-thumbnail race under concurrent writes.
- **Verify (unit test):** Make `compress` fail (e.g. a recycled source bitmap) and assert `writeBitmap` returns `false` and does not publish a file; and a test where a 0-byte thumbnail file exists and `primeThumbnails` is shown to skip it rather than repair it.
- **Confidence:** Confirmed from code.

### C28. Thumbnail cap/eviction bug that remains after the 600 → 5000 raise: sweep deletes half the directory, not just the excess
- **Files:** `util/ThumbnailStore.kt`, `sweep()` (lines ~221–229); cap `MAX_FILES = 5000` (line ~69).
- **Evidence:** `sweep()` runs on every new thumbnail write; when `files.size > MAX_FILES` it deletes `files.size - MAX_FILES / 2` oldest files — e.g. at 5001 files it deletes 2501 and leaves 2500, instead of deleting 1 to get back under the cap.
- **Impact:** For any library steadily above 5000 pages, the thumbnail store sawtooths between 2500 and 5000: every cycle throws away 2500 valid, in-library thumbnails, each of which must later be rebuilt by a full page decode (expensive for Clear B&W / Grayscale PNG pages) the next time that page scrolls into view. The cap itself is not a correctness bug — a swept thumbnail is rebuilt — but the half-sweep turns a bound into a recurring decode storm.
- **Verify (unit test):** With a directory of 5001 thumbnail files: write one more thumbnail and assert only the excess (1 file, or a small bounded number) is removed, not ~2500; or measure on device that scrolling a >5000-page library after a bake does not re-decode thousands of tiles.
- **Confidence:** Confirmed from code.

### C29. BM25 ranking uses page count, not document count, for IDF
- **Files:** `util/DocumentSearch.kt`, `Index.search()` line ~385, with postings built per page in `Index.init`.
- **Evidence:** `val idf = ln(1f + (total - list.size + 0.5f) / (list.size + 0.5f))`, where `list` is `postings[candidate.stem]` — and `init` adds one `Posting(document, page, frequency)` **per page** containing the term. So `list.size` is the number of (document, page) pairs, not the number of documents containing the term. A term printed on all 20 pages of a single long document gets the IDF of a term found in 20 documents; a genuinely rare term on one page of one document is scored as if it were common.
- **Impact:** Ranking distortion that grows with document length: long documents' repeated header/footer words are systematically under-weighted, and rare-term matches lose their intended boost. No wrong inclusion/exclusion — purely wrong ordering.
- **Verify (unit test):** Two docs, one with the query term on 10 pages, one with it on 1 page, same total length; compare scores before/after computing IDF from distinct-document count. Currently the 10-page doc's term IDF is computed with `list.size = 10`.
- **Confidence:** Confirmed from code.

### C30. Library-load failure screen is a dead end: no retry, no recovery action
- **Files:** `MainActivity.kt`, `setContent` → `Crossfade` branch lines ~148–155.
- **Evidence:** When `repo == null && loadError != null` the entire screen is a `Text`: "The library could not be opened. Your existing files were not changed. Restart the app or seek help before clearing app data." There is no Retry button, no "try again" action, and nothing that re-attempts `LibraryViewModel`'s load — the ViewModel's `init` runs once. The only recovery is killing the app from recents (and the text itself warns against clearing data, but offers no safe path).
- **Impact:** A transient read failure (e.g. storage hiccup, file briefly locked) forces the user out of the app with no in-app recovery; less confident users may clear app data and lose the library the message says is safe.
- **Verify (device):** Temporarily corrupt `library.json` (or make it unreadable), launch, observe the error screen has no actionable control; then restore the file and confirm the screen does not recover without a process restart.
- **Confidence:** Confirmed from code.

### C31. Gallery auto-save on Android 8–9 (legacy path): overwrites an existing gallery file and leaves a partial file behind on failure
- **Files:** `util/GalleryExporter.kt`, `saveLegacy()` (lines ~86–103); only reached when `Build.VERSION.SDK_INT < Q` (`saveToGallery`).
- **Evidence:** `target = File(dir, displayName)` then `target.outputStream()` directly — no existence check, no uniquifying, no temp file. The scoped (Android 10+) path inserts a new MediaStore row instead, so this is legacy-only. The copy is `input.copyTo(output)` followed by `MediaScannerConnection.scanFile(...)`; if the copy throws midway, the exception propagates to `saveToGallery`'s catch (returning `false`) but the partially-written target is never deleted. Collision is not theoretical: `pageFileName()` is second-resolution plus only 4 hex digits of randomness, so two captures in the same second can collide.
- **Impact:** On Android 8–9, a gallery copy can silently replace an earlier gallery image with a different page, or leave a corrupt partial JPEG in `Pictures/DocScanner` after a reported failure.
- **Verify:** On an Android 8/9 emulator, pre-create `Pictures/DocScanner/<name>` (or force two same-second captures), run `saveLegacy`, and check whether the old file is overwritten; and force a mid-copy failure and check whether a partial file remains.
- **Confidence:** Confirmed from code; needs an API <29 device/emulator to observe (the app's minSdk is 26, so this path is still reachable).

---

## Checked and not reported (so Arena does not re-report them)

- **Backup-restore concurrent-edit race (dropped after cross-check):** At `3c1e01c`, `importBackup()` takes its local snapshot *after* the slow restore extraction, refuses a restore whose documents overlap local IDs ("Backup contains documents already in this library. Restore cancelled to protect local edits"), and publishes merged folders/documents back-to-back with no suspension point in between. The long IO window is therefore *before* the snapshot, not after it, so edits made during extraction are captured, not dropped.
- **Old OCR re-processing loop:** genuinely closed (`ocrAttempted`/`ocrFailedAttempts` + bake reset).
- **Benchmark trigger in `MainActivity`:** gone (only a dead `PerfAuditRunner` import remains); `BenchmarkAuditActivity` is still `exported=true` + DUMP in the main manifest, but that exposure was already reported in the Round 70.3 review.
- **Splash-gate full library parse, `StartupWork` idle double-count, thumbnail 600 cap:** already known; the cap is now 5000, and C28 is the *remaining* sweep bug, not the old cap itself.
- **`LiveBorderAnalyzer.greyOf()` not reading `plane.pixelStride`:** not ranked — the live outline is advisory only (the crop is re-detected from the full capture), so a non-1 pixel stride would misplace the outline but not the saved pixels; device-dependent and below the cut.
- **`PdfSizeEstimate`, `DocumentNaming`, `CardComposition`:** read in full; no finding cleared the bar (estimates are explicitly approximate, naming quotes its literals, card fit clamps to the cell).
- **Preview-failure banner, batch-bake token rejection / `onDone` gating, `MarkupScreen` canvas sizing:** checked, not silently-wrong paths.
- **`deleteFolder()`'s two sequential state updates:** no suspension point between them, so a crash landing exactly between them is negligible.

## Limitations of this inspection

- Static only: no `./gradlew` build, no emulator/device run, no runtime logs were used. Several items therefore need the "Verify" step above before they are treated as user-visible bugs.
- Line numbers refer to the inspected head (`3c1e01c`). If the repository has moved on, re-check the function names rather than the line numbers.
- Arena's branch fixes at `59b60be` (complete-snapshot PageToken, CAS-committed publication, copy refuses missing files, benchmark-manifest-only activity, gated tracing) are **not** on `main` at `3c1e01c` and were **not** the subject of this hunt; do not treat this report as a review of that branch.

## Bottom line for Arena

Start with **C1, C2, C3, C4, C6, C7** — they are the most user-visible, the easiest to reproduce, and the ones where Arena's confirmation (or refutation) matters most. C1 is deleted content reappearing; C2–C5 are "it said it worked, but the pixels/metadata don't match" paths; C6–C8 are export/share/password paths that mislead the user. Everything else in this file is secondary to those.
