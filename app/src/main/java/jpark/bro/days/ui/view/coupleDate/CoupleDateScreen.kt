package jpark.bro.days.ui.view.coupleDate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import jpark.bro.days.core.util.DateUtil
import jpark.bro.days.ui.view.coupleDate.component.CoupleDateSummary
import jpark.bro.days.ui.view.coupleDate.component.CoupleProfile
import jpark.bro.days.ui.view.coupleDate.component.DaysDatePicker
import jpark.bro.days.ui.viewmodel.CoupleDateViewModel

@Composable
fun CoupleDateScreen(
    modifier: Modifier = Modifier,
    viewModel: CoupleDateViewModel = hiltViewModel(),
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val coupleDate by viewModel.coupleDate.collectAsState()
    val formattedCoupleDate by viewModel.formattedCoupleDate.collectAsState()
    val daysSinceStart by viewModel.daysSinceStart.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier)
        CoupleDateSummary(
            daysSinceStart = daysSinceStart,
            coupleDate = formattedCoupleDate,
            showDatePicker = { showDatePicker = true },
            modifier = Modifier,
        )
        CoupleProfile()
    }
    if(showDatePicker) {
        DaysDatePicker(
            initialDate = coupleDate ?: DateUtil.getCurrentLocalDate(),
            onDismiss = { showDatePicker = false },
            onDateSelected = { selectedDate ->
                viewModel.saveCoupleDate(selectedDate)
                showDatePicker = false
            }
        )
    }
}