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
    aelog = "1.0.0"

    [libraries]
    aelog-core      = { module = "io.github.abdo-essam:ae-log-core", version.ref = "aelog" }
    aelog-network   = { module = "io.github.abdo-essam:ae-log-network", version.ref = "aelog" }
    aelog-analytics = { module = "io.github.abdo-essam:ae-log-analytics", version.ref = "aelog" }
    ```

    ```kotlin title="shared/build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation(libs.aelog.core)
                implementation(libs.aelog.network)
                implementation(libs.aelog.analytics)
            }
        }
    }
    ```

=== "Direct Dependency"

    ```kotlin title="shared/build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("io.github.abdo-essam:ae-log-core:1.0.0")
                implementation("io.github.abdo-essam:ae-log-network:1.0.0")
                implementation("io.github.abdo-essam:ae-log-analytics:1.0.0")
            }
        }
    }
    ```

!!! info "Transitive Dependencies"
    AELog uses `api` dependencies to keep your build file clean. 
    
    * If you use `ae-log-network-ktor`, it **automatically** includes `ae-log-network` and `ae-log-core`.
    * You do **not** need to manually add the core library if you are already using a plugin.

## Verify Installation

```kotlin
import com.ae.log.AELog

fun main() {
    // Simply check if AELog is accessible
    AELog.isEnabled = true
    println("AELog is accessible and enabled: ${AELog.isEnabled}")
}
```

!!! success "Expected output"
    ```text
    AELog is accessible and enabled: true
    ```

## Next Steps
- [Quick Start Guide](quick-start.md) — Integrate into your app in 5 minutes
- [Configuration](configuration.md) — Customize behavior and appearance
- [Custom Plugins](plugins-guide.md) — Build your own debug panels
