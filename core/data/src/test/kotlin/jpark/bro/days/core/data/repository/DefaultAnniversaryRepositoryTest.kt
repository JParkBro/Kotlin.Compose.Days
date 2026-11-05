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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration

@RunWith(AndroidJUnit4::class)
class DefaultAnniversaryRepositoryTest {
    private lateinit var db: DaysDatabase
    private val clock = MutableClock(Today)
    private val session = SwitchableUserSession(ME)
    private val repository by lazy {
        DefaultAnniversaryRepository(db.anniversaryHideDao(), db.coupleDao(), session, clock)
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

    private suspend fun seedCouple(status: String = "ACTIVE") =
        db.coupleDao().upsertCouple(
            CoupleEntity(COUPLE, PARTNER, ME, "2025-06-14", true, status, "K7Q2MX", null, 0, 0, 0),
        )

    private suspend fun hide(ruleKey: String) = db.anniversaryHideDao().getHide(COUPLE, ruleKey)

    @Test
    fun `숨기면 숨긴 목록에 들어가고 올릴 대상(PENDING)이 됨`() =
        runTest {
            seedCouple()

            repository.setHidden("DAY_600", hidden = true).getOrThrow()

            assertEquals(setOf("DAY_600"), repository.observeHiddenRuleKeys().first())
            assertEquals("PENDING", hide("DAY_600")?.syncState)
            assertNull(hide("DAY_600")?.deletedAt)
        }

    @Test
    fun `숨김을 풀면 지우지 않고 해제 시각을 남김`() =
        runTest {
            seedCouple()
            repository.setHidden("YEAR_2", hidden = true).getOrThrow()
            clock.now = Today.plus(Duration.ofMinutes(1))

            repository.setHidden("YEAR_2", hidden = false).getOrThrow()

            assertEquals(emptySet<String>(), repository.observeHiddenRuleKeys().first())
            assertEquals(clock.millis(), hide("YEAR_2")?.deletedAt)
            assertEquals(Today.toEpochMilli(), hide("YEAR_2")?.createdAt)
        }

    @Test
    fun `이미 같은 상태면 아무것도 쓰지 않음`() =
        runTest {
            seedCouple()

            repository.setHidden("DAY_600", hidden = false).getOrThrow()

            assertNull(hide("DAY_600"))
        }

    @Test
    fun `연결된 커플이 없으면 숨길 수 없고 목록은 비어 있음`() =
        runTest {
            assertTrue(repository.setHidden("DAY_600", hidden = true).isFailure)

            seedCouple()
            repository.setHidden("DAY_600", hidden = true).getOrThrow()
            seedCouple(status = "DISCONNECTED")

            assertEquals(emptySet<String>(), repository.observeHiddenRuleKeys().first())
        }
}
