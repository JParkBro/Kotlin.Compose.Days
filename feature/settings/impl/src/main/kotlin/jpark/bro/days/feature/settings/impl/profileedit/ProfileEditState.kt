package jpark.bro.days.feature.settings.impl.profileedit

import jpark.bro.days.core.model.UserProfile
import java.io.File
import java.time.LocalDate

internal data class ProfileEditState(
    val isLoading: Boolean = true,
    val nickname: String = "",
    val birthDate: LocalDate? = null,
    /** 저장할 프로필 사진 id와 그 파일 (없으면 이름 첫 글자) */
    val photoId: String? = null,
    val photoFile: File? = null,
    /** 고른 사진을 줄이는 중 */
    val isPhotoWorking: Boolean = false,
    /** 저장된 값. 바뀐 것이 없으면 저장 버튼을 끈다 */
    val saved: UserProfile? = null,
    val today: LocalDate = LocalDate.MIN,
    val isDatePickerOpen: Boolean = false,
    val isSaving: Boolean = false,
) {
    private val trimmed: String get() = nickname.trim()

    val isChanged: Boolean
        get() = trimmed != saved?.nickname || birthDate != saved?.birthDate || photoId != saved?.profileImageId

    val canSave: Boolean
        get() = !isLoading && !isSaving && !isPhotoWorking && trimmed.isNotEmpty() && isChanged
}
