package com.planetmayo.usvsim.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Waypoint with position, speed, and acceptance radius for REST API.
 *
 * Immutable DTO matching OpenAPI Waypoint schema.
 */
public record WaypointDTO(
    @JsonProperty("position") PositionDTO position,
    @JsonProperty("speed") double speed,  // m/s
    @JsonProperty("acceptanceRadius") double acceptanceRadius,  // meters
    @JsonProperty("type") String type  // NORMAL, BASE, TURN_POINT
) {
    @JsonCreator
    public WaypointDTO {
        if (position == null) {
            throw new IllegalArgumentException("position cannot be null");
        }
        if (speed < 0.0) {
            throw new IllegalArgumentException("speed must be >= 0");
        }
        if (acceptanceRadius <= 0.0) {
            throw new IllegalArgumentException("acceptanceRadius must be > 0");
        }
        if (type == null || (!type.equals("NORMAL") && !type.equals("BASE") && !type.equals("TURN_POINT"))) {
            throw new IllegalArgumentException("type must be NORMAL, BASE, or TURN_POINT");
        }
    }
}
