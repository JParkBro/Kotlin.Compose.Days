# 모듈 구성 (데이즈 적용)

공통 문서 [ANDROID_ARCHITECTURE.md](../common/ANDROID_ARCHITECTURE.md)의 구조를 데이즈에 적용한 결과입니다. 패턴 · 규칙 설명은 공통 문서를 따르고, 이 문서는 **데이즈의 실제 모듈 · 키 · 의존 관계**만 정리합니다.

## 1. 자리표시자 값

| 자리표시자 | 값 |
|---|---|
| `{app}` | `days` |
| `{base.package}` | `jpark.bro.days` |

## 2. 저장소 구조

```
days/
├─ app/ core/ feature/ widget/ build-logic/   # Android 앱 (루트가 Gradle 프로젝트)
├─ workers/          # Cloudflare Worker (TypeScript): 카카오 토큰, 이미지 관문
├─ firebase/         # firestore.rules, firestore.indexes.json, firebase.json(에뮬레이터), 규칙 테스트
├─ policy-site/      # Cloudflare Pages: 약관, 개인정보 처리방침, 오픈소스 라이선스
├─ e2e/              # Maestro 흐름
└─ docs/
   ├─ common/        # 범용 문서
   └─ days/          # 이 프로젝트 문서
```

## 3. Android 모듈

Gradle 프로젝트는 저장소 루트에 있습니다 (`android/` 폴더를 따로 두지 않음).

```
days/
├─ app/
├─ build-logic/
├─ core/
│  ├─ model/          [JVM] Item, Couple, User, Anniversary 등
│  ├─ common/         [JVM] Result, 날짜 유틸, suspendRunCatching, Clock
│  ├─ domain/         [JVM] 디데이 계산, 자동 기념일 생성, 다음 일정 선택
│  ├─ navigation/     Navigator, 탭별 백스택 (홈 / 캘린더 / 설정)
│  ├─ designsystem/   AppColors(27 토큰) · 8 팔레트, 공통 컴포넌트
│  ├─ ui/             MviViewModel, ObserveEvents, DdayRow · 뱃지 등 공유 UI
│  ├─ database/       Room (user, item, couple, user_item_pref, item_reminder, image, anniversary_hide, activity_log, widget_config, sync_cursor)
│  ├─ datastore/      테마 · 모드, 광고 노출 기록, 온보딩 완료 여부
│  ├─ firebase/       Auth, Firestore 원격 데이터 소스, Remote Config, App Check
│  ├─ network/        Ktor: Worker API (카카오 토큰, 이미지)
│  ├─ data/           Repository: Item, Couple, User, Image, Settings
│  ├─ sync/           SyncManager, 동기화 · 재시도 Worker
│  ├─ notification/   기념일 · 일정 로컬 알림, 상대 활동 알림
│  ├─ ads/            AdMob 래퍼, 노출 빈도 규칙
│  └─ testing/        Fake, MainDispatcherRule, 테스트 데이터
├─ feature/
│  ├─ onboarding/  api/ impl/
│  ├─ couple/      api/ impl/
│  ├─ home/        api/ impl/
│  ├─ calendar/    api/ impl/
│  ├─ item/        api/ impl/
│  ├─ notifications/ api/ impl/
│  └─ settings/    api/ impl/
└─ widget/           Glance 위젯, 위젯 설정 Activity
```

공통 구조에 추가된 데이즈 전용 모듈: `core:firebase`, `core:sync`, `core:notification`, `core:ads`, `widget`.

## 4. 내비게이션 키

### 4-1. api에 공개하는 키

| 모듈 | 키 | 인자 |
|---|---|---|
| `onboarding:api` | `OnboardingKey` | – |
| `couple:api` | `PairingKey` | – |
| | `CoupleManageKey` | – |
| `home:api` | `HomeKey` | – |
| `calendar:api` | `CalendarKey` | – |
| `item:api` | `ItemDetailKey` | `id: String` |
| | `ItemEditKey` | `itemId: String?`, `type: ItemType`, `presetDate: String?` |
| `notifications:api` | `NotificationsKey` | – |
| `settings:api` | `SettingsKey` | – |

### 4-2. impl 내부 키 (internal)

| 모듈 | 키 |
|---|---|
| `onboarding:impl` | `LoginKey`, `TermsKey`, `StartModeKey` |
| `couple:impl` | `PairCodeConfirmKey` |
| `settings:impl` | `ProfileEditKey`, `NotificationSettingsKey`, `WidgetSettingsKey`, `AccountKey`, `WithdrawKey`, `PolicyListKey`, `PolicyDocKey(type)` |

### 4-3. app에서 결정하는 목적지

| 상황 | 결과 타입 (api) | app의 처리 |
|---|---|---|
| 앱 시작 | – | 온보딩 미완료 → `OnboardingKey`, 완료 → `HomeKey` |
| 온보딩 완료 | `OnboardingResult.Solo` / `Couple` | `replaceAll(HomeKey)` / `replaceAll(PairingKey)` |
| 로그아웃 · 탈퇴 | `SessionEnded` | `replaceAll(OnboardingKey)` |

## 5. 기능 간 의존 관계

| impl | 의존하는 api |
|---|---|
| `onboarding:impl` | (결과만 알림, app이 목적지 결정) |
| `couple:impl` | `home` |
| `home:impl` | `item`, `couple`, `notifications` |
| `calendar:impl` | `item` |
| `item:impl` | – |
| `notifications:impl` | `item` |
| `settings:impl` | `couple` (커플 관리 · 연결) |
| `widget` | `item` (위젯 탭 시 상세로 진입하는 딥링크) |

## 6. 화면별 MVI 구성

| 화면 | Event | 주요 Event |
|---|---|---|
| 소개 | 있음 | 다음 화면 이동 |
| 로그인 | 있음 | 로그인 성공 이동, 에러 스낵바 |
| 약관 동의 | 있음 | 정책 문서 열기, 완료 이동 |
| 시작 방식 | 있음 | 결과 전달 |
| 내 코드 | 있음 | 복사 완료 스낵바, 카카오톡 공유, 연결 감지 시 이동 |
| 코드 입력 | 있음 | 확인 화면 이동, 에러 |
| 연결 확인 | 있음 | 홈 이동 |
| 홈 | 있음 | 상세 · 추가 · 알림 · 커플 연결 이동 |
| 캘린더 | 있음 | 상세 · 추가 이동 |
| 디데이 상세 | 있음 | 수정 이동, 공유 시트, 위젯 추가 |
| 디데이 · 일정 추가 | 있음 | 저장 후 뒤로, 사진 선택기, 전면광고 |
| 알림 목록 | 있음 | 상세 이동 |
| 설정 | 있음 | 하위 화면 이동 |
| 커플 관리 | 있음 | 연결 해제 완료 |
| 계정 관리 · 탈퇴 | 있음 | 결과 전달 (세션 종료) |
| 정책 문서 보기 | 없음 (`Nothing`) | – |

## 7. Koin 모듈

| 모듈 | 정의 위치 |
|---|---|
| `appModule` | app — Navigator, 결과 핸들러, 전체 `includes` |
| `databaseModule`, `datastoreModule` | core |
| `firebaseModule` | core:firebase — FirebaseAuth, Firestore, RemoteConfig, 원격 데이터 소스 |
| `networkModule` | core:network — HttpClient(토큰 · App Check 헤더), WorkerApi |
| `dataModule`, `domainModule` | core |
| `syncModule` | core:sync — SyncManager, `workerOf(::SyncWorker)` |
| `notificationModule`, `adsModule` | core |
| `{feature}Module` × 7 | 각 impl |
| `widgetModule` | widget |

## 8. 빌드 변형

| flavor | Firebase 프로젝트 | Worker | 앱 ID |
|---|---|---|---|
| `dev` | days-dev (에뮬레이터 연결 옵션) | `wrangler dev` 또는 dev Worker | `jpark.bro.days.dev` |
| `prod` | days-prod | prod Worker | `jpark.bro.days` |

Firebase 프로젝트는 dev · prod를 분리해 개발 중 실수가 실제 데이터 · 사용량에 닿지 않게 합니다.

## 9. 개발 순서

1. `core:model`, `core:common`, `core:domain` (디데이 계산 + 테스트)
2. `core:designsystem` (테마 토큰 8 팔레트)
3. `core:database`, `core:datastore`, `core:data`
   - `core:database` v1: `item`, `user_item_pref`, `item_reminder` + DAO 테스트 (#11). 나머지 테이블은 해당 단계에서 추가
   - `core:data` `ItemRepository` · `ItemMapper` + Repository 테스트 (#14). 사용자 id는 로그인 전까지 `LocalUserSession`(기기 UUID), 로그인을 만들면 Firebase uid로 교체
4. `feature:home`, `feature:item`, `feature:calendar` → **솔로 모드 완성**
5. `widget` (아래 10장 TODO)
6. `core:firebase`, `core:network`, `core:sync`, `feature:couple`, `workers/`, `firebase/` 규칙 → **커플 모드**
7. `feature:onboarding`, `feature:settings`, `feature:notifications`, `core:notification`
8. `core:ads`, 광고 제거 구매
9. `policy-site/`, 출시 체크리스트 (`infra-and-billing-protection.md`)

## 10. TODO: widget 모듈 (Glance)

> 상태: **미착수.** 솔로 모드(9장 4단계)를 마친 뒤 만든다. 기획은 `product-spec.md` 8장, 시안은 `design/settings-widget/`.

**모듈 · 빌드**
- [ ] `:widget` 모듈 추가 (`days.android.library` + `days.android.compose` + `days.koin`), `settings.gradle.kts` include
- [ ] catalog에 `androidx.glance:glance-appwidget`(+ `glance-material3`) 추가
- [ ] app에 `implementation(projects.widget)`, `appModule`에 `widgetModule` 포함

**위젯**
- [ ] 크기 3종: 작게(2×2) · 중간(4×2) · 잠금화면 — `GlanceAppWidget` + `GlanceAppWidgetReceiver`, `appwidget-provider` XML(크기별 `minWidth/minHeight`, `previewLayout`)
- [ ] 표시: 디데이 제목 · D-day 숫자 · 기준일. 계산은 `core:domain`만 사용 (화면 · 위젯 · 알림 공통)
- [ ] 배경: 사진 · 테마색 · 다크 · 화이트, 프로필 사진 표시 여부. 색은 테마 토큰만 (`theme-guide.md`)
- [ ] 탭 시 디데이 상세로 이동: `item:api`의 `ItemDetailKey` 딥링크 → app에서 백스택 구성

**설정 · 데이터**
- [ ] 위젯 설정 Activity (위젯 추가 시 표시할 디데이 · 배경 선택), 설정 화면의 `WidgetSettings`와 같은 값 사용
- [ ] Room `widget_config` (widget_id, item_id, size, background_type, show_profile) — `data-model-erd.md`
- [ ] 갱신: 항목 수정 · 날짜 변경(자정) 시 `GlanceAppWidgetManager`로 업데이트, 필요하면 WorkManager 주기 작업
- [ ] 디데이 상세의 "위젯 추가" Event: `AppWidgetManager.requestPinAppWidget`

**검증**
- [ ] 위젯 크기별 Preview(Glance), 날짜 경계(자정 · 윤년) 갱신 테스트
- [ ] 공유 항목이 삭제 · 연결 해제됐을 때 위젯 표시 처리
