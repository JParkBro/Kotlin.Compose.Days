package jpark.bro.days.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.days.core.database.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class UserDaoTest : DatabaseTest() {
    private val dao get() = db.userDao()

    private fun user(
        id: String = ME,
        nickname: String = "현우",
        updatedAt: Long = NOW,
        syncState: String = "SYNCED",
    ) = UserEntity(id, nickname, "1998-04-12", updatedAt, serverUpdatedAt = null, syncState = syncState)

    @Test
    fun `올릴 프로필은 내 행 중 PENDING과 FAILED만`() =
        runTest {
            dao.upsertUser(user(syncState = "PENDING"))
            dao.upsertUser(user(id = PARTNER, nickname = "지민", syncState = "PENDING"))

            assertEquals("현우", dao.getUnsyncedUser(ME)?.nickname)
            assertEquals(1, dao.observeUnsyncedCount(ME).first())

            dao.setSyncState(ME, NOW, "SYNCED")
            assertNull(dao.getUnsyncedUser(ME))
        }

    @Test
    fun `올리는 사이 다시 저장된 프로필은 SYNCED로 바꾸지 않음`() =
        runTest {
            dao.upsertUser(user(nickname = "현우2", updatedAt = NOW + 1, syncState = "PENDING"))

            dao.setSyncState(ME, NOW, "SYNCED")

            assertEquals("PENDING", dao.getUser(ME)?.syncState)
        }

    @Test
    fun `받은 프로필은 아직 올리지 않은 내 변경을 덮지 않음`() =
        runTest {
            dao.upsertUser(user(nickname = "내 수정", syncState = "PENDING"))

            dao.upsertRemoteUser(user(nickname = "서버 값"))
            dao.upsertRemoteUser(user(id = PARTNER, nickname = "지민", syncState = "PENDING"))

            assertEquals("내 수정", dao.getUser(ME)?.nickname)
            assertEquals("SYNCED", dao.observeUser(PARTNER).first()?.syncState)
        }
}
