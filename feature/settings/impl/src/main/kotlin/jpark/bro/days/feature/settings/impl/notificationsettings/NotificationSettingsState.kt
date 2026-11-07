package jpark.bro.days.feature.settings.impl.notificationsettings

import jpark.bro.days.core.model.NotificationSettings
import java.time.LocalDate

/** 알림 설정 (product-spec 6장, 시안 settings-notification) */
internal data class NotificationSettingsState(
    val isLoading: Boolean = true,
    /** 기기에서 이 앱 알림이 켜져 있는지 (Android 13 이상은 권한 포함). 화면이 다시 보일 때마다 확인 */
    val notificationsEnabled: Boolean = true,
    val settings: NotificationSettings = NotificationSettings(),
    val isHourSheetVisible: Boolean = false,
    val marketing: MarketingRow = MarketingRow.Loading,
)

/** 마케팅 수신 동의 행. 서버 기록을 읽어 와야 보여줄 수 있다 */
internal sealed interface MarketingRow {
    data object Loading : MarketingRow

    /** 읽지 못함: 눌러서 다시 시도 */
    data object Failed : MarketingRow

    data class Loaded(
        val agreed: Boolean,
        /** 동의 중일 때만 "동의 일자" */
        val agreedOn: LocalDate?,
        val isSaving: Boolean = false,
    ) : MarketingRow
}
