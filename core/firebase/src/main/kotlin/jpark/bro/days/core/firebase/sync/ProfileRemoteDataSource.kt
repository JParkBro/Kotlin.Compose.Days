package jpark.bro.days.core.firebase.sync

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * 프로필 `users/{uid}` (data-model-erd.md 4장). 규칙: 나만 쓰고, 나와 연결된 상대만 읽는다.
 * 쓰기는 이름 · 생일 · 프로필 사진 id만 merge한다 (coupleId는 커플 연결이 쓴다).
 */
interface ProfileRemoteDataSource {
    suspend fun pushProfile(profile: ProfileDto)

    /** 없으면 null (처음 로그인) */
    suspend fun getProfile(uid: String): ProfileDto?

    /** 연결된 상대 프로필. 앱이 화면에 떠 있을 때만 구독한다 */
    fun observeProfile(uid: String): Flow<ProfileDto>
}

/**
 * [birthDate]는 yyyy-MM-dd 또는 null. [profileImageId]는 연결된 커플에 올린 사진만 (`couples/{coupleId}/images/{id}.webp`).
 * [coupleId] · [serverUpdatedAtMicros]는 받을 때만
 */
data class ProfileDto(
    val uid: String,
    val nickname: String,
    val birthDate: String?,
    val updatedAt: Long,
    val profileImageId: String? = null,
    val coupleId: String? = null,
    val serverUpdatedAtMicros: Long = 0,
)

internal class FirestoreProfileDataSource(
    private val firestore: FirebaseFirestore,
) : ProfileRemoteDataSource {
    override suspend fun pushProfile(profile: ProfileDto) {
        val data =
            mapOf(
                "nickname" to profile.nickname,
                "birthDate" to profile.birthDate,
                "profileImageId" to profile.profileImageId,
                "updatedAt" to profile.updatedAt,
                SERVER_UPDATED_AT to FieldValue.serverTimestamp(),
            )
        firestore
            .collection(USERS)
            .document(profile.uid)
            .set(data, SetOptions.merge())
            .await()
    }

    override suspend fun getProfile(uid: String): ProfileDto? {
        val snapshot =
            firestore
                .collection(USERS)
                .document(uid)
                .get()
                .await()
        return if (snapshot.exists()) profileDtoOf(uid, snapshot.data.orEmpty(), snapshot.serverMicros()) else null
    }

    override fun observeProfile(uid: String): Flow<ProfileDto> =
        callbackFlow {
            val registration =
                firestore.collection(USERS).document(uid).addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot == null || !snapshot.exists() || snapshot.metadata.hasPendingWrites()) {
                        return@addSnapshotListener
                    }
                    profileDtoOf(uid, snapshot.data.orEmpty(), snapshot.serverMicros())?.let { trySend(it) }
                }
            awaitClose { registration.remove() }
        }
}

/** 이름이 없는 문서(커플 연결만 쓴 경우 등)는 프로필이 아니다 */
internal fun profileDtoOf(
    uid: String,
    data: Map<String, Any?>,
    serverUpdatedAtMicros: Long,
): ProfileDto? =
    parseOrNull {
        ProfileDto(
            uid = uid,
            nickname = data.requiredString("nickname"),
            birthDate = data.string("birthDate"),
            updatedAt = data.requiredLong("updatedAt"),
            profileImageId = data.pathId("profileImageId"),
            coupleId = data.pathId("coupleId"),
            serverUpdatedAtMicros = serverUpdatedAtMicros,
        )
    }

private const val USERS = "users"
