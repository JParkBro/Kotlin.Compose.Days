package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.model.ItemType

internal sealed interface HomeAction {
    /** 디데이는 상세로, 일정은 바로 수정으로 (product-spec 4-5) */
    data class ItemClicked(
        val id: String,
        val type: ItemType,
    ) : HomeAction

    /** 빈 상태 버튼 · 다가오는 일정 카드 · 추가 시트에서 고른 종류로 추가 화면을 연다 */
    data class AddClicked(
        val type: ItemType,
    ) : HomeAction

    data class QuickStartClicked(
        val quickStart: QuickStart,
    ) : HomeAction

    data class FilterSelected(
        val filter: HomeFilter,
    ) : HomeAction

    data object FabClicked : HomeAction

    data object AddSheetDismissed : HomeAction

    data object NotificationsClicked : HomeAction

    data object ConnectCoupleClicked : HomeAction

    data object CoupleBannerDismissed : HomeAction
}
