plugins {
    alias(libs.plugins.days.feature.api)
}

android {
    namespace = "jpark.bro.days.feature.item.api"
}

dependencies {
    api(projects.core.model)
}
