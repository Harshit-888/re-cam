# Rust migration decision record

> Written 2026-10-25 (round 15) in answer to: *"can we use Rust to build the whole
> app?"*
>
> **Verdict: not the UI. Yes for more of the pixel path.** This document records
> why, what each option costs, and the gate every phase must pass.

---

## 1. What "the whole app in Rust" would actually mean

There is no "Rust on Android" switch. Four different things people usually mean:

| Option | What it is | Verdict |
| --- | --- | --- |
| **A. Rust UI** (Slint, Makepad, Dioxus, egui + wgpu) | One Kotlin `Activity` (~150 lines) for permissions/intents, everything else Rust | Technically possible; **rejected** — see §3 |
| **B. Rust core + Kotlin/Compose shell** | Rust owns pixels and bytes; Kotlin owns the screen and Android APIs | **This is the architecture**, extended in phases (§4) |
| **C. Rust for business logic** (libraries, persistence) | Repository/JSON in Rust via JNI | Rejected: no measurable win, adds a marshalling boundary to every state change |
| **D. Rust as an NDK library for hot loops** | Only the compute kernels | **Shipped** (round 15) |

Rust is already in the app (D). The question is really "how far do we push B?"

---

## 2. Where the app's CPU actually goes

| Path | Owner today | Cost per page (3200 px) | Language-sensitive? |
| --- | --- | --- | --- |
| Decode JPEG → ARGB | `BitmapFactory` | ~80–150 ms | No — hardware/OS codec |
| **Filter bake** (paper map, tone tables, median, Laplacian) | Kotlin | **~0.6–2 s** | **Yes** |
| Perspective warp | `Matrix.setPolyToPoly` (native C++) | ~30–60 ms | No — already native |
| Encode JPEG/PNG | `Bitmap.compress` | ~100–250 ms | No — native codec |
| PDF raster + write | `PdfDocument` (native) | ~150–300 ms | No |
| UI (Compose) | Skia/Impeller + RenderThread | per frame, ~2–6 ms | No — not the same question |

Only **one** row is language-sensitive. That is the entire performance argument,
and it is why the kernels came first.

## 3. Why not a Rust UI (option A)

Every one of these is a Compose/platform feature we would have to re-earn in Rust:

| Capability | Compose today | Cost in a Rust UI |
| --- | --- | --- |
| Material 3 Expressive design system | Free, and the whole point of round 14 | None of Slint/Makepad/egui ship it; you build a design system first |
| Accessibility (TalkBack, switch access) | Semantics tree, free | `AccessibilityNodeProvider` bridging must be written per widget — a Play Store expectation, not a nicety |
| Text input (IME, autocorrect, emoji, clipboard) | Free | `InputConnection` bridging; the dashboard search box degrades until that is right |
| Camera | CameraX (focus, exposure, torch, zoom, tap-to-focus) | `camera2` via JNI or the limited Camera NDK; a large regression for zero gain |
| ML Kit OCR (roadmap item) | Drop-in | Requires a JNI bridge back into Java anyway |
| PDF export | `android.graphics.pdf` | Rust `printpdf` changes font metrics → different PDFs than the ones already validated |
| System integration | predictive back, share sheet, FileProvider, SAF picker | Each one is a bridge |
| Tooling | Layout inspector, Baseline Profiles, Play vitals | Rebuild the feedback loop |

And the payoff would be small: **the UI is not the slow part**. A document list
rendering at 120 Hz is limited by text shaping, raster and vsync — the same work
whichever language drives it. Rewriting the shell in Rust trades a mature,
hardware-accelerated UI stack for a younger one and pays for it in accessibility,
input and camera quality.

A full Rust UI stack (wgpu + winit + a text/raster stack) is also roughly an order
of magnitude more native code to ship and keep patched (~5–15 MB vs ~400 KB for
the kernel crate).

## 4. The plan we are on (option B, phased)

Every phase must pass the same gate before it is enabled:

> **Gate:** on a debug device, the native path and the Kotlin reference produce
> **byte-identical output** for every filter type on at least three synthetic
> pages (flat, gradient-shadow, dense text), the self-test passes on cold start,
> and a 10-page bake shows no frame drops (`JankStats`/FrameTimeline). Any failure
> → the phase stays off and Kotlin runs.

| Phase | Scope | Expected win | Risk |
| --- | --- | --- | --- |
| **1 (done)** | `medianBlurGray`, `median3x3`, `laplacian3x3` | Grayscale ~3–5× quicker; Magic Color ~1.2× | Low — kernels are integer/selection exact |
| **2** | Whole `DocumentFilters.apply`: paper map, tone tables, all five filters, one JNI call per page | Clear B&W / Magic Color ~2–4×; removes thread-pool contention with the UI | Medium — float bilinear sampling must match to the bit; needs the all-filter parity harness first |
| **3** | Perspective warp + rotation in Rust (integer bilinear) | One less Bitmap round-trip per bake | Medium — sampling parity testable |
| **4 (later)** | PDF page raster in Rust | Lower peak memory on very large documents | Higher — changes PDF bytes; separate decision |
| **5 (rejected)** | Rust UI | — | See §3 |

Phase 2 is the one worth doing: it is where the seconds are. Its prerequisite is a
**full-pipeline parity harness** — running `apply()` for all `FilterType`s in both
paths and diffing the ARGB arrays — which is cheap to add and makes every later
phase safe.

## 5. Engineering rules for the Rust boundary

1. **One JNI call per page, not per pixel.** Crossing costs microseconds; per-pixel
   crossings would erase the win.
2. **Hand-written JNI is fine to ~8 functions**, then move to UniFFI so the Kotlin
   bindings are generated rather than guarded by hand. Below that, the explicit
   contract (`0` = ran, `-1` = fall back) is simpler and has no build-time codegen.
3. **Never let native own a `Bitmap`.** Rust sees `IntArray`/`FloatArray`; Android
   owns pixels on screen and files on disk.
4. **Never ship a native path that has not proven parity.** The Kotlin reference
   stays in the tree forever — it is the oracle, not dead code.
5. **No new crates without a reason recorded here.** `jni` is the only one today;
   `rayon` is deliberately avoided (we already band rows with `std::thread::scope`
   and must cap threads below core count so the UI keeps its frames).
6. **ABI is versioned.** `NativeFilters.EXPECTED_ABI` vs `ABI_VERSION` in `lib.rs`;
   bump both when output changes so a stale `.so` is refused.

## 6. What would change this decision

- If the roadmap grows a real compute workload — on-device OCR, ML cleanup, batch
  PDF/A — a Rust **core service** grows in value. Still not a Rust UI.
- If Compose ever becomes the bottleneck *and* profiling proves it (it is currently
  ~2–6 ms/frame), revisit with data. The fix then is still baseline profiles and
  composition hygiene long before a language change.
