# Research: USV Mission Planning & Simulation System

**Feature**: 001-usv-mission-planner
**Date**: 2025-10-24
**Status**: Complete

## Overview

Research findings for implementing a JavaFX desktop application for USV mission planning and simulation. Focus areas: JavaFX best practices, mapping integration, geometry algorithms, multi-threading patterns, and testing strategies.

---

## 1. JavaFX 21+ Architecture & Best Practices

### Decision: JavaFX 21 with Java 25

**Rationale**:
- JavaFX 21 is the current LTS release, compatible with Java 25
- Modular structure (javafx.controls, javafx.graphics, javafx.fxml)
- Strong property binding system for MVC implementation
- Active community and mature ecosystem

**Best Practices**:
- Use JavaFX Properties (SimpleDoubleProperty, SimpleObjectProperty) for model bindings
- Separate UI updates to JavaFX Application Thread via Platform.runLater()
- Use FXML for complex layouts (optional - can use pure Java)
- CSS styling with modular stylesheets
- Scene Builder for rapid UI prototyping (optional)

**Alternatives Considered**:
- Swing: Outdated, limited modern UI capabilities
- JavaFX 17: Older LTS, missing recent improvements
- Electron/Web: Overkill for desktop-only demo app

**Implementation Notes**:
- Main class extends `Application`, override `start(Stage primaryStage)`
- Use `BorderPane` for main layout (map center, panels on sides)
- `Canvas` overlay on map for drawing operations
- `Timeline` for animation loops

---

## 2. java_leaflet Integration

### Decision: java_leaflet for map display

**Rationale**:
- Java wrapper around Leaflet.js (industry-standard web mapping)
- Offline tile support (critical for demo reliability)
- Marker/polyline overlay capabilities
- Active development, good documentation

**Best Practices**:
- Bundle OpenStreetMap tiles for Portland Harbour area (zoom levels 10-18)
- Store tiles in `src/main/resources/map-tiles/`
- Implement custom `TileProvider` for offline mode
- Use `MapView` embedded in JavaFX `StackPane`

**Offline Tile Strategy**:
- Pre-download tiles for 20km radius around 50.6°N, 2.4°W
- Organize as: `/map-tiles/{z}/{x}/{y}.png`
- Estimated size: ~50MB for demo area
- Fallback: Simple vector rendering if tiles unavailable

**Alternatives Considered**:
- JXMapViewer2: Older library, less active development
- GMapsFX: Requires Google API key, online dependency
- Custom rendering: Too complex for demo timeframe

**Implementation Notes**:
- Create `MapPanel extends StackPane` wrapping `MapView`
- Add drawing layer as transparent `Canvas` overlay
- Convert mouse clicks to lat/lon via map projection APIs
- Use `Marker` and `Polyline` for route visualization

---

## 3. Geometry & Pattern Generation

### Decision: JTS Topology Suite for geometric operations

**Rationale**:
- Industry-standard computational geometry library
- Polygon intersection, clipping, centroid calculation
- Well-tested, robust algorithms
- Used in GIS applications worldwide

**Best Practices**:
- Use `Coordinate` for points, `Polygon` for search areas
- `LineString` for tracks and paths
- Spatial operations: `intersection()`, `contains()`, `buffer()`
- Geometric validation: `isValid()`, `isSimple()`

**Pattern Generation Algorithms**:

#### Parallel Track Search:
1. Transform polygon vertices to local coordinate system aligned with track orientation
2. Calculate bounding box in transformed coordinates
3. Generate parallel lines at spacing intervals
4. Alternate direction for each track (lawn-mower pattern)
5. Clip lines to polygon boundary using JTS intersection
6. Convert waypoints back to lat/lon

#### Expanding Square Search:
1. Calculate polygon centroid using JTS
2. Generate square spiral: leg lengths (n, n, n+δ, n+δ, n+2δ, n+2δ...)
3. 90° right turns between legs
4. Stop when legs extend beyond polygon bounds
5. Clip to boundary

**Alternatives Considered**:
- Custom geometry code: Error-prone, reinventing wheel
- Apache Commons Math: Lacks spatial operations
- GeoTools: Heavier dependency, overkill for this use case

**Implementation Notes**:
- Create `SearchPatternGenerator` utility class
- Pure functions: `generateParallelTracks(Polygon area, double orientation, double spacing) → List<Waypoint>`
- Unit test with known test cases (square area, L-shaped area, etc.)

---

## 4. Great Circle Navigation

### Decision: Custom GeoUtils class with Haversine/Vincenty formulas

**Rationale**:
- Great circle distance needed for realistic navigation
- Bearing calculations for heading determination
- Libraries available but formulas simple enough to implement
- Educational value for interview demonstration

**Algorithms**:
- **Haversine formula**: Distance between two lat/lon points
- **Initial bearing**: Forward azimuth from point A to B
- **Destination point**: Given start, bearing, distance → end point

**Best Practices**:
- Earth radius: 6371 km (mean radius)
- Convert degrees to radians for calculations
- Handle dateline crossing (longitude wrapping)
- Validate inputs: lat ∈ [-90, 90], lon ∈ [-180, 180]

**Alternatives Considered**:
- GeoTools: Too heavy for simple calculations
- Spatial4j: Additional dependency for minimal benefit
- Flat-earth approximation: Inaccurate for demo scale

**Implementation Notes**:
```java
public class GeoUtils {
    public static double distance(Position p1, Position p2); // metres
    public static double bearing(Position from, Position to); // degrees [0, 360)
    public static Position destination(Position start, double bearing, double distance);
}
```

---

## 5. Platform Dynamics & Simulation

### Decision: Time-stepped simulation on dedicated thread

**Rationale**:
- Fixed time step (e.g., 100ms) ensures consistent physics
- Separate thread prevents UI blocking
- Simple Euler integration sufficient for demo
- Deterministic, repeatable results

**Simulation Loop**:
```
Every Δt (simulation time step):
  1. Get current PlatformState
  2. Get PlatformDemand from active Behaviour
  3. Apply turn rate limits based on speed/turn radius
  4. Apply acceleration/deceleration limits
  5. Update heading: θ_new = θ + (v / r) × Δt  (if turning)
  6. Update speed: v_new = v + a × Δt
  7. Update position: move distance (v × Δt) along heading
  8. Notify observers (UI update via Platform.runLater)
```

**Best Practices**:
- Use `ScheduledExecutorService` for fixed-rate execution
- Thread-safe state access (synchronize on Platform object)
- Decouple simulation rate from UI frame rate
- Time acceleration multiplies Δt, not execution frequency

**Alternatives Considered**:
- Variable time step: More complex, unnecessary for demo
- JavaFX AnimationTimer: Couples simulation to UI thread
- Physics engine (JBox2D): Overkill for 2D navigation

**Implementation Notes**:
- `SimulationEngine implements Runnable`
- `ScheduledExecutorService.scheduleAtFixedRate(engine, 0, 100, MILLISECONDS)`
- State updates wrapped in `synchronized(platform) { ... }`
- UI updates via `Platform.runLater(() -> updateDisplay())`

---

## 6. Multi-Threading & Concurrency

### Decision: JavaFX Platform.runLater() + ScheduledExecutorService

**Rationale**:
- JavaFX requires UI updates on Application Thread
- SimulationEngine runs on background thread for performance
- Clean separation of concerns
- Standard Java concurrency primitives

**Threading Model**:
- **Main Thread**: JavaFX Application Thread (UI updates only)
- **Simulation Thread**: Background executor (physics calculations)
- **Communication**: Platform.runLater() for UI updates, synchronized blocks for shared state

**Best Practices**:
- Never block Application Thread with long calculations
- Use JavaFX Properties with `Platform.runLater()` for updates
- Synchronize access to shared mutable state (Platform, Mission)
- Graceful shutdown: `executor.shutdown()` in `Application.stop()`

**Alternatives Considered**:
- Task/Service API: More overhead than needed
- Single-threaded: UI would freeze during simulation
- Actor model: Complexity not justified for two threads

**Implementation Notes**:
```java
// In SimulationEngine.run()
PlatformState newState = computeNextState();
Platform.runLater(() -> {
    platform.setState(newState);
    // UI properties auto-update via bindings
});
```

---

## 7. Testing Strategy

### Decision: JUnit 5 + TestFX + Manual scenarios

**Rationale**:
- JUnit 5 standard for Java unit testing
- TestFX enables JavaFX UI testing
- Manual scenarios validate visual correctness
- Aligns with constitution's testing requirements

**Test Structure**:

#### Unit Tests (>80% coverage target):
- **GeoUtilsTest**: Distance, bearing, destination calculations
- **SearchPatternGeneratorTest**: Parallel tracks, expanding square
- **PolygonUtilsTest**: Intersection, clipping, validation
- **BehaviourTest**: State transitions, waypoint generation
- Pure functions, no UI dependencies

#### Integration Tests:
- **SimulationEngineTest**: Multi-threaded state updates
- **MissionControllerTest**: Behaviour sequencing
- **BehaviourExecutorTest**: Platform demand generation

#### E2E Tests (TestFX):
- **MissionPlanningWorkflowTest**: Create behaviours, verify display
- **SimulationExecutionTest**: Start/pause/stop controls
- **MultiBehaviourMissionTest**: Sequential execution

**Best Practices**:
- Test pure functions first (util/, model/)
- Use `@ParameterizedTest` for multiple test cases
- TestFX: `FxToolkit.setupApplication()`, `clickOn()`, `verifyThat()`
- Mock external dependencies (map tiles, file I/O)

**Alternatives Considered**:
- Mockito: Useful but minimize mocking (prefer real objects)
- AssertJ: Nice fluent assertions but JUnit assertions sufficient
- Selenium: Wrong tool for JavaFX

**Implementation Notes**:
```java
@Test
void testParallelTrackGeneration() {
    Polygon square = createSquare(0, 0, 1000); // 1km square
    List<Waypoint> tracks = SearchPatternGenerator
        .generateParallelTracks(square, 0, 100);

    assertEquals(11, tracks.size()); // 10 tracks + start/end
    // Verify alternating directions, clipping, etc.
}
```

---

## 8. Maven Configuration

### Decision: Maven with standard conventions

**Rationale**:
- Standard Java build tool
- Dependency management for JavaFX, JTS, JUnit
- IDE integration (IntelliJ, Eclipse, VS Code)
- Simple for demo/interview scope

**Key Dependencies** (pom.xml):
```xml
<properties>
    <maven.compiler.source>25</maven.compiler.source>
    <maven.compiler.target>25</maven.compiler.target>
    <javafx.version>21.0.1</javafx.version>
</properties>

<dependencies>
    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>${javafx.version}</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>${javafx.version}</version>
    </dependency>

    <!-- Mapping -->
    <dependency>
        <groupId>com.github.makbn</groupId>
        <artifactId>java_leaflet</artifactId>
        <version>LATEST</version>
    </dependency>

    <!-- Geometry -->
    <dependency>
        <groupId>org.locationtech.jts</groupId>
        <artifactId>jts-core</artifactId>
        <version>1.19.0</version>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.1</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testfx</groupId>
        <artifactId>testfx-junit5</artifactId>
        <version>4.0.18</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**Best Practices**:
- Use properties for version management
- JavaFX Maven plugin for native packaging (optional)
- Surefire plugin for test execution
- Exec plugin for running main class during development

**Alternatives Considered**:
- Gradle: More flexible but Maven simpler for this scope
- Ant: Outdated, no dependency management
- Manual classpath: Error-prone, not maintainable

---

## 9. Performance Optimization Strategies

### Decision: Defer optimization to Phase 4 (Visual Polish)

**Rationale**:
- Premature optimization wastes effort
- Focus on correctness first (Phases 1-3)
- Measure before optimizing
- Demo scope unlikely to hit performance limits

**Known Optimization Points** (if needed):
- **Pattern Generation**: Cache generated waypoints, avoid regeneration
- **Map Rendering**: Level-of-detail for distant tracks (render fewer points)
- **Simulation**: Reduce update frequency if frame rate drops
- **Track History**: Limit trail length (e.g., last 1000 points)

**Measurement Strategy**:
- JVisualVM for CPU/memory profiling
- System.nanoTime() for micro-benchmarks
- Manual FPS counter in UI

**Best Practices**:
- Profile before optimizing
- Optimize hot paths first
- Trade memory for speed (caching)
- Simplify algorithms if possible

---

## 10. Offline Map Tile Management

### Decision: Pre-bundle tiles for Portland Harbour

**Rationale**:
- Demo must work without internet (interview reliability)
- Portland Harbour area is small (~20km radius)
- Tile size manageable (~50MB)
- Standard Slippy Map tile naming convention

**Tile Download Strategy**:
- Use open tools (e.g., `tile-dl`, `QGIS`)
- Download OSM tiles for zoom levels 10-18
- Center: 50.6°N, 2.4°W
- Radius: 20km
- Format: PNG images

**Directory Structure**:
```
src/main/resources/map-tiles/
├── 10/
│   └── {x}/
│       └── {y}.png
├── 11/
...
└── 18/
```

**Custom TileProvider** (java_leaflet):
```java
public class OfflineTileProvider implements TileProvider {
    @Override
    public String getTileUrl(int zoom, int x, int y) {
        return "/map-tiles/" + zoom + "/" + x + "/" + y + ".png";
    }
}
```

**Fallback Strategy**:
- If tiles missing: Display simple vector grid
- Log warning but don't crash
- Allow mission planning without map (coordinate entry)

---

## Summary of Decisions

| Area | Decision | Rationale |
|------|----------|-----------|
| **Language** | Java 25 (LTS) | Latest LTS, modern features |
| **UI Framework** | JavaFX 21 | Cross-platform, property bindings, active community |
| **Mapping** | java_leaflet | Leaflet.js wrapper, offline support |
| **Geometry** | JTS Topology Suite | Industry standard, robust algorithms |
| **Navigation** | Custom GeoUtils | Great circle formulas, educational value |
| **Simulation** | Time-stepped on separate thread | Consistent physics, non-blocking UI |
| **Threading** | ScheduledExecutorService + Platform.runLater | Standard Java concurrency, JavaFX compatibility |
| **Testing** | JUnit 5 + TestFX | Unit/integration/E2E coverage, JavaFX support |
| **Build** | Maven | Standard, simple, good IDE integration |
| **Offline Maps** | Bundled OSM tiles | Demo reliability, no internet dependency |

---

## Next Steps (Phase 1)

1. **data-model.md**: Define entity classes (Mission, Behaviour, Platform, etc.)
2. **contracts/**: Internal API contracts for behaviour interface, controller methods
3. **quickstart.md**: Development setup, build instructions, running tests
4. **agent-context update**: Add technology stack to agent-specific file

All research questions resolved - ready for design phase.