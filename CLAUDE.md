# Days (데이즈)

커플 · 개인 디데이 앱. 솔로 모드가 기본이고, 커플 코드로 연결하면 "우리 둘이" 항목을 공유한다. Android 전용.

## 저장소 구조

| 경로 | 내용 | 세부 지침 |
|---|---|---|
| 루트 (`app/`, `core/`, `feature/`, `widget/`, `build-logic/`) | Android 앱 (Kotlin, Compose, 멀티 모듈) | 이 파일의 "Android 앱" 섹션 |
| `workers/` | Cloudflare Worker (TypeScript): 카카오 토큰 발급, 이미지 관문 | `workers/CLAUDE.md` |
| `firebase/` | Firestore 보안 규칙 · 인덱스 · 에뮬레이터 설정 | `firebase/CLAUDE.md` |
| `policy-site/` | Cloudflare Pages: 약관 · 개인정보 처리방침 · 라이선스 | – |
| `e2e/` | Maestro 흐름 | – |
| `docs/common/` | 범용 규칙 (아키텍처, 컨벤션, 테스트, Git) | – |
| `docs/days/` | 이 프로젝트의 기획 · 데이터 · 인프라 결정 | – |

## 문서 지도

작업 전에 관련 문서를 읽는다. 문서와 코드가 다르면 **문서가 기준**이고, 의도적으로 바꿔야 하면 먼저 사용자에게 확인한다.

| 작업 | 먼저 읽을 문서 |
|---|---|
| 화면 · 기능 구현 | `docs/days/product-spec.md`, `docs/days/module-map.md` |
| 모듈 추가 · 구조 변경 | `docs/common/ANDROID_ARCHITECTURE.md`, `docs/days/module-map.md` |
| 코드 작성 전반 | `docs/common/CODING_CONVENTIONS.md` |
| 테스트 작성 | `docs/common/TESTING.md` |
| UI · 색상 | `docs/days/theme-guide.md`, 값 원본 `docs/days/assets/theme-tokens.json` |
| DB · 데이터 모델 | `docs/days/data-model-erd.md` |
| 동기화 · 커플 연결 | `docs/days/sync-and-pairing.md` |
| Firebase · Cloudflare · 과금 | `docs/days/infra-and-billing-protection.md`, `docs/days/firebase-cost-strategy.md` |
| 광고 | `docs/days/ads-strategy.md` |
| 브랜치 · 커밋 · PR | `docs/common/GIT_CONVENTIONS.md` |

## 절대 어기면 안 되는 결정

- **Firebase는 Spark(무료) 플랜.** Cloud Functions, Firebase Storage, Firestore TTL, Identity Platform 업그레이드를 쓰는 코드나 설정을 추가하지 않는다. 필요해 보이면 Cloudflare 쪽 대안을 제안한다.
- **Room이 원본.** 화면은 Room만 구독한다. Firestore는 `visibility = SHARED` 항목과 커플 정보만 오간다.
- **"나만 보기"(PRIVATE) 데이터는 서버로 보내지 않는다.**
- **R2는 Worker를 통해서만 접근한다.** presigned URL, 공개 버킷을 만들지 않는다.
- **ID는 클라이언트 생성 UUID 문자열**, 날짜는 `yyyy-MM-dd`(LocalDate), 시각은 epoch millis. 자동 증가 ID 금지.
- **자동 기념일(100일 단위 · 주년 · 생일)은 저장하지 않고 계산한다.** 숨긴 것만 저장.
- **대표 고정 · 알림 설정은 사용자별 데이터**(`user_item_pref`, `item_reminder`)이며 공유하지 않는다.
- **색은 테마 토큰으로만.** `Color(0xFF…)` 하드코딩 금지.
- 문서의 "범위 밖" 기능(커플 다이어리, 사진 아카이브, 오늘의 문구, 백업 메뉴)을 임의로 추가하지 않는다.

## 작업 방식

- 여러 모듈에 걸친 변경, DB 스키마 변경, 보안 규칙 변경은 **먼저 계획을 보여주고** 진행한다.
- 기획이 문서에 없거나 모호하면 추측해서 구현하지 말고 질문한다.
- 결정이 바뀌면 관련 `docs/` 문서도 같은 PR에서 고친다. 공통 규칙에서 벗어나는 결정은 `docs/days/README.md`의 "공통 문서와 다르게 가는 부분"에 기록한다.
- 작업을 끝내기 전에 해당 영역의 "완료 전 확인"을 실행한다. Android는 이 파일의 "Android 앱" 섹션, Worker · Firebase는 `workers/CLAUDE.md` · `firebase/CLAUDE.md`.

## Git

- 브랜치: `{type}/{이슈번호}-{영문-요약}` (예: `feat/12-home-next-plan`)
- 커밋 · PR 제목: `[Type] scope: 요약` (예: `[Feat] home: 다음 일정 카드 추가`)
  - Type: `Feat` `Fix` `Design` `Refactor` `Perf` `Test` `Docs` `Build` `CI` `Chore` `Revert`
  - 요약은 한국어, "~ 추가 / 수정 / 제거 / 변경"으로 끝낸다
- `main`에 직접 커밋 · push하지 않는다. force push는 본인 작업 브랜치에만 `--force-with-lease`.
- 훅을 우회하는 `--no-verify`를 쓰지 않는다.
- 포맷 변경과 로직 변경을 한 커밋에 섞지 않는다.

## 비밀 정보

다음을 커밋하거나 코드에 넣지 않는다. 필요하면 환경 변수 · `local.properties` · GitHub Secrets · `wrangler secret`을 쓴다.

- 키스토어(`*.jks`, `*.keystore`)와 비밀번호
- 서비스 계정 JSON, API 비밀 키, 토큰
- `local.properties`, `.env`, `.dev.vars`

개인정보(토큰, 이메일, 사용자 입력 원문)를 로그에 남기지 않는다.

## 언어

- 사용자에게 하는 설명, 커밋 메시지, PR 본문, 문서: **한국어**
- 코드 식별자, 패키지, 파일명: 영어
- 앱 UI 문자열: `strings.xml`에 한국어로 추가 (하드코딩 금지)

## Android 앱

규칙 원문: `docs/common/ANDROID_ARCHITECTURE.md`, `docs/common/CODING_CONVENTIONS.md`, `docs/common/TESTING.md`
이 프로젝트의 모듈 · 키 · 의존 관계: `docs/days/module-map.md`

### 스택

Kotlin · Jetpack Compose · Navigation 3 · Koin(`koin-compose-navigation3`) · Ktor(OkHttp) · Room · DataStore · WorkManager · Glance · Firebase(Auth, Firestore, Remote Config, App Check) · AdMob

### 명령어

> 빌드 변형은 `dev` / `prod` flavor × `debug` / `release`. 작업 이름이 실제와 다르면 `./gradlew tasks`로 확인하고 이 표를 고친다.

| 목적 | 명령 |
|---|---|
| 디버그 빌드 | `./gradlew :app:assembleDevDebug` |
| 전체 단위 테스트 | `./gradlew testDevDebugUnitTest` |
| 특정 모듈 테스트 | `./gradlew :feature:home:impl:testDevDebugUnitTest` |
| 포맷 자동 수정 | `./gradlew ktlintFormat` |
| 정적 분석 | `./gradlew ktlintCheck detekt lintDevDebug` |
| 스크린샷 비교 | `./gradlew verifyRoborazziDevDebug` |
| 스크린샷 기준 갱신 | `./gradlew recordRoborazziDevDebug` (의도한 UI 변경일 때만) |
| DB 마이그레이션 테스트 | `./gradlew :core:database:connectedDevDebugAndroidTest` (기기 필요) |

### 모듈 규칙

- 기능은 `feature:{name}:api`(NavKey만) / `feature:{name}:impl`(구현)로 나눈다.
- **impl은 다른 기능의 api만 참조한다. impl → impl 금지, api → api 금지.**
- api에는 `@Serializable` NavKey만 둔다. 외부에서 들어올 화면의 키만 공개하고, 내부 화면 키는 impl에 `internal`.
- feature는 `core:data`(Repository)로만 데이터에 접근한다. `core:database`, `core:firebase`, `core:network`를 직접 참조하지 않는다.
- `core:model`, `core:common`, `core:domain`은 순수 Kotlin(JVM) 모듈. Android 의존을 넣지 않는다.
- 디데이 계산 · 자동 기념일 규칙은 `core:domain`에만 둔다. 화면 · 위젯 · 알림이 모두 이것을 쓴다.

### 화면 (MVI)

화면 하나 = 아래 파일 묶음 (`feature/{name}/impl/.../{name}/impl/`)

```
{Feature}Action.kt     sealed interface — 사용자가 한 일 (SaveClicked, TitleChanged)
{Feature}State.kt      data class — 모든 프로퍼티 val + 기본값
{Feature}Event.kt      sealed interface — 일회성 효과 (NavigateBack, ShowSnackbar). 없으면 파일 생략, 타입 인자는 Nothing
{Feature}ViewModel.kt  MviViewModel<S, A, E> 상속
{Feature}Screen.kt     internal {Feature}Root + private {Feature}Screen + private Preview(라이트·다크)
di/{Feature}Module.kt  viewModelOf + navigation<{Feature}Key>
```

- ViewModel 공개 멤버는 `state`, `event`, `onAction`뿐. 화면 입력은 **`onAction` 하나로만** 받는다.
- `onAction`과 Root의 Event 처리 `when`에는 **`else`를 쓰지 않는다.**
- 인자 없는 Action · Event는 `data object`, 있으면 `data class`.
- ViewModel은 `Context`, `Navigator`, Compose 타입을 모른다. **화면 이동은 Event → Root에서 처리.**
- 계속 보여야 하는 것(다이얼로그 표시, 입력값)은 State, 한 번만 일어나는 것은 Event.
- Root는 ViewModel 연결 · State 수집 · Event 처리만 한다. 레이아웃은 Screen에.
- Screen은 `state`, `onAction`, `modifier: Modifier = Modifier`를 받고 ViewModel을 모른다.

### Navigation 3 · Koin

- 이동은 `Navigator`(`core:navigation`)의 `goTo` / `goBack` / `replaceAll`.
- `NavDisplay`의 `entryDecorators`에 `rememberViewModelStoreNavEntryDecorator()`를 빼지 않는다.
- Koin은 생성자 주입만. 일반 클래스에서 `KoinComponent` · `get()` 사용 금지 (위젯 리시버 등 Android 진입점만 예외).
- `@KoinExperimentalAPI` 사용 시 `@OptIn`은 모듈 정의 파일에만.
- Koin 모듈을 추가 · 수정하면 `KoinModulesTest`(`verify()`)가 통과해야 한다.
- 온보딩 완료 · 로그아웃 후 목적지는 impl이 아니라 app의 결과 핸들러가 결정한다.

### 데이터

- Repository: 스트림은 `observe…(): Flow<T>`, 1회성은 `suspend … : Result<T>`. 예외를 ViewModel까지 던지지 않는다.
- suspend 블록 예외 처리는 `runCatching` 대신 `suspendRunCatching` (CancellationException을 삼키지 않기 위해).
- 공유 항목 저장은 Room에 `sync_state = PENDING`으로 쓰고 SyncManager가 올린다. 화면에서 Firestore를 직접 호출하지 않는다.
- 삭제는 soft delete(`deleted_at`). Firestore 문서를 실제로 지우지 않는다.
- Room 컬럼명은 snake_case + `@ColumnInfo(name = …)`. 스키마를 바꾸면 Migration + 스키마 JSON 커밋 + 마이그레이션 테스트.
- 현재 시각은 주입받은 `Clock`, 디스패처도 주입받는다.

### UI

- 색은 `AppThemeTokens.colors.*`만 사용. 보조 버튼 · 트랙 · 비활성 칩은 `fill`.
- 디데이 숫자: 대표 · 다음 기념일은 `primary`, 리스트는 `textPrimary`, 7일 이내 `danger`, 지난 기념일 `textTertiary`. 숫자에 tabular-nums.
- 문자열은 `strings.xml` (`{feature}_{screen}_{purpose}`), 아이콘에 `contentDescription`.
- `LazyColumn` 항목에 `key`.
- 테스트에서 찾을 요소에 `Modifier.testTag("snake_case")`.

### 테스트

코드를 바꾸면 함께 작성 · 수정한다.

| 바꾼 것 | 필요한 테스트 |
|---|---|
| `core:domain` 규칙 | 경계값 포함 단위 테스트 (윤년, 월말, 1일 세기) |
| ViewModel | 주요 Action의 State · Event 테스트 (성공 · 실패) |
| Screen UI | Preview 추가 · 수정 → 스크린샷 기준 갱신 |
| DB 스키마 | Migration 테스트 |
| Koin 모듈 | `verify()` 통과 |

- Fake는 `core:testing`에 두고 재사용한다. Mock보다 Fake 우선.
- 테스트 이름은 백틱 한국어 문장: `` `제목이 비어 있으면 저장 버튼 비활성` ``.
- 샘플 데이터는 `docs/days/product-spec.md` 9장 시나리오(오늘 = 2026.11.05)를 쓴다.

### 새 기능(화면) 추가 순서

1. `settings.gradle.kts`에 `:feature:{name}:api`, `:feature:{name}:impl` 추가
2. api: NavKey / impl: Action · State · (Event) · ViewModel · Screen.kt · di 모듈
3. 이동할 기능의 api만 impl 의존성에 추가
4. app: impl 의존성 + `appModule`의 `includes`에 모듈 추가
5. ViewModel 테스트, Preview, Koin verify
6. `docs/days/module-map.md`의 키 · 의존 표 갱신

### 완료 전 확인

```bash
./gradlew ktlintFormat
./gradlew ktlintCheck detekt
./gradlew testDevDebugUnitTest      # 변경 범위가 작으면 해당 모듈만
```

UI를 바꿨다면 `verifyRoborazziDevDebug` 결과를 확인하고, 의도한 변경이면 기준 이미지를 갱신한다.
