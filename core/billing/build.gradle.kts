plugins {
    alias(libs.plugins.days.android.library)
    alias(libs.plugins.days.android.compose)
    alias(libs.plugins.days.koin)
}

android {
    namespace = "jpark.bro.days.core.billing"
}

dependencies {
    // 개발자 응원하기: 혜택 없는 소모형 인앱 상품 (Play Billing, 서버 검증 없음)
    implementation(libs.billing.ktx)
    implementation(projects.core.common)
    implementation(libs.koin.compose)
    implementation(libs.androidx.activity.compose)
}
