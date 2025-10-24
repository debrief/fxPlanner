# Quickstart Guide: USV Mission Planning & Simulation System

**Feature**: 001-usv-mission-planner
**Date**: 2025-10-24
**Audience**: Developers setting up the project

## Prerequisites

- **Java**: JDK 25 (newest LTS)
- **Maven**: 3.9+ for build management
- **IDE**: IntelliJ IDEA, VS Code with Java extensions, or Eclipse
- **Git**: For version control

---

## Initial Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd fxPlanner
git checkout 001-usv-mission-planner
```

### 2. Verify Java Version

```bash
java -version
# Should show: java version "25" or higher
```

If Java 25 not installed:
- **macOS**: `brew install openjdk@25`
- **Linux**: Download from [Adoptium](https://adoptium.net/) or use package manager
- **Windows**: Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or Adoptium

### 3. Build Project

```bash
mvn clean install
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX s
```

### 4. Run Application

```bash
mvn javafx:run
```

Or from IDE:
- Run main class: `com.planetmayo.usvsim.Main`

---

## Project Structure

```
fxPlanner/
├── pom.xml                          # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/com/deepbluec/usvsim/
│   │   │   ├── model/               # Business logic
│   │   │   ├── controller/          # Application logic
│   │   │   ├── view/                # JavaFX UI
│   │   │   ├── util/                # Utilities (pure functions)
│   │   │   └── Main.java
│   │   └── resources/
│   │       ├── map-tiles/           # Offline OSM tiles
│   │       ├── css/                 # Styling
│   │       └── application.properties
│   └── test/
│       └── java/com/deepbluec/usvsim/
│           ├── unit/                # Unit tests
│           ├── integration/         # Integration tests
│           └── e2e/                 # End-to-end tests
└── specs/
    └── 001-usv-mission-planner/     # Feature documentation
        ├── spec.md
        ├── plan.md
        ├── research.md
        ├── data-model.md
        └── contracts/
```

---

## Development Workflow

### Running Tests

**All tests**:
```bash
mvn test
```

**Unit tests only**:
```bash
mvn test -Dtest="*Test"
```

**Integration tests**:
```bash
mvn test -Dtest="*IntegrationTest"
```

**E2E tests** (requires display):
```bash
mvn test -Dtest="*E2ETest"
```

**With coverage**:
```bash
mvn jacoco:prepare-agent test jacoco:report
# View: target/site/jacoco/index.html
```

### Debugging

**IntelliJ IDEA**:
1. Set breakpoints in code
2. Right-click `Main.java` → Debug 'Main'

**VS Code**:
1. Install "Debugger for Java" extension
2. F5 to start debugging

**Command Line**:
```bash
mvn javafx:run -Ddebug
# Attach debugger to port 5005
```

### Hot Reload (for development)

JavaFX doesn't support true hot reload, but you can minimize restart time:
- Use FXML for layouts (reload without recompile)
- Keep business logic in separate classes
- Use Maven exec plugin for fast restarts

---

## Testing Guide

### Unit Test Example

```java
package com.planetmayo.usvsim.unit.util;

import com.planetmayo.usvsim.util.GeoUtils;
import com.planetmayo.usvsim.model.geometry.Position;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {

    @Test
    void testDistanceCalculation() {
        Position p1 = Position.of(50.6, -2.4); // Portland
        Position p2 = Position.of(50.7, -2.4); // 10km north

        double distance = GeoUtils.distance(p1, p2);

        assertEquals(11119, distance, 10); // ~11km ± 10m
    }

    @Test
    void testBearingCalculation() {
        Position start = Position.of(50.6, -2.4);
        Position end = Position.of(50.7, -2.4); // Due north

        double bearing = GeoUtils.bearing(start, end);

        assertEquals(0.0, bearing, 1.0); // 0° ± 1° (north)
    }
}
```

### Integration Test Example

```java
package com.planetmayo.usvsim.integration;

import com.planetmayo.usvsim.model.mission.Mission;
import com.planetmayo.usvsim.model.behaviour.ParallelTrackSearch;
import com.planetmayo.usvsim.controller.SimulationEngine;
import org.junit.jupiter.api.Test;

class SimulationEngineTest {

    @Test
    void testBehaviourSequencing() throws InterruptedException {
        // Create mission with multiple behaviours
        Mission mission = new Mission();
        mission.addBehaviour(createTestBehaviour1());
        mission.addBehaviour(createTestBehaviour2());

        // Start simulation
        SimulationEngine engine = new SimulationEngine(mission, 100);
        engine.start();

        // Wait for first behaviour to complete
        Thread.sleep(5000);

        // Verify advancement to second behaviour
        assertEquals(BehaviourState.COMPLETE,
            mission.getMissionPlan().getBehaviours().get(0).getState());
        assertEquals(BehaviourState.EXECUTING,
            mission.getMissionPlan().getCurrentBehaviour().getState());

        engine.stop();
    }
}
```

### E2E Test Example (TestFX)

```java
package com.planetmayo.usvsim.e2e;

import com.planetmayo.usvsim.Main;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import javafx.stage.Stage;

class MissionPlanningWorkflowTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    @Test
    void testCreateParallelTrackSearch() {
        // Click "Add Behaviour" button
        clickOn("#addBehaviourButton");

        // Select "Parallel Track Search"
        clickOn("Parallel Track Search");

        // Draw polygon on map (simulate clicks)
        clickOn("#mapPanel", 100, 100);
        clickOn("#mapPanel", 200, 100);
        clickOn("#mapPanel", 200, 200);
        clickOn("#mapPanel", 100, 200);
        clickOn("#mapPanel", 100, 100); // Close polygon

        // Enter parameters
        clickOn("#orientationField").write("045");
        clickOn("#spacingField").write("100");
        clickOn("#confirmButton");

        // Verify behaviour appears in mission plan
        verifyThat("#missionPlanPanel", containsText("Parallel Track Search"));
        verifyThat("#missionPlanPanel", containsText("045°, 100m"));
    }
}
```

---

## Building & Packaging

### JAR (executable)

```bash
mvn clean package
java -jar target/usv-mission-planner-1.0.0.jar
```

### Native Package (macOS/Windows/Linux)

```bash
mvn javafx:jlink
# Creates runtime image in target/image/
```

For installer:
```bash
jpackage --input target/ \
         --name "USV Mission Planner" \
         --main-jar usv-mission-planner-1.0.0.jar \
         --main-class com.planetmayo.usvsim.Main \
         --type dmg  # or exe (Windows), deb (Linux)
```

---

## Common Issues

### JavaFX Not Found

**Error**: `Error: JavaFX runtime components are missing`

**Solution**: Ensure JavaFX dependencies in pom.xml:
```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.1</version>
</dependency>
```

### Headless Test Failures

**Error**: E2E tests fail with "Display required"

**Solution**: Use Monocle for headless testing:
```xml
<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>openjfx-monocle</artifactId>
    <version>jdk-12.0.1+2</version>
    <scope>test</scope>
</dependency>
```

### Map Tiles Not Loading

**Error**: Map displays blank or error

**Solution**:
1. Verify tiles in `src/main/resources/map-tiles/`
2. Check tile naming: `{z}/{x}/{y}.png`
3. Ensure tiles bundled in JAR (Maven resources plugin)

### Out of Memory

**Error**: `java.lang.OutOfMemoryError: Java heap space`

**Solution**: Increase heap size:
```bash
export MAVEN_OPTS="-Xmx1024m"
mvn javafx:run
```

---

## IDE-Specific Setup

### IntelliJ IDEA

1. **Import Project**: File → Open → Select `pom.xml`
2. **Set JDK**: File → Project Structure → Project SDK → Java 25
3. **Enable Maven Auto-Import**: Settings → Build Tools → Maven → Auto-import
4. **Run Configuration**:
   - Main class: `com.planetmayo.usvsim.Main`
   - VM options: `--module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml`

### VS Code

1. **Install Extensions**:
   - Extension Pack for Java
   - Maven for Java

2. **Configure**:
   - `.vscode/launch.json`:
     ```json
     {
       "type": "java",
       "name": "Launch Main",
       "request": "launch",
       "mainClass": "com.planetmayo.usvsim.Main",
       "projectName": "fxPlanner"
     }
     ```

### Eclipse

1. **Import**: File → Import → Existing Maven Projects
2. **Set JDK**: Window → Preferences → Java → Installed JREs → Add JDK 25
3. **Run**: Right-click `Main.java` → Run As → Java Application

---

## Next Steps

1. **Read Feature Spec**: `specs/001-usv-mission-planner/spec.md`
2. **Review Data Model**: `specs/001-usv-mission-planner/data-model.md`
3. **Study Contracts**: `specs/001-usv-mission-planner/contracts/`
4. **Start Implementation**: Follow tasks in `tasks.md` (generated by `/speckit.tasks`)

---

## Additional Resources

- **JavaFX Documentation**: https://openjfx.io/javadoc/21/
- **JTS Topology Suite**: https://locationtech.github.io/jts/
- **java_leaflet**: https://github.com/makbn/java_leaflet
- **Maven JavaFX Plugin**: https://github.com/openjfx/javafx-maven-plugin

---

## Support

- **Issues**: Create GitHub issue in repository
- **Questions**: Contact development team
- **Documentation**: See `specs/001-usv-mission-planner/` directory