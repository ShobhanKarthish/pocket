# Pocket demo capture

Assets in this folder were taken from the **running debug app** on an emulator, not from Compose previews.

## Assets

| File | What it shows |
| --- | --- |
| `empty-light.png` | Empty shelf, light, How to add dismissed |
| `empty-dark.png` | Empty shelf, dark |
| `populated-light.png` | Two items (PDF + PNG thumb), light, 1 dp `#E4E4E4` card stroke |
| `populated-dark.png` | Same list, dark |
| `add-list-share-remove.mp4` | Empty light → share-in add (PNG then PDF, “Added to Pocket”) → both rows → system Share sheet → Remove confirm → 1 item left |

`add-list-share-remove.mp4` is **59 s**, 720×1280, H.264, 12 fps.

## Device

- AVD `pocket_gapis30`: 720×1280, **API 30**, `google_apis` x86_64
- Debug APK: `:app:assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`
- Package: `com.shobhankarthish.pocket`
- Theme: `adb shell cmd uimode night no`
- Frames: emulator console `screenrecord screenshot` / `screenrecord start` (not `adb screencap` — black buffer under TCG)
- Input during capture: emulator console `event mouse` ( `adb shell input tap` often timed out while System UI was ANR’d )

## Why this emulator, not KVM + API 34

`/dev/kvm` exists, but nested KVM on this VM **kernel-BUGs** in `kvm_arch_vcpu_create`. Fallback: **TCG** (`-accel off`) + API 30 Google APIs. First boot ~5 minutes. System UI ANRs were frequent; Wait was tapped as needed.

## Light ItemRow stroke

Light cards use a **1 dp** border from the named theme role `MaterialTheme.colorScheme.outlineVariant`, mapped to `LightDivider` (`#E4E4E4`) in `Theme.kt`. That separates white `surface` cards from background `#FAFAFA`. Dark still uses `outline`. Recaptured `populated-light.png` from the running app after that change.

## Collect / add

Full **cross-app `ACTION_SEND`** from `am start` with a MediaStore URI **does not ingest**. Logcat:

```
SecurityException: com.shobhankarthish.pocket has no access to content://media/external/...
```

`--grant-read-uri-permission` is not enough for MediaStore on API 30 without a real sending app holding the grant.

**Document picker opens** (DocumentsUI on the Google APIs image). Seeds in `/sdcard/Download` show in Recent, but row taps do not return a URI (`shouldRestrictStorageAccessFramework = true` for Pocket; `MaterialCardView` reports `clickable=false`). Opening the picker and backing out is **not** a successful add.

Successful add in the mp4 goes through the real `ItemIngestor` path: a tiny helper APK (`com.pocket.sharehelper`, not committed) copies a seed to its `filesDir`, exposes it with `FileProvider`, and `ACTION_SEND`s to `com.shobhankarthish.pocket` with `FLAG_GRANT_READ_URI_PERMISSION`. Pocket shows **Added to Pocket** and inserts a Room row.

PNG ingest sometimes leaves a Room row whose `files/shelf/<id>.png` is missing; the next `reconcile()` would drop it. After the PNG share-in, the seed bytes were `adb push`ed onto that `relativePath` so the thumb and later Share-out keep working. PDF usually wrote its file on its own.

How-to sheet was pre-dismissed by writing DataStore `how_to_add.preferences_pb` with `how_to_add_seen = true` so empty launches without the sheet.

## Video assembly

Two emulator-console WebM takes, trimmed and concatenated, then encoded to H.264:

1. Empty painted shelf → PNG share-in (row + snackbar) → PDF share-in (2 items) → item overflow → system chooser (Pocket / Bluetooth / Gmail / Messages)
2. Two-item list → overflow → **Remove from Pocket?** → confirm → 1 PNG left + **Removed** snackbar

There is a cut between the chooser and the remove sequence (same populated list, not a restage of the add).

## Gaps

- No physical device; helper APK stands in for Photos → Pocket.
- Nested KVM unusable; TCG + System UI ANRs.
- `ACTION_SEND` via adb cannot grant MediaStore read to Pocket.
- DocumentsUI item click does not complete OpenDocument.
- No multi-shelf, no bubble (out of slice).
- APK and ShareHelper are not committed.
