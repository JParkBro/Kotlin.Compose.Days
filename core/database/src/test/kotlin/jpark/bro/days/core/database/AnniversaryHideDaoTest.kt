package jpark.bro.days.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AnniversaryHideDaoTest : DatabaseTest() {
    private val dao get() = db.anniversaryHideDao()

    @Test
    fun `숨김 해제(deleted_at)한 기념일은 숨긴 목록에서 빠짐`() =
        runTest {
            dao.upsertHide(testHide("DAY_600"))
            dao.upsertHide(testHide("YEAR_2", deletedAt = NOW))

            assertEquals(listOf("DAY_600"), dao.observeHiddenRuleKeys(COUPLE).first())
        }

    @Test
    fun `올릴 것은 PENDING과 FAILED만`() =
        runTest {
            dao.upsertHide(testHide("DAY_600", syncState = "PENDING"))
            dao.upsertHide(testHide("YEAR_2", syncState = "FAILED"))
            dao.upsertHide(testHide("DAY_700"))

            assertEquals(setOf("DAY_600", "YEAR_2"), dao.getUnsyncedHides().map { it.ruleKey }.toSet())
        }

    @Test
    fun `올리는 사이 숨김 해제된 행은 SYNCED로 바꾸지 않음`() =
        runTest {
            dao.upsertHide(testHide("DAY_600", deletedAt = NOW, syncState = "PENDING"))

            dao.setSyncState(COUPLE, "DAY_600", deletedAt = null, syncState = "SYNCED")
            assertEquals("PENDING", dao.getHide(COUPLE, "DAY_600")?.syncState)

            dao.setSyncState(COUPLE, "DAY_600", deletedAt = NOW, syncState = "SYNCED")
            assertEquals("SYNCED", dao.getHide(COUPLE, "DAY_600")?.syncState)
        }

    @Test
    fun `받은 변경은 아직 올리지 않은 내 변경을 덮지 않음`() =
        runTest {
            dao.upsertHide(testHide("DAY_600", syncState = "PENDING"))

            dao.upsertRemoteHides(
                listOf(testHide("DAY_600", deletedAt = NOW), testHide("YEAR_2", syncState = "PENDING")),
            )

            assertEquals(null, dao.getHide(COUPLE, "DAY_600")?.deletedAt)
            assertEquals("SYNCED", dao.getHide(COUPLE, "YEAR_2")?.syncState)
        }
}
