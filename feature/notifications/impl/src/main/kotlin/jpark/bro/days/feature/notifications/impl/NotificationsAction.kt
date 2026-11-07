package jpark.bro.days.feature.notifications.impl

internal sealed interface NotificationsAction {
    /** 읽음 처리하고 항목이 남아 있으면 연다 */
    data class RowClicked(
        val id: String,
    ) : NotificationsAction

    data object MarkAllReadClicked : NotificationsAction

    data object SettingsClicked : NotificationsAction

    data object BackClicked : NotificationsAction
}
