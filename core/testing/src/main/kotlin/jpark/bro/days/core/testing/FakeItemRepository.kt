package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemReminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** 메모리에만 두는 ItemRepository. 시각 · 작성자는 고정값을 쓴다 */
class FakeItemRepository(
    initial: List<Item> = emptyList(),
    private val userId: String = "user-me",
    private val now: Long = 0L,
) : ItemRepository {
    private val items = MutableStateFlow(initial.associateBy { it.id })
    private val pinnedItemId = MutableStateFlow<String?>(null)
    private val reminders = MutableStateFlow<Map<String, List<ItemReminder>>>(emptyMap())
    private var nextId = 1

    /** true면 다음 1회성 작업(save · delete · pin · reminders)이 실패한다 */
    var failNext: Boolean = false

    override fun observeItems(): Flow<List<Item>> =
        items.map { all -> all.values.sortedWith(compareBy(nullsLast()) { it.content.date }) }

    override fun observeItem(id: String): Flow<Item?> = items.map { it[id] }

    override fun observePinnedItemId(): Flow<String?> =
        combine(pinnedItemId, items) { pinned, all -> pinned?.takeIf { it in all } }

    override fun observeReminders(itemId: String): Flow<List<ItemReminder>> =
        reminders.map { all -> all[itemId].orEmpty().sortedBy { it.offsetDays } }

    override fun observeAllReminders(): Flow<Map<String, List<ItemReminder>>> =
        reminders.map { all -> all.mapValues { (_, list) -> list.sortedBy { it.offsetDays } } }

    override suspend fun saveItem(
        id: String?,
        content: ItemContent,
    ): Result<Item> =
        result {
            val existing = id?.let { requireNotNull(items.value[it]) { "수정할 항목이 없습니다." } }
            val item =
                Item(
                    id = existing?.id ?: "item-${nextId++}",
                    content = content,
                    ownerUserId = existing?.ownerUserId ?: userId,
                    updatedBy = userId,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                )
            items.update { it + (item.id to item) }
            item
        }

    override suspend fun deleteItem(id: String): Result<Unit> = result { items.update { it - id } }

    override suspend fun setPinned(
        itemId: String,
        pinned: Boolean,
    ): Result<Unit> =
        result {
            pinnedItemId.update { current ->
                when {
                    pinned -> itemId
                    current == itemId -> null
                    else -> current
                }
            }
        }

    override suspend fun setReminders(
        itemId: String,
        reminders: List<ItemReminder>,
    ): Result<Unit> = result { this.reminders.update { it + (itemId to reminders.distinct()) } }

    private inline fun <T> result(block: () -> T): Result<T> {
        if (failNext) {
            failNext = false
            return Result.failure(IllegalStateException("저장 실패 (테스트)"))
        }
        return runCatching(block)
    }
}
