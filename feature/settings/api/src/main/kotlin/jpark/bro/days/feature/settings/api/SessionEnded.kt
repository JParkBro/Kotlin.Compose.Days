package jpark.bro.days.feature.settings.api

/** 로그아웃 · 회원 탈퇴로 세션이 끝남. app이 온보딩으로 보낸다 (module-map 4-3). */
data object SessionEnded

/** 구현은 app에 둔다. */
fun interface SessionEndedHandler {
    fun handle(result: SessionEnded)
}
