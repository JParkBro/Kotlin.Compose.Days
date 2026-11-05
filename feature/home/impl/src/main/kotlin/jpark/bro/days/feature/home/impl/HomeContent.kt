package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.domain.anniversary.NextAnniversary
import jpark.bro.days.core.domain.anniversary.nextAnniversary
import jpark.bro.days.core.domain.dday.FeaturedItem
import jpark.bro.days.core.domain.dday.ItemDday
import jpark.bro.days.core.domain.dday.countingItems
import jpark.bro.days.core.domain.dday.daysSince
import jpark.bro.days.core.domain.dday.featured
import jpark.bro.days.core.domain.dday.sortTime
import jpark.bro.days.core.model.Couple
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.UserBirthday
import jpark.bro.days.core.model.UserProfile
import java.io.File
import java.time.LocalDate

/**
 * 홈 (product-spec 4-1). 커플이면 [couple]이 히어로를 대신하고, 솔로면 [hero]가 대표 디데이다.
 * 히어로 → 다음 일정 (+ 커플이면 다음 기념일) → 디데이 리스트
 */
internal data class HomeContent(
    val hero: FeaturedItem? = null,
    val couple: CoupleSummary? = null,
    val nextPlan: Item? = null,
    val rows: List<ItemDday> = emptyList(),
)

/** 커플 히어로 · 다음 기념일 카드 */
internal data class CoupleSummary(
    val myName: String?,
    val partnerName: String?,
    /** 프로필 사진 파일. 없으면 이름 첫 글자 */
    val myPhoto: File? = null,
    val partnerPhoto: File? = null,
    val startDate: LocalDate,
    val togetherDays: Int,
    val nextAnniversary: NextAnniversary?,
    /** 생일 기념일 이름에 쓰는 userId → 이름 */
    val nicknames: Map<String, String>,
)

/** 솔로면 null */
internal fun buildCoupleSummary(
    source: CoupleSource,
    today: LocalDate,
): CoupleSummary? =
    source.couple?.let { couple ->
        buildCoupleSummary(couple, source.me, source.partner, source.hidden, today)
            .copy(myPhoto = source.myPhoto, partnerPhoto = source.partnerPhoto)
    }

/** 연결된 커플 + 두 사람 프로필 + 숨긴 기념일로 [CoupleSummary]를 만든다 */
internal fun buildCoupleSummary(
    couple: Couple,
    me: UserProfile?,
    partner: UserProfile?,
    hiddenRuleKeys: Set<String>,
    today: LocalDate,
): CoupleSummary {
    val profiles = listOfNotNull(me, partner)
    return CoupleSummary(
        myName = me?.nickname,
        partnerName = partner?.nickname,
        startDate = couple.startDate,
        togetherDays = daysSince(couple.startDate, today, couple.countFirstDayAsOne),
        nextAnniversary =
            nextAnniversary(
                startDate = couple.startDate,
                countFirstDayAsOne = couple.countFirstDayAsOne,
                birthdays = profiles.mapNotNull { p -> p.birthDate?.let { UserBirthday(p.userId, it) } },
                today = today,
                hiddenRuleKeys = hiddenRuleKeys,
            ),
        nicknames = profiles.associate { it.userId to it.nickname },
    )
}

/**
 * - 리스트: [countingItems]. 솔로는 히어로 항목을 뺀다 (커플 히어로는 항목이 아니라 빼지 않음)
 * - 히어로: 솔로일 때만 [featured] (대표 고정 → 가장 가까운 다가오는 항목)
 * - 다가오는 일정: 오늘 이후 미완료 일정 중 가장 빠른 것 (날짜 → 시각 순, [sortTime])
 */
internal fun buildHomeContent(
    items: List<Item>,
    pinnedItemId: String?,
    today: LocalDate,
    couple: CoupleSummary? = null,
): HomeContent {
    val rows = countingItems(items, today)
    val hero = if (couple == null) rows.featured(pinnedItemId) else null

    val nextPlan =
        items
            .filter { it.content.type == ItemType.PLAN && !it.content.isDone }
            .filter { item -> item.content.date?.let { !it.isBefore(today) } ?: false }
            .minWithOrNull(compareBy<Item>({ it.content.date }).thenBy { it.content.sortTime })

    return HomeContent(
        hero = hero,
        couple = couple,
        nextPlan = nextPlan,
        rows = rows.filter { it.item.id != hero?.entry?.item?.id },
    )
}
