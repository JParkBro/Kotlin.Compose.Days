package jpark.bro.days.feature.couple.impl.paircodeconfirm

internal sealed interface PairCodeConfirmEvent {
    data object Connected : PairCodeConfirmEvent

    data object ConnectFailed : PairCodeConfirmEvent

    data object NavigateBack : PairCodeConfirmEvent
}
