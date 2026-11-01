plugins {
    alias(libs.plugins.days.android.library)
}

android {
    namespace = "jpark.bro.days.core.testing"
}

dependencies {
    implementation(libs.junit)
    implementation(libs.kotlinx.coroutines.test)
}
