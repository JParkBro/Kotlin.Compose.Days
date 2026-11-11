package jpark.bro.days.feature.settings.impl.backup

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.ads.RewardedAdResult
import jpark.bro.days.core.data.repository.BACKUP_INTERVAL_MILLIS
import jpark.bro.days.core.data.repository.BackupNotFoundException
import jpark.bro.days.core.data.repository.BackupRepository
import jpark.bro.days.core.data.repository.BackupTooSoonException
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime

/**
 * 백업은 보상형 광고를 끝까지 본 뒤 한다. 광고를 불러오지 못하면 사용자가 손해 보지 않게 그냥 백업한다 (ads-strategy.md 3장).
 * 복원에는 광고가 없다.
 */
internal class BackupViewModel(
    private val backupRepository: BackupRepository,
    private val clock: Clock,
) : MviViewModel<BackupState, BackupAction, BackupEvent>(BackupState()) {
    /** 마지막 백업 시각(epoch millis). 10분 안이면 광고를 띄우기 전에 알린다 */
    private var lastBackupMillis: Long? = null

    init {
        viewModelScope.launch {
            val last = backupRepository.lastBackupAt().getOrNull()
            lastBackupMillis = last
            setState { copy(isLoading = false, lastBackupAt = last?.toLocalDateTime()) }
        }
    }

    override fun onAction(action: BackupAction) {
        when (action) {
            BackupAction.BackupClicked -> {
                startBackup()
            }

            is BackupAction.RewardedAdFinished -> {
                onAdFinished(action.result)
            }

            BackupAction.RestoreClicked -> {
                if (!currentState.isWorking) {
                    setState {
                        copy(
                            isRestoreConfirmVisible = true,
                        )
                    }
                }
            }

            BackupAction.RestoreConfirmed -> {
                restore()
            }

            BackupAction.RestoreDismissed -> {
                setState { copy(isRestoreConfirmVisible = false) }
            }

            BackupAction.BackClicked -> {
                sendEvent(BackupEvent.NavigateBack)
            }
        }
    }

    private fun startBackup() {
        if (currentState.isWorking) return
        val last = lastBackupMillis
        if (last != null && clock.millis() - last < BACKUP_INTERVAL_MILLIS) {
            sendEvent(BackupEvent.BackupTooSoon)
            return
        }
        setState { copy(work = BackupWork.BACKUP) }
        sendEvent(BackupEvent.ShowRewardedAd)
    }

    private fun onAdFinished(result: RewardedAdResult) {
        when (result) {
            RewardedAdResult.Earned, RewardedAdResult.Failed -> backup()
            RewardedAdResult.Dismissed -> setState { copy(work = null) }
        }
    }

    private fun backup() {
        viewModelScope.launch {
            backupRepository
                .backup()
                .onSuccess {
                    val now = clock.millis()
                    lastBackupMillis = now
                    setState { copy(work = null, lastBackupAt = now.toLocalDateTime()) }
                    sendEvent(BackupEvent.BackedUp)
                }.onFailure { error ->
                    setState { copy(work = null) }
                    sendEvent(
                        if (error is BackupTooSoonException) BackupEvent.BackupTooSoon else BackupEvent.BackupFailed,
                    )
                }
        }
    }

    private fun restore() {
        if (currentState.isWorking) return
        setState { copy(work = BackupWork.RESTORE, isRestoreConfirmVisible = false) }
        viewModelScope.launch {
            backupRepository
                .restore()
                .onSuccess { restored ->
                    setState { copy(work = null) }
                    sendEvent(BackupEvent.Restored(restored.itemCount))
                }.onFailure { error ->
                    setState { copy(work = null) }
                    sendEvent(if (error is BackupNotFoundException) BackupEvent.NoBackup else BackupEvent.RestoreFailed)
                }
        }
    }

    private fun Long.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), clock.zone)
}
