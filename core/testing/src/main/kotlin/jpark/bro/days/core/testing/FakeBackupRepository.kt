package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.BackupNotFoundException
import jpark.bro.days.core.data.repository.BackupRepository
import jpark.bro.days.core.data.repository.BackupTooSoonException
import jpark.bro.days.core.model.RestoredBackup

/** 백업 Fake. [backedUpAt]이 null이면 백업 없음 */
class FakeBackupRepository(
    var backedUpAt: Long? = null,
    private val now: () -> Long = { 0L },
) : BackupRepository {
    /** 백업해 둔 나만 보기 항목 수 (복원 결과) */
    var itemCount: Int = 0

    /** 백업 · 복원 호출 수 */
    var backupCount: Int = 0
        private set
    var restoreCount: Int = 0
        private set

    var failBackup: Boolean = false
    var tooSoon: Boolean = false
    var failRestore: Boolean = false

    override suspend fun lastBackupAt(): Result<Long?> = Result.success(backedUpAt)

    override suspend fun backup(): Result<Unit> {
        backupCount++
        return when {
            tooSoon -> Result.failure(BackupTooSoonException())
            failBackup -> Result.failure(IllegalStateException("백업 실패 (테스트)"))
            else -> Result.success(Unit).also { backedUpAt = now() }
        }
    }

    override suspend fun restore(): Result<RestoredBackup> {
        restoreCount++
        val createdAt = backedUpAt
        return when {
            failRestore -> Result.failure(IllegalStateException("복원 실패 (테스트)"))
            createdAt == null -> Result.failure(BackupNotFoundException())
            else -> Result.success(RestoredBackup(createdAt, itemCount))
        }
    }
}
