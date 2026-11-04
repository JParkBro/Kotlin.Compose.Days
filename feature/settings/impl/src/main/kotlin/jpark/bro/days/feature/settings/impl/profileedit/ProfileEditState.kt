package jpark.bro.days.feature.settings.impl.profileedit

import jpark.bro.days.core.model.UserProfile
import java.time.LocalDate

internal data class ProfileEditState(
    val isLoading: Boolean = true,
    val nickname: String = "",
    val birthDate: LocalDate? = null,
    /** 저장된 값. 바뀐 것이 없으면 저장 버튼을 끈다 */
    val saved: UserProfile? = null,
    val today: LocalDate = LocalDate.MIN,
    val isDatePickerOpen: Boolean = false,
    val isSaving: Boolean = false,
) {
    private val trimmed: String get() = nickname.trim()

    val isChanged: Boolean get() = trimmed != saved?.nickname || birthDate != saved?.birthDate

    val canSave: Boolean
        get() = !isLoading && !isSaving && trimmed.isNotEmpty() && isChanged
}
