# Behavior Configuration Dialog Mockups

ASCII mockups for 4 behavior configuration dialogs (T066a).

---

## 1. Parallel Track Search Dialog

```
┌─────────────────────────────────────────────────┐
│ Configure Parallel Track Search            [X] │
├─────────────────────────────────────────────────┤
│                                                 │
│  Track Orientation (°):  [    45    ] 0-360    │
│                                                 │
│  Track Spacing (m):      [   100    ]          │
│                                                 │
│  Platform Speed (m/s):   [   3.0    ]          │
│                                                 │
│  ┌────────────────────────────────────────┐    │
│  │  [📍 Draw Search Area]                 │    │
│  │                                        │    │
│  │  Click map to define polygon vertices │    │
│  │  Status: Not defined                   │    │
│  └────────────────────────────────────────┘    │
│                                                 │
│              [Cancel]  [Generate Pattern]       │
└─────────────────────────────────────────────────┘
```

**Interaction Flow**:
1. User enters parameters
2. Clicks "Draw Search Area" → map enters polygon drawing mode
3. User clicks map to place vertices
4. After 3+ vertices, "Generate Pattern" enables
5. Click generates waypoints via POST /api/behaviors/parallel-track/generate

---

## 2. Expanding Square Search Dialog

```
┌─────────────────────────────────────────────────┐
│ Configure Expanding Square Search          [X] │
├─────────────────────────────────────────────────┤
│                                                 │
│  Initial Direction (°):  [    90    ] 0-360    │
│                                                 │
│  Leg Increment (m):      [    50    ]          │
│                                                 │
│  Platform Speed (m/s):   [   3.0    ]          │
│                                                 │
│  ┌────────────────────────────────────────┐    │
│  │  [📍 Draw Search Area]                 │    │
│  │                                        │    │
│  │  Click map to define polygon vertices │    │
│  │  Status: Not defined                   │    │
│  └────────────────────────────────────────┘    │
│                                                 │
│              [Cancel]  [Generate Pattern]       │
└─────────────────────────────────────────────────┘
```

**Interaction Flow**: Same as Parallel Track

---

## 3. Waypoint Transit Dialog

```
┌─────────────────────────────────────────────────┐
│ Configure Waypoint Transit                 [X] │
├─────────────────────────────────────────────────┤
│                                                 │
│  [📍 Add Waypoints on Map]                     │
│                                                 │
│  ┌────────────────────────────────────────┐    │
│  │ Waypoints:                             │    │
│  │                                        │    │
│  │  1. 50.6000°N, 2.4000°W  [3.0 m/s] [×]│    │
│  │  2. 50.6050°N, 2.3950°W  [3.0 m/s] [×]│    │
│  │  3. 50.6100°N, 2.3900°W  [3.0 m/s] [×]│    │
│  │                                        │    │
│  │  (No waypoints yet)                    │    │
│  └────────────────────────────────────────┘    │
│                                                 │
│  Click map to place numbered waypoint markers  │
│                                                 │
│              [Cancel]  [Add to Mission]         │
└─────────────────────────────────────────────────┘
```

**Interaction Flow**:
1. Click "Add Waypoints" → map enters waypoint placement mode
2. Click map to place markers (numbered 1, 2, 3...)
3. Each waypoint appears in list with editable speed
4. Click [×] to remove waypoint
5. "Add to Mission" creates WaypointTransit behavior

---

## 4. Return To Base Dialog

```
┌─────────────────────────────────────────────────┐
│ Configure Return To Base                   [X] │
├─────────────────────────────────────────────────┤
│                                                 │
│  [📍 Select Base Location on Map]              │
│                                                 │
│  ┌────────────────────────────────────────┐    │
│  │  Base Location:                        │    │
│  │                                        │    │
│  │  Not selected                          │    │
│  │                                        │    │
│  │  (After selection: 50.6000°N, 2.4000°W)│    │
│  └────────────────────────────────────────┘    │
│                                                 │
│  Platform Speed (m/s):   [   3.0    ]          │
│                                                 │
│  Click map to place base marker                │
│                                                 │
│              [Cancel]  [Add to Mission]         │
└─────────────────────────────────────────────────┘
```

**Interaction Flow**:
1. Click "Select Base Location" → map enters single-point selection mode
2. Click map to place base marker (🏠 icon)
3. Location displays in dialog
4. "Add to Mission" creates ReturnToBase behavior with single waypoint

---

## Common Design Elements

**All dialogs**:
- Modal overlays on main UI
- Close button [X] top right
- Cancel button (discards changes)
- Primary action button (right-aligned, blue/enabled when valid)
- Input validation (red border on invalid, disabled submit)
- Map interaction feedback (cursor changes, hover states)

**Field Formats**:
- Angles: 0-360° integer input with validation
- Distances: positive integers/floats with unit labels
- Speeds: 0.1-8.0 m/s (USV max speed constraint)
- Coordinates: decimal degrees (6 decimal places)

**Status Indicators**:
- "Not defined" (gray) → "3 vertices" (blue) → "Valid polygon" (green)
- Button states: Disabled (gray) → Enabled (blue) → Loading (spinner)
