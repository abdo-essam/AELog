/**
 * docs.js
 *
 * Single Responsibility: AELog documentation page interactivity.
 * Handles:
 *  - Section navigation (show/hide .docs-section panels)
 *  - Copy-to-clipboard buttons on all code blocks
 *  - Mermaid diagram conversion (pre > code.language-mermaid -> div.mermaid)
 *  - Glow orb mouse tracking
 */

import { initTimeline } from "./timeline.js";
import { initScrollProgress } from "./scroll-progress.js";
import { initMagicCards } from "./magic-card.js";

// ── Constants ──────────────────────────────────────────────────────────────
const DOCS_NAV_LINK_CLASS = ".docs-nav-link";
const DOCS_SECTION_CLASS  = ".docs-section";
const COPY_BTN_CLASS      = "copy-btn";
const COPY_LABEL          = "Copy";
const COPIED_LABEL        = "Copied!";
const COPIED_RESET_MS     = 2000;

const MERMAID_CDN = "https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.esm.min.mjs";

const MERMAID_THEME = {
    theme: "dark",
    startOnLoad: true,
    themeVariables: {
        background:          "#0a0a0c",
        primaryColor:        "#3b82f6",
        primaryTextColor:    "#fff",
        primaryBorderColor:  "#3b82f6",
        lineColor:           "#8b5cf6",
        secondaryColor:      "#10b981",
        tertiaryColor:       "#fca5a5",
    },
};

// ── Entry point ─────────────────────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", () => {
    initNavigation();
    initInstallSwitcher();
    initGlowOrb();
    initMermaidDiagrams();
    initCopyButtons();
    initTimeline();
    initScrollProgress();
    initMagicCards();
});

/**
 * Handles toggling between version catalog and direct dependencies in installation docs.
 */
function initInstallSwitcher() {
    const chips = document.querySelectorAll("#install-framework-tabs .tab-chip");
    const catalogBlock = document.getElementById("install-catalog");
    const directBlock = document.getElementById("install-direct");
    if (!chips.length || !catalogBlock || !directBlock) return;

    chips.forEach(chip => {
        chip.addEventListener("click", () => {
            chips.forEach(c => c.classList.remove("active"));
            chip.classList.add("active");

            const installType = chip.getAttribute("data-install");
            if (installType === "catalog") {
                catalogBlock.classList.remove("is-hidden");
                directBlock.classList.add("is-hidden");
            } else {
                catalogBlock.classList.add("is-hidden");
                directBlock.classList.remove("is-hidden");
            }
        });
    });
}

/**
 * Handles documentation sidebar section switching programmatically.
 * Removes the need for inline HTML onclick attributes (H-1, M-4).
 */
function initNavigation() {
    const navLinks = document.querySelectorAll(DOCS_NAV_LINK_CLASS);
    
    navLinks.forEach(link => {
        link.addEventListener("click", (e) => {
            e.preventDefault();
            const targetId = link.getAttribute("data-section");
            
            // Sync active state across all links pointing to the same section
            navLinks.forEach(l => {
                if (l.getAttribute("data-section") === targetId) {
                    l.classList.add("active");
                } else {
                    l.classList.remove("active");
                }
            });
            
            // 2. Hide all doc sections, then show the target one
            document.querySelectorAll(DOCS_SECTION_CLASS).forEach(section => {
                section.classList.remove("active");
            });
            
            const targetSection = document.getElementById(targetId);
            if (targetSection) {
                targetSection.classList.add("active");
            }
            
            window.scrollTo({ top: 0, behavior: "smooth" });
            
            // Recalculate timeline layout instantly when the section is revealed
            setTimeout(() => {
                window.dispatchEvent(new Event("scroll"));
            }, 100);
        });
    });
}

/**
 * Subtle glow orb follows mouse for immersive visual effect.
 * Throttled using requestAnimationFrame to prevent high CPU layout thrashing (M-5).
 */
function initGlowOrb() {
    const orb = document.querySelector(".glow-orb");
    if (!orb) return;

    let mouseX = 0;
    let mouseY = 0;
    let ticking = false;

    document.addEventListener("mousemove", (e) => {
        mouseX = e.clientX;
        mouseY = e.clientY;

        if (!ticking) {
            requestAnimationFrame(() => {
                const x = (mouseX / window.innerWidth  - 0.5) * 50;
                const y = (mouseY / window.innerHeight - 0.5) * 50;
                orb.style.transform = `translate(${x}px, ${y}px)`;
                ticking = false;
            });
            ticking = true;
        }
    });
}

/**
 * Converts <pre><code class="language-mermaid"> blocks into
 * <div class="mermaid"> elements for Mermaid to render.
 * Decodes HTML entities before passing text content to Mermaid.
 */
function initMermaidDiagrams() {
    const mermaidBlocks = document.querySelectorAll("pre code.language-mermaid");
    if (!mermaidBlocks.length) return;

    mermaidBlocks.forEach(codeBlock => {
        const pre = codeBlock.parentNode;

        // Decode HTML entities using a temporary textarea (safe, no innerHTML injection)
        const decoder = document.createElement("textarea");
        decoder.innerHTML = codeBlock.innerHTML;

        const mermaidDiv = document.createElement("div");
        mermaidDiv.className = "mermaid";
        mermaidDiv.textContent = decoder.value;

        pre.parentNode.replaceChild(mermaidDiv, pre);
    });

    loadMermaid();
}

/**
 * Dynamically loads and initializes the Mermaid ES module.
 * Isolated to prevent polluting the global scope.
 */
async function loadMermaid() {
    try {
        const { default: mermaid } = await import(MERMAID_CDN);
        mermaid.initialize(MERMAID_THEME);
    } catch (err) {
        // Fail silently — diagrams degrade to plain text if CDN is unavailable
        console.warn("[AELog docs] Mermaid failed to load:", err.message);
    }
}

/**
 * Adds a "Copy" button to every non-mermaid code block.
 * Uses the Clipboard API with graceful error handling.
 */
function initCopyButtons() {
    document.querySelectorAll("pre code:not(.language-mermaid)").forEach(codeBlock => {
        const pre = codeBlock.parentNode;
        pre.style.position = "relative";

        const btn = document.createElement("button");
        btn.className = COPY_BTN_CLASS;
        btn.textContent = COPY_LABEL;
        btn.setAttribute("aria-label", "Copy code to clipboard");

        btn.addEventListener("click", () => copyCode(codeBlock.innerText, btn));

        pre.appendChild(btn);
    });
}

/**
 * Copies text to the clipboard and gives visual feedback on the button.
 *
 * @param {string}      text - The code text to copy
 * @param {HTMLElement} btn  - The button element to update
 */
function copyCode(text, btn) {
    navigator.clipboard.writeText(text).then(() => {
        btn.textContent = COPIED_LABEL;
        btn.classList.add("copied");
        setTimeout(() => {
            btn.textContent = COPY_LABEL;
            btn.classList.remove("copied");
        }, COPIED_RESET_MS);
    }).catch(err => {
        // Fail gracefully — do not crash, do not alert the user
        console.warn("[AELog docs] Clipboard write failed:", err.message);
    });
}
