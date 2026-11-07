# 데이즈(Days) 프로젝트 문서

커플 · 개인 디데이 앱 **데이즈**에만 해당하는 문서입니다. 코드 구조 · 규칙 · 테스트 같은 범용 내용은 [`../common/`](../common/README.md)을 따르고, 이 폴더에는 데이즈의 결정 사항만 둡니다.

## 문서 목록

| 문서 | 내용 | 주로 보는 시점 |
|---|---|---|
| [product-spec.md](product-spec.md) | 기능 범위, 항목 · 계산 규칙, 화면 목록 · 동작, 설정 구조, 샘플 데이터 | 기획 · 디자인 · 개발 전반 |
| [theme-guide.md](theme-guide.md) | 4 테마 × 라이트/다크 = 8 팔레트, 토큰 27개, 사용 규칙, Compose 적용 | 디자인 · UI 개발 |
| [module-map.md](module-map.md) | 모듈 목록, 내비게이션 키, 기능 간 의존, 화면별 Event 유무, 개발 순서 | 개발 시작 · 기능 추가 |
| [data-model-erd.md](data-model-erd.md) | ERD, Room · Firestore · Storage · 자체 서버 배치, Entity 예시 | 데이터 작업 |
| [sync-and-pairing.md](sync-and-pairing.md) | 동기화 흐름, 커플 코드 연결, 보안 규칙, 연결 해제 · 탈퇴 | 커플 기능 개발 |
| [infra-and-billing-protection.md](infra-and-billing-protection.md) | Firebase(Blaze) 구성, Functions · Storage 구현, 과금 방어(예산 · 결제 자동 해제), 출시 체크리스트 | 서버 작업 · 출시 전 |
| [firebase-cost-strategy.md](firebase-cost-strategy.md) | Blaze 비용 구성, Firestore 무료 사용량, 읽기 · 쓰기 절감 설계 | 동기화 설계 · 운영 |
| [ads-strategy.md](ads-strategy.md) | 광고 배치 판단, 빈도 규칙, 광고 제거 구매, AdMob 정책 | 수익화 작업 |
| `assets/theme-tokens.json` | 테마 색상 값 원본 (8 팔레트 × 27 토큰) | 디자인 시스템 코드 생성 |
| `assets/theme-tokens.css` | 같은 값의 CSS 변수 (정책 페이지 · 웹 미리보기) | 웹 작업 |
| `design/{화면}/` | Stitch 시안: `screen.png` + `code.html` (화면별 폴더). 색 · 글꼴 · 기능 범위는 이 폴더 문서가 기준이고 시안은 레이아웃 참고용 | 화면 구현 |

## AI 도구 지침 (Claude Code)

저장소 루트와 하위 폴더의 `CLAUDE.md`가 이 문서들을 기준으로 작업 지침을 요약합니다. 문서의 결정이 바뀌면 해당 `CLAUDE.md`도 함께 고칩니다.

| 파일 | 범위 |
|---|---|
| `CLAUDE.md` | 프로젝트 전체: 문서 지도, 절대 규칙, Git, 비밀 정보, 언어 + Android 앱(모듈 · MVI · 데이터 · UI 규칙, Gradle 명령, 테스트 요구 사항) |
| `firebase/functions/CLAUDE.md` | Cloud Functions 보안 · 과금 규칙, 명령 |
| `firebase/CLAUDE.md` | 보안 규칙 원칙, 필수 테스트 시나리오 |

> 처음 읽을 때는 위 표의 순서(product-spec → theme-guide → module-map → data-model-erd …)를 추천합니다.

## 핵심 결정 요약

**제품**
- 솔로 모드가 기본, 커플 연결은 그 위에 얹는 기능. 항목마다 "나만 보기" / "우리 둘이".
- 항목은 디데이와 일정 두 종류. 자동 기념일(100일 단위 · 주년 · 생일)은 저장하지 않고 계산.
- 하단 탭은 홈 / 캘린더 / 설정.

**기술**
- 원본 데이터는 Room. Firestore는 "우리 둘이" 공유 데이터의 통로로만 사용.
- **서버는 Firebase Blaze 한 곳.** 카카오 토큰 발급은 Cloud Functions, 이미지는 Cloud Storage, 정책 페이지는 Firebase Hosting. Cloudflare는 쓰지 않음 (2026-11 변경, 처음에는 Spark + Cloudflare).
- Blaze는 넘으면 바로 과금되므로 **예산 알림 + 예산 초과 시 결제 자동 해제 함수**로 상한을 두고, 보안 규칙 · App Check · 함수 인스턴스 제한으로 남용을 막음.
- dev · prod 빌드가 Firebase 프로젝트 하나(`days-61328`)를 함께 씀. 디버그 빌드는 에뮬레이터 연결을 우선함.
- 모든 ID는 UUID, 날짜는 `yyyy-MM-dd`, 시각은 epoch millis. Room → Firestore → 자체 서버로 그대로 이전 가능.
- 대상 플랫폼은 Android. Apple 로그인은 iOS 출시 때만 필요.

**디자인**
- 테마 4종(로맨틱 핑크 기본, 미드나잇 네이비, 포레스트 세이지, 라벤더 선셋) × 라이트/다크.
- 보조 요소는 테마색이 없는 `fill` 토큰으로 통일.

**수익화**
- 배너 · 전면광고 없음. 보상형 광고는 커플 사진 한도를 다 썼을 때 "사진 1장 더"만 (서버 측 확인). 광고 제거 구매 대신 혜택 없는 후원(개발자 응원하기) (2026-11).

## 공통 문서와 다르게 가는 부분

공통 규칙에서 벗어나는 결정을 이유와 함께 기록합니다.

| 항목 | 공통 규칙 | 데이즈 | 이유 |
|---|---|---|---|
| Room 스키마 버전 | 스키마를 바꾸면 version을 올리고 Migration · 마이그레이션 테스트 추가 | **출시 전까지 version 1을 고쳐 쓰고** `schemas/…/1.json`을 덮어씀. 개발 기기에서 스키마가 안 맞으면 앱 데이터를 지우고 다시 설치 | 출시 전에는 보존할 사용자 데이터가 없어 Migration이 비용만 듦. **첫 출시 빌드부터는 공통 규칙을 따름** |

> 요금 · 정책 수치는 2026년 10월 기준으로 확인한 내용입니다. 출시 전 공식 문서에서 다시 확인하세요.
