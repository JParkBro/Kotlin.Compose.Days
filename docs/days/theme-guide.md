# 테마 컬러 시스템 가이드

> 색상 값의 원본은 `assets/theme-tokens.json`입니다. `assets/theme-tokens.css`는 웹 (정책 페이지, 미리보기)용이고, Android 적용 방법은 맨 아래 "Android (Compose) 적용"을 참고하세요.

커플 디데이 앱의 테마 컬러 정의서입니다. 4개 테마(로맨틱 핑크, 미드나잇 네이비, 포레스트 세이지, 라벤더 선셋)에 각각 라이트·다크 모드를 지원해 **총 8개 팔레트**를 제공합니다.

모든 팔레트는 **같은 이름의 토큰 27개**를 사용합니다. 컴포넌트는 토큰 이름만 참조하고, 테마를 바꿀 때는 값만 교체합니다.

## 목차

1. 테마 전환 방식
2. 토큰 정의
3. 팔레트별 색상 값
4. 테마와 무관한 고정 색
5. 사용 규칙
6. 컴포넌트별 토큰 매핑
7. 명도 대비 검증
8. 기존 값에서 변경된 부분
9. 타이포그래피
10. 모서리 · 간격

## 1. 테마 전환 방식

루트 요소에 `data-theme`과 `data-mode` 두 속성을 지정합니다.

```html
<html data-theme="pink" data-mode="light">
```

| 속성 | 값 |
|---|---|
| `data-theme` | `pink` · `navy` · `sage` · `lavender` |
| `data-mode` | `light` · `dark` |

설정에서 "시스템 설정 따르기"를 선택한 경우, OS의 다크 모드 여부(`prefers-color-scheme`)를 읽어 `data-mode`에 반영합니다.

```css
.button-primary {
  background: var(--color-primary);
  color: var(--color-on-primary);
}
.button-primary:active {
  background: var(--color-primary-pressed);
}
```

## 2. 토큰 정의

CSS 변수 이름은 `--color-{토큰}` 형식입니다.


### 브랜드

| 토큰 | 용도 |
|---|---|
| `primary` | 주 CTA 버튼, 대표 디데이 숫자, 활성 탭, 토글 ON, 오늘 날짜 원 |
| `primary-pressed` | primary 버튼 눌림·호버 상태 |
| `on-primary` | primary 위 글자·아이콘 (버튼 텍스트, FAB +) |
| `primary-soft` | 히어로 카드 배경, ‘우리 둘이’ 뱃지 배경, 선택된 카드 배경 |
| `on-primary-soft` | primary-soft 위 글자 (뱃지 텍스트, 히어로 라벨) |

### 보조

| 토큰 | 용도 |
|---|---|
| `accent` | 진행바 그라데이션 끝, 캘린더 일정 마커, 보조 강조 아이콘 |
| `accent-soft` | ‘매년’·카테고리 뱃지 배경, 일정 카드 배경 |
| `on-accent-soft` | accent-soft 위 글자 |

### 표면

| 토큰 | 용도 |
|---|---|
| `bg` | 앱 전체 배경 (캔버스) |
| `surface` | 카드, 리스트 아이템, 탭바 |
| `surface-raised` | 바텀시트, 모달, 팝오버, 토스트 |
| `fill` | 세그먼트 트랙, 진행바 트랙, 비활성 칩, 보조 버튼, 아이콘 배경, 입력창 배경 |
| `overlay` | 바텀시트·모달 뒤 딤 배경 |

### 선

| 토큰 | 용도 |
|---|---|
| `border` | 카드 테두리, 리스트 구분선 |
| `border-strong` | 입력창 테두리, 토글 OFF, 아웃라인 버튼, 빈 체크박스 |

### 텍스트

| 토큰 | 용도 |
|---|---|
| `text-primary` | 제목, 본문, 리스트 디데이 숫자(기본) |
| `text-secondary` | 날짜, 설명, 보조 정보 |
| `text-tertiary` | 플레이스홀더, 비활성 탭, 비활성 텍스트, 지난 기념일 |

### 상태

| 토큰 | 용도 |
|---|---|
| `danger` | 임박 디데이(7일 이내), 연결 해제·삭제, 에러 |
| `danger-soft` | danger 뱃지·알림 배경 |
| `success` | 연결됨 상태, 달성 완료, 저장 완료 |
| `success-soft` | success 뱃지 배경 |
| `warning` | 주의 안내 (코드 만료 임박 등) |
| `warning-soft` | warning 안내 박스 배경 |

### 캘린더

| 토큰 | 용도 |
|---|---|
| `cal-sunday` | 캘린더 일요일·공휴일 숫자 |
| `cal-saturday` | 캘린더 토요일 숫자 |

### 효과

| 토큰 | 용도 |
|---|---|
| `shadow` | 카드·FAB 그림자 색 (box-shadow에 사용) |

## 3. 팔레트별 색상 값

### 로맨틱 핑크 (`pink`)

설레는 첫사랑 무드. 기본 테마.

| 토큰 | 라이트 | 다크 |
|---|---|---|
| `primary` | `#D23C69` | `#F0709A` |
| `primary-pressed` | `#B8305A` | `#F48DAE` |
| `on-primary` | `#FFFFFF` | `#2A0A16` |
| `primary-soft` | `#FCE8EE` | `#3D1E2A` |
| `on-primary-soft` | `#A62852` | `#FFB3CA` |
| `accent` | `#F28C6B` | `#F7A488` |
| `accent-soft` | `#FDEEE7` | `#3A2520` |
| `on-accent-soft` | `#A8492C` | `#FFC9B5` |
| `bg` | `#FBF8F9` | `#161114` |
| `surface` | `#FFFFFF` | `#1F181C` |
| `surface-raised` | `#FFFFFF` | `#2B2227` |
| `fill` | `#F5EFF2` | `#33292E` |
| `overlay` | `rgba(28,16,22,0.45)` | `rgba(0,0,0,0.6)` |
| `border` | `#F0E6EA` | `#33282E` |
| `border-strong` | `#DCCBD2` | `#4A3C43` |
| `text-primary` | `#221A1E` | `#F7EEF2` |
| `text-secondary` | `#6E6268` | `#B8A9B0` |
| `text-tertiary` | `#968A90` | `#85777E` |
| `danger` | `#C62828` | `#F87171` |
| `danger-soft` | `#FDECEC` | `#3A1C1F` |
| `success` | `#127235` | `#5BD69A` |
| `success-soft` | `#E7F5EC` | `#163024` |
| `warning` | `#B45309` | `#FBBF55` |
| `warning-soft` | `#FDF3E1` | `#3A2C12` |
| `cal-sunday` | `#C62828` | `#F87171` |
| `cal-saturday` | `#2563EB` | `#60A5FA` |
| `shadow` | `rgba(210,60,105,0.14)` | `rgba(0,0,0,0.4)` |

### 미드나잇 네이비 (`navy`)

차분한 쿨톤. 성별 무관 유니섹스.

| 토큰 | 라이트 | 다크 |
|---|---|---|
| `primary` | `#2563EB` | `#60A5FA` |
| `primary-pressed` | `#1D4ED8` | `#93C5FD` |
| `on-primary` | `#FFFFFF` | `#0B1120` |
| `primary-soft` | `#EAF2FF` | `#1A2C4E` |
| `on-primary-soft` | `#1D4ED8` | `#BFDBFE` |
| `accent` | `#0EA5E9` | `#38BDF8` |
| `accent-soft` | `#E3F4FD` | `#0C2A3D` |
| `on-accent-soft` | `#0369A1` | `#A5E3FC` |
| `bg` | `#F8FAFC` | `#0B1120` |
| `surface` | `#FFFFFF` | `#111A2E` |
| `surface-raised` | `#FFFFFF` | `#1A2540` |
| `fill` | `#F1F5F9` | `#1E2A44` |
| `overlay` | `rgba(10,15,30,0.45)` | `rgba(0,0,0,0.6)` |
| `border` | `#E2E8F0` | `#223150` |
| `border-strong` | `#CBD5E1` | `#334766` |
| `text-primary` | `#0F172A` | `#F1F5F9` |
| `text-secondary` | `#64748B` | `#94A3B8` |
| `text-tertiary` | `#8A94A6` | `#6B7A91` |
| `danger` | `#C62828` | `#F87171` |
| `danger-soft` | `#FDECEC` | `#3A1C1F` |
| `success` | `#127235` | `#5BD69A` |
| `success-soft` | `#E7F5EC` | `#163024` |
| `warning` | `#B45309` | `#FBBF55` |
| `warning-soft` | `#FDF3E1` | `#3A2C12` |
| `cal-sunday` | `#C62828` | `#F87171` |
| `cal-saturday` | `#2563EB` | `#60A5FA` |
| `shadow` | `rgba(37,99,235,0.14)` | `rgba(0,0,0,0.45)` |

### 포레스트 세이지 (`sage`)

눈이 편한 내추럴 그린. 일상 기록.

| 토큰 | 라이트 | 다크 |
|---|---|---|
| `primary` | `#2F7A57` | `#6CC497` |
| `primary-pressed` | `#256447` | `#8AD3AD` |
| `on-primary` | `#FFFFFF` | `#0A1A12` |
| `primary-soft` | `#E6F1EA` | `#1A3527` |
| `on-primary-soft` | `#24603F` | `#A7E3C3` |
| `accent` | `#9AAE6E` | `#C2CF9F` |
| `accent-soft` | `#EFF3E4` | `#283120` |
| `on-accent-soft` | `#56652F` | `#DCE6C0` |
| `bg` | `#F6F8F6` | `#0C1410` |
| `surface` | `#FFFFFF` | `#142019` |
| `surface-raised` | `#FFFFFF` | `#1C2B23` |
| `fill` | `#EDF2EE` | `#1F3027` |
| `overlay` | `rgba(12,20,16,0.45)` | `rgba(0,0,0,0.6)` |
| `border` | `#E1E8E2` | `#233529` |
| `border-strong` | `#C8D3CA` | `#2F4637` |
| `text-primary` | `#17231C` | `#EAF3EC` |
| `text-secondary` | `#5A6A60` | `#9AAB9F` |
| `text-tertiary` | `#8A968D` | `#6E8073` |
| `danger` | `#C62828` | `#F87171` |
| `danger-soft` | `#FDECEC` | `#3A1C1F` |
| `success` | `#127235` | `#5BD69A` |
| `success-soft` | `#E7F5EC` | `#163024` |
| `warning` | `#B45309` | `#FBBF55` |
| `warning-soft` | `#FDF3E1` | `#3A2C12` |
| `cal-sunday` | `#C62828` | `#F87171` |
| `cal-saturday` | `#2563EB` | `#60A5FA` |
| `shadow` | `rgba(47,122,87,0.14)` | `rgba(0,0,0,0.45)` |

### 라벤더 선셋 (`lavender`)

노을빛 보라. 몽환적이고 트렌디한 감성.

| 토큰 | 라이트 | 다크 |
|---|---|---|
| `primary` | `#7C3AED` | `#A78BFA` |
| `primary-pressed` | `#6D28D9` | `#C4B5FD` |
| `on-primary` | `#FFFFFF` | `#1A0F3D` |
| `primary-soft` | `#F1EBFF` | `#2E2160` |
| `on-primary-soft` | `#5B21B6` | `#DDD3FF` |
| `accent` | `#F3897A` | `#F9A08E` |
| `accent-soft` | `#FDECE8` | `#3A2228` |
| `on-accent-soft` | `#A8402F` | `#FFCFC4` |
| `bg` | `#FAF8FF` | `#0F0C1B` |
| `surface` | `#FFFFFF` | `#18142B` |
| `surface-raised` | `#FFFFFF` | `#221C3A` |
| `fill` | `#F2EFFA` | `#262040` |
| `overlay` | `rgba(20,14,40,0.45)` | `rgba(0,0,0,0.6)` |
| `border` | `#EAE5F6` | `#2C254A` |
| `border-strong` | `#D3CAEA` | `#3E3466` |
| `text-primary` | `#1E1838` | `#F5F3FF` |
| `text-secondary` | `#6B6585` | `#B9B0D6` |
| `text-tertiary` | `#8C879F` | `#7F779E` |
| `danger` | `#C62828` | `#F87171` |
| `danger-soft` | `#FDECEC` | `#3A1C1F` |
| `success` | `#127235` | `#5BD69A` |
| `success-soft` | `#E7F5EC` | `#163024` |
| `warning` | `#B45309` | `#FBBF55` |
| `warning-soft` | `#FDF3E1` | `#3A2C12` |
| `cal-sunday` | `#C62828` | `#F87171` |
| `cal-saturday` | `#2563EB` | `#60A5FA` |
| `shadow` | `rgba(124,58,237,0.14)` | `rgba(0,0,0,0.45)` |

## 4. 테마와 무관한 고정 색

| 토큰 | 값 | 용도 |
|---|---|---|
| `--color-kakao` | `#FEE500` | 카카오 로그인·공유 버튼 배경 |
| `--color-on-kakao` | `#191919` | 카카오 버튼 글자 |
| `--color-on-image` | `#FFFFFF` | 사진 위 글자·아이콘 |
| `--color-image-scrim` | `rgba(0,0,0,0.35)` | 사진 위 글자 가독성을 위한 어두운 그라데이션 |

Apple·Google 로그인 버튼은 각 사의 가이드라인 색을 따릅니다.

## 5. 사용 규칙

### 5.1 primary는 화면당 한두 곳만

주 버튼, 대표 디데이 숫자, 활성 탭처럼 시선이 가야 할 곳에만 사용합니다. primary가 많아지면 정작 중요한 숫자가 묻힙니다. 리스트의 디데이 숫자는 기본 `text-primary`로 두고, 임박한 것만 `danger`로 강조합니다.

### 5.2 soft 배경에는 짝이 되는 on 색

`primary-soft` 위 글자는 `on-primary-soft`, `accent-soft` 위 글자는 `on-accent-soft`, `danger-soft` 위 글자는 `danger`를 사용합니다. 짝을 지켜야 다크 모드에서도 대비가 유지됩니다. `primary-soft` 위에 `primary`를 쓰는 것은 대표 숫자처럼 큰 글자에만 허용합니다.

### 5.3 fill은 테마색이 섞이지 않은 회색 역할

보조 버튼, 세그먼트 트랙, 진행바 트랙, 비활성 칩, 아이콘 배경, 입력창 배경은 모두 `fill`을 사용합니다. 보조 요소에 `primary-soft`를 쓰면 화면 전체가 테마색으로 뒤덮여 강조가 사라집니다.

### 5.4 표면은 세 단계

`bg`(화면 바탕) → `surface`(카드, 리스트) → `surface-raised`(바텀시트, 모달, 토스트) 순으로 올라갑니다. 라이트 모드에서는 `surface`와 `surface-raised`가 모두 흰색이고 그림자(`shadow`)로 구분합니다. 다크 모드에서는 그림자가 잘 보이지 않으므로 `surface-raised`를 한 단계 밝게 해서 구분합니다.

### 5.5 디데이 숫자 색 규칙

대표 디데이와 다음 기념일은 `primary`, 일반 리스트는 `text-primary`, 7일 이내 임박은 `danger`, 지난 기념일은 `text-tertiary`로 통일합니다. D+ 카운트도 같은 규칙을 따릅니다. 숫자에는 `font-variant-numeric: tabular-nums`를 적용해 값이 바뀌어도 폭이 흔들리지 않게 합니다.

### 5.6 진행바는 primary → accent 그라데이션

다음 기념일까지의 진행 상황은 트랙 `fill`, 채움 `linear-gradient(90deg, primary, accent)`로 표현합니다. accent를 단독 글자색으로 쓰지 않습니다. 라이트 모드에서 accent는 흰 배경 대비가 낮아 그래픽 요소 전용입니다.

### 5.7 캘린더 마커 구분

오늘 날짜는 `primary` 채운 원 + `on-primary` 숫자, 기념일·디데이 마커는 `primary` 점, 일정 마커는 `accent` 점, 이전·다음 달 날짜는 `text-tertiary`입니다. 주말 숫자는 `cal-sunday`, `cal-saturday`를 사용합니다.

### 5.8 상태 색은 네 테마 공통

`danger`, `success`, `warning`은 라이트끼리, 다크끼리 같은 값을 씁니다. 단, 핑크 테마에서는 `danger`와 `primary`가, 세이지 테마에서는 `success`와 `primary`가 비슷해 보입니다. 상태를 색만으로 구분하지 말고 "곧 도래", "연결됨" 같은 문구나 아이콘을 함께 표시합니다.

### 5.9 그림자는 shadow 토큰으로

라이트 모드의 `shadow`는 테마 primary를 옅게 섞은 색이라 카드가 은은하게 테마 분위기를 띱니다. 다크 모드는 검정 계열입니다. 예: `box-shadow: 0 4px 16px var(--color-shadow);`

### 5.10 테마별 하드코딩 금지

컴포넌트 코드에 `#D23C69` 같은 값을 직접 쓰지 않습니다. 반드시 토큰을 참조해야 8개 팔레트 전환이 한 번에 적용됩니다. 위젯(iOS·Android 홈 위젯)도 같은 토큰 값을 네이티브 쪽에 복사해 사용합니다.

## 6. 컴포넌트별 토큰 매핑

| 컴포넌트 | 배경 | 글자 · 아이콘 | 테두리 | 기타 |
|---|---|---|---|---|
| 화면 배경 | bg | – | – | – |
| 상단 헤더 | bg (스크롤 시 surface) | text-primary | border (스크롤 시) | – |
| 히어로 카드 (대표 디데이) | primary-soft | 라벨 on-primary-soft · 숫자 primary · 날짜 text-secondary | – | shadow |
| 일반 카드 · 리스트 아이템 | surface | 제목 text-primary · 설명 text-secondary | border | – |
| 리스트 아이콘 배경 | 공유: primary-soft · 반복: accent-soft · 임박: danger-soft · 기타: fill | – | – | – |
| 주 버튼 (Primary CTA) | primary (눌림: primary-pressed) | on-primary | – | – |
| 보조 버튼 (취소 등) | fill | text-primary | – | – |
| 아웃라인 버튼 | 투명 | text-primary | border-strong | – |
| 위험 버튼 (연결 해제 등) | danger-soft 또는 danger | danger 또는 on-primary | – | – |
| FAB (+ 버튼) | primary | on-primary | – | shadow |
| 필터 칩 (활성) | text-primary | bg | – | – |
| 필터 칩 (비활성) | fill | text-secondary | – | – |
| 세그먼트 컨트롤 | 트랙 fill · 선택 surface | 선택 text-primary · 비선택 text-secondary | – | 선택 항목 shadow |
| 뱃지: 우리 둘이 | primary-soft | on-primary-soft | – | – |
| 뱃지: 나만 보기 | fill | text-secondary | – | – |
| 뱃지: 매년·카테고리 | accent-soft | on-accent-soft | – | – |
| 뱃지: 곧 도래 | danger-soft | danger | – | – |
| 뱃지: 연결됨·완료 | success-soft | success | – | – |
| 토글 ON | primary | 손잡이 on-primary | – | – |
| 토글 OFF | border-strong | 손잡이 surface | – | – |
| 입력창 (기본) | surface | 입력 text-primary · 플레이스홀더 text-tertiary | border-strong | – |
| 입력창 (포커스) | surface | text-primary | primary 1.5px + 바깥 링 primary-soft 3px | – |
| 체크박스 (빈 상태) | surface | – | border-strong | – |
| 체크박스 (선택) | primary | 체크 on-primary | – | – |
| 진행바 | 트랙 fill · 채움 primary→accent | – | – | – |
| 하단 탭바 | surface | 활성 primary · 비활성 text-tertiary | 상단 border | – |
| 바텀시트 · 모달 | surface-raised | text-primary | – | 뒤 배경 overlay |
| 시트 드래그 핸들 | border-strong | – | – | – |
| 안내 박스 (팁) | fill | text-secondary | – | – |
| 안내 박스 (주의) | warning-soft | warning | – | – |
| 토스트 | text-primary | bg | – | shadow |
| 캘린더 오늘 | primary | on-primary | – | – |
| 캘린더 마커 | 기념일 primary · 일정 accent | – | – | – |
| 구분선 | – | – | border | – |

## 7. 명도 대비 검증

WCAG 기준: 일반 텍스트 4.5:1 이상, 큰 텍스트(24px 이상 또는 18.7px 이상 굵게)와 UI 그래픽 3:1 이상. 모든 팔레트에서 아래 조합을 계산해 확인했습니다.

| 조합 | 용도 | 로맨틱 핑크 라이트 | 로맨틱 핑크 다크 | 미드나잇 네이비 라이트 | 미드나잇 네이비 다크 | 포레스트 세이지 라이트 | 포레스트 세이지 다크 | 라벤더 선셋 라이트 | 라벤더 선셋 다크 |
|---|---|---|---|---|---|---|---|---|---|
| `on-primary` / `primary` | 버튼 글자 | 4.57 | 6.49 | 5.17 | 7.41 | 5.2 | 8.53 | 5.7 | 6.54 |
| `on-primary-soft` / `primary-soft` | 뱃지·히어로 라벨 | 5.88 | 8.86 | 5.95 | 9.76 | 6.43 | 9.11 | 7.73 | 9.87 |
| `on-accent-soft` / `accent-soft` | 카테고리 뱃지 | 5.08 | 9.74 | 5.26 | 10.6 | 5.64 | 10.4 | 5.33 | 10.4 |
| `text-primary` / `bg` | 본문 | 16.12 | 16.42 | 17.06 | 17.19 | 15.21 | 16.5 | 16.04 | 17.59 |
| `text-secondary` / `surface` | 보조 텍스트 | 5.82 | 7.75 | 4.76 | 6.76 | 5.73 | 6.96 | 5.49 | 8.73 |
| `text-tertiary` / `surface` | 비활성 텍스트 | 3.31 | 4.1 | 3.06 | 3.98 | 3.08 | 4.0 | 3.45 | 4.29 |
| `danger` / `danger-soft` | 임박 뱃지 | 4.92 | 5.56 | 4.92 | 5.56 | 4.92 | 5.56 | 4.92 | 5.56 |

`text-tertiary`는 플레이스홀더·비활성 표시 전용이라 3:1 수준으로 설계했습니다. 읽어야 하는 정보에는 `text-secondary` 이상을 사용합니다. `accent`는 라이트 모드에서 흰 배경 대비가 낮아 글자색으로 쓰지 않고 그래픽(진행바, 마커)에만 사용합니다.

## 8. 기존 값에서 변경된 부분

| 항목 | 기존 (4.md) | 변경 | 이유 |
|---|---|---|---|
| 핑크 라이트 primary | `#E0507A` | `#D23C69` | 흰 글자 대비 3.75:1 → 4.57:1. 버튼 글자 기준 충족. 눈으로 보기엔 차이가 거의 없음 |
| 핑크 다크 primary | `#F472B6` | `#F0709A` | 기존 값은 마젠타에 가까워 라이트의 로즈 톤과 어긋남 |
| 핑크 다크 bg | `#14090E` | `#161114` | 붉은 기가 강해 장시간 보기 피로함. 중립에 가깝게 조정 |
| 세이지 라이트 primary | `#059669` | `#2F7A57` | 흰 글자 대비 3.76:1 → 5.2:1. 에메랄드보다 이름에 맞는 차분한 세이지 톤 |
| 세이지 다크 primary | `#34D399` | `#6CC497` | 라이트와 같은 세이지 계열로 통일 |
| 네이비 다크 primary-soft | `#1E3A8A` | `#1A2C4E` | 채도가 높아 카드 배경으로 쓰기에 너무 강함 |
| 상태 색 (라이트) | 정의 없음 | danger `#C62828` · success `#127235` · warning `#B45309` | soft 배경 위에서도 4.5:1 이상 |
| 추가 토큰 | 9개 | 27개 | pressed, on-soft, fill, raised, overlay, border-strong, tertiary, 상태, 캘린더, shadow 추가 |


## 9. 타이포그래피

글꼴은 **Pretendard**(SIL OFL 1.1) 한 가지입니다. 크기 · 굵기 · 줄 간격 · 자간은 Stitch 시안의 `design/_stitch-design-system/DESIGN.md` typography 값을 그대로 쓰고, 글꼴만 Pretendard로 바꿉니다.

| 스타일 | 크기(sp) | 굵기 | 줄 간격(sp) | 자간 | 숫자 | 용도 (권장) |
|---|---|---|---|---|---|---|
| `displayHero` | 44 | 800 | 52 | -0.03em | tnum | 히어로 · 대표 디데이 숫자 (큰 화면) |
| `displayHeroMobile` | 36 | 800 | 44 | -0.02em | tnum | 히어로 · 대표 디데이 숫자 |
| `headlineLg` | 28 | 700 | 36 | -0.02em |  | 화면 큰 제목 |
| `headlineMd` | 22 | 700 | 30 | -0.015em |  | 섹션 제목 |
| `headlineSm` | 18 | 600 | 24 | -0.01em |  | 카드 제목 |
| `bodyLg` | 17 | 400 | 24 | -0.01em |  | 강조 본문 |
| `bodyMd` | 15 | 400 | 22 | -0.005em |  | 기본 본문 |
| `bodySm` | 13 | 400 | 18 | 0em |  | 보조 설명 · 날짜 |
| `labelLg` | 15 | 600 | 20 | -0.005em |  | 버튼 |
| `labelMd` | 13 | 600 | 18 | 0em |  | 칩 · 탭 |
| `labelSm` | 11 | 600 | 14 | 0.02em |  | 뱃지 · 캡션 |
| `metricTabular` | 24 | 700 | 28 | -0.02em | tnum | 리스트 디데이 숫자 |

- 포함한 굵기: Regular(400) · Medium(500) · SemiBold(600) · Bold(700) · ExtraBold(800). 시안에서 쓰지 않는 Thin · Light · Black은 넣지 않습니다.
- 디데이 숫자는 `tnum`(tabular-nums) 스타일을 씁니다 (5.5).
- 라이선스 원문은 `core/designsystem/licenses/Pretendard-OFL.txt`. oss-licenses 플러그인 목록에는 자동으로 들어가지 않으므로 따로 고지합니다 (product-spec 6장).

## 10. 모서리 · 간격

값은 Stitch `DESIGN.md`의 rounded · spacing을 그대로 씁니다. 코드에서는 `AppShapes` · `AppSpacing`으로만 참조하고 모서리 값을 직접 쓰지 않습니다 (CODING_CONVENTIONS 6-5).

| 모서리 | 값 | | 간격 | 값 |
|---|---|---|---|---|
| `sm` | 4dp | | `xs` | 4dp |
| `default` | 8dp | | `sm` | 8dp |
| `md` | 12dp | | `md` | 14dp |
| `lg` | 16dp (카드) | | `lg` | 20dp |
| `xl` | 24dp | | `xl` | 28dp |
| `full` | 원 · 알약 | | `xxl` | 40dp |
| | | | `marginMobile` · `margin` | 16dp · 20dp (화면 좌우) |
| | | | `gutter` · `gutterCompact` | 16dp · 12dp (카드 사이) |

아이콘은 Material Symbols Outlined(Apache 2.0)를 VectorDrawable로 `core:designsystem`의 `res/drawable/ic_*`에 둡니다.

## Android(Compose) 적용

`core:designsystem` 모듈에서 아래 구조로 사용합니다.

```kotlin
@Immutable
data class AppColors(
    val primary: Color,
    val primaryPressed: Color,
    val onPrimary: Color,
    val primarySoft: Color,
    val onPrimarySoft: Color,
    val accent: Color,
    val accentSoft: Color,
    val onAccentSoft: Color,
    val bg: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val fill: Color,
    val overlay: Color,
    val border: Color,
    val borderStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val danger: Color,
    val dangerSoft: Color,
    val success: Color,
    val successSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val calSunday: Color,
    val calSaturday: Color,
    val shadow: Color,
)

enum class AppThemeType { PINK, NAVY, SAGE, LAVENDER }
enum class AppThemeMode { LIGHT, DARK, SYSTEM }

val LocalAppColors = staticCompositionLocalOf<AppColors> { error("AppTheme 밖에서 사용") }

@Composable
fun AppTheme(
    type: AppThemeType = AppThemeType.PINK,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = palette(type, dark)   // theme-tokens.json 값으로 만든 8개 팔레트 중 선택
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(content = content)  // Material 컴포넌트를 쓸 경우 colorScheme 매핑 추가
    }
}

object AppThemeTokens {
    val colors: AppColors @Composable get() = LocalAppColors.current
}
```

- 토큰 이름은 JSON의 kebab-case를 camelCase로 바꿉니다 (`primary-soft` → `primarySoft`).
- 컴포넌트에서는 `AppThemeTokens.colors.primary`처럼 토큰만 참조합니다. `Color(0xFF…)` 하드코딩은 금지입니다 (CODING_CONVENTIONS 6-5).
- 8개 팔레트(`ThemeTokens.kt`)는 **생성 파일**입니다. 값은 `theme-tokens.json`만 고치고 `python3 scripts/generate-theme-colors.py`로 다시 만듭니다. `ThemeTokensSyncTest`가 JSON과 생성 코드가 다르면 실패합니다.
- 글자 스타일(9장)은 `AppThemeTokens.typography.bodyMd`처럼 참조합니다. Material 컴포넌트용 `Typography`도 같은 값으로 매핑합니다 (`displayLarge`=`displayHero`, `headline*`, `titleLarge`=`headlineSm`, `titleMedium`=`labelLg`, `body*`, `label*`).
- 고정 색(4장)은 `AppFixedColors`(`kakao`, `onKakao`, `onImage`, `imageScrim`)로 씁니다.
- Material 컴포넌트도 토큰 색을 쓰도록 `AppTheme`이 `colorScheme`을 매핑합니다: `primary`·`onPrimary`, `primaryContainer`=`primary-soft`, `secondary`=`accent`, `background`=`bg`, `surface`·`surfaceContainer`=`surface`, `surfaceContainerHigh(est)`=`surface-raised`, `surfaceVariant`=`fill`, `onSurface`=`text-primary`, `onSurfaceVariant`=`text-secondary`, `outline`=`border-strong`, `outlineVariant`=`border`, `error`=`danger`, `scrim`=`overlay`.
- 선택한 테마 · 모드는 `core:datastore`(`SettingsDataStore`)에 저장하고 `SettingsRepository.observeThemeSetting()`으로 읽습니다. `MainActivity`가 이 값으로 `AppTheme`을 그리고, 화면 모드에 맞춰 시스템 바 아이콘 색도 바꿉니다. 위젯도 같은 팔레트를 사용합니다.
- 다크 여부는 `isDarkTheme(mode)`로 계산합니다 (SYSTEM은 기기 설정).
