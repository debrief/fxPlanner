# CLAUDE.md

## Project Overview

Hybrid USV (Unmanned Surface Vehicle) mission planner with dual deployment options:
1. **Desktop**: JavaFX application with native UI
2. **Web**: Spring Boot REST API + React 19 frontend

Users draw search patterns on map, configure mission behaviors, execute realistic simulations with platform dynamics.

**Status**: Web delivery implementation (branch: 003-web-delivery)
**Tech Stack**: Java 25, JavaFX 21+, Spring Boot 3.x, React 19, TypeScript 5.x, Leaflet 1.9+, JTS Topology Suite 1.18.1
**Package**: `com.planetmayo.usvsim`
**Architecture**: Unified JAR (76MB) contains both desktop and web apps sharing model layer

## Build & Run

**Build unified JAR** (desktop + web):
```bash
mvn clean package  # Builds frontend, embeds in backend, creates unified JAR
```

**Run desktop**:
```bash
mvn javafx:run  # Development
java -jar target/usv-mission-planner-1.0.0.jar  # Production
```

**Run web**:
```bash
java -jar target/usv-mission-planner-1.0.0.jar  # Opens http://localhost:8080
PORT=5000 java -jar target/usv-mission-planner-1.0.0.jar  # Custom port
```

**Frontend development** (hot reload):
```bash
cd frontend && npm start  # React dev server on :3000, proxies API to :8080
```

**Tests**:
```bash
mvn test                  # All tests (215 passing)
mvn test -Dtest="*Test"   # Unit only
mvn test -Dtest="*E2ETest"  # E2E (requires display)
```

## Architecture

**Dual Paths** (shared model):
- **Desktop**: JavaFX MVC (`view/`, `controller/`) → SimulationEngine (dedicated thread)
- **Web**: React frontend (`frontend/src/`) → Spring Boot REST API (`api/`) → stateless ticks

**Shared Model** (`model/`, `util/`):
- Mission, Behaviour (stateless), Platform, Geometry (Position, Waypoint, Polygon)
- 4 Behaviors: ParallelTrackSearch, ExpandingSquareSearch, WaypointTransit, ReturnToBase
- CompositeBehaviour (sequences), GeoUtils, SearchPatternGenerator, PolygonUtils

## Development Principles

**Testing**: Business logic test-first (>80% coverage util/model). UI mockup-first (ASCII → Doc approval). **CRITICAL**: Never skip tests. All 215 tests must pass.

**Thread Safety**: SimulationEngine on dedicated thread. No blocking on UI thread.

**Performance**: 60 FPS, <100ms UI response, <50ms REST tick, <500ms pattern gen, <3s startup, <512MB memory

**Design Patterns**:
- Stateless Behaviours: State passed as parameter (enables REST stateless ticks)
- Sequential Execution: CompositeBehaviour auto-advances on completion
- Platform Dynamics: Turn radius 200m, accel/decel 0.5/1.0 m/s², max 8 knots

## Key Paths

`src/test/` - unit/, integration/, e2e/ | `src/main/` - view/, controller/, model/, util/, api/
`frontend/src/` - components/, services/, types/ | `specs/003-web-delivery/` - spec, plan, tasks, contracts

## Workflows

**Pattern Generation**: Parallel tracks (orientation, spacing) or Expanding square (direction, leg increment)

**Mission Execution**: Create behaviors → Generate waypoints → Execute (SimulationEngine or POST /api/simulation/tick) → Platform follows with dynamics → CompositeBehaviour advances

**Cross-Version**: GeoJSON missions load in both desktop/web. Shared model ensures identical behavior. No persistent storage.

## Key Files

`pom.xml` - root Maven (builds frontend, creates unified JAR) | `backend/pom.xml` - Spring Boot config
`frontend/package.json` - React + TypeScript types gen | `Procfile` - Heroku deployment
`src/main/resources/application.properties` - PORT binding | `.specify/memory/constitution.md` - 8 principles

## Constraints

Single USV, Portland Harbour (50.6°N, 2.4°W), offline-capable (desktop), no persistent storage (GeoJSON files), no auth (network-level required)

## Status (003-web-delivery)

**Complete** (81/166, 48.8%): ✅ Structure, ✅ Stateless behaviors (215 tests), ✅ REST API, 🔄 Web UI
**Next**: Phase 4 (mission planning UI), Phase 5 (simulation UI), Phase 6 (cross-version validation)
