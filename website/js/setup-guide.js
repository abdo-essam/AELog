/**
 * setup-guide.js
 *
 * Interactive split-screen project generator.
 * Lets users pick features they want and instantly generates custom-tailored
 * build.gradle.kts, initialization configuration, and usage snippets.
 */

import { AELOG_VERSION } from "./data.js";

// Global Selection State
const state = {
    features: {
        logs: true,
        network: true,
        analytics: false,
        crashes: false
    },
    client: "ktor", // "ktor" or "okhttp"
    ui: "compose",  // "compose" or "xml"
    showCatalog: false, // Gradle Version Catalog
    currentStep: 1
};

/**
 * Initializes the setup guide customizer.
 */
function initSetupGuide() {
    initFeatureCards();
    initStepper();
    initNetworkChips();
    initCatalogToggle();
    initUiSwitcher();
    initCopyBtn();

    // Initial paint
    updateGeneratedCodes();
}

/**
 * Interactive check/uncheck feature cards
 */
function initFeatureCards() {
    const cards = document.querySelectorAll(".selection-card");
    cards.forEach(card => {
        const feature = card.getAttribute("data-feature");

        card.addEventListener("click", (e) => {
            const checkbox = card.querySelector('input[type="checkbox"]');

            // If click was on checkbox, checkbox state already changed, otherwise toggle it
            if (e.target !== checkbox) {
                checkbox.checked = !checkbox.checked;
            }

            state.features[feature] = checkbox.checked;

            if (checkbox.checked) {
                card.classList.add("active");
            } else {
                card.classList.remove("active");
            }

            // Update network client tabs visibility based on whether network is selected
            const networkClientTabs = document.getElementById("network-client-tabs");
            if (networkClientTabs) {
                if (state.features.network) {
                    networkClientTabs.classList.remove("is-hidden");
                } else {
                    networkClientTabs.classList.add("is-hidden");
                }
            }

            updateGeneratedCodes();
        });
    });
}

/**
 * Handles vertical stepper items navigation
 */
function initStepper() {
    const stepperItems = document.querySelectorAll(".stepper-item");
    stepperItems.forEach(item => {
        item.addEventListener("click", () => {
            stepperItems.forEach(i => i.classList.remove("active"));
            item.classList.add("active");

            const stepNum = parseInt(item.getAttribute("data-step"), 10);
            state.currentStep = stepNum;

            // Hide all step controls wrappers and code containers
            document.querySelectorAll(".step-controls-wrapper").forEach(el => el.classList.add("is-hidden"));
            document.querySelectorAll(".step-code-container").forEach(el => el.classList.add("is-hidden"));

            // Show current step elements
            const currentControls = document.getElementById(`step-${stepNum}-controls`);
            if (currentControls) currentControls.classList.remove("is-hidden");

            const currentCode = document.getElementById(`code-step-${stepNum}`);
            if (currentCode) currentCode.classList.remove("is-hidden");

            updateTabName();
            highlightCurrentStepCode();
        });
    });
}

/**
 * Handles Network client toggles (Ktor vs OkHttp)
 */
function initNetworkChips() {
    const networkChips = document.querySelectorAll("#network-client-tabs .tab-chip");
    networkChips.forEach(chip => {
        chip.addEventListener("click", () => {
            networkChips.forEach(c => c.classList.remove("active"));
            chip.classList.add("active");

            state.client = chip.getAttribute("data-client");
            updateGeneratedCodes();
        });
    });
}

/**
 * Handles Version Catalog toggle button in Step 1
 */
function initCatalogToggle() {
    const catalogToggle = document.getElementById("catalog-toggle");
    const depCodeBlock = document.getElementById("dep-code");
    if (!catalogToggle || !depCodeBlock) return;

    catalogToggle.addEventListener("click", () => {
        state.showCatalog = !state.showCatalog;
        catalogToggle.textContent = state.showCatalog ? "Hide libs.versions.toml" : "Show libs.versions.toml";

        updateTabName();
        updateGeneratedCodes();
    });
}

/**
 * Switch Compose UI and XML View instructions
 */
function initUiSwitcher() {
    const uiChips = document.querySelectorAll("#ui-framework-tabs .tab-chip");
    const composeBlock = document.getElementById("ui-compose");
    const xmlBlock = document.getElementById("ui-xml");
    if (!uiChips.length || !composeBlock || !xmlBlock) return;

    uiChips.forEach(chip => {
        chip.addEventListener("click", () => {
            uiChips.forEach(c => c.classList.remove("active"));
            chip.classList.add("active");

            state.ui = chip.getAttribute("data-ui");
            if (state.ui === "compose") {
                composeBlock.classList.remove("is-hidden");
                xmlBlock.classList.add("is-hidden");
            } else {
                composeBlock.classList.add("is-hidden");
                xmlBlock.classList.remove("is-hidden");
            }
            updateTabName();
        });
    });
}

/**
 * Dynamic Filename updates in IDE Mockup Window Header
 */
function updateTabName() {
    const tabName = document.getElementById("window-tab-name");
    if (!tabName) return;

    switch (state.currentStep) {
        case 1:
            tabName.textContent = state.showCatalog ? "gradle/libs.versions.toml" : "shared/build.gradle.kts";
            break;
        case 2:
            tabName.textContent = "shared/.../Application.kt";
            break;
        case 3:
            tabName.textContent = state.ui === "compose" ? "shared/.../App.kt" : "androidApp/.../MainActivity.kt";
            break;
        case 4:
            tabName.textContent = "shared/.../HomeScreen.kt";
            break;
    }
}

/**
 * Triggers HighlightJS on the currently active step code block
 */
function highlightCurrentStepCode() {
    if (!window.hljs) return;
    const activeContainer = document.querySelector(".step-code-container:not(.is-hidden)");
    if (activeContainer) {
        activeContainer.querySelectorAll("code").forEach(el => window.hljs.highlightElement(el));
    }
}

/**
 * Main state updates function: Dynamically generates the KMP customized snippets
 */
function updateGeneratedCodes() {
    const depCodeBlock = document.getElementById("dep-code");
    const configCodeBlock = document.getElementById("config-code");
    const usageCodeBlock = document.getElementById("usage-code");

    // Empty state: check if no features are active
    const noFeaturesActive = !state.features.logs && !state.features.network && !state.features.analytics && !state.features.crashes;
    if (noFeaturesActive) {
        const msg = `// Please select at least one feature above to generate custom setup code!`;
        if (depCodeBlock) depCodeBlock.textContent = msg;
        if (configCodeBlock) configCodeBlock.textContent = msg;
        if (usageCodeBlock) usageCodeBlock.textContent = msg;
        return;
    }

    // Step 1: Dependencies Code Generation
    if (depCodeBlock) {
        if (state.showCatalog) {
            let snippet = `[versions]
aelog = "${AELOG_VERSION}"

[libraries]
`;
            if (state.features.logs) {
                snippet += `aelog-logs             = { module = "io.github.abdo-essam:ae-log-logs",           version.ref = "aelog" }\n`;
            }
            if (state.features.network) {
                if (state.client === "ktor") {
                    snippet += `aelog-network-ktor     = { module = "io.github.abdo-essam:ae-log-network-ktor",   version.ref = "aelog" }\n`;
                } else {
                    snippet += `aelog-network-okhttp   = { module = "io.github.abdo-essam:ae-log-network-okhttp", version.ref = "aelog" }\n`;
                }
            }
            if (state.features.analytics) {
                snippet += `aelog-analytics        = { module = "io.github.abdo-essam:ae-log-analytics",      version.ref = "aelog" }\n`;
            }
            if (state.features.crashes) {
                snippet += `aelog-crashes          = { module = "io.github.abdo-essam:ae-log-crashes",        version.ref = "aelog" }\n`;
            }
            depCodeBlock.textContent = snippet;
            depCodeBlock.className = "language-toml";
        } else {
            let snippet = `// build.gradle.kts (shared module)
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Core library engine carried transitively by each module
`;
            if (state.features.logs) {
                snippet += `            implementation("io.github.abdo-essam:ae-log-logs:${AELOG_VERSION}")\n`;
            }
            if (state.features.network && state.client === "ktor") {
                snippet += `            implementation("io.github.abdo-essam:ae-log-network-ktor:${AELOG_VERSION}")\n`;
            }
            if (state.features.analytics) {
                snippet += `            implementation("io.github.abdo-essam:ae-log-analytics:${AELOG_VERSION}")\n`;
            }
            if (state.features.crashes) {
                snippet += `            implementation("io.github.abdo-essam:ae-log-crashes:${AELOG_VERSION}")\n`;
            }

            snippet += `        }\n`;

            if (state.features.network && state.client === "okhttp") {
                snippet += `        androidMain.dependencies {\n`;
                snippet += `            implementation("io.github.abdo-essam:ae-log-network-okhttp:${AELOG_VERSION}")\n`;
                snippet += `        }\n`;
            }

            snippet += `    }\n}`;
            depCodeBlock.textContent = snippet;
            depCodeBlock.className = "language-kotlin";
        }
    }

    // Step 2: Custom Configuration Code Generation
    if (configCodeBlock) {
        let snippet = `import com.ae.log.AELog\n`;
        if (state.features.logs) snippet += `import com.ae.log.logs.LogPlugin\n`;
        if (state.features.network) snippet += `import com.ae.log.network.NetworkPlugin\n`;
        if (state.features.analytics) snippet += `import com.ae.log.analytics.AnalyticsPlugin\n`;
        if (state.features.crashes) snippet += `import com.ae.log.crashes.CrashPlugin\n`;

        snippet += `
// AELog boots up automatically with zero-config on Android & iOS!
// If you want to disable the floating notch trigger globally:
AELog.showNotch = false // (defaults to true)

// For manual plugin installation on other targets (e.g. Desktop, etc.):
`;
        if (state.features.logs) snippet += `AELog.install(LogPlugin())\n`;
        if (state.features.network) snippet += `AELog.install(NetworkPlugin())\n`;
        if (state.features.analytics) snippet += `AELog.install(AnalyticsPlugin())\n`;
        if (state.features.crashes) snippet += `AELog.install(CrashPlugin())\n`;

        configCodeBlock.textContent = snippet.trim();
    }

    // Step 4: Usage APIs Code Generation
    if (usageCodeBlock) {
        let snippet = ``;
        if (state.features.logs) {
            snippet += `// 1. Logs — mirrors Android Log.* with tag auto-derivation
AELog.log.v("Auth", "Checking token")
AELog.log.d("Token refreshed") // derived tag → "AuthViewModel"
AELog.log.e("Database", "Failed to clear cache", exception)\n\n`;
        }
        if (state.features.network) {
            if (state.client === "ktor") {
                snippet += `// 2. Network — handled automatically via Ktor Interceptor
val client = HttpClient {
    install(AELogKtorInterceptor) {
        excludeHeaders = setOf("X-Api-Key") // secure by default
    }
}\n\n`;
            } else {
                snippet += `// 2. Network — handled automatically via OkHttp Interceptor
val client = OkHttpClient.Builder()
    .addInterceptor(AELogOkHttpInterceptor())
    .build()\n\n`;
            }
        }
        if (state.features.analytics) {
            snippet += `// 3. Analytics Events
AELog.analytics.logEvent("purchase", mapOf("item" to "premium", "price" to "9.99"))
AELog.analytics.logScreen("HomeScreen")\n\n`;
        }
        if (state.features.crashes) {
            snippet += `// 4. Non-fatal Crash Reporting
try {
    performDangerousTask()
} catch (t: Throwable) {
    AELog.crashes.recordNonFatal(t) // persisted on-device
}`;
        }
        usageCodeBlock.textContent = snippet.trim();
    }

    // Re-apply highlightJS styling
    highlightCurrentStepCode();
}

/**
 * Simple Clipboard Copy logic
 */
function initCopyBtn() {
    const copyBtn = document.getElementById("copy-setup-code");
    if (!copyBtn) return;

    copyBtn.addEventListener("click", () => {
        // Retrieve current active code code snippet
        const activeContainer = document.querySelector(".step-code-container:not(.is-hidden)");
        if (!activeContainer) return;

        let textToCopy = "";

        // Handle step 3 subparts compose/xml blocks
        if (state.currentStep === 3) {
            const activeSubBlock = document.querySelector(`#code-step-3 > div:not(.is-hidden) code`);
            textToCopy = activeSubBlock ? activeSubBlock.textContent : "";
        } else {
            const activeCodeEl = activeContainer.querySelector("code");
            textToCopy = activeCodeEl ? activeCodeEl.textContent : "";
        }

        navigator.clipboard.writeText(textToCopy).then(() => {
            const btnSpan = copyBtn.querySelector("span");
            const originalText = btnSpan.textContent;

            btnSpan.textContent = "Copied!";
            copyBtn.style.borderColor = "var(--accent-blue)";
            copyBtn.style.color = "var(--accent-blue)";

            setTimeout(() => {
                btnSpan.textContent = originalText;
                copyBtn.style.borderColor = "";
                copyBtn.style.color = "";
            }, 2000);
        }).catch(err => {
            console.error("Failed to copy code: ", err);
        });
    });
}

export { initSetupGuide };
