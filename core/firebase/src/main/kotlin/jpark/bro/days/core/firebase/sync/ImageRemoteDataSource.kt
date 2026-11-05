package jpark.bro.days.core.firebase.sync

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.storageMetadata
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * 사진 파일 (infra-and-billing-protection.md 2-2 · 2-3). 경로는 `couples/{coupleId}/images/{imageId}.webp`로 정해져 있어
 * 문서에는 사진 id만 둔다. Storage 규칙이 커플 멤버만 허용하고, 새 파일은 방금 [claim]한 id만 만들 수 있다.
 */
interface ImageRemoteDataSource {
    /**
     * 커플 업로드 카운터를 1 올리고 [imageId]를 방금 등록한 사진으로 적는다 (`couples/{id}/usage/images`).
     * 이미 마지막으로 등록한 사진이면 다시 올리지 않는다 (업로드 재시도). 한도를 다 썼으면 [ImageLimitException]
     */
    suspend fun claim(
        coupleId: String,
        imageId: String,
    )

    suspend fun upload(
        coupleId: String,
        imageId: String,
        file: File,
    )

    /** 서버에 파일이 없으면 [ImageNotFoundException] */
    suspend fun download(
        coupleId: String,
        imageId: String,
        destination: File,
    )

    /** 이미 없으면 성공으로 본다 */
    suspend fun delete(
        coupleId: String,
        imageId: String,
    )
}

/** 커플당 누적 업로드 한도 (firestore.rules usage 규칙과 같게) */
const val IMAGE_LIMIT_PER_COUPLE = 30

class ImageLimitException : Exception("커플 사진 업로드 한도를 다 썼습니다.")

class ImageNotFoundException : Exception("서버에 사진이 없습니다.")

internal class FirebaseImageDataSource(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) : ImageRemoteDataSource {
    override suspend fun claim(
        coupleId: String,
        imageId: String,
    ) {
        val ref =
            firestore
                .collection(COUPLES)
                .document(coupleId)
                .collection(USAGE)
                .document(IMAGES)
        firestore
            .runTransaction { transaction ->
                val usage = transaction.get(ref)
                val count = usage.getLong(COUNT) ?: 0L
                when {
                    usage.getString(LAST_IMAGE_ID) == imageId -> Unit
                    count >= IMAGE_LIMIT_PER_COUPLE -> throw ImageLimitException()
                    else -> transaction.set(ref, mapOf(COUNT to count + 1, LAST_IMAGE_ID to imageId))
                }
            }.await()
    }

    override suspend fun upload(
        coupleId: String,
        imageId: String,
        file: File,
    ) {
        fileRef(coupleId, imageId)
            .putFile(Uri.fromFile(file), storageMetadata { contentType = WEBP })
            .await()
    }

    override suspend fun download(
        coupleId: String,
        imageId: String,
        destination: File,
    ) {
        try {
            fileRef(coupleId, imageId).getFile(destination).await()
        } catch (e: StorageException) {
            if (e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) throw ImageNotFoundException()
            throw e
        }
    }

    override suspend fun delete(
        coupleId: String,
        imageId: String,
    ) {
        try {
            fileRef(coupleId, imageId).delete().await()
        } catch (e: StorageException) {
            if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) throw e
        }
    }

    private fun fileRef(
        coupleId: String,
        imageId: String,
    ) = storage.reference.child("$COUPLES/$coupleId/images/$imageId.webp")

    private companion object {
        const val COUPLES = "couples"
        const val USAGE = "usage"
        const val IMAGES = "images"
        const val COUNT = "count"
        const val LAST_IMAGE_ID = "lastImageId"
        const val WEBP = "image/webp"
    }
}
