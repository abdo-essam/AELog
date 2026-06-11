/**
 * screenshot-carousel.js
 *
 * Automatically cycles through AELog screenshot images inside the mobile frame mockup.
 * Uses smooth CSS opacity transitions.
 */

export function initScreenshotCarousel() {
    const frame = document.querySelector(".mobile-frame");
    if (!frame) return;

    const screenshots = frame.querySelectorAll(".mobile-screenshot");
    if (screenshots.length <= 1) return;

    let currentIndex = 0;

    setInterval(() => {
        const currentScreen = screenshots[currentIndex];
        const nextIndex = (currentIndex + 1) % screenshots.length;
        const nextScreen = screenshots[nextIndex];

        currentScreen.classList.remove("active");
        nextScreen.classList.add("active");

        currentIndex = nextIndex;
    }, 3000);
}
