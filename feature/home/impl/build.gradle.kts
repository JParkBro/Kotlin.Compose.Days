plugins {
    alias(libs.plugins.days.feature.impl)
}

android {
    namespace = "jpark.bro.days.feature.home.impl"
}

dependencies {
    implementation(projects.feature.home.api)
    implementation(projects.feature.item.api)
    implementation(projects.feature.couple.api)
    implementation(projects.feature.notifications.api)
}
