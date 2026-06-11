/**
 * timeline.js
 *
 * Implements vertical scroll tracking and highlight transitions for the development timeline.
 * Emulates motion-react's ScrollProgress behavior using lightweight, native scroll events.
 */

export function initTimeline() {
    const timeline = document.getElementById("journey-timeline");
    const fill = document.getElementById("timeline-progress-fill");
    const items = document.querySelectorAll(".timeline-item");

    if (!timeline || !fill || !items.length) return;

    function handleTimelineScroll() {
        const rect = timeline.getBoundingClientRect();
        const windowHeight = window.innerHeight;

        // Custom thresholds to replicate React component offsets
        const startPoint = windowHeight * 0.7;
        const endPoint = windowHeight * 0.3;

        const totalHeight = rect.height;
        const currentTop = rect.top;

        let progress = 0;

        if (currentTop < startPoint) {
            const scrolledDistance = startPoint - currentTop;
            const scrollRange = totalHeight - (endPoint - startPoint);
            progress = Math.min(Math.max((scrolledDistance / scrollRange) * 100, 0), 100);
        }

        fill.style.height = `${progress}%`;

        // Check visibility and active highlight status for each timeline item
        items.forEach((item) => {
            const itemRect = item.getBoundingClientRect();
            // Activate when item crosses 60% of viewport height
            if (itemRect.top < windowHeight * 0.6) {
                item.classList.add("active");
                item.classList.add("visible");
            } else {
                item.classList.remove("active");
            }
        });
    }

    // Bind scroll listener and run once initially
    window.addEventListener("scroll", handleTimelineScroll);
    handleTimelineScroll();
}
