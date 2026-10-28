# Live border detection — the outline in the viewfinder, and the borders after it

Round 63. The ask: *"also add the auto border detection while clicking pictures and also
after capturing from adobe scan."*

Two halves, one detector:

| | where | when it runs | what it produces |
|---|---|---|---|
| **Live** | `ui/camera/ScannerCameraScreen.kt` viewfinder | continuously while framing, ≤ 6×/s | an **outline** drawn over the preview — never part of the capture |
| **After** | `data/ScannerRepository.detectBordersInBackground` | once per page, after a capture or an import | `QuadCorners` written to the page, i.e. the crop the editor opens on |

They share `DocumentQuad` (the geometry) and `QuadSanity` (the plausibility rules), so the
outline the user lines up and the crop that is produced cannot disagree about what a page
is.

---

## 1. What already existed, and what did not

Before this round:

* `DocumentDetector.detect(bitmap, context)` — the **post-capture** detector (TFLite
  `border_detect_224.tflite`, then `DocumentQuad` as the fallback). Used by
  `CropAndFilterScreen`.
* The camera review flow already ran it: `ScannerCameraScreen.onFinishScan` pushes
  `CropAndFilterNav(docId, firstPageId, review = true)`, and the review screen detects the
  displayed page on load and every other uncropped page in a background pass.
* Merged **nothing** for the viewfinder: the screen deliberately drew no guide at all
  ("No guide frame over the preview, deliberately", §the comment above the stage).

So the *capture* path was covered and the *framing* path was not. There was one more hole,
and it is the second half of the ask: **an imported page never went through detection at
all.** The gallery picker on the dashboard (`DashboardScreen`) and the album button inside a
document (`DocumentDetailScreen`) both wrote `ScannedPage(… imageUri = path)` with
`corners = QuadCorners()` and stopped there — no review screen was pushed, and the per-page
auto-detect in `CropAndFilterScreen` only runs once somebody opens the editor. A photo
imported from another app therefore sat in the document uncropped, looking nothing like a
scanned page, until the user found *Crop & Filter* themselves.

---

## 2. The live outline

### Pipeline

```
CameraX ImageAnalysis (4:3, KEEP_ONLY_LATEST)
  → LiveBorderAnalyzer.analyze(image)          ui/camera/LiveBorderAnalyzer.kt
      Y plane only, row-stride aware, box-averaged to 360 px wide
  → GreyFrames.rotate(grey, imageInfo.rotationDegrees)   util/LiveBorderTracker.kt
  → DocumentQuad.detect(luma, w, h)             util/DocumentQuad.kt   (unchanged)
  → LiveBorderTracker.onFrame(corners, sides)   util/LiveBorderTracker.kt
  → LiveBorderOverlay(state, frameAspect)       ui/camera/LiveBorderOverlay.kt
```

`LiveBorderTracker`, `GreyFrames` and `SceneRect` contain **no Android type**, which is why
`app/src/test/java/com/example/docscanner/util/LiveBorderTrackerTest.kt` can cover them on a
plain JVM.

### The rules that make it steady (and why they are the feature)

Detection at 6 Hz is judged as a *signal*, not as an answer. Four rules, all in
`LiveBorderTracker`:

1. **Confidence accumulates, it does not pass through.** +1 per frame that found ≥ 3 real
   sides, −2 per frame that did not, clamped to `0..6`. The outline appears at 3 and is
   removed below 2, so a locked outline survives **two** missed frames (≈ 330 ms) and one
   bad frame is invisible. `SHOW_AT`, `PENALTY`, `HIDE_BELOW` are the constants.
2. **Sub-threshold movement is ignored.** If all four corners of the new quad are within
   `SNAP = 0.02` (2 % of the frame ≈ 7 px on a 360 px frame) of the drawn ones, the drawn
   ones are kept. This is the jitter kill: exposure settling and detector noise move corners
   by a pixel, and a pixel at 6 Hz reads as a wobble.
3. **Real movement glides, it does not teleport.** Beyond `SNAP`, each corner moves
   `GLIDE = 0.5` of the remaining distance per frame and stops inside the snap window. The
   outline arrives in ~3 frames and never overshoots.
4. **Implausible quads are refused before they are drawn.** `≥ 3` real sides, area between
   12 % and 102 % of the frame, longest ÷ shortest side ≤ 8. `DocumentQuad` already rejects
   crossed and sliver quads; this is the cheaper second net for the viewfinder, where the
   cost of a false positive is a line drawn across the user's frame.

These are the same problems Adobe's live boundary shim solves with
`AreCurrentCornersWithinTolerance` + an accumulating display-confidence counter (reference
only — `adobe-scan/decompiled_src/CameraCleanLiveEdgeDetectionAndroidShim.java`, read-only,
never edited). The numbers here are **not** copied from it: that shim is driven by its own
250 ms/750 ms timer and an image-space tolerance, this one by a 150 ms analysis cadence and a
normalised tolerance.

### Deliberately not taken from the shim

* **Automatic torch** (`LiveBoundaryTorchHint`, `imageHasDarkShadow`,
  `imageHasGlareAfterTorch`). Flash here is an explicit three-state control whose state the
  user can see; a preview that turns the light on by itself, and then analyses glare to
  decide whether to turn it off, is a different product.
* **Auto-shutter** (`CanSetReadyForCaptureHint`, `mLiveBoundaryTriggerCapture`). The shutter
  is the user's finger.
* **`mBoundaryConfidenceForDisplayThreshold` drift under load** (`AdjustBoundaryConfidence…`).
  A device that cannot afford 6 Hz here simply gets fewer frames — `KEEP_ONLY_LATEST` drops
  them in the camera pipeline — instead of a detector that silently changes its standards.

### Cost (budget arithmetic, not a measurement)

* Working frame: 640×480 Y-plane → 360×270 = **97 200 px** to read, once.
* `DocumentQuad` then halves it (180×135) for the morphological close, upscales, box-blurs
  and scans columns/rows — the same work the editor does on every detection, on a frame
  **35× smaller** than the 2400 px decode it does there.
* ≤ 1 detection per 150 ms, one dedicated thread, `STRATEGY_KEEP_ONLY_LATEST`.

On the target 6 GB phone that is a few milliseconds of one core, six times a second, while
the preview is otherwise idle. **It has not been timed on hardware here** — the honest check
is `adb shell dumpsys gfxinfo` / a `DocScannerPerf` trace on a device, and that is listed as
owed in `docs/AGENTS_CHANGELOG.md`.

### Two failure modes that would otherwise be regressions, and their answers

* **A LEGACY camera guarantees only two concurrent streams.** Binding `Preview + ImageCapture
  + ImageAnalysis` throws on those devices, and a naive implementation would turn that into
  "the camera does not open". The third stream is therefore bound in its own `try`: on
  failure the analysis use case is unbound and `Preview + ImageCapture` is re-bound, so the
  viewfinder always works and the outline is simply absent.
* **`PreviewView` is `FIT_CENTER`, not `FILL_CENTER`.** A stream that is not 4:3 is
  letterboxed inside the 3:4 stage, so normalised corners mapped as if the stream filled the
  view would sit off the page. `SceneRect.fitCenter(containerW, containerH, contentAspect)`
  does that arithmetic in one place, and `LiveBorderTrackerTest` pins the three cases
  (matching aspect, wider stream, taller stream). The aspect used is the **analysed** frame's,
  passed with every state, not a hard-coded 3:4.

---

## 3. The borders after the fact

### Capture

Unchanged, and it already worked: `onFinishScan` → `CropAndFilterNav(review = true)` → the
review screen detects the displayed page on load and every other uncropped page in a
background pass (`AUTO_DETECT_START_DELAY_MS = 450`, `AUTO_DETECT_LIMIT = 40`).

The live outline is **not** stored as the page's corners. The crop is re-detected from the
full-resolution capture: the live quad comes from a 360 px frame and would be a whole-pixel
approximation of a warp done at 3200 px, and the page may have moved between the last
analysis frame and the shutter. The outline is a framing aid; the crop is an output.

### Import

New: `ScannerRepository.detectBordersInBackground(docId)`.

* Targets every page of `docId` whose corners are still `QuadCorners.FULL_FRAME` — the exact
  condition the review pass uses.
* Decodes at `AUTO_BORDER_DETECT_DIMENSION = 900` (`DocumentDetector` works at 360 px wide
  regardless, so this is already 2.5× more than the geometry can use), detects, recycles in a
  `finally`, and writes the whole batch through `setPageCorners` once.
* Runs on the repository's own scope, like `indexTextInBackground`, so an import never waits
  for it and the result survives the screen that started it being popped.
* **Never overwrites a page the user has already cropped.** `setPageCorners` gained
  `onlyWhereUncropped` (default `false`, so existing callers are unaffected), checked
  *inside* the `_documents.update` — a snapshot taken before the pass would have the same
  race it is meant to prevent.

Called from `DashboardScreen` (system photo picker import) and `DocumentDetailScreen` (album
import into an existing document). Both are fire-and-forget: the pages appear immediately and
their borders appear a moment later.

---

## 4. Invariants for the next agent

* **The live quad is never written to a page.** Detection for the crop happens at capture
  resolution. Changing this needs a measurement showing the live quad is better, and it will
  not be.
* **`LiveBorderTracker` is the only place frame-to-frame smoothing lives.** Do not add
  smoothing to `DocumentQuad` — the editor calls it once per page and must stay exact.
* **Do not remove the two-stream fallback** in `ScannerCameraScreen`'s bind block. It is the
  difference between "no outline on an old phone" and "no camera on an old phone".
* **`SceneRect.fitCenter` must stay in step with `PreviewView.ScaleType.FIT_CENTER`.** If the
  preview scale type ever changes, the mapping changes with it.
* **`detectBordersInBackground` must keep `onlyWhereUncropped = true`.** A background pass
  that overwrites a handedited crop is a data-loss bug, not a cosmetic one.
* Analysis must never outlive its screen: `LiveBorderAnalyzer.stopped` is set in `onDispose`
  **before** `unbindAll()`, and the analysis executor is shut down with the composition.
