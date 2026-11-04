package jpark.bro.days.core.data.auth

import android.content.Context
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import jpark.bro.days.core.common.suspendRunCatching
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 사용자가 카카오 로그인 창을 닫음. 오류로 알리지 않는다.
 * (코루틴 취소와 섞이지 않도록 CancellationException을 상속하지 않는다)
 */
class KakaoSignInCancelledException : Exception("카카오 로그인을 취소했습니다.")

/**
 * 앱 시작 시 한 번 (Application.onCreate). 네이티브 앱 키는 flavor별 local.properties 값이다.
 * 키가 없으면(로컬 설정 전) 초기화하지 않고, 카카오 로그인은 실패로 돌려준다.
 */
object KakaoSetup {
    internal var isInitialized: Boolean = false
        private set

    fun initialize(
        context: Context,
        nativeAppKey: String,
    ) {
        if (nativeAppKey.isBlank()) return
        KakaoSdk.init(context, nativeAppKey)
        isInitialized = true
    }
}

/**
 * 카카오 액세스 토큰을 받는다. 카카오톡 앱 · 웹 로그인 창을 띄우므로 화면(Activity) [Context]가 필요해
 * ViewModel이 아니라 화면 Root가 부른다. 토큰은 kakaoAuth 함수로만 보내고 저장 · 로그하지 않는다.
 */
interface KakaoSignInClient {
    suspend fun requestAccessToken(activityContext: Context): Result<String>

    /** 카카오 쪽 토큰도 끊는다. 카카오로 로그인하지 않았으면 아무 일도 하지 않는다 */
    suspend fun logout()
}

internal class KakaoSdkSignIn : KakaoSignInClient {
    override suspend fun requestAccessToken(activityContext: Context): Result<String> =
        suspendRunCatching {
            check(KakaoSetup.isInitialized) { "카카오 네이티브 앱 키가 설정되지 않았습니다." }
            val client = UserApiClient.instance
            val token =
                if (client.isKakaoTalkLoginAvailable(activityContext)) {
                    suspendRunCatching {
                        client.loginWith { callback -> loginWithKakaoTalk(activityContext, callback = callback) }
                    }.getOrElse { error ->
                        if (error is KakaoSignInCancelledException) throw error
                        // 카카오톡에 연결된 계정이 없는 경우 등: 카카오 계정(웹)으로 다시 시도한다
                        client.loginWith { callback -> loginWithKakaoAccount(activityContext, callback = callback) }
                    }
                } else {
                    client.loginWith { callback -> loginWithKakaoAccount(activityContext, callback = callback) }
                }
            token.accessToken
        }

    override suspend fun logout() {
        if (!KakaoSetup.isInitialized) return
        suspendRunCatching {
            suspendCancellableCoroutine { continuation ->
                // 카카오로 로그인하지 않았으면 오류가 오지만 무시한다
                UserApiClient.instance.logout { continuation.resume(Unit) }
            }
        }
    }
}

/** 카카오 SDK 콜백을 suspend로. 사용자가 닫으면 [KakaoSignInCancelledException] */
private suspend fun UserApiClient.loginWith(
    login: UserApiClient.(callback: (OAuthToken?, Throwable?) -> Unit) -> Unit,
): OAuthToken =
    suspendCancellableCoroutine { continuation ->
        login { token, error ->
            when {
                error is ClientError && error.reason == ClientErrorCause.Cancelled -> {
                    continuation.resumeWithException(KakaoSignInCancelledException())
                }

                error != null -> {
                    continuation.resumeWithException(error)
                }

                token != null -> {
                    continuation.resume(token)
                }

                else -> {
                    continuation.resumeWithException(IllegalStateException("카카오 토큰이 없습니다."))
                }
            }
        }
    }
