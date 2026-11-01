plugins {
    alias(libs.plugins.days.android.library)
    alias(libs.plugins.days.android.compose)
}

android {
    namespace = "jpark.bro.days.core.designsystem"
}

dependencies {
    // ThemeTokensSyncTest: theme-tokens.json 파싱
    testImplementation(libs.kotlinx.serialization.json)
}
