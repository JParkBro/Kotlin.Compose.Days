plugins {
    alias(libs.plugins.days.android.library)
    alias(libs.plugins.days.room)
    alias(libs.plugins.days.koin)
}

android {
    namespace = "jpark.bro.days.core.database"
    testOptions.unitTests {
        isIncludeAndroidResources = true
        // Robolectric이 JDK 17+에서 FileDescriptor · SharedSecrets 내부에 접근하도록 허용
        all {
            it.jvmArgs(
                "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
                "--add-opens=java.base/java.io=ALL-UNNAMED",
            )
        }
    }
}

dependencies {
    implementation(projects.core.model)

    // DAO 테스트: Robolectric + in-memory Room (TESTING.md 2-1)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
}
