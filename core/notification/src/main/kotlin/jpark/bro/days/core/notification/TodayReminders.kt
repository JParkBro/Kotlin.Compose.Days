package jpark.bro.days.core.notification

import jpark.bro.days.core.data.repository.AnniversaryRepository
import jpark.bro.days.core.data.repository.CoupleRepository
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.data.repository.ProfileRepository
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.domain.anniversary.autoAnniversaries
import jpark.bro.days.core.domain.reminder.DueReminder
import jpark.bro.days.core.domain.reminder.dueReminders
import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.NotificationSettings
import jpark.bro.days.core.model.UserBirthday
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate

/** 오늘 띄울 알림 */
data class TodayReminders(
    val items: List<DueReminder>,
    val anniversaries: List<AutoAnniversary>,
    /** 생일 기념일의 사용자 id → 이름 */
    val nicknames: Map<String, String> = emptyMap(),
    val myUserId: String? = null,
)

/**
 * 오늘 보낼 알림을 모은다. 로그인한 계정의 데이터만 본다 (로그아웃이면 보낼 것이 없다).
 * - 디데이 · 일정: 내가 고른 알림(당일 · 1일 전 · 7일 전)
 * - 자동 기념일(100일 단위 · 주년 · 생일): 연결된 커플이 있을 때 당일, 숨긴 것은 빼고
 * 알림 설정에서 끈 종류는 빼고 모은다: 기념일 알림 = 디데이 + 자동 기념일, 일정 알림 = 일정
 */
class TodayRemindersCollector(
    private val itemRepository: ItemRepository,
    private val coupleRepository: CoupleRepository,
    private val profileRepository: ProfileRepository,
    private val anniversaryRepository: AnniversaryRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) {
    suspend fun collect(): TodayReminders {
        val today = LocalDate.now(clock)
        val settings = settingsRepository.observeNotificationSettings().first()
        val items =
            dueReminders(
                items = itemRepository.observeItems().first(),
                remindersByItemId = itemRepository.observeAllReminders().first(),
                today = today,
            ).filter {
                it.item.content.type
                    .isEnabledIn(settings)
            }

        val couple = coupleRepository.observeCouple().first()
        if (!settings.anniversaryEnabled || couple == null) return TodayReminders(items, emptyList())
        val me = profileRepository.observeMyProfile().first()
        val profiles = listOfNotNull(me, profileRepository.observePartnerProfile().first())
        val anniversaries =
            autoAnniversaries(
                startDate = couple.startDate,
                countFirstDayAsOne = couple.countFirstDayAsOne,
                birthdays = profiles.mapNotNull { p -> p.birthDate?.let { UserBirthday(p.userId, it) } },
                period = today..today,
                hiddenRuleKeys = anniversaryRepository.observeHiddenRuleKeys().first(),
            )
        return TodayReminders(
            items = items,
            anniversaries = anniversaries,
            nicknames = profiles.associate { it.userId to it.nickname },
            myUserId = me?.userId,
        )
    }
}

private fun ItemType.isEnabledIn(settings: NotificationSettings): Boolean =
    when (this) {
        ItemType.DDAY -> settings.anniversaryEnabled
        ItemType.PLAN -> settings.planEnabled
    }
