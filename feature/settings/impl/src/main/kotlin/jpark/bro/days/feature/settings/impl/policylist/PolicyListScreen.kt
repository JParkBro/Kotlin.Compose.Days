package jpark.bro.days.feature.settings.impl.policylist

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysBottomSheet
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysDivider
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysListItem
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.settings.api.PolicyDocKey
import jpark.bro.days.feature.settings.api.PolicyType
import jpark.bro.days.feature.settings.impl.R
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun PolicyListRoot(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
    viewModel: PolicyListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val ossTitle = stringResource(R.string.settings_policy_oss)
    // 라이브러리 화면도 지금 앱 테마(색 · 글꼴)로 그린다. 앱이 이미 라이트 · 다크를 정했으므로 둘 다 같은 값을 넘긴다
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is PolicyListEvent.OpenPolicy -> onNavigate(PolicyDocKey(event.type))
            PolicyListEvent.OpenOssLicenses -> context.openOssLicenses(ossTitle, colorScheme, typography)
            PolicyListEvent.NavigateBack -> onBack()
        }
    }

    PolicyListScreen(state = state, onAction = viewModel::onAction)
}

/** 약관 및 정책 (product-spec 6장): 개인정보 처리방침(굵게, 맨 위) · 이용약관 · 오픈소스 라이선스 · 글꼴 라이선스 */
@Composable
private fun PolicyListScreen(
    state: PolicyListState,
    onAction: (PolicyListAction) -> Unit,
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
            title = stringResource(R.string.settings_policy_title),
            isScrolled = scrollState.canScrollBackward,
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.settings_policy_back),
                    onClick = { onAction(PolicyListAction.BackClicked) },
                )
            },
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
        ) {
            DaysCard {
                DaysListItem(
                    title = stringResource(R.string.settings_policy_privacy),
                    emphasized = true,
                    onClick = { onAction(PolicyListAction.PolicyClicked(PolicyType.PRIVACY)) },
                    modifier = Modifier.testTag("policy_privacy"),
                )
                DaysDivider(inset = AppSpacing.marginMobile)
                DaysListItem(
                    title = stringResource(R.string.settings_policy_terms),
                    onClick = { onAction(PolicyListAction.PolicyClicked(PolicyType.TERMS)) },
                    modifier = Modifier.testTag("policy_terms"),
                )
                DaysDivider(inset = AppSpacing.marginMobile)
                DaysListItem(
                    title = stringResource(R.string.settings_policy_oss),
                    onClick = { onAction(PolicyListAction.OssLicensesClicked) },
                    modifier = Modifier.testTag("policy_oss"),
                )
                DaysDivider(inset = AppSpacing.marginMobile)
                DaysListItem(
                    title = stringResource(R.string.settings_policy_font),
                    subtitle = stringResource(R.string.settings_policy_font_subtitle),
                    onClick = { onAction(PolicyListAction.FontLicenseClicked) },
                    modifier = Modifier.testTag("policy_font"),
                )
            }
        }
    }

    if (state.isFontLicenseOpen) {
        FontLicenseSheet(onDismiss = { onAction(PolicyListAction.FontLicenseDismissed) })
    }
}

/** Pretendard SIL OFL 1.1 원문 (designsystem res/raw) */
@Composable
private fun FontLicenseSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val text =
        remember(context) {
            context.resources
                .openRawResource(DesignR.raw.pretendard_ofl)
                .bufferedReader()
                .use { it.readText() }
        }
    DaysBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.settings_policy_font),
            style = AppThemeTokens.typography.headlineSm,
            color = AppThemeTokens.colors.textPrimary,
            modifier = Modifier.padding(horizontal = AppSpacing.marginMobile),
        )
        Text(
            text = text,
            style = AppThemeTokens.typography.bodySm,
            color = AppThemeTokens.colors.textSecondary,
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md)
                    .testTag("policy_font_text"),
        )
    }
}

private fun Context.openOssLicenses(
    title: String,
    colorScheme: ColorScheme,
    typography: Typography,
) {
    OssLicensesMenuActivity.setActivityTitle(title)
    OssLicensesMenuActivity.setTheme(colorScheme, colorScheme, typography)
    startActivity(Intent(this, OssLicensesMenuActivity::class.java))
}

@ScreenPreviews
@Composable
private fun PolicyListScreenPreview() {
    AppTheme { PolicyListScreen(state = PolicyListState(), onAction = {}) }
}
