# PR Preview System Setup Guide

This guide will help you set up the automated JavaFX PR preview system using Fly.io and GitHub Actions.

## Prerequisites

- GitHub repository with admin access
- Fly.io account ([sign up here](https://fly.io/app/sign-up))
- Credit card on file with Fly.io (for app deployments, though costs are minimal)

## Step 1: Set Up Fly.io

### 1.1 Create a Fly.io Account

1. Go to [https://fly.io/app/sign-up](https://fly.io/app/sign-up)
2. Sign up using your GitHub account (recommended) or email
3. Verify your email address

### 1.2 Install Fly CLI (Optional, for local testing)

```bash
# macOS/Linux
curl -L https://fly.io/install.sh | sh

# Windows (PowerShell)
pwsh -Command "iwr https://fly.io/install.ps1 -useb | iex"
```

### 1.3 Get Your Fly.io API Token

1. Log in to Fly.io dashboard: [https://fly.io/dashboard](https://fly.io/dashboard)
2. Click on your profile (top right)
3. Go to "Account" → "Access Tokens"
4. Click "Create Access Token"
5. Name it: `GitHub Actions - fxPlanner`
6. Select scope: `Full Access` (or `Deploy` if available)
7. Click "Create Token"
8. **Copy the token immediately** (you won't be able to see it again)

## Step 2: Configure GitHub Secrets

### 2.1 Add Fly.io API Token to GitHub

1. Go to your GitHub repository
2. Click on "Settings" tab
3. In the left sidebar, click "Secrets and variables" → "Actions"
4. Click "New repository secret"
5. Name: `FLY_API_TOKEN`
6. Value: Paste the Fly.io API token from Step 1.3
7. Click "Add secret"

## Step 3: Verify Setup

### 3.1 Check Workflows

1. Go to the "Actions" tab in your repository
2. You should see three workflows:
   - "Deploy PR Preview" - Runs when PRs are opened/updated
   - "Cleanup PR Preview" - Runs when PRs are closed
   - "Audit Preview Apps" - Runs daily to cleanup orphaned apps

### 3.2 Create a Test PR

1. Create a new branch:
   ```bash
   git checkout -b test-preview-system
   ```

2. Make a small change (e.g., update README.md):
   ```bash
   echo "Testing preview system" >> README.md
   git add README.md
   git commit -m "Test: Verify preview system"
   git push -u origin test-preview-system
   ```

3. Open a Pull Request on GitHub

4. Wait for the workflow to run (5-7 minutes)

5. Check the PR for a comment with the preview URL

6. Click the preview URL to access your JavaFX app in the browser

7. Close or merge the PR and verify the cleanup workflow runs

## Step 4: Verify Deployment

### 4.1 Check Fly.io Dashboard

1. Go to [https://fly.io/dashboard](https://fly.io/dashboard)
2. Click on "Apps" in the left sidebar
3. You should see an app named `fxplanner-pr-{NUMBER}`
4. Click on it to see logs, metrics, and status

### 4.2 Monitor Costs

1. In Fly.io dashboard, click on "Billing"
2. Monitor your usage and costs
3. Expected cost: $0.03-$0.58/month depending on usage
4. Set up billing alerts if needed

## Troubleshooting

### Workflow Fails with "Unauthorized"

**Problem:** `FLY_API_TOKEN` secret is not set or is invalid

**Solution:**
1. Verify the secret is set correctly in GitHub (Settings → Secrets)
2. Regenerate the Fly.io API token and update the GitHub secret
3. Ensure the token has sufficient permissions

### App Fails to Deploy

**Problem:** Docker build or Fly.io deployment fails

**Solution:**
1. Check the workflow logs in GitHub Actions
2. Common issues:
   - Maven build failures: Check `pom.xml` for dependency issues
   - JPro plugin not found: Verify JPro repository is accessible
   - Out of memory: Increase VM memory in `fly.toml`

### App URL Returns 404 or 502

**Problem:** App is deployed but not responding

**Solution:**
1. Check Fly.io logs: `flyctl logs --app fxplanner-pr-{NUMBER}`
2. Verify JPro server is starting correctly
3. Check health check configuration in `fly.toml`
4. Increase startup grace period if app takes longer to start

### Orphaned Apps Not Cleaning Up

**Problem:** Apps remain after PR is closed

**Solution:**
1. Check that the cleanup workflow ran successfully
2. Manually run the audit workflow from GitHub Actions
3. Manually destroy apps using Fly CLI:
   ```bash
   flyctl apps destroy fxplanner-pr-{NUMBER} --yes
   ```

### JPro License Issues

**Problem:** JPro complains about licensing

**Solution:**
1. This project uses JPro's free tier (development/trial)
2. If you see license warnings, verify you're within free tier limits
3. For production use, consider purchasing a JPro license

## Cost Management

### Minimize Costs

1. **Use auto-stop machines**: Already configured in `fly.toml`
2. **Set machine idle timeout**: Apps automatically stop when idle
3. **Regular audits**: The daily audit workflow cleans up orphaned apps
4. **Manual cleanup**: Periodically check Fly.io dashboard for stale apps

### Expected Costs

- **Light usage** (5 PRs/week, 30min each): ~$0.03/month
- **Medium usage** (20 PRs/week, 45min each): ~$0.17/month
- **Heavy usage** (50 PRs/week, 1hr each): ~$0.58/month

## Security Best Practices

1. **Protect secrets**: Never commit `FLY_API_TOKEN` to the repository
2. **Limit token scope**: Use minimum required permissions for Fly.io tokens
3. **Review PR authors**: Consider requiring approval for external contributors
4. **Monitor billing**: Set up alerts in Fly.io for unexpected costs
5. **Regular audits**: Let the scheduled audit workflow run to catch orphaned apps

## Advanced Configuration

### Customize App Region

Edit `.github/workflows/pr-preview-deploy.yml`:

```yaml
--region lhr \  # Change to your preferred region (e.g., iad, sjc, fra)
```

### Adjust Resource Limits

Edit `fly.toml`:

```toml
[[vm]]
  cpu_kind = "shared"
  cpus = 1
  memory_mb = 512  # Increase if needed (costs scale with memory)
```

### Add Basic Authentication

To add password protection to previews, update the Dockerfile to include an HTTP auth proxy or configure JPro's built-in authentication.

## Maintenance

### Regular Tasks

1. **Weekly**: Review Fly.io dashboard for active apps
2. **Monthly**: Check billing and adjust resource limits if needed
3. **Quarterly**: Update dependencies in `pom.xml`
4. **As needed**: Update GitHub Actions workflow versions

### Updating the System

When making changes to the preview system:

1. Test locally first using Docker:
   ```bash
   docker build -t fxplanner-test .
   docker run -p 8080:8080 fxplanner-test
   ```

2. Test on a branch before merging to main

3. Monitor the first few PR previews after updates

## Support

If you encounter issues:

1. Check the [GitHub Issues](https://github.com/debrief/fxPlanner/issues)
2. Review Fly.io documentation: [https://fly.io/docs](https://fly.io/docs)
3. Check JPro documentation: [https://www.jpro.one/docs](https://www.jpro.one/docs)
4. Create a new issue with workflow logs and error messages

## Next Steps

After successful setup:

1. ✅ Test with a real PR to verify everything works
2. ✅ Monitor costs for the first week
3. ✅ Customize PR comment templates if desired
4. ✅ Set up billing alerts in Fly.io
5. ✅ Document any project-specific customizations

Congratulations! Your PR preview system is now set up and ready to use! 🎉
