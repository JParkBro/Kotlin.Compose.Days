package jpark.bro.days.core.firebase.sync

import android.net.Uri
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.storageMetadata
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

/**
 * 사진 파일 (infra-and-billing-protection.md 2-2 · 2-3). 경로는 `couples/{coupleId}/images/{imageId}.webp`로 정해져 있어
 * 문서에는 사진 id만 둔다. Storage 규칙이 커플 멤버만 허용하고, 새 파일은 방금 [claim]한 id만 만들 수 있다.
 */
interface ImageRemoteDataSource {
    /**
     * 커플 업로드 카운터를 1 올리고 [imageId]를 방금 등록한 사진으로 적는다 (`couples/{id}/usage/images`).
     * 이미 마지막으로 등록한 사진이면 다시 올리지 않는다 (업로드 재시도). 한도(30 + bonus)를 다 썼으면 [ImageLimitException]
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

    /** 지금까지 등록한 수와 보상형 광고로 받은 bonus. 읽기 1번 */
    suspend fun usage(coupleId: String): ImageUsage

    /**
     * bonus가 [bonus]보다 커질 때까지 기다린다 (admobReward 함수가 AdMob 콜백을 받아 올림, 몇 초 늦을 수 있다).
     * [timeoutMillis] 안에 오르면 그 사용량, 아니면 null. 기다리는 동안만 리스너를 둔다 (읽기 1번 + 바뀔 때마다 1번)
     */
    suspend fun awaitBonusAbove(
        coupleId: String,
        bonus: Int,
        timeoutMillis: Long,
    ): ImageUsage?

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

data class ImageUsage(
    val count: Int,
    val bonus: Int,
)

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
        val ref = usageRef(coupleId)
        firestore
            .runTransaction { transaction ->
                val snapshot = transaction.get(ref)
                val usage = snapshot.toUsage()
                when {
                    snapshot.getString(LAST_IMAGE_ID) == imageId -> {
                        Unit
                    }

                    usage.count >= IMAGE_LIMIT_PER_COUPLE + usage.bonus -> {
                        throw ImageLimitException()
                    }

                    // bonus는 규칙상 앱이 바꿀 수 없으므로 merge로 그대로 둔다
                    else -> {
                        transaction.set(
                            ref,
                            mapOf(COUNT to usage.count + 1, LAST_IMAGE_ID to imageId),
                            SetOptions.merge(),
                        )
                    }
                }
            }.await()
    }

    override suspend fun usage(coupleId: String): ImageUsage = usageRef(coupleId).get().await().toUsage()

    override suspend fun awaitBonusAbove(
        coupleId: String,
        bonus: Int,
        timeoutMillis: Long,
    ): ImageUsage? =
        withTimeoutOrNull(timeoutMillis) {
            callbackFlow {
                val registration =
                    usageRef(coupleId).addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                        } else if (snapshot != null && !snapshot.metadata.isFromCache) {
                            trySend(snapshot.toUsage())
                        }
                    }
                awaitClose { registration.remove() }
            }.first { it.bonus > bonus }
        }

    private fun usageRef(coupleId: String) =
        firestore
            .collection(COUPLES)
            .document(coupleId)
            .collection(USAGE)
            .document(IMAGES)

    private fun DocumentSnapshot.toUsage() =
        ImageUsage(
            count = getLong(COUNT)?.toInt() ?: 0,
            bonus = getLong(BONUS)?.toInt() ?: 0,
        )

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
        const val BONUS = "bonus"
        const val WEBP = "image/webp"
    }
}
