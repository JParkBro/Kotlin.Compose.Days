package jpark.bro.days.update

import com.google.android.play.core.appupdate.AppUpdateInfo

/** Play 호출은 Activity에 묶여 있어(결과 런처 · 앱 재시작) Event로 넘긴다 */
internal sealed interface UpdateEvent {
    /** startUpdateFlowForResult(FLEXIBLE) */
    data class StartUpdateFlow(
        val info: AppUpdateInfo,
    ) : UpdateEvent

    /** completeUpdate() — 앱이 재시작되며 설치된다 */
    data object CompleteUpdate : UpdateEvent
}
