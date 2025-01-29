package jpark.bro.days.domain.usecase

import jpark.bro.days.domain.repository.CoupleDateRepository
import javax.inject.Inject

class SaveCoupleDateUseCase @Inject constructor(
    private val repository: CoupleDateRepository
) {
    suspend operator fun invoke(dateMillis: Long) {
        repository.saveCoupleDate(dateMillis)
    }
}