import com.vanniktech.maven.publish.SonatypeHost

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
    explicitApi()

    // OkHttp is Android-only — no JVM desktop or iOS targets
    androidLibrary {
        namespace = "com.ae.log.network.okhttp"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
    }

    sourceSets {
        androidMain.dependencies {
            // Brings in network plugin (and transitively core)
            api(projects.plugins.network)
            // OkHttp — consumers only pull this in if they use the OkHttp interceptor
            api(libs.okhttp)
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
        name.set("AELog Network OkHttp")
        description.set("OkHttp interceptor plugin for AELog SDK")
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
