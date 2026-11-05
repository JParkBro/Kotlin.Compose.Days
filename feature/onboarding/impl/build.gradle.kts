plugins {
    alias(libs.plugins.days.feature.impl)
}

android {
    namespace = "jpark.bro.days.feature.onboarding.impl"
}

dependencies {
    implementation(projects.feature.onboarding.api)
    implementation(projects.feature.settings.api)
}
