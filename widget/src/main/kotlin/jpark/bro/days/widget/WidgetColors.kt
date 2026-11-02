package jpark.bro.days.widget

import androidx.glance.color.ColorProvider
import androidx.glance.unit.ColorProvider
import jpark.bro.days.core.designsystem.AppColors
import jpark.bro.days.core.designsystem.themePalette
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.WidgetBackground

/** 위젯 색 (theme-guide 6장 히어로 카드 매핑). 앱과 같은 팔레트를 쓴다 */
internal data class WidgetColors(
    val background: ColorProvider,
    val title: ColorProvider,
    val number: ColorProvider,
    val secondary: ColorProvider,
)

/**
 * - THEME: 히어로 카드와 같은 primary-soft 배경. 기기 다크 모드를 따른다
 * - DARK · WHITE: 기기 모드와 상관없이 고정
 */
internal fun widgetColors(
    theme: AppThemeType,
    background: WidgetBackground,
): WidgetColors {
    val light = themePalette(theme, dark = false)
    val dark = themePalette(theme, dark = true)
    return when (background) {
        WidgetBackground.THEME -> {
            WidgetColors(
                background = ColorProvider(day = light.primarySoft, night = dark.primarySoft),
                title = ColorProvider(day = light.onPrimarySoft, night = dark.onPrimarySoft),
                number = ColorProvider(day = light.primary, night = dark.primary),
                secondary = ColorProvider(day = light.textSecondary, night = dark.textSecondary),
            )
        }

        WidgetBackground.DARK -> {
            fixed(dark)
        }

        WidgetBackground.WHITE -> {
            fixed(light)
        }
    }
}

private fun fixed(palette: AppColors): WidgetColors =
    WidgetColors(
        background = ColorProvider(palette.surface),
        title = ColorProvider(palette.textPrimary),
        number = ColorProvider(palette.primary),
        secondary = ColorProvider(palette.textSecondary),
    )
