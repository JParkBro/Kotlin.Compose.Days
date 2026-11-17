#!/bin/bash
# 스토어 스크린샷용 에뮬레이터 3종을 만든다 (이미 있으면 건너뜀).
#   days_store_phone  Pixel 7        1080x2400
#   days_store_tab7   Nexus 7 2013   1200x1920
#   days_store_tab10  Nexus 10       1600x2560 (세로로 고정)
# 시스템 이미지는 android-34 google_apis (adb root 가능, Play 서비스 포함)
set -euo pipefail
source "$(dirname "$0")/env.sh"

IMAGE="system-images;android-34;google_apis;arm64-v8a"

if [ ! -d "$ANDROID_SDK/system-images/android-34/google_apis/arm64-v8a" ]; then
  echo "시스템 이미지 설치: $IMAGE"
  yes | "$SDKMANAGER" "$IMAGE" >/dev/null
fi

create() {
  local name="$1" device="$2"
  if "$AVDMANAGER" list avd -c 2>/dev/null | grep -qx "$name"; then
    echo "이미 있음: $name"
    return
  fi
  echo "생성: $name ($device)"
  echo no | "$AVDMANAGER" create avd -n "$name" -k "$IMAGE" -d "$device" >/dev/null
  local cfg="$HOME/.android/avd/$name.avd/config.ini"
  # 키보드 · 프레임 끄기, 세로 시작
  sed -i '' -e '/^hw.keyboard=/d' -e '/^showDeviceFrame=/d' -e '/^hw.initialOrientation=/d' "$cfg"
  printf 'hw.keyboard=no\nshowDeviceFrame=no\nhw.initialOrientation=portrait\nhw.ramSize=2048\n' >> "$cfg"
}

create days_store_phone "pixel_7"
create days_store_tab7 "Nexus 7 2013"
create days_store_tab10 "Nexus 10"

# Nexus 10은 가로가 기본이라 LCD 크기를 바꿔 세로 기기로 만든다
cfg10="$HOME/.android/avd/days_store_tab10.avd/config.ini"
sed -i '' -e '/^hw.lcd.width=/d' -e '/^hw.lcd.height=/d' "$cfg10"
printf 'hw.lcd.width=1600\nhw.lcd.height=2560\n' >> "$cfg10"

"$AVDMANAGER" list avd -c 2>/dev/null | grep days_store
