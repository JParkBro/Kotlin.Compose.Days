package jpark.bro.days.feature.settings.impl.account

internal sealed interface AccountEvent {
    data object LoggedOut : AccountEvent

    data object OpenWithdraw : AccountEvent

    data object NavigateBack : AccountEvent
}
