package jpark.bro.days.feature.settings.impl.settings

/** 설정 탭에서 들어가는 하위 화면 (product-spec 6장) */
internal enum class SettingsDestination {
    PROFILE,

    /** 커플 연결됨 → 커플 관리 */
    COUPLE,

    /** 솔로 → 커플 코드로 연결하기 */
    PAIRING,
    NOTIFICATION,
    WIDGET,
    POLICY,
    ACCOUNT,
}

/** 지원 메뉴 */
internal enum class SupportItem {
    NOTICE,
    INQUIRY,
    RATE_APP,
}
