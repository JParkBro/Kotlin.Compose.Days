package jpark.bro.days.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 홈 화면 위젯 설정 (data-model-erd 3장, Room 전용).
 * widget_id는 시스템이 주는 appWidgetId라 UUID가 아니다 (기기 밖으로 나가지 않는 값).
 */
@Entity(tableName = "widget_config")
data class WidgetConfigEntity(
    @PrimaryKey @ColumnInfo(name = "widget_id") val widgetId: Int,
    /** null이면 홈 히어로와 같은 규칙. item은 soft delete라 FK를 걸지 않는다 */
    @ColumnInfo(name = "item_id") val itemId: String?,
    /** SMALL | MEDIUM */
    val size: String,
    /** THEME | DARK | WHITE */
    @ColumnInfo(name = "background_type") val backgroundType: String,
    @ColumnInfo(name = "show_profile") val showProfile: Boolean = false,
)
