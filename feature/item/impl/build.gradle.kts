plugins {
    alias(libs.plugins.days.feature.impl)
}

android {
    namespace = "jpark.bro.days.feature.item.impl"
}

dependencies {
    implementation(projects.feature.item.api)
}
