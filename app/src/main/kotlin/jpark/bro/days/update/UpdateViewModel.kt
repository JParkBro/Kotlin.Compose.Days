package jpark.bro.days.update

import androidx.lifecycle.viewModelScope
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.requestAppUpdateInfo
import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.launch

/**
 * Play 인앱 업데이트(FLEXIBLE). 업데이트 · 재시작 모두 먼저 묻는다 (docs/days/in-app-update.md).
 * 확인 실패(Play에서 설치하지 않은 빌드 등)는 조용히 넘긴다. 업데이트 확인이 앱 사용을 막으면 안 된다.
 */
internal class UpdateViewModel(
    private val appUpdateManager: AppUpdateManager,
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

            // 다운로드된 업데이트는 다음에 앱으로 돌아올 때 다시 묻는다 (Play 권장)
            UpdateAction.UpdateDismissed,
            UpdateAction.InstallDismissed,
            -> {
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
                    info.isFlexibleUpdateAllowed -> {
                    showPrompt(UpdatePrompt.UpdateAvailable(info))
                }
            }
        }
    }

    private fun showPrompt(prompt: UpdatePrompt) {
        setState { copy(prompt = prompt) }
    }
}
