import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

group = "io.github.abdo-essam"
version = project.findProperty("VERSION_NAME")?.toString() ?: "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(23)
    explicitApiWarning()

    // OkHttp is JVM-only — no iOS targets
    androidLibrary {
        namespace = "com.ae.logs.network.okhttp"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_23)
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Brings in logs-network (and transitively logs core)
            api(projects.logsNetwork)
            // OkHttp is a required dep here — consumers only pull this in if they use OkHttp
            implementation(libs.okhttp)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
