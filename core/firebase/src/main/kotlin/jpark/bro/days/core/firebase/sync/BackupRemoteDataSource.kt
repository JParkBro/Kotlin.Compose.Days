package jpark.bro.days.core.firebase.sync

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.storageMetadata
import kotlinx.coroutines.tasks.await

/**
 * 백업 파일 하나 (`backups/{uid}/latest.json`, infra-and-billing-protection.md 2-6). Storage 규칙이 본인만,
 * JSON 2MB 이하, 마지막으로 올린 뒤 10분이 지나야 다시 올리게 한다. 업로드 · 다운로드 1번씩만 쓴다.
 */
interface BackupRemoteDataSource {
    /** 10분 안에 다시 올리면 규칙이 거부한다 ([BackupTooSoonException]) */
    suspend fun upload(
        userId: String,
        json: ByteArray,
    )

    /** 백업이 없으면 null */
    suspend fun download(userId: String): ByteArray?

    /** 마지막으로 올린 시각(epoch millis). 백업이 없으면 null (메타데이터 읽기 1번) */
    suspend fun lastUploadedAt(userId: String): Long?

    /** 이미 없으면 성공으로 본다 (탈퇴) */
    suspend fun delete(userId: String)
}

/** 백업한 지 10분이 지나지 않음 (Storage 규칙 거부) */
class BackupTooSoonException(
    cause: Throwable,
) : Exception("백업은 10분에 한 번만 할 수 있습니다.", cause)

internal class FirebaseBackupDataSource(
    private val storage: FirebaseStorage,
) : BackupRemoteDataSource {
    override suspend fun upload(
        userId: String,
        json: ByteArray,
    ) {
        try {
            backup(userId).putBytes(json, storageMetadata { contentType = JSON }).await()
        } catch (e: StorageException) {
            if (e.errorCode == StorageException.ERROR_NOT_AUTHORIZED) throw BackupTooSoonException(e)
            throw e
        }
    }

    override suspend fun download(userId: String): ByteArray? =
        try {
            backup(userId).getBytes(MAX_BYTES).await()
        } catch (e: StorageException) {
            if (e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) null else throw e
        }

    override suspend fun lastUploadedAt(userId: String): Long? =
        try {
            backup(userId).metadata.await().updatedTimeMillis
        } catch (e: StorageException) {
            if (e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) null else throw e
        }

    override suspend fun delete(userId: String) {
        try {
            backup(userId).delete().await()
        } catch (e: StorageException) {
            if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) throw e
        }
    }

    private fun backup(userId: String) = storage.reference.child("backups/$userId/latest.json")

    private companion object {
        const val JSON = "application/json"

        /** Storage 규칙의 크기 한도와 같게 */
        const val MAX_BYTES = 2L * 1024 * 1024
    }
}
