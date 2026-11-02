package jpark.bro.days.feature.couple.impl.pairing

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
import jpark.bro.days.feature.couple.impl.PairCodeConfirmKey
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PairingRoot(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
    viewModel: PairingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is PairingEvent.NavigateToConfirm -> onNavigate(PairCodeConfirmKey(event.code))
            PairingEvent.NavigateBack -> onBack()
        }
    }

    PairingScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

// 뼈대: 화면을 구현하면 Suppress를 지운다. 레이아웃 참고 docs/days/design/
@Suppress("UnusedParameter")
@Composable
private fun PairingScreen(
    state: PairingState,
    onAction: (PairingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}

@ScreenPreviews
@Composable
private fun PairingScreenPreview() {
    AppTheme {
        PairingScreen(
            state = PairingState(),
            onAction = {},
        )
    }
}
