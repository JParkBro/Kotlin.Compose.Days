#!/bin/bash
# 스토어용 에뮬레이터 3종을 창 없이 띄우고 부팅이 끝날 때까지 기다린다.
# 포트: phone 5570, tab7 5572, tab10 5574 (다른 에뮬레이터와 겹치지 않게 고정)
# 사용: boot_emulators.sh          # 띄우기
#       boot_emulators.sh stop     # 끄기
set -euo pipefail
source "$(dirname "$0")/env.sh"

NAMES=(days_store_phone days_store_tab7 days_store_tab10)
PORTS=(5570 5572 5574)
LOG_DIR="$TOOLS_DIR/.logs"
mkdir -p "$LOG_DIR"

if [ "${1:-}" = "stop" ]; then
  for port in "${PORTS[@]}"; do
    "$ADB" -s "emulator-$port" emu kill >/dev/null 2>&1 || true
  done
  echo "에뮬레이터 종료 요청"
  exit 0
fi

for i in 0 1 2; do
  name="${NAMES[$i]}"; port="${PORTS[$i]}"; serial="emulator-$port"
  if "$ADB" devices | grep -q "^$serial"; then
    echo "이미 실행 중: $name ($serial)"
    continue
  fi
  echo "시작: $name ($serial)"
  # -wipe-data: 매번 깨끗한 상태에서 시작해 같은 결과가 나오게 한다
  nohup "$EMULATOR" -avd "$name" -port "$port" -no-window -no-audio -no-boot-anim \
    -no-snapshot -wipe-data -gpu auto >"$LOG_DIR/$name.log" 2>&1 &
done

for port in "${PORTS[@]}"; do
  serial="emulator-$port"
  echo -n "부팅 대기: $serial "
  "$ADB" -s "$serial" wait-for-device
  for _ in $(seq 1 180); do
    if [ "$("$ADB" -s "$serial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; then
      break
    fi
    echo -n "."
    sleep 2
  done
  [ "$("$ADB" -s "$serial" shell getprop sys.boot_completed | tr -d '\r')" = "1" ] || { echo " 실패"; exit 1; }
  echo " 완료"
done
