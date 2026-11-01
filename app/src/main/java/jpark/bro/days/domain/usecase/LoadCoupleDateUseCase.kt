package jpark.bro.days.domain.usecase

import jpark.bro.days.domain.repository.CoupleDateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadCoupleDateUseCase
    @Inject
    constructor(
        private val repository: CoupleDateRepository,
    ) {
        operator fun invoke(): Flow<Long?> = repository.getCoupleDate()
    }
