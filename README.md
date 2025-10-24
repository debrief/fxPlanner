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

## Future Phases

This project follows a phased development approach as outlined in [Issue #1](https://github.com/debrief/fxPlanner/issues/1):

- **Phase 0: Foundation** ✅ (Current) - Basic JavaFX Hello World application with Maven build
- **Phase 1: MVP** - JPro integration for browser-based preview
- **Phase 2: Automation** - GitHub Actions workflow for PR previews
- **Phase 3: Refinement** - Security, performance, and reliability improvements
- **Phase 4: Enhancements** - Advanced features and optimizations

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
