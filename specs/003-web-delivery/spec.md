# Feature Specification: Web-Based Delivery Option

**Feature Branch**: `003-web-delivery`
**Created**: 2025-11-05
**Status**: Draft
**Input**: User description: "Produce build that's refactored into separate front-end and backend - add optional web-based delivery alongside existing JavaFX desktop app"

## Clarifications

### Session 2025-11-05

- Q: Heroku packaging strategy - separate processes vs embedded frontend in JAR? → A: Single executable JAR/WAR with embedded static frontend (Spring Boot with static resources), single Procfile web process
- Q: Behavior selection and drawing UX flow - which comes first? → A: User selects behavior type from dropdown first, then for polygon-based behaviors (ParallelTrackSearch, ExpandingSquareSearch) user draws polygon; for point-based behaviors (WaypointTransit) user places waypoints; ReturnToBase prompts for single base location
- Q: Mission persistence strategy on Heroku's ephemeral filesystem? → A: Frontend allows loading mission files via file selector (upload from local filesystem) and downloading mission files (export to local filesystem). The app itself has no persistent storage - missions stored in session memory only during active use
- Q: Real-time simulation update mechanism and backend state management? → A: Stateless REST with frontend-driven simulation loop. Frontend drives simulation timing, calls POST /simulation/tick with current mission state + platform state + time delta, backend computes and returns next state. Behaviors refactored to pure functions (accept state, return state) rather than storing internal state. No WebSocket needed. CRITICAL: Refactor behaviors and verify JavaFX desktop version works BEFORE starting web frontend development
- Q: TypeScript state type design approval process? → A: TypeScript type definitions for state passing (API request/response schemas for POST /simulation/tick, behavior state structures) must be reviewed and approved before Phase 3 (frontend) implementation begins. Types documented as part of planning phase.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Deploy Web Application (Priority: P1)

As a developer or system administrator, I want to deploy the USV mission planner as a web application so that users can access it through a browser without installing desktop software.

**Why this priority**: This is the foundational requirement - the ability to run the application in web mode. Without this, no other web-specific features matter. This represents the core value proposition of the refactoring effort.

**Independent Test**: Can be fully tested by starting the web application, navigating to the URL in a browser, and verifying that the main interface loads with the map visible. Delivers immediate value by providing browser-based access.

**Acceptance Scenarios**:

1. **Given** web application package is built, **When** administrator starts the application, **Then** both frontend and backend services start together and the application is accessible via browser at the configured URL
2. **Given** web application is running, **When** user navigates to the application URL, **Then** the main interface loads showing the map panel and control panels in the same layout as the desktop version
3. **Given** web application is running, **When** backend service becomes unavailable, **Then** frontend detects the loss of connectivity through heartbeat monitoring and displays appropriate error message
4. **Given** web application has been idle, **When** user returns to browser tab, **Then** application reconnects to backend and resumes normal operation

---

### User Story 2 - Plan Mission in Web Browser (Priority: P2)

As a mission planner, I want to create and configure USV missions using a web browser so that I can plan missions from any device with a modern browser.

**Why this priority**: This delivers the primary user functionality - the ability to actually use the application for its intended purpose. Must follow P1 (deployment) but is essential for practical use.

**Independent Test**: Can be tested by opening the web application, selecting a behavior type (e.g., Parallel Track Search) from dropdown, clicking Configure, drawing the required polygon on the map, entering parameters, and verifying the behavior appears in the mission plan list with pattern displayed. Delivers the core mission planning workflow.

**Acceptance Scenarios**:

1. **Given** web application is open, **When** user selects a behavior type from dropdown and clicks Configure, **Then** configuration dialog appears with appropriate parameter fields (orientation, spacing for ParallelTrackSearch; direction, leg increment for ExpandingSquareSearch; etc.)
2. **Given** configuration dialog is open for polygon-based behavior (ParallelTrackSearch or ExpandingSquareSearch), **When** user is prompted to draw search area and clicks on map, **Then** polygon vertices are created and displayed in real-time following the same drawing interaction as desktop version
3. **Given** configuration dialog is open for WaypointTransit behavior, **When** user is prompted to add waypoints and clicks on map, **Then** waypoint markers are placed at clicked locations in sequence
4. **Given** behavior parameters are entered and geometry drawn (if required), **When** user confirms configuration, **Then** behavior is added to mission plan list and search pattern waypoints are displayed on map
5. **Given** mission plan contains behaviors, **When** user reorders, edits, or deletes behaviors, **Then** changes are reflected immediately in both the plan list and map display
6. **Given** mission is planned, **When** user clicks Save/Download, **Then** mission file (GeoJSON format) downloads to user's local filesystem
7. **Given** user has a mission file, **When** user clicks Load and selects file from local filesystem, **Then** mission is loaded into application with all behaviors and configuration restored

---

### User Story 3 - Execute Mission Simulation in Browser (Priority: P3)

As a mission planner, I want to run mission simulations in the web browser so that I can validate mission plans without switching to desktop software.

**Why this priority**: This completes the core feature set by enabling full mission workflow (plan + simulate) in web mode. Dependent on P2 (mission planning) being functional.

**Independent Test**: Can be tested by creating a simple mission with one behavior, clicking Start simulation, and observing the platform marker move along the generated path with state information updating in real-time. Delivers the validation capability that makes the mission planner useful.

**Acceptance Scenarios**:

1. **Given** mission is planned, **When** user clicks Start button, **Then** simulation begins and platform marker moves along mission path with realistic dynamics (turn radius, acceleration)
2. **Given** simulation is running, **When** user adjusts time acceleration slider, **Then** simulation speed changes accordingly and simulation time display updates at the new rate
3. **Given** simulation is executing, **When** user clicks Pause, **Then** simulation halts and can be resumed from the same point
4. **Given** simulation is running, **When** platform state changes (position, heading, speed), **Then** state panel updates in real-time without user refresh
5. **Given** multiple users are accessing the application, **When** one user runs simulation, **Then** other users' missions remain independent (no cross-contamination of simulation state)

---

### User Story 4 - Use Either Deployment Option (Priority: P1)

As a user or organization, I want to choose between desktop and web deployment based on my needs so that I can select the most appropriate delivery method for my environment.

**Why this priority**: This validates the architectural goal - both options coexist without forcing migration. Critical to the "optional alternative" requirement.

**Independent Test**: Can be tested by building both desktop and web packages from the same codebase, running each independently, and verifying identical mission planning functionality in both. Delivers on the promise of flexibility.

**Acceptance Scenarios**:

1. **Given** codebase includes both desktop and web implementations, **When** desktop build is executed, **Then** existing JavaFX application runs unchanged with all original features
2. **Given** both deployment options are available, **When** missions are saved from either version, **Then** mission files use the same format (GeoJSON) and can be loaded in either deployment option
3. **Given** web application is deployed, **When** desktop application is also needed, **Then** both can coexist in same environment without conflicts
4. **Given** organization evaluates deployment options, **When** comparing features between desktop and web, **Then** all mission planning and simulation capabilities are available in both modes

---

### Edge Cases

- **What happens when user opens web application in browser without JavaScript enabled?** Application displays clear error message indicating JavaScript requirement with instructions to enable it.

- **What happens when backend service stops responding during active simulation?** Frontend detects missing heartbeat within 5 seconds, pauses simulation state, displays "Connection lost" warning, and attempts automatic reconnection. When reconnected, user can resume from paused state.

- **What happens when multiple browser tabs connect to same application instance?** Each tab maintains independent session state. Mission planning and simulation in one tab does not affect others (sessions are isolated by browser session ID or similar mechanism).

- **What happens when user attempts to load desktop-specific offline map tiles in web version?** If bundled tiles are unavailable, web version falls back to online tile sources (OpenStreetMap CDN). Application displays notification about tile source difference.

- **What happens when web frontend and backend versions become mismatched (e.g., backend updated but frontend cached)?** Backend API includes version endpoint. Frontend checks version on startup/reconnect and displays prominent warning if mismatch detected, prompting cache clear or page refresh.

- **What happens when user draws very complex polygon (hundreds of vertices) in web interface?** Drawing interaction remains responsive (no lag). If polygon becomes invalid for geometry processing (self-intersections), system provides same validation error as desktop version.

- **What happens when simulation runs for extended period (hours) in web browser?** Simulation continues reliably. Frontend maintains simulation state and drives tick loop. Browser tab remains responsive and can be backgrounded/restored without losing simulation state (state held in frontend memory).

- **What happens when user closes browser tab or window without saving mission?** Mission data is lost (held in frontend memory only). Application displays warning prompt when user attempts to close tab with unsaved changes, giving opportunity to download mission file first.

- **What happens when Heroku dyno restarts while user has active simulation running?** Backend is stateless, so dyno restart has no impact on simulation. Frontend continues driving simulation loop, calling POST /simulation/tick which processes successfully with new dyno instance. User experiences no interruption.

## Requirements *(mandatory)*

### Functional Requirements

#### Architecture & Deployment

- **FR-001**: System MUST provide two independent deployment options: original JavaFX desktop application and new web-based application, both installable and runnable from the same codebase
- **FR-002**: Web deployment package MUST be a single executable JAR/WAR containing both backend service and embedded static frontend resources, startable with single command and compatible with Heroku deployment
- **FR-003**: Web frontend MUST communicate with backend exclusively via REST API (no direct model access)
- **FR-004**: Backend MUST provide health check/heartbeat endpoint that frontend polls to detect connectivity issues
- **FR-005**: Backend MUST expose REST API endpoints for behavior pattern generation (waypoint computation from polygon and parameters)
- **FR-005a**: Backend MUST expose REST endpoint POST /simulation/tick for stateless simulation computation (accept mission state + platform state + time delta, return next platform state)

#### Frontend Implementation

- **FR-006**: Web frontend MUST be implemented using TypeScript and React framework
- **FR-007**: Web frontend MUST integrate Leaflet map library directly (not via WebView) for map rendering and interaction
- **FR-008**: Web frontend MUST replicate the current UI layout and arrangement from the desktop version (map panel 70%, controls 30%, same panel organization)
- **FR-009**: Web frontend MUST support context-specific drawing interactions: click-to-draw polygons for search behaviors (ParallelTrackSearch, ExpandingSquareSearch), click-to-place waypoints for WaypointTransit, single point selection for ReturnToBase, following behavior-first UX flow (select behavior → configure → draw)
- **FR-010**: Web frontend MUST drive simulation execution loop (using requestAnimationFrame or setInterval), calling backend POST /simulation/tick endpoint each frame with current state and rendering returned next state with sub-second latency
- **FR-010a**: Web frontend MUST warn user before closing browser tab/window if mission has unsaved changes, prompting to download mission file

#### Backend Implementation

- **FR-011**: Backend MUST be implemented as Java service extracting model layer from current desktop application
- **FR-012**: Backend service MUST use GeoJSON format for mission serialization (same format as current desktop version for interoperability)
- **FR-013**: Backend MUST preserve all existing business logic for behaviors (ParallelTrackSearch, ExpandingSquareSearch, WaypointTransit, ReturnToBase) refactored as stateless pure functions (accept state parameters, return computed state)
- **FR-014**: Backend MUST preserve existing search pattern generation algorithms (parallel track, expanding square) with identical outputs
- **FR-015**: Backend MUST provide stateless simulation computation endpoint (POST /simulation/tick) accepting current state (mission, platform, time delta) and returning next state (updated platform position, heading, speed, behavior progress) using same dynamics as desktop version (turn radius, acceleration/deceleration, great circle navigation)
- **FR-016**: Backend MUST be fully stateless (no session memory for simulation state), enabling horizontal scalability and surviving dyno restarts
- **FR-016a**: Backend API contracts (request/response schemas for all REST endpoints) MUST be documented with corresponding TypeScript type definitions before frontend implementation begins, enabling type-safe frontend-backend integration

#### Feature Parity

- **FR-017**: Web version MUST support all four behavior types available in desktop version
- **FR-018**: Web version MUST support behavior configuration with all parameters (orientation, spacing, speed, etc.)
- **FR-019**: Web version MUST support mission plan editing (add, delete, reorder, edit behaviors)
- **FR-020**: Web version MUST support mission file operations: download mission as GeoJSON file to user's local filesystem (save) and upload mission file from user's local filesystem (load), with no server-side persistent storage
- **FR-021**: Web version MUST support simulation controls (start, pause, resume, stop, time acceleration)
- **FR-022**: Web version MUST display platform state information (position, heading, speed, track history)
- **FR-023**: Web version MUST render search patterns and platform tracks on map identical to desktop version

#### Data Compatibility

- **FR-024**: Mission files saved from web version MUST be loadable in desktop version without modification
- **FR-025**: Mission files saved from desktop version MUST be loadable in web version without modification
- **FR-026**: Both versions MUST use identical GeoJSON schema for mission storage

#### Non-Functional Requirements

- **FR-027**: Backend API MUST respond to pattern generation requests within 500ms for standard mission sizes (up to 10 behaviors, polygons with up to 100 vertices)
- **FR-028**: Web frontend MUST load and display initial interface within 3 seconds on standard broadband connection
- **FR-029**: Backend POST /simulation/tick endpoint MUST respond within 50ms for standard missions (up to 100 waypoints), enabling smooth 20+ FPS simulation
- **FR-030**: Backend MUST support at least 10 concurrent simulation tick requests per second without response time degradation
- **FR-031**: Web application SHALL NOT implement user authentication/authorization, assuming deployment in trusted network environment where access control is managed at network/infrastructure level

### Key Entities

*The entities remain unchanged from the existing application - this refactoring preserves the domain model:*

- **Mission**: Container for mission metadata (name, created date) and collection of behaviors forming mission plan. Includes overall mission state (Planning, Executing, Paused).

- **Behaviour**: Abstract interface representing mission activities. Subtypes: ParallelTrackSearch, ExpandingSquareSearch, WaypointTransit, ReturnToBase. Each generates waypoints and provides demanded platform state (heading, speed).

- **Platform**: USV representation including current state (position, heading, speed, depth), capabilities (max speed, turn radius, acceleration/deceleration limits), and track history.

- **Position**: Geographic coordinate (latitude, longitude) with great circle calculation methods (distance, bearing to other positions).

- **Waypoint**: Navigation target combining Position, target speed, tolerance radius, and type (Transit, Search, Base).

- **Polygon**: Search area boundary defined by ordered vertices. Used by search behaviors to generate coverage patterns.

- **SimulationState**: Current simulation status including elapsed time, time acceleration factor, and platform dynamics state machine.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: After Phase 1 completion, desktop application test suite (unit, integration, E2E) passes with 100% success rate, proving refactored behaviors work identically to original implementation
- **SC-002**: After Phase 2 completion, TypeScript type definitions for all API contracts are documented, reviewed, and approved before Phase 3 frontend development begins
- **SC-003**: Developer can build and start web application with a single command, resulting in accessible browser-based interface within 10 seconds
- **SC-004**: User can complete identical mission planning workflow (select behavior, configure, draw, add to plan) in web version with same number of clicks/interactions as desktop version
- **SC-005**: Web application can run simulations maintaining 60 FPS map rendering and sub-50ms REST API tick latency for missions up to 100 waypoints
- **SC-006**: Mission files created in either deployment option load successfully in the other option with 100% data fidelity (no data loss or corruption)
- **SC-007**: Web backend is fully stateless - Heroku dyno restart during active simulation has zero impact on user experience (simulation continues uninterrupted)
- **SC-008**: Web backend handles 10 concurrent simulation sessions without response time degradation (sub-50ms POST /simulation/tick latency maintained)
- **SC-009**: Organization can choose deployment option based on needs and switch between them without migration effort or feature compromise
- **SC-010**: Simulation results (platform path, completion time, waypoint sequence) are identical between desktop and web versions for the same mission configuration (validates algorithm preservation across refactored behaviors)
- **SC-011**: Web application detects backend connectivity loss within 5 seconds and presents clear recovery options to user

## Scope *(mandatory)*

### In Scope

**CRITICAL PHASING CONSTRAINT**: Behavior refactoring MUST be completed and desktop application verified working BEFORE web frontend development begins.

1. **Phase 1: Behavior Refactoring (Desktop First)**
   - Refactor all four behavior classes (ParallelTrackSearch, ExpandingSquareSearch, WaypointTransit, ReturnToBase) from stateful to stateless pure functions
   - Behaviors accept state parameters (current progress, platform position, etc.) and return computed state
   - Update desktop SimulationEngine to call refactored behaviors with state parameters
   - Run full test suite (unit, integration, E2E) to verify desktop application still works identically
   - No web development until desktop verification complete

2. **Phase 2: Backend REST API Development**
   - Extract refactored model layer into standalone Java service
   - Expose REST endpoint POST /simulation/tick for stateless simulation computation
   - Expose REST endpoints for behavior pattern generation (waypoint computation)
   - Expose REST endpoints for mission serialization/deserialization (GeoJSON format)
   - Implement health check/heartbeat endpoint
   - Backend fully stateless (no session memory)
   - **Document TypeScript type definitions** for all API contracts (request/response schemas for POST /simulation/tick, behavior state structures, platform state structures)

**CHECKPOINT: TypeScript State Type Review** - TypeScript type definitions must be reviewed and approved before Phase 3 begins. Types should be derived from Phase 1 refactored Java state structures and documented in planning artifacts.

3. **Phase 3: Frontend Web Application Development** (BLOCKED until TypeScript types approved)
   - TypeScript/React application structure
   - Direct Leaflet integration (replace WebView approach)
   - File upload/download functionality (Load mission from local file, Save/Download mission to local file)
   - Behavior selection dropdown with Configure action
   - Context-specific map drawing interactions (polygons for search behaviors, waypoints for transit, single point for return-to-base)
   - Frontend-driven simulation loop (requestAnimationFrame calling POST /simulation/tick)
   - Mission plan panel (list of behaviors with edit/delete/reorder)
   - Control panel (simulation controls, time acceleration)
   - State panel (platform information display)
   - Behavior configuration dialogs (matching desktop versions, including drawing prompts)

4. **Phase 4: Packaging & Deployment**
   - Single executable JAR/WAR build with embedded static frontend resources
   - Maven build process producing deployable artifact
   - Heroku Procfile for single-process deployment
   - Configuration for port binding, connection settings (environment variables)
   - Deployment guide for Heroku and standalone execution

5. **Phase 5: Testing & Validation**
   - Phase 1 validation: Desktop test suite passes after behavior refactoring
   - REST API tests for all endpoints
   - Frontend component tests for UI interactions
   - Integration tests validating frontend-backend communication
   - Cross-version compatibility tests (mission file interchange)
   - Feature parity validation tests (web vs desktop)
   - Performance testing (simulation tick latency, frontend responsiveness)

6. **Documentation**
   - Architectural documentation explaining frontend/backend split and stateless behavior pattern
   - API documentation for REST endpoints with complete request/response schemas
   - **TypeScript type definitions** for all API contracts (generated during Phase 2, reviewed before Phase 3)
   - State structure documentation (platform state, mission state, behavior progress)
   - Deployment guide for web version
   - Updated README explaining both deployment options

### Out of Scope

1. **Desktop Application Changes**
   - No modifications to existing JavaFX desktop application functionality
   - No removal or deprecation of desktop version
   - Desktop build process remains unchanged

2. **New Features**
   - No new behaviors or mission planning capabilities beyond existing four behaviors
   - No additional map tile sources or map features beyond current capability
   - No user authentication/authorization system (trusted network deployment per FR-031)
   - No multi-user collaboration features (concurrent editing of same mission)
   - No mobile-specific UI optimizations (responsive design beyond standard web)

3. **Advanced Web Features**
   - No offline mode for web version (no service workers, local caching of tiles)
   - No Progressive Web App (PWA) features
   - No mobile app wrappers (Cordova, React Native)

4. **Infrastructure**
   - No cloud deployment automation (Docker, Kubernetes)
   - No load balancing or horizontal scaling architecture
   - No database backend (missions stored in session memory only; users manage files locally)
   - No server-side file storage or cloud storage integration (S3, etc.)
   - No user account management or multi-tenancy

5. **Migration**
   - No migration tools or wizards
   - No deprecation plan for desktop version
   - No analytics or usage tracking to compare adoption

### Assumptions

1. **Technology Stack**
   - Existing Java codebase can be refactored to separate JavaFX dependencies from model layer
   - React and TypeScript are acceptable for frontend (no organizational constraints)
   - Leaflet JavaScript library integrates readily with React
   - Target deployment environment is modern browsers (Chrome, Firefox, Safari, Edge - last 2 major versions)

2. **Deployment Context**
   - Web version target platform is Heroku (single dyno deployment)
   - Application packaged as single executable JAR with embedded frontend
   - Port binding configured via PORT environment variable (Heroku standard)
   - Ephemeral filesystem - no persistent file storage beyond session duration
   - Users have stable network connection to backend (not offline/intermittent scenarios)

3. **User Expectations**
   - Users familiar with desktop version will adapt to web version with minimal training
   - Users accept browser-based interaction patterns (no native desktop features required)
   - Users willing to use online map tile sources for web version (if offline tiles unavailable)

4. **Data & State**
   - Mission complexity remains bounded (tens of behaviors, hundreds of waypoints - not thousands)
   - Simulation sessions are short-to-medium duration (minutes to hours, not days)
   - Users manage mission files locally on their filesystem (download/upload model)
   - Session-based in-memory state acceptable (mission lost if browser closed or dyno restarted without user saving file)
   - No server-side mission persistence or database required

5. **Authentication**
   - Trusted network deployment with no authentication required (per FR-031)
   - Backend does not enforce user authentication or authorization
   - Sessions identified by browser session tokens only for isolation, not security
   - Access control managed at network/infrastructure level

6. **Testing Environment**
   - Standard test infrastructure available (build server, browsers for automated testing)
   - TestFX tests for desktop remain valid and continue to pass
   - New web tests follow similar patterns (Jest/React Testing Library for frontend, JUnit for backend API)

## Dependencies

- **Phase 1 Completion (Critical Path)**: Web frontend development depends on successful completion of behavior refactoring in Phase 1. Risk: If behavior refactoring breaks desktop application or requires more extensive changes than anticipated, web development timeline delayed. Mitigation: Complete Phase 1 fully and verify desktop application before starting Phase 2/3.

- **Behavior Refactoring**: Current behaviors store internal state (progress tracking, waypoint sequence). Must be refactored to accept/return state as parameters. Risk: Complex state machines in behaviors may be difficult to externalize. Mitigation: Start with simplest behavior (WaypointTransit), establish pattern, then apply to complex behaviors (search patterns).

- **JavaFX Property Decoupling**: Current model layer uses JavaFX `Property` types for UI binding. Backend must replace these with plain Java types (POJOs). Risk: Behavioral differences if property change notifications affect business logic.

- **Desktop Test Suite Validation**: Phase 1 success depends on existing test suite passing after refactoring. Risk: Tests may need updates if refactoring changes interfaces. Mitigation: Keep interface changes minimal, focus on internal state management changes.

- **Leaflet Library Compatibility**: Web frontend depends on Leaflet library working as expected in React environment. Risk: Version conflicts or React integration issues requiring additional wrapper libraries.

- **GeoJSON Format Stability**: Both versions depend on GeoJSON serialization format remaining stable. Risk: Schema changes breaking cross-version compatibility.

## Constraints

- **Phasing Order (Critical)**: Phase 1 (behavior refactoring + desktop verification) MUST complete before Phase 2/3 (web development) begins. No parallel work allowed. This constrains project timeline but reduces risk of maintaining two incompatible implementations.

- **Type Definition Approval Gate**: TypeScript type definitions for API contracts (documented in Phase 2) MUST be reviewed and approved before Phase 3 (frontend implementation) begins. This constrains development flow but ensures type-safe frontend-backend integration and prevents rework.

- **Stateless Backend Architecture**: Backend must be fully stateless with no session memory for simulation state. All state passed via REST request/response. This constrains simulation architecture but enables Heroku compatibility and horizontal scaling.

- **Feature Parity Requirement**: Web version must match desktop functionality exactly (no feature subset allowed). This constrains implementation timeline and complexity.

- **Backward Compatibility**: Mission file format must remain compatible with existing desktop version. This constrains data model changes.

- **Desktop Application Preservation**: Behavior refactoring must not break existing desktop application. All desktop tests must pass after Phase 1. This constrains refactoring approach (focus on internal state management, preserve interfaces).

- **Technology Choices**: Backend must remain Java (organizational constraint). Frontend must be modern web stack but specific framework (React) is flexible if justified.

- **Single-Server Deployment**: Initial web version must work on single server (no distributed system). This constrains scalability architecture.

- **Heroku Platform**: Web deployment must be compatible with Heroku's single-process dyno model, ephemeral filesystem, and PORT environment variable binding. This constrains packaging (single JAR), file persistence approach (none - client-side only), and configuration management.

- **Browser Compatibility**: Must support modern browsers only (last 2 major versions). This allows use of contemporary web APIs without polyfills for legacy browsers.

## Risks

- **Behavior Refactoring Breaking Desktop**: Risk that refactoring behaviors to stateless pure functions breaks existing desktop application or significantly changes behavior. *Mitigation*: Phase 1 dedicated to refactoring with comprehensive test validation. Refactor incrementally (one behavior at a time). Run full test suite after each behavior. Do not proceed to Phase 2 until all desktop tests pass.

- **Complex State Externalization**: Risk that some behavior state is complex or tightly coupled, making externalization difficult. *Mitigation*: Start with simplest behavior (WaypointTransit) to establish pattern. Review behavior state machines before refactoring to identify challenges. Consider keeping some internal state if necessary and passing serialized behavior state to/from API.

- **REST API Latency Impact on Simulation**: Risk that REST API round-trip latency (even if <100ms) makes simulation feel jerky or unresponsive. *Mitigation*: Establish performance benchmarks early (<50ms tick latency target). Optimize JSON serialization. Use efficient backend processing. Frontend predictive rendering if needed (interpolate between states).

- **Feature Drift**: Risk that maintaining two codebases leads to feature divergence over time. *Mitigation*: Share maximum amount of code (refactored behaviors used by both desktop and web). Document parity requirement. Include cross-version tests in CI. Behaviors become shared library used by both.

- **Testing Gaps**: Risk that web-specific issues (browser differences, network failures) aren't adequately tested. *Mitigation*: Establish comprehensive E2E test suite for web version covering edge cases (connection loss, tab backgrounding, concurrent sessions). Test simulation tick latency under various network conditions.

- **Map Rendering Differences**: Risk that Leaflet JavaScript behaves differently than java_leaflet WebView integration, causing UX inconsistencies. *Mitigation*: Create visual comparison tests. Prototype map interactions early.

- **Network Security**: Risk of unauthorized access if web application deployed outside trusted network environment. *Mitigation*: Document deployment security requirements clearly. Recommend network-level access controls (VPN, firewall rules). Consider adding authentication in future phase if deployment context changes.
