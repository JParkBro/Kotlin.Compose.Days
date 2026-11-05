package jpark.bro.days.feature.settings.impl.profileedit

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.image.ImageDraft
import jpark.bro.days.core.data.repository.ImageRepository
import jpark.bro.days.core.data.repository.ProfileRepository
import jpark.bro.days.core.data.repository.observeImageFileOrNull
import jpark.bro.days.core.model.UserProfile
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
internal class ProfileEditViewModel(
    private val profileRepository: ProfileRepository,
    private val imageRepository: ImageRepository,
    clock: Clock,
) : MviViewModel<ProfileEditState, ProfileEditAction, ProfileEditEvent>(
        ProfileEditState(today = LocalDate.now(clock)),
    ) {
    private val photo = ImageDraft(imageRepository, original = null)
    private val photoId = MutableStateFlow<String?>(null)

    init {
        // 입력 중인 값이 동기화로 덮이지 않도록 처음 한 번만 채운다
        viewModelScope.launch {
            val saved = profileRepository.observeMyProfile().first()
            photo.reset(saved?.profileImageId)
            photoId.value = photo.current
            setState {
                copy(
                    isLoading = false,
                    saved = saved,
                    nickname = saved?.nickname.orEmpty(),
                    birthDate = saved?.birthDate,
                    photoId = photo.current,
                )
            }
        }
        photoId
            .flatMapLatest(imageRepository::observeImageFileOrNull)
            .onEach { file -> setState { copy(photoFile = file) } }
            .launchIn(viewModelScope)
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

            ProfileEditAction.PhotoClicked -> {
                sendEvent(ProfileEditEvent.OpenPhotoPicker)
            }

            is ProfileEditAction.PhotoPicked -> {
                editPhoto { replace(action.uri).onFailure { sendEvent(ProfileEditEvent.PhotoFailed) } }
            }

            ProfileEditAction.PhotoRemoved -> {
                editPhoto { remove() }
            }

            ProfileEditAction.SaveClicked -> {
                save()
            }

            ProfileEditAction.BackClicked -> {
                viewModelScope.launch {
                    photo.abandon()
                    sendEvent(ProfileEditEvent.NavigateBack)
                }
            }
        }
    }

    private fun editPhoto(block: suspend ImageDraft.() -> Unit) {
        if (currentState.isPhotoWorking) return
        setState { copy(isPhotoWorking = true) }
        viewModelScope.launch {
            photo.block()
            photoId.value = photo.current
            setState { copy(photoId = photo.current, isPhotoWorking = false) }
        }
    }

    private fun save() {
        if (!currentState.canSave) return
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            profileRepository
                .saveMyProfile(currentState.nickname, currentState.birthDate, photo.current)
                .onSuccess {
                    photo.commit()
                    sendEvent(ProfileEditEvent.Saved)
                }.onFailure {
                    setState { copy(isSaving = false) }
                    sendEvent(ProfileEditEvent.SaveFailed)
                }
        }
    }
}
