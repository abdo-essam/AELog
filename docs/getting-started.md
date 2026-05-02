# Getting Started

## Requirements

| Platform | Minimum |
|----------|---------|
| Android  | API 24 (7.0) |
| iOS      | 15.0 |
| Kotlin   | 2.3.0 |
| Compose Multiplatform | 1.9.3 |

## Installation

=== "Version Catalog (Recommended)"

    ```toml title="gradle/libs.versions.toml"
    [versions]
    logs = "1.0.0"

    [libraries]
    logs-core      = { module = "io.github.abdo-essam:log", version.ref = "logs" }
    logs-network   = { module = "io.github.abdo-essam:log-network", version.ref = "logs" }
    logs-analytics = { module = "io.github.abdo-essam:log-analytics", version.ref = "logs" }
    ```

    ```kotlin title="shared/build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation(libs.logs.core)
                implementation(libs.logs.network)
                implementation(libs.logs.analytics)
            }
        }
    }
    ```

=== "Direct Dependency"

    ```kotlin title="shared/build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("io.github.abdo-essam:log:1.0.0")
                implementation("io.github.abdo-essam:log-network:1.0.0")
                implementation("io.github.abdo-essam:log-analytics:1.0.0")
            }
        }
    }
    ```

=== "Android Only (Debug)"

    ```kotlin title="app/build.gradle.kts"
    dependencies {
        debugImplementation("io.github.abdo-essam:log-android:1.0.0")
    }
    ```

## Verify Installation

```kotlin
import com.ae.log.AELog

fun main() {
    val inspector = AELog.default
    println("AELog ready: ${inspector.plugins.value.size} plugins loaded")
}
```

!!! success "Expected output"
    ```text
    AELog ready: 1 plugins loaded    
    ```

## Next Steps
- **Quick Start Guide** — Integrate into your app in 5 minutes
- **Configuration** — Customize behavior and appearance
- **Custom Plugins** — Build your own debug panels
