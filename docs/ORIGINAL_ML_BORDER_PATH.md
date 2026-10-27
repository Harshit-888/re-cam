# The original APK's ML border path, decompiled

Ground truth for `MlBorderDetector` and for the shipped `border_detect_224.tflite`.
Everything here was read out of artifacts **already in this repository** — no APK was
downloaded, no Android device was involved, and nothing was guessed.

- Bytes: `docs/original_apk_binaries/classes*.dex` (Dalvik) and
  `docs/original_apk_binaries/libimageproc.so` (arm64).
- Model: `app/src/main/assets/border_detect_224.tflite`
  (4 783 628 bytes, sha256 `578f53b6c90ee9bc14b6ba350c3d261ffd6001d3e9b716a7ad571b6a4bdd1972`).
- Method: `androguard` 4.1.4 walking instruction streams (a jadx-style decompile is
  not needed for this — the bytecode is readable), plus `tflite_runtime` 2.14 loading
  the `.tflite` file to print its real tensor names and shapes and to run it.

The broader picture — all 34 activities, the SQLite schema, the paper-size table — is in
[`ORIGINAL_APP_FULL_SPECIFICATION.md`](ORIGINAL_APP_FULL_SPECIFICATION.md); §9 of that
document is where the two readings were reconciled. This file is the ML path only.

Obfuscation is what hid this earlier: the classes are `Ler5;`, `Lht3;`, `Lqt3;`,
`Lsw3;`, `Lit3;`. Name-based searching finds nothing; instruction-text searching finds
everything.

## Call graph

```
Lqt3;->c(Bitmap, PSPage) / Lqt3;->d(Bitmap, PSPage)
  └─ s46;->L(Bitmap, 640, 640)         downscale to fit 640×640
  └─ Lht3;->a(Bitmap) -> Lit3          detect, returns 4 (score, PointF) + mean score
       ├─ Lht3;->b(Bitmap) -> ByteBuffer      preprocess
       ├─ Ler5;->w() -> Interpreter           the (soft-referenced) interpreter
       └─ Interpreter.runForMultipleInputsOutputs(...)
  └─ PSOpenCV.detectPageBorder(ctx, mat, w, h, ratio)   native fallback, runs anyway
```

## 1. Loading — `Ler5;->w()`

- `PSApplication.d` (a static `Context`) → `getAssets().openFd("border_detect_224.tflite")`
  → `FileChannel.map(READ_ONLY, startOffset, declaredLength)` → `MappedByteBuffer`.
- `new Interpreter(mappedBuffer, new Interpreter.Options().setNumThreads(4))`
  (0x04 into `Lnk2;->b`).
- Cached in a **`SoftReference`** (`Ler5;->g`), rebuilt when the VM drops it. Logs
  `PSBorderDetect: Creating took %.2f ms`.
- `openFd` — not `open` — means **the asset must be stored uncompressed**. This is
  exactly the failure our `androidResources { noCompress += "tflite" }` fixes; without
  it the original's own approach would throw and the catch would swallow it.

## 2. Preprocessing — `Lht3;->b(Bitmap)`

- `Bitmap.createScaledBitmap(bitmap, 224, 224, /* bilinear */ true)`; logged as
  `Preprocessing image took %.2f ms`.
- Pixels are copied into a `ByteBuffer` typed `FLOAT32` with logical RGB, **`0…255`
  per channel, not divided by 255**.
- The input tensor is `serving_default_input:0`, shape `[1, 224, 224, 3]`, float32.

## 3. Outputs — six of them, two are four floats wide

`Lht3;->a` calls `getOutputTensorIndex(name)` for **every** output and allocates a
buffer for each, from hard-coded shapes. Read from the file with tflite-runtime:

| name | shape | floats | used? | meaning |
|:---|:---|:--:|:--:|:---|
| `StatefulPartitionedCall:1` | `[1,1,4,2]` | 8 | **yes** | the four corners, `(y, x)` per corner |
| `StatefulPartitionedCall:0` | `[1,1,4]` | 4 | **yes** | one score per corner |
| `StatefulPartitionedCall:5` | `[1,1,4]` | 4 | no | decoy: same width as the scores |
| `StatefulPartitionedCall:2` | `[1]` | 1 | no | scalar, `1.0` on every test input |
| `StatefulPartitionedCall:3` | `[1,1]` | 1 | no | score-like, `≈` mean of `:0` |
| `StatefulPartitionedCall:4` | `[1,1]` | 1 | no | `0.0` on every test input |

**This is why `:5` matters.** Our previous selection rule was "8 floats = corners,
4 floats = scores". Two tensors are 4 floats wide, so the rule could bind the scores to
`:5` — a tensor that is *not* the confidence — and then a perfectly good page would
report ≈0.2 confidence, fail the 0.5 gate, and silently fall back to geometry. The
detector now binds **by name** (`StatefulPartitionedCall:1` / `:0`) and only falls back
to size when the size is unambiguous; otherwise it declines the model and says so.

## 4. Postprocessing — `Lht3;->a`

For corner *k*, with `f = corners[8]`:

```
x = f[2k + 1]      y = f[2k]
```

i.e. each point is stored **`(y, x)`** and must be swapped. Confirmed by the register
maths (`PointF(x = aget(f,1), y = aget(f,0))` for the first corner, `+3/+2`, `+5/+4`,
`+7/+6` for the rest). Values are **normalised `0…1`** — not pixels.

Scores: `mean = (s[0] + s[1] + s[2] + s[3]) / 4f` (`const/high16 1082130432` = `4.0`).
Result is `Lit3(tl, tr, br, bl, mean)` where each corner is `Lsw3(score, PointF)`.

## 5. Consumer — `Lqt3;->c` / `Lqt3;->d`

- Detection runs on a bitmap **downscaled to fit 640×640**, never the full page.
- Gate: `const/high16 1056964608` = **`0.5f`**;
  `cmpl-float(mean, 0.5f)` + `if-ltz` → the ML quad is used only when
  `mean >= 0.5`. There is no per-corner threshold.
- Accepted coordinates are multiplied by the *scaled* bitmap's width (x) and height (y),
  then by the scale ratio back to the original bitmap — plain uniform scaling, no
  per-axis correction for the 224 squash.
- The native `PSOpenCV.detectPageBorder` runs on the same bitmap **regardless**, and is
  what answers when the ML mean is below 0.5. Our port substitutes the Kotlin
  `DocumentQuad.detect` pass there, because `libimageproc.so` is not something we call.

## 6. The 0–255 encoding, measured rather than assumed

A synthetic page (white sheet, rotated −7°, on a dark background) at 224×224, run
through the real model at both encodings:

| input encoding | corner scores `:0` | mean | quad |
|:---|:---|:--:|:---|
| `0…1` (`pixel / 255`) | 0.21, 0.48, 0.81, 0.61 | **0.53** | 43,72 → 60,190 → 194,172 → 177,65 |
| `0…255` (`pixel`) | 0.99, 0.99, 0.98, 0.99 | **0.99** | 21,63 → 39,212 → 223,188 → 209,41 |

The page in that image runs from about `(20, 41)` to `(222, 212)`. Only the `0…255`
answer fits it. Our one-time calibration compares mean confidence between the two and
therefore lands on `0…255` on the first page of a session; the measurement above is now
recorded in the code so the default is not folklore.

## 7. What this changed in `MlBorderDetector`

- Outputs are bound by name; the 4-float ambiguity is resolved and, when it cannot be,
  the model is declined with a log line rather than mis-read.
- `(y, x)` order, `0…1` normalisation, the 0.5 mean gate and the `0…255` input are
  documented at the site where they are used.
- Everything else (size-agnostic buffer allocation from `tensor.shape()`, dtype
  handling, `QuadSanity` plausibility, the geometry fallback, the `[ml]` logging) is
  unchanged from Round 39.

## 8. Still unknown, and honestly so

- `:2`, `:3`, `:4` are computed by the graph and thrown away by the original. `:2` is
  `1.0` and `:4` is `0.0` on every input tried; their meaning is not established. They
  are not needed for a quad, and we do not read them.
- The model's training data and licence are unknown; it was extracted from the original
  APK. `AGENTS.md` §2.5 forbids committing `.tflite` files and this one is committed
  anyway (flagged in the changelog); `verifyBorderModel` guards its presence and hash.
- Whether the original's 640-px detection bitmap, `0.5` gate and no-per-corner-threshold
  choice are *good* is a separate question from what they *are*. Parity is the goal
  here; our `QuadSanity` is a deliberate addition on top, not a replacement.
