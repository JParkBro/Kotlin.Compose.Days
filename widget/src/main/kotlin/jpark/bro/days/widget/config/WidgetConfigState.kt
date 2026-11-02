package jpark.bro.days.widget.config

import jpark.bro.days.core.domain.dday.ItemDday
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.WidgetBackground
import jpark.bro.days.core.model.WidgetSize
import jpark.bro.days.widget.WidgetDisplay

/** 위젯 하나의 설정 (product-spec 8장, 시안 design/settings-widget) */
internal data class WidgetConfigState(
    val widgetId: Int,
    val size: WidgetSize,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    /** 고를 수 있는 디데이 (디데이 + 디데이로 세기 일정) */
    val choices: List<ItemDday> = emptyList(),
    /** null이면 자동 (대표 고정 → 가장 가까운 디데이) */
    val selectedItemId: String? = null,
    val background: WidgetBackground = WidgetBackground.THEME,
    val theme: AppThemeType = AppThemeType.PINK,
    /** 미리보기에 보일 내용 */
    val display: WidgetDisplay = WidgetDisplay.Empty,
)
