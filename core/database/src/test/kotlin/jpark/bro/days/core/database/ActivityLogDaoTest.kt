package jpark.bro.days.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.days.core.database.entity.ActivityLogEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ActivityLogDaoTest : DatabaseTest() {
    private val dao get() = db.activityLogDao()

    private fun log(
        id: String,
        createdAt: Long,
        userId: String = ME,
    ) = ActivityLogEntity(id = id, userId = userId, type = "ITEM_ADDED", itemId = "a", createdAt = createdAt)

    @Test
    fun `내 기록만 최신순으로 나오고 보관 기간 전 기록은 빠짐`() =
        runTest {
            dao.insertActivities(
                listOf(log("old", 10), log("a", 100), log("b", 200), log("p", 300, userId = "partner")),
            )

            assertEquals(listOf("b", "a"), dao.observeActivities(ME, since = 50).first().map { it.id })
        }

    @Test
    fun `기록을 남기면 보관 기간이 지난 기록을 지움`() =
        runTest {
            dao.insertActivities(listOf(log("old", 10)))

            dao.insertAndPrune(listOf(log("new", 100)), olderThan = 50)

            assertEquals(listOf("new"), dao.observeActivities(ME, since = 0).first().map { it.id })
        }

    @Test
    fun `하나 또는 모두 읽음 처리`() =
        runTest {
            dao.insertActivities(listOf(log("a", 100), log("b", 200), log("p", 300, userId = "partner")))

            dao.markRead(ME, "a")
            assertEquals(listOf(false, true), dao.observeActivities(ME, since = 0).first().map { it.isRead })

            dao.markAllRead(ME)
            assertEquals(listOf(true, true), dao.observeActivities(ME, since = 0).first().map { it.isRead })
            assertEquals(listOf(false), dao.observeActivities("partner", since = 0).first().map { it.isRead })
        }
}
