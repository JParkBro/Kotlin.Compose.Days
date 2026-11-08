package jpark.bro.days.feature.settings.impl.policydoc

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.LocalIsDarkTheme
import jpark.bro.days.core.designsystem.component.DaysButtonSize
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.ui.DaysWebView
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.settings.api.PolicyDocKey
import jpark.bro.days.feature.settings.api.PolicyType
import jpark.bro.days.feature.settings.impl.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import jpark.bro.days.core.designsystem.R as DesignR

/** Firebase Hosting에 배포한 정책 문서 (`policy-site/`, infra-and-billing-protection.md 2-4) */
private const val POLICY_SITE_URL = "https://days-61328.web.app/"

@Composable
internal fun PolicyDocRoot(
    key: PolicyDocKey,
    onBack: () -> Unit,
    viewModel: PolicyDocViewModel = koinViewModel { parametersOf(key) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val dark = LocalIsDarkTheme.current
    val url =
        remember(state.type, dark) {
            Uri
                .parse(POLICY_SITE_URL)
                .buildUpon()
                .appendPath(state.type.path)
                .appendQueryParameter("theme", if (dark) "dark" else "light")
                .build()
                .toString()
        }

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            PolicyDocEvent.NavigateBack -> onBack()
        }
    }

    PolicyDocScreen(
        state = state,
        onAction = viewModel::onAction,
        page = { modifier ->
            DaysWebView(
                url = url,
                onPageLoaded = { viewModel.onAction(PolicyDocAction.PageLoaded) },
                onPageFailed = { viewModel.onAction(PolicyDocAction.PageFailed) },
                // 정책 문서에는 스토어 링크가 없다. 다른 사이트 링크는 웹뷰가 막는다
                onStoreLink = {},
                reloadKey = state.reloadCount,
                modifier = modifier,
            )
        },
    )
}

/** [page]는 웹뷰 자리 (Preview에서는 비워 둔다) */
@Composable
private fun PolicyDocScreen(
    state: PolicyDocState,
    onAction: (PolicyDocAction) -> Unit,
    modifier: Modifier = Modifier,
    page: @Composable (Modifier) -> Unit = {},
) {
    val colors = AppThemeTokens.colors
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.bg),
    ) {
        DaysTopBar(
            title = stringResource(state.type.title),
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.settings_policy_back),
                    onClick = { onAction(PolicyDocAction.BackClicked) },
                )
            },
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            // 실패 · 로딩 중에도 웹뷰는 유지한다 (다시 시도할 때 같은 웹뷰로 불러온다)
            page(
                Modifier
                    .fillMaxSize()
                    .alpha(if (state.isLoading || state.isFailed) 0f else 1f)
                    .testTag("policy_doc_page"),
            )
            when {
                state.isFailed -> FailedMessage(onRetry = { onAction(PolicyDocAction.RetryClicked) })
                state.isLoading -> CircularProgressIndicator(color = colors.primary)
                else -> Unit
            }
        }
    }
}

@Composable
private fun FailedMessage(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.padding(AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.settings_policy_doc_failed),
            style = AppThemeTokens.typography.bodyMd,
            color = AppThemeTokens.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        DaysSoftButton(
            text = stringResource(R.string.settings_policy_doc_retry),
            onClick = onRetry,
            size = DaysButtonSize.SMALL,
            modifier = Modifier.testTag("policy_doc_retry"),
        )
    }
}

/** `policy-site/public`의 페이지 이름 (cleanUrls) */
private val PolicyType.path: String
    get() =
        when (this) {
            PolicyType.PRIVACY -> "privacy"
            PolicyType.TERMS -> "terms"
        }

private val PolicyType.title: Int
    get() =
        when (this) {
            PolicyType.PRIVACY -> R.string.settings_policy_privacy
            PolicyType.TERMS -> R.string.settings_policy_terms
        }

@ScreenPreviews
@Composable
private fun PolicyDocLoadingPreview() {
    AppTheme { PolicyDocScreen(state = PolicyDocState(type = PolicyType.PRIVACY), onAction = {}) }
}

@ScreenPreviews
@Composable
private fun PolicyDocFailedPreview() {
    AppTheme {
        PolicyDocScreen(
            state = PolicyDocState(type = PolicyType.TERMS, isLoading = false, isFailed = true),
            onAction = {},
        )
    }
}
