#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "usage: $0 <apk>" >&2
  exit 2
fi

APK="$1"
if [[ ! -f "$APK" ]]; then
  echo "APK not found: $APK" >&2
  exit 1
fi

ANDROID_HOME="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [[ -z "$ANDROID_HOME" ]]; then
  echo "ANDROID_HOME is not set" >&2
  exit 1
fi

AAPT=""
# Prefer the newest build-tools aapt (not aapt2: dump permissions differs).
while IFS= read -r candidate; do
  AAPT="$candidate"
done < <(find "$ANDROID_HOME/build-tools" -maxdepth 2 -type f -name aapt 2>/dev/null | sort)

if [[ -z "$AAPT" || ! -x "$AAPT" ]]; then
  echo "aapt not found under $ANDROID_HOME/build-tools" >&2
  exit 1
fi

if "$AAPT" dump permissions "$APK" | grep -E 'android\.permission\.INTERNET' >/dev/null; then
  echo "INTERNET permission leaked into $APK" >&2
  "$AAPT" dump permissions "$APK" >&2 || true
  exit 1
fi

echo "no INTERNET in $APK"
