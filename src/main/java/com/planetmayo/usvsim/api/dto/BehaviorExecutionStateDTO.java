package com.planetmayo.usvsim.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Behavior execution state for REST API.
 *
 * Immutable DTO matching OpenAPI BehaviorExecutionState schema.
 */
public record BehaviorExecutionStateDTO(
    @JsonProperty("behaviorId") String behaviorId,
    @JsonProperty("currentWaypointIndex") int currentWaypointIndex,
    @JsonProperty("state") String state,  // PENDING, EXECUTING, COMPLETE
    @JsonProperty("lastDistanceToWaypoint") double lastDistanceToWaypoint
) {
    @JsonCreator
    public BehaviorExecutionStateDTO {
        if (behaviorId == null || behaviorId.isBlank()) {
            throw new IllegalArgumentException("behaviorId cannot be null or blank");
        }
        if (currentWaypointIndex < 0) {
            throw new IllegalArgumentException("currentWaypointIndex must be >= 0");
        }
        if (state == null || (!state.equals("PENDING") && !state.equals("EXECUTING") && !state.equals("COMPLETE"))) {
            throw new IllegalArgumentException("state must be PENDING, EXECUTING, or COMPLETE");
        }
    }
}
