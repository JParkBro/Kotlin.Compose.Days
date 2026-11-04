package jpark.bro.days.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.days.core.database.entity.SyncCursorEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SyncCursorDaoTest : DatabaseTest() {
    private val dao get() = db.syncCursorDao()

    @Test
    fun `커서가 없으면 null이고 저장하면 마이크로초 그대로 돌려줌`() =
        runTest {
            assertNull(dao.getCursor("items:$COUPLE"))

            dao.upsertCursor(SyncCursorEntity(key = "items:$COUPLE", lastPulledAtMicros = 1_793_836_800_000_123L))

            assertEquals(1_793_836_800_000_123L, dao.getCursor("items:$COUPLE"))
        }
}
