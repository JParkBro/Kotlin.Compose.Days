package jpark.bro.days.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 사진 (data-model-erd.md 2 · 3장 IMAGE). 배경 사진 · 프로필 사진이 [id]로 가리킨다.
 * 서버 경로는 `couples/{remoteCoupleId}/images/{id}.webp`로 정해져 있어 저장하지 않는다.
 * 나만 보기 항목 · 솔로 프로필 사진은 서버에 올리지 않으므로 [remoteCoupleId]가 null로 남는다.
 */
@Entity(tableName = "image")
data class ImageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "owner_user_id") val ownerUserId: String,
    /** 서버 파일이 있는 커플. 올리기 전이거나 올리지 않는 사진이면 null */
    @ColumnInfo(name = "remote_couple_id") val remoteCoupleId: String?,
    /** filesDir 기준 상대 경로 (images/{id}.webp). 상대 사진을 아직 받지 않았으면 null */
    @ColumnInfo(name = "local_path") val localPath: String?,
    /** [ImageUploadState.name] */
    @ColumnInfo(name = "upload_state") val uploadState: String = ImageUploadState.NONE.name,
    val width: Int?,
    val height: Int?,
    val bytes: Long?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    /** 더 쓰지 않는 사진. 내가 올린 서버 파일은 동기화가 지운 뒤 행도 지운다 */
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)

enum class ImageUploadState {
    /** 보통. 공유될 때 동기화가 올린다 */
    NONE,

    /** 커플 업로드 한도를 다 써서 올리지 못함. 이 기기에서만 보인다 */
    REJECTED,
}
