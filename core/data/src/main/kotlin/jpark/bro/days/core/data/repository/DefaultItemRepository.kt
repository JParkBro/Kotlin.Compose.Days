package jpark.bro.days.core.data.repository

import jpark.bro.days.core.common.IdGenerator
import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.mapper.toEntity
import jpark.bro.days.core.data.mapper.toModel
import jpark.bro.days.core.data.session.UserSession
import jpark.bro.days.core.database.dao.ItemDao
import jpark.bro.days.core.database.dao.ItemReminderDao
import jpark.bro.days.core.database.dao.UserItemPrefDao
import jpark.bro.days.core.database.entity.SyncState
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemReminder
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.TimeSlot
import jpark.bro.days.core.model.Visibility
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Clock

/** Room suspend 함수는 자체 스레드에서 실행되므로 디스패처를 따로 두지 않는다 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultItemRepository(
    private val itemDao: ItemDao,
    private val userItemPrefDao: UserItemPrefDao,
    private val itemReminderDao: ItemReminderDao,
    private val userSession: UserSession,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) : ItemRepository {
    /** 로그인한 계정이 바뀌면 그 계정의 데이터로 다시 구독한다. 로그아웃 상태면 비어 있다 */
    private fun <T> perUser(
        whenSignedOut: T,
        block: (userId: String) -> Flow<T>,
    ): Flow<T> = userSession.observeUserId().flatMapLatest { userId -> userId?.let(block) ?: flowOf(whenSignedOut) }

    override fun observeItems(): Flow<List<Item>> =
        perUser(emptyList()) { userId -> itemDao.observeItems(userId).map { items -> items.map { it.toModel() } } }

    override fun observeItem(id: String): Flow<Item?> =
        perUser(null) { userId ->
            itemDao.observeItem(id, userId).map {
                it?.toModel()
            }
        }

    override fun observePinnedItemId(): Flow<String?> =
        perUser(null) { userId -> userItemPrefDao.observePinnedItemId(userId) }

    override fun observeReminders(itemId: String): Flow<List<ItemReminder>> =
        perUser(emptyList()) { userId ->
            itemReminderDao.observeReminders(userId, itemId).map { reminders -> reminders.map { it.toModel() } }
        }

    override suspend fun saveItem(
        id: String?,
        content: ItemContent,
    ): Result<Item> =
        suspendRunCatching {
            content.requireValid()
            val me = userSession.currentUserId()
            val existing = id?.let { itemDao.getItem(it) }
            require(id == null || (existing != null && existing.deletedAt == null && existing.ownerUserId == me)) {
                "수정할 항목이 없습니다."
            }

            val now = clock.millis()
            val item =
                Item(
                    id = existing?.id ?: idGenerator.newId(),
                    content = content,
                    ownerUserId = existing?.ownerUserId ?: me,
                    updatedBy = me,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                )
            itemDao.upsertItem(
                item.toEntity(
                    syncState = content.visibility.syncStateOnWrite(),
                    serverUpdatedAt = existing?.serverUpdatedAt,
                ),
            )
            item
        }

    override suspend fun deleteItem(id: String): Result<Unit> =
        suspendRunCatching {
            val me = userSession.currentUserId()
            val existing = itemDao.getItem(id)?.takeIf { it.ownerUserId == me }
            requireNotNull(existing) { "삭제할 항목이 없습니다." }
            if (existing.deletedAt != null) return@suspendRunCatching
            itemDao.softDeleteItem(
                id = id,
                deletedAt = clock.millis(),
                updatedBy = me,
                syncState = Visibility.valueOf(existing.visibility).syncStateOnWrite().name,
            )
        }

    override suspend fun setPinned(
        itemId: String,
        pinned: Boolean,
    ): Result<Unit> =
        suspendRunCatching {
            val me = userSession.currentUserId()
            if (pinned) {
                userItemPrefDao.pinItem(userId = me, itemId = itemId, updatedAt = clock.millis())
            } else {
                userItemPrefDao.unpinItem(userId = me, itemId = itemId, updatedAt = clock.millis())
            }
        }

    override suspend fun setReminders(
        itemId: String,
        reminders: List<ItemReminder>,
    ): Result<Unit> =
        suspendRunCatching {
            val me = userSession.currentUserId()
            itemReminderDao.replaceReminders(
                userId = me,
                itemId = itemId,
                reminders =
                    reminders.distinct().map {
                        it.toEntity(
                            id = idGenerator.newId(),
                            userId = me,
                            itemId = itemId,
                        )
                    },
            )
        }
}

/** 나만 보기 항목은 서버로 보내지 않으므로 올릴 것이 없다 (CLAUDE.md 절대 규칙) */
private fun Visibility.syncStateOnWrite(): SyncState =
    when (this) {
        Visibility.PRIVATE -> SyncState.SYNCED
        Visibility.SHARED -> SyncState.PENDING
    }

/** 저장 전 데이터 무결성 확인 (product-spec 2-1 필수 입력, data-model-erd 항목 타입별 컬럼) */
private fun ItemContent.requireValid() {
    require(title.isNotBlank()) { "제목이 비어 있습니다." }
    when (type) {
        ItemType.DDAY -> {
            require(date != null) { "디데이는 기준일이 필요합니다." }
            require(countMode != null) { "디데이는 계산 방식이 필요합니다." }
        }

        ItemType.PLAN -> {
            Unit
        }
    }
    when (visibility) {
        Visibility.PRIVATE -> require(coupleId == null) { "나만 보기 항목에는 커플 id를 넣지 않습니다." }
        Visibility.SHARED -> require(coupleId != null) { "공유 항목은 커플 id가 필요합니다." }
    }
    require((timeSlot == TimeSlot.EXACT) == (time != null)) { "시간 지정일 때만 시각을 저장합니다." }
}
