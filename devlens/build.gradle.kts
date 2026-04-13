import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Base64

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    `maven-publish`
    signing
}

group = "io.github.abdo-essam"
version = project.findProperty("VERSION_NAME")?.toString() ?: "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(17)
    explicitApiWarning()

    androidLibrary {
        namespace = "com.ae.devlens"
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
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "AEDevLens"
            isStatic = true
        }
    }

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.compose.runtime:runtime:1.7.3")
            implementation("org.jetbrains.compose.foundation:foundation:1.7.3")
            implementation("org.jetbrains.compose.material3:material3:1.7.3")
            implementation("org.jetbrains.compose.ui:ui:1.7.3")
            implementation("org.jetbrains.compose.components:components-resources:1.7.3")
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")

            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }

        androidMain.dependencies {}

        iosMain.dependencies {}
    }
}

dokka {
    moduleName.set("AEDevLens")
    dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory.set(projectDir.resolve("src"))
            remoteUrl("https://github.com/abdo-essam/AEDevLens/tree/main/devlens/src")
            remoteLineSuffix.set("#L")
        }
        perPackageOption {
            matchingRegex.set(".*\\.internal.*")
            suppress.set(true)
        }
    }
}

kover {
    reports {
        filters {
            excludes {
                classes("*.ui.*", "*.theme.*")
            }
        }
        // TODO: Raise coverage threshold as tests are added (target: 70%)
        verify {
            rule {
                minBound(0)
            }
        }
    }
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaGeneratePublicationHtml"))
}

publishing {
    publications.withType<MavenPublication> {
        artifact(javadocJar)

        pom {
            name.set("AEDevLens")
            description.set(
                "Extensible on-device dev tools SDK for Kotlin Multiplatform — " +
                    "inspect logs, network, and more with a beautiful Compose UI.",
            )
            url.set("https://github.com/abdo-essam/AEDevLens")
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
                url.set("https://github.com/abdo-essam/AEDevLens")
                connection.set("scm:git:git://github.com/abdo-essam/AEDevLens.git")
                developerConnection.set("scm:git:ssh://github.com/abdo-essam/AEDevLens.git")
            }

            issueManagement {
                system.set("GitHub Issues")
                url.set("https://github.com/abdo-essam/AEDevLens/issues")
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

            credentials {
                username = System.getenv("MAVEN_CENTRAL_USERNAME") ?: ""
                password = System.getenv("MAVEN_CENTRAL_PASSWORD") ?: ""
            }
        }
    }
}

signing {
    val signingKeyId = System.getenv("SIGNING_KEY_ID")
    val rawKey = System.getenv("SIGNING_KEY")
    val signingPassword = System.getenv("SIGNING_PASSWORD")

    if (!rawKey.isNullOrBlank()) {
        val signingKey =
            try {
                String(Base64.getDecoder().decode(rawKey))
            } catch (e: IllegalArgumentException) {
                rawKey
            }
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        sign(publishing.publications)
    }
}

tasks.withType<Sign>().configureEach {
    onlyIf { !version.toString().endsWith("SNAPSHOT") }
}

tasks.withType<JavaCompile>().configureEach {
    classpath = files()
}
