package jpark.bro.days.core.sync

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.database.dao.AnniversaryHideDao
import jpark.bro.days.core.database.dao.ImageSyncDao
import jpark.bro.days.core.database.dao.ItemSyncDao
import jpark.bro.days.core.database.dao.SyncCursorDao
import jpark.bro.days.core.database.entity.SyncCursorEntity
import jpark.bro.days.core.database.entity.SyncState
import jpark.bro.days.core.firebase.sync.HiddenAnniversaryDto
import jpark.bro.days.core.firebase.sync.ItemDto
import jpark.bro.days.core.firebase.sync.SyncRemoteDataSource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * 연결된 커플의 공유 항목 · 숨긴 기념일 (sync-and-pairing.md 2장). [SyncEngine]이 커플 단위로 부른다.
 * - Push: 내 변경(PENDING · FAILED)을 커플별 batch로. 배경 사진을 아직 올리지 않은 항목은 기다린다 ([ItemSyncDao])
 * - Pull · 리스너: 받은 변경을 Room에 반영하고 커서(받은 것 중 가장 큰 서버 시각)를 저장한다
 */
internal class CoupleDataSync(
    private val remote: SyncRemoteDataSource,
    private val itemDao: ItemSyncDao,
    private val hideDao: AnniversaryHideDao,
    private val cursorDao: SyncCursorDao,
    private val imageDao: ImageSyncDao,
) {
    fun observeUnsyncedCount(userId: String): Flow<Int> =
        combine(itemDao.observeUnsyncedCount(userId), hideDao.observeUnsyncedCount(userId)) { items, hides ->
            items + hides
        }

    /** 올리는 사이 새로 저장된 것까지 [MAX_PUSH_ROUNDS]번까지 반복한다. 실패한 행은 FAILED로 두고 false */
    suspend fun push(userId: String): Boolean {
        var succeeded = true
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

    /** 백그라운드 1회성 조회 */
    suspend fun pull(coupleId: String) {
        applyItems(coupleId, remote.getItems(coupleId, cursorDao.getCursor(itemsKey(coupleId))))
        applyHides(coupleId, remote.getHiddenAnniversaries(coupleId, cursorDao.getCursor(hidesKey(coupleId))))
    }

    /** 포그라운드 리스너. 하나가 오류(권한 거부 등)로 끝나도 나머지는 계속 돈다 */
    suspend fun listen(coupleId: String): Unit =
        coroutineScope {
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

    /** 다른 커플 경로에 섞여 들어온 문서는 버린다. 배경 사진은 화면에 보일 때 받도록 알아 둔다 */
    suspend fun applyItems(
        coupleId: String,
        items: List<ItemDto>,
    ) {
        val valid = items.filter { it.coupleId == coupleId }
        valid.forEach { item ->
            item.backgroundImageId?.let { imageDao.registerRemoteImage(it, item.updatedBy, coupleId) }
        }
        itemDao.upsertRemoteItems(valid.map { it.toEntity() })
        advanceCursor(itemsKey(coupleId), items.maxOfOrNull { it.serverUpdatedAtMicros })
    }

    private suspend fun applyHides(
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
