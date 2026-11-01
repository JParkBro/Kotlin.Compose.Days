package jpark.bro.days.feature.item.impl.itemdetail

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
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun ItemDetailRoot(
    key: ItemDetailKey,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
    viewModel: ItemDetailViewModel = koinViewModel { parametersOf(key) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is ItemDetailEvent.OpenEdit -> {
                onNavigate(
                    ItemEditKey(itemId = event.id, type = event.type, presetDate = null),
                )
            }

            ItemDetailEvent.NavigateBack -> {
                onBack()
            }
        }
    }

    ItemDetailScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

// 뼈대: 화면을 구현하면 Suppress를 지운다. 레이아웃 참고 docs/days/design/
@Suppress("UnusedParameter")
@Composable
private fun ItemDetailScreen(
    state: ItemDetailState,
    onAction: (ItemDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ItemDetailScreenPreview() {
    AppTheme {
        ItemDetailScreen(
            state = ItemDetailState(id = "preview"),
            onAction = {},
        )
    }
}
