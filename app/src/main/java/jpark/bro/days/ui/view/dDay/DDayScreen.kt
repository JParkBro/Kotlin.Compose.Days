package jpark.bro.days.ui.view.dDay

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import jpark.bro.days.ui.view.dDay.component.DDayListHeader
import jpark.bro.days.ui.view.dDay.component.DDayListItem
import jpark.bro.days.ui.viewmodel.DDAYViewModel

@Composable
fun DDayScreen(
    modifier: Modifier = Modifier,
    viewModel: DDAYViewModel = hiltViewModel(),
) {
    val currentItems by viewModel.filteredDDays.collectAsState(emptyList())
    val previousItems by viewModel.previousDDays.collectAsState(emptyList())
    val todayIndex by viewModel.scrollToIndex.collectAsState(0)

    val listState = rememberLazyListState()

    LaunchedEffect(currentItems) {
        if (currentItems != previousItems) {
            listState.scrollToItem(todayIndex)
            viewModel.updatePreviousDDays()
        }
    }

    Card(
        modifier =
            modifier
                .fillMaxSize(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = Color.White,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(16.dp),
        ) {
            DDayListHeader(viewModel = viewModel)
            LazyColumn(
                state = listState,
                modifier = Modifier,
            ) {
                items(currentItems) { item ->
                    DDayListItem(item = item)
                }
            }
        }
    }
}
