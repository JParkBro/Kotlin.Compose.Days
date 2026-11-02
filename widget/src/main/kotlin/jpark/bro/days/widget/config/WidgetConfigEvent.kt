package jpark.bro.days.widget.config

internal sealed interface WidgetConfigEvent {
    /** 저장 후 · 닫기 */
    data object Close : WidgetConfigEvent

    data object SaveFailed : WidgetConfigEvent
}
