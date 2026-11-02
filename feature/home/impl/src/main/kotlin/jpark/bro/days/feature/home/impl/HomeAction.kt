package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.model.ItemType

internal sealed interface HomeAction {
    data class ItemClicked(
        val id: String,
    ) : HomeAction

    data class AddClicked(
        val type: ItemType,
    ) : HomeAction

    data class QuickStartClicked(
        val quickStart: QuickStart,
    ) : HomeAction

    data object NotificationsClicked : HomeAction

    data object ConnectCoupleClicked : HomeAction

    data object CoupleBannerDismissed : HomeAction
}
