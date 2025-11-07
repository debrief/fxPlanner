package com.planetmayo.usvsim.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request for single simulation tick execution.
 *
 * Stateless simulation: client sends current state, server returns next state.
 * No server-side state storage between ticks.
 */
public record SimulationTickRequest(
    @JsonProperty("behaviorType") String behaviorType,  // WAYPOINT_TRANSIT, PARALLEL_TRACK, EXPANDING_SQUARE, RETURN_TO_BASE
    @JsonProperty("behaviorConfig") String behaviorConfig,  // JSON string of behavior-specific config
    @JsonProperty("platformState") PlatformStateDTO platformState,
    @JsonProperty("behaviorState") BehaviorExecutionStateDTO behaviorState,
    @JsonProperty("deltaTime") double deltaTime,  // seconds
    @JsonProperty("timeAcceleration") double timeAcceleration  // 1.0 = real-time, up to 500.0
) {
    @JsonCreator
    public SimulationTickRequest {
        if (behaviorType == null || behaviorType.isBlank()) {
            throw new IllegalArgumentException("behaviorType cannot be null or blank");
        }
        if (platformState == null) {
            throw new IllegalArgumentException("platformState cannot be null");
        }
        if (behaviorState == null) {
            throw new IllegalArgumentException("behaviorState cannot be null");
        }
        if (deltaTime <= 0.0 || deltaTime > 60.0) {
            throw new IllegalArgumentException("deltaTime must be in (0, 60] seconds");
        }
        if (timeAcceleration < 1.0 || timeAcceleration > 500.0) {
            throw new IllegalArgumentException("timeAcceleration must be in [1.0, 500.0]");
        }
    }
}
