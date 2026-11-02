package jpark.bro.days.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class UserItemPrefDaoTest : DatabaseTest() {
    private val dao get() = db.userItemPrefDao()

    @Test
    fun `다른 항목을 고정하면 이전 고정이 풀림`() =
        runTest {
            db.itemDao().upsertItem(testItem(id = "a"))
            db.itemDao().upsertItem(testItem(id = "b"))

            dao.pinItem(userId = ME, itemId = "a", updatedAt = NOW)
            dao.pinItem(userId = ME, itemId = "b", updatedAt = NOW + 1)

            assertEquals("b", dao.observePinnedItemId(ME).first())
            val pinnedCount = db.query("SELECT COUNT(*) FROM user_item_pref WHERE is_pinned = 1", null)
            pinnedCount.use {
                it.moveToFirst()
                assertEquals(1, it.getInt(0))
            }
        }

    @Test
    fun `고정은 사용자별로 따로 저장됨`() =
        runTest {
            db.itemDao().upsertItem(testItem(id = "a"))
            db.itemDao().upsertItem(testItem(id = "b"))

            dao.pinItem(userId = ME, itemId = "a", updatedAt = NOW)
            dao.pinItem(userId = "partner", itemId = "b", updatedAt = NOW)

            assertEquals("a", dao.observePinnedItemId(ME).first())
            assertEquals("b", dao.observePinnedItemId("partner").first())
        }

    @Test
    fun `고정한 항목이 삭제되면 대표 없음`() =
        runTest {
            db.itemDao().upsertItem(testItem(id = "a"))
            dao.pinItem(userId = ME, itemId = "a", updatedAt = NOW)

            db.itemDao().softDeleteItem(id = "a", deletedAt = NOW, updatedBy = ME, syncState = "PENDING")

            assertNull(dao.observePinnedItemId(ME).first())
        }
}
