/**
 * magic-card.js
 *
 * Implements Magic UI's spotlight border effect.
 * Tracks cursor positions relative to each .magic-card container
 * and exposes --mouse-x and --mouse-y CSS custom properties.
 */

export function initMagicCards() {
    const cards = document.querySelectorAll(".magic-card");
    if (!cards.length) return;

    cards.forEach((card) => {
        const handleMouseMove = (e) => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            card.style.setProperty("--mouse-x", `${x}px`);
            card.style.setProperty("--mouse-y", `${y}px`);
        };

        const handleMouseLeave = () => {
            card.style.removeProperty("--mouse-x");
            card.style.removeProperty("--mouse-y");
        };

        card.addEventListener("mousemove", handleMouseMove);
        card.addEventListener("mouseleave", handleMouseLeave);
    });
}
