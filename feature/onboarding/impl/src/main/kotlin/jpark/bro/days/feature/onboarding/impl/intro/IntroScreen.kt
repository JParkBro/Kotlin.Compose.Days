package jpark.bro.days.feature.onboarding.impl.intro

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.onboarding.impl.LoginKey
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun IntroRoot(
    onNavigate: (NavKey) -> Unit,
    viewModel: IntroViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            IntroEvent.NavigateToLogin -> onNavigate(LoginKey)
        }
    }

    IntroScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

// 뼈대: 화면을 구현하면 Suppress를 지운다. 레이아웃 참고 docs/days/design/
@Suppress("UnusedParameter")
@Composable
private fun IntroScreen(
    state: IntroState,
    onAction: (IntroAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}

@ScreenPreviews
@Composable
private fun IntroScreenPreview() {
    AppTheme {
        IntroScreen(
            state = IntroState(),
            onAction = {},
        )
    }
}
