package jpark.bro.days.feature.couple.impl.pairing

internal sealed interface PairingEvent {
    data class NavigateToConfirm(
        val code: String,
    ) : PairingEvent

    data object NavigateBack : PairingEvent
}
