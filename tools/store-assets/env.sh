#!/bin/bash
# 공통 경로. 다른 스크립트가 source 한다.
TOOLS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$TOOLS_DIR/../.." && pwd)"
OUT_DIR="$REPO_ROOT/store-assets"

ANDROID_SDK="${ANDROID_HOME:-$(grep '^sdk.dir' "$REPO_ROOT/local.properties" 2>/dev/null | cut -d= -f2)}"
ANDROID_SDK="${ANDROID_SDK:-$HOME/Library/Android/sdk}"
ADB="$ANDROID_SDK/platform-tools/adb"
EMULATOR="$ANDROID_SDK/emulator/emulator"
AVDMANAGER="$ANDROID_SDK/cmdline-tools/latest/bin/avdmanager"
SDKMANAGER="$ANDROID_SDK/cmdline-tools/latest/bin/sdkmanager"

# avdmanager · sdkmanager는 JDK 17 이상이 필요하다. 셸의 JAVA_HOME이 더 낮은 버전일 수 있어
# Android Studio에 든 JBR이 있으면 그것을 쓴다.
if [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
fi

# Pillow용 가상환경
VENV="$TOOLS_DIR/.venv"
PY="$VENV/bin/python"

export TOOLS_DIR REPO_ROOT OUT_DIR ANDROID_SDK ADB EMULATOR AVDMANAGER SDKMANAGER VENV PY
