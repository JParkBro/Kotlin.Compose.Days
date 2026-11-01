# Cloudflare Worker

설계 원문: `docs/days/infra-and-billing-protection.md`

## 역할

| 엔드포인트 | 내용 |
|---|---|
| `POST /auth/kakao` | 카카오 액세스 토큰 검증(app_id 확인) → Firebase 커스텀 토큰 발급 (`uid = kakao:{id}`) |
| `PUT /images/{coupleId}/{imageId}` | 업로드 |
| `GET /images/{coupleId}/{imageId}` | 다운로드 |
| `DELETE /images/{coupleId}/{imageId}` | 삭제 |

## 규칙

- **이미지 요청은 매번** Firebase ID 토큰과 App Check 토큰을 검증한다 (`jose`의 `createRemoteJWKSet` + `jwtVerify`).
- 커플 멤버 확인은 **사용자의 ID 토큰으로** Firestore REST API를 호출해 `couples/{coupleId}` 읽기 성공 여부로 판단한다. 다운로드는 D1 `member_cache`(24시간)를 먼저 본다.
- 업로드 제한(파일 크기, 사용자당 하루 횟수, 커플당 개수 · 용량)은 **환경 변수**로 두고 D1(`couple_usage`, `daily_upload`)로 집계한다. 앱 쪽 값(Remote Config)을 신뢰하지 않는다.
- R2 버킷은 비공개. **presigned URL · 공개 URL을 만들지 않는다.** 모든 R2 접근은 Worker를 거친다.
- 서비스 계정 비공개 키 등 비밀 값은 `wrangler secret put`으로만. `wrangler.toml`, 코드, `.dev.vars` 커밋 금지.
- 무료 플랜 CPU 10ms 제한을 고려한다. 키 import 결과 · JWKS는 전역에 캐시하고 요청마다 반복하지 않는다.
- 에러 응답은 `{ "error": "코드" }` 형식, 내부 정보(스택, 키 이름)를 노출하지 않는다.

## 명령어

> 패키지 매니저 · 테스트 도구가 실제와 다르면 `package.json`을 확인하고 이 표를 고친다.

| 목적 | 명령 |
|---|---|
| 로컬 실행 | `npx wrangler dev` |
| 테스트 | `npm test` |
| 타입 검사 | `npx tsc --noEmit` |
| D1 마이그레이션 (로컬) | `npx wrangler d1 migrations apply <DB 이름> --local` |
| 배포 | 직접 실행하지 않는다. 사용자에게 확인 후 진행 |

## 완료 전 확인

- 타입 검사 · 테스트 통과
- 새 엔드포인트에 토큰 검증 · 제한 확인이 빠지지 않았는지
- `docs/days/infra-and-billing-protection.md`와 동작이 일치하는지
