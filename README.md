# Pocket

Collect now. Share when you're ready.

Pocket is a temporary local tray on Android. Share an image, PDF, note, or http(s) link from another app. Pocket copies the payload into its own storage, lists the item, and lets you share that copy out later. The original file is never modified.

This build is one shelf, on-device only. There is no account, no server, and the app does not request the `INTERNET` permission.

## Build and install a debug APK

You need JDK 17+ and Android SDK platform 35.

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The debug APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

Unit tests for ingest, mime allow-list, and delete:

```bash
./gradlew :app:testDebugUnitTest
```

## Share in

Pocket registers as an `ACTION_SEND` target for `image/*`, `application/pdf`, and `text/plain`.

When a file share arrives, Pocket reads the content URI while the grant is still valid, copies the stream into `filesDir/shelf/`, then writes metadata to Room. The source URI is not stored. After that, the item survives process death from the local file plus the database row.

A `text/plain` share uses `EXTRA_TEXT`. A single http(s) URL becomes a link item whose list title is the hostname and whose subtitle is the truncated URL. Any other non-empty string becomes a text item (first line + `TEXT · size`). Both write UTF-8 into `filesDir/shelf/` and a Room row. You can also add a note from the shelf menu.

You can also tap **Add items** and pick one image or PDF with the system document picker. Pocket copies that file the same way. It does not take a persistable URI permission.

## Share out (FileProvider)

The FileProvider authority is `com.shobhankarthish.pocket.files`. Paths are limited to the `shelf/` directory under internal files.

Share uses `ACTION_SEND` with `FLAG_GRANT_READ_URI_PERMISSION` and a `ClipData` URI so the receiver can read the copy. Removing an item deletes the local copy and the Room row. The file you originally shared from is left alone.

## What this slice includes

- One persistent shelf
- Receive one image, PDF, text note, or http(s) link
- Copy into app-owned storage
- Room metadata
- DataStore flag for the How to add sheet
- List with filename, type, size, and a full-color image thumb when the file decodes
- FileProvider share-out
- Safe remove
- Light and dark, Design Freeze v1 chrome (no Dynamic Color)

## What this slice leaves out

- Multiple shelves, a shelf switcher, move, folders, tags, search
- Floating bubble, overlays, Accessibility, clipboard monitoring
- Mixed `SEND_MULTIPLE`, video, audio
- OCR, AI, ZIP, cloud, accounts, ads, analytics
