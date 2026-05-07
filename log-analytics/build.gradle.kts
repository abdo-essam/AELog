import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktechPublish)
    `maven-publish`
    signing
}

group = "io.github.abdo-essam"
version = project.findProperty("VERSION_NAME")?.toString() ?: "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(21)
    explicitApi()

    androidLibrary {
        namespace = "com.ae.log.analytics"
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
            api(projects.logCore)
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.material.icons.extended)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set("AELog Analytics")
        description.set("Analytics tracking plugin for AELog SDK")
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
