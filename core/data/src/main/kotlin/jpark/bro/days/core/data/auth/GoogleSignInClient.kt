package jpark.bro.days.core.data.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import jpark.bro.days.core.common.suspendRunCatching

/**
 * 사용자가 구글 계정 선택 창을 닫음. 오류로 알리지 않는다.
 * (코루틴 취소와 섞이지 않도록 CancellationException을 상속하지 않는다)
 */
class GoogleSignInCancelledException : Exception("구글 로그인을 취소했습니다.")

/**
 * Credential Manager로 구글 ID 토큰을 받는다. 계정 선택 창을 띄우므로 화면(Activity) [Context]가 필요해
 * ViewModel이 아니라 화면 Root가 부른다.
 */
interface GoogleSignInClient {
    suspend fun requestIdToken(activityContext: Context): Result<String>

    /** 로그아웃 후 다음 로그인에서 계정을 다시 고르게 한다 */
    suspend fun clearCredentialState(activityContext: Context)
}

/** [webClientId]는 google-services가 만든 default_web_client_id */
internal class CredentialManagerGoogleSignIn(
    private val webClientId: String,
) : GoogleSignInClient {
    override suspend fun requestIdToken(activityContext: Context): Result<String> =
        suspendRunCatching {
            val request =
                GetCredentialRequest
                    .Builder()
                    .addCredentialOption(GetSignInWithGoogleOption.Builder(webClientId).build())
                    .build()
            val credential =
                try {
                    CredentialManager.create(activityContext).getCredential(activityContext, request).credential
                } catch (_: GetCredentialCancellationException) {
                    throw GoogleSignInCancelledException()
                }
            check(
                credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
            ) {
                "구글 ID 토큰이 아닙니다."
            }
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        }

    override suspend fun clearCredentialState(activityContext: Context) {
        suspendRunCatching {
            CredentialManager
                .create(
                    activityContext,
                ).clearCredentialState(ClearCredentialStateRequest())
        }
    }
}
