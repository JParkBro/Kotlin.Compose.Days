package jpark.bro.days.feature.settings.impl.widgetsettings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.ui.ObserveEvents
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun WidgetSettingsRoot(
    onBack: () -> Unit,
    viewModel: WidgetSettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            WidgetSettingsEvent.NavigateBack -> onBack()
        }
    }

    WidgetSettingsScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

// 뼈대: 화면을 구현하면 Suppress를 지운다. 레이아웃 참고 docs/days/design/
@Suppress("UnusedParameter")
@Composable
private fun WidgetSettingsScreen(
    state: WidgetSettingsState,
    onAction: (WidgetSettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}

@ScreenPreviews
@Composable
private fun WidgetSettingsScreenPreview() {
    AppTheme {
        WidgetSettingsScreen(
            state = WidgetSettingsState(),
            onAction = {},
        )
    }
}
