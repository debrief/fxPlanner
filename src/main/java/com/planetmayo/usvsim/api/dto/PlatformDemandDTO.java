package com.planetmayo.usvsim.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Platform demand (desired heading, speed, depth) for REST API.
 *
 * Immutable DTO matching OpenAPI PlatformDemand schema.
 */
public record PlatformDemandDTO(
    @JsonProperty("demandedHeading") double demandedHeading,
    @JsonProperty("demandedSpeed") double demandedSpeed,
    @JsonProperty("demandedDepth") double demandedDepth,
    @JsonProperty("turnDirection") String turnDirection  // SHORTEST, PORT, STARBOARD
) {
    @JsonCreator
    public PlatformDemandDTO {
        if (demandedHeading < 0.0 || demandedHeading >= 360.0) {
            throw new IllegalArgumentException("demandedHeading must be in [0, 360)");
        }
        if (demandedSpeed < 0.0) {
            throw new IllegalArgumentException("demandedSpeed must be >= 0");
        }
        if (turnDirection == null || (!turnDirection.equals("SHORTEST") &&
            !turnDirection.equals("PORT") && !turnDirection.equals("STARBOARD"))) {
            throw new IllegalArgumentException("turnDirection must be SHORTEST, PORT, or STARBOARD");
        }
    }
}
