plugins {
    alias(libs.plugins.days.feature.impl)
}

android {
    namespace = "jpark.bro.days.feature.notifications.impl"
}

dependencies {
    implementation(projects.feature.notifications.api)
    implementation(projects.feature.item.api)
}
