package jpark.bro.days.feature.settings.impl.settings.components

import androidx.annotation.StringRes
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.feature.settings.impl.R

/** 테마 시트 · 설정 행에 쓰는 테마 이름 (theme-guide.md 3장) */
internal data class ThemeLabel(
    @StringRes val name: Int,
    @StringRes val description: Int,
    @StringRes val tag: Int,
)

internal val AppThemeType.label: ThemeLabel
    get() =
        when (this) {
            AppThemeType.PINK -> {
                ThemeLabel(
                    R.string.settings_theme_pink,
                    R.string.settings_theme_pink_desc,
                    R.string.settings_theme_pink_tag,
                )
            }

            AppThemeType.NAVY -> {
                ThemeLabel(
                    R.string.settings_theme_navy,
                    R.string.settings_theme_navy_desc,
                    R.string.settings_theme_navy_tag,
                )
            }

            AppThemeType.SAGE -> {
                ThemeLabel(
                    R.string.settings_theme_sage,
                    R.string.settings_theme_sage_desc,
                    R.string.settings_theme_sage_tag,
                )
            }

            AppThemeType.LAVENDER -> {
                ThemeLabel(
                    R.string.settings_theme_lavender,
                    R.string.settings_theme_lavender_desc,
                    R.string.settings_theme_lavender_tag,
                )
            }
        }
