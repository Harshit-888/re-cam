#!/usr/bin/env python3
"""Why the background is still not white, and what to do about it — measured.

`tools/shadow_probe.py` measures the *surface estimator* on paper-only scenes.
`tools/filter_ref.py` measures the *kernel* on uniform paper. Neither can answer the
question actually being asked:

    "Smart Scan is still not clean in shadows, Light Text darkens the whole page, and
     the text is not sharp."

Those are three failures of the **whole chain**, and they interact:

1. **The surface is biased by the ink it is meant to ignore.** `meanPool` averages
   every pixel in a cell, ink included, and the closing afterwards can only remove
   ink *narrower than its window*. On a dark, dense region — or next to a page edge,
   or across a strong shadow — the cell mean is pulled toward the ink, the estimated
   "paper" comes out too dark, the divide under-corrects, and the background stays
   grey. This is the shadow complaint, and it is why the five original presets
   shade dense text.
2. **Nothing clamps the background.** Even with a perfect surface, the curve maps
   paper to *almost* white, so a page keeps a low-frequency grey wash from exposure,
   vignetting, paper tint and residual surface error. "Least background disturbance"
   means the paper region should be **exactly** white, and it can be, because a page
   has a hard ceiling: paper is the brightest thing in the frame except a specular
   glint.
3. **Light Text amplifies its own background error.** Its curve starts at 0.78 of
   paper, so any residual background error of 10–20 % (which (1) guarantees) lands in
   the *steep* part of the curve and comes out as mid-grey — the whole page darkens,
   exactly as reported. The fix is not a gentler curve; it is a background that is
   actually white before the curve is applied.
4. **Sharpness is a separate, later stage.** The current edge boost is at most
   +22 % on the *gain*, applied before the clamp, gated on a Laplacian whose strength
   depends on the (unclamped) tone. After the clamp, edges are stronger and the boost
   should be applied *there* — with a noise gate, and only inward (darkening the ink
   side of an edge, never brightening paper past white, which would clip and ring).

This file implements the chain, not just the kernel, and reports:

| metric | meaning |
|---|---|
| `paper` | mean deviation of **paper** pixels from 255 (0 = perfectly white) |
| `worst` | the worst single paper pixel |
| `sd` | standard deviation of paper — the *disturbance*, not just its average |
| `shadow gap` | mean(lit paper) − mean(shadowed paper): non-zero = a visible shadow |
| `ink` | mean level of text pixels (lower = darker, punchier) |
| `faint` | mean level of the pencil mark (must stay **visible**, i.e. not white) |
| `halo` | darkest paper pixel within 2 px of a stroke edge, as a deviation |

Usage:

    python3 tools/ink_probe.py report          # today vs candidate, per scene
    python3 tools/ink_probe.py variants        # every candidate, all scenes
    python3 tools/ink_probe.py sharp           # the sharpening profile alone

Nothing in this file is used by the app; it is the specification for
`DocumentFilters.buildPaperMap` / `applyAdaptive` and `docs/filter_combined_spec.md`.
"""

from __future__ import annotations

import math
import sys

Q = 256
WHITE_Q8_OVER_PAPER = 255 * Q * Q

# Bounds on the fine/coarse correction. Set from `python3 tools/ink_probe.py bounds`.
FINE_RATIO_LOW, FINE_RATIO_HIGH = 0.85, 1.15
FINE_DIVISOR = 5
FINE_CLOSE_MULT = 1.0

# --------------------------------------------------------------- fixed point ---
# The kernel helpers are the same expressions as `tools/filter_ref.py`; imported
# rather than re-written so a change there cannot silently diverge from here.


def luma_fixed(r: int, g: int, b: int) -> int:
    return (r * 299 + g * 587 + b * 114 + 500) // 1000


def rnd_q8(v: int) -> int:
    return (v + 128) >> 8


def knee_gain(raw: int, knee: int, cap: int) -> int:
    if raw <= knee:
        return raw
    span = cap - knee
    if span <= 0:
        return cap
    over = raw - knee
    return knee + (span * over) // (span + over)


def ratio_curve(lo: float, hi: float, gamma: float):
    span = max(hi - lo, 1e-4)
    out = []
    for i in range(256):
        t = min(1.0, max(0.0, (i / 255.0 - lo) / span))
        out.append(int(255.0 * (t ** gamma) + 0.5))
    return out


# ------------------------------------------------------------------- the page ---
#
# One synthetic A4 under a light field, with four kinds of content. Everything is
# deterministic: a plain LCG for sensor noise, fixed geometry for the strokes.

W, H = 480, 360

PAPER_RGB = (238, 236, 231)      # warm white
PAPER_R = 1.0                    # by definition: everything else is relative to paper
INK_R = 0.16                     # body text, reflectance of paper
FAINT_R = 0.82                   # pencil / faded thermal / fax
BAR_R = 0.14                     # wide bars, for the edge profile

PAPER, INK, FAINT, BAR = 0, 1, 2, 3


def _lcg(seed):
    state = seed & 0xFFFFFFFF

    def nxt():
        nonlocal state
        state = (1103515245 * state + 12345) & 0x7FFFFFFF
        return state

    return nxt


def content_masks():
    """Which pixels are paper, body text, a faint mark, or one of four wide bars.

    Body text is a 6 px stroke band every 24 px vertically, with 14 px gaps — i.e.
    glyph-like coverage of about 20 % per text line, which is realistic for a dense
    page and is exactly what biases a 14 px cell mean.
    """
    kind = bytearray(W * H)
    bar_cols = [40, 140, 250, 360]
    bar_w = 10
    for y in range(H):
        in_stroke = (y % 24) in (4, 5, 6, 7, 8, 9)
        in_line = (y // 24) % 3 != 2
        for x in range(W):
            k = PAPER
            if in_stroke and in_line and (x % 40) < 26:
                k = INK
            if 250 <= y <= 253 and 30 <= x <= 300:
                k = FAINT            # a pencil line across the middle
            for bx in bar_cols:
                if bx <= x < bx + bar_w and 40 <= y <= 250:
                    k = BAR
            if 300 <= x < 380 and 270 <= y < 340:
                k = BAR                 # the bold block (see `block_scene`)
            kind[y * W + x] = k
    return kind, bar_cols, bar_w


KIND, BAR_COLS, BAR_W = content_masks()


def _clean_paper_mask(margin=3):
    """Paper with no content within `margin` px (Chebyshev).

    The page is rendered through a lens now (`optics`), so the two pixels either side
    of a stroke are *true* optical exposure, not background: they are mid-grey because
    the ink is partly there, and every strong tone curve is entitled to render them
    dark. Counting them as background would make the background metric a measure of the
    blur kernel. Every "is the paper white" number below is therefore taken on clean
    paper only — the pixels that must be 255.
    """
    clean = bytearray(W * H)
    dirty = [i for i in range(W * H) if KIND[i] != PAPER]
    mark = bytearray(W * H)
    for i in dirty:
        y, x = divmod(i, W)
        for dy in range(-margin, margin + 1):
            yy = y + dy
            if 0 <= yy < H:
                base = yy * W
                for dx in range(-margin, margin + 1):
                    xx = x + dx
                    if 0 <= xx < W:
                        mark[base + xx] = 1
    for i in range(W * H):
        if KIND[i] == PAPER and not mark[i]:
            clean[i] = 1
    return clean


CLEAN = _clean_paper_mask()


def _distance_to_content(limit=5):
    """Chebyshev distance from each paper pixel to the nearest content pixel."""
    dist = bytearray([limit + 1]) * (W * H)
    for i in range(W * H):
        if KIND[i] != PAPER:
            dist[i] = 0
    for step in range(1, limit + 1):
        prev = bytes(dist)
        for y in range(H):
            for x in range(W):
                i = y * W + x
                if prev[i] < step:
                    continue
                best = limit + 1
                for dy in (-1, 0, 1):
                    yy = y + dy
                    if 0 <= yy < H:
                        base = yy * W
                        for dx in (-1, 0, 1):
                            xx = x + dx
                            if 0 <= xx < W and prev[base + xx] < best:
                                best = prev[base + xx]
                dist[i] = best + 1
    return dist


DIST = _distance_to_content()
# The shell: paper one pixel beyond the optical reach. Sharpening that rings shows up
# here as a darkening that clean paper does not have; sharpening that does not ring
# leaves it alone.
SHELL = bytearray(1 if (KIND[i] == PAPER and DIST[i] == 3) else 0 for i in range(W * H))


def draw(scene) -> dict:
    """Renders one scene: light field x tint x content, plus sensor noise."""
    light, tint, seed = scene
    noise = _lcg(seed)
    pixels = [0] * (W * H)
    lit = bytearray(W * H)          # 1 where the shadow-free side is
    for y in range(H):
        for x in range(W):
            i = y * W + x
            l, is_lit = light(x, y)
            tr, tg, tb = tint(x, y)
            k = KIND[i]
            refl = PAPER_R if k == PAPER else INK_R if k == INK else FAINT_R if k == FAINT else BAR_R
            n = (noise() % 5) - 2
            r = int(PAPER_RGB[0] * l * tr * refl) + n
            g = int(PAPER_RGB[1] * l * tg * refl) + n
            b = int(PAPER_RGB[2] * l * tb * refl) + n
            pixels[i] = (0xFF << 24) | (max(0, min(255, r)) << 16) | (max(0, min(255, g)) << 8) | max(0, min(255, b))
            lit[i] = 1 if is_lit else 0
    return {"pixels": pixels, "lit": lit}


def _flat_light(x, y):
    return 1.0, True


def _grad_light(x, y):
    return 1.0 - 0.62 * (x / (W - 1)), x < W * 0.5


def _step_light(knee=0.30):
    """A crisp shadow over the right 40 %, with a 4 px penumbra — a hand or phone."""
    edge = W * 0.60

    def light(x, y):
        if x < edge - 2:
            return 1.0, True
        if x > edge + 2:
            return knee, False
        t = (x - (edge - 2)) / 4.0
        return 1.0 + (knee - 1.0) * t, x < edge

    return light


def _vignette_light(x, y):
    r2 = ((x - W / 2) / (W / 2)) ** 2 + ((y - H / 2) / (H / 2)) ** 2
    return 1.0 - 0.34 * r2, r2 < 0.5


def _glare_light(x, y):
    glow = 0.30 * math.exp(-(((x - W * 0.22) ** 2 + (y - H * 0.3) ** 2) / (2 * 90.0 ** 2)))
    over = x > W * 0.62
    base = (0.34 if over else 1.0) + glow
    return min(1.0, base), not over


def _warm_tint(x, y):
    return 1.0, 0.99, 0.95


def _lamp_tint(x, y):
    # A warm lamp: the blue channel falls fastest, which is what a tint cast *is*.
    fall = 0.25 * (x / W)
    return 1.0, 1.0 - 0.5 * fall, 1.0 - fall


def _block_scene():
    """A page with a solid dark block — a filled heading, a stamp, a pasted photo.

    The closing exists for exactly this: a cell that is *entirely* ink has no paper in
    it to average, so without a morphological step the estimated surface becomes the
    ink's own level, the divide multiplies by 6, and the block comes out white. Any
    estimator that drops the closing has to survive this scene.
    """
    return _flat_light, _warm_tint, 88


SCENES = {
    "flat": ((_flat_light), _warm_tint, 11),
    # A solid block of ink: no paper inside a cell, so the surface has to find paper
    # *outside* it or the block turns white.
    "bold_block": (_block_scene()[0], _block_scene()[1], _block_scene()[2]),
    "gradient": ((_grad_light), _warm_tint, 22),
    "hand_shadow": ((_step_light(0.30)), _warm_tint, 33),
    "vignette": ((_vignette_light), _warm_tint, 44),
    "glare": ((_glare_light), _warm_tint, 55),
    "lamp_cast": ((_flat_light), _lamp_tint, 66),
    "shadow_cast": ((_step_light(0.42)), _lamp_tint, 77),
}

_cache: dict = {}

# ------------------------------------------------------------------- the optics ---
#
# A phone camera does not deliver a hard edge. The lens PSF, the demosaic and the
# resize to the working size all spread a stroke edge over two or three pixels, and
# that is precisely what "sharpen" means to undo. A synthetic page drawn with hard
# edges has a 1 px transition, so 10-90 % edge width measures 0.0 px on it and no
# sharpening can change it — the first version of this probe reported exactly that,
# and it read as "sharpening does nothing" when it was the page that could not show it.
#
# [1 4 6 4 1]/16, separable, sigma ~ 1.0 px, applied to every scene once. Noise is
# smoothed with it, the same way demosaic smooths it.
OPTICS = True
_TAPS = (1, 4, 6, 4, 1)


def _blur_pass(src, w, h, horizontal):
    out = [0] * len(src)
    for i in range(len(src)):
        r = g = b = 0
        for k, t in enumerate(_TAPS):
            o = (k - 2) * (1 if horizontal else w)
            j = i + o
            if horizontal:
                if (i % w) + (k - 2) < 0 or (i % w) + (k - 2) >= w:
                    j = i
            else:
                if (i // w) + (k - 2) < 0 or (i // w) + (k - 2) >= h:
                    j = i
            p = src[j]
            r += t * ((p >> 16) & 0xFF)
            g += t * ((p >> 8) & 0xFF)
            b += t * (p & 0xFF)
        out[i] = (0xFF << 24) | ((r >> 4) << 16) | ((g >> 4) << 8) | (b >> 4)
    return out


def optics(pixels, w, h):
    if not OPTICS:
        return pixels
    return _blur_pass(_blur_pass(pixels, w, h, True), w, h, False)


def scene(name) -> dict:
    if name not in _cache:
        light, tint, seed = SCENES[name]
        data = draw((light, tint, seed))
        data["pixels"] = optics(data["pixels"], W, H)
        _cache[name] = data
    return _cache[name]


# ------------------------------------------------------------- surface builder ---


def cell_radius(w: int, h: int) -> int:
    """Cell side, in pixels.

    The device builds this from `CELL_PIXELS_PER_RADIUS = 8000` with a floor of 14,
    which on a 3200x2400 page is a 32 px cell — **1 % of the width**. This probe
    renders 480x360, where the same formula would give the 14 px floor and therefore a
    3 % cell: a *coarser* grid than the device ever uses, which over-states every
    coarse-grid artifact and makes small changes look better than they are. So the
    floor here is scaled to keep the ratio: 5 px at 480 wide is 1 %, like 32 px at
    3200. The closing radius then follows the same rule as `paperRadius`.
    """
    return max(5, int(math.sqrt((w * h) / 8000.0)))


def mean_pool(pixels, w, h, cell, gw, gh):
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
            i = gy * gw + gx
            if count:
                out[0][i] = sr / count
                out[1][i] = sg / count
                out[2][i] = sb / count
    return out


def percentile_pool(pixels, w, h, cell, gw, gh, pct, stride=1):
    """Per-cell percentile of each channel — the *paper* of that cell.

    A mean is dragged toward ink by however much ink the cell holds; a percentile is
    not, as long as ink covers less than `100 - pct` of the cell. Text at 20 %
    coverage is invisible to the 80th percentile, and to the 70th; a letter filling
    half a cell is not, which is why the closing afterwards still has a job.
    """
    out = [[0.0] * (gw * gh) for _ in range(3)]
    for gy in range(gh):
        y0 = min(h - 1, gy * cell)
        y1 = min(h, y0 + cell)
        for gx in range(gw):
            x0 = min(w - 1, gx * cell)
            x1 = min(w, x0 + cell)
            chans = ([], [], [])
            for y in range(y0, y1, stride):
                row = y * w
                for x in range(x0, x1, stride):
                    p = pixels[row + x]
                    chans[0].append((p >> 16) & 0xFF)
                    chans[1].append((p >> 8) & 0xFF)
                    chans[2].append(p & 0xFF)
            if not chans[0]:
                continue
            i = gy * gw + gx
            for c in range(3):
                v = chans[c]
                v.sort()
                out[c][i] = v[min(len(v) - 1, int(len(v) * pct))]
    return out


def trimmed_pool(pixels, w, h, cell, gw, gh, drop=0.35, stride=1, drop_high=0.0):
    """Per-cell mean of the **brightest** `1 - drop` of the samples.

    This is the estimator the other two were approximating:

    * a *mean* is smooth across a light step (a cell straddling the edge returns an
      intermediate value, which is what makes the grid interpolate cleanly) but it is
      dragged down by ink — which is the whole "shadows are not cleaned" complaint,
      because the ink keeps paper from reaching white;
    * a *percentile* rejects ink but quantises: every cell returns one of its own
      samples, so a cell straddling a step jumps rather than ramps, and the seam it
      leaves is worse than the mean's (measured: 51.8 vs 18.4 on `hand_shadow`).

    Trimming keeps the first property and gains the second: ink is the dark minority
    of a cell, so dropping the darkest third removes it and averages the rest. A
    256-bin histogram makes it one pass with no sort, which is what makes this
    affordable on a 3200 px page in Kotlin.
    """
    out = [[0.0] * (gw * gh) for _ in range(3)]
    for gy in range(gh):
        y0 = min(h - 1, gy * cell)
        y1 = min(h, y0 + cell)
        for gx in range(gw):
            x0 = min(w - 1, gx * cell)
            x1 = min(w, x0 + cell)
            hist = [[0] * 256 for _ in range(3)]
            total = 0
            for y in range(y0, y1, stride):
                row = y * w
                for x in range(x0, x1, stride):
                    p = pixels[row + x]
                    hist[0][(p >> 16) & 0xFF] += 1
                    hist[1][(p >> 8) & 0xFF] += 1
                    hist[2][p & 0xFF] += 1
                    total += 1
            i = gy * gw + gx
            if not total:
                continue
            cut = int(total * drop)
            keep_high = max(0, total - int(total * drop_high))
            for c in range(3):
                h_c = hist[c]
                seen = 0
                s = 0.0
                n = 0
                for value in range(256):
                    count = h_c[value]
                    if not count:
                        continue
                    take = count
                    if seen + take <= cut:
                        seen += take
                        continue
                    if seen < cut:
                        take -= cut - seen
                    seen += count
                    if seen > keep_high:
                        take -= min(take, seen - keep_high)
                    if take > 0:
                        s += value * take
                        n += take
                out[c][i] = s / n if n else 0.0
    return out


def window_op(src, w, h, radius, dilate):
    out = [0.0] * (w * h)
    for y in range(h):
        for x in range(w):
            best = -1.0 if dilate else 1e9
            for dy in range(-radius, radius + 1):
                yy = min(h - 1, max(0, y + dy))
                row = yy * w
                for dx in range(-radius, radius + 1):
                    xx = min(w - 1, max(0, x + dx))
                    v = src[row + xx]
                    if dilate:
                        if v > best:
                            best = v
                    elif v < best:
                        best = v
            out[y * w + x] = best
    return out


def median3(src, gw, gh):
    out = [0.0] * (gw * gh)
    for y in range(gh):
        for x in range(gw):
            vals = []
            for dy in (-1, 0, 1):
                yy = min(gh - 1, max(0, y + dy))
                row = yy * gw
                for dx in (-1, 0, 1):
                    xx = min(gw - 1, max(0, x + dx))
                    vals.append(src[row + xx])
            vals.sort()
            out[y * gw + x] = vals[4]
    return out


def close(grids, gw, gh, radius, rounding, kind="close"):
    """`close`: dilate then erode (erases ink narrower than the window).

    `max`: dilate only — reaches further into a dark region than the closing does,
    which matters when two dark things are adjacent (a text stroke *on top of* a
    pencil line: the closing's erode drags the image back down to the pencil, the
    dilation never does). Its cost is a known upward noise bias, which the final
    paper clamp absorbs.

    `none`: no morphological step at all — the pooling has to do the ink rejection
    on its own, which a percentile or a trimmed mean can.
    """
    out = []
    for plane in grids:
        g = plane
        if kind == "close":
            g = window_op(plane, gw, gh, radius, True)
            g = window_op(g, gw, gh, radius, False)
        elif kind == "max":
            g = window_op(plane, gw, gh, radius, True)
        for _ in range(rounding):
            g = median3(g, gw, gh)
        out.append(g)
    return out


_pool_cache: dict = {}


def build_surface(pixels, w, h, pooling="mean", pct=0.80, fine=False,
                  morph="close", rounding=2):
    """The paper surface, as three float planes on a cell grid."""
    cell = cell_radius(w, h) + 1
    gw = max(1, (w + cell - 1) // cell)
    gh = max(1, (h + cell - 1) // cell)
    key = (id(pixels), pooling, pct)
    if key not in _pool_cache:
        if pooling == "mean":
            _pool_cache[key] = mean_pool(pixels, w, h, cell, gw, gh)
        elif pooling == "trim":
            _pool_cache[key] = trimmed_pool(pixels, w, h, cell, gw, gh, drop=0.35)
        elif pooling == "sym":
            # Drop the darkest 30 % (ink) *and* the brightest 5 % (specular noise),
            # so the mean of what remains is unbiased on flat paper — a one-sided
            # trim is not: averaging the top 65 % of a noisy flat field returns a
            # level *above* the true one, which under-corrects the whole page.
            _pool_cache[key] = trimmed_pool(pixels, w, h, cell, gw, gh,
                                            drop=0.30, drop_high=0.05)
        elif pooling == "median":
            _pool_cache[key] = trimmed_pool(pixels, w, h, cell, gw, gh,
                                            drop=0.47, drop_high=0.06)
        else:
            _pool_cache[key] = percentile_pool(pixels, w, h, cell, gw, gh, pct)
    pooled = _pool_cache[key]
    radius = max(1, max(gw, gh) // 40)
    coarse = close(pooled, gw, gh, radius, rounding=rounding, kind=morph)

    fine_grid = None
    fcell = fgw = fgh = 0
    if fine:
        # A second, much smaller cell: the surface *within* a cell. The coarse pass is
        # deliberately blind to anything smaller than its window (that is how it
        # ignores ink), which also means it cannot see a shadow boundary closer than
        # one cell. The fine pass sees it; its ratio to the frame's own paper level is
        # the correction applied on top of the coarse one.
        fcell = max(2, cell // FINE_DIVISOR)
        fgw = max(1, (w + fcell - 1) // fcell)
        fgh = max(1, (h + fcell - 1) // fcell)
        fpooled = trimmed_pool(pixels, w, h, fcell, fgw, fgh, drop=0.35)
        # The closing radius is in *pixels* what the coarse one is: the fine pass has
        # to erase anything narrower than a coarse cell (a pencil line, a stroke of
        # body text) or it mistakes a thin mark for a shadow and brightens it away —
        # measured: 105 -> 178 on every scene until this radius was raised.
        fradius = max(2, int(cell * FINE_CLOSE_MULT / fcell))
        fine_grid = close(fpooled, fgw, fgh, fradius, rounding=2, kind=morph)
    return {"planes": coarse, "gw": gw, "gh": gh, "cell": cell,
            "fine": fine_grid, "fgw": fgw, "fgh": fgh, "fine_cell": fcell}


def sample(plane, gw, gh, cell, x, y):
    """Bilinear sample of a cell grid — `DocumentFilters.sample`, exactly.

    The grid is *centred* on its cells (`(x + 0.5) / cell - 0.5`), not floored, so a
    cell's value belongs to the middle of the cell rather than to its top-left pixel.
    Ported literally on purpose: this probe is the specification for the Kotlin, and a
    one-pixel difference in the interpolation is a one-cell difference in where a
    shadow edge is believed to be.
    """
    fx = min(max((x + 0.5) / cell - 0.5, 0.0), gw - 1.0)
    fy = min(max((y + 0.5) / cell - 0.5, 0.0), gh - 1.0)
    x0 = min(int(fx), gw - 1)
    y0 = min(int(fy), gh - 1)
    x1 = min(gw - 1, x0 + 1)
    y1 = min(gh - 1, y0 + 1)
    tx = fx - x0
    ty = fy - y0
    i00 = y0 * gw + x0
    i01 = y0 * gw + x1
    i10 = y1 * gw + x0
    i11 = y1 * gw + x1
    top = plane[i00] + (plane[i01] - plane[i00]) * tx
    bottom = plane[i10] + (plane[i11] - plane[i10]) * tx
    return top + (bottom - top) * ty


# ------------------------------------------------------------------- pipeline ---


def percentile_of(values, p):
    v = sorted(values)
    return v[min(len(v) - 1, int(len(v) * p))]


def normalise(pixels, w, h, surface, knee=8.0, cap=24.0, floor=0.22,
              fine_lo=0.85, fine_hi=1.15, ink_gate=0.86):
    """White balance: divide each channel by the paper at that pixel, in Q8.

    Two estimates of the paper are combined:

    * **coarse** — the percentile-pooled, closed grid. It is blind to anything
      smaller than a cell, which is exactly how it ignores ink, and it is smooth
      enough that the final background is flat.
    * **fine** — the same estimator on a cell a fifth as wide, which *can* see a
      shadow boundary closer than a cell. Its job is not to estimate the paper but to
      report where the coarse one is wrong: the correction is the ratio `fine/coarse`
      at the same pixel, damped to `[fine_lo, fine_hi]`. On flat paper the ratio is
      1 and nothing happens; across a step it is < 1 on the dark side, which is the
      grey seam a coarse grid always leaves beside a shadow.

    The ratio is applied **only to background pixels** (`v >= ink_gate · coarse`).
    That one line is what keeps a pencil mark out of it: the fine estimator inside a
    4 px pencil line sees the pencil, and without the gate it would brighten the mark
    toward white — measured, 105 → 190 on `flat`, i.e. the mark disappears.
    """
    planes = surface["planes"]
    gw, gh, cell = surface["gw"], surface["gh"], surface["cell"]
    fine = surface["fine"]
    fcell, fgw, fgh = surface["fine_cell"], surface["fgw"], surface["fgh"]
    out = [0] * len(pixels)
    kq, cq = int(knee * Q), int(cap * Q)
    gate = ink_gate
    for i, p in enumerate(pixels):
        x = i % w
        y = i // w
        r = (p >> 16) & 0xFF
        g = (p >> 8) & 0xFF
        b = p & 0xFF
        pr = max(1.0, sample(planes[0], gw, gh, cell, x, y))
        pg = max(1.0, sample(planes[1], gw, gh, cell, x, y))
        pb = max(1.0, sample(planes[2], gw, gh, cell, x, y))
        if fine is not None:
            # One scalar correction, from green (the channel a neutral detector
            # trusts), applied to all three so hue cannot shift.
            coarse_g = max(1.0, pg)
            fine_g = max(1.0, sample(fine[1], fgw, fgh, fcell, x, y))
            ratio = min(FINE_RATIO_HIGH, max(FINE_RATIO_LOW, fine_g / coarse_g))
            # Gated on the *fine* estimate: a pixel on the dark side of a shadow
            # edge looks like ink next to the (over-bright) coarse surface, but it
            # is ordinary background next to the fine one, and only the second is a
            # reason not to correct it.
            if luma_fixed(r, g, b) >= gate * fine_g:
                pr *= ratio
                pb *= ratio
                pg *= ratio
        # The surface here is in real units; the kernel's grid is Q8, so the paper
        # enters the divide multiplied by 256.
        gr = knee_gain(int(WHITE_Q8_OVER_PAPER / max(1.0, pr * Q)), kq, cq)
        gg = knee_gain(int(WHITE_Q8_OVER_PAPER / max(1.0, pg * Q)), kq, cq)
        gb = knee_gain(int(WHITE_Q8_OVER_PAPER / max(1.0, pb * Q)), kq, cq)
        out[i] = (p & 0xFF000000) | (min(255, rnd_q8(r * gr)) << 16) | \
                 (min(255, rnd_q8(g * gg)) << 8) | min(255, rnd_q8(b * gb))
    return out


def tone(pixels, curve, floor=0.22, chroma=1.12):
    """The preset's ratio curve, one scale per pixel, hue-preserving chroma."""
    out = [0] * len(pixels)
    fq = int(floor * Q)
    cq = int(chroma * Q)
    for i, p in enumerate(pixels):
        r = (p >> 16) & 0xFF
        g = (p >> 8) & 0xFF
        b = p & 0xFF
        luma = min(255, max(0, luma_fixed(r, g, b)))
        gain = (curve[luma] << 8) // luma if luma > 0 else Q
        if gain < fq:
            gain = fq
        sr = min(255, rnd_q8(r * gain))
        sg = min(255, rnd_q8(g * gain))
        sb = min(255, rnd_q8(b * gain))
        grey = luma_fixed(sr, sg, sb)
        out[i] = (p & 0xFF000000) | \
                 (min(255, max(0, grey + ((sr - grey) * cq >> 8))) << 16) | \
                 (min(255, max(0, grey + ((sg - grey) * cq >> 8))) << 8) | \
                 min(255, max(0, grey + ((sb - grey) * cq >> 8)))
    return out


SHARPEN_MAX_LUMA = 210
# Two 3x3 means, the same primitive the Kotlin uses (`boxBlurInPlace` x2): a
# triangle 5-tap, sigma ~ 1.3 px, i.e. the width of a lens-blurred stroke edge.
SHARPEN_PASSES = 2
SHARPEN_MAX_DROP = 40.0


def finish(pixels, w, h, clamp=0.945, sharpen=0.9, noise_gate=3.0):
    """Background clamp, then edge sharpening — both after the tone map.

    * **clamp**: any pixel at or above `clamp` of white *is* paper. A page has no
      content brighter than its own paper except a specular glint, which is a defect
      rather than content, so those pixels are set to pure white and their chroma is
      dropped. This is what makes the background *uniform* rather than merely light:
      it absorbs the low-frequency wash, the paper tint and the last percent of
      surface error in one step, and it can never darken anything. (It is also
      Adobe's `L_norm = min(100, L/L_bg · 100)` taken literally at the top end.)
    * **sharpen**: a 3×3 Laplacian of luma, applied **inward only** — a pixel darker
      than its neighbourhood gets darker, a pixel brighter does not get brighter
      (it is already at the clamp, and brightening would clip and ring). Sharpening
      the darkness plane instead of the brightness plane is what makes this work even
      when the ink is already black: it acts on the *slope* of a stroke edge, not on
      the ink core, so the transition narrows instead of the ink saturating.
    """
    luma = [0] * len(pixels)
    for i, p in enumerate(pixels):
        luma[i] = luma_fixed((p >> 16) & 0xFF, (p >> 8) & 0xFF, p & 0xFF)

    # The sharpening signal, before the clamp, so the gate sees the contrast it is
    # about to act on. It is a *blur minus the pixel*, not a 3x3 Laplacian.
    #
    # This distinction is the whole of round 64's sharpening. A 3x3 Laplacian is a
    # second derivative: it is zero in the middle of a straight ramp, so on a lens-
    # blurred stroke edge — which is a ramp — it does nothing, and it only responds in
    # the one or two pixels where the curvature changes. Measured on a blurred page it
    # moved the 10-90 % edge width by 0.00 px. `blur(radius 2) - v` is a first-order
    # high pass on the darkness plane: it is large wherever the pixel is darker than
    # its neighbourhood *including thin strokes and their ink-side shoulders*, which
    # is what "sharper text" means, and it stays at zero inside a solid ink block and
    # on paper.
    blur = list(luma)
    for _ in range(SHARPEN_PASSES):
        tmp = list(blur)
        for y in range(h):
            row = y * w
            for x in range(w):
                acc = 0.0
                n = 0
                for dx in (-1, 0, 1):
                    xx = x + dx
                    if 0 <= xx < w:
                        acc += blur[row + xx]
                        n += 1
                tmp[row + x] = acc / n
        for y in range(h):
            row = y * w
            for x in range(w):
                acc = 0.0
                n = 0
                for dy in (-1, 0, 1):
                    yy = y + dy
                    if 0 <= yy < h:
                        acc += tmp[yy * w + x]
                        n += 1
                blur[row + x] = acc / n
    lap = [blur[i] - luma[i] for i in range(len(pixels))]

    limit = clamp * 255.0
    for i, p in enumerate(pixels):
        v = luma[i]
        if clamp > 0 and v >= limit:
            pixels[i] = (p & 0xFF000000) | 0xFFFFFF
            continue
        nv = v
        d = lap[i]
        # The sharpening is bounded twice: it only applies to pixels that are already
        # below paper level (a paper pixel at the side of a stroke has a *huge*
        # positive Laplacian and would be blacked out), and the darkening per pixel is
        # capped. Both bounds are in the Kotlin too.
        if d > 0 and 12 < v < SHARPEN_MAX_LUMA:
            nv = max(0.0, v - min(SHARPEN_MAX_DROP,
                                  max(0.0, d - noise_gate) * sharpen * 0.5))
        if nv == v:
            continue
        ratio = (nv / v) if v > 0 else 1.0
        r = min(255, int(((p >> 16) & 0xFF) * ratio + 0.5))
        g = min(255, int(((p >> 8) & 0xFF) * ratio + 0.5))
        b = min(255, int((p & 0xFF) * ratio + 0.5))
        pixels[i] = (p & 0xFF000000) | (r << 16) | (g << 8) | b
    return pixels


# What shipped in round 62, kept only so `r62` can be measured against `r63`.
LEGACY_CURVES = {
    "SMART": (0.60, 0.98, 1.60, 0.22, 1.12),
    "LIGHT_TEXT": (0.78, 0.99, 1.50, 0.25, 1.20),
}

CURVES = {
    # (lo, hi, gamma, floor, chroma)
    "SMART": (0.60, 0.98, 1.60, 0.22, 1.12),
    "LIGHT_TEXT": (0.70, 0.94, 1.00, 0.25, 1.20),
    # Tuned once the background is guaranteed white: the curve only has to separate
    # ink from paper, and its lower end can be far more sensitive without greying
    # anything, because everything at paper level has already been clamped.
    "SMART_NEW": (0.55, 0.965, 1.55, 0.20, 1.12),
    "LIGHT_NEW": (0.62, 0.975, 1.45, 0.18, 1.18),
}


def pipeline(pixels, w, h, variant):
    """Runs one candidate end to end and returns the output pixels.

    `current` is what shipped before this round; `r63` is the chain described in
    `DocumentFilters` (fine correction + paper clamp + inward sharpening); the
    `no*` variants each remove one part so the report shows what it is worth.
    """
    light = variant.endswith("_light")
    base = variant[:-6] if light else variant

    surface = build_surface(pixels, w, h, "mean", fine=(base not in ("current", "r62")))
    if base != "current":
        out = normalise(pixels, w, h, surface)
        out = [0] * len(pixels) if False else out
    else:
        out = normalise(pixels, w, h, surface)

    curve = CURVES["LIGHT_TEXT" if light else "SMART"]
    if base == "r62":
        curve = LEGACY_CURVES["LIGHT_TEXT" if light else "SMART"]
    lo, hi, gamma, floor, chroma = curve
    out = tone(out, ratio_curve(lo, hi, gamma), floor, chroma)
    if base in ("current", "r62"):
        return out
    # `r63_no_clamp_sharpen` isolates the surface work: no clamp, no sharpening.
    clamp = 0.0 if "no_clamp" in base else 0.90

    sharpen = 0.0 if "no_sharpen" in base or "no_clamp_sharpen" in base else 0.9
    return finish(out, w, h, clamp=clamp, sharpen=sharpen)


# -------------------------------------------------------------------- metrics ---


def edge_width(out, w, h):
    """Mean 10-90 % transition width across a bar edge, in pixels.

    Sharpness is not "how dark is the ink" — that is contrast, and the tone curve owns
    it. It is how many pixels the edge takes to get from paper to ink: an unsharpened
    divide spreads a bar edge over 3-4 px, and a sharpened one over fewer. Measure it
    with the same normalisation on both, so a darker ink cannot look sharper.
    """
    luma = lambda p: luma_fixed((p >> 16) & 0xFF, (p >> 8) & 0xFF, p & 0xFF)  # noqa
    widths = []
    for bx in BAR_COLS:
        for y in (90, 150, 210):
            row = [luma(out[y * w + max(0, min(w - 1, x))]) for x in range(bx - 6, bx + 12)]
            hi = max(row[0:3])                     # paper, well outside the bar
            lo = min(row[10:15])                   # the bar's core
            if hi - lo < 30:
                continue                           # no edge here in this scene
            a = hi - 0.10 * (hi - lo)
            b = hi - 0.90 * (hi - lo)
            widths.append(sum(1 for v in row if b < v <= a))
    return (sum(widths) / len(widths)) if widths else 0.0


def edge_report():
    print("| scene | variant | edge width (px) |")
    print("|-------|---------|-----------------|")
    for name in SCENES:
        base = scene(name)["pixels"]
        for variant in ("r63_no_sharpen", "r63"):
            out = pipeline(base, W, H, variant)
            print(f"| {name:<13} | {variant:<15} | {edge_width(out, W, H):.2f} |")
    print()


def metrics(scene_name, out, w, h):
    data = scene(scene_name)
    lit = data["lit"]
    paper = [out[i] for i in range(w * h) if KIND[i] == PAPER and CLEAN[i]]
    ink = [out[i] for i in range(w * h) if KIND[i] == INK]
    faint = [out[i] for i in range(w * h) if KIND[i] == FAINT]
    v = lambda p: luma_fixed((p >> 16) & 0xFF, (p >> 8) & 0xFF, p & 0xFF)  # noqa: E731
    paper_v = [v(p) for p in paper]
    dev = [255 - x for x in paper_v]
    mean_dev = sum(dev) / len(dev)
    worst = max(dev)
    var = sum((d - mean_dev) ** 2 for d in dev) / len(dev)

    lit_paper = [v(out[i]) for i in range(w * h) if KIND[i] == PAPER and CLEAN[i] and lit[i]]
    shadow_paper = [v(out[i]) for i in range(w * h) if KIND[i] == PAPER and CLEAN[i] and not lit[i]]
    gap = 0.0
    if lit_paper and shadow_paper:
        gap = sum(lit_paper) / len(lit_paper) - sum(shadow_paper) / len(shadow_paper)

    # The shell: paper exactly one pixel outside the optical reach of any content.
    # Sharpening that rings shows up here as a darkening clean paper does not have.
    shell = [255 - v(out[i]) for i in range(w * h) if SHELL[i]]
    halo_mean = sum(shell) / len(shell) if shell else 0.0

    # The seam: the band the surface cannot resolve, i.e. everything within one and a
    # half cells of a light step. This is the grey line users see alongside a shadow,
    # and an average over the whole page hides it completely.
    seam = []
    step = None
    for x in range(w - 1):
        if lit[0 * w + x] and not lit[0 * w + x + 1]:
            step = x
            break
    if step is not None:
        band = int(1.5 * (cell_radius(w, h) + 1))
        for y in range(h):
            for x in range(max(0, step - band), min(w, step + band + 1)):
                i = y * w + x
                if KIND[i] == PAPER and CLEAN[i]:
                    seam.append(255 - v(out[i]))
    seam_mean = sum(seam) / len(seam) if seam else 0.0
    seam_worst = max(seam) if seam else 0.0

    return {
        "paper": mean_dev,
        "worst": worst,
        "sd": math.sqrt(var),
        "gap": gap,
        "ink": sum(v(p) for p in ink) / len(ink),
        "faint": sum(v(p) for p in faint) / len(faint),
        "halo": halo_mean,
        "seam": seam_mean,
        "seamw": seam_worst,
    }


def fmt(m):
    return (f"{m['paper']:5.1f} {m['worst']:5.0f} {m['sd']:5.1f} {m['gap']:6.1f} "
            f"{m['seam']:5.1f} {m['seamw']:5.0f} | {m['ink']:5.1f} {m['faint']:5.1f} "
            f"{m['halo']:5.1f}")


HEADER = (f"| {'scene':<13} | paper worst    sd    gap  seam seamw |   ink faint  halo |")


def report(variants):
    print(HEADER)
    print("|" + "-" * 15 + "|" + "-" * 30 + "|" + "-" * 19 + "|")
    for name in SCENES:
        for variant in variants:
            out = pipeline(scene(name)["pixels"], W, H, variant)
            print(f"| {name:<13} | {fmt(metrics(name, out, W, H))} |  {variant}")
    print()


def surface_score(scene_name, surface, w, h) -> dict:
    """Background metrics from the *divide alone*.

    For a paper pixel the tone curve is an identity at 255 and the chroma term
    cancels, so what decides whether the background is white is only
    `min(255, v · 255 / surface)`. Scoring the surface this way makes an estimator
    sweep cheap — no curve, no finish — and lets the constants be chosen before the
    whole chain is run.
    """
    data = scene(scene_name)
    pixels = data["pixels"]
    lit = data["lit"]
    planes, gw, gh, cell = surface["planes"], surface["gw"], surface["gh"], surface["cell"]
    fine, fcell, fgw, fgh = surface["fine"], surface["fine_cell"], surface["fgw"], surface["fgh"]
    devs = []
    seam = []
    band = int(1.5 * (cell + 1))
    step = None
    for x in range(w - 1):
        if lit[x] and not lit[x + 1]:
            step = x
            break
    ink_vals = []
    block_vals = []
    faint_vals = []
    for y in range(h):
        for x in range(w):
            i = y * w + x
            p = pixels[i]
            if KIND[i] == FAINT:
                v = luma_fixed((p >> 16) & 0xFF, (p >> 8) & 0xFF, p & 0xFF)
                pr = max(1.0, sample(planes[1], gw, gh, cell, x, y))
                if fine is not None:
                    fv = max(1.0, sample(fine[1], fgw, fgh, fcell, x, y))
                    if v >= 0.86 * fv:
                        pr *= min(FINE_RATIO_HIGH, max(FINE_RATIO_LOW, fv / pr))
                faint_vals.append(min(255.0, v * 255.0 / pr))
                continue
            if KIND[i] in (INK, BAR):
                v = luma_fixed((p >> 16) & 0xFF, (p >> 8) & 0xFF, p & 0xFF)
                pr = max(1.0, sample(planes[1], gw, gh, cell, x, y))
                if fine is not None:
                    fv = max(1.0, sample(fine[1], fgw, fgh, fcell, x, y))
                    if v >= 0.86 * fv:
                        pr *= min(FINE_RATIO_HIGH, max(FINE_RATIO_LOW, fv / pr))
                out = min(255.0, v * 255.0 / pr)
                (block_vals if (KIND[i] == BAR and 300 <= x < 380 and 270 <= y < 340)
                 else ink_vals).append(out)
                continue
            if KIND[i] != PAPER or not CLEAN[i]:
                continue
            v = luma_fixed((p >> 16) & 0xFF, (p >> 8) & 0xFF, p & 0xFF)
            pr = max(1.0, sample(planes[1], gw, gh, cell, x, y))
            if fine is not None:
                fv = max(1.0, sample(fine[1], fgw, fgh, fcell, x, y))
                ratio = min(FINE_RATIO_HIGH, max(FINE_RATIO_LOW, fv / pr))
                if v >= 0.86 * fv:
                    pr *= ratio
            out = min(255.0, v * 255.0 / pr)
            dev = 255 - out
            devs.append(dev)
            if step is not None and abs(x - step) <= band:
                seam.append(dev)
    mean = sum(devs) / len(devs)
    var = sum((d - mean) ** 2 for d in devs) / len(devs)
    return {"paper": mean, "sd": math.sqrt(var), "worst": max(devs),
            "seam": (sum(seam) / len(seam)) if seam else 0.0,
            "seamw": max(seam) if seam else 0.0,
            "inkv": (sum(ink_vals) / len(ink_vals)) if ink_vals else 0.0,
            "faintv": (sum(faint_vals) / len(faint_vals)) if faint_vals else 0.0,
            "blockv": (sum(block_vals) / len(block_vals)) if block_vals else 0.0}


ESTIMATORS = {
    "mean+close+fine": ("mean", 0.80, "close", 2, True),
    "sym+close+fine": ("sym", 0.80, "close", 2, True),
    "median+close+fine": ("median", 0.80, "close", 2, True),
    "mean+none+fine": ("mean", 0.80, "none", 2, True),
    # name: (pooling, pct, morph, rounding, fine)
    "mean+close (today)": ("mean", 0.80, "close", 2, False),
    "trim+close": ("trim", 0.80, "close", 2, False),
    "trim+max": ("trim", 0.80, "max", 2, False),
    "trim+none": ("trim", 0.80, "none", 2, False),
    "pct80+close": ("pct", 0.80, "close", 2, False),
    "pct80+max": ("pct", 0.80, "max", 2, False),
    "pct80+median": ("pct", 0.80, "none", 3, False),
    "trim+close+fine": ("trim", 0.80, "close", 2, True),
    "trim+max+fine": ("trim", 0.80, "max", 2, True),
    "pct80+max+fine": ("pct", 0.80, "max", 2, True),
}


def surface_report(names):
    print(f"| {'estimator':<20} | {'scene':<13} | paper worst    sd   seam seamw |"
          f" inkv block |")
    print("|" + "-" * 22 + "|" + "-" * 15 + "|" + "-" * 30 + "|" + "-" * 11 + "|")
    for label in names:
        pooling, pct, morph, rounding, fine = ESTIMATORS[label]
        for name in SCENES:
            data = scene(name)
            surf = build_surface(data["pixels"], W, H, pooling, pct, fine=fine,
                                 morph=morph, rounding=rounding)
            m = surface_score(name, surf, W, H)
            print(f"| {label:<20} | {name:<13} | {m['paper']:5.1f} {m['worst']:5.0f} "
                  f"{m['sd']:5.1f} {m['seam']:5.1f} {m['seamw']:5.0f} |"
                  f" {m['inkv']:4.0f} {m['faintv']:5.0f} {m['blockv']:5.0f} |")
    print()


def bounds_report() -> None:
    """Sweeps the two constants that trade seam against mark safety."""
    global FINE_RATIO_LOW, FINE_RATIO_HIGH, FINE_DIVISOR, FINE_CLOSE_MULT
    for low, high, divisor, mult in ((0.85, 1.15, 5, 0.5), (0.85, 1.15, 5, 1.0),
                                     (0.85, 1.15, 5, 1.5), (0.85, 1.15, 5, 2.5)):
        FINE_RATIO_LOW, FINE_RATIO_HIGH, FINE_DIVISOR, FINE_CLOSE_MULT = low, high, divisor, mult
        _pool_cache.clear()
        tot = []
        for name in SCENES:
            data = scene(name)
            surf = build_surface(data["pixels"], W, H, "mean", fine=True, morph="close")
            m = surface_score(name, surf, W, H)
            tot.append((name, m))
        seam = sum(m["seam"] for _, m in tot) / len(tot)
        worst = max(m["worst"] for _, m in tot)
        faint = min(m["inkv"] for _, m in tot)
        faintv = [m["faintv"] for _, m in tot]
        print(f"| close x{mult} | seam {seam:5.1f} | worst {worst:4.0f} | "
              f"ink {faint:4.0f} | faint {min(faintv):.0f}/{max(faintv):.0f} | "
              + " ".join(f"{n[:6]}:{m['paper']:.1f}" for n, m in tot))
    FINE_RATIO_LOW, FINE_RATIO_HIGH, FINE_DIVISOR, FINE_CLOSE_MULT = 0.85, 1.15, 5, 1.0


def sanity() -> None:
    """Fails loudly if the pipeline stops being a filter.

    A page is not blank: if every pixel of a page comes back white, the gain is
    wrong, not the page. This caught a 256x error in the divide, which silently
    reported a perfect score on every scene.
    """
    for name in ("flat", "hand_shadow", "lamp_cast"):
        data = scene(name)
        out = pipeline(data["pixels"], W, H, "current")
        ink = [luma_fixed((out[i] >> 16) & 0xFF, (out[i] >> 8) & 0xFF, out[i] & 0xFF)
               for i in range(W * H) if KIND[i] == INK]
        mean = sum(ink) / len(ink)
        if mean > 200:
            raise SystemExit(
                f"sanity: {name} leaves text at {mean:.0f} on 255 — the pipeline is "
                f"not filtering. Fix the divide before trusting any number below."
            )
        print(f"sanity: {name} text mean {mean:.0f} (must be < 200)")


def main(argv):
    what = argv[1] if len(argv) > 1 else "report"
    sanity()
    if what == "report":
        report(["r63_no_sharpen", "r63"])
    elif what == "light":
        report(["r62_light", "r63_light"])
    elif what == "bounds":
        bounds_report()
    elif what == "surface":
        surface_report(["mean+close (today)", "mean+close+fine"])
    elif what == "edge":
        edge_report()
    elif what == "variants":
        report(["current", "pct", "pct_fine", "pct_fine_clamp", "no_clamp",
                "pct_fine_clamp_sharp", "no_sharpen"])
    else:
        print(__doc__)
        return 2
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
