# Implementation Plan: Web-Based Delivery Option

**Branch**: `003-web-delivery` | **Date**: 2025-11-05 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/003-web-delivery/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Refactor the existing JavaFX USV mission planner into a web-based delivery option alongside the desktop app. The solution involves a three-phase approach: (1) refactoring behaviors to be stateless pure functions and validating the desktop app still works, (2) creating a stateless REST backend in Java with embedded static frontend resources, and (3) building a TypeScript/React frontend that drives simulation via REST calls. Deployment targets Heroku with a single executable JAR containing both frontend and backend.

## Technical Context

**Language/Version**: Java 25 (backend), TypeScript 5.x (frontend), React 19 (UI framework)
**Primary Dependencies**: Spring Boot (REST API), React 19 (UI), Leaflet 1.9+ (maps), JTS Topology Suite 1.18.1 (geometry)
**Storage**: No persistent storage - client-side file management only (upload/download GeoJSON)
**Testing**: JUnit 5 (backend), Jest + React Testing Library (frontend), TestFX (desktop validation)
**Target Platform**: Heroku (single dyno), modern browsers (Chrome, Firefox, Safari, Edge - last 2 versions)
**Project Type**: Web application with embedded frontend in JAR
**Performance Goals**: <50ms POST /simulation/tick latency, 60 FPS map rendering, 20+ FPS simulation, 3s maximum startup
**Constraints**: Stateless backend (no session memory), Heroku ephemeral filesystem, single JAR deployment
**Scale/Scope**: 10 concurrent simulation sessions, missions up to 100 waypoints, 4 behavior types

## Constitution Compliance Plan

*Assessment of planned implementation against constitution principles. Re-validate after implementation.*

### Principle Compliance Assessment

✅ **I. Test-First Development for Business Logic**
- Phase 1 requires full test suite validation after behavior refactoring
- Backend REST endpoints will have unit tests
- Refactored behaviors tested as pure functions

✅ **II. Mockup-First UI Development**
- React component layouts need ASCII mockup approval
- Map interaction patterns require visual design approval
- Behavior configuration dialogs need mockup review

✅ **III. Model-View-Controller Architecture**
- Backend (Model) separated from React frontend (View)
- REST API forms Controller layer
- Frontend drives simulation loop (separation of concerns)

✅ **IV. Behavior-Driven Design**
- Behaviors refactored to stateless pure functions maintain interface
- CompositeBehaviour pattern preserved
- State passed via parameters rather than stored internally

✅ **V. Incremental Development**
- Phase 1: Behavior refactoring (Core Foundation)
- Phase 2: Backend API (MVP Features)
- Phase 3: React Frontend (Complete Functionality)
- Clear phase gates with validation criteria

✅ **VI. Visual Feedback & User Experience**
- 60 FPS target for map rendering
- <50ms simulation tick latency
- Real-time state updates via frontend-driven loop

✅ **VII. Testable Business Logic Isolation**
- Behaviors become pure functions (state in/out)
- Backend stateless (no session dependencies)
- >80% test coverage maintained for refactored behaviors

✅ **VIII. End-to-End Testing Coverage**
- Cross-version compatibility tests (desktop vs web)
- Complete mission planning workflow tests
- Simulation execution validation

**GATE STATUS**: ✅ PASSED - All principles satisfied

## Project Structure

### Documentation (this feature)

```text
specs/003-web-delivery/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   ├── api.openapi.yaml # REST API specification
│   └── types.ts         # TypeScript type definitions (review checkpoint)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
# Phase 1: Refactored behaviors (within existing structure)
src/main/java/com/planetmayo/usvsim/
├── model/
│   └── behaviour/       # Refactored to stateless pure functions
│       ├── ParallelTrackSearch.java
│       ├── ExpandingSquareSearch.java
│       ├── WaypointTransit.java
│       └── ReturnToBase.java
└── controller/
    └── SimulationEngine.java  # Updated to pass state to behaviors

# Phase 2: Backend REST API
backend/
├── src/main/
│   ├── java/com/planetmayo/usvsim/api/
│   │   ├── USVWebApplication.java     # Spring Boot main
│   │   ├── controllers/
│   │   │   ├── SimulationController.java  # POST /simulation/tick
│   │   │   ├── BehaviorController.java    # Pattern generation
│   │   │   └── MissionController.java     # Serialization
│   │   ├── dto/                       # Data Transfer Objects for API
│   │   │   ├── SimulationTickRequest.java
│   │   │   ├── SimulationTickResponse.java
│   │   │   ├── PlatformState.java
│   │   │   └── BehaviorState.java
│   │   └── services/
│   │       └── SimulationService.java # Stateless computation
│   └── resources/
│       ├── static/                    # Embedded React build
│       └── application.properties
└── pom.xml                            # Spring Boot + embedded frontend

# Phase 3: Frontend React Application
frontend/
├── src/
│   ├── components/
│   │   ├── MapPanel.tsx              # Leaflet integration
│   │   ├── MissionPlanPanel.tsx
│   │   ├── ControlPanel.tsx
│   │   ├── StatePanel.tsx
│   │   └── behaviors/
│   │       ├── BehaviorSelector.tsx
│   │       └── ConfigDialogs.tsx
│   ├── services/
│   │   ├── SimulationService.ts      # REST client, drives tick loop
│   │   └── MissionService.ts         # File upload/download
│   ├── types/                        # TypeScript definitions (checkpoint)
│   │   ├── Mission.ts
│   │   ├── Platform.ts
│   │   └── Behavior.ts
│   └── App.tsx
├── package.json
└── tsconfig.json

# Deployment configuration
Procfile                               # Heroku: web: java -jar backend/target/usv-web.jar
```

**Structure Decision**: Three-phase incremental structure. Phase 1 modifies existing Java codebase for stateless behaviors. Phase 2 adds backend/ directory with Spring Boot REST API. Phase 3 adds frontend/ directory with React application. Final deployment uses single JAR with embedded frontend.

## Complexity Tracking

> No constitution violations requiring justification