package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.ActivityNotifier
import jpark.bro.days.core.data.repository.ActivityRepository
import jpark.bro.days.core.model.ActivityContent
import jpark.bro.days.core.model.ActivityLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeActivityRepository(
    initial: List<ActivityLog> = emptyList(),
    private val now: Long = 0L,
) : ActivityRepository {
    private val activities = MutableStateFlow(initial)
    private var nextId = 1

    /** true면 다음 1회성 작업이 실패한다 */
    var failNext: Boolean = false

    override fun observeActivities(): Flow<List<ActivityLog>> = activities

    override suspend fun record(contents: List<ActivityContent>): Result<Unit> =
        result {
            activities.update { current ->
                contents.map {
                    ActivityLog(
                        id = "activity-${nextId++}",
                        content = it,
                        createdAt = now,
                        isRead = false,
                    )
                } +
                    current
            }
        }

    override suspend fun markRead(id: String): Result<Unit> =
        result { activities.update { all -> all.map { if (it.id == id) it.copy(isRead = true) else it } } }

    override suspend fun markAllRead(): Result<Unit> =
        result {
            activities.update { all -> all.map { it.copy(isRead = true) } }
        }

    private fun result(block: () -> Unit): Result<Unit> {
        if (failNext) {
            failNext = false
            return Result.failure(IllegalStateException("실패 (테스트)"))
        }
        block()
        return Result.success(Unit)
    }
}

/** 띄운 상대 활동 알림을 모아 둔다 */
class FakeActivityNotifier : ActivityNotifier {
    val notified = mutableListOf<ActivityContent>()

    override suspend fun notifyPartnerActivities(activities: List<ActivityContent>) {
        notified += activities
    }
}
