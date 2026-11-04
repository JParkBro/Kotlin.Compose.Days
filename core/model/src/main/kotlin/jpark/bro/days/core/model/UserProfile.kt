package jpark.bro.days.core.model

import java.time.LocalDate

/** 프로필 (product-spec 6장: 이름 · 생일, 사진은 이미지 저장소 단계에서). 상대 프로필은 커플이 연결됐을 때만 */
data class UserProfile(
    val userId: String,
    val nickname: String,
    /** 비워 두면 생일 자동 기념일이 생기지 않는다 */
    val birthDate: LocalDate?,
) {
    companion object {
        /** 문서에 정해진 값이 없어 정한 값 (2026-11) */
        const val NICKNAME_MAX_LENGTH = 10
    }
}
