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
├─ firebase/functions/ # Cloud Functions (TypeScript): 카카오 토큰 발급, 결제 자동 해제
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
| `settings:api` | `SettingsKey`, `ProfileEditKey` (커플 코드 발급에 이름이 필요해 커플 연결에서도 연다) | – |
| | `PolicyDocKey` (온보딩 약관 동의의 "보기"에서도 연다) | `type: PolicyType` |

### 4-2. impl 내부 키 (internal)

| 모듈 | 키 |
|---|---|
| `onboarding:impl` | `LoginKey`, `TermsKey`, `StartModeKey` |
| `couple:impl` | `PairCodeConfirmKey`, `AnniversaryListKey` |
| `settings:impl` | `NotificationSettingsKey`, `WidgetSettingsKey`, `AccountKey`, `WithdrawKey`, `PolicyListKey`, `MoreAppsKey` |

### 4-3. app에서 결정하는 목적지

| 상황 | 결과 타입 (api) | app의 처리 |
|---|---|---|
| 앱 시작 | – | 온보딩 미완료(로그아웃 또는 이 기기에서 약관 동의 전) → `OnboardingKey`, 완료 → `HomeKey` |
| 온보딩 완료 | `OnboardingResult.Solo` / `Couple` | `replaceAll(HomeKey)` / `replaceAll(HomeKey)` + `goTo(PairingKey)` (연결을 닫으면 홈) |
| 로그아웃 · 탈퇴 | `SessionEnded` | `replaceAll(OnboardingKey)` |

## 5. 기능 간 의존 관계

| impl | 의존하는 api |
|---|---|
| `onboarding:impl` | `settings` (정책 문서 보기). 끝나면 결과만 알리고 app이 목적지 결정 |
| `couple:impl` | `home`, `settings` (프로필 수정) |
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
| 내 코드 | 있음 | 복사 완료 스낵바, 연결 감지 시 이동 |
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
| 커플 관리 | 있음 | 연결 해제 완료, 자동 기념일 목록 이동 |
| 자동 기념일 | 있음 | 숨김 저장 실패, 연결이 끊기면 뒤로 |
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
| `syncModule` | core:sync — `SyncEngine` · `SyncManager`. Worker(`SyncPushWorker` · `SyncPullWorker`)는 Android 진입점이라 `KoinComponent`로 꺼낸다 (`WidgetMidnightWorker`와 같은 방식) |
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
   - 6-4a `firebase/functions/` (#26): `kakaoAuth`(카카오 토큰 → 커스텀 토큰, App Check 강제 · `maxInstances` 3), `stopBilling`(예산 알림 Pub/Sub → 결제 계정 연결 해제, `maxInstances` 1). 리전 `asia-northeast3`
   - 6-4b 앱 카카오 로그인 (#27): Kakao SDK(`core:data` `KakaoSignInClient`, 카카오톡 → 카카오 계정 순) → `kakaoAuth`(`core:firebase` `FirebaseFunctionsDataSource`) → `signInWithCustomToken`. 로그인 화면 `DaysKakaoButton`, 로그아웃 시 카카오 토큰도 끊음. 함수 실행 계정 `kakao-auth@`
   - 6-5a 동기화 스키마 (#28): Room `couple` · `anniversary_hide` · `sync_cursor`(+ 보이는 항목 = 내 나만 보기 + 내 ACTIVE 커플 공유 항목), `core:firebase` `SyncRemoteDataSource`(Firestore), 규칙: `serverUpdatedAt == request.time`, 숨긴 기념일 soft delete. `user`(프로필 캐시) 테이블은 채우는 단계(6-6 · 6-7)에서 추가
   - 6-5b `core:sync` (#29): `SyncEngine`(Push · Pull 한 단위) · `SyncManager`(로그인 + 포그라운드 + `sync_enabled`일 때만 리스너, 올릴 것이 생기면 `SyncPushWorker` 5초 디바운스, 연결된 커플이 있을 때만 `SyncPullWorker` 6시간). `core:firebase` `RemoteFlags`(Remote Config `sync_enabled`). `core:sync`는 `core:database` · `core:firebase`를 직접 참조한다 (feature가 아니라 허용). Push는 현재 계정이 고친, 연결된 커플 항목만 (`ItemSyncDao`)
   - 6-6a 프로필 (#31): Room `user`(내 프로필 + 상대 캐시), `ProfileRepository`(이름 1~10자 · 생일 선택), `core:firebase` `ProfileRemoteDataSource`(`users/{uid}` 이름 · 생일 merge), `core:sync` `ProfileSync`(내 프로필 Push, 이 기기에 없을 때만 1회 조회, 상대 프로필은 커플 연결 시 리스너). 프로필 수정 화면 · 설정 카드, 공통 `DaysDatePickerDialog`(core:ui)
   - 6-6b `feature:couple` (#32): 커플 연결(내 코드 · 코드 입력 6칸: 칸 선택 · 덮어쓰기) · 연결 확인(사귄 날 · 1일로 세기 · 함께한 날) · 커플 관리 · 해제 시트 · 30일 안 재연결. `core:data` `CoupleRepository` · `CoupleTransitions`(연결 · 해제 전환을 내 동작 · 동기화 리스너 모두 한 곳에서: `users.coupleId`, 내 코드 삭제, 공유 항목 사본 · 재연결 때 사본 정리), `core:firebase` `CoupleRemoteDataSource`(코드 · 연결 트랜잭션), Room `item.copied_from_item_id`, 설정 커플 뱃지 · 함께한 날. `core:sync`는 커플 전환 때문에 `core:data`를 참조
   - 6-7 커플 홈 · 공개 범위 · 공유 표시 (#33): 홈 커플 히어로(함께한 날) · 다음 일정 | 다음 기념일(진행 바) · 필터(전체 / 우리 / 나만) · 리스트 공개 범위 뱃지, 연결되면 연결 유도 카드 숨김. 디데이 · 일정 수정의 공개 범위(커플일 때만, 새 항목은 우리 둘이 기본, 공유한 항목은 나만 보기로 바꿀 수 없음 · `saveItem`도 막음). `core:data` `AnniversaryRepository`(숨김은 Room PENDING → 기존 동기화), `core:domain` `nextAnniversary`(생일 포함), 커플 관리 → 자동 기념일 목록(앞으로 1년, 숨김 토글). 캘린더의 자동 기념일 표시는 이후
   - 6-8a 사진 저장 · 업로드 기반 (#34): `firebase/storage.rules`(커플 멤버만 · webp · 300KB · 덮어쓰기 금지 · 방금 등록한 `lastImageId`만) + Firestore `usage/images` 카운터(+1씩 · 누적 30), Storage 에뮬레이터 9199. Room `image` · `user.profile_image_id`, `core:firebase` `ImageRemoteDataSource`, `core:data` `ImageStore`(WebP 1080px · 280KB 이하) · `ImageRepository`(가져오기 · 받아서 영구 저장 · 버리기), `core:sync` `ImageSync`(사진 → 프로필 → 공유 항목 순, 사진 올리기 전 항목은 기다림, `upload_enabled`) · `CoupleDataSync`(공유 항목 · 숨긴 기념일을 `SyncEngine`에서 분리). 썸네일 없음, 문서에는 사진 id만
   - 6-8b 사진 화면 (#35): Coil 3(`coil-compose`, 기기 파일만) · `core:ui` `DaysPhoto` · `ProfileAvatar` · `rememberPhotoPicker`(PickVisualMedia), `core:data` `ImageDraft`(고르는 중인 사진: 저장 뒤 이전 사진 · 취소 시 새 사진 버리기). 디데이 커버 사진 선택 · 상세 배경(image-scrim + on-image), 프로필 사진(수정 · 설정 카드 · 커플 관리 · 커플 홈 히어로, 홈은 `HomeCoupleSource`로 묶음), 위젯 사진 배경(`WidgetBackground.PHOTO` = 보여주는 디데이의 커버 사진) · 프로필 원(커플 두 사람 · 솔로 나), 위젯 설정 ViewModel 키는 `WidgetTarget`
7. `feature:onboarding`(소개 · 약관 · 시작 방식, 로그인은 6단계에서), `feature:settings`, `feature:notifications`, `core:notification`
   - 7-2 디데이 · 일정 · 자동 기념일 알림 (#37): `core:notification` `ReminderScheduler`(매일 오전 9시 WorkManager 1회 작업, 실행 · 시각 변경 때 다음 9시로 다시 예약, 앱 시작은 KEEP) · `ReminderWorker` · `ReminderDelivery`(로그인한 계정의 알림 + 연결된 커플의 숨기지 않은 자동 기념일 당일) · `ReminderNotifier`(채널 `anniversary` · `plan`, 태그로 중복 방지, 권한 없으면 건너뜀). `core:domain` `dueReminders`, `ItemRepository.observeAllReminders`, 발송 시각 `ItemReminder.SendTime`. 알림 탭은 `AppIntents.EXTRA_OPEN_ITEM_TYPE`로 일정이면 수정 화면. 디데이 · 일정 수정에서 알림을 켜면 `POST_NOTIFICATIONS` 요청(`core:ui` `rememberNotificationPermissionRequest`). 알림 설정 화면은 7-3, 알림 목록 · 상대 활동 알림(`activity_log`)은 7-4
   - 7-1 온보딩 (#36): 소개(3장, 기기마다 한 번: DataStore `intro_seen`, 본 뒤로는 로그인부터) → 로그인 → 약관 동의(필수 3 + 마케팅 선택, `users/{uid}`에 `termsVersion` · `privacyVersion` · `ageOver14` · `marketing` · `agreedAt` merge, `core:firebase` `TermsRemoteDataSource`) → 시작 방식(커플 추천 기본). `core:data` `OnboardingRepository`: 동의한 계정을 DataStore `terms_agreed_users`에 남기고, 기록이 없으면 로그인 직후 서버 동의 기록을 한 번 읽어(다시 설치 · 다른 기기) 이미 동의했으면 약관 · 시작 방식 없이 홈으로. 앱 시작은 로그인 + 이 기기 동의 기록이 있을 때만 홈. `PolicyDocKey` · `PolicyType`을 `settings:api`로 옮김 (정책 문서 화면은 9단계)
8. `core:ads`, 광고 제거 구매
9. `policy-site/`(Firebase Hosting), 출시 체크리스트 (`infra-and-billing-protection.md`)

## 10. widget 모듈 (Glance)

> 상태: **1차 완료.** 위젯 본체(#20), 위젯 설정 · 상세의 "위젯 추가"(#21), 사진 배경 · 프로필(#35). 기획은 `product-spec.md` 8장, 시안은 `design/settings-widget/`.

**모듈 · 빌드**
- [x] `:widget` 모듈 (`days.android.library` + `days.android.compose` + `days.koin`), catalog에 `glance-appwidget` · `glance-preview` · `work-runtime`
- [x] app에 `implementation(projects.widget)`, `appModule`에 `widgetModule`

**위젯**
- [x] 크기 2종: 작게(2×2) · 중간(4×2). 크기마다 `GlanceAppWidget` 하위 클래스 + 리시버 + `appwidget-provider` XML. 잠금화면은 Android 휴대폰이 지원하지 않아 뺌
- [x] 표시: 아이콘 · 제목 · D-day(+ 중간은 기준일). 고른 디데이가 없으면 홈 히어로와 같은 규칙(`core:domain` `featured`)
- [x] 배경: 테마색(기기 다크 모드를 따름) · 다크 · 화이트. 앱과 같은 팔레트
- [x] 탭: 앱을 `AppIntents.EXTRA_OPEN_ITEM_ID`와 함께 열고, `MainActivity`가 `[HomeKey, ItemDetailKey]` 백스택을 만든다 (`singleTop` + `onNewIntent`)
- [x] 사진 배경(보여주는 디데이의 커버 사진) · 프로필 사진 표시 (#35). 비트맵은 RemoteViews 한도 때문에 배경 720px · 프로필 96px로 줄여 읽는다

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
