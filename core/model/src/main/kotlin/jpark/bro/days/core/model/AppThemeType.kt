package jpark.bro.days.core.model

/** 컬러 테마 4종 (theme-guide.md 3장) */
enum class AppThemeType {
    /** 로맨틱 핑크 (기본) */
    PINK,

    /** 미드나잇 네이비 */
    NAVY,

    /** 포레스트 세이지 */
    SAGE,

    /** 라벤더 선셋 */
    LAVENDER,
}

/** 화면 모드. SYSTEM은 기기 다크 모드 설정을 따른다. */
enum class AppThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}
