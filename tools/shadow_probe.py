#!/usr/bin/env python3
"""Why shadows survive, measured — a probe for the paper-map estimator.

`tools/filter_ref.py` tests the *kernel*. This file tests what feeds it: the paper
surface, estimated by mean-pooling the frame into cells, closing that grid (dilate
then erode, to swallow ink), smoothing it, and sampling it bilinearly.

What it measures, and why that is enough: for a paper pixel the tone curve is an
identity (255 in, 255 out) and the chroma term cancels (grey around grey), so the
only thing that decides whether paper reaches pure white is step 2 of the kernel —
`gain = knee(255 / paper)` — and the clamp in step 3. The probe therefore computes
that one expression per pixel instead of the whole kernel, which makes it fast
enough to sweep variants. Ink is excluded from the measurement: it is supposed to
be dark.

"Wrongness of paper" is reported in output levels: a paper pixel must come out 255,
so 0 is perfect and 105 is a shadow you can see.

    python3 tools/shadow_probe.py            # what ships today, per scenario
    python3 tools/shadow_probe.py variants   # candidate fixes side by side

The pipeline below is a line-by-line port of `DocumentFilters.buildPaperMap`
(`cellRadius`, `meanPool`, `windowOp`, `boxBlur3`) and of
`classifiesAsDocument`, so a variant that wins here is one that wins on the device.
Nothing in this file is used by the app.
"""

from __future__ import annotations

import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import filter_ref as fr  # noqa: E402  (sample_q8, rnd_q8 and the Q8 conventions)

# --------------------------------------------------------------- pipeline port ---

CELL_PIXELS_PER_RADIUS = 8000.0
CELL_MIN_RADIUS = 14
PAPER_CLOSE_DIVISOR = 40
PAPER_OPEN_MIN_RADIUS = 1
PAPER_EPSILON = 4


def cell_radius(w: int, h: int) -> int:
    return max(CELL_MIN_RADIUS, int(math.sqrt((w * h) / CELL_PIXELS_PER_RADIUS)))


def paper_radius(gw: int, gh: int) -> int:
    return max(PAPER_OPEN_MIN_RADIUS, max(gw, gh) // PAPER_CLOSE_DIVISOR)


def mean_pool(pixels, w: int, h: int, cell: int, gw: int, gh: int):
    out = [[0.0] * (gw * gh) for _ in range(3)]
    for gy in range(gh):
        y0 = min(h - 1, gy * cell)
        y1 = min(h, y0 + cell)
        for gx in range(gw):
            x0 = min(w - 1, gx * cell)
            x1 = min(w, x0 + cell)
            sr = sg = sb = 0
            count = 0
            for y in range(y0, y1):
                row = y * w
                for x in range(x0, x1):
                    p = pixels[row + x]
                    sr += (p >> 16) & 0xFF
                    sg += (p >> 8) & 0xFF
                    sb += p & 0xFF
                    count += 1
            index = gy * gw + gx
            if count:
                out[0][index] = sr / count
                out[1][index] = sg / count
                out[2][index] = sb / count
    return out


def window_op(src, w: int, h: int, radius: int, dilate: bool):
    if radius <= 0:
        return src
    horizontal = [0.0] * len(src)
    for y in range(h):
        row = y * w
        for x in range(w):
            value = 0.0 if dilate else 255.0
            for dx in range(-radius, radius + 1):
                s = src[row + min(w - 1, max(0, x + dx))]
                value = max(value, s) if dilate else min(value, s)
            horizontal[row + x] = value
    out = [0.0] * len(src)
    for y in range(h):
        for x in range(w):
            value = 0.0 if dilate else 255.0
            for dy in range(-radius, radius + 1):
                s = horizontal[min(h - 1, max(0, y + dy)) * w + x]
                value = max(value, s) if dilate else min(value, s)
            out[y * w + x] = value
    return out


def box_blur3(src, w: int, h: int):
    horizontal = [0.0] * len(src)
    for y in range(h):
        row = y * w
        for x in range(w):
            centre = src[row + x]
            left = src[row + x - 1] if x > 0 else centre
            right = src[row + x + 1] if x < w - 1 else centre
            horizontal[row + x] = (left + centre + right) / 3.0
    out = [0.0] * len(src)
    for y in range(h):
        for x in range(w):
            centre = horizontal[y * w + x]
            up = horizontal[(y - 1) * w + x] if y > 0 else centre
            down = horizontal[(y + 1) * w + x] if y < h - 1 else centre
            out[y * w + x] = (up + centre + down) / 3.0
    return out


def median3(src, w: int, h: int):
    out = [0.0] * len(src)
    for y in range(h):
        for x in range(w):
            window = []
            for dy in (-1, 0, 1):
                row = min(h - 1, max(0, y + dy)) * w
                for dx in (-1, 0, 1):
                    window.append(src[row + min(w - 1, max(0, x + dx))])
            window.sort()
            out[y * w + x] = window[4]
    return out


def percentiles(src, w: int, h: int, radius: int):
    """Per-cell high percentile of the grid: the candidate edge-preserving estimator."""
    out = [0.0] * len(src)
    for y in range(h):
        for x in range(w):
            window = []
            for dy in range(-radius, radius + 1):
                row = min(h - 1, max(0, y + dy)) * w
                for dx in range(-radius, radius + 1):
                    window.append(src[row + min(w - 1, max(0, x + dx))])
            window.sort()
            out[y * w + x] = window[int(len(window) * 0.85)]
    return out


# --------------------------------------------------------------- the variants ---
#
# `smooth` is the only thing that differs between the candidates: everything else is
# the shipped pipeline. "box3" is what the app runs today.

def smooth(grid, gw, gh, kind):
    if kind == "box3":
        for _ in range(3):
            grid = box_blur3(grid, gw, gh)
        return grid
    if kind == "median2":
        return median3(median3(grid, gw, gh), gw, gh)
    if kind == "median2box1":
        grid = median3(median3(grid, gw, gh), gw, gh)
        return box_blur3(grid, gw, gh)
    if kind == "box3median1":
        for _ in range(3):
            grid = box_blur3(grid, gw, gh)
        return median3(grid, gw, gh)
    if kind == "none":
        return grid
    raise ValueError(kind)


def build_grid(pixels, w: int, h: int, kind="box3", close_scale=1):
    """`DocumentFilters.buildPaperMap`, with the smoothing swappable."""
    cell = cell_radius(w, h) + 1
    gw = max(1, (w + cell - 1) // cell)
    gh = max(1, (h + cell - 1) // cell)
    pooled = mean_pool(pixels, w, h, cell, gw, gh)
    radius = max(PAPER_OPEN_MIN_RADIUS, paper_radius(gw, gh) * close_scale)
    grids = []
    for channel in range(3):
        plane = pooled[channel]
        plane = window_op(plane, gw, gh, radius, True)
        plane = window_op(plane, gw, gh, radius, False)
        grids.append(smooth(plane, gw, gh, kind))
    return grids, gw, gh, cell


def quantise(grids):
    return [[int(v * 256 + 0.5) if v > 0 else 0 for v in plane] for plane in grids]


# ------------------------------------------------------------------ the scenes ---
#
# Each scene returns (pixels, paper_mask, w, h, edge_x). `paper_mask` marks paper
# rather than ink, because only paper can be judged: ink is supposed to be dark.

INK = 0.18        # ink reflectance, relative to the paper beside it


def render(w, h, paper_at):
    pixels = [0] * (w * h)
    mask = bytearray(w * h)
    for y in range(h):
        in_stroke = (y % 24) in (3, 4, 5, 6, 7, 8)   # 6 px stroke band, 24 px pitch
        in_line = (y // 24) % 2 == 0                  # alternate text and blank lines
        for x in range(w):
            pr, pg, pb = paper_at(x, y)
            if in_stroke and in_line and (x % 104) >= 14:
                r, g, b = int(pr * INK), int(pg * INK), int(pb * INK)
            else:
                r, g, b = int(pr), int(pg), int(pb)
                mask[y * w + x] = 1
            pixels[y * w + x] = (0xFF << 24) | (r << 16) | (g << 8) | b
    return pixels, mask


W, H = 900, 675


def _flat():
    return (*render(W, H, lambda x, y: (235, 235, 233)), W, H, None)


def _hard(level):
    edge = int(W * 0.45)

    def paper_at(x, y):
        if x < edge:
            return (236, 236, 234)
        return (level, level + 4, level + 12)     # slightly cool, like a real shadow

    pixels, mask = render(W, H, paper_at)
    return pixels, mask, W, H, edge


def _glare():
    """A page with a lamp reflection *and* a hand shadow: the hard combination.

    A wide, near-clipped highlight is the case that could break a gate keyed on
    "nothing is brighter than the surface", because a reflection genuinely is.
    """
    edge = int(W * 0.62)

    def paper_at(x, y):
        glow = 52 * math.exp(-(((x - W * 0.25) ** 2 + (y - H * 0.35) ** 2) / (2 * 150.0 ** 2)))
        if x > edge:
            base = 74
            return (min(255, base + glow * 0.4), min(255, base + 4 + glow * 0.4),
                    min(255, base + 12 + glow * 0.4))
        return (min(255, 236 + glow), min(255, 236 + glow), min(255, 234 + glow))

    pixels, mask = render(W, H, paper_at)
    return pixels, mask, W, H, edge


def _photo(pixels, w, h):
    return pixels, bytearray(b"\x01" * (w * h)), w, h, None


def _landscape():
    """Sky, horizon, a building, a tree — the scene the classifier must refuse."""
    pixels = []
    for y in range(H):
        for x in range(W):
            if y < H * 0.45:
                t = y / (H * 0.45)
                r, g, b = 150 + 50 * t, 180 + 35 * t, 232 + 8 * t
            else:
                t = (y - H * 0.45) / (H * 0.55)
                r, g, b = 92 - 32 * t, 112 - 42 * t, 62 - 22 * t
            if W * 0.6 < x < W * 0.95 and H * 0.3 < y < H * 0.8:
                r, g, b = 118, 116, 122
                if (x // 22) % 2 == 0 and (y // 22) % 2 == 0 and y < H * 0.72:
                    r, g, b = 62, 64, 74
            if ((x - W * 0.2) ** 2 / 1.0 + (y - H * 0.55) ** 2) < 90 ** 2:
                r, g, b = 46, 78, 38
            pixels.append((0xFF << 24) | (int(r) << 16) | (int(g) << 8) | int(b))
    return _photo(pixels, W, H)


def _room():
    """A lit room: beige wall, wooden floor, a bright window, a dark sofa."""
    pixels = []
    for y in range(H):
        for x in range(W):
            if y > H * 0.68:
                t = (y - H * 0.68) / (H * 0.32)
                r, g, b = 104 - 30 * t, 86 - 26 * t, 66 - 20 * t
            else:
                t = y / (H * 0.68)
                r, g, b = 196 - 26 * t, 186 - 26 * t, 172 - 24 * t
            # window
            if W * 0.12 < x < W * 0.42 and H * 0.12 < y < H * 0.55:
                r, g, b = 246, 246, 242
                if abs(x - W * 0.27) < 4 or abs(y - H * 0.335) < 4:
                    r, g, b = 150, 150, 148
            # sofa
            if W * 0.52 < x < W * 0.94 and H * 0.5 < y < H * 0.78:
                r, g, b = 64, 58, 62
            pixels.append((0xFF << 24) | (int(r) << 16) | (int(g) << 8) | int(b))
    return _photo(pixels, W, H)


_SCENES = {
    # No shadow at all: the control. Any change here is a regression.
    "flat": _flat,
    # A crisp edge over the right 55 % at 3.2x: the phone's body, a hand, a frame.
    "hard_shadow": lambda: _hard(74),
    # 8x: a page lit by a window with the phone's body across it.
    "hard_shadow_deep": lambda: _hard(30),
    # A gradient, 236 -> 94 across the page: the easy case.
    "soft_shadow": lambda: (*render(
        W, H, lambda x, y: (lambda p: (p, p, p + 1))(236 - 142 * (x / (W - 1)))
    ), W, H, None),
    # No shadow, just a dim page at 30 % of white.
    "dim_room": lambda: (*render(W, H, lambda x, y: (70, 71, 72)), W, H, None),
    # Radial falloff to 45 % in the corners.
    "vignette": lambda: (*render(W, H, lambda x, y: (
        lambda k: (236 * k, 236 * k, 234 * k)
    )(1.0 - 0.25 * (((x - W / 2) / (W / 2)) ** 2 + ((y - H / 2) / (H / 2)) ** 2))), W, H, None),
    # The two things the document bounds must never be applied to.
    # A lamp reflection plus a hand shadow.
    "glare": _glare,
    # The two things the document bounds must never be applied to.
    "photo_landscape": _landscape,
    "photo_room": _room,
}

PAGE_SCENES = ["flat", "hard_shadow", "hard_shadow_deep", "soft_shadow", "dim_room", "vignette", "glare"]
PHOTO_SCENES = ["photo_landscape", "photo_room"]

SCENES = list(_SCENES)
_cache: dict = {}


def scene(name):
    if name not in _cache:
        _cache[name] = _SCENES[name]()
    return _cache[name]


# ---------------------------------------------------------------- the classifier ---
#
# `DocumentFilters.classifiesAsDocument`, ported exactly (including the stride and
# the 98th-percentile normalisation).

DOC_MIN_R, DOC_MIN_G, DOC_MIN_B = 80, 80, 70
PHOTO_MIN_R, PHOTO_MIN_G, PHOTO_MIN_B = 60, 100, 120
DOC_MAX_RB_DIFF = 30
CHROMA_TOLERANCE = 40
CLASSIFICATION_RATIO = 60.0
CLASSIFY_LUMA_TARGET = 240.0
CLASSIFY_MAX_GAIN = 8.0


def classify(pixels, w, h):
    stride = max(1, int(math.ceil(math.sqrt(len(pixels) / 10_000.0))))
    hist = [0] * 256
    samples = 0
    for index in range(0, len(pixels), stride):
        p = pixels[index]
        hist[(((p >> 16) & 0xFF) + ((p >> 8) & 0xFF) + (p & 0xFF)) // 3] += 1
        samples += 1
    cutoff = int(samples * 0.98)
    seen = 0
    peak = 255
    for bin_ in range(256):
        seen += hist[bin_]
        if seen > cutoff:
            peak = bin_
            break
    gain = min(CLASSIFY_MAX_GAIN, max(1.0, CLASSIFY_LUMA_TARGET / max(1, peak)))
    docs = photos = total = 0
    for index in range(0, len(pixels), stride):
        p = pixels[index]
        r = int(min(255, ((p >> 16) & 0xFF) * gain + 0.5))
        g = int(min(255, ((p >> 8) & 0xFF) * gain + 0.5))
        b = int(min(255, (p & 0xFF) * gain + 0.5))
        if (r >= DOC_MIN_R and g >= DOC_MIN_G and b >= DOC_MIN_B
                and (r - b) <= DOC_MAX_RB_DIFF
                and (b - CHROMA_TOLERANCE) <= g <= (r + CHROMA_TOLERANCE)):
            docs += 1
        if r >= PHOTO_MIN_R and g >= PHOTO_MIN_G and b >= PHOTO_MIN_B and r <= g <= b:
            photos += 1
        total += 1
    doc_ratio = docs * 100.0 / total
    photo_ratio = photos * 100.0 / total
    return (doc_ratio > CLASSIFICATION_RATIO or photo_ratio > CLASSIFICATION_RATIO), doc_ratio, photo_ratio


# --------------------------------------------------------------------- the run ---

DOC_KNEE, DOC_CAP, DOC_FLOOR = 8.0, 24.0, 56
SCENE_KNEE, SCENE_CAP, SCENE_FLOOR = 1.2, 2.0, 128


def white_balance(pixels, mask, w, h, grids, gw, gh, cell, knee, cap, floor, step=2):
    """Per-pixel `c2` for paper pixels only — the stage that decides paper = 255.

    Errors are returned as a flat list of `255 - c2` (0 is correct), so they can be
    compared against the same unit used everywhere else in this repository.
    """
    plane_r, plane_g, plane_b = quantise(grids)
    knee_q8, cap_q8 = int(knee * 256), int(cap * 256)
    errors = []
    for y in range(0, h, step):
        row = y * w
        for x in range(0, w, step):
            i = row + x
            if not mask[i]:
                continue
            p = pixels[i]
            r = (p >> 16) & 0xFF
            g = (p >> 8) & 0xFF
            b = p & 0xFF
            pr = max(1, fr.sample_q8(plane_r, gw, gh, cell, x, y))
            pg = max(1, fr.sample_q8(plane_g, gw, gh, cell, x, y))
            pb = max(1, fr.sample_q8(plane_b, gw, gh, cell, x, y))
            out = []
            for c, paper in ((r, pr), (g, pg), (b, pb)):
                gain = fr.knee_gain((255 * 256 * 256) // paper, knee_q8, cap_q8)
                if gain < floor:
                    gain = floor
                out.append(min(255, fr.rnd_q8(c * gain)))
            errors.append(255 - max(out))
    return errors


def ratio_bands(name, close_scale=1, kind="box3", step=2):
    """Where the frame's pixels sit relative to its own estimated surface.

    A page is paper (ratio ~1) plus ink (well below). A photograph is a *scene*: its
    estimate follows the scene's brightness, so there is mass on both sides — above
    the estimate (a window against a wall, a lamp against a ceiling) and far below it
    (furniture in shadow). That asymmetry is what a document/scene gate can use, and
    it survives any amount of illumination change, which an absolute threshold does
    not.
    """
    pixels, mask, w, h, edge = scene(name)
    grids, gw, gh, cell = build_grid(pixels, w, h, kind, close_scale)
    plane_r, plane_g, plane_b = quantise(grids)
    bands = {"paper": 0, "lighter": 0, "much_lighter": 0, "ink": 0, "total": 0}
    for y in range(0, h, step):
        row = y * w
        for x in range(0, w, step):
            i = row + x
            p = pixels[i]
            luma = (299 * ((p >> 16) & 0xFF) + 587 * ((p >> 8) & 0xFF) + 114 * (p & 0xFF)) // 1000
            pr = fr.sample_q8(plane_r, gw, gh, cell, x, y)
            pg = fr.sample_q8(plane_g, gw, gh, cell, x, y)
            pb = fr.sample_q8(plane_b, gw, gh, cell, x, y)
            paper = (299 * pr + 587 * pg + 114 * pb) / 1000.0 / 256.0
            ratio = luma / max(1.0, paper)
            bands["total"] += 1
            if 0.90 <= ratio <= 1.05:
                bands["paper"] += 1
            elif ratio > 1.05:
                bands["lighter"] += 1
                if ratio > 1.3:
                    bands["much_lighter"] += 1
            if ratio < 0.5:
                bands["ink"] += 1
    total = max(1, bands["total"])
    return {k: v * 100.0 / total for k, v in bands.items() if k != "total"}, (gw, gh, cell)


def surface_stats(pixels, mask, w, h, grids, gw, gh, cell, step=3):
    """Statistics of the frame *relative to its own paper estimate*.

    These are the candidates for a shadow-robust document/scene decision:

    * `above` — the fraction of pixels brighter than the estimated surface. On a
      page nothing is brighter than paper, so this is ~0; in a photograph the
      estimate follows the scene's own brightness, so roughly half the pixels are
      above it. This is the statistic that separates "a lit surface" from "a place".
    * `paper_frac` — the fraction that is close to the surface level, which is the
      paper area of a page and is large whatever the illumination.
    * `range` — largest / smallest cell of the grid, i.e. how much the light varies
      across the frame. A hand shadow is a factor of 2-8; a photo can be anything.
    """
    plane_r, plane_g, plane_b = quantise(grids)
    above = 0
    paperish = 0
    total = 0
    for y in range(0, h, step):
        row = y * w
        for x in range(0, w, step):
            i = row + x
            if not mask[i]:
                continue
            p = pixels[i]
            luma = (299 * ((p >> 16) & 0xFF) + 587 * ((p >> 8) & 0xFF) + 114 * (p & 0xFF)) // 1000
            pr = fr.sample_q8(plane_r, gw, gh, cell, x, y) / 256.0
            pg = fr.sample_q8(plane_g, gw, gh, cell, x, y) / 256.0
            pb = fr.sample_q8(plane_b, gw, gh, cell, x, y) / 256.0
            paper = (299 * pr + 587 * pg + 114 * pb) / 1000.0
            total += 1
            if luma > paper * 1.05:
                above += 1
            if luma > paper * 0.75:
                paperish += 1
    lo = min(min(plane) for plane in grids)
    hi = max(max(plane) for plane in grids)
    return {
        "above": above * 100.0 / max(1, total),
        "paper_frac": paperish * 100.0 / max(1, total),
        "range": hi / max(1.0, lo),
    }


def repaint(pixels, w, h, grids, gw, gh, cell, knee_scale=1.0, step=3):
    """Fraction of originally-dark pixels pushed to near-white — the photo's verdict.

    A page has almost none of these (its ink is dark and stays dark); a scene that
    has been divided by its own brightness has most of its shadows gone and its dark
    furniture bleached, which is exactly the artefact the scene bounds exist to
    prevent.
    """
    plane_r, plane_g, plane_b = quantise(grids)
    knee_q8, cap_q8 = int(DOC_KNEE * knee_scale * 256), int(DOC_CAP * 256)
    bleached = 0
    dark = 0
    for y in range(0, h, step):
        row = y * w
        for x in range(0, w, step):
            i = row + x
            p = pixels[i]
            luma = (299 * ((p >> 16) & 0xFF) + 587 * ((p >> 8) & 0xFF) + 114 * (p & 0xFF)) // 1000
            if luma >= 150:
                continue
            dark += 1
            out = 0
            for c, paper in (
                ((p >> 16) & 0xFF, fr.sample_q8(plane_r, gw, gh, cell, x, y)),
                ((p >> 8) & 0xFF, fr.sample_q8(plane_g, gw, gh, cell, x, y)),
                (p & 0xFF, fr.sample_q8(plane_b, gw, gh, cell, x, y)),
            ):
                gain = fr.knee_gain((255 * 256 * 256) // max(1, paper), knee_q8, cap_q8)
                if gain < DOC_FLOOR:
                    gain = DOC_FLOOR
                out = max(out, min(255, fr.rnd_q8(c * gain)))
            if out > 200:
                bleached += 1
    return {"dark_frac": dark * 100.0 / max(1, (h // step) * (w // step)),
            "bleached": bleached * 100.0 / max(1, dark)}


def gate_report():
    """Every candidate statistic, on pages and on photos, in one table."""
    print("| scene | family | classifier says | docRatio | above | near-surface | light range |")
    print("|---|---|---|---|---|---|---|")
    for name in SCENES:
        pixels, mask, w, h, edge = scene(name)
        verdict, doc_ratio, photo_ratio = classify(pixels, w, h)
        grids, gw, gh, cell = build_grid(pixels, w, h)
        st = surface_stats(pixels, mask, w, h, grids, gw, gh, cell)
        family = "photo" if name.startswith("photo") else "page"
        print(f"| {name} | {family} | {'document' if verdict else 'scene'} | "
              f"{doc_ratio:.0f} % | {st['above']:.1f} % | {st['paper_frac']:.0f} % | "
              f"{st['range']:.1f}x |")
    print()
    print("What the document bounds would cost a photograph (bleached = originally-dark")
    print("pixels pushed above 200), at several knee limits:")
    print("| scene | knee 2x | knee 4x | knee 8x | knee 16x |")
    print("|---|---|---|---|---|")
    for name in SCENES:
        pixels, mask, w, h, edge = scene(name)
        grids, gw, gh, cell = build_grid(pixels, w, h)
        row = []
        for scale in (0.25, 0.5, 1.0, 2.0):
            r = repaint(pixels, w, h, grids, gw, gh, cell, scale)
            row.append(f"{r['bleached']:.0f} %")
        print(f"| {name} | " + " | ".join(row) + " |")


SURFACE_BRIGHT_PERCENT = 3.0
SURFACE_MARKED_PERCENT = 8.0
PAPER_MEDIAN_PASSES = 2


def gate_stats(pixels, w, h, grids, gw, gh, cell, step=2):
    """The two statistics the document/scene gate is built from.

    * `brighter` — pixels above their own estimated surface by more than 5 %. On a page
      the only content is *darker* than the paper, so the closing recovers the paper
      exactly and almost nothing exceeds it. In a photograph content sits on both sides
      of the local estimate, so every content boundary contributes above-estimate
      pixels. Measured: pages 0.0-1.8 %, photographs 4.2-5.3 %.
    * `marked` — pixels below three quarters of their surface, i.e. local markings that
      are not the surface itself. Pages 10.7-13.6 %, photographs 1.2-4.5 %. This is the
      safety term: it rescues a page with an unusually bright specular region, which is
      the one thing that can inflate `brighter` on a real page.
    """
    plane_r, plane_g, plane_b = quantise(grids)
    brighter = 0
    marked = 0
    sampled = 0
    for y in range(0, h, step):
        row = y * w
        for x in range(0, w, step):
            p = pixels[row + x]
            luma = fr.luma_fixed((p >> 16) & 0xFF, (p >> 8) & 0xFF, p & 0xFF)
            paper = (299 * fr.sample_q8(plane_r, gw, gh, cell, x, y)
                     + 587 * fr.sample_q8(plane_g, gw, gh, cell, x, y)
                     + 114 * fr.sample_q8(plane_b, gw, gh, cell, x, y)) / 1000.0
            sampled += 1
            if luma * 256 * 100 > paper * 105:
                brighter += 1
            if luma * 256 * 4 < paper * 3:
                marked += 1
    n = max(1, sampled)
    return brighter * 100.0 / n, marked * 100.0 / n


def brighter_fraction(*args, **kwargs):
    return gate_stats(*args, **kwargs)[0]


def run(name, kind="median2", close_scale=1, knee_scale=1.0, force=None, step=2, gate="new"):
    """One scenario, end to end, in the white-balance stage only.

    `gate` selects how the document/scene decision is made:
    `"new"` is `DocumentFilters.surfaceIsPaper` (relative to the surface),
    `"classifier"` is `DocumentFilters.classifiesAsDocument` (absolute, shipped in R61).
    """
    pixels, mask, w, h, edge = scene(name)
    verdict, doc_ratio, photo_ratio = classify(pixels, w, h)
    grids, gw, gh, cell = build_grid(pixels, w, h, kind, close_scale)
    brighter, marked = gate_stats(pixels, w, h, grids, gw, gh, cell, step)

    if force is not None:
        use_doc = force
    elif gate == "new":
        use_doc = brighter < SURFACE_BRIGHT_PERCENT or marked > SURFACE_MARKED_PERCENT
    else:
        use_doc = verdict

    if use_doc:
        knee, cap, floor = DOC_KNEE * knee_scale, DOC_CAP, DOC_FLOOR
    else:
        knee, cap, floor = SCENE_KNEE, SCENE_CAP, SCENE_FLOOR

    plane_r, plane_g, plane_b = quantise(grids)
    knee_q8, cap_q8 = int(knee * 256), int(cap * 256)
    buckets = {"all": [], "lit": [], "shadow": [], "band": []}
    for y in range(0, h, step):
        row = y * w
        for x in range(0, w, step):
            i = row + x
            if not mask[i]:
                continue
            p = pixels[i]
            pr = max(1, fr.sample_q8(plane_r, gw, gh, cell, x, y))
            pg = max(1, fr.sample_q8(plane_g, gw, gh, cell, x, y))
            pb = max(1, fr.sample_q8(plane_b, gw, gh, cell, x, y))
            worst = 0
            for c, paper in (((p >> 16) & 0xFF, pr), ((p >> 8) & 0xFF, pg), (p & 0xFF, pb)):
                gain = fr.knee_gain((255 * 256 * 256) // paper, knee_q8, cap_q8)
                if gain < floor:
                    gain = floor
                worst = max(worst, 255 - min(255, fr.rnd_q8(c * gain)))
            buckets["all"].append(worst)
            if edge is None:
                buckets["lit"].append(worst)
            elif x < edge - 3 * cell:
                buckets["lit"].append(worst)
            elif x > edge + 3 * cell:
                buckets["shadow"].append(worst)
            else:
                buckets["band"].append(worst)

    def stat(values):
        if not values:
            return None
        return (sum(values) / len(values), max(values), len(values))

    return {
        "lighter": brighter,
        "marked": marked,
        "scene": name,
        "kind": kind,
        "close_scale": close_scale,
        "knee_scale": knee_scale,
        "forced": force,
        "verdict": verdict,
        "gate": gate,
        "mode": "document" if use_doc else "SCENE",
        "doc_ratio": doc_ratio,
        "photo_ratio": photo_ratio,
        "all": stat(buckets["all"]),
        "lit": stat(buckets["lit"]),
        "shadow": stat(buckets["shadow"]),
        "band": stat(buckets["band"]),
        "cell": cell,
        "grid": (gw, gh),
    }


def fmt(stat):
    if stat is None:
        return "        -        "
    mean, worst, count = stat
    return f"{mean:5.1f} / {worst:3d} (n={count})"


def report(results):
    print("| scene | classifier says | bounds used | all paper: mean / worst | lit | shadowed | edge band | gate: brighter / marked |")
    print("|---|---|---|---|---|---|---|---|")
    for r in results:
        verdict = f"doc {r['doc_ratio']:.0f} %" if r["verdict"] else f"scene {r['doc_ratio']:.0f} %"
        print(f"| {r['scene']} | {verdict} | {r['mode']} | {fmt(r['all'])} | {fmt(r['lit'])} | "
              f"{fmt(r['shadow'])} | {fmt(r['band'])} | {r['lighter']:.1f} % / {r['marked']:.1f} % |")


VARIANTS = [
    ("box3", 1, 1.0, "R61: box x3"),
    ("median2", 1, 1.0, "R62: median x2"),
    ("median2box1", 1, 1.0, "median x2 + box x1"),
    ("box3median1", 1, 1.0, "box x3 + median x1"),
    ("median2box1", 4, 1.0, "median x2 + box, closing r x4"),
    ("median2box1", 4, 2.0, "... and knee 16x"),
    ("median2box1", 4, 4.0, "... and knee 32x"),
]


def main(argv) -> int:
    command = argv[1] if len(argv) > 1 else "current"
    if command == "current":
        print("As shipped in R61: absolute-threshold classifier picks the bounds,")
        print("box-blurred surface. A paper pixel must come out 255, so 0 is correct")
        print("and 105 is a visible shadow.\n")
        report([run(name, kind="box3", gate="classifier") for name in PAGE_SCENES])
        print("\nThat gate applied to photographs — it must say *scene*, or the divide")
        print("repaints them:\n")
        report([run(name, kind="box3", gate="classifier") for name in PHOTO_SCENES])
        print("\n---\n\nNow: relative gate (`surfaceIsPaper`) and the edge-preserving")
        print("surface.\n")
        report([run(name) for name in PAGE_SCENES])
        print("\n... and the same gate on photographs, which is the property that makes")
        print("it safe:\n")
        report([run(name) for name in PHOTO_SCENES])
        return 0

    if command == "variants":
        for name in PAGE_SCENES:
            print(f"\n### {name} (document bounds forced)\n")
            print("| variant | all paper | lit | shadowed | edge band | gate: brighter / marked |")
            print("|---|---|---|---|---|---|")
            for kind, scale, knee_scale, label in VARIANTS:
                r = run(name, kind, scale, knee_scale, force=True)
                print(f"| {label} | {fmt(r['all'])} | {fmt(r['lit'])} | {fmt(r['shadow'])} | "
                      f"{fmt(r['band'])} | {r['lighter']:.1f} % / {r['marked']:.1f} % |")
        return 0

    if command == "gate":
        gate_report()
        return 0

    if command == "bands":
        for scale in (1, 2, 4, 6):
            print(f"\n### relative to the surface, closing radius x{scale}\n")
            print("| scene | paper 0.90-1.05 | lighter >1.05 | much lighter >1.3 | ink <0.5 |")
            print("|---|---|---|---|---|")
            for name in SCENES:
                b, _ = ratio_bands(name, close_scale=scale)
                print(f"| {name} | {b['paper']:.0f} % | {b['lighter']:.1f} % | "
                      f"{b['much_lighter']:.1f} % | {b['ink']:.0f} % |")
        return 0

    print(f"usage: {argv[0]} [current|variants|gate|bands]", file=sys.stderr)
    return 2


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
