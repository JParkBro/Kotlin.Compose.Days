package jpark.bro.days.data.repository

import jpark.bro.days.data.local.CoupleDateDataStore
import jpark.bro.days.domain.repository.CoupleDateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CoupleDateRepositoryImpl
    @Inject
    constructor(
        private val coupleDateDataStore: CoupleDateDataStore,
    ) : CoupleDateRepository {
        override suspend fun saveCoupleDate(date: Long) {
            coupleDateDataStore.saveCoupleDate(date = date)
        }

        override fun getCoupleDate(): Flow<Long?> = coupleDateDataStore.coupleDate
    }
