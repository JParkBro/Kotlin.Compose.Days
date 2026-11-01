package jpark.bro.days.feature.settings.impl.withdraw

internal sealed interface WithdrawEvent {
    data object Withdrawn : WithdrawEvent

    data object NavigateBack : WithdrawEvent
}
