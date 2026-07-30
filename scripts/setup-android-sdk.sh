#!/usr/bin/env bash
set -euo pipefail

ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
export ANDROID_HOME

CMDLINE_TOOLS_ZIP="commandlinetools-linux-11076708_latest.zip"
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/${CMDLINE_TOOLS_ZIP}"

mkdir -p "$ANDROID_HOME/cmdline-tools"
cd /tmp

if [ ! -d "$ANDROID_HOME/cmdline-tools/latest" ]; then
  echo "Downloading Android command-line tools..."
  curl -fsSL -o "$CMDLINE_TOOLS_ZIP" "$CMDLINE_TOOLS_URL"
  rm -rf cmdline-tools
  unzip -q "$CMDLINE_TOOLS_ZIP"
  rm -rf "$ANDROID_HOME/cmdline-tools/latest"
  mv cmdline-tools "$ANDROID_HOME/cmdline-tools/latest"
  rm -f "$CMDLINE_TOOLS_ZIP"
fi

export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

echo "Installing SDK packages..."
yes | sdkmanager --licenses >/dev/null 2>&1 || true
sdkmanager \
  "platform-tools" \
  "platforms;android-34" \
  "build-tools;34.0.0" \
  "emulator" \
  "system-images;android-34;google_apis;x86_64"

echo "ANDROID_HOME=$ANDROID_HOME"
echo "SDK setup complete."
