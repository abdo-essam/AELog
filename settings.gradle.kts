pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("androidx")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("androidx")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "AELog"

include(":core")
include(":plugins:logs")
include(":plugins:network")
include(":plugins:network:interceptors:ktor")
include(":plugins:network:interceptors:okhttp")
include(":plugins:analytics")
include(":sample:composeApp")

// Build cache
buildCache {
    local {
        isEnabled = true
        directory = File(rootDir, ".gradle/build-cache")
    }
}

// Enable type-safe project accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
