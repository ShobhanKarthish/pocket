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
| `text-link-light.png` | Light list after `ACTION_SEND text/plain`: TEXT row (first line + `TEXT · size`) + LINK row (**hostname title**, truncated URL subtitle) mixed with PDF + PNG, **4 items** |
| `text-link-dark.png` | Same four rows, dark |
| `selection-light.png` | Light selection, **2 selected**, Close + Select all on top, check + outline, bottom **Share · Remove**, no Move, FAB hidden |
| `selection-dark.png` | Same selection, dark |
| `arrange-light.png` | Light arrange, drag handles, **Done**, Move up / Move down on each row |
| `arrange-dark.png` | Same arrange, dark |
| `detail-image-light.png` | Image item detail, light, full preview, Close + **Share · Remove** |
| `detail-image-dark.png` | Same image detail, dark |
| `detail-text-light.png` | Text item detail, light, full note, bottom **Copy · Share** |
| `detail-text-dark.png` | Same text detail, dark |
| `detail-link-light.png` | Link item detail, light, hostname + URL, bottom **Open · Copy · Share** |
| `detail-link-dark.png` | Same link detail, dark |
| `mixed-share-choice-light.png` | Light selection **2 selected** (PNG + text) with quiet sheet: **Share files (1) · Share text (1) · Copy text** |
| `mixed-share-choice-dark.png` | Same mixed share choice, dark |

`add-list-share-remove.mp4` is **58 s**, 720×1280, H.264, 12 fps.

## Device

- AVD `pocket_gapis30`: 720×1280, **API 30**, `google_apis` x86_64
- Debug APK: `:app:assembleDebug` on the item-detail + mixed-share slice
- Package: `com.shobhankarthish.pocket`
- Theme: `adb shell cmd uimode night no`
- Accel: **TCG** (`-accel off -gpu swiftshader_indirect`). Nested KVM kernel-BUGs in `kvm_arch_vcpu_create` even after `chmod 666 /dev/kvm`.
- Frames: earlier stills used emulator console `screenrecord screenshot`. This take used `adb exec-out screencap` for dark and arrange because the first console dark frame was black. Input: `adb shell input tap` after `uiautomator dump`.

## Collect / add (this take)

Adds in the mp4 go through the **in-app `OpenDocument` picker** (DocumentsUI Recent grid). Seeds `pocket-demo.png` (226 B) and `pocket-demo.pdf` (539 B) live in `/sdcard/Download`. Tap the grid **preview** (not the truncated filename) until the bar shows **1 selected**, then **SELECT**. Pocket copies bytes, inserts a Room row, and the list refreshes (`Added to Pocket`). After both picks the header is **2 items** and `files/shelf/` holds a `.png` and a `.pdf`.

Share-out uses the same shelf file (`ShareOut` / system chooser: Pocket, Bluetooth, Gmail, Messages). Share-in (`ACTION_SEND`) uses the same `ingestSuspending` path as the picker; this video does not restage a foreign `ACTION_SEND`.

How-to sheet was already dismissed (`how_to_add_seen`) so the empty launch is the empty chrome, not the sheet.

## Text and link stills

`text-link-light.png` and `text-link-dark.png` are emulator-console screenshots of the four-item list after two `ACTION_SEND` `text/plain` extras (plus PDF + PNG still on the shelf):

- `Pack the bag. Do not forget socks.` → TEXT row title is the first line, subtitle `TEXT · 34 B`, `files/shelf/<id>.txt`
- `https://example.com/notes` → LINK row title is **`example.com`**, subtitle is the URL (`https://example.com/notes`, ellipsized when it does not fit), `files/shelf/<id>.url`

Share-out of the text row opened the system sheet with that prose as `EXTRA_TEXT` (Copy / Pocket / Bluetooth / Gmail / Messages). Remove of that row left **3 items** and deleted the `.txt`.

## Selection and arrange stills

`selection-*.png` were recaptured after the freeze motion pass (check + outline 140 ms at rest). Rest chrome is unchanged: **2 selected**, Close + Select all, check + 1 dp outline, bottom **Share · Remove**, FAB hidden. `arrange-*.png` stay on the earlier arrange take (drag handles, **Done**, Move up / Move down).

## Item detail and mixed share stills

`detail-image-*.png`, `detail-text-*.png`, `detail-link-*.png`, and `mixed-share-choice-*.png` are `adb exec-out screencap` frames recaptured after the BRIEF-table motion trim. How to add was dismissed. The shelf had a PNG (`pocket-demo.png`), a text note (`Pack the bag. Do not forget socks.`), and the `example.com` link.

Tap the PNG row for a full preview with Close and **Share · Remove**. Tap the text row to read the note with **Copy · Share**. Tap the link row for hostname + URL with **Open · Copy · Share**. Overflow **Select items**, check the PNG and the note, then bottom Share. The sheet is a quiet middot row: **Share files (1) · Share text (1) · Copy text**. Selection stays at **2 selected** behind the sheet. Close and Back leave the shelf as it was. There is no Sent snackbar.

Pinch zoom on the image preview is in the Compose gesture handler. These stills do not show a pinch. Emulator `screenrecord` on this TCG AVD paints a blank white frame, so motion is not in a clip; rest chrome is the stills, timings are the unit-tested BRIEF table.

## Video assembly

Two emulator-console WebM takes, trimmed and concatenated, then encoded to H.264:

1. Empty painted shelf → picker PNG → picker PDF → 2-item list → item overflow → system Share sheet → back → Remove PDF → 1 PNG + **Removed**
2. Same 1-PNG list → overflow → **Remove from Pocket?** → confirm → empty chrome + **Removed**, header has **no** count

There is a cut between the first remove and the last-item remove (same remaining PNG, not a restage of the adds).

## Gaps

- No physical device.
- `am start` + MediaStore `ACTION_SEND` still cannot grant read to Pocket on API 30; that is a sender-grant problem, not the picker path.
- No multi-shelf, no bubble (out of slice).
- These stills do not restage `ACTION_SEND_MULTIPLE`. That path is covered by unit tests (`ShareIntakeTest`, `ShareBatchTest`, `BatchTallyTest`).
- Item-detail stills do not show pinch zoom or an external PDF viewer. `ShareBatch.decide` covers the share matrix in unit tests.
- APK is not committed.
