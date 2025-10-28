# Contract: SimulationEngine

**Package**: `com.planetmayo.usvsim.controller`
**Type**: Class
**Purpose**: Background thread executing mission simulation with realistic platform dynamics

## Class Definition

```java
package com.planetmayo.usvsim.controller;

import com.planetmayo.usvsim.model.mission.Mission;
import com.planetmayo.usvsim.model.platform.Platform;
import com.planetmayo.usvsim.model.platform.PlatformState;
import com.planetmayo.usvsim.model.platform.PlatformDemand;
import java.util.concurrent.ScheduledExecutorService;

public class SimulationEngine implements Runnable {

    private final Mission mission;
    private final Platform platform;
    private final ScheduledExecutorService executor;
    private double timeAcceleration = 1.0;
    private volatile boolean running = false;

    /**
     * Construct simulation engine.
     * @param mission Mission to execute
     * @param timeStepMs Simulation time step in milliseconds (e.g., 100ms)
     */
    public SimulationEngine(Mission mission, long timeStepMs);

    /**
     * Start simulation execution.
     */
    public void start();

    /**
     * Pause simulation (freeze state).
     */
    public void pause();

    /**
     * Stop simulation (reset to initial state).
     */
    public void stop();

    /**
     * Set time acceleration multiplier.
     * @param acceleration Multiplier (1.0 = real-time, 10.0 = 10× speed)
     */
    public void setTimeAcceleration(double acceleration);

    /**
     * Check if simulation is running.
     * @return true if executing
     */
    public boolean isRunning();

    /**
     * Main simulation loop (called by executor).
     */
    @Override
    public void run();
}
```

## Simulation Loop Algorithm

Each time step (e.g., every 100ms):

```
1. Check if running; if not, return

2. Get current PlatformState from Platform

3. Get active Behaviour from Mission.missionPlan.getCurrentBehaviour()

4. Get PlatformDemand from behaviour.getDemandedState(currentState)

5. Apply platform dynamics constraints:
   a. Compute heading change:
      - Δθ = demandedHeading - currentHeading
      - Normalize to [-180, 180]
      - If |Δθ| > threshold (e.g., 5°):
          headingRate = currentSpeed / turnRadius  (radians/second)
          newHeading = currentHeading + sign(Δθ) × headingRate × Δt
      - Else:
          newHeading = demandedHeading

   b. Compute speed change:
      - Δv = demandedSpeed - currentSpeed
      - If Δv > 0:
          acceleration = min(Δv / Δt, maxAcceleration)
      - Else:
          acceleration = max(Δv / Δt, -maxDeceleration)
      - newSpeed = currentSpeed + acceleration × Δt
      - Clamp to [minSpeed, maxSpeed]

   c. Update position:
      - distance = newSpeed × Δt  (convert knots to m/s first)
      - newPosition = GeoUtils.destination(currentPosition, newHeading, distance)

6. Create new PlatformState(newPosition, newHeading, newSpeed, now())

7. Update behaviour progress:
   behaviour.updateProgress(newState)

8. Check behaviour completion:
   if behaviour.isComplete():
       CompositeBehaviour advances to next behaviour

9. Update Platform state (thread-safe):
   synchronized(platform) {
       platform.setState(newState)
   }

10. Notify UI (JavaFX thread):
    Platform.runLater(() -> {
        // JavaFX properties auto-update views via bindings
    })

11. Check mission completion:
    if mission.isComplete():
        stop()
```

## Contract Obligations

### SimulationEngine MUST:

1. **Threading**:
   - Run on background thread (via ScheduledExecutorService)
   - Never block JavaFX Application Thread
   - Use `Platform.runLater()` for UI updates
   - Synchronize access to shared mutable state (Platform)

2. **Physics Accuracy**:
   - Apply turn radius constraint (can't turn sharper than r = v² / a)
   - Apply acceleration/deceleration limits
   - Use great circle calculations for position updates
   - Maintain consistent time steps

3. **State Management**:
   - Preserve exact state on pause (position, heading, speed, waypoint index)
   - Reset to initial state on stop
   - Handle mission completion gracefully

4. **Performance**:
   - Maintain target update rate (e.g., 10 Hz)
   - Scale gracefully with time acceleration
   - Cap acceleration if system can't keep up

5. **Error Handling**:
   - Catch exceptions in simulation loop
   - Log errors but don't crash
   - Pause simulation on error

## Example Implementation

```java
@Override
public void run() {
    if (!running) return;

    try {
        // 1. Get current state
        PlatformState current = platform.getState();

        // 2-3. Get demand from active behaviour
        Behaviour activeBehaviour = mission.getMissionPlan().getCurrentBehaviour();
        if (activeBehaviour == null || activeBehaviour.isComplete()) {
            stop();
            return;
        }

        PlatformDemand demand = activeBehaviour.getDemandedState(current);

        // 4. Apply dynamics
        double dt = (timeStepMs / 1000.0) * timeAcceleration; // seconds
        PlatformState newState = applyDynamics(current, demand, dt);

        // 5. Update behaviour
        activeBehaviour.updateProgress(newState);

        // 6. Update platform (thread-safe)
        synchronized (platform) {
            platform.setState(newState);
        }

        // 7. Notify UI
        Platform.runLater(() -> {
            // Properties auto-update
        });

        // 8. Check completion
        if (mission.isComplete()) {
            stop();
        }

    } catch (Exception e) {
        logger.error("Simulation error", e);
        pause();
    }
}

private PlatformState applyDynamics(PlatformState current,
                                     PlatformDemand demand,
                                     double dt) {
    // Implement turn radius, acceleration constraints
    // See algorithm above
    ...
}
```

## Configuration

### Time Step Selection:
- **100ms (10 Hz)**: Good balance of accuracy and performance
- **50ms (20 Hz)**: Smoother for high-speed platforms
- **200ms (5 Hz)**: Acceptable for slow platforms

### Time Acceleration Levels:
- 1× (real-time)
- 2×
- 5×
- 10×
- 20× (maximum)

### Turn Radius Calculation:
- r = v / ω  where ω = max angular rate
- Default: r = 200m at 8 knots
- ω ≈ 0.2 rad/s = 11.5°/s

## Testing Contract

### Unit Tests MUST Verify:

1. **State Transitions**:
   - start() sets running = true
   - pause() sets running = false
   - stop() resets to initial state

2. **Dynamics**:
   - Heading changes respect turn radius
   - Speed changes respect acceleration limits
   - Position updates use great circle math

3. **Thread Safety**:
   - Concurrent reads/writes to Platform don't corrupt state
   - No race conditions on pause/stop

4. **Time Acceleration**:
   - 10× acceleration makes simulation run 10× faster
   - Physics remain consistent (scale Δt, not force)

### Integration Tests MUST Verify:

1. **Behaviour Sequencing**:
   - Advances from behaviour 1 → 2 → 3
   - Updates status indicators correctly

2. **Mission Completion**:
   - Stops automatically when all behaviours done
   - Sets mission.state = COMPLETE

3. **UI Updates**:
   - Platform state updates visible in UI
   - No UI freezing during simulation

## Integration Points

- **MissionController**: Calls start(), pause(), stop()
- **ControlPanel**: Sets time acceleration
- **Platform**: Reads/writes PlatformState
- **CompositeBehaviour**: Queries for behaviour sequencing
- **UI Panels**: Observe JavaFX properties for updates