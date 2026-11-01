plugins {
    alias(libs.plugins.days.android.application)
    alias(libs.plugins.days.android.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)

    // oss-licenses
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    namespace = "jpark.bro.days"

    defaultConfig {
        buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    // Datastore
    implementation(libs.androidx.datastore.preferences)

    // datetime
    implementation(libs.kotlinx.datetime)

    // oss-licenses
    implementation(libs.androidx.appcompat)
    implementation(libs.play.services.oss.licenses)
}
