package jpark.bro.days.core.data.image

import jpark.bro.days.core.data.repository.ImageRepository

/**
 * 수정 화면에서 고르는 중인 사진 하나 (배경 · 프로필). 저장 전에는 원래 사진을 지우지 않고,
 * 고르다 바꾼 사진 · 저장하지 않은 사진은 버린다.
 */
class ImageDraft(
    private val images: ImageRepository,
    private var original: String?,
) {
    /** 저장할 사진 id. null이면 사진 없음 */
    var current: String? = original
        private set

    /** 이번 화면에서 새로 가져온 사진 (저장하지 않으면 버릴 것) */
    private val imported = mutableSetOf<String>()

    /** 원래 사진을 늦게 알게 됐을 때 (수정 화면이 항목을 불러온 뒤) */
    fun reset(saved: String?) {
        original = saved
        current = saved
    }

    suspend fun replace(uri: String): Result<String> =
        images.importImage(uri).onSuccess { id ->
            discardIfUnsaved(current)
            imported += id
            current = id
        }

    suspend fun remove() {
        discardIfUnsaved(current)
        current = null
    }

    /** 저장에 성공한 뒤: 바꾼 원래 사진을 버린다 */
    suspend fun commit() {
        val previous = original
        if (previous != null && previous != current) images.discard(previous)
        imported.clear()
        original = current
    }

    /** 저장하지 않고 나갈 때: 새로 가져온 사진을 버린다 */
    suspend fun abandon() {
        imported.forEach { images.discard(it) }
        imported.clear()
        current = original
    }

    private suspend fun discardIfUnsaved(imageId: String?) {
        if (imageId != null && imageId in imported) {
            images.discard(imageId)
            imported -= imageId
        }
    }
}
