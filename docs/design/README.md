# Design assets

Rendered previews of the app's own mark, so a reviewer can see what the vector
drawables put on screen without building the app.

| file | what it shows | source of truth |
| :--- | :--- | :--- |
| `app_icon.png` | the launcher icon: the page, its ink lines and the amber scan line on the brand gradient | `app/src/main/res/drawable/ic_launcher_foreground.xml`, `ic_launcher_background.xml` |
| `launch_screen.png` | the launch screen as composed: mark, "Doc Scanner", tagline, progress bar | `app/src/main/java/com/example/docscanner/ui/common/LaunchSplash.kt`, `res/drawable/launch_background.xml` |

Both PNGs are **previews only** — the app draws both from vectors, at every density.
If the drawables change, re-render these; they are documentation, not assets, and
nothing in `app/` references them.

Colours, kept in step with `res/values/colors.xml`:

| role | value |
| :--- | :--- |
| brand ink (gradient top, Android 12 splash colour) | `#06223A` |
| brand blue (gradient bottom) | `#0A5AA0` |
| scan amber (the scanner accent, `ScanAccentLight`) | `#FFB020` |
