import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "jpark.bro.days.buildlogic"

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.days.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.days.android.library.get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = libs.plugins.days.android.compose.get().pluginId
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("jvmLibrary") {
            id = libs.plugins.days.jvm.library.get().pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("jvmKtor") {
            id = libs.plugins.days.jvm.ktor.get().pluginId
            implementationClass = "JvmKtorConventionPlugin"
        }
        register("koin") {
            id = libs.plugins.days.koin.get().pluginId
            implementationClass = "KoinConventionPlugin"
        }
        register("room") {
            id = libs.plugins.days.room.get().pluginId
            implementationClass = "RoomConventionPlugin"
        }
        register("featureApi") {
            id = libs.plugins.days.feature.api.get().pluginId
            implementationClass = "FeatureApiConventionPlugin"
        }
        register("featureImpl") {
            id = libs.plugins.days.feature.impl.get().pluginId
            implementationClass = "FeatureImplConventionPlugin"
        }
    }
}
