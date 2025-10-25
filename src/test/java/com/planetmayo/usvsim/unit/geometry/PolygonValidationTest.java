package com.planetmayo.usvsim.unit.geometry;

import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Polygon validation logic.
 *
 * Verifies that:
 * - Valid polygons are accepted
 * - Invalid polygons are rejected with clear error messages
 * - Polygon constructor properly handles ring closure
 */
class PolygonValidationTest {

    @Test
    void testValidSimpleSquare() {
        // Simple square - should be valid
        List<Position> vertices = List.of(
            Position.of(50.0, -2.0),
            Position.of(50.0, -1.9),
            Position.of(50.1, -1.9),
            Position.of(50.1, -2.0)
        );

        Polygon polygon = new Polygon(vertices);
        assertTrue(polygon.isValid());
        assertEquals(4, polygon.getVertices().size());
    }

    @Test
    void testValidTriangle() {
        // Minimum valid polygon - triangle
        List<Position> vertices = List.of(
            Position.of(50.0, -2.0),
            Position.of(50.1, -2.0),
            Position.of(50.05, -1.9)
        );

        Polygon polygon = new Polygon(vertices);
        assertTrue(polygon.isValid());
        assertEquals(3, polygon.getVertices().size());
    }

    @Test
    void testPolygonWithDuplicateClosingPoint() {
        // Polygon with duplicate closing point (should be handled gracefully)
        // This simulates what might come from user drawing
        List<Position> vertices = List.of(
            Position.of(50.0, -2.0),
            Position.of(50.0, -1.9),
            Position.of(50.1, -1.9),
            Position.of(50.1, -2.0),
            Position.of(50.0, -2.0)  // Duplicate closing point
        );

        // Should create valid polygon (JTS handles the duplicate)
        Polygon polygon = new Polygon(vertices);
        assertTrue(polygon.isValid());
    }

    @Test
    void testInvalidTooFewVertices() {
        // Only 2 vertices - invalid
        List<Position> vertices = List.of(
            Position.of(50.0, -2.0),
            Position.of(50.1, -2.0)
        );

        assertThrows(IllegalArgumentException.class, () -> new Polygon(vertices),
            "Polygon with < 3 vertices should throw exception");
    }

    @Test
    void testSelfIntersectingPolygon() {
        // Bowtie/figure-8 shape - self-intersecting
        List<Position> vertices = List.of(
            Position.of(50.0, -2.0),
            Position.of(50.1, -1.9),  // Cross to opposite corner
            Position.of(50.0, -1.9),  // This creates intersection
            Position.of(50.1, -2.0)
        );

        assertThrows(IllegalArgumentException.class, () -> new Polygon(vertices),
            "Self-intersecting polygon should throw exception");
    }

    @Test
    void testComplexValidPolygon() {
        // L-shaped polygon - valid but complex
        List<Position> vertices = List.of(
            Position.of(50.0, -2.0),
            Position.of(50.0, -1.9),
            Position.of(50.05, -1.9),
            Position.of(50.05, -1.95),
            Position.of(50.1, -1.95),
            Position.of(50.1, -2.0)
        );

        Polygon polygon = new Polygon(vertices);
        assertTrue(polygon.isValid());
        assertEquals(6, polygon.getVertices().size());
    }

    @Test
    void testPolygonCentroid() {
        // Square centered at origin (for easy centroid verification)
        List<Position> vertices = List.of(
            Position.of(49.9, -2.1),
            Position.of(49.9, -1.9),
            Position.of(50.1, -1.9),
            Position.of(50.1, -2.1)
        );

        Polygon polygon = new Polygon(vertices);
        Position centroid = polygon.centroid();

        // Centroid should be approximately at center
        assertEquals(50.0, centroid.getLatitude(), 0.01);
        assertEquals(-2.0, centroid.getLongitude(), 0.01);
    }

    @Test
    void testPolygonContains() {
        // Square for containment testing
        List<Position> vertices = List.of(
            Position.of(50.0, -2.0),
            Position.of(50.0, -1.9),
            Position.of(50.1, -1.9),
            Position.of(50.1, -2.0)
        );

        Polygon polygon = new Polygon(vertices);

        // Point inside
        assertTrue(polygon.contains(Position.of(50.05, -1.95)),
            "Point inside polygon should be contained");

        // Point outside
        assertFalse(polygon.contains(Position.of(50.2, -1.95)),
            "Point outside polygon should not be contained");

        // Point on edge (implementation-dependent, but typically excluded)
        assertFalse(polygon.contains(Position.of(50.0, -1.95)),
            "Point on edge typically not contained");
    }
}
