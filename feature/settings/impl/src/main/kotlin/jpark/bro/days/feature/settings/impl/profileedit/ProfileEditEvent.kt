package jpark.bro.days.feature.settings.impl.profileedit

internal sealed interface ProfileEditEvent {
    /** 저장하고 이전 화면으로 */
    data object Saved : ProfileEditEvent

    data object SaveFailed : ProfileEditEvent

    data object NavigateBack : ProfileEditEvent
}
