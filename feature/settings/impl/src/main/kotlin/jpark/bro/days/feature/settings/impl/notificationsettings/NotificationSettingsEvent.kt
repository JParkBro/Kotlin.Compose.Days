package jpark.bro.days.feature.settings.impl.notificationsettings

import java.time.LocalDate

internal sealed interface NotificationSettingsEvent {
    data object NavigateBack : NotificationSettingsEvent

    data object OpenDeviceSettings : NotificationSettingsEvent

    data object SaveFailed : NotificationSettingsEvent

    /** 동의 · 철회 처리 결과 안내 (처리 날짜와 함께) */
    data class MarketingChanged(
        val agreed: Boolean,
        val date: LocalDate,
    ) : NotificationSettingsEvent

    data object MarketingSaveFailed : NotificationSettingsEvent
}
