package jpark.bro.days.ui.view.helpCenter.announcements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import jpark.bro.days.ui.view.common.DaysDefaultTopBar
import jpark.bro.days.R

@Composable
fun AnnouncementsScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            DaysDefaultTopBar(
                navController = navController,
                title = stringResource(R.string.announcements)
            )
        },
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(R.string.no_announcements))
        }
    }
}