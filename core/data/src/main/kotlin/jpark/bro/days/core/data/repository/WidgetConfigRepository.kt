package jpark.bro.days.core.data.repository

import jpark.bro.days.core.model.WidgetConfig
import kotlinx.coroutines.flow.Flow

/** 홈 화면 위젯 설정 (기기에만 저장, 서버로 보내지 않음) */
interface WidgetConfigRepository {
    fun observeConfigs(): Flow<List<WidgetConfig>>

    /** 설정한 적 없는 위젯이면 null */
    suspend fun getConfig(widgetId: Int): WidgetConfig?

    suspend fun saveConfig(config: WidgetConfig): Result<Unit>

    suspend fun deleteConfigs(widgetIds: List<Int>): Result<Unit>
}
