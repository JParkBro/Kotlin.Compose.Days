package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.ProfileRepository
import jpark.bro.days.core.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

class FakeProfileRepository(
    me: UserProfile? = null,
    partner: UserProfile? = null,
) : ProfileRepository {
    val myProfile = MutableStateFlow(me)
    val partnerProfile = MutableStateFlow(partner)

    /** true면 다음 저장이 실패한다 */
    var failNext: Boolean = false

    override fun observeMyProfile(): Flow<UserProfile?> = myProfile

    override fun observePartnerProfile(): Flow<UserProfile?> = partnerProfile

    override suspend fun saveMyProfile(
        nickname: String,
        birthDate: LocalDate?,
        profileImageId: String?,
    ): Result<UserProfile> {
        if (failNext) {
            failNext = false
            return Result.failure(IllegalStateException("저장 실패 (테스트)"))
        }
        val saved =
            UserProfile(
                userId = "user-me",
                nickname = nickname.trim(),
                birthDate = birthDate,
                profileImageId = profileImageId,
            )
        myProfile.value = saved
        return Result.success(saved)
    }
}
