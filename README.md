# Pocket

**Collect now. Share when you're ready.**

Pocket is a temporary tray on Android. Share images, PDFs, notes, and links in; Pocket keeps its own copy on this device; share that copy out when you know where it’s going.

No account. No server. The app does not request the `INTERNET` permission.

[![CI](https://github.com/ShobhanKarthish/pocket/actions/workflows/ci.yml/badge.svg)](https://github.com/ShobhanKarthish/pocket/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

<p align="center">
  <img src="docs/demo/empty-light.png" alt="Empty Pocket Shelf" width="220">
  <img src="docs/demo/populated-light.png" alt="Shelf with an image, a note, and a link" width="220">
  <img src="docs/demo/mixed-share-choice-light.png" alt="Share files and text as separate actions" width="220">
</p>

<p align="center"><sub>Shelf stills from the running app.</sub></p>

## Install

Android **8.0** or newer (API 26). Sideload from [Releases](https://github.com/ShobhanKarthish/pocket/releases) once a `vX.Y.Z` tag has been published, or build locally.

You need **JDK 17+** and Android SDK **platform 35**.

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

`scripts/verify-debug.sh` runs those tests, builds the debug APK, and fails if `INTERNET` leaked into it.

## How to use

1. **Add items** with **Share → Pocket** from Photos, Files, a browser, or any other app. You can also tap **Add** on the shelf to pick files or write a note.
2. Pocket **copies** the payload into its own storage while the share grant is still valid. The original in the other app is never modified.
3. On the shelf, open an item, select several, arrange order, then **Share** or **Remove**. Mixed files and text are offered as separate actions so nothing is dropped silently.

## Privacy

- Copies live in Pocket’s app storage on this device.
- There is no account and no server.
- `INTERNET` is removed in the manifest (`tools:node="remove"`). CI checks the built APK.
- Removing an item deletes Pocket’s copy only.

## Releases

App versions live in `gradle.properties` as `VERSION_NAME` and `VERSION_CODE`, and are wired into the Android module. Git tags are `v` plus `VERSION_NAME`.

After merging a version bump and [CHANGELOG](CHANGELOG.md) entry to `main`:

```bash
git tag vX.Y.Z
git push --tags
```

That tag must match `VERSION_NAME` (for example `v0.2.0`). GitHub Actions then creates a GitHub Release and attaches a debug APK (sideloadable) plus a release APK when the unsigned package builds. These are not Play Store–signed.

Current app version: **0.2.0** (`versionCode` 2). Tag `v0.2.0` on `main` to publish the first versioned GitHub Release. Older `debug-*` tags are ad-hoc APK drops, not versioned releases.

## License

[MIT](LICENSE)
