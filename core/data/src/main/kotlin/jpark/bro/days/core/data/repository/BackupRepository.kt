package jpark.bro.days.core.data.repository

import jpark.bro.days.core.model.RestoredBackup

/**
 * 설정 → 백업하기 · 복원하기 (product-spec 6장). 이 계정의 나만 보기 항목과 대표 고정 · 알림을 서버 파일 하나에 둔다.
 * 우리 둘이 항목은 동기화로 받으므로 넣지 않고, 기기에만 있는 사진도 넣지 않는다.
 */
interface BackupRepository {
    /** 마지막 백업 시각(epoch millis). 백업이 없으면 null */
    suspend fun lastBackupAt(): Result<Long?>

    /** 이전 백업을 덮어쓴다. 마지막 백업 뒤 10분이 지나지 않았으면 [BackupTooSoonException] */
    suspend fun backup(): Result<Unit>

    /** 이 계정의 나만 보기 항목 · 설정을 백업 내용으로 바꾼다. 백업이 없으면 [BackupNotFoundException] */
    suspend fun restore(): Result<RestoredBackup>
}

class BackupTooSoonException(
    cause: Throwable? = null,
) : Exception("백업은 10분에 한 번만 할 수 있습니다.", cause)

class BackupNotFoundException : Exception("백업이 없습니다.")

/** Storage 규칙과 같은 백업 간격 */
const val BACKUP_INTERVAL_MILLIS: Long = 10 * 60 * 1000L
