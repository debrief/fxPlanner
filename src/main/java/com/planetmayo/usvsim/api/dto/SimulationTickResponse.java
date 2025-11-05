package com.planetmayo.usvsim.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response from single simulation tick execution.
 *
 * Contains updated platform state, behavior state, and completion flags.
 */
public record SimulationTickResponse(
    @JsonProperty("platformState") PlatformStateDTO platformState,
    @JsonProperty("behaviorState") BehaviorExecutionStateDTO behaviorState,
    @JsonProperty("demand") PlatformDemandDTO demand,
    @JsonProperty("behaviorComplete") boolean behaviorComplete,
    @JsonProperty("missionComplete") boolean missionComplete
) {
    @JsonCreator
    public SimulationTickResponse {
        if (platformState == null) {
            throw new IllegalArgumentException("platformState cannot be null");
        }
        if (behaviorState == null) {
            throw new IllegalArgumentException("behaviorState cannot be null");
        }
        if (demand == null) {
            throw new IllegalArgumentException("demand cannot be null");
        }
    }
}
