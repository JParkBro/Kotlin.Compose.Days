package jpark.bro.days.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.days.core.common.IdGenerator
import jpark.bro.days.core.data.DinnerPlan
import jpark.bro.days.core.data.DischargeDday
import jpark.bro.days.core.data.ME
import jpark.bro.days.core.data.MomBirthday
import jpark.bro.days.core.data.Seoul
import jpark.bro.days.core.data.Today
import jpark.bro.days.core.data.session.UserSession
import jpark.bro.days.core.database.DaysDatabase
import jpark.bro.days.core.model.ItemReminder
import jpark.bro.days.core.model.TimeSlot
import jpark.bro.days.core.model.Visibility
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class DefaultItemRepositoryTest {
    private lateinit var db: DaysDatabase
    private val clock = MutableClock(Today)
    private val session = SwitchableUserSession(ME)
    private var nextId = 1

    private val repository by lazy {
        DefaultItemRepository(
            itemDao = db.itemDao(),
            userItemPrefDao = db.userItemPrefDao(),
            itemReminderDao = db.itemReminderDao(),
            userSession = session,
            clock = clock,
            idGenerator = IdGenerator { "id-${nextId++}" },
        )
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
        db.query("SELECT sync_state FROM item WHERE id = '$id'", null).use {
            it.moveToFirst()
            it.getString(0)
        }

    @Test
    fun `새 항목을 저장하면 id 작성자 시각이 채워짐`() =
        runTest {
            val saved = repository.saveItem(id = null, content = DischargeDday).getOrThrow()

            assertEquals("id-1", saved.id)
            assertEquals(ME, saved.ownerUserId)
            assertEquals(ME, saved.updatedBy)
            assertEquals(Today.toEpochMilli(), saved.createdAt)
            assertEquals(Today.toEpochMilli(), saved.updatedAt)
            assertEquals(saved, repository.observeItem(saved.id).first())
        }

    @Test
    fun `수정하면 생성 시각은 그대로고 수정 시각만 바뀜`() =
        runTest {
            val created = repository.saveItem(id = null, content = DischargeDday).getOrThrow()
            clock.now = Today.plus(Duration.ofHours(1))

            val updated =
                repository
                    .saveItem(
                        id = created.id,
                        content = DischargeDday.copy(title = "전역 축하"),
                    ).getOrThrow()

            assertEquals(created.createdAt, updated.createdAt)
            assertEquals(Today.plus(Duration.ofHours(1)).toEpochMilli(), updated.updatedAt)
            assertEquals(
                "전역 축하",
                repository
                    .observeItem(created.id)
                    .first()
                    ?.content
                    ?.title,
            )
        }

    @Test
    fun `나만 보기 항목은 동기화 대상이 아니고 공유 항목은 PENDING`() =
        runTest {
            val private = repository.saveItem(id = null, content = DischargeDday).getOrThrow()
            val shared =
                repository
                    .saveItem(
                        id = null,
                        content = MomBirthday.copy(visibility = Visibility.SHARED, coupleId = "couple-1"),
                    ).getOrThrow()

            assertEquals("SYNCED", syncStateOf(private.id))
            assertEquals("PENDING", syncStateOf(shared.id))
        }

    @Test
    fun `제목이 비어 있으면 저장 실패`() =
        runTest {
            val result = repository.saveItem(id = null, content = DischargeDday.copy(title = "  "))

            assertTrue(result.isFailure)
            assertEquals(emptyList<Any>(), repository.observeItems().first())
        }

    @Test
    fun `디데이에 기준일이 없으면 저장 실패`() =
        runTest {
            assertTrue(repository.saveItem(id = null, content = DischargeDday.copy(date = null)).isFailure)
        }

    @Test
    fun `공유 항목에 커플 id가 없으면 저장 실패`() =
        runTest {
            assertTrue(
                repository.saveItem(id = null, content = DischargeDday.copy(visibility = Visibility.SHARED)).isFailure,
            )
        }

    @Test
    fun `시간 지정이 아닌데 시각이 있으면 저장 실패`() =
        runTest {
            assertTrue(repository.saveItem(id = null, content = DinnerPlan.copy(timeSlot = TimeSlot.EVENING)).isFailure)
        }

    @Test
    fun `없는 항목을 수정하면 실패`() =
        runTest {
            assertTrue(repository.saveItem(id = "missing", content = DischargeDday).isFailure)
        }

    @Test
    fun `삭제한 항목은 목록과 상세에서 사라지고 다시 수정할 수 없음`() =
        runTest {
            val saved = repository.saveItem(id = null, content = DischargeDday).getOrThrow()
            repository.saveItem(id = null, content = MomBirthday).getOrThrow()

            repository.deleteItem(saved.id).getOrThrow()

            assertEquals(listOf("엄마 생신"), repository.observeItems().first().map { it.content.title })
            assertNull(repository.observeItem(saved.id).first())
            assertTrue(repository.saveItem(id = saved.id, content = DischargeDday).isFailure)
        }

    @Test
    fun `공유 항목을 삭제하면 상대에게 전달하도록 PENDING`() =
        runTest {
            val shared =
                repository
                    .saveItem(
                        id = null,
                        content = MomBirthday.copy(visibility = Visibility.SHARED, coupleId = "couple-1"),
                    ).getOrThrow()
            db.openHelper.writableDatabase.execSQL("UPDATE item SET sync_state = 'SYNCED'")
            assertEquals("SYNCED", syncStateOf(shared.id))

            repository.deleteItem(shared.id).getOrThrow()

            assertEquals("PENDING", syncStateOf(shared.id))
        }

    @Test
    fun `대표를 바꾸면 이전 대표가 풀리고 해제하면 대표 없음`() =
        runTest {
            val a = repository.saveItem(id = null, content = DischargeDday).getOrThrow()
            val b = repository.saveItem(id = null, content = MomBirthday).getOrThrow()

            repository.setPinned(a.id, pinned = true).getOrThrow()
            repository.setPinned(b.id, pinned = true).getOrThrow()
            assertEquals(b.id, repository.observePinnedItemId().first())

            repository.setPinned(b.id, pinned = false).getOrThrow()
            assertNull(repository.observePinnedItemId().first())
        }

    @Test
    fun `알림을 바꾸면 중복 없이 당일부터 순서대로 저장됨`() =
        runTest {
            val saved = repository.saveItem(id = null, content = DischargeDday).getOrThrow()
            val nine = LocalTime.of(9, 0)

            repository.setReminders(saved.id, listOf(ItemReminder(7, nine), ItemReminder(0, nine))).getOrThrow()
            repository
                .setReminders(saved.id, listOf(ItemReminder(1, nine), ItemReminder(0, nine), ItemReminder(1, nine)))
                .getOrThrow()

            assertEquals(listOf(0, 1), repository.observeReminders(saved.id).first().map { it.offsetDays })
        }

    @Test
    fun `다른 계정으로 바꾸면 이전 계정의 항목이 보이지 않고 수정 삭제도 못함`() =
        runTest {
            val mine = repository.saveItem(id = null, content = DischargeDday).getOrThrow()

            session.userId.value = "user-other"

            assertEquals(emptyList<Any>(), repository.observeItems().first())
            assertNull(repository.observeItem(mine.id).first())
            assertTrue(repository.saveItem(id = mine.id, content = DischargeDday.copy(title = "남의 것")).isFailure)
            assertTrue(repository.deleteItem(mine.id).isFailure)

            session.userId.value = ME
            assertEquals(listOf(mine.id), repository.observeItems().first().map { it.id })
        }

    @Test
    fun `로그아웃 상태면 목록이 비고 저장은 실패함`() =
        runTest {
            repository.saveItem(id = null, content = DischargeDday).getOrThrow()

            session.userId.value = null

            assertEquals(emptyList<Any>(), repository.observeItems().first())
            assertTrue(repository.saveItem(id = null, content = MomBirthday).isFailure)
        }
}

/** 테스트 중 시각을 옮길 수 있는 시계 */
private class MutableClock(
    var now: Instant,
) : Clock() {
    override fun getZone(): ZoneId = Seoul

    override fun withZone(zone: ZoneId?): Clock = this

    override fun instant(): Instant = now
}

/** 로그인 계정을 바꿀 수 있는 세션 (null = 로그아웃) */
private class SwitchableUserSession(
    initial: String?,
) : UserSession {
    val userId = MutableStateFlow(initial)

    override fun observeUserId(): Flow<String?> = userId

    override suspend fun currentUserId(): String = checkNotNull(userId.value) { "로그인이 필요합니다." }
}
