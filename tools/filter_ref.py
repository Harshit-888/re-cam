#!/usr/bin/env python3
"""Reference implementation of the adaptive ("Smart Scan" / "Light Text") filter.

This file is the **specification** of the filter, in the same fixed point as the two
real implementations:

  * Kotlin — `DocumentFilters.adaptiveColourReference` (the fallback, and the
    definition the on-device parity self-test compares against);
  * Rust   — `kernels::adaptive_colour` (the fast path, byte-identical by design).

All three must agree byte for byte. That is only possible because every step is
integer: the only floats that enter are the paper grid (quantised to Q8 once) and
the edge-strength plane (already computed before the kernel is called).

Run it to regenerate the numbers quoted in `docs/filter_combined_spec.md`:

    python3 tools/filter_ref.py table     # tone response: what ink becomes what grey
    python3 tools/filter_ref.py shadow    # illumination invariance
    python3 tools/filter_ref.py colour    # what colour marks become
    python3 tools/filter_ref.py all

Why the design works, in one paragraph: the original Magic Color filter divides the
page by an estimated paper surface, but it then indexes its tone curve by the
*absolute* difference from paper, so faint ink (92 % of paper reflectance) lands one
step below white. This filter keeps the divide and indexes the curve by the *ratio*
instead, which is the same quantity the divide itself is built on — so faint ink
falls where the curve can reach it, and the same ink under a shadow normalises to
the same byte. One gain per pixel, applied to all three channels, then restores
saturation around the pixel's own luma instead of clipping each channel separately.
"""

from __future__ import annotations

import sys

# Every product is Q8: 256 == 1.0.
Q = 256

# ---------------------------------------------------------------- fixed point ---


def luma_fixed(r: int, g: int, b: int) -> int:
    """Rec.601 luma with integer rounding, identical on both sides of the JNI."""
    return (r * 299 + g * 587 + b * 114 + 500) // 1000


def rnd_q8(v: int) -> int:
    """The rounding step of a Q8 multiply: `(v + 128) >> 8`, arithmetic."""
    return (v + 128) >> 8


def sample_q8(plane, gw: int, gh: int, cell: int, x: int, y: int) -> int:
    """Bilinear sample of a Q8 paper grid, over cell-sized squares."""
    if gw <= 0 or gh <= 0 or not plane:
        return 255 << 8
    cell = max(1, cell)
    gx0 = min(x // cell, gw - 1)
    gy0 = min(y // cell, gh - 1)
    gx1 = gx0 + 1 if gx0 + 1 < gw else gx0
    gy1 = gy0 + 1 if gy0 + 1 < gh else gy0
    fx = ((x - gx0 * cell) << 8) // cell
    fy = ((y - gy0 * cell) << 8) // cell
    top = (plane[gy0 * gw + gx0] * (256 - fx) + plane[gy0 * gw + gx1] * fx) >> 8
    bottom = (plane[gy1 * gw + gx0] * (256 - fx) + plane[gy1 * gw + gx1] * fx) >> 8
    return (top * (256 - fy) + bottom * fy) >> 8


def knee_gain(raw: int, knee: int, cap: int) -> int:
    """Monotone soft knee: identity to `knee`, asymptotic to `cap`, no seam."""
    if raw <= knee:
        return raw
    span = cap - knee
    if span <= 0:
        return cap
    over = raw - knee
    return knee + (span * over) // (span + over)


def ratio_curve(lo: float, hi: float, gamma: float):
    """`out[i] = 255 * clamp((i/255 - lo) / (hi - lo)) ** gamma`, 256 entries.

    `lo` and `hi` are ratios of paper brightness. Below `lo` a pixel is ink; at or
    above `hi` it is paper. The gap between them is the sensitivity to faint ink.
    """
    span = max(hi - lo, 1e-4)
    out = []
    for i in range(256):
        t = (i / 255.0 - lo) / span
        t = min(1.0, max(0.0, t))
        out.append(int(255.0 * (t**gamma) + 0.5))
    return out


# ---------------------------------------------------------------- the presets ---
#
# The numbers are the ones in `DocumentFilters`; keep both in step.

GAIN_KNEE_DOCUMENT = 8     # 2048 Q8
GAIN_CAP_DOCUMENT = 24     # 6144 Q8
GAIN_KNEE_SCENE = 1.2      # 307 Q8
GAIN_CAP_SCENE = 2.0       # 512 Q8

DETAIL_SCALE = 50

PRESETS = {
    # name: (curve lo, curve hi, gamma, floor Q8, chroma Q8, detail cap Q8)
    "SMART": (0.60, 0.98, 1.6, 56, 287, 56),
    # Round 63: the toe is lower (so faint marks survive) and the shoulder reaches
    # white sooner (so a background the surface could not flatten is not greyed).
    "LIGHT_TEXT": (0.70, 0.94, 1.0, 64, 307, 0),
    # The scene curve: near-identity, so a photograph is white-balanced and nothing
    # else. Selected by `classifiesAsDocument` returning false.
    "SCENE": (0.02, 0.99, 1.0, 128, 256, 0),
}


def preset(name: str):
    lo, hi, gamma, floor, chroma, detail_cap = PRESETS[name]
    curve = ratio_curve(lo, hi, gamma)
    if name == "SCENE":
        params = [
            int(GAIN_KNEE_SCENE * Q + 0.5),
            int(GAIN_CAP_SCENE * Q + 0.5),
            floor,
            chroma,
            detail_cap,
            DETAIL_SCALE,
        ]
    else:
        params = [
            GAIN_KNEE_DOCUMENT * Q,
            GAIN_CAP_DOCUMENT * Q,
            floor,
            chroma,
            detail_cap,
            DETAIL_SCALE,
        ]
    return curve, params


def adaptive(pixels, w: int, paper_r, paper_g, paper_b, gw: int, gh: int, cell: int,
             curve, params, detail=None):
    """In-place application of the kernel. Mirrors `kernels::adaptive_colour`."""
    knee, cap, floor, chroma, detail_cap, detail_scale = params
    detail_scale = max(1, detail_scale)
    use_detail = detail_cap > 0 and detail is not None and len(detail) >= len(pixels)

    for i, p in enumerate(pixels):
        x = i % w
        y = i // w
        r = (p >> 16) & 0xFF
        g = (p >> 8) & 0xFF
        b = p & 0xFF

        pr = max(1, sample_q8(paper_r, gw, gh, cell, x, y))
        pg = max(1, sample_q8(paper_g, gw, gh, cell, x, y))
        pb = max(1, sample_q8(paper_b, gw, gh, cell, x, y))
        gr = knee_gain((255 * Q * Q) // pr, knee, cap)
        gg = knee_gain((255 * Q * Q) // pg, knee, cap)
        gb = knee_gain((255 * Q * Q) // pb, knee, cap)

        cr = min(255, rnd_q8(r * gr))
        cg = min(255, rnd_q8(g * gg))
        cb = min(255, rnd_q8(b * gb))

        luma = min(255, max(0, luma_fixed(cr, cg, cb)))
        out_luma = curve[luma]
        gain = (out_luma << 8) // luma if luma > 0 else Q
        if gain < floor:
            gain = floor

        if use_detail:
            d = detail[i]
            dq = 0 if d <= 0 else int(d * detail_cap / detail_scale + 0.5)
            dq = min(dq, detail_cap)
            gain = (gain * (256 + dq)) >> 8

        sr = min(255, rnd_q8(cr * gain))
        sg = min(255, rnd_q8(cg * gain))
        sb = min(255, rnd_q8(cb * gain))

        grey = luma_fixed(sr, sg, sb)
        out_r = min(255, max(0, grey + ((sr - grey) * chroma >> 8)))
        out_g = min(255, max(0, grey + ((sg - grey) * chroma >> 8)))
        out_b = min(255, max(0, grey + ((sb - grey) * chroma >> 8)))

        pixels[i] = (p & 0xFF000000) | (out_r << 16) | (out_g << 8) | out_b
    return pixels


# ------------------------------------------------------------------- reports ---


def grey(v: int) -> int:
    return (0xFF << 24) | (v << 16) | (v << 8) | v


def run_grey(paper: int, marks, name: str):
    """Each mark on its own 1x1 page with a flat paper of `paper`."""
    curve, params = preset(name)
    grid = [paper * Q]
    out = []
    for mark in marks:
        px = [grey(mark)]
        adaptive(px, 1, grid, grid, grid, 1, 1, 1, curve, params)
        out.append((mark, px[0] & 0xFF))
    return out


def cmd_table() -> None:
    marks = [255, 253, 247, 238, 234, 219, 200, 176, 141, 131, 76, 0]
    print("Tone response: a mark of N on 238 paper (paper is white after the divide).")
    print("reflectance = N / 238.\n")
    print("| mark | refl. | SMART | LIGHT_TEXT | SCENE |")
    print("|---|---|---|---|---|")
    columns = {name: dict(run_grey(238, marks, name)) for name in PRESETS}
    for mark in marks:
        refl = mark / 238
        print(
            f"| {mark} | {refl:.2f} | {columns['SMART'][mark]} | "
            f"{columns['LIGHT_TEXT'][mark]} | {columns['SCENE'][mark]} |"
        )
    print()
    print("The gap that started this work: 219 (92 % reflectance) used to stay at 248")
    print("under Magic Color; Clear B&W drove it to 231 only because it binarises.")


def cmd_shadow() -> None:
    print("Illumination invariance: the same ink under three paper levels.\n")
    ink = [234, 219, 176, 141, 76]          # 98 %, 92 %, 74 %, 59 %, 32 % of 238
    papers = [238, 150, 90]
    print("| ink | " + " | ".join(f"paper {p}" for p in papers) + " | spread |")
    print("|---|" + "---|" * (len(papers) + 1))
    for name in ("SMART", "LIGHT_TEXT"):
        print(f"\n**{name}**\n")
        for mark_ratio in ink:
            row = []
            for p in papers:
                mark = max(1, int(mark_ratio * p / 238 + 0.5))
                curve, params = preset(name)
                grid = [p * Q]
                px = [grey(mark)]
                adaptive(px, 1, grid, grid, grid, 1, 1, 1, curve, params)
                row.append(px[0] & 0xFF)
            print(f"| {mark_ratio / 238:.2f} | " + " | ".join(str(v) for v in row) +
                  f" | {max(row) - min(row)} |")


def cmd_colour() -> None:
    marks = {
        "blue pen (60,80,160)": (60, 80, 160),
        "red stamp (200,60,60)": (200, 60, 60),
        "yellow highlighter (240,220,120)": (240, 220, 120),
        "green signature (80,150,90)": (80, 150, 90),
    }
    print("Colour: one hue-preserving gain, not three independent clips. Paper 238.\n")
    print("| mark | luma | SMART | LIGHT_TEXT |")
    print("|---|---|---|---|")
    for label, (r, g, b) in marks.items():
        row = []
        for name in ("SMART", "LIGHT_TEXT"):
            curve, params = preset(name)
            grid = [238 * Q]
            px = [(0xFF << 24) | (r << 16) | (g << 8) | b]
            adaptive(px, 1, grid, grid, grid, 1, 1, 1, curve, params)
            p = px[0]
            row.append(((p >> 16) & 0xFF, (p >> 8) & 0xFF, p & 0xFF))
        print(f"| {label} | {luma_fixed(r, g, b)} | {row[0]} | {row[1]} |")


def main(argv) -> int:
    command = argv[1] if len(argv) > 1 else "all"
    commands = {"table": cmd_table, "shadow": cmd_shadow, "colour": cmd_colour}
    if command == "all":
        for fn in commands.values():
            fn()
            print()
        return 0
    if command not in commands:
        print(f"usage: {argv[0]} [{'|'.join(commands)}|all]", file=sys.stderr)
        return 2
    commands[command]()
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
