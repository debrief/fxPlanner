package com.planetmayo.usvsim.util;

import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.geometry.Waypoint;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.AffineTransformation;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates search patterns for mission planning.
 *
 * Implements parallel track search and expanding square search algorithms
 * for systematic area coverage.
 */
public final class SearchPatternGenerator {
    private static final GeometryFactory gf = new GeometryFactory();
    private static final double DEGREES_PER_METRE = 1.0 / 111195.0;

    private SearchPatternGenerator() {
        // Utility class - no instantiation
    }

    /**
     * Generates parallel track search pattern.
     *
     * Creates alternating parallel tracks within polygon boundary for
     * systematic area search (lawn-mower pattern).
     *
     * @param searchArea polygon boundary
     * @param trackOrientation bearing of parallel tracks (degrees)
     * @param trackSpacing distance between parallel tracks (metres)
     * @param speed transit speed in knots
     * @return ordered waypoints for search pattern
     */
    public static List<Waypoint> generateParallelTracks(Polygon searchArea,
                                                        double trackOrientation,
                                                        double trackSpacing,
                                                        double speed) {
        List<Waypoint> waypoints = new ArrayList<>();

        // Get polygon centroid and bounds
        Position centroid = PolygonUtils.calculateCentroid(searchArea);
        Envelope env = searchArea.getJTSPolygon().getEnvelopeInternal();

        // Calculate diagonal distance for coverage
        Position cornerNW = Position.of(env.getMaxY(), env.getMinX());
        Position cornerSE = Position.of(env.getMinY(), env.getMaxX());
        double diagonalDistance = GeoUtils.distance(cornerNW, cornerSE);

        // Normalize orientation to perpendicular direction for tracks
        double trackBearing = GeoUtils.normalizeAngle(trackOrientation);
        double perpBearing = GeoUtils.normalizeAngle(trackOrientation + 90);

        // Calculate number of tracks needed to cover the full area
        int numTracks = (int) Math.ceil(diagonalDistance / trackSpacing) + 2;  // Extra tracks for safety

        // Generate tracks from centroid, offset perpendicular to track direction
        List<LineString> trackLines = new ArrayList<>();
        for (int i = 0; i < numTracks; i++) {
            // Offset from center in both directions
            double offset = (i - numTracks / 2.0) * trackSpacing;

            // Start from centroid, offset perpendicular to tracks
            Position trackCenter = centroid.destination(offset, perpBearing);

            // Extend line in both directions along track bearing (long enough to cover polygon)
            Position trackStart = trackCenter.destination(diagonalDistance * 1.5, trackBearing + 180);
            Position trackEnd = trackCenter.destination(diagonalDistance * 1.5, trackBearing);

            // Create line
            Coordinate[] coords = {
                new Coordinate(trackStart.getLongitude(), trackStart.getLatitude()),
                new Coordinate(trackEnd.getLongitude(), trackEnd.getLatitude())
            };
            LineString line = gf.createLineString(coords);
            trackLines.add(line);
        }

        // Clip lines to polygon and generate waypoints
        boolean reverseDirection = false;
        for (LineString trackLine : trackLines) {
            try {
                Geometry intersection = trackLine.intersection(searchArea.getJTSPolygon());
                if (intersection.isEmpty()) continue;

                // Extract waypoints from intersection
                Coordinate[] coords = intersection.getCoordinates();
                if (coords.length < 2) continue;

                // Alternate direction (lawn-mower pattern)
                if (reverseDirection) {
                    for (int i = coords.length - 1; i >= 0; i--) {
                        waypoints.add(Waypoint.search(
                            Position.of(coords[i].getY(), coords[i].getX()),
                            speed
                        ));
                    }
                } else {
                    for (Coordinate coord : coords) {
                        waypoints.add(Waypoint.search(
                            Position.of(coord.getY(), coord.getX()),
                            speed
                        ));
                    }
                }
                reverseDirection = !reverseDirection;
            } catch (Exception e) {
                // Skip tracks that fail intersection
            }
        }

        return waypoints;
    }

    /**
     * Generates expanding square search pattern.
     *
     * Creates expanding square spiral from polygon centroid for contact
     * investigation.
     *
     * @param searchArea polygon boundary
     * @param initialDirection initial bearing of first leg (degrees)
     * @param legIncrement distance increase per leg pair (metres)
     * @param speed transit speed in knots
     * @return ordered waypoints for expanding square
     */
    public static List<Waypoint> generateExpandingSquare(Polygon searchArea,
                                                          double initialDirection,
                                                          double legIncrement,
                                                          double speed) {
        List<Waypoint> waypoints = new ArrayList<>();

        // Start from polygon centroid
        Position center = PolygonUtils.calculateCentroid(searchArea);
        waypoints.add(Waypoint.search(center, speed));

        // Generate expanding square spiral
        double bearing = GeoUtils.normalizeAngle(initialDirection);
        double legLength = legIncrement;
        int legCount = 0;
        int consecutiveOutsideCount = 0; // Track consecutive waypoints outside polygon

        System.out.println("=== Generating Expanding Square Search ===");
        System.out.println("Starting from center: " + center);
        System.out.println("Initial direction: " + initialDirection + "°");
        System.out.println("Leg increment: " + legIncrement + "m");

        while (legLength < 100000) { // Limit: 100km max leg
            int waypointsAddedThisSquare = 0;

            System.out.println("--- Square " + (legCount/4 + 1) + ": leg length = " + String.format("%.0f", legLength) + "m ---");

            // Four directions for square (all 4 legs use same length)
            for (int direction = 0; direction < 4; direction++) {
                bearing = GeoUtils.normalizeAngle(bearing + 90);
                Position current = waypoints.get(waypoints.size() - 1).getPosition();
                Position next = current.destination(legLength, bearing);

                // Check if waypoint is within polygon bounds
                if (PolygonUtils.containsPoint(searchArea, next)) {
                    waypoints.add(Waypoint.search(next, speed));
                    waypointsAddedThisSquare++;
                    consecutiveOutsideCount = 0; // Reset counter
                    System.out.println("WP " + (waypoints.size()-1) + ": bearing=" + String.format("%.0f", bearing) +
                                     "° leg=" + String.format("%.0f", legLength) + "m pos=" +
                                     String.format("%.5f,%.5f", next.getLatitude(), next.getLongitude()));
                } else {
                    consecutiveOutsideCount++;
                    System.out.println("SKIP: bearing=" + String.format("%.0f", bearing) +
                                     "° leg=" + String.format("%.0f", legLength) + "m (outside polygon)");
                    // Stop if we've had 4+ consecutive waypoints outside (full square outside)
                    if (consecutiveOutsideCount >= 4) {
                        System.out.println("=== Terminating: 4+ consecutive outside ===");
                        System.out.println("Total waypoints: " + waypoints.size());
                        return waypoints;
                    }
                }

                legCount++;
            }

            // Increment leg length AFTER completing a full square (4 legs)
            // Square 1: legIncrement
            // Square 2: legIncrement + 2*legIncrement = 3*legIncrement
            // Square 3: 3*legIncrement + 2*legIncrement = 5*legIncrement
            // Pattern: add 2*legIncrement after each complete square
            legLength += 2 * legIncrement;

            // Stop if no waypoints were added in this complete square
            if (waypointsAddedThisSquare == 0) {
                System.out.println("=== Terminating: no waypoints in last square ===");
                System.out.println("Total waypoints: " + waypoints.size());
                break;
            }
        }

        System.out.println("=== Pattern generation complete ===");
        System.out.println("Total waypoints: " + waypoints.size());

        // Check for duplicate positions
        for (int i = 1; i < waypoints.size(); i++) {
            Position prev = waypoints.get(i-1).getPosition();
            Position curr = waypoints.get(i).getPosition();
            double dist = prev.distanceTo(curr);
            if (dist < 1.0) {
                System.out.println("WARNING: Duplicate/near-duplicate waypoints at index " + (i-1) + " and " + i + " (dist=" + dist + "m)");
            }
        }

        return waypoints;
    }
}
