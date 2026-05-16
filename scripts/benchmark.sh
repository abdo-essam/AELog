#!/usr/bin/env bash
# =============================================================================
# AELog Benchmark Runner
# =============================================================================
# Runs JMH benchmarks, saves results to docs/benchmarks/, and shows a diff
# against the previous run so you can immediately see regressions or gains.
#
# Usage:
#   ./scripts/benchmark.sh                  # Run all benchmarks
#   ./scripts/benchmark.sh Storage          # Run only matching benchmarks
#   ./scripts/benchmark.sh "" diff          # Show diff against last run only
#
# Output files are saved to: docs/benchmarks/
# =============================================================================

set -euo pipefail

RESULTS_DIR="$(dirname "$0")/../docs/benchmarks"
FILTER="${1:-}"           # Optional regex filter, e.g. "Storage" or "LogPipeline"
MODE="${2:-run}"          # "run" or "diff"

mkdir -p "$RESULTS_DIR"

TIMESTAMP=$(date +%Y-%m-%d_%H%M)
LATEST="$RESULTS_DIR/latest.txt"
NEW_FILE="$RESULTS_DIR/results-$TIMESTAMP.txt"
PREV_FILE=$(ls -t "$RESULTS_DIR"/results-*.txt 2>/dev/null | head -1 || true)

# ── Helper colours ─────────────────────────────────────────────────────────────
RED='\033[0;31m'
GRN='\033[0;32m'
YLW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No colour

print_header() {
    echo -e "\n${CYAN}══════════════════════════════════════════════${NC}"
    echo -e "${CYAN}  $1${NC}"
    echo -e "${CYAN}══════════════════════════════════════════════${NC}\n"
}

# ── Diff mode: just compare the last two result files ──────────────────────────
if [[ "$MODE" == "diff" ]]; then
    PREV2=$(ls -t "$RESULTS_DIR"/results-*.txt 2>/dev/null | sed -n '2p' || true)
    if [[ -z "$PREV_FILE" ]]; then
        echo -e "${RED}No benchmark results found in $RESULTS_DIR${NC}"
        echo "Run ./scripts/benchmark.sh first to generate a baseline."
        exit 1
    fi
    if [[ -z "$PREV2" ]]; then
        echo -e "${YLW}Only one result file found — need at least two runs to diff.${NC}"
        echo "Latest: $PREV_FILE"
        exit 0
    fi
    print_header "Benchmark Diff: $(basename "$PREV2") → $(basename "$PREV_FILE")"
    bash "$(dirname "$0")/benchmark-diff.sh" "$PREV2" "$PREV_FILE"
    exit 0
fi

# ── Run benchmarks ─────────────────────────────────────────────────────────────
print_header "AELog JMH Benchmarks — $(date '+%Y-%m-%d %H:%M')"

GRADLE_ARGS=":benchmarks:jvmBenchmark"
if [[ -n "$FILTER" ]]; then
    GRADLE_ARGS="$GRADLE_ARGS --args \".*${FILTER}.*\""
    echo -e "Filter: ${YLW}.*${FILTER}.*${NC}\n"
fi

echo "Running: ./gradlew $GRADLE_ARGS"
echo "Results will be saved to: $NEW_FILE"
echo ""

# Run and tee to both screen and file
eval "./gradlew $GRADLE_ARGS" 2>&1 | tee "$NEW_FILE"

# Copy to latest.txt for quick reference
cp "$NEW_FILE" "$LATEST"

# ── Extract and display the summary table ──────────────────────────────────────
print_header "Results Summary"

grep -E "Benchmark|ops/us|ns/op|ms/op|us/op" "$NEW_FILE" \
    | grep -v "^>" \
    | grep -v "Task" \
    | sed 's/thrpt/THROUGHPUT/g' \
    | sed 's/avgt/AVG TIME  /g' \
    || echo "(no benchmark output found — check the full log above)"

# ── Show diff against previous run ────────────────────────────────────────────
if [[ -n "$PREV_FILE" && "$PREV_FILE" != "$NEW_FILE" ]]; then
    print_header "Changes vs Previous Run: $(basename "$PREV_FILE")"
    bash "$(dirname "$0")/benchmark-diff.sh" "$PREV_FILE" "$NEW_FILE" || true
else
    echo -e "\n${YLW}This is the first run — no previous results to compare against.${NC}"
    echo "Run again after making changes to see the diff."
fi

echo ""
echo -e "${GRN}✓ Done. Full results saved to: $NEW_FILE${NC}"
echo ""
