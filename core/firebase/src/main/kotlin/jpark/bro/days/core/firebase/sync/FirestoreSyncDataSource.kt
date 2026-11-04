package jpark.bro.days.core.firebase.sync

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firestore 구현 (data-model-erd.md 4장 경로). 변경분 쿼리는 `serverUpdatedAt > since` 정렬 `serverUpdatedAt`
 * (단일 필드라 자동 인덱스로 충분). 리스너는 바뀐 문서(documentChanges)만 흘려보낸다.
 */
internal class FirestoreSyncDataSource(
    private val firestore: FirebaseFirestore,
) : SyncRemoteDataSource {
    override fun observeCouples(uid: String): Flow<List<CoupleDto>> =
        firestore.couplesOf(uid).observeChanges { coupleDtoOf(it.id, it.data.orEmpty(), it.serverMicros()) }

    override suspend fun getCouples(uid: String): List<CoupleDto> =
        firestore.couplesOf(uid).get().await().documents.mapNotNull {
            coupleDtoOf(it.id, it.data.orEmpty(), it.serverMicros())
        }

    override suspend fun pushItems(
        coupleId: String,
        items: List<ItemDto>,
    ) {
        firestore.setAllWithServerTime(items) { firestore.itemsOf(coupleId).document(it.id) to it.toFirestoreMap() }
    }

    override fun observeItems(
        coupleId: String,
        sinceMicros: Long?,
    ): Flow<List<ItemDto>> =
        firestore.itemsOf(coupleId).changedSince(sinceMicros).observeChanges {
            itemDtoOf(it.id, it.data.orEmpty(), it.serverMicros())
        }

    override suspend fun getItems(
        coupleId: String,
        sinceMicros: Long?,
    ): List<ItemDto> =
        firestore.itemsOf(coupleId).changedSince(sinceMicros).get().await().documents.mapNotNull {
            itemDtoOf(it.id, it.data.orEmpty(), it.serverMicros())
        }

    override suspend fun pushHiddenAnniversaries(
        coupleId: String,
        hides: List<HiddenAnniversaryDto>,
    ) {
        firestore.setAllWithServerTime(
            hides,
        ) { firestore.hidesOf(coupleId).document(it.ruleKey) to it.toFirestoreMap() }
    }

    override fun observeHiddenAnniversaries(
        coupleId: String,
        sinceMicros: Long?,
    ): Flow<List<HiddenAnniversaryDto>> =
        firestore.hidesOf(coupleId).changedSince(sinceMicros).observeChanges {
            hiddenAnniversaryDtoOf(it.id, it.data.orEmpty(), it.serverMicros())
        }

    override suspend fun getHiddenAnniversaries(
        coupleId: String,
        sinceMicros: Long?,
    ): List<HiddenAnniversaryDto> =
        firestore.hidesOf(coupleId).changedSince(sinceMicros).get().await().documents.mapNotNull {
            hiddenAnniversaryDtoOf(it.id, it.data.orEmpty(), it.serverMicros())
        }
}

private fun FirebaseFirestore.couplesOf(uid: String): Query = collection(COUPLES).whereArrayContains("members", uid)

private fun FirebaseFirestore.itemsOf(coupleId: String) = collection(COUPLES).document(coupleId).collection(ITEMS)

private fun FirebaseFirestore.hidesOf(coupleId: String) =
    collection(COUPLES).document(coupleId).collection(HIDDEN_ANNIVERSARIES)

/** batch로 나눠 쓴다. 규칙이 serverUpdatedAt == request.time을 요구해 서버 시각을 붙인다 */
private suspend fun <T> FirebaseFirestore.setAllWithServerTime(
    values: List<T>,
    target: (T) -> Pair<DocumentReference, Map<String, Any?>>,
) {
    values.chunked(BATCH_LIMIT).forEach { chunk ->
        val batch = batch()
        chunk.forEach { value ->
            val (ref, data) = target(value)
            batch.set(ref, data + (SERVER_UPDATED_AT to FieldValue.serverTimestamp()))
        }
        batch.commit().await()
    }
}

private const val COUPLES = "couples"
private const val ITEMS = "items"
private const val HIDDEN_ANNIVERSARIES = "hiddenAnniversaries"
internal const val SERVER_UPDATED_AT = "serverUpdatedAt"

/** Firestore batch 한 번에 쓸 수 있는 최대 문서 수 */
private const val BATCH_LIMIT = 500

private fun Query.changedSince(sinceMicros: Long?): Query {
    val ordered = orderBy(SERVER_UPDATED_AT)
    return if (sinceMicros ==
        null
    ) {
        ordered
    } else {
        ordered.whereGreaterThan(SERVER_UPDATED_AT, timestampOfMicros(sinceMicros))
    }
}

/**
 * 바뀐 문서만 흘려보낸다. 이 기기에서 쓰고 아직 서버에 반영되지 않은 문서(serverUpdatedAt이 아직 없음)는 건너뛴다:
 * 서버에 반영되면 서버 시각과 함께 다시 온다. 지워진 문서(REMOVED)는 없다 (soft delete만 허용).
 */
private fun <T> Query.observeChanges(parse: (DocumentSnapshot) -> T?): Flow<List<T>> =
    callbackFlow {
        val registration =
            addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val changed =
                    snapshot
                        ?.documentChanges
                        .orEmpty()
                        .filter { it.type != DocumentChange.Type.REMOVED && !it.document.metadata.hasPendingWrites() }
                        .mapNotNull { parse(it.document) }
                if (changed.isNotEmpty()) trySend(changed)
            }
        awaitClose { registration.remove() }
    }

/** 서버 시각이 없으면(아직 반영 전) 0 */
internal fun DocumentSnapshot.serverMicros(): Long =
    getTimestamp(SERVER_UPDATED_AT)?.let { microsOf(it.seconds, it.nanoseconds) } ?: 0

internal fun microsOf(
    seconds: Long,
    nanoseconds: Int,
): Long = seconds * MICROS_PER_SECOND + nanoseconds / NANOS_PER_MICRO

private fun timestampOfMicros(micros: Long): Timestamp =
    Timestamp(
        Math.floorDiv(micros, MICROS_PER_SECOND),
        (Math.floorMod(micros, MICROS_PER_SECOND) * NANOS_PER_MICRO).toInt(),
    )

private const val MICROS_PER_SECOND = 1_000_000L
private const val NANOS_PER_MICRO = 1_000
