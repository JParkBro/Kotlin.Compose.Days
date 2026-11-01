package jpark.bro.days.feature.settings.impl.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.model.AppThemeMode
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.couple.api.CoupleManageKey
import jpark.bro.days.feature.couple.api.PairingKey
import jpark.bro.days.feature.settings.impl.AccountKey
import jpark.bro.days.feature.settings.impl.NotificationSettingsKey
import jpark.bro.days.feature.settings.impl.PolicyListKey
import jpark.bro.days.feature.settings.impl.ProfileEditKey
import jpark.bro.days.feature.settings.impl.R
import jpark.bro.days.feature.settings.impl.WidgetSettingsKey
import jpark.bro.days.feature.settings.impl.settings.components.DotChip
import jpark.bro.days.feature.settings.impl.settings.components.ProfileCard
import jpark.bro.days.feature.settings.impl.settings.components.SettingsDivider
import jpark.bro.days.feature.settings.impl.settings.components.SettingsRow
import jpark.bro.days.feature.settings.impl.settings.components.SettingsSection
import jpark.bro.days.feature.settings.impl.settings.components.ThemeSheet
import jpark.bro.days.feature.settings.impl.settings.components.ThemeSheetContent
import jpark.bro.days.feature.settings.impl.settings.components.label
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun SettingsRoot(
    onNavigate: (NavKey) -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val themeSaveFailed = stringResource(R.string.settings_main_theme_save_failed)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is SettingsEvent.Navigate -> {
                onNavigate(
                    when (event.destination) {
                        SettingsDestination.PROFILE -> ProfileEditKey
                        SettingsDestination.COUPLE -> CoupleManageKey
                        SettingsDestination.PAIRING -> PairingKey
                        SettingsDestination.NOTIFICATION -> NotificationSettingsKey
                        SettingsDestination.WIDGET -> WidgetSettingsKey
                        SettingsDestination.POLICY -> PolicyListKey
                        SettingsDestination.ACCOUNT -> AccountKey
                    },
                )
            }

            is SettingsEvent.OpenSupport -> {
                when (event.item) {
                    SupportItem.RATE_APP -> context.openStoreListing()

                    // 공지사항 · 문의하기는 목적지가 아직 정해지지 않음 (product-spec 6장)
                    SupportItem.NOTICE, SupportItem.INQUIRY -> Unit
                }
            }

            SettingsEvent.ThemeSaveFailed -> {
                snackbarHostState.showSnackbar(themeSaveFailed)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScreen(
            state = state,
            appVersion = remember(context) { context.versionName() },
            onAction = viewModel::onAction,
        )
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun SettingsScreen(
    state: SettingsState,
    appVersion: String,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.bg)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.marginMobile),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                stringResource(R.string.settings_main_title),
                style = typography.headlineMd,
                color = colors.textPrimary,
            )
        }

        ProfileCard(
            profile = state.profile,
            onEditClick = { onAction(SettingsAction.MenuClicked(SettingsDestination.PROFILE)) },
        )

        SettingsSection(title = stringResource(R.string.settings_main_section_couple)) {
            SettingsRow(
                icon = DesignR.drawable.ic_favorite,
                title = stringResource(R.string.settings_main_couple_manage),
                onClick = { onAction(SettingsAction.MenuClicked(SettingsDestination.COUPLE)) },
                trailing = {
                    val partner = state.partnerName
                    if (partner != null) {
                        DotChip(
                            text = stringResource(R.string.settings_main_couple_connected, partner),
                            dotColor = colors.success,
                            textColor = colors.success,
                            background = colors.successSoft,
                        )
                    } else {
                        Text(
                            stringResource(R.string.settings_main_couple_connect),
                            style = typography.labelMd,
                            color = colors.primary,
                        )
                    }
                },
            )
        }

        SettingsSection(title = stringResource(R.string.settings_main_section_app)) {
            SettingsRow(
                icon = DesignR.drawable.ic_palette,
                title = stringResource(R.string.settings_main_theme),
                onClick = { onAction(SettingsAction.ThemeClicked) },
                trailing = {
                    DotChip(
                        text = stringResource(state.themeSetting.type.label.name),
                        dotColor = colors.primary,
                        textColor = colors.onPrimarySoft,
                        background = colors.primarySoft,
                    )
                },
            )
            SettingsDivider()
            SettingsRow(
                icon = DesignR.drawable.ic_notifications,
                title = stringResource(R.string.settings_main_notification),
                subtitle = stringResource(R.string.settings_main_notification_desc),
                onClick = { onAction(SettingsAction.MenuClicked(SettingsDestination.NOTIFICATION)) },
            )
            SettingsDivider()
            SettingsRow(
                icon = DesignR.drawable.ic_widgets,
                title = stringResource(R.string.settings_main_widget),
                onClick = { onAction(SettingsAction.MenuClicked(SettingsDestination.WIDGET)) },
                trailing = {
                    Text(
                        stringResource(R.string.settings_main_widget_guide),
                        style = typography.bodySm,
                        color = colors.textSecondary,
                    )
                },
            )
        }

        SettingsSection(title = stringResource(R.string.settings_main_section_support)) {
            SettingsRow(
                icon = DesignR.drawable.ic_campaign,
                title = stringResource(R.string.settings_main_notice),
                onClick = { onAction(SettingsAction.SupportClicked(SupportItem.NOTICE)) },
            )
            SettingsDivider()
            SettingsRow(
                icon = DesignR.drawable.ic_help,
                title = stringResource(R.string.settings_main_inquiry),
                onClick = { onAction(SettingsAction.SupportClicked(SupportItem.INQUIRY)) },
            )
            SettingsDivider()
            SettingsRow(
                icon = DesignR.drawable.ic_star,
                title = stringResource(R.string.settings_main_rate),
                onClick = { onAction(SettingsAction.SupportClicked(SupportItem.RATE_APP)) },
                trailing = {
                    Text(
                        stringResource(R.string.settings_main_rate_action),
                        style = typography.labelMd,
                        color = colors.primary,
                    )
                },
            )
        }

        SettingsSection(title = stringResource(R.string.settings_main_section_info)) {
            SettingsRow(
                icon = DesignR.drawable.ic_shield,
                title = stringResource(R.string.settings_main_policy),
                onClick = { onAction(SettingsAction.MenuClicked(SettingsDestination.POLICY)) },
            )
        }

        SettingsSection(title = stringResource(R.string.settings_main_section_account)) {
            SettingsRow(
                icon = DesignR.drawable.ic_manage_accounts,
                title = stringResource(R.string.settings_main_account),
                subtitle = state.accountEmail,
                onClick = { onAction(SettingsAction.MenuClicked(SettingsDestination.ACCOUNT)) },
            )
        }

        AppVersionFooter(version = appVersion)
    }

    state.themeSheet?.let { sheet -> ThemeSheet(sheet = sheet, onAction = onAction) }
}

/** 리스트 맨 아래 앱 이름 · 버전 (product-spec 6장) */
@Composable
private fun AppVersionFooter(version: String) {
    val colors = AppThemeTokens.colors
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.sm, bottom = AppSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_favorite),
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(14.dp),
            )
            Text(LocalContext.current.appName(), style = AppThemeTokens.typography.labelMd, color = colors.primary)
        }
        Text(
            stringResource(R.string.settings_main_version, version),
            style = AppThemeTokens.typography.bodySm,
            color = colors.textTertiary,
        )
    }
}

private fun Context.appName(): String = applicationInfo.loadLabel(packageManager).toString()

private fun Context.versionName(): String = packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()

private fun Context.openStoreListing() {
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
    try {
        startActivity(market)
    } catch (_: ActivityNotFoundException) {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")),
        )
    }
}

/** product-spec 9장 샘플 (현우 · 지민) */
@Suppress("MagicNumber") // 샘플 데이터
private val PreviewState =
    SettingsState(
        profile = ProfileSummary(nickname = "현우", birthDate = LocalDate.of(1998, 4, 12), togetherDays = 510),
        partnerName = "지민",
        accountEmail = "hyunwoo@example.com",
    )

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(state = PreviewState, appVersion = "1.0.0", onAction = {})
    }
}

@Preview(name = "Solo")
@Composable
private fun SettingsScreenSoloPreview() {
    AppTheme {
        SettingsScreen(state = SettingsState(), appVersion = "1.0.0", onAction = {})
    }
}

@Preview(name = "Theme sheet")
@Preview(name = "Theme sheet · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ThemeSheetPreview() {
    AppTheme {
        Box(modifier = Modifier.background(AppThemeTokens.colors.surfaceRaised)) {
            ThemeSheetContent(
                sheet = ThemeSheetState(type = AppThemeType.PINK, mode = AppThemeMode.LIGHT),
                onAction = {},
            )
        }
    }
}
