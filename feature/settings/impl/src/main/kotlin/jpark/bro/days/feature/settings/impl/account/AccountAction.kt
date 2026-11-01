package jpark.bro.days.feature.settings.impl.account

internal sealed interface AccountAction {
    data object LogoutClicked : AccountAction

    data object WithdrawClicked : AccountAction

    data object BackClicked : AccountAction
}
