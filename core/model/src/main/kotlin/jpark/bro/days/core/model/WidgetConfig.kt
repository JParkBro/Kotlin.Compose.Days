package jpark.bro.days.core.model

/** 위젯 크기 (product-spec 8장). Android는 잠금화면 위젯이 없어 두 가지만 */
enum class WidgetSize {
    /** 2×2 */
    SMALL,

    /** 4×2 */
    MEDIUM,
}

/** 위젯 배경 (product-spec 8장). 사진 배경은 이미지 저장소 단계에서 추가 */
enum class WidgetBackground {
    /** 앱 테마의 primary-soft, 기기 다크 모드를 따름 */
    THEME,
    DARK,
    WHITE,

    /** 보여주는 디데이의 배경 사진. 사진이 없는 디데이면 [THEME]처럼 보인다 */
    PHOTO,
}

/**
 * 홈 화면에 놓인 위젯 하나의 설정 (기기에만 저장).
 * [widgetId]는 시스템이 정한 appWidgetId, [itemId]가 null이면 홈 히어로와 같은 규칙으로 고른다
 */
data class WidgetConfig(
    val widgetId: Int,
    val size: WidgetSize,
    val itemId: String? = null,
    val background: WidgetBackground = WidgetBackground.THEME,
    val showProfile: Boolean = false,
)
