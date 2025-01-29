package jpark.bro.days.ui.view.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jpark.bro.days.R

@Composable
fun HorizontalIndicator(
    pagerState: PagerState
) {
    Row(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + dimensionResource(R.dimen.small_padding)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pagerState.pageCount) { iteration ->
            val isSelected = pagerState.currentPage == iteration
            val color = if (isSelected) Color.DarkGray else Color.DarkGray.copy(alpha = 0.5f)
            val size = if (isSelected) 12.dp else 8.dp
            Box(
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.indicator_padding))
                    .clip(CircleShape)
                    .background(color)
                    .size(size)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HorizontalIndicatorPreview() {
    val pagerState = rememberPagerState { 3 }
    HorizontalIndicator(pagerState)
}