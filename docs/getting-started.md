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
    devlens = "1.0.0"

    [libraries]
    devlens-core      = { module = "io.github.abdo-essam:devlens", version.ref = "devlens" }
    devlens-network   = { module = "io.github.abdo-essam:devlens-network", version.ref = "devlens" }
    devlens-analytics = { module = "io.github.abdo-essam:devlens-analytics", version.ref = "devlens" }
    ```

    ```kotlin title="shared/build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation(libs.devlens.core)
                implementation(libs.devlens.network)
                implementation(libs.devlens.analytics)
            }
        }
    }
    ```

=== "Direct Dependency"

    ```kotlin title="shared/build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("io.github.abdo-essam:devlens:1.0.0")
                implementation("io.github.abdo-essam:devlens-network:1.0.0")
                implementation("io.github.abdo-essam:devlens-analytics:1.0.0")
            }
        }
    }
    ```

=== "Android Only (Debug)"

    ```kotlin title="app/build.gradle.kts"
    dependencies {
        debugImplementation("io.github.abdo-essam:devlens-android:1.0.0")
    }
    ```

## Verify Installation

```kotlin
import com.ae.devlens.AEDevLens

fun main() {
    val inspector = AEDevLens.default
    println("AEDevLens ready: ${inspector.plugins.value.size} plugins loaded")
}
```

!!! success "Expected output"
    ```text
    AEDevLens ready: 1 plugins loaded    
    ```

## Next Steps
- **Quick Start Guide** — Integrate into your app in 5 minutes
- **Configuration** — Customize behavior and appearance
- **Custom Plugins** — Build your own debug panels
