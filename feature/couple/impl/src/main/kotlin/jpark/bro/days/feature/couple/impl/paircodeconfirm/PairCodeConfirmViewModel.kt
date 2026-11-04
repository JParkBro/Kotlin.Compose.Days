package jpark.bro.days.feature.couple.impl.paircodeconfirm

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.couple.PairingException
import jpark.bro.days.core.data.repository.CoupleRepository
import jpark.bro.days.core.model.PairCodeCheck
import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.couple.impl.PairCodeConfirmKey
import jpark.bro.days.feature.couple.impl.R
import jpark.bro.days.feature.couple.impl.messageRes
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

internal class PairCodeConfirmViewModel(
    key: PairCodeConfirmKey,
    private val coupleRepository: CoupleRepository,
    clock: Clock,
) : MviViewModel<PairCodeConfirmState, PairCodeConfirmAction, PairCodeConfirmEvent>(
        LocalDate.now(clock).let { today -> PairCodeConfirmState(code = key.code, today = today, startDate = today) },
    ) {
    init {
        // 입력 화면에서 확인했지만 그사이 만료 · 사용될 수 있어 다시 확인한다
        viewModelScope.launch {
            coupleRepository
                .checkCode(key.code)
                .onSuccess { result ->
                    setState {
                        copy(
                            isLoading = false,
                            partnerName = (result as? PairCodeCheck.Valid)?.code?.creatorNickname.orEmpty(),
                            codeError = result.messageRes(),
                        )
                    }
                }.onFailure { setState { copy(isLoading = false, codeError = R.string.couple_pairing_check_failed) } }
        }
    }

    override fun onAction(action: PairCodeConfirmAction) {
        when (action) {
            PairCodeConfirmAction.StartDateClicked -> {
                setState { copy(isDatePickerOpen = true) }
            }

            is PairCodeConfirmAction.StartDateSelected -> {
                setState {
                    copy(
                        startDate = action.date,
                        isDatePickerOpen = false,
                    )
                }
            }

            PairCodeConfirmAction.DatePickerDismissed -> {
                setState { copy(isDatePickerOpen = false) }
            }

            is PairCodeConfirmAction.CountFirstDayChanged -> {
                setState { copy(countFirstDayAsOne = action.value) }
            }

            PairCodeConfirmAction.ConnectClicked -> {
                connect()
            }

            PairCodeConfirmAction.BackClicked -> {
                sendEvent(PairCodeConfirmEvent.NavigateBack)
            }
        }
    }

    private fun connect() {
        if (!currentState.canConnect) return
        setState { copy(isConnecting = true) }
        viewModelScope.launch {
            val state = currentState
            coupleRepository
                .connect(state.code, state.startDate, state.countFirstDayAsOne)
                .onSuccess { sendEvent(PairCodeConfirmEvent.Connected) }
                .onFailure { error ->
                    val reason = (error as? PairingException)?.reason?.messageRes()
                    setState { copy(isConnecting = false, codeError = reason) }
                    if (reason == null) sendEvent(PairCodeConfirmEvent.ConnectFailed)
                }
        }
    }
}
