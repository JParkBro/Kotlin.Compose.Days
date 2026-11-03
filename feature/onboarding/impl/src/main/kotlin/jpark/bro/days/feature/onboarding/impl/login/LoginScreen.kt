package jpark.bro.days.feature.onboarding.impl.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.data.auth.GoogleSignInCancelledException
import jpark.bro.days.core.data.auth.GoogleSignInClient
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysOutlineButton
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.onboarding.impl.R
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.days.core.designsystem.R as DesignR

/** 로그인 (product-spec 3-2: 카카오 · Google, 비로그인 사용 없음). 카카오는 6-4에서 붙인다 */
@Composable
internal fun LoginRoot(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
    googleSignIn: GoogleSignInClient = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toastHostState = remember { SnackbarHostState() }
    val loginFailed = stringResource(R.string.onboarding_login_failed)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            LoginEvent.RequestGoogleIdToken -> {
                googleSignIn
                    .requestIdToken(context)
                    .onSuccess { viewModel.onAction(LoginAction.GoogleIdTokenReceived(it)) }
                    .onFailure {
                        viewModel.onAction(
                            LoginAction.GoogleIdTokenFailed(cancelled = it is GoogleSignInCancelledException),
                        )
                    }
            }

            LoginEvent.LoggedIn -> {
                onLoggedIn()
            }

            LoginEvent.LoginFailed -> {
                toastHostState.showSnackbar(loginFailed)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LoginScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.bg)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier =
                Modifier
                    .size(96.dp)
                    .clip(AppShapes.xl)
                    .background(colors.primarySoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_favorite),
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(48.dp),
            )
        }
        Spacer(modifier = Modifier.padding(top = AppSpacing.lg))
        Text(
            text = stringResource(R.string.onboarding_login_app_name),
            style = AppThemeTokens.typography.headlineLg,
            color = colors.textPrimary,
        )
        Text(
            text = stringResource(R.string.onboarding_login_subtitle),
            style = AppThemeTokens.typography.bodyMd,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = AppSpacing.sm),
        )
        Spacer(modifier = Modifier.weight(1f))
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            val googleLabel =
                if (state.isSigningIn) R.string.onboarding_login_signing_in else R.string.onboarding_login_google
            DaysOutlineButton(
                text = stringResource(googleLabel),
                onClick = { onAction(LoginAction.GoogleLoginClicked) },
                enabled = !state.isSigningIn,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("login_google"),
            )
        }
    }
}

@ScreenPreviews
@Composable
private fun LoginScreenPreview() {
    AppTheme { LoginScreen(state = LoginState(), onAction = {}) }
}

@ScreenPreviews
@Composable
private fun LoginSigningInPreview() {
    AppTheme { LoginScreen(state = LoginState(isSigningIn = true), onAction = {}) }
}
