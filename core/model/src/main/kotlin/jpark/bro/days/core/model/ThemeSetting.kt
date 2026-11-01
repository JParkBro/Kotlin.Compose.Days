package jpark.bro.days.core.model

/** 사용자가 고른 테마 · 화면 모드 (기기에만 저장) */
data class ThemeSetting(
    val type: AppThemeType = AppThemeType.PINK,
    val mode: AppThemeMode = AppThemeMode.SYSTEM,
)
