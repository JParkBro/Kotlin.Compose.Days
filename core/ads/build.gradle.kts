plugins {
    alias(libs.plugins.days.android.library)
    alias(libs.plugins.days.android.compose)
    alias(libs.plugins.days.koin)
}

android {
    namespace = "jpark.bro.days.core.ads"
}

dependencies {
    // AdMob 보상형 광고 · 맞춤 광고 동의(UMP)
    implementation(libs.play.services.ads)
    implementation(libs.ump)
    implementation(libs.koin.compose)
    implementation(libs.androidx.activity.compose)
}
