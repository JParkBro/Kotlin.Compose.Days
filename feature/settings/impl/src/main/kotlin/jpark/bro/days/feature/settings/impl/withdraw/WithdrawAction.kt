package jpark.bro.days.feature.settings.impl.withdraw

internal sealed interface WithdrawAction {
    data object WithdrawConfirmed : WithdrawAction

    data object BackClicked : WithdrawAction
}
