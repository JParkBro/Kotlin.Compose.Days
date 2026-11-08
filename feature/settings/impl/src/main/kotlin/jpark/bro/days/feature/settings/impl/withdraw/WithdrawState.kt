package jpark.bro.days.feature.settings.impl.withdraw

import jpark.bro.days.core.model.AuthProvider

internal data class WithdrawState(
    /** 본인 확인에 쓸 로그인 수단. 로그인 정보를 읽기 전이면 null */
    val provider: AuthProvider? = null,
    /** 커플이 연결되어 있으면 연결 해제 · 사본 안내를 보여준다 */
    val partnerName: String? = null,
    val isCoupled: Boolean = false,
    val isConfirmed: Boolean = false,
    /** 다시 로그인 중이거나 탈퇴 처리 중 */
    val isWorking: Boolean = false,
) {
    val canWithdraw: Boolean get() = provider != null && isConfirmed && !isWorking
}
