package jpark.bro.days.feature.couple.impl.anniversaries

internal sealed interface AnniversaryListEvent {
    data object SaveFailed : AnniversaryListEvent

    /** 뒤로 가기, 또는 연결이 끊겨 볼 기념일이 없음 */
    data object NavigateBack : AnniversaryListEvent
}
