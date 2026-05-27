/**
 * docs.js
 *
 * Premium documentation engine for AELog documentation page.
 * Responsibilities:
 *  - Categorized Section switching with smooth scroll to top
 *  - Dynamic right-hand Table of Contents (Outline) scroll spy
 *  - Client-side Instant Search & keyword highlighting
 *  - Automatic symmetrical Next/Previous Pagination
 *  - Mobile sliding sidebar overlay menu drawer
 *  - Mermaid diagram conversion & Dynamic code blocks copy actions
 */

import { initTimeline } from "./timeline.js";
import { initScrollProgress } from "./scroll-progress.js";
import { initMagicCards } from "./magic-card.js";
import { initTheme } from "./theme.js";

// ── Constants & State ────────────────────────────────────────────────────────
const DOCS_NAV_LINK_CLASS = ".docs-nav-link";
const DOCS_SECTION_CLASS  = ".docs-section";
const COPY_BTN_CLASS      = "copy-btn";
const COPY_LABEL          = "COPY";
const COPIED_LABEL        = "COPIED!";
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

// Map to cache initial innerHTML of sections to restore original state when search is cleared
const originalSectionHTMLs = new Map();

// ── Entry point ─────────────────────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", () => {
    // 0. Theme toggle — must run first to sync button icon state
    initTheme();

    // 1. Cache original content HTML elements first for Search recovery
    document.querySelectorAll(".docs-section").forEach(sec => {
        originalSectionHTMLs.set(sec.getAttribute("id"), sec.innerHTML);
    });

    // 2. Initialize interactive sub-systems
    initNavigation();
    initInstallSwitcher();
    initGlowOrb();
    initMermaidDiagrams();
    initCopyButtons();
    initTimeline();
    initScrollProgress();
    initMagicCards();
    initNavbarScroll();
    
    // 3. Initialize new premium UX systems
    initSearchEngine();
    updateTableOfContents();
    updatePagination();
    initMobileDrawer();
    
    // Handle loading from hash if present in URL
    handleInitialHashNavigation();
});

/**
 * Solidify navbar background when the user scrolls past the threshold.
 */
function initNavbarScroll() {
    const navbar = document.querySelector(".navbar");
    if (!navbar) return;
    navbar.classList.toggle("scrolled", window.scrollY > 50);
    window.addEventListener("scroll", () => {
        navbar.classList.toggle("scrolled", window.scrollY > 50);
    });
}

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
 */
function initNavigation() {
    const navLinks = document.querySelectorAll(DOCS_NAV_LINK_CLASS);
    
    navLinks.forEach(link => {
        link.addEventListener("click", (e) => {
            e.preventDefault();
            const targetId = link.getAttribute("data-section");
            switchActiveSection(targetId);
        });
    });
}

/**
 * Switch active documentation panel seamlessly
 * @param {string} targetId - ID of target section block
 */
function switchActiveSection(targetId) {
    const navLinks = document.querySelectorAll(DOCS_NAV_LINK_CLASS);
    const targetLink = Array.from(navLinks).find(l => l.getAttribute("data-section") === targetId);
    if (!targetLink) return;

    // Sync active state across sidebar links
    navLinks.forEach(l => {
        if (l.getAttribute("data-section") === targetId) {
            l.classList.add("active");
        } else {
            l.classList.remove("active");
        }
    });
    
    // Hide all doc sections, then show the target one
    document.querySelectorAll(DOCS_SECTION_CLASS).forEach(section => {
        section.classList.remove("active");
    });
    
    const targetSection = document.getElementById(targetId);
    if (targetSection) {
        targetSection.classList.add("active");
    }
    
    window.scrollTo({ top: 0, behavior: "smooth" });
    
    // Rebuild page dependencies
    updateTableOfContents();
    updatePagination();
    
    // Recalculate timeline layout instantly when the section is revealed
    setTimeout(() => {
        window.dispatchEvent(new Event("scroll"));
    }, 100);
}

/**
 * Allows linking directly to subheadings or sections from index.html (hash targets)
 */
function handleInitialHashNavigation() {
    const hash = window.location.hash;
    if (!hash) return;
    
    const targetId = hash.replace("#", "");
    
    // Check if hash matches a main section ID
    const section = document.getElementById(targetId);
    if (section && section.classList.contains("docs-section")) {
        switchActiveSection(targetId);
    } else {
        // Hash points to an internal header (h2/h3) inside a section
        const header = document.getElementById(targetId);
        if (header) {
            const parentSection = header.closest(".docs-section");
            if (parentSection) {
                switchActiveSection(parentSection.id);
                setTimeout(() => {
                    header.scrollIntoView({ behavior: "smooth" });
                }, 400);
            }
        }
    }
}

/**
 * Subtle glow orb follows mouse for immersive visual effect.
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
 * Converts <pre><code class="language-mermaid"> blocks into divs for Mermaid
 */
function initMermaidDiagrams() {
    const mermaidBlocks = document.querySelectorAll("pre code.language-mermaid");
    if (!mermaidBlocks.length) return;

    mermaidBlocks.forEach(codeBlock => {
        const pre = codeBlock.parentNode;
        const decoder = document.createElement("textarea");
        decoder.innerHTML = codeBlock.innerHTML;

        const mermaidDiv = document.createElement("div");
        mermaidDiv.className = "mermaid";
        mermaidDiv.textContent = decoder.value;

        pre.parentNode.replaceChild(mermaidDiv, pre);
    });

    loadMermaid();
}

async function loadMermaid() {
    try {
        const { default: mermaid } = await import(MERMAID_CDN);
        mermaid.initialize(MERMAID_THEME);
    } catch (err) {
        console.warn("[AELog docs] Mermaid failed to load:", err.message);
    }
}

/**
 * Adds a "Copy" button to every code block
 */
function initCopyButtons() {
    // Prevent duplicate buttons when search resets
    document.querySelectorAll(`.${COPY_BTN_CLASS}`).forEach(el => el.remove());

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

function copyCode(text, btn) {
    navigator.clipboard.writeText(text).then(() => {
        btn.textContent = COPIED_LABEL;
        btn.classList.add("copied");
        setTimeout(() => {
            btn.textContent = COPY_LABEL;
            btn.classList.remove("copied");
        }, COPIED_RESET_MS);
    }).catch(err => {
        console.warn("[AELog docs] Clipboard write failed:", err.message);
    });
}

// ── New Premium UX Systems ──────────────────────────────────────────────────

/**
 * Dynamic Right-hand Table of Contents Outline Generator & Spy
 */
let outlineObserver = null;

function updateTableOfContents() {
    const activeSection = document.querySelector(".docs-section.active");
    const outlineList = document.getElementById("docs-outline-list");
    const outlineSidebar = document.querySelector(".docs-outline-sidebar");
    if (!activeSection || !outlineList || !outlineSidebar) return;
    
    // Disconnect old observer to avoid overhead leaks
    if (outlineObserver) {
        outlineObserver.disconnect();
    }
    
    outlineList.innerHTML = "";
    
    const headings = activeSection.querySelectorAll("h2, h3");
    if (!headings.length) {
        outlineSidebar.style.opacity = "0";
        outlineSidebar.style.pointerEvents = "none";
        return;
    }
    
    outlineSidebar.style.opacity = "1";
    outlineSidebar.style.pointerEvents = "all";
    
    headings.forEach((heading, idx) => {
        if (!heading.id) {
            heading.id = `heading-${activeSection.id}-${idx}`;
        }
        
        const li = document.createElement("li");
        li.className = heading.tagName.toLowerCase() === "h3" ? "outline-item outline-item-h3" : "outline-item";
        
        const a = document.createElement("a");
        a.href = `#${heading.id}`;
        a.className = "outline-link";
        a.textContent = heading.textContent;
        
        a.addEventListener("click", (e) => {
            e.preventDefault();
            const targetHeader = document.getElementById(heading.id);
            if (targetHeader) {
                targetHeader.scrollIntoView({ behavior: "smooth" });
                document.querySelectorAll(".outline-link").forEach(l => l.classList.remove("active"));
                a.classList.add("active");
            }
        });
        
        li.appendChild(a);
        outlineList.appendChild(li);
    });
    
    initOutlineScrollSpy(headings);
}

function initOutlineScrollSpy(headings) {
    const outlineLinks = document.querySelectorAll(".outline-link");
    if (!headings.length || !outlineLinks.length) return;
    
    const observerOptions = {
        root: null,
        rootMargin: "-120px 0px -75% 0px",
        threshold: 0
    };
    
    outlineObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const activeId = entry.target.id;
                outlineLinks.forEach(link => {
                    if (link.getAttribute("href") === `#${activeId}`) {
                        link.classList.add("active");
                    } else {
                        link.classList.remove("active");
                    }
                });
            }
        });
    }, observerOptions);
    
    headings.forEach(h => outlineObserver.observe(h));
}

/**
 * Symmetrical Next/Previous Pagination Manager
 */
function updatePagination() {
    const navLinks = Array.from(document.querySelectorAll(DOCS_NAV_LINK_CLASS));
    const activeSection = document.querySelector(".docs-section.active");
    const prevBtn = document.getElementById("prev-page-btn");
    const nextBtn = document.getElementById("next-page-btn");
    
    if (!navLinks.length || !activeSection || !prevBtn || !nextBtn) return;
    
    const activeId = activeSection.getAttribute("id");
    const activeIdx = navLinks.findIndex(link => link.getAttribute("data-section") === activeId);
    
    // Update Previous button
    if (activeIdx > 0) {
        prevBtn.style.display = "flex";
        const prevLink = navLinks[activeIdx - 1];
        document.getElementById("prev-page-title").textContent = prevLink.textContent;
        prevBtn.onclick = (e) => {
            e.preventDefault();
            prevLink.click();
        };
    } else {
        prevBtn.style.display = "none";
    }
    
    // Update Next button
    if (activeIdx < navLinks.length - 1) {
        nextBtn.style.display = "flex";
        const nextLink = navLinks[activeIdx + 1];
        document.getElementById("next-page-title").textContent = nextLink.textContent;
        nextBtn.onclick = (e) => {
            e.preventDefault();
            nextLink.click();
        };
    } else {
        nextBtn.style.display = "none";
    }
}

/**
 * Dynamic Client-side Search Engine & keyword highlighting
 */
function initSearchEngine() {
    const searchInput = document.getElementById("docs-search-input");
    const clearBtn = document.getElementById("docs-search-clear");
    const navLinks = document.querySelectorAll(DOCS_NAV_LINK_CLASS);
    const groups = document.querySelectorAll(".sidebar-category-group");
    
    if (!searchInput) return;
    
    searchInput.addEventListener("input", (e) => {
        const query = e.target.value.trim().toLowerCase();
        
        if (query) {
            clearBtn.style.display = "block";
        } else {
            clearBtn.style.display = "none";
        }
        
        // Restore all sections to original inner HTML to clear past markers
        document.querySelectorAll(".docs-section").forEach(sec => {
            const originalHTML = originalSectionHTMLs.get(sec.id);
            if (originalHTML) {
                sec.innerHTML = originalHTML;
            }
        });
        
        if (!query) {
            navLinks.forEach(link => link.style.display = "block");
            groups.forEach(group => group.style.display = "flex");
            
            updateTableOfContents();
            initCopyButtons();
            initMermaidDiagrams();
            initTimeline();
            return;
        }
        
        navLinks.forEach(link => {
            const sectionId = link.getAttribute("data-section");
            const section = document.getElementById(sectionId);
            if (!section) return;
            
            const sectionText = section.textContent.toLowerCase();
            const isMatch = sectionText.includes(query);
            
            if (isMatch) {
                link.style.display = "block";
                if (section.classList.contains("active")) {
                    highlightKeywords(section, query);
                }
            } else {
                link.style.display = "none";
            }
        });
        
        // Hide sidebar category headers if all children match nothing
        groups.forEach(group => {
            const visibleLinks = group.querySelectorAll(`${DOCS_NAV_LINK_CLASS}[style="display: block;"]`);
            const totalLinks = group.querySelectorAll(DOCS_NAV_LINK_CLASS);
            
            if (visibleLinks.length === 0 && totalLinks.length > 0) {
                group.style.display = "none";
            } else {
                group.style.display = "flex";
            }
        });
        
        // Rebind events for components affected by innerHTML overwrites
        initCopyButtons();
        initMermaidDiagrams();
        updateTableOfContents();
    });
    
    clearBtn.addEventListener("click", () => {
        searchInput.value = "";
        searchInput.dispatchEvent(new Event("input"));
    });
}

/**
 * Escapes characters for dynamic Regular Expression creation
 */
function escapeRegExp(string) {
    return string.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

/**
 * TreeWalker based keyword highlighter (avoids tags, pre/code blocks, scripts)
 */
function highlightKeywords(section, query) {
    if (!query) return;
    const escapedQuery = escapeRegExp(query);
    const regex = new RegExp(`(${escapedQuery})`, "gi");
    
    const walk = document.createTreeWalker(section, NodeFilter.SHOW_TEXT, null, false);
    const textNodes = [];
    let node;
    
    while (node = walk.nextNode()) {
        const parentTag = node.parentNode.tagName;
        if (parentTag !== "CODE" && parentTag !== "PRE" && parentTag !== "SCRIPT" && parentTag !== "STYLE" && parentTag !== "A" && !node.parentNode.classList.contains("copy-btn")) {
            textNodes.push(node);
        }
    }
    
    textNodes.forEach(node => {
        const val = node.nodeValue;
        if (regex.test(val)) {
            const span = document.createElement("span");
            span.innerHTML = val.replace(regex, `<span class="search-highlight">$1</span>`);
            node.parentNode.replaceChild(span, node);
        }
    });
}

/**
 * Floating Responsive Drawer Overlay Menu for Tablet & Mobile Layouts
 */
function initMobileDrawer() {
    const toggleBtn = document.getElementById("mobile-sidebar-toggle");
    const sidebar = document.getElementById("docs-sidebar");
    if (!toggleBtn || !sidebar) return;
    
    // Check if backdrop exists, otherwise create it
    let backdrop = document.querySelector(".docs-drawer-backdrop");
    if (!backdrop) {
        backdrop = document.createElement("div");
        backdrop.className = "docs-drawer-backdrop";
        document.body.appendChild(backdrop);
    }
    
    function openMenu() {
        sidebar.classList.add("active");
        backdrop.classList.add("active");
        document.body.style.overflow = "hidden";
    }
    
    function closeMenu() {
        sidebar.classList.remove("active");
        backdrop.classList.remove("active");
        document.body.style.overflow = "";
    }
    
    toggleBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        if (sidebar.classList.contains("active")) {
            closeMenu();
        } else {
            openMenu();
        }
    });
    
    backdrop.addEventListener("click", closeMenu);
    
    // Auto close drawer when sidebar navigation elements are selected
    document.querySelectorAll(DOCS_NAV_LINK_CLASS).forEach(link => {
        link.addEventListener("click", closeMenu);
    });
}
