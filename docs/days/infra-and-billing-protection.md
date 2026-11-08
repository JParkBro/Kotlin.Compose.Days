# 인프라 구성 · 과금 방어

Firebase(Blaze) 한 곳으로 구성한 서버, 각 흐름의 구현 포인트, 그리고 예상치 못한 청구를 막는 방법을 정리합니다.

> **2026-11 결정 변경:** 처음에는 Firebase를 Spark(무료)로 두고 카카오 토큰 · 이미지를 Cloudflare(Workers · R2 · D1 · Pages)에 맡겼습니다. 운영 부담을 줄이려고 **Cloudflare를 쓰지 않고 Firebase Blaze 한 곳**으로 바꿨습니다. Blaze는 결제 계정이 연결된 종량제라 "한도를 넘으면 멈추는" 안전장치가 없으므로, 3 · 4장의 과금 방어를 반드시 함께 설정합니다.

## 1. 전체 구성

```
[Android 앱]
   │
   ├─ Google 로그인 ─────────────▶ Firebase Auth
   ├─ 카카오 로그인 ─────────────▶ Cloud Functions kakaoAuth (onCall)
   │                                 ├─ App Check 강제
   │                                 ├─ 카카오 API로 토큰 검증 (app_id 확인)
   │                                 └─ Admin SDK로 커스텀 토큰 발급 → 앱이 signInWithCustomToken()
   ├─ 커플 데이터 동기화 ─────────▶ Firestore (보안 규칙)
   ├─ 이미지 업로드 · 다운로드 ───▶ Cloud Storage (Storage 보안 규칙: 커플 멤버만, 크기 · 형식 제한)
   ├─ 보상형 광고 ───────────────▶ AdMob ──SSV 콜백──▶ Cloud Functions admobReward (onRequest, 서명 확인 → 사진 bonus +1)
   └─ 원격 설정 · 차단 스위치 ────▶ Remote Config

[Firebase Hosting] 약관 · 개인정보 처리방침 · 오픈소스 라이선스 페이지
[Cloud Billing] 예산 알림 → Pub/Sub → Cloud Functions stopBilling (예산 초과 시 결제 연결 해제)
```

### 서비스별 역할과 무료 사용량 (Blaze)

| 서비스 | 역할 | 매달 · 매일 무료 사용량 | 넘으면 |
|---|---|---|---|
| Firebase Auth | Google 로그인, 커스텀 토큰 로그인 | 기본 Authentication은 사용자 수 제한 없음 | – (Identity Platform으로 올리지 않음) |
| Firestore | 커플 데이터 | 읽기 5만 · 쓰기 2만 · 삭제 2만 / 일, 1GiB | 종량 과금 |
| Cloud Functions (2세대) | 카카오 토큰 발급, 보상형 광고 확인(사진 +1장), 결제 자동 해제 | 호출 200만 / 월 등 | 종량 과금 |
| Cloud Storage | 이미지 저장 | 미국 리전(`us-central1` · `us-east1` · `us-west1`) 버킷만 저장 5GB 등 무료 | 종량 과금 |
| Firebase Hosting | 정책 문서 정적 페이지 | 저장 10GB, 전송 360MB / 일 | 종량 과금 |
| Remote Config, App Check | 차단 스위치, 위변조 차단 | 무료 | – |

> Blaze의 무료 사용량은 Spark와 같은 수준이지만, **넘는 순간 과금이 시작됩니다.** "기능이 멈추는 상한"은 없습니다 (4-1).

## 2. 흐름별 구현

### 2-1. 카카오 로그인 (`firebase/functions/` `kakaoAuth`)

```
앱: Kakao SDK 로그인 → 카카오 액세스 토큰
앱 → Functions kakaoAuth (onCall, { accessToken })
Function:
  1. App Check 토큰 확인 (enforceAppCheck: true, 없으면 거부)
  2. GET https://kapi.kakao.com/v1/user/access_token_info
     → 응답의 app_id가 우리 카카오 앱 ID와 같은지 확인 (다른 앱 토큰 재사용 차단)
  3. 카카오 회원번호(id) 확보
  4. uid = "kakao:{id}" 로 admin.auth().createCustomToken(uid)
  5. { customToken } 반환
앱: FirebaseAuth.signInWithCustomToken(customToken)
```

- 카카오 앱 하나에 네이티브 앱 키 2개(dev · prod 패키지)를 두므로 **앱 ID는 하나**입니다. 앱 ID는 Functions 파라미터 `KAKAO_APP_ID`(`defineString`)로 두고, 값은 `firebase/functions/.env.days-61328`(커밋하지 않음)에 넣습니다. 배포 · 에뮬레이터가 모두 이 파일을 읽습니다.
- 카카오 응답은 `id` · `app_id`가 양의 정수인지만 확인합니다. 400 · 401은 토큰 거부(`unauthenticated`), 그 밖의 오류 · 시간 초과(5초)는 `unavailable`입니다.
- `createCustomToken`은 함수의 실행 서비스 계정에 **서비스 계정 토큰 생성자** 권한이 필요합니다. 함수는 전용 서비스 계정 `kakao-auth@days-61328.iam.gserviceaccount.com`으로 실행하고, 이 계정에는 **자기 자신에 대한** 토큰 생성자 권한(`roles/iam.serviceAccountTokenCreator`)만 줍니다. 기본 compute 계정은 Editor 권한이 있어 쓰지 않습니다. 서비스 계정 비공개 키 파일은 만들지도, 저장하지도 않습니다.
- 앱(6-4b): Kakao SDK가 카카오톡 앱 로그인을 먼저 시도하고, 카카오톡이 없거나 연결된 계정이 없으면 카카오 계정(웹) 로그인으로 넘어갑니다. 사용자가 창을 닫으면 알리지 않습니다. 로그아웃할 때 카카오 쪽 토큰도 끊습니다(`UserApiClient.logout`). 네이티브 앱 키는 `local.properties`의 `KAKAO_NATIVE_APP_KEY_DEV` · `KAKAO_NATIVE_APP_KEY_PROD`이고, 없으면 빌드는 되지만 카카오 로그인이 실패합니다.
- 사용량: 로그인할 때 한 번만 호출합니다. 남용되어도 App Check 강제와 `maxInstances` 3이 막고, 마지막은 예산 알림 + `stopBilling`입니다.
- 함수 설정: 리전 `asia-northeast3`, `maxInstances` 작게(예: 3), 타임아웃 짧게(10초). 남용되어도 동시에 늘어나는 인스턴스 수가 묶입니다.

### 2-2. 이미지 업로드 · 다운로드 · 삭제 (Cloud Storage)

```
경로: couples/{coupleId}/images/{imageId}.webp  (썸네일 없음)
앱 → Storage SDK putFile / getFile / delete
Storage 보안 규칙 (firebase/storage.rules):
  - 로그인 + App Check 강제(콘솔 설정)
  - firestore.get(couples/{coupleId})로 커플 멤버인지 확인 (해제된 커플 멤버도 읽기 · 삭제는 됨)
  - 새로 만들기: ACTIVE 커플, contentType == image/webp, 크기 ≤ 300KB, 파일 이름 == usage/images.lastImageId (2-3)
  - 덮어쓰기 금지. 삭제: 멤버만. 공개 URL · 공개 읽기는 만들지 않는다
```

- 앱은 WebP · 긴 변 1080px · 280KB 이하(품질 85에서 낮춰 가며)로 압축해서 올리고, 받은 이미지는 이미지 ID 단위로 기기(`filesDir/images`)에 영구 저장해 다시 받지 않습니다. 받지 못한 사진은 그 실행 동안 다시 요청하지 않습니다.
- 썸네일은 두지 않습니다 (2026-11 결정): 받은 사진을 영구 저장하고 표시할 때 줄이므로 썸네일을 따로 받을 일이 없어 업로드 · 규칙 검사 횟수를 절반으로 줄였습니다. 문서에는 사진 id만 두고 경로는 계산합니다.
- 나만 보기 항목의 사진 · 솔로의 프로필 사진은 올리지 않습니다. 커플의 공유 항목 배경 · 커플일 때 내 프로필 사진만 그 커플 경로에 올립니다.
- Storage 규칙 안의 `firestore.get()`도 Firestore 읽기로 집계됩니다. 다운로드는 앱 캐시로 횟수를 줄입니다.
- 버킷 리전은 무료 사용량이 있는 **미국 리전**을 우선 검토합니다. 서울 리전이면 첫 바이트부터 과금되지만 이 앱 규모에서는 금액이 작습니다. 버킷을 만들 때 정합니다.

### 2-3. 업로드 한도 (서버 쪽 값)

앱 값(Remote Config)은 우회되므로 **보안 규칙**에서 막습니다.

| 제한 | 값 (초기) | 막는 곳 |
|---|---|---|
| 파일 1개 크기 | 300KB | Storage 규칙 `request.resource.size` |
| 형식 | `image/webp` | Storage 규칙 `request.resource.contentType` |
| 커플당 이미지 수 | 누적 업로드 30번 + 보상형 광고 1번당 1장(최대 +30, 2-5). 지워도 줄지 않음 | Firestore `couples/{id}/usage/images`의 `count`를 Firestore 규칙이 +1씩만 · `30 + bonus` 이하로만 허용하고 (`bonus`는 앱이 바꿀 수 없고 `admobReward` 함수만 올림) `lastImageId`에 방금 등록한 사진 id를 적는다. Storage 규칙은 그 id의 파일만 새로 만들게 한다 (+1 한 번에 파일 1개). 한도를 다 쓰면 사진은 올린 사람 기기에만 남는다 |

> 사용자당 하루 업로드 수처럼 시간 창이 있는 집계는 규칙만으로 정확히 막기 어렵습니다. 크기 · 개수 한도 + 예산 자동 해제(4-2)로 최악의 금액을 묶습니다.

### 2-4. 정책 문서 (Firebase Hosting)

- `policy-site/`의 개인정보 처리방침, 서비스 이용약관, 오픈소스 라이선스를 같은 Firebase 프로젝트의 Hosting에 배포합니다 (주소 예: `days-61328.web.app`).
- Play 스토어 등록에 개인정보 처리방침 URL이 필요하므로 이 주소를 사용합니다. 앱의 "약관 및 정책" 화면은 이 페이지를 웹뷰로 엽니다.

### 2-5. 보상형 광고로 사진 +1장 (`firebase/functions/` `admobReward`)

```
앱: 사진을 고를 때(커플 프로필 사진 · 공유 항목 커버) usage/images를 한 번 읽어 한도를 다 썼으면
    "광고 보고 사진 1장 더 올리기" 시트 → AdMob 보상형 광고 (SSV 옵션: user_id = uid, custom_data = coupleId)
AdMob 서버 → GET admobReward?…&signature=…&key_id=… (광고를 끝까지 봤을 때, 실패하면 AdMob이 다시 보냄)
Function:
  1. AdMob 공개 키(verifier-keys.json, 하루 캐시 · 모르는 keyId면 한 번 다시 받음)로 ECDSA 서명 확인 → 틀리면 403
  2. ad_unit이 우리 광고 단위(ADMOB_REWARD_AD_UNITS)인지, user_id · custom_data · transaction_id가 있는지 → 아니면 200 무시
  3. 트랜잭션: 커플이 ACTIVE이고 uid가 멤버인지, couples/{id}/adRewards/{transactionId}가 없는지(한 번만), bonus < 30인지
     → usage/images.bonus +1, adRewards/{transactionId} 기록
앱: usage/images 리스너로 bonus가 오를 때까지 최대 20초 기다렸다가 사진 선택기를 연다 (늦으면 "잠시 후 다시")
```

- **App Check 예외:** AdMob 서버가 부르는 공개 HTTPS 함수라 App Check를 강제할 수 없습니다 (2026-11 결정). 대신 AdMob 서명 · 우리 광고 단위 · 커플 멤버를 확인합니다. 다른 AdMob 계정의 광고 단위가 이 URL을 콜백으로 써도 `ad_unit`이 달라 보상하지 않습니다.
- 사용량: 광고 1번을 끝까지 볼 때 함수 호출 1번 + Firestore 읽기 3 · 쓰기 2. 앱은 시트를 띄울 때 읽기 1번, 보상을 기다릴 때 리스너(읽기 1 + 바뀔 때 1). 서명이 틀린 호출은 Firestore에 닿기 전에 거부합니다.
- 최악의 경우: 누군가 URL을 계속 호출하면 함수 호출 수만 늘고(`maxInstances` 1, 무료 200만 / 월), 커플당 사진은 최대 60장 × 300KB = 18MB입니다. 마지막은 예산 알림 + `stopBilling`입니다.
- 실행 계정: 전용 서비스 계정 `admob-reward@days-61328.iam.gserviceaccount.com`에 Firestore 쓰기 권한(`roles/datastore.user`)만 줍니다. 키 파일은 만들지 않습니다.
- 설정 값: `firebase/functions/.env.days-61328`의 `ADMOB_REWARD_AD_UNITS`(보상형 광고 단위 id의 `/` 뒤 숫자, 쉼표로 여럿). 비어 있으면 모든 콜백을 무시합니다. AdMob 콘솔의 광고 단위 → "서버 측 확인" 콜백 URL에 함수 URL을 넣습니다.
- 앱: dev 빌드는 Google 샘플 광고 단위라 SSV 콜백이 오지 않습니다 (보상을 기다리다 "잠시 후 다시"). 끝까지 확인하려면 prod 광고 단위 + 테스트 기기 등록 + 배포된 함수가 필요합니다. 맞춤 광고 동의(UMP)는 광고를 처음 띄울 때 확인합니다 (동의가 필요한 지역만 화면이 뜸).

## 3. 과금 방어의 원칙

Blaze에서 이 앱이 쓰는 Firestore · Storage에는 "이 금액 이상은 절대 청구하지 않는" 상한 기능이 없습니다. 그래서 네 겹으로 막습니다.

| 층 | 방법 | 막는 대상 |
|---|---|---|
| 1. 서버 검증 | 보안 규칙(Firestore · Storage), App Check 강제, 함수 `maxInstances` | 앱을 우회한 직접 호출, 남용 |
| 2. 앱 | 리스너 해제, 디바운스, 캐시, 재시도 백오프, 차단 스위치 | 내 코드의 버그 |
| 3. 예산 알림 | 월 1달러(1,000원) 예산의 50 · 90 · 100% 이메일 알림 | 빠른 인지 |
| 4. 결제 자동 해제 | 예산 초과 시 결제 계정 연결을 끊는 함수 | 청구 금액의 상한 (몇 시간 지연 있음) |

### 앱에서만 막으면 안 되는 이유

Firebase 설정은 APK 안에 들어 있어 추출할 수 있습니다. 그러면 앱을 거치지 않고 Firestore · Storage · Functions를 직접 호출할 수 있으므로 **앱 코드의 제한은 우회됩니다.** 실제 방어는 서버(보안 규칙, App Check, 함수 설정)에서 합니다.

다만 예상치 못한 청구는 외부 공격보다 **무한 반복 쓰기, 해제 안 된 리스너, 즉시 무한 재시도 같은 내 버그**에서 더 자주 생기므로 앱 쪽 제한도 반드시 둡니다.

## 4. 플랫폼 설정

### 4-1. Blaze에서 쓸 수 있는 장치와 한계

| 설정 | 효과 | 한계 |
|---|---|---|
| 예산 알림 | 이메일 알림 | 서비스를 멈추지 않음. 비용 데이터가 몇 시간 늦게 반영됨 |
| 지출 상한(spend cap) | 해당 서비스를 그달 동안 일시 중지 | 일부 서비스(AI Logic, App Hosting, Cloud Functions, Extensions)에만 적용. **Firestore · Storage 불가** |
| 결제 자동 해제 함수 | 예산 초과 시 결제 계정 연결 해제 → 유료 사용 중단 | 알림 지연 동안 쌓인 요금은 못 막음. 해제되면 Firestore 등 **앱 전체가 멈춤** |

### 4-2. 결제 자동 해제 (`firebase/functions/` `stopBilling`)

```
Cloud Billing 예산 (월 1달러 = 결제 계정 통화 기준 1,000원, 알림 50 · 90 · 100%)
  └─ 예산 알림을 Pub/Sub 토픽(billing-alerts)으로 보냄
       └─ Cloud Functions stopBilling (Pub/Sub 트리거)
            costAmount > budgetAmount 이면
            Cloud Billing API로 프로젝트의 결제 계정 연결 해제
```

- Google Cloud 공식 방법을 따릅니다 (참고 링크). 함수는 전용 서비스 계정 `stop-billing@days-61328.iam.gserviceaccount.com`으로 실행하고, 이 계정에만 이 프로젝트의 **결제 관리 권한(Project Billing Manager)**을 줍니다. `kakaoAuth`의 실행 계정(`kakao-auth@`)에는 주지 않습니다.
- 이 계정은 Eventarc 트리거가 함수를 부를 때도 쓰이므로, Cloud Run 서비스 `stopbilling`에만 `roles/run.invoker`를 준다 (프로젝트 전체에 주지 않음). 없으면 메시지가 `run.routes.invoke` 권한 오류로 계속 재시도된다.
- 함수 배포 이미지는 Artifact Registry(`asia-northeast3`)에 쌓이므로 정리 정책(1일)을 둔다: `npx firebase functions:artifacts:setpolicy --location asia-northeast3 --days 1 --project days-61328`
- `kakaoAuth`는 전용 계정 `kakao-auth@`로 실행하고, 이 계정 자신에 대한 **서비스 계정 토큰 생성자** 권한만 줍니다 (2-1). IAM Service Account Credentials API를 켭니다.
- 해제되면 Firestore · Storage · Functions가 모두 멈추고, 앱은 Room 데이터로 계속 동작합니다 (동기화 실패 → 재시도 대기). 다시 쓰려면 원인을 고친 뒤 콘솔에서 결제 계정을 다시 연결합니다.
- 예산 금액은 **월 1달러(1,000원)**, 알림은 50 · 90 · 100%. 무료 사용량 안에서만 쓰겠다는 뜻이라, 무료 사용량을 조금만 넘어도 결제가 끊기고 서버 기능이 멈출 수 있습니다. 사용자가 늘면 예산을 올립니다.
- 함수 자체가 과금 대상이 되지 않도록 `maxInstances: 1`로 둡니다.

### 4-3. Firebase 콘솔 설정

- **App Check:** 두 앱(`jpark.bro.days`, `jpark.bro.days.dev`)을 Play Integrity로 등록. dev는 디버그 제공자 + 디버그 토큰. 동작 확인 후 Firestore · Storage · Functions에 **강제(enforce)**.
- **보안 규칙:** Firestore(`firebase/firestore.rules`) · Storage(`firebase/storage.rules`)는 에뮬레이터 테스트를 통과한 것만 배포. 쿼리 개수 제한(`request.query.limit <= 50`).
- **Identity Platform으로 업그레이드하지 않기:** 기본 Authentication으로 충분하며, 업그레이드하면 MAU 과금이 생깁니다 (`firebase-cost-strategy.md`).
- **dev · prod가 한 프로젝트(`days-61328`)를 함께 씀:** 개발 중 버그가 운영 사용량 · 요금으로 이어지므로, 디버그 빌드는 가능한 한 **에뮬레이터**에 연결합니다 (5장 6번).

## 5. 앱 쪽 방어 (내 버그 대비)

1. **원격 차단 스위치 (Remote Config)**

   | 키 | 기본값 | 용도 |
   |---|---|---|
   | `sync_enabled` | true | 동기화 전체 중단 (6-5b 구현: `RemoteFlags`, 앱 시작 · 포그라운드 진입 때 fetch, 최소 간격 1시간) |
   | `upload_enabled` | true | 이미지 업로드 중단 (6-8a 구현: 사진이 붙은 공유 항목도 올리지 않고 기다림, 받기 · 지우기는 계속) |
   | `kakao_login_enabled` | true | 카카오 로그인 일시 중단 |
   | `min_supported_version` | 1 | 버그 있는 구버전 강제 업데이트 |

2. **리스너 관리:** 화면을 벗어나거나 앱이 백그라운드로 가면 리스너를 반드시 해제합니다.
3. **쓰기 디바운스:** 같은 문서를 몇 초 안에 반복해서 쓰지 않습니다.
4. **이미지 캐시:** 한 번 받은 이미지는 기기에 저장해서 다시 요청하지 않습니다.
5. **실패 재시도 간격:** 동기화 · 업로드 실패 시 지수 백오프(1초, 2초, 4초 … 최대 수 분)로 재시도합니다. 실패 직후 즉시 무한 재시도하는 버그가 가장 흔한 과금 원인입니다.
6. **개발 빌드는 에뮬레이터로:** dev 빌드는 **기본으로** 로컬 Firebase 에뮬레이터(Auth 9099 · Firestore 8080 · Functions 5001 · Storage 9199)에 연결합니다 (`FirebaseSetup`, `BuildConfig.FIREBASE_EMULATOR_HOST`).

   | 상황 | 설정 (`local.properties`) |
   |---|---|
   | Android 에뮬레이터 + Mac 에뮬레이터 (기본) | 없음 (`10.0.2.2`) |
   | 실기기 (USB) | `firebase.emulator.host=127.0.0.1` + `adb reverse tcp:9099 tcp:9099` · `adb reverse tcp:8080 tcp:8080` (5001 · 9199도, 에뮬레이터를 LAN에 열지 않음) |
   | 실제 프로젝트로 확인 | `firebase.useEmulator=false` (운영 예산 1,000원에 들어감) |

   에뮬레이터 실행: `cd firebase && npm --prefix functions run build && npx firebase emulators:start --only auth,firestore,functions,storage --project days-61328`. Functions 에뮬레이터는 `KAKAO_APP_ID`가 든 `firebase/functions/.env.days-61328`이 필요하고, App Check 강제도 그대로 적용된다 (토큰 없는 호출은 `UNAUTHENTICATED`). Pub/Sub 에뮬레이터를 켜지 않으므로 `stopBilling`은 에뮬레이터에서 실행되지 않는다. 앱의 프로젝트 ID와 같아야 데이터가 보인다. dev 빌드는 에뮬레이터(http) 연결을 위해 `10.0.2.2` · `localhost` · `127.0.0.1`에만 평문 통신을 허용한다 (`app/src/dev`만, prod는 허용하지 않음).

## 6. 출시 전 체크리스트

- [ ] Cloud Billing 예산(월 1달러 = 1,000원) · 알림(50 · 90 · 100%) 설정, Pub/Sub 토픽 연결
- [x] 결제 자동 해제 함수(`stopBilling`) 배포 · 테스트 메시지로 동작 확인, 서비스 계정 권한 최소화 (2026-11-03, 예산 이하 메시지 → `under-budget`)
- [ ] Firebase Authentication이 Identity Platform으로 업그레이드되지 않았는지 확인
- [ ] App Check(Play Integrity) 등록 및 Firestore · Storage · Functions 강제 설정
- [ ] 보안 규칙(Firestore · Storage) 배포 및 에뮬레이터 테스트 (연결 · 공유 · 해제 · 비멤버 접근 거부 · 업로드 크기 · 개수)
- [x] `kakaoAuth` 함수: 카카오 앱 ID 환경 변수, `maxInstances`, App Check 강제 확인 (2026-11 배포 · 실기기 카카오 로그인으로 확인, 실행 계정 `kakao-auth@`)
- [x] `admobReward` 함수: 실행 계정 `admob-reward@`(`roles/datastore.user`만) 만들기, `.env.days-61328`에 `ADMOB_REWARD_AD_UNITS`, 배포 후 AdMob 광고 단위의 서버 측 확인 콜백 URL 등록 · "URL 확인"(→ 200 `ignored`) (2026-11-07)
- [ ] Play Console 인앱 상품 `support_small` · `support_medium` · `support_large`(소모형, ₩1,200 / ₩3,300 / ₩6,600) 등록 · 활성화, 내부 테스트 트랙 + 라이선스 테스터로 결제 확인 (`ads-strategy.md` 6장)
- [x] AdMob 앱 · 보상형 광고 단위 만들기, `local.properties`에 `admob.appId` · `admob.rewardedAdUnitId` (없으면 prod도 Google 샘플 광고) (2026-11-07)
- [ ] 사진 +1장 끝까지 확인: 에뮬레이터(자동 테스트 기기) prod 디버그 빌드에서 광고는 나오지만 SSV 콜백이 아직 오지 않음 (2026-11-08). 실기기 테스트 기기 · 출시 후 다시 확인
- [ ] Storage 기본 버킷을 `us-central1`로 만들기(2026-11 결정, 무료 사용량), 공개 URL · 공개 읽기 없음 확인, `storage.rules` 배포
- [ ] Remote Config 차단 스위치 4종 등록 및 앱 동작 확인
- [ ] Firebase(Google) 계정 2단계 인증 설정
- [ ] 개인정보 처리방침에 데이터 보관 위치(Google Cloud) 명시, Firebase Hosting 배포
- [ ] **카카오 키 해시 교체:** 운영용 네이티브 앱 키(`jpark.bro.days`)에 출시용 키 · Play 앱 서명 키의 키 해시를 등록. 지금은 개발 PC의 디버그 키 해시만 등록되어 있음
- [ ] **Firebase 지문 추가:** 운영 앱(`jpark.bro.days`)에 출시용 키 · Play 앱 서명 키의 SHA-1 · SHA-256 등록 (구글 로그인 · App Check)
- [ ] **dev · prod Firebase 분리 재검토:** 지금은 한 프로젝트(`days-61328`)를 dev · prod가 함께 씀. 운영 데이터가 생기기 전에 나눌지 결정

## 참고 링크

- Firebase 요금제 · 무료 사용량: https://firebase.google.com/pricing
- 예산 알림 · 지출 상한: https://firebase.google.com/docs/projects/billing/budget-alerts
- 결제 자동 해제 (Google Cloud): https://cloud.google.com/billing/docs/how-to/disable-billing-with-notifications
- 커스텀 토큰 만들기: https://firebase.google.com/docs/auth/admin/create-custom-tokens
- Cloud Functions에서 App Check 강제: https://firebase.google.com/docs/app-check/cloud-functions
- Cloud Storage 무료 리전 변경 안내: https://firebase.google.com/docs/storage/faqs-storage-changes-announced-sept-2024
- Storage 보안 규칙에서 Firestore 읽기: https://firebase.google.com/docs/storage/security/rules-conditions#enhance_with_firestore
- App Check: https://firebase.google.com/docs/app-check
