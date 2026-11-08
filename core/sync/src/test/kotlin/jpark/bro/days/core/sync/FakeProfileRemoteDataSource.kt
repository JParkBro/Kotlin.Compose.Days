package jpark.bro.days.core.sync

import jpark.bro.days.core.firebase.sync.ProfileDto
import jpark.bro.days.core.firebase.sync.ProfileRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter

internal class FakeProfileRemoteDataSource : ProfileRemoteDataSource {
    val profiles = mutableMapOf<String, ProfileDto>()
    val pushed = mutableListOf<ProfileDto>()
    val fetched = mutableListOf<String>()
    val changes = MutableSharedFlow<ProfileDto>()

    /** 리스너를 붙인 uid */
    val observed = mutableListOf<String>()

    var failPush = false

    override suspend fun pushProfile(profile: ProfileDto) {
        check(!failPush) { "push 실패 (테스트)" }
        pushed += profile
    }

    override suspend fun getProfile(uid: String): ProfileDto? {
        fetched += uid
        return profiles[uid]
    }

    override fun observeProfile(uid: String): Flow<ProfileDto> {
        observed += uid
        return changes.filter { it.uid == uid }
    }

    override suspend fun deleteProfile(uid: String) {
        profiles.remove(uid)
    }
}
