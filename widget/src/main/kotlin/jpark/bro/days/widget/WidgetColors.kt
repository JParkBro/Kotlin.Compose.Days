package jpark.bro.days.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider
import androidx.glance.unit.ColorProvider
import jpark.bro.days.core.designsystem.AppColors
import jpark.bro.days.core.designsystem.themePalette
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.WidgetBackground

/** 위젯 한 벌의 색 (theme-guide 6장 히어로 카드 매핑) */
internal data class WidgetPalette(
    val background: Color,
    val title: Color,
    val number: Color,
    val secondary: Color,
)

/** 기기 라이트 · 다크 모드 각각의 색. 위젯(Glance)과 설정 화면 미리보기(Compose)가 같이 쓴다 */
internal data class WidgetColorScheme(
    val day: WidgetPalette,
    val night: WidgetPalette,
) {
    fun pick(dark: Boolean): WidgetPalette = if (dark) night else day
}

/**
 * 앱과 같은 팔레트를 쓴다.
 * - THEME: 히어로 카드와 같은 primary-soft 배경. 기기 다크 모드를 따른다
 * - DARK · WHITE: 기기 모드와 상관없이 고정
 */
internal fun widgetColorScheme(
    theme: AppThemeType,
    background: WidgetBackground,
): WidgetColorScheme {
    val light = themePalette(theme, dark = false)
    val dark = themePalette(theme, dark = true)
    return when (background) {
        WidgetBackground.THEME -> WidgetColorScheme(day = light.soft(), night = dark.soft())
        WidgetBackground.DARK -> dark.surface().let { WidgetColorScheme(it, it) }
        WidgetBackground.WHITE -> light.surface().let { WidgetColorScheme(it, it) }
    }
}

private fun AppColors.soft() = WidgetPalette(primarySoft, onPrimarySoft, primary, textSecondary)

private fun AppColors.surface() = WidgetPalette(surface, textPrimary, primary, textSecondary)

/** Glance용 색 */
internal data class WidgetColors(
    val background: ColorProvider,
    val title: ColorProvider,
    val number: ColorProvider,
    val secondary: ColorProvider,
)

internal fun WidgetColorScheme.toGlance(): WidgetColors =
    WidgetColors(
        background = ColorProvider(day = day.background, night = night.background),
        title = ColorProvider(day = day.title, night = night.title),
        number = ColorProvider(day = day.number, night = night.number),
        secondary = ColorProvider(day = day.secondary, night = night.secondary),
    )
