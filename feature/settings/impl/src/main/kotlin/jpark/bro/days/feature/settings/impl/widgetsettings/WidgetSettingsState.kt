package jpark.bro.days.feature.settings.impl.widgetsettings

import jpark.bro.days.core.model.WidgetConfig

/** 놓인 위젯 한 줄. [itemTitle]이 null이면 자동, [isMissing]이면 고른 디데이가 삭제됨 */
internal data class WidgetRow(
    val config: WidgetConfig,
    val itemTitle: String?,
    val isMissing: Boolean,
)

internal data class WidgetSettingsState(
    val isLoading: Boolean = true,
    val widgets: List<WidgetRow> = emptyList(),
    /** 런처가 앱에서 위젯 추가 요청을 받을 때만 "위젯 추가" 버튼 */
    val canAddWidget: Boolean = false,
    val isSizeSheetOpen: Boolean = false,
)
