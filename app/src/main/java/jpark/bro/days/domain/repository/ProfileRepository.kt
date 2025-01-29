package jpark.bro.days.domain.repository

import jpark.bro.days.domain.model.ProfileType
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun saveProfile(type: ProfileType, name: String, birthday: Long)
    fun getProfile(type: ProfileType): Flow<Pair<String?, Long?>>
}