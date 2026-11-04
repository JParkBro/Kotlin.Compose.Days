plugins {
    alias(libs.plugins.days.android.library)
}

android {
    namespace = "jpark.bro.days.core.testing"
}

dependencies {
    api(projects.core.data)
    // 원격 데이터 소스 Fake · 테스트용 DataStore
    api(projects.core.firebase)
    api(projects.core.datastore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.junit)
    implementation(libs.kotlinx.coroutines.test)
}
