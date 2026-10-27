# UI/UX review — `emil-design-eng` + `mobile-native`

Applied against `github.com/Harshit-888/skills` (`emil-design-eng`, `mobile-native`,
`animation-performance`, `review-animations`). Round 47, 2026-10-27.

The audit covers every screen: dashboard, camera, document detail, crop & filter,
viewer, PDF export sheet, settings, trash, markup, and the shared `Expressive.kt`
primitives.

## What the audit found

The app already follows most of what those skills ask for, because earlier rounds built
it that way: a custom `cubic-bezier(0.23, 1, 0.32, 1)` ease-out (`Motion.EaseOut`),
springs instead of duration-based motion for anything that answers a touch, alpha and
translation on one `graphicsLayer` instead of two, `pressScale` on 23 pressable
surfaces, staggered list entrances capped at six steps, and `ModalBottomSheet` for the
export sheet (which brings native drag-to-dismiss and its velocity handling).

Four things were wrong.

| Before | After | Why |
| --- | --- | --- |
| Page actions constrained to `Modifier.size(32.dp)` — rotate, crop, move-back and the overflow menu had **32 dp touch targets** | Unconstrained `IconButton` (48 dp target around a 40 dp state layer) | `IconButton` already reserves the 48 dp minimum; the explicit size was undoing it. These are the four most-used controls on the screen. The row is 16 dp taller and every target is a finger wide. |
| Ink swatches were a 30 dp `Box` with `clickable` — the **drawing was the hit area** | `TapTarget` wrapper: 48 dp pressable, 30 dp dot centred inside | A colour picker missed by 6 dp is a colour the user did not choose. The drawing should never dictate the target. |
| Ink swatches had no accessible name — TalkBack read four unlabelled boxes | `label = "Black ink"` / "Red ink" / "Blue ink" / "Yellow ink", published as a radio group with `selected` | Colour is the only signal otherwise, and colour is not one. `selectable` also announces *which* ink is chosen. |
| Finish button after a capture: `initialScale = 0.6f` / `targetScale = 0.6f` | `0.92f` both ways | The skill's rule is never animate from `scale(0)`, start at 0.9 or above. 0.6 is a big pop on a button that appears after **every** capture — tens of times a session — which stops reading as delight and starts reading as lag. |
| Accordions in the export sheet used the same 140 ms to open and close | open 140 ms, close 100 ms | Asymmetric timing: the system answering the user should be faster than the system presenting something to them. |

## Checked and deliberately not changed

| Finding | Why it stays |
| --- | --- |
| `RevealWhen` animates width (`expandHorizontally`), a layout property | It is **unused**. Left in place rather than deleted mid-round; if it is ever adopted, it needs a fade + translation instead. Note for the next agent. |
| Paper/margin accordions animate height | A disclosure has to change height; there is no transform-only equivalent that keeps the rows below from jumping. Accepted exception, and it is a 140 ms accordion in a sheet, not a list. |
| `SpinningRing` uses a 900 ms linear infinite tween | Linear is correct for constant motion (progress, spinners). Under 300 ms applies to UI transitions, not to a spinner's period. |
| `LaunchSplash` 420 ms tween | Splash is a rare, first-time moment; the modal range is 200–500 ms. |
| `PersonCard`/grid cards have no per-item entrance animation | Deliberate and already commented in the code: items that fade in as you scroll re-run the animation for every item that appears, so the list never settles. |
| Sticky hover states, tap highlight flashes, `100vh` bugs, input zoom | Web-platform problems. Compose has no `:hover` to get stuck and no 300 ms tap delay. |
| Reduced motion | Handled by the framework: `animateFloatAsState` and `animate*AsState` read the system animator duration scale, so "Remove animations" makes them snap. Nothing to add — but note this is **not verified on a device** here. |

## Not verifiable in this sandbox

No emulator, no device, no Kotlin compiler. What that means for this review:

- **Verified from code:** every row above. Each is a modifier, a constant, or a
  semantics call, and each was re-read after the edit.
- **Needs a real device:** that the 48 dp row reads as comfortable rather than as a
  gap after the 180 dp thumbnail; that 0.92 still registers as an entrance at 120 Hz;
  that TalkBack announces the swatch group sensibly; and that nothing in the taller
  action row collides on a 320 dp-wide screen.

Test order on hardware: document detail at the smallest screen size you have (the
48 dp row is the layout risk), then a markup colour change with TalkBack on, then a
capture to watch the finish button.
