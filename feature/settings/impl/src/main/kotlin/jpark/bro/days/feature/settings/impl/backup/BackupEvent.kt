package jpark.bro.days.feature.settings.impl.backup

internal sealed interface BackupEvent {
    /** 백업 전에 보상형 광고를 띄운다 (서버 측 확인 없음) */
    data object ShowRewardedAd : BackupEvent

    data object BackedUp : BackupEvent

    /** 마지막 백업 뒤 10분이 지나지 않음 (Storage 규칙과 같음) */
    data object BackupTooSoon : BackupEvent

    data object BackupFailed : BackupEvent

    data class Restored(
        val itemCount: Int,
    ) : BackupEvent

    data object NoBackup : BackupEvent

    data object RestoreFailed : BackupEvent

    data object NavigateBack : BackupEvent
}
