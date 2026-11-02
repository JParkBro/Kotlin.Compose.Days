package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

    /** 이미 있으면 그대로 둔다 (위젯이 놓일 때 기본값 저장 · 추가 요청 콜백이 동시에 와도 고른 디데이를 지키기 위해) */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConfigIfAbsent(config: WidgetConfigEntity)

    /** 홈 화면에서 위젯을 지우면 설정도 지운다 */
    @Query("DELETE FROM widget_config WHERE widget_id IN (:widgetIds)")
    suspend fun deleteConfigs(widgetIds: List<Int>)
}
