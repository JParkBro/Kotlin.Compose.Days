package jpark.bro.days.feature.settings.impl.moreapps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
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
import jpark.bro.days.feature.settings.impl.R
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.days.core.designsystem.R as DesignR

/** 개발자의 다른 앱 소개 페이지. from = 이 앱(목록에서 빼고 유입 경로로 씀), theme = 앱 테마 */
private const val MORE_APPS_URL = "https://jparkbro-apps.jparkbro.workers.dev/"

/** dev 빌드 패키지 접미사 (build-logic Flavors). 페이지는 출시 패키지 이름으로 이 앱을 목록에서 뺀다 */
private const val DEV_PACKAGE_SUFFIX = ".dev"

@Composable
internal fun MoreAppsRoot(
    onBack: () -> Unit,
    viewModel: MoreAppsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val dark = LocalIsDarkTheme.current
    val url =
        remember(context, dark) {
            Uri
                .parse(MORE_APPS_URL)
                .buildUpon()
                .appendQueryParameter("from", context.packageName.removeSuffix(DEV_PACKAGE_SUFFIX))
                .appendQueryParameter("theme", if (dark) "dark" else "light")
                .build()
                .toString()
        }

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is MoreAppsEvent.OpenStore -> context.openStore(event.url)
            MoreAppsEvent.NavigateBack -> onBack()
        }
    }

    MoreAppsScreen(
        state = state,
        onAction = viewModel::onAction,
        page = { modifier ->
            DaysWebView(
                url = url,
                onPageLoaded = { viewModel.onAction(MoreAppsAction.PageLoaded) },
                onPageFailed = { viewModel.onAction(MoreAppsAction.PageFailed) },
                onStoreLink = { viewModel.onAction(MoreAppsAction.StoreLinkClicked(it)) },
                reloadKey = state.reloadCount,
                modifier = modifier,
            )
        },
    )
}

/** [page]는 웹뷰 자리 (Preview에서는 비워 둔다) */
@Composable
private fun MoreAppsScreen(
    state: MoreAppsState,
    onAction: (MoreAppsAction) -> Unit,
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
            title = stringResource(R.string.settings_more_apps_title),
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.settings_more_apps_back),
                    onClick = { onAction(MoreAppsAction.BackClicked) },
                )
            },
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            // 실패 · 로딩 중에도 웹뷰는 유지한다 (다시 시도할 때 같은 웹뷰로 불러온다)
            page(
                Modifier
                    .fillMaxSize()
                    .alpha(if (state.isLoading || state.isFailed) 0f else 1f)
                    .testTag("more_apps_page"),
            )
            when {
                state.isFailed -> FailedMessage(onRetry = { onAction(MoreAppsAction.RetryClicked) })
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
            text = stringResource(R.string.settings_more_apps_failed),
            style = AppThemeTokens.typography.bodyMd,
            color = AppThemeTokens.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        DaysSoftButton(
            text = stringResource(R.string.settings_more_apps_retry),
            onClick = onRetry,
            size = DaysButtonSize.SMALL,
            modifier = Modifier.testTag("more_apps_retry"),
        )
    }
}

/** 스토어 앱이 없으면 브라우저로 */
private fun Context.openStore(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage(PLAY_STORE_PACKAGE)
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

private const val PLAY_STORE_PACKAGE = "com.android.vending"

@ScreenPreviews
@Composable
private fun MoreAppsLoadingPreview() {
    AppTheme { MoreAppsScreen(state = MoreAppsState(), onAction = {}) }
}

@ScreenPreviews
@Composable
private fun MoreAppsFailedPreview() {
    AppTheme { MoreAppsScreen(state = MoreAppsState(isLoading = false, isFailed = true), onAction = {}) }
}
