# fxPlanner

Sample Java/JavaFX UXV planning/monitoring tool

## Overview

This project is a JavaFX-based desktop application designed for UXV (Unmanned Vehicle) planning and monitoring. The project is currently in **Phase 0: Foundation**, providing a simple "Hello World" application to validate the build system and development workflow.

## Prerequisites

- **Java 21** or higher (tested with Java 25)
- **Maven 3.6+** (tested with Maven 3.9.9)
- **JavaFX 21+** (included as Maven dependency)

## Quick Start

### Build and Run

To build and run the application with a single command:

```bash
mvn clean javafx:run
```

This command will:
1. Clean any previous build artifacts
2. Compile the Java source code
3. Download necessary dependencies (JavaFX libraries)
4. Launch the JavaFX application window

### Expected Output

When the application starts, you should see a window titled **"fxPlanner - Hello World"** with:
- A "Hello World from fxPlanner!" heading
- A subtitle describing the application
- An interactive "Click Me!" button
- A click counter that updates when you press the button

## Project Structure

```
fxPlanner/
├── pom.xml                           # Maven project configuration
├── src/
│   └── main/
│       ├── java/
│       │   └── com/planetmayo/fxplanner/
│       │       └── HelloWorldApp.java    # Main JavaFX application
│       └── resources/                    # Application resources (CSS, images, etc.)
└── target/                               # Build output directory
```

## Development

### Compile Only

To compile the project without running:

```bash
mvn compile
```

### Clean Build

To remove all build artifacts:

```bash
mvn clean
```

### Package

To create a JAR file:

```bash
mvn package
```

## PR Preview System

This project features an **automated browser-based PR preview system** that deploys your JavaFX application changes to a web browser for easy review, without requiring local setup.

### How It Works

1. 🔄 **Open a Pull Request** - The system automatically deploys your changes
2. ⏱️ **Wait ~5-7 minutes** - Docker builds and deploys to Fly.io
3. 🌐 **Click the preview link** - Access your JavaFX app directly in the browser
4. 🧹 **PR closes** - Preview environment is automatically destroyed

### Features

- ✅ **Zero local setup** - Review PRs from any device with a browser
- ✅ **Automatic deployment** - Triggered on every PR update
- ✅ **Isolated environments** - Each PR gets its own preview instance
- ✅ **Auto-cleanup** - Resources are cleaned up when PRs close
- ✅ **Cost-effective** - Runs only when needed (~$0.03-$0.58/month)

### Setup

For repository maintainers, see [Setup Guide](docs/SETUP.md) for configuration instructions.

### Technology Stack

- **JPro** - Renders JavaFX applications in web browsers (no plugins)
- **Docker** - Containerizes the application for consistent deployment
- **Fly.io** - Provides ephemeral hosting for preview environments
- **GitHub Actions** - Automates deployment and cleanup workflows

## Development Phases

This project follows a phased development approach as outlined in [Issue #1](https://github.com/debrief/fxPlanner/issues/1):

- **Phase 0: Foundation** ✅ - Basic JavaFX Hello World application with Maven build
- **Phase 1: MVP** ✅ - JPro integration and Docker containerization
- **Phase 2: Automation** ✅ - GitHub Actions workflow for PR previews
- **Phase 3: Refinement** ✅ - Security, error handling, and monitoring
- **Phase 4: Enhancements** ⏳ - Advanced features and optimizations

## Documentation

- **[Quick Start Guide](docs/QUICKSTART.md)** - Get started as a contributor or maintainer
- **[Setup Guide](docs/SETUP.md)** - Detailed setup instructions for the PR preview system
- **[Architecture](docs/ARCHITECTURE.md)** - Technical architecture and system design
- **[Troubleshooting](docs/TROUBLESHOOTING.md)** - Common issues and solutions

## Troubleshooting

### Application Window Doesn't Appear

If the application window doesn't appear when running `mvn javafx:run`:
- Ensure you're running in a graphical environment (not SSH/remote terminal)
- Check that JavaFX native libraries are compatible with your OS
- Try running with additional debug output: `mvn javafx:run -X`

### Build Warnings

You may see warnings about Java version compatibility. These are informational and don't affect functionality:
- `WARNING: location of system modules is not set` - Can be safely ignored
- `WARNING: Restricted methods` - Java platform warnings for future versions

## License

See [LICENSE](LICENSE) file for details.

## Contributing

For contribution guidelines and the project roadmap, see the PRD in [Issue #1](https://github.com/debrief/fxPlanner/issues/1).
