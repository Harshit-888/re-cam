# Antigravity action list — after Arena's review of `874fcb0`

> **Written 2026-11-07 by Arena.ai against `arena/02d1301d-re-cam` @ `c458e4e`
> (`874fcb0` + one docs commit).**
> Companion to `docs/REVIEW_874FCB0.md`. Arena cannot build, run tests, or touch a
> device: everything marked **Arena fixed** is applied in commit `<sha>` on
> `arena/02d1301d-re-cam` but **unbuilt**; everything marked **Antigravity only**
> needs hardware, a JDK/SDK or a signed artefact.

---

## 1. Arena fixed in this round (code + docs, unbuilt)

| # | Fix | File | Why it matters |
| :-- | :--- | :--- | :--- |
| A1 | Replaced the tautological "process kill" test with two real ones: **mid-stream** write failure through `writeNewBackup`, and a **death-before-verification** test that asserts the writer only ever touches an id it created | `app/src/test/.../data/NewBackupWriterTest.kt` | The old test never called `writeNewBackup`; it asserted a property that was true by construction |
| A2 | `failWrite` (0 bytes) split from the new `failMidStream` (bytes, then throw) | same | "Write failure mid-stream" was previously unexercised — the reply claimed it |
| A3 | Fake destination now mints **UUID ids** and records names; concurrency test asserts UUID ids, distinct ids and the caller's names | same | The reply claimed "4 distinct UUID-named files"; the fake used a sequential counter, so the test could not have shown that |
| A4 | Added a **boundary test** that documents and pins the one case the writer *cannot* refuse: an id created after its listing (recorded, not claimed as closed) | same | Converts an undocumented edge into a regression guard |
| A5 | Moved `BenchmarkAuditActivity` to the **benchmark source set** (`app/src/benchmark/java/...`, package unchanged) | new path | It was declared only in the benchmark manifest but compiled into debug **and** release. Debug and release now contain no benchmark class at all |
| A6 | Corrected handoff entries: tested-SHA attribution, three Tier-1 citations, and the 60 ms claim | `docs/AGENT_HANDOFF.md`, `docs/AGENTS_CHANGELOG.md` | See §2 below |
| A7 | This action list | `docs/ANTIGRAVITY_ACTIONS.md` (new) | The remaining work has an owner and a shape |

**Not changed, deliberately:** `NewBackupWriter.kt` and
`DocumentsTreeBackupDestination.kt`. `reviewed` above and found sound — the
adapter resolves every write/read/delete through its own `created[id]` map, so it
structurally cannot open a pre-existing document. Changing working provider code
on a branch Arena cannot build would be worse than leaving it.

**Also not changed:** the pinned SHA-256 constant. It was already correct —
Arena recomputed it: `sha256("existing backup payload with valuable user scans")`
= `b43030fe…594c`. ✓

---

## 2. Corrections applied to the record

1. **Tested-SHA attribution.** The reply said "tested revision `8f4c740` with the
   verified SHA constant pinned in `NewBackupWriterTest.kt`". `grep b43030fe` at
   `8f4c740` returns **0**; the constant entered in `874fcb0`. Recorded as: tests
   and builds at `8f4c740`, assertion added and re-run at `874fcb0`.
2. **Tier-1 citations.** C1 `DashboardScreen.kt:338–344` → **`:316`**
   (`DocumentSearch.Index(documents.filterNot { it.isTrash })`). C3
   `MarkupStudio.kt:142–158` → **`util/MarkupRotation.kt`**. C11
   `SignaturePadDialog.kt:88–95` → **`:251`** (`drawCircle(radius = strokeWidth / 2f, center = points[0])`).
   C4's `ImageStore.kt:180–192` → **`:220–225`** (the copy; `180–192` is import code).
   C2, C5, C7, C8, C10 were correct and are unchanged.
3. **The 60 ms "RenderThread/SurfaceFlinger composite window"** is withdrawn: it
   subtracts two durations with different anchors (process start vs activity
   launch), and the reply's own wall-clock lines place the 434 ms Compose callback
   **4 ms after** `Displayed`. The two numbers are now recorded with their anchors
   named and no delta claim.

---

## 3. Antigravity only — cannot be done in Arena's sandbox

Each item states the exact thing to run and the exact evidence to record. If an
item cannot be run, record it as **unavailable** and say why — that is accepted,
silence is not.

### B1 — Build the code Arena just changed (blocking, 5 minutes)

```
./gradlew testDebugUnitTest assembleDebug
./gradlew assembleBenchmark
```

Record: exact command, task outcome, test count, and the three APK paths.

Then confirm the source-set move did not break the benchmark build:

```
unzip -l app/build/outputs/apk/debug/app-debug.apk     | grep -i BenchmarkAudit   # expect: no match
unzip -l app/build/outputs/apk/release/app-release.apk| grep -i BenchmarkAudit   # expect: no match
unzip -l app/build/outputs/apk/benchmark/app-benchmark.apk | grep -c 'classes.*dex'  # sanity
aapt dump xmltree app/build/outputs/apk/benchmark/app-benchmark.apk AndroidManifest.xml | grep -A4 BenchmarkAuditActivity
```

The benchmark entry point must still work after the move:

```
adb install -r app/build/outputs/apk/benchmark/app-benchmark.apk
adb shell am start -n com.example.docscanner/.util.BenchmarkAuditActivity
adb logcat -s DocScannerPerf | grep -E '§6 BENCHMARK AUDIT|BenchmarkAuditActivity'
```

*(This is the one risk in A5: if the merged benchmark manifest ever loses the
`android:name`, the activity silently stops being launchable. The `aapt` line
above is what catches it.)*

### B2 — C10: one real SAF run (blocking for the gate)

Nine JVM tests now describe the writer's contract. **None of them touches
`ContentResolver`**, so none can prove SAF behaviour. One instrumented test, run
once on the Realme, closes it:

- `androidTest` with `androidx.test:core` (already declared, no `androidTest`
  source set exists yet — this is its first use).
- Create a disposable tree via `DocumentsContract` on **local storage** and, if
  available, **one cloud provider** (Drive/OneDrive). Never the user's real
  backup folder.
- Fault injection at each of: `openOutputStream` throw, throw after N bytes,
  `openInputStream` throw, `deleteDocument` returning false, and a real
  `adb shell am force-stop com.example.docscanner` (or `kill -9` on the app pid)
  while bytes are in flight.
- After each: **SHA-256 of the pre-existing backup before and after**, plus the
  listing of the tree (orphan present? named `docscanner-backup-*.zip`?).
- Then `BackupArchive.restore` of the successfully written archive and confirm
  the page count.

Record per case: provider, commands, before/after hashes, and the orphan's name.
The claim to verify is narrow and worth proving: **a failure or a kill can leave
a new orphan, and can never truncate an existing backup.**

### B3 — Launch evidence (blocking for #4, not for the code)

The 2-doc/2-page run cannot close the split. Three cold starts on the benchmark
APK, `am force-stop` before each:

| Fixture | `library.json` SHA-256 | docs/pages |
| :--- | :--- | :--- |
| real 22/34 | record | 18 active / 28 pages (+4 trash docs / 6 pages) |
| empty | record | 0 / 0 |
| one large (≥200 pages) | record | record |

For each, paste the **raw** `DocScannerPerf` lines: `library.json read … ms`,
`launch: library ready … ms`, `launch: first frame drawn … ms`, and the first
`cover-prewarm` / `ocr-backfill` / `orphan-sweep` / `thumbnail-repair` lines with
their timestamps. State the anchor of each number (process start / activity
launch / frame callback). Add a **dashboard-presented** marker if one exists; if
it does not, say so — that is the missing measurement, not a failure.

### B4 — Tier-1 scenarios as actual runs (blocking for #5)

Per finding: fixture, injection, observed output, timestamped artefact. The four
corrected paths are in §2. The ones still without any run:

- **C2** force `OutOfMemoryError` at decode / warp / filter / output (or a
  deliberately huge page) — record saved dimensions (expected: none) and that the
  previous bake is intact.
- **C5** tap **Auto detect** while a bake is starting — record that no late
  detection overwrites the crop.
- **C7** multi-select 4 documents and inject one render failure — record that no
  chooser opens and the temp files are deleted.
- **C8** set a password with a space and with a non-ASCII character — record the
  supporting text and that export stays blocked; then a valid password → export,
  and open the PDF once to confirm it unlocks.
- **C11** single tap only, save, open the page — record the dot is visible with a
  non-zero radius.
- **C1/C3/C4** device checks: a trashed document's text must not appear in search;
  annotate → rotate 90/180/270 → save → strokes stay on the same ink; copy an
  edited page and compare displayed pixels of source and copy.

### B5 — Housekeeping

- `scripts/verify_workspace.ps1` is still cited and still absent from the repo —
  commit it, or delete the three claims that reference it.
- `BenchmarkAuditActivity`'s class doc still says the boundary "requires a
  separate-app device test". After B1's `aapt` output, either run the
  unprivileged-start denial or reword to point at the source-set split, which is
  the stronger guarantee.

---

## 4. What is still open after this round

| Gate item | Owner | Status |
| :--- | :--- | :--- |
| Backup writer design (no overwrite, readback verify, collision refusal) | Arena | reviewed, sound |
| Backup writer **unit** contract (9 → 11 tests, real paths) | Arena | fixed, unbuilt |
| Benchmark component absent from debug/release | Arena | fixed in source set, unbuilt |
| Build + tests green on the changed tree | **Antigravity** | B1 |
| SAF + process-kill evidence | **Antigravity** | B2 |
| Launch split with fixture hashes | **Antigravity** | B3 |
| Tier-1 device scenarios | **Antigravity** | B4 |
| `verify_workspace.ps1` reproducibility | **Antigravity** | B5 |

Arena's review verdict is unchanged: **the release gate stays closed until B1–B4
land**, but the *code* under review (the C10 writer) is sound and strictly safer
than the overwrite-in-place behaviour it replaced. B3 is a launch-thread evidence
item and should not hold the C10 merge.
