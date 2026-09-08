# Pocket demo capture (`401aace`)

Assets in this folder were taken from the **running debug app** on an emulator, not from Compose previews.

## Assets

| File | What it shows |
| --- | --- |
| `empty-light.png` | Empty shelf, light, How to add dismissed |
| `empty-dark.png` | Empty shelf, dark |
| `populated-light.png` | Two items (PDF + PNG thumb), light |
| `populated-dark.png` | Same list, dark |
| `add-list-share-remove.mp4` | Add (document picker) → list → system Share sheet → remaining item + Remove menu |

## Device

- AVD `pocket_gapis30`: Pixel-ish 720×1280, **API 30**, `google_apis` x86_64
- Debug APK: `:app:assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`
- Package: `com.shobhankarthish.pocket`
- Theme toggled with `adb shell cmd uimode night yes|no`
- Frames grabbed with the emulator console (`screenrecord screenshot` / `screenrecord start`), not `adb screencap` (that path was a black buffer under TCG)

## Why this emulator, not KVM + API 34

`/dev/kvm` exists, but nested KVM on this VM **kernel-BUGs** in `kvm_arch_vcpu_create` (`kvm_spurious_fault`). Both `kvm_intel` and `kvm_amd` were loaded on a GenuineIntel guest. `adb` stayed `offline` and qemu used ~0% CPU.

Fallback: **TCG** (`-accel off`) + API 30 Google APIs. First boot ~5 minutes. System UI ANRs were frequent; Wait was tapped as needed.

An API 30 `aosp_atd` image booted more cleanly but **rendered 0 Skia frames** (host GPU / gfxstream), so screenshots were a solid background. That AVD was abandoned.

## Collect / add

Full **cross-app `ACTION_SEND`** from `am start` **did not ingest**. Logcat:

```
SecurityException: com.shobhankarthish.pocket has no access to content://media/external/images/media/15
```

Shell `--grant-read-uri-permission` is not enough for MediaStore on API 30 without a real sending app holding the grant.

**Document picker works** (DocumentsUI is on the Google APIs image). Seeds were pushed to `/sdcard/Download` and `/sdcard/Pictures`, then scanned into MediaStore. The demo video opens **Add items** into Recent/Images.

For the **populated stills**, the picker tap did not complete while a System UI ANR sat on top of input. Items were then **seeded with adb** into the same on-device store the ingestor uses:

- Files: `/data/data/com.shobhankarthish.pocket/files/shelf/demo001.png` and `demo002.pdf`
- Room: `pocket.db` table `shelf_items` (app force-stopped first so WAL was idle)

That is the same copy+metadata shape as share-in / OpenDocument; it does not go through `ItemIngestor` in-process.

## Gaps

- No physical device; no other-app Share UI (Photos → Pocket).
- Nested KVM unusable; TCG + System UI ANRs.
- `ACTION_SEND` via adb cannot grant MediaStore read to Pocket.
- Demo mp4: picker splash + Recent files (~0:16–0:37), list, Share sheet (Pocket / Maps / Bluetooth / Gmail). The **Remove confirm dialog is not in the video**; the list drops from 2 items to 1 (PNG) at the clip join, then the PDF overflow shows Share / Remove. Empty shelf is in the PNGs, not the mp4.
- No multi-shelf, no bubble (out of slice).
- APK is not committed (gitignored).
