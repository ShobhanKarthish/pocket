#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"

./gradlew :app:testDebugUnitTest :app:assembleDebug

APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
AAPT="$ANDROID_HOME/build-tools/35.0.0/aapt"
if ! [[ -x "$AAPT" ]]; then
  AAPT="$ANDROID_HOME/build-tools/35.0.0/aapt2"
fi

if "$AAPT" dump permissions "$APK" 2>/dev/null | grep -E 'INTERNET' >/dev/null; then
  echo "INTERNET permission leaked into the APK" >&2
  "$AAPT" dump permissions "$APK" >&2 || true
  exit 1
fi

echo "verified $APK"
