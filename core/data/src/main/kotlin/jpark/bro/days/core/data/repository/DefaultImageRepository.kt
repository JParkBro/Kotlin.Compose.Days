package jpark.bro.days.core.data.repository

import jpark.bro.days.core.common.IdGenerator
import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.image.ImageStore
import jpark.bro.days.core.data.session.UserSession
import jpark.bro.days.core.database.dao.ImageDao
import jpark.bro.days.core.database.entity.ImageEntity
import jpark.bro.days.core.firebase.sync.ImageRemoteDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultImageRepository(
    private val imageDao: ImageDao,
    private val remote: ImageRemoteDataSource,
    private val userSession: UserSession,
    private val store: ImageStore,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) : ImageRepository {
    private val downloadLock = Mutex()

    /** 이번 실행에서 받지 못한 사진. 같은 사진을 계속 다시 요청하지 않게 다음 실행까지 기다린다 */
    private val failedDownloads = mutableSetOf<String>()

    override suspend fun importImage(uri: String): Result<String> =
        suspendRunCatching {
            val me = userSession.currentUserId()
            val id = idGenerator.newId()
            val path = store.relativePath(id)
            val stored = store.save(uri, path)
            imageDao.upsertImage(
                ImageEntity(
                    id = id,
                    ownerUserId = me,
                    remoteCoupleId = null,
                    localPath = path,
                    width = stored.width,
                    height = stored.height,
                    bytes = stored.bytes,
                    createdAt = clock.millis(),
                    deletedAt = null,
                ),
            )
            id
        }

    override fun observeImageFile(imageId: String): Flow<File?> =
        imageDao
            .observeImage(imageId)
            .transformLatest { image ->
                val local = image?.localPath?.let(store::file)?.takeIf { it.exists() }
                emit(local)
                // 받으면 local_path가 바뀌어 다시 흐른다
                if (local == null && image != null && image.deletedAt == null) download(image)
            }.distinctUntilChanged()

    override suspend fun discard(imageId: String): Result<Unit> =
        suspendRunCatching {
            val image = imageDao.getImage(imageId) ?: return@suspendRunCatching
            image.localPath?.let { store.delete(it) }
            val me = userSession.currentUserId()
            if (image.remoteCoupleId != null && image.ownerUserId == me) {
                imageDao.markDeleted(imageId, clock.millis())
            } else {
                imageDao.deleteImage(imageId)
            }
        }

    private suspend fun download(image: ImageEntity) {
        val coupleId = image.remoteCoupleId ?: return
        downloadLock.withLock {
            val path = store.relativePath(image.id)
            when {
                image.id in failedDownloads -> Unit

                // 다른 화면이 먼저 받음
                store.file(path).exists() -> imageDao.setLocalPath(image.id, path)

                else -> fetch(coupleId, image.id, path)
            }
        }
    }

    private suspend fun fetch(
        coupleId: String,
        imageId: String,
        path: String,
    ) {
        val temp = store.tempFile(imageId)
        suspendRunCatching { remote.download(coupleId, imageId, temp) }
            .onSuccess {
                val target = store.file(path)
                target.parentFile?.mkdirs()
                if (temp.renameTo(target)) imageDao.setLocalPath(imageId, path) else failedDownloads += imageId
            }.onFailure {
                temp.delete()
                failedDownloads += imageId
            }
    }
}
