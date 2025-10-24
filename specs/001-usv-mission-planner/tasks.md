# Tasks: USV Mission Planning & Simulation System

**Input**: Design documents from `/specs/001-usv-mission-planner/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: TDD required for business logic only (per Constitution Principle I). UI follows mockup-first workflow (Principle II).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/`, `tests/` at repository root
- Paths shown below use Maven standard layout: `src/main/java/`, `src/test/java/`

---

## Phase 1: Setup (Project Infrastructure)

**Purpose**: Initialize Maven project with JavaFX, dependencies, and directory structure per plan.md

- [x] T001 Create Maven project structure with groupId `com.planetmayo` and artifactId `usv-mission-planner`
- [x] T002 Configure pom.xml with Java 25, JavaFX 21, JTS Topology Suite, JUnit 5, TestFX dependencies
- [x] T003 [P] Create package structure: src/main/java/com/planetmayo/usvsim/{model,controller,view,util}/
- [x] T004 [P] Create test package structure: src/test/java/com/planetmayo/usvsim/{unit,integration,e2e}/
- [x] T005 [P] Create resources directory: src/main/resources/{map-tiles,css}/
- [x] T006 [P] Configure Maven plugins: JavaFX plugin, Surefire (tests), Exec (run)
- [x] T007 Verify build with `mvn clean compile` (should succeed with no source files yet)
- [x] T008 Create Main.java in src/main/java/com/planetmayo/usvsim/ extending Application, with start() method that creates empty Stage and calls show(). Verify `mvn javafx:run` launches a blank window without errors.

**Completion Criteria**: `mvn clean install` succeeds, project structure matches plan.md

---

## Phase 2: Foundational (Core Business Logic - Blocking for All Stories)

**Purpose**: Implement framework-independent utilities and core entities needed by multiple user stories. TDD required per Constitution Principle VII.

### Geometry & Navigation Utilities (Pure Functions)

- [x] T009 [P] Write unit tests for GeoUtils.distance() in src/test/java/com/planetmayo/usvsim/unit/util/GeoUtilsTest.java
- [x] T010 [P] Implement GeoUtils.distance() using Haversine formula in src/main/java/com/planetmayo/usvsim/util/GeoUtils.java
- [x] T011 [P] Write unit tests for GeoUtils.bearing() in GeoUtilsTest.java
- [x] T012 [P] Implement GeoUtils.bearing() in GeoUtils.java
- [x] T013 [P] Write unit tests for GeoUtils.destination() in GeoUtilsTest.java
- [x] T014 [P] Implement GeoUtils.destination() in GeoUtils.java

### Core Geometry Entities

- [x] T015 [P] Create Position class in src/main/java/com/planetmayo/usvsim/model/geometry/Position.java (immutable, lat/lon validation)
- [x] T016 [P] Create Waypoint class in src/main/java/com/planetmayo/usvsim/model/geometry/Waypoint.java
- [x] T017 [P] Write unit tests for Polygon validation in src/test/java/com/planetmayo/usvsim/unit/util/PolygonUtilsTest.java
- [x] T018 [P] Create Polygon class in src/main/java/com/planetmayo/usvsim/model/geometry/Polygon.java with JTS integration

### Platform Model (Shared)

- [x] T019 [P] Create PlatformCapabilities class in src/main/java/com/planetmayo/usvsim/model/platform/PlatformCapabilities.java
- [x] T020 [P] Create PlatformState class with JavaFX properties in src/main/java/com/planetmayo/usvsim/model/platform/PlatformState.java
- [x] T021 [P] Create PlatformDemand class in src/main/java/com/planetmayo/usvsim/model/platform/PlatformDemand.java
- [x] T022 Create Platform class in src/main/java/com/planetmayo/usvsim/model/platform/Platform.java (manages state, track history)

### Behaviour Interface & Enums

- [x] T023 [P] Create BehaviourState enum in src/main/java/com/planetmayo/usvsim/model/behaviour/BehaviourState.java
- [x] T024 [P] Create MissionState enum in src/main/java/com/planetmayo/usvsim/model/mission/MissionState.java
- [x] T025 [P] Create WaypointType enum in src/main/java/com/planetmayo/usvsim/model/geometry/WaypointType.java
- [x] T026 [P] Create TurnDirection enum in src/main/java/com/planetmayo/usvsim/model/platform/TurnDirection.java
- [x] T027 Create Behaviour interface in src/main/java/com/planetmayo/usvsim/model/behaviour/Behaviour.java per contracts/

**Completion Criteria**: All util/ tests pass with >80% coverage, core entities buildable

---

## Phase 3: User Story 1 - Plan Simple Search Mission (P1 - MVP)

**Goal**: Enable users to create parallel track search missions by drawing polygons and specifying parameters.

**Independent Test**: Draw polygon, enter parameters (045°, 100m), verify generated track pattern on map.

### Tests First (TDD for Business Logic)

- [x] T028 [US1] Write unit tests for SearchPatternGenerator.generateParallelTracks() in src/test/java/com/planetmayo/usvsim/unit/util/SearchPatternGeneratorTest.java (test square area, L-shape, alternating directions)

### Business Logic Implementation

- [x] T029 [US1] Implement SearchPatternGenerator.generateParallelTracks() in src/main/java/com/planetmayo/usvsim/util/SearchPatternGenerator.java (transform, clip, alternate)
- [x] T030 [US1] Create ParallelTrackSearch behaviour class in src/main/java/com/planetmayo/usvsim/model/behaviour/ParallelTrackSearch.java implementing Behaviour interface
- [x] T031 [US1] Write unit tests for ParallelTrackSearch waypoint generation in src/test/java/com/planetmayo/usvsim/unit/model/behaviour/ParallelTrackSearchTest.java
- [x] T032 [US1] Create CompositeBehaviour class in src/main/java/com/planetmayo/usvsim/model/mission/CompositeBehaviour.java (implements Behaviour, manages sequence)
- [x] T033 [US1] Create Mission class in src/main/java/com/planetmayo/usvsim/model/mission/Mission.java

### UI Mockup & Approval

- [x] T034 [US1] Create ASCII mockup for MainView layout (map center, Mission Plan panel right, Control panel top-right) - submit to Doc for approval
- [x] T035 [US1] Create ASCII mockup for Parallel Track Search dialog (orientation field, spacing field, OK/Cancel buttons) - submit to Doc for approval

### UI Implementation (After Mockup Approval)

- [x] T036 [US1] Create MapPanel in src/main/java/com/planetmayo/usvsim/view/MapPanel.java with java_leaflet MapView integration, offline tile provider, and POI marker for Portland Harbour. Include pan/zoom controls. Drawing overlay comes in T037.
- [ ] T037 [US1] Implement DrawingController in src/main/java/com/planetmayo/usvsim/controller/DrawingController.java (polygon drawing, click capture)
- [x] T038 [US1] Create MissionPlanPanel in src/main/java/com/planetmayo/usvsim/view/MissionPlanPanel.java (list behaviours, show status)
- [x] T039 [US1] Create ParallelTrackSearchDialog in src/main/java/com/planetmayo/usvsim/view/dialogs/ParallelTrackSearchDialog.java
- [x] T040 [US1] Create MainView in src/main/java/com/planetmayo/usvsim/view/MainView.java (BorderPane layout per approved mockup)
- [ ] T041 [US1] Implement MissionController.addParallelTrackSearch() in src/main/java/com/planetmayo/usvsim/controller/MissionController.java
- [x] T042 [US1] Update Main.java to launch MainView with map centered on Portland Harbour

### E2E Test (Complete Workflow)

- [x] T043 [US1] Write E2E test for mission planning workflow in src/test/java/com/planetmayo/usvsim/e2e/MissionPlanningWorkflowTest.java (TestFX: click, draw, verify)

**US1 Completion Criteria**: Can draw polygon, enter params, see generated tracks on map, behaviour listed in Mission Plan panel
**Current Status**: UI framework complete (15 of 16 core UI tasks). Remaining: DrawingController (polygon drawing) and MissionController wiring

---

## Phase 4: User Story 2 - Execute Mission Simulation (P1 - MVP)

**Goal**: Simulate USV executing planned mission with realistic dynamics (turns, speed, track history).

**Independent Test**: Start simulation, observe USV move with smooth curves, pause/resume, stop to reset.

### Tests First (TDD for Simulation Engine)

- [ ] T044 [P] [US2] Write unit tests for platform dynamics (turn radius, acceleration) in src/test/java/com/planetmayo/usvsim/unit/controller/DynamicsTest.java

### Business Logic Implementation

- [ ] T045 [US2] Implement SimulationEngine in src/main/java/com/planetmayo/usvsim/controller/SimulationEngine.java per contracts/ (ScheduledExecutorService, time-stepped loop)
- [ ] T046 [US2] Implement applyDynamics() method in SimulationEngine (turn radius, acceleration limits, great circle position update)
- [ ] T047 [US2] Create BehaviourExecutor in src/main/java/com/planetmayo/usvsim/controller/BehaviourExecutor.java (getDemandedState, updateProgress logic)

### UI Mockup & Approval

- [ ] T048 [US2] Create ASCII mockup for ControlPanel (Start/Pause/Stop buttons, time acceleration slider) - submit to Doc for approval
- [ ] T049 [US2] Create ASCII mockup for StatePanel (platform ID, position, heading, speed, current behaviour) - submit to Doc for approval

### UI Implementation (After Mockup Approval)

- [ ] T050 [US2] Create ControlPanel in src/main/java/com/planetmayo/usvsim/view/ControlPanel.java per approved mockup
- [ ] T051 [US2] Create StatePanel in src/main/java/com/planetmayo/usvsim/view/StatePanel.java with JavaFX property bindings
- [ ] T052 [US2] Update MapPanel to render USV icon (oriented marker) and track history (polyline)
- [ ] T053 [US2] Wire ControlPanel buttons to SimulationEngine.start/pause/stop() in MissionController
- [ ] T054 [US2] Wire time acceleration slider to SimulationEngine.setTimeAcceleration()
- [ ] T055 [US2] Implement Platform.runLater() calls in SimulationEngine for UI updates

### Integration Test

- [ ] T056 [US2] Write integration test for behaviour sequencing in src/test/java/com/planetmayo/usvsim/integration/SimulationEngineTest.java (start, wait for completion, verify state)

### E2E Test (Complete Workflow)

- [ ] T057 [US2] Write E2E test for simulation execution in src/test/java/com/planetmayo/usvsim/e2e/SimulationExecutionTest.java (TestFX: create mission, start, pause, stop)

**US2 Completion Criteria**: Can start simulation, USV moves with smooth curves, pause/resume works, stop resets, track history visible

---

## Phase 5: User Story 3 - Create Multi-Behaviour Missions (P2)

**Goal**: Sequence multiple behaviours (WaypointTransit, ReturnToBase) for complete missions. Reorder, delete behaviours.

**Independent Test**: Add multiple behaviours, reorder, run simulation, verify sequential execution.

### Tests First (TDD for New Behaviours)

- [ ] T058 [P] [US3] Write unit tests for WaypointTransit in src/test/java/com/planetmayo/usvsim/unit/model/behaviour/WaypointTransitTest.java
- [ ] T059 [P] [US3] Write unit tests for ReturnToBase in src/test/java/com/planetmayo/usvsim/unit/model/behaviour/ReturnToBaseTest.java

### Business Logic Implementation

- [ ] T060 [P] [US3] Implement WaypointTransit behaviour in src/main/java/com/planetmayo/usvsim/model/behaviour/WaypointTransit.java
- [ ] T061 [P] [US3] Implement ReturnToBase behaviour in src/main/java/com/planetmayo/usvsim/model/behaviour/ReturnToBase.java
- [ ] T062 [US3] Implement CompositeBehaviour.reorderBehaviour() in CompositeBehaviour.java
- [ ] T063 [US3] Implement CompositeBehaviour.removeBehaviour() in CompositeBehaviour.java

### UI Mockup & Approval

- [ ] T064 [US3] Create ASCII mockup for Waypoint Transit mode (click-to-place, "Done" button) - submit to Doc for approval
- [ ] T065 [US3] Create ASCII mockup for Return to Base dialog (use current position vs. specify coordinates) - submit to Doc for approval

### UI Implementation (After Mockup Approval)

- [ ] T066 [US3] Create WaypointTransitDialog in src/main/java/com/planetmayo/usvsim/view/dialogs/WaypointTransitDialog.java
- [ ] T067 [US3] Create ReturnToBaseDialog in src/main/java/com/planetmayo/usvsim/view/dialogs/ReturnToBaseDialog.java
- [ ] T068 [US3] Add "Add Behaviour" dropdown to MissionPlanPanel (Parallel Track, Waypoint Transit, Return to Base options)
- [ ] T069 [US3] Implement reorder buttons (up/down) in MissionPlanPanel
- [ ] T070 [US3] Implement delete button in MissionPlanPanel
- [ ] T071 [US3] Update DrawingController to support waypoint placement mode

### E2E Test (Complete Workflow)

- [ ] T072 [US3] Write E2E test for multi-behaviour mission in src/test/java/com/planetmayo/usvsim/e2e/MultiBehaviourMissionTest.java (add 3 behaviours, reorder, simulate, verify sequential execution)

**US3 Completion Criteria**: Can add/reorder/delete behaviours, simulation executes them sequentially with status updates

---

## Phase 6: User Story 4 - Detailed Area Investigation with Expanding Square (P2)

**Goal**: Generate expanding square search patterns from polygon centroid for contact investigation.

**Independent Test**: Draw area, specify initial direction and leg increment, verify square spiral pattern.

### Tests First (TDD for Pattern Generation)

- [ ] T073 [P] [US4] Write unit tests for SearchPatternGenerator.generateExpandingSquare() in SearchPatternGeneratorTest.java (test centroid calculation, leg lengths, 90° turns)

### Business Logic Implementation

- [ ] T074 [US4] Implement SearchPatternGenerator.generateExpandingSquare() in SearchPatternGenerator.java (centroid, expanding spiral, clip to polygon)
- [ ] T075 [US4] Create ExpandingSquareSearch behaviour in src/main/java/com/planetmayo/usvsim/model/behaviour/ExpandingSquareSearch.java
- [ ] T076 [US4] Write unit tests for ExpandingSquareSearch waypoint generation in src/test/java/com/planetmayo/usvsim/unit/model/behaviour/ExpandingSquareSearchTest.java

### UI Mockup & Approval

- [ ] T077 [US4] Create ASCII mockup for Expanding Square Search dialog (initial direction field, leg increment field) - submit to Doc for approval

### UI Implementation (After Mockup Approval)

- [ ] T078 [US4] Create ExpandingSquareSearchDialog in src/main/java/com/planetmayo/usvsim/view/dialogs/ExpandingSquareSearchDialog.java
- [ ] T079 [US4] Add "Expanding Square Search" option to "Add Behaviour" dropdown in MissionPlanPanel

**US4 Completion Criteria**: Can create expanding square search, pattern generates from centroid, displays on map

---

## Phase 7: User Story 5 - Monitor Real-Time Platform State (P3)

**Goal**: Display detailed platform state (position, heading, speed, behaviour) updating in real-time.

**Independent Test**: Run simulation, verify StatePanel updates at >=1Hz with accurate values.

### UI Enhancement (No New Mockup - Expand StatePanel)

- [ ] T080 [P] [US5] Add progress indicator to StatePanel (Behaviour N of M display)
- [ ] T081 [P] [US5] Add timestamp display to StatePanel
- [ ] T082 [US5] Implement property bindings from PlatformState to StatePanel labels (auto-update on change)
- [ ] T083 [US5] Add update frequency indicator (display actual Hz) to StatePanel

**US5 Completion Criteria**: StatePanel shows all required fields, updates in real-time (>=1Hz)

---

## Phase 8: User Story 6 - Configure Platform Dynamics (P3)

**Goal**: Allow users to configure turn radius, max speed, acceleration for different USV types.

**Independent Test**: Change turn radius from 200m to 100m, run simulation, verify tighter turns.

### UI Mockup & Approval

- [ ] T084 [US6] Create ASCII mockup for Platform Configuration dialog (turn radius, max speed, acceleration, deceleration fields, Save button) - submit to Doc for approval

### Business Logic Implementation

- [ ] T085 [US6] Add properties file support for PlatformCapabilities persistence in src/main/resources/platform.properties
- [ ] T086 [US6] Implement PlatformCapabilities.save() and load() methods

### UI Implementation (After Mockup Approval)

- [ ] T087 [US6] Create PlatformConfigDialog in src/main/java/com/planetmayo/usvsim/view/dialogs/PlatformConfigDialog.java
- [ ] T088 [US6] Add "Configure Platform" menu item to MainView menu bar
- [ ] T089 [US6] Wire dialog Save button to PlatformCapabilities.save()

**US6 Completion Criteria**: Can modify platform dynamics, settings persist, simulation behaviour changes accordingly

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Visual polish, error handling, validation, performance optimization

### Validation & Error Handling

- [ ] T090 [P] Implement polygon validation in PolygonUtils (min 3 vertices, no self-intersection)
- [ ] T091 [P] Add input validation to all dialogs (numeric ranges, required fields). Validate angles ∈ [0,360), positive distances/speeds. Show validation errors immediately on field change (red border).
- [ ] T092 [P] Implement error dialogs for invalid inputs with descriptive JavaFX Alert messages that state field name and valid range (e.g., "Track spacing must be > 0 metres")
- [ ] T093 [P] Add warning dialogs for edge cases: (1) "Platform may cut outside boundary during turns" if search area < turn radius; (2) "Track spacing is large - only N tracks generated" if spacing > area dimension; (3) "Pattern may terminate early on narrow sides" for elongated polygons in expanding square
- [ ] T094 Disable mission plan editing during simulation (gray out buttons)
- [ ] T095 Disable Start button when mission plan is empty

### Visual Polish

- [ ] T096 [P] Create CSS stylesheet in src/main/resources/css/main.css (Material Design colors)
- [ ] T097 [P] Apply color coding to behaviours (different colors per type) in MapPanel
- [ ] T098 [P] Implement smooth curve rendering for platform paths (Bezier interpolation)
- [ ] T099 [P] Add behaviour labels to map display (text overlays)
- [ ] T100 Add loading indicator during pattern generation

### Performance Optimization

- [ ] T101 [P] Profile pattern generation with JVisualVM, optimize if >500ms. Also profile map pan/zoom responsiveness: target >30 FPS and <50ms input-to-response latency. Verify with frame rate profiler.
- [ ] T102 [P] Implement track history limit (e.g., 1000 points max) to prevent memory growth
- [ ] T103 Implement FPS counter overlay (toggle with F3 key) in MapPanel. Test frame rate at 1× (target 60 FPS) and 20× acceleration (minimum 10 FPS). Verify smooth animation at both speeds.
- [ ] T103a Implement automatic time acceleration capping: if frame rate drops below 10 FPS at current acceleration, automatically cap acceleration at achievable level and show notification

### Documentation

- [ ] T104 [P] Add JavaDoc comments to all public APIs in util/ and model/
- [ ] T105 [P] Create screenshots for README (mission planning, simulation execution)
- [ ] T106 Write README.md with build instructions and feature highlights

**Phase 9 Completion Criteria**: All validation in place, professional visual appearance, >60 FPS at 1× speed, documentation complete

---

## Dependencies & Execution Strategy

### User Story Dependency Graph

```
Phase 1 (Setup)
    ↓
Phase 2 (Foundational) - BLOCKING for all user stories
    ↓
    ├─→ Phase 3 (US1: Plan Simple Search) - P1 MVP ← START HERE
    ├─→ Phase 4 (US2: Execute Simulation) - P1 MVP ← Required for demo
    ↓
    ├─→ Phase 5 (US3: Multi-Behaviour) - P2 (builds on US1 + US2)
    ├─→ Phase 6 (US4: Expanding Square) - P2 (independent of US3)
    ↓
    ├─→ Phase 7 (US5: State Monitoring) - P3 (enhances US2)
    ├─→ Phase 8 (US6: Configure Dynamics) - P3 (independent)
    ↓
Phase 9 (Polish) - After all user stories
```

### MVP Scope (Minimum Viable Product)

**MVP = US1 + US2**
- Tasks T001-T057 (Setup + Foundational + US1 + US2)
- Delivers: Create parallel track search mission, execute simulation with realistic dynamics
- Demonstrates core capability for interview

### Independent Parallel Opportunities

**Phase 2 (Foundational)**: Tasks T009-T027 highly parallelizable (different files, pure functions)

**Phase 3 (US1)**:
- Parallel: T028 (tests), T034-T035 (mockups can be created simultaneously)
- Sequential: Mockup approval → UI implementation

**Phase 4 (US2)**:
- Parallel: T044 (tests), T048-T049 (mockups)
- Sequential: Must complete US1 first (depends on Mission, CompositeBehaviour)

**Phase 5 (US3)**:
- Parallel: T058-T059 (tests for both behaviours), T064-T065 (mockups)
- Independent from US4

**Phase 6 (US4)**:
- Parallel: T073 (tests), T077 (mockup)
- Independent from US3

**Phase 9 (Polish)**:
- Highly parallel: T090-T092, T096-T099, T104-T105 (different concerns)

### Implementation Strategy

1. **Week 1**: Complete MVP (Phases 1-4, Tasks T001-T057)
   - Day 1-2: Setup + Foundational
   - Day 3-4: US1 (parallel track search)
   - Day 5-7: US2 (simulation execution)

2. **Week 2**: Complete P2 stories (Phases 5-6, Tasks T058-T079)
   - Day 1-3: US3 (multi-behaviour)
   - Day 4-5: US4 (expanding square)

3. **Week 3**: Complete P3 stories + Polish (Phases 7-9, Tasks T080-T106)
   - Day 1: US5 (state monitoring)
   - Day 2: US6 (configure dynamics)
   - Day 3-5: Polish, optimization, documentation

---

## Task Summary

**Total Tasks**: 107

**By Phase**:
- Phase 1 (Setup): 8 tasks
- Phase 2 (Foundational): 19 tasks (BLOCKING)
- Phase 3 (US1 - P1 MVP): 16 tasks
- Phase 4 (US2 - P1 MVP): 14 tasks
- Phase 5 (US3 - P2): 15 tasks
- Phase 6 (US4 - P2): 7 tasks
- Phase 7 (US5 - P3): 4 tasks
- Phase 8 (US6 - P3): 6 tasks
- Phase 9 (Polish): 18 tasks

**Parallel Opportunities**: 47 tasks marked [P] (44% can run in parallel within their phase)

**Test Coverage**:
- Unit tests: 12 test-writing tasks (TDD for business logic)
- Integration tests: 1 task
- E2E tests: 4 tasks
- Total test tasks: 17 (16% of tasks - aligns with constitution's >80% coverage goal)

**MVP Path** (US1 + US2): 57 tasks (53% of total)