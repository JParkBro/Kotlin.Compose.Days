package jpark.bro.days.feature.couple.impl.paircodeconfirm

import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.couple.impl.PairCodeConfirmKey

internal class PairCodeConfirmViewModel(
    key: PairCodeConfirmKey,
) : MviViewModel<PairCodeConfirmState, PairCodeConfirmAction, PairCodeConfirmEvent>(
        PairCodeConfirmState(code = key.code),
    ) {
    override fun onAction(action: PairCodeConfirmAction) {
        when (action) {
            PairCodeConfirmAction.ConnectClicked -> sendEvent(PairCodeConfirmEvent.Connected)
            PairCodeConfirmAction.BackClicked -> sendEvent(PairCodeConfirmEvent.NavigateBack)
        }
    }
}
