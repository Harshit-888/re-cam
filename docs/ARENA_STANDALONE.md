# Working without Antigravity

Arena.ai's sandbox is not a development machine. It has no JDK, no Android SDK,
no device, and no route to `dl.google.com`, `maven.google.com`, `repo1.maven.org`,
`services.gradle.org` or `plugins.gradle.org`. Every claim in this repository's
docs that says "Arena cannot build" is therefore true, and will stay true.

But "Arena cannot build" and "Antigravity is the only way to build" are different
claims. This document separates what is genuinely blocked from what was only
assumed blocked, and gives each job a second authority.

Measured on 2026-11-11 from this sandbox — not inferred from documentation:

| Endpoint | Result |
| :--- | :--- |
| `api.github.com`, `pypi.org`, `registry.npmjs.org` | **reachable (200)** |
| `dl.google.com`, `maven.google.com`, `repo1.maven.org`, `maven-central.storage-download.googleapis.com`, `services.gradle.org`, `plugins.gradle.org`, `jitpack.io` | **blocked** |
| `github.com` (HTML, latest-release redirect) | reachable |
| `objects.githubusercontent.com`, `release-assets.githubusercontent.com`, `raw.githubusercontent.com` | **blocked** |
| `deb.debian.org`, `archive.ubuntu.com` | **blocked** |

The consequence is a clean split, and it is not the split the repo has been
assuming:

- **Building needs a JVM + Android SDK + Maven.** Every route to those is closed
  here. Even `jdk4py` (a JDK in a wheel — it does download) cannot help, because
  a JDK with no Maven repository has no dependencies to resolve and no Android
  platform to compile against. → **GitHub Actions is the build authority.**
- **Reverse engineering needs parsers, not a JVM.** `androguard`, `capstone` and
  `pyelftools` are pure wheels on PyPI, which is open. What Ghidra, apktool and
  jadx actually *did* for this repo's questions is now reproducible here. →
  **Arena can do the RE itself.**

## 1. Building and verifying: GitHub Actions, read by Arena

`.github/workflows/android.yml` (new, in this branch) runs on every push:

1. JDK **17** — matching `jvmToolchain(17)` in `app/build.gradle.kts`, so Gradle
   never has to auto-provision a toolchain mid-build.
2. Android platform 36 + build-tools 36.0.0 (matches `compileSdk = 36`, `minSdk = 26`).
3. `./gradlew testDebugUnitTest` — the wrapper, i.e. **Gradle 9.1.0** as pinned,
   which also settles the 9.1.0-vs-9.6.0 ambiguity from
   `docs/TRIAGE_ANDROID_STUDIO_REPORT.md` §2.
4. `assembleDebug` and `assembleBenchmark` (non-debuggable, so performance claims
   have something honest to run on).
5. A benchmark-only guard: fails the build if `BenchmarkAuditActivity` appears in
   a debug APK.
6. Uploads the test XML/HTML and the APKs, and writes a table to the run summary.

Arena then reads results instead of predicting them:

```
gh run list --limit 3                      # what just happened
gh run watch <id> --exit-status            # wait for it
gh run view <id> --log-failed              # the compiler, verbatim
gh run download <id> -n apks-<sha>         # the artifact
```

This is the whole point: the recurring changelog species *"fix(build): resolve
Round NN compilation errors"* exists because the only compiler in the loop was on
one laptop. With this workflow, a push gets a verdict in minutes and the verdict
is quotable.

**What still needs Antigravity (or a phone):** anything measuring wall-clock time,
jank, camera behaviour, or memory on real hardware. CI is an emulator-free build
and unit-test authority, not a device. Keep `docs/AGENT_HANDOFF.md` for those.

**What Arena will not do:** take signing material. No keystore, no
`local.properties`, no `signingConfig` secrets through Drive or chat. The workflow
deliberately does not build `release` — that stays blocked on F8 in
`docs/PROJECT_CONTEXT_AND_ROADMAP.md`, as it should.

## 2. Reverse engineering: now a local tool, not a download

`tools/re/re_probe.py` (new, in this branch) wraps the three wheels into one
command. Install per session (the sandbox does not persist pip state):

```bash
pip install --break-system-packages androguard capstone pyelftools
```

```bash
tools/re/re_probe.py apk adobe-scan/base.apk            # manifest truth, no aapt
tools/re/re_probe.py apk build.apk --grep Benchmark     # is the audit activity declared?
tools/re/re_probe.py so  indymobile/native_libs/libimageproc.so
tools/re/re_probe.py so  libnms.so --disasm --address 0x149a0 --count 40
tools/re/re_probe.py dex docs/original_apk_binaries/classes.dex --grep android
tools/re/re_probe.py strings adobe-scan/base.apk        # endpoints, keys, hosts
```

Verified output on this repo's own corpora, this session:

- `adobe-scan/base.apk` → `com.adobe.scan.android`, 26.10.06 (263183520), minSdk 30 /
  target 36, **82 activities**, 33 permissions, all without `aapt`.
- `libimageproc.so` → EM_AARCH64, 160 dynsyms, **7 JNI exports**
  (`enhancePhoto`, `signatureFilter`, `perspectiveTransformRect`, `detectPageBorder`,
  `enhanceSignature`, `enhanceDocument`, `calculateIOU`) and 70 undefined imports
  that name the algorithm: `cv::HoughLines`, `cv::medianBlur`, `cv::contourArea`.
  That import list is the actual answer to "what does their page detection do" —
  no decompiler required.
- `libnms.so` → 41 dynsyms, 1778 relocations, ARM64 disassembly at any address.
- `classes.dex` → **10143 classes**, package histogram, substring search.

### Coverage, honestly stated

| Job | Replacement | Status |
| :--- | :--- | :--- |
| Read `AndroidManifest.xml` without `aapt` | androguard | ✅ done |
| Component/permission inventory, "is X declared" | androguard | ✅ done |
| DEX class/method inventory, string + xref search | androguard | ✅ done |
| ELF imports/exports/relocations, JNI boundary map | pyelftools | ✅ done |
| ARM64 disassembly of a specific function | capstone | ✅ done |
| High-quality Java pseudo-source | *jadx* | ⚠️ not available; `androguard`'s DAD yields nothing usable on this APK (verified: 0 methods) |
| Resource decode / repack (icons, layouts, `strings.xml`) | *apktool* | ⚠️ not available; irrelevant to a clean-room rewrite, which must not reuse resources anyway |
| Full C decompilation, types, dataflow | *Ghidra* | ⚠️ not available; `--disasm` gives instructions, not expressions |

The three gaps do not block this project. Its RE questions have been "what
components exist", "what do the native filters compute", "what is copied from
where" — all answered above. Where a decompiler would genuinely help, the honest
move is to run Ghidra/jadx **on the machine that already has a JVM**
(Antigravity's), then paste the *output* into `docs/re/` — a text file, not a
tool. That keeps the sandbox dependency-free.

## 3. Closing the last open manifest check without `aapt`

`docs/TRIAGE_ANDROID_STUDIO_REPORT.md` Track 1 wants proof that
`BenchmarkAuditActivity` is declared only in the benchmark manifest. The CI job
above now enforces it on every push. For a one-off check on a locally built APK,
upload it to Drive (debug APK is ~65 MB, under the 100 MB per-file cap) and run:

```bash
tools/re/re_probe.py apk downloaded.apk --grep Benchmark   # exit 1 when absent
```

Exit code 0/1 makes it a test, not a reading exercise.

## 4. What changed in this branch

- `.github/workflows/android.yml` — new. Build/test authority outside Antigravity.
- `tools/re/re_probe.py` — new. README-able, dependency-light RE probe; runs on
  this repo's corpora as shown.
- `docs/ARENA_STANDALONE.md` — this file.
- No app source, no test, no build config touched. Nothing under `adobe-scan/`,
  `indymobile/` or `docs/original_apk_binaries/` was modified.

## 5. Residual risks

- **The workflow is unrun.** It is committed and syntax-checked (YAML parses, 12
  steps), but no run has completed. The first push that touches it will tell us
  whether `sdkmanager` package names and the benchmark guard hold. Treat the
  first red run as information, not failure.
- **CI minutes.** Public repo, so standard runners are free; the Gradle cache step
  exists to keep runs near 5–8 minutes.
- **Pip state is per-session.** Re-run the install line each time; it is two
  packages' worth of seconds.
