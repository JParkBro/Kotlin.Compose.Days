package jpark.bro.days.core.model

import java.time.LocalTime

/**
 * 알림 설정 (product-spec 6장). 기기에만 저장하고 서버로 보내지 않는다.
 * - 기념일 알림: 디데이 알림 + 자동 기념일 당일 알림
 * - 일정 알림: 일정 알림
 * - 상대 활동 알림: 상대가 공유 항목을 추가 · 수정 · 삭제했을 때 (끄면 시스템 알림만 끄고 알림 목록에는 남긴다)
 * - [sendHour]: 기념일 · 일정 알림을 보내는 시각 (정각)
 */
data class NotificationSettings(
    val anniversaryEnabled: Boolean = true,
    val planEnabled: Boolean = true,
    val activityEnabled: Boolean = true,
    val sendHour: Int = DEFAULT_SEND_HOUR,
) {
    val sendTime: LocalTime get() = LocalTime.of(sendHour, 0)

    companion object {
        const val DEFAULT_SEND_HOUR = 9
        private const val FIRST_SEND_HOUR = 8
        private const val LAST_SEND_HOUR = 12

        /** 고를 수 있는 시각: 오전 8시 ~ 정오 12시 (시안 settings-notification) */
        val SendHours: List<Int> = (FIRST_SEND_HOUR..LAST_SEND_HOUR).toList()
    }
}

/** 마케팅 정보 수신 동의 (product-spec 5장 선택 항목). [updatedAt]은 마지막으로 동의 · 철회한 시각(epoch millis), 기록이 없으면 null */
data class MarketingConsent(
    val agreed: Boolean,
    val updatedAt: Long?,
)
