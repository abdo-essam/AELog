/**
 * main.js
 *
 * Main Orchestrator and entry point for AELog Landing Page.
 * Implements low-coupling and separation of concerns by delegating
 * feature-specific logic to modular ES sub-systems.
 */

import { initArchDiagram } from "./arch-diagram.js";
import { initSetupGuide } from "./setup-guide.js";
import { initScrollProgress } from "./scroll-progress.js";
import { initMagicCards } from "./magic-card.js";
import { initTextFlip } from "./text-flip.js";
import { initTheme } from "./theme.js";

// DOM Selector Constants to prevent hardcoding strings in methods
const FADE_UP_SELECTOR = ".fade-up";
const GLOW_ORB_SELECTOR = ".glow-orb";
const NAVBAR_SELECTOR = ".navbar";

document.addEventListener("DOMContentLoaded", () => {
    // 0. Theme toggle — must run first to sync button icon state
    initTheme();

    // 1. Initialize UI enhancement features
    initScrollAnimations();
    initMouseGlowEffect();
    initNavbarScroll();
    initScrollProgress();
    initMagicCards();
    initTextFlip();

    // 2. Initialize interactive architecture diagram interactions
    initArchDiagram();

    // 3. Initialize Firebase-style installation steps
    initSetupGuide();
});

/**
 * Scroll reveal animations for premium, modern look.
 */
function initScrollAnimations() {
    const observerOptions = {
        root: null,
        rootMargin: "0px",
        threshold: 0.1
    };

    const observer = new IntersectionObserver((entries, obs) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add("visible");
                obs.unobserve(entry.target); // Run animation once, prevent redundant overhead
            }
        });
    }, observerOptions);

    const fadeElements = document.querySelectorAll(FADE_UP_SELECTOR);
    fadeElements.forEach(el => observer.observe(el));
}

/**
 * Glowing background orbs tracking mouse slightly for rich aesthetics.
 */
function initMouseGlowEffect() {
    const orbs = document.querySelectorAll(GLOW_ORB_SELECTOR);
    if (!orbs.length) return;

    let mouseX = 0;
    let mouseY = 0;
    let targetX = 0;
    let targetY = 0;

    document.addEventListener("mousemove", (e) => {
        mouseX = (e.clientX / window.innerWidth) - 0.5;
        mouseY = (e.clientY / window.innerHeight) - 0.5;
    });

    function animate() {
        // Easing interpolation for butter-smooth movement
        targetX += (mouseX - targetX) * 0.05;
        targetY += (mouseY - targetY) * 0.05;

        orbs[0].style.transform = `translate(${targetX * 50}px, ${targetY * 50}px)`;
        if (orbs[1]) {
            orbs[1].style.transform = `translate(${targetX * -40}px, ${targetY * -40}px)`;
        }

        requestAnimationFrame(animate);
    }

    animate();
}

const NAVBAR_SCROLL_THRESHOLD = 50;

/**
 * Solidify navbar color as page scrolls down to maintain text contrast.
 */
function initNavbarScroll() {
    const navbar = document.querySelector(NAVBAR_SELECTOR);
    if (!navbar) return;

    // Separate concerns by toggling a CSS class instead of injecting inline styles directly
    window.addEventListener("scroll", () => {
        navbar.classList.toggle("scrolled", window.scrollY > NAVBAR_SCROLL_THRESHOLD);
    });
}

