package jpark.bro.days.core.model

/** 공개 범위 (product-spec 2-6) */
enum class Visibility {
    /** 나만 보기: 내 기기에만 저장, 서버에 올리지 않음 */
    PRIVATE,

    /** 우리 둘이: 커플 상대와 공유 */
    SHARED,
}
