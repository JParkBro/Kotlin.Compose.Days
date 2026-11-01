plugins {
    alias(libs.plugins.days.android.library)
}

android {
    namespace = "jpark.bro.days.core.testing"
}

dependencies {
    api(projects.core.data)
    implementation(libs.junit)
    implementation(libs.kotlinx.coroutines.test)
}
