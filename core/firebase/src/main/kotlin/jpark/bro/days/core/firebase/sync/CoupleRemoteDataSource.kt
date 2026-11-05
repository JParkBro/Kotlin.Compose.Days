package jpark.bro.days.core.firebase.sync

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * 커플 연결 · 관리 쓰기 (sync-and-pairing.md 3 · 5장). 읽기 · 리스너는 [SyncRemoteDataSource].
 * 온라인일 때만 된다 (연결은 두 사람이 동시에 확인해야 하므로 오프라인 큐에 넣지 않는다).
 */
interface CoupleRemoteDataSource {
    /** 코드 문서가 비어 있을 때만 만든다. 이미 있으면 false (다른 코드로 다시 시도) */
    suspend fun createPairCode(code: PairCodeDto): Boolean

    /** 코드를 아는 사람만 단건 조회 (목록 조회는 규칙이 막음). 없으면 null */
    suspend fun getPairCode(code: String): PairCodeDto?

    /** 내가 멤버인 커플 (30일 안 재연결할 해제된 커플 찾기) */
    suspend fun findMyCouples(me: String): List<CoupleDto>

    /** 발급자만 지울 수 있다 */
    suspend fun deletePairCode(code: String)

    /**
     * 한 트랜잭션에서 코드에 `usedBy = 나`를 기록하고 커플을 만든다.
     * [restore]면 같은 두 사람의 해제된 커플 문서를 ACTIVE로 되돌린다 (30일 안 재연결)
     */
    suspend fun connect(
        code: String,
        me: String,
        couple: CoupleDto,
        restore: Boolean,
    )

    /** 사귄 날 · 1일로 세기 · 해제(status · disconnectedAt) 수정. 멤버 구성은 바꾸지 않는다 */
    suspend fun updateCouple(couple: CoupleDto)

    /** 내 프로필 문서의 coupleId (연결된 상대만 내 프로필을 읽게 하는 규칙 조건) */
    suspend fun setMyCoupleId(
        me: String,
        coupleId: String?,
    )
}

/** Firestore `pairCodes/{code}`. [usedBy]는 읽을 때만 */
data class PairCodeDto(
    val code: String,
    val creatorUid: String,
    val creatorNickname: String,
    val expiresAt: Long,
    val createdAt: Long,
    val usedBy: String? = null,
)

internal class FirestoreCoupleDataSource(
    private val firestore: FirebaseFirestore,
) : CoupleRemoteDataSource {
    override suspend fun createPairCode(code: PairCodeDto): Boolean {
        val ref = firestore.collection(PAIR_CODES).document(code.code)
        return firestore
            .runTransaction { transaction ->
                if (transaction.get(ref).exists()) {
                    false
                } else {
                    transaction.set(
                        ref,
                        mapOf(
                            "creatorUid" to code.creatorUid,
                            "creatorNickname" to code.creatorNickname,
                            "expiresAt" to code.expiresAt,
                            "createdAt" to code.createdAt,
                        ),
                    )
                    true
                }
            }.await()
    }

    override suspend fun getPairCode(code: String): PairCodeDto? {
        val snapshot =
            firestore
                .collection(PAIR_CODES)
                .document(code)
                .get()
                .await()
        return if (snapshot.exists()) pairCodeDtoOf(code, snapshot.data.orEmpty()) else null
    }

    override suspend fun findMyCouples(me: String): List<CoupleDto> =
        firestore
            .collection(COUPLES)
            .whereArrayContains("members", me)
            .get()
            .await()
            .documents
            .mapNotNull { coupleDtoOf(it.id, it.data.orEmpty(), it.serverMicros()) }

    override suspend fun deletePairCode(code: String) {
        firestore
            .collection(PAIR_CODES)
            .document(code)
            .delete()
            .await()
    }

    override suspend fun connect(
        code: String,
        me: String,
        couple: CoupleDto,
        restore: Boolean,
    ) {
        val codeRef = firestore.collection(PAIR_CODES).document(code)
        val coupleRef = firestore.collection(COUPLES).document(couple.id)
        firestore
            .runTransaction { transaction ->
                transaction.update(codeRef, "usedBy", me)
                if (restore) {
                    transaction.set(coupleRef, couple.toFirestoreMap(), SetOptions.merge())
                } else {
                    transaction.set(coupleRef, couple.toFirestoreMap())
                }
            }.await()
    }

    override suspend fun updateCouple(couple: CoupleDto) {
        firestore
            .collection(COUPLES)
            .document(couple.id)
            .set(couple.toFirestoreMap(), SetOptions.merge())
            .await()
    }

    override suspend fun setMyCoupleId(
        me: String,
        coupleId: String?,
    ) {
        firestore
            .collection(USERS)
            .document(me)
            .set(mapOf("coupleId" to coupleId, SERVER_UPDATED_AT to FieldValue.serverTimestamp()), SetOptions.merge())
            .await()
    }
}

/** members 순서: [0] 코드 발급자, [1] 코드 입력자 (규칙의 커플 생성 조건) */
private fun CoupleDto.toFirestoreMap(): Map<String, Any?> =
    mapOf(
        "members" to members,
        "startDate" to startDate,
        "countFirstDayAsOne" to countFirstDayAsOne,
        "status" to status,
        "pairCode" to pairCode,
        "disconnectedAt" to disconnectedAt,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        SERVER_UPDATED_AT to FieldValue.serverTimestamp(),
    )

internal fun pairCodeDtoOf(
    code: String,
    data: Map<String, Any?>,
): PairCodeDto? =
    parseOrNull {
        PairCodeDto(
            code = code,
            creatorUid = data.requiredString("creatorUid"),
            creatorNickname = data.requiredString("creatorNickname"),
            expiresAt = data.requiredLong("expiresAt"),
            createdAt = data.requiredLong("createdAt"),
            usedBy = data.string("usedBy"),
        )
    }

private const val PAIR_CODES = "pairCodes"
private const val COUPLES = "couples"
private const val USERS = "users"
