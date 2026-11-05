package jpark.bro.days.feature.couple.impl.couplemanage

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.CoupleRepository
import jpark.bro.days.core.data.repository.ImageRepository
import jpark.bro.days.core.data.repository.ProfileRepository
import jpark.bro.days.core.data.repository.observeImageFileOrNull
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
internal class CoupleManageViewModel(
    private val coupleRepository: CoupleRepository,
    profileRepository: ProfileRepository,
    imageRepository: ImageRepository,
    clock: Clock,
) : MviViewModel<CoupleManageState, CoupleManageAction, CoupleManageEvent>(
        CoupleManageState(today = LocalDate.now(clock)),
    ) {
    init {
        combine(coupleRepository.observeCouple(), profileRepository.observePartnerProfile()) { couple, partner ->
            val wasConnected = currentState.couple != null
            setState { copy(isLoading = false, couple = couple, partner = partner) }
            if (wasConnected && couple == null) sendEvent(CoupleManageEvent.Disconnected)
        }.launchIn(viewModelScope)
        profileRepository
            .observePartnerProfile()
            .map { it?.profileImageId }
            .distinctUntilChanged()
            .flatMapLatest(imageRepository::observeImageFileOrNull)
            .onEach { file -> setState { copy(partnerPhoto = file) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: CoupleManageAction) {
        when (action) {
            CoupleManageAction.StartDateClicked -> {
                setState { copy(isDatePickerOpen = true) }
            }

            is CoupleManageAction.StartDateSelected -> {
                setState { copy(isDatePickerOpen = false) }
                update(startDate = action.date)
            }

            CoupleManageAction.DatePickerDismissed -> {
                setState { copy(isDatePickerOpen = false) }
            }

            is CoupleManageAction.CountFirstDayChanged -> {
                update(countFirstDayAsOne = action.value)
            }

            CoupleManageAction.AnniversariesClicked -> {
                sendEvent(CoupleManageEvent.OpenAnniversaries)
            }

            CoupleManageAction.UnpairClicked -> {
                setState { copy(isUnpairSheetOpen = true, deleteCopies = false) }
            }

            CoupleManageAction.UnpairDismissed -> {
                setState { copy(isUnpairSheetOpen = false) }
            }

            is CoupleManageAction.DeleteCopiesChanged -> {
                setState { copy(deleteCopies = action.value) }
            }

            CoupleManageAction.UnpairConfirmed -> {
                unpair()
            }

            CoupleManageAction.BackClicked -> {
                sendEvent(CoupleManageEvent.NavigateBack)
            }
        }
    }

    private fun update(
        startDate: LocalDate? = null,
        countFirstDayAsOne: Boolean? = null,
    ) {
        val couple = currentState.couple ?: return
        if (currentState.isWorking) return
        setState { copy(isWorking = true) }
        viewModelScope.launch {
            coupleRepository
                .updateCouple(startDate ?: couple.startDate, countFirstDayAsOne ?: couple.countFirstDayAsOne)
                .onFailure { sendEvent(CoupleManageEvent.UpdateFailed) }
            setState { copy(isWorking = false) }
        }
    }

    private fun unpair() {
        if (currentState.isWorking) return
        setState { copy(isWorking = true) }
        viewModelScope.launch {
            coupleRepository
                .disconnect(currentState.deleteCopies)
                .onSuccess { setState { copy(isUnpairSheetOpen = false) } }
                .onFailure { sendEvent(CoupleManageEvent.UnpairFailed) }
            setState { copy(isWorking = false) }
        }
    }
}
