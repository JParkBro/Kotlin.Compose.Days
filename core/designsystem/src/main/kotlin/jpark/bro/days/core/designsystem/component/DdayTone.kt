package jpark.bro.days.core.designsystem.component

/**
 * 디데이 숫자 색 (theme-guide.md 5.5). D+도 같은 규칙.
 * 어떤 tone인지(7일 이내 등)는 core:domain 계산 결과로 화면이 정한다.
 */
enum class DdayTone {
    /** 대표 디데이 · 다음 기념일: primary */
    HIGHLIGHT,

    /** 일반 리스트: text-primary */
    DEFAULT,

    /** 7일 이내 임박: danger */
    SOON,

    /** 지난 기념일: text-tertiary */
    PASSED,
}
