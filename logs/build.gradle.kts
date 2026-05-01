import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)

    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktechPublish)
    `maven-publish`
    signing
}

group = "io.github.abdo-essam"
version = project.findProperty("VERSION_NAME")?.toString() ?: "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(21)
    explicitApiWarning()

    androidLibrary {
        namespace = "com.ae.logs"
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
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "AELogs"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Re-export all modules — consumers only need this one dependency

            implementation(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.atomicfu)
            implementation(libs.components.resources)

            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.material.icons.extended)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}

dokka {
    moduleName.set("AELogs")
    dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory.set(projectDir.resolve("src"))
            remoteUrl("https://github.com/abdo-essam/AELogs/tree/main/logs/src")
            remoteLineSuffix.set("#L")
        }
        perPackageOption {
            matchingRegex.set(".*\\.internal.*")
            suppress.set(true)
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set("AELogs")
        description.set(
            "Extensible on-device dev tools SDK for Kotlin Multiplatform — " +
                "inspect logs, network, and more with a beautiful Compose UI.",
        )
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

tasks.withType<JavaCompile>().configureEach {
    classpath = files()
}
