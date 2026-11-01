# Firebase 비용 전략

> 이 문서는 **Firebase를 Spark(무료) 플랜으로 유지**하는 구성을 기준으로 합니다. 카카오 로그인과 이미지 저장은 Cloudflare가 맡습니다. 전체 인프라 구성과 과금 방어는 [infra-and-billing-protection.md](infra-and-billing-protection.md)를 참고하세요.

## 1. 구성 요약

| 기능 | 담당 | 요금제 |
|---|---|---|
| Google 로그인 | Firebase Auth | Spark |
| 카카오 로그인 (커스텀 토큰으로 Firebase 로그인) | 토큰 발급은 Cloudflare Worker, 로그인은 Firebase Auth | Spark |
| 커플 데이터 동기화 | Firestore | Spark |
| 원격 설정 · 기능 차단 스위치 | Remote Config | 무료 |
| 앱 위변조 요청 차단 | App Check | 무료 |
| 크래시 · 사용 통계 | Crashlytics, Analytics | 무료 |
| 이미지 저장 | Cloudflare R2 | – |
| 약관 · 개인정보 처리방침 페이지 | Cloudflare Pages | – |

**Firebase에는 결제 수단을 등록하지 않습니다.** Spark 플랜은 한도를 넘으면 기능이 멈출 뿐 청구가 발생하지 않으므로, 가장 확실한 과금 방어입니다.

## 2. Blaze 없이 가능한 이유

처음 검토에서 Blaze가 필요했던 기능과, 이를 대체한 방법입니다.

| 기능 | Firebase만 쓸 때 | 대체 방법 |
|---|---|---|
| 카카오 로그인 | Cloud Functions에서 커스텀 토큰 발급 → 외부 API 호출 때문에 Blaze 필요 | Cloudflare Worker에서 커스텀 토큰 발급 |
| 카카오 로그인 (OIDC) | Identity Platform 업그레이드 필요. Spark에서는 OIDC 사용자 하루 2명 제한 | 사용하지 않음 |
| 이미지 저장 | 2026년 2월 3일부터 Cloud Storage는 Blaze 필수 | Cloudflare R2 |

### 주의: Identity Platform으로 업그레이드하지 않기

Firebase Authentication을 Identity Platform으로 업그레이드하면 Spark 프로젝트는 하루 활성 사용자 3,000명 제한이 생깁니다. 커스텀 토큰과 Google 로그인은 기본 Authentication으로 충분하므로 **업그레이드하지 않습니다.**

## 3. Firestore 무료 한도

Spark · Blaze 공통이며, 일일 한도는 매일 태평양 시간 자정에 초기화됩니다.

| 항목 | 무료 한도 |
|---|---|
| 문서 읽기 | 50,000 / 일 |
| 문서 쓰기 | 20,000 / 일 |
| 문서 삭제 | 20,000 / 일 |
| 저장 용량 | 1 GiB |
| 외부 전송 | 10 GiB / 월 |

- 무료 사용량은 프로젝트당 데이터베이스 1개에만 적용됩니다.
- **TTL(자동 만료 삭제), 백업, PITR은 무료 사용량이 없어** Spark에서는 사용하지 않습니다. 커플 코드 만료는 보안 규칙과 앱에서 처리합니다 (`sync-and-pairing.md`).
- 한도를 넘으면 그날 남은 시간 동안 해당 작업이 실패합니다. 앱은 실패를 조용히 재시도 대기열에 넣고, Room 데이터로 계속 동작해야 합니다.

## 4. 읽기 · 쓰기를 줄이는 설계

Firestore 비용과 한도 초과는 대부분 **읽기**에서 발생합니다. 특히 커지는 컬렉션에 조건 없는 실시간 리스너를 붙여두면 읽기가 빠르게 불어납니다.

1. **Room이 원본, Firestore는 공유 통로.** "나만 보기" 데이터는 업로드하지 않습니다.
2. **변경분만 가져오기.** 리스너를 `serverUpdatedAt > lastPulledAt` 조건으로 겁니다. 조건이 없으면 리스너를 붙일 때마다 전체 문서를 다시 읽습니다.
3. **리스너는 앱이 화면에 떠 있을 때만.** 백그라운드 위젯 갱신은 WorkManager로 하루 몇 번이면 충분합니다.
4. **자동 기념일은 계산.** 100일 단위, 주년, 생일은 저장하지 않고 기기에서 계산합니다. 사용자가 숨긴 것만 저장합니다.
5. **soft delete.** 상대 기기에 삭제를 전달하기 위해 `deletedAt`을 기록합니다.
6. **쓰기 묶기.** 저장 시점에 한 번 쓰고, 여러 건은 batch로 처리합니다. 같은 문서를 몇 초 안에 반복해서 쓰지 않도록 디바운스합니다.
7. **상대 활동 알림은 로컬 생성.** 동기화로 받아온 항목의 `updatedBy`가 상대이면 기기에서 알림을 만듭니다. FCM 발송 서버가 필요 없습니다.
8. **Firestore 오프라인 캐시 최소화.** Room이 이미 로컬 저장소 역할을 하므로 중복 저장을 피합니다.
9. **이미지 메타 정보는 항목 문서 안에.** 별도 컬렉션을 두지 않아 추가 읽기를 만들지 않습니다.

### 사용량 추정

- 사용자 1명이 하루 5회 앱을 열고, 열 때마다 변경분 수 건을 읽는다고 가정하면 하루 읽기는 1인당 수십 건 수준입니다.
- 이 기준이면 **일일 활성 사용자 1,000명대 중반까지 읽기 무료 한도 안**에 들어옵니다.
- 쓰기는 디데이 · 일정 저장 시에만 발생하므로 읽기보다 훨씬 적습니다.
- 보안 규칙 안의 `get()` 호출도 읽기로 집계되므로, 규칙에서 다른 문서를 읽는 횟수를 최소화합니다.

## 5. 모니터링

- Firebase 콘솔 > 프로젝트 설정 > **사용량 및 결제**에서 Firestore 일일 사용량을 주 1회 확인합니다.
- 일일 읽기가 **한도의 50%(25,000건)**를 꾸준히 넘기 시작하면 6장의 전환 단계를 준비합니다.
- 이상 증가가 보이면 Remote Config의 `sync_enabled`를 꺼서 즉시 멈출 수 있게 해둡니다 (`infra-and-billing-protection.md`).

## 6. 사용자가 늘었을 때

Firestore 한도를 넘는 시점이 오면 두 가지 중 선택합니다.

| 선택 | 내용 | 비고 |
|---|---|---|
| Blaze 전환 | 초과분만 과금. 이 앱 구조에서는 초과 요금이 작음 | 예산 알림 + 결제 자동 해제 함수 설정 (`infra-and-billing-protection.md`) |
| 자체 서버 이전 | `data-model-erd.md`의 ERD 그대로 저가 VPS로 이전 | 앱은 `SyncRemoteDataSource` 구현체만 교체 |

Blaze로 전환하더라도 Firestore에는 지출 상한(spend cap)을 걸 수 없습니다. 지출 상한은 AI Logic, App Hosting, Cloud Functions, Extensions에만 적용됩니다.

## 참고 링크

- Firebase 요금제: https://firebase.google.com/docs/projects/billing/firebase-pricing-plans
- Firestore 할당량: https://docs.cloud.google.com/firestore/quotas
- Cloud Storage 변경 FAQ: https://firebase.google.com/docs/storage/faqs-storage-changes-announced-sept-2024
- Authentication 한도: https://firebase.google.com/docs/auth/limits
- 예산 알림 · 지출 상한: https://firebase.google.com/docs/projects/billing/budget-alerts
