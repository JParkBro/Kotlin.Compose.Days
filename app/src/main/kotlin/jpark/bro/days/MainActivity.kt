package jpark.bro.days

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.navigation.DaysApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 지금은 기본 테마(핑크 · 시스템 모드). 선택한 테마 · 모드 저장은 core:datastore에서 연결한다 (theme-guide.md)
            AppTheme {
                DaysApp()
            }
        }
    }
}
