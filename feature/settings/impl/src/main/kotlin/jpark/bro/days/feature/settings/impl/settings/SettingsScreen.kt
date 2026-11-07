package jpark.bro.days.feature.settings.impl.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.res.Configuration
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysBadge
import jpark.bro.days.core.designsystem.component.DaysBadgeSize
import jpark.bro.days.core.designsystem.component.DaysBadgeTone
import jpark.bro.days.core.designsystem.component.DaysDivider
import jpark.bro.days.core.designsystem.component.DaysIconBadge
import jpark.bro.days.core.designsystem.component.DaysIconBadgeTone
import jpark.bro.days.core.designsystem.component.DaysListItem
import jpark.bro.days.core.designsystem.component.DaysSection
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.model.AppThemeMode
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.couple.api.CoupleManageKey
import jpark.bro.days.feature.couple.api.PairingKey
import jpark.bro.days.feature.settings.api.NotificationSettingsKey
import jpark.bro.days.feature.settings.api.ProfileEditKey
import jpark.bro.days.feature.settings.impl.AccountKey
import jpark.bro.days.feature.settings.impl.MoreAppsKey
import jpark.bro.days.feature.settings.impl.PolicyListKey
import jpark.bro.days.feature.settings.impl.R
import jpark.bro.days.feature.settings.impl.WidgetSettingsKey
import jpark.bro.days.feature.settings.impl.addebug.AdDebugRoot
import jpark.bro.days.feature.settings.impl.settings.components.SettingsMenu
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
                        SettingsDestination.MORE_APPS -> MoreAppsKey
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
            // 디버그 빌드에서만: 커플 사진 한도 · 보상형 광고 확인 (출시 빌드에는 ViewModel도 만들지 않음)
            debugSection = if (remember(context) { context.isDebuggable() }) ({ AdDebugRoot() }) else null,
        )
        DaysToastHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun SettingsScreen(
    state: SettingsState,
    appVersion: String,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
    debugSection: (@Composable () -> Unit)? = null,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg),
    ) {
        DaysTopBar(
            title = stringResource(R.string.settings_main_title),
            isScrolled = scrollState.canScrollBackward,
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = AppSpacing.marginMobile),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            SettingsMenu(state = state, onAction = onAction)
            debugSection?.invoke()
            AppVersionFooter(version = appVersion)
        }
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

private fun Context.isDebuggable(): Boolean = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

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
        isCoupled = true,
        partnerName = "지민",
        accountEmail = "hyunwoo@example.com",
    )

@ScreenPreviews
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(state = PreviewState, appVersion = "1.0.0", onAction = {})
    }
}

@Preview(name = "Solo", device = "spec:width=411dp,height=891dp")
@Composable
private fun SettingsScreenSoloPreview() {
    AppTheme {
        SettingsScreen(state = SettingsState(), appVersion = "1.0.0", onAction = {})
    }
}

@ComponentPreviews
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
