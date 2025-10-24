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
- **NFR-PERF-004**: Application startup < 3 seconds
- **NFR-PERF-005**: Pattern generation < 500ms for typical area (1km²)
- **NFR-PERF-006**: Memory usage < 512MB during normal operation

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

### 3.5 Visual Design
- **NFR-VIS-001**: Smooth 60 FPS animations for USV movement and map transitions
- **NFR-VIS-002**: Professional Material Design-inspired UI theme
- **NFR-VIS-003**: Real-time mission dashboard with:
  - Speed/heading gauge with animated needle
  - Coverage percentage indicator with progress bar
  - Time-to-completion estimate with live updates
  - Distance traveled counter
- **NFR-VIS-004**: Animated transitions between behaviors (fade/slide effects)
- **NFR-VIS-005**: Consistent color scheme for behavior types
- **NFR-VIS-006**: Smooth bezier curves for turn visualization
- **NFR-VIS-007**: Platform wake trail animation (optional toggle)

### 3.6 Robustness
- **NFR-ROB-001**: Graceful handling of invalid polygons (self-intersecting, insufficient vertices)
- **NFR-ROB-002**: Recovery from simulation exceptions without data loss
- **NFR-ROB-003**: Input validation with descriptive user feedback
- **NFR-ROB-004**: Automatic state saving every 30 seconds
- **NFR-ROB-005**: Handling of extreme values (very small/large areas, speeds)
- **NFR-ROB-006**: Thread-safe simulation engine with proper synchronization

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
com.planetmayo.usvsim/
├── model/
│   ├── mission/
│   │   ├── Mission.java              // Mission plan container
│   │   └── CompositeBehaviour.java   // Behaviour sequencer
│   ├── behaviour/
│   │   ├── Behaviour.java            // Core behaviour interface
│   │   ├── ParallelTrackSearch.java  // Implements Behaviour
│   │   ├── ExpandingSquareSearch.java
│   │   ├── WaypointTransit.java
│   │   └── ReturnToBase.java
│   ├── platform/
│   │   ├── Platform.java             // Platform manager
│   │   ├── PlatformCapabilities.java // Static capabilities
│   │   ├── PlatformState.java        // Dynamic state
│   │   └── PlatformDemand.java       // Control demands
│   ├── geometry/
│   │   ├── Position.java             // Lat/lon position
│   │   ├── Waypoint.java             // Navigation waypoint
│   │   └── Polygon.java              // Search area polygon
│   └── samples/
│       └── SampleBehaviours.java     // Hard-coded test behaviors
├── controller/
│   ├── MissionController.java        // Main application controller
│   ├── SimulationEngine.java        // Simulation execution thread
│   ├── BehaviourExecutor.java       // Behaviour state machine
│   └── DrawingController.java       // Handle map drawing interactions
├── view/
│   ├── MainView.java                // Primary UI layout
│   ├── MapPanel.java                // Map display component
│   ├── MissionPlanPanel.java       // Behaviour list panel
│   ├── ControlPanel.java           // Simulation controls
│   ├── StatePanel.java             // Platform state display
│   └── DashboardPanel.java         // Real-time gauges/indicators
├── util/
│   ├── GeoUtils.java               // Great circle calculations
│   ├── SearchPatternGenerator.java // Pattern generation algorithms
│   ├── PolygonUtils.java          // Polygon clipping/intersection
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
  1. Get current platform state
  2. Pass state to current behavior
  3. Receive demanded state (heading, speed, depth)
  4. Apply platform dynamics constraints:
     a. Calculate angle difference: Δθ = demanded_heading - current_heading
     b. If |Δθ| > threshold:
          heading_rate = speed / turn_radius
          current_heading += sign(Δθ) × heading_rate × dt
     c. Else:
          current_heading = demanded_heading
  5. Apply speed changes with acceleration limits
  6. Calculate distance traveled: d = speed × dt
  7. Update position along current heading
  8. Update behavior progress
  9. If behavior.isComplete():
       Advance to next behavior in composite
```

#### 4.3.4 Behavior Execution Model
```
SimulationEngine (runs at fixed timestep):
  1. Get current PlatformState
  2. CompositeBehavior.getDemandedState(currentState)
     → Returns PlatformDemand from active behavior
  3. Apply platform dynamics to demanded state
  4. Update PlatformState
  5. CompositeBehavior.updateProgress(newState)
  6. Check for behavior completion
  7. Render updated state to UI
```

### 4.4 Offline Map Strategy

#### 4.4.1 Tile Caching
- **OFFLINE-001**: Pre-bundle OpenStreetMap tiles for Portland Harbour area (zoom levels 10-18)
- **OFFLINE-002**: Include tiles in resources/ folder (~50MB for the region)
- **OFFLINE-003**: Coverage area: 20km radius around 50.6°N, 2.4°W
- **OFFLINE-004**: Implement custom TileProvider that reads from local cache first

#### 4.4.2 Implementation Approach
- **OFFLINE-005**: Use java_leaflet's offline mode configuration
- **OFFLINE-006**: Package tiles in JAR for single-file distribution
- **OFFLINE-007**: Fallback to simple vector map if tiles unavailable
- **OFFLINE-008**: Support for both online and offline modes via configuration toggle

### 4.5 Data Formats

#### 4.5.1 Mission Persistence
- **DATA-001**: Mission files stored as JSON for save/load functionality
- **DATA-002**: Include version field for forward compatibility
- **DATA-003**: Support export/import of mission plans

#### 4.5.2 Configuration Storage
- **DATA-004**: User preferences in properties file (.properties)
- **DATA-005**: Platform dynamics settings persisted between sessions
- **DATA-006**: Window size and position remembered

#### 4.5.3 Telemetry Export
- **DATA-007**: Optional CSV export of simulation data
- **DATA-008**: Include timestamp, position, heading, speed, current behavior
- **DATA-009**: Support for KML export for Google Earth visualization

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
    CompositeBehaviour missionPlan;
    Platform platform;
    PlatformCapabilities capabilities;
    MissionState state;  // PLANNING, EXECUTING, PAUSED, COMPLETE
}
```

#### Behaviour (Core Interface)
```java
interface Behaviour {
    String getName();
    String getDescription();
    BehaviourState getState();  // PENDING, EXECUTING, COMPLETE
    double getProgress();        // 0.0 to 1.0

    // Core behavior execution
    PlatformDemand getDemandedState(PlatformState currentState);
    void updateProgress(PlatformState currentState);
    boolean isComplete();

    // Visualization
    List<Waypoint> getWaypoints();
    Color getDisplayColor();
}
```

#### CompositeBehaviour
```java
class CompositeBehaviour implements Behaviour {
    List<Behaviour> behaviours;
    int currentBehaviourIndex;

    void addBehaviour(Behaviour behaviour);
    void removeBehaviour(int index);
    void reorderBehaviour(int from, int to);
    Behaviour getCurrentBehaviour();

    // Implements Behaviour interface
    PlatformDemand getDemandedState(PlatformState currentState) {
        return getCurrentBehaviour().getDemandedState(currentState);
    }
}
```

#### Concrete Behaviour Types
```java
class ParallelTrackSearch implements Behaviour {
    Polygon searchArea;
    double trackOrientation;  // 0-360°
    double trackSpacing;       // metres
    double platformSpeed;      // knots
    int currentWaypointIndex;
    List<Waypoint> generatedWaypoints;
}

class ExpandingSquareSearch implements Behaviour {
    Polygon searchArea;
    Position centerPoint;
    double initialDirection;   // 0-360°
    double legIncrement;       // metres
    double platformSpeed;      // knots
    int currentLegNumber;
    List<Waypoint> generatedWaypoints;
}

class WaypointTransit implements Behaviour {
    List<Position> userDefinedWaypoints;
    double transitSpeed;       // knots
    int currentWaypointIndex;
}

class ReturnToBase implements Behaviour {
    Position baseLocation;
    double transitSpeed;       // knots
    boolean reachedBase;
}
```

#### Platform Classes
```java
class PlatformCapabilities {
    String platformType;       // "USV", "AUV", etc
    double maxSpeed;           // knots
    double minSpeed;           // knots
    double maxDepth;           // metres (0 for surface vehicles)
    double turnRadius;         // metres at max speed
    double acceleration;       // m/s²
    double deceleration;       // m/s²
    double fuelCapacity;       // litres (optional)
}

class PlatformState {
    String id;
    Position position;         // lat, lon
    double heading;            // 0-360°
    double speed;              // knots
    double depth;              // metres (0 for surface)
    double fuelRemaining;      // litres (optional)
    List<Position> trackHistory;
    Instant timestamp;
}

class PlatformDemand {
    double demandedHeading;    // 0-360°
    double demandedSpeed;      // knots
    double demandedDepth;      // metres (0 for surface, included for AUV extensibility)
    TurnDirection turnDirection; // PORT, STARBOARD, SHORTEST
}
```

#### Supporting Classes
```java
class Waypoint {
    Position position;         // lat, lon
    double speed;              // target speed at this waypoint
    double acceptanceRadius;   // metres
    WaypointType type;         // TRANSIT, SEARCH, BASE, TURN
}

class Position {
    double latitude;
    double longitude;
}
```

---

## 7. Development Phases (MVP-First Approach)

### Phase 1: Core Foundation
**Priority**: Critical - Must complete first
- **Deliverables**:
  - Maven project setup with JavaFX and java_leaflet
  - Basic application window with layout structure
  - Offline map implementation with Portland Harbour tiles
  - Platform model and basic data structures
  - Mission class hierarchy
- **Validation**: Map displays offline, basic UI renders
- **Estimated Effort**: 10-15% of total time

### Phase 2: MVP Features
**Priority**: Critical - Basic working system
- **Deliverables**:
  - All 4 behavior types with basic implementation
  - Simple waypoint generation (no complex algorithms yet)
  - Hard-coded sample behaviors for immediate testing:
    - Pre-defined Portland Harbour search area polygon
    - Sample parallel track search (045°, 100m spacing)
    - Sample waypoint transit route (5 waypoints)
    - Sample expanding square (090°, 50m increment)
  - Basic simulation engine with simple movement
  - Minimal UI panels (unstyled but functional)
  - Platform marker on map
- **Validation**: Can create mission with all behaviors and run basic simulation
- **Estimated Effort**: 20-25% of total time

### Phase 3: Complete Functionality
**Priority**: High - Full feature implementation
- **Deliverables**:
  - Proper pattern generation algorithms
  - Polygon clipping and intersection
  - Realistic platform dynamics (turn radius, acceleration)
  - Full simulation control (start/pause/stop/speed)
  - Drawing tools for polygons and waypoints
  - Mission plan management (add/delete/reorder)
- **Validation**: All functional requirements met, behaviors execute correctly
- **Estimated Effort**: 30-35% of total time

### Phase 4: Visual Polish & UX
**Priority**: Medium - Professional presentation
- **Deliverables**:
  - Smooth 60 FPS animations
  - Professional Material Design styling
  - Real-time dashboard components
  - Animated behavior transitions
  - Track history with smooth curves
  - Color coding and visual hierarchy
  - Input validation and error dialogs
- **Validation**: Application looks professional and feels responsive
- **Estimated Effort**: 25-30% of total time

### Phase 5: Demo Preparation & Optimization
**Priority**: Medium - Interview readiness
- **Deliverables**:
  - Performance optimization
  - Bug fixes from testing
  - Compelling demo scenarios
  - Screenshot generation
  - Backup demo videos
  - README and documentation
  - Final testing on target platform
- **Validation**: Ready for interview demonstration
- **Estimated Effort**: 10-15% of total time

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

### 10.4 Technical Risks & Mitigations

#### Critical Risks
- **Risk**: java_leaflet offline mode not working as expected
  - **Mitigation**: Phase 1 proof-of-concept, fallback to JXMapViewer2 or GMapsFX
  - **Backup**: Pre-rendered static map images for worst case

- **Risk**: Performance issues with large search areas (>100 tracks)
  - **Mitigation**: Implement level-of-detail rendering for distant tracks
  - **Backup**: Limit maximum search area size with user warning

- **Risk**: Complex polygon intersection mathematics
  - **Mitigation**: Use JTS Topology Suite from start, not custom implementation
  - **Backup**: Simplified rectangular areas if polygon math fails

#### Moderate Risks
- **Risk**: JavaFX version compatibility issues
  - **Mitigation**: Target JavaFX 21 LTS, test on multiple JDK versions
  - **Backup**: Bundle JRE with application using jpackage

- **Risk**: Time overrun on visual polish
  - **Mitigation**: MVP-first approach ensures functionality by Phase 3
  - **Backup**: Focus on one perfect behavior type rather than all four

- **Risk**: Drawing tools complexity
  - **Mitigation**: Start with simple click-based polygon creation
  - **Backup**: Pre-defined test areas for demo if drawing fails

---

## 11. Demo Scenarios

### 11.1 Scenario 1: Mine Clearance Operation
**Duration**: 5 minutes
**Location**: Portland Harbour entrance
**Narrative**: "Demonstrating a typical mine clearance mission in a strategic waterway"

**Sequence**:
1. Draw search area at harbour entrance (1km × 2km polygon)
2. Add Parallel Track Search (045°, 100m spacing)
3. Start simulation, show systematic coverage
4. Pause mid-search, simulate "contact detected"
5. Draw 200m × 200m box around contact
6. Add Expanding Square Search for detailed investigation
7. Resume simulation, show transition between behaviors
8. Add Return to Base behavior
9. Complete mission execution

**Key Highlights**:
- Pattern generation algorithms
- Smooth behavior transitions
- Realistic turn dynamics
- Mission progress tracking

### 11.2 Scenario 2: Multi-Area Maritime Survey
**Duration**: 3 minutes
**Location**: Three separate areas in Weymouth Bay
**Narrative**: "Coordinated survey of multiple areas of interest"

**Sequence**:
1. Create three distinct polygons of different sizes
2. Apply different search patterns:
   - Area 1: Parallel Track Search (000°, 150m)
   - Area 2: Expanding Square Search (090°, 50m)
   - Area 3: Parallel Track Search (270°, 200m)
3. Add Waypoint Transit behaviors between areas
4. Execute at 10× speed

**Key Highlights**:
- Complex mission planning
- Multiple behavior types
- Efficient route planning
- Time acceleration capability

### 11.3 Scenario 3: Emergency Response & Replanning
**Duration**: 4 minutes
**Location**: Portland Harbour
**Narrative**: "Dynamic mission adjustment for emergency tasking"

**Sequence**:
1. Start with ongoing Parallel Track Search
2. Pause simulation after 30% completion
3. Insert high-priority Waypoint Transit (simulated emergency location)
4. Add small Expanding Square Search at emergency site
5. Insert Return to Original Mission behavior
6. Resume and show seamless mission adaptation

**Key Highlights**:
- Dynamic replanning capability
- Mission state preservation
- Priority task insertion
- Operational flexibility

### 11.4 Screenshot Opportunities
1. **Planning View**: Full mission with multiple colored behaviors
2. **Execution View**: USV mid-turn with track history
3. **Dashboard Close-up**: Real-time gauges and progress indicators
4. **Pattern Generation**: Complex search pattern fully rendered
5. **Split View**: Plan on left, execution on right
6. **Time Acceleration**: Motion blur effect at 20× speed

---

## 12. References

- **Search Patterns**: https://en.wikipedia.org/wiki/Water_surface_searches
- **java_leaflet**: https://github.com/makbn/java_leaflet
- **ASSET Simulator**: Internal diagrams (Images 1-12)
- **Great Circle Navigation**: Standard maritime navigation formulas

---

## 13. Glossary

- **USV**: Unmanned Surface Vehicle
- **Waypoint**: Geographic position (lat/lon) that platform navigates to
- **Behaviour**: A planned mission activity with associated geometry and parameters
- **Track**: The path followed by the platform
- **Great Circle**: Shortest distance between two points on a sphere
- **Turn Radius**: Minimum radius of curvature for platform turn at given speed
- **Acceptance Radius**: Distance threshold for considering waypoint "reached"

---

**Document Status**: Draft v1.2
**Last Updated**: October 2025
**Next Review**: After Phase 1 completion

**Change Log v1.2**:
- Added hard-coded sample behaviors for rapid MVP testing
- Redesigned behavior architecture with core Behaviour interface
- Introduced CompositeBehaviour for mission sequencing
- Separated PlatformCapabilities from PlatformState
- Added behavior progress tracking mechanism
- Defined clear behavior execution model with demanded state pattern
