# Pocket design freeze

Scope **A**. One shelf. No bubble. No multi-shelf. On-device only.

## 1 Product

Collect now. Share when ready. Pocket copies inbound images, PDFs, text, and http(s) links into app-owned storage and lists them on one shelf.

## 2 Elevation B (superseded on geometry)

The first chrome pass used a 56 dp bar, 22/28 title, count **below** the bar, 16 dp boxed cards with 12 dp gaps, a 16 dp FAB, and 1 dp selection outlines.

**§12 Astra R wins** wherever that geometry conflicts with R1–R5.

## 3 Color

Light and dark schemes in `Color.kt`. No Dynamic Color. Follows system night mode.

## 4 Type

`titleLarge` is **20 / 28 sp** (R1). `titleMedium` 16/22. `bodyMedium` 14/20. `labelLarge` 14/20.

## 5 Motion (BRIEF table only)

| Token | ms |
| --- | --- |
| Select outline / fill | 140 (range 120–160) |
| Add / remove | 180 (range 160–200) |
| Sheets | ~250 |
| Add hide (select / arrange) | **0** |

Press ripple is Material default. Arrange lift is tonal + light haptic. Reduce-motion (`animator_duration_scale` or `transition_animation_scale` == 0) uses `snap()`.

**Fail:** bounce, scaleIn/scaleOut, launch stagger, glass, glow, confetti, detail fade-through, FAB/bar slides, empty↔list crossfade, selection haptic, decorative loops.

## 6 Share

`ShareBatch.decide()`:

- one file → `SendFile`
- several images → `SendFiles`
- several PDFs → `SendFiles`
- images + PDFs → `SendFiles` `*/*` + mixed-mime warning
- text/links → `SendText`, joined `\n\n` in shelf order
- files + text/links → `Choose` (never silent `EXTRA_TEXT` on a file send)

No Sent snackbar. Selection is not cleared after share.

## 7 Detail

See Astra R6.

## 8 Selection / arrange

See Astra R5. Move up / Move down stays. No Move off a single shelf.

## 9 Empty / how to add

See Astra R2 and R7. DataStore `how_to_add_seen`.

## 10 Settings

See Astra R8.

## 11 Out of slice

Multiple shelves, bubble, overlays, Accessibility, clipboard monitor, video, audio, OCR, cloud, accounts.

## 12 Astra redesign pack

**Ink PASS 2026-09-09.**

Implement `docs/ASTRA-REDESIGN-v1.md` as **one pack** on main.

- R1 header 20/28, min 72 dp, count inside the bar
- R2 empty: no mark, left-align, docked Add 136×48 r12, How to add ≥48
- R3 unboxed resting rows
- R4 no FAB; header text Add; hide Add 0 ms in selection/arrange
- R5 tonal fill + 2 dp outline r12 + check; arrange handles **and** Move up/down
- R6–R8 detail / sheets / settings as in the redesign doc

R1–R5 together. No partial mix with old card/FAB geometry. Motion: 0 ms change to §5.
