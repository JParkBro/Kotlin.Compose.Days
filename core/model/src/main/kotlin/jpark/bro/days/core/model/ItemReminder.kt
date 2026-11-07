package jpark.bro.days.core.model

import java.time.LocalTime

/** 항목 알림 (사용자별, 공유하지 않음). [offsetDays] 0 = 당일, 1 = 하루 전, 7 = 일주일 전 */
data class ItemReminder(
    val offsetDays: Int,
    val timeOfDay: LocalTime,
) {
    companion object {
        /**
         * 항목 알림에 저장하는 시각. 실제 발송 시각은 알림 설정의 [NotificationSettings.sendHour]라
         * 이 값은 저장만 하고 쓰지 않는다 (2026-11, 7-3)
         */
        val DefaultTime: LocalTime = LocalTime.of(NotificationSettings.DEFAULT_SEND_HOUR, 0)
    }
}
