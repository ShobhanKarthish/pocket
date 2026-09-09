# Pocket demo capture

Assets in this folder were taken from the **running debug app** on an emulator, not from Compose previews.

Ink **PASS 2026-09-09**. Astra redesign freeze §12 (`docs/ASTRA-REDESIGN-v1.md`, `docs/DESIGN-FREEZE.md` §12). Where R conflicts with elevation B geometry, R wins.

## Assets

| File | What it shows |
| --- | --- |
| `empty-light.png` | Empty shelf, light, no mark, copy at top, docked **Add items** 136×48 r12 + **How to add** at the bottom |
| `empty-dark.png` | Same empty chrome, dark |
| `populated-light.png` | Three unboxed rows (PNG + text + link), header **Pocket Shelf** + **3 items** inside the 72 dp bar, header text **Add**, no FAB |
| `populated-dark.png` | Same list, dark (header Add + count + unboxed rows) |
| `text-link-light.png` | Same three-item unboxed list (text + link with PNG), light |
| `text-link-dark.png` | Same three-item list, dark |
| `selection-light.png` | **2 selected**, Close + Select all, tonal fill + 2 dp r12 outline + **24 dp circle** check, bottom **Share · Remove**, Add hidden |
| `selection-dark.png` | Same selection, dark |
| `arrange-light.png` | Arrange + Done, drag handles **and** Move up / Move down, titles readable, Add hidden |
| `arrange-dark.png` | Same arrange, dark |
| `detail-image-light.png` | Image detail, 72 dp Close + title, **Share · Remove** |
| `detail-image-dark.png` | Same image detail, dark |
| `detail-text-light.png` | Text detail, full note, **Copy · Share** |
| `detail-text-dark.png` | Same text detail, dark |
| `detail-link-light.png` | Link detail, hostname + URL, bottom **Open link · Copy · Share** |
| `detail-link-dark.png` | Same link detail, dark |
| `add-sheet-light.png` | Add sheet headed **Add items**: **Choose files** / **Add text** |
| `add-sheet-dark.png` | Same Add sheet, dark |
| `mixed-share-choice-light.png` | Material sheet: **Share selected items**, body, then **Share files (1)** / **Share text (1)** / **Copy text** rows |
| `mixed-share-choice-dark.png` | Same mixed share, dark |
| `settings-light.png` | Settings: Appearance System/Light/Dark, Haptics, Storage + Clear shelf, Privacy |
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

Ink **PASS 2026-09-09** chrome-gate fix on Astra redesign freeze §12.

How to add was already dismissed. Populated / selection / arrange / detail / mixed / add-sheet stills used a PNG (`pocket-demo.png` / `img1.png`), a text note (`Pack the bag. Do not forget socks.`), and the `example.com` link.

Header browse: title 20/28, count inside the min-72 dp bar, text **Add**. Empty docks **Add items** 136×48 r12 at the bottom with **How to add**, not under the copy. Resting rows are unboxed with bare glyph slots. Selected rows: tonal fill + 2 dp r12 outline + **24 dp circle** check. Arrange keeps Move up / Move down with readable titles. Add sheet: **Add items** / **Choose files** / **Add text**. Mixed share: Material sheet headed **Share selected items**. Settings: Appearance System/Light/Dark, Haptics, Storage + Clear shelf, Privacy. Motion unchanged (BRIEF table only).

Empty stills were taken after Select all → Remove (with Undo snackbar allowed to expire). Re-seed is not in git.

## Gaps

- No physical device.
- `am start` + MediaStore `ACTION_SEND` still cannot grant read to Pocket on API 30.
- No multi-shelf, no bubble (out of slice).
- `add-list-share-remove.mp4` still shows the pre-Astra FAB/card chrome.
- Item-detail stills do not show pinch zoom or an external PDF viewer.
- APK is not committed.
