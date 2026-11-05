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
import androidx.compose.runtime.rememberCoroutineScope
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
import jpark.bro.days.core.data.auth.KakaoSignInCancelledException
import jpark.bro.days.core.data.auth.KakaoSignInClient
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysKakaoButton
import jpark.bro.days.core.designsystem.component.DaysOutlineButton
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.onboarding.impl.R
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.days.core.designsystem.R as DesignR

/** 로그인 (product-spec 3-2: 카카오 · Google, 비로그인 사용 없음) */
@Composable
internal fun LoginRoot(
    onNeedsTerms: () -> Unit,
    onFinished: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
    googleSignIn: GoogleSignInClient = koinInject(),
    kakaoSignIn: KakaoSignInClient = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toastHostState = remember { SnackbarHostState() }
    // 로그인 창(카카오 웹 로그인의 Custom Tab 등)이 떠 있는 동안 화면이 STOP되면 ObserveEvents 수집이 취소된다.
    // 로그인 요청은 화면 수명 범위에서 따로 실행해 결과가 돌아올 때까지 기다린다
    val signInScope = rememberCoroutineScope()
    val loginFailed = stringResource(R.string.onboarding_login_failed)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            LoginEvent.RequestGoogleIdToken -> {
                signInScope.launch {
                    googleSignIn
                        .requestIdToken(context)
                        .onSuccess { viewModel.onAction(LoginAction.GoogleIdTokenReceived(it)) }
                        .onFailure {
                            viewModel.onAction(
                                LoginAction.GoogleIdTokenFailed(cancelled = it is GoogleSignInCancelledException),
                            )
                        }
                }
            }

            LoginEvent.RequestKakaoAccessToken -> {
                signInScope.launch {
                    kakaoSignIn
                        .requestAccessToken(context)
                        .onSuccess { viewModel.onAction(LoginAction.KakaoAccessTokenReceived(it)) }
                        .onFailure {
                            viewModel.onAction(
                                LoginAction.KakaoAccessTokenFailed(cancelled = it is KakaoSignInCancelledException),
                            )
                        }
                }
            }

            is LoginEvent.LoggedIn -> {
                if (event.termsAgreed) onFinished() else onNeedsTerms()
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
            // 로그인 중에는 두 버튼 모두 막고, 어느 쪽을 눌렀는지 구분하지 않는다
            DaysKakaoButton(
                text = stringResource(R.string.onboarding_login_kakao),
                onClick = { onAction(LoginAction.KakaoLoginClicked) },
                enabled = !state.isSigningIn,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("login_kakao"),
            )
            DaysOutlineButton(
                text = stringResource(R.string.onboarding_login_google),
                onClick = { onAction(LoginAction.GoogleLoginClicked) },
                enabled = !state.isSigningIn,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("login_google"),
            )
            if (state.isSigningIn) {
                Text(
                    text = stringResource(R.string.onboarding_login_signing_in),
                    style = AppThemeTokens.typography.bodySm,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
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
