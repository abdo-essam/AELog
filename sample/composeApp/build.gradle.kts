plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
    androidTarget {
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core")) // infrastructure + ui shell
            implementation(project(":plugins:logs")) // LogPlugin
            implementation(project(":plugins:network")) // NetworkPlugin
            implementation(project(":plugins:network:interceptors:ktor")) // Ktor auto-interceptor
            implementation(project(":plugins:analytics")) // AnalyticsPlugin
            implementation(libs.ktor.client.core) // HttpClient DSL in commonMain
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.material.icons.extended)
            implementation(libs.components.resources)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp) // OkHttp engine for Android
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.ae.log.sample"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        applicationId = "com.ae.log.sample"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        buildConfig = true // Required in AGP 8+ — disabled by default
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
