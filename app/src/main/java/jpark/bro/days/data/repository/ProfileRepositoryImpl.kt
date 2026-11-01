package jpark.bro.days.data.repository

import androidx.datastore.preferences.core.Preferences
import jpark.bro.days.data.local.ProfileDataStore
import jpark.bro.days.domain.model.ProfileType
import jpark.bro.days.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ProfileRepositoryImpl
    @Inject
    constructor(
        private val profileDataStore: ProfileDataStore,
    ) : ProfileRepository {
        private fun getKeys(type: ProfileType): Pair<Preferences.Key<String>, Preferences.Key<Long>> =
            when (type) {
                ProfileType.A -> ProfileDataStore.A_NAME_KEY to ProfileDataStore.A_BIRTHDAY_KEY
                ProfileType.B -> ProfileDataStore.B_NAME_KEY to ProfileDataStore.B_BIRTHDAY_KEY
            }

        override suspend fun saveProfile(
            type: ProfileType,
            name: String,
            birthday: Long,
        ) {
            val keys = getKeys(type)
            profileDataStore.saveName(keys.first, name)
            profileDataStore.saveBirthday(keys.second, birthday)
        }

        override fun getProfile(type: ProfileType): Flow<Pair<String?, Long?>> {
            val keys = getKeys(type)
            return combine(
                profileDataStore.getName(keys.first),
                profileDataStore.getBirthday(keys.second),
            ) { name, birthday -> name to birthday }
        }
    }
