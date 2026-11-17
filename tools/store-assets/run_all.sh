#!/bin/bash
# 스토어 그래픽을 처음부터 끝까지 다시 만든다.
#   1) 에뮬레이터 3종 생성       2) dev debug APK + 계측 테스트 APK 빌드
#   3) Firebase 에뮬레이터 · 기기 부팅   4) 실제 화면 캡처 (샘플 데이터는 StoreSeedTest)
#   5) 아이콘 · 대표 그래픽       6) 스크린샷 합성      7) 규격 검증
# 사용: tools/store-assets/run_all.sh            # 전체
#       tools/store-assets/run_all.sh compose    # 캡처는 건너뛰고 아이콘 · 합성 · 검증만 (문구 · 디자인을 고쳤을 때)
set -euo pipefail
source "$(dirname "$0")/env.sh"
cd "$TOOLS_DIR"

if [ ! -x "$PY" ]; then
  python3 -m venv "$VENV"
  "$VENV/bin/pip" install -q -r requirements.txt
fi

if [ "${1:-all}" != "compose" ]; then
  ./create_avds.sh
  # dev debug: 로컬 Firebase 에뮬레이터에 연결하는 빌드 (app/google-services.json 필요)
  (cd "$REPO_ROOT" && ./gradlew :app:assembleDevDebug :app:assembleDevDebugAndroidTest --console=plain -q)
  ./firebase_emulators.sh
  ./boot_emulators.sh
  "$PY" capture.py
  ./boot_emulators.sh stop
  ./firebase_emulators.sh stop
fi

"$PY" graphics.py
"$PY" compose.py
"$PY" verify.py
