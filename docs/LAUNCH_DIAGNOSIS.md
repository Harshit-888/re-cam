# Launch diagnosis: ~10 seconds to a quick, snappy start

> **Status: diagnosis only. No code changed, no signature changed, no file deleted.**
> **Author: Muse (review-only agent — no JDK / Android SDK in its sandbox, like Arena).**
> **Base: `main` at `087cf89` (Round 67), read 2026-10-30. Not verified on a device.**
> **Owner's constraint: launch speed beats storage space. The app may take more space if it is quick.**

## The user report, in the owner's words

The doc scanner app lags for about 10 seconds on initial launch. The owner wants
it quick and snappy, even if it takes more space.

## The one-sentence opinion

There is no single 10-second bug. Round 65 fixed one of the three launch phases,
and the other two are still in the code. Do not start tuning decodes until the
measurement split in §5 has told you which phase the 10 seconds actually is.

## 1. The three phases — do not conflate them

| Phase | What it is | Where it is measured (if at all) |
| :---- | :--------- | :------------------------------- |
| A. Tap → first frame | Process start, ART class loading / verification, Compose + Material 3 init | `MainActivity` logs "launch: first frame drawn … ms after process start" (`DocScannerPerf`) |
| B. First frame → usable grid | The splash gate + the first cover decodes | `LibraryViewModel` logs "launch: library ready … ms after the ViewModel was created" |
| C. Grid appears → feels smooth | Background work leaking into the first ~10 s of scrolling / looking | Not measured by any launch log. This is the phase Round 65 was about, and the one most likely to still be felt |

`MainActivity` only shows `LaunchSplash` while `library.repository` is null, then
crossfades over 300 ms (`SPLASH_FADE_MS`). A 300 ms crossfade is not 10 seconds,
and the old 320 ms splash floor was already removed in Round 65. Do not chase
either of them.

## 2. Ranked hypotheses, with the evidence in the tree

### H1 — The splash gate blocks on the whole library (most likely if the splash itself lasts seconds)

`MainActivity` → `LibraryViewModel.init` → `ScannerRepository(context)` on
`Dispatchers.IO` → `loadLibrary()` → `LibraryStore.load()`.

`LibraryStore.load()` does `file.readText()` and then a full
`json.decodeFromString` of the entire `LibrarySnapshot`: every document, every
page, and every page's OCR text. `SettingsStore.load()` is read in the same
constructor. Nothing can appear before that parse finishes, by design ("first
composition sees the restored library, not an empty flash").

Consequence: launch time grows linearly with library size and OCR volume.
A single JSON snapshot was a fine choice for a small library. It is not a
launch-critical data store.

**How to prove / disprove it:** the "library ready" log line, plus the size of
`library.json` and the document/page count. If "library ready" is itself
seconds, H1 is confirmed and H2/H3 are secondary. If it is tens of milliseconds
on the owner's library, H1 is dead — move on.

### H2 — The build / cold-start cost (most likely if even an empty library is slow)

Evidence in the tree, not a guess:

- `app/build.gradle.kts`, release: `isMinifyEnabled = false`. No shrinking.
- No Baseline Profile found in the repo, no SplashScreen API, no custom
  `Application` class doing work at process start (so process cost is class
  loading, not app code).
- Dependencies loaded into the same classpath: Compose + Material 3 (+ Icons
  Extended), Navigation 3, CameraX, ML Kit text recognition, TFLite.
- The `benchmark` build type exists precisely because `assembleDebug` is
  debuggable (ART interprets / JITs, Compose debug hooks live) and is not a
  fair judge of launch speed.

If the owner is testing the debug APK — which is what has been built in
previous rounds — a large share of "10 seconds" may be build artifact, not
app code. That must be ruled out before any code is rewritten.

**How to prove / disprove it:** force-stop, cold-start the benchmark APK vs
the debug APK on the same device, same library. If benchmark is dramatically
faster, H2 dominates. If both are ~10 s, H2 is not the story.

### H3 — Background work still leaks into the first 10 seconds (most likely if the grid appears but stutters)

Round 65 was real work, and it did fix the old pattern (prefetch at 48 ms,
thumbnail repair at 2.2 s + 1.2 s, OCR backfill at 2.2 s + 3 s, orphan sweep
at 5 s, all un-gated). What it replaced it with is in
`util/StartupWork.kt` and `ui/dashboard/DashboardScreen.kt`:

- Cover prewarm: after 140 ms quiet, 6 rows (12 covers).
- OCR backfill: after 2,500 ms quiet (`OCR_IDLE_MS`), up to 24 pages
  (`OCR_BACKFILL_BATCH` in `ScannerRepository`), one ML Kit inference plus a
  2400 px decode per page (`OcrEngine.readPage`).
- Thumbnail repair: after 4,000 ms quiet (`REPAIR_IDLE_MS`), full-page decode
  + JPEG encode per page, 8 per chunk (`PRIME_LIMIT`), 350 ms between chunks.
- Orphan sweep: after 6,000 ms quiet, via `StartupWork.runWhenQuiet`.

The flaw in the gate: "quiet" means *no pointer touch and not scrolling*.
A user who opens the app and just **looks** at the list is quiet by that
definition. So after 2.5 s of looking, OCR starts; after 4 s, repair can
overlap it; after 6 s, the sweep can join. That is exactly the window in
which the owner starts scrolling.

Worse, the gate is not even uniform: the orphan sweep runs on
`StartupWork`'s single-thread dispatcher, but OCR backfill runs on the
repository's unbounded IO scope and `OcrEngine.readPage` decodes on
`Dispatchers.Default`. Thumbnail repair (`PageImageLoader.primeThumbnails`)
also runs on plain IO. So the three passes can compete with the visible
tiles' decode pool (`PageImageLoader.decodeDispatcher`, limited to 2) at
the same time, on the same cores the frame needs.

**How to prove / disprove it:** watch `DocScannerPerf` for
"ocr-backfill", "thumbnail-repair" / prime, and "orphan-sweep" lines during
the first 10 s, and correlate them with dropped frames. If the grid is
already up and the stutter starts at ~2.5 s, H3 is confirmed.

### H4 — Cover decodes on the first grid (contributing, not the whole story)

The cover path is well designed and should not be blamed first:

- `ThumbnailStore`: 768 px JPEG per page, written at save time
  (`ImageStore` save paths call `writeFor`), ~40 KB each, in `filesDir`
  (not `cacheDir`, so it does not silently expire).
- `PageImageLoader`: memory cache at 1/5 of the heap, decode pool limited
  to 2, prefetch gate of 1, single-flight dedup, `RGB_565` for tiles.
- Dashboard grid items are keyed by document id, with a single content type.

The one self-defeating rule in this path:

- `ThumbnailStore.MAX_FILES = 600`: once the directory passes 600 thumbnails,
  the oldest half is swept. A library with more than 600 pages therefore has
  permanently cold older covers: every launch, those tiles fall back to a
  full-page decode — and for Clear B&W / Grayscale pages that is a 3200 px
  PNG, ~30 MB transient, through the same 2 decode slots.

**Space maths, for the owner's stated trade:** 1,000 thumbnails × ~40 KB
≈ 40 MB. The photos themselves are 1–3 MB per page. A 600-file cap saves
tens of megabytes at most, and buys launch latency in return. That trade
is backwards under the owner's constraint, and it contradicts the app's
own earlier trade (1/5 heap for the bitmap cache, thumbnails in `filesDir`
precisely so they do not expire).

**How to prove / disprove it:** `PerfTrace` counts — thumbnail decodes vs
full-page decodes on first launch. Many page decodes on the first screen
means the thumbnail layer is missing, not that decoding is slow.

### What is NOT a prime suspect (checked, no evidence on the launch path)

- CameraX, TFLite border model (4.7 MB, memory-mapped, `noCompress`), ML Kit
  recogniser, and Rust kernels: all first-use / lazy on the paths checked.
  They are not loaded by the dashboard launch. Do not blame them first.
- Dashboard sorting / date labels: `activeDocs` is re-sorted on each
  `documents` emission, but for a library of hundreds of documents that is
  milliseconds, not seconds. Revisit only for a library of thousands, and
  only after H1–H4 are ruled out.

## 3. Ranked fix plan

### P0 — Measure first (no code, must happen before any fix)

See §5. No fix should be merged on the basis of this document alone: every
hypothesis above is ranked, not confirmed, because no device log, library
size, or build type was available to the diagnosing agent.

### P1 — Build: Baseline Profile + R8 + SplashScreen API

- Add a Baseline Profile for the launch → dashboard path (Macrobenchmark).
- Enable R8 / shrinking for release, with keep-rules verified for
  kotlinx.serialization, TFLite, and ML Kit. A broken keep-rule is worse
  than no shrinking, so this needs a real release-build smoke test.
- Use the SplashScreen API instead of a Compose-only splash, so the system
  splash covers process/class-load time.

Expected win: largest if H2 dominates. Risk: low code risk, medium
verification risk (keep-rules, release smoke test).

### P2 — Get background work out of the launch window entirely

- Move OCR backfill, thumbnail repair, and orphan sweep to WorkManager
  with charging / device-idle / not-low-battery constraints, or — as a
  smaller step — a much longer delay (tens of seconds, not 2.5–6 s) and a
  single low-priority dispatcher shared by all three passes.
- Searching an old page a minute later is a fine trade. Dropping frames
  in the first 10 seconds is not.

Expected win: largest if H3 dominates ("grid appears, then stutters").
Risk: low. No signature change.

### P3 — Remove the thumbnail cap, keep every cover precomputed

- Remove or raise `ThumbnailStore.MAX_FILES` so covers are never swept
  merely for count. Keep the sweep only as a true storage emergency, not
  a 600-file rule.
- Keep writing thumbnails at save time (already done). Never generate a
  cover at launch time if a save-time one can exist.

Expected win: large if H4 dominates, and it is pure space-for-speed, which
is exactly the owner's stated trade. Risk: low. Space cost: ~40 KB per page.

### P4 — Split the launch-critical data (largest long-term win, do not rush it)

- Parse a lightweight index first (id, title, dates, folder, page count,
  cover URI), show the dashboard shell immediately, load full documents
  and OCR lazily / on demand.
- Move OCR text out of `library.json` into its own store (separate file,
  or a Room table in a later migration). OCR is search data, not list data,
  and it should never be on the launch-critical path.
- Only after P4: consider Room / paging for very large libraries, and
  maintaining a sorted list instead of re-sorting on every emission.

Expected win: largest if H1 dominates at scale. Risk: high — this changes
persistence, backup/restore (`BackupArchive` reads `library.json`), and
the §3 integration contract. It needs its own claim, its own changelog
entry, and a migration / compatibility plan. Do not bundle it with P1–P3.

### P5 — Only then, micro-tune

- Burst decode parallelism for the first viewport only (2 → 4), then
  throttle back to 2 for scrolling.
- Hardware bitmaps / further tile-size tuning, if Macrobenchmark still
  shows decode time on the critical path.

Do not start here. Micro-tuning a 2-slot decode pool will not fix a
whole-library JSON parse, a missing Baseline Profile, or an OCR pass
starting at 2.5 seconds.

## 4. What not to chase

- The 300 ms `Crossfade` and the removed 320 ms splash floor: not 10 seconds.
- A single decode being "slow": the system is a queue + a gate + a parse,
  not one function.
- Saving space: the owner has explicitly traded space for speed. Any fix
  that saves megabytes by re-decoding at launch is going in the wrong
  direction.

## 5. Measurement split the next agent must run first

Run these before writing any fix, and paste the numbers into the next
changelog entry:

1. Force-stop the app. Cold-start it on an **empty library** and on the
   owner's real library, on the **same device**.
2. Repeat both on the **benchmark APK** (`assembleBenchmark`,
   non-debuggable, debug-signed) and on the **debug APK**.
3. `adb logcat -s DocScannerPerf` and record:
   - "launch: first frame drawn … ms after process start"
   - "launch: library ready … ms after the ViewModel was created"
   - Any "ocr-backfill", thumbnail-repair / prime, or "orphan-sweep"
     lines inside the first 10 seconds.
4. Record `library.json` size (bytes), document count, page count, and
   the thumbnail-decode vs full-page-decode counts from `PerfTrace`.
5. Decision table:
   - Empty library also slow → H2 (build) dominates.
   - "library ready" itself is seconds → H1 (parse) dominates.
   - Grid up, stutter starts at ~2.5 s → H3 (background) dominates.
   - Many full-page decodes on the first screen → H4 (thumbnails) dominates.

## 6. Prompt for the next agent (copy / adapt)

> Read `docs/LAUNCH_DIAGNOSIS.md` at base `087cf89` before touching anything.
> Run the §5 measurement split first and report the numbers. Do not start
> with decode micro-tuning. Claim your files in
> `docs/AGENT_COORDINATION.md` §4 before editing them, add a changelog
> entry, and do not change any §3 integration-contract symbol without
> updating every caller in the same commit. Muse is review-only (no JDK /
> Android SDK); Antigravity is the build authority — `./gradlew
> testDebugUnitTest assembleDebug` (and the benchmark APK for launch work)
> plus the §5 device checks are owed by Antigravity before this is called
> fixed. A fix that saves space by re-decoding at launch contradicts the
> owner's stated trade and must not be merged.

## 7. Open questions for the owner

- Is the 10 seconds a blank / splash screen, or a grid that appears and
  then stutters? The answer chooses between H1+H2 and H3+H4.
- How large is the owner's library (documents, pages, `library.json`
  size), and which build (debug / benchmark) was the 10 seconds measured on?
- Is a WorkManager-based deferral of OCR backfill acceptable, even if
  search over old pages becomes available minutes later instead of
  seconds later?
