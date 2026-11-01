plugins {
    alias(libs.plugins.days.feature.impl)
}

android {
    namespace = "jpark.bro.days.feature.couple.impl"
}

dependencies {
    implementation(projects.feature.couple.api)
    implementation(projects.feature.home.api)
}
