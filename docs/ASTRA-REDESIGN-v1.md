# Astra redesign v1

Ink **PASS 2026-09-09**. Scope **A**: one shelf, no bubble, no multi-shelf.

This pack is freeze **DESIGN-FREEZE §12**. Where redesign **R** conflicts with elevation **B** geometry (56 dp bar, count below the bar, boxed 16 dp cards, 12 dp gaps, FAB), **R wins**.

**Ship rule:** R1–R5 land together. Do not mix header Add with a FAB, or unboxed selection with resting card chrome.

Motion is unchanged: freeze §5 / BRIEF §8 timings only. No bounce, stagger, glass, glow, or extra fades.

## R1 Header (both themes)

- Title **20 / 28 sp**, `titleLarge`, bold.
- Bar **min 72 dp** (status insets extra).
- Item count sits **inside** the header, under the title, `bodyMedium` / onSurfaceVariant.
- Empty shelf: title only, no count line.
- Selecting: **N selected** is the title; Close + Select all / Deselect.
- Arranging: **Arrange** + Done.

## R2 Empty

- **No** mark / icon well.
- Headline and body **left-aligned**.
- Docked **Add items** **136 × 48 dp**, radius **12**, at the **bottom** of the empty canvas (not under the copy). Opens the Add sheet (R7).
- **How to add** control **≥ 48 dp** tall, docked with Add items.

## R3 Resting rows

Unboxed. No container fill, no row radius, no 12 dp vertical gap, no resting borders. Glyph slots are **bare** (no grey tile fill). Image thumbs stay clipped photos. Rows sit on `background` with internal padding only.

## R4 Add

- **No FAB.**
- Populated browse: header text **Add** (opens the Add sheet).
- Hide Add in **0 ms** when selecting or arranging (not composed; no slide/fade).
- Empty uses the R2 docked Add, not a header Add.

## R5 Selection and arrange

- Selected row: tonal `surfaceVariant` fill + **2 dp** essential outline, radius **12**, plus a **24 dp circle** check (filled when selected). Outline color animates 140 ms (BRIEF).
- Unselected rows while selecting stay unboxed; empty 24 dp circle only.
- Arrange: drag handles **and** Move up / Move down (not drag-only). Titles stay readable. Lift uses tonal fill. Done in the header.

## R6 Detail

Same 72 dp / 20–28 header as R1. Close + title. Content first, no card around the preview.

Bottom quiet 56 dp middot row, 1 dp divider:

| Kind | Actions |
| --- | --- |
| Image | Share · Remove |
| Text | Copy · Share |
| Link | Open link · Copy · Share |
| PDF | Open · Share |

Pinch-zoom on images. Confirm remove. Close if the item is gone.

## R7 Sheets

Quiet. No grabber. Sheet motion ~250 ms (Material default). Radius 12 on filled controls (not pills).

- **Add:** Heading **Add items**. Options **Choose files** / **Add text** (56 dp rows). Choose files opens the document picker. Add text opens the add-text sheet.
- **Add text:** title, field, Cancel / Add.
- **How to add:** title 20/28, body, Got it 48 dp × radius 12.
- **Mixed share:** Material sheet. Heading **Share selected items**. Body **Files and text are shared separately.** Then **16 dp** group gap and 56 dp rows: Share files (n) / Share text (n) / Copy text. Not a middot bar. Selection stays.

## R8 Settings

Overflow **Settings**. Full-screen, same 72 dp header, Close.

Flat sections on that screen:

- **Appearance** — System / Light / Dark (no Dynamic Color).
- **Haptics** — on/off for Pocket’s arrange lift.
- **Storage** — shelf size, **Clear shelf** (confirm; same delete/Undo path).
- **Privacy** — on-device, no account, no INTERNET.

No How-to-add / Version-only stub.

## Fail

FAB, boxed resting cards, 56 dp title-only bar with count underneath, pill empty Add, empty mark, drag-only arrange, mixing R with leftover B geometry.
