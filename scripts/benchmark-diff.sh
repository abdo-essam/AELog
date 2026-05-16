#!/usr/bin/env bash
# =============================================================================
# AELog Benchmark Diff Tool
# =============================================================================
# Compares two JMH result files and shows which benchmarks got faster, slower,
# or are new/removed. Designed to be called by benchmark.sh but can be used
# standalone:
#
#   ./scripts/benchmark-diff.sh docs/benchmarks/results-before.txt \
#                                docs/benchmarks/results-after.txt
# =============================================================================

set -euo pipefail

BEFORE_FILE="${1:-}"
AFTER_FILE="${2:-}"

if [[ -z "$BEFORE_FILE" || -z "$AFTER_FILE" ]]; then
    echo "Usage: $0 <before.txt> <after.txt>"
    exit 1
fi

# ── Colours ────────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GRN='\033[0;32m'
YLW='\033[1;33m'
CYAN='\033[0;36m'
GRAY='\033[0;90m'
NC='\033[0m'

# ── Extract benchmark lines: "BenchmarkName   value unit" ──────────────────────
# JMH output lines look like:
#   LogPipelineBenchmark.logWithExplicitTag  thrpt   10   45231.234 ±  123.4  ops/us
#   StorageBenchmark.addSingle               avgt    10       3.421 ±    0.1  us/op
extract_benchmarks() {
    local file="$1"
    grep -E "^[a-zA-Z].*Benchmark\." "$file" 2>/dev/null \
        | awk '{print $1, $NF, $(NF-1)}' \
        || true
}

declare -A BEFORE_SCORES
declare -A BEFORE_UNITS

# Load BEFORE values
while IFS= read -r line; do
    name=$(echo "$line" | awk '{print $1}')
    score=$(echo "$line" | awk '{print $2}')
    unit=$(echo "$line" | awk '{print $3}')
    BEFORE_SCORES["$name"]="$score"
    BEFORE_UNITS["$name"]="$unit"
done < <(extract_benchmarks "$BEFORE_FILE")

# ── Compare with AFTER values ──────────────────────────────────────────────────
IMPROVED=0
REGRESSED=0
UNCHANGED=0
NEW_COUNT=0

printf "%-60s %12s %12s %10s\n" "Benchmark" "Before" "After" "Change"
printf "%-60s %12s %12s %10s\n" "─────────────────────────────────────────────────────────" "──────────" "──────────" "────────"

while IFS= read -r line; do
    name=$(echo "$line" | awk '{print $1}')
    after_score=$(echo "$line" | awk '{print $2}')
    unit=$(echo "$line" | awk '{print $3}')

    before_score="${BEFORE_SCORES[$name]:-}"

    if [[ -z "$before_score" ]]; then
        printf "${CYAN}%-60s %12s %12s %10s${NC}\n" "$name" "N/A" "$after_score $unit" "[NEW]"
        ((NEW_COUNT++)) || true
        continue
    fi

    # Calculate % change (awk for floating point)
    pct=$(awk "BEGIN {
        b=$before_score; a=$after_score
        if (b == 0) { print \"N/A\"; exit }
        diff = (a - b) / b * 100
        printf \"%.1f\", diff
    }")

    # Determine if this is throughput (higher=better) or time (lower=better)
    # ops/us, ops/ms → higher is better. ns/op, us/op, ms/op → lower is better
    is_throughput=0
    if [[ "$unit" == *"ops/"* ]]; then
        is_throughput=1
    fi

    # Color: green = improved, red = regressed
    color="$GRAY"
    label=""
    improved=false
    regressed=false

    if [[ "$pct" != "N/A" ]]; then
        numeric_pct=$(echo "$pct" | tr -d '-')
        # Only flag if change is > 2% (noise threshold)
        if (( $(echo "$numeric_pct > 2.0" | bc -l 2>/dev/null || echo 0) )); then
            if [[ $is_throughput -eq 1 ]]; then
                # Throughput: positive pct = IMPROVED (more ops/s is good)
                if [[ "$pct" != -* ]]; then
                    color="$GRN"; label="▲ +${pct}%"; improved=true
                else
                    color="$RED"; label="▼ ${pct}%"; regressed=true
                fi
            else
                # Time: negative pct = IMPROVED (fewer ns is good)
                if [[ "$pct" == -* ]]; then
                    color="$GRN"; label="▲ ${pct}%"; improved=true
                else
                    color="$RED"; label="▼ +${pct}%"; regressed=true
                fi
            fi
        else
            label="${pct}%"
        fi
    fi

    printf "${color}%-60s %12s %12s %10s${NC}\n" \
        "$name" \
        "$before_score $unit" \
        "$after_score $unit" \
        "$label"

    if $improved; then ((IMPROVED++)) || true; fi
    if $regressed; then ((REGRESSED++)) || true; fi
    if ! $improved && ! $regressed; then ((UNCHANGED++)) || true; fi

done < <(extract_benchmarks "$AFTER_FILE")

# ── Summary ────────────────────────────────────────────────────────────────────
echo ""
echo "────────────────────────────────────────────────────────────────────────────"
echo -e "  ${GRN}Improved:  $IMPROVED${NC}   ${RED}Regressed: $REGRESSED${NC}   ${GRAY}Unchanged: $UNCHANGED${NC}   ${CYAN}New: $NEW_COUNT${NC}"
echo "────────────────────────────────────────────────────────────────────────────"

if [[ $REGRESSED -gt 0 ]]; then
    echo -e "\n${RED}⚠ $REGRESSED benchmark(s) regressed — review before merging!${NC}"
    exit 1
elif [[ $IMPROVED -gt 0 ]]; then
    echo -e "\n${GRN}✓ $IMPROVED benchmark(s) improved, no regressions.${NC}"
else
    echo -e "\n${GRAY}No significant changes (within ±2% noise threshold).${NC}"
fi
