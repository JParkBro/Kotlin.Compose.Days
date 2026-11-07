package jpark.bro.days.feature.settings.impl.notificationsettings

internal sealed interface NotificationSettingsAction {
    data object BackClicked : NotificationSettingsAction

    /** 화면이 다시 보일 때 확인한 기기 알림 상태 */
    data class NotificationsChecked(
        val enabled: Boolean,
    ) : NotificationSettingsAction

    /** 맨 위 기기 알림 카드: 시스템의 앱 알림 설정으로 */
    data object DeviceSettingsClicked : NotificationSettingsAction

    data class AnniversaryToggled(
        val enabled: Boolean,
    ) : NotificationSettingsAction

    data class PlanToggled(
        val enabled: Boolean,
    ) : NotificationSettingsAction

    data object SendHourClicked : NotificationSettingsAction

    data class SendHourSelected(
        val hour: Int,
    ) : NotificationSettingsAction

    data object HourSheetDismissed : NotificationSettingsAction

    data class MarketingToggled(
        val agreed: Boolean,
    ) : NotificationSettingsAction

    data object MarketingRetryClicked : NotificationSettingsAction
}
