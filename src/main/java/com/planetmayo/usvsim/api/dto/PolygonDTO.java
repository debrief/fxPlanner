package com.planetmayo.usvsim.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Polygon (list of positions) for REST API.
 *
 * Immutable DTO matching OpenAPI Polygon schema.
 */
public record PolygonDTO(
    @JsonProperty("vertices") List<PositionDTO> vertices,
    @JsonProperty("isClosed") boolean isClosed
) {
    @JsonCreator
    public PolygonDTO {
        if (vertices == null || vertices.size() < 3) {
            throw new IllegalArgumentException("Polygon requires minimum 3 vertices");
        }
    }
}
