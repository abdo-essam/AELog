/**
 * scroll-progress.js
 *
 * Implements a global scroll progress indicator positioned directly below the navbar.
 * Calculates scroll percentage dynamically on window scroll.
 */

export function initScrollProgress() {
    // Create the scroll progress bar element dynamically
    const progressBar = document.createElement("div");
    progressBar.className = "scroll-progress-bar";
    progressBar.id = "scroll-progress-bar";
    
    // Position it fixed right below the navbar (top: 70px matches the navbar height)
    progressBar.style.position = "fixed";
    progressBar.style.top = "70px"; 
    progressBar.style.left = "0";
    progressBar.style.width = "0%";
    progressBar.style.height = "3px";
    progressBar.style.background = "linear-gradient(90deg, var(--accent-blue) 0%, var(--accent-purple) 100%)";
    progressBar.style.zIndex = "999";
    progressBar.style.pointerEvents = "none";
    progressBar.style.transition = "width 0.1s ease-out";

    document.body.appendChild(progressBar);

    const handleScroll = () => {
        const scrollTop = window.scrollY;
        const docHeight = document.documentElement.scrollHeight - window.innerHeight;
        
        if (docHeight <= 0) {
            progressBar.style.width = "0%";
            return;
        }

        const progress = (scrollTop / docHeight) * 100;
        progressBar.style.width = `${progress}%`;
    };

    window.addEventListener("scroll", handleScroll);
    handleScroll(); // Trigger initially to set correct progress state
}
