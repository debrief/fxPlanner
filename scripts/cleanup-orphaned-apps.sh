#!/bin/bash
# Script to audit and cleanup orphaned Fly.io preview apps
# This should be run periodically to ensure no apps are left running

set -e

echo "🔍 Auditing Fly.io apps for orphaned previews..."

# Get all apps with the fxplanner-pr prefix
APPS=$(flyctl apps list | grep "fxplanner-pr-" | awk '{print $1}' || true)

if [ -z "$APPS" ]; then
    echo "✅ No preview apps found"
    exit 0
fi

echo "Found the following preview apps:"
echo "$APPS"
echo ""

# For each app, check if the PR is still open
for APP in $APPS; do
    # Extract PR number from app name (e.g., fxplanner-pr-123 -> 123)
    PR_NUMBER=$(echo "$APP" | sed 's/fxplanner-pr-//')
    
    echo "Checking PR #$PR_NUMBER for app $APP..."
    
    # Check PR status using GitHub CLI (requires gh to be installed and authenticated)
    if command -v gh &> /dev/null; then
        PR_STATE=$(gh pr view "$PR_NUMBER" --json state --jq '.state' 2>/dev/null || echo "NOTFOUND")
        
        if [ "$PR_STATE" = "CLOSED" ] || [ "$PR_STATE" = "MERGED" ] || [ "$PR_STATE" = "NOTFOUND" ]; then
            echo "  ⚠️  PR #$PR_NUMBER is $PR_STATE - marking for cleanup"
            echo "  🗑️  Destroying app: $APP"
            flyctl apps destroy "$APP" --yes
            echo "  ✅ Destroyed $APP"
        else
            echo "  ✓  PR #$PR_NUMBER is still OPEN - keeping app"
        fi
    else
        echo "  ⚠️  GitHub CLI (gh) not installed - cannot verify PR status"
        echo "  📊 App info:"
        flyctl status --app "$APP" || true
    fi
    echo ""
done

echo "✅ Audit complete"
