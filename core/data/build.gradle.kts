plugins {
    alias(libs.plugins.days.android.library)
    alias(libs.plugins.days.koin)
}

android {
    namespace = "jpark.bro.days.core.data"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.network)
    implementation(projects.core.firebase)
    // 구글 로그인 (Credential Manager)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    // 카카오 로그인 (Kakao SDK)
    implementation(libs.kakao.user)

    // Repository 테스트: Robolectric + in-memory Room (TESTING.md 5-6)
    testImplementation(libs.bundles.robolectric)
    testImplementation(libs.bundles.room)
}
