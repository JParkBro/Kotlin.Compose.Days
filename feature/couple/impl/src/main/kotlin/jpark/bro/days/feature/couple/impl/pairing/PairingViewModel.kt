package jpark.bro.days.feature.couple.impl.pairing

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.couple.ProfileRequiredException
import jpark.bro.days.core.data.repository.CoupleRepository
import jpark.bro.days.core.model.PairCodeCheck
import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.couple.impl.messageRes
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal class PairingViewModel(
    private val coupleRepository: CoupleRepository,
) : MviViewModel<PairingState, PairingAction, PairingEvent>(PairingState()) {
    init {
        issue()
        // 상대가 내 코드로 연결하면 동기화가 커플을 받아 온다
        viewModelScope.launch {
            coupleRepository.observeCouple().filterNotNull().first()
            sendEvent(PairingEvent.Connected)
        }
    }

    override fun onAction(action: PairingAction) {
        when (action) {
            is PairingAction.TabSelected -> {
                setState { copy(tab = action.tab) }
            }

            PairingAction.IssueClicked -> {
                issue()
            }

            PairingAction.CopyClicked -> {
                currentState.myCode?.let { sendEvent(PairingEvent.CopyCode(it.code)) }
            }

            PairingAction.SetProfileClicked -> {
                sendEvent(PairingEvent.OpenProfileEdit)
            }

            is PairingAction.CellClicked -> {
                setState {
                    copy(
                        activeCell = action.index.coerceIn(0, PAIR_CODE_LENGTH - 1),
                    )
                }
            }

            is PairingAction.CharsTyped -> {
                type(action.text)
            }

            PairingAction.DeletePressed -> {
                delete()
            }

            PairingAction.ConnectClicked -> {
                check()
            }

            PairingAction.BackClicked -> {
                sendEvent(PairingEvent.NavigateBack)
            }
        }
    }

    private fun issue() {
        setState { copy(isIssuing = true, issueError = null) }
        viewModelScope.launch {
            coupleRepository
                .issueCode()
                .onSuccess { code -> setState { copy(isIssuing = false, myCode = code) } }
                .onFailure { error ->
                    val reason =
                        if (error is ProfileRequiredException) IssueError.PROFILE_REQUIRED else IssueError.FAILED
                    setState { copy(isIssuing = false, issueError = reason) }
                }
        }
    }

    /** 선택된 칸부터 덮어쓰고 다음 칸으로. 마지막 칸에서는 그 자리에 머문다 */
    private fun type(text: String) {
        val chars = text.uppercase().filter { it in PAIR_CODE_CHARS }
        if (chars.isEmpty()) return
        setState {
            val cells = cells.toMutableList()
            var index = activeCell
            chars.forEach { char ->
                if (index < PAIR_CODE_LENGTH) cells[index++] = char
            }
            copy(cells = cells, activeCell = index.coerceAtMost(PAIR_CODE_LENGTH - 1), inputError = null)
        }
    }

    private fun delete() {
        setState {
            val cells = cells.toMutableList()
            val index = if (cells[activeCell] == null && activeCell > 0) activeCell - 1 else activeCell
            cells[index] = null
            copy(cells = cells, activeCell = index, inputError = null)
        }
    }

    private fun check() {
        val code = currentState.input ?: return
        if (currentState.isChecking) return
        setState { copy(isChecking = true, inputError = null) }
        viewModelScope.launch {
            coupleRepository
                .checkCode(code)
                .onSuccess { result ->
                    setState { copy(isChecking = false, inputError = result.messageRes()) }
                    if (result is PairCodeCheck.Valid) sendEvent(PairingEvent.NavigateToConfirm(code))
                }.onFailure {
                    setState { copy(isChecking = false) }
                    sendEvent(PairingEvent.CheckFailed)
                }
        }
    }
}
