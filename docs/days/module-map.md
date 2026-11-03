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
├─ functions/        # Cloud Functions (TypeScript): 카카오 토큰 발급, 결제 자동 해제
├─ firebase/         # firestore.rules, firestore.indexes.json, firebase.json(에뮬레이터), 규칙 테스트
├─ policy-site/      # Firebase Hosting: 약관, 개인정보 처리방침, 오픈소스 라이선스
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
│  ├─ network/        Ktor: 외부 HTTP가 필요할 때 (카카오 토큰 발급은 Functions callable이라 core:firebase)
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
| | `ItemEditKey` | `itemId: String?`, `type: ItemType`, `presetDate: String?`, `presetCategory: Category?`, `presetTitle: String?` (빠른 시작) |
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
| `widget` | – (탭하면 `core:navigation`의 `AppIntents.EXTRA_OPEN_ITEM_ID`로 앱을 열고, app이 상세 백스택을 만든다) |

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
| 디데이 상세 | 있음 | 수정 이동, 뒤로(삭제됨), 위젯 추가 실패 안내 |
| 디데이 · 일정 추가 | 있음 | 저장 후 뒤로, 사진 선택기, 전면광고 |
| 알림 목록 | 있음 | 상세 이동 |
| 설정 | 있음 | 하위 화면 이동 |
| 위젯 설정 목록 | 있음 | 뒤로, 위젯 추가 실패 안내 |
| 위젯 하나 설정 (:widget Activity) | 있음 | 닫기, 저장 실패 안내 |
| 커플 관리 | 있음 | 연결 해제 완료 |
| 계정 관리 · 탈퇴 | 있음 | 결과 전달 (세션 종료) |
| 정책 문서 보기 | 없음 (`Nothing`) | – |

## 7. Koin 모듈

| 모듈 | 정의 위치 |
|---|---|
| `appModule` | app — Navigator, 결과 핸들러, 전체 `includes` |
| `databaseModule`, `datastoreModule` | core |
| `firebaseModule` | core:firebase — FirebaseAuth, Firestore, RemoteConfig, 원격 데이터 소스 |
| `networkModule` | core:network — HttpClient (외부 HTTP가 생기면) |
| `dataModule`, `domainModule` | core |
| `syncModule` | core:sync — SyncManager, `workerOf(::SyncWorker)` |
| `notificationModule`, `adsModule` | core |
| `{feature}Module` × 7 | 각 impl |
| `widgetModule` | widget |

## 8. 빌드 변형

| flavor | Firebase 프로젝트 | 카카오 네이티브 앱 키 | 앱 ID |
|---|---|---|---|
| `dev` | `days-61328` (에뮬레이터 연결 옵션) | `KAKAO_NATIVE_APP_KEY_DEV` (`local.properties`) | `jpark.bro.days.dev` |
| `prod` | `days-61328` | `KAKAO_NATIVE_APP_KEY_PROD` (`local.properties`) | `jpark.bro.days` |

> 처음에는 dev · prod Firebase 프로젝트를 나누기로 했지만, 지금은 **한 프로젝트를 함께 씁니다** (2026-11). 개발 중 사용량 · 데이터가 운영과 섞이므로 디버그 빌드는 에뮬레이터 연결을 우선하고, 출시 전에 분리를 다시 검토합니다 (`infra-and-billing-protection.md` 출시 전 체크리스트). 카카오는 앱 하나에 네이티브 앱 키를 패키지별로 2개 둡니다 (회원번호는 같음).

## 9. 개발 순서

1. `core:model`, `core:common`, `core:domain` (디데이 계산 + 테스트)
2. `core:designsystem` (테마 토큰 8 팔레트)
3. `core:database`, `core:datastore`, `core:data`
   - `core:database` v1: `item`, `user_item_pref`, `item_reminder` + DAO 테스트 (#11). 나머지 테이블은 해당 단계에서 추가
   - `core:data` `ItemRepository` · `ItemMapper` + Repository 테스트 (#14). 사용자 id는 로그인 전까지 `LocalUserSession`(기기 UUID), 로그인을 만들면 Firebase uid로 교체
4. `feature:home`, `feature:item`, `feature:calendar` → **솔로 모드 완성**
5. `widget` (아래 10장 TODO)
6. 커플 모드. 커플 연결에 로그인(Firebase uid)이 필요해서 7단계의 로그인을 앞당긴다
   - 6-1 `firebase/` 보안 규칙 · 에뮬레이터 테스트 (#23)
   - 6-2 `core:firebase` (#24): `FirebaseSetup`(App Check: prod Play Integrity · dev 디버그, Firestore 메모리 캐시, 에뮬레이터 연결), dev 빌드는 기본으로 에뮬레이터 (`local.properties` `firebase.useEmulator` · `firebase.emulator.host`). SDK 인스턴스는 Koin에 직접 등록하지 않고 6-3부터 데이터 소스로 감싼다
   - 6-3 구글 로그인 (#25): `FirebaseAuthDataSource`(core:firebase), `AuthRepository` · `GoogleSignInClient`(Credential Manager, `default_web_client_id`) · `FirebaseUserSession`(core:data). `LocalUserSession` · `SessionDataStore` 제거. 첫 화면은 로그인 여부로 결정(홈 / 온보딩). 7단계 전까지 온보딩은 로그인부터, 로그인하면 혼자 시작. 로그아웃해도 기기 데이터는 남기고 항목 · 대표 고정 · 알림을 계정(`owner_user_id`, `user_id`)별로 걸러 보여준다 (커플 공유 항목 조건은 6-5)
   - 6-4 `functions/` `kakaoAuth`(카카오 토큰 → 커스텀 토큰) + 카카오 로그인, `stopBilling`(결제 자동 해제)
   - 6-5 `core:sync` (Room couple · user · anniversary_hide · sync_cursor, Push · Pull)
   - 6-6 `feature:couple` (코드 발급 · 입력 · 확인 · 관리)
   - 6-7 커플 홈 · 공개 범위 · 공유 표시
   - 6-8 이미지 (Cloud Storage + Storage 규칙): 배경 사진 · 프로필
7. `feature:onboarding`(소개 · 약관 · 시작 방식, 로그인은 6단계에서), `feature:settings`, `feature:notifications`, `core:notification`
8. `core:ads`, 광고 제거 구매
9. `policy-site/`(Firebase Hosting), 출시 체크리스트 (`infra-and-billing-protection.md`)

## 10. widget 모듈 (Glance)

> 상태: **1차 완료.** 위젯 본체(#20), 위젯 설정 · 상세의 "위젯 추가"(#21). 사진 배경 · 프로필은 이후 단계. 기획은 `product-spec.md` 8장, 시안은 `design/settings-widget/`.

**모듈 · 빌드**
- [x] `:widget` 모듈 (`days.android.library` + `days.android.compose` + `days.koin`), catalog에 `glance-appwidget` · `glance-preview` · `work-runtime`
- [x] app에 `implementation(projects.widget)`, `appModule`에 `widgetModule`

**위젯**
- [x] 크기 2종: 작게(2×2) · 중간(4×2). 크기마다 `GlanceAppWidget` 하위 클래스 + 리시버 + `appwidget-provider` XML. 잠금화면은 Android 휴대폰이 지원하지 않아 뺌
- [x] 표시: 아이콘 · 제목 · D-day(+ 중간은 기준일). 고른 디데이가 없으면 홈 히어로와 같은 규칙(`core:domain` `featured`)
- [x] 배경: 테마색(기기 다크 모드를 따름) · 다크 · 화이트. 앱과 같은 팔레트
- [x] 탭: 앱을 `AppIntents.EXTRA_OPEN_ITEM_ID`와 함께 열고, `MainActivity`가 `[HomeKey, ItemDetailKey]` 백스택을 만든다 (`singleTop` + `onNewIntent`)
- [ ] 사진 배경 · 프로필 사진 표시 (이미지 저장소 · 커플 프로필 단계)

**설정 · 데이터**
- [x] Room `widget_config` (widget_id, item_id, size, background_type, show_profile), `WidgetConfigRepository`. 위젯을 지우면 설정도 지움
- [x] 갱신: 항목 · 대표 고정 · 테마 · 위젯 설정이 바뀌면 전체 갱신(`WidgetUpdater`, 앱 프로세스가 살아 있는 동안), 자정마다 WorkManager 1회 작업을 다시 예약, 시각 · 시간대 변경 브로드캐스트
- [x] 위젯 설정 화면 `WidgetConfigActivity`(:widget): 미리보기 · 표시할 디데이(자동 포함) · 배경 · 저장. 런처(길게 눌러 설정, Android 11은 놓을 때) · 앱 설정 목록에서 연다. 넘어온 위젯 id가 이 앱 위젯인지 확인
- [x] 놓을 때는 설정 없이 바로(`widgetFeatures="reconfigurable|configuration_optional"`), 놓이면 기본 설정 저장(`saveConfigIfAbsent`)
- [x] 앱 설정 → 위젯 설정(`feature:settings`): 놓인 위젯 목록 → 누르면 설정 화면, 없으면 안내 + "위젯 추가"
- [x] 디데이 상세의 "위젯 추가": 크기 고르기 → `requestPinAppWidget`, 놓이면 `WidgetPinnedReceiver`가 그 디데이로 설정
- [x] feature는 widget 모듈을 모르므로 `core:data`의 `HomeWidgets` 인터페이스로 쓰고 구현은 widget 모듈

**검증**
- [x] 크기별 Glance Preview, 표시 규칙 · 자정 예약 시간(자정 · 윤년 · 서머타임) 테스트
- [x] 고른 디데이가 삭제되면 "다시 골라주세요" 안내
- [ ] 공유 항목이 연결 해제됐을 때 위젯 표시 처리 (커플 모드 단계)
