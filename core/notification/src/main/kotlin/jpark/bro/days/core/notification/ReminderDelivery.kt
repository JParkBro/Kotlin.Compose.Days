package jpark.bro.days.core.notification

import jpark.bro.days.core.data.repository.AnniversaryRepository
import jpark.bro.days.core.data.repository.CoupleRepository
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.data.repository.ProfileRepository
import jpark.bro.days.core.domain.anniversary.autoAnniversaries
import jpark.bro.days.core.domain.reminder.dueReminders
import jpark.bro.days.core.model.UserBirthday
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate

/**
 * 오늘 보낼 알림을 모아 띄운다. 로그인한 계정의 데이터만 본다 (로그아웃이면 보낼 것이 없다).
 * - 디데이 · 일정: 내가 고른 알림(당일 · 1일 전 · 7일 전)
 * - 자동 기념일(100일 단위 · 주년 · 생일): 연결된 커플이 있을 때 당일, 숨긴 것은 빼고
 */
class ReminderDelivery(
    private val itemRepository: ItemRepository,
    private val coupleRepository: CoupleRepository,
    private val profileRepository: ProfileRepository,
    private val anniversaryRepository: AnniversaryRepository,
    private val notifier: ReminderNotifier,
    private val clock: Clock,
) {
    suspend fun deliverToday() {
        val today = LocalDate.now(clock)
        dueReminders(
            items = itemRepository.observeItems().first(),
            remindersByItemId = itemRepository.observeAllReminders().first(),
            today = today,
        ).forEach(notifier::notifyItem)

        val couple = coupleRepository.observeCouple().first() ?: return
        val me = profileRepository.observeMyProfile().first()
        val profiles = listOfNotNull(me, profileRepository.observePartnerProfile().first())
        autoAnniversaries(
            startDate = couple.startDate,
            countFirstDayAsOne = couple.countFirstDayAsOne,
            birthdays = profiles.mapNotNull { p -> p.birthDate?.let { UserBirthday(p.userId, it) } },
            period = today..today,
            hiddenRuleKeys = anniversaryRepository.observeHiddenRuleKeys().first(),
        ).forEach { anniversary ->
            notifier.notifyAnniversary(
                anniversary = anniversary,
                nicknames = profiles.associate { it.userId to it.nickname },
                myUserId = me?.userId,
            )
        }
    }
}
