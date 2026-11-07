package jpark.bro.days.core.data.repository

import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemReminder
import kotlinx.coroutines.flow.Flow

/**
 * 디데이 · 일정 저장소. Room이 원본이고 화면은 observe 스트림만 구독한다.
 * 대표 고정 · 알림은 현재 사용자 것만 다룬다 (공유하지 않음).
 */
interface ItemRepository {
    /** 삭제되지 않은 항목. 날짜순, 날짜 없는 일정은 뒤 */
    fun observeItems(): Flow<List<Item>>

    /** 삭제됐거나 없으면 null */
    fun observeItem(id: String): Flow<Item?>

    /** 내가 대표로 고정한 항목 id. 없거나 삭제됐으면 null */
    fun observePinnedItemId(): Flow<String?>

    /** 내 알림. offsetDays 오름차순 */
    fun observeReminders(itemId: String): Flow<List<ItemReminder>>

    /** 내 알림 전체 (항목 id → offsetDays 오름차순). 지운 항목의 알림도 있을 수 있어 [observeItems]와 맞춰 쓴다 */
    fun observeAllReminders(): Flow<Map<String, List<ItemReminder>>>

    /**
     * [id]가 null이면 새로 만들고, 있으면 그 항목을 수정한다.
     * 제목이 비었거나 타입별 필수 값이 없으면 실패한다.
     */
    suspend fun saveItem(
        id: String?,
        content: ItemContent,
    ): Result<Item>

    /** soft delete. 공유 항목이면 상대 기기에 삭제가 전달되도록 동기화 대상으로 둔다 */
    suspend fun deleteItem(id: String): Result<Unit>

    /** 대표는 1개만. 고정하면 이전 대표는 풀린다 */
    suspend fun setPinned(
        itemId: String,
        pinned: Boolean,
    ): Result<Unit>

    /** 항목의 내 알림을 [reminders]로 통째로 바꾼다 */
    suspend fun setReminders(
        itemId: String,
        reminders: List<ItemReminder>,
    ): Result<Unit>
}
