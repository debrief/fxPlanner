package com.planetmayo.usvsim.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response containing generated waypoints for a search pattern.
 */
public record PatternResponse(
    @JsonProperty("waypoints") List<WaypointDTO> waypoints,
    @JsonProperty("estimatedDuration") double estimatedDuration  // seconds
) {
    @JsonCreator
    public PatternResponse {
        if (waypoints == null || waypoints.isEmpty()) {
            throw new IllegalArgumentException("waypoints cannot be null or empty");
        }
        if (estimatedDuration < 0.0) {
            throw new IllegalArgumentException("estimatedDuration must be >= 0");
        }
    }
}
