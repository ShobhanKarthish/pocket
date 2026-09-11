# Astra redesign v1

Ink **PASS 2026-09-09**. Scope **A**: one shelf, no multi-shelf. The floating bubble is now in-product (owner override); this pack still owns **shelf** chrome only.

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
- Dock **Add items** (136 × 48 dp, radius 12) and **How to add** together at the bottom of the empty canvas, with 28 dp clearance above the safe inset and 8 dp between controls.
- **How to add** occupies a real, visible **≥ 48 dp** control. Never clip its label or overlap touch targets.

## R3 Resting rows

Unboxed. No container fill, no row radius, no 12 dp vertical gap, no resting borders. Glyph slots are **bare** (no grey tile fill). Image thumbs stay clipped photos. Rows sit on `background` with internal padding only.

## R4 Add

- **No FAB.**
- Populated browse: header text **Add** (opens the Add sheet).
- Hide Add in **0 ms** when selecting or arranging (not composed; no slide/fade).
- Empty uses the R2 docked Add, not a header Add.

## R5 Selection and arrange

- Selected row: neutral `surfaceVariant` fill + **2 dp** outline, radius **12**, plus a **24 dp circle** with a visible check. Fill and outline animate in both directions over 140 ms; the whole row exposes one checkbox action and state.
- Unselected rows stay transparent and unboxed. Selection and browse use the same 48 dp trailing slot, so titles do not shift.
- Arrange: 48 dp drag handles **and** Move up / Move down controls. Drag targets use measured visible rows, not a fixed row height; accessible move actions remain available. Lift uses tonal fill with light haptics. Done in the header.

## R6 Detail

Same 72 dp / 20–28 header as R1. Close + title. Content first, no card around the preview.

Bottom quiet action row, 1 dp divider, equal-width controls with at least 48 dp touch targets. Dividers follow the app's chosen appearance, not the system setting.

| Kind | Actions |
| --- | --- |
| Image | Share · Remove |
| Text | Copy · Share |
| Link | Open link · Copy · Share |
| PDF | Open · Share |

Pinch-zoom on images, with pan constrained to the fitted image bounds and rendering clipped to the preview viewport. Resizing the viewport resets zoom. Confirm remove. Close if the item is gone.

## R7 Sheets

Quiet. No grabber. Sheet motion uses Material defaults. Radius 12 on filled controls (not pills). Button actions finish hiding the sheet before opening the next surface; repeated taps cannot dispatch twice. Content scrolls when space is constrained and uses the sheet's safe/keyboard insets.

- **Add:** Heading **Add items**. Options **Choose files** / **Add text** (56 dp rows). Choose files opens the document picker. Add text opens the add-text sheet.
- **Add text:** title, field, Cancel / Add. Open draft survives rotation; explicit dismissal discards it.
- **How to add:** title 20/28, body, Got it 48 dp × radius 12.
- **Mixed share:** Material sheet. Heading **Share selected items**. Body **Files and text are shared separately.** Then **16 dp** group gap and 56 dp rows: Share files (n) / Share text (n) / Copy text. Not a middot bar. Selection stays.

## R8 Settings

Overflow **Settings**. Full-screen, same 72 dp header, Close.

Flat sections on that screen:

- **Appearance** — System / Light / Dark (no Dynamic Color). Full-width radio rows with a visible selected state. Every Material color role, including errors and elevated surfaces, is neutral; photos retain their original colors.
- **Haptics** — one whole-row switch for Pocket’s arrange lift and bubble snap/tap.
- **Floating bubble** — whole-row switch. Needs Display over other apps. Hide from the bubble, the notification, or this row.
- **Storage** — shelf size, **Clear shelf** (confirm; same delete/Undo path).
- **Privacy** — on-device, no account, no INTERNET.

No How-to-add / Version-only stub.

## Fail

FAB, boxed resting cards, 56 dp title-only bar with count underneath, pill empty Add, empty mark, drag-only arrange, mixing R with leftover B geometry.
