package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import jpark.bro.days.core.database.entity.SyncCursorEntity

@Dao
interface SyncCursorDao {
    /** 없으면 null (처음부터 받는다) */
    @Query("SELECT last_pulled_at_micros FROM sync_cursor WHERE `key` = :key")
    suspend fun getCursor(key: String): Long?

    @Upsert
    suspend fun upsertCursor(cursor: SyncCursorEntity)
}
