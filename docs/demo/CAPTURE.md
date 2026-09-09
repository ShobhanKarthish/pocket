# Pocket demo capture

Assets in this folder were taken from the **running debug app** on an emulator, not from Compose previews.

Ink **PASS 2026-09-09**. Astra redesign freeze §12 (`docs/ASTRA-REDESIGN-v1.md`, `docs/DESIGN-FREEZE.md` §12). Where R conflicts with elevation B geometry, R wins.

## Assets

| File | What it shows |
| --- | --- |
| `empty-light.png` | Empty shelf, light, no mark, left-aligned copy, docked **Add** 136×48 r12, **How to add** ≥48. Header title only (no count, no header Add). |
| `empty-dark.png` | Same empty chrome, dark |
| `populated-light.png` | Three unboxed rows (PNG + text + link), header **Pocket Shelf** + **3 items** inside the 72 dp bar, header text **Add**, no FAB |
| `populated-dark.png` | Same list, dark |
| `text-link-light.png` | Same three-item unboxed list (text + link with PNG), light |
| `text-link-dark.png` | Same three-item list, dark |
| `selection-light.png` | **2 selected**, Close + Select all, tonal fill + 2 dp r12 outline + check on selected rows, unboxed unselected row, bottom **Share · Remove**, Add hidden |
| `selection-dark.png` | Same selection, dark |
| `arrange-light.png` | Arrange + Done, drag handles **and** Move up / Move down, Add hidden |
| `arrange-dark.png` | Same arrange, dark |
| `detail-image-light.png` | Image detail, 72 dp Close + title, **Share · Remove** |
| `detail-image-dark.png` | Same image detail, dark |
| `detail-text-light.png` | Text detail, full note, **Copy · Share** |
| `detail-text-dark.png` | Same text detail, dark |
| `detail-link-light.png` | Link detail, hostname + URL, **Open · Copy · Share** |
| `detail-link-dark.png` | Same link detail, dark |
| `add-sheet-light.png` | Quiet Add sheet: **Image or PDF** / **Text or a link**, no grabber |
| `add-sheet-dark.png` | Same Add sheet, dark |
| `mixed-share-choice-light.png` | **2 selected** (PNG + text) with **Share files (1) · Share text (1) · Copy text** |
| `mixed-share-choice-dark.png` | Same mixed share choice, dark |
| `settings-light.png` | Settings: Close + title, Appearance follows system, How to add, Version 0.1.0 |
| `settings-dark.png` | Same settings, dark |
| `add-list-share-remove.mp4` | Earlier picker add / share / remove take (elevation B chrome; not recaptured) |

## Device

- AVD `pocket_gapis30`: 720×1280, **API 30**, `google_apis` x86_64
- Debug APK: `:app:assembleDebug` on the Astra §12 pack
- Package: `com.shobhankarthish.pocket`
- Theme: `adb shell cmd uimode night no` / `yes`
- Accel: **TCG** (`-accel off -gpu swiftshader_indirect`)
- Frames: `adb exec-out screencap -p`. Input: `adb shell input tap` after `uiautomator dump`. First dark populated frame after `uimode` can be blank; retake after a tap once Compose has painted.

## Astra §12 stills

How to add was already dismissed (`how_to_add_seen`). The shelf for populated / selection / arrange / detail / mixed / add-sheet stills had a PNG (`pocket-demo.png` / `img1.png`), a text note (`Pack the bag. Do not forget socks.`), and the `example.com` link.

Header browse: title 20/28, count inside the min-72 dp bar, text **Add**. Empty uses the docked Add, not the header Add. Resting rows have no card fill, radius, gap, or border. Selected rows get tonal `surfaceVariant` + 2 dp outline radius 12 + checkbox. Arrange keeps Move up / Move down. Motion was not changed (BRIEF table only).

Empty stills were taken after Select all → Remove (with Undo snackbar allowed to expire). Re-seed is not in git.

## Gaps

- No physical device.
- `am start` + MediaStore `ACTION_SEND` still cannot grant read to Pocket on API 30.
- No multi-shelf, no bubble (out of slice).
- `add-list-share-remove.mp4` still shows the pre-Astra FAB/card chrome.
- Item-detail stills do not show pinch zoom or an external PDF viewer.
- APK is not committed.
