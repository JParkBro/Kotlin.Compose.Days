package jpark.bro.days.feature.couple.impl.pairing

internal sealed interface PairingEvent {
    data class CopyCode(
        val code: String,
    ) : PairingEvent

    data object OpenProfileEdit : PairingEvent

    data class NavigateToConfirm(
        val code: String,
    ) : PairingEvent

    data object CheckFailed : PairingEvent

    /** 상대가 내 코드로 연결함 (또는 이미 연결됨) */
    data object Connected : PairingEvent

    data object NavigateBack : PairingEvent
}
