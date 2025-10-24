package com.planetmayo.usvsim.controller;

import com.planetmayo.usvsim.model.behaviour.Behaviour;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.mission.CompositeBehaviour;
import com.planetmayo.usvsim.model.mission.Mission;
import com.planetmayo.usvsim.model.platform.Platform;
import com.planetmayo.usvsim.model.platform.PlatformCapabilities;
import com.planetmayo.usvsim.model.platform.PlatformDemand;
import com.planetmayo.usvsim.model.platform.PlatformState;

import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simulation engine running on dedicated background thread.
 *
 * Responsibilities:
 * - Execute mission behaviours sequentially
 * - Apply platform dynamics (turn radius, acceleration, speed limits)
 * - Update platform state at configurable frequency (1Hz minimum)
 * - Support pause/resume and time acceleration
 * - Post UI updates via Platform.runLater() to avoid blocking JavaFX thread
 *
 * Architecture:
 * - ScheduledExecutorService runs time-stepped loop every 100ms (~10 Hz internally)
 * - Time step scaled by acceleration factor (1× = real-time, 10× = 10 times faster)
 * - Platform state updated continuously
 * - Behaviours polled for demanded state and progress
 */
public class SimulationEngine {
    private static final long STEP_INTERVAL_MS = 100;  // 10 Hz internal tick
    private static final double KNOTS_TO_MS = 0.51444;  // Conversion factor

    private final Mission mission;
    private final ScheduledExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);

    private double timeAcceleration = 1.0;  // 1× = real-time, 10× = 10× faster
    private long lastStepTimeMs;
    private Runnable onStateChanged;
    private Runnable onSimulationComplete;

    public SimulationEngine(Mission mission) {
        this.mission = mission;
        this.executor = new ScheduledThreadPoolExecutor(1, runnable -> {
            Thread thread = new Thread(runnable, "SimulationEngine");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Start the simulation
     */
    public void start() {
        if (running.get()) {
            System.out.println("Simulation already running");
            return;
        }

        running.set(true);
        paused.set(false);
        mission.start();
        lastStepTimeMs = System.currentTimeMillis();

        System.out.println("Starting simulation");

        // Schedule the time-stepped loop
        executor.scheduleAtFixedRate(this::simulationStep, 0, STEP_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Pause the simulation (maintains state)
     */
    public void pause() {
        if (!running.get()) {
            System.out.println("Simulation not running");
            return;
        }

        paused.set(true);
        mission.pause();
        System.out.println("Simulation paused");
    }

    /**
     * Resume the simulation after pause
     */
    public void resume() {
        if (!running.get() || !paused.get()) {
            System.out.println("Cannot resume: not paused");
            return;
        }

        paused.set(false);
        mission.resume();
        lastStepTimeMs = System.currentTimeMillis();
        System.out.println("Simulation resumed");
    }

    /**
     * Stop the simulation and reset
     */
    public void stop() {
        if (!running.get()) {
            System.out.println("Simulation not running");
            return;
        }

        running.set(false);
        paused.set(false);
        mission.stop();

        System.out.println("Simulation stopped");
        // Note: executor tasks will naturally end when running.get() returns false
    }

    /**
     * Set time acceleration factor (1× = real-time, 10× = 10× faster)
     */
    public void setTimeAcceleration(double factor) {
        if (factor < 1.0 || factor > 100.0) {
            System.err.println("Time acceleration must be in [1, 100]");
            return;
        }
        this.timeAcceleration = factor;
        System.out.println("Time acceleration set to " + factor + "×");
    }

    /**
     * Set callback for when state changes
     */
    public void setOnStateChanged(Runnable callback) {
        this.onStateChanged = callback;
    }

    /**
     * Set callback for when simulation completes
     */
    public void setOnSimulationComplete(Runnable callback) {
        this.onSimulationComplete = callback;
    }

    /**
     * Main simulation loop (runs on background thread)
     */
    private void simulationStep() {
        if (!running.get()) {
            return;
        }

        if (paused.get()) {
            lastStepTimeMs = System.currentTimeMillis();
            return;
        }

        try {
            // Calculate actual time step accounting for acceleration
            long nowMs = System.currentTimeMillis();
            double dtSeconds = ((nowMs - lastStepTimeMs) / 1000.0) * timeAcceleration;
            lastStepTimeMs = nowMs;

            Platform platform = mission.getPlatform();
            CompositeBehaviour missionPlan = mission.getMissionPlan();
            Behaviour currentBehaviour = missionPlan.getCurrentBehaviour();

            if (currentBehaviour == null) {
                // No behaviour to execute, mission complete
                running.set(false);
                mission.stop();
                notifySimulationComplete();
                return;
            }

            // Get demanded state from current behaviour
            PlatformDemand demand = currentBehaviour.getDemandedState(platform.getState());

            // Apply platform dynamics (using getters from PlatformDemand)
            PlatformState newState = applyDynamics(
                platform.getState(),
                demand,
                platform.getCapabilities(),
                dtSeconds
            );

            // Update platform state
            platform.setState(newState);

            // Update behaviour progress (automatically advances when complete)
            missionPlan.updateProgress(newState);

            // Notify UI of state change
            notifyStateChanged();

            // Check if mission is complete
            if (missionPlan.isComplete()) {
                System.out.println("Mission complete");
                running.set(false);
                mission.stop();
                notifySimulationComplete();
            }

        } catch (Exception e) {
            System.err.println("Error in simulation step: " + e.getMessage());
            e.printStackTrace();
            running.set(false);
        }
    }

    /**
     * Apply platform dynamics to compute new state from current state + demand
     *
     * Handles:
     * - Speed acceleration/deceleration with limits
     * - Turn radius and turn rate calculations
     * - Great circle position update
     * - Heading normalization
     */
    private PlatformState applyDynamics(
        PlatformState current,
        PlatformDemand demand,
        PlatformCapabilities caps,
        double dtSeconds
    ) {
        // 1. Calculate new speed (with acceleration limits)
        double currentSpeedKnots = current.getSpeed();
        double demandedSpeedKnots = demand.getDemandedSpeed();

        // Cap demand to capabilities
        demandedSpeedKnots = Math.min(demandedSpeedKnots, caps.getMaxSpeed());
        demandedSpeedKnots = Math.max(demandedSpeedKnots, 0.0);

        // Apply acceleration limits
        double maxSpeedChangeKnots;
        if (demandedSpeedKnots > currentSpeedKnots) {
            // Accelerating
            double accelMs = caps.getAcceleration();  // m/s²
            double accelKnots = (accelMs * KNOTS_TO_MS) * dtSeconds;
            maxSpeedChangeKnots = accelKnots;
        } else {
            // Decelerating
            double decelMs = caps.getDeceleration();  // m/s²
            double decelKnots = (decelMs * KNOTS_TO_MS) * dtSeconds;
            maxSpeedChangeKnots = -decelKnots;
        }

        double newSpeed = Math.min(
            currentSpeedKnots + maxSpeedChangeKnots,
            demandedSpeedKnots
        );
        newSpeed = Math.max(newSpeed, 0.0);

        // 2. Calculate new heading (with turn radius limits)
        double currentHeading = current.getHeading();
        double demandedHeading = demand.getDemandedHeading();
        double turnRadius = caps.getTurnRadius() * (newSpeed / caps.getMaxSpeed());

        // Calculate max heading change per second
        double speedMs = newSpeed * KNOTS_TO_MS;
        double maxHeadingChangePerSecond = 0.0;
        if (turnRadius > 0) {
            // Angular velocity = speed / radius
            double angularVelocityRad = speedMs / turnRadius;
            maxHeadingChangePerSecond = Math.toDegrees(angularVelocityRad);
        }

        // Apply turn direction preference
        double maxHeadingChange = maxHeadingChangePerSecond * dtSeconds;
        double headingDifference = normalizeHeadingDelta(demandedHeading - currentHeading);

        double newHeading;
        if (Math.abs(headingDifference) <= maxHeadingChange) {
            // Can reach demanded heading in this step
            newHeading = demandedHeading;
        } else {
            // Limited by turn rate
            if (headingDifference > 0) {
                newHeading = currentHeading + maxHeadingChange;
            } else {
                newHeading = currentHeading - maxHeadingChange;
            }
        }

        newHeading = normalizeHeading(newHeading);

        // 3. Calculate new position (great circle navigation)
        double distanceMeters = speedMs * dtSeconds;
        Position newPosition = current.getPosition().destination(distanceMeters, newHeading);

        // 4. Create new state
        return new PlatformState(
            current.getId(),
            newPosition,
            newHeading,
            newSpeed,
            current.getDepth(),  // Depth unchanged (surface USV)
            Instant.now()
        );
    }

    /**
     * Normalize heading to [0, 360)
     */
    private double normalizeHeading(double heading) {
        return ((heading % 360) + 360) % 360;
    }

    /**
     * Calculate shortest heading difference (-180 to +180)
     */
    private double normalizeHeadingDelta(double delta) {
        delta = delta % 360;
        if (delta > 180) {
            delta -= 360;
        } else if (delta < -180) {
            delta += 360;
        }
        return delta;
    }

    /**
     * Notify UI of state change (posts to JavaFX thread)
     */
    private void notifyStateChanged() {
        if (onStateChanged != null) {
            // Use Platform.runLater to post update to UI thread
            // (Mock implementation here, real version would use javafx.application.Platform)
            try {
                onStateChanged.run();
            } catch (Exception e) {
                System.err.println("Error in state change callback: " + e.getMessage());
            }
        }
    }

    /**
     * Notify that simulation is complete
     */
    private void notifySimulationComplete() {
        if (onSimulationComplete != null) {
            try {
                onSimulationComplete.run();
            } catch (Exception e) {
                System.err.println("Error in completion callback: " + e.getMessage());
            }
        }
    }

    /**
     * Check if simulation is currently running
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Check if simulation is paused
     */
    public boolean isPaused() {
        return paused.get();
    }

    /**
     * Shutdown the engine (cleanup thread pool)
     */
    public void shutdown() {
        stop();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
