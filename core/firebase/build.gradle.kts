plugins {
    alias(libs.plugins.days.android.library)
    alias(libs.plugins.days.koin)
}

android {
    namespace = "jpark.bro.days.core.firebase"
}

dependencies {
    implementation(projects.core.model)

    api(platform(libs.firebase.bom))
    api(libs.firebase.auth)
    api(libs.firebase.firestore)
    api(libs.kotlinx.coroutines.play.services)
    implementation(libs.firebase.appcheck.playintegrity)
    // dev 빌드만 App Check 디버그 제공자 (src/dev)
    devImplementation(libs.firebase.appcheck.debug)
}
