# Data Model: Web-Based Delivery Option

**Generated**: 2025-11-05 | **Feature**: 003-web-delivery

## Core Entities

### 1. Mission

**Description**: Container for mission metadata and collection of behaviors forming mission plan

**Attributes**:
- `id`: string (UUID)
- `name`: string (max 100 chars)
- `description`: string (optional, max 500 chars)
- `createdAt`: ISO 8601 timestamp
- `behaviors`: array of Behavior
- `state`: MissionState enum

**Validation Rules**:
- Name is required and non-empty
- At least one behavior required for valid mission
- Behaviors execute sequentially in array order

**State Transitions**:
- `PLANNING` → `EXECUTING` (when simulation starts)
- `EXECUTING` → `PAUSED` (user pauses)
- `PAUSED` → `EXECUTING` (user resumes)
- `EXECUTING` → `COMPLETE` (all behaviors complete)

### 2. Behavior

**Description**: Abstract interface for mission activities (ParallelTrackSearch, ExpandingSquareSearch, WaypointTransit, ReturnToBase)

**Common Attributes**:
- `id`: string (UUID)
- `type`: BehaviorType enum
- `name`: string
- `description`: string
- `waypoints`: array of Waypoint
- `displayColor`: string (hex color)

**Type-Specific Attributes**:

#### ParallelTrackSearch
- `searchArea`: Polygon
- `trackOrientation`: number (0-360 degrees)
- `trackSpacing`: number (meters)
- `platformSpeed`: number (m/s)

#### ExpandingSquareSearch
- `searchArea`: Polygon
- `initialDirection`: number (0-360 degrees)
- `legIncrement`: number (meters)
- `platformSpeed`: number (m/s)

#### WaypointTransit
- `transitWaypoints`: array of Position
- `platformSpeed`: number (m/s)

#### ReturnToBase
- `baseLocation`: Position
- `platformSpeed`: number (m/s)

**Validation Rules**:
- Type must match one of four supported behaviors
- Search behaviors require valid polygon (min 3 vertices)
- Speed must be positive and ≤ max platform speed (8 knots)
- Orientation/direction in range [0, 360)

### 3. BehaviorExecutionState

**Description**: Runtime state for behavior execution (stateless pattern)

**Attributes**:
- `behaviorId`: string (references Behavior)
- `currentWaypointIndex`: number (0-based)
- `state`: BehaviorState enum
- `lastDistanceToWaypoint`: number (meters)
- `startTime`: ISO 8601 timestamp
- `waypointsReached`: number

**Validation Rules**:
- currentWaypointIndex ≥ 0
- currentWaypointIndex ≤ total waypoints in behavior
- State transitions follow: PENDING → EXECUTING → COMPLETE

**State Transitions**:
- `PENDING` → `EXECUTING` (simulation starts behavior)
- `EXECUTING` → `EXECUTING` (waypoint reached, more remain)
- `EXECUTING` → `COMPLETE` (final waypoint reached)

### 4. Platform

**Description**: USV representation with capabilities and current state

**Attributes**:
- `id`: string
- `name`: string
- `capabilities`: PlatformCapabilities
- `currentState`: PlatformState
- `trackHistory`: array of PlatformState

**Validation Rules**:
- Capabilities define operational limits
- Current state must respect capability constraints

### 5. PlatformCapabilities

**Description**: Performance limits and characteristics

**Attributes**:
- `maxSpeed`: number (m/s, default 8 knots = 4.12 m/s)
- `turnRadius`: number (meters, default 200)
- `acceleration`: number (m/s², default 0.5)
- `deceleration`: number (m/s², default 1.0)

**Validation Rules**:
- All values must be positive
- Deceleration typically > acceleration

### 6. PlatformState

**Description**: Current platform position and motion

**Attributes**:
- `position`: Position
- `heading`: number (0-360 degrees)
- `speed`: number (m/s)
- `depth`: number (meters, 0 for surface)
- `timestamp`: ISO 8601 timestamp

**Validation Rules**:
- Heading in range [0, 360)
- Speed ≥ 0 and ≤ maxSpeed
- Position within operational area bounds

### 7. PlatformDemand

**Description**: Commanded state from behavior to platform

**Attributes**:
- `demandedHeading`: number (0-360 degrees)
- `demandedSpeed`: number (m/s)
- `demandedDepth`: number (meters)
- `turnDirection`: TurnDirection enum

**Validation Rules**:
- Demanded values within platform capabilities
- Turn direction affects path when angle > 180°

### 8. Position

**Description**: Geographic coordinate

**Attributes**:
- `latitude`: number (decimal degrees)
- `longitude`: number (decimal degrees)

**Validation Rules**:
- Latitude in range [-90, 90]
- Longitude in range [-180, 180]
- Portland Harbour area: ~50.6°N, -2.4°W

**Methods** (computed):
- `distanceTo(other: Position)`: number (meters, great circle)
- `bearingTo(other: Position)`: number (degrees)

### 9. Waypoint

**Description**: Navigation target

**Attributes**:
- `position`: Position
- `speed`: number (m/s)
- `tolerance`: number (meters, acceptance radius)
- `type`: WaypointType enum

**Validation Rules**:
- Tolerance > 0 (typically 50m)
- Speed within platform capabilities

### 10. Polygon

**Description**: Search area boundary

**Attributes**:
- `vertices`: array of Position (min 3)
- `isClosed`: boolean (last vertex connects to first)

**Validation Rules**:
- Minimum 3 vertices for valid polygon
- No self-intersections (validated via JTS)
- Vertices in order (clockwise or counter-clockwise)

### 11. SimulationTickRequest

**Description**: Frontend→Backend request for simulation step

**Attributes**:
- `mission`: Mission
- `platformState`: PlatformState
- `behaviorState`: BehaviorExecutionState
- `deltaTime`: number (seconds)
- `timeAcceleration`: number (1-500x)

**Validation Rules**:
- deltaTime > 0 (typically 0.1s)
- timeAcceleration ≥ 1

### 12. SimulationTickResponse

**Description**: Backend→Frontend response with next state

**Attributes**:
- `platformState`: PlatformState (updated)
- `behaviorState`: BehaviorExecutionState (updated)
- `demand`: PlatformDemand (for visualization)
- `behaviorComplete`: boolean
- `missionComplete`: boolean

**Validation Rules**:
- State changes reflect deltaTime progression
- Platform dynamics constraints applied

## Enumerations

### MissionState
```
PLANNING    // Creating/editing behaviors
EXECUTING   // Simulation running
PAUSED      // Simulation temporarily halted
COMPLETE    // All behaviors finished
```

### BehaviorType
```
PARALLEL_TRACK_SEARCH
EXPANDING_SQUARE_SEARCH
WAYPOINT_TRANSIT
RETURN_TO_BASE
```

### BehaviorState
```
PENDING     // Not yet started
EXECUTING   // Currently active
COMPLETE    // Finished
```

### WaypointType
```
TRANSIT     // Navigation waypoint
SEARCH      // Search pattern point
BASE        // Home/return location
```

### TurnDirection
```
PORT        // Left turn
STARBOARD   // Right turn
SHORTEST    // Optimal direction
```

## Relationships

```
Mission
  └── 1..* Behavior
        └── 1..* Waypoint
              └── 1 Position

Platform
  ├── 1 PlatformCapabilities
  ├── 1 PlatformState (current)
  │     └── 1 Position
  └── 0..* PlatformState (track history)

SimulationTickRequest
  ├── 1 Mission
  ├── 1 PlatformState
  └── 1 BehaviorExecutionState

SimulationTickResponse
  ├── 1 PlatformState
  ├── 1 BehaviorExecutionState
  └── 1 PlatformDemand
```

## Data Persistence

### GeoJSON Format (Mission Files)

```json
{
  "type": "FeatureCollection",
  "properties": {
    "mission": {
      "id": "uuid",
      "name": "Portland Harbour Survey",
      "createdAt": "2025-11-05T10:00:00Z",
      "state": "PLANNING"
    }
  },
  "features": [
    {
      "type": "Feature",
      "geometry": {
        "type": "Polygon",
        "coordinates": [[[-2.4, 50.6], [-2.3, 50.6], [-2.3, 50.5], [-2.4, 50.5]]]
      },
      "properties": {
        "behavior": {
          "type": "PARALLEL_TRACK_SEARCH",
          "name": "Survey Area 1",
          "trackOrientation": 45,
          "trackSpacing": 100,
          "platformSpeed": 3.0
        }
      }
    }
  ]
}
```

## Constraints

### Performance
- Mission size: Up to 10 behaviors
- Waypoints per behavior: Up to 1000
- Total waypoints per mission: Up to 10,000
- Platform track history: Last 1000 points
- Polygon complexity: Up to 100 vertices

### Validation
- All positions within operational area
- No duplicate behavior IDs within mission
- Chronological timestamps in track history
- Consistent units (meters, degrees, seconds)