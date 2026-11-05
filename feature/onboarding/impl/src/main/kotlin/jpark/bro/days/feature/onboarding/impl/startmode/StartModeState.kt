package jpark.bro.days.feature.onboarding.impl.startmode

internal enum class StartMode {
    SOLO,
    COUPLE,
}

/** 시안대로 커플 연결을 추천으로 미리 고른다. 혼자 시작해도 나중에 설정에서 연결할 수 있다 */
internal data class StartModeState(
    val selected: StartMode = StartMode.COUPLE,
)
