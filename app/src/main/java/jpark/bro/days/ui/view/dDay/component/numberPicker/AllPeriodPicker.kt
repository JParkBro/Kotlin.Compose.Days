package jpark.bro.days.ui.view.dDay.component.numberPicker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AllPeriodPicker(
    selectedTextStyle: TextStyle = TextStyle(fontSize = 24.sp, color = Color.Black),
    itemPadding: PaddingValues = PaddingValues(8.dp),
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    val itemHeight = with(density) {
        selectedTextStyle.fontSize.toDp() + itemPadding.calculateTopPadding() + itemPadding.calculateBottomPadding()
    }

    Box(
        modifier = modifier
            .height(itemHeight * 3)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(itemHeight)
                .background(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                )
        )
        Text(
            text = "전체",
            style = selectedTextStyle
        )
    }
}

@Composable
@Preview(showBackground = true)
fun AllPeriodPickerPreview(){
    AllPeriodPicker()
}