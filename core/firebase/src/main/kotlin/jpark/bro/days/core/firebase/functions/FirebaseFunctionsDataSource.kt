package jpark.bro.days.core.firebase.functions

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

/**
 * Cloud Functions callable 감싸기 (firebase/functions). App Check 토큰은 SDK가 요청마다 붙인다.
 * 호출할 때마다 함수 사용량이 늘어나므로 사용자 행동(로그인 등) 한 번에 한 번만 부른다.
 */
class FirebaseFunctionsDataSource(
    private val functions: FirebaseFunctions,
) {
    /** 카카오 액세스 토큰 → Firebase 커스텀 토큰 (uid `kakao:{회원번호}`, infra-and-billing-protection.md 2-1) */
    suspend fun kakaoCustomToken(accessToken: String): String {
        val result =
            functions
                .getHttpsCallable(KAKAO_AUTH)
                .call(mapOf("accessToken" to accessToken))
                .await()
        val data = result.data as? Map<*, *>
        return checkNotNull(data?.get("customToken") as? String) { "커스텀 토큰이 없습니다." }
    }
}

private const val KAKAO_AUTH = "kakaoAuth"
