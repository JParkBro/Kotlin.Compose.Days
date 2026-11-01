package jpark.bro.days.feature.couple.impl.pairing

import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.couple.api.PairingKey
import jpark.bro.days.feature.couple.impl.PairCodeConfirmKey

internal class PairingViewModel : MviViewModel<PairingState, PairingAction, PairingEvent>(PairingState()) {
    override fun onAction(action: PairingAction) {
        when (action) {
            is PairingAction.CodeSubmitted -> sendEvent(PairingEvent.NavigateToConfirm(action.code))
            PairingAction.BackClicked -> sendEvent(PairingEvent.NavigateBack)
        }
    }
}
