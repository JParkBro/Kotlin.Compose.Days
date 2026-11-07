package jpark.bro.days.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.days.core.common.IdGenerator
import jpark.bro.days.core.data.ME
import jpark.bro.days.core.data.PARTNER
import jpark.bro.days.core.data.Today
import jpark.bro.days.core.database.DaysDatabase
import jpark.bro.days.core.model.ActivityContent
import jpark.bro.days.core.model.ActivityType
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.testing.FakeActivityNotifier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration

@RunWith(AndroidJUnit4::class)
class DefaultActivityRepositoryTest {
    private lateinit var db: DaysDatabase
    private val clock = MutableClock(Today)
    private val session = SwitchableUserSession(ME)
    private val notifier = FakeActivityNotifier()
    private var nextId = 1

    private val repository by lazy {
        DefaultActivityRepository(db.activityLogDao(), session, notifier, clock, IdGenerator { "id-${nextId++}" })
    }

    private val partnerAdded =
        ActivityContent(
            ActivityType.ITEM_ADDED,
            itemId = "dinner",
            itemType = ItemType.PLAN,
            itemTitle = "성수 저녁",
            actorUserId = PARTNER,
        )
    private val reminder =
        ActivityContent(
            ActivityType.REMINDER,
            itemId = "toeic",
            itemType = ItemType.DDAY,
            itemTitle = "토익",
            offsetDays = 7,
        )

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

    @Test
    fun `기록은 최신순으로 나오고 상대 활동만 시스템 알림으로 보냄`() =
        runTest {
            repository.record(listOf(reminder)).getOrThrow()
            clock.now = Today.plusSeconds(60)
            repository.record(listOf(partnerAdded)).getOrThrow()

            assertEquals(listOf(partnerAdded, reminder), repository.observeActivities().first().map { it.content })
            assertEquals(listOf(partnerAdded), notifier.notified)
        }

    @Test
    fun `30일이 지난 기록은 보이지 않고 새로 남길 때 지움`() =
        runTest {
            repository.record(listOf(reminder)).getOrThrow()
            clock.now = Today.plus(Duration.ofDays(31))

            assertTrue(repository.observeActivities().first().isEmpty())
            repository.record(listOf(partnerAdded)).getOrThrow()
            assertEquals(
                1,
                db
                    .activityLogDao()
                    .observeActivities(ME, since = 0)
                    .first()
                    .size,
            )
        }

    @Test
    fun `모두 읽음 처리`() =
        runTest {
            repository.record(listOf(reminder, partnerAdded)).getOrThrow()

            repository.markAllRead().getOrThrow()

            assertTrue(repository.observeActivities().first().all { it.isRead })
        }

    @Test
    fun `다른 계정의 기록은 보이지 않고 로그아웃이면 기록하지 않음`() =
        runTest {
            repository.record(listOf(reminder)).getOrThrow()

            session.userId.value = "user-other"
            assertTrue(repository.observeActivities().first().isEmpty())

            session.userId.value = null
            assertTrue(repository.record(listOf(reminder)).isFailure)
        }
}
