import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.vanniktechPublish)
    `maven-publish`
    signing
}

group = "io.github.abdo-essam"
version = project.findProperty("VERSION_NAME")?.toString() ?: "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(21)
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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set("AELogs Network OkHttp")
        description.set("OkHttp interceptor plugin for AELogs SDK")
        url.set("https://github.com/abdo-essam/AELogs")
        inceptionYear.set("2025")

        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("abdo-essam")
                name.set("Abdo Essam")
                url.set("https://github.com/abdo-essam")
            }
        }

        scm {
            url.set("https://github.com/abdo-essam/AELogs")
            connection.set("scm:git:git://github.com/abdo-essam/AELogs.git")
            developerConnection.set("scm:git:ssh://github.com/abdo-essam/AELogs.git")
        }

        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/abdo-essam/AELogs/issues")
        }
    }
}
