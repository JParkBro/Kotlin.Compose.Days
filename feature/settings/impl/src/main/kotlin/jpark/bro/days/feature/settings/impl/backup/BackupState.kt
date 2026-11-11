package jpark.bro.days.feature.settings.impl.backup

import java.time.LocalDateTime

internal enum class BackupWork {
    /** 광고를 보는 중이거나 백업 파일을 올리는 중 */
    BACKUP,
    RESTORE,
}

/** 백업 · 복원 (product-spec 6장) */
internal data class BackupState(
    val isLoading: Boolean = true,
    /** 마지막 백업 시각. 없으면 아직 백업하지 않음 */
    val lastBackupAt: LocalDateTime? = null,
    val work: BackupWork? = null,
    /** 복원 전 "지금 기기의 나만 보기 데이터가 백업 내용으로 바뀐다" 확인 창 */
    val isRestoreConfirmVisible: Boolean = false,
) {
    val isWorking: Boolean get() = work != null
}
