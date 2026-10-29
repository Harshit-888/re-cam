# Round 65 — a full read of the app: launch, jank, bugs, annotate

*"rescan the whole doc scanner application find bugs and fix them accordingly, also fix
the major bottleneck of the application which is the initial lag the application lags for
about seconds to get smooth … also optimize the whole app inspect the code deeply and fix
the bugs, also the annotate m3 buttons are still glitching and disappearing."*

This round is an **audit first, then fixes**. Nothing here was compiled or run — there is
no Android SDK, no NDK and no Rust toolchain in the sandbox — so every claim below is
either a code fact (a call site, a measured unit test, a documented platform behaviour) or
explicitly marked as needing a device. Where a claim could not be proved, it says so.

---

## 1. What the launch actually does, in order

| # | Step | Where | Cost | Verdict |
|---|---|---|---|---|
| 1 | Process fork → Application → Activity | platform | Compose runtime, CameraX, ML Kit's `MlKitInitProvider`, TFLite: class loading + native library mapping | **outside our code.** This is the floor of a cold start, and it is where a "takes seconds" report is usually spent |
| 2 | Window background drawn | `res/drawable/launch_background` | one gradient | already the brand gradient, so there is no white flash |
| 3 | `MainActivity.onCreate` → `FramePacing.apply`, `PerfTrace.install` | `MainActivity` | a display-mode query; a frame-metrics listener in debug only | fine |
| 4 | First Compose frame (`LaunchSplash`) | `ui/common/LaunchSplash.kt` | one gradient, one PNG, a text column | fine |
| 5 | `ScannerRepository` constructed on IO | `data/LibraryViewModel` | reads and parses `library.json`, then `settings.json` | already off the main thread and traced |
| 6 | **Splash held for a minimum of 320 ms** | `data/LibraryViewModel` | **+320 ms on every launch, unconditionally** | **removed** — see §2.1 |
| 7 | Crossfade splash → dashboard | `MainActivity` | 300 ms of the *content* being composed underneath | intentional; this is the animation the old floor was paying for |
| 8 | First dashboard composition | `ui/dashboard/DashboardScreen` | header, folder pills, one screen of cards, date labels for **every** document | date labels now memoised per document — see §2.6 |
| 9 | Cover prewarm, ~140 ms later | `DashboardScreen` | up to 12 decodes | **could decode 12 full pages**; now thumbnails only — see §2.4 |
| 10 | Thumbnail repair (4 s idle), OCR backfill (2.5 s idle), orphan sweep (6 s idle) | `StartupWork` | heavy, but gated on the app being still | correct already |

### 1.1 The number this round exists to produce

The three "it lags" causes in step 1, 5/6 and 8–9 are indistinguishable by feel. Two log
lines now separate them, both on the `DocScannerPerf` tag (debug builds):

```
launch: first frame drawn 412 ms after process start
launch: library ready 96 ms after the ViewModel was created, 508 ms after process start
library.json read: 14 doc(s), 63 page(s) in 71 ms
launch: library ready … (ScannerRepository's own line is unchanged)
```

`Process.getStartUptimeMillis()` and `SystemClock.uptimeMillis()` share a clock, so
`first frame − process start` is *everything*, including what the system did before us.

```
adb logcat -s DocScannerPerf
```

If `first frame` is large and `library ready − first frame` is small, the app's own start-up
is not the problem and no amount of Kotlin will fix it — the answer is a baseline profile
(see §5). If it is the other way round, the library read is the problem and the fix is in
`ScannerRepository`.

---

## 2. Found and fixed

### 2.1 A 320 ms floor on every launch — `data/LibraryViewModel`

`SPLASH_MIN_MS = 320` held the launch screen for at least a third of a second even when the
library was ready in 20 ms, "so a warm start shows a logo rather than a one-frame flash".

The flash it was preventing cannot happen any more: `MainActivity` crossfades the splash
*out* over the content with a 300 ms `tween`, so a warm start is a crossfade, not a cut.
The floor was therefore purely 320 ms of added latency, on every launch, for a visual
problem that was already solved one file away.

**Fixed:** the delay is gone; the library is published the moment it exists.

### 2.2 Closing the app blocked the main thread and rewrote everything — `data/ScannerRepository`

`close()` ran from `LibraryViewModel.onCleared` — the UI thread of a finishing Activity —
and called

```kotlin
runBlocking {
    store.writeNowBlocking(wholeLibrarySnapshot)   // json.encodeToString of every page + OCR text
    settingsStore.writeNowBlocking(settings)
}
```

Two defects in four lines:

* **Blocking the main thread** to serialise megabytes of JSON while the window is closing.
  On a scanned library that is hundreds of milliseconds of string building on the one
  thread that is trying to finish an animation.
* **Writing unconditionally.** Every mutation is already persisted through a 150 ms
  debounce, so the file is almost always up to date; the whole encode-and-write was waste.

**Fixed:** the flush is skipped entirely when nothing is dirty (`LibraryStore.isDirty` /
`SettingsStore.isDirty`), and when something *is* dirty it runs on IO in a scope of its own.
The trade is written down in the code: an edit made in the last 150 ms of a session that is
*killed* rather than finished can be lost.

### 2.3 Identical snapshots were re-serialised — `data/LibraryStore`, `data/SettingsStore`

`combine(documents, folders)` emits once immediately, and every emission produced a fresh
`LibrarySnapshot` that the store encoded in full — the entire library, including every
page's recognised text. The repository already special-cased the *first* emission; every
other no-op emission (a settings-like change, two mutations that landed on equal content)
still paid for a full encode and a full file write.

**Fixed:** both stores now compare the **document and folder list instances** (O(1),
identity — a list whose contents changed is always a new instance) and skip anything
identical to what is on disk. `load()` registers the lists it just read, so a "flush on the
way out" of an unchanged library is free.

### 2.4 The launch-window cover prewarm could decode twelve full pages — `util/PageImageLoader`

The prewarm's contract is "the first fling finds pixels". It called `load()`, and `load()`'s
slow path — taken whenever a page has **no stored thumbnail** — decodes the page in full.
For a *Clear B&W* or *Grayscale* page that is a **3200 px PNG**: ~30 MB of transient
allocation and tens of milliseconds of CPU, per cover, twelve times, in the first seconds
after launch, on the same two decode slots the visible tiles need. Pages written before
`ThumbnailStore` existed — or whose thumbnail was swept — take that path.

**Fixed:** `load()` gained `allowPageFallback`; `prefetch()` passes `false`, so speculative
work only ever reads a thumbnail. A page without one is the thumbnail-repair pass's job
(idle-gated, chunked). The visible tile still decodes in full when it appears, because a
tile has to show *something*.

**Also fixed:** the prewarm now waits for `PREWARM_QUIET_MS = 140 ms` of stillness instead
of `awaitQuiet(0, …)`, which was not a gate at all. It used to start decoding during the
launch crossfade and the grid's first layout — precisely the two frames that decide whether
the app feels quick to open.

### 2.5 The annotate M3 buttons — `ui/editor/MarkupScreen`

This is the "annotate m3 buttons are still glitching and disappearing" report, and the
cause is in a **default argument**, which is why five rounds of fixing the surrounding
layout never touched it.

`SegmentedButton`'s signature ends in

```kotlin
icon: @Composable () -> Unit = { SegmentedButtonDefaults.Icon(selected) },
```

— the **animated tick** that M3 grows into a cell when it becomes the selected one. This
row never passed an `icon`, so every cell had one, and it is an `AnimatedVisibility`: on
selection the tick is composed *inside the cell* and grows from scale 0 while occupying its
full measured width, and on deselection it is removed again once the fade finishes. So the
content width of the cell that is being selected or deselected changes over ~150 ms, and
every frame of that is a layout pass for the row:

1. the label — `maxLines = 1` with the **default** `TextOverflow.Clip` — loses its tail to
   the tick for the duration of the animation and gets it back afterwards ("the text
   glitches"); on a 360 dp phone, three cells wide with a tick in one of them, the label is
   the part that is dropped ("one of them disappears");
2. the neighbours move, because a connected row is laid out from the children's widths —
   and a row whose geometry depends on which cell is selected also re-measures from the
   *default* `defaultMinSize(minWidth = 58.dp)` whenever a label is longer than a third of
   the row.

A connected segmented row already says which cell is chosen by *filling* it. The glyph was a
second, worse indicator, drawn inside the same space as the label.

**Fixed:** `icon = {}` on all six cells (tools and weights) — no tick, so no per-selection
width animation at all — plus `overflow = TextOverflow.Ellipsis` so a long word shortens
instead of being clipped, and `Modifier.weight(1f)` so the three cells are equal shares of
the row and that stays true at every font scale, whatever M3's own default weighting does in
a future version.

**Also fixed (same bar):** the annotate bar is one Column of 230 dp — two segmented rows, a
swatch row and the save button — which is **more than 340 dp at a 2.0× font scale**. A
`Scaffold` measures its bottom bar against the window, so in landscape or with large text
the overflow is drawn *outside* the window: the save button, then the swatch row, are simply
not on screen and there is nothing to scroll to reach them. The bar is now capped at 62 % of
the window height and scrolls when it needs to.

### 2.6 Date labels were reformatted for every document on every change — `ui/dashboard`

`documents` is re-emitted by every bake — a rotate, an annotate, a signature, each
recognised page — and the labels map was rebuilt from scratch each time: two
`SimpleDateFormat.format` calls per document, on the main thread, to produce labels that had
not changed. A 200-document library paid 400 formats for a one-page edit.

**Fixed:** a per-document memo keyed by `(id, modifiedDate)`. A change now formats only the
documents it touched. The formatters are still created per pass rather than held — they are
not thread-safe, and two per rebuild is cheaper than tracking that they are only ever used
from one thread.

### 2.7 A crop drag could crash on a degenerate quad — `util/CropHandles`

The four edge drags clamped a delta between bounds derived from the quad:

```kotlin
val maxDy = minOf(bottomLeftY - MIN_GAP - topLeftY, …)   // can be < minDy
val effectiveDy = dy.coerceIn(minDy, maxDy)              // throws when minDy > maxDy
```

`coerceIn` throws `IllegalArgumentException` when `low > high`. A quad that is **unsound** —
one stored by an older revision, one detected on a page so skewed two corners ended up
within `MIN_GAP`, one collapsed by an earlier bug — makes those bounds cross, and the
exception is thrown **inside a drag gesture**: the app dies with a finger on the crop handle.
The file's own `clamp()` helper exists for exactly this ("`coerceIn` throws when `low >
high`; order the bounds first") and the corner paths used it; the edge paths did not.

**Fixed:** all eight clamps go through `clamp`, and two unit tests pin it
(`a collapsed quad cannot throw out of an edge drag`, `an inverted quad cannot throw …`).

### 2.9 Two writers could interleave inside `library.json.tmp` — `data/LibraryStore`

Found while making 2.2 safe, and a bug in its own right. Both stores write to a fixed temp
path and then rename it over the target. Two writers can reach that path at once: the
debounce collector (150 ms) and — now that `close()` flushes — the final flush. A
cancelled coroutine does not stop a blocking write that is already inside
`temp.writeText(...)`, so cancelling the repository scope does nothing to prevent the
overlap. Two `writeText` calls on one file interleave with separate file descriptors and
the rename then publishes the mixture: a truncated `library.json`, produced by closing the
app.

**Fixed:** `writeNow` is wrapped in a monitor (`private val writeLock = Any()`), so the
second writer waits for the first to finish and both publish a complete file. It is held
only around a small file write, and never on the main thread — `close()` no longer writes
there at all.

### 2.8 A failed bake looked like lost ink — `ui/editor/MarkupScreen`

`save()` stored the strokes as data and then re-baked the page, and if the bake returned
`null` it closed the screen silently. The ink is not lost — it is on the page and replays on
the next bake — but nothing said so, so it reads as "my annotation vanished". It now says
what happened.

---

## 3. Checked and found sound

Worth recording, so the next round does not "fix" these again:

| Area | Why it is fine |
|---|---|
| Every `PageRenderer.bake` call site | `bake` is a `suspend` function that wraps `withContext(Dispatchers.IO)`. Rotate, annotate, sign, filter-all and the PDF path are all off the main thread already |
| PDF export | `withContext(Dispatchers.IO)` around generation; the button state is driven from the main thread only |
| `MarkupSession` | `@Stable`, backed by `mutableStateOf`; the shelf and the canvas observe the same object, no copied state |
| `ConnectedButtonGroup` | no ripple on the label, no whole-cell alpha when disabled, press layer's ink captured at gesture start, one `DropdownMenu` for the group. The four historical "buttons vanish" mechanisms are gone |
| `ExpressiveActionBar` | arrival is a translation, never an alpha — a persistent bar cannot be hidden by its own entrance |
| `AnnotateShelf` | owns no alpha; the drag is translation only |
| `ZoomablePageView` | pan clamp bounds cannot invert (`overflow = max(0, scale−1)`) |
| Theme | colour scheme is `remember`ed on exactly what changes it; a settings change does not re-read every colour in the app |
| Memory caches | `LruCache` sized from `maxMemory/8`, `RGB_565` for decoration tiles, one 768 px JPEG per page written at save time |
| `LibraryStore` writes | temp file + rename, never delete-then-write; a kill mid-write cannot truncate the library. (Two *concurrent* writers did, now serialised — §2.9) |
| ML Kit / TFLite | model loads lazily (`by lazy`), the `.tflite` asset is `noCompress` so `openFd` works |
| Search | index built off-composition, ranking debounced and on `Dispatchers.Default`, viewport-only selection pruning |

---

## 4. Known, measured, and *not* fixed

Honest list — each of these is a real cost that this round did not address:

1. **Cold start is dominated by class loading** of Compose + CameraX + ML Kit + TFLite.
   Nothing in this repository can fix that; the answer is a **baseline profile** (§5).
2. **`ThumbnailStore.read` decodes a JPEG twice** (`inJustDecodeBounds` then the real
   decode) to learn its size. It is on the decode threads and a thumbnail is ~40 KB, so it
   is a few milliseconds; removing it means keeping the thumbnail's dimensions in the
   library file.
3. **A full library write is a full serialisation.** The debounce collapses bursts, but a
   rotate on a 1000-page library still re-encodes every page's metadata. Incremental
   persistence (Room, or per-document files) is the real fix and is a design change.
4. **`LibraryViewModel` still builds the repository before anything is drawn.** Showing the
   dashboard skeleton first would need the document list, i.e. the parse — the splash is the
   honest answer.
5. **Debug builds are not the app.** `assembleDebug` runs ART in interpreter/JIT mode with
   Compose's debug hooks live. `./gradlew assembleBenchmark` produces the same code
   non-debuggable; judge frame rate there.

---

## 5. The next real win: a baseline profile

If the log lines in §1.1 show a large `first frame − process start`, the fix is not more
Kotlin. It is `androidx.profileinstaller` plus a baseline profile for the launch classes —
Compose's runtime, `NavDisplay`, the dashboard's list path, `BitmapFactory`, CameraX's
`ProcessCameraProvider` — so the first launch after an install runs AOT-compiled instead of
interpreted. That is typically 20–40 % off cold start on a mid-range device.

It needs one device to generate (`./gradlew generateBaselineProfile`), a `benchmark` build
type (this repo already has one) and a new dependency; it was deliberately **not** attempted
blind, in a sandbox that cannot resolve `dl.google.com` to verify a version.

---

## 6. What to measure next

```
adb logcat -s DocScannerPerf            # the launch lines, plus per-scroll frame stats
adb shell dumpsys gfxinfo com.example.docscanner reset
# …scroll the library for ten seconds…
adb shell dumpsys gfxinfo com.example.docscanner
```

The second pair gives the 90th/95th/99th percentile frame time for the library, which is the
only number that answers "is it smooth now".
