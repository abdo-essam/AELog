/**
 * bg-text-hover.js
 *
 * Implements the vanilla JavaScript cursor tracking logic for the background Text Hover Effect,
 * mirroring shadcn's Framer Motion-based SVG mask-reveal animation.
 */

export function initBgTextHover() {
    const hero = document.querySelector(".hero");
    const svg = document.getElementById("bg-text-hover-svg");
    const mask = document.getElementById("bgRevealMask");

    if (!hero || !svg || !mask) return;

    let mouseX = 50;
    let mouseY = 50;
    let currentX = 50;
    let currentY = 50;
    let isHovering = false;

    // Listen to mouse movement on the hero section to control the reveal gradient
    hero.addEventListener("mouseenter", () => {
        isHovering = true;
        const container = document.querySelector(".text-hover-bg-container");
        if (container) {
            container.style.opacity = "0.22"; // Increase opacity subtly on active hover
        }
    });

    hero.addEventListener("mouseleave", () => {
        isHovering = false;
        const container = document.querySelector(".text-hover-bg-container");
        if (container) {
            container.style.opacity = "0.12"; // Revert to static subtle background opacity
        }
    });

    hero.addEventListener("mousemove", (e) => {
        const svgRect = svg.getBoundingClientRect();
        
        // Calculate coordinate percentages relative to the SVG container dimensions
        const targetX = ((e.clientX - svgRect.left) / svgRect.width) * 100;
        const targetY = ((e.clientY - svgRect.top) / svgRect.height) * 100;

        mouseX = targetX;
        mouseY = targetY;
    });

    function updateMask() {
        if (isHovering) {
            // Easing interpolation for butter-smooth movement
            currentX += (mouseX - currentX) * 0.1;
            currentY += (mouseY - currentY) * 0.1;
        } else {
            // Return to center slowly when cursor exits
            currentX += (50 - currentX) * 0.05;
            currentY += (50 - currentY) * 0.05;
        }

        mask.setAttribute("cx", `${currentX}%`);
        mask.setAttribute("cy", `${currentY}%`);

        requestAnimationFrame(updateMask);
    }

    updateMask();
}
