package jpark.bro.days.feature.settings.impl.profileedit

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.ProfileRepository
import jpark.bro.days.core.model.UserProfile
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

internal class ProfileEditViewModel(
    private val profileRepository: ProfileRepository,
    clock: Clock,
) : MviViewModel<ProfileEditState, ProfileEditAction, ProfileEditEvent>(
        ProfileEditState(today = LocalDate.now(clock)),
    ) {
    init {
        // 입력 중인 값이 동기화로 덮이지 않도록 처음 한 번만 채운다
        viewModelScope.launch {
            val saved = profileRepository.observeMyProfile().first()
            setState {
                copy(
                    isLoading = false,
                    saved = saved,
                    nickname = saved?.nickname.orEmpty(),
                    birthDate = saved?.birthDate,
                )
            }
        }
    }

    override fun onAction(action: ProfileEditAction) {
        when (action) {
            is ProfileEditAction.NicknameChanged -> {
                setState { copy(nickname = action.value.take(UserProfile.NICKNAME_MAX_LENGTH)) }
            }

            ProfileEditAction.BirthDateClicked -> {
                setState { copy(isDatePickerOpen = true) }
            }

            is ProfileEditAction.BirthDateSelected -> {
                setState { copy(birthDate = action.date, isDatePickerOpen = false) }
            }

            ProfileEditAction.BirthDateDismissed -> {
                setState { copy(isDatePickerOpen = false) }
            }

            ProfileEditAction.BirthDateCleared -> {
                setState { copy(birthDate = null) }
            }

            ProfileEditAction.SaveClicked -> {
                save()
            }

            ProfileEditAction.BackClicked -> {
                sendEvent(ProfileEditEvent.NavigateBack)
            }
        }
    }

    private fun save() {
        if (!currentState.canSave) return
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            profileRepository
                .saveMyProfile(currentState.nickname, currentState.birthDate)
                .onSuccess { sendEvent(ProfileEditEvent.Saved) }
                .onFailure {
                    setState { copy(isSaving = false) }
                    sendEvent(ProfileEditEvent.SaveFailed)
                }
        }
    }
}
