package jpark.bro.days.ui.view.helpCenter.policy

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import jpark.bro.days.ui.view.common.DaysDefaultTopBar
import jpark.bro.days.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyScreen(
    navController: NavController
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            DaysDefaultTopBar(
                navController = navController,
                title = stringResource(R.string.policy)
            )
        },
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClick = {
                                val uri = "https://sites.google.com/view/jparkbro-days"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
                            },
                        )
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.privacy_policy),
                        modifier = Modifier,
                        fontSize = 18.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClick = {
                                OssLicensesMenuActivity.setActivityTitle(context.getString(R.string.oss_licenses))
                                context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                            },
                        )
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.oss_licenses),
                        modifier = Modifier,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}