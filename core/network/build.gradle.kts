plugins {
    alias(libs.plugins.days.android.library)
    alias(libs.plugins.days.jvm.ktor)
    alias(libs.plugins.days.koin)
}

android {
    namespace = "jpark.bro.days.core.network"
}
