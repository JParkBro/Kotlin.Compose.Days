# Cloud Functions for Firebase

설계 원문: `docs/days/infra-and-billing-protection.md` (2-1 카카오 로그인, 2-5 보상형 광고, 4-2 결제 자동 해제)

> 2026-11까지는 Cloudflare Worker(`workers/`)가 맡던 역할입니다. Firebase Blaze 한 곳으로 옮기면서 이 폴더로 바꿨습니다.
> 런타임 Node 22, TypeScript 5.9, `firebase-functions` 7 · `firebase-admin` 14, 테스트 vitest.

## 역할

| 함수 | 트리거 | 내용 |
|---|---|---|
| `kakaoAuth` | callable (`onCall`) | 카카오 액세스 토큰 검증(`access_token_info`의 `app_id` 확인) → `createCustomToken("kakao:{회원번호}")` |
| `admobReward` | HTTPS (`onRequest`, AdMob SSV 콜백) | AdMob 서명(ECDSA) · 광고 단위 확인 → 커플 멤버 · 거래 id 한 번만 · bonus 상한 확인 후 `couples/{id}/usage/images.bonus` +1 |
| `stopBilling` | Pub/Sub (예산 알림 토픽) | 예산을 넘으면 Cloud Billing API로 프로젝트의 결제 계정 연결 해제 |

## 규칙

- **Blaze라 함수 호출 · 인스턴스가 곧 비용이다.** 모든 함수에 리전 `asia-northeast3`, 작은 `maxInstances`(`kakaoAuth` 3, `admobReward` · `stopBilling` 1), 짧은 타임아웃을 둔다.
- 앱이 부르는 함수는 **App Check를 강제**한다 (`enforceAppCheck: true`). 인증이 필요한 함수는 `request.auth`를 확인한다.
- 예외: `admobReward`는 AdMob 서버가 부르므로 App Check를 걸 수 없다. 서명이 맞지 않으면 Firestore에 닿기 전에 거부하고, 서명이 맞으면 처리 여부와 상관없이 200(AdMob 재전송 방지), 일시 오류만 500.
- 카카오 앱 ID 등 설정 값은 환경 변수(파라미터), 비밀 값은 `firebase functions:secrets:set`으로만. 코드 · `.env` 커밋 금지.
- **서비스 계정 비공개 키 파일을 만들거나 저장하지 않는다.** 커스텀 토큰 서명은 전용 실행 계정 `kakao-auth@`(자기 자신에 대한 서비스 계정 토큰 생성자 권한만)으로, 결제 해제는 결제 관리 권한만 준 서비스 계정으로, 보상 반영은 Firestore 쓰기 권한(`roles/datastore.user`)만 준 `admob-reward@`으로.
- 외부 API(카카오) 응답은 신뢰하지 않고 필요한 필드만 검사한다. 에러는 `HttpsError` 코드로 돌려주고 내부 정보(스택, 키 이름)를 노출하지 않는다.
- 개인정보(토큰, 회원번호 원문, uid · coupleId)를 로그에 남기지 않는다.

## 구조

```
src/index.ts        함수 정의 (옵션 · 트리거 · SDK 연결만)
src/kakaoAuth.ts    토큰 검증 · 발급 로직 (의존성 주입, 테스트 대상)
src/admobReward.ts  SSV 서명 확인 · 보상 판단 로직 (의존성 주입, 테스트 대상)
src/stopBilling.ts  예산 초과 판단 · 결제 해제 로직 (의존성 주입, 테스트 대상)
test/*.test.ts      vitest 단위 테스트 (카카오 API · Billing API는 Fake)
```

- 로직은 SDK · 네트워크를 함수 인자로 받아 단위 테스트하고, `index.ts`는 연결만 한다.
- 설정 값은 `firebase/functions/.env.days-61328`(커밋하지 않음, `.gitignore`의 `.env*`)에 둔다. 배포와 에뮬레이터가 모두 읽는다.

  ```
  KAKAO_APP_ID=카카오 개발자 콘솔의 앱 ID (숫자)
  ADMOB_REWARD_AD_UNITS=보상형 광고 단위 id의 / 뒤 숫자 (쉼표로 여럿, 비우면 모든 보상 콜백을 무시)
  ```

## 명령어

`firebase/functions/` 안에서 실행한다. (Firebase CLI가 프로젝트 폴더 `firebase/` 밖의 소스를 배포하지 못해 이 위치에 둔다)

| 목적 | 명령 |
|---|---|
| 설치 | `npm install` (`.npmrc`의 `legacy-peer-deps`) |
| 타입 검사 | `npm run typecheck` |
| 테스트 | `npm test` (에뮬레이터 불필요) |
| 빌드 (`lib/`) | `npm run build` |
| 로컬 실행 | `npm run build && cd .. && npx firebase emulators:start --only auth,firestore,functions --project days-61328` |
| 배포 | 직접 실행하지 않는다. 사용자에게 확인 후 `cd .. && npx firebase deploy --only functions --project days-61328` |

- 에뮬레이터에서도 App Check 강제가 적용된다 (토큰 없는 호출은 `UNAUTHENTICATED`).
- `stopBilling`은 에뮬레이터에서 실행되지 않는다 (Pub/Sub 에뮬레이터를 켜지 않음). **Pub/Sub 에뮬레이터를 켜고 예산 초과 메시지를 보내면 실제 결제 API를 호출하니 하지 않는다.** 동작 확인은 배포 후 4-2의 테스트 메시지(예산 이하 금액)로 한다.

## 완료 전 확인

- 타입 검사 · 테스트 통과
- 새 함수에 App Check · 인증 확인 · `maxInstances`가 빠지지 않았는지 (App Check를 걸 수 없는 외부 콜백은 서명 확인으로 대신하고 문서에 이유를 남긴다)
- `docs/days/infra-and-billing-protection.md`와 동작이 일치하는지
