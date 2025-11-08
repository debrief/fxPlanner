# Desktop Application Layout Reference

## Purpose
This document serves as a reference for replicating the desktop JavaFX application layout in the web-based React application (FR-008 requirement). The web UI should match the desktop UI layout and interaction patterns.

## Overall Layout Structure

The application uses a **70/30 horizontal split**:
- **Left panel (70% width)**: Map display with drawing overlay
- **Right panel (30% width)**: Vertical stack of control panels

**Window dimensions**: 1000px × 900px

```
┌──────────────────────────────────────────────────────────────────────────┐
│                     USV Mission Planner                                   │
├────────────────────────────────────┬─────────────────────────────────────┤
│                                    │  ═══ Simulation Control ═══         │
│                                    ├─────────────────────────────────────┤
│                                    │  [Start] [Pause] [Stop]             │
│                                    │                                     │
│                                    │  Speed: 1× [========] 500× [1×]    │
│                                    │                                     │
│            MAP PANEL               │  Simulation Time: 00:00:00          │
│         (70% width)                ├─────────────────────────────────────┤
│                                    │  Mission Plan                       │
│    Portland Harbour                │  ┌───────────────────────────────┐ │
│    50.6°N, 2.4°W                   │  │ Parallel Track Search         │ │
│                                    │  │ Pattern: 45°, spacing 100m    │ │
│    [Interactive Map Display]       │  │ Status: PENDING               │ │
│    [Leaflet-based map]             │  ├───────────────────────────────┤ │
│    [Click-to-draw polygons]        │  │ Waypoint Transit              │ │
│    [Waypoint markers]              │  │ 5 waypoints                   │ │
│    [Platform position]             │  │ Status: EXECUTING             │ │
│    [Pattern visualization]         │  └───────────────────────────────┘ │
│                                    │  [↑] [↓] [✕]    [⬇ Save] [⬆ Load] │
│                                    ├─────────────────────────────────────┤
│                                    │  [+ Add Behaviour ▼] [⚙ Configure] │
│                                    ├─────────────────────────────────────┤
│                                    │  State Panel                        │
│                                    │  Position:  50.60°N, 2.40°W        │
│                                    │  Heading:   0°                      │
│                                    │  Speed:     0 knots                 │
│                                    │  Status:    Ready                   │
│                                    │  Progress:  0%                      │
│                                    │  Behaviour: 0 of 0 waypoints       │
│                                    │  Update Freq: 0 Hz                  │
└────────────────────────────────────┴─────────────────────────────────────┘
     70% width                            30% width
```

## Component Breakdown

### 1. Map Panel (Left, 70% width)

**File**: `src/main/java/com/planetmayo/usvsim/view/MapPanel.java`

**Features**:
- Interactive map centered on Portland Harbour (50.6°N, -2.4°W)
- Default zoom level: 12
- Map tiles: OpenStreetMap
- Drawing modes:
  - Polygon drawing (click-to-draw for search areas)
  - Waypoint placement (click-to-place markers)
  - Single point selection (base location)
- Visualizations:
  - Search area polygons (with semi-transparent fill)
  - Waypoint markers (numbered)
  - Waypoint paths (polylines with behavior-specific colors)
  - Platform marker (triangle showing heading direction)
  - Track history (last 100 positions with fade effect)

**Dimensions**: prefWidth = 700px

### 2. Control Panel (Top Right)

**File**: `src/main/java/com/planetmayo/usvsim/view/ControlPanel.java`

**Layout**:
```
═══ Simulation Control ═══

[Start]  [Pause]  [Stop]

Speed: 1×  [========]  500×  [1×]

Simulation Time: 00:00:00
```

**Features**:
- **Buttons**: Start, Pause/Resume, Stop
  - Start: Begins simulation (disabled when running)
  - Pause/Resume: Toggles pause state
  - Stop: Resets simulation to start
  - Button width: 80px each
- **Time acceleration slider**:
  - Range: 1× to 500×
  - Shows tick marks and labels
  - Current value displayed to right of slider
  - Updates simulation speed when released
- **Simulation time display**:
  - Format: HH:MM:SS
  - Updates in real-time during simulation
  - Monospace font (Courier New) for clarity

**Dimensions**: prefHeight = 150px

**State Management**:
- Buttons enable/disable based on simulation state
- Start button only enabled when mission has behaviors
- Pause/Resume button toggles text based on state
- All controls disabled when simulation completes

### 3. Mission Plan Panel (Middle Right, growable)

**File**: `src/main/java/com/planetmayo/usvsim/view/MissionPlanPanel.java`

**Layout**:
```
Mission Plan
┌───────────────────────────────┐
│ Parallel Track Search         │
│ Pattern: 45°, spacing 100m    │
│ Status: PENDING               │
├───────────────────────────────┤
│ Waypoint Transit              │
│ 5 waypoints                   │
│ Status: EXECUTING             │
└───────────────────────────────┘
[↑] [↓] [✕]    [⬇ Save] [⬆ Load]
```

**Features**:
- **Behavior list** (ListView):
  - Shows all behaviors in sequential order
  - Each cell displays:
    - Behavior name (bold)
    - Description (gray, smaller font)
    - Status with color (PENDING=blue, EXECUTING=blue, COMPLETE=green)
  - Empty state: "[No behaviors added]" (italic, gray)
  - Selection support for editing
- **Control buttons**:
  - ↑ (Move Up): Reorder selected behavior up
  - ↓ (Move Down): Reorder selected behavior down
  - ✕ (Delete): Remove selected behavior (red text)
  - ⬇ (Save): Save mission to GeoJSON file
  - ⬆ (Load): Load mission from GeoJSON file
- **Double-click to edit**: Opens configuration dialog for selected behavior

**Dimensions**: prefHeight = 200px, VBox.setVgrow(ALWAYS) for dynamic sizing

### 4. Behavior Selection Bar (Between Mission Plan and State Panel)

**File**: `src/main/java/com/planetmayo/usvsim/view/MainView.java` (lines 82-110)

**Layout**:
```
[+ Add Behaviour ▼] [⚙ Configure]
```

**Features**:
- **Add Behaviour dropdown** (ComboBox):
  - Prompt text: "+ Add Behaviour"
  - Options:
    - Parallel Track Search
    - Expanding Square Search
    - Waypoint Transit
    - Return to Base
  - Width: 180px
  - Selecting an option opens configuration dialog
  - Resets to null after selection (allows re-adding same type)
- **Configure button**:
  - Opens platform configuration dialog
  - Icon: ⚙
  - Width: 100px

### 5. State Panel (Bottom Right)

**File**: `src/main/java/com/planetmayo/usvsim/view/StatePanel.java`

**Layout**:
```
State Panel
Position:     50.60°N, 2.40°W
Heading:      0°
Speed:        0 knots
Status:       Ready
Progress:     0%
Behaviour:    0 of 0 waypoints
Update Freq:  0 Hz
```

**Features**:
- **Real-time platform state**:
  - Position: Latitude/Longitude (2 decimal places)
  - Heading: Degrees (0 decimal places)
  - Speed: Knots (1 decimal place)
- **Simulation status**:
  - Status: Text description (Ready, Executing, Complete, etc.)
  - Progress: Percentage of mission complete (1 decimal place)
  - Behaviour: Current waypoint N of M total waypoints
  - Update Freq: Display refresh rate in Hz (1 decimal place)

**Dimensions**: prefHeight = 150px

**Layout implementation**: GridPane with 2 columns (labels left, values right)

**Additional feature**: Dialog overlay capability (for inline behavior configuration - not currently used)

## Behavior Configuration Dialogs

When user selects a behavior from the dropdown, a modal dialog appears with behavior-specific configuration options.

### Dialog Pattern (All Behaviors)

```
┌─────────────────────────────────┐
│  [Behavior Name]                │
├─────────────────────────────────┤
│  [Configuration Fields]         │
│                                 │
│  [Drawing Instructions]         │
│                                 │
│       [Cancel]  [OK]            │
└─────────────────────────────────┘
```

### Dialog Types

1. **Parallel Track Search**:
   - Track Orientation (0-360°)
   - Track Spacing (meters)
   - Platform Speed (m/s)
   - "Click on map to draw search area polygon"

2. **Expanding Square Search**:
   - Initial Direction (0-360°)
   - Leg Increment (meters)
   - Platform Speed (m/s)
   - "Click on map to draw search area polygon"

3. **Waypoint Transit**:
   - Speed per waypoint (m/s)
   - "Click on map to place waypoints in order"

4. **Return to Base**:
   - Platform Speed (m/s)
   - "Click on map to select base location"

## Color Scheme

**Borders**: #DDD (light gray)
**Background**: White
**Text**: Black (default)
**Secondary text**: #666 (gray), #999 (light gray for empty state)
**Status colors**:
  - PENDING/EXECUTING: #2196F3 (blue)
  - COMPLETE: #4CAF50 (green)
  - Delete button: #F44336 (red)

**Fonts**:
- Default: System default
- Monospace (time display): Courier New
- Bold: Headings and labels

## Interaction Patterns

### Mission Planning Workflow

1. User selects behavior from "+ Add Behaviour" dropdown
2. Configuration dialog appears
3. User enters parameters
4. User draws on map (polygon, waypoints, or point)
5. User clicks OK
6. Behavior added to Mission Plan list
7. Pattern visualized on map
8. Start button becomes enabled

### Simulation Execution Workflow

1. User clicks Start
2. Platform marker appears at first waypoint
3. Platform moves along path with realistic dynamics
4. State Panel updates in real-time (60 FPS target)
5. Time acceleration can be adjusted during simulation
6. User can Pause/Resume or Stop at any time
7. Behaviors transition automatically when complete
8. Mission completes when all behaviors finish

### Save/Load Workflow

1. **Save**: Click ⬇ button → File chooser → Save as GeoJSON
2. **Load**: Click ⬆ button → File chooser → Select GeoJSON → Mission restores

## Technical Implementation Details

**Threading**:
- UI updates on JavaFX Application Thread
- Simulation runs on dedicated SimulationEngine thread
- No blocking operations on UI thread

**Map Implementation**:
- Uses JXMapViewer2 library (Swing-based, embedded in JavaFX)
- OpenStreetMap tile provider
- Custom overlays for geometry rendering

**Layout Manager**: BorderPane (root) with VBox for right panel

**Event Handling**: Callbacks and handlers wired in MainView and MissionController

**Window Dimensions**: 1000px × 900px (from Scene creation in MainView:178)

## Web Replication Requirements (FR-008)

The web-based React application should replicate:

1. **Layout structure**: 70/30 split with identical panel placement
2. **Control placement**: Same button positions and slider layout
3. **Panel content**: Same fields and information display
4. **Interaction patterns**: Same workflow for planning and execution
5. **Visual consistency**: Similar colors, fonts, and spacing
6. **Behavior dialogs**: Same configuration fields and drawing instructions

**Key differences allowed**:
- Map library: Leaflet (web) vs JXMapViewer2 (desktop)
- UI framework: React (web) vs JavaFX (desktop)
- File handling: Browser download/upload vs native file chooser

**Not required to match**:
- Exact pixel dimensions (responsive design acceptable)
- Native OS styling (web uses consistent cross-platform styling)
- Font rendering (browser fonts vs system fonts)

## Reference Files

**Desktop Views**:
- `src/main/java/com/planetmayo/usvsim/view/MainView.java` - Overall layout
- `src/main/java/com/planetmayo/usvsim/view/MapPanel.java` - Map display
- `src/main/java/com/planetmayo/usvsim/view/ControlPanel.java` - Simulation controls
- `src/main/java/com/planetmayo/usvsim/view/MissionPlanPanel.java` - Behavior list
- `src/main/java/com/planetmayo/usvsim/view/StatePanel.java` - Platform state

**Web Replication Targets**:
- `frontend/src/App.tsx` - Overall layout
- `frontend/src/components/MapPanel.tsx` - Map with Leaflet
- `frontend/src/components/ControlPanel.tsx` - Simulation controls
- `frontend/src/components/MissionPlanPanel.tsx` - Behavior list
- `frontend/src/components/StatePanel.tsx` - Platform state
