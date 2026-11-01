package jpark.bro.days.feature.settings.impl.policydoc

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.feature.settings.impl.PolicyDocKey
import jpark.bro.days.feature.settings.impl.PolicyType
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun PolicyDocRoot(
    key: PolicyDocKey,
    onBack: () -> Unit,
    viewModel: PolicyDocViewModel = koinViewModel { parametersOf(key) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PolicyDocScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

// 뼈대: 화면을 구현하면 Suppress를 지운다. 레이아웃 참고 docs/days/design/
@Suppress("UnusedParameter")
@Composable
private fun PolicyDocScreen(
    state: PolicyDocState,
    onAction: (PolicyDocAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PolicyDocScreenPreview() {
    AppTheme {
        PolicyDocScreen(
            state = PolicyDocState(type = PolicyType.PRIVACY),
            onAction = {},
            onBack = {},
        )
    }
}
