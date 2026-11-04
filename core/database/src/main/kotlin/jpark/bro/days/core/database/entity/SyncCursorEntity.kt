package jpark.bro.days.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 변경분 조회 커서 (sync-and-pairing.md 2장 Pull). Room에만 둔다.
 * [lastPulledAtMicros]는 받은 문서 중 가장 큰 서버 시각. Firestore 서버 시각은 마이크로초 단위라
 * 밀리초로 자르면 같은 문서를 매번 다시 읽게 되므로(읽기 과금) 이 컬럼만 마이크로초로 둔다.
 */
@Entity(tableName = "sync_cursor")
data class SyncCursorEntity(
    /** 예: `items:{coupleId}`, `hiddenAnniversaries:{coupleId}` */
    @PrimaryKey val key: String,
    @ColumnInfo(name = "last_pulled_at_micros") val lastPulledAtMicros: Long,
)
