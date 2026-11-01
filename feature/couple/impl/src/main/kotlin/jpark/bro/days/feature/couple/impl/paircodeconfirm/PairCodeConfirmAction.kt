package jpark.bro.days.feature.couple.impl.paircodeconfirm

internal sealed interface PairCodeConfirmAction {
    data object ConnectClicked : PairCodeConfirmAction

    data object BackClicked : PairCodeConfirmAction
}
