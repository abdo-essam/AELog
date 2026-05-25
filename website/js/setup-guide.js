/**
 * setup-guide.js
 *
 * Single Responsibility: Interactive Firebase-style installation steps.
 * Handles scenario tab selection, version catalog toggle, and UI framework switcher.
 *
 * Reads data snippets dynamically from data.js.
 */

import { DEP_SNIPPETS } from "./data.js";

// DOM Selector Constants to avoid magic selectors inside logic
const SCENARIO_TAB_SELECTOR = ".scenario-tab";
const DEP_CODE_ID = "dep-code";
const CATALOG_TOGGLE_ID = "catalog-toggle";
const CATALOG_SNIPPET_ID = "catalog-snippet";
const TAB_CHIP_SELECTOR = ".tab-chip";
const UI_COMPOSE_ID = "ui-compose";
const UI_XML_ID = "ui-xml";

/**
 * Initializes all step guide interactions.
 */
function initSetupGuide() {
    initScenarioTabs();
    initCatalogToggle();
    initUiSwitcher();
}

/**
 * Switch gradle dependency snippet when clicking scenario buttons.
 */
function initScenarioTabs() {
    const depCode = document.getElementById(DEP_CODE_ID);
    const scenarioTabs = document.querySelectorAll(SCENARIO_TAB_SELECTOR);
    if (!depCode || !scenarioTabs.length) return;

    // Fill initial state
    depCode.textContent = DEP_SNIPPETS["logs"];
    if (window.hljs) window.hljs.highlightElement(depCode);

    scenarioTabs.forEach(tab => {
        tab.addEventListener("click", () => {
            scenarioTabs.forEach(t => t.classList.remove("active"));
            tab.classList.add("active");
            
            const scenario = tab.getAttribute("data-scenario");
            depCode.textContent = DEP_SNIPPETS[scenario] || "";
            
            if (window.hljs) window.hljs.highlightElement(depCode);
        });
    });
}

/**
 * Toggle libs.versions.toml container visibility.
 */
function initCatalogToggle() {
    const catalogToggle = document.getElementById(CATALOG_TOGGLE_ID);
    const catalogSnippet = document.getElementById(CATALOG_SNIPPET_ID);
    if (!catalogToggle || !catalogSnippet) return;

    catalogToggle.addEventListener("click", () => {
        const isHidden = catalogSnippet.style.display === "none";
        catalogSnippet.style.display = isHidden ? "block" : "none";
        catalogToggle.textContent = isHidden ? "Hide libs.versions.toml" : "Show libs.versions.toml";
        
        if (isHidden && window.hljs) {
            catalogSnippet.querySelectorAll("code").forEach(el => window.hljs.highlightElement(el));
        }
    });
}

/**
 * Switch between Compose UI and XML View instructions.
 */
function initUiSwitcher() {
    const uiChips = document.querySelectorAll(TAB_CHIP_SELECTOR);
    const composeBlock = document.getElementById(UI_COMPOSE_ID);
    const xmlBlock = document.getElementById(UI_XML_ID);
    if (!uiChips.length || !composeBlock || !xmlBlock) return;

    uiChips.forEach(chip => {
        chip.addEventListener("click", () => {
            uiChips.forEach(c => c.classList.remove("active"));
            chip.classList.add("active");

            const uiType = chip.getAttribute("data-ui");
            if (uiType === "compose") {
                composeBlock.style.display = "block";
                xmlBlock.style.display = "none";
            } else {
                composeBlock.style.display = "none";
                xmlBlock.style.display = "block";
            }
        });
    });
}

export { initSetupGuide };
