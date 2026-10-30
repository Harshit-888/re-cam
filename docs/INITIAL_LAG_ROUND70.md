# Initial dashboard lag — Round 69 review + Round 70 fixes (Muse, unbuilt)

> **Status: code changes + diagnosis. NOT built, NOT tested by Muse.**
> **Author: Muse AI (independent reviewer — no JDK / Android SDK in its sandbox).**
> **Base: `main` at `9950c0a` (Round 69, Antigravity).**
> **Branch: `muse/initial-lag-round70`. Do NOT merge to `main` until Antigravity
> runs `./gradlew testDebugUnitTest assembleDebug assembleBenchmark` and the
> device checks in §6 below, and records them in H004.**
> **Owner's report, still open: the app lags when it first opens, the dashboard
> lags on initial open. Owner's trade: speed over space.**

## 1. Round 69 review — what Antigravity actually did, and what it did NOT do

Round 69 (`9950c0a`) is real, useful work, and it is not a launch fix.
That distinction is the whole point of this document.

### What Round 69 did well (verified by reading the diff, not by running it)

- **§6 benchmarks, on a benchmark APK, on a real device** (Realme Narzo 30
  Pro 5G, Android 12): bake per filter at 3200×2400, 10-page batch
  sequential vs 2-parallel, preview 2400 px vs 1080 px, PDF platform vs
  custom writer, OCR 2400 px vs 1600 px. Numbers are in H003. The honest
  parts matter: "with Rust .so present" is marked **UNAVAILABLE** (no
  cargo/cargo-ndk on the build machine), so every number is the Kotlin
  path. That honesty is correct and must be kept.
- **Rank 1 (measurable Rust shipping):** `NativeFilters.EXPECTED_ABI` made
  public, `DocScannerPerf` log of `isLoaded` + ABI on init, and a startup
  log in `MainActivity`. Good idea, wrong place — see §3.
- **Rank 2 (bounded parallel batch bakes):** `Semaphore(2)` in
  `CropAndFilterScreen.applyFilterToPages`, single final publish kept.
  Measured 20.0 s → 11.6 s (1.72×) on a 10-page SMART batch. Plausible.
- **Rank 3 (display-size preview):** `PREVIEW_RENDER_DIMENSION` 2400 → 1080,
  plus a preview cache per (page, filter, corners, rotation). Measured
  1610 ms → 450 ms (3.58×), re-taps 0 ms. Plausible.

### What Round 69 did NOT do — and why the owner's lag is still there

1. **It did not measure the launch split (H002).** H003 records a cold
   launch of **512 ms TotalTime / 516 ms WaitTime** on the benchmark APK,
   but that is ActivityManager's time to first frame, not time to a
   usable, smooth dashboard. It does **not** record what H002 asked for:
   `library.json` size, document/page counts, "first frame drawn" vs
   "library ready" `DocScannerPerf` lines, or thumbnail vs full-page
   decode counts. A sub-second activity start and a 10-second dashboard
   are not contradictory — they are different measurements, and only the
   second one matches the owner's report.
2. **Its benchmark page is synthetic.** `PerfAuditRunner.createTestPage`
   draws white + rendered text lines, not a photographed page. Bake,
   paper-map, fine-pool and OCR numbers from it are indicative, not
   photographic. OCR "100% accuracy at 1600 px" on rendered text is
   **not** evidence for real photos, and must not be used to justify
   lowering OCR resolution for real pages (Round 70 does not do that).
3. **Its PDF comparison is not like-for-like.** Platform writer at
   ORIGINAL (lossless) vs custom writer at BALANCED (JPEG) — the 4.27× /
   45% smaller gap is mostly compression, not writer speed.
4. **Its batch OOM fallback does not exist as claimed.** The changelog
   says "automatic sequential fallback on OOM". In the code, `oomOccurred`
   is set and **never read anywhere**, and the retry bakes the same page
   at the **same** dimension — after `PageRenderer` had already stepped
   its own dimension down on OOM. That retry repeats work that just
   failed. Round 70 fixes this (§4).
5. **Its preview cache leaks memory.** 8 entries at 1080 px ARGB ≈ 48 MB,
   held alongside the grid cache and the bake pipeline, and an evicted
   entry's bitmap is never recycled. Round 70 fixes this (§4).

None of this is a criticism of Antigravity's measurements — they are the
first real numbers this project has had. It is a warning against reading
"512 ms cold launch" as "the launch problem is solved".

## 2. Why the dashboard still lags on first open (current code, 9950c0a)

After the splash crossfade (300 ms) and the library parse (IO thread),
the dashboard itself does this, in the first seconds:

- **First tiles compete for 2 decode slots.** `PageImageLoader`'s decode
  pool is 2. If a tile has no stored thumbnail, it decodes the full page
  (3200 px PNG for Clear B&W / Grayscale is the worst case) through
  those slots. 12 visible tiles, 2 slots, grey tiles popping in over
  seconds — that *feels* like lag even when the process started fast.
- **The idle clock was wrong.** `StartupWork.lastInteractionNanos` is set
  at process start. A user who opens the app and has not touched anything
  yet is therefore already "idle", so OCR (2.5 s) and thumbnail repair
  (4 s) could start while they were still settling in. OCR is the most
  expensive pass in the app: ML Kit inference + 2400 px decode per page,
  up to 24 pages, on `Dispatchers.Default` (not on `StartupWork`'s
  single thread). Repair is a full decode + JPEG encode per missing
  thumbnail, in chunks of 8. Both land in the exact window the owner
  reports.
- **The thumbnail cap still hurts.** `ThumbnailStore.MAX_FILES = 600`
  sweeps the oldest half of thumbnails. A library over 600 pages has
  permanently cold older covers. Round 69 did not change this, even
  though the launch diagnosis (P3) and the owner's space-for-speed
  trade both point at it.
- **The library parse is still the gate.** `LibraryViewModel` → 
  `ScannerRepository` constructor → `LibraryStore.load()` =
  `file.readText()` + full decode of every document, page and OCR
  string, before anything can render. That has not changed, and it
  grows with library size. Round 70 does **not** fix this structurally
  (that needs the lightweight-index / Room work from
  `LAUNCH_DIAGNOSIS.md` P4, which is too risky to bundle here). What
  Round 70 does is protect the dashboard *after* the parse, and make
  the parse measurable (§5).

## 3. Round 70 fixes — what changed, file by file

All changes are in this branch. No §3 integration-contract symbol was
renamed or re-signed. No file was deleted. No Rust, ABI, or filter
output was changed — so no pixel parity test is needed for this round.

### MainActivity.kt — native log off the main thread, after the first frame

Round 69's startup log ran `System.loadLibrary` (via
`NativeFilters.isLoaded`) in `onCreate`, before `setContent`. That is a
regression on the first frame, for a status nobody needs until the first
filter runs. Now the log and the `run_benchmarks` trigger run inside
the first-frame `LaunchedEffect`, after `withFrameNanos`, on IO.
Benchmark work no longer competes with the splash, the library parse
or the first tiles.

### DashboardScreen.kt — protect the first 10 seconds

- **Idle clock reset on dashboard appearance:** a `LaunchedEffect(Unit)`
  calls `StartupWork.markInteraction()` when the dashboard first
  composes. "Quiet" now counts from when the user can see the list,
  not from when the process forked.
- **OCR idle 2.5 s → 10 s, repair idle 4 s → 12 s.** Search over old
  pages and thumbnail repair are delayed, not removed. Both still pause
  on any touch or scroll. The trade is explicit: a few seconds of
  search/repair delay buys a protected first 10 seconds of dashboard.
- **Preview cache is not touched here** (it lives in the editor, §4).

### ScannerRepository.kt — OCR backfill batch 24 → 8 per launch

At Round 69's ~1.2 s per page, a 24-page backfill is ~29 s of ML Kit +
decode CPU in the launch window. 8 pages caps the first launch's cost.
Backfill runs once per app start, so the rest is picked up on later
launches, not lost. No accuracy change: the decode size stays 2400 px.

### PageImageLoader.kt — repair chunk 8 → 2 pages

A repair chunk was 8 full-page decodes back to back. With the
dashboard's 350 ms pause between chunks, 2 pages per chunk makes repair
a background trickle instead of a burst. It finishes later, not faster
— that is the point.

### ThumbnailStore.kt — cap 600 → 5000 files

At ~40 KB per thumbnail, 5000 files is ~200 MB worst case, still far
below the pages themselves, and it covers libraries up to 5000 pages
instead of 600. It is still a bound (above 5000, the oldest half is
swept as before), not unbounded growth. Owner's trade, applied.

### CropAndFilterScreen.kt — three Round 69 corrections

- **OOM retry at half dimension, not the same dimension.** A page that
  OOMs is retried once at `dimension / 2`, with a `DocScannerPerf` log
  line (`[batch-bake] OOM for page …, retrying at …px`) so the
  downgrade is visible, not silent. The unused `oomOccurred` flag is
  removed — a flag that is never read is worse than no flag, because
  it makes the changelog claim look implemented.
- **Preview cache 8 → 4 entries, with recycle on eviction.** Evicted
  bitmaps are recycled via `asAndroidBitmap().recycle()`. Access order
  means the eldest is the least recently used, never the bitmap on
  screen. 4 entries still covers tap-back between recent filters.
- **`applyingPage` writes confined to Main.** Parallel bakes on
  `Dispatchers.Default` were writing Compose state directly. The write
  is now wrapped in `withContext(Dispatchers.Main)`. The value is
  completion order, not page order — the sheet shows progress, not a
  position, and the comment says so.

## 4. What Round 70 deliberately does NOT do

- No library-parse split, no Room migration, no Baseline Profile, no R8.
  Those are the big launch wins, and they are also the big risks. They
  belong in their own round, with their own measurements, not bundled
  with dashboard fixes.
- No OCR resolution change. Round 69's 1600 px result is synthetic-only.
  Do not lower OCR for real pages until Antigravity measures accuracy
  on real photos.
- No Rust change, no ABI change, no filter change. The user's actual
  device has no Rust .so (toolchain absent), so Rust is irrelevant to
  their lag today. Fixing the Kotlin dashboard path helps them now;
  fixing Rust helps them only after the toolchain exists.
- No change to the splash, the crossfade, or the grid's content type /
  keys. Those are already tuned.

## 5. Easier to debug — what to look for now

All of these are plain `Log.i` under `DocScannerPerf`, visible with
`adb logcat -s DocScannerPerf` on a benchmark build (benchmark is
non-debuggable, so `PerfTrace` frame counters are off there — these
log lines are the ones that still work):

- `[startup] Native filters status: …` — now after the first frame, not
  in `onCreate`. If it appears before "first frame drawn", the move
  regressed.
- `launch: library ready … ms` and `launch: first frame drawn … ms` —
  the gap between them is the dashboard composition + first tiles.
  If the gap is seconds, the problem is tiles / composition, not the
  parse. If "library ready" itself is seconds, the problem is the
  parse, and Round 70 will not fix it — that needs P4.
- `[batch-bake] OOM for page …, retrying at …px` — new in Round 70.
  If this appears often, the device is memory-constrained and parallel
  batch bakes should be reduced to 1, not increased.
- `ocr-backfill: … page(s) queued` — should now be at most 8, and
  should not appear within 10 s of the dashboard appearing.
- Thumbnail vs full-page decode counts from `PerfTrace` (debug builds
  only, Settings → Performance). If full-page decodes are common on
  the first screen, thumbnails are missing — check the 5000 cap and
  whether the pages are legacy / re-baked.

**The question Antigravity must answer with numbers, not feel:**
is the owner's lag (a) library-ready time, (b) first-frame → first
tiles time, or (c) grid-visible stutter starting at ~10 s? Round 70
fixes (c) and part of (b). It does not fix (a).

## 6. Verification owed by Antigravity before merge

1. `./gradlew testDebugUnitTest assembleDebug assembleBenchmark` —
   expect the existing 233 tests to pass, 0 failures. Record the
   actual output, not "passed".
2. Benchmark APK, real library, cold start, three runs each:
   - `library.json` size, document/page counts, first-frame and
     library-ready times, thumbnail vs page decode counts.
   - Dashboard: time from dashboard appearance to all first-screen
     tiles visible, with and without this branch, on the same device
     and library. This is the number Round 70 claims to improve.
   - Scroll the first screen immediately on open (before 10 s):
     no OCR or repair pass should start during that scroll.
   - 10-page batch bake with a forced OOM (or a low-memory device):
     the half-dimension retry should appear in logcat, and the page
     should save at lower resolution instead of failing.
   - Preview cache: tap through 5 filters and back — re-taps should
     be instant, and no recycled-bitmap crash should appear.
3. Record all of the above as a reply under H004 in
   `docs/AGENT_HANDOFF.md`, with device, build type, library size,
   and whether the Rust .so was present (expected: absent, toolchain
   still missing).
4. If any check fails, do **not** merge this branch to `main`. Report
   the failure in H004 and let Muse revise it — do not silently fix
   it on `main` and diverge the branches.

## 7. Prompt for the next agent (copy / adapt)

> Pull branch `muse/initial-lag-round70` in Harshit-888/re-cam and read
> `docs/INITIAL_LAG_ROUND70.md` before touching anything. This branch is
> UNBUILT and UNTESTED by Muse (no JDK / Android SDK in its sandbox).
> Run §6 first: existing tests, benchmark APK cold start on the real
> library, dashboard first-tiles time with and without this branch,
> immediate-scroll check, OOM batch check, preview-cache check. Record
> everything as a reply under H004, with device, build type, library
> size, and Rust .so presence. Do not merge to `main` until §6 passes.
> If §6 shows the lag is library-parse time (a), stop and report that
> — Round 70 does not fix (a), and the next round must be the
> lightweight-index work from `LAUNCH_DIAGNOSIS.md` P4, not more
> dashboard tuning. Claim your files in `docs/AGENT_COORDINATION.md`
> §4 before editing, and do not touch `adobe-scan/` without the
> owner's explicit authorisation.

## 8. Arena review (H004) — Round 70.1 response, 2026-10-31

**What Muse actually received:** the owner relayed Arena's summary of
findings and said Arena's detailed static review is in Arena's
working-tree `docs/AGENT_HANDOFF.md` under H004, **not yet shared to
Muse's branch**. Muse has therefore NOT read Arena's full text, only
the four findings as relayed. Arena's full text should be shared
before a final merge decision, so nothing in it is missed. Arena's
contract finding is confirmed from the diff itself: no §3 signature
or return type changed in Round 70, and no Rust, ABI, or filter-source
change — that part is not in dispute.

**Muse's verdict on the four findings: all four accepted, all four
fixed in this commit. None is claimed as device-verified** (see §9).

### 8.1 Preview cache — ACCEPTED, recycling removed

Arena: LRU eviction can recycle a bitmap still owned by
`previewSource` or the Compose pipeline; in particular
Original/full-frame/zero-rotation can cache the source bitmap itself.

Confirmed, and stronger than stated: `PerspectiveWarp.warp` returns
`src` for full-frame corners, `applyEnhancements` returns its input
for ORIGINAL and for zero rotation, and `DocumentFilters.apply`
returns `src` for ORIGINAL/failure — and the §3 table itself says
"compare identity before recycling" for that function. Round 70's
cache stored `shown.asImageBitmap()` whenever `rendered != null`,
with no identity check, so a cached entry could BE `previewSource`.
Evicting it recycled `previewSource` while the borders stage, the
swatches, or a still-drawn `previewBitmap` could still use it, and
while Compose's render thread could still be drawing it.

Fix (Round 70.1): manual recycling is removed entirely — the
eviction function now only returns `size > 4`. The cache also never
stores an alias: it only caches when `rendered != null && rendered
!== source`. Bitmaps in the cache are now GC-owned, matching the
codebase's own rule for `filterThumbs`. Trade: ~24 MB held for 4
entries instead of freed on eviction, vs a recycled-bitmap crash.
That trade is correct.

Unverified on device: no recycled-bitmap crash test, no preview
regression test — see §9.

### 8.2 OOM retry — ACCEPTED, retry removed, user warning added

Arena: a half-dimension bake can be reported as ordinary success
despite reducing output quality; logcat is not a user warning; check
whether reducing `maxDimension` actually reduces the allocation that
failed; preserve cancellation; avoid overwriting a previous good
output silently.

All four sub-points confirmed:

- Quality/overwrite: a downgraded bake in `baked` would be published
  by `updatePages` and would overwrite the previous good file in
  place. The only warning was a logcat line.
- Allocation: reducing `maxDimension` only shrinks the filter pixel
  arrays (`DocumentFilters.rgbaOf` downsamples to it). It does NOT
  shrink the warp, the rotation, the signature/markup composites, or
  the source bitmap (loaded at full capture dimension in
  `PageRenderer`). An OOM in those stages would fail again at the
  same cost. `PageRenderer` had already stepped 3200 → 2400 on OOM
  before throwing, so Round 70's retry was already a fallback failure.
- Cancellation: Round 70's retry caught `Throwable`, which would
  swallow a `CancellationException` from the retry bake and count a
  cancelled page as merely failed. The outer catch order was correct;
  the retry catch was not.

Fix (Round 70.1): no retry, no downgraded bake. An OOM page is
counted as failed, its previous version is kept (failed pages are
not in `baked`, so `updatePages` does not replace them), a
`DocScannerPerf` log line is kept for diagnostics, and the toast
now names memory as the cause and says previous versions were kept
and to try fewer pages at a time. The misleading "sequential OOM
fallback" comment is removed.

Unverified on device: no OOM batch test, no toast screenshot — see §9.

### 8.3 OCR fairness — ACCEPTED, durable attempted flag added

Arena: eight blank or failing pages at the front of the backfill
queue can be selected on every launch, starving later pages.

Confirmed, and Round 70 made it worse: the batch cut from 24 to 8
means eight front blockers can consume the whole batch. The root
cause is that `ocrText.isBlank()` cannot distinguish "never tried"
from "tried and blank/failed" — `OcrEngine.readPage` returns `null`
for both a truly blank page and a failed recognition, and the old
backfill had no other state. It also had a second starvation bug:
`setPageOcrTextsBulk` returned early when the text map was empty,
so a batch that yielded no text advanced the queue by nothing.

Fix (Round 70.1): new `ScannedPage.ocrAttempted: Boolean = false`
(additive field, default, all in-repo callers use named arguments,
`LibraryStore` JSON uses `ignoreUnknownKeys = true`, so old
library.json loads with `false` and an old app reading a new file
ignores the field). Backfill and `indexTextInBackground` now select
only `ocrText.isBlank() && !ocrAttempted`, and both mark every
attempted page in the same single library emission — including
pages that yielded no text. A page is therefore attempted once, then
skipped, so the next launch reaches later pages. Tried pages are
deliberately NOT retried by backfill; a page whose pixels change is
reset to `ocrAttempted = false` on bake in `PageRenderer`, so it
gets another chance on its new image. Pages with non-blank `ocrText`
are still skipped, so no already-recognised page is re-attempted on
upgrade.

Known limitation, stated plainly: a page that failed transiently
(ML Kit model not ready, transient error) and was never edited will
not be retried by backfill. That is the price of not re-trying blank
pages every launch; the alternative is the starvation this fix
removes. A future round could add a dated retry (e.g. retry tried
pages only when the untried queue is empty, once per week), but it
is not in this commit and must not be assumed.

Unverified on device: no two-launch fairness test — see §9.

### 8.4 Launch verification — ACCEPTED, orphan gate raised, claim softened

Arena: the first ten seconds are not proven protected; the orphan
sweep still has a six-second quiet gate. Run the measurement split
before claiming a launch improvement.

Confirmed: `ORPHAN_SWEEP_IDLE_MS` was 6_000, and the sweep is
launched from `ScannerRepository` init (not the dashboard), so it
could run at dashboard-appearance + 6 s, inside the claimed
protected window, on the same IO threads as the tiles. Round 70's
"first 10 seconds protected" claim was therefore overstated, even
though the sweep itself is only a directory listing plus deletes.

Fix (Round 70.1): `ORPHAN_SWEEP_IDLE_MS` 6_000 → 12_000, matching
the thumbnail-repair gate. After this change, the only speculative
work in the first 10 s is the thumbnail-only cover prewarm (140 ms
gate, stored thumbnails only, no full-page decodes) — and that is
now stated as the claim, not "nothing runs". The sweep's 10-minute
grace means delaying it costs nothing.

Claim correction: **Round 70.1 does NOT prove a launch improvement.**
No device, benchmark, or first-tiles measurement has been run by
Muse for Round 70 or Round 70.1. The gates are changed, the
hypothesis is that the dashboard window improves, and §9 is the
measurement that would prove or refute it. Any message that says
Round 70 "protects" or "fixes" the launch without that measurement
is overstating the evidence.

## 9. Verification owed by Antigravity before merge (Round 70.1, supersedes §6)

All of §6, plus the following, all recorded under H004 with device,
build type, library size, and Rust .so presence. **Muse ran no
tests and no device checks for Round 70 or Round 70.1 — no JDK,
Android SDK, device, or benchmark in its sandbox. `git diff --check`
passed on the changed files; that is a whitespace check, not a
build, and must not be reported as verification.**

1. `./gradlew testDebugUnitTest assembleDebug assembleBenchmark` —
   expect the existing 233 tests to pass. Pay attention to any test
   that constructs `ScannedPage` positionally (none found in-repo;
   all in-repo callers use named arguments, but Antigravity's local
   tree may differ).
2. Preview cache: open the editor on an ORIGINAL, full-frame,
   zero-rotation page, tap through 5+ filters and back, and scroll
   pages. No recycled-bitmap crash, no blank preview, no preview
   showing the wrong page. Check logcat for a recycled-bitmap
   exception.
3. OOM batch: force a low-memory batch (or a device that OOMs on a
   10-page SMART batch). The toast must name memory, previous
   versions must be kept (check the page files / viewer after the
   failed batch), and cancelling the batch mid-bake must actually
   cancel (no partial publish, no toast claiming success).
4. OCR fairness: build a library with 8 blank pages at the front
   and 1 text page later. Launch twice. The later page must be
   recognised by the second launch (not starved), and the blank
   front pages must not be re-attempted on the second launch
   (check `ocr-backfill` log lines / library.json `ocrAttempted`).
   Also check a library.json written by an older version loads
   correctly (all `ocrAttempted` defaulting to `false`) and that a
   re-baked blank page is attempted again.
5. Launch split (H002, still owed): benchmark APK, real library,
   cold start — `library.json` size, doc/page counts, first-frame
   vs library-ready times, thumbnail vs page decode counts, and
   timestamps for orphan-sweep, OCR-backfill, and thumbnail-repair
   relative to dashboard appearance. No OCR, repair, or sweep line
   should appear within 10 s of dashboard appearance; only the
   thumbnail prewarm may. If any does, report it — do not merge
   on the basis of the gate constants alone.
6. If any check fails, do NOT merge to `main`. Report the failure
   under H004 and let Muse revise — do not silently fix it on
   `main` and diverge the branches.
