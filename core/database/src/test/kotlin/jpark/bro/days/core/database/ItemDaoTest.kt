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

    @Test
    fun `연결된 커플의 공유 항목은 상대가 만든 것도 보임`() =
        runTest {
            db.coupleDao().upsertCouple(testCouple())
            dao.upsertItem(testItem(id = "mine"))
            dao.upsertItem(testSharedItem(id = "birthday", ownerUserId = PARTNER))

            assertEquals(listOf("birthday", "mine"), dao.observeItems(ME).first().map { it.id })
            assertEquals("birthday", dao.observeItem("birthday", ME).first()?.id)
            assertEquals(listOf("birthday"), dao.observeItems(PARTNER).first().map { it.id })
        }

    @Test
    fun `연결 해제된 커플이나 남의 커플 공유 항목은 보이지 않음`() =
        runTest {
            db.coupleDao().upsertCouple(testCouple(status = "DISCONNECTED"))
            db.coupleDao().upsertCouple(testCouple(id = "couple-other", member1 = "user-x", member2 = "user-y"))
            dao.upsertItem(testSharedItem(id = "old", ownerUserId = PARTNER))
            dao.upsertItem(testSharedItem(id = "others", ownerUserId = "user-x").copy(coupleId = "couple-other"))

            assertEquals(emptyList<String>(), dao.observeItems(ME).first().map { it.id })
            assertNull(dao.observeItem("others", ME).first())
        }

    @Test
    fun `커플이 연결되면 공유 항목 목록이 다시 나옴`() =
        runTest {
            dao.upsertItem(testSharedItem(id = "birthday", ownerUserId = PARTNER))
            assertEquals(emptyList<String>(), dao.observeItems(ME).first().map { it.id })

            db.coupleDao().upsertCouple(testCouple())

            assertEquals(listOf("birthday"), dao.observeItems(ME).first().map { it.id })
        }

    @Test
    fun `올릴 항목은 내가 고친 연결된 커플의 공유 항목 중 PENDING과 FAILED만`() =
        runTest {
            db.coupleDao().upsertCouple(testCouple())
            dao.upsertItem(testItem(id = "private").copy(syncState = "PENDING"))
            dao.upsertItem(testSharedItem(id = "pending", syncState = "PENDING"))
            dao.upsertItem(testSharedItem(id = "failed", syncState = "FAILED"))
            dao.upsertItem(testSharedItem(id = "synced", syncState = "SYNCED"))
            dao.upsertItem(testSharedItem(id = "partner", ownerUserId = PARTNER, syncState = "PENDING"))

            assertEquals(
                setOf("pending", "failed"),
                db
                    .itemSyncDao()
                    .getUnsyncedSharedItems(ME)
                    .map { it.id }
                    .toSet(),
            )
            assertEquals(2, db.itemSyncDao().observeUnsyncedCount(ME).first())
            assertEquals(
                setOf("partner"),
                db
                    .itemSyncDao()
                    .getUnsyncedSharedItems(PARTNER)
                    .map { it.id }
                    .toSet(),
            )
        }

    @Test
    fun `연결 해제된 커플의 항목은 올리지 않음`() =
        runTest {
            db.coupleDao().upsertCouple(testCouple(status = "DISCONNECTED"))
            dao.upsertItem(testSharedItem(id = "pending", syncState = "PENDING"))

            assertEquals(emptyList<String>(), db.itemSyncDao().getUnsyncedSharedItems(ME).map { it.id })
        }

    @Test
    fun `올리는 사이 다시 저장된 항목은 SYNCED로 바꾸지 않음`() =
        runTest {
            dao.upsertItem(testSharedItem(id = "a", syncState = "PENDING"))
            dao.upsertItem(testSharedItem(id = "a", syncState = "PENDING").copy(updatedAt = NOW + 5))

            db.itemSyncDao().setSyncState(id = "a", updatedAt = NOW, syncState = "SYNCED")
            assertEquals("PENDING", dao.getItem("a")?.syncState)

            db.itemSyncDao().setSyncState(id = "a", updatedAt = NOW + 5, syncState = "SYNCED")
            assertEquals("SYNCED", dao.getItem("a")?.syncState)
        }

    @Test
    fun `받은 변경은 덮어쓰지만 아직 올리지 않은 내 변경은 지킴`() =
        runTest {
            dao.upsertItem(testSharedItem(id = "synced", title = "옛 제목"))
            dao.upsertItem(testSharedItem(id = "pending", title = "내 수정", syncState = "PENDING"))

            db.itemSyncDao().upsertRemoteItems(
                listOf(
                    testSharedItem(id = "synced", title = "상대 수정", syncState = "PENDING"),
                    testSharedItem(id = "pending", title = "상대 수정"),
                    testSharedItem(id = "new", ownerUserId = PARTNER, deletedAt = NOW),
                ),
            )

            assertEquals("상대 수정", dao.getItem("synced")?.title)
            assertEquals("SYNCED", dao.getItem("synced")?.syncState)
            assertEquals("내 수정", dao.getItem("pending")?.title)
            assertEquals(NOW, dao.getItem("new")?.deletedAt)
        }
}
