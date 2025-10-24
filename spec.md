# USV Mission Planning & Simulation System
## Product Requirements Document

**Version:** 1.0  
**Date:** October 2025  
**Author:** Ian Mayo  
**Purpose:** Demonstrate JavaFX proficiency and autonomous systems understanding for TKMS ATLAS UK interview

---

## 1. Executive Summary

A JavaFX desktop application for planning and simulating Unmanned Surface Vehicle (USV) mine clearance missions. The system allows users to visually plan mission behaviours on a map, then simulate realistic USV execution with configurable platform dynamics.

**Key Objectives:**
- Demonstrate JavaFX UI development capability
- Show understanding of autonomous platform behaviour
- Illustrate mission planning and execution patterns
- Create compelling screenshots for interview presentation

---

## 2. Functional Requirements

### 2.1 Mission Planning

#### 2.1.1 Map Display
- **FR-MAP-001**: Display interactive map using java_leaflet library
- **FR-MAP-002**: Default map center: Portland Harbour/Weymouth Bay (50.6°N, 2.4°W)
- **FR-MAP-003**: Support pan and zoom operations
- **FR-MAP-004**: Display planned mission geometry (polygons, waypoints, tracks)
- **FR-MAP-005**: Display real-time USV position, heading, and track history

#### 2.1.2 Behaviour Types

##### Parallel Track Search
- **FR-PTS-001**: User draws polygon search area using custom drawing tool
- **FR-PTS-002**: User specifies track orientation (0-360°)
- **FR-PTS-003**: User specifies track spacing in metres
- **FR-PTS-004**: System generates parallel tracks within polygon at specified spacing
- **FR-PTS-005**: Tracks alternate direction for efficient coverage (see ASSET diagram reference)
- **FR-PTS-006**: All tracks clipped to polygon boundary
- **FR-PTS-007**: Display generated track pattern on map with behaviour label

##### Expanding Square Search
- **FR-ESS-001**: User draws polygon search area
- **FR-ESS-002**: System calculates polygon centroid as search start point
- **FR-ESS-003**: User specifies initial direction (0-360°)
- **FR-ESS-004**: User specifies leg increment in metres
- **FR-ESS-005**: System generates expanding square pattern from centroid
- **FR-ESS-006**: Pattern follows expanding square algorithm (leg lengths: n, n, n+inc, n+inc, n+2×inc, n+2×inc...)
- **FR-ESS-007**: 90° right turns between legs
- **FR-ESS-008**: Pattern generation stops when legs extend beyond polygon boundary
- **FR-ESS-009**: Display generated pattern on map with behaviour label

##### Waypoint Transit
- **FR-WPT-001**: User clicks map to add waypoint markers
- **FR-WPT-002**: System displays polyline connecting waypoints in order
- **FR-WPT-003**: User can add multiple waypoints sequentially
- **FR-WPT-004**: "Done" button appears after first waypoint added
- **FR-WPT-005**: "Cancel" button allows user to abort waypoint entry
- **FR-WPT-006**: Display waypoints and route on map with behaviour label

##### Return to Base
- **FR-RTB-001**: User selects "Return to Base" behaviour
- **FR-RTB-002**: Dialog offers: "Use current position" or "Specify coordinates"
- **FR-RTB-003**: System creates direct transit to base location
- **FR-RTB-004**: Display return route on map with behaviour label

#### 2.1.3 Mission Plan Management
- **FR-PLAN-001**: Display Mission Plan panel listing all planned behaviours
- **FR-PLAN-002**: Each behaviour numbered sequentially (1, 2, 3...)
- **FR-PLAN-003**: Show execution status for each behaviour (☐ pending, ☑ complete, ▶ executing)
- **FR-PLAN-004**: "Add Behaviour" dropdown with options:
  - Parallel Track Search
  - Expanding Square Search
  - Waypoint Transit
  - Return to Base
- **FR-PLAN-005**: Allow reordering behaviours (drag-and-drop or up/down buttons)
- **FR-PLAN-006**: Allow deleting behaviours
- **FR-PLAN-007**: Allow editing behaviour parameters

#### 2.1.4 Drawing Tools
- **FR-DRAW-001**: Implement custom JavaFX canvas overlay for drawing
- **FR-DRAW-002**: Polygon drawing mode: click to add vertices, close on first point
- **FR-DRAW-003**: Point drawing mode: click to add markers
- **FR-DRAW-004**: "Cancel" button exits drawing mode without creating behaviour
- **FR-DRAW-005**: Capture lat/lon from mouse position on map
- **FR-DRAW-006**: Provide visual feedback during drawing (temporary lines/points)
- **FR-DRAW-007**: Fallback to java_leaflet built-in drawing if available

### 2.2 Simulation Execution

#### 2.2.1 Platform Dynamics
- **FR-DYN-001**: USV follows waypoint list with realistic turn dynamics
- **FR-DYN-002**: Configurable turn radius (default: 200m)
- **FR-DYN-003**: Configurable max speed (default: 8 knots)
- **FR-DYN-004**: Configurable acceleration (default: 0.5 m/s²)
- **FR-DYN-005**: Configurable deceleration (default: 1.0 m/s²)
- **FR-DYN-006**: Calculate great circle bearing/distance to waypoints
- **FR-DYN-007**: Execute turn arcs when angle to waypoint exceeds threshold
- **FR-DYN-008**: Reduce speed approaching tight turns
- **FR-DYN-009**: Advance waypoint when within acceptance radius (50m)
- **FR-DYN-010**: Display smooth curved tracks (not sharp corners)

#### 2.2.2 Simulation Control
- **FR-CTRL-001**: Start button begins simulation from current platform state
- **FR-CTRL-002**: Pause button freezes simulation, preserving state
- **FR-CTRL-003**: Stop button resets simulation to initial state
- **FR-CTRL-004**: Time acceleration slider: 1×, 2×, 5×, 10×, 20×
- **FR-CTRL-005**: Update display at reasonable frame rate (minimum 10 FPS)
- **FR-CTRL-006**: Multi-threaded simulation engine separate from UI thread

#### 2.2.3 Platform State Display
- **FR-STATE-001**: Display current platform identifier (e.g., "USV-1")
- **FR-STATE-002**: Display current position (lat/lon, decimal degrees)
- **FR-STATE-003**: Display current heading (0-360°)
- **FR-STATE-004**: Display current speed (knots)
- **FR-STATE-005**: Display current behaviour being executed
- **FR-STATE-006**: Display mission progress (behaviour N of M)
- **FR-STATE-007**: Update state display in real-time during simulation

#### 2.2.4 Visual Feedback
- **FR-VIS-001**: Display USV as oriented marker/icon on map
- **FR-VIS-002**: Display track history as polyline trail
- **FR-VIS-003**: Auto-pan map to keep USV in view (optional toggle)
- **FR-VIS-004**: Display sensor coverage circle around USV (optional)
- **FR-VIS-005**: Differentiate planned route (dashed) from executed track (solid)
- **FR-VIS-006**: Use color coding for behaviour types

---

## 3. Non-Functional Requirements

### 3.1 Performance
- **NFR-PERF-001**: Simulation update rate: minimum 1Hz (1 update/second simulation time)
- **NFR-PERF-002**: UI responsiveness: maximum 100ms for user interactions
- **NFR-PERF-003**: Map rendering: smooth pan/zoom with no visible lag

### 3.2 Usability
- **NFR-USE-001**: Intuitive workflow: plan → simulate → review
- **NFR-USE-002**: Clear visual distinction between planning and execution modes
- **NFR-USE-003**: Tooltips for all controls and buttons
- **NFR-USE-004**: Error messages for invalid inputs (e.g., negative spacing)

### 3.3 Code Quality
- **NFR-CODE-001**: MVC architecture pattern
- **NFR-CODE-002**: Clean separation: simulation engine / UI / map rendering
- **NFR-CODE-003**: Commented code for key algorithms
- **NFR-CODE-004**: Maven build system with dependency management
- **NFR-CODE-005**: Git version control throughout development

### 3.4 Documentation
- **NFR-DOC-001**: README with build instructions
- **NFR-DOC-002**: JavaDoc for public APIs
- **NFR-DOC-003**: Screenshots of key features

---

## 4. Technical Architecture

### 4.1 Technology Stack
- **Language**: Java 17+
- **UI Framework**: JavaFX 21+
- **Build System**: Maven
- **Mapping Library**: java_leaflet (https://github.com/makbn/java_leaflet)
- **Geometry**: Custom great circle calculations or JTS Topology Suite
- **Version Control**: Git

### 4.2 Package Structure
```
com.deepbluec.usvsim/
├── model/
│   ├── Mission.java              // Mission plan container
│   ├── Behaviour.java            // Abstract behaviour base class
│   ├── ParallelTrackSearch.java
│   ├── ExpandingSquareSearch.java
│   ├── WaypointTransit.java
│   ├── ReturnToBase.java
│   ├── Platform.java             // USV state and dynamics
│   └── Waypoint.java
├── controller/
│   ├── MissionController.java    // Main application controller
│   ├── SimulationEngine.java    // Simulation execution thread
│   └── DrawingController.java   // Handle map drawing interactions
├── view/
│   ├── MainView.java            // Primary UI layout
│   ├── MapPanel.java            // Map display component
│   ├── MissionPlanPanel.java   // Behaviour list panel
│   ├── ControlPanel.java       // Simulation controls
│   └── StatePanel.java         // Platform state display
├── util/
│   ├── GeoUtils.java           // Great circle calculations
│   ├── SearchPatternGenerator.java  // Pattern generation algorithms
│   └── Constants.java
└── Main.java
```

### 4.3 Key Algorithms

#### 4.3.1 Parallel Track Generation
1. Transform polygon vertices to local coordinate system aligned with track orientation
2. Calculate bounding box in transformed coordinates
3. Generate parallel lines at track spacing intervals
4. Alternate line direction for each track
5. Clip lines to polygon boundary using line-polygon intersection
6. Transform waypoints back to lat/lon coordinates

**Reference**: ASSET simulator diagram (Image 1)

#### 4.3.2 Expanding Square Generation
1. Calculate polygon centroid (arithmetic mean of vertices)
2. Starting from centroid, travel in initial direction for leg_increment distance
3. Turn 90° right
4. For each subsequent leg:
   - If leg count is odd: distance = leg_increment × ((count + 1) / 2)
   - If leg count is even: distance = leg_increment × (count / 2)
   - Turn 90° right after each leg
5. Stop when leg endpoints exceed polygon bounds
6. Clip pattern to polygon boundary

**Reference**: https://en.wikipedia.org/wiki/Water_surface_searches#Expanding_square_search (Image 2)

#### 4.3.3 Platform Navigation
```
For each simulation time step (dt):
  1. Calculate bearing to next waypoint
  2. Calculate angle difference: Δθ = target_bearing - current_heading
  3. If |Δθ| > threshold:
       heading_rate = speed / turn_radius
       current_heading += sign(Δθ) × heading_rate × dt
  4. Else:
       current_heading = target_bearing
  5. Calculate distance traveled: d = speed × dt
  6. Update position along current heading
  7. If distance_to_waypoint < acceptance_radius:
       Advance to next waypoint
```

---

## 5. User Interface Design

### 5.1 Layout
```
┌─────────────────────────────────────────────────────────────┐
│ Menu: File | Mission | View | Help                          │
├──────────────────────────────┬──────────────────────────────┤
│                              │  Control Panel               │
│                              │  ▶ Start ⏸ Pause ⏹ Stop     │
│                              │  Speed: [========  ] 10×     │
│                              ├──────────────────────────────┤
│                              │  Mission Plan                │
│      Map Display             │  1. ▶ Parallel Track Search  │
│                              │     Orient: 045° Spc: 100m   │
│   (Leaflet + Drawing Tools)  │  2. ☐ Waypoint Transit       │
│                              │     3 waypoints              │
│                              │  3. ☐ Return to Base         │
│                              │                              │
│                              │  [+ Add Behaviour ▼]         │
│                              │  [Edit] [Delete] [▲] [▼]    │
│                              ├──────────────────────────────┤
│                              │  Platform State              │
│                              │  USV-1                       │
│                              │  Status: Executing #1        │
│                              │  Pos: 50.612°N, 2.456°W      │
│                              │  Hdg: 045° Spd: 7.2kts       │
│                              │  Progress: Behaviour 1 of 3  │
└──────────────────────────────┴──────────────────────────────┘
```

### 5.2 Menu Structure
- **File**
  - New Mission
  - Open Mission... (future)
  - Save Mission... (future)
  - Exit
- **Mission**
  - Add Behaviour >
    - Parallel Track Search
    - Expanding Square Search
    - Waypoint Transit
    - Return to Base
  - Clear Mission
- **View**
  - Show Track History
  - Show Planned Routes
  - Show Sensor Coverage
  - Auto-pan to Platform
- **Help**
  - About
  - User Guide (future)

### 5.3 Dialog Designs

#### Parallel Track Search Properties
```
┌─────────────────────────────────┐
│  Parallel Track Search          │
├─────────────────────────────────┤
│  Track Orientation:  [___]°     │
│  (Direction of first track)     │
│                                 │
│  Track Spacing:      [___] m    │
│                                 │
│  Platform Speed:     [___] kts  │
│                                 │
│          [OK]  [Cancel]         │
└─────────────────────────────────┘
```

#### Expanding Square Search Properties
```
┌─────────────────────────────────┐
│  Expanding Square Search        │
├─────────────────────────────────┤
│  Initial Direction:  [___]°     │
│  (First leg from center)        │
│                                 │
│  Leg Increment:      [___] m    │
│                                 │
│  Platform Speed:     [___] kts  │
│                                 │
│          [OK]  [Cancel]         │
└─────────────────────────────────┘
```

#### Return to Base
```
┌─────────────────────────────────┐
│  Return to Base                 │
├─────────────────────────────────┤
│  Base Location:                 │
│  ⦿ Use current platform position│
│  ○ Specify coordinates:         │
│     Lat: [_______]°N            │
│     Lon: [_______]°W            │
│                                 │
│          [OK]  [Cancel]         │
└─────────────────────────────────┘
```

---

## 6. Data Model

### 6.1 Core Classes

#### Mission
```java
class Mission {
    List<Behaviour> behaviours;
    Platform platform;
    int currentBehaviourIndex;
    MissionState state;  // PLANNING, EXECUTING, PAUSED, COMPLETE
}
```

#### Behaviour (Abstract)
```java
abstract class Behaviour {
    String name;
    List<Waypoint> waypoints;
    BehaviourState state;  // PENDING, EXECUTING, COMPLETE
    abstract List<Waypoint> generateWaypoints();
}
```

#### Platform
```java
class Platform {
    String id;
    double lat, lon;          // Current position
    double heading;           // 0-360°
    double speed;             // knots
    double maxSpeed;          // knots
    double turnRadius;        // metres
    double acceleration;      // m/s²
    double deceleration;      // m/s²
    List<Position> trackHistory;
}
```

#### Waypoint
```java
class Waypoint {
    double lat, lon;
    double speed;  // target speed at this waypoint
    WaypointType type;  // TRANSIT, SEARCH, BASE
}
```

---

## 7. Development Phases

### Phase 1: Project Setup & Basic Map (Day 1)
- **Deliverables**:
  - Maven project with JavaFX and java_leaflet dependencies
  - Basic application window
  - Map view displaying Portland Harbour area
  - Verify pan/zoom functionality

### Phase 2: Simulation Engine (Day 2)
- **Deliverables**:
  - Platform class with dynamics model
  - SimulationEngine with time-stepping loop
  - Basic waypoint following (no behaviours yet)
  - Platform renders as marker on map
  - Track history visualization

### Phase 3: Drawing Tools & Mission Planning (Day 3)
- **Deliverables**:
  - Custom drawing overlay (polygon, points)
  - Mission Plan panel UI
  - Add/delete behaviours
  - Property dialogs for each behaviour type
  - Planned routes display on map

### Phase 4: Search Pattern Generation (Day 4)
- **Deliverables**:
  - ParallelTrackSearch implementation
  - ExpandingSquareSearch implementation
  - SearchPatternGenerator utility
  - Verify patterns clip to polygon correctly
  - Visual differentiation of behaviour types

### Phase 5: Simulation Execution & Polish (Day 5)
- **Deliverables**:
  - Control panel (start/pause/stop)
  - Time acceleration
  - Platform state panel with real-time updates
  - Smooth turn dynamics
  - Mission execution: sequential behaviour processing
  - Generate compelling screenshots

---

## 8. Testing Strategy

### 8.1 Unit Tests (Optional, time permitting)
- GeoUtils great circle calculations
- SearchPatternGenerator algorithms
- Platform dynamics calculations

### 8.2 Manual Testing Scenarios
1. **Basic Planning**:
   - Add parallel track search, verify tracks generate correctly
   - Add expanding square search, verify pattern from centroid
   - Add waypoint transit, verify route displays
   - Add return to base, verify routing

2. **Simulation Execution**:
   - Execute single behaviour, verify completion
   - Execute multi-behaviour mission, verify sequential execution
   - Verify turn radius creates smooth arcs
   - Verify speed changes approaching waypoints

3. **Edge Cases**:
   - Very small search area (< turn radius)
   - Very large search area (many tracks)
   - Complex polygon shapes (concave, self-intersecting)
   - Behaviours with zero waypoints

### 8.3 Visual Verification
- Track history matches expected paths
- Planned routes align with polygon boundaries
- Platform heading indicator points correctly
- Time acceleration maintains smooth display

---

## 9. Future Enhancements (Out of Scope)

- Multiple simultaneous platforms
- Coordinated multi-platform behaviours
- Sensor simulation (detection zones, contacts)
- Mission save/load (XML or JSON)
- Mission replay with timeline scrubbing
- Integration with real hardware (via MAVLink or similar)
- 3D visualization
- Environmental factors (currents, wind)
- Fuel/battery consumption modeling
- Obstacle avoidance

---

## 10. Success Criteria

### 10.1 Functional Success
- ✓ User can plan mission with all 4 behaviour types
- ✓ Planned routes display correctly on map
- ✓ Simulation executes mission from start to completion
- ✓ Platform follows realistic turn dynamics
- ✓ Track history shows smooth curves (not sharp corners)

### 10.2 Technical Success
- ✓ Clean MVC architecture
- ✓ Multi-threaded simulation (non-blocking UI)
- ✓ Responsive UI at 10× time acceleration
- ✓ Code demonstrates JavaFX proficiency

### 10.3 Interview Success
- ✓ Generate 5+ compelling screenshots showing:
  - Mission planning UI
  - Search pattern generation
  - Realistic platform dynamics
  - Multi-behaviour mission execution
  - Professional appearance
- ✓ Be able to discuss architecture and algorithms confidently
- ✓ Demonstrate understanding of autonomous systems concepts

---

## 11. References

- **Search Patterns**: https://en.wikipedia.org/wiki/Water_surface_searches
- **java_leaflet**: https://github.com/makbn/java_leaflet
- **ASSET Simulator**: Internal diagrams (Images 1-12)
- **Great Circle Navigation**: Standard maritime navigation formulas

---

## 12. Glossary

- **USV**: Unmanned Surface Vehicle
- **Waypoint**: Geographic position (lat/lon) that platform navigates to
- **Behaviour**: A planned mission activity with associated geometry and parameters
- **Track**: The path followed by the platform
- **Great Circle**: Shortest distance between two points on a sphere
- **Turn Radius**: Minimum radius of curvature for platform turn at given speed
- **Acceptance Radius**: Distance threshold for considering waypoint "reached"

---

**Document Status**: Draft v1.0  
**Next Review**: After Phase 1 completion
