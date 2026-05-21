buildscript {
    dependencies {
        classpath("org.ow2.asm:asm:9.10")
        classpath("org.ow2.asm:asm-tree:9.10")
        classpath("org.ow2.asm:asm-analysis:9.10")
        classpath("org.ow2.asm:asm-commons:9.10")
        classpath("org.ow2.asm:asm-util:9.10")
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
    lineEndings = com.diffplug.spotless.LineEnding.UNIX
    kotlin {
        target(
            "core/src/**/*.kt",
            "plugins/**/src/**/*.kt",
            "sample/composeApp/src/**/*.kt",
            "benchmarks/src/**/*.kt",
        )
        targetExclude("**/build/**", "**/.gradle/**")
        ktlint("1.5.0")
            .setEditorConfigPath("$rootDir/.editorconfig")
    }
    kotlinGradle {
        target(
            "*.gradle.kts",
            "core/*.gradle.kts",
            "plugins/*/*.gradle.kts",
            "plugins/*/*/*.gradle.kts",
            "sample/composeApp/*.gradle.kts",
            "benchmarks/*.gradle.kts",
        )
        targetExclude("**/build/**", "**/.gradle/**")
        ktlint("1.5.0")
    }
}

tasks.clean {
    delete("sample/iosApp/build")
}
