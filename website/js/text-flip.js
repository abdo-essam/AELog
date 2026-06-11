/**
 * text-flip.js
 *
 * Implements Magic UI's LayoutTextFlip / Aceternity's FlipWords.
 * Automatically cycles through words with a highly legible vertical slide fade.
 * Dynamically adjusts container width to avoid gaps or layout breaks.
 */

export function initTextFlip() {
    const container = document.querySelector(".text-flip-container");
    if (!container) return;

    const words = container.querySelectorAll(".text-flip-word");
    if (words.length <= 1) return;

    // Measures the text width of a specific word
    const setContainerWidth = (word) => {
        const rect = word.getBoundingClientRect();
        // Fallback to text length calculation if rect is not ready
        const width = rect.width > 0 ? rect.width : (word.innerText.length * 10);
        container.style.width = `${width + 4}px`;
    };

    // Set initial width to match the first word
    const firstWord = container.querySelector(".text-flip-word.active") || words[0];
    
    // Ensure styles are resolved before measuring
    setTimeout(() => {
        setContainerWidth(firstWord);
    }, 100);

    // Readjust on resize
    window.addEventListener("resize", () => {
        const activeWord = container.querySelector(".text-flip-word.active");
        if (activeWord) setContainerWidth(activeWord);
    });

    let currentIndex = 0;

    setInterval(() => {
        const currentWord = words[currentIndex];
        const nextIndex = (currentIndex + 1) % words.length;
        const nextWord = words[nextIndex];

        // 1. Slide out current word
        currentWord.classList.remove("active");
        currentWord.classList.add("exit");

        // 2. Slide in next word
        nextWord.classList.remove("exit");
        nextWord.classList.add("active");

        // 3. Update container width to match the new active word
        setContainerWidth(nextWord);

        // 4. Remove exit class after transition completes (400ms)
        setTimeout(() => {
            currentWord.classList.remove("exit");
        }, 400);

        currentIndex = nextIndex;
    }, 2500);
}
