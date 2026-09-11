# Changelog

All notable changes to Pocket are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Versions match Android `VERSION_NAME` in `gradle.properties` (`VERSION_CODE` alongside).
Git tags are `v` plus `VERSION_NAME`, for example `v0.2.0`.

## [Unreleased]

### Removed

- Always-on floating bubble overlay (`SYSTEM_ALERT_WINDOW`, special-use foreground service, boot persistence, first-run Display over other apps, Settings toggle)

## [0.2.0] - 2026-09-11

Current app (`versionCode` 2). First taggable GitHub Release.

### Added

- Temporary on-device shelf for images, PDFs, notes, and http(s) links
- Copy-on-ingest so the source app can go away; originals are never modified
- Share-out through FileProvider, including batches and an explicit mixed files/text sheet
- Always-on floating bubble over other apps (Display over other apps)
- Tap-the-bubble tray: open shelf, share all, clear, hide
- Share → Pocket as the reliable add path (cross-app drag onto overlays is not)
- Settings: appearance, haptics, bubble, storage, privacy
- No account, no server, no `INTERNET` permission

[Unreleased]: https://github.com/ShobhanKarthish/pocket/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/ShobhanKarthish/pocket/releases/tag/v0.2.0
