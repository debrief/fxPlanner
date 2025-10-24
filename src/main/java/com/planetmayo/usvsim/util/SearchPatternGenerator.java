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

        // Get polygon bounds and centroid
        Envelope env = searchArea.getJTSPolygon().getEnvelopeInternal();
        double minLat = env.getMinY();
        double maxLat = env.getMaxY();
        double minLon = env.getMinX();
        double maxLon = env.getMaxX();

        // Normalize orientation to perpendicular direction for tracks
        double trackBearing = GeoUtils.normalizeAngle(trackOrientation);
        double perpBearing = GeoUtils.normalizeAngle(trackOrientation + 90);

        // Generate track lines at regular intervals
        // Calculate number of tracks needed
        Position cornerNW = Position.of(maxLat, minLon);
        Position cornerSE = Position.of(minLat, maxLon);
        double diagonalDistance = GeoUtils.distance(cornerNW, cornerSE);
        int numTracks = (int) Math.ceil(diagonalDistance / trackSpacing) + 1;

        // Generate tracks perpendicular to orientation
        List<LineString> trackLines = new ArrayList<>();
        for (int i = 0; i < numTracks; i++) {
            double offset = (i - numTracks / 2.0) * trackSpacing;

            // Create a line segment perpendicular to track bearing
            Position start = Position.of(maxLat, minLon);
            Position offsetStart = start.destination(offset, perpBearing);
            Position offsetEnd = offsetStart.destination(diagonalDistance * 2, trackBearing);

            // Create line
            Coordinate[] coords = {
                new Coordinate(offsetStart.getLongitude(), offsetStart.getLatitude()),
                new Coordinate(offsetEnd.getLongitude(), offsetEnd.getLatitude())
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

        while (legLength < 100000) { // Limit: 100km max leg
            // Four directions for square
            for (int direction = 0; direction < 4; direction++) {
                bearing = GeoUtils.normalizeAngle(bearing + 90);
                Position current = waypoints.get(waypoints.size() - 1).getPosition();
                Position next = current.destination(legLength, bearing);

                // Check if waypoint is still within reasonable bounds
                if (PolygonUtils.containsPoint(searchArea, next) ||
                    current.distanceTo(next) < legLength * 1.1) { // Allow slight overshoot
                    waypoints.add(Waypoint.search(next, speed));
                }
            }

            legLength += legIncrement;
            legCount++;
        }

        return waypoints;
    }
}
