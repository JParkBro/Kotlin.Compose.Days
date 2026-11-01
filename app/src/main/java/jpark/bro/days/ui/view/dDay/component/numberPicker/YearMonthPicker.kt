package jpark.bro.days.ui.view.dDay.component.numberPicker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jpark.bro.days.ui.view.dDay.component.numberPicker.util.MONTH_RANGE
import jpark.bro.days.ui.view.dDay.component.numberPicker.util.YEAR_RANGE
import jpark.bro.days.ui.view.dDay.component.numberPicker.util.currentDate

@Composable
fun YearMonthPicker(
    modifier: Modifier = Modifier,
    yearPickerState: PickerState<Int> = rememberPickerState(currentDate.year),
    monthPickerState: PickerState<Int> = rememberPickerState(currentDate.monthNumber),
    yearItems: List<Int> = YEAR_RANGE,
    monthItems: List<Int> = MONTH_RANGE,
    textStyle: TextStyle = TextStyle(fontSize = 16.sp),
    selectedTextStyle: TextStyle = TextStyle(fontSize = 24.sp),
    spacingBetweenPickers: Dp = 20.dp,
    pickerWidth: Dp = 100.dp,
    initialYear: Int,
    initialMonth: Int,
    onYearSelected: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit,
) {
    Surface(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val yearStartIndex =
                remember {
                    yearItems.indexOf(initialYear)
                }
            val monthStartIndex =
                remember {
                    monthItems.indexOf(initialMonth)
                }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        spacingBetweenPickers,
                        Alignment.CenterHorizontally,
                    ),
            ) {
                DaysPicker(
                    state = yearPickerState,
                    modifier = Modifier.width(pickerWidth),
                    items = yearItems,
                    startIndex = yearStartIndex,
                    textModifier = Modifier.padding(PaddingValues(8.dp)),
                    textStyle = textStyle,
                    selectedTextStyle = selectedTextStyle,
                    onSelectedChange = { selectedItem ->
                        selectedItem?.let { onYearSelected(it) }
                    },
                )
                DaysPicker(
                    state = monthPickerState,
                    items = monthItems,
                    startIndex = monthStartIndex,
                    modifier = Modifier.width(pickerWidth),
                    textStyle = textStyle,
                    selectedTextStyle = selectedTextStyle,
                    textModifier = Modifier.padding(PaddingValues(8.dp)),
                    onSelectedChange = { selectedItem ->
                        selectedItem?.let { onMonthSelected(it) }
                    },
                )
            }
        }
    }
}
