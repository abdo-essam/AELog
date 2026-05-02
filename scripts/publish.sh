#!/usr/bin/env bash
set -euo pipefail

# Usage: ./scripts/publish.sh 1.0.0

VERSION=${1:?Usage: ./scripts/publish.sh <version>}

echo "🚀 Releasing AELog v$VERSION"
echo "================================="

# 1. Validate
if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9.]+)?$ ]]; then
    echo "❌ Invalid version format: $VERSION"
    echo "   Expected: X.Y.Z or X.Y.Z-alpha01"
    exit 1
fi

# 2. Check clean working tree
if [[ -n $(git status --porcelain) ]]; then
    echo "❌ Working tree is not clean. Commit or stash changes first."
    exit 1
fi

# 3. Check we're on main
BRANCH=$(git branch --show-current)
if [[ "$BRANCH" != "main" ]]; then
    echo "❌ Must release from 'main' branch (currently on '$BRANCH')"
    exit 1
fi

# 4. Update version in gradle.properties
echo "📝 Updating VERSION_NAME to $VERSION"
sed -i.bak "s/^VERSION_NAME=.*/VERSION_NAME=$VERSION/" gradle.properties
rm -f gradle.properties.bak

# 5. Run full check
echo "🧪 Running tests..."
./gradlew clean check allTests

# 6. Verify API compatibility
echo "🔍 Checking API compatibility..."
./gradlew apiCheck

# 7. Commit and tag
echo "📦 Creating release commit and tag..."
git add gradle.properties

# Only commit if there are staged changes
if git diff --cached --quiet; then
    echo "ℹ️  No version change to commit (already at v$VERSION)"
else
    git commit -m "release: v$VERSION"
fi

# Create the tag (fail if it already exists)
if git rev-parse "v$VERSION" >/dev/null 2>&1; then
    echo "❌ Tag v$VERSION already exists!"
    exit 1
fi

git tag -a "v$VERSION" -m "Release v$VERSION"

# 8. Push
echo "⬆️ Pushing to origin..."
git push origin main
git push origin "v$VERSION"

echo ""
echo "✅ Tag v$VERSION pushed!"
echo "   GitHub Actions will now:"
echo "   1. Run full CI"
echo "   2. Publish to Maven Central"
echo "   3. Create GitHub Release with changelog"
echo ""
echo "   Monitor: https://github.com/abdo-essam/AELog/actions"
