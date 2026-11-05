package jpark.bro.days.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CoupleItemDaoTest : DatabaseTest() {
    private val dao get() = db.coupleItemDao()
    private val disconnectedAt = NOW + 100
    private var nextId = 1

    private suspend fun disconnect() {
        db.coupleDao().upsertCouple(testCouple(status = "DISCONNECTED"))
        dao.copySharedItems(ME, COUPLE, copiedAt = disconnectedAt) { "copy-${nextId++}" }
    }

    @Test
    fun `해제하면 공유 항목이 내 나만 보기 사본이 되고 대표 고정 · 알림이 사본으로 옮겨감`() =
        runTest {
            db.coupleDao().upsertCouple(testCouple())
            db.itemDao().upsertItem(testSharedItem(id = "birthday", ownerUserId = PARTNER))
            db.userItemPrefDao().pinItem(userId = ME, itemId = "birthday", updatedAt = NOW)
            db.itemReminderDao().replaceReminders(ME, "birthday", listOf(testReminder("r1", "birthday", 1)))

            disconnect()

            val items = db.itemDao().observeItems(ME).first()
            assertEquals(listOf("copy-1"), items.map { it.id })
            val copy = items.single()
            assertEquals(ME, copy.ownerUserId)
            assertEquals("PRIVATE", copy.visibility)
            assertNull(copy.coupleId)
            assertEquals("birthday", copy.copiedFromItemId)
            assertEquals("copy-1", db.userItemPrefDao().observePinnedItemId(ME).first())
            assertEquals(
                listOf("copy-1"),
                db
                    .itemReminderDao()
                    .observeReminders(ME, "copy-1")
                    .first()
                    .map { it.itemId },
            )
        }

    @Test
    fun `재연결하면 고치지 않은 사본만 지우고 대표 고정을 원본으로 되돌림`() =
        runTest {
            db.coupleDao().upsertCouple(testCouple())
            db.itemDao().upsertItem(testSharedItem(id = "birthday", ownerUserId = PARTNER))
            db.itemDao().upsertItem(testSharedItem(id = "trip", ownerUserId = PARTNER))
            db.userItemPrefDao().pinItem(userId = ME, itemId = "birthday", updatedAt = NOW)
            disconnect()
            // 해제 뒤 trip 사본은 고쳤다
            val editedCopy = db.itemDao().getItem("copy-2")!!
            db.itemDao().upsertItem(editedCopy.copy(title = "제주 여행 (내 메모)", updatedAt = disconnectedAt + 1))

            db.coupleDao().upsertCouple(testCouple())
            dao.removeUnchangedCopies(ME, COUPLE, copiedAt = disconnectedAt, now = disconnectedAt + 2)

            val visible =
                db
                    .itemDao()
                    .observeItems(ME)
                    .first()
                    .map { it.id }
                    .toSet()
            assertEquals(setOf("birthday", "trip", "copy-2"), visible)
            assertEquals("birthday", db.userItemPrefDao().observePinnedItemId(ME).first())
        }
}
