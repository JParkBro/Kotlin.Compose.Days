# 동기화 · 커플 연결 설계

## 1. 기본 원칙

- **Room이 원본(Source of Truth).** 화면은 항상 Room만 구독합니다.
- **Firestore는 공유 데이터의 통로.** `visibility = SHARED`인 항목과 커플 정보만 오갑니다.
- **앱은 서버 없이 동작해야 합니다.** 오프라인에서도 추가 · 수정이 되고, 연결되면 밀어 올립니다.
- 원격 저장소 접근은 인터페이스로 분리합니다. 나중에 자체 서버로 옮길 때 구현체만 교체합니다.

```kotlin
interface SyncRemoteDataSource {
    suspend fun pushItems(items: List<ItemDto>)
    fun observeItems(coupleId: String, since: Long): Flow<List<ItemDto>>
    suspend fun createPairCode(code: String, expiresAt: Long)
    suspend fun joinCouple(code: String, me: String): CoupleDto
    // ...
}
// 1차: FirestoreSyncDataSource / 이후: ApiSyncDataSource
```

## 2. 동기화 흐름

### Push (내 변경 → 서버)

1. 사용자가 공유 항목을 저장하면 Room에 `syncState = PENDING`으로 기록합니다.
2. 네트워크가 있으면 즉시, 없으면 WorkManager가 연결 시점에 PENDING 항목을 batch로 업로드합니다.
3. 성공하면 `syncState = SYNCED`, 실패하면 `FAILED`로 두고 재시도합니다.
4. 삭제는 문서를 지우지 않고 `deletedAt`을 기록해서 올립니다 (soft delete).

### Pull (상대 변경 → 내 기기)

1. 앱이 포그라운드일 때만 리스너를 붙입니다.
2. 조건: `serverUpdatedAt > lastPulledAt` 정렬 `serverUpdatedAt`
3. 받은 항목을 Room에 upsert합니다. `deletedAt`이 있으면 로컬에서도 숨김 처리합니다.
4. 받은 항목 중 최대 `serverUpdatedAt`을 `sync_cursor.last_pulled_at`에 저장합니다.
5. 받은 항목의 `updatedBy`가 상대라면 로컬 활동 알림을 생성합니다 ("지민 님이 일정을 추가했어요").
6. 백그라운드에서는 WorkManager가 하루 몇 회 1회성 조회(get)로 같은 작업을 하고 위젯을 갱신합니다.

### 구현 (6-5)

- 실행 시점(`core:sync` `SyncManager`): 리스너는 로그인 + 앱이 화면에 떠 있음(`ProcessLifecycleOwner`) + Remote Config `sync_enabled`일 때만 붙이고, 하나라도 바뀌면 바로 해제합니다. 리스너는 내 커플 문서 · 연결된 커플의 공유 항목 · 숨긴 기념일 3개입니다. 올릴 것이 생기면 `SyncPushWorker`(5초 디바운스 · 네트워크 · 지수 백오프 30초, 5회까지), 연결된 커플이 있으면 `SyncPullWorker`(6시간 주기 1회성 조회 + Push)를 예약합니다. 위젯은 Room을 구독하므로 받은 변경이 그대로 반영됩니다.
- 프로필(6-6a, `ProfileSync`): 내 프로필은 저장하면 PENDING → Push가 `users/{uid}`에 이름 · 생일만 merge로 씁니다(`coupleId`는 커플 연결이 씀). 이 기기에 내 프로필이 없을 때만 1회 조회합니다(새 기기 로그인). 상대 프로필은 커플이 연결됐을 때만 리스너(포그라운드) · 1회성 조회(백그라운드)로 받습니다. 규칙상 연결 전에는 상대 문서를 읽을 수 없어, 커플 연결 확인 화면의 발급자 이름은 코드 문서에 함께 넣습니다(6-6b).
- Push 대상은 현재 계정이 마지막으로 고친(`updated_by`) 연결된 커플의 항목뿐입니다. 규칙이 `updatedBy == 나` · ACTIVE 커플만 허용하므로, 같은 기기의 다른 계정 변경은 그 계정이 로그인했을 때 올립니다.

- 원격: `core:firebase` `SyncRemoteDataSource`(Firestore 구현 `FirestoreSyncDataSource`). 리스너는 바뀐 문서(`documentChanges`)만, 이 기기에서 쓰고 서버에 아직 반영되지 않은 문서(`hasPendingWrites`)는 건너뜁니다. 상대가 쓴 문서는 필수 필드 · 타입을 확인하고 맞지 않으면 건너뜁니다.
- 커서: `sync_cursor.last_pulled_at_micros`. Firestore 서버 시각은 마이크로초라, 밀리초로 자르면 `>` 조건에 같은 문서가 매번 다시 걸려 읽기가 과금됩니다.
- `serverUpdatedAt`은 보안 규칙이 `request.time`만 허용합니다. 기기가 시각을 조작해 커서를 건너뛰는 것을 막습니다.
- Room: 화면에 보이는 항목 = 내 나만 보기 항목 + 내 ACTIVE 커플의 공유 항목(상대가 만든 것 포함). 수정 · 삭제도 같은 조건입니다.
- 받은 변경 반영(`upsertRemoteItems` · `upsertRemoteHides`)은 SYNCED 행만 덮습니다. 올린 뒤 SYNCED로 바꿀 때는 `updated_at`(숨긴 기념일은 `deleted_at`)이 그대로인 행만 바꿔, 올리는 사이 다시 저장한 변경을 잃지 않습니다.

### 시각 필드 두 개를 쓰는 이유

| 필드 | 누가 기록 | 용도 |
|---|---|---|
| `updatedAt` | 기기 시각 | 화면 표시, 충돌 시 비교 |
| `serverUpdatedAt` | Firestore `serverTimestamp()` | 동기화 커서. 기기 시각이 틀려도 변경분을 놓치지 않음 |

### 충돌 처리

- 두 사람이 같은 항목을 거의 동시에 수정하면 **나중에 저장한 쪽이 이깁니다 (Last-Write-Wins).**
- 커플 앱 특성상 동시 수정은 드물어서 필드 단위 병합까지는 필요하지 않습니다.
- 내 기기에 PENDING 상태인 항목이 Pull로 덮이지 않도록, PENDING 항목은 Push가 끝난 뒤 비교합니다.

## 3. 커플 코드 연결 (서버 함수 없이)

```
A (코드 발급)                         Firestore                          B (코드 입력)
──────────────                        ─────────                          ──────────────
코드 생성 (예: K7Q2MX)
pairCodes/K7Q2MX 작성  ───────────▶  { creatorUid: A,
                                       expiresAt: now+24h }
couples 쿼리 리스너 시작
(members array-contains A)
                                                                   코드 입력 → pairCodes/K7Q2MX 조회
                                                                   만료 확인, 생성자 프로필 확인 화면
                                                                   [연결하기]
                                     트랜잭션  ◀──────────────────  ① pairCodes/K7Q2MX.usedBy = B
                                     { members: [A, B],              ② couples/{새 UUID} 생성
                                       status: ACTIVE }                 { pairCode: K7Q2MX, startDate, ... }
리스너가 새 couple 감지  ◀─────────
pairCodes/K7Q2MX 삭제
users/A.coupleId 갱신                                              users/B.coupleId 갱신
```

- B는 같은 트랜잭션에서 코드에 `usedBy`를 기록하고 커플 문서를 만듭니다. 규칙이 `usedBy`를 한 번만 허용하므로, 같은 코드로 제3자가 연결하는 것을 막습니다.
- B가 A의 사용자 문서를 직접 수정하지 않아도 되므로 보안 규칙이 단순해집니다.
- 코드는 6자리 영문 대문자 + 숫자. 헷갈리는 문자(0/O, 1/I)는 제외합니다.
- 연결 시 기존 개인 항목은 자동 공유하지 않습니다. 사용자가 항목별로 공개 범위를 바꿉니다.
- 공유한 항목은 나만 보기로 되돌릴 수 없습니다 (`ItemRepository.saveItem`이 거부). 되돌리면 상대 기기와 서버에 공유본이 남기 때문입니다.
- **만료 처리:** Firestore TTL은 유료 기능이므로, 만료 여부는 보안 규칙과 앱에서 검사하고 남은 코드 문서는 발급자 앱이 다음 실행 때 삭제합니다.

## 4. 보안 규칙

원본은 `firebase/firestore.rules`, 테스트는 `firebase/tests/rules.test.js`(`cd firebase && npm test`, 에뮬레이터)입니다. 아래는 요약입니다.

| 경로 | 읽기 | 쓰기 |
|---|---|---|
| `users/{uid}` | 나, 또는 같은 커플에 연결된 상대 | 나만. `serverUpdatedAt == 서버 시각`. `coupleId`는 null이거나 **내가 멤버인 커플**만 (초안에서 추가: 남의 커플 id를 적어 상대 프로필을 읽는 것을 막음) |
| `pairCodes/{code}` | 코드를 아는 사람만 단건 조회, 목록 조회 불가 | 생성: 발급자 본인 · 만료 전 · `usedBy` 없이. 사용: 발급자가 아닌 사람이 만료 전 한 번만 `usedBy = 나`. 삭제: 발급자 또는 만료된 코드 |
| `couples/{id}` | 멤버만 | 생성: 코드를 쓴 사람이 같은 트랜잭션에서 `usedBy`를 기록했을 때만(`getAfter`). 수정: 멤버, 멤버 구성은 못 바꿈. 삭제 불가 |
| `couples/{id}/items/{itemId}` | 멤버 | 멤버 · ACTIVE · `updatedBy == 나` · `visibility == SHARED` · `serverUpdatedAt == 서버 시각`. 삭제 불가(soft delete) |
| `couples/{id}/hiddenAnniversaries/{ruleKey}` | 멤버 | 멤버 · ACTIVE · `serverUpdatedAt == 서버 시각`. 숨김 해제는 `deletedAt` 기록, 삭제 불가 (변경분 조회로 상대 기기에 전달) |


> 규칙 안의 `get()` 호출도 문서 읽기로 과금됩니다. 쿼리 리스너는 쿼리 단위로 한 번 평가되므로 부담이 크지 않지만, 단건 요청이 많아지면 연결 시 Cloud Functions가 `coupleId`를 커스텀 클레임에 넣어 `get()`을 없애는 방식을 고려하세요. 이미지는 Storage 보안 규칙이 `firestore.get()`으로 같은 커플 문서를 읽어 멤버 여부를 확인합니다 (`infra-and-billing-protection.md`).
> 이미 다른 커플에 연결된 사용자가 또 연결하는 것은 규칙만으로 막기 어려우므로, 앱에서 `users.coupleId`를 확인해 차단합니다.

## 5. 커플 연결 해제

1. 한쪽이 해제하면 `couples.status = DISCONNECTED`, `disconnectedAt` 기록.
2. 양쪽 앱이 이를 감지하면 공유 항목을 **개인 항목(PRIVATE, 새 UUID)으로 복사**합니다. "사본도 삭제"를 선택했다면 복사하지 않습니다.
3. 각자 `users.coupleId`를 null로 바꿉니다.
4. 상대 앱에는 로컬 알림으로 "연결이 해제되었어요"를 표시합니다.

### 구현 (6-6b)

- 연결 · 해제 전환은 `core:data` `CoupleTransitions` 한 곳에서 처리합니다. 내가 연결 · 해제했을 때(CoupleRepository)와 상대가 했을 때(동기화 리스너) 모두 이곳을 지나고, 로컬 상태가 바뀔 때만 한 번 실행합니다.
  - 연결됨: `users/{나}.coupleId` 기록, 내가 발급한 코드 문서 삭제, 재연결이면 해제 뒤 고치지 않은 사본 정리
  - 해제됨: 공유 항목을 나만 보기 사본(새 UUID, `copied_from_item_id` = 원본, `updated_at` = 해제 시각)으로 복사하고 내 대표 고정 · 알림을 사본으로 옮김. 내가 "사본도 삭제"를 고르면 복사하지 않음. `coupleId` 비움
- 코드 문서에 발급자 이름(`creatorNickname`, 1~10자)을 넣습니다. 연결 전에는 규칙상 상대 `users` 문서를 읽을 수 없어 확인 화면의 "{이름} 님과 연결할까요?"에 씁니다. 코드를 아는 사람만 읽을 수 있습니다.
- 내가 발급한 코드는 기기(DataStore)에 계정과 함께 기억해 24시간 안에 다시 보여주고, 새로 발급하거나 연결되면 서버에서도 지웁니다.
- 연결 · 관리 · 해제는 온라인일 때만 됩니다 (두 사람의 상태가 어긋나지 않게 오프라인 큐에 넣지 않음).

### 30일 안에 재연결 시 복구

- 같은 두 사람이 다시 연결하면, 앱이 `members`가 같고 `disconnectedAt`이 30일 이내인 DISCONNECTED couple을 찾아 **기존 문서를 ACTIVE로 되돌립니다.** 공유 항목이 그대로 살아납니다.
- 보안 규칙: 해제된 커플을 ACTIVE로 되돌리는 것은 상대가 발급한 코드에 같은 트랜잭션에서 `usedBy = 나`를 기록할 때만 허용합니다 (한쪽이 혼자 되살리는 것을 막음, 재연결 때만 `getAfter` 2회).
- 30일이 지난 공유 데이터 정리는 예약 함수(Cloud Scheduler + Functions)로 할 수 있지만, 데이터량이 작으므로 앱이 다음 실행 때 정리하거나 남겨두는 것으로 시작합니다.

## 6. 사진 동기화 (6-8a)

설계 원문: `infra-and-billing-protection.md` 2-2 · 2-3. 기기 파일(`filesDir/images/{id}.webp`)이 원본이고 Room `image`가 상태를 갖는다.

- **올릴 사진:** 연결된 커플의 공유 항목 배경, 커플일 때 내 프로필 사진만. 나만 보기 항목 · 솔로 프로필 사진은 올리지 않는다. 다른 커플에 올렸던 사진은 지금 커플에 다시 올린다.
- **Push 순서:** 사진 → 내 프로필 → 공유 항목 · 숨긴 기념일 (`SyncEngine`). 배경 사진을 아직 올리지 않은 공유 항목은 기다린다(`ItemSyncDao`). 프로필 문서에는 지금 커플에 올린 사진 id만 적고, 사진을 올린 뒤 프로필을 다시 올린다.
- **올리기:** 카운터 트랜잭션(`usage/images` `count` +1, `lastImageId`)으로 등록한 뒤 파일을 올린다. 올리기만 실패해 다시 시도할 때는 이미 마지막 등록 사진이므로 카운터를 또 쓰지 않는다. 두 사람이 동시에 올리면 한쪽이 다시 등록하며 카운터를 1 더 쓸 수 있다.
- **한도 초과:** 사진을 `REJECTED`로 두고 다시 시도하지 않는다. 항목은 사진 id와 함께 올라가지만 상대 기기에는 사진이 없다.
- **받기:** 받은 공유 항목 · 상대 프로필의 사진 id는 `image` 행으로 알아 두고, 화면에 보일 때 한 번 받아 영구 저장한다 (`ImageRepository.observeImageFile`). 상대가 쓴 id는 영문 · 숫자 · `-`만 받는다 (파일 경로 조작 방지).
- **지우기:** 사진을 바꾸거나 지우면 기기 파일을 바로 지우고, 내가 올린 서버 파일은 다음 Push가 지운다. 카운터는 줄이지 않는다.
- **차단 스위치:** Remote Config `upload_enabled`가 꺼지면 올리기만 멈춘다.

## 7. 회원 탈퇴

순서가 중요합니다. 인증 계정을 먼저 지우면 내 데이터를 지울 권한이 사라집니다.

1. 커플이 연결되어 있으면 연결 해제 처리 (5장)
2. 내가 올린 Storage 이미지 삭제 (Storage SDK `delete`, 규칙이 멤버만 허용)
3. `users/{uid}` 문서 삭제
4. Firebase Auth 계정 삭제 (최근 로그인이 필요하면 재인증)
5. Room 데이터베이스 · DataStore 초기화
6. 카카오 연결 끊기(unlink) API 호출

공유 항목은 상대에게 사본으로 남길지 정책을 정해 약관 · 탈퇴 안내 화면에 명시합니다.
