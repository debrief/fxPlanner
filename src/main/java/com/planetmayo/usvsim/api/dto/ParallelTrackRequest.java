package com.planetmayo.usvsim.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request for parallel track search pattern generation.
 */
public record ParallelTrackRequest(
    @JsonProperty("searchArea") PolygonDTO searchArea,
    @JsonProperty("trackOrientation") double trackOrientation,  // degrees 0-360
    @JsonProperty("trackSpacing") double trackSpacing,  // meters
    @JsonProperty("platformSpeed") double platformSpeed  // m/s
) {
    @JsonCreator
    public ParallelTrackRequest {
        if (searchArea == null) {
            throw new IllegalArgumentException("searchArea cannot be null");
        }
        if (trackOrientation < 0.0 || trackOrientation >= 360.0) {
            throw new IllegalArgumentException("trackOrientation must be in [0, 360)");
        }
        if (trackSpacing <= 0.0) {
            throw new IllegalArgumentException("trackSpacing must be > 0");
        }
        if (platformSpeed <= 0.0) {
            throw new IllegalArgumentException("platformSpeed must be > 0");
        }
    }
}
