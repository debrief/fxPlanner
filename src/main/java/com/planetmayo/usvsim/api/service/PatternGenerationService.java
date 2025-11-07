package com.planetmayo.usvsim.api.service;

import com.planetmayo.usvsim.api.dto.*;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.geometry.Waypoint;
import com.planetmayo.usvsim.util.SearchPatternGenerator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for generating search patterns (parallel track, expanding square).
 *
 * Stateless service using existing SearchPatternGenerator utility.
 */
@Service
public class PatternGenerationService {

    /**
     * Generate parallel track search pattern.
     *
     * @param request contains search area, track orientation, spacing, speed
     * @return generated waypoints with estimated duration
     */
    public PatternResponse generateParallelTrack(ParallelTrackRequest request) {
        // Convert DTO to domain models
        Polygon searchArea = toPolygon(request.searchArea());

        // Generate pattern
        List<Waypoint> waypoints = SearchPatternGenerator.generateParallelTracks(
            searchArea,
            request.trackOrientation(),
            request.trackSpacing(),
            request.platformSpeed()
        );

        // Calculate estimated duration
        double estimatedDuration = calculateEstimatedDuration(waypoints, request.platformSpeed());

        // Convert to DTOs
        return new PatternResponse(
            waypoints.stream().map(this::toWaypointDTO).collect(Collectors.toList()),
            estimatedDuration
        );
    }

    /**
     * Generate expanding square search pattern.
     *
     * @param request contains search area, initial direction, leg increment, speed
     * @return generated waypoints with estimated duration
     */
    public PatternResponse generateExpandingSquare(ExpandingSquareRequest request) {
        // Convert DTO to domain models
        Polygon searchArea = toPolygon(request.searchArea());

        // Generate pattern
        List<Waypoint> waypoints = SearchPatternGenerator.generateExpandingSquare(
            searchArea,
            request.initialDirection(),
            request.legIncrement(),
            request.platformSpeed()
        );

        // Calculate estimated duration
        double estimatedDuration = calculateEstimatedDuration(waypoints, request.platformSpeed());

        // Convert to DTOs
        return new PatternResponse(
            waypoints.stream().map(this::toWaypointDTO).collect(Collectors.toList()),
            estimatedDuration
        );
    }

    /**
     * Convert PolygonDTO to domain Polygon.
     */
    private Polygon toPolygon(PolygonDTO dto) {
        List<Position> vertices = dto.vertices().stream()
            .map(p -> Position.of(p.latitude(), p.longitude()))
            .collect(Collectors.toList());
        return new Polygon(vertices);
    }

    /**
     * Convert domain Waypoint to DTO.
     */
    private WaypointDTO toWaypointDTO(Waypoint waypoint) {
        // Map domain WaypointType to DTO string
        String dtoType;
        if (waypoint.getType() == com.planetmayo.usvsim.model.geometry.WaypointType.BASE) {
            dtoType = "BASE";
        } else if (waypoint.getType() == com.planetmayo.usvsim.model.geometry.WaypointType.TURN) {
            dtoType = "TURN_POINT";
        } else {
            dtoType = "NORMAL"; // TRANSIT or SEARCH
        }

        return new WaypointDTO(
            new PositionDTO(
                waypoint.getPosition().getLatitude(),
                waypoint.getPosition().getLongitude()
            ),
            waypoint.getSpeed(),
            waypoint.getAcceptanceRadius(),
            dtoType
        );
    }

    /**
     * Calculate estimated mission duration based on waypoint distances and speed.
     */
    private double calculateEstimatedDuration(List<Waypoint> waypoints, double platformSpeed) {
        if (waypoints.isEmpty()) {
            return 0.0;
        }

        double totalDistance = 0.0;
        for (int i = 1; i < waypoints.size(); i++) {
            Position prev = waypoints.get(i - 1).getPosition();
            Position curr = waypoints.get(i).getPosition();
            totalDistance += prev.distanceTo(curr);
        }

        // Duration = distance / speed (convert speed to m/s if needed)
        return totalDistance / platformSpeed;  // returns seconds
    }
}
