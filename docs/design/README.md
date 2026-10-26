# Design assets

Rendered previews of the app's own mark, so a reviewer can see what the vector
drawables put on screen without building the app.

| file | what it shows | source of truth |
| :--- | :--- | :--- |
| `app_icon.png` | the launcher icon: the page, its ink lines and the amber scan line on the brand gradient | `app/src/main/res/drawable/ic_launcher_foreground.xml`, `ic_launcher_background.xml` |
| `launch_screen.png` | the launch screen as composed: mark, "Doc Scanner", tagline, progress bar | `app/src/main/java/com/example/docscanner/ui/common/LaunchSplash.kt`, `res/drawable/launch_background.xml` |
| `pdf_export_sheet.png` | the PDF export sheet, before and after Round 34 — three tiles: the old layout, the new one collapsed, the new one with the paper grid open (light) | `app/src/main/java/com/example/docscanner/ui/pdf/PdfExportSheet.kt` |
| `pdf_export_sheet_dark.png` | the same three tiles in the dark palette (Material You purple, matching the phone in the bug report) | same, plus `theme/Color.kt` |

All PNGs are **previews only** — the app draws the icon and the launch screen from
vectors, at every density, and the sheet mock is a drawing of a Compose layout, not a
screenshot (this project's sandbox cannot build the app).
If the drawables change, re-render these; they are documentation, not assets, and
nothing in `app/` references them.

Colours, kept in step with `res/values/colors.xml`:

| role | value |
| :--- | :--- |
| brand ink (gradient top, Android 12 splash colour) | `#06223A` |
| brand blue (gradient bottom) | `#0A5AA0` |
| scan amber (the scanner accent, `ScanAccentLight`) | `#FFB020` |

## `pdf_export_sheet.png`

Regenerate with `python3 docs/design/render_pdf_sheet.py` (Pillow); it writes both the
light and the dark file from one source of truth. The script mirrors
the dp values and the light palette so the drawing stays honest: 1 px = 1 dp, the same
20 dp sheet padding, 16 dp card padding, 40 dp chips, 54 dp paper cards, 76 dp
compression cards and 56 dp CTA. It is a *layout* drawing — real type metrics differ by
a couple of pixels per line, which is why the text is measured and ellipsised rather
than pasted at fixed positions.

What the three tiles argue, in order:

1. **Before.** `SegmentedPill` divides its width evenly, which is right for
   "Borders | Filters" and wrong for eight paper sizes: each segment got ~34 dp, so
   every label was clipped to "A4…", "US…", "Ex…". Margins and file size had the same
   treatment at four options ("No Man…", "Compac…"). Six identical uppercase eyebrows
   over six equally weighted blocks, and nothing said that paper is set once while file
   size and password are chosen on every export.
2. **After, default.** Paper is one row that states the current value ("A4 · 210 × 297
   mm") with a Change affordance; file size is a 2 × 2 of cards because it is the
   question asked every time; margins are a 2 × 2 of equal chips that cannot orphan;
   each toggle is a whole tappable row; and the CTA carries a caption naming exactly
   what will be written — "12 pages · A4 · Balanced".
3. **After, paper expanded.** Eight sizes in a two-column grid with their dimensions,
   the current one filled and checked. Choosing one folds the grid away again.
