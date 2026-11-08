package jpark.bro.days.core.sync

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.repository.ActivityRepository
import jpark.bro.days.core.database.dao.AnniversaryHideDao
import jpark.bro.days.core.database.dao.AppliedRemoteItem
import jpark.bro.days.core.database.dao.ImageSyncDao
import jpark.bro.days.core.database.dao.ItemSyncDao
import jpark.bro.days.core.database.dao.SyncCursorDao
import jpark.bro.days.core.database.entity.ItemEntity
import jpark.bro.days.core.database.entity.SyncCursorEntity
import jpark.bro.days.core.database.entity.SyncState
import jpark.bro.days.core.firebase.sync.HiddenAnniversaryDto
import jpark.bro.days.core.firebase.sync.ItemDto
import jpark.bro.days.core.firebase.sync.SyncRemoteDataSource
import jpark.bro.days.core.model.ActivityContent
import jpark.bro.days.core.model.ActivityType
import jpark.bro.days.core.model.ItemType
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
    private val activities: ActivityRepository,
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

    /** 백그라운드 1회성 조회. [userId]는 이 기기에서 로그인한 나 (상대 활동을 가려낸다) */
    suspend fun pull(
        coupleId: String,
        userId: String,
    ) {
        applyItems(coupleId, remote.getItems(coupleId, cursorDao.getCursor(itemsKey(coupleId))), userId)
        applyHides(coupleId, remote.getHiddenAnniversaries(coupleId, cursorDao.getCursor(hidesKey(coupleId))))
    }

    /** 포그라운드 리스너. 하나가 오류(권한 거부 등)로 끝나도 나머지는 계속 돈다 */
    suspend fun listen(
        coupleId: String,
        userId: String,
    ): Unit =
        coroutineScope {
            launch {
                remote
                    .observeItems(coupleId, cursorDao.getCursor(itemsKey(coupleId)))
                    .catch { /* 다음 포그라운드 진입 때 다시 붙는다 */ }
                    .collect { applyItems(coupleId, it, userId) }
            }
            launch {
                remote
                    .observeHiddenAnniversaries(coupleId, cursorDao.getCursor(hidesKey(coupleId)))
                    .catch { /* 다음 포그라운드 진입 때 다시 붙는다 */ }
                    .collect { applyHides(coupleId, it) }
            }
        }

    /**
     * 다른 커플 경로에 섞여 들어온 문서는 버린다. 배경 사진은 화면에 보일 때 받도록 알아 둔다.
     * 상대가 고친 것을 실제로 반영했으면 알림 목록에 남긴다 (sync-and-pairing.md Pull 5단계).
     * 처음 받을 때(커서 없음)는 이미 있던 항목이 한꺼번에 들어오므로 남기지 않는다
     */
    suspend fun applyItems(
        coupleId: String,
        items: List<ItemDto>,
        userId: String,
    ) {
        val valid = items.filter { it.coupleId == coupleId }
        valid.forEach { item ->
            item.backgroundImageId?.let { imageDao.registerRemoteImage(it, item.updatedBy, coupleId) }
        }
        val isFirstPull = cursorDao.getCursor(itemsKey(coupleId)) == null
        val applied = itemDao.upsertRemoteItems(valid.map { it.toEntity() })
        if (!isFirstPull) {
            // 기록에 실패해도 동기화는 계속한다
            activities.record(applied.mapNotNull { it.toPartnerActivity(userId) })
        }
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

/**
 * 반영한 항목이 상대 활동이면 그 내용. 내가 고친 것(내 Push가 되돌아온 것)이나 이미 반영한 변경은 null.
 * 기기에 없었거나 지워져 있던 항목이면 추가, 이번에 지워졌으면 삭제, 나머지는 수정
 */
internal fun AppliedRemoteItem.toPartnerActivity(userId: String): ActivityContent? {
    val type = partnerActivityType(previous, current, userId) ?: return null
    return ActivityContent(
        type = type,
        itemId = current.id,
        itemType = ItemType.valueOf(current.type),
        itemTitle = current.title,
        actorUserId = current.updatedBy,
    )
}

private fun partnerActivityType(
    before: ItemEntity?,
    after: ItemEntity,
    userId: String,
): ActivityType? {
    val wasVisible = before != null && before.deletedAt == null
    return when {
        after.updatedBy == userId -> null
        before != null && before.updatedAt >= after.updatedAt -> null
        after.deletedAt != null -> ActivityType.ITEM_DELETED.takeIf { wasVisible }
        wasVisible -> ActivityType.ITEM_UPDATED
        else -> ActivityType.ITEM_ADDED
    }
}
