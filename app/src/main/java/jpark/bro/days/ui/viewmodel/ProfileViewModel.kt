package jpark.bro.days.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jpark.bro.days.core.util.DateUtil
import jpark.bro.days.core.util.DateUtil.toEpochMilli
import jpark.bro.days.core.util.ResourceProvider
import jpark.bro.days.domain.model.ProfileType
import jpark.bro.days.domain.usecase.LoadProfileUseCase
import jpark.bro.days.domain.usecase.SaveProfileUseCase
import jpark.bro.days.ui.state.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import javax.inject.Inject
import jpark.bro.days.R

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val saveProfileUseCase: SaveProfileUseCase,
    private val loadProfileUseCase: LoadProfileUseCase,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {
    private val defaultBirthday: LocalDate = DateUtil.getCurrentLocalDate().minus(DatePeriod(years = 19))

    private val _profileA = MutableStateFlow(ProfileUiState())
    val profileA: StateFlow<ProfileUiState> = _profileA

    private val _profileB = MutableStateFlow(ProfileUiState())
    val profileB: StateFlow<ProfileUiState> = _profileB

    init {
        loadProfile(ProfileType.A)
        loadProfile(ProfileType.B)
    }

    private fun loadProfile(type: ProfileType) {
        viewModelScope.launch {
            loadProfileUseCase(type).collect { profile ->
                val birthday: LocalDate = profile.second?.let { DateUtil.formatDateToLocalDate(it) }
                    ?: if (type == ProfileType.A) defaultBirthday.minus(DatePeriod(years = 1)) else defaultBirthday

                val koreanAge = DateUtil.calculateKoreanAge(birthday, DateUtil.getCurrentLocalDate())

                val profileData = ProfileUiState(
                    name = profile.first ?: resourceProvider.asString(R.string.nick_name),
                    birthday = birthday,
                    ageText = "${koreanAge}살 (${birthday.monthNumber}월 ${birthday.dayOfMonth}일)"
                )

                updateProfileData(type, profileData)
            }
        }
    }

    fun saveProfile(type: ProfileType, name: String, birthday: LocalDate) {
        viewModelScope.launch {
            saveProfileUseCase(type, name, birthday.toEpochMilli())
            loadProfile(type)
        }
    }

    private fun updateProfileData(type: ProfileType, profileData: ProfileUiState) {
        when (type) {
            ProfileType.A -> _profileA.value = (profileData)
            ProfileType.B -> _profileB.value = (profileData)
        }
    }
}