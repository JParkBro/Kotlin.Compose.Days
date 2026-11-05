package jpark.bro.days.feature.couple.impl.couplemanage

internal sealed interface CoupleManageEvent {
    data object UpdateFailed : CoupleManageEvent

    data object UnpairFailed : CoupleManageEvent

    /** 내가 해제했거나 상대가 해제해 연결이 끝남 */
    data object Disconnected : CoupleManageEvent

    data object OpenAnniversaries : CoupleManageEvent

    data object NavigateBack : CoupleManageEvent
}
