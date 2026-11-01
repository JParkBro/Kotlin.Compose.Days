package jpark.bro.days.feature.couple.impl.pairing

import jpark.bro.days.feature.couple.api.PairingKey
import jpark.bro.days.feature.couple.impl.PairCodeConfirmKey

internal sealed interface PairingEvent {
    data class NavigateToConfirm(
        val code: String,
    ) : PairingEvent

    data object NavigateBack : PairingEvent
}
