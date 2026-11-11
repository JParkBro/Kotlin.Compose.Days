package jpark.bro.days.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.days.core.common.IdGenerator
import jpark.bro.days.core.data.DischargeDday
import jpark.bro.days.core.data.ME
import jpark.bro.days.core.data.MomBirthday
import jpark.bro.days.core.data.Today
import jpark.bro.days.core.database.DaysDatabase
import jpark.bro.days.core.firebase.sync.BackupRemoteDataSource
import jpark.bro.days.core.model.ItemReminder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalTime
import jpark.bro.days.core.firebase.sync.BackupTooSoonException as RemoteTooSoon

/** 백업 · 복원 (product-spec 6장, 오늘 = 2026.11.05) */
@RunWith(AndroidJUnit4::class)
class DefaultBackupRepositoryTest {
    private lateinit var db: DaysDatabase
    private val clock = MutableClock(Today)
    private val session = SwitchableUserSession(ME)
    private val remote = FakeBackupRemote()
    private var nextId = 1

    private val items by lazy {
        DefaultItemRepository(
            itemDao = db.itemDao(),
            userItemPrefDao = db.userItemPrefDao(),
            itemReminderDao = db.itemReminderDao(),
            userSession = session,
            clock = clock,
            idGenerator = IdGenerator { "id-${nextId++}" },
        )
    }
    private val repository by lazy { DefaultBackupRepository(session, db.backupDao(), remote, clock) }

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
    fun `백업한 나만 보기 항목과 대표 고정 알림을 다른 기기에서 복원함`() =
        runTest {
            val discharge = items.saveItem(null, DischargeDday).getOrThrow()
            items.setPinned(discharge.id, pinned = true).getOrThrow()
            items.setReminders(discharge.id, listOf(ItemReminder(1, LocalTime.of(9, 0)))).getOrThrow()
            assertNull(repository.lastBackupAt().getOrThrow())
            assertTrue(repository.backup().isSuccess)
            assertEquals(Today.toEpochMilli(), repository.lastBackupAt().getOrThrow())

            // 로그아웃으로 기기 데이터가 지워진 뒤 (다른 기기와 같음)
            db.clearAllTables()
            val restored = repository.restore().getOrThrow()

            assertEquals(Today.toEpochMilli(), restored.createdAt)
            assertEquals(1, restored.itemCount)
            assertEquals(listOf("전역일"), items.observeItems().first().map { it.content.title })
            assertEquals(discharge.id, items.observePinnedItemId().first())
            assertEquals(listOf(1), items.observeReminders(discharge.id).first().map { it.offsetDays })
        }

    @Test
    fun `복원하면 백업 뒤에 만든 나만 보기 항목은 사라짐`() =
        runTest {
            items.saveItem(null, DischargeDday).getOrThrow()
            repository.backup().getOrThrow()
            items.saveItem(null, MomBirthday).getOrThrow()

            repository.restore().getOrThrow()

            assertEquals(listOf("전역일"), items.observeItems().first().map { it.content.title })
        }

    @Test
    fun `백업이 없으면 복원은 BackupNotFound로 실패하고 데이터를 건드리지 않음`() =
        runTest {
            items.saveItem(null, DischargeDday).getOrThrow()

            val result = repository.restore()

            assertTrue(result.exceptionOrNull() is BackupNotFoundException)
            assertEquals(1, items.observeItems().first().size)
        }

    @Test
    fun `10분 안에 다시 백업하면 BackupTooSoon으로 실패함`() =
        runTest {
            remote.tooSoon = true

            assertTrue(repository.backup().exceptionOrNull() is BackupTooSoonException)
        }

    @Test
    fun `사진은 백업하지 않음`() =
        runTest {
            items.saveItem(null, DischargeDday.copy(backgroundImageId = "photo-1")).getOrThrow()
            repository.backup().getOrThrow()
            db.clearAllTables()

            repository.restore().getOrThrow()

            assertNull(
                items
                    .observeItems()
                    .first()
                    .single()
                    .content.backgroundImageId,
            )
        }
}

private class FakeBackupRemote : BackupRemoteDataSource {
    private val files = mutableMapOf<String, ByteArray>()
    var tooSoon = false

    override suspend fun upload(
        userId: String,
        json: ByteArray,
    ) {
        if (tooSoon) throw RemoteTooSoon(IllegalStateException("규칙 거부 (테스트)"))
        files[userId] = json
    }

    override suspend fun download(userId: String): ByteArray? = files[userId]

    override suspend fun lastUploadedAt(userId: String): Long? = if (userId in files) Today.toEpochMilli() else null

    override suspend fun delete(userId: String) {
        files -= userId
    }
}
