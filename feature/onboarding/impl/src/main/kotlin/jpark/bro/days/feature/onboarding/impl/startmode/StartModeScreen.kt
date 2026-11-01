package jpark.bro.days.feature.onboarding.impl.startmode

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.onboarding.api.OnboardingResult
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun StartModeRoot(
    onFinished: (OnboardingResult) -> Unit,
    viewModel: StartModeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is StartModeEvent.Finished -> onFinished(event.result)
        }
    }

    StartModeScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

// 뼈대: 화면을 구현하면 Suppress를 지운다. 레이아웃 참고 docs/days/design/
@Suppress("UnusedParameter")
@Composable
private fun StartModeScreen(
    state: StartModeState,
    onAction: (StartModeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StartModeScreenPreview() {
    AppTheme {
        StartModeScreen(
            state = StartModeState(),
            onAction = {},
        )
    }
}
