package jpark.bro.days.domain.usecase

import jpark.bro.days.core.util.DateUtil
import jpark.bro.days.domain.helper.DDayHelper
import jpark.bro.days.domain.model.ProfileData
import jpark.bro.days.domain.model.ProfileType
import jpark.bro.days.domain.repository.CoupleDateRepository
import jpark.bro.days.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FetchAllDDayDataUseCase
    @Inject
    constructor(
        private val coupleDateRepository: CoupleDateRepository,
        private val profileRepository: ProfileRepository,
        private val dDayHelper: DDayHelper,
    ) {
        operator fun invoke(): Flow<List<Triple<String, String, String>>> =
            flow {
                combine(
                    coupleDateRepository.getCoupleDate().distinctUntilChanged(),
                    profileRepository.getProfile(ProfileType.A).distinctUntilChanged(),
                    profileRepository.getProfile(ProfileType.B).distinctUntilChanged(),
                ) { coupleDate, profileA, profileB ->
                    var startDate = coupleDate?.let { DateUtil.formatDateToLocalDate(it) }
                    val profileAData =
                        profileA.second?.let {
                            ProfileData(nickname = profileA.first, birthday = DateUtil.formatDateToLocalDate(it))
                        }
                    val profileBData =
                        profileB.second?.let {
                            ProfileData(nickname = profileB.first, birthday = DateUtil.formatDateToLocalDate(it))
                        }

                    dDayHelper.generateDDayDates(startDate, profileAData, profileBData)
                }.collect { generatedDates ->
                    generatedDates?.let {
                        emit(generatedDates)
                    }
                }
            }
    }
