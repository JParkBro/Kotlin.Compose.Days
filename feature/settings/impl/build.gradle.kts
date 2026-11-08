plugins {
    alias(libs.plugins.days.feature.impl)
}

android {
    namespace = "jpark.bro.days.feature.settings.impl"
}

dependencies {
    implementation(projects.feature.settings.api)
    implementation(projects.feature.couple.api)
    // 사진 한도를 다 썼을 때 보상형 광고 (사진 +1장)
    implementation(projects.core.ads)
    // 개발자 응원하기 (Play Billing 후원)
    implementation(projects.core.billing)
    // 오픈소스 라이선스 목록 화면 (목록은 app의 oss-licenses 플러그인이 빌드 때 만든다)
    implementation(libs.play.services.oss.licenses)
}
