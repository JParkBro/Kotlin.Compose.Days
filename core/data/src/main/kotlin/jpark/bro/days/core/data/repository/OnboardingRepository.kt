package jpark.bro.days.core.data.repository

/**
 * 온보딩 진행 (product-spec 3-1 · 5장): 소개 → 로그인 → 약관 동의 → 시작 방식.
 * 소개는 기기마다 한 번, 약관 동의는 계정마다 한 번 (이미 동의한 계정은 다시 로그인해도 묻지 않는다).
 */
interface OnboardingRepository {
    suspend fun isIntroSeen(): Boolean

    suspend fun markIntroSeen()

    /** 로그인했고 이 기기에서 약관 동의까지 마쳤으면 true. 앱을 켤 때 첫 화면을 정한다 */
    suspend fun isOnboarded(): Boolean

    /** 로그인 직후: 기기 기록이 없으면 서버 동의 기록을 한 번 읽는다 (다시 설치 · 다른 기기). 읽지 못하면 실패 */
    suspend fun checkTermsAgreed(): Result<Boolean>

    /** 필수 항목(만 14세 · 이용약관 · 개인정보)은 화면에서 모두 체크해야 부른다. [marketing]만 선택 */
    suspend fun agreeTerms(marketing: Boolean): Result<Unit>
}
