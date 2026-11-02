enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Days"
include(":app")

include(":core:model")
include(":core:common")
include(":core:domain")
include(":core:navigation")
include(":core:designsystem")
include(":core:ui")
include(":core:database")
include(":core:datastore")
include(":core:network")
include(":core:firebase")
include(":core:data")
include(":core:sync")
include(":core:notification")
include(":core:ads")
include(":core:testing")
include(":widget")
include(":feature:onboarding:api")
include(":feature:onboarding:impl")
include(":feature:couple:api")
include(":feature:couple:impl")
include(":feature:home:api")
include(":feature:home:impl")
include(":feature:calendar:api")
include(":feature:calendar:impl")
include(":feature:item:api")
include(":feature:item:impl")
include(":feature:notifications:api")
include(":feature:notifications:impl")
include(":feature:settings:api")
include(":feature:settings:impl")
