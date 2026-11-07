package jpark.bro.days.core.model

/** 알림 목록 한 줄 (product-spec 알림 목록). 기기에만 저장하고 [RETENTION_DAYS]일이 지나면 지운다 */
data class ActivityLog(
    val id: String,
    val content: ActivityContent,
    /** epoch millis */
    val createdAt: Long,
    val isRead: Boolean,
) {
    companion object {
        const val RETENTION_DAYS = 30L
    }
}

/** 기록할 내용. 종류마다 쓰는 값이 다르다 */
data class ActivityContent(
    val type: ActivityType,
    /** 상대 활동 · REMINDER */
    val itemId: String? = null,
    val itemType: ItemType? = null,
    /** 기록 당시 제목 (항목이 지워져도 보이게) */
    val itemTitle: String? = null,
    /** 상대 활동을 한 사람 */
    val actorUserId: String? = null,
    /** REMINDER: 0 = 당일, 1 = 하루 전, 7 = 일주일 전 */
    val offsetDays: Int? = null,
    /** ANNIVERSARY: [AutoAnniversary.ruleKey] */
    val ruleKey: String? = null,
)

enum class ActivityType {
    /** 상대가 공유 항목을 추가 */
    ITEM_ADDED,

    /** 상대가 공유 항목을 수정 */
    ITEM_UPDATED,

    /** 상대가 공유 항목을 삭제 */
    ITEM_DELETED,

    /** 보낸 디데이 · 일정 알림 */
    REMINDER,

    /** 보낸 자동 기념일 당일 알림 */
    ANNIVERSARY,
    ;

    val isPartnerActivity: Boolean get() = this == ITEM_ADDED || this == ITEM_UPDATED || this == ITEM_DELETED
}
