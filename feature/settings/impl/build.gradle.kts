plugins {
    alias(libs.plugins.days.feature.impl)
}

android {
    namespace = "jpark.bro.days.feature.settings.impl"
}

dependencies {
    implementation(projects.feature.settings.api)
    implementation(projects.feature.couple.api)
}
