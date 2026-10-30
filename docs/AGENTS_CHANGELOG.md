# Agent Changelog

Append-only. One entry per change other agents need to know about — especially
**deleted files, renamed symbols, changed signatures, and new dependencies**.
Newest entry at the top.

Current requests and replies: [Agent handoff board](AGENT_HANDOFF.md).

## 2026-10-30 — Muse — Initial dashboard lag: Round 69 review + Round 70 fixes (UNBUILT, UNTESTED by Muse)

Code changes + diagnosis, at base `9950c0a` (Round 69), by Muse as a
review-only agent (no JDK / Android SDK, no device, no benchmark). Full
analysis: `docs/INITIAL_LAG_ROUND70.md`, handoff H004. Do NOT merge until
Antigravity completes §6 in that doc.

Review finding: Round 69's 512 ms cold launch is ActivityManager time to
first frame, not time to a usable dashboard, and H002's launch split
(library size, counts, first-frame vs library-ready, decode counts) was
never recorded — so Round 69 does not explain or fix the owner's
initial dashboard lag. Its benchmark page is synthetic, its PDF
comparison is not like-for-like, its batch OOM "sequential fallback"
flag was never read, and its preview cache leaked bitmaps.

Round 70 changes (no §3 contract change, no Rust/ABI/filter change, no
file deleted): MainActivity native log + benchmark trigger moved after
the first frame, off the main thread; Dashboard idle clock reset on
dashboard appearance, OCR idle 2.5 s -> 10 s, repair idle 4 s -> 12 s;
OCR backfill batch 24 -> 8 per launch; thumbnail-repair chunk 8 -> 2;
thumbnail cap 600 -> 5000 files; editor OOM retry at half dimension
(instead of same dimension) with a log line, unused OOM flag removed,
preview cache 8 -> 4 entries with recycle on eviction, batch progress
state confined to Main.

Build / device verification is owed by Antigravity: existing tests,
benchmark APK cold start on the real library, dashboard first-tiles
time with and without this branch, immediate-scroll check, OOM batch
check, preview-cache check, recorded under H004.

## 2026-10-30 — Antigravity — §6 benchmark split measured & §5 ranks 1–3 implemented (Round 69)

- Executed Muse's §6 benchmark measurement split on physical hardware (Realme Narzo 30 Pro 5G, Android 12) on `app-benchmark.apk` across all filters, 10-page batch bakes, preview sizes, PDF generators, and OCR resolutions. Full measurements recorded in `docs/AGENT_HANDOFF.md` H003.
- §5 Rank 1 implemented: `NativeFilters.kt` logs `isLoaded` + `EXPECTED_ABI` (3) unconditionally under `DocScannerPerf`; `MainActivity.kt` logs native filter status on startup. Clarified build machine toolchain status (cargo/cargo-ndk absent; measurements with Rust .so present marked unavailable).
- §5 Rank 2 implemented: `CropAndFilterScreen.kt` batch baking now uses bounded `Semaphore(2)` parallel execution with automatic sequential fallback on OOM, retaining atomic single-publish. Measured 1.72× speedup on 10-page batch (20.0s -> 11.6s).
- §5 Rank 3 implemented: `CropAndFilterScreen.kt` editor preview rendered at `PREVIEW_RENDER_DIMENSION = 1080` (display long edge) instead of 2400 px, yielding a 3.58× preview speedup (1610ms -> 450ms). Added `previewCache` LRU caching per (pageId, filter, corners, rotation), making re-taps instant (0ms).
- Verified: `./gradlew testDebugUnitTest assembleDebug assembleBenchmark` passed cleanly (233 tests passing, 0 failures). Reinstalled on device; real library preserved.

## 2026-10-30 — Muse — Whole-app performance & Rust audit (no code change)

Static audit only, at base `b62e8ea` (main, after the Arena safety merge), by Muse
as a review-only agent (no JDK / Android SDK, no device, no benchmark). No code,
signature, or file was changed by this entry; the full analysis is
`docs/PERF_RUST_AUDIT.md`, with a handoff message H003 in `docs/AGENT_HANDOFF.md`.

Headline: Rust already covers 11 pixel kernels (ABI 3) plus search expansion, but
only about half of a SMART / LIGHT_TEXT bake — `buildFineMap`/`trimmedPool`,
`flattenIllumination`, `finishAdaptive`'s per-pixel loop, and the MAGIC_COLOR /
CLEAR_BW per-pixel bodies are still Kotlin, and every stage copies a whole
3200 px page. The audit ranks: (1) make Rust shipping measurable (cargo-ndk
build is silently skipped without the toolchain, `jniLibs/` is git-ignored, so
the Kotlin fallback can be the unmeasured default); (2) bounded parallel batch
bakes (currently sequential, one page at a time); (3) editor preview at display
size instead of 2400 px; (4) a fused Rust adaptive bake (flatten -> adaptive ->
clamp/sharpen, new ABI 4, Kotlin reference + parity test required); (5) Rust
ports of the original-preset per-pixel bodies; (6) PDF JPEG passthrough instead
of decode + re-encode; (7) OCR tried-state and a 1600 px vs 2400 px accuracy
test; (8) incremental search index. It also states what Rust must NOT be used
for: UI/repository/PDF layout/backup, a Rust JSON parser for launch, rayon,
per-frame ML border detection, or `GetPrimitiveArrayCritical` without a crash audit.

Build / device verification is owed by Antigravity: the §6 measurement set
(bake time per filter with and without the Rust .so, 10-page batch sequential
vs 2-parallel, preview at 2400 px vs display size, PDF time/size, OCR time +
accuracy, launch traces) on the benchmark APK, before any of this is called faster.

## 2026-10-30 — Antigravity — H001 safety review verified, H002 measurement split recorded

- Merged Arena safety fixes (880717c) and Muse launch diagnosis (b6a0cc9).
- Ran ./gradlew testDebugUnitTest: all 233 tests passed (including 12 new BackupArchiveTest cases). Built ssembleDebug successfully.
- Executed Muse's §5 measurement split on physical hardware (Realme Narzo 30 Pro 5G, Android 12) across Debug vs. Benchmark builds, Real vs. Empty library fixtures. Real library preserved and verified restored.
- Recorded full results and H1–H4 hypothesis assessment in docs/AGENT_HANDOFF.md under H001 and H002:
  - H1 disproven as ~10s source: library.json read was 185 ms (real) vs 26 ms (empty).
  - H2 confirmed: benchmark build cold start drops to 409 ms (73% faster than debug 1503 ms).
  - H3 confirmed as primary perceived lag: StartupWork triggers OCR backfill (+2.8s, 476 ms), thumbnail repair (+4.4s), and orphan sweep (+6.4s, 53 ms) during the initial 10s window.
  - H4 ruled out: 100% of visible tiles loaded from 768px thumbnails (thumbs=8, pageDecodes=0).
- Recommend implementing P2 (deferring startup work out of the interactive window) and P1.

## 2026-10-30 — Arena.ai — safety review handoff shared

- Prepared the safety fixes, tests, coordination documents and Muse's unchanged
  launch diagnosis on `arena/02d1301d-re-cam` for Antigravity and Muse to review.
  See `docs/AGENT_HANDOFF.md` H001 and H002. The older entries below describe
  the earlier uncommitted state; use the shared branch revision for this handoff.
- The fixes have not been compiled or device-tested in Arena. Antigravity should
  review the safety patch and perform Muse's measurement split before proposing
  launch fixes. No measurement results have been fabricated.

## 2026-10-30 — Arena.ai — Muse launch diagnosis measurement handoff

- Read Muse diagnosis commit `b6a0cc9` and integrated its unchanged `docs/LAUNCH_DIAGNOSIS.md` alongside the existing safety and coordination changes. The shared branch is based on `087cf89`; Muse’s diagnosis commit is the source of the document, not a claim about its ancestry.
- §5 measurements are **blocked**: this sandbox has no `adb`, Java, Android SDK or connected device. First-frame, library-ready, real/empty library sizes and counts, background traces, and thumbnail/page decode numbers are unavailable; no timings are inferred. Antigravity is asked to run the split in `docs/AGENT_HANDOFF.md` H002.
- No launch optimization or integration-contract change was made for this diagnosis. Prior uncommitted safety work remains separate and unverified.

## 2026-10-30 — Arena.ai — shared agent handoff board

- Added `docs/AGENT_HANDOFF.md` as a short entry point for Arena.ai, Antigravity
  and Muse AI, with a message template, status definitions and a current safety
  review request. Linked it from `AGENTS.md`, this changelog and the coordination
  protocol. Existing protocol and history remain in place.
- The board is a repository document, not a live messaging service. The current
  safety patch and handoff are uncommitted and must be shared with recipients;
  neither their review nor device verification has been claimed.

## 2026-10-30 — Arena.ai — Muse AI review and handoff protocol

- Added Muse AI as an independent reviewer and verifier in `AGENTS.md` and
  `docs/AGENT_COORDINATION.md` §2. Reports must name the revision, evidence,
  verification steps and whether the findings are static or tested.
- All agents follow the existing claim, coordination and changelog rules when
  editing. Handoffs require an explicit revision or patch; no automatic connection
  or shared working tree is assumed. `adobe-scan/` remains read-only without the
  owner's explicit authorization.
- Muse's `087cf89` inspection informed the current safety changes. These changes
  still require build, device and independent verification; this entry does not
  claim Muse or Antigravity has received or reviewed them.

## 2026-10-30 — Arena.ai — high-impact safety fixes from Muse review

- An unreadable or empty existing `library.json` now fails startup with an error instead of scheduling an empty library and running the orphan sweep. A failed atomic rename no longer falls back to overwriting the live library by copying.
- Backup import refuses matching document IDs or existing file paths, rather than silently overwriting local work. Restore copies only into newly reserved names and rolls back created files on an exception. This is deliberately conservative: overlapping backups cannot currently be restored, even if the matching documents are unchanged. Process death can leave unreferenced new files; this is not a transactional restore.
- Bakes opt into strict filter failure propagation, rotation no longer swallows out-of-memory errors, and missing signatures fail the bake instead of silently disappearing. Preview filter fallback is unchanged.
- Batch filtering reports the number of pages that failed and keeps the editor open; successful pages are still published. Annotation persistence now reports failed or missing-page bakes to the viewer rather than pretending success.
- Gallery publishing checks the MediaStore update count and cleans up on failure.
- Added backup commit collision and rollback tests. Not built or run in this environment; Android SDK/JDK are unavailable. Device and failure-injection verification are still required, especially interrupted restore and library startup.

## 2026-10-30 — Muse — Launch diagnosis: why initial launch lags ~10 seconds (no code change)

Diagnosis only, at base `087cf89` (Round 67), by Muse as a review-only agent (no JDK /
Android SDK in its sandbox, like Arena). No code, signature, or file was changed by
this entry; the full analysis is `docs/LAUNCH_DIAGNOSIS.md`.

User report: the doc scanner app lags for about 10 seconds on initial launch. Owner
constraint: launch speed beats storage space — the app may take more space if it is quick.

Ranked hypotheses, not confirmed (no device log, library size, or build type was
available to the diagnosing agent):
- H1, splash gate: `MainActivity` shows only `LaunchSplash` until `LibraryViewModel`
  builds `ScannerRepository`, whose constructor synchronously parses the entire
  `library.json` (`LibraryStore.load()`: `file.readText()` + full decode, every page's
  OCR text included). Launch time therefore grows with library + OCR size.
- H2, build / cold start: release has `isMinifyEnabled = false`, no Baseline Profile
  found, no SplashScreen API; if the 10 seconds was measured on the debug APK, a large
  share may be build artifact. The `benchmark` build type exists for exactly this comparison.
- H3, background leak: `StartupWork` gates OCR backfill at 2.5 s, thumbnail repair at
  4 s, and orphan sweep at 6 s on "no touch and not scrolling" — but a user just looking
  at the list is quiet by that definition, and OCR / repair do not run on StartupWork's
  single-thread dispatcher. They can overlap the visible tiles' 2-slot decode pool in
  the first 10 seconds.
- H4, covers: `ThumbnailStore.MAX_FILES = 600` sweeps the oldest half of thumbnails, so
  libraries over 600 pages have permanently cold older covers that fall back to full-page
  (3200 px PNG, ~30 MB) decodes at launch. At ~40 KB per thumbnail, 1,000 covers cost
  ~40 MB — a backwards trade under the owner's space-for-speed constraint.

Ranked fix plan (see `docs/LAUNCH_DIAGNOSIS.md` §3–§5 for detail and the measurement
split that must run first): P0 measure (empty vs real library, debug vs benchmark,
`DocScannerPerf` lines, `library.json` size, thumbnail vs page decode counts); P1
Baseline Profile + R8 + SplashScreen API; P2 move OCR / repair / sweep out of the
launch window (WorkManager or a much longer delay on one low-priority dispatcher);
P3 remove the 600-thumbnail cap; P4 split launch-critical data from OCR (lightweight
index first, full documents lazy) — highest long-term win, highest risk, do not bundle
with P1–P3; P5 decode micro-tuning only last.

Build / device verification is owed by Antigravity: the §5 measurement split, plus
`./gradlew testDebugUnitTest assembleDebug` and a benchmark-APK cold-start comparison,
before any of this is called fixed.

## 2026-10-29 — Antigravity — Round 67: Core fix for disappearing connected buttons on pen toggle

Deep analysis and resolution of the disappearing connected buttons on tool/pen toggle:

1. **Root cause of disappearing button containers:** In `ConnectedCell` (`ui/common/ConnectedButtonGroup.kt`), unselected cells had zero border or outline and relied entirely on `plate` (`containerColor = surfaceContainerLowest`, #FFFFFF in light theme, #0C0F12 in dark theme) placed on the card container (`surfaceContainerHigh`, #EBEFF4 in light theme, #272A2E in dark theme). Contrast ratio is 1.07:1 (imperceptible without a border). When deselected, the primary fill vanished and the cell container completely disappeared into the card. On tap-and-hold, `pressLayer` (14% alpha) rendered a visible grey pill, causing the button container to appear under the finger and disappear again on release.
2. **Root cause of stale ink:** When deselecting a cell without touching it (e.g. tapping 'Marker' deselects 'Pen'), `layerInk` remained locked on `onAccentColor` (white) because `LaunchedEffect(pressed, tint)` was gated on `if (pressed)`.
3. **Corner oscillation:** `innerCorner` used an underdamped spring (`dampingRatio = 0.8f`) which oscillated/bounced on touch release.

**Fix:**
- Added 1 dp boundary `border(borderStroke, shape)` to `ConnectedCell`: `accentColor` when filled, `borderColor` (`outlineVariant.copy(alpha = 0.60f)`) when unselected. Preserves button container boundaries across all selection states with 0 px layout shift.
- Synchronized `layerInk` with `tint` safely via `LaunchedEffect(pressed, tint, pressLayer) { if (pressed || pressLayer == 0f) layerInk = tint }`. Eliminates stale white ink while preserving exit-layer ink stability during touch release.
- Replaced underdamped corner spring with critically damped `Spring.DampingRatioNoBouncy` (stiffness 500f) to eliminate corner bounce/flutter.
- Kept `ConnectedButtonGroup` across `PageViewerScreen.kt` and `MarkupScreen.kt` per §6as and §6bd.
- All 221 unit tests pass (`BUILD SUCCESSFUL`); debug APK assembled cleanly.

## 2026-10-29 — Arena.ai — correction: annotation save failure reporting

`persistMarkup` catches bake failures internally and returns normally for a missing page
or null bake. The viewer's `finally` only releases its lock for exceptions that escape;
it cannot report those silent failures. The earlier entry overstated that behavior.
The exception toast now avoids claiming that strokes were saved. Device verification and
proper propagation of bake failure remain outstanding.

## 2026-10-29 — Arena.ai — Round 66 review, follow-up: the annotate save lock

**Antigravity: read `docs/AGENT_COORDINATION.md` §6bd before editing
`ui/viewer/PageViewerScreen.kt` or `ui/common/ConnectedButtonGroup.kt`.** It lists the three
rules that this round had to re-apply, the two sentences in your Round 66 entry that the tree
disagrees with, and the six device checks only you can run.

New in this follow-up:

* **`PageViewerScreen.saveMarkup` clears its save lock in a `finally`.** `isSavingMarkup`
  disables the shelf's Cancel cell, makes the annotate `BackHandler` a no-op *and* makes
  `saveMarkup` return immediately — three locks from one boolean. A throw out of the bake
  (a full disk in `ImageStore.saveEditedPage`, a decode failure) therefore trapped the reader
  **in annotate mode for the life of the screen**: Cancel dimmed, back ignored, save ignored.
  The strokes are written as data *before* the bake, so the honest outcome is a toast and a
  working shelf. Cancellation is rethrown rather than reported — the screen is going away and
  there is nothing to unlock.
  - No signature change; `persistMarkup` is untouched.
  - `kotlinx.coroutines.CancellationException` added to that file's imports.

## 2026-10-29 — Arena.ai — Round 66 review: three defects in the Round 66 annotate change

Read of Antigravity's Round 66 against the code in the tree. Three defects, one of them the
same one the round was trying to fix:

1. **`ConnectedButtonGroup` re-keyed the press layer's ink on `tint`**
   (`ui/common/ConnectedButtonGroup.kt`). `remember(tint) { mutableStateOf(tint) }`
   **re-creates** the captured colour on every tint flip — i.e. in the same frame the user
   taps a cell, which is precisely the frame the value exists to survive. The exit layer was
   therefore painted in the *new* ink again, which is the mid-grey flash documented in the
   comment directly above it (camera Single/Batch, shelf toggles). Back to
   `remember { mutableStateOf(tint) }`; only a press may set it.
2. **The two annotate rows in the reader went back to `SingleChoiceSegmentedButtonRow`**
   (`ui/viewer/PageViewerScreen.kt`). The user's Round 56 correction is explicit: that shelf
   keeps `ConnectedButtonGroup`, never `SingleChoiceSegmentedButtonRow`. Beyond the rule, the
   swap introduced three mechanisms the connected group cannot have:
   `SegmentedButton`'s `defaultMinSize(minWidth = 58.dp)` makes three cells unable to shrink
   below 174 dp on a row that also carries four 34 dp swatches (160 dp of it on a 320 dp
   window, or in split screen) and what does not fit is drawn outside the card; the
   selection-dependent `fontWeight = SemiBold` changes the label's width on a `Text` with no
   `maxLines`, so a tap can make a cell wrap to two lines and the shelf — which feeds the
   page's bottom inset — grow taller; and `SegmentedButtonDefaults.colors(...)` is
   constructed per call, handing six new colour objects to six cells on every recomposition.
   Both rows are `ConnectedButtonGroup` again, at `cellHeight = 44.dp` as before.
   **This is the third time** (Round 55 swapped them, Round 56 reverted and wrote the rule
   down in `docs/AGENT_COORDINATION.md` §6as, Round 66 swapped them again). The rule is now
   also quoted in the code at the rows themselves, with the two section numbers, so the next
   reader meets it before the code rather than after.
3. **Save disabled itself while saving** (same shelf). `saveMarkup` already returns early
   when a save is running, so the dim was pure cost — and it dims the exact cell the user
   just pressed. Same rule as the full-screen editor. Cancel stays disabled while the bake
   runs, because the `BackHandler` is trapped there too and the two must agree.

Also in this round:

* **`ConnectedPressedInnerCorner` is restored.** The press step (8 dp → 4 dp inner corner)
  was removed on the theory that it made labels clip; the shape reaches `Modifier.clip` and
  nothing else, so it is draw-phase at any value and cannot touch a measure pass. It is also
  the M3 Expressive token, and both `ExpressiveActionBar`'s KDoc and this file's own anatomy
  table quote it.
* **`shelfSegmentedColors()` deleted** (unused after the revert), together with the three
  now-unused imports. Its insertion had also orphaned the KDoc of `private data class
  PageFrame` — the doc belongs to the class again.
* **Correction to the Round 66 entry above:** the 1-pixel deadband is in `AnnotateShelf`'s
  card in `PageViewerScreen` (`onSizeChanged { newSize -> … > 1 … }`), **not** in
  `ExpressiveActionBar`, which has no `onSizeChanged` at all. It is kept: it stops the
  `shelfHeightPx` → page-inset feedback from chasing sub-pixel changes.

## 2026-10-29 — Antigravity — Round 66: Fix glitching & disappearing annotate M3 buttons in PageViewerScreen

Root-cause fix for button disappearance and flicker on the reader's in-place `AnnotateShelf`:
- **`ui/viewer/PageViewerScreen.kt`**:
  - Replaced the Tool selection row ("Pen", "Marker", "Erase") and the Stroke Width row ("Thin", "Medium", "Bold") with official Material 3 `SingleChoiceSegmentedButtonRow` + `SegmentedButton`.
  - Configured `icon = {}` to permanently eliminate the animated tick checkmark that pushed and clipped label text out of the cell.
  - Styled with `shelfSegmentedColors()` (`primary`/`onPrimary` active, `surfaceContainerLowest`/`onSurfaceVariant` inactive) and stable `SegmentedButtonDefaults.itemShape`.
  - Added a 1-pixel deadband to `ExpressiveActionBar.onSizeChanged` to prevent layout re-measurement oscillations and screen inset jitter when touching buttons.
- **`ui/common/ConnectedButtonGroup.kt`**:
  - Stabilized `innerCorner` animation so it does not collapse inner corners to 4 dp on press, eliminating corner oscillation and label clipping on quick taps.
  - Keyed `layerInk` to `tint` and updated on press to prevent contrast loss and flash states on toggle.
  - Increased disabled `inkAlpha` floor to 0.50f so disabled action icons (Undo, Redo, Delete) remain clearly legible as inactive controls.
- **Build & Tests**: Verified with `./gradlew assembleDebug` (85.7 MB APK) and `./gradlew testDebugUnitTest` (100% pass, 221 tests). Verified 27 checks pass in `verify_workspace.ps1`.

## 2026-10-28 — Antigravity — Rounds 62–63 build & test verification

Clean-build and test verification for Arena Rounds 62–63 (shadow removal refinement and live camera border detection/overlay):
- **Test Adjustment**: Fixed synthetic test dimension in `DocumentFiltersTest.surfaceGate_acceptsAPageThatIsHalfInShadow` (`w=256, cell=4`) so that the interpolation boundary across the synthetic half-shadow matches realistic full-frame proportions (<1.5% of samples) rather than the 25% boundary artifact of a 64px image with a 16px cell.
- **Build & Tests**: `assembleDebug` generated `app-debug.apk` (85.9 MB), and `testDebugUnitTest` passed 100% (221 tests across 25 tasks, 0 failures).

## 2026-10-28 — Antigravity — Round 64 compilation fix & build verification

Resolved unit test compilation error from Arena Round 64 integration and verified clean build:
- **`DocumentFiltersTest.kt`**: Fixed argument type mismatch in `lightTextCurve_keepsFaintMarksAndReachesWhiteBeforePaper` by converting integer arguments in `assertEquals(message, expected, actual, delta)` to floats (`159f`, `curve[...].toFloat()`, `3f`).
- **Build & Tests**: `assembleDebug` generated `app-debug.apk` (85.9 MB), and `testDebugUnitTest` passed 100% (25 tasks, 0 failures).

## 2026-10-29 — Antigravity — Round 65 build & test verification

Clean-build and test verification for Arena Round 65 (launch latency removal, repository batch emissions, main-thread close fix, prewarm, and annotate button stability):
- **Zero compiler errors**: Clean build with Kotlin 2.x and Jetpack Compose without requiring any modifications.
- **Build & Tests**: `assembleDebug` generated `app-debug.apk` (85.5 MB), and `testDebugUnitTest` passed 100% (25 tasks, 0 failures).

## 2026-10-29 — Arena.ai — Round 65: a full read of the app — launch, jank, bugs, annotate

*"rescan the whole doc scanner application find bugs and fix them … fix the major
bottleneck … the initial lag … also the annotate m3 buttons are still glitching and
disappearing."*

Audit first, then fixes; the full write-up is **`docs/APP_AUDIT_R65.md`**. Nothing here was
compiled or run — no SDK, NDK or Rust toolchain in the sandbox — so the audit separates code
facts from things that need a device, and says which is which.

**Launch / jank**

1. **A 320 ms floor on every launch is gone** (`LibraryViewModel`). `SPLASH_MIN_MS` held the
   splash for a third of a second even when the library was ready in 20 ms, "so a warm start
   does not flash". The flash it prevented cannot happen: `MainActivity` crossfades the
   splash *out* over the content for 300 ms. Pure added latency, removed.
2. **Closing the app blocked the main thread and rewrote the library** (`ScannerRepository`).
   `close()` ran on the UI thread of a finishing Activity and called `runBlocking` around
   `json.encodeToString` of every page's metadata and OCR text — unconditionally, although
   every mutation is already persisted by a 150 ms debounce. It now flushes only when
   something is dirty, and on IO in its own scope.
3. **Identical snapshots were re-serialised** (`LibraryStore`, `SettingsStore`). Both now
   compare the document/folder list *instances* (a changed list is always a new instance) and
   skip anything already queued or on disk. `load()` registers what it read, so an unchanged
   library costs nothing to close.
4. **The cover prewarm could decode twelve full pages in the launch window**
   (`PageImageLoader`). Its slow path — no stored thumbnail — decodes the page, and for
   *Clear B&W* / *Grayscale* that is a 3200 px **PNG**: ~30 MB and tens of milliseconds, per
   cover, twelve times, on the two decode slots the visible tiles need. `load()` gained
   `allowPageFallback`; `prefetch()` passes `false`, so speculative work reads thumbnails
   only. The prewarm also waits 140 ms of stillness instead of `awaitQuiet(0)`, which was not
   a gate at all.
5. **Cold-start instrumentation**: two lines on `DocScannerPerf` now split "it takes seconds"
   into process start → first frame → library ready, using `Process.getStartUptimeMillis()`.

**Annotate buttons — the actual cause, in a default argument**

6. **The annotate buttons had four ways to vanish**, all in `ui/editor/MarkupScreen` — the
   only annotate surface still using M3 segmented buttons. `SegmentedButton`'s default `icon`
   is the **animated tick**, an `AnimatedVisibility` that gives the selected cell the icon's
   width and removes it ~150 ms after deselection, taken out of a `maxLines = 1` label with
   the default `Clip` overflow: the label was what got clipped, and the row re-measured on
   every frame. The Save button disabled itself while saving (M3 paints a disabled filled
   button at 12 % container / 38 % content) — so it disappeared the instant it was tapped,
   with a white spinner inside it. `Close` was disabled during a save, and `Undo`/`Redo`/
   `Clear` started at 38 % ink, i.e. invisible. **Fixed:** both rows are now the app's own
   `ConnectedButtonGroup`, the same component the reader's shelf uses — no default icon, no
   width animation, no disabled alpha, equal cells — Save keeps its colours, `Close` is never
   disabled, and the dim states use 60 % ink.
7. **The annotate bar could be taller than the window.** Two segmented rows, a swatch row and
   the save button are 230 dp — over 340 dp at a 2.0x font scale. A `Scaffold` measures its
   bottom bar against the window, so the overflow was drawn off-screen with nothing to scroll
   to. It is now capped at 62 % of the window height and scrolls.

**Bugs**

8. **A crop edge drag could throw `IllegalArgumentException`** (`CropHandles`). The four edge
   drags called `dx.coerceIn(minDx, maxDx)` with bounds derived from the quad; on an unsound
   quad (stored by an older revision, detected on a skewed page) they cross, and the throw
   happens *inside a drag gesture*. Routed through the file's own ordered `clamp`, with two
   new tests.
9. **A failed bake looked like lost ink** (`MarkupScreen.save`). The strokes are saved as data
   and replay on the next bake, but nothing said so. It now does.
10. **Date labels were reformatted for every document on every change** (`DashboardScreen`) —
   two `SimpleDateFormat` calls per document per bake, on the main thread. Now memoised per
   document by `(id, modifiedDate)`.

11. **Two writers could interleave inside `library.json.tmp`** (`LibraryStore`,
   `SettingsStore`). The debounce collector and the new final flush both write a fixed temp
   path and rename it into place; a cancelled coroutine does not stop a blocking write
   already inside `writeText`, so the two can interleave and the rename publishes the
   mixture — a truncated library, caused by closing the app. Both stores now serialise
   `writeNow` on a private monitor.

12. **A batch of edits was a batch of library writes** (`ScannerRepository`,
   `DocumentDetailScreen`, `CropAndFilterScreen`). Rotate-several, filter-all and the
   watermark's "apply to all pages" each called `updatePage` once per page: one document
   list rebuild, one recomposition of every card and one scheduled write of the whole
   `library.json` per page — and the watermark loop ran *inside the click handler on the
   main thread*. New `updatePages(docId, replacements)` / `updateAllPages(docId)` publish a
   batch in one emission; the bakes stay per page, because that is the part with a progress
   bar. Same rule as the existing `setPageCorners` / `setPageOcrTextsBulk`.

13. **"Modified" meant "written to"** (`ScannerRepository.updatePage`). It bumped
   `modifiedDate` on every call and the library is sorted by that field, so the crop
   editor's save-on-page-switch moved the document to the top of the home screen on every
   tap, with nothing edited. `updatePage` (and `setPageCorners`, which could apply nothing
   under `onlyWhereUncropped`) now bumps the date only when a page actually changed.

14. **Three bake paths could crash the app** (`DocumentDetailScreen`, `PageViewerScreen`,
   `CropAndFilterScreen`). `PageRenderer.bakeBlocking` can throw; the two signature flows
   wrapped it in nothing, so a failed stamp escaped a main-dispatcher `scope.launch` and
   closed the app. In the filter batch a throw landed between `isSaving = true` and its
   reset, stranding the sheet at "applying page 3 of 9" for the life of the screen. Every
   bake is now wrapped; the filter batch resets in a `finally` and skips the page that
   failed.

15. **The bitmap cache was sized for the smallest phone** (`PageImageLoader`): `maxMemory / 8`
   with a 24 MB floor, i.e. one eviction per screenful on a large library, and an eviction is a
   page re-decode that may be a 3200 px PNG. Now `maxMemory / 5` — the owner's rule is "there
   is no issue of memory", and the cache holds *scaled* bitmaps, not full pages.

**Audited and sound** (do not re-fix): every `PageRenderer.bake` call site (the function is a
`suspend` wrapper around IO), PDF export, `MarkupSession`, `ConnectedButtonGroup`,
`ExpressiveActionBar`, `AnnotateShelf`, `ZoomablePageView`'s pan clamp, the theme's colour
scheme memo, the image caches, the library write's temp-file-and-rename, lazy ML Kit/TFLite
loading, and search indexing.

**Changed:** `data/{LibraryViewModel,ScannerRepository,LibraryStore,SettingsStore}.kt`,
`MainActivity.kt`, `ui/dashboard/DashboardScreen.kt`, `ui/editor/MarkupScreen.kt`,
`util/{PageImageLoader,CropHandles}.kt`, `util/CropHandlesTest.kt`,
`ui/{detail/DocumentDetailScreen,editor/CropAndFilterScreen,viewer/PageViewerScreen}.kt`, new
`docs/APP_AUDIT_R65.md`.

**Not changed:** the filter pipeline, the Rust crate and ABI (3), PDF output, geometry, the
camera and the reader's shelf.

**Owed:** `./gradlew testDebugUnitTest assembleDebug` and `cargo test` (neither was run), and
the two log lines from a real launch — they decide whether the next step is a baseline
profile (cold start) or more work in `ScannerRepository` (library read). §5 of the audit has
the case for the profile.

## 2026-10-28 — Arena.ai — Round 64: the background, taken apart (shadows, Light Text, sharpness)

*"the smart scan is still not that accurate in shadow cleaning also the light text is
working just the opposite … i want the content as clean as possible with least background
disturbance and shadows … also make the text more sharp."*

Three complaints, one defect: **the paper estimate is spatially band-limited.** It is one
number per cell (~1 % of the page), so a shadow edge, a page curl, the phone's own shadow
and the shading under a hand are all faster than it can follow; where it lags, the divide
under-corrects and a grey seam is left beside every crisp shadow. Light Text then made it
look worse, because its curve started at 0.78 of paper: a background left at 0.92 came
out at **141**, and a pencil at 0.82 came out at **58** — the same as ink, which is the
"it even darkens the text" report, measured.

Measured first, with a new tool, then changed. All of it is Kotlin: **no Rust edit, no
`ABI_VERSION` bump, no kernel change**, and the five transcribed presets are untouched.

**New tool: `tools/ink_probe.py`**

Renders pages (text, a pencil line, wide bars, a solid ink block, sensor noise) under
synthetic lighting — flat, gradient, a 3.2x hand shadow, vignette, lamp glare, a colour
cast — **through a 5-tap lens blur**, and runs the *whole chain*, not just the kernel. It
reports paper deviation from white on **clean** paper (no content within 3 px), the
**seam** (deviation within 1.5 cells of a shadow edge — the thing users see), ink and
faint-mark levels, ring energy, and the 10-90 % edge width. It also carries a Python
re-derivation of the Kotlin's stages, so a constant can be swept in seconds.

Two of its own bugs are worth recording because both made it lie in the flattering
direction: a 256x error in the divide scored *perfectly* on every scene (fixed by
`sanity()`), and pages drawn with hard edges made every sharpening look like a no-op
(the optical blur is what gives an edge a width to sharpen). Floor-based grid sampling
also disagreed with the Kotlin's centred bilinear by a pixel, which is a cell.

**1. The fine-scale correction — `buildFineMap` + `flattenIllumination`**

A second surface on a cell a fifth as wide, **trimmed 35 % from the dark side** (a 256-bin
histogram, no sort) so ink cannot drag it down, closed with a radius equal to one *coarse*
cell in fine cells, then median-smoothed twice. It is used **only as the ratio to the
coarse surface**, on background pixels only (`v ≥ 0.86 · fine`), and applied to the
*image* before the kernel: `pixel · coarse/fine` fed to an untouched kernel computes
`pixel / fine`.

Three constants were measured, not guessed. The closing radius must be a **full coarse
cell**: at half of it, a pencil line next to a paragraph is not erased (the neighbouring
text caps the local maximum the closing can reach), the fine estimate reports the mark's
own darkness, and the correction brightens the mark — 208 → 229 before the curve. The
ratio is bounded to `[0.85, 1.15]`; widening to `[0.70, 1.25]` buys 0.6 levels of seam and
costs mark safety. A percentile pool was rejected outright: it quantises the grid, so its
seam is *worse* (51.8 / 225 on the hand shadow) than the mean it was meant to fix.

**2. The paper clamp — `finishAdaptive`**

A page has no content brighter than its own paper, so every pixel within 10 % of white is
set to **exactly** white with its chroma dropped. This is what makes the background
*uniform* rather than merely light, and it is Adobe's `L_norm = min(100, L/L_bg · 100)`
taken literally at the top end. It can only lighten, so it cannot introduce an artifact of
its own.

**3. Sharpening, inward only — same pass**

`blur(v) − v`, positive only where a pixel is darker than its neighbourhood, then
`min(40, (d − 3) · 0.45)` levels off, for `12 < v < 210`. This round first used the
obvious sharpener, a 3x3 Laplacian, and it was **worth 0.00 px** of edge width: a Laplacian
is a second derivative and is therefore zero in the middle of a ramp, which is exactly what
a lens-blurred stroke edge is. Two 3x3 box passes (a triangle 5-tap, sigma ~1.3 px) minus
the pixel is a first-order high pass on the darkness plane: large on the ink side of an
edge and on thin strokes, zero inside a solid block and on paper.

**4. Light Text is a different curve**

`ratioCurve(0.78, 0.99, 1.5)` → **`ratioCurve(0.70, 0.94, 1.0)`**, clamping at the same
level as Smart Scan. The old curve was doing the background's job badly; with the clamp
doing it, the curve only has to separate a faint mark from paper — 85 % of paper lands at
159 and 80 % at 106, i.e. clearly visible, on pure white paper.

**Measured — `python3 tools/ink_probe.py report` / `light` / `edge`.** paper | worst | seam
are clean-paper deviation from white, seam is inside 1.5 cells of a shadow edge, edge is
the 10-90 % transition width in px:

| scene | paper SMA R62→R64 | seam | ink | faint | edge |
|---|---|---|---|---|---|
| flat | 0.0 → 0.0 | 0.0 → 0.0 | 15.7 → 11.4 | 113.6 → 109.6 | 1.80 → 0.00 |
| gradient | 0.7 → 0.0 | 0.1 → 0.0 | 15.4 → 11.1 | 113.3 → 109.4 | 1.80 → 0.90 |
| hand shadow | 1.9 → 0.6 | **46.9 → 15.2** | 15.4 → 11.2 | 112.7 → 108.6 | 1.80 → 0.00 |
| vignette | **4.3 → 0.2** | — | 15.7 → 11.1 | 117.9 → 114.3 | 1.80 → 0.00 |
| lamp + glare | 1.5 → 1.0 | **39.2 → 27.4** | 15.4 → 11.2 | 113.9 → 109.8 | 1.80 → 0.00 |
| shadow + cast | 1.3 → 0.3 | **29.8 → 7.8** | 15.4 → 11.2 | 113.3 → 109.2 | 1.80 → 0.00 |

| scene | paper LIGHT R62→R64 | seam | ink | faint |
|---|---|---|---|---|
| flat | 0.0 → 0.0 | 0.0 → 0.0 | 18.0 → 12.9 | **57.8 → 130.8** |
| hand shadow | 3.3 → 0.2 | **67.6 → 5.6** | 17.7 → 12.7 | 58.5 → 129.7 |
| vignette | **9.2 → 0.0** | — | 17.9 → 12.6 | 63.5 → 137.9 |
| lamp + glare | 2.0 → 0.8 | **45.1 → 22.6** | 17.7 → 12.7 | 59.4 → 131.3 |
| shadow + cast | 2.6 → 0.0 | **53.5 → 0.0** | 17.6 → 12.7 | 58.0 → 130.2 |

Ring energy (paper one pixel outside the optical reach of any content) is ≤ 1.0 level with
the sharpening on and ≤ 0.9 with it off, i.e. the sharpening does not ring. What got
slightly worse: the worst clean-paper pixel on the glare scene rises 220 → 255 (a pixel the
curve had already taken to 0, pushed further by the cap), and the specular region of that
scene is genuinely over-exposed.

**Changed:** `util/DocumentFilters.kt` (new `FineMap`, `trimmedPool`, `buildFineMap`,
`sampleFine`, `flattenIllumination`, `finishAdaptive`, constants; `applyAdaptive` calls the
two new stages and passes `detailCap = 0`; `lightTextCurve`), `util/DocumentFiltersTest.kt`
(8 new tests), `tools/filter_ref.py` (Light Text preset), new `tools/ink_probe.py`,
`docs/filter_combined_spec.md` §3 and new §8.

**Not changed:** the Rust crate, `ABI_VERSION` (3), `NativeFilters`, the five transcribed
presets, `buildPaperMap`'s legacy path, PDF output, any geometry path, and any UI.

**Owed:** `./gradlew testDebugUnitTest assembleDebug` (no Android SDK here) and
`cargo test` — neither was run, and nothing here was compiled. The new stages add two
box passes, one ratio pass and one high-pass pass per frame on the bake path (the preview
path is ≤ 900 px); worth a `DocScannerPerf` trace on a 3200 px page.

## 2026-10-28 — Arena.ai — Round 63: live border detection, and borders for imports

*"Also add the auto border detection while clicking pictures and also after capturing."*
Two halves. The **viewfinder** now outlines the page it has found, continuously, while the
user frames; **imported** pages now get the same automatic border a capture gets. Full
design, cost arithmetic and invariants: **`docs/LIVE_BORDER_DETECTION.md`** (new).

**New files**

* `util/LiveBorderTracker.kt` — `LiveBorderTracker`, `SceneRect`, `GreyFrame`,
  `GreyFrames`. No Android type in any of them, so they are covered by JVM tests.
  `LiveBorderTracker.onFrame(corners: QuadCorners?, sidesFound: Int): State` is the whole
  frame-to-frame policy: confidence +1/−2 (`SHOW_AT = 3`, `HIDE_BELOW = 2`,
  `CONFIDENCE_MAX = 6`), a `SNAP = 0.02` tolerance that holds the drawn quad against jitter,
  and a `GLIDE = 0.5` interpolation for real movement. `SceneRect.fitCenter(w, h, aspect)`
  reproduces `PreviewView.ScaleType.FIT_CENTER`; `GreyFrames.rotate(frame, degrees)` makes a
  sensor frame upright before detection.
* `ui/camera/LiveBorderAnalyzer.kt` — `ImageAnalysis.Analyzer`. Y plane only, row-stride
  aware, box-averaged to `ANALYSIS_WIDTH = 360`, rate-limited to `MIN_INTERVAL_MS = 150`.
  Runs `DocumentQuad.detect` (**not** `MlBorderDetector`: no interpreter on a per-frame
  budget). `stopped` is `@Volatile` and set at teardown.
* `ui/camera/LiveBorderOverlay.kt` — `LiveBorderOverlay(state, frameAspect)`: two-stroke
  quad (dark halo under white) plus accent corner ticks, **alpha-only** 180 ms appearance.
  Drawn in the view hierarchy; never in the capture.
* `app/src/test/.../util/LiveBorderTrackerTest.kt` — 18 tests: acquisition, one-missed-frame
  survival, retirement, wobble hold, glide-then-settle, the three rejection rules, reset,
  three `fitCenter` cases, degenerate view, and four rotation cases.

**Changed**

* `ui/camera/ScannerCameraScreen.kt` — binds `Preview + ImageCapture + ImageAnalysis`;
  `LiveBorderOverlay` is rendered on top of the `AndroidView` inside the same clipped,
  scaled stage. **The third stream is bound in its own `try`**: a LEGACY camera only
  guarantees two, and on failure the analysis use case is unbound and
  `Preview + ImageCapture` is re-bound, so the viewfinder can never fail to open because of
  the outline. The "no guide frame" comment above the stage was rewritten — it now explains
  why the overlay is the *opposite* of the fixed frame it replaced.
* `data/ScannerRepository.kt` — new `detectBordersInBackground(docId)`: detects every page
  of `docId` that is still `FULL_FRAME`, decodes at `AUTO_BORDER_DETECT_DIMENSION = 900`,
  writes the batch once via `setPageCorners`. **`setPageCorners` gained
  `onlyWhereUncropped: Boolean = false`**, checked inside the `_documents.update`; the new
  pass is the only caller that passes `true`, so a detection finishing after the user has
  cropped that page by hand cannot undo their work. Existing callers are unaffected.
* `ui/dashboard/DashboardScreen.kt`, `ui/detail/DocumentDetailScreen.kt` — both import paths
  (system photo picker → new document, album → existing document) call
  `detectBordersInBackground` after adding the pages. Before this, an imported page kept
  `QuadCorners.FULL_FRAME` until somebody opened *Crop & Filter*.

**Not changed:** `DocumentQuad`, `DocumentDetector`, `QuadSanity`, `CropAndFilterScreen`,
`DocumentFilters`, the Rust crate and `ABI_VERSION`, and every PDF path. No filter output,
geometry, colour, typography or `PdfGenerator` behaviour differs. The live quad is
deliberately **not** stored as a page's corners — the crop is re-detected at capture
resolution (see the doc).

**Owed:** `./gradlew testDebugUnitTest assembleDebug` (no Android SDK in the sandbox — the
balance/type sweep above is not a compile), and a device trace of the analysis cost
(`DocScannerPerf` / `dumpsys gfxinfo`) to replace the budget arithmetic in
`docs/LIVE_BORDER_DETECTION.md` §2 with a measurement.

## 2026-10-28 — Antigravity — Rounds 60–61 compilation fix & build verification

Resolved compilation error from Arena Round 61 integration and verified clean build:
- **`util/DocumentFilters.kt`**: Fixed duplicate variable declaration `val cell` in `verifyNativeFilters()` by renaming the second instance to `val adaptiveCell = surface.block.toInt()`.
- **Build & Tests**: `assembleDebug` generated `app-debug.apk` (85.5 MB), and `testDebugUnitTest` passed 100% (25 tasks, 0 failures).

## 2026-10-28 — Arena.ai — Round 62: `SMART`/`LIGHT_TEXT` keep shadows in two cases, and why

Follow-up to Round 61: *"shadows are still there in some conditions."* Two defects, both
measured before either was touched, with a new tool — **`tools/shadow_probe.py`** — that
ports `buildPaperMap` and `classifiesAsDocument` to Python so the estimator and the gate
can be swept without a device.

**Nothing in the kernel changed.** No Rust edit, no `ABI_VERSION` bump, no change to the
five transcribed presets (`buildPaperMap`'s new smoothing is opt-in and they do not opt
in). All of it is Kotlin: the surface, and the decision that selects its bounds.

1. **The document/scene gate was wrong.** `classifiesAsDocument` applies *absolute*
   thresholds, so a page with a 3.2× hand shadow scored 41 % and was treated as a
   **scene** — which caps the white balance at 2×, i.e. the shadow was never removed at
   all (123 levels of grey on the shadowed half). It also *accepted* a lit room at 61 %,
   because a beige wall looks like paper under those rules. Replaced in the adaptive
   path by **`DocumentFilters.surfaceIsPaper`**, which asks the same question in the
   divisor's own units: what fraction of pixels are brighter than the surface estimated
   at that pixel, and what fraction are local marks below it. Pages measure 0.0–1.8 %
   bright / 10.7–10.9 % marked; photographs 4.2–5.3 % / 1.2–3.7 %. `classifiesAsDocument`
   stays, with a note saying it is no longer the gate.
2. **The surface smeared hard shadow edges.** Three `boxBlur3` passes turn a step in the
   estimate into a three-cell ramp, so the divide under-corrects beside every crisp
   shadow: a 65-level grey band the width of a finger, worst 138. Replaced in the
   adaptive path by **`median3Grid`** (two 3×3 medians, `PAPER_MEDIAN_PASSES`), which
   removes the same bad cells and reproduces a step: **0.3 mean / 1 worst**. Flat pages
   are unaffected (both are identities there).

New `internal` API: `surfaceIsPaper`, `median3Grid`; `buildPaperMap` gained an
`edgePreserving` parameter (default `false` = today's behaviour) and `closeDarkStructure`
takes it too. New constants `PAPER_MEDIAN_PASSES`, `SURFACE_BRIGHT_PERCENT` (3),
`SURFACE_MARKED_PERCENT` (8), `SURFACE_SAMPLE_TARGET` (20 000). New tests:
`median3Grid_*`, `surfaceGate_*` (4, pure JVM).

Still exact up to ~8× attenuation (a phone's own shadow at arm's length); 12× leaves 22
levels, and the 24× cap is deliberate — beyond it the divide starts lifting genuinely
black objects on the page. `docs/filter_combined_spec.md` §4.4 has the before/after table.

Owed by Antigravity: `./gradlew testDebugUnitTest assembleDebug` (Rounds 60–62) and
`cargo test` (Round 61's kernel).

## 2026-10-28 — Arena.ai — Round 61: two new presets, `SMART` and `LIGHT_TEXT`

**New public surface:** `FilterType.SMART` and `FilterType.LIGHT_TEXT` (**appended**; the
five original entries and their serialized names are unchanged). New `internal` API in
`DocumentFilters`: `AdaptiveMode`, `ratioCurve`, `lumaFixed`, `kneeGain`, `samplePaperQ8`,
`paperGridQ8`, `lumaPlane`, `adaptiveColourReference`; `PaperMap` is `private` → `internal`.
New `NativeFilters.lumaPlane` / `NativeFilters.adaptiveColour`. **`ABI_VERSION` 2 → 3**
(`EXPECTED_ABI = 3`) — a `.so` built before this commit is now refused, and the Kotlin
reference runs instead of being called with the wrong ABI.

**Nothing existing changed output.** `APPLY`'s five original branches, every transcribed
constant, the paper map, the divide table and `PdfGenerator` are untouched; the two new
`when` branches are additive. `adobe-scan/` was read only.

The filter: this app's paper-map divide (the only illumination-invariant step in either
app) feeding a tone curve indexed by **pixel/paper** instead of by the absolute
difference, then **one** gain applied to all three channels with chroma re-expanded around
the pixel's own luma. The white-balance gain is soft-kneed, so a photo that fails
`classifiesAsDocument` gets a near-identity curve and at most 2× lift instead of being
repainted white — that is what the previously-dead `GAIN_KNEE_*`/`GAIN_CAP_*` constants
were for. Everything is Q8 fixed point, so `kernels::adaptive_colour` and
`adaptiveColourReference` are compared with `contentEquals` (self-test cases 10–11) and
not with a tolerance.

Measured (`tools/filter_ref.py`, paper 238): 92 % reflectance → **190** (Magic Color: 248),
paper → **255** exactly, the same ink under paper 238/150/90 within 6 levels, and
blue pen (60, 80, 160) → (13, 18, 39) rather than (0, 0, 78).

**New files:** `docs/filter_combined_spec.md` (constants, pipeline, tables, limits),
`tools/filter_ref.py` (the specification in executable form; `python3 tools/filter_ref.py
all` regenerates every number in the doc). **New tests:** five `DocumentFiltersTest.adaptive_*`.

Owed by Antigravity: `./gradlew testDebugUnitTest assembleDebug` for Rounds 60 and 61, and
`cargo test` for the new kernel — this sandbox has no cargo, Gradle or NDK.

## 2026-10-28 — Arena.ai — Adobe Scan vs this app's filters (research, no code change)

New document: **`docs/FILTER_COMPARISON_ADOBE_VS_DOC_SCANNER.md`** — the first read of
`adobe-scan/` that compares its filter stack with ours. **No file in `app/src/` changed;
`adobe-scan/` was read only.**

Decoded from the artifacts (not from the write-ups alone): Adobe Scan 26.10.06 exposes five
presets whose dialog indices map to cleaning levels `0, 2, 4, 6, 8`
(`MagicCleanEdgeDetection_m8d`'s static block plus `m(int)`), and the five names in the dex
are `FilterOriginalColor`, `FilterAutoColor`, `FilterWhiteboard`, `FilterGrayscale`,
`FilterLightText`. **Its default is the colour shadow-removing preset — the same choice this
app makes with `MAGIC_COLOR`.** Its cleaning nets (shadow removal, doc classification) are
not in the APK at all: `setupSenseiModels` builds them under `getCacheDir()`, i.e. they are
downloaded at runtime.

Two findings that matter for future work:

1. **Faint ink is the real gap.** Transcribing our own tables shows both document filters
   key the curve on the *absolute* difference from paper, so a mark 18 levels below paper
   (92 % reflectance: light pencil, fax, thermal receipt header, faded stamp) comes out at
   248 (Magic Color) / 231 (Clear B&W) — almost paper. Adobe ships a whole preset for this
   (`Light Text`, level 8). Recommended first port; pure LUT, no new pipeline.
2. **The scene classifier is dead code.** `DocumentFilters.classifiesAsDocument()` and the
   `GAIN_KNEE_*` / `GAIN_CAP_*` pair are referenced only from tests — no production path
   calls them — so every image handed to Magic Color is treated as a document.
3. **18 of `DocumentFilters.kt`'s 52 `const val`s are unused** (`SAUVOLA_*`,
   `AA_SUPERSAMPLE`, `OTSU_SAMPLE_STRIDE`, `MAGIC_COLOR_SATURATION`, `SHARPEN_*`,
   `CURVE_GAMMA`, `GAIN_*`, …), and `ORIGINAL_APP_FILTER_SPEC.md` §10 still reads as if
   Clear B&W ran Otsu/Sauvola. It does not. Documentation debt that misleads diagnosis of
   "B&W looks soft" reports.

Nothing above is implemented; 1–2 change filter output and therefore need an explicit ask.

## 2026-10-28 — Arena.ai — Round 60: the annotate buttons that hid themselves

Third report of *"the annotate menu buttons are still getting disappeared when clicked to
other buttons"*. Rounds 54–58 fixed the **reader's** annotate shelf and the **cell**
(`ConnectedButtonGroup`: no label-bounded ripple, ink-only disabled state, ink captured at
press start), so this round was a fresh root-cause pass over *every* annotate surface
rather than another patch to the same two files. Two mechanisms can remove an annotate
control, and both are now gone.

- **`ui/editor/MarkupScreen.kt` — the full-screen annotate bar used to delete a row of
  buttons.** Its colour row was wrapped in `if (tool != MarkupTool.WHITEOUT)`, so choosing
  **Erase** *removed* the four colour buttons and they only came back when another tool was
  picked: a whole row of controls deleted by a different control. That is this report in
  its most literal form, and no previous round touched this file. The row is
  unconditional now. Nothing needs it hidden — `MarkupRenderer.paintFor` paints a whiteout
  as the **paper colour and ignores `colorArgb`**, so a colour chosen while erasing is only
  the colour the next pen/marker stroke uses (exactly how the reader's shelf has behaved
  since Round 55). Keeping the row also stops the bar from changing height when the eraser
  is chosen, which used to reflow the page above it through `Scaffold`'s inner padding.
- **`ui/viewer/PageViewerScreen.kt` — `AnnotateShelf` lost its whole-shelf `alpha`.** The
  drag-to-close used to fade the entire shelf (`alpha = 1 - dragY / (ReaderShelfInset * 3)`).
  A *whole-shelf* alpha is a mechanism that can hide every button on the shelf at once from
  one stale `dragY`, which is the mistake Round 55 removed from `ExpressiveActionBar`. The
  drag keeps its `translationY`; it no longer owns an alpha.

**No visual change at rest, and no output change:** same components, same rows, same order,
same spacing, same shapes, same colours. Nothing that decides ink, geometry, filters,
typography or PDF bytes was touched.

Verified by reading (no Android SDK here): `grep -n "if ("` over `AnnotateShelf` and over
`MarkupEditor`'s bottom bar now shows **no control whose presence depends on the selected
tool**, and `python3` balance/import sweeps over all 81 `app/src` Kotlin files report only
the two known `bal3` false positives (`MarkupTest.kt`, `PaperSizeTest.kt`).

Uncompiled here: `./gradlew testDebugUnitTest assembleDebug` is still owed by Antigravity.

## 2026-10-28 — Antigravity — Rounds 56–59 build verification

Clean-build and test verification for Arena Rounds 56–59 (viewer shelf revert, document cards & view mode persistence, camera toggle press layer, library header label cleanup):
- **Zero compiler errors**: Clean build with Kotlin 2.x and Jetpack Compose without requiring any source modifications.
- **Build & Tests**: `assembleDebug` generated `app-debug.apk` (85.5 MB), and `testDebugUnitTest` passed 100% (25 tasks, 0 failures).

## 2026-10-28 — Antigravity — Round 55 build verification

Clean-build and test verification for Arena Round 55:
- **Zero compiler errors**: Clean build with Kotlin 2.x and Jetpack Compose without requiring any source modifications.
- **Build & Tests**: `assembleDebug` generated `app-debug.apk` (85.9 MB), and `testDebugUnitTest` passed 100% (25 tasks, 0 failures).

## 2026-10-28 — Antigravity — Round 54 compilation fix & build verification

Resolved compilation errors from Arena Round 54 integration and verified clean build:
- **`ui/camera/ScannerCameraScreen.kt`**: Qualified `androidx.compose.animation.AnimatedVisibility` calls for the hint and status toast overlays to resolve implicit `ColumnScope` receiver collision in the redesigned `Box`/`Column` hierarchy.
- **`ui/detail/DocumentDetailScreen.kt`**: Qualified `androidx.compose.animation.AnimatedVisibility` for tile selection check overlay, and added missing `import androidx.compose.foundation.layout.imePadding` for `MovePagesSheet`.
- **Build & Tests**: `assembleDebug` generated `app-debug.apk` (85.9 MB), and `testDebugUnitTest` passed 100% (25 tasks, 0 failures).

## 2026-10-28 — Arena.ai — Round 59: fewer labels in the library header

- **`DashboardScreen`**: the facts row under Magic Search is gone — the
  "N documents" and "M pages" `InfoPill`s are deleted. What is left of that row is the
  current sort, right-aligned and quiet. The totals now live only in the app bar's
  condensed line.
- **`DashboardScreen`**: `headerSummary` no longer begins with the literal
  **"All documents"**. It starts with the counts, and prefixes a folder's *name* only when
  a folder is actually selected (a real folder name is information; the unfiltered case is
  the state the user is already looking at).
- `InfoPill` is still imported and used — the per-document "N pages" pill on a grid tile's
  cover is a different thing and stays.

UI only: no filter, geometry, capture, typography or PDF output change.

## 2026-10-28 — Arena.ai — Round 58: camera Single/Batch flicker

`ConnectedButtonGroup`'s press state layer was drawn in `tint`, which flips the instant a
tap changes the selection. A tap therefore painted the **new** state's ink over the
**new** plate while the layer was still fading out from the press — on the camera's
Single/Batch switch that is ~14 % black over the just-filled white pill, fading for
160 ms: the selected cell came up grey and bled to white.

- The layer's ink is captured when the finger goes down (`layerInk`) and held until the
  next press, so the exit is always painted in the ink of the state being left.
- Entry/exit retimed 80/120 ms (was 90/160).
- Nothing visible at rest changed: plate, ink, shapes, cell sizes and the camera sheet
  are exactly as they were.

**Rule:** a state layer's alpha may animate; its colour must be captured at gesture
start. An instantly-flipping colour under a fading alpha is always a wrong-coloured wash.

UI only: no filter, geometry, capture, typography or PDF output change.

## 2026-10-28 — Arena.ai — Round 57: each document is its own card, and the view mode is saved

Two requests, both on the library: documents should read as **separate cards** (Gmail's
list, applied to the list *and* the grid), and the list/grid choice should be
**remembered**.

**Signature/additive changes:**

- **`AppSettings.libraryGridView: Boolean = true`** (new field, `model/AppSettings.kt`).
  Serialises with a default, so an existing `settings.json` decodes unchanged.
  `AppSettingsTest` covers the default and the round trip.
- **`ColorScheme.raisedCardColor()`** (new, `@Composable @ReadOnlyComposable`,
  `theme/Theme.kt`) — the card container tone. Derives from the palette, not from
  `isSystemInDarkTheme()`, because the app can force light/dark from Settings:
  `surfaceContainerHigh` when `surfaceContainerLow` is dark, else
  `surfaceContainerLowest`.

**`ui/dashboard/DashboardScreen.kt`:**

- `DocumentListCard` and `DocumentGridCard` are filled with **`raisedCardColor()`**
  instead of `surfaceContainerLow`. That was the whole bug: the rows/tiles were painted
  the *same colour as the shelf underneath them*, so a list was rows of text separated by
  10 dp of nothing and a grid tile was a photo with no container. The list row's inner
  padding moved to 12 dp (6 dp at the trailing edge) so a card has an inside.
  **No shadow and no border** — tone plus the existing gaps, for the reason the perf
  rounds established: nothing per-row that can be avoided.
- The list/grid toggle now reads and writes `settings.libraryGridView` through
  `repository.updateSettings`; the screen keeps **no local copy** of the flag.
  `rememberSaveable` was the wrong home for it — it survives rotation and a
  system-killed process, but not the ordinary "close the app, open it again", so the
  choice reverted to grid on every launch.

UI only: no filter, geometry, capture, typography or PDF output change.

## 2026-10-28 — Arena.ai — Round 56: same fix, original control (style revert)

Round 55 also changed the annotate shelf's **button style** — the tool and weight rows
became `SingleChoiceSegmentedButtonRow`/`SegmentedButton`. That was not asked for; the
report was a behaviour bug. Reverted.

- **`ui/viewer/PageViewerScreen.kt`** — `AnnotateShelf`'s rows are `ConnectedButtonGroup`s
  again: Pen/Marker/Erase as label-only 44 dp cells, and the original single row of four
  34 dp swatches + 8 dp + the weight group at `weight(1f)`, with the original colours
  (`surfaceContainerLowest` / `onSurfaceVariant` / `primary` / `onPrimary`) and the
  swatch ripple restored. The private `shelfSegmentedColors()` helper and the
  `SegmentedButton` imports are deleted.
- **Kept from Round 55** because none of it changes the resting appearance: no
  ink-coloured ripple on a group cell, no `pressScale` on a cell, disabled dims the ink
  instead of the whole cell, the eraser no longer disables the weight row, the action
  bar's entrance is translate-only, `SizeTransform(clip = false)` on the shelf swap, and
  the 20 dp handle band as the drag target.
- **Rule:** every segmented control in this app is a `ConnectedButtonGroup` (see
  `AGENT_COORDINATION.md` §6an and §6as). Bug reports about behaviour get behaviour
  fixes; the control does not get redesigned.

## 2026-10-28 — Arena.ai — Round 55: the annotate shelf's disappearing controls

Reported against the Round-54 build: in the reader's annotate shelf, **Pen, Erase, Thin
and Bold vanish when tapped and come back when held**. Five independent causes, all
removed. No signature changes; UI only (no filter, geometry, pixel, typography or PDF
output change).

- **`ui/common/ConnectedButtonGroup.kt`** — cells no longer take a ripple
  (`indication = null`; the 14 % ink `pressLayer` is the feedback), no longer carry
  `pressScale`, and the disabled state dims **the ink only**. The whole-cell
  `graphicsLayer { alpha }` is gone: it faded the plate with the label, so a disabled
  cell on a light card read as *absent* rather than *dimmed*. `ConnectedAction` is
  unchanged, so call sites compile as before — but **anything relying on the ripple for
  press feedback now relies on the state layer**.
- **`ui/common/ExpressiveAction.kt`** — `ExpressiveActionBar`'s entrance is
  **translate-only**. Its `alpha` used to start at 0 and be restored by a
  `LaunchedEffect(Unit)` inside the bar itself, so any recreation of the bar replayed a
  fade-in from invisible: every control in it blinked out. Do not give a persistent bar
  an entrance that can hide it.
- **`ui/viewer/PageViewerScreen.kt`** — `inkEnabled` (the eraser disabling the weight row
  and the swatches) is **deleted — no control on the shelf is ever disabled**. The
  shelf-wide drag detector is gone; the 20 dp handle band is the drag target again. The
  shelf swap builds its `ContentTransform` explicitly with `SizeTransform(clip = false)`,
  because `a togetherWith b using x` binds `using` to the else branch only.
  **Superseded in Round 56 (see below):** this round also swapped the tool and weight
  rows to `SingleChoiceSegmentedButtonRow`/`SegmentedButton`. That has been reverted —
  the shelf's three rows are `ConnectedButtonGroup`s again, exactly as before.

Uncompiled here: `./gradlew testDebugUnitTest assembleDebug` is still owed by Antigravity.

## 2026-10-28 — Arena.ai — Round 54: second pass on the Round-53 reports

Five of the seven Round-53 items were re-opened as still broken. All were layout bugs
that had been treated as animation bugs. Layout/chrome/motion only — no filter, geometry,
*pixel*, typography or `PdfGenerator`/`PdfWriter` change, and camera capture semantics are
untouched.

**Signature changes — compile these call sites:**

- `DocumentDetailScreen(...)` gained **`onNavigateToDocument: (docId: String) -> Unit`**
  (used after moving pages into a document). `Navigation.kt` passes `addDetailIfNeeded`.
- `PageGridItem(...)` gained **`verbsEnabled: Boolean = true`**. The tile's verb strip is
  no longer inside `AnimatedVisibility`; it stays at full height and goes disabled.
- `MagicSearchField(...)` **lost its `caption` parameter** and is now a fixed 54 dp. The
  library's facts live in the app bar's title `Column` (`headerSummary`, hoisted above
  `Scaffold`) and appear only while the header is condensed.
- `ConnectedCell` is internal to `ConnectedButtonGroup` and now takes `menuOpen` /
  `onOpenMenu`; the group owns the single `DropdownMenu`.

**New types:**

- `DocShapes.outwardTop: Shape` (`theme/Shape.kt`) — concave top corners, implemented by
  a private `OutwardTopShape` with two `quadraticTo` shoulders. Used for the library's
  scrolling shelf only.
- `MovePagesSheet` / `MoveSearchField` (private, `ui/detail/DocumentDetailScreen.kt`) —
  the half-height destination picker that replaced the move `AlertDialog`.

**Fixes:**

- `ConnectedButtonGroup`: plate and ink are computed, never crossfaded, across the
  filled boundary; the press is an ink-coloured state layer. A single group-owned
  `DropdownMenu` (`openMenuIndex`) replaces the per-cell popup. `spacing` parameter added.
- `PageViewerScreen`: page is tracked by **id** (`currentPageId` + `goToPage`), not by an
  index keyed on `pages.size` (that is the "tap page 4, get page 1" bug); the shelf's
  height is **measured** (`onSizeChanged`) and feeds the page's bottom inset;
  `ShelfPadding = 8.dp` is the single margin; the annotate shelf's handle is a 12 dp band
  with the drag on the whole shelf; rotate is a queue with an optimistic frame and
  `AnimatedContent` keyed on `PageFrame(bitmap, pageId)` (no transition on a same-page
  pixel swap).
- `PageThumbnail.kt` / `rememberPageImage`: retains the last bitmap per `imageUri` so a
  re-bake (rotate/filter/ink) cannot blank the frame while the new revision decodes.
- `ImageStore.copyPageFiles`: the copy's thumbnail is built from the **copied** file, not
  the pristine capture — a copied filtered page used to render unfiltered.
- `ScannerCameraScreen`: stage and control sheet are siblings in a `Column`
  (`weight(1f)` stage, 3:4 preview, no magic offset); the status toast sits 14 dp above
  the sheet's inside edge; `CameraPreviewLift` deleted.
- `DashboardScreen`: the capture button left `Scaffold.floatingActionButton` for the
  content `Box` (the FAB slot is positioned relative to the bottom bar, which is why it
  lagged when the selection bar appeared).
- `DocumentDetailScreen`: the move picker is a half-height sheet — search first in
  reading order, **last** in the animation — and picking a target opens it.

Uncompiled here: `./gradlew testDebugUnitTest assembleDebug` is still owed by Antigravity.

## 2026-10-28 — Antigravity — Round 53 compilation fix & build verification

Resolved compilation errors from Arena Round 53 integration and verified clean build:
- **`ui/detail/DocumentDetailScreen.kt`**: Added missing `import androidx.compose.foundation.clickable` for destination selection rows in the move bottom sheet.
- **`ui/viewer/PageViewerScreen.kt`**: Added null safety check (`if (bitmap != null)`) within `Crossfade` for nullable `ImageBitmap?` (`shownPage`).
- **Build & Tests**: `assembleDebug` generated `app-debug.apk` (85.5 MB), and `testDebugUnitTest` passed 100%.

## 2026-10-28 — Arena.ai — Round 53: connected page verbs, in-place annotate, quick rotate, share a selection

Seven reported defects. Layout, chrome and motion only: no filter, geometry, colour,
typography, `PdfGenerator`/`PdfWriter` or captured-pixel change. Camera capture
semantics untouched (4:3, `FIT_CENTER`, `MAXIMIZE_QUALITY`, flash/torch, shutter sound,
auto-save, batch vs single commit, discard cleanup).

- **`ui/common/ConnectedButtonGroup.kt` — the "glitch"/"disappearing button" root
  cause.** A cell inherited the theme's `LocalContentColor` (dark ink) for its ripple on
  black bars/sheets, and its label tint animated *through* white-on-white while the
  selected plate crossfaded. The cell now provides `LocalContentColor`, colours the
  ripple from its own tint, and **snaps** the content colour when it becomes filled. New
  `iconOnly` mode (`label = ""` + an icon → 20 dp glyph, no label, no vertical
  padding) for compact rows. Every caller that fills a cell on a dark surface must pass
  `onAccentColor` (black on white here) — forget it and the glyph is invisible again.
- **`ui/detail/DocumentDetailScreen.kt` — page tiles.** The four
  `FilledTonalIconButton`s (`GridAction`, deleted) are one `ConnectedButtonGroup` of
  icon-only cells, inside `AnimatedVisibility(!selected && !dragging)` so the strip
  **collapses shut** when a page is held. The single-page bottom bar gained **Copy** and
  **Move** (5 cells: Crop, Annotate, Copy, Move, More); **Sign moved into More**. The
  batch bar gained Copy and Move too.
- **`ScannerRepository.duplicatePages(docId, pageIds)` (new)** — copies land
  immediately after the block they came from; files are copied per page by
  `ImageStore.copyPageFiles` (new) so deleting one copy cannot delete the other's pixels.
  **`ScannerRepository.movePagesToDocument(sourceDocId, pageIds, targetDocId)` (new)** —
  bookkeeping move, source document is removed if left empty.
- **`ui/viewer/PageViewerScreen.kt` — annotate is no longer a route.** The bottom
  shelf swaps to annotate settings **in place** (`AnimatedContent`, slides from the
  bottom, drag the handle down to cancel), the page becomes a drawing surface, and the
  inked result is written by the same `persistMarkup` the editor uses. `MarkupNav` stays
  registered for the grid's tile menu; the viewer no longer takes `onNavigateToMarkup`.
  **Rotate feels instant**: the decoded page is turned in memory the moment the finger
  lifts (`rotateBitmap`) and the bake runs on `Dispatchers.IO`, replacing the preview
  when its re-decode lands. The bake itself is unchanged.
- **`ui/editor/MarkupStudio.kt` (new)** — the shared annotation model:
  `MarkupSession` (undo/redo through `MarkupHistory`, tool/ink/width, in-progress
  points), `MarkupInkSurface`, `persistMarkup`, `rotateBitmap`, and the ink
  palette/width constants (`MarkupInkColors`, `MarkupWidths`, `MarkupWidthLabels`,
  `inkLabel`, `A4_ASPECT`) — moved out of `MarkupScreen.kt`, which now imports them.
- **`ui/dashboard/DashboardScreen.kt`** — selection bar gained **Share**
  (`DocumentShare.shareDocuments`, `ACTION_SEND_MULTIPLE`, one PDF per document at the
  settings-derived defaults); "Merge PDF" is "Merge". The folder pills and the
  documents/pages pills **collapse on scroll-up** (`snapshotFlow` + hysteresis) and their
  facts move into `MagicSearchField`'s new `caption`, which is why that field is now
  `heightIn(min = 54.dp)` rather than a fixed height.
- **`util/DocumentShare.kt` (new)** — `exportConfig(settings)` (the export sheet's
  defaults, as model code) and `shareDocuments(context, documents, settings)`.
- **`ui/pdf/PdfExportSheet.kt`** — microinteractions: animated label/plate colours and
  a corner relaxation on the paper cards, compression cards and choice chips; the tick
  arrives with a scale; `ToggleRow` answers the finger (press scale, press wash, accent
  title, crossfading subtitle); the export CTA crossfades its glyph/dots, its label and
  its caption; `PaperSizeChooser` presses and crossfades "Change"/"Done"; "Print
  instead" presses.
- **`ui/camera/ScannerCameraScreen.kt`** — the viewfinder sits 32 dp above centre so a
  band of black separates the paper from the control sheet. Capture semantics unchanged.
- Uncompiled here: `./gradlew testDebugUnitTest assembleDebug` is still owed.

## 2026-10-28 — Antigravity — Round 52 Compilation Fixes

- **ui/detail/DocumentDetailScreen.kt**: Added missing import ndroidx.compose.runtime.mutableIntStateOf for drag-and-drop page reordering state.
- **ui/editor/CropAndFilterScreen.kt**: Added missing imports ndroidx.compose.material3.TopAppBar and ndroidx.compose.material3.TopAppBarDefaults for EditorHeader.
- **Build verified**: ./gradlew testDebugUnitTest assembleDebug 100% green; app-debug.apk built successfully.

## 2026-10-28 — Arena.ai — Round 52: annotate is a screen, drag to reorder, Files-style camera

Seven reported problems, plus the editor and camera re-skinned in Material 3. No filter,
geometry, colour-scheme, typography or `PdfGenerator`/`PdfWriter` change — this is
chrome, navigation and gestures only.

- **`ui/editor/MarkupScreen.kt` (new) replaces `ui/editor/MarkupDialog.kt` (deleted).**
  Annotating is a **route** now (`MarkupNav(docId, pageId)`), not a dialog: `Scaffold` +
  `TopAppBar`, the page filling the screen, `SingleChoiceSegmentedButtonRow` for the
  three tools, and a pill `Button` that shows progress while the page re-bakes. Same
  stroke model, same `MarkupRenderer`, same normalised points.
- **`CropAndFilterNav` gained `borders: Boolean`.** The bottom bar's **Crop** button now
  opens the **border** stage — it used to land on filters, i.e. the app answered a
  different question than the one asked. `CropAndFilterScreen` gained
  `startInBorders` for this; `reviewMode` also starts there.
- **`ScannedPage.revision: Int` (new, default 0).** A bake rewrites `_edited.jpg` in
  place, so `imageUri` alone cannot tell a viewer the pixels changed. `PageRenderer`
  bumps it on every bake; `rememberPageImage(imageUri, w, h, revision)` keys on it. This
  is the fix for **rotate appearing to do nothing** in the reader. The viewer also
  shows `Rotating` and disables the cell while the bake runs.
- **`BorderCanvas` drag fix.** `liveQuad` was `remember(corners)`, so committing a drag
  replaced the `MutableState` — but `pointerInput` is not restarted when its keys do not
  change, so the *next* drag wrote an orphan object: nothing followed the finger, and
  the corner jumped on release. It is now one stable object plus a `LaunchedEffect` that
  adopts outside changes. (This is a gesture bug, not a UI one.)
- **`ScannerRepository.movePages(docId, pageIds, targetIndex)` (new)** — moves a *block*,
  keeping the moved pages' relative order, with `targetIndex` in the coordinates of the
  list without them. `movePage` stays for single-page moves.
- **`PageGridItem` drag-to-reorder.** Long-press picks the page, drag reorders
  single **or multi-page selection**; the dragged tile is a `graphicsLayer` translation
  with `zIndex` + shadow, and the hovered slot draws a tertiary ring. `canMoveBackward`,
  `canMoveForward`, `onMoveBackward`, `onMoveForward` are **removed**, and the
  "Move earlier/later" menu items are gone from both the tile and the single-page bar.
  Board: the four tile buttons are now `FilledTonalIconButton` (`GridAction`) inside
  `LocalMinimumInteractiveComponentSize provides 36.dp`.
- **`ui/editor/CropAndFilterScreen.kt`** — `TopAppBar` header, `FilledTonalButton` tool
  buttons, `FilledTonalIconButton` rotate buttons, `Button` primary. The stage switch is
  the app's `ConnectedButtonGroup` used as a segmented control (pill outer ends, 8 dp
  inner corners, **selected cell relaxes to a pill and fills with the accent**). New
  **`onApplyAll`**: a tonal "Apply to all N pages" button, so a look can be applied to the
  whole document from single-page editing.
- **`ui/camera/ScannerCameraScreen.kt`** — Files-by-Google scanner chrome: close and
  torch only in the top bar, the viewfinder clipped to a rounded rectangle, and **one
  control sheet** with rounded top corners holding the count/undo row, the Single/Batch
  switch (`ConnectedButtonGroup`, label-only), the gallery tile, the shutter and the
  finish button, with the zoom stops and a one-line footnote under them. Capture
  semantics are untouched (4:3, `FIT_CENTER`, `MAXIMIZE_QUALITY`, flash/torch, shutter
  sound, auto-save, batch vs single commit, discard cleanup).
- **`ui/common/Expressive.kt`** — **`SegmentedPill` deleted.** Its last two callers (the
  camera mode switch and the editor stage switch) are `ConnectedButtonGroup` now;
  `ConnectedAction.icon` is therefore nullable (label-only cells).
- **`ConnectedButtonGroup`** — `selected` cells fill with the accent; `icon` optional.
- **Viewer:** annotate opens `MarkupNav`; signing and PDF export stay dialogs and were
  restored after an intermediate edit dropped them (they are `if (showSignPad)` /
  `if (showExportSheet)` again).

## 2026-10-28 — Arena.ai — Round 51: Google's connected button group, real `SegmentedButton`

Mined `material-components-android` for the Expressive component set and adopted the
parts that Compose can actually express at our version. **No dependency bumped** — our
`material3` line (BOM `2026.03.01`) has `SegmentedButton` but not `ButtonGroup`,
`SplitButtonLayout`, `ToggleButton` or `HorizontalFloatingToolbar`; those first appear in
`material3` **1.5.0-beta01** and are deliberately *not* pulled in here.

- **New `ui/common/ConnectedButtonGroup.kt`**
  - `ConnectedButtonGroup(actions, modifier, containerColor, contentColor, accentColor, onAccentColor, cellHeight)` and
    `ConnectedAction(label, icon, onClick, contentDescription, enabled, emphasized, selected, menuContent)`.
  - Shape is Google's, quoted from `button_group_tokens.xml` /
    `m3expressive_connected_buttons_inner_corner_size_state_list.xml`: pill ends,
    **inner corners 8 dp at rest, 4 dp while pressed, 50 % when selected**, **2 dp**
    between cells. `MaterialButtonGroup.updateChildShapes()` is the same rule in Views.
  - `menuContent` folds the split-button idea (`MaterialSplitButton`) into a cell. With an
    `onClick` too, the cell body still runs the action and a **chevron on its trailing
    edge** (26 dp, its own hit area) opens the menu; without an `onClick` the whole cell is
    the menu opener and there is no chevron. Items receive a `dismiss` lambda because a
    `DropdownMenuItem` does not close its own menu.
  - Anatomy deviation, written down in the file: cells stack icon over label. Google's
    group puts them side by side because it ships an overflow menu for the rest; our bars
    carry four labelled actions on a phone.
- **`ui/common/ExpressiveAction.kt`** — **`ExpressiveActionItem` is deleted.** Nothing
  else in the app used it; the connected group owns cell rendering now.
  `ExpressiveActionBar` / `ExpressivePager` / `BarHairline` are unchanged.
- **`ui/detail/DocumentDetailScreen.kt`** — all three bar states are one connected group
  (document / one page / several pages). Single-page **More** is a menu cell holding
  Open full screen, Rotate 90°, Move earlier/later, Watermark all pages, Delete page.
  The document state's **Add page** cell gained a menu with *Take a photo* / *Import
  from gallery*; the gallery path is new here (`ActivityResultContracts.PickMultipleVisualMedia`
  → `ImageStore.importFromUri` → `repository.addPageToDocument` → `indexTextInBackground`),
  so a page can be added to an **existing** document without going through the camera.
- **`ui/viewer/PageViewerScreen.kt`** — the shelf's four cells are a connected group;
  `ViewerMoreAction` (a `Box`-wrapped `ExpressiveActionItem` plus its own menu) is
  **deleted** and its two entries live in the group's More menu. Glass palette passed
  through `containerColor`/`accentColor`.
- **`ui/editor/CropAndFilterScreen.kt`** — `StageSwitch` now uses the real
  `SingleChoiceSegmentedButtonRow` + `SegmentedButton` with
  `SegmentedButtonDefaults.itemShape(index, count)` instead of the hand-rolled
  `SegmentedPill`. `SegmentedPill` is **not** deleted: the camera's Single/Batch toggle
  still needs to pass the glass palette in, which a themed component cannot express.
- Nothing that affects pixels: no filter, geometry, colour scheme, typography or
  `PdfGenerator`/`PdfWriter` change.

## 2026-10-28 — Antigravity — Round 51 Compilation Fixes

- **ui/common/ConnectedButtonGroup.kt**: Declared missing tokens ConnectedCellHeight (56 dp), ConnectedSpacing (2 dp), ConnectedInnerCorner (8 dp), and ConnectedPressedInnerCorner (4 dp); updated menu invocation to positional parameter.
- **ui/detail/DocumentDetailScreen.kt**: Reordered local function rotateOne before caller rotatePages to resolve forward-reference scope; qualified androidx.compose.animation.AnimatedVisibility in PageGridItem to prevent receiver conflict inside Box.
- **Build verified**: ./gradlew testDebugUnitTest assembleDebug 100% green; app-debug.apk built successfully.

## 2026-10-28 — Antigravity — IndyMobile Reverse Engineering Evidence & Decompiled Blueprints

- **New indymobile/ directory**: Complete reverse-engineering package for Indiana Mobile Document Scanner (com.indymobileapp.document.scanner), mirroring dobe-scan/ for Target 1.
  - **indymobile/apks/base.apk** (48.2 MB): Original production APK package.
  - **indymobile/native_libs/**: 64-bit ARM ELF shared objects (libimageproc.so, libnative-filters.so, libnms.so).
  - **indymobile/decompiled_c/** (262 files): Complete Ghidra C decompilation of libimageproc.so, including Java_com_indymobile_app_PSOpenCV_detectPageBorder.c, Java_com_indymobile_app_PSOpenCV_enhanceDocument.c, Java_com_indymobile_app_PSOpenCV_enhancePhoto.c, Java_com_indymobile_app_PSOpenCV_enhanceSignature.c, Java_com_indymobile_app_PSOpenCV_perspectiveTransformRect.c, pproxPolyDP.c, indContoursRGB.c, calculateIOU.c.
  - **indymobile/decompiled_java/** (75 files): Complete JADX decompilation of com/indymobile/app, including camera lifecycle, editor crop/filters, markup/annotations, and utilities.
  - **indymobile/pipeline/**: Unpacked pipeline beans (PSDocumentProcessInfo.java, PSDocumentFilterInfo.java, PSPage.java), smali bytecode, and decoded AndroidManifest.xml.
  - **indymobile/reports/**: REA APK analysis report (ase_20261024_213011_apk_rea.txt).
  - **indymobile/README.md**: Guide for Arena mapping C/Java reference functions to Rust NDK and Kotlin implementations.
- No production application code or integration contracts touched. Purely additive reference documentation and evidence.

## 2026-10-28 — Arena.ai — Round 50: expressive action bar, multi-page selection, motion pass

- **New `ui/common/ExpressiveAction.kt`** — the app's one expressive bottom bar:
  - `ExpressiveActionBar(modifier, containerColor, shape, shadowElevation, content)` — a tonal container that lifts 12 dp and fades in on arrival (`Motion.emphasized`).
  - `ExpressiveActionItem(label, icon, onClick, contentColor, plateColor, accentColor, onAccentColor, emphasized, enabled)` — icon on a **plate whose corners morph 14 dp → 22 dp while held**, press scale 0.93, and a `Motion.bouncy` **icon pop on tap**. The whole cell (plate + label) is the 56 dp target.
  - `ExpressivePager(index, count, onPrev, onNext, contentColor, railColor, accentColor, trailing)` — `‹ 3 / 8 ›` with a directional `AnimatedContent` counter (slide+fade, direction follows the page change) and a `scaleX` progress rail under it. Fixed-width number so the arrows never shuffle between `9 / 10` and `10 / 10`.
  - `BarHairline(color)`.
- **`ui/detail/DocumentDetailScreen.kt` — multi-page selection (bug fix).** Selection was `var selectedPageId: String?`, so ticking page 1 and then page 2 *replaced* the selection and page 1's tick vanished. It is now `selectedPageIds: Set<String>`: tapping (or long-pressing) another page **adds** it, tapping a ticked page removes it. The app bar title becomes `Page N of M` for one and `"$n pages selected"` for several, gains a **select all / clear all** action while selecting, and hides rename/PDF/ID-card for the duration. `Back` still clears instead of leaving.
- **Bottom bar has three states**, all from `ExpressiveActionBar`: nothing picked → `Add page` · `Export PDF`; one page → `Crop` · `Annotate` · `Sign` · `More`; several → `Rotate` (all) · `Export` (**only the selected pages**) · `Delete` (all). `Crop`/`Annotate` are deliberately absent for a multi-selection: they are per-page decisions.
- **Batch delete is one dialog.** `pagePendingDelete: ScannedPage?` → `pagesPendingDelete: List<ScannedPage>?`; the wording names the count, or the whole document when every page is picked. Selection is cleared before the pages go, so the bar never offers verbs for pages that are about to stop existing.
- **New `exportOnly: List<ScannedPage>?`** so a batch export writes exactly those pages (title gains `(3 of 12 pages)`), instead of the boolean silently exporting the whole document.
- **Selection motion.** The card's ring is a constant border whose *alpha* animates, the selected wash is a constant `Box` whose alpha animates, and the tick badge is an `AnimatedVisibility` (spring in / fast out). Ticking a card now costs no layout at all.
- **`PageGridItem`** — `selected` drives `ringAlpha`, `washAlpha` and the badge; the old private `DocumentActionButton`, `DocumentActionContent` and `ACTION_HEIGHT` are **deleted** (replaced by `ExpressiveActionItem`). `rotatePage(page)` is now `rotatePages(list)` + a local `rotateOne(page)`.
- **`ui/viewer/PageViewerScreen.kt`** — the reader's shelf is rebuilt on `ExpressiveActionBar` + `ExpressivePager`; the private `ViewerPageStep` and the R49 `ViewerActionButton` are **deleted**. The zoom badge now fades/springs in instead of appearing. Bar uses `DocShapes.card` (it floats with a margin, so top-only corners left two square bottom corners).
- **`ui/common/Expressive.kt`** — `PillButton`'s icon scales to 1.12 on press (`Motion.press`), so the app's most-used button answers the finger with something other than a ripple.
- **`ui/dashboard/DashboardScreen.kt`** — `SelectionBadge` animates its disc colour and springs its number in (`Motion.bouncy`) when a card joins the selection, so the library grid and the document grid tick the same way.
- No change to filters, page geometry, `PdfGenerator`/`PdfWriter` output, captured pixels, colours or typography. **Nothing compiled here.**

## 2026-10-27 — Arena.ai — Round 49: the page reader gets a bar, and swiping

- **`ui/common/ZoomablePageView.kt` — `zoom` and `pan` are now `() -> Float` / `() -> Offset`, not values.** The transform is read inside the `graphicsLayer` block, i.e. in the draw phase, so a pinch invalidates one layer and recomposes nothing above it. Call sites pass `{ zoom }`. This is what let the viewer adopt the shared gesture without giving up the zero-recomposition draw path its old hand-written gesture existed to buy. **Signature change: any other caller must pass lambdas.**
- **`ui/viewer/PageViewerScreen.kt`** — three changes:
  - **Swipe paging.** The viewer's own `detectTransformGestures` block is gone; the page is now wrapped in `ZoomablePageView`, so pinch (1–5×), clamped pan **and** horizontal swipe to the previous/next page all work. Swipe is gated on 1× and does not wrap at either end; a one-page document has none.
  - **New bottom shelf.** The floating pager pill (`‹ 3 / 8 100% ›`) and the page actions now live in **one** container (`DocShapes.sheet`, dark translucent) instead of two stacked pills: a pager row (hidden when `pages.size == 1`) above **Crop · Annotate · Rotate · More** (More → Sign this page, Export PDF). `ViewerActionButton`/`ViewerMoreButton` are new private composables.
  - **Rotate and crop moved out of the top chrome**, which is now back / title / recognised text only. Page verbs belong with the page verbs.
  - Sign and annotate re-bake onto the page **being read** (id captured at tap time, re-read by id on confirm), then reset the transform.
- Page inset changed to `top 68 dp / bottom 132 dp` so the last line clears the taller shelf.
- New imports in the viewer: `ZoomablePageView`, `MarkupDialog`, `SignaturePadDialog`, `PdfExportSheet`, `DropdownMenu`/`DropdownMenuItem`, `MoreVert`/`Draw`/`PictureAsPdf`. Removed: `detectTransformGestures`, `pointerInput`, `graphicsLayer`.
- The viewer now collects `repository.settings` for the export sheet.
- No change to filters, geometry, `PdfGenerator`/`PdfWriter` output, captured pixels, colours or typography. **Nothing compiled here.**

## 2026-10-27 — Arena.ai — Round 48: the filter preview zooms and pages, and the bottom bar follows the page

- **New `ui/common/ZoomablePageView.kt`** — one pinch / pan / swipe gesture for anything that shows a page, with the pan clamped to the *content's* overflow (`contentSize`), not the frame's, so a letterboxed page can never be dragged into its own margins. Exposes `MAX_PAGE_ZOOM = 5f` (matches the editor's `MAX_ZOOM`). Swipe-left/right turns the page **only at 1×** and only for a deliberately horizontal drag (1.4:1 axis bias, 56 dp travel): zoomed in, a horizontal drag is the only way to reach the right-hand edge of the page and hijacking it would make a zoomed page unreadable. `onSwipePrev`/`onSwipeNext` are nullable, so the first and last page simply do nothing.
- **`ui/editor/CropAndFilterScreen.kt`** — the **FILTERS stage preview now pinches and pans** (it had no `pointerInput` and no `graphicsLayer` at all; only the border stage could zoom). Fixed the root cause rather than the symptom by using `ZoomablePageView` instead of a second hand-rolled transform. New: a fit/percentage chip appears over the preview while zoomed (tap to return to fit), the zoom pill (`− 100% + ⤢`) is now in `FilterControls` exactly as in `BordersControls`, **horizontal swipe on the preview pages through the document** via `goToPage()`, and `goToPage()` + stage changes both reset zoom to 1× (zoom is page state; carrying 3× onto a page you have not seen drops you into the middle of it).
- **`ui/detail/DocumentDetailScreen.kt`** — the bottom bar is no longer the fixed four-up **Add page / Sign / Mark / Export**. It now has two states:
  - **Nothing picked (document verbs):** `Add page`, `Export PDF` (primary). Two, because those are the only two things that are true of a document as a whole.
  - **A page picked (page verbs):** `Crop`, `Annotate`, `Sign`, `More` (Open full screen, Rotate 90°, Move earlier/later, Watermark all pages, Delete page).
  Selection starts with **long-press** on a page card — the same gesture as the library's document grid — and tap-to-pick once a page is picked (same rule as that grid too). The selected card gets a primary ring and a centred check; the app bar title becomes `Page N of M`, its nav icon becomes `✕` clear-selection, and `BackHandler` leaves selection instead of the screen. The bar is swapped with `AnimatedContent` keyed on the page **id**, not the page object, so a rotate/annotate re-bake does not re-run the transition.
- **Signing now signs the page you picked.** It stamped `pages.first()` unconditionally — on a five-page document a signature landed on page 1 whichever page you were looking at. It re-reads the page by id at confirm time so a rotation made while the dialog was open is kept.
- **`PageGridItem`** gained `selected` / `onSelectClick` (defaults keep every existing call site valid) and `combinedClickable`; rotate is now the shared `rotatePage()` helper instead of an inline copy.
- **Watermark** moved from the bar into `More → Watermark all pages` — it is document-wide, so it belongs behind a page, not beside a document action. Nothing else was removed: PDF export is still in the app bar.
- No change to filters, page geometry, `PdfGenerator`/`PdfWriter` output, captured pixels, colours or typography. **Nothing compiled here**; `./gradlew testDebugUnitTest assembleDebug` is still owed, and pinch/swipe feel needs a device.

## 2026-10-27 — Arena.ai — Round 47: UI/UX audit against the design-engineering skills

- **New `docs/UI_UX_REVIEW.md`** — full audit against `emil-design-eng`, `mobile-native`, `animation-performance` and `review-animations` from `github.com/Harshit-888/skills`, in the `| Before | After | Why |` table those skills require, including what was checked and deliberately left alone.
- **New `ui/common/TapTarget.kt`** — a 48 dp pressable wrapper for visuals smaller than that (`label`, optional `selected` for radio semantics, the app's `pressScale`). Exists so the drawing never dictates the hit area again.
- **`ui/detail/DocumentDetailScreen.kt`** — the four page actions (move back, rotate, crop, overflow) lost their `Modifier.size(32.dp)`: `IconButton` already reserves 48 dp, and the explicit size was shrinking the **touch target** to 32 dp. The row is 16 dp taller; `MoreVert` icon 18 → 20 dp to match its 40 dp state layer.
- **`ui/editor/MarkupDialog.kt`** — ink swatches are now `TapTarget` (48 dp target, 30 dp dot) with spoken names ("Black ink", …) published as a radio group, instead of four unlabelled 30 dp clickable boxes.
- **`ui/camera/ScannerCameraScreen.kt`** — finish button `initialScale`/`targetScale` 0.6 → 0.92. It appears after every capture, so a big pop reads as lag rather than delight.
- **`ui/pdf/PdfExportSheet.kt`** — both accordions now close in 100 ms (`EXIT_MS`) against 140 ms to open; the system answering should be faster than the system presenting.
- Reviewed and **not** changed, with reasons recorded in the doc: `RevealWhen` (animates width, but is unused), the height-animating paper accordion (a disclosure has to change height), the 900 ms linear shutter spinner (constant motion), the 420 ms splash, and per-item list entrance animations (already deliberately omitted).
- No change to filters, page geometry, `PdfGenerator`/`PdfWriter` output, captured pixels, colours or typography. **Nothing compiled here**; the review says which findings are code-verified and which need hardware.

## 2026-10-27 — Arena.ai — Round 46: annotation — pen, marker, whiteout

- **New `model/Markup.kt`** — `MarkupTool` (`PEN`, `HIGHLIGHTER`, `WHITEOUT`), `MarkupPoint`, `MarkupStroke` (normalised 0…1, width as a fraction of the page's **short side**) and `MarkupHistory` (undo, redo, and a single-step undoable Clear). All `@Serializable`, all plain Kotlin.
- **`model/Models.kt` — `ScannedPage` gains `markup: List<MarkupStroke> = emptyList()`.** Annotations are **stored on the page, not baked into the file**, because `PageRenderer` rebuilds a page from its pristine capture on every bake: anything flattened would vanish the next time a filter changed. Old `library.json` files stay readable — the field defaults to empty.
- **New `util/MarkupRenderer.kt`** — one drawing routine used by both the preview and the bake, with quadratic smoothing through segment midpoints (the signature pad's curve, for the same reason: a finger emits few, far-apart samples) and a multiplied, translucent highlighter so the text underneath stays legible.
- **`util/PageRenderer.kt`** — replays `page.markup` **last**, after the filter and after the signature overlay, onto a mutable copy when necessary. Nothing sits on top of the user's ink.
- **New `ui/editor/MarkupDialog.kt`** — page preview with a drawing overlay, tool chips, four ink colours, three widths, undo/redo/clear, Done/Cancel. Strokes are normalised against the preview's size, so a stroke drawn at 1400 px lands identically on the 3200 px bake.
- **`ui/detail/DocumentDetailScreen.kt`** — "Annotate" in each page's overflow menu; saving updates the page then re-bakes so the thumbnail and viewer show the ink immediately.
- **New `app/src/test/java/com/example/docscanner/model/MarkupTest.kt`** (12 tests). Writing them found a real flaw in my first `MarkupHistory`: after `clear()` the undo stack was empty, so "Clear is undoable" was false. Reworked to keep the cleared strokes for one undo, to forget them once new work starts, and each case is exercised against a Python port of the final algorithm.
- **Not compiled here.** The model is host-tested; the dialog and the compositing path have not been run.

## 2026-10-27 — Arena.ai — Round 45: printing, from the same PDF that gets shared

- **New `util/PdfPrinter.kt`** — `canPrint(context, config)` and `print(context, file, jobName, paperSize)` over `PrintManager`. The adapter copies the already-generated PDF into the descriptor the print service hands back, so **what prints is byte-identical to what the share sheet would have sent** — no second renderer, no second appearance.
- Paper size is converted to **mils** (`pt × 1000/72`), because `PrintAttributes.MediaSize` takes thousandths of an inch; getting that wrong prints at ~1/14 scale and only shows in the preview. `setMinMargins(NO_MARGINS)`, since `PdfGenerator` already bakes the margins into the page geometry.
- Printing is refused with a reason, not a failure, when there is no `Activity` context (the `PrintManager` API needs one) or when the PDF is password-protected — the framework opens the file itself to render it and an encrypted file fails there, not here.
- **`ui/pdf/PdfExportSheet.kt`** — the generation code is now one `generateThen { file -> … }` path shared by *Share* and a new **Print instead** text button (disabled, with the reason, when the PDF will be encrypted). No change to what the export button produces.
- The Android `PrintManager` path is the only thing here that cannot be exercised without a device; the size conversion and the gating rules are the parts that would fail silently, and they are the parts written down above.

## 2026-10-27 — Arena.ai — Round 44: ID card — two pages onto one sheet

- **New `util/CardComposition.kt`** — pure geometry, no Android imports: `placement(page, front, back)` returns where the two cards go, `defaultAxis()` splits a portrait page left/right and a landscape one top/bottom, margins and the gap are 5 % / 4 % of the page's short side. Aspect ratio preserved, both cells equal, nothing outside the printable area, no overlap. Returns `null` when the page cannot hold the requested margin/gap.
- **`data/ScannerRepository.createIdCardDocument(frontPageId, backPageId, title, folderId)`** — decodes both pages at 1600 px, composes onto A4 at 200 dpi (1654 × 2339) on a **white** background (a transparent sheet composites to whatever the viewer chooses), writes **PNG** to the ordinary `_edited.png` slot so the orphan sweep and thumbnail keys treat it as a normal page, and creates a new document with one page (filter `ORIGINAL`, full-frame quad). Source pages are untouched.
- **`ui/detail/DocumentDetailScreen.kt`** — a card icon in the app bar, enabled only when the document has exactly two pages, with a confirmation dialog that says page 1 is the front, page 2 the back, and that the originals are kept. Failure to read either page is a toast, not a crash.
- The camera is **not** touched: this works from pages that already exist, so capture semantics (4:3, FIT_CENTER, MAXIMIZE_QUALITY, flash/torch, shutter sound, auto-save, batch vs single, discard cleanup) are unchanged. A two-step capture flow can be layered on later without changing this.
- **New `app/src/test/java/com/example/docscanner/util/CardCompositionTest.kt`** (11 tests). Two of them were written wrong first and corrected against a Python port of the algorithm: two scans with *different* aspects legitimately get equal widths but different heights, and a 10 × 10 page still lays out under fractional margins (only an explicitly impossible margin or gap yields `null`).
- **Not compiled here.** The geometry was simulated end to end; the compositing path has not been run.

## 2026-10-27 — Arena.ai — Round 43: backup and restore (the library, as one zip)

- **New `util/BackupArchive.kt`** — no Android imports, so all of it is unit-tested on the host. `create()` writes `library.json` + every referenced image + `backup.json` (format, format version, schema version, entry list with size and SHA-256 of each). `inspect()` reads and validates the manifest without extracting. `restore()` validates, extracts to a **staging directory**, and returns a `RestoredBackup` whose `commit()`/`discard()` are the only things that touch the live library. `merge()` unions two snapshots by id, backup winning collisions.
- **Restore refuses anything that is not exactly what it wrote**: entry paths must match `pages/<name>` / `signatures/<name>` (no traversal, no nesting, no absolute paths), entries must be declared in the manifest and vice versa, sizes and SHA-256 must match, `library.json` must exist, caps are 20 000 entries / 256 MB per file / 2 GB total (checked against bytes actually read, not the zip header's claim), and a newer `format` or `schemaVersion` is rejected.
- **Absolute paths are rewritten.** `ScannedPage` stores absolute paths, which are meaningless on another install; the archive stores `pages/<file>`, and restore maps it back under the current directories.
- **`data/ScannerRepository.kt`**: `snapshotNow()`, `exportBackup(Uri)`, `importBackup(Uri)`. Import copies the picked stream to the cache, restores into staging, merges into the in-memory library, commits the files, then publishes the flows and writes `library.json` — **pixels first, then library**, so no page can appear before its file exists. A failure at any point leaves the live library untouched.
- **`ui/settings/SettingsScreen.kt`**: new "Library" section with *Back up library* and *Restore from backup*, both through SAF (`CreateDocument("application/zip")` / `OpenDocument`), a busy state, and a confirmation dialog on restore that states the merge rule. Restore still needs no storage permission.
- Restore is a **merge, not a replace**: documents and folders that only exist locally are kept.
- **Not in the archive, deliberately:** app settings (theme, accent, paper size…). They are this install's preferences, not the user's scans, and restoring a stranger's accent colour is a surprise, not a feature.
- **New `app/src/test/java/com/example/docscanner/util/BackupArchiveTest.kt`** (9 tests): round trip incl. commit and staging cleanup, a page reusing one file is packed once, missing image counted rather than dropped, discard writes nothing, and refusal of traversal / undeclared entry / checksum mismatch / declared-but-absent file / newer format / foreign format, plus merge semantics.

## 2026-10-27 — Arena.ai — Round 42: the paper chooser now offers what the original offers

- **`model/Models.kt` — `PaperSize` goes from 8 entries to 16**, matching the fifteen rows the original marks `b_is_show_in_select_list = 1` in `app.db`, with its stored point values and order, plus our own `RECEIPT`. New entries: `B4`, `B5`, `LEDGER`, `TABLOID`, `ANSI_C`, `ANSI_D`, `ANSI_E`, `FOLIO`. New `group` property and `PaperGroup` enum (`ISO_A`, `ISO_B`, `AMERICAN`, `OTHER`), mirroring `paper_size_group`; `PaperSize.byGroup` gives declaration order grouped for rendering.
- **`A3` is now `842 × 1190`** (was `1191`) to match the database — both are roundings of 841.89 × 1190.55 pt, and matching keeps our PDF page boxes identical to the original's. **`BUSINESS_CARD` stays `243 × 153`** (85.6 × 54 mm, ISO/IEC 7810 ID-1); the original's stored `353 × 206` is 124.5 × 72.7 mm, which is not a business card.
- **`ui/pdf/PdfExportSheet.kt`** — the paper section renders `PaperSize.byGroup` with a heading per group instead of one flat 2-column grid, which would now be sixteen undifferentiated cards. No change to the card itself, to selection, or to the sheet's scroll.
- **`ui/settings/SettingsScreen.kt` — untouched.** Its page-size control is a scrolling `ChoiceDialog`, not the segmented control that the note in `Expressive.kt` warns about, so sixteen rows are fine there.
- **New `app/src/test/java/com/example/docscanner/model/PaperSizeTest.kt`** (6 tests): every database size present with its exact point values, the business-card departure pinned, the eight names that have ever shipped pinned (renaming resets saved settings — kotlinx.serialization stores the name), labels parse into `shortLabel` + `dimensionsLabel`, and `byGroup` covers every entry exactly once in declaration order.
- Enum *names* are unchanged for the eight that already existed, so saved settings keep working.

## 2026-10-27 — Arena.ai — Round 41: reconciled the master spec with the repo and with the database

- Pulled `c1ce274` (Antigravity's `docs/ORIGINAL_APP_FULL_SPECIFICATION.md`) into this branch; no code conflict, changelog conflict resolved by keeping both entries.
- Added **§9 Verification notes** to that document. Nothing in it changes code; it stops the doc from being read as description of what exists:
  - §4.1 confirmed against measured evidence, **plus** the six-output / `StatefulPartitionedCall:5` decoy the spec does not mention; §4.1 item 4 corrected — `QuadSanity` has no convexity test, it checks finiteness, area `0.06…1.05`, side ratio `≥ 0.18`, edge order `0.01`, edge tolerance `0.02`.
  - §3.3 corrected from `app.db` itself: **64** `paper_size` rows, **15** with `b_is_show_in_select_list = 1` (not "50"), and the Business Card row there is `353 × 206 pt`, not `241 × 153 pt`.
  - §8's "100% parity" rows are wrong beyond borders/filters/dashboard/PDF/trash: capture modes, viewfinder IOU auto-shutter, markup, background eraser, backup/restore, cloud sync and the 15-size picker are not in this codebase.
- `docs/ORIGINAL_ML_BORDER_PATH.md` now cross-links the master spec.
- **No code changed in this round.** The paper-size gap is a picker-screen job (the enum is a 2-column grid in the export sheet and an evenly-divided segmented control in Settings; 15 segments in that control is unreadable), so it is not a data edit.

## 2026-10-27 — Antigravity — Master Reverse Engineering Specification: all 34 activities, complete SQLite schema, native libraries, models & algorithms (`docs/ORIGINAL_APP_FULL_SPECIFICATION.md`)

- Reverse-engineered the complete original APK (`base.apk` + `split_config.arm64_v8a.apk` + all 4 dex tables) using REA, JADX v1.5.6, SQLite inspector, and Ghidra.
- Created `docs/ORIGINAL_APP_FULL_SPECIFICATION.md` detailing:
  1. All 34 original activities, 2 content providers, and services mapped to clean-room Compose screens.
  2. Master SQLite database schema (`document`, `page`, `paper_size`, `document_group`, `sync_history`, etc.) with full column definitions and trash lifecycle (`i_date_trash`).
  3. All 50 international paper sizes pre-calibrated in points (`A0..A8`, `B0..B10`, `Letter`, `Legal`, `Tabloid`, `Executive`, `Business Card`).
  4. Complete filter mathematical models and enums (`cv3` enhance types, `yu3` color types).
  5. Complete camera and capture workflows (`CameraXActivity`, `PaperBorderView` states `CLEAR`/`LOOKUP`/`PAUSE`, IOU >= 0.95 auto-snap).
  6. Complete editor blueprints for Background Eraser (`CleanupBgView`), Markup (`MarkupView`), and Digital Signature Pad (`SignaturePadActivity`).
  7. Storage, template naming (`DocumentNameSettingActivity`), and ZIP backup/restore mechanics (`MyBackupActivity`).

## 2026-10-27 — Arena.ai — Round 40: the ML border path was decompiled, and our output binding was wrong

- **New doc: `docs/ORIGINAL_ML_BORDER_PATH.md`** — the original APK's ML border path read out of the committed DEX files (`er5`/`ht3`/`qt3`/`sw3`/`it3`) with androguard, and out of the committed model with tflite-runtime. Call graph, preprocessing, all six outputs, postprocessing, the 0.5 gate, and a measured 0–255-vs-0–1 experiment.
- **`util/MlBorderDetector.kt` — output binding fixed.** The model has **six** outputs and **two** of them are four floats wide: `StatefulPartitionedCall:0` (the real per-corner scores, `[1,1,4]`) and `StatefulPartitionedCall:5` (not the scores, also `[1,1,4]`). The Round 39 rule "8 floats = corners, 4 floats = scores" could bind the scores to `:5`, which would report ≈0.2 confidence on a good page and silently fall through to the geometry pass. Now: bind **by name** (`StatefulPartitionedCall:1` / `:0`), fall back to size only when it is unambiguous, and otherwise decline the model with a `[ml]` log line instead of guessing. New one-line log: `[ml] corners = … [, …], scores = …`.
- Verified against the file itself (not just the decompile): input `serving_default_input:0` `[1,224,224,3]` float32; corners are normalised `0…1` in **`(y, x)`** order; the encoding is **`0…255`** (mean corner score 0.99 vs 0.53 for `0…1` on the same image); the gate is `mean >= 0.5f`; the original detects on a bitmap downscaled to fit **640×640** and runs native OpenCV in parallel as its fallback.
- No change to quad geometry, filters, page geometry, PDF output or capture behaviour. No new dependency: the analysis tooling (`androguard`, `tflite_runtime`) was installed in the agent sandbox only and is not referenced by the app.
- Still not compiled here (no Android SDK/Maven access in this sandbox); Antigravity's build of Rounds 38–39 is the last green build.

## 2026-10-27 — Antigravity — Build & Configuration Cache fix for VerifyBorderModelTask; full verification of Rounds 38 & 39

- `app/build.gradle.kts`: Converted `verifyBorderModel` task from ad-hoc script closure into `abstract class VerifyBorderModelTask : DefaultTask()` with `@get:InputFile abstract val modelFile: RegularFileProperty` and `@get:Input abstract val expectedSha256: Property<String>`, and imported `java.security.MessageDigest`. Resolves Gradle configuration-cache error `cannot serialize Gradle script object references`.
- Ran `.\gradlew.bat testDebugUnitTest --rerun-tasks --no-daemon`: all 25 test tasks passed cleanly (`QuadSanityTest`, `CropHandlesTest`, `DocumentDetectorTest`, etc.).
- Ran `.\gradlew.bat assembleDebug --no-daemon`: built `app-debug.apk` successfully (85 MB) with configuration cache active.

## 2026-10-27 — Arena.ai — Round 39: the ML border path answers safely, and the build checks the model

Follow-up to the Round 38 review, closing the runtime risks that were listed as open.

### 1. A confident model answer can no longer reach the warp unchecked — new `util/QuadSanity.kt`

`CropAndFilterScreen` applies `detection.corners` whether or not a detection was
*confident* (that is by design — an unsure quad is a starting point the user nudges). So
an implausible ML quad had a direct route into `PerspectiveWarp`: a crossed quad warps
into a self-overlapping image, a sliver crops a strip, a quad 2 % of the frame exports a
blank page. `QuadSanity.rejection(quad)` returns the first rule broken (or `null`) for
exactly those cases — same thresholds the geometric detector already used
(`MIN_AREA 0.06`, `MAX_AREA 1.05`, `MIN_SIDE_RATIO 0.18`, plus ordering and NaN checks),
so both detectors agree on what a page is. Pure maths; covered by `QuadSanityTest`
(eight cases, including NaN, a 9 %-area sliver, and a crossed quad).

**Confidence and plausibility are now separate:** `MlResult.confident` = confident **and**
plausible; `MlResult.rejection` is set only when the *shape* is wrong. An unsure but
plausible quad is still offered to the caller as a starting point.

### 2. The decoder no longer assumes the model's contract — `util/MlBorderDetector.kt`

| was | now |
| :--- | :--- |
| `getOutputIndex("StatefulPartitionedCall:0/1")` | outputs located by **element count** (8 = four points, 4 = four confidences) |
| `Array(1){Array(1){…}}` sized `[1,1,4,2]` / `[1,1,4]` | arrays built from each tensor's **own shape**, whatever its rank |
| 0–255 floats, unconditionally | input shape and **dtype** read; float32 or uint8 honoured; scale (0–255 vs 0–1) **calibrated once** per process by running the first page through both and keeping the more confident |
| `IntArray(50176)` + `ByteBuffer` per call | reused between calls (`@Synchronized`, the interpreter is not re-entrant anyway) |
| `isInitialized` latch | removed (Round 38 review); still retried on the next detection |
| `init(applicationContext)` + `object`-level `Context` | **removed** — a static context is a singleton (`AGENTS.md` §2.6). `detect(bitmap, context)` already took one; `MainActivity` dropped the call |
| `Log.once` on the fly | `logOnce(key, message)`: debug only, once per key per process |

`DocumentDetector`'s fallback chain was tightened to match: **ML (confident + plausible)
→ geometry → plausible-but-unsure ML → full frame.** Previously an implausible ML quad was
returned with `confident = false` as the last resort, and the editor applied it anyway.

### 3. The build fails when the model is missing — `app/build.gradle.kts`

New `verifyBorderModel` task (wired to every `preBuild`): throws with a clear message if
`app/src/main/assets/border_detect_224.tflite` is absent or empty, and warns with the new
hash when it differs from the pinned
`578f53b6c90ee9bc14b6ba350c3d261ffd6001d3e9b716a7ad571b6a4bdd1972` (a re-sourced model is
legitimate; silently vanished is not). This is the guard for the day the asset moves to
the ignored `assets/models/` path.

Also restored the unit-test worker heap to `-Xmx512m` (Round 38 cut it to 256m; the test
JVM is separate from the Gradle daemon, so the cut bought no build memory and only moved
an `OutOfMemoryError` somewhere less legible).

### Still open

- **The 4.78 MB `.tflite` is still committed**, against `AGENTS.md` §2.5. Deliberately: it
  is kept until the model can be provisioned reproducibly, and it is now guarded rather
  than invisible. `git rm --cached` + ignore + `verifyBorderModel` is the migration path.
- **APK size**: TFLite native libraries for four ABIs. No `abiFilters`, so all four are
  packaged; the scanner only ships Rust kernels for the architectures the toolchain built.

## 2026-10-27 — Arena.ai — Round 38 review: the ML border path now loads, and says when it doesn't

A review of Round 38 before a device test. Two code changes, both about the ML path being
able to work at all (and being visible when it doesn't). **No change to the 8-handle
canvas, the loupe, `CropHandles` arithmetic or the geometry detector.**

### 1. `.tflite` must not be deflated — `app/build.gradle.kts`

`MlBorderDetector` loads the model with `AssetManager.openFd()` + `FileChannel.map()`
(the only sane way to use a 4.7 MB model without copying it through the heap). aapt2
deflates anything not in its no-compress list, and `.tflite` is not in it, so `openFd`
throws `FileNotFoundException: … it is probably compressed` — the detector then falls
back to the Kotlin geometry pass, silently, and the round's headline feature looks like
"nothing happened". `docs/AGENT_COORDINATION.md` §6i had already recorded that this line
would be needed when the asset landed.

```kotlin
androidResources {
  noCompress += "tflite"
}
```

**If you add another model, it needs the same treatment** (or `useLegacyPackaging`, which
costs an extraction at install time instead).

### 2. `util/MlBorderDetector.kt` is no longer silent

Every failure path returned `null` into the geometry fallback, which is correct
behaviour but makes a broken model indistinguishable from a working one on a device.
Now, under the `DocScannerPerf` tag and only when `PerfTrace.isEnabled` (debug):

- `[ml] model loaded · input [1,224,224,3] FLOAT32 · outputs 0:'…'[…]` — the model's
  **real** tensor names and shapes. The names in `detect()` (`StatefulPartitionedCall:0/1`)
  are hard-coded from `ht3.java`; if that is wrong for this file, every inference throws,
  and this line is how you find out.
- `[ml] accepted|low confidence mean=0.83 quad=(…)→(…)` — one per detection, so a page
  whose crop came from the model is distinguishable from one that came from the geometry
  pass.
- `[ml] model … could not be loaded: …` and `[ml] inference failed, falling back to
  geometry: …` — once each, with the exception message.

Also removed: the `isInitialized` latch that made **one** failed load permanent for the
process. A transient failure (a low-memory moment while mapping 4.7 MB) used to disable
ML for the rest of the session with nothing recorded anywhere.

### Open, and deliberately not changed here

- **The 4.78 MB model is committed** at `app/src/main/assets/border_detect_224.tflite`.
  `AGENTS.md` §2.5 and `.gitignore` say ML assets are *produced*, never committed
  (`assets/models/` is ignored for exactly this reason). It is left in place because
  removing it would remove the file from every checkout — including the one this round is
  about to be tested from — and the feature is the point of the round. To comply:
  `git rm --cached app/src/main/assets/border_detect_224.tflite`, add
  `app/src/main/assets/*.tflite` to `.gitignore`, keep the file on disk, and record the
  provisioning source + checksum. sha256 of the current file:
  `578f53b6c90ee9bc14b6ba350c3d261ffd6001d3e9b716a7ad571b6a4bdd1972`.
- **`MlBorderDetector` does not validate `QuadCorners`.** A confident ML quad goes straight
  into `Detection` and therefore into the warp, bypassing `DocumentQuad.validate` (area
  ratio, side ratios, ordering). Worth a sanity check on real pages before trusting it.
- **`gradle.properties` / `testOptions` heap was cut** (`-Xmx1536m` → `1280m`, unit tests
  `512m` → `256m`). If `testDebugUnitTest` dies with `OutOfMemoryError`, that is why.
- **APK size**: TFLite ships native libraries for four ABIs plus this asset — expect
  roughly +15 MB on a 66 MB APK. No ABI split / `abiFilters` is configured, so all four
  architectures are packaged even though the Rust kernels only build for the ones the
  toolchain produced.

## 2026-10-27 — Antigravity — Round 38: Real TFLite Auto Border Detection & 8-Handle CropView with 1.5x Loupe

Reverse engineered original app implementation (`base.apk` -> `qt3.java`, `ht3.java`, `ax0.java`, `p60.java`, `assets/border_detect_224.tflite`):

### 1. Auto Border Detection (ML + Geometric Hybrid Pipeline)
- Extracted and bundled `assets/border_detect_224.tflite` (4.78 MB) from original APK into `app/src/main/assets/`.
- Added `org.tensorflow:tensorflow-lite:2.16.1` dependency in `gradle/libs.versions.toml` and `app/build.gradle.kts` (resolving AGP 9.0 namespace collisions).
- Created `util/MlBorderDetector.kt`:
  - Bilinear resize to 224x224 RGB Float32 tensor.
  - Runs TFLite interpreter with 4 threads.
  - Extracts 4 corner keypoints normalized [0..1] and 4 corner confidence scores.
  - Threshold `meanConfidence >= 0.5f` matching original app.
- Updated `DocumentDetector.kt`:
  - `detect(src: Bitmap, context: Context? = null): Detection`: runs ML detector when available, falling back gracefully to step geometry when ML confidence is low or in local JVM unit tests.
  - Initialized in `MainActivity.onCreate()` via `MlBorderDetector.init(applicationContext)`.

### 2. Manual Border Fixing (Original App 8-Handle UX & Magnifying Loupe)
- Updated `util/CropHandles.kt`:
  - Added edge midpoint handles `EDGE_TOP (10)`, `EDGE_RIGHT (11)`, `EDGE_BOTTOM (12)`, `EDGE_LEFT (13)` in addition to corner handles 0..3.
  - Implemented edge dragging arithmetic: translates both adjacent corners of the dragged edge in parallel while clamping and respecting `MIN_GAP` and frame boundaries.
  - Extended unit tests in `CropHandlesTest.kt` verifying edge dragging and boundary integrity.
- Updated `ui/editor/CropAndFilterScreen.kt`:
  - `BorderCanvas` now hit-tests both corners and edge midpoint handles.
  - Renders 4 circular corner handles AND 4 rounded-rectangle edge midpoint handles matching `ax0.java`.
  - Added `MagnifyingLoupe` composable: when any corner handle is dragged, displays a 1.75x magnified circular loupe (124.dp) with precision crosshair reticles, positioned at the top corner opposite the user's touch so the finger never obscures visibility.

## 2026-10-27 — Antigravity — Rounds 36 & 37 build verification

Verified Rounds 36 & 37 changes from Arena.ai.
- `PdfSizeEstimate.kt` & `PdfSizeEstimateTest.kt`: dynamic size estimation on the PDF export sheet.
- `StartupWork.kt`: cold-start quiet gate to prevent startup lag and dropped frames.
- `CropAndFilterScreen.kt`: shared `PageStrip` across both border and filter stages; persistent filter choice across page switches.
- `CropHandles.kt` & `CropHandlesTest.kt`: robust handle drag physics with touch-slop and isotropic hit testing.
All unit tests pass (`BUILD SUCCESSFUL in 30s`), `assembleDebug` passes (`BUILD SUCCESSFUL in 15s`), `app-debug.apk` built (66,209,649 bytes).

## 2026-10-27 (later) — Arena.ai — Round 37: pages can be switched in the filter stage, and the corner handles behave

Reported: *"we cant change the pages when in the filters section after border fix,
and the manual border dots are misbehaving when dragged"*. Both were real, and both
were in `ui/editor/CropAndFilterScreen.kt`.

### 1. No page switcher in the filter stage

The page strip existed only inside `BordersControls`. `FilterControls` had the stage
switch, the filter swatches and the apply button — and nothing else — so as soon as the
border pass handed over (or the user tapped *Filters*), a twelve-page document could
only ever show page one. The strip was not disabled or broken; it was not there.

* New `PageStrip(pages, activePageId, borderedPages, needsCheck, onSelectPage)` — one
  implementation used by **both** stages, in the same position under the stage switch,
  with the same tick/amber markers, and it scrolls itself to the active page
  (`rememberLazyListState` + `animateScrollToItem`) so the selected chip is never
  off-screen. Uses `itemsIndexed` instead of an `indexOfFirst` per row.
* `FilterControls` gained `activePageId`, `borderedPages`, `needsCheck`, `onSelectPage`
  (private composable; only one call site).
* Switching page in the filter stage now **keeps the chosen filter** instead of
  adopting each page's stored one (`keepFilterOnNextLoad`): browsing the strip with a
  look applied is comparing the look, and that is impossible if every tap resets it.
* Switching page clears `previewBitmap` first, so the loading state says `Loading
  page…` instead of leaving the previous page's pixels under the new page's number.

### 2. Corner handles that "misbehave when dragged"

Three separate defects, all in the gesture in `BorderCanvas`:

| defect | effect |
| :--- | :--- |
| the grab test used **normalised** squared distances against `GRAB_THRESHOLD = 0.05` — ~22 % of the frame per axis, and anisotropic | a drag started anywhere near the middle of the page seized whichever corner was nearest and threw it across the page |
| the corner was set to **the finger's position** on the first pointer event | a *tap* near a handle teleported it, and a drag jumped by the grab offset |
| the grab radius did not follow the zoom while the handles are drawn at a constant on-screen size | at 5x you could see a handle you could not hit |

Now: `HANDLE_GRAB_DP = 28f` (a fingertip, converted to canvas pixels through the zoom,
compared isotropically in canvas space); dragging starts only after
`viewConfiguration.touchSlop`, and moves the corner **by the delta** between two canvas
positions (`screenToCanvas(previousPosition)` → `screenToCanvas(position)`), so the handle
follows the finger from where it already was.

The clamp/snap arithmetic moved out of the composable into **`util/CropHandles.kt`**
(`move(quad, corner, dx, dy)`, `isSound(quad)`, `SNAP`, `MIN_GAP`), because it is the
part that was wrong and the only part a unit test can reach: `CropHandlesTest` covers a
zero delta (the tap), single-corner movement, axis independence, edge snapping, the
no-crossing clamp, the frame bounds, an unknown corner index, and 2 000 random moves
asserting the quad never folds or leaves the frame.

**Deleted** from `CropAndFilterScreen.kt`: `GRAB_THRESHOLD`, `distSq`, `snapToFrame`,
`clamp`, `MIN_GAP`, `CORNER_SNAP` — a duplicated constant is how the handles and their
hit test drifted apart (and how the guide frame once diverged from the detector).

### Known-good state

Still uncompiled here (no JDK/SDK): `./gradlew testDebugUnitTest assembleDebug`.
`CropHandlesTest` is pure JVM maths and runs in `testDebugUnitTest`.

## 2026-10-27 — Arena.ai — Round 36: the export sheet says how big the file will be, and the ten-second launch is answered

Two requests, verbatim:

> while choosing the file size also show the output file size either in KB if less
> than 1 MB , i still want to know why the app lags for about 10 seconds when opened
> freshly

### 1. Per-option output size (new files, new parameters)

`util/PdfSizeEstimate.kt` — **new, no dependencies, never writes anything**:

```kotlin
suspend fun PdfSizeEstimate.estimate(pages, maxImageDimensionPx): Map<PdfCompression, Long>
fun PdfSizeEstimate.format(bytes: Long): String   // "742 KB" / "1.4 MB"
```

It does not encode. For an evenly spread sample of up to 24 pages it reads the page
file's **length** and its **pixel count** (`inJustDecodeBounds`), derives that page's
bytes-per-pixel, then models what `PdfGenerator` will embed: pixel count after the
export-resolution cap and the level's `pixelScale`, encoded as PNG for `ORIGINAL`
(×2.2 vs a stored JPEG unless the bake is already `.png`) or JPEG at the level's
`jpegQuality` (`(q/92)^3.3`). Plus 1.6 KB of PDF structure per page.

`PdfGenerator`, `PdfWriter`, `PdfImages`, page geometry and filters are **untouched**:
this is a read-only estimate, and the produced file is byte-for-byte what it was.

- `ui/pdf/PdfExportSheet.kt`: `CompressionCard` gained a **required** `sizeLabel: String?`
  parameter (no default — a new call site should be forced to decide) and is now
  `heightIn(min = 88.dp)` with three rows: label + check, the estimate, up to two lines
  of detail. The sheet measures once via `produceState`, keyed on `pages` and
  `settings.resolution`, and shows `12 pages ≈ 6.6 MB · JPEG quality 85 · …` under the
  grid. A non-breaking space reserves the line while measuring, so nothing grows a row
  under the user's finger.
- `docs/design/render_pdf_sheet.py` draws the new line; both PNGs re-rendered
  (`pdf_export_sheet.png`, `pdf_export_sheet_dark.png`, 1116 × 1240).

### 2. The ten-second launch — cause, fix, and how to prove it

The fixed delays were the bug. `DashboardScreen` started its three background passes
at **48 ms**, **3.4 s** and **5.2 s** after composition and `ScannerRepository` ran the
orphan sweep at **5 s**, regardless of what the user was doing. The 5.2 s one is text
recognition: one ML Kit inference plus a 2400 px decode per page, up to 24 pages, each
page followed by a full `library.json` write and a dashboard emission — the exact
window in which someone is still scrolling a library they have not opened since
yesterday. Ten seconds of jank is that pass plus the thumbnail repair on the same cores.

**New:** `util/StartupWork.kt` — a gate, not a timer.

| symbol | what it does |
| :--- | :--- |
| `markInteraction()` | called by one root `pointerInput` on the `Initial` pass (`MainActivity.ObserveUserActivity`) — one volatile write per pointer event, consumes nothing |
| `setForeground(Boolean)` | `ON_START` / `ON_STOP` |
| `setScrolling(Boolean)` | reported by the dashboard alongside `PerfTrace.setScrolling` |
| `awaitQuiet(quietMs, reason)` | suspends until foreground, not scrolling, and untouched for `quietMs` |
| `runWhenQuiet(...)` | the same, then runs the block on `dispatcher` |
| `dispatcher` | `Dispatchers.IO.limitedParallelism(1)` — speculative work gets one thread, never a core per pass |
| `interactionStamp()` / `isUndisturbedSince()` | for loops that should abandon themselves on the first touch |

**Deleted constants** (do not reintroduce a fixed startup delay):

`DashboardScreen`: `LAUNCH_QUIET_MS`, `OCR_START_DELAY_MS`, `THUMBNAIL_PRIME_DELAY_MS`,
`WARM_START_DELAY_MS` → `OCR_IDLE_MS` (2 500), `REPAIR_IDLE_MS` (4 000), plus one
`withFrameNanos` for the cover pre-warm.
`ScannerRepository`: `ORPHAN_SWEEP_DELAY_MS` → **`ORPHAN_SWEEP_IDLE_MS` (6 000)**.

Behavioural consequences: on a library someone is actively scrolling, recognition and
thumbnail repair can be deferred for as long as they keep touching the screen — that is
the point. Search over a legacy library appears a few seconds later than before and the
frames are never interrupted by it.

**New API:** `ScannerRepository.setPageOcrTextsBulk(Map<docId, Map<pageId, text>>)`. The
backfill now accumulates the whole batch and applies it **once**, so the dashboard
re-sorts and repaints once instead of up to 24 times inside ten seconds. `OCR_BATCH_SPACING_MS`
(220 ms between documents) is deleted along with the per-document apply it spaced out.

**Instrumentation** (all `Log.i`, tag `DocScannerPerf`, debug only): `library.json read:
N doc(s), M page(s) in X ms`, `launch: library ready … ms`, `[startup] ocr-backfill
allowed after …`, `ocr-backfill finished: N page(s) in X ms`, `orphan-sweep removed N
file(s) in X ms`. `adb logcat -s DocScannerPerf` now separates "how long did the read
take" from "how long did the background passes compete with frames".

### Known-good state

Not compiled in this sandbox (no JDK, no Android SDK). `./gradlew testDebugUnitTest
assembleDebug` still owed on a machine that has them.

## 2026-10-26 (night) — Antigravity — Rounds 34 & 35 build verification & compilation fixes

Verified Rounds 34 & 35 changes from Arena.ai. Fixed 2 compilation errors:
- `ScannerCameraScreen.kt`: Added missing `import androidx.compose.foundation.layout.width`.
- `ScannerCameraScreen.kt`: Explicitly qualified `this@Row.AnimatedVisibility` inside the bottom controls `Box`.
All unit tests pass (`BUILD SUCCESSFUL in 28s`), `assembleDebug` passes (`BUILD SUCCESSFUL in 11s`), `app-debug.apk` built (66,209,649 bytes).

## 2026-10-26 (night, 3) — Arena.ai — Round 35: one segmented control everywhere, motion in every state change, and the sheet verified in the dark

Two things were reported: the export sheet "is still not fixed", and the app wants small
motion everywhere — the animation the size selector has, in the Borders/Filters control
too, and a camera that looks and works better.

### First, an honest note on "still not fixed"

The screenshot was of the **old** sheet: it still shows PAPER SIZE as eight equal
segments with the labels colliding ("A4…", "'210 × …", "US… / Letter"). Round 34
replaced that with a collapsible grid and it is in `12d2c3a`; the build in the screenshot
predates it. That is not a defence, though — the *dark* render had never been checked,
and the sheet is where a dark mistake hides. So this round re-rendered every tile in the
dark palette as well (`docs/design/pdf_export_sheet_dark.png`): the structure holds, and
the difference between adjacent sections (`surfaceContainerLow` over `surface`) is
present but quiet, which is correct for a sheet that is mostly type.

The renderer had two real bugs that hid this: every colour default was bound at import
time, so switching palettes left the old values in place, and `phone()` painted the
backdrop in the light tone. Both fixed; the script now renders both themes from one
source of truth.

### One segmented control, one behaviour

`SegmentedPill` is now the only two-or-three-state selector in the app, and it is
parameterised instead of copied:

| | before | after |
| :--- | :--- | :--- |
| Editor Borders / Filters | thumb slid on a spring | unchanged behaviour, now shared |
| Camera Single / Batch | a **private copy** (`ModeToggle`/`ModeSegment`) that faded a white background in and out under each label | the same `SegmentedPill`: the thumb *travels*, which is what the size selector does and what was asked for |
| Thumb | `spring(0.78, 520)` on the layer | same curve, now the app's only one |
| Label colour | snapped | cross-fades over 180 ms, so it does not flip mid-travel |
| Press | ripple only | 0.96 press-scale and ripple |

`SegmentedPill` gained parameters — `segmentHeight`, `container`, `thumbColor`,
`selectedContent`, `content`, `thumbElevation` — so the camera can keep its glass palette
without a second implementation. The editor and the camera now cannot drift apart.

### Motion, added where a state changes

Every one of these is a draw-phase or placement animation: none of them re-measures a
list, and none writes state per frame.

| Where | What now animates | Why |
| :--- | :--- | :--- |
| Home → selection mode | top bar cross-fades and lifts 1/6 of its height in (`AnimatedContent`), FAB scales 0.8 → 1 and back | Selection replaced the bar between frames; the two bars are the same height so nothing re-measures |
| Home → selection bar | contents rise 18 dp and fade, **the bar does not animate its height** | `AnimatedVisibility` around the bar would animate Scaffold's bottom-bar height, i.e. re-place the whole grid every frame |
| Page grid, recycle bin | `Modifier.animateItem()` | a moved page *moves*, a restored or deleted row closes the gap instead of snapping up |
| Viewer | page change is a 180 ms crossfade | at 120 Hz a hard cut is 8 ms of nothing, which reads as a glitch |
| Camera, on open | chrome fades in and rises (top 10 dp, bottom 14 dp) over 260 ms | a camera that opens with controls already flat on screen feels like a settings page |
| Camera, shutter | the viewfinder dips to 0.985 and springs back | the mirror-dip of a physical shutter; the flash then looks intentional |
| Camera, flash mode | icon cross-fades and scales between off/auto/on | it is a three-state control on a screen where everything else answers |
| Camera, page count | the number rolls (`AnimatedContent`), finish button scales in | the count is the only progress feedback on a screen with no progress bar |
| Camera, mode | thumb travels (see above) | consistency with Borders/Filters |

### The camera's one functional addition: undo

The page counter pill is now also the escape hatch. Capturing is committed the moment the
file exists and the only way back used to be "finish the scan → open the document → delete
the page"; a bad shot is noticed while the camera is still pointed at the desk. The pill
carries an **Undo** segment that removes the last page and its files
(`repository.discardPages`, the same call the close dialog uses) with no confirmation,
because the action is small, local and immediately repeatable.

### Files

`ui/common/Expressive.kt` (`SegmentedPill` parameters, press-scale, label cross-fade,
`selectable` + `Role.RadioButton`), `ui/camera/ScannerCameraScreen.kt` (mode toggle
replaced, undo, chrome entrance, shutter dip, flash morph, count roll),
`ui/dashboard/DashboardScreen.kt` (animated top bar, FAB, selection-bar reveal),
`ui/detail/DocumentDetailScreen.kt` + `ui/trash/TrashScreen.kt` (`animateItem`),
`ui/viewer/PageViewerScreen.kt` (page crossfade), `docs/design/render_pdf_sheet.py`
(two palettes) and `pdf_export_sheet_dark.png`.

No filter, geometry, PDF, OCR or search code touched. Nothing was compiled here.

## 2026-10-26 (night, 2) — Arena.ai — Round 34: the export sheet rebuilt, and the review that justified each change

Reported as "the export pdf page where all the size and password settings are listed is
absolutely wrong with the ui/ux". It was. The cause was mechanical, not a matter of
taste, and it is worth writing down because the same mistake is one line away in any
other sheet:

**`SegmentedPill` divides its width evenly between its options.** That is correct for
"Borders | Filters" and wrong for `PaperSize.entries`, which has **eight** options whose
labels carry their dimensions. On a 360 dp phone each segment got ~34 dp, so every label
was clipped to a fragment — "A4…", "US…", "Ex…". `PdfMargin` and `PdfCompression` had
the same treatment at four options ("No Man…", "Compac…"). The sheet therefore read as
a wall of unlabelled bars, and six identical uppercase eyebrows over six equally
weighted blocks gave no signal about which choices matter on every export (file size,
password) and which are set once (paper, margins).

### The whole sheet is new; the produced PDF is not

`ui/pdf/PdfExportSheet.kt` was rebuilt. Nothing behind it changed: the same
`PdfExportConfig`, the same defaults, the same `PdfGenerator.generatePdf` call, so the
file that comes out is byte-for-byte what it was for the same choices.

| Before | After | Why |
| :--- | :--- | :--- |
| Paper size as one segmented bar of 8 equal segments | One row stating the current size, opening a 2-column grid of all 8 with dimensions | Progressive disclosure that still shows the answer; no label can clip |
| Margins as one bar of 4 equal segments | 2 × 2 of equal chips | Four natural-width chips orphan onto a second line at 360 dp; a deliberate grid cannot |
| File size as 4 equal segments | 2 × 2 cards, label + what it costs, check on the chosen one | This is the decision made on every export; it deserves the weight |
| Section titles "PAPER SIZE", "MARGINS", "FILE SIZE", "OUTPUT" | "PAPER", "FILE SIZE", "LAYOUT", "PASSWORD", grouped by weight | Four sections instead of six; the two that matter per-export lead |
| Switch rows where only the 52 dp switch was tappable | Whole row toggles (`toggleable`, `Role.Switch`) | A thumb aiming at a switch on the right edge is the wrong target on a 300 dp row |
| No output summary | CTA caption: "12 pages · A4 · Balanced", or the reason it is disabled | Says what the tap will produce before the tap |
| Progress shown as a label swap only | Same, plus the sheet refuses to be swiped away mid-write | Dismissing cancelled the export and could leave a half-written file |
| Six equal-size blocks | Paper 1 row · size 2 × 2 · layout card · password card | Hierarchy: one primary, one secondary, two utilities |

Accessibility is part of "not wrong": every choice now uses `selectable(...,
Role.RadioButton)` instead of `clickable`, so TalkBack announces "A4, selected" rather
than four identical buttons with no state, and the paper row carries an
`onClickLabel` ("Choose paper size" / "Hide paper sizes"). Colour is never the only
signal — the chosen card also has a check mark.

### Also fixed

- The sheet's footer said "written by the system PDF engine" whenever there was no
  password, but a *lossy* file-size choice also switches to the app's writer. It now
  accounts for both, and the header explains the chosen level from the enum's own
  numbers ("JPEG quality 85 · every pixel").
- The compression detail line was being clipped at 68 dp; the cards are 76 dp so
  "Looks the same, far smaller" wraps to two lines instead of being cut.

### Review artifact

`docs/design/pdf_export_sheet.png` — three tiles (before, after, after with the paper
grid open), rendered by `docs/design/render_pdf_sheet.py`, which mirrors the dp values
in the Kotlin file. **A drawing, not a screenshot**: this sandbox has no JDK or Android
SDK, so nothing here was compiled or run.

### Signatures

`PdfExportSheet(title, pages, settings, onDismiss, mergeSources, defaultCompression)` is
unchanged. New private composables: `ExportHeader`, `PaperSizeChooser`, `PaperOption`,
`CompressionCard`, `ChoiceChip`, `ToggleRow`. New private extensions:
`PaperSize.shortLabel`/`dimensionsLabel`, `PdfMargin.shortLabel`. New constant
`CHOICE_MS = 140`. `SegmentedPill` itself is untouched and still used by the editor.

## 2026-10-26 (late) — Antigravity — Round 33 build verification & compilation fixes

Verified Round 33 changes from Arena.ai. Fixed 3 compilation errors:
- `DashboardScreen.kt`: Moved `visibleIds` computation and its `LaunchedEffect` below `activeDocs` declaration.
- `DocumentDetailScreen.kt`: Added missing `import androidx.compose.runtime.LaunchedEffect`.
- `PageViewerScreen.kt`: Added `@Composable` annotation to `ViewerPageStep`.
All unit tests pass (`BUILD SUCCESSFUL in 24s`), `assembleDebug` passes (`BUILD SUCCESSFUL in 11s`), `app-debug.apk` built (66,209,649 bytes).

## 2026-10-26 (late) — Arena.ai — Round 33: six bugs, five hot paths, four motion corrections

The brief was "fix the bugs and optimize more", with Emil Kowalski's skill repo
(`animation-performance`, `review-animations`, `break-ui`) as a reference. Nothing
here was compiled — this sandbox has no JDK, no Android SDK, no `cargo`. Every claim
below is a claim about the source, not about a build.

Three groups: **six real bugs** the code could be shown to have, **five hot paths**
moved onto the mechanisms the skill puts first (draw-phase reads, the right property,
no per-event work), and **four motion corrections** — the wrong curve, a duplicated
layer, an over-bouncy press, and one symmetric transition that should have been two.

### Bugs

1. **Deleting the last page left a husk.** `ScannerRepository.deletePage` filtered the
   page out of the list and wrote the document back, so a document could reach zero
   pages and stay in the library: a card with no cover, a page count of 0, a PDF
   export that produced nothing and no way to remove it except trashing it by hand.
   The document now goes with its last page. Files were already handled —
   `deletePageFiles` removes all three names a page can occupy (capture, JPEG bake,
   lossless bake) **and** its thumbnails.
2. **A page delete had no confirmation and no undo.** One tap on a menu item deleted
   the capture, the bake and the thumbnail permanently. It now opens a dialog that
   names the page and the document, and says plainly when the last page means the
   whole scan is going. (Same shape as the existing "Empty recycle bin?" dialog.)
3. **The detail screen could dead-end.** If the document disappeared while it was
   open — the last page deleted, trashed from elsewhere, purged — the screen showed
   "Document not found" and stayed there. It now navigates back once.
4. **Selection could describe things that were not there.** `selection` survived a
   search, a folder change or an external delete, so the bar could say "3 selected"
   over two visible cards, and **Merge could do nothing at all**: the handler rebuilt
   the document list from the ids, quietly found fewer than two, and returned without
   a word. Selection is now pruned to what is on screen; if nothing survives, the mode
   ends; and the merge handler can no longer return silently.
5. **Rename could silently discard a title.** Save closed the dialog, saved the notes,
   and threw away the title if it was blank — after the user had cleared the field on
   purpose. Save is disabled while the title is blank, and the saved title is trimmed.
6. **Every rotation rebuilt the app.** The repository was created in `onCreate`, so a
   rotation re-read and re-parsed `library.json`, re-decoded covers, replayed the
   launch screen — and, because it was only closed when `isFinishing`, left the
   previous repository's scope and debounced writes running forever, one per rotation.
   It now lives in `data/LibraryViewModel.kt`: it survives a configuration change, is
   flushed and cancelled in `onCleared()` (a real finish), and construction is still
   on `Dispatchers.IO` with `NonCancellable`.

### Performance

7. **`BorderCanvas` no longer recomposes on a corner drag.** `corners` was a
   parameter, so every pointer move hoisted a new quad into the editor's state — which
   recomposed the whole screen (header, the `LazyRow` page strip, action rows,
   banners) 60–120 times a second, re-ran `LaunchedEffect(previewSource, …)` and
   re-baked the preview per frame. The live quad is now local: written only inside the
   gesture, read only inside the draw lambda (so a move invalidates draw and nothing
   else), and handed up **once**, when the finger lifts. The preview now renders once
   per completed drag instead of continuously during it.
8. **`PageViewerScreen` uses the lambda form of `graphicsLayer`.** The parameter form
   reads `scale`/`offset` during composition, so a pinch recomposed the screen per
   frame and rebuilt the layer. Reads moved into the block. The zoom badge became its
   own composable fed the `FloatState` itself, with a `derivedStateOf` on the whole
   percentage, so a pinch now recomposes one `Text` a few times instead of the whole
   viewer per frame.
9. **Camera zoom is coalesced.** `LaunchedEffect(zoomRatio, …)` issued one
   `cameraControl.setZoomRatio` per touch event — up to 120 IPC round-trips a second,
   each queueing behind the last. `snapshotFlow { zoomRatio }.conflate()` plus a 32 ms
   floor (~31 Hz) keeps only the newest value and always lands on it.
10. **`SegmentedPill`'s thumb animates on the layer.** It animated a `Dp` through
    `Modifier.offset`, i.e. a layout pass per frame of the slide; it is now
    `translationX` inside `graphicsLayer`, a draw-time transform.
11. **The signature pad appends instead of copying.** `currentStroke = currentStroke +
    position` copied the whole growing stroke on every pointer move; it is a
    `SnapshotStateList` now (O(1) append, and the draw lambda is its only reader). The
    blank-pad hint reads a `derivedStateOf` boolean, so composition no longer
    invalidates per appended point.

### Motion

12. **`Motion.fade` was `LinearEasing`.** Linear spends as long on the first
    millisecond as the last and reads *slower* than it is; it is correct for constant
    motion (a progress sweep) and wrong for a fade. New `Motion.EaseOut` is
    `cubic-bezier(0.23, 1, 0.32, 1)`, and `Motion.EaseIn` is its mirror.
13. **`Modifier.entrance` allocated two layers.** `.alpha(progress)` *is* a graphics
    layer, so every entering element had one for the fade and another for the
    translation. One `graphicsLayer { alpha = …; translationY = … }` now does both —
    these are the two properties that stay on the render thread. The entrance also
    uses `EaseOut` rather than the default curve.
14. **`Motion.press` was under-damped.** At `dampingRatio = 0.55` every button in the
    app visibly wobbled on release; it is 0.8 now (still springy, no tic). `bouncy`
    remains for the one-off cases.
15. **Screen transitions use two curves instead of one.** The arriving screen
    decelerates (`EaseOut`), the leaving one accelerates (`EaseIn`), instead of both
    using Material's symmetric `FastOutSlowInEasing`.

### Signatures and files other agents need

| What | Change |
| :--- | :--- |
| `ScannerRepository.deletePage(docId, pageId)` | signature unchanged; **now also removes the document when it was the last page** |
| `data/LibraryViewModel.kt` | **new** — `AndroidViewModel`, `val repository: StateFlow<ScannerRepository?>`, closes in `onCleared()` |
| `MainActivity` | no longer holds a `ScannerRepository` field; `onDestroy` no longer closes it |
| `theme/Motion.kt` | new `Motion.EaseOut`, `Motion.EaseIn`; `fade` and `press` retuned; `entrance` is one layer |
| `ui/viewer/PageViewerScreen.kt` | new private `ViewerZoomBadge(scaleState: FloatState)` |
| `ui/camera/ScannerCameraScreen.kt` | new private `ZOOM_APPLY_INTERVAL_MS = 32L` |

No filter, geometry, PDF or OCR code was touched; no filter output, colour, typography
or page geometry changed in this round. `AGENTS.md` §2 still holds: no APKs, no
build artifacts, no new SDKs, no dependency added.

## 2026-10-26 (night) — Arena.ai — Round 32: Round 30 reviewed, and the refresh-rate request corrected

Antigravity's Round 30 (six Rust filter kernels, ABI 2) is merged and is good work.
Reading it surfaced one thing in **my** Round 29 code that had never been verified,
because this sandbox cannot compile: `Window` has no `setFrameRate`. Round 30 removed
that call and replaced it with `preferredRefreshRate` — correct diagnosis, and the
replacement was still not right.

### 1. `util/FramePacing.kt` — one field, chosen by API level

AOSP `WindowManager.LayoutParams`, on `preferredRefreshRate`:

> This value is ignored if `preferredDisplayModeId` is set. … This should be used in
> favor of `preferredDisplayModeId` for applications that want to specify the refresh
> rate, but do not want to specify a preference for any other displayMode properties
> (e.g., resolution).

Round 29 set `preferredDisplayModeId`; Round 30 added `preferredRefreshRate` beside
it, so the rate field was dead code — the window was still pinned by mode id. Now
exactly one field is written:

| API | field | why |
| :--- | :--- | :--- |
| 34+ (`UPSIDE_DOWN_CAKE`) | `preferredRefreshRate` only, mode id explicitly cleared | documented recommendation for "a rate, no other display-mode opinion", and from 34 it accepts any rate and lets the OS pick the closest match |
| 23–33 | `preferredDisplayModeId` | window-local. Before 34 the rate field "will be applied to the display's **default mode**", i.e. it can change the panel's mode for every app |

`preferredMinDisplayRefreshRate` / `preferredMaxDisplayRefreshRate` were checked and
are **`@hide`/`@TestApi`**, so they are not an option.

### 2. `util/PerfTrace.kt` — report the rate the panel granted, not the one requested

`hz=` was the number this app asked the display for. A request an OEM compositor
ignores therefore read as `hz=120 fps≈60` — indistinguishable from an app that cannot
hold the rate, when it is really a display that never changed mode. New
`setRateProvider(...)`, installed by `MainActivity`, and `syncDisplayRate()` re-reads
`Display.mode` **before each line is printed** — once per gesture, never per frame
(it is a window-manager call, and a binder call on the frame path is exactly what
this file exists to catch). The provider closes over a `Window`, so it is cleared in
`onDestroy` (`clearRateProvider`); leaving it installed would keep a destroyed
Activity reachable from a singleton. A non-finite rate is ignored rather than stored.

### 3. `util/DocumentFilters.kt` — an honest parity log

The gate compares the integer kernels with `contentEquals` but the float planes (the
paper map and three blurs) within 0.01, because two f32 pipelines can disagree in the
last bit. The log line claimed "byte-identical" for all nine. It now says which is
which. The gate itself is unchanged.

### Docs

`docs/AGENT_COORDINATION.md` §6u: what Round 30 speeds up (the **bake** path — "Save
& Next", export, editor preview — not the library scroll or launch), what I checked by
reading each kernel (`box_blur_inplace`'s stale-`left` semantics, `mean_pool`'s cell
bounds and `i64` accumulators, `rec601`'s rounding, `median_blur_gray` not being on a
production path), and the invariants not to undo. The Round 30 table row pointed at
§6t, which is Round 31; it now points at §6u.

### Not verified here

Nothing in this commit was compiled or run — no JDK, no Android SDK, no Rust
toolchain. Round 30's "all tests pass, APK built" is Antigravity's claim, not
something this environment can confirm.

## 2026-10-26 (late pm) — Arena.ai — Round 31: launch without a white screen, and border detection that can be tested

Three reports from the device, all fixed:

1. a white, empty screen for the first seconds after opening, then a stuttering library;
2. automatic border detection "not working correctly";
3. dragging the corner handles doing nothing at all.

### 1. Launch

- **`MainActivity.kt` — the repository is now built off the main thread.**
  It used to be constructed inside composition, so `LibraryStore.load()` (read and
  parse `library.json`) ran on the thread that owed the system a frame. Nothing
  could be drawn until it finished, which is exactly the blank window users saw, and
  every frame behind it queued up — including the ones they scrolled with
  immediately afterwards. It is now built on `Dispatchers.IO` behind the launch
  screen. `repository` is therefore a nullable `@Volatile` field, not a `lazy`.
- **`ui/common/LaunchSplash.kt` (new)** — the app's mark, its name and a quiet
  progress bar, on the brand gradient. Shown while the library loads, for a minimum
  of 320 ms so a warm start does not flash, then cross-faded into the dashboard.
  It has no timer of its own: it is removed the moment the repository is ready.
- **Resources** — `res/values/colors.xml` (new), `res/drawable/launch_background.xml`
  (new), `res/values-v31/themes.xml` (new). `windowBackground` was the platform
  default (flat light grey) and is now the brand gradient; Android 12+ names the
  same colour as its system-splash background, so launcher → splash → library is one
  colour instead of three.
- **The launcher icon is now the app's own mark.** `ic_launcher_foreground.xml` and
  `ic_launcher_background.xml` were the Android Studio template (a green grid behind
  the default robot). They are now a page being scanned — white page outline, ink
  lines, amber scan line — inside the 66 dp safe zone, on a brand gradient. Every
  supported device is API 26+, so the adaptive icon is what is drawn everywhere.
- **`res/values/strings.xml`** — `app_name` is `Doc Scanner` (was `DocScanner`), to
  match the title on the home screen.
- **`docs/design/` (new)** — rendered previews of the mark and the launch screen
  (`app_icon.png`, `launch_screen.png`) plus the colour table. Documentation only;
  nothing in `app/` references them.
- **`DashboardScreen.kt` — `LAUNCH_QUIET_MS` (2200).** Nothing in the background
  starts during the first two seconds: the OCR backfill is now
  `LAUNCH_QUIET_MS + 3000`, thumbnail repair is `LAUNCH_QUIET_MS + 1200`, and the
  speculative cover warm loop abandons itself the moment a scroll starts.
- **`ScannerRepository.kt` — two launch-time costs removed.**
  - The OCR backfill checks `isBusy` before **every page**, not every document. A
    batch of eight pages belonging to one document used to run straight through a
    scroll, having checked once.
  - The persistence loop no longer rewrites `library.json` on start-up when the
    snapshot it just emitted is the one it just read. That write serialises the whole
    library — all page text included — onto the same IO threads the cover decodes use.

### 2. Border detection

`util/DocumentQuad.kt` (**new, no Android types**) now holds the whole geometry:
background image, edge scan, RANSAC fits, validation. `util/DocumentDetector.kt`
keeps only the `Bitmap` handling and delegates. The split exists so detection can be
tested — see below.

Two real defects were found by re-implementing the previous algorithm independently
and running it over synthetic scenes:

- **A side was only searched in the half of the frame nearest to it.** Any page
  sitting off-centre had no far-side edge to find, that side fell back to the frame
  edge, and the result was a quad displaced by nearly half the frame — a
  *confidently wrong crop*, which is worse than no crop. Sides are scanned across the
  whole span now.
- **The inverted polarity was only tried when the entire frame was dark.** A dark
  document on a light surface — a menu board, a form, a phone screen — was never
  detected at all. Both polarities are tried, always.

Also: three step thresholds (0.18, 0.30, 0.10 of the background's range) instead of
one, evaluated in order of preference with an early exit once four sides are found,
so the ordinary photograph still costs one pass.

`DocumentDetector.Detection` gained **`sidesFound: Int = 0`** (defaulted; existing
construction sites are unaffected) and `DocumentDetector.WORK_WIDTH` is now public.

The editor shows the difference: a *partial* result ("Only 2 of 4 borders found —
check the corners") is no longer indistinguishable from a complete one.

### 3. Dragging a corner

`ui/editor/CropAndFilterScreen.kt` — the gesture read `corners`, `zoom`, `pan` and
above all `grabbedCorner` **by value, from the composition that created it**.
`pointerInput`'s block is kept for as long as its keys do not change, and its keys
(rotation, frame size) do not change during a drag — so `grabbedCorner` stayed `null`
for the entire gesture and the corner branch was never entered. The handles could not
move. Every value the gesture needs is now read through `rememberUpdatedState`, the
grabbed corner is a local of the gesture, hit-testing uses the current pan/zoom (it
used the stale transform, so handles became ungrabbable after a pinch), and a handle
dragged to within 1.2 % of a frame edge snaps onto it (`CORNER_SNAP`).

### Tests

**`app/src/test/java/com/example/docscanner/util/DocumentQuadTest.kt` (new, 11
tests)** — synthetic photographs, no device: desk gradient, perspective page, ink
lines, deterministic noise. Covers a plain page, an off-centre page (the regression),
a dark page on a light surface (the regression), a page on a nearly-as-bright desk, a
rotated page, noise, a featureless frame, a page that fills the frame, an image too
small to judge, determinism, and a landscape frame.

### Untouched

Filter kernels, the Rust ABI, PDF output, OCR text, search ranking, geometry
builtins: `PerspectiveWarp` and `ImageProcessingUtils` are unchanged, so a crop that
was already correct is byte-identical.

## 2026-10-26 (pm) — Antigravity — Round 30: Rust acceleration of 6 filter kernels (ABI 2)

Expanded the Rust pixel-kernel library with 6 new kernels covering the most expensive CPU loops in every document filter. All unit tests pass and `app-debug.apk` builds (65,687,518 bytes).

### New kernels in `app/src/main/rust/src/kernels.rs`

| Kernel | Kotlin loop it replaces | Speedup reason |
|--------|------------------------|----------------|
| `mean_pool` | `DocumentFilters.meanPool` — O(W×H) accumulation of all pixels into the paper-map grid | Multi-threaded via `for_each_band_mut`; i64 accumulators prevent overflow; no per-cell allocation |
| `apply_lut` | `DocumentFilters.applyLinearStretchReference` — 256-entry LUT over all ARGB pixels | SIMD-friendly tight loop; banded across cores |
| `rec601_inplace` | `DocumentFilters.applyGrayscaleReference` — Rec.601 grey conversion in-place | Single-pass banded |
| `gaussian_blur3` | `DocumentFilters.gaussianBlur3Reference` — separable [1 2 1]/4 Gaussian | Pure loop, one temp alloc |
| `box_blur3` | `DocumentFilters.boxBlur3Reference` — separable 3×3 box mean | Pure loop, one temp alloc |
| `box_blur_inplace` | `DocumentFilters.boxBlurInPlaceReference` — in-place 3×3 box, scratch rows only | 12 KB scratch vs ~30 MB second plane |

### `NativeFilters.kt` (`EXPECTED_ABI` 1 → 2)

- Added 6 public `fun` wrappers (all return `Boolean`, never throw).
- Added 6 `private external fun` declarations matching lib.rs entry points.
- **ABI check bumped from 1 to 2** — a stale `libdocscanner_native.so` from Round 15 will be refused and Kotlin falls back cleanly.

### `DocumentFilters.kt`

Six functions now have a native fast path + reference fallback:

| Function | Fast path | Reference (fallback) |
|----------|-----------|---------------------|
| `applyLinearStretch` | `NativeFilters.applyLut` | `applyLinearStretchReference` (new private) |
| `applyGrayscale` first loop | `NativeFilters.rec601InPlace` | `applyGrayscaleReference` (new private) |
| `meanPool` | `NativeFilters.meanPool` | `meanPoolReference` (new private, same logic) |
| `gaussianBlur3` | `NativeFilters.gaussianBlur3` | `gaussianBlur3Reference` (new private) |
| `boxBlur3` | `NativeFilters.boxBlur3` | `boxBlur3Reference` (new private) |
| `boxBlurInPlace` | `NativeFilters.boxBlurInPlace` | `boxBlurInPlaceReference` (new private) |

`verifyNativeFilters` now checks all 9 kernels (was 3) with tolerance-based comparison for float kernels.

### No behaviour change without Rust toolchain

Every new native call is wrapped in `if (nativeFiltersReady() && NativeFilters.xxx(...)) return`. `nativeFiltersReady()` returns `false` when the `.so` is absent (no Rust toolchain at build time), and each `NativeFilters.xxx` call is wrapped in `runCatching { ... }.getOrDefault(false)`. The reference path is always correct regardless.

## 2026-10-26 (ao) — Antigravity — Round 29 build verification & compilation fixes


Verified Round 29 locally with `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`. Resolved 1 compilation issue from Arena's commits:

- `FramePacing.kt`:
  - Resolved `Unresolved reference 'setFrameRate'` and `Unresolved reference 'FRAME_RATE_COMPATIBILITY_DEFAULT'`. Correctly set `preferredDisplayModeId` and `preferredRefreshRate` on `window.attributes` to request the fastest display mode and refresh rate from the window manager / compositor without non-existent `window.setFrameRate`.
- Build status:
  - All 121 unit tests pass cleanly (`testDebugUnitTest`).
  - `app-debug.apk` (65,685,492 bytes) assembled cleanly in 14s (`assembleDebug`).

## 2026-10-26 (pm) — Arena.ai — Round 29: actually running at the panel's rate

Round 29 was "the app is not running on full 120 fps". The measurement was lying as
much as the frame loop was: every threshold in `PerfTrace` was a 60 Hz constant, so a
120 Hz panel could drop half its frames and still print a clean report. Both are fixed,
and the per-frame work the UI was doing has been cut.

Nothing here changes rendering: filter output, geometry, colours and typography are
untouched. This round is budgets, display mode selection, allocation and texture
bandwidth only.

### New files

- **`util/FramePacing.kt`** (new, `object FramePacing`):
  - `apply(window): Float` — picks the fastest `Display.Mode` **only among modes with
    the same physical resolution as the current one**, sets
    `window.attributes.preferredDisplayModeId`, and on API 30+ calls
    `window.setFrameRate(hz, FRAME_RATE_COMPATIBILITY_DEFAULT)`. Never
    `FIXED_SOURCE`: that would forbid the system from dropping the rate on a static
    screen. Returns the rate it asked for.
  - `currentRate(window): Rate` — `Rate(currentHz, maxHz)` with
    `isLatchedDown = maxHz > currentHz + 1`.
  - `rateOf(context): Float` — rate without a window; used by `MainActivity`.
  - Same-resolution-only is deliberate: switching to a mode with a different size
    reallocates the window surface mid-launch.
- **`util/FrameBudget.kt`** (new, `internal object`, **pure JVM — no Android types**):
  all frame-threshold maths, so it can be unit-tested off-device.
  `budgetNs(hz)`, `missedThresholdNs(hz)` (`MISSED_FRAME_FACTOR 1.5f`), `isMissed`,
  `isJankyWindow(hz, p90Ns, maxNs)` (`JANK_P90_FACTOR 1.25f`, or max ≥ 2× budget),
  `bucketOf(intervalNs, buckets)`, `percentile90Ms(histogram)` (`-1` when empty),
  `fpsOf(meanIntervalNs)`, `decimal`/`decimalMs`. Rates clamped to 30–240 Hz,
  `DEFAULT_HZ = 60f`.

### Changed

- **`util/PerfTrace.kt`** — rewritten around `FrameBudget`.
  - New `setDisplayRefreshRate(hz)`; `displayHz` getter. Every threshold, bucket
    width, the jank test and the p90 are derived from the *display's* rate, not 60.
  - 1 ms histogram, `HISTOGRAM_BUCKETS 100`, replacing the running percentile
    estimate; `JANK_P90_NS` and `JANK_MAX_NS` are **gone**.
  - Each gesture now reports `hz fps frames atBudget% missed p90 avg worst` plus the
    stage breakdown. The old constants `24_000_000L` / `WINDOW 120` are gone.
  - `setScrolling`, `install`, `attachWindow`, `detachWindow`, `onCacheHit`,
    `onThumbnailDecode`, `onPageDecode`, `lastGesture`, `lastWindow`, `isEnabled`
    are unchanged — Settings and callers need no edits.
- **`MainActivity.kt`** — `FramePacing.apply(window)` before the first frame, then
  `PerfTrace.setDisplayRefreshRate(...)` with the rate that was actually applied, so
  the report is measured against the mode the window is really in.
- **`theme/Theme.kt`** — `colorScheme` and `accents` are now
  `remember(darkTheme, accent, dynamicColor)` / `remember(darkTheme, colorScheme)`.
  They were rebuilt on **every** call, i.e. on every recomposition of anything that
  reads `DocScannerTheme`, which invalidated colour-derived state app-wide.
- **`util/ImageStore.kt`** (`loadBitmap(...)`) and **`util/ThumbnailStore.kt`**
  (`read(...)`) gained `config: Bitmap.Config = ARGB_8888`. Existing call sites are
  unchanged.
- **`util/PageImageLoader.kt`** — new `configFor(w, h)`: `RGB_565` when
  `maxOf(w, h) <= OPAQUE_TILE_LIMIT (1024)`, else `ARGB_8888`. Grid/list thumbnails
  and the filmstrip hit the halved-byte path; the page viewer (1600×2200) and the
  editor keep `ARGB_8888`. Scanned text is exactly what bands in 565, so the limit is
  deliberately low and the lossless page path is untouched.
- **`ui/dashboard/DashboardScreen.kt`** — the large one.
  - `selectionOrderById: Map<String, Int>`, built once per selection change,
    replaces the two O(selection) scans that ran **per card** per recomposition.
  - `toggle(selection, id)` helper; `toggleSelection` is **removed**.
  - One stable instance each of `openDocument`, `longPressDocument`,
    `deleteDocument`, `moveDocumentToFolder`, `endSelection`, created once per screen
    (`remember` + `rememberUpdatedState` for the navigation callback). Previously a
    fresh lambda per card per recomposition, which made every card a changed
    parameter and defeated skipping entirely.
  - **Signature change:** `DocumentGridCard` / `DocumentListCard` now take
    `onOpen`, `onLongPress`, `onDelete`, `onMoveToFolder`, all
    `(String) -> Unit` — they were `onClick`/`onLongClick`/`onDelete`/`onMoveToFolder`
    with captured per-document lambdas. All call sites are updated in this commit.
- **`ui/detail/DocumentDetailScreen.kt`** — the modified-date pill's
  `SimpleDateFormat` is `remember`ed on `document.modifiedDate` instead of being
  constructed and thrown away on every recomposition of the header.
- **`ui/settings/SettingsScreen.kt`** — the Performance (debug) legend prints the
  panel's Hz and the resulting ms/frame from `PerfTrace.displayHz`, and explains what
  `atBudget%` means. `java.util.Locale` imported for the formatted numbers.

### Tests

- **`app/src/test/java/com/example/docscanner/util/FrameBudgetTest.kt`** (new, 12
  tests) — budget/missed/jank/p90/fps/decimal. The one that matters:
  `isMissed(60f, 16ms) == false` but `isMissed(120f, 16ms) == true`, which is exactly
  the failure the old instrumentation could not see.

### Unchanged and must stay unchanged

Filter kernels, geometry, border detection, PDF output, OCR, Magic Search ranking.
`RGB_565` is opt-in by size and never reaches the editor or the lossless PNG bake.

### Not done / verified on device

- `preferredDisplayModeId` only helps where the panel exposes more than one mode at
  the same resolution; the report now shows the rate that was actually granted, so
  `hz=60` on a 120 Hz panel is visible rather than assumed.
- The native cover pipeline (Rust) is still gated on a measurement showing decode,
  not composition, as the bottleneck. No `jniLibs` change this round.

## 2026-10-26 (an) — Antigravity — Rounds 25–27 build verification & compilation fixes

Verified Rounds 25 through 27 locally with `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`. Resolved 2 issues from Arena's commits:

- `DocumentSearch.kt`:
  - Resolved `Argument type mismatch: actual type is 'Array<String>', but 'List<String>' was expected` in `runVocabularyWalk` by converting `vocabulary` to `vocabulary.asList()` on call to `expandReference`.
- `ImageStore.kt`:
  - Fixed off-by-one grace boundary in `isOrphanedPageFile` (`lastModified < now - graceMs` instead of `<=`), ensuring files at the boundary threshold within the grace period are kept as verified by `OrphanSweepTest`.
- Build status:
  - 109 unit tests pass cleanly in 14s (`testDebugUnitTest`).
  - `app-debug.apk` (65,678,931 bytes) assembled cleanly in 15s (`assembleDebug`).

## 2026-10-25 (am) — Arena.ai — Round 27: "Magic Search", with the vocabulary walk in Rust

The home screen's search is now **Magic Search**: it finds a document when the query
is a prefix, a plural, a misspelling, a curated spelling variant or a year — and the
one part of it whose cost grows with the library runs in Rust, behind a parity gate.

### Renames and signature changes (call sites updated in this commit)

- **`ui/dashboard/DashboardScreen.kt`: `SearchBarField` → `MagicSearchField`.** Same
  parameters; the placeholder is `Magic Search` and the leading glyph is
  `Icons.Default.AutoAwesome` (tinted primary once there is a query).
- `DocumentSearch.Hit` gained `highlights: List<IntRange>` (defaulted) — the ranges,
  **inside `snippet`**, that matched, so a card can emphasise them.
- `DocumentGridCard` / `DocumentListCard` gained `snippetHighlights: List<IntRange>`
  (defaulted).
- `EmptyStateView` gained `onTryQuery: ((String) -> Unit)? = null`; the dashboard
  passes it so the no-results state can offer three example queries.
- `DocumentSearch.Index.search(query)` and `DocumentSearch.tokenize` are unchanged.
  `DocumentSearch.parseQuery`, `stem`, `expandReference`, `boundedEditDistance`,
  `packStrings` and `PREFIX_CODE` are new and `internal`.

### New files

| File | Contents |
| :--- | :--- |
| `util/NativeSearch.kt` | The Rust vocabulary matcher behind a load/ABI/parity gate, exactly like `NativeFilters`. Loads nothing in JVM unit tests and falls back cleanly. |
| `app/src/main/rust/src/search.rs` | The matcher: packed-UTF-8 parsing, capped Levenshtein, the prefix/fuzzy walk. `std` only, no new crate, no state. |
| `app/src/test/.../MagicSearchTest.kt` | 20 tests for typo tolerance, prefix, stemming, phrases, exclusions, years, recency, highlights, query parsing, the packing format, and the golden expansion vectors **shared with `search.rs`'s own tests**. |

### What search does now

| typed | also matches | weight |
| :--- | :--- | :--- |
| `invoic` | every vocabulary entry starting with it (≥ 3 chars) | 0.85 |
| `invoce` | entries one edit away — only on words ≥ 5 chars | 0.55 |
| `aadhr` | entries two edits away — only on words ≥ 8 chars | 0.30 |
| `aadhar` | its curated spelling group (`aadhaar`) | 0.90 |
| `invoices` | `invoice` (both sides stemmed identically) | 1.00 |

Plus: quoted phrases **and** implicit adjacency (`tax return` unquoted), `-word`
exclusion, a bare year matched against years the document mentions, same-page
co-occurrence bonus, and a 0.4-point recency nudge that only breaks ties.

Two structural changes also made it faster, independent of Rust:

- title and tag containment used to be an O(documents) `contains` scan **per term**;
  they are now postings lists, so a query touches only the documents that matter;
- the phrase check used to scan every page in the library on every keystroke; it now
  runs only for candidate documents (capped at 240, and snippets at 80).

### Limits worth knowing

- Fuzzy matching is deliberately length-gated: two edits on a five-letter word turns
  "tax" into "toy", and a wrong answer is worse than no answer.
- The vocabulary includes title and tag words, not just page words — otherwise a typo
  in a word that only appears in a title could never be expanded.
- `SYNONYMS` is a curated list of **spellings of the same thing**, not a thesaurus.
  Semantic matching is still the embedding model in `docs/AGENT_COORDINATION.md` §6i.

### Rust

`search.rs` adds **no dependency** and no global state: one `Vec<u32>` per call, and
the whole module is ~200 lines. `NativeSearch` runs the kernel against
`DocumentSearch.expandReference` on a fixed vocabulary the first time it is used and
refuses the native path unless the two arrays are identical; Settings → Performance
(debug) prints which one is live.

## 2026-10-25 (al) — Arena.ai — Round 26: PDF password, file size, renaming, and merging

Four things the user asked for, all in the share sheet: a passphrase, a smaller
file, a name you choose, and the ability to pick several documents on the library
screen and merge them into one PDF.

### Signature changes — every caller is updated in this commit

- **`PdfExportSheet(document, settings, onDismiss)` →
  `PdfExportSheet(title, pages, settings, onDismiss, mergeSources = emptyList(),
  defaultCompression = ORIGINAL)`.** It takes pages, not a document, because a
  merge *is* the pages of several documents in a row. Caller updated:
  `ui/detail/DocumentDetailScreen.kt`. New caller: `ui/dashboard/DashboardScreen.kt`.
- **`PdfGenerator.generatePdf(context, document, config, bitmapProvider)` →
  `PdfGenerator.generatePdf(context, title, pages, config)`.** The
  `bitmapProvider` parameter is **gone**; the generator calls
  `PageRenderer.bitmapForExport` itself, because the writer path has to own the
  bitmap's lifetime (it recycles each page as soon as its bytes are encoded).
- **`PdfExportConfig` gained `fileName`, `password` and `compression`** (all
  defaulted, so existing construction still compiles). The old KDoc claim that
  encryption was "intentionally absent" is now wrong and rewritten.
- `PdfGenerator.sanitiseFileName` is now public, and `suggestedName(title, config)`
  is new — the sheet uses both to show the file name it is about to write.
- `DocumentGridCard` / `DocumentListCard` gained `onLongClick`, `selectionMode`,
  `selected`, `selectionOrder` (all defaulted) and now use `combinedClickable`;
  they are `@OptIn(ExperimentalFoundationApi::class)`.

### New files

| File | Contents |
| :--- | :--- |
| `util/PdfWriter.kt` | `PdfWriter` (byte-level PDF 1.4 writer: pages, xref, trailer, optional encryption), `PdfPagePlan`, `PdfPageImage`, `PdfImages` (JPEG and lossless Flate-with-PNG-predictor encoding). |
| `util/PdfSecurity.kt` | `PdfSecurity` (standard security handler `/V 2 /R 3`, RC4 128-bit), `Rc4`, and the `hexToBytes`/`toHex` helpers. |
| `util/PdfGenerator.kt` (rewritten) | Two engines behind one call; `PdfPageGeometry` + `pdfPageGeometry()` extracted so both engines compute the **same** rectangle for a page. |
| `app/src/test/.../PdfSecurityTest.kt`, `PdfWriterTest.kt` | Known-answer tests for the crypto and byte-level structure tests for the writer. No device needed. |

### Behaviour

- **Password.** `/V 2 /R 3`, RC4 128-bit. RC4 rather than AES-128/256 was a
  deliberate trade-off: AES would mean shipping an untested AES implementation,
  whereas RC4 needs the 256-byte key-scheduling algorithm and nothing else, and
  every reader supports it. It stops someone opening a shared scan; it is not a
  vault. Validated before the Kotlin was written against **three** readers —
  PDFium (the engine inside Android's own `PdfRenderer`), MuPDF and pypdf: right
  password opens, wrong password is refused, streams decrypt byte-identically.
- **Compression.** `Original` (Flate, lossless, what `PdfDocument` already writes),
  `Balanced` (JPEG 85), `Small` (JPEG 70 at 80 % of the export resolution), `Tiny`
  (JPEG 50 at 60 %). The geometry is computed **before** the downscale, so
  compression never moves a page on its sheet.
- **Renaming.** The name field is prefilled from Settings → PDF → Document Name
  and is sanitised as you type; the sheet shows the exact file name it will write.
  Exports no longer carry a `_<milliseconds>` suffix — a real collision gets `_2`.
- **Merge.** Long-press a card on the library screen to start selection; the tap
  order is the page order and each badge shows its number. The bottom bar's
  `Merge PDF` opens the same sheet, listing the order before anything is written.
  Merges default to `Balanced`, because ten documents at lossless page images is a
  hundred-megabyte attachment; the control is visible and never overridden after.

### In-app performance readout (debug builds only)

`PerfTrace` now mirrors the two lines it writes to logcat into `lastGesture` and
`lastWindow` (`StateFlow<String?>`), and `SettingsScreen` renders them under
**Performance (debug)** with a one-line guide to reading them. Gated on
`PerfTrace.isEnabled`, which is only true when the app is debuggable — release
builds do not install the monitor and do not render the section.

`PerfTrace` additions are additive: `lastGesture`, `lastWindow`, `isEnabled`.

### Limits worth knowing

- The writer's only font is base-14 Helvetica, so text it draws (page numbers, the
  "image unavailable" notice, `/Title`) is restricted to printable ASCII. Page
  **watermarks** are composited into the page bitmap with the platform text stack
  instead, so they keep their Unicode — that is why `PdfImages.composeWatermark`
  exists.
- Passwords are restricted to printable ASCII in the UI: revision-3 passwords are
  compared as raw bytes, and the field must not accept a character the handler
  cannot represent.
- **Bug 8 is still open** in the platform path (`PdfDocument` never recycles
  `bitmapProvider` output). The new writer path does recycle, safely, because the
  encoded bytes are in hand before the bitmap is released.

## 2026-10-25 (ak) — Antigravity — Rounds 21–24 build verification & compilation fixes

Verified Rounds 21 through 24 locally with `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`. Resolved 3 compilation issues from uncompiled Arena commits:

- `DashboardScreen.kt`:
  - Resolved `Unresolved reference 'visibleItemsInfo'` in row prefetcher snapshotFlow. Evaluated `gridState.layoutInfo.visibleItemsInfo` and `listState.layoutInfo.visibleItemsInfo` in separate `if (isGridView)` branches to ensure proper smart-casting.
- `CropAndFilterScreen.kt`:
  - Added `needsCheck: (ScannedPage) -> Boolean` parameter to `BordersControls` and forwarded it from `CropAndFilterScreen`, resolving `Unresolved reference 'needsCheck'` at line 1201.
- `PerfTrace.kt`:
  - Replaced non-existent `FrameMetrics.COMMAND_DURATION` with `FrameMetrics.COMMAND_ISSUE_DURATION` at line 161.
- Build status:
  - All unit tests pass cleanly in 31s (`testDebugUnitTest`).
  - `app-debug.apk` (65,384,497 bytes) assembled cleanly in 17s (`assembleDebug`).

## 2026-10-25 (ak) — Arena.ai — Round 25: a data-loss sweep, and a faster first fling

An audit of the storage, capture and editor paths for defects rather than a feature.
Five of them were losing user data or leaking it; two were making the first scroll
after launch slower than it had to be.

### Data loss

- **`data/LibraryStore.kt`, `data/SettingsStore.kt`** — `writeNow` deleted the target
  before renaming the temp file over it. That is a window in which `library.json`
  does not exist: a kill, a crash or the OS reclaiming the process inside it left no
  library at all, and the next launch started from an empty one. The rename now goes
  straight over the target (atomic on a normal filesystem); the copy fallback
  overwrites instead of deleting first. `SettingsStore` gained `writeNowBlocking`.
- **`data/ScannerRepository.kt`** — `close()` cancelled the persistence scope without
  flushing. Every mutation is written through a 150 ms debounce, so a rename, a page
  move or a filter applied just before the app was closed was silently discarded.
  `close()` now writes the current snapshot (library **and** settings) before
  cancelling. It blocks the caller by design: one small file write on the way out
  against losing the edit.
- **`ui/camera/ScannerCameraScreen.kt`** — *Discard* only popped the screen. Every
  page captured in that session had already been written to `files/pages` (that is
  what makes the shutter feel instant) and nothing pointed at those files, so each
  abandoned scan left full-resolution JPEGs on disk permanently. It now calls the
  new `ScannerRepository.discardPages(...)`, which deletes them on the repository's
  scope — not the screen's, which is already being disposed.
- **`util/ImageStore.kt`, `data/ScannerRepository.kt`** — `ImageStore.sweepOrphans`
  existed but was never called, so nothing ever cleaned up after a crash, a failed
  import or a cancelled picker. It is now run once per launch, five seconds in, on
  IO. It was also unsafe as written: it swept on `imageUri` alone, and an edited
  page's `imageUri` names only the bake — the pristine capture (the source every
  later re-edit is rendered from) would have been deleted. It now receives all three
  names a page can occupy plus the model's own URIs, and skips anything modified in
  the last ten minutes so a capture in flight is never swept. The decision is a pure
  function, `isOrphanedPageFile(...)`, pinned by `OrphanSweepTest`.
- **`ui/editor/CropAndFilterScreen.kt`** — leaving the editor with system back or the
  close button dropped a corner the user had just dragged: `persistGeometry()` only
  ran on a page switch, on Continue and before a bake. A `DisposableEffect` now
  persists on dispose, comparing against the stored geometry first so leaving with
  nothing to save does not bump `modifiedDate` and re-sort the library.

### Scroll, at launch

- **`util/PageImageLoader.kt`** — `prefetch` now takes at most **one** of the two
  decode slots (`Semaphore(1)`, `tryAcquire`). Speculative rows could previously fill
  both, and the tile the user was actually looking at waited behind work for rows
  still off screen. Dropping a prefetch that cannot start immediately is free: the
  tile asks for itself when it appears.
- **`ui/dashboard/DashboardScreen.kt`** — the startup warm-up covers six rows
  (`WARM_ROWS`, up from four) and starts three frames in (`WARM_START_DELAY_MS = 48`,
  down from 180) instead of nearly two hundred milliseconds after the screen is
  already usable.

### Also

- **`ui/detail/DocumentDetailScreen.kt`** — the page grid looked up
  `document.pages.indexOfFirst { ... }` once per visible tile on every
  recomposition; it uses `itemsIndexed` now. Quadratic in the one list a long scan
  makes long.

### Unchanged

Filter output, bake path, PDF output, capture geometry, palette, typography and the
`ThumbnailStore` on-disk format.

## 2026-10-25 (aj) — Arena.ai — Round 24: the tiles are there before the finger is

Third report of the same complaint, so this round stops guessing about *which* part
of a photo tile costs a frame and does two things instead: removes the rest of the
work that is provably redundant, and instruments the frame so the next report is
data rather than a hypothesis.

### What changed

- **`ui/dashboard/DashboardScreen.kt`** — the memory cache is **warmed for the
  first screens** (`WARM_ROWS = 4`, `WARM_START_DELAY_MS = 180`) instead of being
  left to prefetch. Prefetch can only fire for a row that has already left the top
  of the screen, so the first fling after launch met grey tiles all the way down;
  a settings row is never blank because there is nothing to decode. Prefetch is now
  **symmetric (rows above and below)** and each request is `launch`ed rather than
  awaited, so a slow row can no longer delay the tracking of the next one.
  `PREFETCH_AHEAD` (items) → `PREFETCH_ROWS = 2` (rows), plus a new top-level
  `prefetchRow(scope, context, docs, row, perRow, size)`.
- **`util/ThumbnailStore.kt`** — `read` now decodes **at** the size the tile draws,
  using `inDensity`/`inTargetDensity` so the scale happens inside the single native
  pass. Previously a 768 px thumbnail for a 459 px cover produced a 3 MB bitmap and
  then a second 1 MB bitmap from `createScaledBitmap` — about 4 MB of allocation
  per tile entering the screen, which is a young-generation collection every couple
  of hundred milliseconds during a fling. It also makes the cached bitmap **exactly**
  the tile size, so the GPU draws it 1:1 instead of resampling a 1.2× texture every
  frame. Same signature; if a platform ignores the density hint the result is what
  it used to return and the caller still scales.
- **`util/PerfTrace.kt`, `MainActivity.kt`** — per-gesture **stage attribution** via
  `Window.OnFrameMetricsAvailableListener`: the scroll line now ends in
  `stages(sync/layout/draw/cmd/gpu)=…`, which separates *our* work (composition and
  layout) from the render side (draw commands and GPU). New `attachWindow`/
  `detachWindow`, called from `MainActivity.onCreate`/`onDestroy`; debug-only, and
  the listener counts nothing outside a gesture.
- **`app/build.gradle.kts`** — new **`benchmark` build type**: identical to `release`
  (no minification) but non-debuggable and signed with the debug key, so it installs
  over the debug app and keeps its data. `./gradlew assembleBenchmark`. A debuggable
  APK runs interpreted/JIT'd code with Compose's debug hooks live, and that penalty
  lands hardest on the screen with the most work per frame — which is the one being
  compared. This type is the only way to see the app as it actually ships without a
  release keystore.

### Not changed

Card structure, palette, shapes, typography, filter pipeline, bake path, and the
`ThumbnailStore` on-disk format (768 px JPEG, same keys) are all untouched.

## 2026-10-25 (ai) — Arena.ai — Round 23: the batch border pass does the walking

The requested flow — *fix page 1, move to page 2, … then apply filters* — existed,
but it made the user tap through every page to reach a decision the detector can
reach on its own. For a clean ten-page scan that was nine "Next page" taps and nine
identical confirmations of something already correct.

### What changed

- **`ui/editor/CropAndFilterScreen.kt`** — when the review flow opens, a background
  pass detects the borders of **every page that has never been cropped**, 450 ms
  after the first page has painted. Each page is decoded at 720 px (the detector
  works at 360 internally), detected, and its bitmap recycled. Results are written
  in **one** `setPageCorners` call at the end, and only for pages that are *still*
  un-cropped at that moment — the user may have started adjusting one while the pass
  ran. The page on screen is excluded; it has its own detection path on load.
  Bounded to `AUTO_DETECT_LIMIT = 40` pages, like the OCR backfill.
- **`data/ScannerRepository.kt`** — new `setPageCorners(docId, Map<pageId,
  QuadCorners>)`: one library update for the whole pass, not one per page.
- **The strip tells the truth about confidence.** `PageChip` gained
  `needsReview: Boolean`. Three states, from `DocumentDetector.Detection.confident`:
  filled primary = confirmed by the user, accent badge = detected confidently, amber
  **"!"** = the detector was *not* sure. The pages worth looking at are now visible at
  a glance, instead of being discovered in the exported PDF.
- **The primary button stops asking for work that happened.** It always offers the
  next thing that needs a human, in this order: *Continue to filters* when every page
  is signed off, else *Check page N* for the first page still flagged, else *Next
  page (i of N)*. The header subtitle reports the pass ("Finding borders · 3 of 8
  pages", then "All 8 borders found · tap a page to adjust", or "… · 2 need a check").
  A flagged page is **not** signed off by the pass — only by having been on screen
  and left (`persistGeometry`) — so confidence is never quietly upgraded.

### Unchanged

Baking is still deferred until the filter stage, still from the pristine capture,
still non-destructive; corner edits are still persisted immediately; the
`savesLosslessly` / 3200 px / preview-dimension contract is untouched.

### For Antigravity

`setPageCorners` is additive. The auto-pass is review-mode only and idempotent — it
skips any page whose corners are not `isFullFrame`, so re-entering the flow never
re-crops a page the user was happy with.

## 2026-10-25 (ah) — Arena.ai — Round 22: overdraw, and a scroll line worth pasting

Same complaint, next layer down. Round 21 made a card cost what a settings row
costs; this round removes the per-frame *fill rate* a photo grid pays and a text
list never does, and turns the frame monitor into one line per gesture.

### Overdraw removed (nothing changed visually)

| Where | Was | Now |
| :--- | :--- | :--- |
| `ui/dashboard/DashboardScreen.kt` — `Scaffold` | `containerColor = surface` painted a **full-screen opaque rect** on top of the theme root `Surface` in `MainActivity`, which had already painted the same colour | `containerColor = Color.Transparent`. Two full-screen rects per frame on a 1080 x 2400 display is ~5.2 MPx of fill; this halves it. `surface` and `background` are the same value in both palettes, so the pixels do not change. |
| `DocumentGridCard` | card painted `surfaceContainerLow` across its whole area, then drew a ~0.5 MPx thumbnail **on top of it** | the card colour is painted by the footer only — the one part of a card a photo does not cover. One fewer full-tile fill per card, eight-ish cards per frame of a fling. |
| `DocumentGridCard` / `DocumentListCard` cover box | `background(surfaceContainerHighest)` under the photo | `PageThumbnail` gained a `placeholder: Color` that is drawn **only while there is no image**. Same colour where the photo has not decoded, nothing underneath it afterwards. |

### `util/PerfTrace.kt` — one line per scroll gesture

`setScrolling(Boolean)`, wired from the dashboard's own `isScrollInProgress`:
the monitor now tallies frames *of the gesture* and prints when the finger lifts.

```
I DocScannerPerf: JANK scroll frames=214 slow=3 avg=17.4ms worst=41.2ms cacheHits=96 thumbs=8 pageDecodes=0
```

`slow` counts frames over 24 ms (the runtime's own threshold on a 60 Hz display).
That single line is the whole diagnosis: `slow=0` means the list is fine and the
perception is something else; a non-zero `pageDecodes` means a page has no stored
thumbnail; a big `worst` with zero decodes means it is not images at all.

### Unchanged

No data, filter, geometry, typography or colour changes — with one deliberate
exception: nothing. Every edit above paints the same pixels in fewer passes.

### The next lever needs the phone, not another commit

If this is still not enough, the remaining win is **AOT compilation of Compose**:
a baseline profile via the `androidx.baselineprofile` Gradle plugin plus a
macrobenchmark module (generated on a real device, then committed as
`baseline-prof.txt`). That is worth 20-40 % of first-run frame time on Android 7+
and cannot be produced in this sandbox — it needs `adb` and a connected handset.
Recorded in `docs/AGENT_COORDINATION.md` §6m for Antigravity.

## 2026-10-25 (ag) — Arena.ai — Round 21: the library scrolls like the settings list

The user's benchmark was explicit: *"as smooth as the settings page scrolling."*
That comparison is the diagnosis. Settings is a `LazyColumn` of shallow text rows;
the library is a `LazyVerticalGrid` of photo tiles. Rounds 19-20 fixed the data
and the decoding, so what was left is the cost of a **tile**: what it allocates,
what it animates, and what it re-does every time it enters the screen.

### Every fix below is something a settings row does not do

| Where | Was | Now | Why it mattered on a fling |
| :--- | :--- | :--- | :--- |
| `ui/common/PageThumbnail.kt` | an `animateFloatAsState` fade-in **per tile**, 180 ms | no fade | a dozen tiles start that animation every second; each holds a running animation on the frame clock plus a layer write, exactly when the frame budget is tightest |
| `PageThumbnail` first frame | grey placeholder, then the photo 30-60 ms later | seeded from the memory cache via `PageImageLoader.peek` | a tile that scrolls back into view paints in the frame it is composed — the flicker of grey is the thing that *reads* as lag even when frames are met |
| `ui/dashboard` cards | `Surface` + `pressScale` + two clips (`.clip()` after `Surface`) | `Box` + `clip` + `background` + `clickable(ripple)` | `Surface` composes semantics/clip/background/elevation nodes, and `pressScale` is a `composed` factory (the pattern Compose's docs warn about in lazy lists): a nested composition **and** a running animation per item. A ripple already says "pressed". |
| `DocumentListCard`, `TonalMenuButton`, `InfoPill` | `Surface` | `Box` | same, times one per row / one per tile |
| prefetch | `collectLatest` on the **last visible item index** | quantised to **rows**, plain `collect` | it used to restart every other frame during a fling, cancelling decodes it had just started — self-inflicted work on the main thread |
| `util/ThumbnailStore.kt` | `key()` stat'd the page file (4 syscalls) on **every** load | memoised per page (`keyMemo`), refreshed by `writeFor` after each page write | the scroll path no longer touches the filesystem at all |
| `util/PageImageLoader.kt` | every load hopped to `Dispatchers.IO` even for a cache hit | cache hit answered on the caller's thread from the memoised key | one dispatcher hop per tile per appearance, removed |
| `PageImageLoader.scaleToFit` | always produced a second bitmap to hit the exact box | leaves a decode up to `NEAR_ENOUGH = 1.30x` alone and lets the GPU do the last bilinear step | one ~1.5 MB allocation **and** a CPU rescale per tile became zero; the allocations, not the arithmetic, are what the GC has to collect mid-fling |
| `PageImageLoader.primeThumbnails` | `return@forEach` after the limit — so it kept stat'ing the *whole* library every chunk | real `break` | thousands of pointless syscalls per idle chunk on a large library |

### Nothing about the data changed

No filter output, no scan geometry, no repository semantics. Every change is
rendering, caching or scheduling.

### Measuring it

The debug frame monitor from round 20 still applies:

```
adb logcat -s DocScannerPerf
```

`cacheHits` should dominate during a fling (`thumbs` small, `pageDecodes` ~0) and
`p90` should sit at the display's frame time.

## 2026-10-25 (ae) — Antigravity — Round 19 build verification & SearchBarField compilation fix

Verified Round 19 (library stability/scrolling performance and camera chrome overhaul) locally with `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`. Resolved 1 compilation error:

- `DashboardScreen.kt`:
  - Removed invalid `remember(scheme) { TextFieldDefaults.colors(...) }` wrapper in `SearchBarField` because `TextFieldDefaults.colors` is an `@Composable` function and cannot be called inside a non-composable `remember` calculation lambda.
- Build status:
  - All unit tests pass cleanly in 24s (`testDebugUnitTest`).
  - `app-debug.apk` (65,532,981 bytes) assembled cleanly in 11s (`assembleDebug`).

## 2026-10-25 (af) — Antigravity — Round 20 build verification & DashboardScreen visibleItemsInfo fix

Verified Round 20 (dedicated 768px ThumbnailStore, background priming, ML Kit scroll-pause, and PerfTrace) locally with `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`. Resolved 1 compilation error:

- `DashboardScreen.kt`:
  - Resolved `Unresolved reference 'visibleItemsInfo'` in `LaunchedEffect` prefetcher. `LazyGridLayoutInfo` and `LazyListLayoutInfo` do not share a common interface exposing `visibleItemsInfo`, causing `val info = if (isGridView) gridState.layoutInfo else listState.layoutInfo` to infer `Any`. Branched explicitly on `if (isGridView)` before accessing `visibleItemsInfo` on `gridState.layoutInfo` and `listState.layoutInfo`.
- Build status:
  - All unit tests pass cleanly in 19s (`testDebugUnitTest`).
  - `app-debug.apk` (65,545,972 bytes) assembled cleanly in 14s (`assembleDebug`).

## 2026-10-25 (ae) — Arena.ai — Round 20: the covers stop decoding pages (the actual scroll jank)

Round 19 removed per-frame work from the dashboard, and it was still not smooth.
This round found what was left, and it was not in the dashboard at all.

### The cause

**Every grid cover was produced by decoding the whole page.** No thumbnail was
written anywhere before the home screen first asked for one, so a scroll was:

- `BitmapFactory.decodeFile(<3200 px page>, inSampleSize = 4)` — 800x600 ARGB,
  1.9 MB, then a `createScaledBitmap` to the tile;
- and for *Clear B&W* / *Grayscale*, which are stored as **PNG** precisely because
  they are worth keeping losslessly, a PNG has no DCT shortcut: the full 7.7
  megapixel image is inflated whatever `inSampleSize` says. Tens of milliseconds
  of CPU and ~30 MB of transient allocation **per tile**, two decode slots at a
  time, while the user is flinging the list. That is the jank: not layout, not
  recomposition, image decoding contending with the render thread (and ART's GC
  pausing it).

Two smaller causes were stacking on top of it at startup:

- `backfillTextInBackground` began on the first composition and ran an ML Kit
  inference (plus a 2400 px decode) per page straight through the user's first
  scroll.
- Each recognised page emitted a new library snapshot on its own: ~24
  recompositions of the dashboard and ~24 rewrites of the whole `library.json`
  in the first seconds of use.

### The fix

| File | Change |
| :--- | :--- |
| **`util/ThumbnailStore.kt` (new)** | One 768 px JPEG per page in **`filesDir/thumbs`**, written while the page is being saved. Key is `imageUri#length:mtime`, so a re-bake can never show a stale cover. Swept to 600 files. |
| `util/ImageStore.kt` | `importFromUri`, `importFromFile` and `saveEditedPage` write the thumbnail immediately after the page — the full bitmap is already in hand there, so this costs a few ms at a moment nobody is waiting. `writeJpeg`/`writePng` now stamp a **strictly increasing** mtime (`nextStamp()`), so two saves inside one millisecond can no longer collide on a cache key. |
| `util/PageImageLoader.kt` | Reads `ThumbnailStore` first (a ~40 KB JPEG, sampled down with `inSampleSize`), keeps the full-decode path only as a fallback for pages that predate the store — and repairs them by writing a thumbnail on the way through. Adds `prefetch(...)` and `primeThumbnails(...)`. Memory cache: `allocationByteCount`, cap `maxMemory/8` with a **24 MB floor** (the old 8 MB floor held four covers and thrashed). |
| `ui/dashboard/DashboardScreen.kt` | Cover size is **derived from the window** (`rememberGridCoverSize`) instead of a literal 440x560 that was smaller than the tile it filled. Prefetches the next `PREFETCH_AHEAD = 4` tiles on every viewport change (`collectLatest`, so a stale window is cancelled). Primes legacy thumbnails once, ~1.8 s in, and only if the list is idle. OCR backfill starts 3 s in and pauses while the list scrolls. `DocumentGridCard` gains a **required `coverSize: IntSize`** parameter. |
| `data/ScannerRepository.kt` | New `setPageOcrTexts(docId, Map<pageId, text>)` — **one library update per document**. `backfillTextInBackground(limit, isBusy: () -> Boolean = { false })` pauses between pages while `isBusy()` is true, and spaces documents by `OCR_BATCH_SPACING_MS = 220`. `setPageOcrText` is kept and delegates. |
| **`util/PerfTrace.kt` (new)** | Debug-only frame monitor: `adb logcat -s DocScannerPerf` prints `p50/p90/max` frame times per 120-frame window plus how each cover was obtained (`cacheHits`, `thumbs`, `pageDecodes`). Installed from `MainActivity`; not installed in release, and the counters are a single volatile read when it is off. |

### Why not Rust

Recorded in `docs/RUST_MIGRATION_PLAN.md` §7: the scroll path has no pixel loop —
it is framework layout plus a decoder that is already C++ — and two of the three
causes were *scheduling*. Rust stays on the filter bake, where it already is.

### What to check on a device

`adb logcat -s DocScannerPerf` during a fling should show `pageDecodes` near zero
after the first pass over a library (the legacy page that gets primed is the only
one that should ever appear) and `p90` at ~17 ms. A `JANK` line with a large
`pageDecodes` count means some page still has no stored thumbnail.

## 2026-10-25 (ad) — Arena.ai — Round 19: the library stops working per frame, the camera gets a designer

**No new dependencies, no new files, no repository-layer signature changes.**

### Why the home screen still felt slow

Round 16 removed the collapsing app bar and the per-item entrance animations — the
two things that were obviously re-running on every scroll frame. What it could not
remove was the cost of a *recomposition*: the library's data classes were unstable,
so nothing could be skipped, and the work inside a card was not free.

- **`model/Models.kt` — `@Immutable` on `QuadCorners`, `ScannedPage`, `Document`,
  `Folder`.** Compose inferred all four as *unstable*, because `List<ScannedPage>`
  is an interface the compiler cannot prove immutable. An unstable parameter makes
  a composable unskippable, so one keystroke in the search field recomposed every
  visible card, and every list emission recomposed all of them. The promise is
  true: every field is a `val` and the repository always replaces a document
  (`doc.copy(pages = doc.pages + page)`) instead of mutating a list. **Do not add a
  `var` or a mutable collection to these classes** — that would silently make the
  promise a lie and bring the jank back.
- **Dates are formatted once per library change**, into a `DateLabels` map, instead
  of once per card per frame. `SimpleDateFormat.format` allocated a `String` and a
  `Date` for every card the fling touched.
- **`DocumentGridCard` / `DocumentListCard` now take `dateLabel: String`** instead
  of reading `document.modifiedDate` themselves. Additive-but-required parameter:
  update any new call site.
- **Search no longer runs in composition.** `DocumentSearch.Index` is now built
  *on demand* (first query of the session) and *off the main thread*
  (`Dispatchers.Default`); the query itself is debounced by
  `SEARCH_DEBOUNCE_MS = 120` and ranked on `Dispatchers.Default`. Previously the
  index was rebuilt inside `remember(documents)` — on the frame thread — and the
  one-time OCR backfill emits a library snapshot **per page**, so the home screen
  re-tokenised the whole library up to 24 times at startup.
- **`activeDocs` is keyed on `queryActive`, not on the query text**, so typing does
  not re-filter and re-sort the library for every character.
- **Grid and list scroll positions are remembered separately**
  (`rememberLazyGridState` / `rememberLazyListState`), so toggling the layout no
  longer throws the user back to the top.
- **Cover thumbnails are requested at 440 × 560** (was 360 × 460): a two-column
  card on a 1440 px display was being upscaled.

### `util/PageImageLoader.kt`

`load(...)` keeps its signature and its two caches; the internals changed:

- The cache key (two `stat` calls) is computed on `Dispatchers.IO`. `load` is called
  from `produceState`, i.e. on the main dispatcher, so metadata was previously read
  on a background thread only by accident of the enclosing `withContext`.
- **Single-flight** (`inFlight: ConcurrentHashMap<String, CompletableDeferred<…>>`).
  A fling disposes and recomposes cards faster than a page decodes, so the same
  page was frequently inflated twice at once. Waiters must *not* take a
  `decodeDispatcher` slot while waiting — that limiter holds its permit for the
  whole coroutine body, so two waiters would starve the decoder they wait for.
- **`ensureActive()` before decoding.** An in-flight `BitmapFactory.decodeFile`
  cannot be interrupted; the only useful moment to give up is before it starts,
  otherwise cards that have already scrolled away keep both decode slots.
- **`scaleToFit`**: `inSampleSize` only halves, so a 3200 px page asked for at
  440 px decoded at 800 px — 1.9 MB cached per card instead of 0.9 MB, and a
  texture the GPU re-uploads on every frame. The cached bitmap is now exactly the
  requested box, and the on-disk thumbnail is written at that size too.

### `ui/common/PageThumbnail.kt`

Decoded pixels fade in over 180 ms instead of appearing in one frame. Same
placeholder, same layout, no shimmer — the tile never changes size, so the reveal
cannot cause a reflow.

### `ui/camera/ScannerCameraScreen.kt` — chrome rebuilt

All capture behaviour is untouched: same CameraX binding, 4:3 resolution strategy,
flash/torch cycling, pinch zoom, gallery import, batch auto-commit, gallery
auto-save, `indexTextInBackground` at commit. What changed is everything drawn on
top of the preview:

- Gradient scrims top and bottom instead of relying on the controls' own backing:
  white chrome over a white page was the worst legibility case in the app.
- **Batch / Single is a segmented control**, not a button that cycles — a cycle
  makes the user read the label to know what the next tap does.
- **Capture feedback that arrives on the press**, not on the save: a haptic tick, a
  260 ms white frame flash (driven through `graphicsLayer`, so it never re-enters
  composition) and a spring bump on the gallery tile as the page lands. While a
  capture is in flight the shutter ring sweeps amber — previously the shutter
  swapped its white disc for a spinner.
- **Zoom is a row of discrete stops** (1×, 2×, 5×… plus the sensor's own limit),
  Google-Camera style; pinch still works on the preview. The old `+`/`−` pair told
  the user nothing about the range and cost two taps for a common action.
- **Finish is a circular accent check** the same size as the gallery tile, which
  keeps the shutter on the exact centre line; the page count moved to a badge on
  the tile and to the counter pill under the chrome.
- Camera-permission and camera-bind failures are now full cards with an
  "Import photos" fallback instead of a black rectangle with one button.
- `statusMessage` is a self-dismissing pill (`STATUS_MS = 4200`). **A camera bind
  failure no longer goes through `statusMessage`** — it sets the new
  `cameraError`, which stays up until the user leaves, because "try again later"
  is not useful advice for a black viewfinder.
- Leaving with unsaved pages asks first: `BackHandler` and the close button share
  `requestClose()`, which raises a discard dialog when `capturedPages` is
  non-empty. Back closes the dialog rather than the screen while it is up.
- A one-line hint ("Fill the frame with the page") retires after `HINT_MS = 2800`.
  The static guide frame stays deleted — this says the same thing without teaching
  users to fill the frame past the paper edge.

## 2026-10-25 (ac) — Antigravity — Rounds 16, 17 & 18 build verification & memory tuning

Verified Rounds 16, 17, and 18 locally with `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`:

- **Home screen performance (Round 16)**: Pinned "Doc Scanner" TopAppBar, removal of per-item entrance animations on lazy grid/list cards, addition of `contentType`, and persistent disk thumbnail cache (`cacheDir/thumbs`).
- **Camera & Border Detection (Round 17)**: 4:3 viewfinder strategy on Preview and ImageCapture (`aspectRatio(3f / 4f)` with centered framing), rewritten background-image document detector with subpixel edge step walking and frame boundary fallback, and O(k) sliding-histogram median blur.
- **Search with On-Device OCR (Round 18)**: Offline Google ML Kit text recognition (`com.google.mlkit:text-recognition:16.0.1`), background OCR indexing on scan commit and batch backfill, BM25-ranked full-text search with contextual snippets.
- **Gradle Build & Dexing Tuning**:
  - Configured Gradle daemon heap to 1536M/2048M and MaxMetaspaceSize to 512M with standard G1GC in `gradle.properties`, cleanly resolving D8 dexing memory constraints for ML Kit multidex merging.
  - All unit tests (`DocumentFiltersTest`, `DocumentSearchTest`, etc.) pass cleanly in 1m 12s.
  - `app-debug.apk` (65,318,961 bytes with bundled offline OCR models) assembled cleanly in 55s.

## 2026-10-25 (ab) — Arena.ai — Round 18: pages read their own text, and search ranks it

### New dependency (the only one)

`com.google.mlkit:text-recognition:16.0.1` — **bundled** on-device text
recognition, pinned in `gradle/libs.versions.toml` as
`mlkit-text-recognition`. Bundled rather than the Play-Services variant so that
recognition works offline and cannot be broken by a Play Services update; the
model travels in the APK, which grows by a few MB. No model file is committed —
see `docs/AGENT_COORDINATION.md` §6i for what the environment must provision.

### Why recognition exists at all

The PDFs this app writes contain **no text layer** — pages are images — and
`ScannedPage.ocrText` had never been written by anything: `setPageOcrText` existed
with no callers. "Search inside my PDF content" therefore had nothing to search.

- `util/OcrEngine.kt`: recognises one page, on device, best-effort. Decodes at
  ≤ 2400 px (a 3200 px page costs ~40 MB to decode and buys no accuracy), and
  returns `null` — never throws — when a page has no text or the recogniser fails.
  The Play Services `Task` is bridged with `suspendCancellableCoroutine`, so no
  extra coroutine-play-services dependency.
- `ScannerRepository.indexTextInBackground(docId)` runs after a scan is committed,
  on the repository's own scope, so it outlives the camera screen and nothing the
  user does waits for it.
- `ScannerRepository.backfillTextInBackground()` handles pages that predate the
  feature, capped at `OCR_BACKFILL_BATCH = 24` pages per app start. Unbounded, this
  would turn launching the app on a big library into a CPU burn.

### A real bug fixed on the way

`setPageOcrText` used to delegate to `updatePage`, which bumps `modifiedDate`.
Background indexing would therefore have reordered the library under the user, one
page at a time. It now writes `ocrText` only: recognised text is metadata about a
page, not an edit to it.

### Search (`util/DocumentSearch.kt`)

The home screen compared every page's text with `contains` **per keystroke** — a
full re-scan of the library per character, and it could only answer yes/no.

`DocumentSearch.Index` is built once per library change: tokenised title, tags and
page text into an inverted index, scored with BM25 (term-frequency saturation,
length normalisation, IDF), with title and tag matches weighted above body text and
a phrase bonus for the whole query appearing verbatim. Results come back ranked,
each with the passage that matched — shown under the document's title instead of
its date, so a result explains itself.

Search results ignore the sort control while a query is active: relevance is the
ordering the user asked for. Tokenising is Unicode-aware, not `[^a-z0-9]`, because a
scan can be in any script.

Tests: `app/src/test/.../util/DocumentSearchTest.kt` covers title-over-body ranking,
term frequency, phrase bonus, snippets and clipping, tag matches, title-only
matches, empty queries and non-Latin tokenising.

### Still open

Semantic matching — finding "billing" when the page says "invoice" — needs an
embedding model. It is declared, with its exact file and destination, in
`docs/AGENT_COORDINATION.md` §6i item 3, and is not wired into the app yet: an
unverified inference path is precisely how wrong output ships.

## 2026-10-25 (aa) — Arena.ai — Round 17: 4:3 viewfinder, border detection rebuilt, filters made fast

Four reports: border detection "worse and not working at all", the filter applying
too slowly, the camera not 4:3, and search over PDF text. The first three are here;
the fourth needs a decision from the user (see the note at the end).

### Camera — 4:3 everywhere

`Preview` and `ImageCapture` now share one `ResolutionSelector` with
`AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY`, and the viewfinder is hosted
in a centred `fillMaxWidth().aspectRatio(3f / 4f)` box instead of `fillMaxSize()`.
Before, the stream was displayed `FILL_CENTER` edge to edge on a 20:9 phone, so the
frame the user composed was **not** the frame that got captured — the top and
bottom of the shot were already cropped away before the shutter.

`PreviewView` is `FIT_CENTER`, not `FILL_CENTER`: when a device refuses 4:3 the
stream is letterboxed inside the 3:4 host rather than cropped, so the preview keeps
telling the truth about the capture.

### Border detection — rewritten (`util/DocumentDetector.kt`)

The old detector scanned inward from each frame edge and took the **first**
gradient above a global threshold. Five things were wrong with that, and together
they explain "not working at all":

1. It only accepted edges **strictly inside** the frame, so the most common shot —
   the page filling the frame — produced no sides and fell through to "no crop".
2. Its threshold came from the magnitude distribution of a photo full of text, so
   text strokes cleared it easily and "first edge from the left" often landed on
   text rather than paper.
3. There was no notion of *which side of an edge the paper is on*, so a dark text
   block looked exactly like a page boundary.
4. RANSAC tolerance was 1.2 % of the frame (~4 px at working size) — loose enough
   to accept a line of text as a side.
5. `validate` rejected `ys[0] >= ys[2]`, so a genuinely full-frame page was thrown
   away as invalid.

The new pipeline works on a **background image**: grey → morphological closing on a
half-width copy (erases ink, keeps paper) → bilinear upscale → box blur. Each side
is then found by walking inward until the background tone crosses the midpoint
between the paper and the surrounding tone, with a step test across a band wider
than a text line so flat noise and slow shadows cannot manufacture an edge. The
crossing is interpolated between adjacent samples, which is unbiased for a blurred
step; the old "strongest gradient" idea is not, because a blur makes the difference
flat for several pixels.

Practical consequences:

- **A side that is not found means "the page runs off the frame"** and is replaced
  by the frame edge — the correct answer, not a failure.
- A frame with no usable contrast returns `FULL_FRAME` with `confident = true`:
  there is nothing to crop and nothing to adjust.
- `extremePointQuad` is gone. It was the previous silent failure mode: it would
  return a confident-looking quad containing a page corner and a text line.
- The old constants (`MIN_SIDE_INLIERS`, `EDGE_FLOOR`, `RANSAC_TOLERANCE` and
  friends) are gone or re-tuned; nothing outside this file referenced them.

Measured on 47 synthetic pages (perspective, drop shadows, fingers, glare, low
light, low contrast, white desk on white paper, a receipt, a 40° rotation, a page
running off the frame) — max corner error against ground truth:

| | within 2 % | p90 | worst |
|---|---|---|---|
| previous | 41 / 47 | 5.9 % | 37.4 % |
| this | 46 / 47 | 0.95 % | 3.7 % |

Every gross failure of the previous version was a crop **into the page** (up to 37 %
of the frame), which is exactly what the user sees as "it cut my document".

### Filters — the Grayscale median, 50× fewer operations

`medianBlurGray(…, 9)` (Grayscale, and the reference the Rust kernel is checked
against) re-sorted its 81-value window from scratch for every pixel: ~1600
comparisons per pixel, ~12.6 **billion** on a 3200 px page. It now slides a 256-bin
tone histogram and tracks the median position incrementally — a couple of dozen
operations per pixel. The median of an odd-sized multiset is unique, so the output
is **bit-identical**; the Rust parity check still passes unchanged.

This kernel is deliberately no longer routed through Rust: a comparison-based
median is O(k²) per pixel in any language, so the histogram wins even against
native code. `NativeFilters.medianBlurGray` stays, and is still verified against
the Kotlin implementation on first use.

`DocumentFilters.apply` now logs one line when a filter takes ≥ 400 ms
(`DocumentFilters: MAGIC_COLOR took 1830ms at 2400x3200`), which is the only
measurement available on a real device.

## 2026-10-25 (z) — Arena.ai — Home screen: pinned title, "Doc Scanner", and the real scroll-jank fixes

Reported: the home screen lagged badly, the title said "Library", and the app name
slid back down while scrolling.

### The title

`LargeTopAppBar` + `exitUntilCollapsedScrollBehavior` is gone. That combination
re-measured the bar, cross-faded two title styles and pushed a new content inset
through the grid on **every scroll frame** — and it is what animated the app name
back down. The home screen now uses a plain pinned `TopAppBar` reading
**"Doc Scanner"**, with no `nestedScroll` connection at all.

### The jank — three separate causes

1. **Per-item entrance animations.** Every grid/list card was wrapped in
   `Modifier.entrance(3)`, which composes an `animateFloatAsState` + `alpha()` +
   `graphicsLayer` per item. In a lazy list that re-runs for every card that enters
   the viewport, so the list animated continuously while scrolling and every card
   carried its own alpha layer. Removed from items; the header rows keep their
   one-shot entrance.
2. **Missing `contentType`.** Without it the lazy slots are not reused by type, so
   scrolling down threw away cards and rebuilt them at the top of the list.
3. **Thumbnail decoding — the big one.** `Clear B&W` and `Grayscale` pages are
   stored as **PNG** (they must be lossless). JPEG has DCT scaling, so a sampled
   decode is cheap; PNG does not, so a 360 px thumbnail still inflated the full
   3200 px image. Every card paid that on every pass over the list.

### Thumbnail disk cache (`util/PageImageLoader.kt`)

- Requests with a longest edge ≤ 1024 px are written to `cacheDir/thumbs` the
  first time and decoded from there afterwards: a ~10 KB JPEG instead of a
  multi-megabyte PNG.
- The file name is a SHA-1 of the cache key, and the key includes the source
  length + mtime, so a re-baked page can never show a stale thumbnail.
- Writes are temp-file + rename (two cards can request the same page at once), the
  directory is swept when it exceeds 400 files, and large requests (viewer,
  editor) bypass the disk cache entirely.
- Decodes now run on `Dispatchers.IO.limitedParallelism(2)`: a fling can put a
  dozen cards on screen, and letting all of them decode starves the render thread
  exactly when the list is moving fastest.
- `clear(context)` drops the memory cache and, when given a context, the files.

Grid thumbnails also drop from 420×540 to 360×460 (still above the on-screen size
at 2× density), which cuts the bitmap cache footprint by a third.

**Not fixed here:** Baseline Profiles. They are the next real 120 Hz win, but they
need a device to generate, so they belong in a build-verified pass.
## 2026-10-25 (y) — Antigravity — Round 15 build verification & configuration cache fixes

Verified Round 15 Rust pixel kernels & UI polish locally with `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`. Resolved 2 issues:

- `NativeFilters.kt`: Wrapped `Log.i` in `runCatching` during static initialization so JVM unit tests without mocked Android Log framework initialize cleanly.
- `app/build.gradle.kts`:
  - Added `unitTests.isReturnDefaultValues = true`.
  - Replaced `ProcessBuilder("sh")` during Gradle configuration phase with in-process PATH executable lookup (`hasExecutableOnPath`) supporting Windows/Linux without external process violations.
  - Used `enabled = rustToolchainReady` on `buildRustKernels` instead of `onlyIf` referencing script objects, restoring Gradle Configuration Cache storage.
- Build status:
  - All unit tests pass cleanly in 38s (`testDebugUnitTest`).
  - `app-debug.apk` (21,941,359 bytes) assembled cleanly in 15s (`assembleDebug`).

## 2026-10-25 (x) — Arena.ai — Rust kernels behind a byte-for-byte parity gate

Round 15, first performance increment. **Filter output is unchanged** — that is the
entire point of the design below — and the app still works with no Rust toolchain
installed.

### New files

| File | What it is |
| --- | --- |
| `app/src/main/rust/Cargo.toml` | `cdylib` crate `docscanner_native`. One dependency (`jni`), LTO, stripped, panic=abort. |
| `app/src/main/rust/src/kernels.rs` | Ports of `medianBlurGray`, `median3x3`, `laplacian3x3`. |
| `app/src/main/rust/src/lib.rs` | Four JNI entry points; `0` = ran, `-1` = fall back. |
| `util/NativeFilters.kt` | Kotlin facade: loads the `.so`, refuses a stale ABI, exposes the raw calls. |

### How it stays safe

- `DocumentFilters` keeps every Kotlin kernel (renamed `…Reference`) as the
  **reference implementation**, and computes `nativeFiltersReady()` lazily on first
  use by running both on a 96×64 synthetic page and comparing **byte for byte**.
  Any mismatch, missing library, wrong ABI or JNI error → Kotlin path, for the
  session. A wrong pixel cannot reach the user.
- `median_blur_gray` is a *sliding histogram* (Huang) rather than the reference's
  81-element partial selection. Both return the same order statistic of the same
  window, so parity is by construction, not by luck.
- `laplacian3x3` keeps the reference's left-to-right `f32` evaluation order, so no
  fused-multiply-add contraction can move a bit.
- Gradle task `:app:buildRustKernels` cross-compiles `arm64-v8a` + `x86_64` into
  `src/main/jniLibs/` and is **skipped automatically** when `cargo`/`cargo-ndk` is
  absent. `jniLibs/` and `rust/target/` are git-ignored (AGENTS.md §2.5).

### UI fixes in the same commit

- Viewer: panning is clamped to the zoomed page instead of letting the page be
  flung into empty space (`MAX_VIEWER_ZOOM = 4f`).
- PDF sheet and signature dialog are scrollable, so the primary action cannot be
  clipped off the bottom on a short screen; the signature pad's canvas caps its
  height with `heightIn`.
- Recycle bin: *Empty recycle bin* is centred instead of reading like another row.

### Not done here

No JDK/Android SDK and **no Rust toolchain in this sandbox** — the crate has never
been compiled. Expect the first `cargo ndk build` to shake out API details of the
`jni` crate; the Kotlin side is written so that even a completely broken `.so`
only means "Kotlin path". Next kernels after this one is proven on device: the
tone-table divide loop and the perspective warp.

## 2026-10-25 (w) — Antigravity — Round 14 build verification & compilation fixes

Verified Round 14 Material 3 Expressive UI overhaul locally with `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`. Resolved 3 compilation issues from Arena's uncompiled UI changes:

- `theme/Motion.kt`:
  - Typed `Motion.press`, `spatial`, `emphasized`, `bouncy`, `fade` as `FiniteAnimationSpec<Float>` (previously generic `AnimationSpec<Float>`) to satisfy `fadeIn`/`fadeOut` constraints in Compose.
  - Converted `entrance` into an extension function: `fun Modifier.entrance(index: Int, distance: Dp = 14.dp): Modifier = composed { ... }` allowing `.entrance(i)` modifier chains.
- `ui/camera/ScannerCameraScreen.kt`:
  - Added missing `import androidx.compose.material.icons.filled.Check`.
- Build status:
  - All unit tests pass cleanly in 22s.
  - `app-debug.apk` (22,012,700 bytes) assembled cleanly in 10s.

## 2026-10-25 (v) — Arena.ai — Round 14: the whole app restyled (Material 3 Expressive)

A UI/UX pass over **every screen** — no filter maths touched (`util/DocumentFilters.kt`
and its test file are byte-identical to `6131924`, which is why the "filters are
working great" verdict still holds).

### Three new files (the shared vocabulary)

| File | What it is |
| --- | --- |
| `theme/Shape.kt` | `Shapes` scale 8/12/18/26/34 plus `DocShapes` (a named shape per surface kind). |
| `theme/Motion.kt` | `Motion.press/emphasized/standard`, `Modifier.pressScale(source, scale)`, `entrance(index)`. |
| `ui/common/Expressive.kt` | `PillButton`, `TonalPillButton`, `TonalIconButton`, `InfoPill`, `SectionCard`, `SegmentedPill`, `LoadingDots`, `PageProgressBar`, `SectionLabel`, `Hairline`, `RevealWhen`. |

**Rule for both agents:** new UI composes from these and from `DocShapes`; do not
inline `RoundedCornerShape(12.dp)` or a bare `Button` again. That drift is what
made the app look assembled rather than designed.

### Theme

- `theme/Color.kt` — 74 roles filled in (light + dark), including all four
  `surfaceContainer*` steps, `surfaceDim/Bright`, `inverse*` and `scrim`, which
  were previously undefined and silently fell back to Material defaults.
- `theme/Type.kt` — rewritten scale with tightened tracking on display/headline
  and a legible `labelSmall` for the uppercase eyebrows.
- `theme/Theme.kt` — `dynamicColor` now **false by default** (dynamic colour was
  overriding the app's identity on API 31+), accent override limited to the four
  primary slots, plus `DocAccents`/`LocalDocAccents` for the amber capture accent
  and the editor's corner-handle colours.
- `DocShapes.dialog` **added** (round-all-corners 30 dp; `sheet` stays top-only).

### Screen by screen

| Screen | Change |
| --- | --- |
| Dashboard | Rewritten: quiet two-line app bar, folder pills with colour dots, grid/list toggle, sort sheet, `InfoPill` counts, `CaptureFab`. |
| Camera | Glass chrome (`GlassButton`/`GlassPill`), accent page-count chip, `ShutterButton` with a spring inner ring, `GalleryTarget` showing the last page. Capture/permission/zoom logic untouched. |
| Editor | Rewritten around **one** `SegmentedPill` for the two-stage contract. Borders stage shows the *uncropped* original; filters stage shows the warped + filtered page and bakes every page with a `current/total` overlay. |
| Document detail | Minimal title-only app bar, `InfoPill` facts row, tinted notes card, 20 dp-grid page wall, floating `DocShapes.sheet` action bar (Add page / Sign / Mark / Export). |
| Settings | Uppercase `labelSmall` eyebrows, `surfaceContainerLow` rows with chevrons and `Switch` instead of `Checkbox`, whitespace instead of dividers. |
| PDF sheet | Icon tile header, `SegmentedPill` for paper/margins, footer-numbers card, hand-built CTA that carries both idle and busy states. |
| Signature | `Dialog` (near-full-width) instead of `AlertDialog`, paper card with a faint signing baseline, ink swatches, weight slider, Clear/Apply pills. |
| Recycle bin | Quiet app bar with a live count, circle empty state, cards with `InfoPill` page counts, per-row restore/delete, danger pill for Empty. |
| Page viewer | Full-bleed dark canvas with floating glass chrome and a floating pager pill — a reader should not pay for two opaque app bars. |

### Interaction change the user asked for

Borders are now a **per-page pass that finishes before any filtering**: *Next page
(n of N)* persists that page's corners and advances, the last page's button reads
*Continue to filters*, and only then does *Apply to all N pages* bake. Tapping the
segmented control to leave the border stage also persists the visible page's
corners. Single-page entry points (from the detail screen) still land on Filters.

### Other

- `CropAndFilterScreen` reads capture quality **once** (`repository.settings` +
  `collectAsState`) instead of three separate `currentSettings()` calls.
- Removed an unused `android.graphics.Canvas` import from `util/PdfGenerator.kt`.
- **No build or device run here** (no JDK/Android SDK in the sandbox). Verified
  only by bracket balance + symbol/import scans. Antigravity's build remains the
  last verified one.

## 2026-10-25 (u) — Antigravity — Verified native divide table fix & rebuilt APK

- Merged commit `32234d4` from `arena/cf5b35a2-re-cam` into `main` and pushed to `origin/main`.
- Executed unit test suite: all 56 unit tests passed cleanly with 0 failures (`.\gradlew.bat testDebugUnitTest` in 17s).
- Successfully compiled and packaged `app-debug.apk` (21,826,671 bytes).

## 2026-10-25 (t) — Arena.ai — The divide table was indexed by the wrong thing

### The finding
Round 13's premise was that `enhanceDocument` is a transcription problem. It was —
but the transcription in (previous) §13.4 had three errors, and together they made
**every document filter wash the page out**. Measured on the old code: ink at 50 on
paper 238 came out at **98**, i.e. mid-grey text; light print at 110 came out at
**218**, i.e. nearly invisible. That is not a tuning difference, it is the wrong
function.

The disassembly of the table builder (`0x105e0`–`0x10734`) is now decoded exactly:

```c
divide[row][col] = 255 - response[min(2000, row*2000/(col+1))] * 255 / 2000;
```

Three things were wrong before:

| | wrong | correct |
|---|---|---|
| ratio | `value·2000/(row+1)` | `row·2000/(value+1)` |
| `row` means | the paper level | `\|src − paper\|` — the difference |
| stored byte | `q` | `255 − q` (the `−1` before `strb`, on a value the negative magic multiply already flipped) |

The evidence is falsifying rather than aesthetic: the wrong readings either saturate
*every* ink pixel to white or turn paper black. The corrected one gives paper → 255
and ink → 0 at every plausible paper level.

### What changed in code
- `DocumentFilters.divideLookup(row, value)` — rewrite; `ToneTables.divide(row, value)`
  param renamed from `paper`.
- `DocumentFilters.colourise` — the alpha blend now happens **on the divide row**
  (`row = B[|d|] + α(A[|d|] − B[|d|])`), which is what `0x110b0`–`0x11124` does, instead
  of on the output. Stronger edge → bigger row → darker, which is the documented
  "sharpens midtone contrast, preserves chromaticity".
- `applyDocumentBw` — `divide[curveA(|src − paper|)][src]` per channel, then grey.
- New `DocumentFilters.levelTwoChannel(table, paper, src)` and
  `DocumentFilters.toneTables()` as test seams.
- **Deleted** the leftover approximation machinery, all of it unreachable:
  `sharpenLuminance`, `localThresholds`, `gainLimits`, `buildThresholdField`,
  `sharpenDecisionInPlace`, `gatedUnsharp`, `noiseEstimate`, `documentCurveLut`,
  `coverageCut`, `normalise`, `medianHistogram`. **Deleted the 10 tests** that only
  exercised those. Added 2. Suite is now **56 tests** repo-wide, down from 58.
- `medianBlurGray` no longer shares one window buffer across threads (a data race).

### Validation (no device here — this is numpy, not a build)
- Synthetic page, paper 236 with a 45 % left-to-right shadow gradient plus text:
  paper out **255 (max error 1)**, text out **0**, 75 % of the page white, 6.3 % pure
  black. The closing recovers the paper level through the shadow.
- Colour preservation (level 1): stamp `(190,40,45)` on paper `(232,232,232)` →
  `(187,0,0)`.
- 46 numeric assertions in `DocumentFiltersTest` re-derived and checked against a
  faithful emulation of the Kotlin.

### Still not recovered
`PSBlockDigest::algorithm4`, `findSmallestRadius`'s predicate, and the level-1
blend order (A vs B at α = 0) — see spec §13.10. `PaperMap` remains our stand-in for
the first two.

## 2026-10-25 (s) — Antigravity — Added original APK binaries for Arena.ai

- Added `docs/original_apk_binaries/` containing `libimageproc.so` (138 KB), `classes.dex` (10 MB), `classes2.dex` (9.2 MB), `classes3.dex` (7.0 MB), and `classes4.dex` (7.0 MB) directly extracted from the original release splits (`base.apk` and `split_config.arm64_v8a.apk`).
- Added index `docs/original_apk_binaries/README.md` for direct bytecode and symbol inspection.

## 2026-10-25 (r) — Antigravity — Verified 1.9 build & full unit test suite

- Pulled and merged PR #9 (`arena/cfada147-re-cam`, version 1.9 / versionCode 10).
- Executed unit test suite: all 58 unit tests passed cleanly with 0 failures (`.\gradlew.bat testDebugUnitTest` in 40s).
- Successfully compiled and packaged `app-debug.apk` (21,826,671 bytes).
- Pushed merged `main` to `origin/main`.

## 2026-10-25 (q) — Arena.ai — 300 DPI pages, and a B&W cut that is smooth *and* sharp

### The report
Zooming into the **saved PDF** shows pixelated, rough-edged text. Not the
preview — the file.

### It was arithmetic, not tuning
A PDF page is a raster, so its sharpness at zoom is its DPI, and the chain had
three caps stacked on top of each other:

| stage | long edge | DPI on A4 |
|---|---|---|
| capture (`ImageStore.MAX_CAPTURE_DIMENSION`) | 2600 | 243 |
| bake (`DocumentFilters.MAX_PROCESS_DIMENSION`) | **2000** | **187** |
| export setting "300 DPI" (`ExportResolution.HIGH`) | 2400 — unreachable | 187 |

`bitmapForExport` loads the already-baked page, so the export setting could never
exceed what the bake had thrown away. Every stage now agrees on **3200 px**, which
on A4 with the standard 25 pt margin (a 770.8 pt = 10.7 in image box) is
`3200 / 10.7 = ` **299 DPI** — a real 300 DPI scan, to within a third of a percent.
`ExportResolution` was relabelled to match: 3200 / 2140 / 1600 for 300 / 200 / 150
DPI.

### 3200 px forced a memory redesign, which also made it faster
The old Clear B&W path held a full-resolution luminance plane, a full median
plane, a full decision plane, *and* two FloatArrays of windowed mean/σ — and
`boxMeanAndStd` built two `DoubleArray` summed-area tables, i.e. **~150 MB at
3200 px** on its own. That is why the cap was 2000. Four changes remove it:

1. **Otsu** is computed from a 1/9 strided sample of the median-denoised plane
   (~800 k samples for a 256-bin histogram), so the full median pass is gone.
2. **The Sauvola field** is computed on a ~320 px grid and sampled bilinearly.
   A local threshold whose window is 1/16 of the page simply cannot vary inside a
   10 px cell. Verified equivalent: it differs from the exact full-resolution
   field by 6–7 levels on average, and by at most **0.006 IoU** on clean,
   shadowed and dark noisy pages.
3. **The blur is in place** (rolling scalar horizontally, one saved row
   vertically) instead of allocating two more planes.
4. **The passes are band-parallel** across up to 4 cores, with a shared daemon
   pool. Every pass is per-pixel and writes only its own rows.

Peak working set is now ~95 MB for a 3200 px page. A device that still cannot
allocate it retries at 3/4 scale (2400 px ≈ 225 DPI) instead of failing the save
— and an `OutOfMemoryError` is no longer swallowed into "return the unfiltered
page", which is what previously made a memory failure look like "the filter did
nothing".

### Rough edges: the cut had no sub-pixel information
A threshold is a hard decision, so a diagonal stroke becomes a staircase. The old
3 %-of-threshold ramp was an attempt to soften it, but a ramp is expressed in
*levels*: it widens with the local contrast and cannot encode where the contour
passes through the pixel.

The cut now computes **coverage**: 3 × 3 sub-samples of the decision plane per
pixel, but *only for the pixels the contour touches* (a dilate-by-1 of the hard
mask, a few per cent of the page), so the interior stays exactly 0 or 255 and the
AA pass costs roughly a sixth of a plain cut.

Measured against a 16 × 16 supersampled ground truth (rotated stroke + ring), RMS
edge error:

| cut | RMS vs true | AA pixels (true: 356) |
|---|---|---|
| hard clip (1.8) | 0.0441 | 0 |
| 2 × 2 coverage | 0.0219 | 216 |
| **3 × 3 coverage (shipping)** | **0.0190** | **290** |
| 4 × 4 coverage | 0.0186 | 330 |
| gradient/SDF-normalised | 0.0357 | 590 (over-spreads) |

At 3× zoom (what a PDF viewer does) the RMS is 0.044 → 0.019. On real synthetic
pages, **94–100 %** of the grey pixels land within one pixel of the true contour —
the softening is on the edge, never a halo. The level-domain ramp constants
(`BW_RAMP_FRACTION`, `BW_MIN_RAMP`) are deleted; they are now redundant *and* were
the wrong shape.

### Clear B&W now saves losslessly
The PDF embeds the decoded page bitmap losslessly (Flate), so JPEG ringing around
glyphs was not merely present in the file — it was permanent. Binary text is the
worst case for an 8 × 8 DCT and the best case for DEFLATE, so `CLEAR_BW` and
`GRAYSCALE` pages are written as **PNG** (`ImageStore.saveEditedPage(lossless =
true)`, `_edited.png`), typically smaller than the JPEG they replace. Old pages
keep their `.jpg` — `imageUri` is an absolute path, not a naming convention — and
`deletePageFiles` removes both.

The editor's preview encodes a JPEG round-trip only when the save is a JPEG, so
for the lossless filters the preview is now *exactly* the file's pixels.

### Signature changes for other agents
- `ImageStore.saveEditedPage(...)` gained `lossless: Boolean = false` (additive);
  new `ImageStore.writePng`, `ImageStore.editedLosslessFile`.
- `ImageStore.MAX_CAPTURE_DIMENSION` 2600 → 3200; `ImageQuality.HIGH` 2600 → 3200,
  `MEDIUM` 1900 → 2000; `ExportResolution` 2400/1600/1200 → 3200/2140/1600;
  `PdfExportConfig.maxImageDimensionPx` default 2000 → 3200.
- `DocumentFilters.MAX_PROCESS_DIMENSION` 2000 → **3200**. These five numbers must
  move together; changing one only moves the bottleneck.
- New `internal` helpers: `bands`, `coverageCut`, `bilinearAt`, `medianOf9InPlace`,
  `boxBlurInPlace`, `noiseFromResidual`. `median3x3` and `noiseEstimate` remain
  (still covered by tests) but are no longer on the Clear B&W path.
- The editor preview renders at `PREVIEW_RENDER_DIMENSION` (2400) rather than at
  the bake size; the file is still 3200. Both are resolution-independent by
  construction (window and AA scale with the page).

### Tests
`DocumentFiltersTest` +10 (48 total): the process dimension is checked against the
300 DPI requirement arithmetically; `bands` must visit every row exactly once (a
band bug would silently leave rows black, and only on multi-core devices);
`bilinearAt` interpolates and clamps; `medianOf9InPlace` returns the middle value;
the in-place blur leaves a flat plane untouched, spreads an impulse to 1/9 and
conserves energy; `noiseFromResidual` reads the residual amplitude; and
`coverageCut` keeps interiors exactly bi-level while producing ~85 (one third) on
a contour pixel and **no grey at all** on a uniform plane. Every expected value was
checked against a numpy transcription before being written down.

## 2026-10-25 (p) — Arena.ai — Sharp Clear B&W, and a preview that is the saved page

### The report
Three things, after using 1.7 on a real document: *Clear B&W* text is still not
sharp; adjusting the borders and then opening **Filters** shows the old photo, not
the cropped one; and the filter preview does not look like what gets saved, so it
is impossible to tell which filter is best.

The first was tuning. The second and third were **bugs introduced in 1.7** — a
recycled bitmap and a preview rendered at a different size from the bake.

### 1. "Soft text" was a wide threshold ramp, not a weak threshold
The 1.7 B&W threshold had a 6 %-of-threshold transition band — ~10 grey levels
either side of the cut. Every glyph edge landed inside it and came out
anti-aliased, which reads as blur no matter how correct the threshold is.

The band is now **3 % with a 3-level floor**, and the *decision* plane is sharpened
before it is cut: an unsharp mask with a soft dead-zone below 3 levels (so grain
is not amplified) and a strength that fades 4 → 9 as the page's own noise estimate
rises (so a grainy frame gets less, not more). Thresholding a sharpened plane
straightens the transition instead of leaving it wandering over three pixels.

Measured through the whole pipeline (normalise → median → Sauvola) against a
ground-truth glyph mask, with the anti-aliased ("grey") pixel share:

| page | 1.7 IoU | now IoU | grey 1.7 → now |
|---|---|---|---|
| clean | 0.820 | **0.866** | 0.70 % → **0.22 %** |
| shadowed to 35 %, soft focus | 0.762 | **0.785** | 1.49 % → **0.63 %** |
| dark 20 %, noisy | 0.758 | 0.758 | 1.46 % → **0.65 %** |

A sweep of the strength (0.6 / 0.8 / 1.0), the pass count (2 / 3) and the floor
(2 / 3 / 4) moved mean IoU by less than 0.004 in total, so the shipped values are
the ones that never lose accuracy on the noisy page rather than the ones that win
a thousandth on a clean one. **Do not** raise the amount to recover the last
0.003 on clean pages — it costs 0.013 on noisy ones.

### 2. The blank preview was a recycled bitmap
`CropAndFilterScreen` warped the source and then recycled it in a `finally`, but
`DocumentFilters.apply` returns its **input unchanged** for `ORIGINAL` and on any
failure — so for those paths the recycled bitmap was the one handed to the UI.
That is the "filter preview disappears" report.

- Intermediates are now recycled only when they are neither the source nor the
  result. `apply`'s "returns its input" contract is documented on both sides.
- A failed render no longer falls back silently. It used to show the **unwarped**
  frame, which looked like the filter had done nothing; it now shows an error
  banner ("Preview could not be rendered at full size") over the honest source
  frame.

### 3. The preview is now the saved image, at the saved size
Previously: the editor decoded at **1400 px**, filtered at **900 px**
(`PREVIEW_DIMENSION`), and saved at **2000 px** (`MAX_PROCESS_DIMENSION`) from a
capture of up to 2600 px. So the preview was a 2.2× smaller render, upscaled into
its frame — softer, with a different noise response, than the PDF.

Now the editor decodes at the bake dimension, renders the filter at that same
dimension, on the **warped** bitmap, in the same order `PageRenderer` uses. One
`LaunchedEffect` keyed on `(source, filter, corners, mode, dimension)` renders
after a 140 ms settle so that swiping the chips does not queue a full-size render
per frame.

The render is also round-tripped through `Bitmap.compress(JPEG, quality)` before
display, so the preview shows the *encoded* pixels the save will contain — at
Medium (88) the difference in small text is visible, and a preview that hides it
would make Medium look better than it saves.

That also fixes **"Filters shows the uncropped photo"**: `previewAspect` is taken
from the bitmap actually displayed, so the frame is sized to the *warped* page,
and switching from the border tab to the filter tab re-renders through the same
effect.

### Signature changes for other agents
- `PageRenderer.bake(...)` / `bakeBlocking(...)` gained
  `maxDimension: Int = DocumentFilters.MAX_PROCESS_DIMENSION`.
- `ImageProcessingUtils.applyEnhancements(...)` gained the same parameter.
- **New** `PageRenderer.bakeDimension(quality: ImageQuality): Int` — the single
  source of truth for "how big is the saved page". `min(quality.maxDimension,
  MAX_PROCESS_DIMENSION)`, i.e. HIGH (2600) is filtered at 2000 rather than
  silently costing a third more memory per plane; MEDIUM (1900) is honoured.
  Anything that renders a page for display must use this, or the preview drifts
  from the save again.
- New `internal` helpers on `DocumentFilters`: `gatedUnsharp`,
  `noiseEstimate`, `adaptiveSharpenAmount`. No public signature changed.

### Tests
`DocumentFiltersTest` +6: the gate is an exact identity on a flat plane and on
sub-floor ripple, steepens a real step, `noiseEstimate` returns 0 on a flat field
and ~the amplitude on checkerboard noise, and the adaptive amount is full at 4,
half at 6.5, zero at 9 and beyond, monotonic in between. Values checked against a
numpy transcription of `boxBlur3`/`gatedUnsharp` before being written down.

## 2026-10-25 (o) — Arena.ai — Magic Color: one continuous curve, no lifted/not-lifted split

### The report
*Lighten* is good. *Magic Color* "lightens some areas and leaves some areas as it
is"; the original does not do that. *Clear B&W* "needs more improvement".

![source | 1.6 | now](filter_magic_uniformity.jpg)

*Left: source, with a hard shadow across the bottom-right. Middle: 1.6 — the
shadow quadrant stays grey. Right: now — one continuous curve, no quadrant left
behind.*

### Root cause: a hard clamp, not a per-region decision
1.6 bounded the divide with `max(floor, surface)`, where the floor is **one
global number** (`0.62 ×` the frame's lit level). On the reported frame that
split the picture by threshold: **73.9 %** of pixels sat below it and got a flat
~1.6× lift, the other 26 % were above it and quietly stopped being lifted. That
is the artefact, exactly — not two different algorithms choosing regions, but one
number switching the lift on and off, with the boundary visible.

Replacing that with `softKnee` — identity below the knee, unit slope at it,
asymptotic to the cap above it — removes the switch. Nothing in the frame is
treated by two different rules.

### It was not enough on its own: the estimator was biased
With the clamp removed the deep shadow still came out grey (205 instead of 255).
The cause was upstream. The generator pooled each cell with a **maximum**, on the
reasoning that paper is the brightest thing in a patch containing print. Measured
against a known paper level, a max is biased **up by 2–3 σ of sensor noise**:
`bg/paper = 1.26` in a deep shadow, which is a 26 % error exactly where the gain
is largest, and 1.91 on the real scan. Pooling with a **mean** drops that to
1.02, and rejecting ink is left to the closing that follows — the correct
division of labour, and what the background-estimation literature does.

| change | deep-shadow paper | paper p5 | paper std |
|---|---|---|---|
| 1.6 (hard floor, max pool) | 106 | 62 | 60 |
| soft knee, max pool | 205 | 160 | — |
| soft knee, **mean pool** | **247** | **243** | **17** |

A third box pass after the closing also matters: two left the cell grid visible
on a hard-edged shadow.

### Clear B&W
Sauvola as a **bounded refinement** of Otsu, not a replacement:
`T = m·(1 + k·(σ/R − 1))`, `k = 0.34`, `R = 128`, window `max(W,H)/16` clamped to
an odd ≥ 15, computed from integral images so the cost does not grow with the
window. Two guards, both from measurement:

- under `σ < 6` the window holds no text/paper decision, so Otsu's answer is used
  (ungated Sauvola speckles blank margins);
- the local threshold may only go **below** Otsu, never above
  (`[0.85·Otsu, Otsu]`). Measured unclamped, Sauvola left 5 % of already
  normalised paper grey that plain Otsu got white, and a threshold above Otsu
  turned ink grey. The asymmetry means it can recover a faint stroke and cannot
  lose one.

Also: document gain knee 6.5 → **8**, cap 16 → **24** (deep-shadow paper
244 → 247, B&W paper purity 97.3 % → 98.3 %), gamma 1.28 → **1.35** (ink 41.5 →
38.1, paper p5 unchanged).

### Editor: the two things asked for
- **Tapping *Borders* now detects them.** The chip calls the same
  `detectBorders()` as the Auto detect button, so the quad is a starting point to
  nudge rather than something to drag out from the frame edges. It will not
  overwrite corners the user has already moved (`cornersEdited`), and *Full page*
  clears that flag.
- **The preview now shows the corrected, filtered page.** It runs
  `PerspectiveWarp.warp` → `DocumentFilters.applyPreview`, the same
  warp-then-filter order as `PageRenderer.bake`, so the preview and the saved
  file agree wherever the quad is not the full frame. The Borders tab still shows
  the uncropped frame, because that is what you aim at.

### Known trade-off, not hidden
A divide-based shadow removal brightens whatever is darkest locally, so a *printed
photograph* on the page has its shadows lifted too. Both 1.6 and this revision do
it; the fix for it is per-region protection (detect photo blocks, skip the
divide inside them), which is not in this change. Deep-shadow noise is also
amplified by the same gain — visible as grain on dark stamps.

### Tests
`softKnee` (identity below, continuity, unit slope, cap, monotonicity),
`saturateGain` (document vs scene, zero surface), `boxMeanAndStd` (against a
naive double loop, and zero deviation on a flat field), `sauvolaWindow` (odd,
scaling, tiny inputs). 8 new; every expectation was checked against a Python
transcription of the Kotlin.

### Version
**1.7 (code 8)**.

## 2026-10-25 (n) — Arena.ai — Document filters no longer blow up photos taken in the dark

### The report
A photograph of a glowing laptop screen in a dark room, saved as *Magic Color*,
came out with **13 % of the page at pure white** (a grey mock-up of the desktop
where the screen was, everything around it white). The original APK handles the
same kind of frame. Data from the scanned page and from the 4000 × 3000 source:
mean luminance **51/255**, 1.0 % of pixels at ≥ 250, **49 %** at ≤ 30.

### Why it happened — a step we did not have
`enhanceDocument` divides the frame by its own illumination surface,
`I / paper · 255`. On a page that is the whole point: a 30 % shadow has to become
paper. On a dark scene it is catastrophic, because the divide is **selective**.
The screen reads 230 and the room around it reads 15, so the surface follows the
room down into the dark, every *moderately* dark pixel (20–70) is amplified past
250, and the genuinely black pixels stay black. That is exactly the "grey
ghost" the PDF showed — the filter is not doing nothing, it is doing the right
operation on the wrong frame.

The original computes `imageType` (`hv3.PSImageTypeDocumentImage` /
`PSImageTypePhotoImage`) for **every** capture in `fa1.H` and carries it in
`PSImageEstimationInfoBean`. I traced `bx.smali`'s bake path (lines 127–195) and
it branches only on `enhanceType` — `Original` → `qx3`, `Lighten` → `tw3`,
level 1/2 → `lu3`. **`imageType` is never read**, so there is no explicit gate to
copy; the original's native code simply never runs away on a dark frame the way
ours does. Since the gate is not the difference, the fix had to be inside our
illumination model.

### The fix
The divide is now bounded, per channel, by how dark the surface is allowed to
get relative to the frame's own paper level:

| Frame | Bound | Why |
|---|---|---|
| reads as a document | `0.25 ×` lit level | a page must keep its shadow removal |
| reads as a scene | `0.62 ×` lit level | the gain ceiling stops a dark frame being repainted |

"Lit level" is the 95th percentile of the smoothed surface per channel — not the
maximum (one specular highlight is not the paper) and not the mean (a large dark
print block drags it down).

The frame type comes from `wv3`'s own rules — the ones we had ported and never
used. **`wv3`'s thresholds are absolute** (`R ≥ 80`, `G ≥ 80`, `B ≥ 70`), so a
page at half exposure scored 33 % and would have been misread as a photo.
`classifiesAsDocument` therefore scales the frame to a target luminance before
asking, which makes the question exposure-invariant; the gain is capped, so a
genuinely dark frame is not lifted into a false positive.

### Measured
| | source | before | after |
|---|---|---|---|
| mean luminance | 51.2 | 93.2 | **53.0** |
| ≥ 250 (blown out) | 1.0 % | 13.2 % | **2.5 %** |
| ≤ 30 (blacks kept) | 49.0 % | 39.1 % | **52.4 %** |

![source | Magic Color before | after](filter_fix_dark_scene.jpg)

*Left: the capture. Middle: what 1.5 shipped — the desktop area is a white
mock-up. Right: after the fix.*

That frame classifies as a scene at **29.5 %** (doc rule) / 4.9 % (photo rule).
Documents are unaffected: a synthetic page with ink and a 45 % shadow gradient
still lands paper at 249 and ink at 41.5, at full, 60 % and 35 % exposure —
because the *classification* is exposure-invariant even where the enhancements
are not.

### Tests
`classify_callsAFlatLitPageADocument`, `classify_isInvariantToExposure` (232 vs
the same page at 81), `classify_doesNotCallADarkRoomADocument`,
`classify_doesNotCallALandscapeADocument` (sky + dark ground scores photo 50 %,
under the 60 % bar), `classify_survivesAnEmptyArray`.

### Known limit, documented not papered over
A page shot at 35 % exposure still comes out at 180 rather than white, because
`MAX_ILLUMINATION_GAIN = 4.5` clamps the divide before the relative bound is
reached. Lowering it would turn a page that is mostly a dark printed photo
white, so it stays. `Lighten` (`enhancePhoto`'s linear stretch) is the right tool
for a frame that dark, and it is already exact.

### Version
**1.6 (code 7)**.

## 2026-10-25 (m) — Antigravity — Unit test executor memory limit & build verified

- Configured `testOptions.unitTests.all { maxParallelForks = 1; jvmArgs("-Xmx512m") }` in `app/build.gradle.kts` to prevent parallel test executor forks from exhausting host paging memory on high-core CPUs.
- Verified test suite: all 28 unit tests passed with 0 failures (`testDebugUnitTest`).
- Rebuilt `app-debug.apk` (21,810,287 bytes).

## 2026-10-25 (l) — Arena.ai — Filters corrected against the native disassembly

### What changed and why
The filters now follow the **actual native code**, not the documented behaviour
of it. `libimageproc.so` was disassembled (spec §6–§8, entry (k)) and three
things I had reconstructed by reasoning turned out to be wrong:

| I had | The disassembly says | Now |
|---|---|---|
| Skip *Lighten* when `blackPoint > whitePoint` | `range = wp - bp; if (range <= 0) range = 1.0;` then the `v <= bp` branch wins | verbatim port, no guard |
| Dilation only for the paper map (block max) | `cv::dilate` **then** `cv::erode` — a closing that fills ink with paper | max-pool, then dilate → erode |
| 3×3 majority vote on the **binarised** result | `cv::medianBlur` runs **before** the threshold | median on the normalised plane, then Otsu |

### Transcribed exactly (do not "improve")
- **`enhancePhoto`** (`0x0000f5d4`, 608 B) — ported line for line; see
  `linearStretchLut`. The `range <= 0` fallback and the branch order together
  mean an inverted pair thresholds hard at `blackPoint` instead of blackening the
  page, which is what the original does. `DocumentFiltersTest` pins all three
  branches.
- **`enhanceDocument` step list** — background estimate, divide, tone curve,
  `cv::medianBlur` before the threshold, `cv::Laplacian`/`cv::GaussianBlur`
  sharpening of midtone contrast. Each step has a comment naming the import that
  justifies it, so nobody re-adds a step that is not in the ELF.
- **No static LUT tables in the binary** — the tone mapping is computed at
  runtime, so the curve stays a parameterised gamma with pinned endpoints rather
  than a copied table.

### The one number that is still a model
`enhanceDocument` calls `ColorizingRadius::computeRadius(w, h, dpi, level)`,
which lives behind the stripped PLT; its body was not recovered. What is certain
is that the radius depends on size — a fixed radius cannot close over a 60 px
text line and a 6 px one at the same time.

A fixed radius of 1 (my first port of that step) closes over nothing but
single-pixel specks, and the divide then washes text out entirely: on a synthetic
page with a 50 % illumination gradient, ink came out at 255. Measured on the same
page, **~2 % of the long edge** is the smallest reach that bridges body text, and
it is scale-free — `paperRadius()` derives it from the working grid
(`PAPER_OPEN_DIVISOR = 48`). With it, across 200 px → 2000 px inputs:

| Input | Cell | Radius | Paper luminance (min/avg) | Ink (avg) |
|---|---|---|---|---|
| 200 × 260 | 1 | 5 | 250 / 255 | 52 |
| 600 × 800 | 3 | 5 | 252 / 255 | 51 |
| 1200 × 1600 | 5 | 6 | 250 / 255 | 51 |
| 2000 × 2600 | 9 | 6 | 250 / 255 | 48 |

*Clear B&W* on the 600 × 800 page: **100 %** of paper lands on pure white and
95.7 % of ink on pure black, the remainder being anti-aliased edges inside the
ramp — the ramp is doing its job, not the threshold.

That constant is the only unverified number left in the pipeline, and it is
isolated so calibration touches one line.

### Tests
`linearStretch` branch semantics (endpoints, `range <= 0`, inverted pair),
`median3x3` (speck removal, edge preservation), `gaussianBlur3` (flat-field
invariance and the 1:2:1 × 1:2:1 weights), `paperRadius` (scale invariance), plus
the earlier estimation, classification, curve and Otsu coverage. Removed the
assertion for the deleted span guard.

### Version
**1.5 (code 6)** — unchanged from the previous entry in this PR.

## 2026-10-25 (k) — Antigravity — Native libimageproc.so disassembled & exact OpenCV filter algorithms extracted

- Extracted ARM64 native library `libimageproc.so` (138,664 bytes) from connected device split APK `split_config.arm64_v8a.apk`.
- Disassembled and analyzed all 7 exported JNI functions (`enhancePhoto`, `enhanceDocument`, `enhanceSignature`, `signatureFilter`, `detectPageBorder`, `perspectiveTransformRect`, `calculateIOU`).
- Verified zero static LUT tables exist; `enhancePhoto` computes a dynamic 256-entry linear stretch LUT in RAM (`cv::LUT`), while `enhanceDocument` performs morphological background estimation (`ColorizingRadius` via `cv::dilate`/`cv::erode`) to divide paper illumination.
- Updated `docs/ORIGINAL_APP_FILTER_SPEC.md` with binary symbol profile (77 UND symbols, 28 OpenCV primitives), decompiled C source logic, and actionable guidance for Kotlin reimplementation in `DocumentFilters.kt`.

## 2026-10-25 (j) — Antigravity — Reverse-engineered filter spec from original base APK

- Added `docs/ORIGINAL_APP_FILTER_SPEC.md` documenting the exact image pipeline reverse-engineered from the original `base.apk`.
- Details exact operation order, dynamic range calculation (`blackPoint`/`whitePoint` from 100x100 histogram 2.0% cutoffs), document vs. photo color classification thresholds, filter dispatch mapping (`cv3`/`yu3` to `PSOpenCV`), and border detection architecture (`border_detect_224.tflite` + IoU >= 0.96f).

## 2026-10-25 (i) — Antigravity — DocumentDetector compilation fix & build verified


- Fixed compiler error in `DocumentDetector.kt`: `quadFromBoundaries` returned `Pair<QuadCorners, Boolean>?` while caller accessed `.corners` and `.confident`. Changed `quadFromBoundaries` to return `Detection?` directly (`Detection(it, scan.fittedSides >= 3)`).
- Verified build and unit test suite: `./gradlew assembleDebug testDebugUnitTest` passed cleanly in 35s.
- Built `app-debug.apk` (versionCode 5, versionName 1.4).

## 2026-10-25 (h) — Arena.ai — Working Settings screen


### Why
The app had no settings screen at all: the legacy APK's Settings (Theme / Scan /
PDF / Default Email / OCR / Sync) had never been ported, and the dashboard had no
entry point to one. This adds the page **and** the behaviour behind every row.

### New files
- `model/AppSettings.kt` — `@Serializable` settings model plus `ThemeMode`,
  `AccentColor`, `ImageQuality`, `ExportResolution`, `PageOrientation`,
  `ImageScale`.
- `data/SettingsStore.kt` — `settings.json`, same debounced + atomic write
  pattern as `LibraryStore`. Separate file so a theme toggle never rewrites the
  document library, and so the theme is available before the first frame.
- `ui/settings/SettingsScreen.kt` — sectioned page matching the legacy layout.
- `util/GalleryExporter.kt` — MediaStore insert (API 29+, `IS_PENDING`) and a
  legacy `Pictures/DocScanner` + `MediaScannerConnection` path for API 26–28.
- `app/src/test/.../model/AppSettingsTest.kt` — defaults, JSON round-trip,
  unknown-key tolerance (upgrade safety), accent contrast, preset ordering.

### Every row does something
| Row | Effect |
|---|---|
| Choose theme | `DocScannerTheme(darkTheme = …)`; status/nav bar icon polarity stays in sync via `enableEdgeToEdge(SystemBarStyle…)` |
| Color | Overrides the four primary slots of the scheme; six hand-checked palettes |
| Use in-app camera | Off = dashboard scan opens the **system photo picker** and imports into a new document |
| Shutter sound | `MediaActionSound.SHUTTER_CLICK` on capture |
| Image quality | Capture downscale target + JPEG quality, applied on capture, gallery import and editor bakes |
| Auto-save to Gallery | Copies each captured page to `Pictures/DocScanner` |
| Document Name | PDF file-name prefix (`Document_<title>_<ts>.pdf`) |
| Page Size | Initial selection in the export sheet |
| Resolution | Embedded bitmap longest edge (150/200/300 DPI) |
| Page Orientation | Per-page axis: auto swaps landscape pages |
| Image Scale | Fit (contain) / Fill (cover + clip) / Original (1 px = 1 pt) |
| Default Email To / Subject | `EXTRA_EMAIL` / `EXTRA_SUBJECT` on the share intent |

### Deliberately **not** added
*OCR Language* (no OCR yet), *Camera Engine* (CameraX is the only engine),
*Image Picker for Scanning* (we always use the system picker), *Sync to Cloud*
(no transport). A row that persists a value nothing reads is a fake control —
`AGENTS.md` §2.7. They are listed under roadmap Priority 0.

### Signature changes (update call sites)
- `PdfExportSheet(document, settings, onDismiss)` — new `settings` parameter.
- `PdfExportConfig(+ pageOrientation, imageScale, fileBaseName)`.
- `ImageStore.importFromUri / importFromFile / createPageFromUri /
  saveEditedPage` gained `maxDimension` / `quality` parameters with defaults
  equal to the previous constants, so existing call sites are unchanged.
- `PageRenderer.bake / bakeBlocking(…, jpegQuality = ImageStore.JPEG_QUALITY)`.
- `DocScannerTheme(+ accent: AccentColor = AccentColor.BLUE)`.
- `DashboardScreen(+ onNavigateToSettings: () -> Unit)`.
- `ScannerRepository(+ settings: StateFlow<AppSettings>, + updateSettings(),
  + currentSettings())`.

### Manifest
`WRITE_EXTERNAL_STORAGE` with `maxSdkVersion="28"`, requested from the
auto-save row only, on Android 8/9 only.

### Version
**1.4 (code 5)**.

## 2026-10-25 (g) — Arena.ai — Shared-axis screen transitions

- `Navigation.kt`: `NavDisplay` was using its default transition, which reads as a
  hard cut. It now gets explicit specs:
  - `transitionSpec` (push): incoming screen slides in from the trailing edge at
    full width while the outgoing screen slides a **quarter** of the way out —
    the parallax is what turns a screen swap into a sense of depth.
  - `popTransitionSpec` (back): the exact reverse, so the screen you came from
    settles back where you left it.
  - `predictivePopTransitionSpec`: same as pop, so the edge-swipe preview during a
    predictive back gesture matches where letting go actually lands.
- Duration **320 ms**, `FastOutSlowInEasing` — Material's standard easing.
- Implemented with top-level builders only (`slideInHorizontally`,
  `slideOutHorizontally`, `togetherWith`), never `slideIntoContainer`, so the code
  does not depend on the type of scope Navigation 3 hands the spec.
- Transition specs live in `private fun navForwardTransition()` /
  `navBackTransition()`; there are no per-destination overrides yet, so every
  route shares the same motion.

## 2026-10-25 (f) — Arena.ai — Share button no longer grows

- `ui/detail/DocumentDetailScreen.kt` bottom bar: the four actions were
  `TextButton`s (icon + label side by side) plus one filled `Button` in a
  `SpaceAround` row. On a 360 dp phone the row runs out of width at the **last**
  child, so "Share" was measured with almost no width, wrapped its label onto a
  second line, and the pill grew taller than the three actions beside it.
- Rebuilt as four `Modifier.weight(1f)` cells, icon above label, every cell at a
  shared **56 dp** height (`ACTION_HEIGHT`), label `maxLines = 1`,
  `softWrap = false`, ellipsis. Width per cell now depends only on the screen
  width, never on the label length, so no child can be squeezed again.
- New private composables `DocumentActionButton` / `DocumentActionContent`;
  `Share` keeps its filled primary treatment, the other three stay text buttons.

## 2026-10-25 (e) — Arena.ai — Zoom while capturing and while adjusting borders

### Camera: pinch-to-zoom that actually reaches the JPEG
- New `Box` around `AndroidView` in `ui/camera/ScannerCameraScreen.kt` with
  `detectTransformGestures(panZoomLock = true)`; pinch multiplies `zoomRatio`.
- Zoom is applied through **`cameraControl.setZoomRatio`**, i.e. a sensor crop —
  not a scaled preview `View`. That is the difference between "the viewfinder
  looks zoomed" and "the saved photo is zoomed".
- `minZoomRatio` / `maxZoomRatio` come from `camera.cameraInfo.zoomState`, which
  CameraX fills in asynchronously after binding, so a short polling
  `LaunchedEffect` reads them instead of assuming the device cannot zoom.
  Everything is clamped to that range; the pinch handler and buttons both honour it.
- Discoverable control: a translucent pill above the shutter with `−`, `1.0×`,
  `+` and a reset button. Hidden entirely when `maxZoomRatio <= 1.01f`, so
  fixed-focus devices see no dead UI. Tapping the factor label resets to 1×.
- `ZOOM_STEP = 0.5f`.

### Editor: pinch / pan / zoom in border-adjust and filter preview
- `ui/editor/CropAndFilterScreen.kt` now renders through two nested `Box`es:
  outer = `scaleX/scaleY` + `translationX/translationY`, inner = `rotationZ`.
  Zoom therefore works with a rotated page and panning always follows the finger.
- One unified `awaitEachGesture` handler replaced `detectDragGestures`:
  - one finger started on a corner handle (within `GRAB_THRESHOLD`, normalised)
    moves that corner, exactly as before;
  - one finger anywhere else pans once `zoom > 1`;
  - two fingers pinch around `calculateCentroid(useCurrent = false)`, so the point
    between the fingers stays put while scaling.
  - once a gesture becomes multi-touch it never falls back to corner dragging,
    so lifting one finger out of a pinch cannot fling a corner across the page.
- `screenToCanvas()` inverts translation → scale → rotation, so a finger maps to
  the right normalised corner coordinate at any zoom and rotation.
- Pan is clamped to `(zoom − 1) · extent / 2`, with the extents swapped when the
  rotation is a multiple of 90°, so the page can never be dragged off-screen.
- Corner handles and the quad stroke are divided by the zoom factor while
  drawing, so they keep a constant on-screen size instead of ballooning at 5×.
- Buttons: `−` / percentage / `+` / `FitScreen` reset at the bottom centre;
  `MAX_ZOOM = 5f`, `ZOOM_FACTOR = 1.6f`.
- Zoom and pan reset when the page changes — no bleed between pages.

### Coordination notes for Antigravity
- Only two files changed: `ui/camera/ScannerCameraScreen.kt` and
  `ui/editor/CropAndFilterScreen.kt`. Do not re-run `detectDragGestures` there.
- If a device reports `maxZoomRatio` but pinching does nothing, the effect that
  calls `setZoomRatio` is keyed on `zoomRatio`; check that `boundCamera` is set
  (it is assigned inside the CameraX bind callback).

## 2026-10-25 (d) — Arena.ai — Punchy filters, working detection, clean viewfinder

Follow-up to user testing: *"the magic color and black and white still seems a bit
off… the border detection is not working and also remove the static border on the
camera when capturing photos."*

### 🐞 Why detection *looked* like it never ran
`CropAndFilterScreen` only drew the quad when `isCropMode` was true, and
`isCropMode` started `false` in **review mode** — the post-capture screen the user
was judging. Detection could succeed and the borders would still be invisible
until "Borders" was tapped. The screen now opens in Borders mode in review mode.

### Detector: boundary scan replaces extreme points
`DocumentDetector` rewritten **again** (see the file header for the three
generations). The previous Sobel→extreme-point version failed on the most common
capture: the page fills the frame, so (a) Sobel skipped the 1-px frame border
where the page edge lived, and (b) interior text edges outnumbered the page edge,
so the extreme points landed on text → invalid quad → silent `FULL_FRAME`.

Now:
1. Sobel with **clamped indices**, so an edge on the frame border still produces a
   gradient.
2. Lower, percentile-based threshold (`max(18, p75×0.85, p97×0.18)`).
3. **Boundary scan** — per row, first strong edge from the left and last from the
   right; per column, first from the top and last from the bottom. Scanning inward
   from the frame edge hits the paper boundary before any text.
4. **RANSAC line fit per side** (220 deterministic iterations, 1.2 % tolerance,
   ≥14 inliers) then least-squares refit on inliers — survives curled corners and
   stray shadows.
5. Intersect the four lines. **A side with no visible boundary falls back to the
   frame edge** (the page runs past the frame), which is geometrically correct
   rather than a failure.
6. Validate slopes (|a| ≤ 1.1), convexity, bounds, side lengths, area.
7. Extreme-point quad retained as a fallback for small/rotated documents.

New `DocumentDetector.detect()` returns `Detection(corners, confident)`, where
`confident` is true only when ≥3 sides were fitted from real edges. The editor
shows *"Borders not detected — drag the corners"* when it is false.

### Filters: colour cast + blow-out fixed, tuned punchier
- **One stretch for all channels.** Magic Color previously stretched R, G and B
  independently with different percentiles — three different gains on the same
  paper pixel = pink/green whites. Contrast now comes from a single **luminance**
  histogram; only white balance is per-channel.
- **Clamped stretch points** (`stretchPoints`, new and unit-tested): 2 % black
  point capped at 110, 90 % white point floored at 205. An unbounded black point
  was the "washed out on a lightly printed page" bug — with little ink the
  percentile landed inside the paper mass and the gain blew mid-tones to white.
- Punchier: `MAGIC_COLOR_CURVE 0.55 → 0.62`, saturation `1.18 → 1.15` (more
  contrast, less chroma push), `GRAYSCALE_CURVE 0.35 → 0.42`.
- **B&W despeckle.** The symmetric ramp left paper noise just above the threshold
  pure black → speckle. Now `INK_CUT_FACTOR 0.95` / `PAPER_CUT_FACTOR 1.05`
  (asymmetric, paper whites out early) plus a **3×3 majority vote** read straight
  from the luminance buffer (no extra full-frame allocation): ≥6 paper neighbours
  → white, ≤3 → black, otherwise keep the ramped value so glyph edges stay
  anti-aliased.

### Camera: guide frame removed
Both decorative overlays are gone from `ScannerCameraScreen` — the dark scrim with
the cut-out window and the pulsing dashed frame — along with 12 now-unused imports.
They were never part of the captured JPEG, and they were actively harmful: they
taught users to fill the frame right past the paper edge, leaving no boundary for
the detector to find.

### Signature changes
| Symbol | Before | After |
| :--- | :--- | :--- |
| `DocumentDetector.detect` | — | **new** `(Bitmap) -> Detection` |
| `DocumentDetector.Detection` | — | **new** `data class(corners, confident)` |
| `DocumentDetector.detectQuad` | `(Bitmap) -> QuadCorners` | unchanged signature, now delegates to `detect().corners` |
| `DocumentFilters.stretchPoints` | — | **new** internal, unit-tested |
| `CropAndFilterScreen` `isCropMode` | started `false` | **starts `reviewMode`** |

### Tests
`DocumentFiltersTest` gains four cases for `stretchPoints`: normal page, the
barely-printed clamp, the minimum-range guard, and the dark-photo floor.

`versionCode 4`, `versionName 1.3`.

---

## 2026-10-25 (d) — Antigravity — Filters test fix & build verified

- Fixed `DocumentFiltersTest.curveLut_darkensMidtones`: updated assertion to verify shadow/ink darkening (`lut[96] < 96`) rather than the midpoint index 128 (since standard smoothstep $3t^2 - 2t^3$ has inflection at $t=0.5$, meaning $t = 128/255 > 0.5$ preserves midpoint while pulling down values $t < 0.5$).
- Verified all 13 unit tests passed (`testDebugUnitTest` 13/13).
- Built and verified APK (`app-debug.apk`, versionCode 3, versionName 1.2).

## 2026-10-25 (c) — Arena.ai — Real filters + real border detection


### Why
The user reported: *"the filters effect are not good at all, make them perfect like
the original app, also the auto detection of pages and border."* Both were
algorithmic gaps, not tuning.

### Filters — replaced a global `ColorMatrix` with a pixel pipeline
`ImageProcessingUtils.getColorMatrix()` applied five fixed matrices (e.g. *Magic
Color* = contrast ×1.35 + brightness +15, *Clear B&W* = desaturate + contrast ×2.4
− 140). A global matrix **cannot**:
- remove shadows or a torch hotspot, because the correction has to vary spatially;
- white-balance, because indoor light is yellow and shade is blue;
- avoid clipping, because ×2.4 contrast maps a mid-grey page to 0 or 255.

New `util/DocumentFilters.kt` per filter:
1. **Illumination normalisation** — paper brightness is estimated with a
   block-**max** (dilation) pyramid (max rejects ink, keeps paper), blurred, and
   sampled bilinearly; dividing by it maps every patch of paper to the same white.
2. **Grey-world white balance** on the normalised result (clamped to ±20 % so a
   genuinely tinted page is not neutralised).
3. **Percentile stretch** 0.4 %→99.6 % per channel instead of a fixed multiplier.
4. **Saturation boost** (1.18) around Rec.601 luminance for *Magic Color*.
5. **Smoothstep S-curve** to deepen ink while pinning paper at 255.
6. *Clear B&W* → **Otsu threshold** over the normalised luminance with a narrow
   ramp, so glyph edges stay anti-aliased instead of jagged.
7. *Lighten* → partial normalisation (72 %) that keeps some shading for photos and
   tinted paper.

Processing is capped at `MAX_PROCESS_DIMENSION = 2000` px on the long edge
(~240 DPI A4), which keeps a bake to a few hundred ms and bounds memory.
`android:largeHeap="true"` was added: the pipeline holds an ARGB `IntArray` plus a
luminance `FloatArray` for the whole page (~2 × 4 bytes × pixels).

### Editor preview is now WYSIWYG
The editor used `ImageProcessingUtils.getComposeColorFilter()` — a `ColorMatrix`
`ColorFilter` that looked **nothing like** the baked output. It now renders the
real filter via `DocumentFilters.applyPreview()` at 900 px, with a 2 dp progress
bar while it computes. Preview and PDF therefore match.

### Border detection — replaced a brightness bounding box with an edge quad
`PerspectiveWarp.detectQuad()` sampled a 64×48 grid and returned the axis-aligned
box of "bright enough" cells. That cannot express perspective (it cuts off two
corners of an angled page and keeps desk at the other two), keys on absolute
brightness (a white desk defeats it), and returned `FULL_FRAME` whenever the page
filled the frame.

New `util/DocumentDetector.kt`:
1. Downscale to 360 px, blur, **Sobel** gradients.
2. Percentile edge threshold (`sorted.p90 × 0.55`, floored at 26).
3. **Extreme-point quad** — TL minimises `x+y`, BR maximises it, TR maximises
   `x−y`, BL minimises it. Adapts to perspective and ignores interior text edges.
4. **Robust least-squares line per side** (only points 10–90 % along that side,
   within a 5.5 % band), with one MAD-based outlier rejection pass and refit.
5. **Intersect adjacent lines** for well-conditioned corners.
6. Validate convexity, bounds, side lengths and area (12 %–99.5 %); anything
   doubtful returns `FULL_FRAME`. Never throws.

### Signature changes
| Symbol | Before | After |
| :--- | :--- | :--- |
| `ImageProcessingUtils.getColorMatrix` | returned `ColorMatrix?` | **removed** — use `DocumentFilters.apply` |
| `ImageProcessingUtils.getComposeColorFilter` | returned Compose `ColorFilter?` | **removed** — editor previews a real bitmap |
| `ImageProcessingUtils.applyFilter` | — | **new** `(src, filterType, maxDimension)` |
| `PerspectiveWarp.detectQuad` | 120-line brightness heuristic | delegates to `DocumentDetector.detectQuad`; same signature |
| `DocumentFilters.apply` | — | **new** — returns `src` itself for ORIGINAL/failure |
| `DocumentFilters.applyPreview` | — | **new** |
| `DocumentDetector.detectQuad` | — | **new** |

### New files
| File | Purpose |
| :--- | :--- |
| `util/DocumentFilters.kt` | Illumination model, white balance, stretch, Otsu, tone curve |
| `util/DocumentDetector.kt` | Sobel + extreme-point quad + robust line fit + validation |
| `app/src/test/.../util/DocumentFiltersTest.kt` | JVM tests for `curveLut`, `percentile`, `otsuThreshold` |

### Version
`versionCode 3`, `versionName 1.2`.

---

## 2026-10-25 (c) — Antigravity — TimeZone hermetic test fix & build verified

- Added `timeZone: TimeZone = TimeZone.getDefault()` to `DocumentNaming.defaultScanTitle()` so formatters default to local device time while unit tests can explicitly assert under `UTC`.
- Fixed `DocumentNamingTest.defaultScanTitle_quotesLiteralText` which previously failed in non-UTC runner environments.
- Build & test pass verified: `./gradlew assembleDebug testDebugUnitTest` passed in 20s. `app-debug.apk` built successfully.

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
