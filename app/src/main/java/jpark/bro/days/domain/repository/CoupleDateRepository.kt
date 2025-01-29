package jpark.bro.days.domain.repository

import kotlinx.coroutines.flow.Flow

interface CoupleDateRepository {
    suspend fun saveCoupleDate(date: Long)
    fun getCoupleDate(): Flow<Long?>
}