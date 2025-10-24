# Feature Specification: USV Mission Planning & Simulation System

**Feature Branch**: `001-usv-mission-planner`
**Created**: 2025-10-24
**Status**: Draft
**Input**: User description: "USV Mission Planning & Simulation System - A desktop application for planning and simulating Unmanned Surface Vehicle mine clearance missions with visual mission planning on a map and realistic platform dynamics simulation"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Plan Simple Search Mission (Priority: P1)

As a mission planner, I need to create a basic parallel track search mission over a defined area so that I can systematically cover the search zone for mine clearance operations.

**Why this priority**: This is the core MVP functionality. A parallel track search is the most common search pattern for mine clearance. Without this, the system provides no value.

**Independent Test**: Can be fully tested by drawing a search area polygon, specifying track parameters (orientation and spacing), and verifying the generated track pattern appears on the map. Delivers immediate value by generating a valid search pattern.

**Acceptance Scenarios**:

1. **Given** the application is open with a map displayed, **When** I select "Add Behaviour → Parallel Track Search" and draw a polygon search area, **Then** the system prompts me for track orientation (0-360°) and track spacing (metres)

2. **Given** I have drawn a search area and entered parameters (045° orientation, 100m spacing), **When** I confirm the behaviour, **Then** the system generates parallel tracks within the polygon, alternating direction for efficiency, and displays them on the map with a behaviour label

3. **Given** a parallel track search is created, **When** I view the Mission Plan panel, **Then** I see the behaviour listed as "#1 Parallel Track Search" with its parameters displayed

---

### User Story 2 - Execute Mission Simulation (Priority: P1)

As a mission planner, I need to simulate the USV executing my planned mission with realistic movement dynamics so that I can verify the mission is feasible and estimate completion time.

**Why this priority**: Planning without execution validation provides limited value. Simulation is the key differentiator that demonstrates understanding of autonomous systems and validates mission feasibility.

**Independent Test**: Can be fully tested by creating a simple mission (even with pre-defined sample behaviours), pressing "Start", and observing the USV icon move along the planned route with realistic turns and speed changes. Delivers value by validating mission feasibility.

**Acceptance Scenarios**:

1. **Given** a mission plan exists with at least one behaviour, **When** I press the "Start" button, **Then** the USV begins executing the mission from its current position, moving along the planned waypoints with realistic turn dynamics

2. **Given** the simulation is running, **When** I observe the USV movement, **Then** I see smooth curved paths (not sharp corners), the USV heading indicator points in the direction of travel, and a track history trail is drawn behind the platform

3. **Given** the simulation is executing, **When** I adjust the time acceleration slider (1×, 2×, 5×, 10×, 20×), **Then** the simulation speed changes accordingly while maintaining smooth visual display

4. **Given** the simulation is running, **When** I press "Pause", **Then** the simulation freezes at the current state, and I can press "Start" again to resume from that point

5. **Given** the simulation is paused or running, **When** I press "Stop", **Then** the simulation resets to the initial state, clearing the track history

---

### User Story 3 - Create Multi-Behaviour Missions (Priority: P2)

As a mission planner, I need to sequence multiple behaviours (search patterns, waypoint transits, return to base) into a complete mission so that I can plan realistic operational scenarios with multiple phases.

**Why this priority**: While a single search pattern demonstrates core capability, real missions require multiple phases (transit to area, search, return). This is needed for compelling demonstration scenarios but builds on P1 functionality.

**Independent Test**: Can be fully tested by adding multiple behaviours to the mission plan (e.g., waypoint transit, parallel search, return to base), reordering them, and running the simulation to verify sequential execution. Delivers value by enabling complete mission planning.

**Acceptance Scenarios**:

1. **Given** a mission plan with one behaviour already exists, **When** I select "Add Behaviour" and choose "Waypoint Transit", then click points on the map and press "Done", **Then** a waypoint transit behaviour is added to the mission plan as behaviour #2

2. **Given** multiple behaviours exist in the mission plan, **When** I use the up/down buttons (or drag-and-drop), **Then** I can reorder the behaviours, and they are renumbered sequentially

3. **Given** a behaviour is selected in the mission plan, **When** I press the "Delete" button, **Then** the behaviour is removed from the mission, and remaining behaviours are renumbered

4. **Given** a mission with multiple behaviours is executing, **When** the USV completes the first behaviour (reaches all waypoints), **Then** the system automatically advances to the second behaviour, updating the status indicators (☑ complete for #1, ▶ executing for #2)

5. **Given** a mission with behaviours of different types (search, transit, return to base), **When** I execute the simulation, **Then** each behaviour executes in sequence, and I see the platform transition smoothly between different movement patterns

---

### User Story 4 - Detailed Area Investigation with Expanding Square (Priority: P2)

As a mission planner, I need to create an expanding square search pattern for detailed investigation of a specific area so that I can thoroughly examine a contact or area of interest from the center outward.

**Why this priority**: Expanding square is the standard pattern for investigating contacts found during initial search. Required for realistic demo scenarios showing multi-phase operations (initial search detects contact, then investigate with expanding square).

**Independent Test**: Can be fully tested by drawing a small area, selecting "Expanding Square Search", specifying initial direction and leg increment, and verifying the square spiral pattern generates from the polygon centroid. Delivers value by providing a second search pattern type.

**Acceptance Scenarios**:

1. **Given** the application is open, **When** I select "Add Behaviour → Expanding Square Search" and draw a polygon area, **Then** the system prompts me for initial direction (0-360°) and leg increment (metres)

2. **Given** I have drawn an area and entered parameters (090° initial direction, 50m leg increment), **When** I confirm the behaviour, **Then** the system calculates the polygon centroid and generates an expanding square pattern with 90° right turns between legs, stopping when legs extend beyond the polygon boundary

3. **Given** an expanding square search pattern is displayed, **When** I examine the generated path, **Then** I see legs increase in length following the pattern (n, n, n+inc, n+inc, n+2×inc, n+2×inc...) spiraling outward from the center

---

### User Story 5 - Monitor Real-Time Platform State (Priority: P3)

As a mission planner watching a simulation, I need to see real-time platform state information (position, heading, speed, current behaviour) so that I can monitor mission progress and verify correct execution.

**Why this priority**: While helpful for monitoring, the visual map display provides most of this information. Detailed state display enhances professional appearance but is not critical for core functionality.

**Independent Test**: Can be fully tested during any simulation run by observing the Platform State panel and verifying that position, heading, speed, and behaviour information update in real-time as the USV moves. Delivers value by providing detailed mission monitoring.

**Acceptance Scenarios**:

1. **Given** a simulation is running, **When** I view the Platform State panel, **Then** I see the platform identifier ("USV-1"), current position (lat/lon in decimal degrees), heading (0-360°), speed (knots), and current behaviour being executed

2. **Given** the USV is executing a mission, **When** the platform state updates, **Then** all displayed values refresh in real-time (minimum 1Hz update rate) and show accurate information matching the visual map display

3. **Given** a multi-behaviour mission is executing, **When** the platform completes a behaviour and advances to the next, **Then** the "Status" field updates to show "Executing #N" where N is the new behaviour number, and "Progress" shows "Behaviour N of M"

---

### User Story 6 - Configure Platform Dynamics (Priority: P3)

As a mission planner, I need to configure platform dynamics (turn radius, max speed, acceleration) so that I can simulate different USV types and evaluate mission feasibility under different platform constraints.

**Why this priority**: Default dynamics are sufficient for demonstration. Configurability is a nice-to-have for advanced users but not required for initial interview presentation. Can be implemented as simple input fields once simulation engine is working.

**Independent Test**: Can be fully tested by accessing platform configuration settings, changing values (e.g., reduce turn radius from 200m to 100m), and observing that subsequent simulations exhibit tighter turns. Delivers value by enabling "what-if" analysis for different platforms.

**Acceptance Scenarios**:

1. **Given** the application is running, **When** I access platform configuration (via menu or settings panel), **Then** I can modify turn radius (metres), max speed (knots), acceleration (m/s²), and deceleration (m/s²)

2. **Given** I have changed platform dynamics settings, **When** I run a simulation, **Then** the USV exhibits the configured behavior (e.g., tighter turns with smaller turn radius, faster top speed with increased max speed)

3. **Given** platform dynamics are configured, **When** I save the settings, **Then** the values persist between application sessions

---

### Edge Cases

- **What happens when the search area is smaller than the USV turn radius?**
  System should generate waypoints but display a warning that turns may cut outside the search boundary. Simulation shows realistic behavior (platform cannot physically make turns that tight).

- **What happens when a polygon is self-intersecting or has insufficient vertices?**
  System validates polygon on creation. If invalid, display error message: "Invalid polygon: must have at least 3 vertices and cannot self-intersect." User must redraw.

- **What happens when track spacing is larger than the search area dimensions?**
  System generates as many tracks as fit (minimum 1 track through the polygon center). Display warning: "Track spacing is large relative to search area - only N tracks generated."

- **What happens when the user tries to start a simulation with no behaviours in the mission plan?**
  Start button is disabled when mission plan is empty. If somehow triggered, display error: "Cannot start simulation: mission plan is empty."

- **How does the system handle behaviour completion during pause?**
  Pause freezes all simulation state. When resumed, the platform continues from the exact position/heading/speed where it was paused, even if mid-behaviour.

- **What happens when the USV is already executing a behaviour and the user tries to modify the mission plan?**
  For MVP: Mission plan editing is disabled during execution (buttons grayed out). User must stop the simulation to modify the plan.

- **What happens to the expanding square pattern when the polygon is very elongated (not roughly square)?**
  Pattern generates from centroid as normal but may terminate early on one side. This is expected behavior - pattern stops when legs extend beyond boundary in any direction.

- **How does the system handle concurrent execution at very high time acceleration (20×)?**
  Simulation engine runs on a separate thread with fixed time steps. UI updates at reasonable frame rate (10-60 FPS) regardless of time acceleration. If system cannot keep up, time acceleration is capped at achievable rate.

## Requirements *(mandatory)*

### Functional Requirements

#### Mission Planning

- **FR-PLAN-001**: System MUST display an interactive map centered on Portland Harbour/Weymouth Bay (50.6°N, 2.4°W) with pan and zoom operations
- **FR-PLAN-002**: System MUST provide drawing tools for creating polygons (click to add vertices, close on first point) and waypoint markers (click to place)
- **FR-PLAN-003**: System MUST provide a Mission Plan panel listing all planned behaviours with sequential numbering (1, 2, 3...)
- **FR-PLAN-004**: System MUST provide an "Add Behaviour" control with options: Parallel Track Search, Expanding Square Search, Waypoint Transit, Return to Base
- **FR-PLAN-005**: Users MUST be able to reorder behaviours in the mission plan using up/down buttons or drag-and-drop
- **FR-PLAN-006**: Users MUST be able to delete behaviours from the mission plan
- **FR-PLAN-007**: Users MUST be able to edit behaviour parameters after creation
- **FR-PLAN-008**: System MUST show execution status for each behaviour using visual indicators (☐ pending, ☑ complete, ▶ executing)

#### Parallel Track Search Behaviour

- **FR-PTS-001**: Users MUST be able to draw a polygon search area on the map
- **FR-PTS-002**: System MUST prompt users to specify track orientation (0-360°) and track spacing (metres)
- **FR-PTS-003**: System MUST generate parallel tracks within the polygon at the specified spacing and orientation
- **FR-PTS-004**: Generated tracks MUST alternate direction (back and forth) for efficient coverage
- **FR-PTS-005**: All tracks MUST be clipped to polygon boundaries
- **FR-PTS-006**: System MUST display the generated track pattern on the map with a behaviour label

#### Expanding Square Search Behaviour

- **FR-ESS-001**: Users MUST be able to draw a polygon search area for expanding square pattern
- **FR-ESS-002**: System MUST calculate the polygon centroid as the search start point
- **FR-ESS-003**: System MUST prompt users to specify initial direction (0-360°) and leg increment (metres)
- **FR-ESS-004**: System MUST generate expanding square pattern with 90° right turns between legs
- **FR-ESS-005**: Pattern MUST follow expanding square algorithm: leg lengths increase as (n, n, n+inc, n+inc, n+2×inc, n+2×inc...)
- **FR-ESS-006**: Pattern generation MUST stop when legs extend beyond polygon boundary
- **FR-ESS-007**: System MUST display the generated pattern on the map with a behaviour label

#### Waypoint Transit Behaviour

- **FR-WPT-001**: Users MUST be able to add waypoint markers by clicking the map
- **FR-WPT-002**: System MUST display a polyline connecting waypoints in order as they are added
- **FR-WPT-003**: System MUST provide a "Done" button (appears after first waypoint) to complete waypoint entry
- **FR-WPT-004**: System MUST provide a "Cancel" button to abort waypoint entry without creating the behaviour
- **FR-WPT-005**: System MUST display waypoints and route on the map with a behaviour label

#### Return to Base Behaviour

- **FR-RTB-001**: Users MUST be able to select "Return to Base" behaviour from the menu
- **FR-RTB-002**: System MUST present a dialog offering: "Use current position" or "Specify coordinates"
- **FR-RTB-003**: System MUST create a direct transit to the base location
- **FR-RTB-004**: System MUST display the return route on the map with a behaviour label

#### Simulation Execution

- **FR-SIM-001**: System MUST provide Start, Pause, and Stop controls for simulation execution
- **FR-SIM-002**: System MUST provide a time acceleration slider with settings: 1×, 2×, 5×, 10×, 20×
- **FR-SIM-003**: System MUST execute mission behaviours sequentially in the order defined in the mission plan
- **FR-SIM-004**: System MUST advance to the next behaviour automatically when the current behaviour is complete
- **FR-SIM-005**: System MUST update the simulation at minimum 1Hz (1 update per second of simulation time)
- **FR-SIM-006**: System MUST update the display at a reasonable frame rate (minimum 10 FPS, target 60 FPS)
- **FR-SIM-007**: Simulation engine MUST run on a separate thread from the UI to prevent blocking

#### Platform Dynamics

- **FR-DYN-001**: USV MUST follow waypoints with realistic turn dynamics using configurable turn radius (default 200m)
- **FR-DYN-002**: USV MUST respect configurable max speed (default 8 knots), acceleration (default 0.5 m/s²), and deceleration (default 1.0 m/s²)
- **FR-DYN-003**: System MUST calculate great circle bearing and distance between waypoints
- **FR-DYN-004**: System MUST execute turn arcs (not sharp corners) when changing direction
- **FR-DYN-005**: System MUST reduce speed when approaching tight turns
- **FR-DYN-006**: System MUST advance to the next waypoint when the USV comes within acceptance radius (50m)
- **FR-DYN-007**: System MUST display smooth curved tracks, not sharp corners

#### Visual Feedback

- **FR-VIS-001**: System MUST display the USV as an oriented icon on the map showing current heading
- **FR-VIS-002**: System MUST display track history as a polyline trail behind the USV
- **FR-VIS-003**: System MUST display planned routes (dashed lines) and executed tracks (solid lines) with different visual styles
- **FR-VIS-004**: System MUST use color coding to differentiate behaviour types
- **FR-VIS-005**: System MUST provide visual feedback during drawing operations (temporary lines/points)

#### Platform State Display

- **FR-STATE-001**: System MUST display platform identifier (e.g., "USV-1")
- **FR-STATE-002**: System MUST display current position (latitude/longitude in decimal degrees)
- **FR-STATE-003**: System MUST display current heading (0-360°)
- **FR-STATE-004**: System MUST display current speed (knots)
- **FR-STATE-005**: System MUST display the current behaviour being executed
- **FR-STATE-006**: System MUST display mission progress (behaviour N of M)
- **FR-STATE-007**: All platform state information MUST update in real-time during simulation

#### Performance & Quality

- **FR-PERF-001**: System MUST start up in under 3 seconds
- **FR-PERF-002**: User interactions MUST respond within 100ms
- **FR-PERF-003**: Pattern generation MUST complete within 500ms for typical areas (1km²)
- **FR-PERF-004**: Map rendering MUST provide smooth pan and zoom with no visible lag
- **FR-PERF-005**: System MUST maintain smooth animations (target 60 FPS) during normal operation

#### Input Validation

- **FR-VAL-001**: System MUST validate polygon geometry (minimum 3 vertices, no self-intersection)
- **FR-VAL-002**: System MUST validate numerical inputs (angles 0-360°, positive distances/speeds)
- **FR-VAL-003**: System MUST display descriptive error messages for invalid inputs
- **FR-VAL-004**: System MUST prevent simulation start when mission plan is empty

### Key Entities

- **Mission**: A complete mission plan containing a sequence of behaviours, platform information, and mission state (PLANNING, EXECUTING, PAUSED, COMPLETE)

- **Behaviour**: An abstract mission activity that the USV executes. Each behaviour has a name, description, state (PENDING, EXECUTING, COMPLETE), progress indicator, and generates waypoints for the platform to follow. Types include: Parallel Track Search, Expanding Square Search, Waypoint Transit, and Return to Base.

- **CompositeBehaviour**: A special behaviour that sequences multiple child behaviours for sequential execution, managing the active behaviour and transitioning to the next when each completes.

- **Platform**: Represents the USV with static capabilities (max speed, turn radius, acceleration limits) and dynamic state (position, heading, speed, track history).

- **PlatformState**: Current dynamic state including position (lat/lon), heading (0-360°), speed (knots), and timestamp.

- **PlatformCapabilities**: Static platform characteristics including platform type (USV), max/min speed (knots), turn radius (metres), acceleration/deceleration (m/s²).

- **PlatformDemand**: Control demands from a behaviour to the platform, specifying demanded heading, speed, and turn direction.

- **Position**: Geographic location represented as latitude and longitude (decimal degrees).

- **Waypoint**: A target position for the platform to navigate to, with associated target speed, acceptance radius (distance threshold for "reached"), and waypoint type (TRANSIT, SEARCH, BASE, TURN).

- **Polygon**: A search area defined by a list of vertices (positions) forming a closed boundary.

- **SearchPattern**: The geometric pattern of waypoints generated by search behaviours (parallel tracks or expanding square).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can create a complete parallel track search mission (draw area, specify parameters, view generated pattern) in under 2 minutes
- **SC-002**: Users can execute a simulated mission from start to completion for a typical search area (1km²) and observe the USV complete all waypoints
- **SC-003**: System generates search patterns (parallel tracks or expanding square) for typical areas (1km²) in under 500 milliseconds
- **SC-004**: Simulation displays smooth platform movement at 60 FPS during normal operation (no acceleration) and maintains minimum 10 FPS at maximum time acceleration (20×)
- **SC-005**: USV exhibits realistic turn dynamics with smooth curved paths (no sharp corners) during simulation
- **SC-006**: Users can create multi-behaviour missions (3+ behaviours) and observe correct sequential execution during simulation
- **SC-007**: System responds to all user interactions (button clicks, map operations) within 100ms
- **SC-008**: Application generates compelling visual output suitable for screenshot capture and interview presentation
- **SC-009**: Mission simulation completes all planned behaviours without errors or crashes
- **SC-010**: Platform state information updates in real-time (minimum 1Hz) with values matching visual display

### Assumptions

- Map tiles for Portland Harbour area (50.6°N, 2.4°W) will be pre-bundled in offline mode for reliable demonstration without internet dependency
- Default platform dynamics (8 knot max speed, 200m turn radius) represent a typical small USV suitable for mine clearance operations
- Great circle navigation calculations are appropriate for the geographic scale (local area operations, <100km range)
- Users have basic understanding of maritime navigation concepts (heading, speed in knots, lat/lon coordinates)
- Single USV operation is sufficient; multi-platform coordination is out of scope
- Mission save/load functionality is deferred; missions are created fresh each session for MVP
- Sensor simulation (sonar, detection ranges) is out of scope; focus is on navigation and behavior execution
- Environmental factors (wind, currents) are not simulated; platform follows idealized physics
- Interview demonstration duration is 5-10 minutes per scenario, informing performance targets