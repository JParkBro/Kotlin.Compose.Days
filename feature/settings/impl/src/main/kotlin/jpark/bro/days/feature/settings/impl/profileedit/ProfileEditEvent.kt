package jpark.bro.days.feature.settings.impl.profileedit

internal sealed interface ProfileEditEvent {
    /** 저장하고 이전 화면으로 */
    data object Saved : ProfileEditEvent

    data object SaveFailed : ProfileEditEvent

    data object NavigateBack : ProfileEditEvent

    data object OpenPhotoPicker : ProfileEditEvent

    /** 고른 사진을 읽거나 줄이지 못함 */
    data object PhotoFailed : ProfileEditEvent
}
