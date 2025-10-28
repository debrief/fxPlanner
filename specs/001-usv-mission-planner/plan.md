# Implementation Plan: USV Mission Planning & Simulation System

**Branch**: `001-usv-mission-planner` | **Date**: 2025-10-24 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-usv-mission-planner/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Build a desktop application for planning and simulating USV (Unmanned Surface Vehicle) mine clearance missions. Users draw search patterns on a map, configure mission behaviors (parallel track search, expanding square search, waypoint transit, return to base), and execute realistic simulations with platform dynamics (turn radius, acceleration, speed limits). The system demonstrates autonomous systems understanding through behavior sequencing, pattern generation algorithms, and realistic navigation physics.

## Technical Context

**Language/Version**: Java 25 (newest LTS)
**Primary Dependencies**: JavaFX 21+, java_leaflet (map library), JTS Topology Suite (geometry), Maven (build)
**Storage**: N/A (missions created fresh each session for MVP; optional: properties files for user preferences)
**Testing**: JUnit 5 (unit), TestFX (UI/integration), manual testing scenarios
**Target Platform**: Desktop (macOS, Windows, Linux) - cross-platform JavaFX application
**Project Type**: Single desktop application
**Performance Goals**: 60 FPS animation target, 1Hz simulation minimum, <500ms pattern generation, <3s startup
**Constraints**: <100ms UI response, <512MB memory, offline-capable (bundled map tiles), single USV only
**Scale/Scope**: Demo/interview application - ~50 classes, 4 behavior types, Portland Harbour map area only

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Principle I: Test-First Development for Business Logic
**Status**: ✅ PASS
- Business logic modules (pattern generation, navigation, geometry calculations) will have unit tests written first
- UI components follow mockup-first workflow per Principle II
- Coverage target: >80% for business logic modules

### Principle II: Mockup-First UI Development
**Status**: ✅ PASS
- All UI panels require ASCII mockup approval from Doc before implementation
- Mockups needed: MainView layout, MissionPlanPanel, ControlPanel, StatePanel, behavior dialogs
- No test-first requirement for UI code

### Principle III: Model-View-Controller Architecture
**Status**: ✅ PASS
- Clear separation: model/ (Mission, Behaviour, Platform), view/ (JavaFX panels), controller/ (MissionController, SimulationEngine)
- SimulationEngine runs on dedicated thread (not UI thread)
- JavaFX property bindings connect views to models

### Principle IV: Behavior-Driven Design
**Status**: ✅ PASS
- Common Behaviour interface for all mission activities
- CompositeBehaviour for sequential execution
- Four behavior types: ParallelTrackSearch, ExpandingSquareSearch, WaypointTransit, ReturnToBase

### Principle V: Incremental Development
**Status**: ✅ PASS
- Following phased approach from existing PRD:
  - Phase 1: Core Foundation (Maven, map, basic UI)
  - Phase 2: MVP Features (all 4 behaviors, basic simulation)
  - Phase 3: Complete Functionality (algorithms, dynamics, drawing tools)
  - Phase 4: Visual Polish (60 FPS, animations, styling)
  - Phase 5: Demo Preparation (optimization, screenshots)

### Principle VI: Visual Feedback & User Experience
**Status**: ✅ PASS
- 60 FPS target for animations
- <100ms UI response requirement
- Real-time state display (minimum 1Hz)
- Color coding for behavior types
- Smooth curved paths for platform movement

### Principle VII: Testable Business Logic Isolation
**Status**: ✅ PASS
- Framework-independent modules: GeoUtils, SearchPatternGenerator, PolygonUtils
- Pure functions for calculations (great circle, pattern generation)
- >80% coverage target for util/ and model/ packages

### Principle VIII: End-to-End Testing Coverage
**Status**: ✅ PASS
- E2E tests for complete workflows: mission planning, simulation execution, behavior sequencing
- TestFX for UI integration testing
- Manual test scenarios documented in original PRD

**GATE RESULT**: ✅ ALL CHECKS PASSED - Proceed to Phase 0 research

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/planetmayo/usvsim/
├── model/
│   ├── mission/
│   │   ├── Mission.java
│   │   └── CompositeBehaviour.java
│   ├── behaviour/
│   │   ├── Behaviour.java             # Core interface
│   │   ├── ParallelTrackSearch.java
│   │   ├── ExpandingSquareSearch.java
│   │   ├── WaypointTransit.java
│   │   └── ReturnToBase.java
│   ├── platform/
│   │   ├── Platform.java
│   │   ├── PlatformCapabilities.java
│   │   ├── PlatformState.java
│   │   └── PlatformDemand.java
│   ├── geometry/
│   │   ├── Position.java
│   │   ├── Waypoint.java
│   │   └── Polygon.java
│   └── samples/
│       └── SampleBehaviours.java      # Hard-coded test data
├── controller/
│   ├── MissionController.java
│   ├── SimulationEngine.java          # Separate thread
│   ├── BehaviourExecutor.java
│   └── DrawingController.java
├── view/
│   ├── MainView.java
│   ├── MapPanel.java
│   ├── MissionPlanPanel.java
│   ├── ControlPanel.java
│   ├── StatePanel.java
│   └── DashboardPanel.java
├── util/
│   ├── GeoUtils.java                  # Great circle calculations
│   ├── SearchPatternGenerator.java
│   ├── PolygonUtils.java
│   └── Constants.java
└── Main.java

src/test/java/com/planetmayo/usvsim/
├── unit/
│   ├── util/
│   │   ├── GeoUtilsTest.java
│   │   ├── SearchPatternGeneratorTest.java
│   │   └── PolygonUtilsTest.java
│   ├── model/
│   │   └── behaviour/
│   │       ├── ParallelTrackSearchTest.java
│   │       └── ExpandingSquareSearchTest.java
│   └── controller/
│       └── BehaviourExecutorTest.java
├── integration/
│   ├── SimulationEngineTest.java
│   ├── MissionControllerTest.java
│   └── BehaviourSequencingTest.java
└── e2e/
    ├── MissionPlanningWorkflowTest.java
    ├── SimulationExecutionTest.java
    └── MultiB ehaviourMissionTest.java

src/main/resources/
├── map-tiles/                          # Offline Portland Harbour tiles
├── fxml/                               # Optional: FXML layouts
├── css/                                # Styling
└── application.properties

pom.xml                                 # Maven configuration
```

**Structure Decision**: Single desktop application using Maven standard layout (src/main/java, src/test/java). MVC pattern with clear package separation. Test structure mirrors source with unit/integration/e2e divisions. Resources include bundled map tiles for offline operation.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations - all constitution principles satisfied.
