package jpark.bro.days.core.sync

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.days.core.common.IdGenerator
import jpark.bro.days.core.data.couple.CoupleTransitions
import jpark.bro.days.core.data.image.ImageStore
import jpark.bro.days.core.database.DaysDatabase
import jpark.bro.days.core.database.entity.ImageEntity
import jpark.bro.days.core.database.entity.UserEntity
import jpark.bro.days.core.firebase.sync.ProfileDto
import jpark.bro.days.core.testing.FakeActivityRepository
import jpark.bro.days.core.testing.FakeCoupleRemoteDataSource
import jpark.bro.days.core.testing.FakeImageRemoteDataSource
import jpark.bro.days.core.testing.testSettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock

/** 사진 올리기 · 지우기와 사진을 가리키는 문서의 순서 (infra-and-billing-protection.md 2-2 · 2-3) */
@RunWith(AndroidJUnit4::class)
class ImageSyncTest {
    private lateinit var db: DaysDatabase
    private val remote = FakeSyncRemoteDataSource()
    private val profileRemote = FakeProfileRemoteDataSource()
    private val imageRemote = FakeImageRemoteDataSource()
    private val activities = FakeActivityRepository()
    private val uploadEnabled = MutableStateFlow(true)
    private val store = ImageStore(ApplicationProvider.getApplicationContext(), Dispatchers.Unconfined)
    private val couples by lazy {
        CoupleTransitions(
            db.coupleDao(),
            db.coupleItemDao(),
            FakeCoupleRemoteDataSource(),
            testSettingsDataStore(),
            Clock.systemUTC(),
            IdGenerator { "copy" },
        )
    }
    private val coupleData by lazy {
        CoupleDataSync(
            remote,
            db.itemSyncDao(),
            db.anniversaryHideDao(),
            db.syncCursorDao(),
            db.imageSyncDao(),
            activities,
        )
    }
    private val engine by lazy {
        SyncEngine(
            remote,
            couples,
            coupleData,
            ProfileSync(profileRemote, db.userDao(), db.imageSyncDao()),
            ImageSync(imageRemote, db.imageSyncDao(), db.userDao(), store, uploadEnabled),
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
    fun tearDown() {
        db.close()
        store.file("images").deleteRecursively()
    }

    private suspend fun connectCouple() = couples.apply(ME, listOf(coupleDto()))

    /** 기기에 저장한 내 사진 */
    private suspend fun myImage(
        id: String,
        remoteCoupleId: String? = null,
        deletedAt: Long? = null,
    ) {
        val path = store.relativePath(id)
        store.file(path).apply {
            parentFile?.mkdirs()
            writeBytes(byteArrayOf(1, 2, 3))
        }
        db.imageDao().upsertImage(ImageEntity(id, ME, remoteCoupleId, path, "NONE", 1, 1, 3, NOW, deletedAt))
    }

    private suspend fun sharedItemWithImage(
        id: String,
        imageId: String,
    ) = db.itemDao().upsertItem(mySharedItem(id).copy(backgroundImageId = imageId))

    @Test
    fun `공유 항목의 배경 사진을 먼저 올리고 항목을 올림`() =
        runTest {
            connectCouple()
            myImage("bg")
            sharedItemWithImage("a", "bg")

            assertTrue(engine.push(ME))

            assertTrue("$COUPLE/bg" in imageRemote.files)
            assertEquals(1, imageRemote.counts[COUPLE])
            assertEquals(COUPLE, db.imageDao().getImage("bg")?.remoteCoupleId)
            assertEquals(
                "bg",
                remote.pushedItems
                    .single()
                    .second
                    .single()
                    .backgroundImageId,
            )
            assertEquals(0, engine.observeUnsyncedCount(ME).first())
        }

    @Test
    fun `나만 보기 항목의 사진은 올리지 않음`() =
        runTest {
            connectCouple()
            myImage("private-bg")
            db.itemDao().upsertItem(
                mySharedItem("p").copy(visibility = "PRIVATE", coupleId = null, backgroundImageId = "private-bg"),
            )

            assertTrue(engine.push(ME))

            assertEquals(emptyMap<String, ByteArray>(), imageRemote.files)
            assertNull(db.imageDao().getImage("private-bg")?.remoteCoupleId)
        }

    @Test
    fun `한도를 다 썼으면 사진은 이 기기에만 두고 항목은 올림`() =
        runTest {
            connectCouple()
            imageRemote.counts[COUPLE] = 30
            myImage("bg")
            sharedItemWithImage("a", "bg")

            assertTrue(engine.push(ME))

            assertEquals("REJECTED", db.imageDao().getImage("bg")?.uploadState)
            assertEquals(
                listOf("a"),
                remote.pushedItems
                    .single()
                    .second
                    .map { it.id },
            )
            assertEquals(0, engine.observeUnsyncedCount(ME).first())
        }

    @Test
    fun `사진 올리기에 실패하면 항목도 기다리고 다시 시도하면 카운터를 또 쓰지 않음`() =
        runTest {
            connectCouple()
            myImage("bg")
            sharedItemWithImage("a", "bg")
            imageRemote.failure = IllegalStateException("network")

            assertFalse(engine.push(ME))
            assertEquals(emptyList<Any>(), remote.pushedItems)
            assertEquals("PENDING", db.itemDao().getItem("a")?.syncState)

            imageRemote.failure = null
            assertTrue(engine.push(ME))
            assertEquals(1, imageRemote.counts[COUPLE])
            assertEquals(
                listOf("a"),
                remote.pushedItems
                    .single()
                    .second
                    .map { it.id },
            )
        }

    @Test
    fun `업로드 스위치가 꺼지면 사진과 그 항목은 기다리고 나머지는 올림`() =
        runTest {
            connectCouple()
            uploadEnabled.value = false
            myImage("bg")
            sharedItemWithImage("a", "bg")
            db.itemDao().upsertItem(mySharedItem("b"))

            assertTrue(engine.push(ME))

            assertEquals(emptyMap<String, ByteArray>(), imageRemote.files)
            assertEquals(
                listOf("b"),
                remote.pushedItems
                    .single()
                    .second
                    .map { it.id },
            )
            assertEquals("PENDING", db.itemDao().getItem("a")?.syncState)
        }

    @Test
    fun `프로필 사진은 커플이 연결되면 올리고 그 뒤에 프로필 문서에 사진 id를 적음`() =
        runTest {
            myImage("profile")
            db.userDao().upsertUser(UserEntity(ME, "현우", null, NOW, null, "PENDING", profileImageId = "profile"))

            assertTrue(engine.push(ME))
            assertNull(profileRemote.pushed.last().profileImageId)

            connectCouple()
            assertEquals(1, engine.observeUnsyncedCount(ME).first())
            assertTrue(engine.push(ME))

            assertTrue("$COUPLE/profile" in imageRemote.files)
            assertEquals("profile", profileRemote.pushed.last().profileImageId)
            assertEquals(0, engine.observeUnsyncedCount(ME).first())
        }

    @Test
    fun `받은 항목 · 상대 프로필의 사진은 받을 수 있게 알아 둠`() =
        runTest {
            connectCouple()
            coupleData.applyItems(COUPLE, listOf(itemDto("a").copy(backgroundImageId = "partner-bg")), ME)
            profileRemote.profiles[PARTNER] =
                ProfileDto(PARTNER, "지민", null, NOW, profileImageId = "partner-face", coupleId = COUPLE)
            remote.remoteCouples = listOf(coupleDto())

            assertTrue(engine.pullOnce(ME))

            assertEquals(COUPLE, db.imageDao().getImage("partner-bg")?.remoteCoupleId)
            assertEquals(PARTNER, db.imageDao().getImage("partner-face")?.ownerUserId)
            assertEquals("partner-face", db.userDao().getUser(PARTNER)?.profileImageId)
            assertEquals(0, engine.observeUnsyncedCount(ME).first())
        }

    @Test
    fun `버린 내 사진은 서버 파일을 지우고 행도 지움`() =
        runTest {
            imageRemote.files["$COUPLE/old"] = byteArrayOf(1)
            myImage("old", remoteCoupleId = COUPLE, deletedAt = NOW)

            assertTrue(engine.push(ME))

            assertFalse("$COUPLE/old" in imageRemote.files)
            assertNull(db.imageDao().getImage("old"))
        }
}
