plugins {
    alias(libs.plugins.days.android.application)
    alias(libs.plugins.days.android.compose)
    alias(libs.plugins.days.koin)
}

android {
    namespace = "jpark.bro.days"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.navigation3)
    implementation(libs.bundles.koin.compose)

    // core
    implementation(projects.core.navigation)
    implementation(projects.core.designsystem)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.network)
    implementation(projects.core.firebase)
    implementation(projects.core.data)
    implementation(projects.core.sync)
    implementation(projects.core.notification)
    implementation(projects.core.ads)

    // feature
    implementation(projects.feature.onboarding.api)
    implementation(projects.feature.onboarding.impl)
    implementation(projects.feature.couple.api)
    implementation(projects.feature.couple.impl)
    implementation(projects.feature.home.api)
    implementation(projects.feature.home.impl)
    implementation(projects.feature.calendar.api)
    implementation(projects.feature.calendar.impl)
    implementation(projects.feature.item.api)
    implementation(projects.feature.item.impl)
    implementation(projects.feature.notifications.api)
    implementation(projects.feature.notifications.impl)
    implementation(projects.feature.settings.api)
    implementation(projects.feature.settings.impl)
}
