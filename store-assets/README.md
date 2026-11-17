# 스토어 그래픽 (Google Play Console)

모든 파일은 `tools/store-assets/`의 스크립트로 만든다. 스크린샷은 실제 앱(dev debug 빌드)을 에뮬레이터에서 캡처한 것이다.

## 어디에 올리나

Play Console → 성장 → 스토어 등록정보 → 기본 스토어 등록정보 → 그래픽

| 파일 | Play Console 칸 | 규격 |
|---|---|---|
| `icon/icon-512.png` | 앱 아이콘 | 512x512, 32비트 PNG(알파), 1MB 이하 |
| `feature-graphic/feature-1024x500.png` | 그래픽 이미지 | 1024x500, 24비트 PNG |
| `screenshots/phone/01.png` ~ `06.png` | 휴대전화 스크린샷 (순서대로) | 1080x1920 |
| `screenshots/tablet-7/01.png` ~ `05.png` | 7인치 태블릿 스크린샷 | 1440x2560 |
| `screenshots/tablet-10/01.png` ~ `05.png` | 10인치 태블릿 스크린샷 | 1800x3200 |

스크린샷 순서와 문구:

| 번호 | 화면 (원본 `raw/`) | 문구 |
|---|---|---|
| 01 | 커플 홈 `01_couple_home` | 둘이 함께한 날을 한눈에 (라벨: 둘이 쓸 때) |
| 02 | 혼자 쓰는 홈 · 네이비 테마 `02_solo_home` | 혼자여도 충분한 디데이 (라벨: 혼자 쓸 때) |
| 03 | 디데이 상세 · 다가오는 기념일 `03_detail` | 기념일은 알아서 챙겨요 |
| 04 | 캘린더 `04_calendar` | 둘의 약속도 달력 하나에 |
| 05 | 홈 화면 위젯 `05_widget` (휴대폰만) | 앱을 열지 않아도 바로 |
| 06 | 캘린더 · 다크 모드 `06_dark_calendar` | 내 취향대로 고르는 테마 |

태블릿은 05(위젯)가 없어 06이 05번이 된다.

## 그 밖의 파일

- `icon/options/`: 아이콘 시안 3개(`a-calendar` · `b-ring` · `c-two-days`, PNG · SVG)와 비교 시트 `overview.png`. 최종본은 A
- `icon/android-res/`: 최종 아이콘의 앱 리소스 (적응형 아이콘 + 단색 테마 아이콘 + 해상도별 PNG). `app/src/main/res/`와 같은 폴더 구조이고,
  **2026-11-17에 앱에 적용했다** (이전 하트 · "DAYS" 아이콘의 webp는 지움, `app/src/main/ic_launcher-playstore.png`도 교체).
  아이콘을 다시 바꾸면 `graphics.py`를 돌린 뒤 같은 방법으로 덮어 쓴다 (같은 이름의 webp가 남아 있으면 png와 충돌)
- 위젯 원본(`raw/phone/05_widget.png`)은 아이콘 적용 전에 찍어 독에 이전 아이콘이 있다. 합성본에서는 화면 밖으로 잘려 보이지 않는다.
  다음에 `run_all.sh`를 돌리면 새 아이콘으로 다시 찍힌다
- `raw/`: 합성 전 원본 캡처 (기기 해상도 그대로)

## 다시 만들기

```bash
tools/store-assets/run_all.sh            # 전체: 에뮬레이터 생성 → 빌드 → Firebase 에뮬레이터 · 기기 부팅 → 캡처 → 아이콘 · 대표 그래픽 → 합성 → 검증
tools/store-assets/run_all.sh compose    # 캡처는 건너뛰고 아이콘 · 대표 그래픽 · 합성 · 검증만 (문구 · 디자인만 고쳤을 때)
```

단계별로:

```bash
cd tools/store-assets
./create_avds.sh                 # days_store_phone · days_store_tab7 · days_store_tab10 (android-34 google_apis)
./firebase_emulators.sh          # Auth · Firestore · Storage 에뮬레이터 (stop으로 끄기)
./boot_emulators.sh              # 기기 3종 (포트 5570 · 5572 · 5574, stop으로 끄기)
.venv/bin/python capture.py [phone|tablet-7|tablet-10]
.venv/bin/python graphics.py     # 아이콘 시안 · 최종 · android-res · 대표 그래픽
.venv/bin/python compose.py      # 스크린샷 합성 (문구는 compose.py의 SCREENS)
.venv/bin/python verify.py       # 크기 · 형식 · 알파 · 용량 표
```

필요한 것: `app/google-services.json`, Android SDK(에뮬레이터 · android-34 google_apis 이미지), Node(`firebase/`의 Firebase CLI), Java, Google Chrome(아이콘 · 대표 그래픽 렌더링).

## 캡처 방식

- 샘플 데이터: 계측 테스트 `app/src/androidTest/.../storeassets/StoreSeedTest.kt`가 앱의 실제 저장소 API로 넣는다.
  Auth 에뮬레이터에 가짜 Google 토큰으로 로그인 → 약관 동의 → 프로필 → (커플) 상대 계정이 코드 발급 · 내가 연결 → 항목 저장.
  내용은 `docs/days/product-spec.md` 9장 시나리오(지민 · 현우 커플, 혼자 쓰는 도윤). 실제 개인정보 없음
- 기기 시계: 데이터를 넣을 때는 실제 시각(보안 규칙이 서버 시각과 비교), 캡처할 때는 2026-11-05 09:00 (9장의 "오늘")
- 상태바: 시스템 UI 데모 모드 (12:00, 배터리 100%, 알림 아이콘 없음). 애니메이션 · 키보드 끔
- 태블릿은 캡처하는 동안 런처를 꺼서 화면 아래 작업 표시줄(다른 앱 아이콘)이 찍히지 않게 한다
- 위젯 캡처는 휴대폰만: 상세의 "위젯 추가"로 실제 런처에 추가한 화면이다. 아래쪽 독(다른 앱 아이콘)은 합성할 때 기기를 크게 놓아 화면 밖으로 밀어낸다
