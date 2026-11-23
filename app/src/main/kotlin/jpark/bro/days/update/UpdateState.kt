package jpark.bro.days.update

import com.google.android.play.core.appupdate.AppUpdateInfo

/** 인앱 업데이트 안내. 다이얼로그 두 개가 동시에 뜨지 않도록 하나의 값으로 둔다 */
internal data class UpdateState(
    val prompt: UpdatePrompt = UpdatePrompt.None,
)

internal sealed interface UpdatePrompt {
    data object None : UpdatePrompt

    /** 새 버전이 있음 → "업데이트 안내" */
    data class UpdateAvailable(
        val info: AppUpdateInfo,
    ) : UpdatePrompt

    /** 백그라운드 다운로드가 끝남 → "설치 준비 완료" */
    data object ReadyToInstall : UpdatePrompt
}
