plugins {
    alias(libs.plugins.days.feature.impl)
}

android {
    namespace = "jpark.bro.days.feature.item.impl"
}

dependencies {
    implementation(projects.feature.item.api)
    // 사진 한도를 다 썼을 때 보상형 광고 (사진 +1장)
    implementation(projects.core.ads)
}
