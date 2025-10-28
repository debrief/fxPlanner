# Phase 3 UI Mockups - Parallel Track Search (US1)

**Created**: 2025-10-24
**Status**: Awaiting Doc Approval
**Tasks**: T034 (MainView), T035 (ParallelTrackSearchDialog)

---

## T034: MainView Layout Mockup

**Purpose**: Overall application layout with map, mission planning panel, and controls

```
┌────────────────────────────────┬──────────────────────────────────┐
│                                │ ═══ Simulation Control ═══       │
│                                ├──────────────────────────────────┤
│                                │ [Start] [Pause] [Stop]           │
│   MAP VIEW                     │ [Speed: 1×] ▬▬▬▬▬▬▬ [Speed: 10×] │
│   (pan/zoom controls)          ├──────────────────────────────────┤
│   ├── Portland Harbour         │ Mission Plan                      │
│   ├── Search area polygon      │ ┌────────────────────────────────┤
│   ├── Track patterns (blue)    │ │ Behavior 1: Parallel Track     │
│   └── USV icon (when running)  │ │ Status: Ready                  │
│                                │ │ [↑] [↓] [✕]                   │
│                                │ ├────────────────────────────────┤
│                                │ │ [No behaviors added]           │
│                                │ │                                │
│                                │ ├────────────────────────────────┤
│                                │ [+ Add Behaviour ▼]  [Configure] │
│                                ├──────────────────────────────────┤
│                                │ State Panel:                     │
│                                │ Position: 50.60°N, 2.40°W        │
│                                │ Heading: 045° | Speed: 0 knots  │
│                                │ Status: Ready                    │
│                                └──────────────────────────────────┘
└────────────────────────────────┴──────────────────────────────────┘
```

**Layout Details**:
- **Left**: Map panel (70% width) with java_leaflet MapView, pan/zoom controls
- **Right**: Vertical stack (30% width)
  - Simulation Control banner + Start/Pause/Stop buttons + Speed slider (top)
  - Mission Plan panel (behaviors list with edit/delete/reorder controls, middle)
  - Add Behaviour dropdown + Configure button (below Mission Plan)
  - State Panel (platform state display, bottom)
- **Map Features**:
  - Default centered on Portland Harbour (50.6°N, 2.4°W)
  - Drawing overlay for polygon creation (T037)
  - Polygon fill (light blue) + outline (dark blue)
  - Track pattern rendering (blue polylines for parallel tracks)
  - USV icon when simulating (T052)
  - Track history polyline (T052)

---

## T035: Parallel Track Search Dialog Mockup

**Purpose**: Input form for creating parallel track search behavior

```
┌──────────────────────────────────────┐
│ Add Parallel Track Search            │
├──────────────────────────────────────┤
│                                      │
│ Track Orientation (°):               │
│ ┌──────────────────────────────────┐│
│ │ 45        [Draw on map to set]   ││
│ └──────────────────────────────────┘│
│                                      │
│ Track Spacing (metres):              │
│ ┌──────────────────────────────────┐│
│ │ 500                              ││
│ └──────────────────────────────────┘│
│                                      │
│ Platform Speed (knots):              │
│ ┌──────────────────────────────────┐│
│ │ 6.0                              ││
│ └──────────────────────────────────┘│
│                                      │
│ ╔════════════════════════════════════╗│
│ ║ Preview: X tracks, ~Y nm total     ║│
│ ╚════════════════════════════════════╝│
│                                      │
│                 [Cancel]  [OK]       │
└──────────────────────────────────────┘
```

**Dialog Details**:
- **Input Fields**:
  - Orientation: 0-360°, text input (or visual bearing selector)
  - Track Spacing: >0 metres, text input
  - Platform Speed: >0 knots, text input
- **Validation** (real-time):
  - Red border if values invalid
  - Error message below field: "Spacing must be > 0 metres"
  - Disable OK button if any field invalid
- **Preview Box**:
  - Shows estimated number of tracks: "5 tracks, 12.3 nm total distance"
  - Updates as user types
- **Buttons**:
  - Cancel: Close without adding
  - OK: Add behavior to mission, close dialog

---

## Visual Styling Notes

**Colors**:
- Primary: Material Blue (#2196F3)
- Success: Material Green (#4CAF50)
- Error: Material Red (#F44336)
- Background: White (#FFFFFF)
- Text: Dark Gray (#212121)

**Fonts**:
- Labels: 11pt system font
- Dialog title: 14pt bold
- Button text: 11pt

**Behavior Colors** (for map display):
- Parallel Track Search: Blue (#2196F3)
- Expanding Square Search: Green (#4CAF50)
- Waypoint Transit: Orange (#FF9800)
- Return to Base: Red (#F44336)

---

## Approval Checklist

- [ ] MainView layout matches application needs (80/20 split, bottom controls)
- [ ] Mission Plan panel layout clear and functional
- [ ] Dialog is modal, prevents editing map during input
- [ ] Control panel positioned for easy access
- [ ] State panel information relevant and readable
- [ ] Visual hierarchy clear (main map vs. supporting panels)

**Doc's Feedback**: [Awaiting approval]

---

## Implementation Notes (After Approval)

Once approved, implement in this order:
1. **T036**: Create MapPanel with map view (no drawing yet)
2. **T037**: Add DrawingController for polygon capture
3. **T038**: Create MissionPlanPanel with behavior list
4. **T039**: Create ParallelTrackSearchDialog
5. **T040**: Create MainView with layout
6. **T041**: Wire MissionController.addParallelTrackSearch()
7. **T042**: Update Main.java to launch MainView
