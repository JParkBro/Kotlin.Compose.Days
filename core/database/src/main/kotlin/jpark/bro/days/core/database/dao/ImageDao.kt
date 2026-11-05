package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import jpark.bro.days.core.database.entity.ImageEntity
import kotlinx.coroutines.flow.Flow

/** 사진 (화면 · 저장소용). 올리기 · 지우기 · 받은 사진은 [ImageSyncDao] */
@Dao
interface ImageDao {
    @Query("SELECT * FROM image WHERE id = :id")
    fun observeImage(id: String): Flow<ImageEntity?>

    @Query("SELECT * FROM image WHERE id = :id")
    suspend fun getImage(id: String): ImageEntity?

    @Upsert
    suspend fun upsertImage(image: ImageEntity)

    /** 기기 파일이 사라짐 (사용자가 앱 데이터를 일부 지움 등). 다시 받을 수 있으면 받는다 */
    @Query("UPDATE image SET local_path = :localPath WHERE id = :id")
    suspend fun setLocalPath(
        id: String,
        localPath: String?,
    )

    @Query("UPDATE image SET deleted_at = :deletedAt WHERE id = :id")
    suspend fun markDeleted(
        id: String,
        deletedAt: Long,
    )

    @Query("DELETE FROM image WHERE id = :id")
    suspend fun deleteImage(id: String)
}
