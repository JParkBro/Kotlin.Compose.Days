plugins {
    alias(libs.plugins.days.android.library)
    alias(libs.plugins.days.android.compose)
}

android {
    namespace = "jpark.bro.days.core.navigation"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
}
