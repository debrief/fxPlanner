# Troubleshooting Guide

This guide covers common issues you might encounter with the JavaFX PR Preview system.

## Table of Contents

- [Deployment Issues](#deployment-issues)
- [Runtime Issues](#runtime-issues)
- [GitHub Actions Issues](#github-actions-issues)
- [Fly.io Issues](#flyio-issues)
- [JPro Issues](#jpro-issues)
- [Cost and Billing](#cost-and-billing)

## Deployment Issues

### Build Fails: "Could not find artifact com.sandec.jpro"

**Symptoms:**
```
[ERROR] Failed to execute goal on project fxplanner: Could not resolve dependencies
[ERROR] Could not find artifact com.sandec.jpro:jpro-maven-plugin
```

**Cause:** JPro repository is not accessible or JPro version doesn't exist

**Solutions:**
1. Check if the JPro repository is accessible:
   ```bash
   curl -I https://sandec.jfrog.io/artifactory/repo
   ```

2. Verify JPro version in `pom.xml` exists:
   - Visit [JPro Maven Repository](https://sandec.jfrog.io/artifactory/repo/com/sandec/jpro/)
   - Update `<jpro.version>` to a stable release

3. Check your internet connection and DNS resolution

### Docker Build Timeout

**Symptoms:**
```
Error: The operation was canceled.
Error: Process completed with exit code 1.
```

**Cause:** Build takes longer than GitHub Actions timeout (15 minutes)

**Solutions:**
1. Optimize Dockerfile with better caching:
   - Ensure `COPY pom.xml .` happens before `COPY src`
   - This allows Maven to cache dependencies

2. Use `--remote-only` flag (already configured):
   ```yaml
   flyctl deploy --remote-only
   ```

3. Reduce build complexity:
   - Skip tests during Docker build: `-DskipTests`
   - Use multi-stage builds to keep final image small

### Insufficient Memory During Build

**Symptoms:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Cause:** Maven build runs out of memory

**Solutions:**
1. Increase JAVA_OPTS in Dockerfile:
   ```dockerfile
   ENV JAVA_OPTS="-Xmx1024m"  # Increase from 512m
   ```

2. Update fly.toml to allocate more memory:
   ```toml
   [[vm]]
     memory_mb = 1024  # Increase from 512
   ```

3. Optimize build by excluding unnecessary resources

## Runtime Issues

### App Deploys But Returns 502 Bad Gateway

**Symptoms:**
- Deployment succeeds
- URL returns "502 Bad Gateway"
- App shows as "running" in Fly.io

**Cause:** JPro server not starting or crashing on startup

**Solutions:**
1. Check Fly.io logs:
   ```bash
   flyctl logs --app fxplanner-pr-{NUMBER}
   ```

2. Look for Java exceptions or JPro startup errors

3. Common issues:
   - JavaFX dependencies missing
   - Main class not found
   - Port mismatch (ensure app listens on 8080)

4. Test locally with Docker:
   ```bash
   docker build -t fxplanner-test .
   docker run -p 8080:8080 fxplanner-test
   ```

### Health Checks Failing

**Symptoms:**
```
Health check on port 8080 failed
```

**Cause:** JPro server takes too long to start or crashes

**Solutions:**
1. Increase health check grace period in `fly.toml`:
   ```toml
   [[http_service.checks]]
     grace_period = "120s"  # Increase from 60s
   ```

2. Verify JPro is actually starting:
   ```bash
   flyctl ssh console --app fxplanner-pr-{NUMBER}
   curl localhost:8080
   ```

3. Check if port 8080 is correct:
   - JPro default is 8080
   - Verify in logs what port JPro binds to

### App Stops Immediately After Starting

**Symptoms:**
- App starts, then immediately stops
- No error messages in logs

**Cause:** Process exits too quickly or auto-stop is too aggressive

**Solutions:**
1. Check if main process stays running:
   - Ensure entrypoint script doesn't exit
   - JPro server should run in foreground

2. Disable auto-stop temporarily for debugging:
   ```toml
   [http_service]
     auto_stop_machines = false
   ```

3. Add keepalive to entrypoint script:
   ```bash
   exec mvn jpro:run  # Use exec to keep process running
   ```

## GitHub Actions Issues

### Workflow Not Triggering

**Symptoms:**
- PR opened but no workflow runs
- No bot comment appears

**Cause:** Workflow files not in correct location or syntax errors

**Solutions:**
1. Verify files are in `.github/workflows/` directory

2. Check workflow syntax:
   ```bash
   # Install actionlint
   brew install actionlint  # macOS
   
   # Check workflow files
   actionlint .github/workflows/*.yml
   ```

3. Check repository settings:
   - Settings → Actions → General
   - Ensure "Allow all actions" is enabled

4. Check branch protection rules don't block workflows

### "FLY_API_TOKEN Secret Not Found"

**Symptoms:**
```
Error: Input required and not supplied: api-token
```

**Cause:** `FLY_API_TOKEN` secret not configured

**Solutions:**
1. Add secret to GitHub:
   - Settings → Secrets and variables → Actions
   - New repository secret: `FLY_API_TOKEN`

2. Verify secret name matches workflow:
   ```yaml
   env:
     FLY_API_TOKEN: ${{ secrets.FLY_API_TOKEN }}
   ```

3. Check organization vs repository secrets if using organization

### PR Comment Not Posted

**Symptoms:**
- Deployment succeeds
- No comment appears on PR

**Cause:** GitHub token permissions insufficient

**Solutions:**
1. Check workflow permissions:
   ```yaml
   permissions:
     pull-requests: write
     contents: read
   ```

2. Verify GITHUB_TOKEN has correct scopes:
   - Settings → Actions → General → Workflow permissions
   - Enable "Read and write permissions"

3. Check if workflow runs in fork (forks have limited permissions)

## Fly.io Issues

### "App Name Already Taken"

**Symptoms:**
```
Error: App name fxplanner-pr-123 is already taken
```

**Cause:** Previous deployment didn't clean up or app exists in different organization

**Solutions:**
1. Manually destroy the app:
   ```bash
   flyctl apps destroy fxplanner-pr-123 --yes
   ```

2. Check which organization owns the app:
   ```bash
   flyctl apps list | grep fxplanner
   ```

3. Update app name pattern to include organization:
   ```yaml
   APP_NAME="org-fxplanner-pr-${{ github.event.pull_request.number }}"
   ```

### "Insufficient Resources" or "Out of Capacity"

**Symptoms:**
```
Error: unable to allocate machine: insufficient capacity
```

**Cause:** Fly.io region is at capacity

**Solutions:**
1. Try different region in workflow:
   ```yaml
   --region iad \  # Try US region
   --region fra \  # Try Europe region
   ```

2. Wait a few minutes and retry

3. Consider using multiple regions with fallback logic

### Deployment Stuck on "Waiting for Health Checks"

**Symptoms:**
- Deployment runs for 10+ minutes
- Stuck on health check phase

**Solutions:**
1. Cancel deployment and check logs:
   ```bash
   flyctl logs --app fxplanner-pr-{NUMBER}
   ```

2. SSH into machine to debug:
   ```bash
   flyctl ssh console --app fxplanner-pr-{NUMBER}
   ps aux | grep java
   curl localhost:8080
   ```

3. Increase timeout in workflow:
   ```yaml
   timeout-minutes: 20  # Increase from 15
   ```

## JPro Issues

### "JPro License Required"

**Symptoms:**
```
JPro license required for production use
```

**Cause:** JPro detects production deployment

**Solutions:**
1. This project qualifies for JPro free tier (development/trial)

2. Add JPro configuration to indicate development:
   ```xml
   <configuration>
     <jproEnvironment>DEVELOPMENT</jproEnvironment>
   </configuration>
   ```

3. For production use, purchase JPro license

### JavaFX Features Not Working in Browser

**Symptoms:**
- Certain UI elements don't render
- Features work locally but not in preview

**Cause:** JPro has some JavaFX limitations

**Solutions:**
1. Check [JPro Compatibility Matrix](https://www.jpro.one/docs/current/2.6/COMPATIBILITY)

2. Common incompatible features:
   - Native file dialogs (use JPro FileManager API)
   - Some 3D rendering features
   - Hardware-accelerated media

3. Test feature locally with JPro:
   ```bash
   mvn jpro:run
   ```

4. Implement fallback for unsupported features

### Poor Performance in Browser

**Symptoms:**
- Laggy UI
- Slow rendering
- High latency

**Cause:** Network latency or insufficient server resources

**Solutions:**
1. Increase VM resources in `fly.toml`:
   ```toml
   [[vm]]
     cpus = 2
     memory_mb = 1024
   ```

2. Choose region closer to users:
   ```yaml
   --region lhr \  # London for EU users
   ```

3. Optimize JavaFX application:
   - Reduce UI complexity
   - Minimize frequent updates
   - Use efficient layouts

## Cost and Billing

### Unexpected High Costs

**Symptoms:**
- Fly.io bill higher than expected
- Multiple apps running simultaneously

**Cause:** Orphaned apps not cleaned up

**Solutions:**
1. Check active apps:
   ```bash
   flyctl apps list | grep fxplanner
   ```

2. Destroy orphaned apps:
   ```bash
   flyctl apps destroy fxplanner-pr-{NUMBER} --yes
   ```

3. Run audit script manually:
   ```bash
   ./scripts/cleanup-orphaned-apps.sh
   ```

4. Enable scheduled audit workflow to run daily

5. Set up billing alerts in Fly.io dashboard

### Apps Not Stopping When Idle

**Symptoms:**
- Apps continue consuming resources when not in use

**Cause:** Auto-stop not configured correctly

**Solutions:**
1. Verify auto-stop in `fly.toml`:
   ```toml
   [http_service]
     auto_stop_machines = true
     min_machines_running = 0
   ```

2. Manually stop apps:
   ```bash
   flyctl machine stop --app fxplanner-pr-{NUMBER}
   ```

3. Consider destroying apps after 24 hours of inactivity

## Getting Help

If you've tried these solutions and still have issues:

1. **Check workflow logs**: GitHub Actions → Failed workflow → View logs
2. **Check Fly.io logs**: `flyctl logs --app fxplanner-pr-{NUMBER}`
3. **Search existing issues**: [GitHub Issues](https://github.com/debrief/fxPlanner/issues)
4. **Create new issue** with:
   - Detailed error messages
   - Workflow logs
   - Fly.io logs
   - Steps to reproduce

## Quick Reference Commands

```bash
# View Fly.io logs
flyctl logs --app fxplanner-pr-{NUMBER}

# SSH into running app
flyctl ssh console --app fxplanner-pr-{NUMBER}

# Check app status
flyctl status --app fxplanner-pr-{NUMBER}

# List all apps
flyctl apps list

# Destroy an app
flyctl apps destroy fxplanner-pr-{NUMBER} --yes

# Test Docker build locally
docker build -t fxplanner-test .
docker run -p 8080:8080 fxplanner-test

# Test JPro locally
mvn jpro:run

# Check GitHub Actions logs
# Go to: https://github.com/{owner}/{repo}/actions
```
