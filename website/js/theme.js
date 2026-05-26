/**
 * theme.js
 *
 * Light / Dark mode toggle.
 * Reads from localStorage, applies [data-theme] on <html>,
 * and wires the #theme-toggle button in the navbar.
 */

const STORAGE_KEY  = "aelog-theme";
const LIGHT_THEME  = "light";
const DARK_THEME   = "dark";

/** Return the user's preferred theme: stored value → OS preference → dark */
function getInitialTheme() {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === LIGHT_THEME || stored === DARK_THEME) return stored;
    if (window.matchMedia && window.matchMedia("(prefers-color-scheme: light)").matches) {
        return LIGHT_THEME;
    }
    return DARK_THEME;
}

/** Apply theme to <html> element */
function applyTheme(theme) {
    document.documentElement.setAttribute("data-theme", theme);
}

/** Sync the toggle button icon to the current theme */
function syncToggleButton(theme) {
    const btn = document.getElementById("theme-toggle");
    if (!btn) return;
    const isLight = theme === LIGHT_THEME;
    btn.setAttribute("aria-label", isLight ? "Switch to dark mode" : "Switch to light mode");
    btn.setAttribute("title",      isLight ? "Switch to dark mode" : "Switch to light mode");
    // Swap sun ↔ moon icon
    btn.innerHTML = isLight ? getSunIcon() : getMoonIcon();
}

function getMoonIcon() {
    return `<svg width="18" height="18" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
    </svg>`;
}

function getSunIcon() {
    return `<svg width="18" height="18" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="5"/>
        <line x1="12" y1="1"  x2="12" y2="3"/>
        <line x1="12" y1="21" x2="12" y2="23"/>
        <line x1="4.22" y1="4.22"  x2="5.64" y2="5.64"/>
        <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/>
        <line x1="1" y1="12" x2="3"  y2="12"/>
        <line x1="21" y1="12" x2="23" y2="12"/>
        <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/>
        <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
    </svg>`;
}

export function initTheme() {
    // 1. Apply theme before first paint (already set inline in <head> script if present)
    const theme = getInitialTheme();
    applyTheme(theme);

    // 2. Wire toggle button after DOM is ready
    const btn = document.getElementById("theme-toggle");
    if (!btn) return;

    syncToggleButton(theme);

    btn.addEventListener("click", () => {
        const current = document.documentElement.getAttribute("data-theme") || DARK_THEME;
        const next    = current === LIGHT_THEME ? DARK_THEME : LIGHT_THEME;
        applyTheme(next);
        syncToggleButton(next);
        localStorage.setItem(STORAGE_KEY, next);
    });
}
