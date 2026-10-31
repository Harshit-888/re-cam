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

## 10. Arena follow-up investigation — Round 70.2 (Muse, 2026-10-31, static only)

**Exact revision investigated:** `muse/initial-lag-round70` at
`5fd010b` (Round 70.1), against Round 70 at `d42d77d` and base
`9950c0a`. **This time Arena's complete original H004 review was
provided by the owner, so the four findings below are checked
against Arena's own text, not a relayed summary.** Arena's text
states it is a static review only, with no build or device test —
and so is this investigation. **Nothing in this section is
device-verified, and nothing in it, on its own, justifies merging.**

### 10.1 Original four findings — are they addressed by 5fd010b?

Yes, as far as static code review can tell, with the exact
mechanisms Arena asked for:

1. **Preview cache (P1, Arena lines: cache 317–318, 344–350,
   534–553, 568–609).** Arena's mechanism is precise: LRU order
   records map accesses, not ownership; an ORIGINAL / full-frame /
   zero-rotation preview wraps `previewSource` itself; inserting a
   fifth entry evicts and recycles it while `previewSource` still
   refers to it; and insertion at line 607 happened before
   `previewBitmap` was replaced at 609, so even the formerly visible
   image could still be in the pipeline. Round 70.1 chose Arena's
   first option — "remove manual eviction recycling" — and also
   added the identity check Arena's mechanism implies: a cached
   entry is now only stored when `rendered !== source`. With no
   recycling, eviction only drops a map reference; a bitmap still
   held by `previewBitmap` stays alive until `previewBitmap` is
   replaced, which is exactly the ownership rule that was missing.
   The remaining device check is still owed (Arena's test:
   ORIGINAL / full-frame / zero-rotation, through at least four
   other filters, back to Original and Borders, watching for
   recycled-bitmap exceptions, blank previews, crashes).

2. **Half-resolution retry (P1, Arena lines: editor 741–765,
   792–797; PageRenderer 40–46, 117–131; ImageProcessingUtils
   28–56).** Arena's two options were "report failure and retain
   the previous output, OR explicitly disclose per-page dimensions
   and get approval". Round 70.1 chose the first: no retry, no
   downgraded bake, previous version kept (failed pages are not in
   the publish map), and a toast that names memory. Cancellation
   is preserved because the retry — whose `catch (Throwable)`
   would have swallowed it — no longer exists; the outer catch
   still rethrows `CancellationException` first. Arena's
   allocation point is also confirmed: `maxDimension` applies to
   the filter, not to the initial full-resolution load or to
   warp/rotation, and ORIGINAL returns before filtering, so
   halving it was never guaranteed to reduce the allocation that
   failed or the saved dimensions. The outer retry was also not a
   sequential fallback, as Arena notes: the other semaphore holder
   could still bake concurrently. What Round 70.1 did NOT fix is
   the *inner* fallback in `PageRenderer` — see §10.2.

3. **OCR starvation (P1/P2, Arena lines: repository 58–64,
   778–785, 803–805; OcrEngine 50–61).** Round 70.1's durable
   `ocrAttempted` flag does break the front-of-queue loop for
   blank pages, and it does so in the way Arena asked for
   ("durable tried/no-text/retry state or fair resumable
   scheduling"). The claim that "all remaining pages are picked up
   on subsequent launches" is now true for *untried* pages, with
   one stated exception: a page that was attempted and then never
   edited is not retried (see §10.3 on transient failures). The
   device test Arena asked for — eight blank/failing pages
   followed by a text page, across restarts — is still owed, and
   is now in §9 as check 4. It has not been run by Muse.

4. **First ten seconds (P2).** Round 70.1 raised the orphan gate
   to 12 s and softened the claim to an unmeasured hypothesis.
   Arena's second point in this finding — that native/benchmark
   work launched after `withFrameNanos` is not proof of presented
   pixels — was NOT fully addressed by Round 70.1, and is
   investigated in §10.5. The before/after launch split is still
   owed and still not run.

**Contract / parity, re-checked against Arena's own wording:**
Arena says the `9950c0a..d42d77d` diff does not rename, change
signatures or return types of listed §3 symbols, and no Rust, ABI,
or filter source is changed, so a Rust pixel-parity run is not
required *solely* by that patch — but that "does not establish
runtime output equivalence", because the lower-resolution retry
could change saved pixels/dimensions. Muse agrees, and notes the
corollary for Round 70.1: removing that retry *removes* the
output-equivalence risk Arena flagged, and Round 70.1's new
`ocrAttempted` field is additive only (no signature or return
type changed, §3 table updated). Round 70.2 below adds no §3
signature change either: the OCR guard adds optional parameters
with defaults to two repository functions, and the atomic-write
change keeps `writeJpeg` / `writePng` signatures unchanged.

### 10.2 Inner OOM fallback — investigated, NOT fixed in this round

`PageRenderer.renderEnhanced` (the fallback Arena's follow-up
asks about) works like this, confirmed in code:

- It starts at `maxDimension` (3200 for High, 2000 for Medium) and,
  on `OutOfMemoryError` from `applyEnhancements`, steps down by
  3/4 to a floor of 2400, then throws. For High that is at most
  one retry (3200 → 2400); for Medium, a first OOM throws
  immediately (2000 is already below the floor). The comment
  "at 2400 px (~225 DPI)" therefore describes only the High path.
- It covers **only** `applyEnhancements` (warp → rotate → filter).
  It does NOT cover: the source load (always at
  `MAX_CAPTURE_DIMENSION` = 3200, ~30 MB for an ARGB page, before
  any fallback can run); the signature overlay (a new full-size
  bitmap plus a 1200 px signature decode); the markup copy (a full
  copy if the result is immutable); or the save/encode stage.
  An OOM in any of those stages propagates out of `bakeBlocking`
  with no fallback at all.
- For ORIGINAL, and for any OOM in warp or rotation, the retry
  cannot help even inside `applyEnhancements`: warp output size
  is set by the quad, rotation output size is the source size —
  neither scales with `maxDimension`. Only the filter pixel arrays
  scale, via `rgbaOf` downsampling to `maxDimension`. So a warp /
  rotation OOM is retried at the same allocation and fails again,
  after doing the warp work twice.
- On a failed first attempt, intermediate bitmaps are not
  recycled before the retry: if warp succeeded and rotation OOMs,
  the warped bitmap is a local in `applyEnhancements` and is only
  freed by the GC, so the retry starts under *more* memory
  pressure, not less.

**Why this is not fixed here:** fixing it properly means
downsampling the source *before* warp/rotation (so those stages
scale with the fallback dimension), or adding a separate fallback
for signature/markup/save. Either change alters saved pixels and
dimensions for every bake that hits the fallback, which is a
quality / parity decision that Arena and Antigravity must agree
on, with a device test at decode, warp, filter, and output stages
(including ORIGINAL) — exactly the test Arena specified. A
static, untested change to that pipeline in this round would
repeat the mistake Round 70's outer retry made. **Recommended
next step, not implemented:** a Round 71 proposal (Arena +
Antigravity) for a dimension-aware warp/rotate path, with
per-stage OOM tests and a disclosed fallback dimension in the
user-visible failure message, rather than a silent downgrade.

**What Round 70.2 does fix in the same area:** the *save* stage
can no longer corrupt the previous output on failure — see §10.4.
That removes one OOM-adjacent data-loss path without changing
any bake pixels.

### 10.3 OCR revision races and transient-failure retry — investigated, race fixed, retry design recorded (not implemented)

**Revision race — confirmed, and fixed in Round 70.2.**

`backfillTextInBackground` and `indexTextInBackground` both
snapshot pages at call time, then spend seconds (ML Kit +
2400 px decode per page, with idle pauses) before applying
results by page ID only, in `setPageOcrTexts` /
`setPageOcrTextsBulk`. Nothing in those functions checks whether
the page still is the image that was recognised. Concretely:

- A page re-baked during backfill gets a new record with
  `revision + 1` and `ocrAttempted = false` (Round 70.1's reset).
  The in-flight backfill then applies the *old* image's text and
  sets `ocrAttempted = true` on the *new* record, because it
  matches by ID. The new image is then never recognised, and
  search shows the old image's text for new pixels. The same
  applies to `indexTextInBackground` for a page edited while its
  new-document indexing is running.
- The same ID-only apply also ignores a move to trash during the
  pass (the snapshot filtered trash, the apply does not), so a
  trashed page can still be indexed after it was trashed.
- Concurrent backfill + index for the same document can both
  attempt the same untried pages (duplicate ML Kit work) and
  apply in either order; with the same image that is harmless
  (same text), with different images it is the race above.

Fix in Round 70.2 (no model change): both apply functions now
take an optional expected map, `pageId -> (imageUri, revision)`,
built from the snapshot. A page whose current `imageUri` or
`revision` differs is skipped entirely — neither text nor
attempted flag is applied — so the new image remains untried
and is picked up on the next launch. The single-page helper
keeps the old no-guard behaviour via its default (it has no
caller in main code, and no snapshot to guard against).

**Transient-failure retry — confirmed as an unresolved gap,
design recorded, not implemented in this round.**

`OcrEngine.readPage` returns `null` for three indistinguishable
cases: the page truly has no text, the image cannot be decoded,
and recognition threw (including Play Services / model-not-ready
failures). The docstring says so explicitly: "or `null` when the
page has no text, the image cannot be decoded, or recognition
fails. Never throws." Round 70.1 marks *all* of those as
attempted, so a transient failure — model not yet downloaded on
first launch, a file briefly unavailable because a concurrent
bake was writing it (non-atomic, before §10.4), a transient
Play Services error — is permanently treated as "tried and
blank" for an unedited page. That is the exact opposite of the
fairness fix for blank pages, and it cannot be solved by
re-trying tried pages blindly: re-trying every tried page every
launch reintroduces the original starvation, just for a
different reason.

Recommended design (for Arena + Antigravity to agree on, not
implemented here because it changes the OCR API and, if a
counter is used, the page model again):

1. `OcrEngine` returns a result type, not just text-or-null:
   `Success(text)`, `NoText` (decoded and recognised, empty),
   `Failure(cause)` (decode failed or recognition threw).
   `readPage` is kept as a compatibility wrapper for the
   benchmark runner.
2. Backfill marks attempted only for `Success` and `NoText`.
   `Failure` pages are NOT marked attempted, but are retried
   only after all untried pages in that launch have been taken
   — i.e. untried-first scheduling — and with a bounded attempt
   count (e.g. a per-page `ocrAttempts` counter, max 3, after
   which the page is marked attempted-final). Without the cap,
   a persistently corrupt file starves the queue forever; with
   the cap, a transient model failure gets up to 3 launches to
   succeed.
3. A page that is re-baked already resets its state (Round 70.1),
   so a transient failure on an old image does not need a
   separate retry path once the image changes.

Until that design is implemented and tested, the honest statement
is: **Round 70.1 + 70.2 guarantee that untried pages are reached,
but do NOT guarantee that a transiently failed page is ever
retried.** That limitation must be in the merge decision, not
buried.

**Persistence note, checked:** an attempted flag is only durable
once the repository's debounced `library.json` write runs
(150 ms debounce, plus the final flush on close). If the app is
killed mid-backfill, before the bulk emission, nothing is marked
— which is safe (the pages are retried next launch, not
starved). If it is killed after the bulk emission but before
the write, the same applies. The dangerous window is the
opposite: a flag written, then a later kill, does not lose the
flag. No additional persistence work is needed for fairness.

### 10.4 Non-atomic edited-page writes — confirmed, fixed in Round 70.2

`ImageStore.writeJpeg` and `writePng` wrote directly to the
target with `FileOutputStream(target)`, and ignored
`bitmap.compress`'s Boolean return. That is confirmed, and it
contradicts the codebase's own atomicity rule, which both
`LibraryStore` (temp + rename, with a writeLock, and an explicit
"do not fall back to truncating the live file" comment) and
`ThumbnailStore` (temp + rename) follow.

Why it matters for edited pages specifically, and not just for
imports: `saveEditedPage` writes to `_edited.jpg` / `_edited.png`,
the same path the repository already points at for a previously
baked page. A kill, crash, OOM, or failed compress mid-write
therefore truncated the *previous good output*, even though
`saveEditedPage` returned `null` and the caller (correctly, after
Round 70.1) treated the page as "failed, previous kept". The
previous file was not kept; it was already truncated, and the
viewer would then try to decode a partial file. For imports the
risk is lower (a new UUID page file, not yet referenced, becomes
an orphan and is swept), but the same write functions are used,
so the fix covers both.

Fix in Round 70.2 (signatures of `writeJpeg` / `writePng`
unchanged): a shared `writeAtomically` helper writes to a
uniquely-named temp file in the same directory (UUID suffix, so
two concurrent bakes for the same page cannot interleave in a
fixed `.tmp` file — the mixture bug `LibraryStore`'s lock
prevents for `library.json`), checks `compress`'s return value
and throws on `false`, deletes the temp on any failure, and
only then renames to the target. On rename failure the temp is
deleted and the previous file is untouched. The mtime bump
(`nextStamp`) now happens after the rename, so thumbnail cache
keys still advance exactly once per successful bake.

Unverified on device: no kill-mid-write test, no compress-failure
test — see §11.

### 10.5 Native / benchmark scheduling — investigated, not fixed in this round

Arena's point is confirmed: `withFrameNanos { }` in
`MainActivity`'s first `LaunchedEffect` waits for the next frame
callback, not for pixels to be presented, and the frame it waits
for is the *splash* frame (the repository is still `null` at
that point; the dashboard has not composed yet). Round 70 moved
the native log and the benchmark trigger from `onCreate` to
after that callback, which removes them from the process-start
and splash-composition window, but it does **not** move them
after the dashboard is visible, and it does not isolate them
from the work they compete with:

- `NativeFilters.getStatusSummary()` triggers `System.loadLibrary`
  on IO, on every launch, even if the user never uses a filter,
  while the library is still parsing and tiles are about to load.
  The cost is small (typically a few ms, more on a low-end
  device), but it is not zero and it is not gated on filter use.
- `PerfAuditRunner.runAudit` (triggered by the `run_benchmarks`
  intent extra) runs a full synthetic benchmark — 3200 px bitmap,
  all filters, batch, preview, PDF, OCR — on Default/IO while
  the library parse, tile decodes, OCR backfill, and repair are
  also running. Its numbers are therefore polluted by dashboard
  work, and it pollutes any launch measurement taken in the same
  run. It is also triggered by an intent extra on an
  `exported = true` launcher activity, so any other app could
  launch it and start heavy CPU/ML work without user consent.
  Gating it on "debuggable" would break Antigravity's benchmark
  workflow (the benchmark build is non-debuggable by design),
  and `buildConfig = false` in `app/build.gradle.kts`, so there
  is no `BuildConfig.DEBUG` to gate on — that is why no gating
  change is made in this round.

Recommended next step (not implemented): move the benchmark
trigger out of the normal launch path entirely — e.g. a
separate benchmark entry point, or a trigger that only runs
after the repository is ready *and* the dashboard has been idle
for a full quiet window, with a clear log line that benchmark
numbers were taken with dashboard work quiesced — and gate it
on something stronger than a guessable intent extra. Until then,
benchmark numbers from a `run_benchmarks` launch must be
reported with the caveat that dashboard work was concurrent.

## 11. Verification owed by Antigravity before merge (Round 70.2, supersedes §9)

All of §9, plus the following, all recorded under H004 with
device, build type, library size, and Rust .so presence.
**Muse ran no tests and no device checks for Round 70, 70.1, or
70.2 — no JDK, Android SDK, device, or benchmark in its
sandbox. `git diff --check` passed; that is not a build and not
a test. Do NOT merge solely on this static review, or on any
previous static review in this document.**

1. `./gradlew testDebugUnitTest assembleDebug assembleBenchmark` —
   expect the existing 233 tests to pass, plus any new tests for
   the atomic write and the OCR guard if Antigravity adds them
   (Muse added none, because it cannot run them).
2. Atomic write: kill the app mid-bake (or force a compress
   failure), then reopen the page. The previous edited output
   must be intact and viewable, not truncated, and the library
   must still point at a decodable file. Repeat for a JPEG bake
   and a PNG (Clear B&W / Grayscale) bake.
3. OCR race: start a backfill on a library with untried pages,
   re-bake one of those pages mid-pass, then check that the
   re-baked page is NOT stamped with the old image's text and
   is NOT marked attempted for its new image, and that it is
   recognised on a later launch.
4. OCR transient failure: simulate a recognition failure on
   first launch (e.g. model unavailable), then a successful
   second launch. Record whether the page is ever recognised
   under the current code (expected, per §10.3: NO, unless the
   page is edited) — and do not merge on the assumption that it
   will be, until the §10.3 design is implemented.
5. Inner OOM: force OOM at decode, warp, filter, and output
   stages, including ORIGINAL, on a low-memory device or with a
   large page. Record the saved dimensions (or failure) at each
   stage, and whether the previous output is preserved. Do not
   accept a silent downgrade.
6. Native/benchmark: launch with `run_benchmarks` from adb and
   from another app (if possible), and record whether the
   benchmark runs, what dashboard work was concurrent, and
   whether the first-frame and library-ready log lines are
   affected. If the benchmark is triggerable by another app,
   record that as a security finding for the next round.
7. Launch split (H002, still owed): benchmark APK, real and
   empty libraries, cold start, with pass timestamps relative to
   dashboard appearance. No OCR, repair, or sweep line within
   10 s of dashboard appearance (only thumbnail prewarm may).
8. If any check fails, do NOT merge to `main`. Report the
   failure under H004 and let Muse revise — do not silently fix
   it on `main` and diverge the branches.
