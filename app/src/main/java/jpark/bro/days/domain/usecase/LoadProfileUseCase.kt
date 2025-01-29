package jpark.bro.days.domain.usecase

import jpark.bro.days.domain.model.ProfileType
import jpark.bro.days.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(type: ProfileType): Flow<Pair<String?, Long?>> {
        return profileRepository.getProfile(type)
    }
}