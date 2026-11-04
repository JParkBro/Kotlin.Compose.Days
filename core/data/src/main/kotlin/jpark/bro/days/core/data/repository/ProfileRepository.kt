package jpark.bro.days.core.data.repository

import jpark.bro.days.core.model.UserProfile
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** 프로필 (product-spec 6장 프로필 수정). 저장은 기기에 먼저 하고 동기화가 서버에 올린다 */
interface ProfileRepository {
    /** 아직 설정하지 않았거나 로그아웃 상태면 null */
    fun observeMyProfile(): Flow<UserProfile?>

    /** 연결된 상대의 프로필. 솔로이거나 아직 받지 못했으면 null */
    fun observePartnerProfile(): Flow<UserProfile?>

    /** [nickname]은 앞뒤 공백을 지우고 1~[UserProfile.NICKNAME_MAX_LENGTH]자, [birthDate]는 오늘 이전 */
    suspend fun saveMyProfile(
        nickname: String,
        birthDate: LocalDate?,
    ): Result<UserProfile>
}
