plugins {
    alias(libs.plugins.days.android.library)
    alias(libs.plugins.days.koin)
}

android {
    namespace = "jpark.bro.days.core.sync"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.firebase)
    implementation(libs.androidx.work.runtime)
    // 앱이 화면에 떠 있을 때만 리스너를 붙인다 (ProcessLifecycleOwner)
    implementation(libs.androidx.lifecycle.process)

    // SyncEngine 테스트: Robolectric + in-memory Room
    testImplementation(libs.bundles.robolectric)
    testImplementation(libs.bundles.room)
}
