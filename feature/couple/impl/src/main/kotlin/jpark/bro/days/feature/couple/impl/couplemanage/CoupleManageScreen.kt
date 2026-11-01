package jpark.bro.days.feature.couple.impl.couplemanage

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
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun CoupleManageRoot(
    onBack: () -> Unit,
    viewModel: CoupleManageViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            CoupleManageEvent.NavigateBack -> onBack()
        }
    }

    CoupleManageScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

// 뼈대: 화면을 구현하면 Suppress를 지운다. 레이아웃 참고 docs/days/design/
@Suppress("UnusedParameter")
@Composable
private fun CoupleManageScreen(
    state: CoupleManageState,
    onAction: (CoupleManageAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CoupleManageScreenPreview() {
    AppTheme {
        CoupleManageScreen(
            state = CoupleManageState(),
            onAction = {},
        )
    }
}
