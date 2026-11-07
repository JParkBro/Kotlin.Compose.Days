package jpark.bro.days.core.data.repository

import jpark.bro.days.core.model.ActivityContent
import jpark.bro.days.core.model.ActivityLog
import kotlinx.coroutines.flow.Flow

/**
 * 알림 목록 (상대 활동 · 보낸 알림). 로그인한 계정의 기록만 다루고 서버로 보내지 않는다.
 * 보관 기간([ActivityLog.RETENTION_DAYS]일)이 지난 기록은 새로 남길 때 지운다.
 */
interface ActivityRepository {
    /** 최신순, 보관 기간 안의 기록. 로그아웃이면 비어 있다 */
    fun observeActivities(): Flow<List<ActivityLog>>

    /** 기록을 남긴다. 상대 활동이면 [ActivityNotifier]로 시스템 알림도 띄운다. 로그아웃이면 실패 */
    suspend fun record(contents: List<ActivityContent>): Result<Unit>

    suspend fun markRead(id: String): Result<Unit>

    suspend fun markAllRead(): Result<Unit>
}

/**
 * 상대 활동 시스템 알림. core:sync가 core:notification을 참조할 수 없어 이 인터페이스로 쓰고,
 * 구현은 core:notification에 있다 (알림 설정 · 앱이 화면에 떠 있는지 확인).
 */
interface ActivityNotifier {
    suspend fun notifyPartnerActivities(activities: List<ActivityContent>)
}
