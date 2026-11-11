package jpark.bro.days.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.days.core.database.entity.UserItemPrefEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/** 백업 · 복원: 이 계정의 나만 보기 항목과 사용자별 설정만 */
@RunWith(AndroidJUnit4::class)
internal class BackupDaoTest : DatabaseTest() {
    private val dao get() = db.backupDao()
    private val other = "user-other"

    private suspend fun seed() {
        db.coupleDao().upsertCouple(testCouple())
        db.itemDao().upsertItem(testItem("mine"))
        db.itemDao().upsertItem(testItem("deleted").copy(deletedAt = NOW))
        db.itemDao().upsertItem(testSharedItem("shared"))
        db.itemDao().upsertItem(testItem("other").copy(ownerUserId = other, updatedBy = other))
        db.userItemPrefDao().upsertPref(UserItemPrefEntity(ME, "shared", isPinned = true, updatedAt = NOW))
        db.userItemPrefDao().upsertPref(UserItemPrefEntity(other, "other", isPinned = true, updatedAt = NOW))
        db.itemReminderDao().insertReminders(
            listOf(testReminder("r1", "mine", 0), testReminder("r2", "other", 0, userId = other)),
        )
    }

    @Test
    fun `지우지 않은 내 나만 보기 항목과 내 설정만 읽음`() =
        runTest {
            seed()
            assertEquals(listOf("mine"), dao.getPrivateItems(ME).map { it.id })
            assertEquals(listOf("shared"), dao.getPrefs(ME).map { it.itemId })
            assertEquals(listOf("r1"), dao.getReminders(ME).map { it.id })
        }

    @Test
    fun `복원하면 내 나만 보기 항목과 설정만 백업 내용으로 바뀌고 공유 항목과 다른 계정은 그대로`() =
        runTest {
            seed()
            dao.replace(
                userId = ME,
                items = listOf(testItem("restored")),
                prefs = listOf(UserItemPrefEntity(ME, "restored", isPinned = true, updatedAt = NOW)),
                reminders = listOf(testReminder("r3", "restored", 7)),
            )

            assertEquals(listOf("restored"), dao.getPrivateItems(ME).map { it.id })
            assertEquals(listOf("restored"), dao.getPrefs(ME).map { it.itemId })
            assertEquals(listOf("r3"), dao.getReminders(ME).map { it.id })
            assertEquals("shared", db.itemDao().getItem("shared")?.id)
            // 지운 나만 보기 항목도 백업에 없으므로 함께 사라진다
            assertNull(db.itemDao().getItem("deleted"))
            assertEquals(listOf("other"), dao.getPrivateItems(other).map { it.id })
            assertEquals(listOf("other"), dao.getPrefs(other).map { it.itemId })
            assertEquals(listOf("r2"), dao.getReminders(other).map { it.id })
        }
}
