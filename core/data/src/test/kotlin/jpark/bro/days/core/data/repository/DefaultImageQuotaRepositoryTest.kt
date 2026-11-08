package jpark.bro.days.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.days.core.data.COUPLE
import jpark.bro.days.core.data.ME
import jpark.bro.days.core.data.PARTNER
import jpark.bro.days.core.database.DaysDatabase
import jpark.bro.days.core.database.entity.CoupleEntity
import jpark.bro.days.core.model.ImageUploadQuota
import jpark.bro.days.core.testing.FakeImageRemoteDataSource
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultImageQuotaRepositoryTest {
    private lateinit var db: DaysDatabase
    private val remote = FakeImageRemoteDataSource()
    private val repository by lazy { DefaultImageQuotaRepository(db.coupleDao(), remote, SwitchableUserSession(ME)) }

    @Before
    fun setUp() {
        db =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), DaysDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `커플이 아니면 사진 한도가 없음`() =
        runTest {
            assertNull(repository.uploadQuota().getOrThrow())
        }

    @Test
    fun `연결된 커플이면 서버의 등록 수와 bonus로 한도를 계산`() =
        runTest {
            db.coupleDao().upsertCouple(
                CoupleEntity(COUPLE, PARTNER, ME, "2025-06-14", true, "ACTIVE", "K7Q2MX", null, 0, 0, 0),
            )
            remote.counts[COUPLE] = 31
            remote.bonuses[COUPLE] = 1

            val quota = repository.uploadQuota().getOrThrow()
            assertEquals(ImageUploadQuota(ME, COUPLE, used = 31, bonus = 1), quota)
            assertEquals(0, quota?.remaining)
        }

    @Test
    fun `보상이 반영되면 늘어난 한도, 반영되지 않으면 실패`() =
        runTest {
            val quota = ImageUploadQuota(ME, COUPLE, used = 30, bonus = 0)
            remote.counts[COUPLE] = 30
            assertTrue(repository.awaitRewardedQuota(quota).isFailure)

            remote.bonuses[COUPLE] = 1
            assertEquals(1, repository.awaitRewardedQuota(quota).getOrThrow().remaining)
        }
}
