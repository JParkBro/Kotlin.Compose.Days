# Firebase 설정

설계 원문: `docs/days/sync-and-pairing.md` (보안 규칙), `docs/days/firebase-cost-strategy.md` (Spark 제약)

## 파일

| 파일 | 내용 |
|---|---|
| `firestore.rules` | 보안 규칙 |
| `firestore.indexes.json` | 복합 인덱스 |
| `firebase.json` | 에뮬레이터 설정 |
| `tests/` | 보안 규칙 테스트 |

## 규칙

- **Spark 플랜에서 동작해야 한다.** Cloud Functions, Storage, TTL 정책을 추가하지 않는다.
- 커플 데이터(`couples/{coupleId}/**`)는 `members`에 포함된 사용자만 읽고 쓴다.
- 공유 항목은 `delete`를 허용하지 않는다 (soft delete만). `updatedBy == request.auth.uid`를 검사한다.
- 커플 생성은 `pairCodes`의 `usedBy`를 같은 트랜잭션에서 기록한 경우에만 허용한다 (`getAfter`).
- 규칙 안의 `get()` · `getAfter()`도 읽기로 과금되므로 호출 수를 늘리는 변경은 이유를 설명한다.
- 쿼리 리스너는 `serverUpdatedAt > cursor` 조건을 전제로 한다. 인덱스가 필요하면 `firestore.indexes.json`에 추가한다.

## 명령어

| 목적 | 명령 |
|---|---|
| 에뮬레이터 실행 | `firebase emulators:start` |
| 규칙 테스트 | `npm test` (에뮬레이터 대상, `@firebase/rules-unit-testing`) |
| 배포 | 직접 실행하지 않는다. 사용자에게 확인 후 진행 |

## 규칙을 바꿀 때 반드시 테스트할 시나리오

- 멤버는 공유 항목을 읽고 쓸 수 있다
- 비멤버 · 비로그인 사용자는 거부된다
- 만료된 코드, 이미 사용된 코드, 본인이 만든 코드로는 커플을 만들 수 없다
- 연결 해제(`DISCONNECTED`)된 커플에는 항목을 쓸 수 없다
- 공유 항목 삭제(delete)는 거부된다
