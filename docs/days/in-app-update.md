# 인앱 업데이트 (Play In-App Updates)

Play 스토어에 새 버전이 있으면 **앱 시작 시 컨펌 다이얼로그 → 수락하면 백그라운드 다운로드 → 다운로드가 끝나면 재시작 컨펌 → 설치**하는 구조. FLEXIBLE 방식만 쓴다.

AniPick(`docs/in-app-update.md`)의 구현을 데이즈 규칙(MVI `MviViewModel`, `strings.xml`, `suspendRunCatching`, 테마 토큰)에 맞춰 옮겼다. 코드는 `app/src/main/kotlin/jpark/bro/days/update/`.

## 한눈에 보기

```
onCreate (savedInstanceState == null) ── Action.AppStarted
   │  DOWNLOADED이면 ReadyToInstall로 (아래)
   │  UPDATE_AVAILABLE && isFlexibleUpdateAllowed
   │  오늘 "나중에"를 누른 적이 없을 때만
   ▼
prompt = UpdateAvailable(info) ──▶ "업데이트 안내"  [업데이트] [나중에 → None, 오늘 날짜 저장]
   │ 업데이트 (UpdateConfirmed)
   ▼
Event.StartUpdateFlow(info) ──▶ MainActivity: startUpdateFlowForResult(FLEXIBLE)
   │  Play 시스템 화면에서 동의 → 백그라운드 다운로드 (앱은 계속 사용 가능)
   ▼
InstallStateUpdatedListener: DOWNLOADED ──┐
onResume ── Action.AppResumed: DOWNLOADED ──┤
                                           ▼
prompt = ReadyToInstall ──▶ "설치 준비 완료"  [재시작] [나중에 → None, 다음 onResume에 다시 뜸]
   │ 재시작 (InstallConfirmed)
   ▼
Event.CompleteUpdate ──▶ MainActivity: appUpdateManager.completeUpdate() → 앱 재시작 · 설치
```

## 정해 둔 것

- **FLEXIBLE만 쓴다.** IMMEDIATE(전체 화면, 끝날 때까지 앱 사용 불가)는 쓰지 않는다. 강제 업데이트는 맨 아래 "확장"을 본다.
- **Play 화면을 바로 띄우지 않고 앱 다이얼로그로 먼저 묻는다.** 다운로드 후 재시작도 한 번 더 묻는다. 컨펌 없이 업데이트 · 재시작하지 않는다.
- **판단 · 상태는 `UpdateViewModel`, Play 호출은 MainActivity.** `startUpdateFlowForResult()`는 Activity에 묶인 `ActivityResultLauncher`가 필요하고 `completeUpdate()`는 앱을 재시작시키므로, ViewModel은 Event만 보내고 `UpdateRoot`가 Event를 받아 MainActivity가 넘긴 람다로 호출한다.
- **확인은 Action으로 받는다.** ViewModel 공개 멤버는 `state` · `event` · `onAction`뿐이라 AniPick의 `checkForUpdate()` · `checkDownloadedUpdate()`를 `AppStarted` · `AppResumed`로 바꿨다.
- **`AppStarted`는 `savedInstanceState == null`일 때만.** 회전 · 다크 모드 변경으로 Activity가 다시 만들어질 때 "나중에" 누른 안내가 다시 뜨지 않는다. ViewModel은 그대로라 떠 있던 다이얼로그는 유지된다.
- **"업데이트 안내"를 미루면 그날은 다시 묻지 않는다.** "나중에"(바깥 터치 · 뒤로 포함)를 누르면 그날 날짜를 `SettingsRepository` → `core:datastore` `AppUpdateDataStore`(`settings` 파일의 `update_prompt_dismissed_date`, `yyyy-MM-dd`)에 저장하고, 같은 날에는 `AppStarted`에서 띄우지 않는다. 날짜가 바뀐 뒤 첫 실행 때 다시 묻는다 (24시간이 아니라 날짜 기준, 오늘은 주입받은 `Clock`). 기기 설정이라 로그아웃 · 탈퇴해도 지우지 않는다. 저장값을 못 읽으면 미루지 않은 것으로 보고 묻는다.
- **"설치 준비 완료"를 미루면 앱으로 돌아올 때(`onResume`)마다 다시 묻는다.** 이미 다운로드된 업데이트는 설치를 계속 유도하라는 Play 권장 동작이라 날짜 제한을 걸지 않는다. "업데이트"를 눌렀을 때도 날짜를 저장하지 않아, 같은 날 다운로드가 끝나면 재시작을 물을 수 있다.
- **다운로드가 끝난 상태면 업데이트 안내 대신 재시작을 묻는다.** `AppStarted`도 `installStatus() == DOWNLOADED`를 먼저 본다.
- **실패는 조용히 넘긴다 (`suspendRunCatching`).** 업데이트 확인 실패가 앱 사용을 막으면 안 된다. Play에서 설치하지 않은 빌드(Android Studio 설치 등)는 실패하거나 `UPDATE_NOT_AVAILABLE`이 와서 아무것도 안 뜬다.
- **app 모듈에만 둔다.** feature 모듈은 이 기능을 모른다.
- **서버 사용량 없음.** Firebase를 부르지 않는다 (Play 스토어 앱과만 통신).
- 문구는 `app/src/main/res/values/strings.xml`의 `app_update_*`. 다이얼로그는 다른 확인 창과 같은 Material 3 `AlertDialog` + `surfaceRaised`.

## 파일 구성

```
app/src/main/kotlin/jpark/bro/days/
├── update/
│   ├── UpdateState.kt       # UpdateState(prompt) + UpdatePrompt: None / UpdateAvailable(info) / ReadyToInstall
│   ├── UpdateAction.kt      # AppStarted · AppResumed + 다이얼로그 버튼 4개
│   ├── UpdateEvent.kt       # StartUpdateFlow(info) · CompleteUpdate
│   ├── UpdateViewModel.kt   # MviViewModel, InstallStateUpdatedListener 등록 · 해제, 미룬 날 확인
│   └── UpdateRoot.kt        # State 수집 · Event 처리 · 다이얼로그 두 개 (+ Preview)
├── di/AppModule.kt          # AppUpdateManager single + UpdateViewModel
└── MainActivity.kt          # 결과 런처 필드, AppStarted · AppResumed, UpdateRoot
core/datastore/.../AppUpdateDataStore.kt         # 업데이트 안내 미룬 날 (SettingsDataStore와 같은 파일)
core/data/.../SettingsRepository.kt              # updatePromptDismissedDate · setUpdatePromptDismissedDate
app/src/test/.../update/UpdateViewModelTest.kt   # FakeAppUpdateManager + FakeSettingsRepository + Robolectric
```

## 의존성

`gradle/libs.versions.toml`의 `appUpdate = "2.1.0"`, `app-update` · `app-update-ktx`. `-ktx`는 `requestAppUpdateInfo()`(suspend)와 `isFlexibleUpdateAllowed` 때문에 쓴다. app은 `MviViewModel` · `ObserveEvents` 때문에 `core:ui`, `suspendRunCatching` 때문에 `core:common`을 참조한다.

## DI

```kotlin
single<AppUpdateManager> { AppUpdateManagerFactory.create(androidContext()) }
viewModelOf(::UpdateViewModel)
```

ViewModel(리스너)과 MainActivity(Play 호출)가 **같은 `AppUpdateManager` 인스턴스**를 써야 하므로 `single`.

## 알고 있는 동작 (버그 아님, 바꿀 때 참고)

| 상황 | 동작 | 바꾸려면 |
|---|---|---|
| "업데이트 안내"에서 "나중에" | 그날은 다시 뜨지 않는다. 날짜가 바뀐 뒤 첫 실행(`AppStarted`)에 다시 묻는다 | 간격을 바꾸려면 `UpdateViewModel.dismissedToday()`의 날짜 비교를 바꾼다 |
| "설치 준비 완료"에서 "나중에" | 앱으로 돌아올 때(`onResume`)마다 다시 뜬다. 다운로드된 업데이트는 설치를 계속 유도하라는 Play 권장 동작 | 덜 보이게 하려면 다이얼로그 대신 토스트 · 스낵바(액션 "재시작")로 |
| 다운로드 중 앱 종료 | Play가 다운로드를 계속하고, 다음 실행에서 `DOWNLOADED`를 잡아 재시작을 묻는다 | – |
| Play 업데이트 화면에서 취소 | 할 일 없음. 다음 실행 때 다시 묻는다 (런처 결과를 쓰지 않음) | – |
| 디버그 · Studio 설치 빌드 | 아무것도 안 뜬다 | – |

## 확인 방법

- **단위 테스트**: `./gradlew :app:testDevDebugUnitTest`. `FakeAppUpdateManager`로 `setUpdateAvailable()` → `startUpdateFlowForResult()` → `userAcceptsUpdate()` → `downloadStarts()` → `downloadCompletes()` 전이를 검증한다. Play Task 콜백이 메인 Looper로 와서 Robolectric `shadowOf(Looper.getMainLooper()).idle()`을 부르고, `DaysApplication`(Firebase 초기화)을 피하려고 `@Config(application = Application::class)`를 쓴다.
- **실기기**: 실제 다이얼로그 · 다운로드는 **Play에서 설치한 빌드**에서만 동작한다. 내부 앱 공유로 사용자가 확인한다.
  1. versionCode가 낮은 빌드와 높은 빌드를 둘 다 내부 앱 공유에 올린다.
  2. 기기의 Play 스토어 설정에서 "내부 앱 공유"를 켠다.
  3. 낮은 빌드 링크로 설치 → 높은 빌드 링크를 열기만 하고(설치 X) → 앱 실행.
  4. "업데이트 안내" → 업데이트 → 다운로드 → "설치 준비 완료" → 재시작 순서로 뜨는지 본다.
- 내부 테스트 트랙으로도 된다. 어느 쪽이든 **versionCode가 올라가 있어야** 업데이트로 인식된다.

## 확장 (지금은 안 함)

- **강제 업데이트**: 특정 versionCode 미만을 막아야 하면 Remote Config에 최소 versionCode를 두고, 그 미만이면 `AppUpdateType.IMMEDIATE` + 닫을 수 없는 다이얼로그. Play 우선순위(`updatePriority()`)는 출시 후 값을 못 바꾸므로 기준으로 쓰지 않는다.
- **안내 간격 늘리기**: 지금은 "업데이트 안내"를 미루면 다음 날 다시 묻는다. 더 늦추려면 저장한 날짜와 오늘의 차이로 비교하거나 `clientVersionStalenessDays()`(업데이트가 나온 지 며칠)를 함께 본다.
