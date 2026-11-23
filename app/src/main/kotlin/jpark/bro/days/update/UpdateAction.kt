package jpark.bro.days.update

internal sealed interface UpdateAction {
    /** 앱을 처음 열었을 때 (회전 등으로 다시 만들어질 때는 아님) */
    data object AppStarted : UpdateAction

    /** 앱으로 돌아왔을 때. 다운로드가 끝났는지 다시 확인한다 */
    data object AppResumed : UpdateAction

    data object UpdateConfirmed : UpdateAction

    data object UpdateDismissed : UpdateAction

    data object InstallConfirmed : UpdateAction

    data object InstallDismissed : UpdateAction
}
