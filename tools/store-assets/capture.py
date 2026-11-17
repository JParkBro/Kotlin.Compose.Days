"""에뮬레이터 3종에서 실제 앱 화면을 캡처한다 → store-assets/raw/{phone,tablet-7,tablet-10}/NN_name.png

전제: boot_emulators.sh로 에뮬레이터가, firebase_emulators.sh로 Firebase 에뮬레이터가 떠 있고,
dev debug APK와 계측 테스트 APK가 빌드되어 있다 (run_all.sh가 순서대로 한다).

샘플 데이터는 앱의 계측 테스트(StoreSeedTest)가 실제 저장소 API로 넣는다:
가짜 Google 토큰으로 Auth 에뮬레이터에 로그인 → 약관 동의 → 프로필 → (커플이면) 코드 발급 · 연결 → 항목 저장.
기기 날짜는 product-spec.md 9장의 "오늘"(2026-11-05)로 맞춘다.

사용:
    python capture.py                 # 3종 모두 (동시에)
    python capture.py phone           # 하나만
"""

from __future__ import annotations

import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timedelta, timezone
from concurrent.futures import ThreadPoolExecutor

from adbui import REPO_ROOT, Device

PACKAGE = "jpark.bro.days.dev"
ACTIVITY = f"{PACKAGE}/jpark.bro.days.MainActivity"
APK = REPO_ROOT / "app/build/outputs/apk/dev/debug/app-dev-debug.apk"
TEST_APK = REPO_ROOT / "app/build/outputs/apk/androidTest/dev/debug/app-dev-debug-androidTest.apk"
RUNNER = f"{PACKAGE}.test/androidx.test.runner.AndroidJUnitRunner"
SEED_CLASS = "jpark.bro.days.storeassets.StoreSeedTest"
RAW_DIR = REPO_ROOT / "store-assets/raw"

DEVICES = {
    "phone": "emulator-5570",
    "tablet-7": "emulator-5572",
    "tablet-10": "emulator-5574",
}

# Firebase 에뮬레이터 포트 (firebase/firebase.json). local.properties의 firebase.emulator.host=127.0.0.1이면
# adb reverse로 Mac에 잇는다 (10.0.2.2를 쓰는 기본 설정이어도 해가 없다)
FIREBASE_PORTS = (9099, 8080, 5001, 9199)
FIREBASE_PROJECT = "days-61328"

KST = timezone(timedelta(hours=9))
SAMPLE_TODAY = "110509002026.00"  # 2026-11-05 (목) 09:00

DEMO = "am broadcast -a com.android.systemui.demo -e command"


def log(name: str, message: str) -> None:
    print(f"[{name}] {message}", flush=True)


def prepare(d: Device) -> None:
    """시계 · 시간대 · 애니메이션 · 키보드를 맞추고 앱을 새로 설치한다."""
    d.root()
    d.shell(
        "settings put global auto_time 0; settings put global auto_time_zone 0; "
        "cmd alarm set-timezone Asia/Seoul; "
        "settings put global window_animation_scale 0; "
        "settings put global transition_animation_scale 0; "
        "settings put global animator_duration_scale 0; "
        "settings put system screen_off_timeout 1800000; "
        "settings put secure show_ime_with_hard_keyboard 0"
    )
    # 소프트 키보드가 찍히지 않게 끈다
    for ime in d.shell("ime list -s").split():
        d.shell(f"ime disable {ime}", check=False)
    for port in FIREBASE_PORTS:
        d.adb("reverse", f"tcp:{port}", f"tcp:{port}")
    d.adb("uninstall", PACKAGE, check=False)
    d.adb("uninstall", f"{PACKAGE}.test", check=False)
    d.adb("install", "-r", str(APK))
    d.adb("install", "-r", str(TEST_APK))
    d.key(82)  # 잠금 화면 해제
    d.shell("wm dismiss-keyguard", check=False)


def home_package(d: Device) -> str:
    resolved = d.shell(
        "cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME",
        check=False,
    )
    component = resolved.strip().splitlines()[-1]
    if "/" not in component:
        raise RuntimeError(f"홈 앱을 찾지 못함: {resolved!r}")
    return component.split("/")[0]


def demo_mode(d: Device) -> None:
    """상태바: 시계 12:00, 배터리 100%, 알림 아이콘 없음."""
    d.shell("settings put global sysui_demo_allowed 1")
    for command in (
        "enter",
        "clock -e hhmm 1200",
        "battery -e level 100 -e plugged false",
        "network -e wifi show -e level 4 -e fully true",
        "network -e mobile show -e datatype none -e level 4 -e fully true",
        "notifications -e visible false",
        "status -e location hide -e alarm hide -e sync hide -e volume hide -e bluetooth hide",
    ):
        d.shell(f"{DEMO} {command} >/dev/null")


def set_clock(d: Device, value: str) -> None:
    """MMDDhhmmYYYY.ss (기기 시간대 기준)"""
    d.shell(f"date {value}")


def seed(d: Device, name: str, mode: str) -> None:
    """앱 데이터를 지우고 계측 테스트로 샘플 데이터를 넣는다.

    넣는 동안은 기기 시계를 실제 시각에 둔다: 보안 규칙이 서버 시각과 비교하는 값(커플 코드 만료 등)이 있다.
    다 넣은 뒤 product-spec 9장의 "오늘"(2026-11-05)로 옮긴다.
    """
    set_clock(d, datetime.now(KST).strftime("%m%d%H%M%Y.%S"))
    d.shell(f"am force-stop {PACKAGE}")
    d.shell(f"pm clear {PACKAGE}")
    d.shell(f"pm grant {PACKAGE} android.permission.POST_NOTIFICATIONS", check=False)
    tag = name.replace("-", "")
    output = d.shell(f"am instrument -w -r -e class {SEED_CLASS} -e mode {mode} -e tag {tag} {RUNNER}", check=False)
    if "OK (1 test)" not in output:
        raise RuntimeError(f"샘플 데이터 실패 ({mode}):\n{output[-2000:]}")
    stop(d)
    set_clock(d, SAMPLE_TODAY)
    log(name, f"샘플 데이터 ({mode}) 완료")


def launch(d: Device, extras: str = "") -> None:
    d.shell(f"am start -W -n {ACTIVITY} {extras} >/dev/null")


def stop(d: Device) -> None:
    d.shell(f"am force-stop {PACKAGE}")
    time.sleep(1)


def dismiss_system_dialogs(d: Device) -> None:
    """런처를 껐다 켤 때 태블릿에서 가끔 뜨는 "System UI keeps stopping" 같은 창을 닫는다."""
    for _ in range(3):
        for label in ("Close app", "Close", "Wait"):
            if d.has(label):
                d.tap_label(label)
                time.sleep(1)
                break
        else:
            return


def capture(d: Device, name: str, file_name: str) -> None:
    time.sleep(1.5)  # 그리기 · 동기화가 끝날 때까지
    dismiss_system_dialogs(d)
    demo_mode(d)  # System UI가 다시 시작되면 데모 모드가 풀린다
    time.sleep(1)
    path = RAW_DIR / name / file_name
    d.screencap(path)
    log(name, f"캡처 {path.relative_to(REPO_ROOT)}")


def scroll_to(d: Device, label: str) -> None:
    width, height = d.size()
    for _ in range(6):
        if d.has(label):
            return
        d.swipe(width // 2, int(height * 0.7), width // 2, int(height * 0.4))
    d.wait_for(label, timeout=2)


def capture_couple(d: Device, name: str, launcher: str | None) -> None:
    """커플(지민 · 현우, 핑크 라이트): 홈 · 캘린더 · 디데이 상세 · 홈 화면 위젯(휴대폰만)"""
    seed(d, name, "couple")
    launch(d)
    dismiss_system_dialogs(d)
    d.wait_for("현우 · 지민, 함께한 지", timeout=40)  # 상대 프로필이 동기화로 들어올 때까지
    capture(d, name, "01_couple_home.png")

    d.tap_label("캘린더")
    d.wait_for("이번 달")
    capture(d, name, "04_calendar.png")

    d.tap_label("홈")
    d.wait_for("함께한 지", exact=False)
    scroll_to(d, "처음 만난 날")
    d.tap_label("처음 만난 날")
    d.wait_for("다가오는 기념일")
    d.wait_for("벚꽃", exact=False)
    capture(d, name, "03_detail.png")

    # 홈 화면 위젯은 휴대폰만: 태블릿 런처는 첫 화면에 시계 위젯 · 다른 앱 아이콘이 가득해 자리가 없다
    if launcher:
        return
    # 상세의 "위젯 추가" → 중간 크기 → 런처의 추가 확인 → 홈 화면
    d.tap_label("위젯 추가")
    d.tap_label("중간 (4×2)")
    d.tap_label("Add to home screen", timeout=30)
    time.sleep(2)
    d.key(3)
    time.sleep(3)
    d.wait_for("D+558")
    capture(d, name, "05_widget.png")


def capture_solo(d: Device, name: str) -> None:
    """혼자(도윤, 네이비): 홈(라이트) · 캘린더(다크)"""
    seed(d, name, "solo")
    launch(d)
    d.wait_for("고정된 디데이")
    d.wait_for("전역일")
    capture(d, name, "02_solo_home.png")

    d.shell("cmd uimode night yes")
    time.sleep(2)
    d.tap_label("캘린더")
    d.wait_for("이번 달")
    capture(d, name, "06_dark_calendar.png")
    d.shell("cmd uimode night no")


def run_device(name: str) -> None:
    d = Device(DEVICES[name])
    log(name, f"준비 ({d.serial}, {d.size()})")
    prepare(d)
    d.shell("cmd uimode night no")
    demo_mode(d)
    # 태블릿은 앱 화면 아래에 런처 작업 표시줄(다른 앱 아이콘)이 나온다. 찍는 동안 런처를 끈다
    launcher = home_package(d) if name != "phone" else None
    if launcher:
        d.shell(f"pm disable-user --user 0 {launcher}", check=False)
        time.sleep(3)
    try:
        capture_couple(d, name, launcher)
        capture_solo(d, name)
    finally:
        if launcher:
            d.shell(f"pm enable {launcher}", check=False)
        d.shell(f"{DEMO} exit >/dev/null", check=False)
    log(name, "완료")


def reset_firebase_emulators() -> None:
    """지난 실행의 계정 · 문서를 지운다 (같은 계정에 커플이 여러 개 쌓이지 않게)."""
    base = "http://127.0.0.1"
    for url in (
        f"{base}:9099/emulator/v1/projects/{FIREBASE_PROJECT}/accounts",
        f"{base}:8080/emulator/v1/projects/{FIREBASE_PROJECT}/databases/(default)/documents",
    ):
        for attempt in range(5):
            try:
                urllib.request.urlopen(urllib.request.Request(url, method="DELETE"), timeout=30).read()
                break
            except urllib.error.URLError as error:  # 앱 리스너가 붙어 있으면 Firestore 에뮬레이터가 가끔 499를 준다
                if attempt == 4:
                    raise RuntimeError(f"Firebase 에뮬레이터 초기화 실패: {url}") from error
                time.sleep(2)


def main() -> None:
    for apk in (APK, TEST_APK):
        if not apk.exists():
            sys.exit(f"APK 없음: {apk}\n./gradlew :app:assembleDevDebug :app:assembleDevDebugAndroidTest 를 먼저 실행하세요.")
    names = sys.argv[1:] or list(DEVICES)
    for name in names:  # 앱이 리스너를 붙이고 있으면 초기화가 실패할 수 있어 먼저 멈춘다
        Device(DEVICES[name]).shell(f"am force-stop {PACKAGE}", check=False)
    reset_firebase_emulators()
    with ThreadPoolExecutor(max_workers=len(names)) as pool:
        futures = {name: pool.submit(run_device, name) for name in names}
    failed = False
    for name, future in futures.items():
        error = future.exception()
        if error:
            failed = True
            log(name, f"실패: {error}")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
