package jpark.bro.days.widget.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.model.ThemeSetting
import jpark.bro.days.core.model.WidgetSize
import jpark.bro.days.widget.providerOf
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * 위젯 하나의 설정 화면. 런처(위젯 길게 눌러 설정 · Android 11에서 놓을 때)와 앱 설정의 위젯 목록에서 연다.
 * 런처가 열 수 있도록 내보내므로, 넘어온 위젯 id가 이 앱 위젯인지 확인하고 아니면 바로 닫는다.
 */
class WidgetConfigActivity : ComponentActivity() {
    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val size = widgetSizeOf(widgetId)
        if (size == null) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        // 닫아도 위젯은 기본 설정(홈 히어로 규칙)으로 놓이게 한다 (product-spec 8장 "바로 놓기")
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))

        val viewModel: WidgetConfigViewModel by viewModel { parametersOf(widgetId, size) }
        enableEdgeToEdge()
        setContent {
            val theme by settingsRepository.observeThemeSetting().collectAsStateWithLifecycle(ThemeSetting())
            AppTheme(type = theme.type, mode = theme.mode) {
                WidgetConfigRoot(viewModel = viewModel, onClose = ::finish)
            }
        }
    }

    /** 이 앱의 위젯이면 크기, 아니면 null */
    private fun widgetSizeOf(widgetId: Int): WidgetSize? {
        // 잘못된 id면 getAppWidgetInfo가 null을 돌려준다
        val provider = AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetId)?.provider
        return WidgetSize.entries.firstOrNull { provider != null && providerOf(it) == provider }
    }
}
