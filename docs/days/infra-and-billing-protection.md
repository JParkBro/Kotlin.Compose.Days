# 인프라 구성 · 과금 방어

Firebase(Spark)와 Cloudflare를 함께 쓰는 구성, 각 흐름의 구현 포인트, 그리고 예상치 못한 청구를 막는 방법을 정리합니다.

## 1. 전체 구성

```
[Android 앱]
   │
   ├─ Google 로그인 ─────────────▶ Firebase Auth (Spark)
   ├─ 커플 데이터 동기화 ─────────▶ Firestore (Spark)
   ├─ 원격 설정 · 차단 스위치 ────▶ Remote Config
   │
   ├─ 카카오 로그인 ─────────────▶ Worker /auth/kakao
   │                                 ├─ 카카오 API로 토큰 검증
   │                                 └─ Firebase 커스텀 토큰 발급 → 앱이 signInWithCustomToken()
   │
   └─ 이미지 업로드 · 다운로드 ───▶ Worker /images/*
                                     ├─ Firebase ID 토큰 · App Check 토큰 검증
                                     ├─ 커플 멤버 확인, 크기 · 횟수 제한 (D1)
                                     └─ R2 비공개 버킷

[Cloudflare Pages] 약관 · 개인정보 처리방침 · 오픈소스 라이선스 페이지
```

### 서비스별 역할과 무료 범위

| 서비스 | 역할 | 무료 범위 | 결제 수단 |
|---|---|---|---|
| Firebase Auth | Google 로그인, 커스텀 토큰 로그인 | 기본 Authentication은 사용자 수 제한 없음 | 등록 안 함 |
| Firestore | 커플 데이터 | 읽기 5만 · 쓰기 2만 · 삭제 2만 / 일, 1GiB | 등록 안 함 |
| Remote Config, App Check | 차단 스위치, 위변조 차단 | 무료 | 등록 안 함 |
| Cloudflare Workers (무료 플랜) | 카카오 토큰 발급, 이미지 관문 | 10만 요청 / 일, 요청당 CPU 10ms | 불필요 |
| Cloudflare D1 | 업로드 횟수 · 용량 집계 | 읽기 500만 · 쓰기 10만 행 / 일, 5GB | 불필요 |
| Cloudflare R2 | 이미지 저장 | 저장 10GB, 쓰기 100만 · 읽기 1,000만 건 / 월, 전송료 0원 | 활성화 시 요구될 수 있음 |
| Cloudflare Pages | 정책 문서 정적 페이지 | 무료 | 불필요 |

> Workers 무료 플랜은 한도를 넘으면 **과금되지 않고 요청이 실패**합니다. 이 성질을 R2 사용량 상한으로 활용합니다 (4장).

## 2. 흐름별 구현

### 2-1. 카카오 로그인

```
앱: Kakao SDK 로그인 → 카카오 액세스 토큰
앱 → Worker POST /auth/kakao { accessToken }
Worker:
  1. GET https://kapi.kakao.com/v1/user/access_token_info
     → 응답의 app_id가 우리 앱 ID와 같은지 확인
  2. 카카오 회원번호(id) 확보
  3. uid = "kakao:{id}" 로 Firebase 커스텀 토큰(JWT) 서명
  4. { customToken } 반환
앱: FirebaseAuth.signInWithCustomToken(customToken)
```

**커스텀 토큰 JWT 구성** (Admin SDK 없이 직접 서명)

| 항목 | 값 |
|---|---|
| alg | RS256 |
| iss, sub | 서비스 계정 이메일 |
| aud | `https://identitytoolkit.googleapis.com/google.identity.identitytoolkit.v1.IdentityToolkit` |
| iat | 현재 시각 (초) |
| exp | iat + 최대 3600초 |
| uid | `kakao:{회원번호}` |

```ts
import { SignJWT, importPKCS8 } from "jose";

async function createCustomToken(env: Env, uid: string) {
  const key = await importPKCS8(env.FIREBASE_PRIVATE_KEY, "RS256");
  const now = Math.floor(Date.now() / 1000);
  return new SignJWT({ uid })
    .setProtectedHeader({ alg: "RS256", typ: "JWT" })
    .setIssuer(env.FIREBASE_CLIENT_EMAIL)
    .setSubject(env.FIREBASE_CLIENT_EMAIL)
    .setAudience("https://identitytoolkit.googleapis.com/google.identity.identitytoolkit.v1.IdentityToolkit")
    .setIssuedAt(now)
    .setExpirationTime(now + 3600)
    .sign(key);
}
```

- 서비스 계정 비공개 키는 코드에 넣지 않고 `wrangler secret put FIREBASE_PRIVATE_KEY`로 저장합니다.
- 서비스 계정은 토큰 서명 전용으로 새로 만들고, Firebase 콘솔에서 필요한 최소 권한만 부여합니다.
- **무료 플랜 CPU 10ms 확인:** 배포 후 Workers 대시보드에서 `/auth/kakao`의 CPU 시간을 확인합니다. 키 가져오기(importPKCS8)는 전역 변수에 캐시해 매 요청마다 반복하지 않습니다. 부족하면 Workers 유료 플랜(월 5달러)으로 올리거나 이 엔드포인트만 홈 서버로 옮깁니다.

### 2-2. 이미지 업로드

```
앱 → Worker PUT /images/{coupleId}/{imageId}
     헤더: Authorization: Bearer {Firebase ID 토큰}
           X-Firebase-AppCheck: {App Check 토큰}
     본문: WebP 이미지 (앱에서 압축 완료)
Worker:
  1. ID 토큰 검증 → uid
  2. App Check 토큰 검증
  3. 커플 멤버 확인 (아래 방법)
  4. 크기 · 횟수 제한 확인 (D1)
  5. R2에 저장: couples/{coupleId}/images/{imageId}.webp
  6. D1 집계 갱신
```

**토큰 검증 기준**

| 토큰 | 공개 키 (JWKS) | iss | aud |
|---|---|---|---|
| Firebase ID 토큰 | `https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com` | `https://securetoken.google.com/{projectId}` | `{projectId}` |
| App Check 토큰 | `https://firebaseappcheck.googleapis.com/v1/jwks` | `https://firebaseappcheck.googleapis.com/{projectNumber}` | `projects/{projectNumber}` 포함 |

`jose`의 `createRemoteJWKSet` + `jwtVerify`로 검증하며, 공개 키는 Worker가 자동 캐시합니다.

**커플 멤버 확인**

Worker가 **사용자의 ID 토큰 그대로** Firestore REST API로 `couples/{coupleId}`를 읽습니다. Firestore 보안 규칙이 멤버만 읽기를 허용하므로, 읽기에 성공하면 멤버입니다.

- 서비스 계정 권한이 필요 없어 안전하고, 규칙 한 곳에서 권한을 관리합니다.
- Firestore에 App Check를 강제했다면 앱에서 받은 App Check 토큰을 `X-Firebase-AppCheck` 헤더로 함께 전달합니다.
- 업로드 1건당 Firestore 읽기 1회가 추가되지만 업로드 빈도가 낮아 부담이 작습니다.

### 2-3. 이미지 다운로드

```
앱 → Worker GET /images/{coupleId}/{imageId}
Worker:
  1. ID 토큰 · App Check 토큰 검증
  2. D1 멤버 캐시 확인 (uid, coupleId, 24시간 이내 확인 기록)
     없으면 Firestore로 확인 후 캐시에 기록
  3. R2에서 꺼내 응답
앱: 받은 이미지를 기기에 영구 캐시
```

- 다운로드마다 Firestore를 읽지 않도록 멤버 확인 결과를 D1에 24시간 캐시합니다.
- 앱은 이미지 ID 단위로 기기에 캐시해 같은 이미지를 다시 요청하지 않습니다.
- R2 버킷은 **비공개**로 두고 공개 URL(r2.dev, 커스텀 도메인 공개)은 켜지 않습니다.

### 2-4. 이미지 삭제

- 항목 삭제, 연결 해제 후 사본 삭제, 회원 탈퇴 시 앱이 Worker `DELETE /images/...`를 호출합니다.
- Worker는 업로드와 같은 검증 후 R2에서 삭제하고 D1 집계를 줄입니다.

### 2-5. 정책 문서 (Cloudflare Pages)

- 개인정보 처리방침, 서비스 이용약관, 오픈소스 라이선스를 정적 페이지로 배포합니다.
- Play 스토어 등록에 개인정보 처리방침 URL이 필요하므로 이 주소를 사용합니다.
- 앱의 "약관 및 정책" 화면은 이 페이지를 웹뷰로 엽니다.

### D1 테이블

```sql
CREATE TABLE couple_usage (
  couple_id   TEXT PRIMARY KEY,
  image_count INTEGER NOT NULL DEFAULT 0,
  total_bytes INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE daily_upload (
  user_id TEXT NOT NULL,
  day     TEXT NOT NULL,          -- yyyy-MM-dd (UTC)
  count   INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY (user_id, day)
);
CREATE TABLE member_cache (
  user_id     TEXT NOT NULL,
  couple_id   TEXT NOT NULL,
  verified_at INTEGER NOT NULL,   -- epoch millis
  PRIMARY KEY (user_id, couple_id)
);
```

## 3. 과금 방어의 원칙

두 플랫폼 모두 **이 앱이 쓰는 서비스(Firestore, R2)에는 "이 금액 이상은 절대 청구하지 않는" 상한 기능이 없습니다.** 그래서 세 겹으로 막습니다.

| 층 | 방법 | 막는 대상 |
|---|---|---|
| 1. 결제 수단 최소화 | 필요 없는 곳엔 카드를 등록하지 않음 | 청구 자체 |
| 2. 서버 검증 | 보안 규칙, App Check, Worker 제한 | 앱을 우회한 직접 호출, 남용 |
| 3. 앱 | 리스너 해제, 디바운스, 캐시, 차단 스위치 | 내 코드의 버그 |
| 4. 플랫폼 알림 | 예산 알림 | 위가 모두 뚫렸을 때 빠른 인지 |

### 앱에서만 막으면 안 되는 이유

Firebase 설정과 Worker 주소는 APK 안에 들어 있어 추출할 수 있습니다. 그러면 앱을 거치지 않고 Firestore나 Worker를 직접 호출할 수 있으므로 **앱 코드의 제한은 우회됩니다.** 실제 방어는 서버(보안 규칙, Worker)에서 해야 합니다.

다만 예상치 못한 청구는 외부 공격보다 **무한 반복 쓰기, 해제 안 된 리스너 같은 내 버그**에서 더 자주 생기므로 앱 쪽 제한도 반드시 둡니다.

## 4. 플랫폼별 설정

### 4-1. Firebase (Spark)

- **결제 수단 등록 안 함.** 한도 초과 시 기능이 멈출 뿐 청구가 불가능합니다.
- **App Check 적용:** Play Integrity 제공자 사용. Firestore에 강제(enforce) 설정.
- **보안 규칙:** 커플 멤버만 접근, 문서 크기 제한, 쿼리 개수 제한 (`request.query.limit <= 50`).
- **Identity Platform으로 업그레이드하지 않기** (`firebase-cost-strategy.md` 참고).

### 4-2. Firebase를 나중에 Blaze로 전환한다면

| 설정 | 효과 | 한계 |
|---|---|---|
| 예산 알림 (1%, 50%, 100%) | 이메일 알림 | 서비스를 멈추지 않음. 알림이 몇 시간~며칠 늦을 수 있음 |
| 지출 상한 (spend cap) | 해당 서비스를 그달 동안 일시 중지 | AI Logic, App Hosting, Cloud Functions, Extensions에만 적용. **Firestore 불가** |
| 결제 자동 해제 함수 | 예산 초과 시 Pub/Sub → 함수가 결제 계정 연결 해제 | 알림 지연 동안 쌓인 요금은 못 막음. 유료 기능 함께 중단 |

결제 자동 해제 함수는 Cloud Functions로 만들어지므로, 그 함수 자체에도 지출 상한을 걸어둡니다.

### 4-3. Cloudflare Workers (무료 플랜 유지)

- **무료 플랜을 유지하는 것 자체가 상한 장치입니다.** 하루 10만 요청을 넘으면 실패할 뿐 청구되지 않습니다.
- 모든 요청에서 ID 토큰 · App Check 토큰을 검증합니다.
- 비정상적으로 많은 요청을 보내는 uid는 D1에 차단 목록으로 기록해 거부합니다.

### 4-4. Cloudflare R2 (카드가 등록되는 유일한 곳)

**R2는 반드시 Worker를 통해서만 접근**하게 해서 Worker 무료 한도를 R2 사용량의 상한으로 만듭니다.

| R2 항목 | 무료 범위 (월) | Worker 경유 시 최대 | 대응 |
|---|---|---|---|
| 읽기 (Class B) | 1,000만 건 | 약 300만 건 (10만 × 30일) | 자동으로 무료 범위 안 |
| 쓰기 (Class A) | 100만 건 | 약 300만 건 | **Worker에서 업로드 횟수 제한** |
| 저장 용량 | 10GB | 제한 없음 | **Worker에서 크기 · 개수 제한** |

**Worker 업로드 제한 값 (초기값)**

| 제한 | 값 | 비고 |
|---|---|---|
| 파일 1개 최대 크기 | 300KB | 앱에서 WebP · 1080px · 150KB 안팎으로 압축하므로 정상 사용자는 영향 없음 |
| 사용자당 하루 업로드 | 20장 | `daily_upload` |
| 커플당 총 이미지 수 | 무료 30장, 보상형 광고로 추가 | `couple_usage` |
| 커플당 총 용량 | 10MB | `couple_usage` |

> 커플당 10MB 기준이면 커플 1,000쌍이 모두 꽉 채워도 10GB입니다. 사용자 증가에 맞춰 Remote Config가 아니라 Worker 환경 변수로 조정합니다 (서버 쪽 값이어야 우회되지 않음).

**presigned URL은 사용하지 않습니다.** Cloudflare 문서는 비용 절감을 위해 클라이언트가 presigned URL로 R2에 직접 접근하는 방식을 권장하지만, 그러면 Worker 무료 한도라는 상한이 사라집니다. 이 앱 규모에서는 Worker 경유가 더 안전합니다.

**예산 알림:** Cloudflare 대시보드 > Manage Account > Billing > Billable Usage에서 **1달러, 5달러** 알림을 만듭니다. 정보 제공용이며 사용량을 멈추지는 않습니다.

### 4-5. 최악의 경우 금액

모든 방어가 뚫려 R2 쓰기가 무료 범위보다 200만 건 더 발생해도, 쓰기 요금(100만 건당 4.5달러) 기준 **약 9달러**입니다. Worker 무료 한도가 상한 역할을 하므로 큰 금액의 청구가 나오는 구조가 아닙니다.

## 5. 앱 쪽 방어 (내 버그 대비)

1. **원격 차단 스위치 (Remote Config)**

   | 키 | 기본값 | 용도 |
   |---|---|---|
   | `sync_enabled` | true | 동기화 전체 중단 |
   | `upload_enabled` | true | 이미지 업로드 중단 |
   | `kakao_login_enabled` | true | 카카오 로그인 일시 중단 |
   | `min_supported_version` | 1 | 버그 있는 구버전 강제 업데이트 |

2. **리스너 관리:** 화면을 벗어나거나 앱이 백그라운드로 가면 리스너를 반드시 해제합니다.
3. **쓰기 디바운스:** 같은 문서를 몇 초 안에 반복해서 쓰지 않습니다.
4. **이미지 캐시:** 한 번 받은 이미지는 기기에 저장해서 다시 요청하지 않습니다.
5. **실패 재시도 간격:** 동기화 · 업로드 실패 시 지수 백오프(1초, 2초, 4초 … 최대 수 분)로 재시도합니다. 실패 직후 즉시 무한 재시도하는 버그가 가장 흔한 과금 원인입니다.
6. **개발 빌드는 로컬로:** 디버그 빌드는 Firebase 에뮬레이터와 `wrangler dev`에 연결해, 개발 중 버그가 실제 사용량으로 이어지지 않게 합니다.

## 6. 출시 전 체크리스트

- [ ] Firebase 프로젝트에 결제 수단이 연결되어 있지 않은지 확인
- [ ] Firebase Authentication이 Identity Platform으로 업그레이드되지 않았는지 확인
- [ ] App Check(Play Integrity) 등록 및 Firestore 강제 설정
- [ ] 보안 규칙 배포 및 에뮬레이터 테스트 (연결 · 공유 · 해제 · 비멤버 접근 거부)
- [ ] Workers 무료 플랜 확인, 서비스 계정 키를 Secret으로 등록
- [ ] `/auth/kakao` CPU 시간 10ms 이내 확인
- [ ] R2 버킷 비공개, 공개 URL 비활성화 확인
- [ ] Worker 업로드 제한 값 설정 (크기, 일일 횟수, 커플당 개수 · 용량)
- [ ] Cloudflare 예산 알림 1달러 · 5달러 설정
- [ ] Remote Config 차단 스위치 4종 등록 및 앱 동작 확인
- [ ] Firebase · Cloudflare 계정 모두 2단계 인증 설정
- [ ] 개인정보 처리방침에 데이터 보관 위치(Google, Cloudflare) 명시

## 참고 링크

- Firebase 예산 알림 · 지출 상한: https://firebase.google.com/docs/projects/billing/budget-alerts
- 결제 자동 해제 (Google Cloud): https://cloud.google.com/billing/docs/how-to/disable-billing-with-notifications
- 커스텀 토큰 만들기: https://firebase.google.com/docs/auth/admin/create-custom-tokens
- App Check: https://firebase.google.com/docs/app-check
- Workers 요금 · 한도: https://developers.cloudflare.com/workers/platform/pricing/
- R2 요금: https://developers.cloudflare.com/r2/pricing
- Cloudflare 예산 알림: https://developers.cloudflare.com/billing/manage/budget-alerts/
