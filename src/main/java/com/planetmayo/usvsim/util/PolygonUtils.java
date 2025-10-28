package com.planetmayo.usvsim.util;

import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import org.locationtech.jts.geom.*;

import java.util.List;

/**
 * Utility class for polygon operations using JTS.
 *
 * Provides validation, area calculation, point-in-polygon tests,
 * and geometric operations.
 */
public final class PolygonUtils {
    private static final GeometryFactory gf = new GeometryFactory();

    private PolygonUtils() {
        // Utility class - no instantiation
    }

    /**
     * Validates if positions form a valid polygon.
     *
     * @param vertices ordered vertices
     * @return true if >= 3 vertices and forms valid polygon
     */
    public static boolean isValidPolygon(List<Position> vertices) {
        if (vertices == null || vertices.size() < 3) {
            return false;
        }

        try {
            Coordinate[] coords = new Coordinate[vertices.size() + 1];
            for (int i = 0; i < vertices.size(); i++) {
                coords[i] = new Coordinate(
                    vertices.get(i).getLongitude(),
                    vertices.get(i).getLatitude()
                );
            }
            coords[vertices.size()] = coords[0]; // Close ring
            LinearRing ring = gf.createLinearRing(coords);
            org.locationtech.jts.geom.Polygon poly = gf.createPolygon(ring);
            return poly.isValid();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Calculates centroid of polygon.
     *
     * @param polygon the polygon
     * @return centroid position
     */
    public static Position calculateCentroid(Polygon polygon) {
        Point centroid = polygon.getJTSPolygon().getCentroid();
        return Position.of(centroid.getY(), centroid.getX());
    }

    /**
     * Calculates area in square metres.
     *
     * @param polygon the polygon
     * @return area in square metres
     */
    public static double calculateArea(Polygon polygon) {
        // JTS area is in degrees squared, convert to approximate metres squared
        double areaDegreesSquared = polygon.getJTSPolygon().getArea();

        // Rough conversion: 1 degree ≈ 111,195 metres
        // Area ≈ (111195 m/degree)^2 × areaDegreesSquared
        double degreeToMetres = 111195.0;
        return areaDegreesSquared * degreeToMetres * degreeToMetres;
    }

    /**
     * Tests if polygon contains a point (boundary inclusive).
     *
     * @param polygon the polygon
     * @param point the point to test
     * @return true if point is inside or on boundary
     */
    public static boolean containsPoint(Polygon polygon, Position point) {
        Point jtsPoint = gf.createPoint(
            new Coordinate(point.getLongitude(), point.getLatitude())
        );
        org.locationtech.jts.geom.Polygon jtsPolygon = polygon.getJTSPolygon();
        return jtsPolygon.contains(jtsPoint) || jtsPolygon.touches(jtsPoint);
    }

    /**
     * Gets minimum bounding rectangle (axis-aligned).
     *
     * @param polygon the polygon
     * @return bounding box polygon (4 vertices)
     */
    public static Polygon getMinimumBoundingBox(Polygon polygon) {
        Envelope envelope = polygon.getJTSPolygon().getEnvelopeInternal();

        List<Position> bboxVertices = List.of(
            Position.of(envelope.getMinY(), envelope.getMinX()),
            Position.of(envelope.getMaxY(), envelope.getMinX()),
            Position.of(envelope.getMaxY(), envelope.getMaxX()),
            Position.of(envelope.getMinY(), envelope.getMaxX())
        );

        return new Polygon(bboxVertices);
    }

    /**
     * Gets perimeter of polygon in metres.
     *
     * @param polygon the polygon
     * @return perimeter in metres
     */
    public static double calculatePerimeter(Polygon polygon) {
        // JTS perimeter is in degrees, convert to approximate metres
        double perimeterDegrees = polygon.getJTSPolygon().getLength();
        double degreeToMetres = 111195.0; // Approximate metres per degree
        return perimeterDegrees * degreeToMetres;
    }
}
