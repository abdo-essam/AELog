buildscript {
    dependencies {
        classpath("org.ow2.asm:asm:9.9.1")
        classpath("org.ow2.asm:asm-tree:9.9.1")
        classpath("org.ow2.asm:asm-analysis:9.9.1")
        classpath("org.ow2.asm:asm-commons:9.9.1")
        classpath("org.ow2.asm:asm-util:9.9.1")
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.binaryCompatibilityValidator)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.vanniktechPublish) apply false
    alias(libs.plugins.kotlinxBenchmark) apply false
    alias(libs.plugins.kotlinAllopen) apply false
}

apiValidation {
    ignoredProjects.addAll(listOf("sample", "composeApp", "benchmarks"))
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint("1.5.0")
            .setEditorConfigPath("$rootDir/.editorconfig")
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint("1.5.0")
    }
}
