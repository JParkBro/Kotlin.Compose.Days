package jpark.bro.days.feature.couple.impl.couplemanage

internal sealed interface CoupleManageAction {
    data object BackClicked : CoupleManageAction
}
