package jpark.bro.days.feature.settings.impl.profileedit

import java.time.LocalDate

internal sealed interface ProfileEditAction {
    data class NicknameChanged(
        val value: String,
    ) : ProfileEditAction

    data object BirthDateClicked : ProfileEditAction

    data class BirthDateSelected(
        val date: LocalDate,
    ) : ProfileEditAction

    data object BirthDateDismissed : ProfileEditAction

    /** 생일 지우기 (생일 기념일을 만들지 않음) */
    data object BirthDateCleared : ProfileEditAction

    /** 사진 선택기를 연다 */
    data object PhotoClicked : ProfileEditAction

    data class PhotoPicked(
        val uri: String,
    ) : ProfileEditAction

    data object PhotoRemoved : ProfileEditAction

    data object SaveClicked : ProfileEditAction

    data object BackClicked : ProfileEditAction
}
