package jpark.bro.days.ui.view.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import jpark.bro.days.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaysCustomTopBar(
    startContent: @Composable () -> Unit = {},
    centerContent: @Composable () -> Unit = {},
    endContent: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        ) {
            startContent()
        }
        Box(
            modifier = Modifier.align(Alignment.Center)
        ) {
            centerContent()
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            endContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaysDefaultTopBar(
    navController: NavController,
    title: String,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(
                onClick = { navController.navigateUp() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_24),
                    contentDescription = stringResource(R.string.arrow_back_icon)
                )
            }
        },
        modifier = modifier
    )
}