plugins {
    alias(libs.plugins.days.android.library)
    alias(libs.plugins.days.android.compose)
    alias(libs.plugins.days.koin)
}

android {
    namespace = "jpark.bro.days.widget"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.navigation)
    implementation(projects.core.ui)

    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
    implementation(libs.glance.preview)
    implementation(libs.glance.appwidget.preview)
    implementation(libs.androidx.work.runtime)

    testImplementation(projects.core.testing)
}
