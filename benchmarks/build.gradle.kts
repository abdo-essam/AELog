plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxBenchmark)
    alias(libs.plugins.kotlinAllopen)
}

// allopen is required by JMH — benchmark classes must be non-final
allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

kotlin {
    jvmToolchain(21)

    // JVM-only: JMH does not support KMP targets
    jvm {
        mainRun {
            mainClass = "kotlinx.benchmark.generated.MainKt"
        }
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(project(":plugins:logs"))
                implementation(project(":plugins:network"))
                implementation(libs.kotlinx.coroutines.core)
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:${libs.versions.kotlinx.benchmark.get()}")
            }
        }
    }
}

benchmark {
    targets {
        register("jvm")
    }

    configurations {
        named("main") {
            warmups = 5           // JVM warm-up iterations (JIT stabilisation)
            iterations = 10       // Measurement iterations
            iterationTime = 1     // Seconds per iteration
            iterationTimeUnit = "s"
            // Run specific benchmarks with: ./gradlew :benchmarks:jvmBenchmark --args ".*RingBuffer.*"
        }

        // Lightweight "smoke" config — fast feedback during development
        register("smoke") {
            warmups = 1
            iterations = 3
            iterationTime = 500
            iterationTimeUnit = "ms"
        }
    }
}
