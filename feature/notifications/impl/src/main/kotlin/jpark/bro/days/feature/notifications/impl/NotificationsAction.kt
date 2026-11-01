package jpark.bro.days.feature.notifications.impl

internal sealed interface NotificationsAction {
    data class ItemClicked(
        val id: String,
    ) : NotificationsAction

    data object BackClicked : NotificationsAction
}
