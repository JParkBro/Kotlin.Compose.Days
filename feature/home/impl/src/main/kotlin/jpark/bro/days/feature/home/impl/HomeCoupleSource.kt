package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.data.repository.AnniversaryRepository
import jpark.bro.days.core.data.repository.CoupleRepository
import jpark.bro.days.core.data.repository.ImageRepository
import jpark.bro.days.core.data.repository.ProfileRepository
import jpark.bro.days.core.data.repository.observeImageFileOrNull
import jpark.bro.days.core.model.Couple
import jpark.bro.days.core.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.io.File

/** 커플 히어로 · 다음 기념일에 필요한 것: 연결된 커플, 두 사람 프로필 · 사진, 숨긴 기념일 */
internal data class CoupleSource(
    val couple: Couple?,
    val me: UserProfile?,
    val partner: UserProfile?,
    val hidden: Set<String>,
    val myPhoto: File? = null,
    val partnerPhoto: File? = null,
)

/** 홈이 커플 정보를 한 흐름으로 받도록 묶는다 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class HomeCoupleSource(
    private val coupleRepository: CoupleRepository,
    private val profileRepository: ProfileRepository,
    private val anniversaryRepository: AnniversaryRepository,
    private val imageRepository: ImageRepository,
) {
    fun observe(): Flow<CoupleSource> =
        combine(
            coupleRepository.observeCouple(),
            profileRepository.observeMyProfile(),
            profileRepository.observePartnerProfile(),
            anniversaryRepository.observeHiddenRuleKeys(),
            photos(),
        ) { couple, me, partner, hidden, (myPhoto, partnerPhoto) ->
            CoupleSource(couple, me, partner, hidden, myPhoto, partnerPhoto)
        }

    private fun photos(): Flow<Pair<File?, File?>> =
        combine(
            photoOf(profileRepository.observeMyProfile()),
            photoOf(profileRepository.observePartnerProfile()),
        ) { mine, partner -> mine to partner }

    private fun photoOf(profile: Flow<UserProfile?>): Flow<File?> =
        profile
            .map { it?.profileImageId }
            .distinctUntilChanged()
            .flatMapLatest(imageRepository::observeImageFileOrNull)
}
