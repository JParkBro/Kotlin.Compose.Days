package jpark.bro.days.core.sync

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.image.ImageStore
import jpark.bro.days.core.database.dao.ImageSyncDao
import jpark.bro.days.core.database.dao.UserDao
import jpark.bro.days.core.database.entity.ImageEntity
import jpark.bro.days.core.firebase.sync.ImageLimitException
import jpark.bro.days.core.firebase.sync.ImageRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * 사진 올리기 · 지우기 (infra-and-billing-protection.md 2-2 · 2-3). [SyncEngine.push]가 항목 · 프로필보다 먼저 부른다.
 * - 올리기: 연결된 커플의 공유 항목 배경 · 내 프로필 사진만. 카운터를 +1 하고(방금 등록한 id만 규칙이 허용) 파일을 올린다
 * - 한도를 다 쓰면 REJECTED로 두고 다시 시도하지 않는다 (그 사진은 이 기기에서만 보인다)
 * - 지우기: 내가 올렸다가 더 쓰지 않게 된 사진의 서버 파일. 카운터는 줄이지 않는다
 * - [uploadEnabled]가 꺼지면(Remote Config `upload_enabled`) 올리기만 멈춘다. 사진이 붙은 공유 항목도 기다린다
 */
class ImageSync internal constructor(
    private val remote: ImageRemoteDataSource,
    private val imageDao: ImageSyncDao,
    private val userDao: UserDao,
    private val store: ImageStore,
    private val uploadEnabled: StateFlow<Boolean>,
) {
    fun observeUploadCount(userId: String): Flow<Int> = imageDao.observeUploadCount(userId)

    /** 하나라도 실패하면 false (Worker가 백오프로 다시 시도) */
    suspend fun push(userId: String): Boolean {
        val uploaded = if (uploadEnabled.value) upload(userId) else true
        return deleteRemoved(userId) && uploaded
    }

    private suspend fun upload(userId: String): Boolean {
        var succeeded = true
        imageDao.getImagesToUpload(userId).forEach { upload ->
            val file = store.file(upload.localPath)
            if (!file.exists()) {
                // 기기 파일이 사라짐: 올릴 수 없으니 받기 전 상태로 둔다
                imageDao.setLocalPath(upload.imageId, null)
                return@forEach
            }
            suspendRunCatching {
                remote.claim(upload.coupleId, upload.imageId)
                remote.upload(upload.coupleId, upload.imageId, file)
            }.onSuccess {
                imageDao.markUploaded(upload.imageId, upload.coupleId)
                // 프로필 문서는 커플에 올린 사진 id만 적는다 (ProfileSync)
                userDao.markPendingIfProfileImage(userId, upload.imageId)
            }.onFailure { error ->
                if (error is ImageLimitException) imageDao.markRejected(upload.imageId) else succeeded = false
            }
        }
        return succeeded
    }

    private suspend fun deleteRemoved(userId: String): Boolean {
        var succeeded = true
        imageDao.getImagesToDelete(userId).forEach { image ->
            val coupleId = image.remoteCoupleId ?: return@forEach
            suspendRunCatching { remote.delete(coupleId, image.id) }
                .onSuccess { imageDao.deleteImage(image.id) }
                .onFailure { succeeded = false }
        }
        return succeeded
    }
}

/** 받은 문서(공유 항목 · 상대 프로필)에 나온 사진. 화면에 보일 때 ImageRepository가 받는다. 이미 아는 사진이면 그대로 둔다 */
internal suspend fun ImageSyncDao.registerRemoteImage(
    imageId: String,
    ownerUserId: String,
    coupleId: String,
) {
    insertRemoteImage(
        ImageEntity(
            id = imageId,
            ownerUserId = ownerUserId,
            remoteCoupleId = coupleId,
            localPath = null,
            width = null,
            height = null,
            bytes = null,
            createdAt = 0,
            deletedAt = null,
        ),
    )
}
