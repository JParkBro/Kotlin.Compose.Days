package jpark.bro.days.core.data.repository

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.backup.BackupFile
import jpark.bro.days.core.data.backup.toBackup
import jpark.bro.days.core.data.backup.toEntity
import jpark.bro.days.core.data.session.UserSession
import jpark.bro.days.core.database.dao.BackupDao
import jpark.bro.days.core.firebase.sync.BackupRemoteDataSource
import jpark.bro.days.core.model.RestoredBackup
import kotlinx.serialization.json.Json
import java.time.Clock
import jpark.bro.days.core.firebase.sync.BackupTooSoonException as RemoteTooSoon

internal class DefaultBackupRepository(
    private val userSession: UserSession,
    private val backupDao: BackupDao,
    private val remote: BackupRemoteDataSource,
    private val clock: Clock,
) : BackupRepository {
    override suspend fun lastBackupAt(): Result<Long?> =
        suspendRunCatching { remote.lastUploadedAt(userSession.currentUserId()) }

    override suspend fun backup(): Result<Unit> =
        suspendRunCatching {
            val userId = userSession.currentUserId()
            val file =
                BackupFile(
                    createdAt = clock.millis(),
                    items = backupDao.getPrivateItems(userId).map { it.toBackup() },
                    prefs = backupDao.getPrefs(userId).map { it.toBackup() },
                    reminders = backupDao.getReminders(userId).map { it.toBackup() },
                )
            try {
                remote.upload(userId, json.encodeToString(BackupFile.serializer(), file).encodeToByteArray())
            } catch (e: RemoteTooSoon) {
                throw BackupTooSoonException(e)
            }
        }

    override suspend fun restore(): Result<RestoredBackup> =
        suspendRunCatching {
            val userId = userSession.currentUserId()
            val bytes = remote.download(userId) ?: throw BackupNotFoundException()
            val file = json.decodeFromString(BackupFile.serializer(), bytes.decodeToString())
            check(file.version <= BackupFile.CURRENT_VERSION) { "앱을 업데이트해야 읽을 수 있는 백업입니다." }
            backupDao.replace(
                userId = userId,
                items = file.items.map { it.toEntity(userId) },
                prefs = file.prefs.map { it.toEntity(userId) },
                reminders = file.reminders.map { it.toEntity(userId) },
            )
            RestoredBackup(createdAt = file.createdAt, itemCount = file.items.size)
        }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
