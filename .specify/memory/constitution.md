<!-- Sync Impact Report
Version change: 0.0.0 → 1.2.0 (Initial constitution with clarified testing principles)
Added principles:
  - I. Test-First Development for Business Logic
  - II. Mockup-First UI Development
  - III. Model-View-Controller Architecture
  - IV. Behavior-Driven Design
  - V. Incremental Development
  - VI. Visual Feedback & User Experience
  - VII. Testable Business Logic Isolation
  - VIII. End-to-End Testing Coverage
Added sections:
  - Quality Standards
  - Development Workflow
Templates requiring updates: None (initial constitution)
Follow-up TODOs: None
Clarifications: TDD applies only to business logic; UI requires ASCII mockup approval first
-->

# fxPlanner Constitution

## Core Principles

### I. Test-First Development for Business Logic

All business logic (algorithms, calculations, state machines, data transformations)
MUST begin with tests that define expected behavior. Tests are written first,
reviewed and approved, then implementation follows the Red-Green-Refactor cycle.
This principle applies ONLY to business logic, NOT to UI code.

**Rationale**: Tests serve as living documentation and ensure correctness of
critical algorithms. TDD for business logic prevents regression in core
functionality while allowing UI to evolve separately.

### II. Mockup-First UI Development

All UI components MUST begin with an ASCII mockup approved by the product owner
(Doc) before implementation. The mockup defines layout, controls, and visual
hierarchy. Implementation follows approved design without requiring test-first
approach for UI code itself.

**Rationale**: UI development benefits from visual design approval rather than
test-first methodology. ASCII mockups enable rapid iteration and clear
communication of intent before coding begins.

### III. Model-View-Controller Architecture

The application MUST maintain strict separation between data models, user
interface views, and controller logic. The simulation engine runs independently
from the UI thread. JavaFX property bindings connect views to models without
direct coupling.

**Rationale**: MVC architecture ensures maintainability, testability, and
allows parallel development of UI and business logic. Thread separation
prevents UI blocking during intensive calculations.

### IV. Behavior-Driven Design

All USV behaviors MUST implement the common Behaviour interface with consistent
state management, progress tracking, and demand generation. CompositeBehaviour
orchestrates sequential execution. Each behavior is self-contained with its own
waypoint generation and completion logic.

**Rationale**: The behavior pattern enables extensibility for new mission types
while maintaining consistent execution semantics across all behavior types.

### V. Incremental Development

Development MUST follow the MVP-first approach defined in Phase 1 (Core Foundation),
Phase 2 (MVP Features), Phase 3 (Complete Functionality), Phase 4 (Visual Polish),
and Phase 5 (Demo Preparation). Each phase has clear deliverables and validation
criteria before proceeding.

**Rationale**: Incremental delivery ensures a working system early, allows for
course correction, and guarantees core functionality even if time runs short.

### VI. Visual Feedback & User Experience

The application MUST provide real-time visual feedback for all user actions and
simulation states. This includes smooth 60 FPS animations, clear behavior
visualization with color coding, progress indicators, and responsive controls
with <100ms interaction latency.

**Rationale**: As a demonstration application for an interview, professional
visual presentation and smooth user experience are critical success factors.

### VII. Testable Business Logic Isolation

All business logic MUST be placed in dedicated, framework-independent modules
that can be thoroughly unit tested without UI or external dependencies. Core
algorithms (pattern generation, navigation, geometry) must be pure functions
where possible. Business logic modules must achieve >80% unit test coverage.

**Rationale**: Isolated business logic enables fast, reliable unit testing.
Pure functions are easier to test, debug, and maintain. High coverage ensures
reliability of critical mission planning algorithms.

### VIII. End-to-End Testing Coverage

High-level components and complete user workflows MUST be covered by end-to-end
tests. This includes mission creation workflows, behavior sequencing, simulation
execution, and state transitions. E2E tests validate that integrated components
work correctly together from the user's perspective.

**Rationale**: While unit tests verify correctness of individual components,
E2E tests ensure the system works as a cohesive whole. They catch integration
issues and validate real user scenarios.

## Quality Standards

### Performance Requirements
- Simulation engine update rate: minimum 1Hz
- UI frame rate: target 60 FPS for animations
- User interaction response: <100ms
- Pattern generation: <500ms for 1km² areas
- Memory usage: <512MB during normal operation

### Code Quality
- Clean separation of concerns (MVC pattern)
- Multi-threaded architecture (simulation/UI separation)
- Commented algorithms for key calculations
- Maven-based dependency management
- Git version control with meaningful commits

### Testing Coverage
- **Unit Tests** (>80% coverage for business logic):
  - Geometry calculations (GeoUtils)
  - Pattern generation algorithms (SearchPatternGenerator)
  - Navigation logic (platform dynamics)
  - Behavior state machines
  - Mission sequencing logic
- **Integration Tests**:
  - Behavior-to-behavior transitions
  - Controller-model interactions
  - Thread synchronization verification
- **End-to-End Tests**:
  - Complete mission planning workflow
  - Full simulation execution cycle
  - User interaction sequences
  - Error recovery scenarios
- **Manual Testing**:
  - Visual verification of patterns
  - Performance under load
  - Edge cases and boundary conditions

## Development Workflow

### Feature Implementation Process
1. Define feature in specification
2. **For Business Logic**:
   - Write unit tests first
   - Implement in framework-independent modules
   - Follow Red-Green-Refactor cycle
3. **For UI Components**:
   - Create ASCII mockup
   - Get product owner (Doc) approval
   - Implement approved design
4. Write E2E tests for complete workflows
5. Integrate business logic with UI via controllers
6. Verify all tests pass (unit, integration, E2E)
7. Verify against acceptance criteria

### Code Review Gates
- All tests pass before merging (unit, integration, E2E)
- Business logic modules have >80% test coverage
- UI components have approved ASCII mockup on record
- No business logic in UI components
- No blocking operations on UI thread
- Behavior contract compliance verified
- Visual elements match approved mockup
- Performance metrics within defined limits

### Documentation Requirements
- JavaDoc for all public APIs
- README with build and run instructions
- Inline comments for complex algorithms
- Test documentation explaining scenarios
- Screenshots of key features for portfolio

## Governance

### Amendment Procedure
Changes to this constitution require:
1. Documented justification for the change
2. Impact assessment on existing code
3. Migration plan for affected components
4. Version increment following semantic versioning

### Versioning Policy
- MAJOR: Removing principles or fundamental architecture changes
- MINOR: Adding new principles or significant expansions
- PATCH: Clarifications and minor refinements

### Compliance Review
- All feature implementations must verify constitution compliance
- Architecture decisions reference relevant principles
- Deviations require explicit justification in code comments
- Phase gates enforce principle adherence before progression

**Version**: 1.2.0 | **Ratified**: 2025-10-24 | **Last Amended**: 2025-10-24