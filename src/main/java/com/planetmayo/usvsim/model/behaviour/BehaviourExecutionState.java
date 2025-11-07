package com.planetmayo.usvsim.model.behaviour;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable execution state for behaviors in stateless simulation pattern.
 * This record externalizes behavior runtime state to enable stateless REST API.
 *
 * @param behaviorId              Unique identifier linking to parent Behaviour
 * @param currentWaypointIndex    Index of current target waypoint (0-based)
 * @param state                   Current execution state (PENDING/EXECUTING/COMPLETE)
 * @param lastDistanceToWaypoint  Distance to target waypoint at last update (meters)
 */
public record BehaviourExecutionState(
    @JsonProperty("behaviorId") String behaviorId,
    @JsonProperty("currentWaypointIndex") int currentWaypointIndex,
    @JsonProperty("state") BehaviourState state,
    @JsonProperty("lastDistanceToWaypoint") double lastDistanceToWaypoint
) {

    /**
     * Jackson constructor for JSON deserialization.
     */
    @JsonCreator
    public BehaviourExecutionState {
        // Compact constructor - validates parameters
        if (behaviorId == null || behaviorId.isBlank()) {
            throw new IllegalArgumentException("Behavior ID cannot be null or blank");
        }
        if (currentWaypointIndex < 0) {
            throw new IllegalArgumentException("Current waypoint index cannot be negative");
        }
        if (state == null) {
            throw new IllegalArgumentException("Behavior state cannot be null");
        }
        if (lastDistanceToWaypoint < 0) {
            throw new IllegalArgumentException("Last distance to waypoint cannot be negative");
        }
    }

    /**
     * Factory method for initial state when behavior starts.
     *
     * @param behaviorId  Unique identifier for the behavior
     * @return Initial execution state (PENDING, waypoint 0, distance 0)
     */
    public static BehaviourExecutionState initial(String behaviorId) {
        return new BehaviourExecutionState(
            behaviorId,
            0,                      // Start at first waypoint
            BehaviourState.PENDING, // Not yet started
            0.0                     // No distance measured yet
        );
    }

    /**
     * Creates a new state with updated waypoint index.
     *
     * @param newIndex  New waypoint index
     * @return New state with updated index
     */
    public BehaviourExecutionState withWaypointIndex(int newIndex) {
        return new BehaviourExecutionState(
            this.behaviorId,
            newIndex,
            this.state,
            this.lastDistanceToWaypoint
        );
    }

    /**
     * Creates a new state with updated execution state.
     *
     * @param newState  New execution state
     * @return New state with updated state
     */
    public BehaviourExecutionState withState(BehaviourState newState) {
        return new BehaviourExecutionState(
            this.behaviorId,
            this.currentWaypointIndex,
            newState,
            this.lastDistanceToWaypoint
        );
    }

    /**
     * Creates a new state with updated distance.
     *
     * @param distance  Distance to waypoint in meters
     * @return New state with updated distance
     */
    public BehaviourExecutionState withLastDistance(double distance) {
        return new BehaviourExecutionState(
            this.behaviorId,
            this.currentWaypointIndex,
            this.state,
            distance
        );
    }
}
