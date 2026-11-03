# Cloud Functions for Firebase

설계 원문: `docs/days/infra-and-billing-protection.md` (2-1 카카오 로그인, 4-2 결제 자동 해제)

> 2026-11까지는 Cloudflare Worker(`workers/`)가 맡던 역할입니다. Firebase Blaze 한 곳으로 옮기면서 이 폴더로 바꿨습니다.

## 역할

| 함수 | 트리거 | 내용 |
|---|---|---|
| `kakaoAuth` | callable (`onCall`) | 카카오 액세스 토큰 검증(`access_token_info`의 `app_id` 확인) → `createCustomToken("kakao:{회원번호}")` |
| `stopBilling` | Pub/Sub (예산 알림 토픽) | 예산을 넘으면 Cloud Billing API로 프로젝트의 결제 계정 연결 해제 |

## 규칙

- **Blaze라 함수 호출 · 인스턴스가 곧 비용이다.** 모든 함수에 리전 `asia-northeast3`, 작은 `maxInstances`(`kakaoAuth` 3, `stopBilling` 1), 짧은 타임아웃을 둔다.
- 앱이 부르는 함수는 **App Check를 강제**한다 (`enforceAppCheck: true`). 인증이 필요한 함수는 `request.auth`를 확인한다.
- 카카오 앱 ID 등 설정 값은 환경 변수(파라미터), 비밀 값은 `firebase functions:secrets:set`으로만. 코드 · `.env` 커밋 금지.
- **서비스 계정 비공개 키 파일을 만들거나 저장하지 않는다.** 커스텀 토큰 서명은 함수 실행 서비스 계정(서비스 계정 토큰 생성자 권한)으로, 결제 해제는 결제 관리 권한만 준 서비스 계정으로.
- 외부 API(카카오) 응답은 신뢰하지 않고 필요한 필드만 검사한다. 에러는 `HttpsError` 코드로 돌려주고 내부 정보(스택, 키 이름)를 노출하지 않는다.
- 개인정보(토큰, 회원번호 원문)를 로그에 남기지 않는다.

## 명령어

> 아직 함수 코드가 없다 (6-4에서 추가). 추가할 때 실제 스크립트와 맞춰 이 표를 고친다.

| 목적 | 명령 |
|---|---|
| 설치 | `npm install` (`functions/` 안에서) |
| 타입 검사 | `npx tsc --noEmit` |
| 테스트 | `npm test` (에뮬레이터 대상) |
| 로컬 실행 | `npx firebase emulators:start --only functions,auth,firestore --project demo-days` |
| 배포 | 직접 실행하지 않는다. 사용자에게 확인 후 진행 |

## 완료 전 확인

- 타입 검사 · 테스트 통과
- 새 함수에 App Check · 인증 확인 · `maxInstances`가 빠지지 않았는지
- `docs/days/infra-and-billing-protection.md`와 동작이 일치하는지
