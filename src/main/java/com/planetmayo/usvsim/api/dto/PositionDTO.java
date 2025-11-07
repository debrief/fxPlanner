package com.planetmayo.usvsim.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Geographic position (latitude, longitude) for REST API.
 *
 * Immutable DTO matching OpenAPI Position schema.
 */
public record PositionDTO(
    @JsonProperty("latitude") double latitude,
    @JsonProperty("longitude") double longitude
) {
    @JsonCreator
    public PositionDTO {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be in [-90, 90]");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be in [-180, 180]");
        }
    }
}
