# Contract: Behaviour Interface

**Package**: `com.planetmayo.usvsim.model.behaviour`
**Type**: Interface
**Purpose**: Core abstraction for all mission activities

## Interface Definition

```java
package com.planetmayo.usvsim.model.behaviour;

import com.planetmayo.usvsim.model.platform.PlatformState;
import com.planetmayo.usvsim.model.platform.PlatformDemand;
import com.planetmayo.usvsim.model.geometry.Waypoint;
import javafx.scene.paint.Color;
import java.util.List;

public interface Behaviour {

    /**
     * Get human-readable name for this behaviour.
     * @return Name (e.g., "Parallel Track Search")
     */
    String getName();

    /**
     * Get description with parameters.
     * @return Description (e.g., "045°, 100m spacing")
     */
    String getDescription();

    /**
     * Get current execution state.
     * @return State (PENDING, EXECUTING, COMPLETE)
     */
    BehaviourState getState();

    /**
     * Get completion progress.
     * @return Progress from 0.0 (not started) to 1.0 (complete)
     */
    double getProgress();

    /**
     * Compute demanded platform state given current state.
     * @param currentState Current platform position/heading/speed
     * @return Demanded heading/speed/turn direction
     */
    PlatformDemand getDemandedState(PlatformState currentState);

    /**
     * Update internal progress based on platform position.
     * @param currentState Current platform state
     */
    void updateProgress(PlatformState currentState);

    /**
     * Check if behaviour is complete.
     * @return true if all waypoints reached or objective met
     */
    boolean isComplete();

    /**
     * Get all waypoints for visualization.
     * @return List of waypoints (may be empty for non-waypoint behaviours)
     */
    List<Waypoint> getWaypoints();

    /**
     * Get display color for map rendering.
     * @return JavaFX Color for route visualization
     */
    Color getDisplayColor();
}
```

## Enum: BehaviourState

```java
package com.planetmayo.usvsim.model.behaviour;

public enum BehaviourState {
    PENDING,    // Not yet started
    EXECUTING,  // Currently active
    COMPLETE    // Finished
}
```

## Contract Obligations

### Implementers MUST:

1. **Initialization**:
   - Generate waypoints in constructor (if applicable)
   - Start with `state = PENDING`
   - Initialize `progress = 0.0`

2. **getDemandedState()**:
   - Return desired heading to next waypoint
   - Return appropriate speed (may reduce for tight turns)
   - Specify turn direction (usually SHORTEST)
   - Must not return null

3. **updateProgress()**:
   - Check distance to current target waypoint
   - Advance waypoint if within acceptance radius
   - Update `progress` based on completion ratio
   - Set `state = COMPLETE` when finished
   - Must be idempotent (safe to call repeatedly)

4. **isComplete()**:
   - Return true when all waypoints reached
   - Must be consistent with `state == COMPLETE`

5. **getWaypoints()**:
   - Return all waypoints for route visualization
   - Empty list OK for non-waypoint behaviours
   - Must not return null

6. **Thread Safety**:
   - Methods called from simulation thread
   - If using JavaFX properties, wrap updates in Platform.runLater()

## Example Implementation Pattern

```java
public class ParallelTrackSearch implements Behaviour {
    private final String name = "Parallel Track Search";
    private final List<Waypoint> waypoints;
    private int currentWaypointIndex = 0;
    private BehaviourState state = BehaviourState.PENDING;

    public ParallelTrackSearch(Polygon area, double orientation, double spacing) {
        this.waypoints = SearchPatternGenerator
            .generateParallelTracks(area, orientation, spacing);
    }

    @Override
    public PlatformDemand getDemandedState(PlatformState current) {
        if (currentWaypointIndex >= waypoints.size()) {
            return PlatformDemand.hold(current); // Stay in place if complete
        }

        Waypoint target = waypoints.get(currentWaypointIndex);
        double bearing = current.getPosition().bearingTo(target.getPosition());
        double speed = target.getSpeed();

        return new PlatformDemand(bearing, speed, 0.0, TurnDirection.SHORTEST);
    }

    @Override
    public void updateProgress(PlatformState current) {
        if (currentWaypointIndex >= waypoints.size()) {
            state = BehaviourState.COMPLETE;
            return;
        }

        state = BehaviourState.EXECUTING;

        Waypoint target = waypoints.get(currentWaypointIndex);
        double distance = current.getPosition().distanceTo(target.getPosition());

        if (distance <= target.getAcceptanceRadius()) {
            currentWaypointIndex++;
            if (currentWaypointIndex >= waypoints.size()) {
                state = BehaviourState.COMPLETE;
            }
        }
    }

    @Override
    public double getProgress() {
        if (waypoints.isEmpty()) return 1.0;
        return (double) currentWaypointIndex / waypoints.size();
    }

    @Override
    public boolean isComplete() {
        return state == BehaviourState.COMPLETE;
    }

    // ... other methods
}
```

## Testing Contract

### Unit Tests MUST Verify:

1. **Waypoint Generation**:
   - Correct number of waypoints
   - Within boundary polygon
   - Alternating direction (for parallel tracks)

2. **State Transitions**:
   - PENDING → EXECUTING on first updateProgress()
   - EXECUTING → COMPLETE when all waypoints reached

3. **Progress Calculation**:
   - Returns 0.0 initially
   - Increases monotonically
   - Reaches 1.0 when complete

4. **Demanded State**:
   - Bearing points toward next waypoint
   - Speed within platform capabilities
   - Consistent across multiple calls (deterministic)

5. **Edge Cases**:
   - Empty waypoint list
   - Single waypoint
   - Platform already at waypoint

## Integration Points

- **SimulationEngine**: Calls `getDemandedState()` and `updateProgress()` each time step
- **CompositeBehaviour**: Checks `isComplete()` to advance to next behaviour
- **MapPanel**: Reads `getWaypoints()` for route visualization
- **MissionPlanPanel**: Displays `getName()`, `getDescription()`, `getState()`, `getProgress()`