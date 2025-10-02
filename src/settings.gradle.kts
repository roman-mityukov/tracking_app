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
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "GeoApp"
include(":app")
include(":core:log")
include(":core:database")
include(":core:datastore")
include(":core:datastore-proto")
include(":core:data")
include(":core:sharing")
include(":core:common")
include(":core:ui")
include(":core:test")
include(":core:model")
include(":feature:onboarding")
include(":feature:about")
include(":feature:map")
include(":feature:profile")
include(":feature:settings")
include(":feature:splash")
include(":feature:track-capture")
include(":feature:track-list")
include(":feature:track-details")
include(":core:yandexmap")
include(":core:designsystem")
include(":core:testing")
include(":core:sync")
include(":core:network")
