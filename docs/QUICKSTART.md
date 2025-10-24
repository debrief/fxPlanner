# Developer Quick Start Guide

## For Contributors (Using PR Previews)

### Creating a PR with Preview

1. **Fork and clone the repository:**
   ```bash
   git clone https://github.com/YOUR-USERNAME/fxPlanner.git
   cd fxPlanner
   ```

2. **Create a feature branch:**
   ```bash
   git checkout -b feature/my-awesome-feature
   ```

3. **Make your changes** to the JavaFX application
   ```bash
   # Edit files in src/main/java/com/planetmayo/fxplanner/
   ```

4. **Test locally:**
   ```bash
   mvn clean javafx:run
   ```

5. **Commit and push:**
   ```bash
   git add .
   git commit -m "Add awesome feature"
   git push origin feature/my-awesome-feature
   ```

6. **Open a Pull Request** on GitHub

7. **Wait for preview** (~5-7 minutes)
   - A bot will comment on your PR
   - Click the preview link when ready
   - Your JavaFX app will open in your browser!

8. **Iterate:**
   - Make more changes
   - Push to the same branch
   - Preview automatically updates

### What to Expect

✅ **Automatic deployment** - No manual steps required  
✅ **Browser-based preview** - Works on any device  
✅ **Isolated environment** - Your PR gets its own preview  
✅ **Auto-cleanup** - Preview destroyed when PR closes  

## For Maintainers (Setting Up the System)

### Prerequisites

- [ ] Fly.io account
- [ ] Credit card on file with Fly.io
- [ ] GitHub repository admin access

### One-Time Setup (10 minutes)

1. **Get Fly.io API Token:**
   - Go to [Fly.io Dashboard](https://fly.io/dashboard)
   - Settings → Access Tokens → Create Token
   - Copy the token

2. **Add Token to GitHub:**
   - Repository → Settings → Secrets and variables → Actions
   - New repository secret: `FLY_API_TOKEN`
   - Paste your Fly.io token

3. **Verify Workflows:**
   - Go to Actions tab
   - Check that workflows are enabled

4. **Test with a PR:**
   - Create a test PR
   - Verify preview deploys successfully
   - Close PR and verify cleanup works

5. **Done!** 🎉

See [SETUP.md](SETUP.md) for detailed instructions.

## Local Development

### Running Locally

```bash
# Standard JavaFX execution
mvn clean javafx:run
```

### Testing JPro Locally

```bash
# Run with JPro (browser-based)
mvn jpro:run

# Then open: http://localhost:8080
```

### Testing Docker Build

```bash
# Build Docker image
docker build -t fxplanner-test .

# Run container
docker run -p 8080:8080 fxplanner-test

# Open: http://localhost:8080
```

### Project Structure

```
fxPlanner/
├── .github/workflows/          # GitHub Actions automation
│   ├── pr-preview-deploy.yml   # Deploy previews
│   ├── pr-preview-cleanup.yml  # Cleanup on PR close
│   └── audit-preview-apps.yml  # Daily orphan cleanup
├── docs/                       # Documentation
│   ├── SETUP.md               # Setup instructions
│   ├── ARCHITECTURE.md        # Technical architecture
│   └── TROUBLESHOOTING.md     # Common issues & fixes
├── scripts/                    # Helper scripts
│   └── cleanup-orphaned-apps.sh
├── src/main/java/              # Java source code
│   └── com/planetmayo/fxplanner/
│       └── HelloWorldApp.java  # Main application
├── Dockerfile                  # Container definition
├── fly.toml                    # Fly.io configuration
├── pom.xml                     # Maven configuration
└── README.md                   # Project overview
```

## Common Tasks

### Adding a New Feature

1. Create branch: `git checkout -b feature/feature-name`
2. Modify JavaFX application in `src/main/java/`
3. Test locally: `mvn clean javafx:run`
4. Commit and push
5. Open PR → Preview deploys automatically

### Updating Dependencies

```bash
# Update Maven dependencies
mvn versions:display-dependency-updates

# Update in pom.xml, then test
mvn clean compile
mvn javafx:run
```

### Debugging Preview Issues

```bash
# View Fly.io logs
flyctl logs --app fxplanner-pr-{NUMBER}

# SSH into running preview
flyctl ssh console --app fxplanner-pr-{NUMBER}

# Check app status
flyctl status --app fxplanner-pr-{NUMBER}
```

### Manual Cleanup

```bash
# List all preview apps
flyctl apps list | grep fxplanner

# Destroy specific app
flyctl apps destroy fxplanner-pr-{NUMBER} --yes

# Run audit script
./scripts/cleanup-orphaned-apps.sh
```

## Development Workflow Best Practices

### Before Creating a PR

- [ ] Test locally with `mvn javafx:run`
- [ ] Verify compilation with `mvn clean compile`
- [ ] Check for errors: `mvn verify`
- [ ] Update documentation if needed

### During PR Review

- [ ] Check the preview link in PR comment
- [ ] Test your changes in the browser
- [ ] Verify no console errors (browser DevTools)
- [ ] Test on mobile if applicable

### After PR Merge

- [ ] Verify preview app is destroyed (check Fly.io dashboard)
- [ ] Delete your feature branch
- [ ] Pull latest main: `git checkout main && git pull`

## Tips & Tricks

### Faster Local Development

```bash
# Skip tests for faster builds
mvn compile -DskipTests

# Run in debug mode
mvn javafx:run -X

# Clean build cache
mvn clean
```

### Preview Not Loading?

1. Check GitHub Actions logs for errors
2. Wait 5-7 minutes (builds take time)
3. Try refreshing the preview URL
4. Check [TROUBLESHOOTING.md](TROUBLESHOOTING.md)

### Making Preview-Specific Changes

If you need to test changes only in the preview environment:

```java
// Detect if running in JPro
if (System.getProperty("jpro.running") != null) {
    // JPro-specific code
} else {
    // Desktop JavaFX code
}
```

## Getting Help

- 📖 **Documentation**: Check `docs/` directory
- 🐛 **Issues**: [GitHub Issues](https://github.com/debrief/fxPlanner/issues)
- 💬 **Discussions**: GitHub Discussions tab
- 📧 **Contact**: Maintainers via GitHub

## Useful Links

- [JavaFX Documentation](https://openjfx.io/)
- [JPro Documentation](https://www.jpro.one/docs)
- [Fly.io Documentation](https://fly.io/docs)
- [Maven Documentation](https://maven.apache.org/guides/)
- [GitHub Actions Documentation](https://docs.github.com/actions)

---

**Happy coding!** 🚀 Your changes will be visible in the browser within minutes of opening a PR.
