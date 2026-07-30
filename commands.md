# Command Registry

All build and test commands for KidSpace Launcher.

## Prerequisites

Set `ANDROID_HOME` (default: `$HOME/android-sdk`) and ensure SDK components are installed via `scripts/setup-android-sdk.sh`.

## Build Commands

- `./gradlew assembleDebug` — Build debug APK
- `./gradlew assembleRelease` — Build release APK (unsigned)

## Test Commands

- `./gradlew test` — Run unit tests
- `./gradlew connectedDebugAndroidTest` — Run instrumented tests (requires device/emulator)

## Screenshot Commands

- `python3 scripts/generate-preview-screenshots.py` — Generate UI preview images to `docs/screenshots/` (no emulator required)
- `./scripts/capture-screenshots.sh` — Build APK, launch on emulator, capture UI screenshots (requires KVM)

## Install Commands

- `./gradlew installDebug` — Install debug build on connected device/emulator
- `adb shell am start -n com.kidspace.launcher/.MainActivity` — Open launcher

## SDK Setup

- `bash scripts/setup-android-sdk.sh` — Download Android SDK and required packages
