package jpark.bro.days

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.isDarkTheme
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.navigation.AppIntents
import jpark.bro.days.core.navigation.Navigator
import jpark.bro.days.feature.home.api.HomeKey
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey
import jpark.bro.days.navigation.DaysApp
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()
    private val navigator: Navigator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 회전 등으로 다시 만들어질 때는 이미 이동한 백스택을 유지한다
        if (savedInstanceState == null) openItemFrom(intent)
        enableEdgeToEdge()
        setContent {
            val theme by viewModel.themeSetting.collectAsStateWithLifecycle()
            val dark = isDarkTheme(theme.mode)

            // 앱에서 고른 화면 모드에 맞춰 상태 표시줄 · 내비게이션 바 아이콘 색을 바꾼다
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                )
                onDispose {}
            }

            AppTheme(type = theme.type, mode = theme.mode) {
                DaysApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openItemFrom(intent)
    }

    /** 위젯 · 알림에서 열면 [홈, 항목 상세] 백스택으로 (module-map 10장). 일정은 상세가 없어 수정 화면으로 (product-spec 4-5) */
    private fun openItemFrom(intent: Intent) {
        val itemId = intent.getStringExtra(AppIntents.EXTRA_OPEN_ITEM_ID) ?: return
        val isPlan = intent.getStringExtra(AppIntents.EXTRA_OPEN_ITEM_TYPE) == ItemType.PLAN.name
        intent.removeExtra(AppIntents.EXTRA_OPEN_ITEM_ID)
        intent.removeExtra(AppIntents.EXTRA_OPEN_ITEM_TYPE)
        navigator.replaceAll(HomeKey)
        val target = if (isPlan) ItemEditKey(itemId, ItemType.PLAN, presetDate = null) else ItemDetailKey(itemId)
        navigator.goTo(target)
    }
}
