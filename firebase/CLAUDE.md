# Firebase 설정

설계 원문: `docs/days/sync-and-pairing.md` (보안 규칙), `docs/days/infra-and-billing-protection.md` (Blaze 과금 방어)

## 파일

| 파일 | 내용 |
|---|---|
| `firestore.rules` | 보안 규칙 |
| `firestore.indexes.json` | 복합 인덱스 |
| `firebase.json` | 에뮬레이터 설정 |
| `tests/rules.test.js` | 보안 규칙 테스트 (vitest + `@firebase/rules-unit-testing`) |
| `package.json` · `.npmrc` | 테스트 도구 (버전 고정). `.npmrc`의 `legacy-peer-deps`는 npm 10 설치 오류 회피용 |

## 규칙

- **Blaze(종량제)라 규칙이 곧 과금 방어다.** 읽기 · 쓰기를 늘리는 규칙, 넓게 열린 규칙은 이유와 최악의 경우를 설명한다.
- 이미지는 Storage 규칙(`storage.rules`, 6-8에서 추가)으로만 접근한다: 커플 멤버만, `image/webp`, 300KB 이하, 커플당 개수 한도. 공개 읽기 금지.
- 커플 데이터(`couples/{coupleId}/**`)는 `members`에 포함된 사용자만 읽고 쓴다.
- 공유 항목은 `delete`를 허용하지 않는다 (soft delete만). `updatedBy == request.auth.uid`를 검사한다.
- 커플 생성은 `pairCodes`의 `usedBy`를 같은 트랜잭션에서 기록한 경우에만 허용한다 (`getAfter`).
- 규칙 안의 `get()` · `getAfter()`도 읽기로 과금되므로 호출 수를 늘리는 변경은 이유를 설명한다.
- 쿼리 리스너는 `serverUpdatedAt > cursor` 조건을 전제로 한다. 인덱스가 필요하면 `firestore.indexes.json`에 추가한다.

## 명령어

| 목적 | 명령 |
|---|---|
| 설치 | `npm install` (`firebase/` 안에서) |
| 에뮬레이터 실행 | `npx firebase emulators:start --project demo-days` |
| 규칙 테스트 | `npm test` (에뮬레이터를 띄워 vitest 실행, `demo-days` 가짜 프로젝트라 실제 Firebase에 닿지 않음. Java 11+ 필요) |
| 배포 | 직접 실행하지 않는다. 사용자에게 확인 후 진행 |

## 규칙을 바꿀 때 반드시 테스트할 시나리오

- 멤버는 공유 항목을 읽고 쓸 수 있다
- 비멤버 · 비로그인 사용자는 거부된다
- 만료된 코드, 이미 사용된 코드, 본인이 만든 코드로는 커플을 만들 수 없다
- 연결 해제(`DISCONNECTED`)된 커플에는 항목을 쓸 수 없다
- 공유 항목 삭제(delete)는 거부된다
