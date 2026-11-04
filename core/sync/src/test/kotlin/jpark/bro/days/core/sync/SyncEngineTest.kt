package jpark.bro.days.core.sync

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.days.core.common.IdGenerator
import jpark.bro.days.core.data.couple.CoupleTransitions
import jpark.bro.days.core.database.DaysDatabase
import jpark.bro.days.core.database.entity.UserEntity
import jpark.bro.days.core.firebase.sync.ProfileDto
import jpark.bro.days.core.testing.FakeCoupleRemoteDataSource
import jpark.bro.days.core.testing.testSettingsDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SyncEngineTest {
    private lateinit var db: DaysDatabase
    private val remote = FakeSyncRemoteDataSource()
    private val profileRemote = FakeProfileRemoteDataSource()
    private val couples by lazy {
        CoupleTransitions(
            db.coupleDao(),
            db.coupleItemDao(),
            FakeCoupleRemoteDataSource(),
            testSettingsDataStore(),
            Clock.systemUTC(),
            IdGenerator { "copy-${nextCopyId++}" },
        )
    }
    private var nextCopyId = 1
    private val engine by lazy {
        SyncEngine(
            remote,
            db.itemSyncDao(),
            db.anniversaryHideDao(),
            couples,
            db.syncCursorDao(),
            ProfileSync(profileRemote, db.userDao()),
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

    private suspend fun connectCouple() = couples.apply(ME, listOf(coupleDto()))

    private suspend fun syncStateOf(id: String) = db.itemDao().getItem(id)?.syncState

    @Test
    fun `올리면 공유 항목이 커플 경로로 가고 SYNCED가 됨`() =
        runTest {
            connectCouple()
            db.itemDao().upsertItem(mySharedItem("a"))
            db.anniversaryHideDao().upsertHide(myHide("DAY_600"))

            assertTrue(engine.push(ME))

            assertEquals(
                listOf(COUPLE to listOf("a")),
                remote.pushedItems.map { (c, items) -> c to items.map { it.id } },
            )
            assertEquals(
                listOf("DAY_600"),
                remote.pushedHides
                    .single()
                    .second
                    .map { it.ruleKey },
            )
            assertEquals("SYNCED", syncStateOf("a"))
            assertEquals("SYNCED", db.anniversaryHideDao().getHide(COUPLE, "DAY_600")?.syncState)
            assertEquals(0, engine.observeUnsyncedCount(ME).first())
        }

    @Test
    fun `올리기에 실패하면 FAILED로 두고 false`() =
        runTest {
            connectCouple()
            db.itemDao().upsertItem(mySharedItem("a"))
            remote.failPush = true

            assertFalse(engine.push(ME))

            assertEquals("FAILED", syncStateOf("a"))
            assertEquals(1, engine.observeUnsyncedCount(ME).first())
        }

    @Test
    fun `올리는 사이 다시 저장한 항목은 다음 차례에 함께 올림`() =
        runTest {
            connectCouple()
            db.itemDao().upsertItem(mySharedItem("a"))
            var resaved = false
            remote.duringPush = {
                if (!resaved) {
                    resaved = true
                    db.itemDao().upsertItem(mySharedItem("a").copy(title = "지민 생일 🎂", updatedAt = NOW + 1))
                }
            }

            assertTrue(engine.push(ME))

            assertEquals(listOf("지민 생일", "지민 생일 🎂"), remote.pushedItems.map { it.second.single().title })
            assertEquals("SYNCED", syncStateOf("a"))
        }

    @Test
    fun `연결된 커플이 없으면 올리지 않음`() =
        runTest {
            db.itemDao().upsertItem(mySharedItem("a"))

            assertTrue(engine.push(ME))

            assertEquals(emptyList<Any>(), remote.pushedItems)
            assertEquals("PENDING", syncStateOf("a"))
        }

    @Test
    fun `받은 항목을 반영하고 커서는 가장 큰 서버 시각으로 앞으로만 감`() =
        runTest {
            connectCouple()

            engine.applyItems(COUPLE, listOf(itemDto("a", serverMicros = 30), itemDto("b", serverMicros = 50)))
            engine.applyItems(COUPLE, listOf(itemDto("c", serverMicros = 40)))

            assertEquals(
                listOf("a", "b", "c"),
                db
                    .itemDao()
                    .observeItems(ME)
                    .first()
                    .map { it.id }
                    .sorted(),
            )
            assertEquals(50L, db.syncCursorDao().getCursor("items:$COUPLE"))
        }

    @Test
    fun `아직 올리지 않은 내 변경은 받은 변경이 덮지 않음`() =
        runTest {
            connectCouple()
            db.itemDao().upsertItem(mySharedItem("a").copy(title = "내 수정"))

            engine.applyItems(COUPLE, listOf(itemDto("a", title = "상대 수정")))

            assertEquals("내 수정", db.itemDao().getItem("a")?.title)
        }

    @Test
    fun `다른 커플 경로의 문서는 버림`() =
        runTest {
            connectCouple()

            engine.applyItems(COUPLE, listOf(itemDto("x", coupleId = "couple-other")))

            assertNull(db.itemDao().getItem("x"))
        }

    @Test
    fun `백그라운드 조회는 커플을 받고 커서 이후 변경만 요청`() =
        runTest {
            remote.remoteCouples = listOf(coupleDto())
            remote.remoteItems = listOf(itemDto("a", serverMicros = 70))

            assertTrue(engine.pullOnce(ME))
            remote.remoteItems = emptyList()
            assertTrue(engine.pullOnce(ME))

            assertEquals(listOf(null, 70L), remote.requestedSince)
            assertEquals(
                "a",
                db
                    .itemDao()
                    .observeItem("a", ME)
                    .first()
                    ?.id,
            )
        }

    @Test
    fun `리스너가 받은 커플 · 항목 변경이 Room에 들어감`() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { engine.listen(ME) }

            remote.coupleChanges.emit(listOf(coupleDto()))
            // Room 변경 알림은 다른 스레드에서 오므로, 커플이 반영되어 항목 리스너가 붙을 때까지 기다린다
            remote.itemChanges.subscriptionCount.first { it > 0 }
            remote.itemChanges.emit(listOf(itemDto("a")))

            val items = db.itemDao().observeItems(ME).first { it.isNotEmpty() }
            assertEquals(listOf("a"), items.map { it.id })
            assertEquals(COUPLE, engine.observeActiveCoupleId(ME).first())
        }

    @Test
    fun `내 프로필을 저장했으면 올리고 SYNCED가 됨, 실패하면 FAILED`() =
        runTest {
            db.userDao().upsertUser(UserEntity(ME, "현우", "1998-04-12", NOW, null, "PENDING"))
            profileRemote.failPush = true

            assertFalse(engine.push(ME))
            assertEquals("FAILED", db.userDao().getUser(ME)?.syncState)

            profileRemote.failPush = false
            assertTrue(engine.push(ME))
            assertEquals(listOf("현우"), profileRemote.pushed.map { it.nickname })
            assertEquals("SYNCED", db.userDao().getUser(ME)?.syncState)
        }

    @Test
    fun `내 프로필은 이 기기에 없을 때만 받아 오고 상대 프로필은 커플이 있을 때 받음`() =
        runTest {
            profileRemote.profiles[ME] = ProfileDto(ME, "현우", null, NOW, serverUpdatedAtMicros = 1)
            profileRemote.profiles[PARTNER] = ProfileDto(PARTNER, "지민", "1999-09-28", NOW)
            remote.remoteCouples = listOf(coupleDto())

            assertTrue(engine.pullOnce(ME))
            assertTrue(engine.pullOnce(ME))

            assertEquals(listOf(ME, PARTNER, PARTNER), profileRemote.fetched)
            assertEquals("현우", db.userDao().getUser(ME)?.nickname)
            assertEquals("지민", db.userDao().getUser(PARTNER)?.nickname)
        }

    @Test
    fun `상대 프로필 리스너는 커플이 연결되면 붙음`() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { engine.listen(ME) }
            assertEquals(emptyList<String>(), profileRemote.observed)

            remote.coupleChanges.emit(listOf(coupleDto()))
            profileRemote.changes.subscriptionCount.first { it > 0 }
            profileRemote.changes.emit(ProfileDto(PARTNER, "지민", null, NOW))

            assertEquals(listOf(PARTNER), profileRemote.observed)
            assertEquals(
                "지민",
                db
                    .userDao()
                    .observeUser(PARTNER)
                    .first { it != null }
                    ?.nickname,
            )
        }
}
