package jpark.bro.days.core.data.auth

import android.content.Context
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 로그아웃 · 탈퇴 뒤 구글 · 카카오 쪽 정리. 다음 로그인에서 계정을 다시 고르게 하는 것뿐이라
 * 각각 [TIMEOUT_MILLIS] 안에 끝나지 않으면 기다리지 않는다 (SDK 콜백이 오지 않아 화면이 멈추지 않게)
 */
internal class ProviderSessions(
    private val context: Context,
    private val googleSignIn: GoogleSignInClient,
    private val kakaoSignIn: KakaoSignInClient,
) {
    /** [unlinkKakao]면 카카오 계정과 앱의 연결도 끊는다 (탈퇴: 다음 로그인 때 다시 동의) */
    suspend fun clear(unlinkKakao: Boolean) {
        withTimeoutOrNull(TIMEOUT_MILLIS) { googleSignIn.clearCredentialState(context) }
        withTimeoutOrNull(TIMEOUT_MILLIS) { if (unlinkKakao) kakaoSignIn.unlink() else kakaoSignIn.logout() }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 3_000L
    }
}
