package jpark.bro.days.core.model

import java.time.LocalTime

/** 항목 알림 (사용자별, 공유하지 않음). [offsetDays] 0 = 당일, 1 = 하루 전, 7 = 일주일 전 */
data class ItemReminder(
    val offsetDays: Int,
    val timeOfDay: LocalTime,
) {
    companion object {
        private const val SEND_HOUR = 9

        /** 알림 발송 시각: 오전 9시 고정 (product-spec 4-5). 저장 · 예약 모두 이 값을 쓴다 */
        val SendTime: LocalTime = LocalTime.of(SEND_HOUR, 0)
    }
}
