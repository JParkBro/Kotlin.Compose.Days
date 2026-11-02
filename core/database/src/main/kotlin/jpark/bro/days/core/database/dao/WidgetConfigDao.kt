package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import jpark.bro.days.core.database.entity.WidgetConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetConfigDao {
    @Query("SELECT * FROM widget_config ORDER BY widget_id")
    fun observeConfigs(): Flow<List<WidgetConfigEntity>>

    @Query("SELECT * FROM widget_config WHERE widget_id = :widgetId")
    suspend fun getConfig(widgetId: Int): WidgetConfigEntity?

    @Upsert
    suspend fun upsertConfig(config: WidgetConfigEntity)

    /** 홈 화면에서 위젯을 지우면 설정도 지운다 */
    @Query("DELETE FROM widget_config WHERE widget_id IN (:widgetIds)")
    suspend fun deleteConfigs(widgetIds: List<Int>)
}
