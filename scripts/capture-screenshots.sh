#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
export ANDROID_HOME
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

SCREENSHOT_DIR="$ROOT/docs/screenshots"
mkdir -p "$SCREENSHOT_DIR"

cd "$ROOT"
./gradlew assembleDebug

AVD_NAME="kidspace_preview"
if ! avdmanager list avd | grep -q "Name: $AVD_NAME"; then
  echo "no" | avdmanager create avd \
    -n "$AVD_NAME" \
    -k "system-images;android-34;google_apis;x86_64" \
    --device "pixel_6" \
    --force
fi

# Start emulator headless
adb devices | grep -q emulator || {
  emulator -avd "$AVD_NAME" -no-window -no-audio -gpu swiftshader_indirect &
  adb wait-for-device
  for i in $(seq 1 60); do
  boot=$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
    if [ "$boot" = "1" ]; then break; fi
    sleep 2
  done
}

adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell cmd package set-home-activity com.kidspace.launcher/.MainActivity 2>/dev/null || true
adb shell am start -n com.kidspace.launcher/.MainActivity
sleep 3
adb exec-out screencap -p > "$SCREENSHOT_DIR/01-child-home.png"

echo "Screenshots saved to $SCREENSHOT_DIR"
