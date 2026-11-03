package jpark.bro.days.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ItemDaoTest : DatabaseTest() {
    private val dao get() = db.itemDao()

    @Test
    fun `삭제된 항목은 목록에 나오지 않음`() =
        runTest {
            dao.upsertItem(testItem(id = "a"))
            dao.upsertItem(testItem(id = "b", deletedAt = NOW))

            assertEquals(listOf("a"), dao.observeItems(ME).first().map { it.id })
        }

    @Test
    fun `날짜순으로 정렬하고 날짜 없는 일정은 맨 뒤`() =
        runTest {
            dao.upsertItem(testItem(id = "no-date", date = null))
            dao.upsertItem(testItem(id = "discharge", date = "2027-03-19"))
            dao.upsertItem(testItem(id = "mom", date = "2027-01-02"))
            dao.upsertItem(testItem(id = "quit", date = "2026-08-07"))

            assertEquals(
                listOf("quit", "mom", "discharge", "no-date"),
                dao.observeItems(ME).first().map { it.id },
            )
        }

    @Test
    fun `soft delete하면 행은 남고 삭제 시각과 동기화 상태가 바뀜`() =
        runTest {
            dao.upsertItem(testItem(id = "a").copy(syncState = "SYNCED"))

            dao.softDeleteItem(id = "a", deletedAt = NOW + 1, updatedBy = ME, syncState = "PENDING")

            assertNull(dao.observeItem("a", ME).first())
            val row = db.query("SELECT deleted_at, updated_at, sync_state FROM item WHERE id = 'a'", null)
            row.use {
                it.moveToFirst()
                assertEquals(NOW + 1, it.getLong(0))
                assertEquals(NOW + 1, it.getLong(1))
                assertEquals("PENDING", it.getString(2))
            }
        }

    @Test
    fun `같은 id로 저장하면 덮어씀`() =
        runTest {
            dao.upsertItem(testItem(id = "a", title = "전역일"))
            dao.upsertItem(testItem(id = "a", title = "전역 축하"))

            assertEquals("전역 축하", dao.observeItem("a", ME).first()?.title)
        }

    @Test
    fun `다른 계정의 항목은 목록과 상세에 나오지 않음`() =
        runTest {
            dao.upsertItem(testItem(id = "mine"))
            dao.upsertItem(testItem(id = "other").copy(ownerUserId = "user-other"))

            assertEquals(listOf("mine"), dao.observeItems(ME).first().map { it.id })
            assertNull(dao.observeItem("other", ME).first())
            assertEquals(listOf("other"), dao.observeItems("user-other").first().map { it.id })
        }
}
