package jpark.bro.days.ui.view.helpCenter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import jpark.bro.days.BuildConfig
import jpark.bro.days.R
import jpark.bro.days.ui.view.common.DaysDefaultTopBar

@Composable
fun HelpCenterScreen(navController: NavController) {
    Scaffold(
        topBar = {
            DaysDefaultTopBar(
                navController = navController,
                title = stringResource(R.string.help_center),
            )
        },
        modifier =
            Modifier
                .fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            Column {
                HorizontalDivider()
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    navController.navigate("announcements")
                                },
                            ).padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.circle_notifications_24),
                        contentDescription = stringResource(R.string.circle_notifications_icon),
                    )
                    Text(
                        text = stringResource(R.string.announcements),
                        modifier =
                            Modifier
                                .padding(horizontal = 8.dp),
                        fontSize = 18.sp,
                    )
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    navController.navigate("policy")
                                },
                            ).padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_help_outline_24),
                        contentDescription = stringResource(R.string.help_outline_icon),
                    )
                    Text(
                        text = stringResource(R.string.policy),
                        modifier =
                            Modifier
                                .padding(horizontal = 8.dp),
                        fontSize = 18.sp,
                    )
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.info_outline_24),
                        contentDescription = stringResource(R.string.info_outline_icon),
                    )
                    Text(
                        text = stringResource(R.string.app_version),
                        modifier =
                            Modifier
                                .padding(horizontal = 8.dp),
                        fontSize = 18.sp,
                    )
                }
                Text(
                    text = BuildConfig.VERSION_NAME,
                    modifier =
                        Modifier
                            .padding(horizontal = 48.dp),
                    fontSize = 14.sp,
                    color = Color.Gray,
                )
            }
        }
    }
}
