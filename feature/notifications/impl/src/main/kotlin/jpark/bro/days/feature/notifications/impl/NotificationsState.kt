package jpark.bro.days.feature.notifications.impl

import jpark.bro.days.core.model.ActivityType
import jpark.bro.days.core.model.ItemType

/** 알림 목록 (product-spec 3-2, 시안 notifications) */
internal data class NotificationsState(
    val isLoading: Boolean = true,
    val sections: List<ActivitySection> = emptyList(),
    val unreadCount: Int = 0,
)

internal data class ActivitySection(
    val group: ActivityGroup,
    val rows: List<ActivityRow>,
)

/** 오늘 · 이번 주(지난 7일) · 이전 */
internal enum class ActivityGroup {
    TODAY,
    THIS_WEEK,
    EARLIER,
}

/** 한 줄. 문구는 화면이 종류별로 만든다 */
internal data class ActivityRow(
    val id: String,
    val type: ActivityType,
    val itemType: ItemType?,
    val itemTitle: String?,
    /** 상대 활동: 상대 이름 (모르면 빈 문자열) */
    val actorName: String?,
    /** REMINDER: 며칠 전 알림 */
    val offsetDays: Int?,
    /** ANNIVERSARY */
    val anniversary: AnniversaryLabel?,
    val elapsed: Elapsed,
    val isRead: Boolean,
    /** 눌렀을 때 열 항목 (지운 항목 · 자동 기념일은 null) */
    val target: OpenTarget?,
)

internal data class OpenTarget(
    val itemId: String,
    val itemType: ItemType,
)

internal sealed interface AnniversaryLabel {
    data class Days(
        val days: Int,
    ) : AnniversaryLabel

    data class Years(
        val years: Int,
    ) : AnniversaryLabel

    /** [name]이 null이면 내 생일 */
    data class Birthday(
        val name: String?,
    ) : AnniversaryLabel
}

/** 기록된 지 얼마나 지났는지 */
internal sealed interface Elapsed {
    data object JustNow : Elapsed

    data class Minutes(
        val minutes: Long,
    ) : Elapsed

    data class Hours(
        val hours: Long,
    ) : Elapsed

    data object Yesterday : Elapsed

    data class Days(
        val days: Long,
    ) : Elapsed
}
