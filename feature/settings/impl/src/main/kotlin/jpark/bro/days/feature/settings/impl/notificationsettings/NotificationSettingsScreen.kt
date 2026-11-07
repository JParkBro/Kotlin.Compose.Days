package jpark.bro.days.feature.settings.impl.notificationsettings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysBadge
import jpark.bro.days.core.designsystem.component.DaysBadgeSize
import jpark.bro.days.core.designsystem.component.DaysBadgeTone
import jpark.bro.days.core.designsystem.component.DaysBottomSheet
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysDivider
import jpark.bro.days.core.designsystem.component.DaysIconBadge
import jpark.bro.days.core.designsystem.component.DaysIconBadgeTone
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysInfoBox
import jpark.bro.days.core.designsystem.component.DaysListItem
import jpark.bro.days.core.designsystem.component.DaysSection
import jpark.bro.days.core.designsystem.component.DaysSwitch
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.model.NotificationSettings
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.core.ui.ObserveNotificationsEnabled
import jpark.bro.days.core.ui.openAppNotificationSettings
import jpark.bro.days.core.ui.toShortDisplayText
import jpark.bro.days.feature.settings.impl.R
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun NotificationSettingsRoot(
    onBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toastHostState = remember { SnackbarHostState() }
    val saveFailed = stringResource(R.string.settings_notification_save_failed)
    val marketingSaveFailed = stringResource(R.string.settings_notification_marketing_save_failed)
    // 날짜를 넣는 문구: 형식 문자열만 미리 받아 둔다
    val marketingAgreed = stringResource(R.string.settings_notification_marketing_agreed_toast)
    val marketingWithdrawn = stringResource(R.string.settings_notification_marketing_withdrawn_toast)

    ObserveNotificationsEnabled { enabled ->
        viewModel.onAction(NotificationSettingsAction.NotificationsChecked(enabled))
    }
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            NotificationSettingsEvent.NavigateBack -> {
                onBack()
            }

            NotificationSettingsEvent.OpenDeviceSettings -> {
                context.openAppNotificationSettings()
            }

            NotificationSettingsEvent.SaveFailed -> {
                toastHostState.showSnackbar(saveFailed)
            }

            is NotificationSettingsEvent.MarketingChanged -> {
                val message = if (event.agreed) marketingAgreed else marketingWithdrawn
                toastHostState.showSnackbar(message.format(event.date.toShortDisplayText()))
            }

            NotificationSettingsEvent.MarketingSaveFailed -> {
                toastHostState.showSnackbar(marketingSaveFailed)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotificationSettingsScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
    if (state.isHourSheetVisible) {
        SendHourSheet(selected = state.settings.sendHour, onAction = viewModel::onAction)
    }
}

/** 알림 설정 (product-spec 6장, 시안 settings-notification). 상대 활동 알림은 7-4에서 */
@Composable
private fun NotificationSettingsScreen(
    state: NotificationSettingsState,
    onAction: (NotificationSettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg),
    ) {
        DaysTopBar(
            title = stringResource(R.string.settings_notification_title),
            isScrolled = scrollState.canScrollBackward,
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.settings_notification_back),
                    onClick = { onAction(NotificationSettingsAction.BackClicked) },
                )
            },
        )
        if (state.isLoading) return@Column
        Column(
            modifier =
                Modifier
                    .verticalScroll(scrollState)
                    .padding(horizontal = AppSpacing.marginMobile)
                    .padding(top = AppSpacing.sm, bottom = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            DeviceNotificationCard(enabled = state.notificationsEnabled, onAction = onAction)
            ReminderSection(settings = state.settings, onAction = onAction)
            MarketingSection(row = state.marketing, onAction = onAction)
            DaysInfoBox(text = stringResource(R.string.settings_notification_info))
        }
    }
}

/** 기기 알림 상태. 누르면 시스템의 앱 알림 설정으로 (권한이 없으면 앱 알림이 오지 않는다) */
@Composable
private fun DeviceNotificationCard(
    enabled: Boolean,
    onAction: (NotificationSettingsAction) -> Unit,
) {
    val title = if (enabled) R.string.settings_notification_device_on else R.string.settings_notification_device_off
    val subtitle =
        if (enabled) R.string.settings_notification_device_on_desc else R.string.settings_notification_device_off_desc
    val badge =
        if (enabled) R.string.settings_notification_device_allowed else R.string.settings_notification_device_blocked
    DaysCard(
        onClick = { onAction(NotificationSettingsAction.DeviceSettingsClicked) },
        modifier = Modifier.testTag("notification_device_card"),
    ) {
        DaysListItem(
            title = stringResource(title),
            subtitle = stringResource(subtitle),
            showChevron = false,
            leading = {
                DaysIconBadge(
                    icon = DesignR.drawable.ic_notifications,
                    tone = if (enabled) DaysIconBadgeTone.SHARED else DaysIconBadgeTone.SOON,
                )
            },
            trailing = {
                DaysBadge(
                    text = stringResource(badge),
                    tone = if (enabled) DaysBadgeTone.SHARED else DaysBadgeTone.SOON,
                    size = DaysBadgeSize.MEDIUM,
                    showDot = true,
                )
            },
        )
    }
}

@Composable
private fun ReminderSection(
    settings: NotificationSettings,
    onAction: (NotificationSettingsAction) -> Unit,
) {
    DaysSection(title = stringResource(R.string.settings_notification_section_reminder)) {
        SwitchRow(
            title = stringResource(R.string.settings_notification_anniversary),
            subtitle = stringResource(R.string.settings_notification_anniversary_desc),
            checked = settings.anniversaryEnabled,
            onCheckedChange = { onAction(NotificationSettingsAction.AnniversaryToggled(it)) },
            testTag = "notification_anniversary_switch",
        )
        DaysDivider(inset = AppSpacing.marginMobile)
        SwitchRow(
            title = stringResource(R.string.settings_notification_plan),
            subtitle = stringResource(R.string.settings_notification_plan_desc),
            checked = settings.planEnabled,
            onCheckedChange = { onAction(NotificationSettingsAction.PlanToggled(it)) },
            testTag = "notification_plan_switch",
        )
        DaysDivider(inset = AppSpacing.marginMobile)
        DaysListItem(
            title = stringResource(R.string.settings_notification_send_time),
            subtitle = stringResource(R.string.settings_notification_send_time_desc),
            onClick = { onAction(NotificationSettingsAction.SendHourClicked) },
            trailing = {
                Text(
                    text = sendHourLabel(settings.sendHour),
                    style = AppThemeTokens.typography.labelLg,
                    color = AppThemeTokens.colors.primary,
                )
            },
            modifier = Modifier.testTag("notification_send_time"),
        )
    }
}

@Composable
private fun MarketingSection(
    row: MarketingRow,
    onAction: (NotificationSettingsAction) -> Unit,
) {
    DaysSection(title = stringResource(R.string.settings_notification_section_marketing)) {
        val title = stringResource(R.string.settings_notification_marketing)
        when (row) {
            MarketingRow.Loading -> {
                DaysListItem(title = title, subtitle = stringResource(R.string.settings_notification_marketing_loading))
            }

            MarketingRow.Failed -> {
                DaysListItem(
                    title = title,
                    subtitle = stringResource(R.string.settings_notification_marketing_failed),
                    onClick = { onAction(NotificationSettingsAction.MarketingRetryClicked) },
                    showChevron = false,
                    modifier = Modifier.testTag("notification_marketing_retry"),
                )
            }

            is MarketingRow.Loaded -> {
                val desc = stringResource(R.string.settings_notification_marketing_desc)
                SwitchRow(
                    title = title,
                    subtitle =
                        row.agreedOn?.let {
                            desc + "\n" +
                                stringResource(
                                    R.string.settings_notification_marketing_agreed_on,
                                    it.toShortDisplayText(),
                                )
                        } ?: desc,
                    checked = row.agreed,
                    enabled = !row.isSaving,
                    onCheckedChange = { onAction(NotificationSettingsAction.MarketingToggled(it)) },
                    testTag = "notification_marketing_switch",
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    enabled: Boolean = true,
) {
    DaysListItem(
        title = title,
        subtitle = subtitle,
        trailing = {
            DaysSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                modifier = Modifier.testTag(testTag),
            )
        },
    )
}

/** 알림 시간 선택 시트: 오전 8시 ~ 정오 12시 */
@Composable
private fun SendHourSheet(
    selected: Int,
    onAction: (NotificationSettingsAction) -> Unit,
) {
    DaysBottomSheet(onDismissRequest = { onAction(NotificationSettingsAction.HourSheetDismissed) }) {
        Column(modifier = Modifier.navigationBarsPadding().padding(bottom = AppSpacing.lg)) {
            Column(modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm)) {
                Text(
                    stringResource(R.string.settings_notification_hour_sheet_title),
                    style = AppThemeTokens.typography.headlineMd,
                    color = AppThemeTokens.colors.textPrimary,
                )
                Text(
                    stringResource(R.string.settings_notification_hour_sheet_desc),
                    style = AppThemeTokens.typography.bodySm,
                    color = AppThemeTokens.colors.textSecondary,
                )
            }
            NotificationSettings.SendHours.forEach { hour ->
                DaysListItem(
                    title = sendHourLabel(hour),
                    emphasized = hour == selected,
                    onClick = { onAction(NotificationSettingsAction.SendHourSelected(hour)) },
                    showChevron = false,
                    trailing = {
                        if (hour == selected) {
                            Icon(
                                painter = painterResource(DesignR.drawable.ic_check),
                                contentDescription = stringResource(R.string.settings_notification_hour_selected),
                                tint = AppThemeTokens.colors.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    },
                    modifier = Modifier.testTag("notification_hour_$hour"),
                )
            }
        }
    }
}

@Composable
private fun sendHourLabel(hour: Int): String =
    if (hour == NOON) {
        stringResource(R.string.settings_notification_hour_noon)
    } else {
        stringResource(R.string.settings_notification_hour_am, hour)
    }

private const val NOON = 12

@ScreenPreviews
@Composable
private fun NotificationSettingsScreenPreview() {
    AppTheme {
        NotificationSettingsScreen(
            state =
                NotificationSettingsState(
                    isLoading = false,
                    marketing = MarketingRow.Loaded(agreed = true, agreedOn = LocalDate.of(2026, 11, 5)),
                ),
            onAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun NotificationSettingsScreenOffPreview() {
    AppTheme {
        NotificationSettingsScreen(
            state =
                NotificationSettingsState(
                    isLoading = false,
                    notificationsEnabled = false,
                    settings = NotificationSettings(planEnabled = false, sendHour = NOON),
                    marketing = MarketingRow.Failed,
                ),
            onAction = {},
        )
    }
}
