package jpark.bro.days.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CoupleDaoTest : DatabaseTest() {
    private val dao get() = db.coupleDao()

    @Test
    fun `두 멤버 모두 연결된 커플을 찾음`() =
        runTest {
            dao.upsertCouple(testCouple())

            assertEquals(COUPLE, dao.observeActiveCouple(ME).first()?.id)
            assertEquals(COUPLE, dao.getActiveCouple(PARTNER)?.id)
            assertNull(dao.getActiveCouple("user-other"))
        }

    @Test
    fun `연결 해제된 커플은 연결된 커플이 아님`() =
        runTest {
            dao.upsertCouple(testCouple(status = "DISCONNECTED"))

            assertNull(dao.observeActiveCouple(ME).first())
            assertEquals("DISCONNECTED", dao.getCouple(COUPLE)?.status)
        }
}
