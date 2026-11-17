package jpark.bro.days.storeassets

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import jpark.bro.days.BuildConfig
import jpark.bro.days.core.data.repository.AuthRepository
import jpark.bro.days.core.data.repository.CoupleRepository
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.data.repository.OnboardingRepository
import jpark.bro.days.core.data.repository.ProfileRepository
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.model.AppThemeMode
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.Category
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.ThemeSetting
import jpark.bro.days.core.model.TimeSlot
import jpark.bro.days.core.model.Visibility
import jpark.bro.days.core.model.defaultIcon
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import java.time.LocalDate
import java.time.LocalTime

/**
 * 스토어 스크린샷용 샘플 데이터 (tools/store-assets/capture.py가 부른다. 평소 테스트에서는 건너뛴다).
 *
 * 실제 앱의 저장소 API로 Firebase 에뮬레이터(Auth · Firestore)에 로그인하고 항목을 넣는다.
 * 로그인은 Auth 에뮬레이터가 받아 주는 가짜 Google ID 토큰(서명 없는 JSON)을 쓴다.
 * 데이터는 product-spec.md 9장 시나리오 (기기 날짜를 2026-11-05로 맞춘 뒤 실행한다).
 *
 * 인자 (am instrument -e): mode = couple | solo, tag = 기기마다 다른 계정을 쓰기 위한 접미사
 * (Auth 에뮬레이터는 같은 이메일을 같은 계정으로 묶으므로 이메일에도 붙인다)
 */
@RunWith(AndroidJUnit4::class)
class StoreSeedTest {
    private val args = InstrumentationRegistry.getArguments()
    private val koin get() = GlobalContext.get()
    private val auth: AuthRepository get() = koin.get()
    private val onboarding: OnboardingRepository get() = koin.get()
    private val profiles: ProfileRepository get() = koin.get()
    private val couples: CoupleRepository get() = koin.get()
    private val items: ItemRepository get() = koin.get()
    private val settings: SettingsRepository get() = koin.get()

    @Test
    fun seed() {
        val mode = args.getString("mode") ?: return
        val tag = args.getString("tag") ?: "local"
        runBlocking {
            withTimeout(SEED_TIMEOUT_MILLIS) {
                when (mode) {
                    "couple" -> seedCouple(tag)
                    "solo" -> seedSolo(tag)
                    else -> error("알 수 없는 mode: $mode")
                }
            }
        }
    }

    private suspend fun seedCouple(tag: String) {
        // 상대(현우): 프로필이 서버에 올라간 뒤 코드를 발급하고 로그아웃한다
        signIn("store-hyunwoo-$tag", "hyunwoo.$tag")
        profiles.saveMyProfile("현우", LocalDate.of(1997, 3, 8), null).getOrThrow()
        awaitProfileOnServer()
        val code = couples.issueCode().getOrThrow().code
        auth.signOut().getOrThrow()

        // 나(지민): 코드로 연결
        signIn("store-jimin-$tag", "jimin.$tag")
        profiles.saveMyProfile("지민", null, null).getOrThrow()
        val couple = couples.connect(code, LocalDate.of(2025, 6, 14), countFirstDayAsOne = true).getOrThrow()
        partnerLearnsCouple(tag, couple.id)
        settings.setThemeSetting(ThemeSetting(AppThemeType.PINK, AppThemeMode.SYSTEM)).getOrThrow()

        fun shared(content: ItemContent) = content.copy(visibility = Visibility.SHARED, coupleId = couple.id)

        save(shared(plan("성수 저녁", "2026-11-06", TimeSlot.EXACT, LocalTime.of(19, 0), place = "성수동")))
        save(dday("토익 시험", "2026-11-08", Category.GOAL, "NAVY"))
        save(shared(plan("제주 여행", "2026-11-17", TimeSlot.ALL_DAY, showAsDday = true, place = "제주")))
        save(shared(dday("지민 생일", "2026-12-20", Category.BIRTHDAY, "PINK", repeat = RepeatRule.YEARLY)))
        save(
            shared(
                dday("처음 만난 날", "2025-04-26", Category.ANNIVERSARY, "LAVENDER", countMode = CountMode.DPLUS)
                    .copy(memo = "벚꽃 보러 갔다가 처음 만난 날. 600일에는 그 공원 다시 가기"),
            ),
        )
        save(shared(dday("크리스마스", "2026-12-25", Category.ETC, "SAGE", ItemIcon.GIFT, RepeatRule.YEARLY)))
        save(shared(plan("영화 보기", "2026-11-14", TimeSlot.EVENING, place = "메가박스 코엑스")))
        save(shared(plan("전시회", "2026-11-22", TimeSlot.LUNCH, place = "국립현대미술관")))
        save(plan("필라테스", "2026-11-03", TimeSlot.MORNING, isDone = true))
        save(plan("엄마 생신 선물 사기", "2026-11-11", TimeSlot.LUNCH))
    }

    private suspend fun seedSolo(tag: String) {
        signIn("store-doyun-$tag", "doyun.$tag")
        profiles.saveMyProfile("도윤", null, null).getOrThrow()
        settings.setThemeSetting(ThemeSetting(AppThemeType.NAVY, AppThemeMode.SYSTEM)).getOrThrow()

        val discharge = save(dday("전역일", "2027-03-19", Category.GOAL, "NAVY", ItemIcon.FLAG))
        items.setPinned(discharge, pinned = true).getOrThrow()
        save(dday("엄마 생신", "2027-01-02", Category.BIRTHDAY, "PINK", repeat = RepeatRule.YEARLY))
        save(
            dday(
                "금연",
                "2026-08-07",
                Category.GOAL,
                "SAGE",
                ItemIcon.STAR,
                countMode = CountMode.DPLUS,
                firstDayAsOne = true,
            ),
        )
        save(dday("토익 시험", "2026-11-08", Category.GOAL, "LAVENDER"))
        save(dday("운전면허 시험", "2026-11-27", Category.GOAL, "NAVY", ItemIcon.FLAG))
        save(dday("적금 만기", "2027-06-30", Category.ETC, "SAGE", ItemIcon.STAR))
        save(plan("친구 결혼식", "2026-11-21", TimeSlot.LUNCH, place = "강남"))
        save(plan("헬스장 PT", "2026-11-06", TimeSlot.EVENING))
    }

    private suspend fun signIn(
        sub: String,
        email: String,
    ) {
        auth.signInWithGoogle(fakeIdToken(sub, email)).getOrThrow()
        onboarding.markIntroSeen()
        onboarding.agreeTerms(marketing = false).getOrThrow()
        auth.observeUser().filterNotNull().first()
    }

    /**
     * 상대 기기가 연결을 받았을 때 하는 일 (CoupleTransitions.onConnected → setMyCoupleId)을 별도 FirebaseApp으로 한다.
     * 이것이 있어야 규칙상 내 기기가 상대 프로필(이름 · 생일)을 읽는다. 앱의 로그인 상태는 건드리지 않는다.
     */
    private suspend fun partnerLearnsCouple(
        tag: String,
        coupleId: String,
    ) {
        val sub = "store-hyunwoo-$tag"
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val app =
            FirebaseApp.getApps(context).firstOrNull { it.name == PARTNER_APP }
                ?: FirebaseApp.initializeApp(context, FirebaseApp.getInstance().options, PARTNER_APP).also { app ->
                    val host = checkNotNull(BuildConfig.FIREBASE_EMULATOR_HOST) { "에뮬레이터 연결 빌드가 아닙니다." }
                    Firebase.auth(app).useEmulator(host, AUTH_EMULATOR_PORT)
                    Firebase.firestore(app).useEmulator(host, FIRESTORE_EMULATOR_PORT)
                }
        val partnerAuth = Firebase.auth(app)
        partnerAuth
            .signInWithCredential(
                GoogleAuthProvider.getCredential(fakeIdToken(sub, "hyunwoo.$tag"), null),
            ).await()
        val uid = checkNotNull(partnerAuth.currentUser).uid
        Firebase
            .firestore(app)
            .collection("users")
            .document(uid)
            .set(mapOf("coupleId" to coupleId, "serverUpdatedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
            .await()
        partnerAuth.signOut()
    }

    /** 상대 기기에 보일 프로필이 동기화로 서버에 올라갈 때까지 (5초 디바운스 뒤 업로드) */
    private suspend fun awaitProfileOnServer() {
        val uid = checkNotNull(auth.currentUser()).uid
        repeat(PROFILE_POLL_COUNT) {
            val doc =
                runCatching {
                    Firebase.firestore
                        .collection(
                            "users",
                        ).document(uid)
                        .get(Source.SERVER)
                        .await()
                }.getOrNull()
            if (doc?.getString("nickname") != null) return
            delay(PROFILE_POLL_MILLIS)
        }
        error("프로필이 서버에 올라가지 않았습니다.")
    }

    /** Auth 에뮬레이터는 서명 없는 JSON을 Google ID 토큰으로 받아 준다 */
    private fun fakeIdToken(
        sub: String,
        email: String,
    ) = """{"sub":"$sub","email":"$email@example.com","email_verified":true}"""

    private suspend fun save(content: ItemContent): String = items.saveItem(null, content).getOrThrow().id

    private fun dday(
        title: String,
        date: String,
        category: Category,
        color: String,
        icon: ItemIcon = category.defaultIcon,
        repeat: RepeatRule = RepeatRule.NONE,
        countMode: CountMode = CountMode.DDAY,
        firstDayAsOne: Boolean = false,
    ) = ItemContent(
        type = ItemType.DDAY,
        title = title,
        colorKey = color,
        iconKey = icon.name,
        category = category,
        date = LocalDate.parse(date),
        countMode = countMode,
        countFirstDayAsOne = firstDayAsOne,
        repeatRule = repeat,
    )

    private fun plan(
        title: String,
        date: String,
        slot: TimeSlot,
        time: LocalTime? = null,
        place: String? = null,
        showAsDday: Boolean = false,
        isDone: Boolean = false,
    ) = ItemContent(
        type = ItemType.PLAN,
        title = title,
        colorKey = "PINK",
        iconKey = if (showAsDday) ItemIcon.FLIGHT.name else ItemIcon.EVENT.name,
        date = LocalDate.parse(date),
        timeSlot = slot,
        time = time,
        place = place,
        showAsDday = showAsDday,
        isDone = isDone,
        category = if (showAsDday) Category.TRAVEL else Category.ETC,
    )

    private companion object {
        const val SEED_TIMEOUT_MILLIS = 120_000L
        const val PROFILE_POLL_COUNT = 60
        const val PROFILE_POLL_MILLIS = 1_000L
        const val PARTNER_APP = "store-partner"
        const val AUTH_EMULATOR_PORT = 9099
        const val FIRESTORE_EMULATOR_PORT = 8080
    }
}
