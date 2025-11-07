package com.planetmayo.usvsim.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Platform state snapshot for REST API.
 *
 * Immutable DTO matching OpenAPI PlatformState schema.
 */
public record PlatformStateDTO(
    @JsonProperty("platformId") String platformId,
    @JsonProperty("position") PositionDTO position,
    @JsonProperty("heading") double heading,
    @JsonProperty("speed") double speed,
    @JsonProperty("depth") double depth,
    @JsonProperty("timestamp") String timestamp  // ISO 8601 format
) {
    @JsonCreator
    public PlatformStateDTO {
        if (platformId == null || platformId.isBlank()) {
            throw new IllegalArgumentException("platformId cannot be null or blank");
        }
        if (position == null) {
            throw new IllegalArgumentException("position cannot be null");
        }
        if (heading < 0.0 || heading >= 360.0) {
            throw new IllegalArgumentException("heading must be in [0, 360)");
        }
        if (speed < 0.0) {
            throw new IllegalArgumentException("speed must be >= 0");
        }
    }
}
