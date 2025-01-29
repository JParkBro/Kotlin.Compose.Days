package jpark.bro.days

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import jpark.bro.days.ui.theme.DaysTheme
import jpark.bro.days.ui.view.common.NavGraph

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DaysTheme {
                val navController = rememberNavController()

                NavGraph(
                    navController = navController
                )
            }
        }
    }
}