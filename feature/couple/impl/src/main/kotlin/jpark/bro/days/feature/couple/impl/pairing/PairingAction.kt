package jpark.bro.days.feature.couple.impl.pairing

internal sealed interface PairingAction {
    data class CodeSubmitted(
        val code: String,
    ) : PairingAction

    data object BackClicked : PairingAction
}
