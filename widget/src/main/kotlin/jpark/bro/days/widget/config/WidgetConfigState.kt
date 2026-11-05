package jpark.bro.days.widget.config

import jpark.bro.days.core.domain.dday.ItemDday
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.WidgetBackground
import jpark.bro.days.core.model.WidgetSize
import jpark.bro.days.widget.WidgetDisplay
import jpark.bro.days.widget.WidgetProfile
import java.io.File

/** 설정할 위젯 (런처가 준 id와 크기) */
internal data class WidgetTarget(
    val widgetId: Int,
    val size: WidgetSize,
)

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
    /** 커플이면 두 사람, 솔로면 나 (product-spec 8장) */
    val showProfile: Boolean = false,
    val theme: AppThemeType = AppThemeType.PINK,
    /** 미리보기에 보일 내용 */
    val display: WidgetDisplay = WidgetDisplay.Empty,
    /** 미리보기에 보일 디데이 배경 사진 (사진 배경일 때) */
    val photo: File? = null,
    val profiles: List<WidgetProfile> = emptyList(),
)
