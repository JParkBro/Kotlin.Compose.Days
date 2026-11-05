package jpark.bro.days.core.firebase.sync

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * 약관 동의 기록 (data-model-erd.md 4장 `users/{uid}`의 termsVersion · privacyVersion · ageOver14 · marketing · agreedAt).
 * 프로필과 같은 문서지만 온보딩에서 한 번 쓰고 로그인할 때 한 번 읽어 프로필 동기화와 나눈다.
 */
interface TermsRemoteDataSource {
    suspend fun pushAgreement(
        uid: String,
        agreement: TermsAgreementDto,
    )

    /** 동의한 서비스 이용약관 버전. 문서나 동의 기록이 없으면 null */
    suspend fun agreedTermsVersion(uid: String): String?
}

data class TermsAgreementDto(
    val termsVersion: String,
    val privacyVersion: String,
    val ageOver14: Boolean,
    val marketing: Boolean,
    val agreedAt: Long,
)

internal class FirestoreTermsDataSource(
    private val firestore: FirebaseFirestore,
) : TermsRemoteDataSource {
    override suspend fun pushAgreement(
        uid: String,
        agreement: TermsAgreementDto,
    ) {
        firestore
            .collection(USERS)
            .document(uid)
            .set(agreement.toFirestoreMap(), SetOptions.merge())
            .await()
    }

    override suspend fun agreedTermsVersion(uid: String): String? {
        val snapshot =
            firestore
                .collection(USERS)
                .document(uid)
                .get()
                .await()
        return snapshot.data?.string(TERMS_VERSION)
    }
}

/** 규칙이 `users/{uid}`의 모든 쓰기에 서버 시각을 요구한다 */
internal fun TermsAgreementDto.toFirestoreMap(): Map<String, Any> =
    mapOf(
        TERMS_VERSION to termsVersion,
        "privacyVersion" to privacyVersion,
        "ageOver14" to ageOver14,
        "marketing" to marketing,
        "agreedAt" to agreedAt,
        SERVER_UPDATED_AT to FieldValue.serverTimestamp(),
    )

private const val USERS = "users"
private const val TERMS_VERSION = "termsVersion"
