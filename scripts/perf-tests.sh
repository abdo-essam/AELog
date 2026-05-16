#!/usr/bin/env bash
# =============================================================================
# AELog Performance Test Runner
# =============================================================================
# Runs the commonTest performance/stress tests and shows their [Perf] output
# in a clean, readable format. Much faster than JMH — runs in ~30 seconds.
#
# Usage:
#   ./scripts/perf-tests.sh              # Run all perf tests, all modules
#   ./scripts/perf-tests.sh Storage      # Filter by class name
#   ./scripts/perf-tests.sh "" ios       # Run on iOS simulator (macOS only)
# =============================================================================

set -euo pipefail

FILTER="${1:-Performance}"   # Default: match all *Performance* classes
TARGET="${2:-jvm}"           # jvm | iosSimulatorArm64

CYAN='\033[0;36m'
GRN='\033[0;32m'
RED='\033[0;31m'
YLW='\033[1;33m'
NC='\033[0m'

print_header() {
    echo -e "\n${CYAN}══════════════════════════════════════════════${NC}"
    echo -e "${CYAN}  $1${NC}"
    echo -e "${CYAN}══════════════════════════════════════════════${NC}\n"
}

MODULES=(
    ":core"
    ":plugins:logs"
    ":plugins:network"
)

TASK_SUFFIX="${TARGET}Test"

# Build the Gradle test task list
TASKS=""
for mod in "${MODULES[@]}"; do
    TASKS="$TASKS ${mod}:${TASK_SUFFIX}"
done

print_header "AELog Performance Tests — target: $TARGET"
echo -e "Filter: ${YLW}*${FILTER}*${NC}"
echo -e "Modules: ${CYAN}${MODULES[*]}${NC}\n"

# Run tests, capture output
TMPFILE=$(mktemp /tmp/aelogtest.XXXXXX)

START=$(date +%s)

./gradlew $TASKS \
    --tests "*${FILTER}*" \
    --rerun-tasks \
    -Dkotlinx.coroutines.debug=off \
    2>&1 | tee "$TMPFILE"

END=$(date +%s)
ELAPSED=$((END - START))

# ── Extract [Perf] timing lines ───────────────────────────────────────────────
print_header "Timing Results"

PERF_LINES=$(grep "\[Perf\]" "$TMPFILE" || true)
if [[ -n "$PERF_LINES" ]]; then
    echo "$PERF_LINES" | while IFS= read -r line; do
        # Highlight lines that look slow (contains "s" duration like "1.234s")
        if echo "$line" | grep -qE "[0-9]+\.[0-9]+s\b"; then
            echo -e "${YLW}${line}${NC}"
        else
            echo -e "${GRN}${line}${NC}"
        fi
    done
else
    echo -e "${YLW}No [Perf] output found.${NC}"
    echo "Make sure Gradle is configured to show test stdout."
    echo "Try running with: ./gradlew ... --info | grep '\[Perf\]'"
fi

# ── Test counts ────────────────────────────────────────────────────────────────
print_header "Test Summary"

PASSED=$(grep -c "PASSED" "$TMPFILE" || true)
FAILED=$(grep -c "FAILED" "$TMPFILE" || true)
BUILD_STATUS=$(grep -E "^BUILD (SUCCESSFUL|FAILED)" "$TMPFILE" | tail -1 || true)

echo -e "  ${GRN}Passed:  $PASSED${NC}"
echo -e "  ${RED}Failed:  $FAILED${NC}"
echo -e "  Time:    ${ELAPSED}s"
echo ""

if echo "$BUILD_STATUS" | grep -q "SUCCESSFUL"; then
    echo -e "${GRN}✓ $BUILD_STATUS${NC}"
else
    echo -e "${RED}✗ $BUILD_STATUS${NC}"
    echo ""
    echo "Failed tests:"
    grep "FAILED" "$TMPFILE" | grep -v "^>" || true
fi

# ── HTML report locations ──────────────────────────────────────────────────────
echo ""
echo "HTML Reports:"
for mod in "${MODULES[@]}"; do
    mod_dir=$(echo "$mod" | tr ':' '/' | sed 's|^/||')
    report="${mod_dir}/build/reports/tests/${TASK_SUFFIX}/index.html"
    if [[ -f "$report" ]]; then
        echo "  open $report"
    fi
done

rm -f "$TMPFILE"
