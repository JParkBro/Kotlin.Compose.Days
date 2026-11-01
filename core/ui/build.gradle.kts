plugins {
    alias(libs.plugins.days.android.library)
    alias(libs.plugins.days.android.compose)
}

android {
    namespace = "jpark.bro.days.core.ui"
}

dependencies {
    api(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(projects.core.designsystem)
}
