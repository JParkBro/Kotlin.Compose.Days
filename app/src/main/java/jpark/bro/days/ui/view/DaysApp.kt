package jpark.bro.days.ui.view

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import jpark.bro.days.R
import jpark.bro.days.ui.view.common.DaysCustomTopBar
import jpark.bro.days.ui.view.coupleDate.CoupleDateScreen
import jpark.bro.days.ui.view.dDay.DDayScreen
import kotlinx.coroutines.launch

@Composable
fun DaysApp(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState { 2 }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0xFFC9D8FF)),
    ) {
        DaysHeader(
            navController = navController,
            pagerState = pagerState,
        )
        DaysTabRow(
            pagerState = pagerState,
        )
        DaysPager(
            pagerState = pagerState,
        )
    }
}

@Composable
fun DaysHeader(
    navController: NavController,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val title =
        if (pagerState.currentPage ==
            0
        ) {
            stringResource(R.string.our_love_timeline)
        } else {
            stringResource(R.string.moments_we_share)
        }
    DaysCustomTopBar(
        startContent = {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        },
        endContent = {
            Row {
                IconButton(
                    onClick = {
                        navController.navigate("helpCenter")
                    },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_help_outline_24),
                        contentDescription = stringResource(R.string.help_outline_icon),
                        tint = Color.White,
                    )
                }
            }
        },
        modifier = modifier,
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DaysTabRow(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val tabTitles =
        listOf(
            stringResource(R.string.couple_tab),
            stringResource(R.string.d_day_tab),
        )

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
    ) {
        val calculatedWidth = maxWidth * 0.5f
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            indicator = {},
            divider = {},
            modifier =
                modifier
                    .wrapContentSize()
                    .width(calculatedWidth),
        ) {
            tabTitles.forEachIndexed { index, title ->
                val isSelected = pagerState.currentPage == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    modifier =
                        Modifier
                            .height(32.dp)
                            .border(
                                width = 2.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(16.dp),
                            ).clip(shape = RoundedCornerShape(16.dp)),
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF666666),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
fun DaysPager(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(
        state = pagerState,
        modifier =
            modifier
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .fillMaxSize(),
    ) { page: Int ->
        when (page) {
            0 -> CoupleDateScreen()
            1 -> DDayScreen()
        }
    }
}
