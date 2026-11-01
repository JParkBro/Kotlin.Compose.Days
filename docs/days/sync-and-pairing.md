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
- **만료 처리:** Firestore TTL은 유료 기능이므로, 만료 여부는 보안 규칙과 앱에서 검사하고 남은 코드 문서는 발급자 앱이 다음 실행 때 삭제합니다.

## 4. 보안 규칙 초안

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{db}/documents {

    function signedIn() { return request.auth != null; }
    function me() { return request.auth.uid; }
    function couple(id) { return get(/databases/$(db)/documents/couples/$(id)).data; }
    function isMember(id) { return signedIn() && me() in couple(id).members; }
    function isActive(id) { return couple(id).status == 'ACTIVE'; }

    match /users/{uid} {
      allow read: if signedIn() && (me() == uid ||
        (get(/databases/$(db)/documents/users/$(me())).data.coupleId != null &&
         get(/databases/$(db)/documents/users/$(me())).data.coupleId ==
         get(/databases/$(db)/documents/users/$(uid)).data.coupleId));
      allow write: if signedIn() && me() == uid;
    }

    match /pairCodes/{code} {
      allow get: if signedIn();
      allow list: if false;
      allow create: if signedIn()
        && request.resource.data.creatorUid == me()
        && request.resource.data.expiresAt > request.time.toMillis()
        && !('usedBy' in request.resource.data);
      // B가 코드를 "사용함"으로 표시 (한 번만, 본인 uid로만, 만료 전에만)
      allow update: if signedIn()
        && !('usedBy' in resource.data)
        && resource.data.creatorUid != me()
        && resource.data.expiresAt > request.time.toMillis()
        && request.resource.data.diff(resource.data).affectedKeys().hasOnly(['usedBy'])
        && request.resource.data.usedBy == me();
      // 발급자 본인 또는 만료된 코드만 삭제
      allow delete: if signedIn()
        && (resource.data.creatorUid == me()
            || resource.data.expiresAt < request.time.toMillis());
    }

    match /couples/{coupleId} {
      allow get, list: if signedIn() && me() in resource.data.members;
      allow create: if signedIn()
        && request.resource.data.members.size() == 2
        && request.resource.data.members[1] == me()
        && request.resource.data.members[0] != me()
        && request.resource.data.status == 'ACTIVE'
        && getAfter(/databases/$(db)/documents/pairCodes/$(request.resource.data.pairCode)).data.creatorUid
             == request.resource.data.members[0]
        && getAfter(/databases/$(db)/documents/pairCodes/$(request.resource.data.pairCode)).data.usedBy
             == me();
      allow update: if signedIn() && me() in resource.data.members
        && request.resource.data.members == resource.data.members;
      allow delete: if false;

      match /items/{itemId} {
        allow read: if isMember(coupleId);
        allow create, update: if isMember(coupleId) && isActive(coupleId)
          && request.resource.data.updatedBy == me()
          && request.resource.data.visibility == 'SHARED';
        allow delete: if false; // soft delete만 허용
      }

      match /hiddenAnniversaries/{ruleKey} {
        allow read, write: if isMember(coupleId);
      }
    }
  }
}
```

> 규칙 안의 `get()` 호출도 문서 읽기로 과금됩니다. 쿼리 리스너는 쿼리 단위로 한 번 평가되므로 부담이 크지 않지만, 단건 요청이 많아지면 연결 시 Worker가 서비스 계정으로 `coupleId`를 커스텀 클레임에 넣어 `get()`을 없애는 방식을 고려하세요. 이미지 업로드 시 Worker도 이 규칙으로 멤버 여부를 확인합니다 (`infra-and-billing-protection.md`).
> 이미 다른 커플에 연결된 사용자가 또 연결하는 것은 규칙만으로 막기 어려우므로, 앱에서 `users.coupleId`를 확인해 차단합니다.

## 5. 커플 연결 해제

1. 한쪽이 해제하면 `couples.status = DISCONNECTED`, `disconnectedAt` 기록.
2. 양쪽 앱이 이를 감지하면 공유 항목을 **개인 항목(PRIVATE, 새 UUID)으로 복사**합니다. "사본도 삭제"를 선택했다면 복사하지 않습니다.
3. 각자 `users.coupleId`를 null로 바꿉니다.
4. 상대 앱에는 로컬 알림으로 "연결이 해제되었어요"를 표시합니다.

### 30일 안에 재연결 시 복구

- 같은 두 사람이 다시 연결하면, 앱이 `members`가 같고 `disconnectedAt`이 30일 이내인 DISCONNECTED couple을 찾아 **기존 문서를 ACTIVE로 되돌립니다.** 공유 항목이 그대로 살아납니다.
- 30일이 지난 공유 데이터 정리는 서버 예약 작업이 필요합니다. Spark에서는 앱이 다음 실행 때 정리하거나, 데이터량이 작으므로 남겨두는 것도 방법입니다.

## 6. 회원 탈퇴

순서가 중요합니다. 인증 계정을 먼저 지우면 내 데이터를 지울 권한이 사라집니다.

1. 커플이 연결되어 있으면 연결 해제 처리 (5장)
2. 내가 올린 R2 이미지 삭제 (Worker `DELETE /images/...` 호출)
3. `users/{uid}` 문서 삭제
4. Firebase Auth 계정 삭제 (최근 로그인이 필요하면 재인증)
5. Room 데이터베이스 · DataStore 초기화
6. 카카오 연결 끊기(unlink) API 호출

공유 항목은 상대에게 사본으로 남길지 정책을 정해 약관 · 탈퇴 안내 화면에 명시합니다.
