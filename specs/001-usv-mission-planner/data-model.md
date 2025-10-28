# Data Model: USV Mission Planning & Simulation System

**Feature**: 001-usv-mission-planner
**Date**: 2025-10-24
**Status**: Design Complete

## Overview

Entity definitions for the USV mission planning and simulation system. All classes use JavaFX properties for MVC bindings where appropriate. Business logic classes (util/, model/geometry, model/behaviour logic) are framework-independent and unit-testable.

---

## Entity Diagram

```
Mission
├── CompositeBehaviour (contains)
│   └── List<Behaviour>
│       ├── ParallelTrackSearch
│       ├── ExpandingSquareSearch
│       ├── WaypointTransit
│       └── ReturnToBase
├── Platform
│   ├── PlatformCapabilities
│   ├── PlatformState
│   └── List<PlatformState> trackHistory
└── MissionState (enum)

Behaviour → generates → List<Waypoint>
PlatformState + Behaviour → produces → PlatformDemand
```

---

## Core Entities

### 1. Mission

**Package**: `com.planetmayo.usvsim.model.mission`

**Purpose**: Top-level container for a complete mission plan.

**Fields**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `missionPlan` | `CompositeBehaviour` | Sequence of behaviours | Not null |
| `platform` | `Platform` | The USV being controlled | Not null |
| `capabilities` | `PlatformCapabilities` | Static platform characteristics | Not null |
| `state` | `MissionState` | Current execution state | PLANNING, EXECUTING, PAUSED, COMPLETE |
| `startTime` | `Instant` | When simulation started | Nullable (null if not started) |

**Relationships**:
- Contains one CompositeBehaviour (mission plan)
- Contains one Platform (the USV)
- Contains one PlatformCapabilities (fixed attributes)

**Operations**:
- `void start()`: Begin executing mission
- `void pause()`: Freeze current state
- `void stop()`: Reset to initial state
- `boolean isComplete()`: Check if all behaviours finished
- `double getProgress()`: Overall progress (0.0 to 1.0)

---

### 2. Behaviour (Interface)

**Package**: `com.planetmayo.usvsim.model.behaviour`

**Purpose**: Core abstraction for all mission activities.

**Methods**:
| Method | Return Type | Description |
|--------|-------------|-------------|
| `getName()` | `String` | Human-readable name (e.g., "Parallel Track Search") |
| `getDescription()` | `String` | Parameter summary (e.g., "045°, 100m spacing") |
| `getState()` | `BehaviourState` | PENDING, EXECUTING, COMPLETE |
| `getProgress()` | `double` | Completion percentage (0.0 to 1.0) |
| `getDemandedState(PlatformState current)` | `PlatformDemand` | Compute desired heading/speed given current state |
| `updateProgress(PlatformState current)` | `void` | Update internal state based on platform position |
| `isComplete()` | `boolean` | Check if behaviour finished |
| `getWaypoints()` | `List<Waypoint>` | All waypoints for visualization |
| `getDisplayColor()` | `Color` | Color for map display |

**Implementations**:
- `ParallelTrackSearch`
- `ExpandingSquareSearch`
- `WaypointTransit`
- `ReturnToBase`
- `CompositeBehaviour` (special: contains other behaviours)

---

### 3. CompositeBehaviour

**Package**: `com.planetmayo.usvsim.model.mission`

**Purpose**: Sequences multiple behaviours for execution.

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| `behaviours` | `List<Behaviour>` | Ordered list of child behaviours |
| `currentIndex` | `int` | Index of currently executing behaviour |

**Operations**:
- `void addBehaviour(Behaviour b)`: Append to sequence
- `void removeBehaviour(int index)`: Delete behaviour
- `void reorderBehaviour(int from, int to)`: Move behaviour in list
- `Behaviour getCurrentBehaviour()`: Get active behaviour
- Implements `Behaviour` interface: delegates to current child

**Behavior**:
- `getDemandedState()` calls current behaviour's method
- `updateProgress()` updates current behaviour, advances when complete
- `isComplete()` true when all children complete

---

### 4. ParallelTrackSearch

**Package**: `com.planetmayo.usvsim.model.behaviour`

**Purpose**: Systematic search pattern with parallel tracks.

**Fields**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `searchArea` | `Polygon` | Boundary polygon | Minimum 3 vertices, no self-intersection |
| `trackOrientation` | `double` | Track angle in degrees | [0, 360) |
| `trackSpacing` | `double` | Spacing between tracks (metres) | > 0 |
| `platformSpeed` | `double` | Transit speed (knots) | > 0 |
| `waypoints` | `List<Waypoint>` | Generated waypoints | Computed on construction |
| `currentWaypointIndex` | `int` | Current target waypoint | [0, waypoints.size()) |

**State Management**:
- `PENDING`: Not started
- `EXECUTING`: Navigating to waypoints
- `COMPLETE`: All waypoints reached

**Waypoint Generation** (constructor):
1. Transform polygon to track-aligned coordinate system
2. Generate parallel lines at spacing intervals
3. Alternate direction for efficiency (lawn-mower pattern)
4. Clip to polygon boundary (JTS intersection)
5. Convert to Waypoint list

**Progress Calculation**:
- `progress = currentWaypointIndex / waypoints.size()`

---

### 5. ExpandingSquareSearch

**Package**: `com.planetmayo.usvsim.model.behaviour`

**Purpose**: Spiral search pattern from center outward.

**Fields**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `searchArea` | `Polygon` | Boundary polygon | Minimum 3 vertices |
| `centerPoint` | `Position` | Starting point (centroid) | Computed from polygon |
| `initialDirection` | `double` | First leg bearing (degrees) | [0, 360) |
| `legIncrement` | `double` | Distance increase per leg pair (metres) | > 0 |
| `platformSpeed` | `double` | Transit speed (knots) | > 0 |
| `waypoints` | `List<Waypoint>` | Generated waypoints | Computed on construction |
| `currentWaypointIndex` | `int` | Current target waypoint | [0, waypoints.size()) |

**Waypoint Generation** (constructor):
1. Calculate polygon centroid (JTS)
2. Generate expanding square: legs (n, n, n+δ, n+δ, n+2δ, n+2δ, ...)
3. 90° right turns between legs
4. Stop when legs extend beyond polygon
5. Clip to boundary

**Progress Calculation**:
- Same as ParallelTrackSearch

---

### 6. WaypointTransit

**Package**: `com.planetmayo.usvsim.model.behaviour`

**Purpose**: Simple point-to-point navigation.

**Fields**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `userDefinedWaypoints` | `List<Position>` | User-specified points | Minimum 1 point |
| `transitSpeed` | `double` | Speed (knots) | > 0 |
| `currentWaypointIndex` | `int` | Current target | [0, waypoints.size()) |

**Behavior**:
- Navigate sequentially through waypoints
- No pattern generation (user specifies all points)
- Simple and direct

---

### 7. ReturnToBase

**Package**: `com.planetmayo.usvsim.model.behaviour`

**Purpose**: Direct transit to base location.

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| `baseLocation` | `Position` | Target position |
| `transitSpeed` | `double` | Speed (knots) |
| `reachedBase` | `boolean` | Completion flag |

**Behavior**:
- Single waypoint (base location)
- Complete when within acceptance radius

---

## Platform Entities

### 8. Platform

**Package**: `com.planetmayo.usvsim.model.platform`

**Purpose**: Manages USV state and track history.

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` | Platform identifier (e.g., "USV-1") |
| `capabilities` | `PlatformCapabilities` | Static characteristics |
| `state` | `PlatformState` | Current dynamic state |
| `trackHistory` | `List<PlatformState>` | Historical states (position, heading, speed, timestamp) |

**Operations**:
- `void setState(PlatformState newState)`: Update current state, append to history
- `void clearTrackHistory()`: Reset history (on simulation stop)
- `PlatformState getState()`: Get current state
- `List<PlatformState> getTrackHistory()`: Get complete state trail for visualization

---

### 9. PlatformCapabilities

**Package**: `com.planetmayo.usvsim.model.platform`

**Purpose**: Static platform characteristics (configuration).

**Fields**:
| Field | Type | Description | Default | Validation |
|-------|------|-------------|---------|------------|
| `platformType` | `String` | Type identifier | "USV" | Not null |
| `maxSpeed` | `double` | Maximum speed (knots) | 8.0 | > 0 |
| `minSpeed` | `double` | Minimum speed (knots) | 0.0 | >= 0 |
| `maxDepth` | `double` | Max depth (metres) | 0.0 | >= 0 (0 for surface) |
| `turnRadius` | `double` | Turn radius at max speed (metres) | 200.0 | > 0 |
| `acceleration` | `double` | Acceleration limit (m/s²) | 0.5 | > 0 |
| `deceleration` | `double` | Deceleration limit (m/s²) | 1.0 | > 0 |

**Immutability**: Fields final after construction (or use JavaFX ReadOnlyProperties).

---

### 10. PlatformState

**Package**: `com.planetmayo.usvsim.model.platform`

**Purpose**: Current dynamic state of the USV.

**Fields**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `String` | Platform identifier | Matches Platform.id |
| `position` | `Position` | Current lat/lon | Not null |
| `heading` | `double` | Current heading (degrees) | [0, 360) |
| `speed` | `double` | Current speed (knots) | >= 0 |
| `depth` | `double` | Current depth (metres) | >= 0 (0 for surface USV) |
| `timestamp` | `Instant` | State timestamp | Not null |

**JavaFX Properties** (for UI binding):
- Use `SimpleObjectProperty<Position>`, `SimpleDoubleProperty`, etc.
- Enables automatic UI updates via bindings

---

### 11. PlatformDemand

**Package**: `com.planetmayo.usvsim.model.platform`

**Purpose**: Control demands from behaviour to platform.

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| `demandedHeading` | `double` | Desired heading (degrees [0, 360)) |
| `demandedSpeed` | `double` | Desired speed (knots) |
| `demandedDepth` | `double` | Desired depth (metres, 0 for USV) |
| `turnDirection` | `TurnDirection` | Preferred turn (PORT, STARBOARD, SHORTEST) |

**Usage**:
- Behaviour produces PlatformDemand given current state
- SimulationEngine applies demands respecting capabilities (turn radius, acceleration)

---

## Geometry Entities

### 12. Position

**Package**: `com.planetmayo.usvsim.model.geometry`

**Purpose**: Geographic location (immutable).

**Fields**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `latitude` | `double` | Latitude (decimal degrees) | [-90, 90] |
| `longitude` | `double` | Longitude (decimal degrees) | [-180, 180] |

**Operations**:
- `static Position of(double lat, double lon)`: Factory method
- `double distanceTo(Position other)`: Great circle distance (metres)
- `double bearingTo(Position other)`: Initial bearing (degrees)

**Immutability**: Final fields, no setters.

---

### 13. Waypoint

**Package**: `com.planetmayo.usvsim.model.geometry`

**Purpose**: Navigation target with metadata.

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| `position` | `Position` | Target location |
| `speed` | `double` | Target speed at waypoint (knots) |
| `acceptanceRadius` | `double` | Distance threshold for "reached" (metres) |
| `type` | `WaypointType` | TRANSIT, SEARCH, BASE, TURN |

**Default Values**:
- `speed`: Defaults to behaviour's platformSpeed
- `acceptanceRadius`: 50 metres
- `type`: SEARCH for search patterns, TRANSIT otherwise

---

### 14. Polygon

**Package**: `com.planetmayo.usvsim.model.geometry`

**Purpose**: Search area boundary.

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| `vertices` | `List<Position>` | Ordered vertices |

**Validation**:
- Minimum 3 vertices
- No self-intersection (use JTS `isValid()`)
- Vertices ordered (clockwise or counter-clockwise)

**Operations**:
- `boolean contains(Position p)`: Point-in-polygon test (JTS)
- `Position centroid()`: Calculate centroid (JTS)
- `boolean isValid()`: Validate geometry (JTS)

---

## Enumerations

### 15. MissionState

**Package**: `com.planetmayo.usvsim.model.mission`

**Values**:
- `PLANNING`: Mission being created/edited
- `EXECUTING`: Simulation running
- `PAUSED`: Simulation frozen
- `COMPLETE`: All behaviours finished

---

### 16. BehaviourState

**Package**: `com.planetmayo.usvsim.model.behaviour`

**Values**:
- `PENDING`: Not yet started
- `EXECUTING`: Currently active
- `COMPLETE`: Finished

---

### 17. WaypointType

**Package**: `com.planetmayo.usvsim.model.geometry`

**Values**:
- `TRANSIT`: Simple point-to-point navigation
- `SEARCH`: Part of search pattern
- `BASE`: Return-to-base destination
- `TURN`: Intermediate turn waypoint

---

### 18. TurnDirection

**Package**: `com.planetmayo.usvsim.model.platform`

**Values**:
- `PORT`: Turn left
- `STARBOARD`: Turn right
- `SHORTEST`: Take shortest angle

---

## Validation Rules Summary

| Entity | Validation Rule |
|--------|-----------------|
| **Polygon** | >= 3 vertices, no self-intersection |
| **Position** | lat ∈ [-90, 90], lon ∈ [-180, 180] |
| **PlatformCapabilities** | All speeds/distances/accelerations > 0 |
| **Waypoint** | acceptanceRadius > 0 |
| **Behaviour** | trackSpacing > 0, speeds > 0, angles ∈ [0, 360) |

---

## State Transitions

### Mission State Transitions

```
PLANNING → EXECUTING (on start())
EXECUTING → PAUSED (on pause())
EXECUTING → COMPLETE (when all behaviours done)
PAUSED → EXECUTING (on start() again)
* → PLANNING (on stop() - resets)
```

### Behaviour State Transitions

```
PENDING → EXECUTING (when CompositeBehaviour activates it)
EXECUTING → COMPLETE (when isComplete() returns true)
```

---

## Data Flow

```
User Action (UI)
    ↓
MissionController
    ↓
Mission.addBehaviour() / Mission.start()
    ↓
CompositeBehaviour.getCurrentBehaviour()
    ↓
Behaviour.getDemandedState(currentPlatformState)
    ↓
PlatformDemand
    ↓
SimulationEngine (background thread)
    ↓
Apply dynamics (turn radius, acceleration)
    ↓
Update PlatformState
    ↓
Platform.runLater(() -> update UI properties)
    ↓
UI Display (map, panels)
```

---

## Next Steps

1. **contracts/**: Define Behaviour interface contract, SimulationEngine API
2. **quickstart.md**: Setup instructions, build commands, test execution
3. **agent-context update**: Add Java 25, JavaFX 21, JTS to technology list