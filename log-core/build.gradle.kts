import com.vanniktech.maven.publish.SonatypeHost

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
    explicitApi()
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.time.ExperimentalTime",
            "-opt-in=kotlin.uuid.ExperimentalUuidApi",
        )
    }

    androidLibrary {
        namespace = "com.ae.log"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    )

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.atomicfu)
            implementation(libs.components.resources)
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.material.icons.extended)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}

dokka {
    moduleName.set("AELog")
    dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory.set(projectDir.resolve("src"))
            remoteUrl("https://github.com/abdo-essam/AELog/tree/main/log-core/src")
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
        name.set("AELog")
        description.set(
            "Extensible on-device dev tools SDK for Kotlin Multiplatform — " +
                "inspect logs, network, and more with a beautiful Compose UI.",
        )
        url.set("https://github.com/abdo-essam/AELog")
        inceptionYear.set("2026")

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
            url.set("https://github.com/abdo-essam/AELog")
            connection.set("scm:git:git://github.com/abdo-essam/AELog.git")
            developerConnection.set("scm:git:ssh://github.com/abdo-essam/AELog.git")
        }

        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/abdo-essam/AELog/issues")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    classpath = files()
}
