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

Unit tests for ingest, mime allow-list, delete, selection order, arrange, and the share matrix:

```bash
./gradlew :app:testDebugUnitTest
```

## Share in

Pocket registers as an `ACTION_SEND` target for `image/*`, `application/pdf`, and `text/plain`. It also registers `ACTION_SEND_MULTIPLE` for images and PDFs. A mixed batch can arrive as `*/*`. Pocket copies each URI it can read, skips the rest, and says how many landed if the batch is only a partial success.

When a file share arrives, Pocket reads the content URI while the grant is still valid, copies the stream into `filesDir/shelf/`, then writes metadata to Room. The source URI is not stored. After that, the item survives process death from the local file plus the database row.

A `text/plain` share uses `EXTRA_TEXT`. A single http(s) URL becomes a link item whose list title is the hostname and whose subtitle is the truncated URL. Any other non-empty string becomes a text item (first line + `TEXT · size`). Both write UTF-8 into `filesDir/shelf/` and a Room row. You can also add a note from the shelf menu.

You can also tap **Add items** and pick one image or PDF with the system document picker. Pocket copies that file the same way. It does not take a persistable URI permission.

## Share out (FileProvider)

The FileProvider authority is `com.shobhankarthish.pocket.files`. Paths are limited to the `shelf/` directory under internal files.

A single file or one text/link still uses `ACTION_SEND`. Several images use `ACTION_SEND_MULTIPLE` with `image/*` or one shared image MIME. Several PDFs use `application/pdf`. Images and PDFs together use `*/*` and Pocket warns that some apps may not accept the mix.

Text and links share as one `text/plain` body in shelf order. Files plus text or links open a sheet: Share files, Share text, or Copy text. Pocket does not drop the text, attach it silently onto the file send, show Sent, or clear the selection after share.

Tap a row to open it. Images fill the screen and pinch to zoom. Text and links can be copied or shared. A link can also open in a browser. A PDF opens in another app if one is installed. If none is, Pocket says so and Share stays.

Long-press a row, or use **Select items**, to select several. The top bar shows **N selected** with Close and Select all. Share and Remove sit in the bottom bar. There is no Move on a single shelf. Close leaves selection.

**Arrange** shows drag handles and Done. Move up / Move down stays on each row for access. Order is stored as `sortIndex` on the Room row.

Removing an item deletes the local copy and the Room row. The file you originally shared from is left alone.

## What this slice includes

- One persistent shelf
- Receive one image, PDF, text note, or http(s) link
- Receive a batch of images and/or PDFs through `ACTION_SEND_MULTIPLE`
- Copy into app-owned storage
- Room metadata and persisted shelf order
- DataStore flag for the How to add sheet
- List with filename, type, size, and a full-color image thumb when the file decodes
- Multi-select share and remove, share order matching the shelf
- Arrange with drag handles and Move up / Move down
- Item detail: image preview, text and link read, PDF handoff
- Mixed share choice when files and text are selected together
- FileProvider share-out, including `ACTION_SEND_MULTIPLE`
- Safe remove
- Light and dark, Design Freeze v1 chrome (no Dynamic Color)

## What this slice leaves out

- Multiple shelves, a shelf switcher, move, folders, tags, search
- Floating bubble, overlays, Accessibility, clipboard monitoring
- Video, audio
- OCR, AI, ZIP, cloud, accounts, ads, analytics
