package jpark.bro.days.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.days.core.data.COUPLE
import jpark.bro.days.core.data.ME
import jpark.bro.days.core.data.PARTNER
import jpark.bro.days.core.data.Today
import jpark.bro.days.core.database.DaysDatabase
import jpark.bro.days.core.database.entity.CoupleEntity
import jpark.bro.days.core.database.entity.UserEntity
import jpark.bro.days.core.model.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class DefaultProfileRepositoryTest {
    private lateinit var db: DaysDatabase
    private val session = SwitchableUserSession(ME)
    private val repository by lazy {
        DefaultProfileRepository(db.userDao(), db.coupleDao(), session, MutableClock(Today))
    }

    @Before
    fun setUp() {
        db =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), DaysDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun tearDown() = db.close()

    private fun syncStateOf(id: String): String =
        db.query("SELECT sync_state FROM user WHERE id = '$id'", null).use {
            it.moveToFirst()
            it.getString(0)
        }

    @Test
    fun `저장하면 앞뒤 공백을 지우고 올릴 대상(PENDING)이 됨`() =
        runTest {
            repository.saveMyProfile("  현우 ", LocalDate.of(1998, 4, 12), null).getOrThrow()

            assertEquals(UserProfile(ME, "현우", LocalDate.of(1998, 4, 12)), repository.observeMyProfile().first())
            assertEquals(null, repository.observeMyProfile().first()?.profileImageId)
            assertEquals("PENDING", syncStateOf(ME))
        }

    @Test
    fun `생일은 비워 둘 수 있음`() =
        runTest {
            repository.saveMyProfile("현우", null, null).getOrThrow()

            assertNull(repository.observeMyProfile().first()?.birthDate)
        }

    @Test
    fun `이름이 비었거나 10자를 넘거나 생일이 오늘 이후면 실패`() =
        runTest {
            assertTrue(repository.saveMyProfile("   ", null, null).isFailure)
            assertTrue(repository.saveMyProfile("가".repeat(11), null, null).isFailure)
            assertTrue(repository.saveMyProfile("현우", LocalDate.of(2026, 11, 6), null).isFailure)
            assertTrue(repository.saveMyProfile("가".repeat(10), LocalDate.of(2026, 11, 5), null).isSuccess)
        }

    @Test
    fun `로그아웃 상태면 내 프로필이 없고 저장은 실패`() =
        runTest {
            repository.saveMyProfile("현우", null, null).getOrThrow()
            session.userId.value = null

            assertNull(repository.observeMyProfile().first())
            assertTrue(repository.saveMyProfile("현우", null, null).isFailure)
        }

    @Test
    fun `연결된 커플이 있을 때만 상대 프로필을 보여줌`() =
        runTest {
            db.userDao().upsertUser(UserEntity(PARTNER, "지민", "1999-09-28", 0, 0, "SYNCED"))
            assertNull(repository.observePartnerProfile().first())

            db.coupleDao().upsertCouple(couple(status = "ACTIVE"))
            assertEquals("지민", repository.observePartnerProfile().first()?.nickname)

            db.coupleDao().upsertCouple(couple(status = "DISCONNECTED"))
            assertNull(repository.observePartnerProfile().first())
        }

    @Test
    fun `상대가 쓴 생일 형식이 틀리면 생일 없음`() =
        runTest {
            db.coupleDao().upsertCouple(couple(status = "ACTIVE"))
            db.userDao().upsertUser(UserEntity(PARTNER, "지민", "9월 28일", 0, 0, "SYNCED"))

            assertNull(repository.observePartnerProfile().first()?.birthDate)
        }

    private fun couple(status: String) =
        CoupleEntity(COUPLE, PARTNER, ME, "2025-06-14", true, status, "K7Q2MX", null, 0, 0, 0)

    @Test
    fun `프로필 사진 id를 저장하고 지울 수 있음`() =
        runTest {
            repository.saveMyProfile("현우", null, "photo-1").getOrThrow()
            assertEquals("photo-1", repository.observeMyProfile().first()?.profileImageId)

            repository.saveMyProfile("현우", null, null).getOrThrow()
            assertNull(repository.observeMyProfile().first()?.profileImageId)
        }
}
