# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

JavaFX desktop application for USV (Unmanned Surface Vehicle) mission planning & simulation. Users draw search patterns on a map, configure mission behaviors, execute realistic simulations with platform dynamics.

**Status**: Pre-implementation (planning phase)
**Tech Stack**: Java 25, JavaFX 21+, java_leaflet (maps), JTS Topology Suite (geometry), Maven
**Package**: `com.planetmayo.usvsim`

## Build & Run Commands

**Build project**:
```bash
mvn clean install
```

**Run application**:
```bash
mvn javafx:run
```

**Run tests**:
```bash
mvn test                           # All tests
mvn test -Dtest="*Test"            # Unit only
mvn test -Dtest="*IntegrationTest" # Integration only
mvn test -Dtest="*E2ETest"         # E2E (requires display)
```

**Coverage**:
```bash
mvn jacoco:prepare-agent test jacoco:report
# View: target/site/jacoco/index.html
```

**Package**:
```bash
mvn clean package
java -jar target/usv-mission-planner-1.0.0.jar
```

## Architecture

**MVC Pattern** (strict separation):
- **Model** (`model/`): Mission, Behaviour interface, Platform (state/capabilities/demand), geometry (Position, Waypoint, Polygon)
- **View** (`view/`): JavaFX panels (MainView, MapPanel, MissionPlanPanel, ControlPanel, StatePanel)
- **Controller** (`controller/`): MissionController, SimulationEngine (dedicated thread), BehaviourExecutor, DrawingController

**Core Contracts**:
- `Behaviour` interface: All mission activities implement this (ParallelTrackSearch, ExpandingSquareSearch, WaypointTransit, ReturnToBase)
- `CompositeBehaviour`: Sequences child behaviours for multi-phase missions
- `SimulationEngine`: Runs on separate thread, never blocks UI

**Utility Modules** (`util/`): Framework-independent, pure functions
- `GeoUtils`: Great circle calculations (distance, bearing)
- `SearchPatternGenerator`: Pattern algorithms (parallel tracks, expanding square)
- `PolygonUtils`: Geometry operations

## Development Principles

### Testing Strategy (Constitution Principles I, VII, VIII)
1. **Business logic** (algorithms, calculations): Test-first. Write tests, get approval, implement (Red-Green-Refactor). Target >80% coverage for `util/` and `model/`.
2. **UI components**: Mockup-first. Create ASCII mockup, get Doc approval, then implement. No test-first requirement for UI.
3. **E2E workflows**: TestFX for complete scenarios (mission planning → simulation execution)

**CRITICAL**: Never skip tests to achieve project goals. All tests must pass.

### UI Development (Principle II)
All UI requires ASCII mockup approved by Doc before implementation. Mockups needed: MainView layout, MissionPlanPanel, ControlPanel, StatePanel, behavior dialogs.

### Thread Safety (Principle III)
- SimulationEngine runs on dedicated thread
- JavaFX property bindings connect views to models
- No blocking operations on UI thread

### Incremental Phases (Principle V)
- Phase 1: Core Foundation (Maven, map, basic UI)
- Phase 2: MVP Features (4 behaviors, basic simulation)
- Phase 3: Complete Functionality (algorithms, dynamics, drawing tools)
- Phase 4: Visual Polish (60 FPS, animations, styling)
- Phase 5: Demo Preparation (optimization, screenshots)

### Performance Targets (Principle VI)
- 60 FPS animations
- <100ms UI response
- 1Hz simulation minimum
- <500ms pattern generation
- <3s startup
- <512MB memory

## Key Design Patterns

**Behaviour Interface Pattern**: All USV activities implement common interface with:
- State management (PENDING, EXECUTING, COMPLETE)
- Progress tracking
- Waypoint generation
- Demand calculation (heading, speed for platform)

**Sequential Execution**: CompositeBehaviour manages behavior transitions. When behavior completes (all waypoints reached), automatically advances to next.

**Platform Dynamics**: Realistic navigation with:
- Turn radius (default 200m)
- Acceleration/deceleration (0.5/1.0 m/s²)
- Speed limits (max 8 knots)
- Smooth curved paths (no sharp corners)
- Turn arcs calculated from geometry

## Test Structure

```
src/test/java/com/planetmayo/usvsim/
├── unit/              # Pure business logic (GeoUtils, SearchPatternGenerator, behaviors)
├── integration/       # Component interactions (SimulationEngine, MissionController)
└── e2e/              # Complete workflows (planning → execution)
```

**Unit test example locations**:
- Geometry: `GeoUtilsTest`, `PolygonUtilsTest`
- Patterns: `SearchPatternGeneratorTest`
- Behaviors: `ParallelTrackSearchTest`, `ExpandingSquareSearchTest`
- Platform: `BehaviourExecutorTest`

**E2E test examples**:
- `MissionPlanningWorkflowTest`: Draw polygon → configure → verify pattern
- `SimulationExecutionTest`: Start → run → observe movement
- `MultiBehaviourMissionTest`: Sequence 3+ behaviors → verify transitions

## Documentation Structure

```
specs/001-usv-mission-planner/
├── spec.md              # Feature specification with user stories
├── plan.md              # Implementation plan (phases, structure)
├── research.md          # Research findings
├── data-model.md        # Entity definitions & relationships
├── quickstart.md        # Developer setup guide
└── contracts/           # Interface contracts
    ├── behaviour-interface.md
    └── simulation-engine.md
```

## Common Patterns

**Search Pattern Generation**:
- Parallel tracks: Specify orientation (0-360°), spacing (m) → generates alternating tracks
- Expanding square: Specify initial direction, leg increment → spirals from centroid

**Mission Execution Flow**:
1. User creates behaviors (draws areas, specifies parameters)
2. Behaviors generate waypoints
3. SimulationEngine executes on separate thread
4. Platform follows waypoints with dynamics (turns, acceleration)
5. CompositeBehaviour advances through sequence

**Great Circle Navigation**:
- All distance/bearing calculations use spherical geometry (GeoUtils)
- Appropriate for local operations (<100km)
- Position: lat/lon decimal degrees

## Code Quality Requirements

- JavaDoc for all public APIs
- Comments for complex algorithms (pattern generation, turn calculations)
- No business logic in view classes
- No UI blocking in controller classes
- Clean MVC separation (verified in code review)

## Special Files

- `src/main/resources/map-tiles/`: Offline Portland Harbour tiles (bundled for demo)
- `src/main/resources/application.properties`: User preferences
- `pom.xml`: Maven dependencies (JavaFX, JTS, JUnit 5, TestFX)

## Notes

- Single USV only (no multi-platform)
- Portland Harbour area only (50.6°N, 2.4°W)
- Offline-capable (bundled map tiles)
- Interview demo target: 5-10 min scenarios
- Constitution document: `.specify/memory/constitution.md` (defines all 8 core principles)
