package jpark.bro.days.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 커플 (data-model-erd.md 2 · 3장). Firestore `couples/{id}`의 사본.
 * Firestore의 `members` 배열(2명)은 순서대로 두 컬럼에 둔다: [member1UserId] 코드 발급자, [member2UserId] 코드 입력자.
 * 한 기기에 여러 계정이 로그인할 수 있어 계정별로 "내 커플"을 멤버 컬럼으로 찾는다.
 */
@Entity(
    tableName = "couple",
    indices = [Index("member1_user_id"), Index("member2_user_id")],
)
data class CoupleEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "member1_user_id") val member1UserId: String,
    @ColumnInfo(name = "member2_user_id") val member2UserId: String,
    /** yyyy-MM-dd 사귄 날 */
    @ColumnInfo(name = "start_date") val startDate: String,
    @ColumnInfo(name = "count_first_day_as_one") val countFirstDayAsOne: Boolean,
    /** ACTIVE | DISCONNECTED */
    val status: String,
    @ColumnInfo(name = "pair_code") val pairCode: String,
    @ColumnInfo(name = "disconnected_at") val disconnectedAt: Long?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "server_updated_at") val serverUpdatedAt: Long?,
    /** Room 전용. [SyncState.name] */
    @ColumnInfo(name = "sync_state") val syncState: String = SyncState.SYNCED.name,
)
