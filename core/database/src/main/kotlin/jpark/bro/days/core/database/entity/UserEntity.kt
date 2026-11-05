package jpark.bro.days.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 프로필 (data-model-erd.md 2 · 3장 USER). 내 프로필 + 연결된 상대 프로필 캐시.
 * 내 프로필은 저장하면 PENDING → Push가 `users/{uid}`에 올리고, 상대 프로필은 받기만 한다.
 */
@Entity(tableName = "user")
data class UserEntity(
    /** Firebase uid */
    @PrimaryKey val id: String,
    val nickname: String,
    /** yyyy-MM-dd. 없으면 null */
    @ColumnInfo(name = "birth_date") val birthDate: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "server_updated_at") val serverUpdatedAt: Long?,
    /** Room 전용. [SyncState.name] */
    @ColumnInfo(name = "sync_state") val syncState: String = SyncState.PENDING.name,
    /** [ImageEntity.id]. 사진이 없으면 null */
    @ColumnInfo(name = "profile_image_id") val profileImageId: String? = null,
)
