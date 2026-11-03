package jpark.bro.days.feature.settings.impl.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.days.core.data.auth.GoogleSignInClient
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysIconBadge
import jpark.bro.days.core.designsystem.component.DaysIconBadgeTone
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysListItem
import jpark.bro.days.core.designsystem.component.DaysSecondaryButton
import jpark.bro.days.core.designsystem.component.DaysSection
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.model.AuthProvider
import jpark.bro.days.core.model.AuthUser
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.settings.impl.R
import jpark.bro.days.feature.settings.impl.WithdrawKey
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun AccountRoot(
    onNavigate: (NavKey) -> Unit,
    onSessionEnded: () -> Unit,
    onBack: () -> Unit,
    viewModel: AccountViewModel = koinViewModel(),
    googleSignIn: GoogleSignInClient = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toastHostState = remember { SnackbarHostState() }
    val logoutFailed = stringResource(R.string.settings_account_logout_failed)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            AccountEvent.LoggedOut -> {
                // 다음 로그인에서 구글 계정을 다시 고르게 한다
                googleSignIn.clearCredentialState(context)
                onSessionEnded()
            }

            AccountEvent.LogoutFailed -> {
                toastHostState.showSnackbar(logoutFailed)
            }

            AccountEvent.OpenWithdraw -> {
                onNavigate(WithdrawKey)
            }

            AccountEvent.NavigateBack -> {
                onBack()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AccountScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 계정 관리 (product-spec 6장): 로그인 수단 · 로그아웃 · 회원 탈퇴 */
@Composable
private fun AccountScreen(
    state: AccountState,
    onAction: (AccountAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg),
    ) {
        DaysTopBar(
            title = stringResource(R.string.settings_account_title),
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.settings_account_back),
                    onClick = { onAction(AccountAction.BackClicked) },
                )
            },
        )
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            state.user?.let { user ->
                DaysSection(title = stringResource(R.string.settings_account_login_method)) {
                    DaysListItem(
                        title = stringResource(user.provider.label),
                        subtitle = user.email,
                        leading = { DaysIconBadge(icon = DesignR.drawable.ic_person, tone = DaysIconBadgeTone.SHARED) },
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                DaysSecondaryButton(
                    text = stringResource(R.string.settings_account_logout),
                    onClick = { onAction(AccountAction.LogoutClicked) },
                    enabled = !state.isSigningOut,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag("account_logout"),
                )
                Text(
                    text = stringResource(R.string.settings_account_logout_desc),
                    style = AppThemeTokens.typography.bodySm,
                    color = AppThemeTokens.colors.textSecondary,
                    modifier = Modifier.padding(horizontal = AppSpacing.xs),
                )
            }
            DaysListItem(
                title = stringResource(R.string.settings_account_withdraw),
                onClick = { onAction(AccountAction.WithdrawClicked) },
                modifier = Modifier.testTag("account_withdraw"),
            )
        }
    }
}

private val AuthProvider.label: Int
    get() =
        when (this) {
            AuthProvider.GOOGLE -> R.string.settings_account_provider_google
            AuthProvider.KAKAO -> R.string.settings_account_provider_kakao
        }

@ScreenPreviews
@Composable
private fun AccountScreenPreview() {
    AppTheme {
        AccountScreen(
            state = AccountState(user = AuthUser("google-1", AuthProvider.GOOGLE, email = "hyunwoo@example.com")),
            onAction = {},
        )
    }
}
