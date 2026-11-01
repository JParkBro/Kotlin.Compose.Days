# 데이즈(Days) 프로젝트 문서

커플 · 개인 디데이 앱 **데이즈**에만 해당하는 문서입니다. 코드 구조 · 규칙 · 테스트 같은 범용 내용은 [`../common/`](../common/README.md)을 따르고, 이 폴더에는 데이즈의 결정 사항만 둡니다.

## 문서 목록

| 문서 | 내용 | 주로 보는 시점 |
|---|---|---|
| [product-spec.md](product-spec.md) | 기능 범위, 항목 · 계산 규칙, 화면 목록 · 동작, 설정 구조, 샘플 데이터 | 기획 · 디자인 · 개발 전반 |
| [theme-guide.md](theme-guide.md) | 4 테마 × 라이트/다크 = 8 팔레트, 토큰 27개, 사용 규칙, Compose 적용 | 디자인 · UI 개발 |
| [module-map.md](module-map.md) | 모듈 목록, 내비게이션 키, 기능 간 의존, 화면별 Event 유무, 개발 순서 | 개발 시작 · 기능 추가 |
| [data-model-erd.md](data-model-erd.md) | ERD, Room · Firestore · R2 · 자체 서버 배치, Entity 예시 | 데이터 작업 |
| [sync-and-pairing.md](sync-and-pairing.md) | 동기화 흐름, 커플 코드 연결, 보안 규칙, 연결 해제 · 탈퇴 | 커플 기능 개발 |
| [infra-and-billing-protection.md](infra-and-billing-protection.md) | Firebase(Spark) + Cloudflare 구성, Worker 구현, 과금 방어, 출시 체크리스트 | 서버 작업 · 출시 전 |
| [firebase-cost-strategy.md](firebase-cost-strategy.md) | Spark 유지 이유, Firestore 한도, 읽기 · 쓰기 절감 설계 | 동기화 설계 · 운영 |
| [ads-strategy.md](ads-strategy.md) | 광고 배치 판단, 빈도 규칙, 광고 제거 구매, AdMob 정책 | 수익화 작업 |
| `assets/theme-tokens.json` | 테마 색상 값 원본 (8 팔레트 × 27 토큰) | 디자인 시스템 코드 생성 |
| `assets/theme-tokens.css` | 같은 값의 CSS 변수 (정책 페이지 · 웹 미리보기) | 웹 작업 |

## AI 도구 지침 (Claude Code)

저장소 루트와 하위 폴더의 `CLAUDE.md`가 이 문서들을 기준으로 작업 지침을 요약합니다. 문서의 결정이 바뀌면 해당 `CLAUDE.md`도 함께 고칩니다.

| 파일 | 범위 |
|---|---|
| `CLAUDE.md` | 프로젝트 전체: 문서 지도, 절대 규칙, Git, 비밀 정보, 언어 + Android 앱(모듈 · MVI · 데이터 · UI 규칙, Gradle 명령, 테스트 요구 사항) |
| `workers/CLAUDE.md` | Worker 보안 · 제한 규칙, 명령 |
| `firebase/CLAUDE.md` | 보안 규칙 원칙, 필수 테스트 시나리오 |

> 처음 읽을 때는 위 표의 순서(product-spec → theme-guide → module-map → data-model-erd …)를 추천합니다.

## 핵심 결정 요약

**제품**
- 솔로 모드가 기본, 커플 연결은 그 위에 얹는 기능. 항목마다 "나만 보기" / "우리 둘이".
- 항목은 디데이와 일정 두 종류. 자동 기념일(100일 단위 · 주년 · 생일)은 저장하지 않고 계산.
- 하단 탭은 홈 / 캘린더 / 설정.

**기술**
- 원본 데이터는 Room. Firestore는 "우리 둘이" 공유 데이터의 통로로만 사용.
- **Firebase는 Spark(무료)로 유지하고 결제 수단을 등록하지 않음.** 카카오 토큰 발급과 이미지 저장은 Cloudflare Workers(무료 플랜) + R2.
- 결제 수단이 등록되는 곳은 R2 한 곳뿐이며, Worker를 통해서만 접근해 사용량을 묶음.
- 모든 ID는 UUID, 날짜는 `yyyy-MM-dd`, 시각은 epoch millis. Room → Firestore → 자체 서버로 그대로 이전 가능.
- 대상 플랫폼은 Android. Apple 로그인은 iOS 출시 때만 필요.

**디자인**
- 테마 4종(로맨틱 핑크 기본, 미드나잇 네이비, 포레스트 세이지, 라벤더 선셋) × 라이트/다크.
- 보조 요소는 테마색이 없는 `fill` 토큰으로 통일.

**수익화**
- 배너 없음. 보상형 광고 중심, 전면광고는 저장 후 화면 전환 시 빈도 제한. 광고 제거 1회 구매(커플 공동 적용).

## 공통 문서와 다르게 가는 부분

현재 없음. 공통 규칙에서 벗어나는 결정을 하면 이 섹션에 이유와 함께 기록합니다.

> 요금 · 정책 수치는 2026년 10월 기준으로 확인한 내용입니다. 출시 전 공식 문서에서 다시 확인하세요.
