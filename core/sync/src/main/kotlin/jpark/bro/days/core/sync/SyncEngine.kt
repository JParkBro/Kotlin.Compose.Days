package jpark.bro.days.core.sync

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.couple.CoupleTransitions
import jpark.bro.days.core.database.dao.AnniversaryHideDao
import jpark.bro.days.core.database.dao.ItemSyncDao
import jpark.bro.days.core.database.dao.SyncCursorDao
import jpark.bro.days.core.database.entity.SyncCursorEntity
import jpark.bro.days.core.database.entity.SyncState
import jpark.bro.days.core.firebase.sync.HiddenAnniversaryDto
import jpark.bro.days.core.firebase.sync.ItemDto
import jpark.bro.days.core.firebase.sync.SyncRemoteDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * 동기화 한 단위씩 (sync-and-pairing.md 2장). 언제 실행할지는 [SyncManager] · Worker가 정한다.
 * - Push: 내 변경(PENDING · FAILED)을 커플별 batch로 올린다
 * - Pull: 받은 변경을 Room에 반영하고 커서(받은 것 중 가장 큰 서버 시각)를 저장한다
 * 오류는 밖으로 던지지 않고 결과로 돌려준다. 실패한 행은 sync_state = FAILED로 남는다.
 * (공통 로거가 생기면 실패 종류만 기록한다. android.util.Log 직접 사용 금지, 개인정보 금지)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SyncEngine internal constructor(
    private val remote: SyncRemoteDataSource,
    private val itemDao: ItemSyncDao,
    private val hideDao: AnniversaryHideDao,
    private val couples: CoupleTransitions,
    private val cursorDao: SyncCursorDao,
    private val profileSync: ProfileSync,
) {
    /** 올릴 것이 있으면 0보다 크다 */
    fun observeUnsyncedCount(userId: String): Flow<Int> =
        combine(
            itemDao.observeUnsyncedCount(userId),
            hideDao.observeUnsyncedCount(userId),
            profileSync.observeUnsyncedCount(userId),
        ) { items, hides, profile -> items + hides + profile }

    fun observeActiveCoupleId(userId: String): Flow<String?> =
        couples.observeActiveCouple(userId).map { it?.id }.distinctUntilChanged()

    /**
     * 내 변경을 올린다. 올리는 사이 새로 저장된 것까지 [MAX_PUSH_ROUNDS]번까지 반복한다.
     * 한 번이라도 실패하면 그 행을 FAILED로 두고 false (Worker가 백오프로 다시 시도)
     */
    suspend fun push(userId: String): Boolean {
        var succeeded = profileSync.push(userId)
        var round = 0
        while (succeeded && round < MAX_PUSH_ROUNDS) {
            val items = itemDao.getUnsyncedSharedItems(userId)
            val hides = hideDao.getUnsyncedHides(userId)
            if (items.isEmpty() && hides.isEmpty()) break
            items.groupBy { checkNotNull(it.coupleId) }.forEach { (coupleId, group) ->
                val ok = attempt { remote.pushItems(coupleId, group.map { it.toDto(coupleId) }) }
                group.forEach { itemDao.setSyncState(it.id, it.updatedAt, stateAfterPush(ok)) }
                succeeded = succeeded && ok
            }
            hides.groupBy { it.coupleId }.forEach { (coupleId, group) ->
                val ok = attempt { remote.pushHiddenAnniversaries(coupleId, group.map { it.toDto() }) }
                group.forEach { hideDao.setSyncState(coupleId, it.ruleKey, it.deletedAt, stateAfterPush(ok)) }
                succeeded = succeeded && ok
            }
            round++
        }
        return succeeded
    }

    /** 백그라운드 1회성 조회. 로컬에 연결된 커플이 있을 때만 부른다 (Worker) */
    suspend fun pullOnce(userId: String): Boolean =
        attempt {
            profileSync.pullMineIfMissing(userId)
            couples.apply(userId, remote.getCouples(userId))
            val couple = couples.getActiveCouple(userId) ?: return@attempt
            val coupleId = couple.id
            profileSync.pullPartner(couple.partnerOf(userId))
            applyItems(coupleId, remote.getItems(coupleId, cursorDao.getCursor(itemsKey(coupleId))))
            applyHides(
                coupleId,
                remote.getHiddenAnniversaries(coupleId, cursorDao.getCursor(hidesKey(coupleId))),
            )
        }

    /**
     * 포그라운드 리스너. 취소될 때까지 돌아간다: 내 커플 문서 + 연결된 커플의 공유 항목 · 숨긴 기념일 변경분 + 상대 프로필.
     * 리스너 하나가 오류(권한 거부 등)로 끝나도 나머지는 계속 돈다. 다음 포그라운드 진입 때 다시 붙는다.
     */
    suspend fun listen(userId: String): Unit =
        coroutineScope {
            launch { profileSync.pullMineIfMissing(userId) }
            launch {
                remote
                    .observeCouples(userId)
                    .catch { /* 다음 포그라운드 진입 때 다시 붙는다 */ }
                    .collect { couples.apply(userId, it) }
            }
            couples
                .observeActiveCouple(userId)
                .map { it?.let { couple -> couple.id to couple.partnerOf(userId) } }
                .distinctUntilChanged()
                .collectLatest { active ->
                    val (coupleId, partnerId) = active ?: return@collectLatest
                    coroutineScope {
                        launch { profileSync.listenPartner(partnerId) }
                        launch {
                            remote
                                .observeItems(coupleId, cursorDao.getCursor(itemsKey(coupleId)))
                                .catch { /* 다음 포그라운드 진입 때 다시 붙는다 */ }
                                .collect { applyItems(coupleId, it) }
                        }
                        launch {
                            remote
                                .observeHiddenAnniversaries(coupleId, cursorDao.getCursor(hidesKey(coupleId)))
                                .catch { /* 다음 포그라운드 진입 때 다시 붙는다 */ }
                                .collect { applyHides(coupleId, it) }
                        }
                    }
                }
        }

    /** 다른 커플 경로에 섞여 들어온 문서는 버린다 */
    internal suspend fun applyItems(
        coupleId: String,
        items: List<ItemDto>,
    ) {
        val valid = items.filter { it.coupleId == coupleId }
        itemDao.upsertRemoteItems(valid.map { it.toEntity() })
        advanceCursor(itemsKey(coupleId), items.maxOfOrNull { it.serverUpdatedAtMicros })
    }

    internal suspend fun applyHides(
        coupleId: String,
        hides: List<HiddenAnniversaryDto>,
    ) {
        hideDao.upsertRemoteHides(hides.map { it.toEntity(coupleId) })
        advanceCursor(hidesKey(coupleId), hides.maxOfOrNull { it.serverUpdatedAtMicros })
    }

    /** 커서는 앞으로만 간다 */
    private suspend fun advanceCursor(
        key: String,
        micros: Long?,
    ) {
        if (micros == null) return
        val current = cursorDao.getCursor(key)
        if (current == null || micros > current) cursorDao.upsertCursor(SyncCursorEntity(key, micros))
    }

    private suspend fun attempt(block: suspend () -> Unit): Boolean = suspendRunCatching { block() }.isSuccess

    private companion object {
        const val MAX_PUSH_ROUNDS = 3

        fun itemsKey(coupleId: String) = "items:$coupleId"

        fun hidesKey(coupleId: String) = "hiddenAnniversaries:$coupleId"

        fun stateAfterPush(ok: Boolean): String = if (ok) SyncState.SYNCED.name else SyncState.FAILED.name
    }
}
