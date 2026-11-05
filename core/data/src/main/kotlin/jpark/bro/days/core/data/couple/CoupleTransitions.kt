package jpark.bro.days.core.data.couple

import jpark.bro.days.core.common.IdGenerator
import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.database.dao.CoupleDao
import jpark.bro.days.core.database.dao.CoupleItemDao
import jpark.bro.days.core.database.entity.CoupleEntity
import jpark.bro.days.core.datastore.IssuedPairCode
import jpark.bro.days.core.datastore.SettingsDataStore
import jpark.bro.days.core.firebase.sync.CoupleDto
import jpark.bro.days.core.firebase.sync.CoupleRemoteDataSource
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap

/**
 * 커플 문서 변경을 기기에 반영하고, 연결 · 해제 전환 때 할 일을 한다 (sync-and-pairing.md 3 · 5장).
 * 내가 연결 · 해제했을 때(CoupleRepository)와 상대가 했을 때(동기화 리스너) 모두 이곳을 거쳐 한 번만 처리한다.
 * - 연결됨: 내 users.coupleId 기록, 내가 발급한 코드 문서 삭제, 재연결이면 고치지 않은 사본 정리
 * - 해제됨: 공유 항목을 나만 보기 사본으로 복사(내가 "사본도 삭제"를 고르지 않았을 때), users.coupleId 비우기
 * 서버 쓰기(coupleId · 코드 삭제)는 실패해도 연결 상태에는 영향이 없어 다시 시도하지 않는다.
 */
class CoupleTransitions(
    private val coupleDao: CoupleDao,
    private val coupleItemDao: CoupleItemDao,
    private val remote: CoupleRemoteDataSource,
    private val settingsDataStore: SettingsDataStore,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) {
    /** 내가 "사본도 삭제"를 골라 해제하는 커플 */
    private val skipCopies = ConcurrentHashMap.newKeySet<String>()

    fun observeActiveCouple(userId: String): Flow<CoupleEntity?> = coupleDao.observeActiveCouple(userId)

    suspend fun getActiveCouple(userId: String): CoupleEntity? = coupleDao.getActiveCouple(userId)

    /** 받은(또는 내가 쓴) 커플 문서 반영. [userId]가 멤버가 아닌 문서는 버린다 */
    suspend fun apply(
        userId: String,
        couples: List<CoupleDto>,
    ) {
        couples.filter { userId in it.members }.forEach { couple ->
            val local = coupleDao.getCouple(couple.id)
            coupleDao.upsertCouple(couple.toEntity())
            val wasActive = local?.status == ACTIVE
            val isActive = couple.status == ACTIVE
            when {
                !wasActive && isActive -> onConnected(userId, couple, restoredFrom = local)
                wasActive && !isActive -> onDisconnected(userId, couple)
                else -> Unit
            }
        }
    }

    internal fun skipCopiesOnce(coupleId: String) {
        skipCopies += coupleId
    }

    internal suspend fun issuedCode(userId: String): IssuedPairCode? =
        settingsDataStore.issuedPairCode()?.takeIf { it.userId == userId }

    internal suspend fun rememberIssuedCode(code: IssuedPairCode?) {
        settingsDataStore.setIssuedPairCode(code)
    }

    /** 지난 코드 문서는 서버에서도 지운다 (TTL을 쓰지 않음, firebase-cost-strategy.md 2장) */
    internal suspend fun forgetIssuedCode(userId: String) {
        val issued = issuedCode(userId) ?: return
        suspendRunCatching { remote.deletePairCode(issued.code) }
        rememberIssuedCode(null)
    }

    private suspend fun onConnected(
        userId: String,
        couple: CoupleDto,
        restoredFrom: CoupleEntity?,
    ) {
        suspendRunCatching { remote.setMyCoupleId(userId, couple.id) }
        forgetIssuedCode(userId)
        val copiedAt = restoredFrom?.takeIf { it.status == DISCONNECTED }?.disconnectedAt ?: return
        coupleItemDao.removeUnchangedCopies(userId, couple.id, copiedAt = copiedAt, now = clock.millis())
    }

    private suspend fun onDisconnected(
        userId: String,
        couple: CoupleDto,
    ) {
        if (!skipCopies.remove(couple.id)) {
            val copiedAt = couple.disconnectedAt ?: clock.millis()
            coupleItemDao.copySharedItems(userId, couple.id, copiedAt) { idGenerator.newId() }
        }
        suspendRunCatching { remote.setMyCoupleId(userId, null) }
    }
}
