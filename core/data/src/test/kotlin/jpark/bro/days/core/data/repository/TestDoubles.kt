package jpark.bro.days.core.data.repository

import jpark.bro.days.core.data.Seoul
import jpark.bro.days.core.data.session.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/** 테스트 중 시각을 옮길 수 있는 시계 */
internal class MutableClock(
    var now: Instant,
) : Clock() {
    override fun getZone(): ZoneId = Seoul

    override fun withZone(zone: ZoneId?): Clock = this

    override fun instant(): Instant = now
}

/** 로그인 계정을 바꿀 수 있는 세션 (null = 로그아웃) */
internal class SwitchableUserSession(
    initial: String?,
) : UserSession {
    val userId = MutableStateFlow(initial)

    override fun observeUserId(): Flow<String?> = userId

    override fun currentUserIdOrNull(): String? = userId.value
}
