package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.io.File

/** 가져온 사진을 메모리에 두는 Fake. 파일은 `/fake/{id}.webp` 경로만 흉내 낸다 */
class FakeImageRepository : ImageRepository {
    val files = MutableStateFlow<Map<String, File>>(emptyMap())
    val imported = mutableListOf<String>()
    val discarded = mutableListOf<String>()

    /** true면 다음 가져오기가 실패한다 */
    var failNextImport: Boolean = false
    private var nextId = 1

    fun add(imageId: String): File = File("/fake/$imageId.webp").also { files.value += imageId to it }

    override suspend fun importImage(uri: String): Result<String> {
        if (failNextImport) {
            failNextImport = false
            return Result.failure(IllegalStateException("가져오기 실패 (테스트)"))
        }
        val id = "image-${nextId++}"
        imported += uri
        add(id)
        return Result.success(id)
    }

    override fun observeImageFile(imageId: String): Flow<File?> = files.map { it[imageId] }

    override suspend fun discard(imageId: String): Result<Unit> {
        discarded += imageId
        files.value -= imageId
        return Result.success(Unit)
    }
}
