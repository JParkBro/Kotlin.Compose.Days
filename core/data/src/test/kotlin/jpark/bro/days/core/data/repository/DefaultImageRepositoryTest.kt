package jpark.bro.days.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.days.core.common.IdGenerator
import jpark.bro.days.core.data.COUPLE
import jpark.bro.days.core.data.ME
import jpark.bro.days.core.data.PARTNER
import jpark.bro.days.core.data.Today
import jpark.bro.days.core.data.image.ImageStore
import jpark.bro.days.core.database.DaysDatabase
import jpark.bro.days.core.database.entity.ImageEntity
import jpark.bro.days.core.testing.FakeImageRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultImageRepositoryTest {
    private lateinit var db: DaysDatabase
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val store = ImageStore(context, Dispatchers.Unconfined)
    private val remote = FakeImageRemoteDataSource()
    private val repository by lazy {
        DefaultImageRepository(
            db.imageDao(),
            remote,
            SwitchableUserSession(ME),
            store,
            MutableClock(Today),
            IdGenerator { "unused" },
        )
    }

    @Before
    fun setUp() {
        db =
            Room
                .inMemoryDatabaseBuilder(context, DaysDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun tearDown() {
        db.close()
        store.file("images").deleteRecursively()
    }

    private fun image(
        id: String,
        owner: String = ME,
        remoteCoupleId: String? = null,
        localPath: String? = store.relativePath(id),
    ) = ImageEntity(id, owner, remoteCoupleId, localPath, "NONE", 1080, 720, 3, 0, null)

    private fun writeLocal(id: String) =
        store.file(store.relativePath(id)).apply {
            parentFile?.mkdirs()
            writeBytes(byteArrayOf(1, 2, 3))
        }

    @Test
    fun `기기에 있는 사진은 바로 파일을 줌`() =
        runTest {
            writeLocal("bg")
            db.imageDao().upsertImage(image("bg"))

            assertEquals(store.file(store.relativePath("bg")), repository.observeImageFile("bg").first())
        }

    @Test
    fun `상대 사진은 처음 한 번 받아 기기에 저장`() =
        runTest {
            remote.files["$COUPLE/partner-bg"] = byteArrayOf(9, 9)
            db.imageSyncDao().insertRemoteImage(
                image("partner-bg", owner = PARTNER, remoteCoupleId = COUPLE, localPath = null),
            )

            val file = repository.observeImageFile("partner-bg").filterNotNull().first()

            assertArrayEquals(byteArrayOf(9, 9), file.readBytes())
            assertEquals(store.relativePath("partner-bg"), db.imageDao().getImage("partner-bg")?.localPath)
        }

    @Test
    fun `서버에 없는 사진은 null이고 다시 요청하지 않음`() =
        runTest {
            db.imageSyncDao().insertRemoteImage(
                image("gone", owner = PARTNER, remoteCoupleId = COUPLE, localPath = null),
            )

            assertNull(repository.observeImageFile("gone").first())
            remote.files["$COUPLE/gone"] = byteArrayOf(1)
            assertNull(repository.observeImageFile("gone").first())
        }

    @Test
    fun `올리지 않은 사진을 버리면 파일과 행을 바로 지움`() =
        runTest {
            val file = writeLocal("local")
            db.imageDao().upsertImage(image("local"))

            repository.discard("local").getOrThrow()

            assertFalse(file.exists())
            assertNull(db.imageDao().getImage("local"))
        }

    @Test
    fun `내가 올린 사진을 버리면 서버 파일을 지우도록 표시만 함`() =
        runTest {
            writeLocal("uploaded")
            db.imageDao().upsertImage(image("uploaded", remoteCoupleId = COUPLE))

            repository.discard("uploaded").getOrThrow()

            assertEquals(listOf("uploaded"), db.imageSyncDao().getImagesToDelete(ME).map { it.id })
            assertNull(repository.observeImageFile("uploaded").first())
        }
}
