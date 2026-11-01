plugins {
    alias(libs.plugins.days.feature.impl)
}

android {
    namespace = "jpark.bro.days.feature.calendar.impl"
}

dependencies {
    implementation(projects.feature.calendar.api)
    implementation(projects.feature.item.api)
}
