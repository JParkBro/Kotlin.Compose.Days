package jpark.bro.days.feature.couple.impl.anniversaries

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.AnniversaryRepository
import jpark.bro.days.core.data.repository.CoupleRepository
import jpark.bro.days.core.data.repository.ProfileRepository
import jpark.bro.days.core.domain.anniversary.autoAnniversaries
import jpark.bro.days.core.domain.dday.calculateDday
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.UserBirthday
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

/** 자동 기념일 목록과 숨김 (product-spec 2-3). 숨김은 커플 단위라 상대에게도 숨겨진다 */
internal class AnniversaryListViewModel(
    coupleRepository: CoupleRepository,
    profileRepository: ProfileRepository,
    private val anniversaryRepository: AnniversaryRepository,
    private val clock: Clock,
) : MviViewModel<AnniversaryListState, AnniversaryListAction, AnniversaryListEvent>(AnniversaryListState()) {
    init {
        combine(
            coupleRepository.observeCouple(),
            profileRepository.observeMyProfile(),
            profileRepository.observePartnerProfile(),
            anniversaryRepository.observeHiddenRuleKeys(),
        ) { couple, me, partner, hidden ->
            if (couple == null) {
                // 보는 중에 연결이 끊기면 숨길 대상이 없다
                if (!currentState.isLoading) sendEvent(AnniversaryListEvent.NavigateBack)
                setState { copy(isLoading = false, rows = emptyList()) }
                return@combine
            }
            val today = LocalDate.now(clock)
            val profiles = listOfNotNull(me, partner)
            val rows =
                autoAnniversaries(
                    startDate = couple.startDate,
                    countFirstDayAsOne = couple.countFirstDayAsOne,
                    birthdays = profiles.mapNotNull { p -> p.birthDate?.let { UserBirthday(p.userId, it) } },
                    period = today..today.plusYears(1),
                ).map {
                    AnniversaryRow(
                        anniversary = it,
                        dday = calculateDday(it.date, CountMode.DDAY, false, RepeatRule.NONE, today),
                        isHidden = it.ruleKey in hidden,
                    )
                }
            setState {
                copy(isLoading = false, rows = rows, nicknames = profiles.associate { it.userId to it.nickname })
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: AnniversaryListAction) {
        when (action) {
            is AnniversaryListAction.VisibilityChanged -> {
                viewModelScope.launch {
                    anniversaryRepository
                        .setHidden(action.ruleKey, hidden = !action.visible)
                        .onFailure { sendEvent(AnniversaryListEvent.SaveFailed) }
                }
            }

            AnniversaryListAction.BackClicked -> {
                sendEvent(AnniversaryListEvent.NavigateBack)
            }
        }
    }
}
