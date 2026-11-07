package jpark.bro.days.core.data.repository

import jpark.bro.days.core.common.IdGenerator
import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.session.UserSession
import jpark.bro.days.core.database.dao.ActivityLogDao
import jpark.bro.days.core.database.entity.ActivityLogEntity
import jpark.bro.days.core.model.ActivityContent
import jpark.bro.days.core.model.ActivityLog
import jpark.bro.days.core.model.ActivityType
import jpark.bro.days.core.model.ItemType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultActivityRepository(
    private val dao: ActivityLogDao,
    private val userSession: UserSession,
    private val notifier: ActivityNotifier,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) : ActivityRepository {
    override fun observeActivities(): Flow<List<ActivityLog>> =
        userSession.observeUserId().flatMapLatest { userId ->
            if (userId == null) {
                flowOf(emptyList())
            } else {
                // 화면을 여는 시점 기준. 오래 열어 둔 사이 지난 기록은 다음 기록 때 지워진다
                dao.observeActivities(userId, since = retentionStart()).map { rows -> rows.map { it.toModel() } }
            }
        }

    override suspend fun record(contents: List<ActivityContent>): Result<Unit> =
        suspendRunCatching {
            if (contents.isEmpty()) return@suspendRunCatching
            val userId = userSession.currentUserId()
            val now = clock.millis()
            dao.insertAndPrune(
                activities = contents.map { it.toEntity(id = idGenerator.newId(), userId = userId, createdAt = now) },
                olderThan = retentionStart(),
            )
            contents.filter { it.type.isPartnerActivity }.takeIf { it.isNotEmpty() }?.let {
                notifier.notifyPartnerActivities(it)
            }
        }

    override suspend fun markRead(id: String): Result<Unit> =
        suspendRunCatching { dao.markRead(userSession.currentUserId(), id) }

    override suspend fun markAllRead(): Result<Unit> =
        suspendRunCatching { dao.markAllRead(userSession.currentUserId()) }

    private fun retentionStart(): Long = clock.millis() - Duration.ofDays(ActivityLog.RETENTION_DAYS).toMillis()
}

private fun ActivityContent.toEntity(
    id: String,
    userId: String,
    createdAt: Long,
): ActivityLogEntity =
    ActivityLogEntity(
        id = id,
        userId = userId,
        type = type.name,
        itemId = itemId,
        itemType = itemType?.name,
        itemTitle = itemTitle,
        actorUserId = actorUserId,
        offsetDays = offsetDays,
        ruleKey = ruleKey,
        createdAt = createdAt,
    )

private fun ActivityLogEntity.toModel(): ActivityLog =
    ActivityLog(
        id = id,
        content =
            ActivityContent(
                type = ActivityType.valueOf(type),
                itemId = itemId,
                itemType = itemType?.let(ItemType::valueOf),
                itemTitle = itemTitle,
                actorUserId = actorUserId,
                offsetDays = offsetDays,
                ruleKey = ruleKey,
            ),
        createdAt = createdAt,
        isRead = isRead,
    )
