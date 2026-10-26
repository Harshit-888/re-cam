"""Renders before/after mocks of the PDF export sheet.

This is a *drawing*, not a screenshot: the sandbox has no JDK or Android SDK, so the
Compose layout cannot be built here. It mirrors the dp values in
`ui/pdf/PdfExportSheet.kt` (1 px = 1 dp) and the light palette in `theme/Color.kt`,
so what it shows is the structure the code produces — spacing, grouping, what is
clipped and what is not.
"""

from PIL import Image, ImageDraw, ImageFont

# ---------------------------------------------------------------- palette
PRIMARY = (0, 97, 164)
ON_PRIMARY = (255, 255, 255)
PRIMARY_CONTAINER = (209, 228, 255)
ON_PRIMARY_CONTAINER = (0, 29, 54)
SURFACE = (253, 252, 255)
ON_SURFACE = (26, 28, 30)
ON_SURFACE_VARIANT = (67, 71, 78)
SURFACE_LOW = (247, 249, 252)
SURFACE_HIGHEST = (229, 233, 238)
OUTLINE_VARIANT = (195, 199, 207)
ERROR = (186, 26, 26)
SCRIM = (26, 28, 30)

FONT = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
FONT_BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"

_cache = {}


def f(size, bold=False):
    key = (size, bold)
    if key not in _cache:
        _cache[key] = ImageFont.truetype(FONT_BOLD if bold else FONT, size)
    return _cache[key]


def text(d, xy, s, size=13, bold=False, fill=ON_SURFACE, anchor="la"):
    d.text(xy, s, font=f(size, bold), fill=fill, anchor=anchor)


def ellipsis(d, s, size, max_w, bold=False):
    """Clips a string the way a Compose Text with maxLines = 1 does."""
    font = f(size, bold)
    if d.textlength(s, font=font) <= max_w:
        return s
    out = s
    while out and d.textlength(out + "\u2026", font=font) > max_w:
        out = out[:-1]
    return out + "\u2026"


def label(d, x, y, s):
    text(d, (x + 8, y), s.upper(), 10, True, ON_SURFACE_VARIANT)
    return y + 18


def card(d, x, y, w, h, color=SURFACE_LOW, radius=20):
    d.rounded_rectangle([x, y, x + w, y + h], radius=radius, fill=color)


def phone(w, h):
    img = Image.new("RGB", (w, h), (222, 226, 231))
    d = ImageDraw.Draw(img, "RGBA")
    # what is behind the sheet: a card grid, dimmed by the scrim
    for i in range(6):
        yy = 20 + (i // 2) * 150
        xx = 16 + (i % 2) * 172
        d.rounded_rectangle([xx, yy, xx + 156, yy + 138], radius=16, fill=(236, 239, 243))
    d.rectangle([0, 0, w, h], fill=(*SCRIM, 96))
    return img, d


def sheet_canvas(img, d, w, h, top):
    d.rounded_rectangle([0, top, w, h], radius=30, fill=SURFACE)
    d.rectangle([0, top + 30, w, h], fill=SURFACE)
    d.rounded_rectangle([w // 2 - 16, top + 12, w // 2 + 16, top + 16], radius=2,
                        fill=(190, 195, 202))


def draw_share(d, cx, cy, r=9):
    """A stand-in for the material Share glyph: a node with two leaves."""
    d.ellipse([cx - 2, cy - r, cx + 2, cy - r + 4], fill=ON_PRIMARY)
    d.ellipse([cx - 2, cy + r - 4, cx + 2, cy + r], fill=ON_PRIMARY)
    d.ellipse([cx - r, cy - 2, cx - r + 4, cy + 2], fill=ON_PRIMARY)
    d.line([(cx - 1, cy - r + 3), (cx - r + 3, cy - 1)], fill=ON_PRIMARY, width=2)
    d.line([(cx - 1, cy + r - 3), (cx - r + 3, cy + 1)], fill=ON_PRIMARY, width=2)


def switch(d, x, y, on, w=44, h=26):
    track = PRIMARY if on else (200, 204, 210)
    d.rounded_rectangle([x, y, x + w, y + h], radius=h // 2, fill=track)
    knob = (255, 255, 255) if on else (250, 250, 252)
    cx = x + (w - h / 2 - 3) if on else x + h / 2 + 3
    d.ellipse([cx - h / 2 + 3, y + 3, cx + h / 2 - 3, y + h - 3], fill=knob)


# ------------------------------------------------------------------- before
def render_before(w=360, h=1240):
    """The sheet as it was: eight options split into eight equal segments."""
    img, d = phone(w, h)
    sheet_canvas(img, d, w, h, 24)
    x = 20
    cw = w - 40
    y = 24 + 34

    # header
    d.rounded_rectangle([x, y, x + 46, y + 46], radius=14, fill=PRIMARY_CONTAINER)
    text(d, (x + 46 // 2, y + 23), "PDF", 10, True, ON_PRIMARY_CONTAINER, anchor="mm")
    text(d, (x + 60, y + 2), "EXPORT", 9, True, PRIMARY)
    text(d, (x + 60, y + 16), ellipsis(d, "Receipts October", 18, cw - 60, True), 18, True)
    for i, (pill, pw) in enumerate([("12 pages", 74), ("300 DPI", 60)]):
        px = x + 60 + (0 if i == 0 else 80)
        d.rounded_rectangle([px, y + 42, px + pw, y + 62], radius=11, fill=SURFACE_HIGHEST)
        text(d, (px + 8, y + 47), pill, 10, False, ON_SURFACE_VARIANT)
    y += 46 + 22

    # file name
    y = label(d, x, y, "File name")
    d.rounded_rectangle([x, y, x + cw, y + 56], radius=18, outline=(-1), fill=None)
    d.rounded_rectangle([x, y, x + cw, y + 56], radius=18, outline=OUTLINE_VARIANT, width=1)
    text(d, (x + 16, y + 18), "Tax_2026", 14)
    text(d, (x + cw - 16, y + 20), ".pdf", 11, False, ON_SURFACE_VARIANT, anchor="ra")
    y += 62
    text(d, (x + 8, y), "Saved as Tax_2026.pdf", 10, False, ON_SURFACE_VARIANT)
    y += 24

    # three equal-width segmented rows, labels clipped
    for title, opts, selected in [
        ("Paper size", PaperSizeLabels, 0),
        ("Margins", ["No Margin", "Compact (10 pt)", "Standard (25 pt)", "Wide (40 pt)"], 2),
        ("File size", ["Original", "Balanced", "Small", "Tiny"], 0),
    ]:
        y = label(d, x, y, title)
        d.rounded_rectangle([x, y, x + cw, y + 46], radius=23, fill=SURFACE_HIGHEST)
        seg = (cw - 8) / len(opts)
        d.rounded_rectangle([x + 4 + seg * selected, y + 4, x + 4 + seg * (selected + 1), y + 42],
                            radius=19, fill=SURFACE)
        for i, o in enumerate(opts):
            sx = x + 4 + seg * i
            text(d, (sx + seg / 2, y + 23), ellipsis(d, o, 13, seg - 8), 13, False,
                 ON_SURFACE if i == selected else ON_SURFACE_VARIANT, anchor="mm")
        # annotation on the paper row, where eight options make it obvious
        if title == "Paper size":
            d.rounded_rectangle([x - 2, y - 2, x + cw + 2, y + 48], radius=24,
                                outline=(226, 72, 72), width=2)
            text(d, (x + cw, y + 52), "\u2190 8 options \u00f7 8 segments = every label clipped",
                 10, False, (200, 40, 40), anchor="ra")
            y += 18
        y += 46 + 22

    # output switch
    y = label(d, x, y, "Output")
    card(d, x, y, cw, 68)
    text(d, (x + 18, y + 14), "Footer page numbers", 14, True)
    text(d, (x + 18, y + 34), "1 / 12 at the bottom centre", 11, False, ON_SURFACE_VARIANT)
    switch(d, x + cw - 18 - 44, y + 21, True)
    y += 68 + 14
    text(d, (x + 8, y), "Auto Orientation \u00b7 Fit width or height image scale", 10,
         False, ON_SURFACE_VARIANT)
    y += 26

    # password
    y = label(d, x, y, "Password")
    card(d, x, y, cw, 68)
    text(d, (x + 18, y + 14), "Require a password", 14, True)
    text(d, (x + 18, y + 34), "The PDF asks for it when it is opened", 11, False,
         ON_SURFACE_VARIANT)
    switch(d, x + cw - 18 - 44, y + 21, False)
    y += 68 + 22

    # CTA
    d.rounded_rectangle([x, y, x + cw, y + 56], radius=28, fill=PRIMARY)
    text(d, (x + cw / 2, y + 28), "Generate & share PDF", 14, True, ON_PRIMARY, anchor="mm")
    y += 56 + 12
    text(d, (x + 8, y), "Six equally loud sections, no summary of the output.",
         10, False, ON_SURFACE_VARIANT)
    y += 34
    d.line([(x, y), (x + cw, y)], fill=OUTLINE_VARIANT, width=1)
    y += 14
    notes = [
        "Paper size: 8 options split into 8 equal",
        "segments, so every label is clipped \u2014",
        "\u201cA4\u2026\u201d, \u201cUS\u2026\u201d, \u201cEx\u2026\u201d. Margins and file",
        "size get the same treatment at 4.",
        "",
        "Nothing says which decisions matter:",
        "paper and margins are set once, file size",
        "and password are chosen every export.",
        "",
        "No summary of the output before the tap,",
        "and no way to tell the two PDF engines",
        "apart from the file that comes out.",
    ]
    for i, line in enumerate(notes):
        text(d, (x + 4, y + i * 15), line, 10, False, ON_SURFACE_VARIANT)
    text(d, (70, 8), "BEFORE", 12, True, (255, 255, 255))
    return img


PaperSizeLabels = [
    "A4 (210 \u00d7 297 mm)",
    "US Letter (8.5 \u00d7 11 in)",
    "US Legal (8.5 \u00d7 14 in)",
    "A3 (297 \u00d7 420 mm)",
    "A5 (148 \u00d7 210 mm)",
    "Executive (7.25 \u00d7 10.5 in)",
    "Business Card (85.6 \u00d7 54 mm)",
    "Receipt (80 mm roll)",
]


# -------------------------------------------------------------------- after
def render_after(expanded=False, w=360, h=1240):
    img, d = phone(w, h)
    sheet_canvas(img, d, w, h, 24)
    x = 20
    cw = w - 40
    y = 24 + 34

    # ---- header
    d.rounded_rectangle([x, y, x + 46, y + 46], radius=14, fill=PRIMARY_CONTAINER)
    text(d, (x + 46 // 2, y + 23), "PDF", 10, True, ON_PRIMARY_CONTAINER, anchor="mm")
    text(d, (x + 60, y + 2), "EXPORT", 9, True, PRIMARY)
    text(d, (x + 60, y + 16), "Receipts October", 18, True)
    pills = [("12 pages", 74), ("300 DPI", 60)]
    px = x + 60
    for name, pw in pills:
        fill = SURFACE_HIGHEST
        fg = ON_SURFACE_VARIANT
        d.rounded_rectangle([px, y + 42, px + pw, y + 62], radius=11, fill=fill)
        text(d, (px + 8, y + 47), name, 10, False, fg)
        px += pw + 6
    y += 46 + 20

    # ---- file name
    y = label(d, x, y, "File name")
    d.rounded_rectangle([x, y, x + cw, y + 56], radius=18, outline=OUTLINE_VARIANT, width=1)
    text(d, (x + 16, y + 18), "Tax_2026", 14)
    text(d, (x + cw - 16, y + 20), ".pdf", 11, False, ON_SURFACE_VARIANT, anchor="ra")
    y += 62
    text(d, (x + 8, y), "Saved as Tax_2026.pdf", 10, False, ON_SURFACE_VARIANT)
    y += 24

    # ---- paper
    y = label(d, x, y, "Paper")
    ph = 70 if not expanded else 70 + 12 + 4 * 54 + 3 * 8
    card(d, x, y, cw, ph)
    text(d, (x + 16, y + 14), "A4", 15, True)
    text(d, (x + 16, y + 36), "210 \u00d7 297 mm", 12, False, ON_SURFACE_VARIANT)
    text(d, (x + cw - 40, y + 25), "Done" if expanded else "Change", 13, False, PRIMARY,
         anchor="ra")
    d.polygon([(x + cw - 30, y + 27), (x + cw - 22, y + 27), (x + cw - 26, y + 33)],
              fill=PRIMARY) if not expanded else d.polygon(
        [(x + cw - 30, y + 32), (x + cw - 22, y + 32), (x + cw - 26, y + 26)], fill=PRIMARY)

    if expanded:
        gy = y + 70 + 12
        sizes = ["A4", "US Letter", "US Legal", "A3", "A5", "Executive", "Business Card",
                 "Receipt"]
        dims = ["210 \u00d7 297 mm", "8.5 \u00d7 11 in", "8.5 \u00d7 14 in", "297 \u00d7 420 mm",
                "148 \u00d7 210 mm", "7.25 \u00d7 10.5 in", "85.6 \u00d7 54 mm", "80 mm roll"]
        for row in range(4):
            for col in range(2):
                i = row * 2 + col
                if i >= len(sizes):
                    continue
                ox = x + 12 + col * (156 + 8)
                oy = gy + row * 62
                sel = i == 0
                d.rounded_rectangle([ox, oy, ox + 156, oy + 54], radius=12,
                                    fill=PRIMARY_CONTAINER if sel else SURFACE_HIGHEST)
                fg = ON_PRIMARY_CONTAINER if sel else ON_SURFACE
                text(d, (ox + 12, oy + 9), ellipsis(d, sizes[i], 14, 118, True), 14, True, fg)
                text(d, (ox + 12, oy + 30), dims[i], 10, False,
                     ON_PRIMARY_CONTAINER if sel else ON_SURFACE_VARIANT)
    y += ph + 18

    # ---- file size
    y = label(d, x, y, "File size")
    levels = [
        ("Original", "Lossless, largest file", False),
        ("Balanced", "Looks the same, far smaller", True),
        ("Small", "Good for email", False),
        ("Tiny", "Smallest, soft small text", False),
    ]
    for row in range(2):
        for col in range(2):
            name, detail, sel = levels[row * 2 + col]
            ox = x + col * (156 + 8)
            oy = y + row * (76 + 8)
            d.rounded_rectangle([ox, oy, ox + 156, oy + 76], radius=20,
                                fill=PRIMARY_CONTAINER if sel else SURFACE_LOW)
            fg = ON_PRIMARY_CONTAINER if sel else ON_SURFACE
            text(d, (ox + 12, oy + 11), name, 14, True, fg)
            if sel:
                d.line([(ox + 122, oy + 20), (ox + 127, oy + 25), (ox + 136, oy + 14)],
                       fill=fg, width=2)
            words = detail.split()
            line, lines = "", []
            for word in words:
                probe = (line + " " + word).strip()
                if d.textlength(probe, font=f(10)) > 128:
                    lines.append(line)
                    line = word
                else:
                    line = probe
            lines.append(line)
            for i, ln in enumerate(lines[:2]):
                text(d, (ox + 12, oy + 34 + i * 13), ln, 10, False,
                     ON_PRIMARY_CONTAINER if sel else ON_SURFACE_VARIANT)
    y += 2 * 76 + 8 + 8
    text(d, (x + 8, y), "JPEG quality 85 \u00b7 every pixel \u00b7 smaller file", 10,
         False, ON_SURFACE_VARIANT)
    y += 24

    # ---- layout
    y = label(d, x, y, "Layout")
    card_h = 14 + 2 * 40 + 8 + 14 + 1 + 64
    card(d, x, y, cw, card_h)
    # 2 x 2 equal chips, exactly as the code lays them out: weight(1f) each inside a
    # Row, so every chip is the same width and the grid cannot wrap into an orphan.
    chips = [("None", False), ("Compact", False), ("Standard", True), ("Wide", False)]
    chip_w = (cw - 32 - 8) / 2
    for row in range(2):
        for col in range(2):
            name, sel = chips[row * 2 + col]
            ox = x + 16 + col * (chip_w + 8)
            oy = y + 14 + row * 48
            d.rounded_rectangle([ox, oy, ox + chip_w, oy + 40], radius=20,
                                fill=PRIMARY if sel else SURFACE_HIGHEST)
            fg = ON_PRIMARY if sel else ON_SURFACE_VARIANT
            label_w = d.textlength(name, font=f(13, sel))
            total = label_w + (21 if sel else 0)
            tx = ox + (chip_w - total) / 2
            if sel:
                d.line([(tx + 1, oy + 21), (tx + 5, oy + 25), (tx + 12, oy + 15)],
                       fill=fg, width=2)
            text(d, (tx + (21 if sel else 0), oy + 13), name, 13, sel, fg)
    hy = y + 14 + 2 * 40 + 8 + 14
    d.line([(x + 16, hy), (x + cw - 16, hy)], fill=(*OUTLINE_VARIANT, 140), width=1)
    text(d, (x + 16, hy + 12), "Footer page numbers", 14, True)
    text(d, (x + 16, hy + 32), "1 / 12 at the bottom centre", 11, False, ON_SURFACE_VARIANT)
    switch(d, x + cw - 16 - 44, hy + 19, True)
    y += card_h + 18

    # ---- password
    y = label(d, x, y, "Password")
    card(d, x, y, cw, 64)
    text(d, (x + 16, y + 12), "Require a password", 14, True)
    text(d, (x + 16, y + 32), "Anyone with the file can open it", 11, False, ON_SURFACE_VARIANT)
    switch(d, x + cw - 16 - 44, y + 19, False)
    y += 64 + 20

    # ---- CTA
    d.rounded_rectangle([x, y, x + cw, y + 56], radius=28, fill=PRIMARY)
    label_w = d.textlength("Generate & share PDF", font=f(14, True))
    start = x + (cw - (label_w + 28)) / 2
    draw_share(d, start + 9, y + 28)
    text(d, (start + 28, y + 28), "Generate & share PDF", 14, True, ON_PRIMARY, anchor="lm")
    y += 56 + 8
    text(d, (x + 8, y), "12 pages \u00b7 A4 \u00b7 Balanced", 10, False, ON_SURFACE_VARIANT)
    y += 22
    text(d, (x + 8, y), "Auto Orientation \u00b7 Fit width or height \u00b7 written by the",
         10, False, ON_SURFACE_VARIANT)
    text(d, (x + 8, y + 13), "app's PDF writer", 10, False, ON_SURFACE_VARIANT)

    caption = "AFTER \u00b7 paper \u00b7 expanded" if expanded else "AFTER \u00b7 default"
    text(d, (14, 8), caption, 12, True, (255, 255, 255))
    return img


def main():
    tiles = [render_before(), render_after(False), render_after(True)]
    gap = 18
    total_w = sum(t.width for t in tiles) + gap * (len(tiles) - 1)
    total_h = max(t.height for t in tiles)
    out = Image.new("RGB", (total_w, total_h), (233, 236, 240))
    ox = 0
    for t in tiles:
        out.paste(t, (ox, 0))
        ox += t.width + gap
    out.save("docs/design/pdf_export_sheet.png", optimize=True)
    print("wrote docs/design/pdf_export_sheet.png", out.size)


if __name__ == "__main__":
    main()
