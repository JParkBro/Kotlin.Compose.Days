plugins {
    alias(libs.plugins.days.android.library)
    alias(libs.plugins.days.koin)
}

android {
    namespace = "jpark.bro.days.core.datastore"
}

dependencies {
    api(projects.core.model)
    implementation(libs.androidx.datastore.preferences)
}
