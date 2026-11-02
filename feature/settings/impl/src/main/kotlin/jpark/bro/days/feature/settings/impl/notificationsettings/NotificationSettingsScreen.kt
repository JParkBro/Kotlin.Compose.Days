package jpark.bro.days.feature.settings.impl.notificationsettings

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
internal fun NotificationSettingsRoot(
    onBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            NotificationSettingsEvent.NavigateBack -> onBack()
        }
    }

    NotificationSettingsScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

// 뼈대: 화면을 구현하면 Suppress를 지운다. 레이아웃 참고 docs/days/design/
@Suppress("UnusedParameter")
@Composable
private fun NotificationSettingsScreen(
    state: NotificationSettingsState,
    onAction: (NotificationSettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}

@ScreenPreviews
@Composable
private fun NotificationSettingsScreenPreview() {
    AppTheme {
        NotificationSettingsScreen(
            state = NotificationSettingsState(),
            onAction = {},
        )
    }
}
