package jpark.bro.days.ui.view.dDay.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import jpark.bro.days.R
import jpark.bro.days.core.util.DateUtil
import jpark.bro.days.ui.state.FilterOption
import jpark.bro.days.ui.view.dDay.component.numberPicker.AllPeriodPicker
import jpark.bro.days.ui.view.dDay.component.numberPicker.YearMonthPicker
import jpark.bro.days.ui.view.dDay.component.numberPicker.YearPicker
import jpark.bro.days.ui.viewmodel.DDAYViewModel

@Composable
fun DDayListHeader(
    viewModel: DDAYViewModel
){
    var showDialog by remember { mutableStateOf(false) }
    val selectedOption by viewModel.selectedOption.collectAsState(FilterOption.All)

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { showDialog = true }
                )
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_expand_more_24),
                contentDescription = stringResource(R.string.expand_more_icon)
            )
            Text(
                text = selectedOption.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
    if (showDialog) {
        SelectDateRangeDialog(
            selectedOption = selectedOption,
            onConfirm = { newOption ->
                viewModel.updateSelectedOption(newOption)
                showDialog = false
                        },
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
fun SelectDateRangeDialog(
    selectedOption: FilterOption,
    onConfirm: (FilterOption) -> Unit,
    onDismiss: () -> Unit
) {
    val onOptionSelected = remember { mutableStateOf(selectedOption) }
    val selectedYear = remember {
        mutableIntStateOf(
            when (selectedOption) {
                is FilterOption.Yearly -> selectedOption.year
                is FilterOption.Monthly -> selectedOption.year
                else -> DateUtil.getCurrentLocalDate().year
            }
        )
    }
    val selectedMonth = remember { mutableIntStateOf(
        (selectedOption as? FilterOption.Monthly)?.month ?: DateUtil.getCurrentLocalDate().monthNumber)
    }

    val filterOptions = listOf(
        FilterOption.All,
        FilterOption.Yearly(selectedYear.intValue),
        FilterOption.Monthly(selectedYear.intValue, selectedMonth.intValue)
    )

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(16.dp)
                .width(300.dp)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
            ) {
                TabRow(
                    containerColor = Color.Transparent,
                    indicator = {},
                    divider = {},
                    selectedTabIndex = when (onOptionSelected.value) {
                        is FilterOption.All -> 0
                        is FilterOption.Yearly -> 1
                        is FilterOption.Monthly -> 2
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray.copy(alpha = 0.2f))
                ) {
                    filterOptions.forEach { filterOption ->
                        val isSelected = onOptionSelected.value == filterOption
                        Tab(
                            selected = isSelected,
                            onClick = { onOptionSelected.value = filterOption },
                            modifier = Modifier
                                .background(if (isSelected) Color.LightGray.copy(0.3f) else Color.Transparent)
                        ) {
                            Text(
                                text = when (filterOption) {
                                    is FilterOption.All -> stringResource(R.string.all_picker)
                                    is FilterOption.Yearly -> stringResource(R.string.year_picker)
                                    is FilterOption.Monthly -> stringResource(R.string.month_picker)
                                },
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (onOptionSelected.value) {
                        is FilterOption.All -> AllPeriodPicker()
                        is FilterOption.Yearly -> YearPicker(
                            initialYear = selectedYear.intValue,
                            onYearSelected = { selectedYear.intValue = it }
                        )
                        is FilterOption.Monthly -> YearMonthPicker(
                            initialYear = selectedYear.intValue,
                            initialMonth = selectedMonth.intValue,
                            onYearSelected = { selectedYear.intValue = it },
                            onMonthSelected = { selectedMonth.intValue = it }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { onDismiss() }) { Text(stringResource(R.string.cancel)) }
                    TextButton(onClick = {
                        onConfirm(
                            when (onOptionSelected.value) {
                                is FilterOption.All -> FilterOption.All
                                is FilterOption.Yearly -> FilterOption.Yearly(selectedYear.intValue)
                                is FilterOption.Monthly -> FilterOption.Monthly(selectedYear.intValue, selectedMonth.intValue)
                            } )
                    }) { Text(stringResource(R.string.confirm)) }
                }
            }
        }
    }
}