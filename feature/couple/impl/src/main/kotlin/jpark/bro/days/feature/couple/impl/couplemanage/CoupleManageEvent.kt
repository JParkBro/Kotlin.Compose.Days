package jpark.bro.days.feature.couple.impl.couplemanage

internal sealed interface CoupleManageEvent {
    data object NavigateBack : CoupleManageEvent
}
