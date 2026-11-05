plugins {
    alias(libs.plugins.days.feature.impl)
}

android {
    namespace = "jpark.bro.days.feature.couple.impl"
}

dependencies {
    implementation(projects.feature.couple.api)
    implementation(projects.feature.home.api)
    // 코드 발급에 필요한 이름이 없으면 프로필 수정으로 보낸다
    implementation(projects.feature.settings.api)
}
