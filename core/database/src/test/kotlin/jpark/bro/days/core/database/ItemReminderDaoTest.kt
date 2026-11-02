package jpark.bro.days.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ItemReminderDaoTest : DatabaseTest() {
    private val dao get() = db.itemReminderDao()

    @Test
    fun `알림을 교체하면 기존 알림이 모두 바뀜`() =
        runTest {
            dao.replaceReminders(ME, "a", listOf(testReminder("r0", "a", 0), testReminder("r1", "a", 1)))

            dao.replaceReminders(ME, "a", listOf(testReminder("r7", "a", 7)))

            assertEquals(listOf("r7"), dao.observeReminders(ME, "a").first().map { it.id })
        }

    @Test
    fun `내 알림을 교체해도 상대 알림은 남음`() =
        runTest {
            dao.replaceReminders("partner", "a", listOf(testReminder("p1", "a", 1, userId = "partner")))

            dao.replaceReminders(ME, "a", emptyList())

            assertEquals(listOf("p1"), dao.observeReminders("partner", "a").first().map { it.id })
        }

    @Test
    fun `알림은 당일 하루 전 일주일 전 순서로 나옴`() =
        runTest {
            dao.replaceReminders(
                ME,
                "a",
                listOf(testReminder("r7", "a", 7), testReminder("r0", "a", 0), testReminder("r1", "a", 1)),
            )

            assertEquals(listOf(0, 1, 7), dao.observeReminders(ME, "a").first().map { it.offsetDays })
        }
}
