package jpark.bro.days.feature.settings.impl.account

import jpark.bro.days.core.model.AuthUser

internal data class AccountState(
    val user: AuthUser? = null,
    /** 로그아웃 전 "기기 데이터가 모두 지워져요" 확인 창 */
    val isLogoutConfirmVisible: Boolean = false,
    val isSigningOut: Boolean = false,
)
