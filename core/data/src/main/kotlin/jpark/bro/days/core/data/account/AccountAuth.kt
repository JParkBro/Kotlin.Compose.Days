package jpark.bro.days.core.data.account

import com.google.firebase.auth.FirebaseAuthInvalidUserException
import jpark.bro.days.core.data.auth.KakaoSignInClient
import jpark.bro.days.core.firebase.auth.FirebaseAuthDataSource
import jpark.bro.days.core.firebase.functions.FirebaseFunctionsDataSource
import jpark.bro.days.core.model.Reauthentication

/** 탈퇴 전 본인 확인과 로그인 계정 삭제 (계정 삭제는 최근 로그인이 필요하다) */
internal class AccountAuth(
    private val authDataSource: FirebaseAuthDataSource,
    private val functionsDataSource: FirebaseFunctionsDataSource,
    private val kakaoSignIn: KakaoSignInClient,
) {
    /**
     * 지금 로그인한 [userId]와 같은 계정인지 확인하고 로그인 시각을 새로 한다. 다른 계정이면 [AccountMismatchException]
     * (카카오는 로그인을 바꾸기 전에 회원번호로 확인해, 다른 계정으로 바뀌어 지워지는 일이 없게 한다)
     */
    suspend fun reauthenticate(
        userId: String,
        reauthentication: Reauthentication,
    ) {
        when (reauthentication) {
            is Reauthentication.Google -> {
                try {
                    authDataSource.reauthenticateWithGoogle(reauthentication.idToken)
                } catch (e: FirebaseAuthInvalidUserException) {
                    throw AccountMismatchException(e)
                }
            }

            is Reauthentication.Kakao -> {
                val kakaoId = kakaoSignIn.currentUserId().getOrThrow()
                if ("$KAKAO_UID_PREFIX$kakaoId" != userId) throw AccountMismatchException()
                val user =
                    authDataSource.signInWithCustomToken(
                        functionsDataSource.kakaoCustomToken(reauthentication.accessToken),
                    )
                check(user.uid == userId) { "다시 로그인한 계정이 다릅니다." }
            }
        }
    }

    suspend fun deleteAccount() = authDataSource.deleteCurrentUser()

    suspend fun unlinkKakao() = kakaoSignIn.unlink()

    private companion object {
        const val KAKAO_UID_PREFIX = "kakao:"
    }
}

/** 본인 확인에서 다른 계정으로 로그인함 */
class AccountMismatchException(
    cause: Throwable? = null,
) : Exception("지금 로그인한 계정과 다른 계정입니다.", cause)
