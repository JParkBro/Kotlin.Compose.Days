package jpark.bro.days.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.days.core.common.IdGenerator
import jpark.bro.days.core.data.Today
import jpark.bro.days.core.data.couple.CoupleTransitions
import jpark.bro.days.core.data.couple.PAIR_CODE_ALPHABET
import jpark.bro.days.core.data.couple.PairingException
import jpark.bro.days.core.data.couple.ProfileRequiredException
import jpark.bro.days.core.database.DaysDatabase
import jpark.bro.days.core.database.entity.ItemEntity
import jpark.bro.days.core.database.entity.UserEntity
import jpark.bro.days.core.model.PairCodeCheck
import jpark.bro.days.core.testing.FakeCoupleRemoteDataSource
import jpark.bro.days.core.testing.testSettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.time.LocalDate

/** 지민(코드 발급) · 현우(코드 입력) 두 기기가 같은 서버(Fake)를 쓴다 (product-spec 9장) */
@RunWith(AndroidJUnit4::class)
class DefaultCoupleRepositoryTest {
    private val server = FakeCoupleRemoteDataSource()
    private val clock = MutableClock(Today)
    private var nextId = 1
    private val jimin = Device(JIMIN, "지민")
    private val hyunwoo = Device(HYUNWOO, "현우")
    private val startDate = LocalDate.of(2025, 6, 14)

    private inner class Device(
        val uid: String,
        nickname: String?,
    ) {
        val db: DaysDatabase =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), DaysDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        private val ids = IdGenerator { "id-${nextId++}" }
        val transitions =
            CoupleTransitions(db.coupleDao(), db.coupleItemDao(), server, testSettingsDataStore(), clock, ids)
        val repository =
            DefaultCoupleRepository(transitions, server, db.userDao(), SwitchableUserSession(uid), clock, ids)

        init {
            if (nickname != null) {
                kotlinx.coroutines.runBlocking {
                    db.userDao().upsertUser(
                        UserEntity(uid, nickname, null, 0, null, "SYNCED"),
                    )
                }
            }
        }

        /** 동기화 리스너가 서버의 커플 문서를 받은 것처럼 */
        suspend fun receiveCouples() = transitions.apply(uid, server.couples.values.toList())

        suspend fun visibleTitles(): List<String> =
            db
                .itemDao()
                .observeItems(uid)
                .first()
                .map { it.title }
                .sorted()
    }

    @After
    fun tearDown() {
        jimin.db.close()
        hyunwoo.db.close()
    }

    private suspend fun connectCouple(): String {
        val code =
            jimin.repository
                .issueCode()
                .getOrThrow()
                .code
        hyunwoo.repository.connect(code.lowercase(), startDate, countFirstDayAsOne = true).getOrThrow()
        jimin.receiveCouples()
        return code
    }

    @Test
    fun `이름이 없으면 코드를 발급할 수 없음`() =
        runTest {
            val noName = Device("user-noname", nickname = null)

            assertTrue(noName.repository.issueCode().exceptionOrNull() is ProfileRequiredException)
            noName.db.close()
        }

    @Test
    fun `코드는 허용 문자 6자리이고 24시간 안에는 같은 코드를 다시 보여줌`() =
        runTest {
            server.takenCodes = 2 // 겹치면 다른 코드로 다시 시도

            val first = jimin.repository.issueCode().getOrThrow()
            val again = jimin.repository.issueCode().getOrThrow()

            assertEquals(6, first.code.length)
            assertTrue(first.code.all { it in PAIR_CODE_ALPHABET })
            assertEquals("지민", first.creatorNickname)
            assertEquals(first, again)
            assertEquals(setOf(first.code), server.pairCodes.keys)
        }

    @Test
    fun `만료가 가까우면 새 코드를 발급하고 지난 코드는 지움`() =
        runTest {
            val first = jimin.repository.issueCode().getOrThrow()
            clock.now = clock.now.plus(Duration.ofHours(24))

            val second = jimin.repository.issueCode().getOrThrow()

            assertNotEquals(first.code, second.code)
            assertEquals(setOf(second.code), server.pairCodes.keys)
        }

    @Test
    fun `코드 확인 결과`() =
        runTest {
            val code =
                jimin.repository
                    .issueCode()
                    .getOrThrow()
                    .code

            assertEquals(PairCodeCheck.NotFound, hyunwoo.repository.checkCode("K7Q").getOrThrow())
            assertEquals(PairCodeCheck.NotFound, hyunwoo.repository.checkCode("ZZZZZZ").getOrThrow())
            assertEquals(PairCodeCheck.OwnCode, jimin.repository.checkCode(code).getOrThrow())
            val valid = hyunwoo.repository.checkCode(code.lowercase()).getOrThrow()
            assertEquals("지민", (valid as PairCodeCheck.Valid).code.creatorNickname)

            clock.now = clock.now.plus(Duration.ofHours(25))
            assertEquals(PairCodeCheck.Expired, hyunwoo.repository.checkCode(code).getOrThrow())
        }

    @Test
    fun `연결하면 두 기기 모두 커플이 생기고 코드는 사용됨 · 발급자가 지움`() =
        runTest {
            val code = connectCouple()

            val couple = hyunwoo.repository.observeCouple().first()!!
            assertEquals(JIMIN, couple.partnerId)
            assertEquals(startDate, couple.startDate)
            assertTrue(couple.countFirstDayAsOne)
            assertEquals(
                HYUNWOO,
                jimin.repository
                    .observeCouple()
                    .first()
                    ?.partnerId,
            )
            assertEquals(
                listOf(JIMIN, HYUNWOO),
                server.couples.values
                    .single()
                    .members,
            )
            assertEquals(couple.id, server.coupleIds[HYUNWOO])
            assertEquals(couple.id, server.coupleIds[JIMIN])
            assertNull(server.pairCodes[code])
        }

    @Test
    fun `이미 사용한 코드 · 이미 연결된 사람은 연결할 수 없음`() =
        runTest {
            val code =
                jimin.repository
                    .issueCode()
                    .getOrThrow()
                    .code
            server.pairCodes[code] = server.pairCodes.getValue(code).copy(usedBy = "user-other")
            assertEquals(PairCodeCheck.AlreadyUsed, hyunwoo.repository.checkCode(code).getOrThrow())
            val failure = hyunwoo.repository.connect(code, startDate, true).exceptionOrNull()
            assertEquals(PairCodeCheck.AlreadyUsed, (failure as PairingException).reason)

            clock.now = clock.now.plus(Duration.ofHours(24)) // 지민이 새 코드를 발급하도록
            connectCouple()
            assertEquals(PairCodeCheck.AlreadyConnected, hyunwoo.repository.checkCode("ABCDEF").getOrThrow())
            assertTrue(jimin.repository.issueCode().isFailure)
        }

    @Test
    fun `사귄 날 · 1일로 세기를 바꾸면 서버와 기기에 반영`() =
        runTest {
            connectCouple()

            hyunwoo.repository.updateCouple(LocalDate.of(2025, 6, 15), countFirstDayAsOne = false).getOrThrow()
            jimin.receiveCouples()

            val couple = jimin.repository.observeCouple().first()!!
            assertEquals(LocalDate.of(2025, 6, 15), couple.startDate)
            assertTrue(!couple.countFirstDayAsOne)
            assertTrue(hyunwoo.repository.updateCouple(LocalDate.of(2026, 11, 6), true).isFailure)
        }

    @Test
    fun `해제하면 공유 항목이 각자의 나만 보기 사본으로 남고 사본 삭제를 고른 쪽은 남지 않음`() =
        runTest {
            val coupleId = connectCouple().let { server.couples.keys.single() }
            listOf(jimin, hyunwoo).forEach { it.db.itemDao().upsertItem(sharedItem("birthday", coupleId)) }

            hyunwoo.repository.disconnect(deleteCopies = true).getOrThrow()
            jimin.receiveCouples()

            assertNull(hyunwoo.repository.observeCouple().first())
            assertNull(jimin.repository.observeCouple().first())
            assertEquals(emptyList<String>(), hyunwoo.visibleTitles())
            assertEquals(listOf("지민 생일"), jimin.visibleTitles())
            assertEquals(null, server.coupleIds[HYUNWOO])

            // 같은 해제를 다시 받아도 사본을 또 만들지 않음
            jimin.receiveCouples()
            assertEquals(listOf("지민 생일"), jimin.visibleTitles())
        }

    @Test
    fun `30일 안에 같은 상대와 다시 연결하면 기존 커플을 되살리고 고치지 않은 사본은 지움`() =
        runTest {
            connectCouple()
            val coupleId = server.couples.keys.single()
            listOf(jimin, hyunwoo).forEach { it.db.itemDao().upsertItem(sharedItem("birthday", coupleId)) }
            hyunwoo.repository.disconnect(deleteCopies = false).getOrThrow()
            jimin.receiveCouples()
            assertEquals(listOf("지민 생일"), hyunwoo.visibleTitles()) // 사본만 보임

            clock.now = clock.now.plus(Duration.ofDays(29))
            connectCouple()

            assertEquals(setOf(coupleId), server.couples.keys)
            assertEquals(
                coupleId,
                hyunwoo.repository
                    .observeCouple()
                    .first()
                    ?.id,
            )
            assertEquals(listOf("지민 생일"), hyunwoo.visibleTitles()) // 원본만 보임
            assertEquals(listOf("지민 생일"), jimin.visibleTitles())
        }

    @Test
    fun `30일이 지나면 새 커플로 연결`() =
        runTest {
            connectCouple()
            val oldId = server.couples.keys.single()
            hyunwoo.repository.disconnect(deleteCopies = false).getOrThrow()
            jimin.receiveCouples()

            clock.now = clock.now.plus(Duration.ofDays(31))
            connectCouple()

            assertEquals(2, server.couples.size)
            assertNotEquals(
                oldId,
                hyunwoo.repository
                    .observeCouple()
                    .first()
                    ?.id,
            )
        }

    private fun sharedItem(
        id: String,
        coupleId: String,
    ) = ItemEntity(
        id = id,
        type = "DDAY",
        ownerUserId = JIMIN,
        coupleId = coupleId,
        visibility = "SHARED",
        title = "지민 생일",
        category = "BIRTHDAY",
        date = "2026-12-20",
        countMode = "DDAY",
        timeSlot = null,
        time = null,
        place = null,
        memo = null,
        colorKey = "PINK",
        iconKey = "CAKE",
        backgroundImageId = null,
        updatedBy = JIMIN,
        createdAt = 0,
        updatedAt = 0,
        serverUpdatedAt = 0,
        deletedAt = null,
        syncState = "SYNCED",
    )

    private companion object {
        const val JIMIN = "user-jimin"
        const val HYUNWOO = "user-hyunwoo"
    }
}
