# Web Application Layout Mockup

## Purpose
This mockup shows the proposed web-based React application layout, replicating the desktop JavaFX interface (per FR-008). This layout requires approval before implementation begins.

## Main Application Layout

**Responsive Design**: 70/30 split on desktop, stacks vertically on mobile (< 768px)

```
┌────────────────────────────────────────────────────────────────────────────────┐
│  USV Mission Planner                                          [●] Connected    │
├──────────────────────────────────────┬─────────────────────────────────────────┤
│                                      │ ╔═══════════════════════════════════╗   │
│                                      │ ║   Simulation Control              ║   │
│                                      │ ╚═══════════════════════════════════╝   │
│                                      │                                         │
│                                      │ ┌─────────────────────────────────────┐ │
│                                      │ │ [Start] [Pause] [Stop]              │ │
│         MAP PANEL                    │ │                                     │ │
│      (70% width)                     │ │ Speed: 1× [========○───] 500× 1×   │ │
│                                      │ │                                     │ │
│  ┌────────────────────────────────┐  │ │ Simulation Time: 00:00:00           │ │
│  │                                │  │ └─────────────────────────────────────┘ │
│  │   [Leaflet Map]                │  │                                         │
│  │   Portland Harbour             │  │ ╔═══════════════════════════════════╗   │
│  │   50.6°N, -2.4°W               │  │ ║   Mission Plan                    ║   │
│  │                                │  │ ╚═══════════════════════════════════╝   │
│  │   [OSM Tiles]                  │  │                                         │
│  │   [Zoom +/-]                   │  │ ┌─────────────────────────────────────┐ │
│  │   [Drawing tools]              │  │ │ ▼ Parallel Track Search             │ │
│  │                                │  │ │   Track: 45°, spacing 100m          │ │
│  │   • Search areas (polygons)    │  │ │   Status: PENDING                   │ │
│  │   • Waypoint markers           │  │ ├─────────────────────────────────────┤ │
│  │   • Platform position          │  │ │ ▼ Waypoint Transit                  │ │
│  │   • Track history              │  │ │   5 waypoints at 3.0 m/s            │ │
│  │                                │  │ │   Status: EXECUTING                 │ │
│  │                                │  │ └─────────────────────────────────────┘ │
│  │                                │  │                                         │
│  └────────────────────────────────┘  │ [↑] [↓] [✕]         [⬇ Save] [⬆ Load] │
│                                      │                                         │
│  [Layer controls]                    │ ┌─────────────────────────────────────┐ │
│  [Scale indicator]                   │ │ [+ Add Behaviour ▼] [⚙ Configure]  │ │
│                                      │ └─────────────────────────────────────┘ │
│                                      │                                         │
│                                      │ ╔═══════════════════════════════════╗   │
│                                      │ ║   Platform State                  ║   │
│                                      │ ╚═══════════════════════════════════╝   │
│                                      │                                         │
│                                      │ ┌─────────────────────────────────────┐ │
│                                      │ │ Position:    50.60°N, 2.40°W        │ │
│                                      │ │ Heading:     0°                     │ │
│                                      │ │ Speed:       0 knots                │ │
│                                      │ │ Status:      Ready                  │ │
│                                      │ │ Progress:    0%                     │ │
│                                      │ │ Behaviour:   0 of 0 waypoints      │ │
│                                      │ │ Update Freq: 0 Hz                   │ │
│                                      │ └─────────────────────────────────────┘ │
└──────────────────────────────────────┴─────────────────────────────────────────┘
      70% width                                30% width
```

## Component Details

### Header Bar (Full Width)

```
┌────────────────────────────────────────────────────────────────┐
│  USV Mission Planner                      [●] Connected        │
└────────────────────────────────────────────────────────────────┘
```

**Features**:
- Application title (left)
- Health status indicator (right):
  - Green dot (●) + "Connected" when backend UP
  - Red dot (●) + "Disconnected" when backend DOWN
- Fixed position at top

### Left Panel: Map Display (70%)

```
┌────────────────────────────────────┐
│                                    │
│   [Leaflet Map]                    │
│   Portland Harbour                 │
│   50.6°N, -2.4°W                   │
│                                    │
│   [OSM Tiles]                      │
│   [Zoom controls: +/-]             │
│                                    │
│   Visualizations:                  │
│   • Polygon overlays (search areas)│
│   • Polyline paths (waypoints)     │
│   • Markers (waypoints, platform)  │
│   • Track history (fading trail)   │
│                                    │
│   [Layer controls]                 │
│   [Scale: 1km]                     │
└────────────────────────────────────┘
```

**React Component**: `<MapPanel />`

**Features**:
- Leaflet map with react-leaflet
- OSM tile layer
- Center: [50.6, -2.4] (Portland Harbour)
- Zoom: 12 (default)
- Drawing interactions:
  - Polygon drawing mode (for search areas)
  - Click-to-place waypoints
  - Single point selection (base location)
- Real-time updates during simulation

**Styling**:
- Full height of viewport (minus header)
- Responsive: 70% on desktop, 100% on mobile (stacks above panels)

### Right Panel: Control Stack (30%)

Three vertically stacked panels in this order (top to bottom):

#### 1. Control Panel (Top)

```
╔═══════════════════════════════════╗
║   Simulation Control              ║
╚═══════════════════════════════════╝

┌─────────────────────────────────────┐
│ [Start] [Pause] [Stop]              │
│                                     │
│ Speed: 1× [========○───] 500× 1×   │
│                                     │
│ Simulation Time: 00:00:00           │
└─────────────────────────────────────┘
```

**React Component**: `<ControlPanel />`

**Elements**:
- **Buttons** (Material-UI or similar):
  - Start (primary color, disabled when running)
  - Pause/Resume (toggles text)
  - Stop (secondary color, disabled when not running)
- **Speed slider**:
  - Range: 1-500
  - Shows current value to right
  - Material-UI Slider component
- **Time display**:
  - Format: HH:MM:SS
  - Updates during simulation
  - Monospace font

**Height**: ~150px

#### 2. Mission Plan Panel (Middle, grows to fill space)

```
╔═══════════════════════════════════╗
║   Mission Plan                    ║
╚═══════════════════════════════════╝

┌─────────────────────────────────────┐
│ ▼ Parallel Track Search             │
│   Track: 45°, spacing 100m          │
│   Status: PENDING                   │
├─────────────────────────────────────┤
│ ▼ Waypoint Transit                  │
│   5 waypoints at 3.0 m/s            │
│   Status: EXECUTING                 │
├─────────────────────────────────────┤
│ [No behaviors added]                │
└─────────────────────────────────────┘

[↑] [↓] [✕]         [⬇ Save] [⬆ Load]
```

**React Component**: `<MissionPlanPanel />`

**Elements**:
- **Behavior list**:
  - Expandable/collapsible cards (▼/▶ indicator)
  - Each shows: name, description, status
  - Status color-coded: PENDING/EXECUTING (blue), COMPLETE (green)
  - Empty state: "[No behaviors added]" (gray, italic)
  - Clickable for selection
- **Control buttons**:
  - ↑ ↓ (Reorder): IconButtons
  - ✕ (Delete): IconButton, red color
  - ⬇ (Save): Downloads GeoJSON
  - ⬆ (Load): File upload trigger

**Height**: Flexible (flex-grow: 1)

#### 3. Behavior Selection Bar

```
┌─────────────────────────────────────┐
│ [+ Add Behaviour ▼] [⚙ Configure]  │
└─────────────────────────────────────┘
```

**React Component**: Part of `<MissionPlanPanel />` or separate component

**Elements**:
- **Add Behaviour dropdown** (Material-UI Select):
  - Options: Parallel Track Search, Expanding Square Search, Waypoint Transit, Return to Base
  - Opens behavior-specific dialog
- **Configure button**: Opens platform configuration dialog

**Height**: ~50px

#### 4. State Panel (Bottom)

```
╔═══════════════════════════════════╗
║   Platform State                  ║
╚═══════════════════════════════════╝

┌─────────────────────────────────────┐
│ Position:    50.60°N, 2.40°W        │
│ Heading:     0°                     │
│ Speed:       0 knots                │
│ Status:      Ready                  │
│ Progress:    0%                     │
│ Behaviour:   0 of 0 waypoints      │
│ Update Freq: 0 Hz                   │
└─────────────────────────────────────┘
```

**React Component**: `<StatePanel />`

**Elements**:
- **Platform state fields** (label: value pairs):
  - Position (lat/lon, 2 decimals)
  - Heading (degrees, 0 decimals)
  - Speed (knots, 1 decimal)
- **Simulation state**:
  - Status (text)
  - Progress (percentage, 1 decimal)
  - Behaviour (N of M waypoints)
  - Update Freq (Hz, 1 decimal)

**Layout**: CSS Grid (2 columns, labels bold on left, values on right)

**Height**: ~170px

## Behavior Configuration Dialogs

Modal dialogs appear when user adds a behavior:

### Example: Parallel Track Search Dialog

```
┌─────────────────────────────────────────┐
│  Parallel Track Search Configuration   │
│                                         │
│  Track Orientation (°):   [45      ]   │
│  Track Spacing (m):       [100     ]   │
│  Platform Speed (m/s):    [3.0     ]   │
│                                         │
│  📍 Click on map to draw search area    │
│                                         │
│            [Cancel]  [OK]               │
└─────────────────────────────────────────┘
```

**React Component**: `<ParallelTrackSearchDialog />`

**Features**:
- Material-UI Dialog component
- Input fields for parameters
- Map enters drawing mode when dialog opens
- OK button generates pattern via API call
- Cancel button closes dialog and exits drawing mode

**Other Dialogs**:
- `<ExpandingSquareSearchDialog />` - similar fields
- `<WaypointTransitDialog />` - speed list for waypoints
- `<ReturnToBaseDialog />` - base location + speed

## Connection Lost Modal

When health check fails:

```
┌─────────────────────────────────────┐
│  ⚠️  Connection Lost                 │
│                                     │
│  Cannot connect to backend server.  │
│  Reconnecting...                    │
│                                     │
│  [Retry Now]                        │
└─────────────────────────────────────┘
```

**React Component**: Part of `<App />` or `<ConnectionModal />`

**Behavior**:
- Appears when health check returns error
- Health service polls every 5 seconds
- Dismisses automatically when connection restored
- "Retry Now" button triggers immediate health check

## Responsive Behavior

### Desktop (≥ 768px)
- 70/30 horizontal split
- Fixed layout as shown in mockup

### Mobile (< 768px)
```
┌──────────────────────┐
│  USV Mission Planner │
│  [●] Connected       │
├──────────────────────┤
│                      │
│   [Leaflet Map]      │
│   (Full width)       │
│                      │
├──────────────────────┤
│ Simulation Control   │
│ [Start] [Pause] ...  │
├──────────────────────┤
│ Mission Plan         │
│ [Behavior list]      │
├──────────────────────┤
│ Platform State       │
│ [State fields]       │
└──────────────────────┘
```

- Panels stack vertically
- Map at top (300-400px height)
- Full-width panels below

## Color Scheme & Styling

**Colors**:
- Primary: #2196F3 (Material Blue 500)
- Secondary: #4CAF50 (Material Green 500)
- Error: #F44336 (Material Red 500)
- Background: #FFFFFF (White)
- Borders: #E0E0E0 (Gray 300)
- Text: #212121 (Gray 900)
- Secondary text: #757575 (Gray 600)

**Typography**:
- Font family: 'Roboto', sans-serif (Material-UI default)
- Headings: 14-16px, bold
- Body: 14px, regular
- Monospace (time): 'Courier New', monospace

**Spacing**:
- Panel padding: 16px
- Component spacing: 8-12px
- Border radius: 4px (rounded corners)

**Borders**:
- Panel borders: 1px solid #E0E0E0
- Section dividers: 1px solid #F5F5F5

## Technology Stack

**Map**: Leaflet 1.9+ with react-leaflet
**UI Framework**: Material-UI v5 or similar (buttons, sliders, dialogs)
**State Management**: React Context or useState/useReducer
**HTTP Client**: axios
**TypeScript**: Full type safety with generated types from OpenAPI spec

## File Structure

```
frontend/src/
  ├── components/
  │   ├── MapPanel.tsx            (70% left panel)
  │   ├── ControlPanel.tsx        (Top right)
  │   ├── MissionPlanPanel.tsx    (Middle right)
  │   ├── StatePanel.tsx          (Bottom right)
  │   ├── behaviors/
  │   │   ├── BehaviorSelector.tsx
  │   │   ├── ParallelTrackSearchDialog.tsx
  │   │   ├── ExpandingSquareSearchDialog.tsx
  │   │   ├── WaypointTransitDialog.tsx
  │   │   └── ReturnToBaseDialog.tsx
  │   └── ConnectionModal.tsx
  ├── services/
  │   ├── api.ts                  (Axios instance)
  │   ├── HealthService.ts        (Health checks)
  │   ├── SimulationService.ts    (Tick calls)
  │   ├── BehaviorService.ts      (Pattern generation)
  │   └── MissionService.ts       (Save/load)
  ├── App.tsx                     (Root layout)
  └── types/
      ├── generated.ts            (From OpenAPI)
      └── api.ts                  (Type exports + helpers)
```

## Implementation Notes

**Phase 4 (US1 - Current)**: Basic layout with map + placeholder panels ✅
**Phase 5 (US2)**: Add mission planning (behavior dialogs, drawing, pattern display)
**Phase 6 (US3)**: Add simulation execution (controls, real-time updates, animation)

**This mockup covers**: Complete vision for all phases

## Questions for Approval

1. **Layout**: Is the 70/30 split with vertical right panel stack acceptable?
2. **Responsiveness**: Should mobile users get full-featured UI or limited functionality?
3. **Styling**: Is Material-UI + Leaflet the right choice, or prefer different UI library?
4. **Behavior cards**: Expandable/collapsible or always-expanded list items?
5. **Dialogs**: Modal dialogs for behavior configuration, or inline panel?
6. **Health indicator**: Top-right corner or different location?

## Approval Status

**Status**: PENDING APPROVAL

**Approved by**: ___________
**Date**: ___________
**Notes**: ___________
