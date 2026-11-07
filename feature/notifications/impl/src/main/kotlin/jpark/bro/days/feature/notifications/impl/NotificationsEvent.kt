package jpark.bro.days.feature.notifications.impl

import jpark.bro.days.core.model.ItemType

internal sealed interface NotificationsEvent {
    /** 디데이는 상세, 일정은 수정 화면 (product-spec 4-5) */
    data class OpenItem(
        val itemId: String,
        val itemType: ItemType,
    ) : NotificationsEvent

    data object OpenSettings : NotificationsEvent

    data object MarkReadFailed : NotificationsEvent

    data object NavigateBack : NotificationsEvent
}
