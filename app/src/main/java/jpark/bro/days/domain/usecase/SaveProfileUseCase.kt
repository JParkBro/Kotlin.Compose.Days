package jpark.bro.days.domain.usecase

import jpark.bro.days.domain.model.ProfileType
import jpark.bro.days.domain.repository.ProfileRepository
import javax.inject.Inject

class SaveProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(type: ProfileType, name: String, birthday: Long) {
        profileRepository.saveProfile(type, name, birthday)
    }
}