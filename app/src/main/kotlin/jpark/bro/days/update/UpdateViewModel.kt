package jpark.bro.days.update

import androidx.lifecycle.viewModelScope
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.requestAppUpdateInfo
import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

/**
 * Play 인앱 업데이트(FLEXIBLE). 업데이트 · 재시작 모두 먼저 묻는다 (docs/days/in-app-update.md).
 * 확인 실패(Play에서 설치하지 않은 빌드 등)는 조용히 넘긴다. 업데이트 확인이 앱 사용을 막으면 안 된다.
 * "업데이트 안내"를 미루면 그날은 다시 묻지 않고, "설치 준비 완료"는 앱으로 돌아올 때마다 다시 묻는다.
 */
internal class UpdateViewModel(
    private val appUpdateManager: AppUpdateManager,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<UpdateState, UpdateAction, UpdateEvent>(UpdateState()) {
    // Activity가 아니라 ViewModel 생명주기에 건다. 회전해도 유지되고 onCleared에서 해제된다
    private val installStateListener =
        InstallStateUpdatedListener { installState ->
            if (installState.installStatus() == InstallStatus.DOWNLOADED) showPrompt(UpdatePrompt.ReadyToInstall)
        }

    init {
        appUpdateManager.registerListener(installStateListener)
    }

    override fun onCleared() {
        appUpdateManager.unregisterListener(installStateListener)
        super.onCleared()
    }

    override fun onAction(action: UpdateAction) {
        when (action) {
            UpdateAction.AppStarted -> {
                check(offerUpdate = true)
            }

            UpdateAction.AppResumed -> {
                check(offerUpdate = false)
            }

            UpdateAction.UpdateConfirmed -> {
                val prompt = currentState.prompt
                if (prompt is UpdatePrompt.UpdateAvailable) sendEvent(UpdateEvent.StartUpdateFlow(prompt.info))
                showPrompt(UpdatePrompt.None)
            }

            UpdateAction.InstallConfirmed -> {
                sendEvent(UpdateEvent.CompleteUpdate)
                showPrompt(UpdatePrompt.None)
            }

            // 저장에 실패해도 닫는다. 그때는 다음 실행에 다시 묻는다
            UpdateAction.UpdateDismissed -> {
                showPrompt(UpdatePrompt.None)
                viewModelScope.launch { settingsRepository.setUpdatePromptDismissedDate(today()) }
            }

            // 다운로드된 업데이트는 다음에 앱으로 돌아올 때 다시 묻는다 (Play 권장)
            UpdateAction.InstallDismissed -> {
                showPrompt(UpdatePrompt.None)
            }
        }
    }

    /** 다운로드가 끝났으면 설치를 먼저 묻는다. [offerUpdate]면 새 버전도 안내한다 */
    private fun check(offerUpdate: Boolean) {
        viewModelScope.launch {
            val info = suspendRunCatching { appUpdateManager.requestAppUpdateInfo() }.getOrNull() ?: return@launch
            when {
                info.installStatus() == InstallStatus.DOWNLOADED -> {
                    showPrompt(UpdatePrompt.ReadyToInstall)
                }

                offerUpdate &&
                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isFlexibleUpdateAllowed &&
                    !dismissedToday() -> {
                    showPrompt(UpdatePrompt.UpdateAvailable(info))
                }
            }
        }
    }

    /** 저장값을 못 읽으면 미루지 않은 것으로 본다 */
    private suspend fun dismissedToday(): Boolean =
        settingsRepository.updatePromptDismissedDate().getOrNull() == today()

    private fun today(): LocalDate = LocalDate.now(clock)

    private fun showPrompt(prompt: UpdatePrompt) {
        setState { copy(prompt = prompt) }
    }
}
