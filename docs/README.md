# docs

```
docs/
├─ common/                          # 범용: 다른 프로젝트에도 그대로 복사해서 사용
│  ├─ README.md
│  ├─ ANDROID_ARCHITECTURE.md       # 모듈 · 계층 · 의존 방향 · Navigation 3 · Koin · Ktor
│  ├─ CODING_CONVENTIONS.md         # 이름 · MVI · Compose · 코루틴 · 에러 처리 · Git
│  ├─ TESTING.md                    # 테스트 종류 · 템플릿 · CI
│  └─ GIT_CONVENTIONS.md            # 브랜치 · 커밋 · PR · 리뷰 · 릴리스
│
└─ days/                            # 데이즈 전용
   ├─ README.md
   ├─ firebase-cost-strategy.md
   ├─ sync-and-pairing.md
   ├─ data-model-erd.md
   ├─ ads-strategy.md
   ├─ infra-and-billing-protection.md
   ├─ theme-guide.md
   ├─ product-spec.md
   ├─ module-map.md
   └─ assets/
      ├─ theme-tokens.json
      └─ theme-tokens.css
```

- **common**은 "어떻게 만드는가", **days**는 "무엇을 만드는가"를 다룹니다.
- 두 문서가 충돌하면 days 문서의 "공통 문서와 다르게 가는 부분"에 기록된 예외만 days를 따르고, 나머지는 common을 따릅니다.
