# Architecture Documentation

This document describes the technical architecture of the JavaFX PR Preview System.

## System Overview

The PR Preview System automatically deploys JavaFX applications to web browsers for every pull request, enabling remote review without local setup requirements.

```
┌─────────────────┐
│  Pull Request   │
│     Opened      │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│      GitHub Actions Workflow            │
│  ┌──────────────────────────────────┐  │
│  │ 1. Checkout Code                 │  │
│  │ 2. Build Docker Image            │  │
│  │ 3. Deploy to Fly.io              │  │
│  │ 4. Wait for Health Checks        │  │
│  │ 5. Post PR Comment with URL      │  │
│  └──────────────────────────────────┘  │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│           Fly.io Platform               │
│  ┌──────────────────────────────────┐  │
│  │  Docker Container                │  │
│  │  ┌────────────────────────────┐ │  │
│  │  │  Java 21 + Maven           │ │  │
│  │  │  JavaFX Runtime            │ │  │
│  │  │  JPro Server (Port 8080)   │ │  │
│  │  │  Application Code          │ │  │
│  │  └────────────────────────────┘ │  │
│  └──────────────────────────────────┘  │
│           HTTPS Endpoint                │
│    https://fxplanner-pr-X.fly.dev      │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────┐
│  Web Browser    │
│  (Reviewer)     │
│  - HTML5        │
│  - WebGL        │
│  - WebSocket    │
└─────────────────┘
```

## Component Architecture

### 1. Source Code Repository (GitHub)

**Purpose:** Version control and collaboration platform

**Components:**
- Source code (`src/`)
- Build configuration (`pom.xml`)
- Docker configuration (`Dockerfile`)
- Fly.io configuration (`fly.toml`)
- GitHub Actions workflows (`.github/workflows/`)

**Responsibilities:**
- Store application code
- Trigger automation on PR events
- Manage secrets
- Display PR comments with preview URLs

### 2. GitHub Actions Workflows

#### 2.1 Deploy PR Preview (`pr-preview-deploy.yml`)

**Triggers:**
- `pull_request` → `opened`
- `pull_request` → `synchronize`
- `pull_request` → `reopened`

**Steps:**
1. **Checkout**: Clone repository code
2. **Setup Fly CLI**: Install Fly.io command-line tool
3. **Generate App Name**: Create unique name `fxplanner-pr-{NUMBER}`
4. **Check App Exists**: Verify if app already exists
5. **Create App**: Create Fly.io app if first deployment
6. **Post Initial Comment**: Notify PR about build starting
7. **Deploy**: Build Docker image and deploy to Fly.io
8. **Health Check**: Wait for app to become healthy
9. **Post Success Comment**: Share preview URL
10. **Post Failure Comment**: Report errors if deployment fails

**Environment Variables:**
- `FLY_API_TOKEN`: Authentication for Fly.io API

**Secrets Used:**
- `FLY_API_TOKEN`: From GitHub repository secrets
- `GITHUB_TOKEN`: Automatically provided by GitHub Actions

#### 2.2 Cleanup PR Preview (`pr-preview-cleanup.yml`)

**Triggers:**
- `pull_request` → `closed`

**Steps:**
1. **Setup Fly CLI**: Install Fly.io CLI
2. **Generate App Name**: Determine app to destroy
3. **Check App Exists**: Verify app is running
4. **Destroy App**: Remove Fly.io app and all resources
5. **Post Cleanup Comment**: Confirm cleanup completed

#### 2.3 Audit Preview Apps (`audit-preview-apps.yml`)

**Triggers:**
- `schedule`: Daily at 2 AM UTC (cron: `0 2 * * *`)
- `workflow_dispatch`: Manual trigger

**Steps:**
1. **Checkout**: Get cleanup script
2. **Setup Fly CLI**: Install Fly.io CLI
3. **Run Audit Script**: Execute `scripts/cleanup-orphaned-apps.sh`
4. **Report Results**: Log cleanup actions

**Purpose:** Safety net to cleanup orphaned apps if PR cleanup workflow fails

### 3. Docker Container

#### Build Stage (Multi-stage Build)

**Base Image:** `maven:3.9-eclipse-temurin-21-alpine`

**Build Steps:**
1. Copy `pom.xml`
2. Download Maven dependencies (cached layer)
3. Copy source code
4. Compile and package application
5. Generate JPro artifacts

**Optimizations:**
- Dependency caching via separate `pom.xml` copy
- Skip tests during build (`-DskipTests`)
- Multi-stage build to reduce final image size

#### Runtime Stage

**Base Image:** `eclipse-temurin:21-jre-alpine`

**Installed Packages:**
- `fontconfig`: Font rendering support
- `ttf-dejavu`: Default fonts
- `bash`: Shell for entrypoint script
- `curl`: Health check requests
- `maven`: Required for JPro execution

**Runtime Configuration:**
- **Exposed Port:** 8080 (JPro default)
- **Working Directory:** `/app`
- **Environment Variables:**
  - `JPRO_PORT=8080`
  - `JAVA_OPTS="-Xmx512m"`

**Entrypoint Script:**
```bash
#!/bin/bash
# 1. Optional: Clone and build from GitHub if GITHUB_REPO provided
# 2. Start JPro server: mvn jpro:run
# 3. Keep process running in foreground
```

**Health Check:**
- **Interval:** 30s
- **Timeout:** 10s
- **Start Period:** 60s
- **Retries:** 3
- **Command:** `curl -f http://localhost:8080/`

### 4. Fly.io Platform

**Purpose:** Container hosting and HTTPS termination

**Configuration (`fly.toml`):**

```toml
[build]
  dockerfile = "Dockerfile"

[http_service]
  internal_port = 8080
  force_https = true
  auto_stop_machines = true
  auto_start_machines = true
  min_machines_running = 0
```

**Features:**
- **Automatic HTTPS**: SSL certificate provisioned automatically
- **Auto-scaling**: Machines start on request, stop when idle
- **Health Checks**: Monitor application status
- **Rolling Deploys**: Zero-downtime updates
- **Log Aggregation**: Centralized logging

**VM Configuration:**
- **CPU:** 1 shared core
- **Memory:** 512 MB
- **Region:** `lhr` (London) - configurable
- **High Availability:** Disabled (single instance per PR)

**Networking:**
- **Public URL:** `https://fxplanner-pr-{NUMBER}.fly.dev`
- **Ports:**
  - 80 → 443 (HTTPS redirect)
  - 443 → 8080 (HTTPS to JPro)

### 5. JPro Server

**Purpose:** Render JavaFX application in web browser

**Architecture:**
```
┌────────────────────────────────────────┐
│          JPro Server (JVM)             │
│  ┌──────────────────────────────────┐ │
│  │    JavaFX Application            │ │
│  │    (HelloWorldApp.java)          │ │
│  └───────────┬──────────────────────┘ │
│              │                          │
│  ┌───────────▼──────────────────────┐ │
│  │    JPro Rendering Engine         │ │
│  │    - Scene Graph Processing      │ │
│  │    - WebGL Conversion            │ │
│  │    - Event Handling              │ │
│  └───────────┬──────────────────────┘ │
│              │                          │
│  ┌───────────▼──────────────────────┐ │
│  │    HTTP/WebSocket Server         │ │
│  │    Port: 8080                    │ │
│  └──────────────────────────────────┘ │
└────────────┬───────────────────────────┘
             │
             ▼
    ┌────────────────┐
    │  Web Browser   │
    │  HTML + JS     │
    └────────────────┘
```

**Communication Protocol:**
1. **Initial Request:** Browser requests HTML page from JPro
2. **HTML Delivery:** JPro serves HTML with embedded JavaScript client
3. **WebSocket Setup:** Client establishes WebSocket connection
4. **Scene Sync:** JPro serializes JavaFX scene graph to client
5. **User Events:** Browser sends mouse/keyboard events via WebSocket
6. **Scene Updates:** JPro sends UI updates back to browser

**Rendering:**
- JavaFX controls → HTML5 Canvas + WebGL
- Fonts rendered server-side, images cached client-side
- Event loop synchronized between server and client

### 6. Web Browser (Client)

**Requirements:**
- Modern browser (Chrome, Firefox, Safari, Edge)
- JavaScript enabled
- WebSocket support
- HTML5 Canvas + WebGL

**Client Components:**
- **JPro Client Library** (auto-loaded)
- **WebSocket Connection** (for bidirectional communication)
- **Canvas Renderer** (for UI rendering)
- **Event Handlers** (mouse, keyboard, touch)

## Data Flow

### PR Open Flow

```
1. Developer pushes code → GitHub
2. Developer opens PR → GitHub
3. GitHub triggers workflow → GitHub Actions
4. Workflow checks out code → GitHub Actions
5. Workflow builds Docker image → GitHub Actions + Docker
6. Workflow creates Fly.io app → Fly.io API
7. Workflow deploys container → Fly.io
8. Fly.io starts container → Docker + JPro
9. JPro server starts → Port 8080
10. Health check succeeds → Fly.io
11. Workflow posts PR comment → GitHub API
12. Reviewer clicks URL → Browser → Fly.io → JPro
13. Browser loads JavaFX app → JPro renders UI
```

### User Interaction Flow

```
1. User opens preview URL → Browser
2. Browser requests page → Fly.io HTTPS (443)
3. Fly.io forwards to JPro → Container (8080)
4. JPro serves HTML + JS → Browser
5. JS client connects WebSocket → JPro
6. JPro sends scene graph → Browser
7. User clicks button → Browser event
8. Event sent via WebSocket → JPro
9. JavaFX processes event → Application
10. Scene graph updates → JPro
11. Updates sent to browser → WebSocket
12. Browser renders changes → Canvas
```

### PR Close Flow

```
1. Developer closes/merges PR → GitHub
2. GitHub triggers cleanup workflow → GitHub Actions
3. Workflow identifies app name → GitHub Actions
4. Workflow calls Fly.io API → Destroy app
5. Fly.io stops container → Docker shutdown
6. Fly.io deletes resources → VM destroyed
7. Workflow posts comment → GitHub API
```

## Security Architecture

### Authentication & Authorization

1. **GitHub Actions:**
   - Uses `GITHUB_TOKEN` (automatic, scoped per workflow)
   - `FLY_API_TOKEN` stored as GitHub secret
   - Secrets never exposed in logs

2. **Fly.io:**
   - API token authenticates all operations
   - Token scoped to organization
   - Apps isolated by network namespace

3. **Preview Apps:**
   - Publicly accessible (no auth by default)
   - Consider adding HTTP Basic Auth for sensitive projects
   - HTTPS enforced (Fly.io automatic SSL)

### Network Security

- **Encryption in Transit:** HTTPS/TLS 1.3 for all external connections
- **Firewall:** Fly.io provides DDoS protection
- **Isolation:** Each PR gets separate VM/container
- **Ephemeral:** Apps destroyed after PR close (no persistent data)

### Secret Management

| Secret | Storage | Usage | Scope |
|--------|---------|-------|-------|
| `FLY_API_TOKEN` | GitHub Secrets | Deploy/destroy apps | Repository |
| `GITHUB_TOKEN` | GitHub (automatic) | Post comments, read PRs | Workflow |

**Best Practices:**
- Rotate `FLY_API_TOKEN` quarterly
- Use least-privilege tokens
- Monitor Fly.io audit logs
- Never commit secrets to repository

## Resource Management

### Memory Allocation

```
Total VM: 512 MB
├── JVM Heap: ~300 MB (-Xmx512m with overhead)
├── Native Memory: ~100 MB (JavaFX, JPro)
├── OS + Processes: ~100 MB
└── Buffer: ~12 MB
```

### CPU Allocation

- **Shared CPU:** 1 vCPU (time-sliced)
- **Burstable:** Can use more during idle periods
- **Sufficient for:** UI rendering, event processing, small workloads

### Disk Usage

- **Container Image:** ~500 MB (compressed)
- **Ephemeral Disk:** Default Fly.io allocation
- **No Persistent Storage:** All data lost on destroy

### Network

- **Bandwidth:** 100 GB/month free (Fly.io)
- **Connections:** WebSocket + HTTPS
- **Latency:** Depends on region selection

## Scaling Considerations

### Current Limits

- **Concurrent PRs:** Limited by Fly.io organization resources
- **Users per Preview:** Single user recommended (no session sharing)
- **Preview Lifetime:** Tied to PR (auto-cleanup on close)

### Scaling Up

If higher capacity needed:

1. **More Resources:**
   ```toml
   [[vm]]
     cpus = 2
     memory_mb = 1024
   ```

2. **Multiple Regions:**
   ```yaml
   regions: ["lhr", "iad", "sjc"]
   ```

3. **Horizontal Scaling:**
   - Deploy multiple instances per PR
   - Load balancer distributes traffic
   - State synchronization required

4. **Dedicated Fly.io Organization:**
   - Separate billing
   - Higher resource quotas
   - Better isolation

## Monitoring & Observability

### Available Metrics

1. **GitHub Actions:**
   - Workflow execution time
   - Success/failure rates
   - Step-by-step logs

2. **Fly.io Dashboard:**
   - App status (running/stopped)
   - CPU/memory usage
   - HTTP request rates
   - Error rates

3. **Application Logs:**
   - JPro server logs
   - Maven build logs
   - Health check results

### Logging

```bash
# Real-time logs
flyctl logs --app fxplanner-pr-{NUMBER}

# Historical logs (Fly.io dashboard)
https://fly.io/apps/fxplanner-pr-{NUMBER}/logs
```

### Alerting

- **Billing Alerts:** Set in Fly.io dashboard
- **Failed Deployments:** Visible in PR comments + GitHub Actions
- **Orphaned Apps:** Daily audit workflow

## Cost Model

### Per-PR Cost

```
Deployment:
- Build time: 2-3 minutes (free - GitHub Actions)
- Deploy time: 1-2 minutes (free - Fly.io transfer)

Runtime:
- Active time: ~30 minutes average
- Cost: 512 MB × $0.0000008/sec × 1800 sec = $0.0007
- Rounded: ~$0.001 per PR
```

### Monthly Cost Projections

| Scenario | PRs/Month | Total Cost |
|----------|-----------|------------|
| Light | 20 | $0.02 |
| Medium | 80 | $0.08 |
| Heavy | 200 | $0.20 |

**Notes:**
- Assumes 30-minute average lifetime per PR
- Auto-stop reduces idle costs to $0
- Bandwidth included (first 100 GB free)

## Failure Modes & Recovery

### Deployment Failures

**Failure:** Docker build fails
- **Detection:** GitHub Actions reports failure
- **Recovery:** Check logs, fix issue, push new commit
- **Impact:** No preview available

**Failure:** Fly.io deployment timeout
- **Detection:** Workflow times out after 15 minutes
- **Recovery:** Re-run workflow manually
- **Impact:** Delayed preview

### Runtime Failures

**Failure:** JPro server crashes
- **Detection:** Health checks fail, app stops
- **Recovery:** Fly.io attempts restart, else manual redeploy
- **Impact:** Preview becomes unavailable

**Failure:** Out of memory
- **Detection:** OOMKilled in logs
- **Recovery:** Increase memory in `fly.toml`
- **Impact:** App crashes during use

### Cleanup Failures

**Failure:** Cleanup workflow doesn't run
- **Detection:** App remains after PR closed
- **Recovery:** Daily audit workflow destroys orphans
- **Impact:** Small cost accumulation

## Future Enhancements

### Potential Improvements

1. **Screenshot Automation:**
   - Capture screenshots on deploy
   - Visual regression testing
   - Embedded in PR comments

2. **Session Recording:**
   - Record user interactions
   - Replay for async review
   - Stored in cloud storage

3. **Multi-User Support:**
   - Shared sessions
   - Real-time collaboration
   - Cursor synchronization

4. **Performance Monitoring:**
   - Client-side metrics
   - Latency tracking
   - Render time analysis

5. **Custom Domains:**
   - Vanity URLs per PR
   - Stable preview links
   - Better branding

6. **Staging Environment:**
   - Long-lived staging app
   - Integration testing
   - Pre-production validation

## References

- [JPro Documentation](https://www.jpro.one/docs)
- [Fly.io Documentation](https://fly.io/docs)
- [GitHub Actions Documentation](https://docs.github.com/actions)
- [Docker Multi-stage Builds](https://docs.docker.com/build/building/multi-stage/)
- [JavaFX Documentation](https://openjfx.io/)
