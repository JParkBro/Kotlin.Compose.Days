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
        // 카카오 SDK는 카카오 저장소에만 있다. com.kakao.* 는 이 저장소에서만, 이 저장소에서는 com.kakao.* 만 받는다
        exclusiveContent {
            forRepository { maven("https://devrepo.kakao.com/nexus/content/groups/public/") }
            filter { includeGroupByRegex("com\\.kakao.*") }
        }
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
include(":core:billing")
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
