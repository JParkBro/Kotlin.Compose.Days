#!/bin/bash
# 샘플 데이터용 Firebase 에뮬레이터 (Auth · Firestore · Storage). dev 빌드가 기본으로 여기에 연결한다.
# 함수는 쓰지 않는다 (로그인은 가짜 Google 토큰, 커플 연결은 Firestore 직접).
# 데이터는 저장하지 않아 매번 빈 상태로 시작한다.
# 사용: firebase_emulators.sh        # 띄우고 준비될 때까지 기다림
#       firebase_emulators.sh stop   # 끄기
set -euo pipefail
source "$(dirname "$0")/env.sh"

LOG_DIR="$TOOLS_DIR/.logs"
PID_FILE="$LOG_DIR/firebase.pid"
mkdir -p "$LOG_DIR"

if [ "${1:-}" = "stop" ]; then
  if [ -f "$PID_FILE" ]; then
    pkill -P "$(cat "$PID_FILE")" >/dev/null 2>&1 || true
    kill "$(cat "$PID_FILE")" >/dev/null 2>&1 || true
    rm -f "$PID_FILE"
  fi
  echo "Firebase 에뮬레이터 종료 요청"
  exit 0
fi

if curl -s -o /dev/null "http://127.0.0.1:9099" && curl -s -o /dev/null "http://127.0.0.1:8080"; then
  echo "Firebase 에뮬레이터 이미 실행 중"
  exit 0
fi

cd "$REPO_ROOT/firebase"
[ -x node_modules/.bin/firebase ] || npm install --silent
nohup npx firebase emulators:start --only auth,firestore,storage --project days-61328 \
  >"$LOG_DIR/firebase.log" 2>&1 &
echo $! >"$PID_FILE"

echo -n "Firebase 에뮬레이터 대기 "
for _ in $(seq 1 90); do
  if grep -q "All emulators ready" "$LOG_DIR/firebase.log" 2>/dev/null; then
    echo " 완료"
    exit 0
  fi
  echo -n "."
  sleep 2
done
echo " 실패 (로그: $LOG_DIR/firebase.log)"
exit 1
