package com.planetmayo.usvsim.model.behaviour;

import com.planetmayo.usvsim.model.geometry.Waypoint;
import com.planetmayo.usvsim.model.platform.PlatformDemand;
import com.planetmayo.usvsim.model.platform.PlatformState;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Core interface for all mission activities/behaviours.
 *
 * All mission activities (search patterns, waypoint transit, return to base)
 * implement this interface for consistent control and sequencing.
 */
public interface Behaviour {

    /**
     * Gets human-readable name.
     *
     * @return name e.g. "Parallel Track Search"
     */
    String getName();

    /**
     * Gets parameter summary for display.
     *
     * @return description e.g. "045°, 100m spacing"
     */
    String getDescription();

    /**
     * Gets current state.
     *
     * @return one of PENDING, EXECUTING, COMPLETE
     */
    BehaviourState getState();

    /**
     * Gets completion progress.
     *
     * @return value [0.0, 1.0]
     */
    double getProgress();

    /**
     * Computes desired heading/speed given current platform state.
     *
     * Called each simulation timestep to generate control demands.
     *
     * @param currentState current platform state
     * @return control demand (heading, speed, depth, turn direction)
     */
    PlatformDemand getDemandedState(PlatformState currentState);

    /**
     * Updates behaviour state based on platform progress.
     *
     * Called each simulation timestep after platform moves. Increments
     * waypoint index if platform reached current waypoint.
     *
     * @param currentState current platform state
     */
    void updateProgress(PlatformState currentState);

    /**
     * Checks if behaviour is complete.
     *
     * @return true if all waypoints reached
     */
    boolean isComplete();

    /**
     * Gets all waypoints for visualization on map.
     *
     * @return list of waypoints (may be empty before generation)
     */
    List<Waypoint> getWaypoints();

    /**
     * Gets colour for map display.
     *
     * Used to distinguish behaviour types on map visualization.
     *
     * @return JavaFX Color
     */
    Color getDisplayColor();

    // ===================================================================
    // STATELESS INTERFACE METHODS (for REST API / Web Frontend)
    // ===================================================================

    /**
     * Calculates platform demand given external execution state (stateless).
     *
     * Pure function that computes control demands without modifying internal state.
     * Used by stateless REST API backend for web frontend.
     *
     * @param executionState Current behavior execution state (waypoint index, progress)
     * @param platformState  Current platform state (position, heading, speed)
     * @return Control demand (heading, speed, depth, turn direction)
     */
    PlatformDemand calculateDemand(BehaviourExecutionState executionState, PlatformState platformState);

    /**
     * Updates execution state based on platform progress (stateless).
     *
     * Pure function that returns new state without modifying input.
     * Increments waypoint index if platform reached current waypoint.
     * Used by stateless REST API backend for web frontend.
     *
     * @param executionState Current behavior execution state
     * @param platformState  Current platform state
     * @return Updated execution state (may have incremented waypoint index or changed state)
     */
    BehaviourExecutionState updateProgress(BehaviourExecutionState executionState, PlatformState platformState);

    /**
     * Checks if behavior is complete given external execution state (stateless).
     *
     * Pure function that determines completion without accessing internal state.
     * Used by stateless REST API backend for web frontend.
     *
     * @param executionState Current behavior execution state
     * @return true if all waypoints reached or behavior complete
     */
    boolean isComplete(BehaviourExecutionState executionState);
}
