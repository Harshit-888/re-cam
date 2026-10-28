# Adobe Scan vs Doc Scanner — the filters, and which to use

**Status:** research only. No file in `app/src/` was changed for this document, and
`adobe-scan/` was read, not modified.

**Sources, all in this repo:**

| What | Where |
|---|---|
| Adobe Scan 26.10.06 filter → cleaning-level mapping | `adobe-scan/decompiled_src/MagicCleanEdgeDetection_m8d.java` (static block + `m(int)`) |
| Adobe's bridge structures (cleaning levels, tint pixel, masks, output flags) | `adobe-scan/decompiled_src/CameraCleanUtils.java` |
| Adobe's native engine, symbol list | `adobe-scan/extracted/lib/arm64-v8a/libMagicClean.so` (43,943,240 B) |
| Adobe filter names in the dex | `classes.dex` string table: `FilterOriginalColor`, `FilterAutoColor`, `FilterWhiteboard`, `FilterGrayscale`, `FilterLightText` |
| Adobe engine write-up | `docs/ADOBE_SCAN_DEEP_REVERSE_ENGINEERING.md` §4–5 |
| This app's filters | `app/src/main/java/com/example/docscanner/util/DocumentFilters.kt` |
| This app's filter lineage | `docs/ORIGINAL_APP_FILTER_SPEC.md` §4, §13 (the authoritative disassembly) |

---

## 0. The answer first — which filter to use

For **this app's** five presets, in the order that gets the best page out of a phone:

| Use it for | Filter | Why |
|---|---|---|
| **Everything, by default** — receipts, coloured forms, stamps, signatures, a page under mixed light | **Magic Color** | Divides out the lighting per channel *and* keeps colour. Paper is pinned to white and ink to black, so a hand shadow and a desk lamp stop mattering. This is the right default; it already is the app's. |
| **Pure black print** — printed paper, contracts, notes, anything going to print or OCR | **Clear B&W** | The same illumination correction with a steeper curve, then grayscale. Crispest, most "scanned" output, and usually the smallest file (near-binary content in a lossless PNG). Colour is gone: a red stamp becomes a dark grey blob. |
| **Nothing may be lost** — photos, artwork, a page you will edit later, saturated colour | **Original** | Passthrough. The only preset that cannot clip a colour or invent contrast. |
| **Faithful mono** — a page already lit evenly, where you want the real tones | **Grayscale** | Rec.601 desaturate + a 9×9 median. No illumination correction at all, so a shadow stays a shadow. Also the softest of the five (the median is 9 px wide). |
| **Underexposed *photos*** | **Lighten** | A global linear stretch. It cannot fix shading — measured below: shadowed paper lands at 144, not 255. It is a rescue for a dark frame, never the right choice for a document. |

Short version: **Magic Color for documents, Clear B&W for print, Original when you must
not lose anything, Grayscale only for even light, Lighten almost never.**

Against **Adobe**, the honest verdict:

- **For printed text there is no structural reason to think this app is behind.** The
  transcribed divide table is steeper than a textbook Normalize, which is exactly the
  "punchy, like a document scanner" goal — and Adobe's own mono preset is a *high-contrast*
  level, i.e. it is chasing the same curve shape. Nothing here was rendered side by side
  (there is no device in this sandbox), so read this as a statement about the algorithms,
  not a pixel diff.
- **Adobe wins in four specific places**, and only one of them is worth chasing now:
  1. **faint / low-contrast ink** (its *Light Text* preset) — this app leaves faint ink
     faint, see §4.1. *This is the real gap.*
  2. hard, local shadows and curled pages (an on-device CNN shadow-removal model plus a
     dewarping grid);
  3. glossy whiteboards and glare (its *Whiteboard* preset);
  4. colour fidelity under coloured light (Adobe normalises **L\*** only, in CIELAB, and
     boosts chroma separately; this app divides **R, G and B separately**, which
     neutralises a colour cast but pushes saturated ink toward clipping).

---

## 1. What Adobe Scan actually ships

### 1.1 Five presets, five cleaning levels — decoded, not guessed

`MagicCleanEdgeDetection_m8d` holds the four non-zero levels as static fields and maps
the dialog's index onto them:

```java
static { h = 6; i = 2; j = 4; k = 8; }

public static int m(int i2) {
    if (i2 == 0) return 0;   // level 0
    if (i2 == 1) return i;   // level 2
    if (i2 == 2) return j;   // level 4
    if (i2 == 3) return h;   // level 6
    if (i2 == 4) return k;   // level 8
}
```

Combined with `CameraCleanUtils.CleaningLevel`
(`kMinCleaningLevel = 0`, `kDefaultCleaningLevel = 2`,
`kDefaultAggressiveCleaningLevel = 4`, `kMaxCleaningLevel = 6`,
`kLightTextCleaningLevel = 8`) and the five `Filter*` names in the dex:

| # | Preset | Level | What it is for | Default? |
|---|---|---|---|---|
| 0 | **Original** | 0 | raw capture | |
| 1 | **Auto** | 2 | shadow removal + colour; the everyday filter | ✔ `kDefaultCleaningLevel` |
| 2 | **Whiteboard** | 4 | aggressive background whitening + text sharpening, glare | `kDefaultAggressiveCleaningLevel` |
| 3 | **Grayscale** | 6 | high-contrast monochrome | `kMaxCleaningLevel` |
| 4 | **Light Text** | 8 | low-contrast ink / pencil boost | outside the 0–6 range on purpose |

Note the shape of that table: **Adobe's default is a colour, shadow-removing filter**, and
its "grayscale" is *high contrast mono*, not a faithful desaturate. That is already a
statement about what users actually keep.

### 1.2 The engine behind them

- `libMagicClean.so`, 42 MB, class names recovered from its own mangled symbols:
  `CALABBitmap`, `CRGBABitmap`, `CMonoBitmap`, `CNNShadowRemoval`,
  `CNNDocClassification`, `CNNBoundaryDetection`, `CNNDewarpingGridCompute`,
  `BDParamsAlgoHC / LC / NN / Fusion`, `BDBinarizationParamsDocType*`,
  `BDParamsDocTypeDocument / Book / Form / WhiteBoard / BusinessCard`.
- So: **classical CV fused with neural nets**, per-document-type parameter sets, CIELAB
  working space, a shadow-removal CNN, a document-type classifier, and a dewarping grid
  (`DewarpDocumentsAndroidShim`).
- The CIELAB pass is corroborated by the bridge, not just the write-up:
  `ImageCleaningOutput` carries `mTintPixel` (`LABColorPixel L,A,B`), `mIsTintRemoved`,
  `mDocumentColorType` — i.e. paper-tint detection, tint removal and a colour-type verdict
  per page.
- The NN models are **not in the APK**. `setupSenseiModels$1` creates model directories
  under `getCacheDir()` and calls `setBasePathToModels(cacheDir)`; the `models/` folder in
  `adobe-scan/` holds only the capture / page-turn / resizer nets. Adobe downloads the
  cleaning nets at runtime.

### 1.3 What is solid in that write-up and what is not

`docs/ADOBE_SCAN_DEEP_REVERSE_ENGINEERING.md` §5.1 (the level table) and §5.2 (CIELAB,
tint pixel, `L_norm = min(100, L/L_bg·100)`, chroma ≈ 1.2) are consistent with the
artifacts above. Treat the exact per-level kernels as **inferred** — the cleaning path is
inside a 42 MB stripped `.so` and only the class names, the bridge structs and the level
numbers are actually decoded. Nothing here depends on the inferred details.

---

## 2. What this app ships

Five presets, `model/Models.kt` → `FilterType`, applied by `DocumentFilters.apply()`.
They are a **transcription of the original APK's `libimageproc.so`** (`enhanceDocument`,
`enhancePhoto`), not an invention — `docs/ORIGINAL_APP_FILTER_SPEC.md` §13 is the
instruction-level source.

| Preset | Pipeline (exact) |
|---|---|
| **Original** | returns the source bitmap unchanged |
| **Lighten** | 256-entry LUT from the 2 %/98 % black/white points (`fa1.H`), linear between them, per channel |
| **Magic Color** | paper map (mean-pool → closing → 3 box blurs) → `divide[row][value]` with `row = curveB\|d\| + α·(curveA\|d\| − curveB\|d\|)`, `α = min(1, Laplacian/50)`, per channel. `d = src − paper` |
| **Clear B&W** | the same paper map → `divide[curveA\|d\|][value]` per channel → Rec.601 grey |
| **Grayscale** | Rec.601 + `medianBlur(9)`; **no** illumination correction, **no** enhancement |

Key facts that matter for choosing:

- The **paper map** is a per-cell mean of the frame (cell radius `sqrt(pixels/8000)`, floor
  14 px), closed with a radius of `max(gw,gh)/40` cells, then smoothed. It estimates *the
  paper level at each point of the page*, which is what makes shadows disappear.
- The **divide table** is the actual tone curve, and it is steep by construction. With a
  paper level of 238: paper → 255, anything ≤ 120 → 0–1, and the whole ramp lives between
  150 and 238 (spec §13.4).
- **Non-destructive**: every bake starts from the pristine capture (`PageRenderer`), so a
  filter can be changed after the fact — annotations and the signature are replayed on top
  and are never filtered.
- **Output format depends on the filter** (`ImageStore`): Original / Magic Color / Lighten
  save JPEG; Clear B&W and Grayscale save lossless PNG.
  That is a real file-size consideration: Clear B&W's output is near-binary and `PNG`
  compresses it hard, while **Grayscale is a lossless PNG of a tonal image** and should
  be expected to be several times larger than the Magic Color JPEG for the same page
  (an expectation from the encoders, not a measurement — there is no device here).

---

## 3. Side by side

| | Adobe Scan | Doc Scanner (this app) |
|---|---|---|
| Presets | Original, Auto, Whiteboard, Grayscale, Light Text | Original, Magic Color, Clear B&W, Grayscale, Lighten |
| Default | Auto (colour, shadow-removing) | Magic Color — *same idea* |
| Shadow removal | CIELAB L\* normalisation by a local background surface + a **CNN shadow-removal** pass | per-channel divide by a block-pooled + closed paper map |
| Colour under coloured light | chroma untouched (L\*-only), tint pixel detected and removed | per-channel divide neutralises the cast **and** saturates ink |
| Low-contrast ink | dedicated **Light Text** level | none — faint ink stays faint (§4.1) |
| Whiteboard / glare | dedicated level 4 | none |
| Curled pages | NN dewarping grid | none (4-corner perspective only) |
| Per-document tuning | CNN doc classification → parameter set per type | none wired (the classifier exists, see §7) |
| Engine | 42 MB native + runtime-downloaded NN models | pure Kotlin + optional Rust kernels, fully offline |
| Determinism | model version dependent | byte-exact, unit-tested against a reference |
| Cost per page @3200 px | tens–hundreds of ms on the NPU/CPU | LUT-type work; Lighten cheapest, then Clear B&W, then Magic Color, then Grayscale's 9×9 median |

---

## 4. Measured tone behaviour

Everything in this section is **computed by transcribing the Kotlin tables** (`toneCurve`,
`responseValue`, `divideLookup`, `colourise`, `levelTwoChannel`) into a scratch script and
evaluating them — the same arithmetic the app runs, not a screenshot of it. Lighten is
evaluated with a typical page's estimated points (black 40, white 235).

### 4.1 Tone response at a paper level of 238

| input | Magic Color (flat) | Magic Color (edge, α=1) | Clear B&W (grey) | Grayscale | Lighten |
|---:|---:|---:|---:|---:|---:|
| 250 | 253 | 245 | 245 | 250 | 255 |
| 238 *(paper)* | 255 | 255 | 255 | 238 | 255 |
| 220 *(faint print, 92 %)* | **248** | 231 | **231** | 220 | 235 |
| 200 | 216 | 168 | 168 | 200 | 209 |
| 180 | 156 | 89 | 89 | 180 | 183 |
| 160 | 78 | 24 | 24 | 160 | 157 |
| 140 | 12 | 0 | 0 | 140 | 131 |
| 120 | 0 | 0 | 0 | 120 | 105 |
| 100 | 0 | 0 | 0 | 100 | 78 |
| 60 | 0 | 0 | 0 | 60 | 26 |

Read the **bold** row. This is the important finding of the whole document:

> **Faint ink stays faint.** Both document filters key the tone curve on the *absolute*
> difference from paper. Text 18 levels below paper (92 % reflectance — a light pencil
> line, a faxed copy, thermal receipt, a faded stamp) comes out at 248 / 231, i.e. almost
> paper. They are very good at *dark* ink and nearly blind to *light* ink. Adobe ships a
> whole preset (Light Text, level 8) for exactly this.

Also note **Grayscale is an identity curve** — it changes nothing about tone, only chroma
and grain. And **Lighten's mid-tones barely move**: its whole effect is at the two ends.

### 4.2 Illumination invariance (the same ink under two lights)

Paper at 238 (lit) vs 150 (a hand shadow across the page), ink and pencil defined by
*reflectance* so the two rows describe the same physical mark:

| case | Magic Color | Clear B&W | Grayscale | Lighten |
|---|---:|---:|---:|---:|
| paper, lit | 255 | 255 | 238 | 255 |
| paper, in shadow | **255** | **255** | 150 | 144 |
| ink 40 %, lit | 0 | 0 | 95 | 72 |
| ink 40 %, shadow | 0 | 0 | 60 | 26 |
| pencil 74 %, lit | 141 | 75 | 176 | 178 |
| pencil 74 %, shadow | 168 | 83 | 111 | 93 |

- **Magic Color and Clear B&W remove the shadow completely** (paper → 255 either way, ink
  → 0 either way). That is the divide table doing its job.
- **Grayscale keeps the shadow** (238 vs 150) — it never looks at the illumination.
- **Lighten keeps the shadow too** (255 vs 144). A global stretch brightens everything
  equally, so the *difference* between lit and shadowed paper survives. Lighten is
  therefore the **worst** choice for a page in shade, which is the opposite of what its
  name suggests.
- Mid-tones are not perfectly invariant (pencil: 141 lit vs 168 shadowed) because the
  curve is indexed by an absolute level difference, not a ratio. The bias is toward
  *lifting* pencil in shadow, which is the safe direction.

### 4.3 Colour (paper 238)

| mark | Magic Color | Clear B&W | Grayscale | Lighten |
|---|---|---|---|---|
| printed black (40,40,40) | (0,0,0) | 0 | 40 | (0,0,0) |
| pencil (176,176,176) | (141,141,141) | 75 | 176 | (178,178,178) |
| blue pen (60,80,160) | (0,0,78) navy | 2 | 83 | (26,52,157) |
| red stamp (200,60,60) | (216,0,0) pure red | 50 dark grey | 101 | (209,26,26) |
| yellow marker (240,220,120) | (255,248,0) | 211 | 214 | (255,235,105) |
| cream paper (232,224,200) | (255,252,216) still cream | 235 near-white | 223 | (251,241,209) |

Two consequences:

1. **Magic Color already boosts chroma**, as a side effect of the per-channel divide — a
   red stamp goes to *pure* red, blue pen to navy. That is why copying Adobe's 1.2× chroma
   boost on top would mostly clip colours rather than improve them, and why it would
   change output pixels this app promises not to change.
2. **Clear B&W destroys colour information** — the red stamp becomes grey 50, blue ink
   becomes black. Perfect for print, wrong for a highlighted or stamped page.

---

## 5. Verdict, per filter

**This app**

- **Magic Color — best overall, keep it as the default.** The only preset that both fixes
  the lighting and keeps the page's colour. Failure mode: saturated marks clip, and faint
  ink stays light.
- **Clear B&W — best for print and for file size.** Sharpest text, smallest PDF (PNG of
  near-binary content). Failure mode: no colour, and its ramp starts at ~150 on a 238
  paper, so pencil and light stamps go light-grey to nothing.
- **Original — the only lossless one.** Needed for photos and for anyone who will edit the
  page again.
- **Grayscale — the honest middle, but it does not fix light.** Choose it only when the
  page is already evenly lit and the tones matter (a photo inside a document).
- **Lighten — the weakest preset for documents.** Global, no shading model, no ink model.
  Its one legitimate job is an underexposed *photo*.

**Adobe**

- **Auto** is the direct competitor of Magic Color and is better under *coloured* light and
  under hard, local shadows (per-L\* normalisation + the shadow CNN). Under plain
  fluorescent light on a flat page, the gap is small.
- **Light Text** is the one preset this app does not answer at all, and it fixes the most
  common real-world disappointment (a faint receipt or a pencil note saved as a nearly
  blank page).
- **Whiteboard** is a real capability this app lacks (glare and specular sheen on a glossy
  surface).
- **Grayscale (level 6, "high contrast mono")** is more aggressive than this app's
  Grayscale — it is closer to this app's Clear B&W.

---

## 6. If you want to close the gap — ranked, cheapest first

Nothing here is implemented; all of it is a proposal, and any of 1–3 changes filter
output, which the standing rule says needs your explicit go-ahead.

1. **Add a "Light Text" preset** (Adobe level 8 equivalent). The cheapest real win: no new
   pipeline, no new model. Run the existing paper map, and for the tone stage use a curve
   indexed on the *ratio* to paper (or a gamma on `d/paper`) so that a mark 5–15 % below
   paper lands well below half. Today those marks land at 248/231; a light-text curve
   should put them around 120–150. Pure LUT — testable against the existing
   `DocumentFiltersTest` style of synthetic pages.
2. **Wire the page-type classifier in front of the default.** `classifiesAsDocument()` and
   its tests already exist and are **not called by any production path** (§7). It answers
   Adobe's `CNNDocClassification` question — "is this a page or a scene?" — and is the
   cheap way to stop Magic Color repainting a dark photo as a white page. This is
   Adobe's per-type parameter switching, minus the CNN.
3. **Tint removal** (Adobe's `mTintPixel` / `mIsTintRemoved`). Low value for now: the
   per-channel divide already whitenes a uniformly tinted page (cream paper goes from
   (232,224,200) to (255,252,216) — close to white). Worth it only if you want *pure*
   white paper on aged documents.
4. **Whiteboard / glare mode.** Needs a specular mask (highlight detection) before the
   divide, or highlights get divided into white blobs. Medium effort, narrow use.
5. **Do not port:** the 42 MB native engine, the runtime model download (this app is
   offline and deterministic by design), NN dewarping, or a chroma boost (see §4.3 — the
   per-channel curve already saturates chroma, and it would change output pixels).

---

## 7. Two things in the code you should know about (not changed)

1. **The scene classifier is written, tested, and unused.** `classifiesAsDocument()` in
   `DocumentFilters.kt` is referenced only from `DocumentFiltersTest`. So is the
   `GAIN_KNEE_*` / `GAIN_CAP_*` pair that was meant to bound how far a *scene* may be
   lifted. In practice every image handed to Magic Color is treated as a document, which
   is why pointing the app at a dark photo paints it white.
2. **`DocumentFilters.kt` still carries 18 constants and KDoc paragraphs for algorithms
   that are no longer in the shipped path** — `GAIN_KNEE_DOCUMENT`, `GAIN_CAP_DOCUMENT`,
   `GAIN_KNEE_SCENE`, `GAIN_CAP_SCENE`, `CURVE_BLACK_FLOOR`, `CURVE_WHITE_CEIL`,
   `CURVE_GAMMA`, `MAGIC_COLOR_SATURATION`, `SHARPEN_AMOUNT`, `SAUVOLA_K`, `SAUVOLA_R`,
   `SAUVOLA_MIN_STD`, `BW_LOCAL_CLAMP_LOW`, `AA_SUPERSAMPLE`, `OTSU_SAMPLE_STRIDE`,
   `BW_SHARPEN_AMOUNT`, `BW_SHARPEN_PASSES`, `SHARPEN_NOISE_FLOOR` — 18 of the file's 52
   `const val`s. Each appears exactly once in the file: its own declaration. The
   shipped Clear B&W has **no Otsu and no Sauvola**; the header comment and
   `ORIGINAL_APP_FILTER_SPEC.md` §10 still read as if it did. This is documentation debt
   that actively misleads: two of the four "why is my B&W soft" hypotheses in the docs
   describe code that is not there.
