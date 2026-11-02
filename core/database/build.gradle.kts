plugins {
    alias(libs.plugins.days.android.library)
    alias(libs.plugins.days.room)
    alias(libs.plugins.days.koin)
}

android {
    namespace = "jpark.bro.days.core.database"
}

dependencies {
    implementation(projects.core.model)

    // DAO 테스트: Robolectric + in-memory Room (TESTING.md 2-1)
    testImplementation(libs.bundles.robolectric)
}
