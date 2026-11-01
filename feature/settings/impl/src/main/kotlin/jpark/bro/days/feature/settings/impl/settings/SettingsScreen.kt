package jpark.bro.days.feature.settings.impl.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.couple.api.CoupleManageKey
import jpark.bro.days.feature.settings.api.SettingsKey
import jpark.bro.days.feature.settings.impl.AccountKey
import jpark.bro.days.feature.settings.impl.LicenseDetailKey
import jpark.bro.days.feature.settings.impl.LicensesKey
import jpark.bro.days.feature.settings.impl.NotificationSettingsKey
import jpark.bro.days.feature.settings.impl.PolicyDocKey
import jpark.bro.days.feature.settings.impl.PolicyListKey
import jpark.bro.days.feature.settings.impl.PolicyType
import jpark.bro.days.feature.settings.impl.ProfileEditKey
import jpark.bro.days.feature.settings.impl.WidgetSettingsKey
import jpark.bro.days.feature.settings.impl.WithdrawKey
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun SettingsRoot(
    onNavigate: (NavKey) -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is SettingsEvent.Navigate -> {
                onNavigate(
                    when (event.destination) {
                        SettingsDestination.PROFILE -> ProfileEditKey
                        SettingsDestination.COUPLE -> CoupleManageKey
                        SettingsDestination.NOTIFICATION -> NotificationSettingsKey
                        SettingsDestination.WIDGET -> WidgetSettingsKey
                        SettingsDestination.POLICY -> PolicyListKey
                        SettingsDestination.ACCOUNT -> AccountKey
                    },
                )
            }
        }
    }

    SettingsScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

// 뼈대: 화면을 구현하면 Suppress를 지운다. 레이아웃 참고 docs/days/design/
@Suppress("UnusedParameter")
@Composable
private fun SettingsScreen(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            state = SettingsState(),
            onAction = {},
        )
    }
}
