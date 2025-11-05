package com.planetmayo.usvsim.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request for expanding square search pattern generation.
 */
public record ExpandingSquareRequest(
    @JsonProperty("searchArea") PolygonDTO searchArea,
    @JsonProperty("initialDirection") double initialDirection,  // degrees 0-360
    @JsonProperty("legIncrement") double legIncrement,  // meters
    @JsonProperty("platformSpeed") double platformSpeed  // m/s
) {
    @JsonCreator
    public ExpandingSquareRequest {
        if (searchArea == null) {
            throw new IllegalArgumentException("searchArea cannot be null");
        }
        if (initialDirection < 0.0 || initialDirection >= 360.0) {
            throw new IllegalArgumentException("initialDirection must be in [0, 360)");
        }
        if (legIncrement <= 0.0) {
            throw new IllegalArgumentException("legIncrement must be > 0");
        }
        if (platformSpeed <= 0.0) {
            throw new IllegalArgumentException("platformSpeed must be > 0");
        }
    }
}
