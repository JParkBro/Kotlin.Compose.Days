plugins {
    alias(libs.plugins.days.android.library)
    alias(libs.plugins.days.koin)
}

android {
    namespace = "jpark.bro.days.core.notification"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    // 오늘 보낼 알림 · 자동 기념일 계산
    implementation(projects.core.domain)
    implementation(projects.core.data)
    // 알림을 누르면 MainActivity가 항목을 연다 (AppIntents)
    implementation(projects.core.navigation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.work.runtime)
    // 앱이 화면에 떠 있으면 상대 활동 시스템 알림을 띄우지 않는다
    implementation(libs.androidx.lifecycle.process)
}
