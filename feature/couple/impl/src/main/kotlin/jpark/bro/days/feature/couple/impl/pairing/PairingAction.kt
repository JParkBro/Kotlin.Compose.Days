package jpark.bro.days.feature.couple.impl.pairing

import jpark.bro.days.feature.couple.api.PairingKey
import jpark.bro.days.feature.couple.impl.PairCodeConfirmKey

internal sealed interface PairingAction {
    data class CodeSubmitted(
        val code: String,
    ) : PairingAction

    data object BackClicked : PairingAction
}
