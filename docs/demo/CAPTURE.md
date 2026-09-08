# Pocket demo capture

Assets in this folder were taken from the **running debug app** on an emulator, not from Compose previews.

## Assets

| File | What it shows |
| --- | --- |
| `empty-light.png` | Empty shelf, light, How to add dismissed, **no** item-count line |
| `empty-dark.png` | Empty shelf, dark (earlier take; same empty chrome) |
| `populated-light.png` | Two items (PDF + PNG thumb), light, 1 dp `#E4E4E4` card stroke, **2 items** |
| `populated-dark.png` | Same list, dark (earlier take) |
| `add-list-share-remove.mp4` | Empty light → **in-app document picker** add PNG → picker add PDF → both rows → system Share sheet → Remove PDF → Remove last PNG → empty chrome again, **no** stale “1 item” |
| `text-link-light.png` | Light list after `ACTION_SEND text/plain`: TEXT row + LINK row (hostname metadata) mixed with PDF + PNG, **4 items** |
| `text-link-dark.png` | Same four rows, dark |

`add-list-share-remove.mp4` is **58 s**, 720×1280, H.264, 12 fps.

## Device

- AVD `pocket_gapis30`: 720×1280, **API 30**, `google_apis` x86_64
- Debug APK: `:app:assembleDebug` on the text+link slice (`text/plain` share-in)
- Package: `com.shobhankarthish.pocket`
- Theme: `adb shell cmd uimode night no`
- Accel: **KVM** (`-accel on -gpu swiftshader_indirect`). `/dev/kvm` is usable after `chmod 666`.
- Frames: emulator console `screenrecord start` / `screenshot` (host path). Input: console `event mouse`.

## Collect / add (this take)

Adds in the mp4 go through the **in-app `OpenDocument` picker** (DocumentsUI Recent grid). Seeds `pocket-demo.png` (226 B) and `pocket-demo.pdf` (539 B) live in `/sdcard/Download`. Tap the grid **preview** (not the truncated filename) until the bar shows **1 selected**, then **SELECT**. Pocket copies bytes, inserts a Room row, and the list refreshes (`Added to Pocket`). After both picks the header is **2 items** and `files/shelf/` holds a `.png` and a `.pdf`.

Share-out uses the same shelf file (`ShareOut` / system chooser: Pocket, Bluetooth, Gmail, Messages). Share-in (`ACTION_SEND`) uses the same `ingestSuspending` path as the picker; this video does not restage a foreign `ACTION_SEND`.

How-to sheet was already dismissed (`how_to_add_seen`) so the empty launch is the empty chrome, not the sheet.

## Text and link stills

`text-link-light.png` and `text-link-dark.png` are emulator-console screenshots after two `ACTION_SEND` `text/plain` extras on top of the existing PDF + PNG:

- `Pack the bag. Do not forget socks.` → TEXT row, `TEXT · 34 B`, `files/shelf/<id>.txt`
- `https://example.com/notes` → LINK row, `LINK · example.com`, `files/shelf/<id>.url`

Share-out of the text row opened the system sheet with that prose as `EXTRA_TEXT` (Copy / Pocket / Bluetooth / Gmail / Messages). Remove of that row left **3 items** and deleted the `.txt`.

## Video assembly

Two emulator-console WebM takes, trimmed and concatenated, then encoded to H.264:

1. Empty painted shelf → picker PNG → picker PDF → 2-item list → item overflow → system Share sheet → back → Remove PDF → 1 PNG + **Removed**
2. Same 1-PNG list → overflow → **Remove from Pocket?** → confirm → empty chrome + **Removed**, header has **no** count

There is a cut between the first remove and the last-item remove (same remaining PNG, not a restage of the adds).

## Gaps

- No physical device.
- `am start` + MediaStore `ACTION_SEND` still cannot grant read to Pocket on API 30; that is a sender-grant problem, not the picker path.
- No multi-shelf, no bubble (out of slice).
- APK is not committed.
