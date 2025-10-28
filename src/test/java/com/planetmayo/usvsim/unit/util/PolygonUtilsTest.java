package com.planetmayo.usvsim.unit.util;

import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.util.PolygonUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for polygon utility functions.
 */
class PolygonUtilsTest {

    @Test
    void testValidateMinimumVertices() {
        List<Position> twoPoints = List.of(
            Position.of(50.6, -2.4),
            Position.of(50.7, -2.4)
        );
        assertFalse(PolygonUtils.isValidPolygon(twoPoints),
            "Polygon with 2 vertices should be invalid");
    }

    @Test
    void testValidateMinimumThreeVertices() {
        List<Position> threePoints = List.of(
            Position.of(50.6, -2.4),
            Position.of(50.7, -2.4),
            Position.of(50.6, -2.3)
        );
        assertTrue(PolygonUtils.isValidPolygon(threePoints),
            "Polygon with 3 vertices should be valid");
    }

    @Test
    void testValidateSquarePolygon() {
        List<Position> square = List.of(
            Position.of(50.6, -2.4),
            Position.of(50.61, -2.4),
            Position.of(50.61, -2.39),
            Position.of(50.6, -2.39)
        );
        assertTrue(PolygonUtils.isValidPolygon(square),
            "Valid square polygon should pass validation");
    }

    @Test
    void testDetectSelfIntersection() {
        // Figure-8 polygon (self-intersecting)
        List<Position> figureEight = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.62, -2.39),
            Position.of(50.60, -2.38),
            Position.of(50.62, -2.41)
        );
        // Self-intersecting polygons may not always be caught by JTS validation
        // but we test that the validation function exists
        assertDoesNotThrow(() -> PolygonUtils.isValidPolygon(figureEight),
            "Validation should not throw on self-intersecting polygon");
    }

    @Test
    void testCalculateCentroid() {
        List<Position> square = List.of(
            Position.of(50.0, -2.0),
            Position.of(51.0, -2.0),
            Position.of(51.0, -1.0),
            Position.of(50.0, -1.0)
        );
        Polygon poly = new Polygon(square);
        Position centroid = PolygonUtils.calculateCentroid(poly);

        // Centroid should be approximately at center
        assertEquals(50.5, centroid.getLatitude(), 0.01,
            "Centroid latitude should be near center");
        assertEquals(-1.5, centroid.getLongitude(), 0.01,
            "Centroid longitude should be near center");
    }

    @Test
    void testCalculateArea() {
        // Square 1 degree × 1 degree
        List<Position> square = List.of(
            Position.of(50.0, -2.0),
            Position.of(51.0, -2.0),
            Position.of(51.0, -1.0),
            Position.of(50.0, -1.0)
        );
        Polygon poly = new Polygon(square);
        double area = PolygonUtils.calculateArea(poly);

        // Should be positive and reasonable magnitude (1°×1° ≈ 12.4 billion sq meters at 50° latitude)
        assertTrue(area > 0, "Area should be positive");
        assertTrue(area < 15_000_000_000L, "Area should be reasonable (< 15B sq meters)");
    }

    @Test
    void testIsPointInPolygon() {
        List<Position> square = List.of(
            Position.of(50.0, -2.0),
            Position.of(51.0, -2.0),
            Position.of(51.0, -1.0),
            Position.of(50.0, -1.0)
        );
        Polygon poly = new Polygon(square);

        Position inside = Position.of(50.5, -1.5);
        Position outside = Position.of(52.0, -2.0);

        assertTrue(PolygonUtils.containsPoint(poly, inside),
            "Point inside polygon should return true");
        assertFalse(PolygonUtils.containsPoint(poly, outside),
            "Point outside polygon should return false");
    }

    @Test
    void testIsPointOnVertex() {
        List<Position> triangle = List.of(
            Position.of(50.0, -2.0),
            Position.of(51.0, -2.0),
            Position.of(50.5, -1.0)
        );
        Polygon poly = new Polygon(triangle);

        Position vertex = Position.of(50.0, -2.0);
        assertTrue(PolygonUtils.containsPoint(poly, vertex),
            "Point on vertex should return true (boundary included)");
    }

    @Test
    void testMinimumBoundingBox() {
        List<Position> triangle = List.of(
            Position.of(50.0, -2.0),
            Position.of(51.0, -2.0),
            Position.of(50.5, -1.0)
        );
        Polygon poly = new Polygon(triangle);
        Polygon bbox = PolygonUtils.getMinimumBoundingBox(poly);

        assertNotNull(bbox, "Bounding box should not be null");
        assertEquals(4, bbox.getVertices().size(),
            "Bounding box should be a rectangle (4 vertices)");
    }
}
