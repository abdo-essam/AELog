/**
 * arch-diagram.js
 *
 * Single Responsibility: architecture diagram hover interactions.
 * Reads node data from data.js; writes to the static info panel.
 *
 * No global mutable state. All state is local to the init function.
 */

import { ARCH_DATA } from "./data.js";

// ── DOM Element IDs (explicit, no magic strings scattered in logic) ─────────
const PANEL_ID   = "arch-info-panel";
const BADGE_ID   = "arch-info-badge";
const TITLE_ID   = "arch-info-title";
const DESC_ID    = "arch-info-desc";
const CODE_ID    = "arch-info-code";
const NODE_CLASS = "interactive-node";

/**
 * Binds hover interactions to all interactive SVG nodes.
 * Updates the static detail panel on the right on mouseenter.
 */
function initArchDiagram() {
    const panel = document.getElementById(PANEL_ID);
    if (!panel) return; // Not on architecture page — bail early (Fail Fast)

    const badge = document.getElementById(BADGE_ID);
    const title = document.getElementById(TITLE_ID);
    const desc  = document.getElementById(DESC_ID);
    const code  = document.getElementById(CODE_ID);

    const nodes = document.querySelectorAll(`.${NODE_CLASS}`);

    nodes.forEach(node => {
        node.addEventListener("mouseenter", () => onNodeHover(node, { badge, title, desc, code }));
    });
}

/**
 * Pure update: reads data for a node key and writes it to the panel elements.
 * Extracted to keep the event handler under 10 lines (SRP, KISS).
 *
 * @param {Element} node   - The SVG group element that was hovered
 * @param {Object}  panel  - References to the four panel child elements
 */
function onNodeHover(node, panel) {
    const key  = node.getAttribute("data-arch");
    const data = ARCH_DATA[key];
    if (!data) return; // Defensive: unknown node key

    if (panel.badge) panel.badge.textContent = data.badge;
    panel.title.textContent = data.title;
    panel.desc.textContent  = data.desc;
    panel.code.textContent  = data.code;

    if (window.hljs) window.hljs.highlightElement(panel.code);
}

export { initArchDiagram };
