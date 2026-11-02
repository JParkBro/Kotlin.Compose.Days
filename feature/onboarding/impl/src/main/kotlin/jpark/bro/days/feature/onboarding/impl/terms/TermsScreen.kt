package jpark.bro.days.feature.onboarding.impl.terms

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
import jpark.bro.days.feature.onboarding.impl.StartModeKey
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun TermsRoot(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
    viewModel: TermsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            TermsEvent.NavigateToStartMode -> onNavigate(StartModeKey)
            TermsEvent.NavigateBack -> onBack()
        }
    }

    TermsScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

// 뼈대: 화면을 구현하면 Suppress를 지운다. 레이아웃 참고 docs/days/design/
@Suppress("UnusedParameter")
@Composable
private fun TermsScreen(
    state: TermsState,
    onAction: (TermsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}

@ScreenPreviews
@Composable
private fun TermsScreenPreview() {
    AppTheme {
        TermsScreen(
            state = TermsState(),
            onAction = {},
        )
    }
}
