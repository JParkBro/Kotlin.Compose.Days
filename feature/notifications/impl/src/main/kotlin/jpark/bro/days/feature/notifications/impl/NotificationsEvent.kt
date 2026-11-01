package jpark.bro.days.feature.notifications.impl

internal sealed interface NotificationsEvent {
    data class OpenItemDetail(
        val id: String,
    ) : NotificationsEvent

    data object NavigateBack : NotificationsEvent
}
